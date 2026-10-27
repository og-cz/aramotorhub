package com.ogcz.app.controllers.admin;

import com.ogcz.app.SettingsWindowHelper;
import com.ogcz.app.database.DatabaseConnection;
import com.ogcz.app.database.User;
import com.ogcz.app.database.UserSession;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class AdminController {

    @FXML
    private TableView<User> userAdmin;

    @FXML
    private TableColumn<User, String> username;

    @FXML
    private TableColumn<User, String> password;

    @FXML
    private TextField usernameTextField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Button createButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button updateButton;

    @FXML
    private Label adminName;

    private final ObservableList<User> adminUsers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        username.setCellValueFactory(new PropertyValueFactory<>("email"));
        password.setCellValueFactory(new PropertyValueFactory<>("password"));

        userAdmin.setEditable(true);
        username.setCellFactory(TextFieldTableCell.forTableColumn());
        password.setCellFactory(TextFieldTableCell.forTableColumn());

        username.setOnEditCommit(this::onEmailEditCommit);
        password.setOnEditCommit(this::onPasswordEditCommit);

        userAdmin.setItems(adminUsers);
        loadAdminsFromDatabase();

        if (UserSession.getCurrentUser() != null) {
            adminName.setText(UserSession.getCurrentUser().getEmail());
        }
    }

    private void loadAdminsFromDatabase() {
        adminUsers.clear();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user WHERE role = 'admin'");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                adminUsers.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("address"),
                        rs.getString("district"),
                        rs.getString("barangay"),
                        rs.getString("role")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("ARA Motorhub", "Failed to load admin users: " + e.getMessage());
        }
    }

    @FXML
    void handleCreate(ActionEvent event) {
        String email = usernameTextField.getText().trim();
        String pass = passwordTextField.getText();

        if (email.isEmpty() || pass.isEmpty()) {
            showAlert("ARA Motorhub", "Please enter both email and password.");
            return;
        }

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO user (email, password, first_name, last_name, address, district, barangay, role) VALUES (?, ?, '', '', '', '', '', 'admin')")) {
            stmt.setString(1, email);
            stmt.setString(2, pass);
            stmt.executeUpdate();

            showAlert("ARA Motorhub", "Admin created successfully.");

            usernameTextField.clear();
            passwordTextField.clear();
            loadAdminsFromDatabase();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("ARA Motorhub", "Could not create admin: " + e.getMessage());
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        User selected = userAdmin.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("ARA Motorhub", "Please select an admin to delete.");
            return;
        }

        User current = UserSession.getCurrentUser();
        if (current != null && current.getUserId() == selected.getUserId()) {
            showAlert("ARA Motorhub", "You can't delete the account you're logged in with.");
            return;
        }

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM user WHERE user_id = ?")) {
            stmt.setInt(1, selected.getUserId());
            stmt.executeUpdate();

            showAlert("ARA Motorhub", "Admin deleted successfully.");
            loadAdminsFromDatabase();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("ARA Motorhub", "Could not delete admin: " + e.getMessage());
        }
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        for (User user : adminUsers) {
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE user SET email = ?, password = ? WHERE user_id = ?")) {
                stmt.setString(1, user.getEmail());
                stmt.setString(2, user.getPassword());
                stmt.setInt(3, user.getUserId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                showAlert("ARA Motorhub", "Could not update admin: " + e.getMessage());
                return;
            }
        }

        showAlert("ARA Motorhub", "All changes saved successfully.");
        loadAdminsFromDatabase();
    }

    @FXML
    public void onEmailEditCommit(TableColumn.CellEditEvent<User, String> event) {
        User user = event.getRowValue();
        user.setEmail(event.getNewValue());
    }

    @FXML
    public void onPasswordEditCommit(TableColumn.CellEditEvent<User, String> event) {
        User user = event.getRowValue();
        user.setPassword(event.getNewValue());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void handleLogOutButton(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SettingsWindowHelper.logout(currentStage);
    }

    @FXML
    void handleUserClick(ActionEvent e) {
       switchScene(e, "/fxml/admin/AdminUsers.fxml");
    }

    @FXML
    void handleProductsClick(ActionEvent e) {
          switchScene(e, "/fxml/admin/AdminProduct.fxml");
    }

    @FXML
    void handleOrderClick(ActionEvent e) {
          switchScene(e, "/fxml/admin/AdminOrder.fxml");
    }

    @FXML
    void handleCategoryClick(ActionEvent e) {
          switchScene(e, "/fxml/admin/AdminCategory.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newRoot = loader.load();
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(newRoot));
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
