package com.ogcz.app.controllers.auth;

import java.io.IOException;

import com.ogcz.app.database.AuthService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class RegisterStep2Controller {

    @FXML
    private TextField addressField;

    @FXML
    private SplitMenuButton districtFieldMenu;
    private String selectedDistrict;

    @FXML
    private SplitMenuButton barangayFieldMenu;
    private String selectedBarangay;

    @FXML
    private SplitMenuButton roleFieldMenu;
    private String selectedRole;

    @FXML
    private Button submitButton;

    private AuthService authService;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setContentText(message);
        alert.setHeaderText(null);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/assets/z_favicon.png").toString()));
        alert.showAndWait();
    }

    @FXML
    public void initialize() {

        districtFieldMenu.getItems().forEach(item -> {
            item.setOnAction(e -> {
                selectedDistrict = item.getText();
                districtFieldMenu.setText(selectedDistrict);
            });
        });

        barangayFieldMenu.getItems().forEach(item -> {
            item.setOnAction(e -> {
                selectedBarangay = item.getText();
                barangayFieldMenu.setText(selectedBarangay);
            });
        });

        roleFieldMenu.getItems().forEach(item -> {
            item.setOnAction(e -> {
                selectedRole = item.getText();
                roleFieldMenu.setText(selectedRole.toUpperCase());
            });
        });
    }

    @FXML
    void handleSubmitButtonClick(MouseEvent event) {
        if (!addressField.getText().matches("^.{10,100}$")) {
            showAlert("ARA Motorhub", "Invalid address! Must be between 10 and 100 characters");
            return;
        }

        if (selectedBarangay == null || selectedDistrict == null || selectedRole == null) {
            showAlert("ARA Motorhub", "Please fill in the blanks");
            return;
        }

        authService.setAddress(addressField.getText());
        authService.setDistrict(selectedDistrict);
        authService.setBarangay(selectedBarangay);
        authService.setRole(selectedRole);

        if (authService.registerNewUser() == null) {
            showAlert("ARA Motorhub", "Registration failed. Please try again.");
            return;
        }

        showAlert("ARA Motorhub", "Your account has been created! You can now sign in.");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/Login.fxml"));
            Parent newRoot = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(newRoot));
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

}
