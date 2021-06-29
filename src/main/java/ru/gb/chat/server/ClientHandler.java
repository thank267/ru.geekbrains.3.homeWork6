package ru.gb.chat.server;

import lombok.extern.log4j.Log4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by Artem Kropotov on 17.05.2021
 */
@Log4j
public class ClientHandler {

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private ServerChat serverChat;
    private AuthService<User> authService = DBAuthService.getInstance();
    private User user;

    public ClientHandler(Socket socket, ServerChat serverChat) {
        try {
            this.socket = socket;
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
            this.serverChat = serverChat;

            new Thread(() -> {
                try {
                    while (true) {
                        String msg = in.readUTF();
                        // /auth login password
                        if (msg.startsWith("/auth ")) {
                            log.info("Клиент прислал команду: "+msg);
                            String[] token = msg.split("\\s");
                            User user = authService.findByLoginAndPassword(token[1], token[2]);
                            if (user != null && !serverChat.isNickBusy(user.getNickname())) {
                                sendMessage("/authok " + user.getNickname());
                                this.user = user;
                                serverChat.subscribe(this);
                                break;
                            } else {
                                sendMessage("/authfail");
                            }
                            // /register login nickname password
                        } else if (msg.startsWith("/register ")) {
                            log.info("Клиент прислал команду: "+msg);
                            String[] token = msg.split("\\s");
                            User user = authService.findByLoginOrNick(token[1], token[2]);
                            if (user == null) {
                                user = authService.save(new User(token[1], token[3], token[2]));
                                sendMessage("/authok " + user.getNickname());
                                this.user = user;
                                serverChat.subscribe(this);
                                break;
                            } else {
                                sendMessage("/regfail");
                            }
                        }
                    }
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/")) {
                            if (msg.equals("/end")) {
                                log.info("Клиент прислал команду: "+msg);
                                sendMessage("/end");
                                break;
                            }
                            // /w nick fg sdg sdfg sd
                            if (msg.startsWith("/w")) {
                                String[] token = msg.split("\\s", 3);
                                serverChat.privateMsg(this, token[1], token[2]);

                            }
                            if (msg.startsWith("/update")) {
                                log.info("Клиент прислал команду: "+msg);
                                String[] token = msg.split("\\s", 2);
                                serverChat.unsubscribe(this);
                                this.user= authService.updateNickByUser(this.getUser(),token[1]);
                                serverChat.subscribe(this);

                            }
                            if (msg.equals("/del")) {
                                log.info("Клиент прислал команду: "+msg);
                                authService.remove(user);
                                sendMessage("/end");
                                break;
                            }
                        } else {
                            log.info("Клиент " + user.getNickname() +" прислал сообщение: "+msg);
                            serverChat.broadcastMsg(user.getNickname() + ": " + msg);
                        }
                    }
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(),e);
                } finally {
                    log.info("Клиент отключился");
                    disconnect();
                }
            }).start();
        } catch (IOException e) {
            disconnect();
            log.error(e.getLocalizedMessage(),e);
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(),e);
        }
    }

    public void disconnect() {
        serverChat.unsubscribe(this);
        try {
            socket.close();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(),e);
        }
        try {
            in.close();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(),e);
        }
        try {
            out.close();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(),e);
        }
    }

    public User getUser() {
        return user;
    }
}
