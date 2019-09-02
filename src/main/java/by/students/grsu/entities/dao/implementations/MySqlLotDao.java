package by.students.grsu.entities.dao.implementations;

import by.students.grsu.entities.dao.interfaces.ItemDao;
import by.students.grsu.entities.dao.interfaces.LotDao;
import by.students.grsu.entities.item.Item;
import by.students.grsu.entities.lot.Lot;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MySqlLotDao implements LotDao {
    private JdbcTemplate jdbcTemplate;
    private ItemDao itemDao;
    public MySqlLotDao(JdbcTemplate jdbcTemplate, ItemDao itemDao){
        this.jdbcTemplate = jdbcTemplate;
        this.itemDao=itemDao;
    }
//    public int addLot(String name,double startPrice,double minPrice,String status,int auctionId) throws Exception {
//        try {
//            ResultSet rs = st.executeQuery("SELECT * FROM lots WHERE name=\'<empty>\'");
//            int id;
//            if(rs.next()){
//                id = rs.getInt("id");
//                st.execute("UPDATE lots SET lotName=\'" + name + "\',startPrice=" + startPrice + ",minPrice=" + minPrice + ",status=\'" + status.toLowerCase() + "\',auctionId=" + auctionId + " WHERE id="+id);
//                //st.execute("INSERT INTO lots VALUES("+id+", \'"+name+"\', "+startPrice+", "+minPrice+", \'"+status.toLowerCase()+"\', "+auctionId+")");
//            }
//            else{
//                rs = st.executeQuery("SELECT MAX(id) FROM lots");
//                rs.next();
//                id = rs.getInt("MAX(id)") + 1;
//                st.execute("INSERT INTO lots VALUES("+(id)+", \'"+name+"\', "+startPrice+", "+minPrice+", \'"+status.toLowerCase()+"\', "+auctionId+")");
//            }
//            return id;
//        } catch (SQLException e) {
//            throw new Exception(e.getMessage());
//        }
//    }
    public int addLot(String name,double startPrice,double minPrice,String status,int auctionId) throws Exception {
        synchronized (name) {
            try {
                ResultSet rs = st.executeQuery("SELECT id FROM lots ORDER BY id");
                int id = 1;
                if (rs.next()) {
                    do {
                        if (rs.getInt("id") != id)
                            break;
                        else id++;
                    } while (rs.next());
                    st.execute("INSERT INTO lots VALUES(" + id + ", \'" + name + "\', " + startPrice + ", " + minPrice + ", \'" + status.toLowerCase() + "\', " + auctionId + ")");
                } else {
                    st.execute("INSERT INTO lots VALUES(1, \'" + name + "\', " + startPrice + ", " + minPrice + ", \'" + status.toLowerCase() + "\', " + auctionId + ")");
                }
                return id;
            } catch (SQLException e) {
                throw new Exception(e.getMessage());
            }
        }
}
    public void deleteLot(int id) throws Exception {
        synchronized (new Integer(3)) {
            try {
                ResultSet rs = st.executeQuery("SELECT * FROM lots WHERE id=" + id);
                if (rs.next()) {
                    st.execute("DELETE FROM lots WHERE id=" + id);
                } else throw new Exception("Lot not found");
            } catch (SQLException e) {
                throw new Exception(e.getMessage());
            }
        }
    }
    public void deleteLotsByAuction(int auctionId) throws Exception {
        synchronized (new Integer(3)) {
            try {
                ResultSet rs = st.executeQuery("SELECT * FROM lots WHERE auctionId=" + auctionId);
                if (rs.next()) {
                    st.execute("DELETE FROM lots WHERE auctionId=" + auctionId);
                } else throw new Exception("No lots found");
            } catch (SQLException e) {
                throw new Exception(e.getMessage());
            }
        }
    }
    public void setStatusByLotId(int id,String status) throws Exception {
        synchronized (status) {
            try {
                ResultSet rs = st.executeQuery("SELECT * FROM lots WHERE id=" + id);
                if (rs.next()) {
                    st.execute("UPDATE lots SET status=\'" + status + "\', auctionId=0 WHERE id=" + id);
                } else throw new Exception("Lot not found");
            } catch (SQLException e) {
                throw new Exception(e.getMessage());
            }
        }
    }
//    public void setStatusByAuctionId(int auctionId,String status) throws Exception {
//        try {
//            ResultSet rs = st.executeQuery("SELECT * FROM lots WHERE auctionId="+auctionId);
//            if(rs.next()){
//                st.execute("UPDATE lots SET status=\'"+status.toLowerCase()+"\' WHERE auctionId="+auctionId);
//            }
//            else throw new Exception("Lots not found");
//        } catch (SQLException e) {
//            throw new Exception(e.getMessage());
//        }
//    }
    public void setEndByAuctionId(int auctionId) throws Exception {
        synchronized (new Integer(3)) {
            try {
                ResultSet rs = st.executeQuery("SELECT * FROM lots WHERE auctionId=" + auctionId);
                st.execute("UPDATE lots SET status=\'end\' WHERE auctionId=" + auctionId + " and status=\'registered\'");
            } catch (SQLException e) {
                throw new Exception(e.getMessage());
            }
        }
}
    public Lot getLotById(int id) throws Exception {
        try {
            ResultSet rs = st.executeQuery("SELECT * FROM lots WHERE id="+id);
            if(rs.next()){
                return new Lot(rs.getInt("id"),rs.getString("lotName"),
                        rs.getDouble("startPrice"),rs.getDouble("minPrice"),
                        rs.getString("status"),rs.getInt("auctionId"));
            }
            else throw new Exception("Lot not found");
        } catch (SQLException e) {
            throw new Exception(e.getMessage());
        }
    }
    public List<Lot> getLotsByAuctionId(int auctionId) throws Exception {
        try {
            List<Lot> lotList = new ArrayList<Lot>();
            List<Integer> lotsIndexes = new ArrayList<Integer>();
            ResultSet rs = st.executeQuery("SELECT id FROM lots WHERE auctionId="+auctionId);
            while(rs.next())
                lotsIndexes.add(rs.getInt("id"));
            for(Integer index : lotsIndexes){
                List<Item> lotItems = itemDao.getItemsByLot(index);
                rs = st.executeQuery("SELECT * FROM lots WHERE id="+index);
                rs.next();
                lotList.add(new Lot(rs.getInt("id"), rs.getString("lotName"),
                        rs.getDouble("startPrice"), rs.getDouble("minPrice"),
                        rs.getString("status"), rs.getInt("auctionId"),lotItems));
            }
            return lotList;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage() + " (LotDao)");
        }
    }
