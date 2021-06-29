package ru.gb.chat.client;

import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


/**
 * Created by Artem Kropotov on 17.05.2021
 */
public class NetworkService {
    private static final String IP_ADDRESS = "localhost";
    private static final int PORT = 8189;

    private static Socket socket;
    private static DataInputStream in;
    private static DataOutputStream out;


    private static Callback callOnException;
    private static Callback callOnMsgReceived;
    private static Callback callOnAuthenticated;
    private static Callback callOnDisconnect;
    private static Thread thread;

    static {
        Callback callback = (args) -> {
        };
        callOnException = callback;
        callOnMsgReceived = callback;
        callOnAuthenticated = callback;
        callOnDisconnect = callback;
    }

    public static void setCallOnException(Callback callOnException) {
        NetworkService.callOnException = callOnException;
    }

    public static void setCallOnMsgReceived(Callback callOnMsgReceived) {
        NetworkService.callOnMsgReceived = callOnMsgReceived;
    }

    public static void setCallOnAuthenticated(Callback callOnAuthenticated) {
        NetworkService.callOnAuthenticated = callOnAuthenticated;
    }

    public static void setCallOnDisconnect(Callback callOnDisconnect) {
        NetworkService.callOnDisconnect = callOnDisconnect;
    }

    public static void sendAuth(String login, String password) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        sendMessage("/auth " + login + " " + password);
    }

    public static void sendRegister(String login, String nickname, String password) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        sendMessage("/register " + login + " " + nickname + " " + password);
    }

    public static void sendMessage(String message) {
        if (thread != null ) {
            thread.interrupt();
        } if (!message.equals("/end")) {
            thread = new Thread(() -> {
                try {
                    Thread.sleep(50000);
                    NetworkService.sendMessage("/end");
                } catch (InterruptedException ignored) {}
            });
            thread.setDaemon(true);
            thread.start();
        }
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            Thread clientListener = new Thread(() -> {
                try {
                    // цикл успешной аутентификации
                    String msg;
                    while (true) {
                        msg = in.readUTF();
                        if (msg.startsWith("/authok ")) {
                            callOnAuthenticated.callback(msg.split("\\s")[1]);
                            break;
                        } else if (msg.startsWith("/authfail")) {
                            callOnException.callback("Error login or password");
                        } else if (msg.startsWith("/regfail")) {
                            callOnException.callback("Duplicate login or nickname");
                        }
                    }
                    while (true) {
                        msg = in.readUTF();
                        if (msg.equals("/end")) {
                            break;
                        }
                        callOnMsgReceived.callback(msg);
                    }
                } catch (IOException e) {
                    callOnException.callback("Соединение с сервером разорвано");
                    e.printStackTrace();
                } finally {
                    disconnect();
                }
            });
            clientListener.setDaemon(true);
            clientListener.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        callOnDisconnect.callback();
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

    public static void close() {
        Platform.runLater(() -> {
            if (socket != null && !socket.isClosed()) {
                sendMessage("/end");
            }
            Platform.exit();
        });
    }
}
