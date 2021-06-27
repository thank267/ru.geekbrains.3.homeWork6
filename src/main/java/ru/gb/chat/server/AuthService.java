package ru.gb.chat.server;

/**
 * Created by Artem Kropotov on 24.05.2021
 */
public interface AuthService<T> extends CrudService<T, Long> {
    User findByLoginAndPassword(String login, String password);
    User findByLoginOrNick(String login, String nick);
    User updateNickByUser(User user, String newNick);
}
