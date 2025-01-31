package by.students.grsu.rowMappers;

import by.students.grsu.entities.auction.Auction;
import by.students.grsu.entities.auction.FollowedAuction;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FollowedAuctionRowMapper implements RowMapper<FollowedAuction> {

    @Override
    public FollowedAuction mapRow(ResultSet resultSet, int i) throws SQLException {
        //if(resultSet.next() && resultSet.getString("username") != null)
        return new FollowedAuction(new Auction(resultSet.getInt("id"), resultSet.getString("description"),
                resultSet.getInt("maxLots"), resultSet.getTime("beginTime").toLocalTime(),
                resultSet.getInt("maxDuration"), resultSet.getString("status"),
                resultSet.getInt("currentLots")), true);

    }
}
