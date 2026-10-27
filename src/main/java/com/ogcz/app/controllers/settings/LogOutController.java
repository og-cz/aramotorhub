package com.ogcz.app.controllers.settings;

import java.io.IOException;

import com.ogcz.app.SettingsWindowHelper;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class LogOutController {

    @FXML
    private Button deleteAccButton;

    @FXML
    private Button editProfileButton;

    @FXML
    private HBox handleDeleteAccount;

    @FXML
    private Button logOutButton;

    @FXML
    void handleLogOutButton(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SettingsWindowHelper.logout(currentStage);
    }

    // switching scene dont touch
    @FXML
    void switchToDeleteAccount(ActionEvent event) {
        loadFXMLScene("/fxml/settings/DeleteAccount.fxml", event);
    }

    @FXML
    void switchToEditProfile(ActionEvent event) {
        loadFXMLScene("/fxml/settings/EditProfile.fxml", event);
    }

    @FXML
    void switchToLogOut(ActionEvent event) {
        loadFXMLScene("/fxml/settings/LogOut.fxml", event);
    }

    private void loadFXMLScene(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
