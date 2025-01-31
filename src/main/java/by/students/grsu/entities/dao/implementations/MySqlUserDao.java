package by.students.grsu.entities.dao.implementations;

import by.students.grsu.entities.dao.interfaces.UserDao;
import by.students.grsu.entities.users.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.util.ArrayList;
import java.util.List;

public class MySqlUserDao implements UserDao {
    private JdbcTemplate template;
    private ResultSetExtractor<List<User>> userListExtractor = rs -> {
        List<User> userList = new ArrayList<>();

        while(rs.next()) {
            User newUser = new User(rs.getString("username"), rs.getString("authority").toLowerCase());
            newUser.setEnabled(!isBanned(newUser.getUsername()));
            userList.add(newUser);
        }

        return userList;
    };

    public MySqlUserDao(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    public void setEmail(String username, String email) throws Exception {
        if(template.update("update users set email = '" + email + "' where username = '" + username + "'") == 0)
            throw new Exception("User not found");
    }

    @Override
    public String getEmail(String username) {
        return template.queryForObject("select email from users where username = '" + username + "'", String.class);
    }

    @Override
    public void setRole(String username, String newRole) throws Exception {
        if (template.update("UPDATE authorities SET authority = 'ROLE_" + newRole.toUpperCase() + "' where username = '" + username + "'") == 0) {
            throw new Exception("User not found");
        }
    }

    @Override
    public void banUser(String username) throws Exception {
        if (template.update("UPDATE users SET enabled=0 WHERE username='" + username + "'") == 0) {
            throw new Exception("User not found");
        }
    }

    @Override
    public void unbanUser(String username) throws Exception {
        if (template.update("UPDATE users SET enabled=1 WHERE username='" + username + "'") == 0) {
            throw new Exception("User not found");
        }
    }

    @Override
    public List<User> searchUsers(String... words) {
        //return template.query(buildSql(words), userListExtractor);
        if(words.length == 0) return template.query("SELECT * FROM authorities", userListExtractor);
        StringBuilder substr = new StringBuilder();
        for(int i = 0; i < words.length; i++) substr.append(words[i]).append(" ");
        return template.query("SELECT * FROM authorities WHERE MATCH (username) AGAINST ('" + substr.toString() + "')", userListExtractor);
    }

//    private String buildSql(String[] words) {
//
//
//        StringBuilder sql = new StringBuilder("SELECT * FROM authorities");
//        if (words.length > 0) {
//            sql.append(" WHERE ");
//        }
//
//        for(int i = 0; i < words.length; ++i) {
//            if (i != 0) {
//                sql.append(" OR ");
//            }
//
//            sql.append("username LIKE \"%").append(words[i]).append("%\"");
//        }
//
//        sql.append(" ORDER BY username;");
//        return sql.toString();
//    }

    private boolean isBanned(String username) {
        Integer enabled = template.queryForObject("SELECT enabled FROM users WHERE username='" + username + "'", Integer.class);
        return enabled != 1;
    }
}