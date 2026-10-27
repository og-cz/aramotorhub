package com.ogcz.app.controllers.store;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ogcz.app.SettingsWindowHelper;
import com.ogcz.app.database.DatabaseConnection;
import com.ogcz.app.database.Shops;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ShopsQuickViewController {

    @FXML
    private Label addressLabel;

    @FXML
    private Label barangayLabel;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private Label emailLabel;

    @FXML
    private Label firstLastNameLabel;

    @FXML
    private Label labelCategory1;

    @FXML
    private Label labelCategory2;

    @FXML
    private Label labelCategory3;

    @FXML
    private Label labelCategory4;

    @FXML
    private Label labelCategory5;

    @FXML
    private Label labelCategory6;

    @FXML
    private Label labelCategory7;

    @FXML
    private Label labelCategory8;

    @FXML
    private Label labelCategoryItem1;

    @FXML
    private Label labelCategoryItem2;

    @FXML
    private Label labelCategoryItem3;

    @FXML
    private Label labelCategoryItem4;

    @FXML
    private Label labelCategoryItem5;

    @FXML
    private Label labelCategoryItem6;

    @FXML
    private Label labelCategoryItem7;

    @FXML
    private Label labelCategoryItem8;

    @FXML
    private Label sellerAddress;

    @FXML
    private Label sellerEmail;

    @FXML
    private Label sellerName;

    @FXML
    private Label sellerBarangay;

        @FXML
    private GridPane productsContainer;



    @FXML
    void handleCategoryEngine(ActionEvent event) {
        openCategoryView("Engine");
    }

    @FXML
    void handleCategoryBolts(ActionEvent event) {
        openCategoryView("Bolts");
    }

    @FXML
    void handleCategoryElectrical(ActionEvent event) {
        openCategoryView("Electrical");
    }

    @FXML
    void handleCategoryExterior(ActionEvent event) {
        openCategoryView("Exterior");
    }

    @FXML
    void handleCategoryOil(ActionEvent event) {
        openCategoryView("Oil");
    }

    @FXML
    void handleCategorySuspension(ActionEvent event) {
        openCategoryView("Suspension");
    }

    @FXML
    void handleCategoryTransmission(ActionEvent event) {
        openCategoryView("Transmission");
    }

    @FXML
    void handleCategoryWheels(ActionEvent event) {
        openCategoryView("Wheels");
    }

    @FXML
    public void initialize() {
        setupCategoryComboBox();
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

    private void openCategoryView(String categoryName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/store/Categories.fxml"));
            Parent root = loader.load();

            CategoriesController controller = loader.getController();
            controller.setSelectedCategory(categoryName);

            Stage stage = (Stage) categoryComboBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setShopDetails(Shops shop) {
        sellerName.setText(shop.getShopName() != null ? shop.getShopName() : "N/A");
        sellerEmail.setText(shop.getShopEmail() != null ? shop.getShopEmail() : "N/A");
        sellerAddress.setText(shop.getShopAddress() != null ? shop.getShopAddress() : "N/A");
        sellerBarangay.setText(shop.getShopBarangay() != null ? shop.getShopBarangay() : "N/A");

        loadCategoryDataForShop(shop.getShopId());
    }

    public void loadCategoryDataForShop(int sellerId) {
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

            stmt.setInt(1, sellerId);
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

    @FXML
    void handleDashboardClick(ActionEvent event) {
        loadScene("/fxml/store/Dashboard.fxml", event);
    }

    @FXML
    void handleInventoryClick(ActionEvent event) {
        loadScene("/fxml/store/Inventory.fxml", event);
    }

    @FXML
    void handleHomeButton(ActionEvent event) {
        loadScene("/fxml/store/Profile.fxml", event);
    }

    @FXML
    void handleLogOutButton(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SettingsWindowHelper.logout(currentStage);
    }

    @FXML
    void handleOpenSettings(ActionEvent event) {
        SettingsWindowHelper.openSettings((Node) event.getSource());
    }

    @FXML
    void handleProductsClick(ActionEvent event) {
        loadScene("/fxml/store/Products.fxml", event);
    }

    @FXML
    void handleProfileClick(ActionEvent event) {
        loadScene("/fxml/store/Profile.fxml", event);
    }

    @FXML
    void handleShopsClick(ActionEvent event) {
        loadScene("/fxml/store/Shops.fxml", event);
    }

    private void loadScene(String fxmlPath, ActionEvent event) {
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
