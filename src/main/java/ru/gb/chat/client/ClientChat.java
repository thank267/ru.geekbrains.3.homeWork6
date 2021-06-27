package ru.gb.chat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientChat extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        primaryStage.setTitle("Chat");
        primaryStage.setScene(new Scene(root, 400, 275));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            controller.shutdown();
            NetworkService.close();
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
