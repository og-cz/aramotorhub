package com.ogcz.app.controllers.auth;

import java.io.IOException;

import com.ogcz.app.database.AuthService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class RegisterStep1Controller {

    @FXML
    private TextField emailField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private Button signInButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    void handleNextButtonClick(ActionEvent event) {
        if (emailField.getText().isEmpty() || firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showAlert("ARA Motorhub", "Please fill in all blanks");
            return;
        }

        if (!firstNameField.getText().matches("^[a-zA-Z0-9-]{2,}\\b$")) {
            showAlert("ARA Motorhub", "Invalid first name");
            return;
        }

        if (!lastNameField.getText().matches("^[a-zA-Z0-9-]{2,}\\b$")) {
            showAlert("ARA Motorhub", "Invalid last name");
            return;
        }

        if (!(emailField.getText().contains("@") && emailField.getText().endsWith(".com"))) {
            showAlert("ARA Motorhub", "Email must contain '@' and end with '.com'");
            return;
        }

        if (!passwordField.getText().matches("^.{4,25}$")) {
            showAlert("ARA Motorhub", "Password must be at least 4 characters and maximum of 25 characters");
            return;
        }

        AuthService authService = new AuthService();
        if (authService.emailExists(emailField.getText().trim())) {
            showAlert("ARA Motorhub", "An account with this email already exists.");
            return;
        }

        authService.setFirstName(firstNameField.getText());
        authService.setLastName(lastNameField.getText());
        authService.setEmail(emailField.getText().trim());
        authService.setPassword(passwordField.getText());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/RegisterStep2.fxml"));
            Parent newRoot = loader.load();

            RegisterStep2Controller controller = loader.getController();
            controller.setAuthService(authService);

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene newScene = new Scene(newRoot);
            currentStage.setScene(newScene);
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleSignInButtonClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/Login.fxml"));
            Parent newRoot = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene newScene = new Scene(newRoot);
            currentStage.setScene(newScene);
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle(title);
            alert.setContentText(message);

            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(this.getClass().getResource("/assets/z_favicon.png").toString()));
            alert.showAndWait();
        }

}
