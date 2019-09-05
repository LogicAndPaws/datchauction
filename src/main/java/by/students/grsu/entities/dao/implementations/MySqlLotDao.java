package by.students.grsu.entities.dao.implementations;

import by.students.grsu.entities.dao.interfaces.LotDao;
import by.students.grsu.entities.item.Item;
import by.students.grsu.entities.lot.Lot;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySqlLotDao implements LotDao {
    private JdbcTemplate template;
    private ResultSetExtractor<Lot> lotWithoutItemsExtractor = new ResultSetExtractor<Lot>() {
        public Lot extractData(ResultSet rs) throws SQLException, DataAccessException {
            return rs.next() ? new Lot(rs.getInt("id"), rs.getString("lotName"), rs.getDouble("startPrice"), rs.getDouble("minPrice"), rs.getString("status"), rs.getInt("auctionId")) : null;
        }
    };
    private ResultSetExtractor<List<Lot>> lotListWithItemsExtractor = new ResultSetExtractor<List<Lot>>() {
        public List<Lot> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<Lot> lots = new ArrayList<>();
            int lotId = 0;
            Lot newLot = null;
            ArrayList items;
            for(items = null; rs.next(); items.add(new Item(rs.getInt("items.id"), rs.getString("name"), rs.getString("description"), rs.getString("owner"), rs.getInt("lotId")))) {
                if (rs.getInt("lots.id") != lotId) {
                    if (newLot != null) {
                        newLot.setItems(items);
                        lots.add(newLot);
                    }

                    newLot = new Lot(rs.getInt("lots.id"), rs.getString("lotName"), rs.getDouble("startPrice"), rs.getDouble("minPrice"), rs.getString("status"), rs.getInt("auctionId"));
                    items = new ArrayList();
                    lotId = rs.getInt("lots.id");
                }
            }
            if (newLot != null) {
                newLot.setItems(items);
                lots.add(newLot);
            }
            return lots;
        }
    };
    private ResultSetExtractor<List<Lot>> lotListWithoutItemsExtractor = new ResultSetExtractor<List<Lot>>() {
        public List<Lot> extractData(ResultSet rs) throws SQLException, DataAccessException {
            ArrayList lots = new ArrayList();

            while(rs.next()) {
                lots.add(new Lot(rs.getInt("lots.id"), rs.getString("lotName"), rs.getDouble("startPrice"), rs.getDouble("minPrice"), rs.getString("status"), rs.getInt("auctionId")));
            }

            return lots;
        }
    };
    private ResultSetExtractor<List<Integer>> lotsIdList = new ResultSetExtractor<List<Integer>>() {
        public List<Integer> extractData(ResultSet rs) throws SQLException, DataAccessException {
            ArrayList lotsIndexes = new ArrayList();

            while(rs.next()) {
                lotsIndexes.add(rs.getInt("id"));
            }

            return lotsIndexes;
        }
    };

    public MySqlLotDao(JdbcTemplate template) {
        this.template = template;
    }

    public synchronized int addLot(String name, double startPrice, double minPrice, String status, int auctionId) throws Exception {
        template.update("INSERT INTO lots VALUES(NULL, '" + name + "', " + startPrice + ", " + minPrice + ", '" + status.toLowerCase() + "', " + auctionId + ")");
        return template.queryForObject("SELECT MAX(id) FROM lots", Integer.class);
    }

    public void deleteLot(int id) throws Exception {
        template.execute("DELETE FROM lots WHERE id=" + id);
    }

//    public void deleteLotsByAuction(int auctionId) {
//        template.update("UPDATE lots SET lotName='<empty>',startPrice=0,minPrice=0,status='<empty>',auctionId=0 WHERE auctionId=" + auctionId);
//    }

    public void setStatusByLotId(int id, String status) throws Exception {
        if (template.update("UPDATE lots SET status='" + status + "', auctionId=0 WHERE id=" + id) == 0) {
            throw new Exception("Lot not found");
        }
    }

    public void setEndByAuctionId(int auctionId) {
        template.update("UPDATE lots SET status='end' WHERE auctionId=" + auctionId + " and status='registered'");
    }

    public Lot getLotById(int id) throws Exception {
        Lot lot = template.query("SELECT * FROM lots WHERE id=" + id, lotWithoutItemsExtractor);
        if (lot == null) {
            throw new Exception("Lot not found");
        } else {
            return lot;
        }
    }

    public List<Lot> getLotsByAuctionId(int auctionId) {
        return template.query("SELECT * FROM lots LEFT OUTER JOIN items ON lots.id=items.lotId WHERE auctionId=" + auctionId, lotListWithItemsExtractor);
    }

    public List<Lot> getAllLots() {
        return template.query("SELECT * FROM lots ORDER BY ID", lotListWithoutItemsExtractor);
    }

    public List<Lot> getLotsBySearch(String substr) {
        //return template.query("SELECT * FROM lots LEFT OUTER JOIN items ON lots.id=items.lotId WHERE MATCH (lotName) AGAINST ('" + substr + "') ORDER BY id", lotListWithItemsExtractor);
        return template.query("SELECT * FROM lots WHERE MATCH (lotName) AGAINST ('" + substr + "') ORDER BY id", lotListWithoutItemsExtractor);
    }

    public List<Integer> deleteEndedLots() {
        List<Integer> lotsIndexes = template.query("SELECT id FROM lots WHERE status='end'", lotsIdList);
        template.execute("DELETE FROM lots WHERE status='end'");
        return lotsIndexes;
    }

    public List<Lot> getRegisteredLots() {
        return template.query("SELECT * FROM lots LEFT OUTER JOIN items ON lots.id=items.lotId WHERE status='registered'", lotListWithItemsExtractor);
    }
}