//    public Lot getLotWithItems(int lotId) throws Exception {
//        try {
//            ResultSet rs = st.executeQuery("SELECT * FROM lots WHERE id="+lotId);
//            if(rs.next())
//                return new Lot(rs.getInt("id"),rs.getString("lotName"),
//                        rs.getDouble("startPrice"),rs.getDouble("minPrice"),
//                        rs.getString("status"),rs.getInt("auctionId"), itemDao.getItemsByLot(lotId));
//            else throw new Exception("Lot not found");
//        } catch (SQLException e) {
//            throw new Exception(e.getMessage());
//        }
//    }
    public List<Lot> getAllLots() throws Exception {
        ResultSet rs = st.executeQuery("SELECT * FROM lots ORDER BY ID");
        List<Lot> lots = new ArrayList<Lot>();
        int id;
        while (rs.next()){
            id = rs.getInt("ID");
            Lot lot = new Lot(id,rs.getString("lotName"),
                    rs.getDouble("startPrice"), rs.getDouble("minPrice"),
                    rs.getString("status"),rs.getInt("auctionId"),
                    itemDao.getItemsByLot(id));
            lots.add(lot);
        }
        return lots;
    }
    public List<Lot> getLotsBySearch(String substr) throws Exception {
        try {
            //List<Item> items = itemDao.getItemsByLot(auctionId);
            List<Lot> lotList = new ArrayList<Lot>();
            ResultSet rs = st.executeQuery("SELECT * FROM lots WHERE MATCH (lotName) AGAINST (\'" + substr + "\') ORDER BY id");
            // if(rs.next())
            while (rs.next()){

                lotList.add(new Lot(rs.getInt("id"), rs.getString("lotName"),
                        rs.getDouble("startPrice"), rs.getDouble("minPrice"),
                        rs.getString("status"), rs.getInt("auctionId"), new ArrayList<Item>())); //TODO fetch items from DB. NO MATTER TO DO THAT
            }
            //    else throw new Exception("Lots not found");
            return lotList;
        } catch (SQLException e) {
            throw new Exception(e.getMessage());
        }
    }
    public List<Integer> deleteEndedLots(){
        synchronized (new Integer(3)) {
            try {
                List<Integer> lotsIndexes = new ArrayList<Integer>();
                ResultSet rs = st.executeQuery("SELECT id FROM lots WHERE status=\'end\'");
                while (rs.next())
                    lotsIndexes.add(rs.getInt("id"));
                st.execute("DELETE FROM lots WHERE status=\'end\'");
                return lotsIndexes;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
    public List<Lot> getRegisteredLots() throws Exception {
        try {
            List<Lot> lotList = new ArrayList<Lot>();
            ResultSet rs = st.executeQuery("SELECT * FROM lots WHERE status=\'registered\'");
            // if(rs.next())
            while (rs.next()){
                List<Item> items = itemDao.getItemsByLot(rs.getInt("id"));
                lotList.add(new Lot(rs.getInt("id"), rs.getString("lotName"),
                        rs.getDouble("startPrice"), rs.getDouble("minPrice"),
                        rs.getString("status"), rs.getInt("auctionId"),items));
            }
            //    else throw new Exception("Lots not found");
            return lotList;
        } catch (SQLException e) {
            throw new Exception(e.getMessage()+" (LotDao)");
        }
    }
}