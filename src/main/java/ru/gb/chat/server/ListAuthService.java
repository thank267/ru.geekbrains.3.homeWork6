package ru.gb.chat.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Artem Kropotov on 24.05.2021
 */
public class ListAuthService implements AuthService<User> {

    private static ListAuthService INSTANCE;

    private final CopyOnWriteArrayList<User> users = new CopyOnWriteArrayList<>();

    private ListAuthService() {
        for (int i = 0; i <= 10; i++) {
            users.add(new User("login" + i, "pass" + i, "nick" + i));
        }
    }

    public static ListAuthService getInstance() {
        if (INSTANCE == null) {
            synchronized (ListAuthService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ListAuthService();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public User findByLoginAndPassword(String login, String password) {
        for (User u : users) {
            if (u.getLogin().equals(login) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public User findByLoginOrNick(String login, String nick) {
        for (User u : users) {
            if (u.getLogin().equals(login) || u.getNickname().equals(nick)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public User updateNickByUser(User user, String newNick) {
        return null;
    }

    @Override
    public User save(User user) {
        users.add(user);
        return user;
    }

    @Override
    public User remove(User user) {
        users.remove(user);
        return user;
    }

    @Override
    public List<User> findAll() {
        return null;
    }

    // todo не надо
    @Override
    public User removeById(Long aLong) {
        return null;
    }

    // todo не надо
    @Override
    public User findById(Long aLong) {
        return null;
    }
}
