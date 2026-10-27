package com.ogcz.app.controllers.store;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

import com.ogcz.app.SettingsWindowHelper;
import com.ogcz.app.database.DatabaseConnection;
import com.ogcz.app.database.Products;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ProfileCategoryController implements Initializable {

    private static final int TOTAL_WIDTH = 714;
    private static final int COLUMN_COUNT = 5;
    private static final int COLUMN_WIDTH = TOTAL_WIDTH / COLUMN_COUNT;

    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private GridPane gridPane;
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Label labelCategory1, labelCategory2, labelCategory3, labelCategory4;
    @FXML
    private Label labelCategory5, labelCategory6, labelCategory7, labelCategory8;
    @FXML
    private Label labelCategoryItem1, labelCategoryItem2, labelCategoryItem3, labelCategoryItem4;
    @FXML
    private Label labelCategoryItem5, labelCategoryItem6, labelCategoryItem7, labelCategoryItem8;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupCategoryComboBox();
        loadCategoryStats();
    }

    private void setupCategoryComboBox() {
        categoryComboBox.setPromptText("Category");

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement("SELECT name FROM category ORDER BY name ASC");
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categoryComboBox.getItems().add(rs.getString("name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        categoryComboBox.setOnAction(event -> {
            String selectedCategory = categoryComboBox.getValue();
            if (selectedCategory != null && !selectedCategory.isEmpty()) {
                filterProductsByCategory(selectedCategory);
            }
        });
    }

    private void loadCategoryStats() {
        int sellerId = UserSession.getCurrentUser().getUserId();
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
                int count = rs.getInt("product_count");

                switch (index) {
                    case 1 -> {
                        labelCategory1.setText(category);
                        labelCategoryItem1.setText(count + " items");
                    }
                    case 2 -> {
                        labelCategory2.setText(category);
                        labelCategoryItem2.setText(count + " items");
                    }
                    case 3 -> {
                        labelCategory3.setText(category);
                        labelCategoryItem3.setText(count + " items");
                    }
                    case 4 -> {
                        labelCategory4.setText(category);
                        labelCategoryItem4.setText(count + " items");
                    }
                    case 5 -> {
                        labelCategory5.setText(category);
                        labelCategoryItem5.setText(count + " items");
                    }
                    case 6 -> {
                        labelCategory6.setText(category);
                        labelCategoryItem6.setText(count + " items");
                    }
                    case 7 -> {
                        labelCategory7.setText(category);
                        labelCategoryItem7.setText(count + " items");
                    }
                    case 8 -> {
                        labelCategory8.setText(category);
                        labelCategoryItem8.setText(count + " items");
                    }
                }
                index++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void filterProductsByCategory(String category) {
    gridPane.getChildren().clear();
    gridPane.getColumnConstraints().clear();

    for (int i = 0; i < COLUMN_COUNT; i++) {
        ColumnConstraints column = new ColumnConstraints(COLUMN_WIDTH);
        gridPane.getColumnConstraints().add(column);
    }

    gridPane.setHgap(0);
    gridPane.setVgap(0);

    int sellerId = UserSession.getCurrentUser().getUserId();
    String query = """
                SELECT p.product_id, p.name AS product_name, p.price, u.first_name, u.last_name
                FROM product p
                JOIN category c ON p.category_id = c.category_id
                JOIN user u ON p.seller_id = u.user_id
                WHERE p.seller_id = ? AND TRIM(c.name) = TRIM(?) AND p.status = 'AVAILABLE'
                ORDER BY p.created_at DESC
            """;

    try (Connection conn = DatabaseConnection.connect();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, sellerId);
        stmt.setString(2, category);
        ResultSet rs = stmt.executeQuery();

        int column = 0;
        int row = 1;

        while (rs.next()) {
            Products product = new Products();


            product.setProductId(rs.getInt("product_id"));
            product.setProductName(rs.getString("product_name"));
            product.setProductPrice(String.valueOf(rs.getInt("price")));
            product.setStoreName(rs.getString("first_name") + " " + rs.getString("last_name"));

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/store/ProductsContainer.fxml"));
            VBox productBox = loader.load();

            productBox.setPrefWidth(COLUMN_WIDTH);
            productBox.setMaxWidth(COLUMN_WIDTH);
            productBox.setMinWidth(COLUMN_WIDTH);

            ProductsContainerController controller = loader.getController();
            controller.setData(product);

            gridPane.add(productBox, column++, row);
            if (column == COLUMN_COUNT) {
                column = 0;
                row++;
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    // Category Quick Access
    @FXML
    void handleCategoryBolts(ActionEvent event) {
        filterProductsByCategory("Bolts");
    }

    @FXML
    void handleCategoryElectrical(ActionEvent event) {
        filterProductsByCategory("Electrical");
    }

    @FXML
    void handleCategoryEngine(ActionEvent event) {
        filterProductsByCategory("Engine");
    }

    @FXML
    void handleCategoryExterior(ActionEvent event) {
        filterProductsByCategory("Exterior");
    }

    @FXML
    void handleCategoryOil(ActionEvent event) {
        filterProductsByCategory("Oil");
    }

    @FXML
    void handleCategorySuspension(ActionEvent event) {
        filterProductsByCategory("Suspension");
    }

    @FXML
    void handleCategoryTransmission(ActionEvent event) {
        filterProductsByCategory("Transmission");
    }

    @FXML
    void handleCategoryWheels(ActionEvent event) {
        filterProductsByCategory("Wheels");
    }

    // Navigation
    @FXML
    void handleBackToShop(ActionEvent event) {
        navigateTo("/fxml/store/Profile.fxml", event);
    }

    @FXML
    void handleProfileClick(ActionEvent event) {
        navigateTo("/fxml/store/Profile.fxml", event);
    }

    @FXML
    void handleDashboardClick(ActionEvent event) {
        navigateTo("/fxml/store/Dashboard.fxml", event);
    }

    @FXML
    void handleInventoryClick(ActionEvent event) {
        navigateTo("/fxml/store/Inventory.fxml", event);
    }

    @FXML
    void handleHomeButton(ActionEvent event) {
        navigateTo("/fxml/store/Profile.fxml", event);
    }

    @FXML
    void handleLogOutButton(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SettingsWindowHelper.logout(stage);
    }

    @FXML
    void handleOpenSettings(ActionEvent event) {
        SettingsWindowHelper.openSettings((Node) event.getSource());
    }

    @FXML
    void handleProductsClick(ActionEvent event) {
        navigateTo("/fxml/store/Products.fxml", event);
    }

    @FXML
    void handleShopsClick(ActionEvent event) {
        navigateTo("/fxml/store/Shops.fxml", event);
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
