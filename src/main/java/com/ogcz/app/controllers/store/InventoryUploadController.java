package com.ogcz.app.controllers.store;

import com.ogcz.app.database.DatabaseConnection;
import com.ogcz.app.database.UserSession;
import com.ogcz.app.SettingsWindowHelper;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class InventoryUploadController {

    @FXML
    private TextField productName;
    @FXML
    private TextField productDescription;
    @FXML
    private TextField productPrice;
    @FXML
    private TextField productStock;
    @FXML
    private SplitMenuButton productCategory;
    @FXML
    private Button submitButton;

    private String selectedCategoryName;

    @FXML
    void handleBack(ActionEvent e) {
        navigateTo(e, "/fxml/store/Inventory.fxml");
    }

    @FXML
    public void initialize() {
        productCategory.setText("Select Category");

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement("SELECT name FROM category");
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                final String name = rs.getString("name");
                MenuItem item = new MenuItem(name);
                item.setOnAction(e -> {
                    selectedCategoryName = name;
                    productCategory.setText(name);
                });
                productCategory.getItems().add(item);
            }

        } catch (SQLException e) {
            showAlert("Error loading categories: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleSubmitButton(ActionEvent event) {
        String name = productName.getText();
        String description = productDescription.getText();
        String priceText = productPrice.getText();
        String stockText = productStock.getText();
        String categoryName = selectedCategoryName;

        if (name.isBlank() || description.isBlank() || priceText.isEmpty() || stockText.isEmpty()
                || categoryName == null) {
            showAlert("Please fill in all fields.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int price = Integer.parseInt(priceText.trim()); // price column is an int
            int stock = Integer.parseInt(stockText.trim());

            if (price <= 0 || stock < 0) {
                showAlert("Price must be above 0 and stock can't be negative.", Alert.AlertType.WARNING);
                return;
            }

            int categoryId = getCategoryIdByName(categoryName);
            if (categoryId == -1) {
                showAlert("Category not found.", Alert.AlertType.ERROR);
                return;
            }

            try (Connection conn = DatabaseConnection.connect();
                    PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO product (name, description, price, stock, category_id, seller_id, status) "
                                    + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setString(1, name);
                stmt.setString(2, description);
                stmt.setInt(3, price);
                stmt.setInt(4, stock);
                stmt.setInt(5, categoryId);
                stmt.setInt(6, UserSession.getCurrentUser().getUserId());
                stmt.setString(7, stock > 0 ? "AVAILABLE" : "OUT_OF_STOCK");

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    showAlert("Product added successfully.", Alert.AlertType.INFORMATION);
                    clearFields();
                } else {
                    showAlert("Failed to add product.", Alert.AlertType.ERROR);
                }
            }

        } catch (NumberFormatException e) {
            showAlert("Price and stock must be whole numbers.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            showAlert("Database error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private int getCategoryIdByName(String name) {
        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement("SELECT category_id FROM category WHERE name = ?")) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt("category_id");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("ARA Motorhub");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        productName.clear();
        productDescription.clear();
        productPrice.clear();
        productStock.clear();
        productCategory.setText("Select Category");
        selectedCategoryName = null;
    }

    // --- Navigation Handlers ---

    @FXML
    void handleDashboardClick(ActionEvent e) {
        navigateTo(e, "/fxml/store/Dashboard.fxml");
    }

    @FXML
    void handleInventoryClick(ActionEvent e) {
        navigateTo(e, "/fxml/store/Inventory.fxml");
    }

    @FXML
    void handleProductsClick(ActionEvent e) {
        navigateTo(e, "/fxml/store/Products.fxml");
    }

    @FXML
    void handleProfileClick(ActionEvent e) {
        navigateTo(e, "/fxml/store/Profile.fxml");
    }

    @FXML
    void handleShopsClick(ActionEvent e) {
        navigateTo(e, "/fxml/store/Shops.fxml");
    }

    @FXML
    void handleHomeButton(ActionEvent e) {
        navigateTo(e, "/fxml/store/Profile.fxml");
    }

    @FXML
    void handleOpenSettings(ActionEvent e) {
        SettingsWindowHelper.openSettings((Node) e.getSource());
    }

    @FXML
    void handleLogOutButton(ActionEvent e) {
        SettingsWindowHelper.logout((Stage) ((Node) e.getSource()).getScene().getWindow());
    }

    private void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showAlert("Cannot load: " + fxmlPath, Alert.AlertType.ERROR);
        }
    }
}
