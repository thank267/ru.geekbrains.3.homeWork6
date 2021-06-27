package ru.gb.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by Artem Kropotov on 17.05.2021
 */
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
                                sendMessage("/end");
                                break;
                            }
                            // /w nick fg sdg sdfg sd
                            if (msg.startsWith("/w")) {
                                String[] token = msg.split("\\s", 3);
                                serverChat.privateMsg(this, token[1], token[2]);

                            }
                            if (msg.startsWith("/update")) {
                                String[] token = msg.split("\\s", 2);
                                System.out.println(token[1]);
                                serverChat.unsubscribe(this);
                                this.user= authService.updateNickByUser(this.getUser(),token[1]);
                                serverChat.subscribe(this);

                            }
                            if (msg.equals("/del")) {
                                authService.remove(user);
                                sendMessage("/end");
                                break;
                            }
                        } else {
                            serverChat.broadcastMsg(user.getNickname() + ": " + msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Клиент отключился");
                    disconnect();
                }
            }).start();
        } catch (IOException e) {
            disconnect();
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        serverChat.unsubscribe(this);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getUser() {
        return user;
    }
}
