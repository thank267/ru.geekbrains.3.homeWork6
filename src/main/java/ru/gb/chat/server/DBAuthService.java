package ru.gb.chat.server;

import java.sql.*;
import java.util.List;

/**
 * Created by Artem Kropotov on 24.05.2021
 */
public class DBAuthService  implements AuthService<User>  {

    private static String URI = "jdbc:postgresql://localhost/gb?user=gb&password=gb";

    private static DBAuthService INSTANCE;

    private static Connection connection;


    private DBAuthService() {

        try {
            connect();

        } catch (Exception e) {
            e.printStackTrace();
            disconnect();
        }


    }


    public static DBAuthService getInstance() {
        if (INSTANCE == null) {
            synchronized (ListAuthService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DBAuthService();
                }
            }
        }
        return INSTANCE;
    }

    public static void connect() throws SQLException {
        connection = DriverManager.getConnection(URI);
        System.out.println("Success connected to DB");
    }

    public static void disconnect() {

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User findByLoginAndPassword(String login, String password) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("SELECT login, password, nickname FROM USERS WHERE login=? AND password=?");
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet rs=ps.executeQuery();
            if (!rs.next()) return null;
            else {
                User user = new User(rs.getString(1),rs.getString(2),rs.getString(3));
                System.out.println("Find user: "+String.valueOf(user));
                return user;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    @Override
    public User findByLoginOrNick(String login, String nick) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("SELECT login, password, nickname FROM USERS WHERE login=? OR nickname=?");
            ps.setString(1, login);
            ps.setString(2, nick);
            ResultSet rs=ps.executeQuery();
            if (!rs.next()) return null;
            else {
                User user = new User(rs.getString(1),rs.getString(2),rs.getString(3));
                System.out.println("Find user: "+String.valueOf(user));
                return user;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    @Override
    public User updateNickByUser(User user, String newNick) {
        PreparedStatement ps = null;

        try {
            ps = connection.prepareStatement("UPDATE USERS SET nickname=? WHERE login=? AND password=? AND nickname=?");
            ps.setString(1, newNick);
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getNickname());
            int rs = ps.executeUpdate();
            System.out.println("Update row count: "+rs);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }

        return findByLoginAndPassword(user.getLogin(), user.getPassword());
    }

    @Override
    public User save(User object)  {

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("INSERT INTO USERS (login, password, nickname) VALUES (?,?,?)");
            ps.setString(1, object.getLogin());
            ps.setString(2, object.getPassword());
            ps.setString(3, object.getNickname());
            int rs = ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return object;
        }

        return object;

    }

    @Override
    public User remove(User object) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("DELETE FROM USERS WHERE login=? AND password=? AND nickname=?");
            ps.setString(1, object.getLogin());
            ps.setString(2, object.getPassword());
            ps.setString(3, object.getNickname());
            int rs = ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return object;
        }

        return object;
    }

    @Override
    public User removeById(Long aLong) {
        return null;
    }

    @Override
    public User findById(Long aLong) {
        return null;
    }

    @Override
    public List<User> findAll() {
        return null;
    }
}
