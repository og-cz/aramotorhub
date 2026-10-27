package com.ogcz.app.controllers.settings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;

import com.ogcz.app.database.User;
import com.ogcz.app.database.UserService;
import com.ogcz.app.database.UserSession;

public class EditProfileController {

    @FXML
    private TextField firstNameField, lastNameField, addressField, emailField, passwordField;

    @FXML
    private Button deleteAccButton;

    @FXML
    private SplitMenuButton barangayFieldMenu, roleFieldMenu;

    @FXML
    private Button editProfileButton;

    @FXML
    private Button logOutButton;

    private User currentUser = UserSession.getCurrentUser();

    @FXML
    public void initialize() {
        if (currentUser == null) {
            return;
        }

        firstNameField.setText(currentUser.getFirstName());
        lastNameField.setText(currentUser.getLastName());
        addressField.setText(currentUser.getAddress());
        emailField.setText(currentUser.getEmail());
        passwordField.setText(currentUser.getPassword());

        roleFieldMenu.setText(currentUser.getRole());
        barangayFieldMenu.setText(currentUser.getBarangay());

        barangayFieldMenu.getItems().forEach(item -> {
            item.setOnAction(e -> barangayFieldMenu.setText(item.getText()));
        });
    }

    @FXML
    void handleSaveButton(ActionEvent event) {
        if (currentUser == null) {
            return;
        }

        if (firstNameField.getText().isBlank() || lastNameField.getText().isBlank()
                || emailField.getText().isBlank() || passwordField.getText().isBlank()) {
            showMessage(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("ARA Motorhub");
        alert.setContentText("Do you want to save the changes to your profile information?");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/assets/z_favicon.png").toString()));

        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }

            // edit a copy so the session isn't changed if the save fails.
            // role stays as is, users can't change their own role
            User updated = new User(currentUser.getUserId(), emailField.getText().trim(), passwordField.getText(),
                    firstNameField.getText().trim(), lastNameField.getText().trim(), addressField.getText().trim(),
                    currentUser.getDistrict(), barangayFieldMenu.getText(), currentUser.getRole());

            if (new UserService().updateUser(updated)) {
                UserSession.setCurrentUser(updated);
                currentUser = updated;
                showMessage(Alert.AlertType.INFORMATION, "Success", "Your profile has been updated successfully!");
            } else {
                showMessage(Alert.AlertType.ERROR, "Error", "Failed to update profile. Please try again.");
            }
        });
    }

    private void showMessage(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
