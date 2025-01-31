package by.students.grsu.entities.dao.implementations;


import by.students.grsu.entities.dao.interfaces.SoldLotDao;
import by.students.grsu.entities.lot.SoldLot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.util.ArrayList;
import java.util.List;

public class MySqlSoldLotDao implements SoldLotDao {
    private JdbcTemplate template;
    private ResultSetExtractor<List<SoldLot>> soldLotListExtractor = rs -> {
        List<SoldLot> lotList = new ArrayList<>();
        while(rs.next()) {
            lotList.add(new SoldLot(rs.getNString("buyer"), rs.getNString("seller"), rs.getInt("lotId"), rs.getDouble("price")));
        }
        return lotList;
    };
    private ResultSetExtractor<SoldLot> soldLotExtractor = rs -> {
        return rs.next() ? new SoldLot(rs.getString("buyer"), rs.getNString("seller"), rs.getInt("lotId"), rs.getDouble("price")) : null;
    };

    public MySqlSoldLotDao(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    public void addSoldLot(int lotId, String buyerUsername, String sellerUsername, double price) {
        template.execute("INSERT INTO soldLots VALUES(" + lotId + ", '" + buyerUsername + "', " + price + ",'" + sellerUsername + "')");
    }

    @Override
    public void deleteSoldLot(int lotId) {
        template.execute("DELETE FROM soldLots WHERE lotId=" + lotId);
    }

    @Override
    public List<SoldLot> getSoldLotsByUser(String username){
        return template.query("SELECT * FROM soldLots WHERE buyer='" + username + "'", soldLotListExtractor);
    }

    @Override
    public SoldLot getSoldLotById(int lotId) throws Exception {
        SoldLot soldLot = template.query("SELECT * FROM soldLots WHERE lotId=" + lotId, soldLotExtractor);
        if (soldLot != null) {
            return soldLot;
        } else {
            throw new Exception("SoldLot not found");
        }
    }
}
