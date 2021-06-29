import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import ru.gb.chat.client.Controller;
import ru.gb.chat.client.NetworkService;
import ru.gb.chat.server.ServerChat;

public class ControllerLoginFaledTest extends ApplicationTest {

    Controller controller;

    ServerChat serverChat = new ServerChat();

    @Override
    public void start (Stage primaryStage) throws Exception {
        Thread thread = new Thread(){
            public void run(){
                serverChat.start();
            }
        };

        thread.start();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        primaryStage.setTitle("Chat");
        primaryStage.setScene(new Scene(root, 400, 275));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            controller.shutdown();
            NetworkService.close();
        });
    }

    @Test
    public void loginFailedTest() throws InterruptedException {
        clickOn("#loginField").write("Wrong");
        clickOn("#passField").write("Wrong");
        clickOn("#sendAuth");
        Thread.sleep(1000);
        Assertions.assertEquals(false, controller.isAuthenticated());
    }



}
