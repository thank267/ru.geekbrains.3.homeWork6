package ru.gb.chat.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private final Map<String, Integer> uniqCheckMap = new HashMap<>();

    @FXML
    TextArea textArea;
    @FXML
    TextField textField;

    @FXML
    TextField loginField, loginRegister, nickRegister;

    @FXML
    PasswordField passField, passRegister;


    @FXML
    HBox authPanel, msgPanel, centralPanel;

    @FXML
    VBox registerPanel;

    @FXML
    ListView<String> clientsList;

    private String nickname;
    private boolean authenticated;

    public void sendMsg(){
        String warning = validate();
        if (warning == null) {
            NetworkService.sendMessage(textField.getText());
            textField.clear();
        } else {
            new Alert(Alert.AlertType.WARNING, warning, ButtonType.OK).showAndWait();
        }

        textField.requestFocus();
    }



    private String validate() {
        String textFromField = textField.getText();
        String warning = null;
        if (textFromField.isEmpty()) {
            warning = "Нельзя отправлять пустое сообщение";
        } else {
            Integer count = uniqCheckMap.getOrDefault(textFromField, 0);
            if (count.equals(0)) {
                uniqCheckMap.clear();
            }
            uniqCheckMap.put(textFromField, ++count);
            if (count > 3) {
                warning = "Нельзя отправлять больше 3 одинаковых сообщений подряд. № вашей попытки: " + count;
            }
        }
        return warning;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textArea.setText(HistoryService.load());
        setAuthenticated(false);
        clientsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedNickname = clientsList.getSelectionModel().getSelectedItem();
                textField.setText("/w " + selectedNickname + " ");
                textField.requestFocus();
                textField.selectEnd();
            }
        });
        setCallbacks();
    }

    public void sendAuth() {
        NetworkService.sendAuth(loginField.getText(), passField.getText());
        loginField.clear();
        passField.clear();
    }


    public void sendRegister() {
        NetworkService.sendRegister(loginRegister.getText(), nickRegister.getText(), passRegister.getText());
        loginRegister.clear();
        nickRegister.clear();
        passRegister.clear();
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        registerPanel.setVisible(!authenticated);
        registerPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        centralPanel.setVisible(authenticated);
        centralPanel.setManaged(authenticated);
        if (!authenticated) {
            nickname = "";
        }

    }

    public boolean isAuthenticated() {
        return authenticated;
    }



    public void setCallbacks() {
        NetworkService.setCallOnException(args ->
                Platform.runLater(() -> {
                new Alert(
                        Alert.AlertType.WARNING,
                        String.valueOf(args[0]),
                        ButtonType.OK).showAndWait();
                }));

        NetworkService.setCallOnAuthenticated(args -> {
            nickname = String.valueOf(args[0]);
            setAuthenticated(true);
        });

        NetworkService.setCallOnMsgReceived(args -> {
            String msg = String.valueOf(args[0]);
            if (msg.startsWith("/")) {
                if (msg.startsWith("/clients")) {
                    String[] nicknames = msg.split("\\s");
                    Platform.runLater(() -> {
                        clientsList.getItems().clear();
                        for (int i = 1; i < nicknames.length; i++) {
                            clientsList.getItems().add(nicknames[i]);
                        }
                    });
                }
            } else {
                textArea.appendText(msg + "\n");
            }
        });

        NetworkService.setCallOnDisconnect(args -> setAuthenticated(false));
    }

    public void shutdown() {
        HistoryService.save(textArea.getText());
    }
}
