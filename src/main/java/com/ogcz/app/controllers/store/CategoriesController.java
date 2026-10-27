package com.ogcz.app.controllers.store;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

import com.ogcz.app.SettingsWindowHelper;
import com.ogcz.app.database.DatabaseConnection;
import com.ogcz.app.database.Products;

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

public class CategoriesController implements Initializable {

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private Label categoryResult;

    @FXML
    private GridPane productsContainer;

    @FXML
    private ScrollPane scrollPane;

    public void setSelectedCategory(String category) {
        categoryComboBox.setValue(category);
        categoryResult.setText("" + category);
        loadProductsForCategory(category);
    }

    private void loadProductsForCategory(String category) {
        productsContainer.getChildren().clear(); // Clear previous results

        productsContainer.getColumnConstraints().clear();
        int TOTAL_WIDTH = 715;
        int COLUMN_COUNT = 5;
        int COLUMN_WIDTH = TOTAL_WIDTH / COLUMN_COUNT;

        for (int i = 0; i < COLUMN_COUNT; i++) {
            ColumnConstraints column = new ColumnConstraints(COLUMN_WIDTH);
            productsContainer.getColumnConstraints().add(column);
        }

        String query = """
                   SELECT p.*, u.first_name, u.last_name
                    FROM product p
                    JOIN user u ON p.seller_id = u.user_id
                    WHERE p.category_id = (SELECT category_id FROM category WHERE TRIM(name) = TRIM(?) LIMIT 1)
                """;

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();

            int col = 0;
            int row = 0;

            while (rs.next()) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/store/ProductsContainer.fxml"));
                VBox productCard = loader.load();
                productCard.setPrefWidth(COLUMN_WIDTH);
                productCard.setMinWidth(COLUMN_WIDTH);
                productCard.setMaxWidth(COLUMN_WIDTH);

                ProductsContainerController controller = loader.getController();
                Products product = new Products();

                product.setProductName(rs.getString("name"));
                product.setProductPrice(rs.getString("price"));

                String storeName = rs.getString("first_name") + " " + rs.getString("last_name");
                product.setStoreName(storeName);

                controller.setData(product);

                productsContainer.setMaxWidth(200);
                productsContainer.setMinWidth(200);

                if (col == 5) {
                    col = 0;
                    row++;
                }
                productsContainer.add(productCard, col++, row);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCategoriesFromDatabase();

        categoryComboBox.setOnAction(event -> {
            String selected = categoryComboBox.getValue();
            if (selected != null) {
                setSelectedCategory(selected);
            }
        });
    }

    private void loadCategoriesFromDatabase() {
        String query = "SELECT name FROM category";

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String categoryName = rs.getString("name");
                categoryComboBox.getItems().add(categoryName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // NAVIGATION

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
    void handleDashboardClick(ActionEvent event) {
        loadScene("/fxml/store/Dashboard.fxml", event);
    }

    @FXML
    void handleInventoryClick(ActionEvent event) {
        loadScene("/fxml/store/Inventory.fxml", event);
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
