package com.ogcz.app;

import com.ogcz.app.database.UserSession;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ARAMotorhub extends Application {
    public static void main(String[] args) {
        launch(args);
    }

        @Override
        public void start(Stage primaryStage) throws Exception {

            UserSession.setMainStage(primaryStage);
            primaryStage.setTitle("ARA Motorhub");
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/assets/z_favicon.png")));

            Parent root = FXMLLoader.load(getClass().getResource("/fxml/auth/Login.fxml"));
            Scene scene = new Scene(root, 938.32, 609.23);

            scene.getRoot().setStyle("-fx-font-smoothing-type: lcd;");

            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        }
}
