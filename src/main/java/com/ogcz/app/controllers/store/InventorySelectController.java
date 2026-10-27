package com.ogcz.app.controllers.store;

import com.ogcz.app.SettingsWindowHelper;
import com.ogcz.app.database.DatabaseConnection;
import com.ogcz.app.database.Products;
import com.ogcz.app.database.User;
import com.ogcz.app.database.UserSession;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class InventorySelectController implements Initializable {

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCategoryData();
    }

    private void openCategoryView(String categoryName, Stage currentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/store/InventorySelect.fxml"));
            Parent root = loader.load();

            InventorySelectController controller = loader.getController();

            User currentUser = UserSession.getCurrentUser();
            if (currentUser != null) {
                controller.setSellerId(currentUser.getUserId());
            }

            controller.loadProductsByCategory(categoryName);

            currentStage.setScene(new Scene(root));
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCategoryData() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null)
            return;

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

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Stage getStage(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    @FXML
    void handleCategoryBolts(ActionEvent event) {
        openCategoryView("Bolts", getStage(event));
    }

    @FXML
    void handleCategoryElectrical(ActionEvent event) {
        openCategoryView("Electrical", getStage(event));
    }

    @FXML
    void handleCategoryEngine(ActionEvent event) {
        openCategoryView("Engine", getStage(event));
    }

    @FXML
    void handleCategoryExterior(ActionEvent event) {
        openCategoryView("Exterior", getStage(event));
    }

    @FXML
    void handleCategoryOil(ActionEvent event) {
        openCategoryView("Oil", getStage(event));
    }

    @FXML
    void handleCategorySuspension(ActionEvent event) {
        openCategoryView("Suspension", getStage(event));
    }

    @FXML
    void handleCategoryTransmission(ActionEvent event) {
        openCategoryView("Transmission", getStage(event));
    }

    @FXML
    void handleCategoryWheels(ActionEvent event) {
        openCategoryView("Wheels", getStage(event));
    }

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private VBox inventoryContainer;

    private int sellerId;

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public void loadProductsByCategory(String category) {
        categoryComboBox.setValue(category);
        inventoryContainer.getChildren().clear();

        List<Products> products = Products.getProductsBySellerIdAndCategory(sellerId, category);
        for (Products product : products) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/store/InventoryRow.fxml"));
                Node row = loader.load();

                InventoryRowController controller = loader.getController();
                controller.setProduct(product);

                inventoryContainer.getChildren().add(row);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleInventoryClick(ActionEvent event) {
        loadScene(event, "/fxml/store/Inventory.fxml");
    }

    @FXML
    void handleDashboardClick(ActionEvent event) {
        loadScene(event, "/fxml/store/Dashboard.fxml");
    }

    @FXML
    void handleHomeButton(ActionEvent event) {
        loadScene(event, "/fxml/store/Profile.fxml");
    }

    @FXML
    void handleProductsClick(ActionEvent event) {
        loadScene(event, "/fxml/store/Products.fxml");
    }

    @FXML
    void handleProfileClick(ActionEvent event) {
        loadScene(event, "/fxml/store/Profile.fxml");
    }

    @FXML
    void handleShopsClick(ActionEvent event) {
        loadScene(event, "/fxml/store/Shops.fxml");
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

    private void loadScene(ActionEvent event, String fxmlPath) {
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
