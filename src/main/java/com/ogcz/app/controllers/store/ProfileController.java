package com.ogcz.app.controllers.store;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

import com.ogcz.app.SettingsWindowHelper;
import com.ogcz.app.database.DatabaseConnection;
import com.ogcz.app.database.User;
import com.ogcz.app.database.UserSession;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ProfileController implements Initializable {

    @FXML private Label firstLastNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label addressLabel;
    @FXML private Label barangayLabel;

    @FXML private ComboBox<String> categoryComboBox;
    @FXML private GridPane gridPane;

    @FXML private Label labelCategory1, labelCategory2, labelCategory3, labelCategory4;
    @FXML private Label labelCategory5, labelCategory6, labelCategory7, labelCategory8;
    @FXML private Label labelCategoryItem1, labelCategoryItem2, labelCategoryItem3, labelCategoryItem4;
    @FXML private Label labelCategoryItem5, labelCategoryItem6, labelCategoryItem7, labelCategoryItem8;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserInfo();
        setupCategoryComboBox();
        loadCategoryData();
    }

    private void loadUserInfo() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            firstLastNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
            emailLabel.setText(currentUser.getEmail());
            addressLabel.setText(currentUser.getAddress());
            barangayLabel.setText(currentUser.getBarangay());
        }
    }

    private void setupCategoryComboBox() {
        String sql = "SELECT name FROM category ORDER BY name ASC";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categoryComboBox.getItems().add(rs.getString("name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        categoryComboBox.setOnAction(event -> {
            String selectedCategory = categoryComboBox.getValue();
            if (selectedCategory != null) {
                openCategoryView(selectedCategory);
            }
        });
    }

    private void loadCategoryData() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) return;

        String query = """
                SELECT c.name AS category_name, COUNT(p.product_id) AS product_count
                FROM category c
                LEFT JOIN product p ON c.category_id = p.category_id
                    AND p.seller_id = ? AND p.status = 'AVAILABLE'
                GROUP BY c.category_id, c.name
                ORDER BY c.category_id ASC
                """;

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();

            int index = 1;
            while (rs.next() && index <= 8) {
                String category = rs.getString("category_name");
                int productCount = rs.getInt("product_count");

                switch (index) {
                    case 1 -> {
                        labelCategory1.setText(category);
                        labelCategoryItem1.setText(productCount + " items");
                    }
                    case 2 -> {
                        labelCategory2.setText(category);
                        labelCategoryItem2.setText(productCount + " items");
                    }
                    case 3 -> {
                        labelCategory3.setText(category);
                        labelCategoryItem3.setText(productCount + " items");
                    }
                    case 4 -> {
                        labelCategory4.setText(category);
                        labelCategoryItem4.setText(productCount + " items");
                    }
                    case 5 -> {
                        labelCategory5.setText(category);
                        labelCategoryItem5.setText(productCount + " items");
                    }
                    case 6 -> {
                        labelCategory6.setText(category);
                        labelCategoryItem6.setText(productCount + " items");
                    }
                    case 7 -> {
                        labelCategory7.setText(category);
                        labelCategoryItem7.setText(productCount + " items");
                    }
                    case 8 -> {
                        labelCategory8.setText(category);
                        labelCategoryItem8.setText(productCount + " items");
                    }
                }
                index++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openCategoryView(String categoryName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/store/ProfileCategory.fxml"));
            Parent root = loader.load();

            ProfileCategoryController controller = loader.getController();
            controller.filterProductsByCategory(categoryName);

            Stage stage = (Stage) categoryComboBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML void handleCategoryBolts(ActionEvent event) { openCategoryView("Bolts"); }
    @FXML void handleCategoryElectrical(ActionEvent event) { openCategoryView("Electrical"); }
    @FXML void handleCategoryEngine(ActionEvent event) { openCategoryView("Engine"); }
    @FXML void handleCategoryExterior(ActionEvent event) { openCategoryView("Exterior"); }
    @FXML void handleCategoryOil(ActionEvent event) { openCategoryView("Oil"); }
    @FXML void handleCategorySuspension(ActionEvent event) { openCategoryView("Suspension"); }
    @FXML void handleCategoryTransmission(ActionEvent event) { openCategoryView("Transmission"); }
    @FXML void handleCategoryWheels(ActionEvent event) { openCategoryView("Wheels"); }

    // Navigation
    @FXML void handleDashboardClick(ActionEvent event) { loadScene(event, "/fxml/store/Dashboard.fxml"); }
    @FXML void handleInventoryClick(ActionEvent event) { loadScene(event, "/fxml/store/Inventory.fxml"); }
    @FXML void handleProductsClick(ActionEvent event) { loadScene(event, "/fxml/store/Products.fxml"); }
    @FXML void handleShopsClick(ActionEvent event) { loadScene(event, "/fxml/store/Shops.fxml"); }
    @FXML void handleProfileClick(ActionEvent event) { loadScene(event, "/fxml/store/Profile.fxml"); }
    @FXML void handleHomeButton(ActionEvent event) { loadScene(event, "/fxml/store/Profile.fxml"); }

    @FXML void handleLogOutButton(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SettingsWindowHelper.logout(currentStage);
    }
    @FXML void handleOpenSettings(ActionEvent event) { SettingsWindowHelper.openSettings((Node) event.getSource()); }

    private void loadScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
