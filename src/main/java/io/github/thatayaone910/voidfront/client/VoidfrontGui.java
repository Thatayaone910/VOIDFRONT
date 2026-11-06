package io.github.thatayaone910.voidfront.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class VoidfrontGui extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Setting up the User Interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Voidfront.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("VOIDFRONT");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMaximized(true);
        primaryStage.show();

        VoidfrontController controller = loader.getController();
        controller.configureEnvironment( getParameters().getRaw().toArray(new String[0]), primaryStage );
    }
}
