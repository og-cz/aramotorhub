package com.ogcz.app.controllers.store;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;

import com.ogcz.app.SettingsWindowHelper;
import com.ogcz.app.database.DatabaseConnection;
import com.ogcz.app.database.Products;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ProductsController implements Initializable {

    @FXML
    private GridPane productsContainer;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private ScrollPane scrollPane;

    private static final int TOTAL_WIDTH = 715;
    private static final int COLUMN_COUNT = 5;
    private static final int COLUMN_WIDTH = TOTAL_WIDTH / COLUMN_COUNT;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Products> productsList = getAllProducts();

        productsContainer.getColumnConstraints().clear();
        for (int i = 0; i < COLUMN_COUNT; i++) {
            ColumnConstraints column = new ColumnConstraints(COLUMN_WIDTH);
            productsContainer.getColumnConstraints().add(column);
        }

        productsContainer.setHgap(0);
        productsContainer.setVgap(0);

        int column = 0;
        int row = 1;

        try {
            for (Products product : productsList) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/fxml/store/ProductsContainer.fxml"));
                VBox productBox = fxmlLoader.load();

                productBox.setPrefWidth(COLUMN_WIDTH);
                productBox.setMaxWidth(COLUMN_WIDTH);
                productBox.setMinWidth(COLUMN_WIDTH);

                ProductsContainerController controller = fxmlLoader.getController();
                controller.setData(product);

                productsContainer.setMaxWidth(200);
                productsContainer.setMinWidth(200);
                if (column == COLUMN_COUNT) {
                    column = 0;
                    ++row;
                }

                productsContainer.add(productBox, column++, row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        setupCategoryComboBox();
    }

    private void setupCategoryComboBox() {
        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement("SELECT name FROM category");
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categoryComboBox.getItems().add(rs.getString("name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        categoryComboBox.setOnAction(event -> {
            String selectedCategory = categoryComboBox.getValue();
            if (selectedCategory != null && !selectedCategory.isEmpty()) {
                loadProductsByCategory(selectedCategory);
            }
        });
    }

    private void loadProductsByCategory(String categoryName) {
        productsContainer.getChildren().clear();

        String sql = """
                    SELECT p.product_id, p.name AS product_name, p.price, p.seller_id,
                           u.first_name, u.last_name
                    FROM product p
                    JOIN user u ON p.seller_id = u.user_id
                    WHERE p.category_id = (SELECT category_id FROM category WHERE TRIM(name) = TRIM(?) LIMIT 1)
                """;

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();

            int column = 0;
            int row = 1;

            while (rs.next()) {
                Products p = new Products();
                p.setProductId(rs.getInt("product_id"));
                p.setProductName(rs.getString("product_name"));
                p.setProductPrice(rs.getString("price"));
                p.setSellerId(rs.getInt("seller_id"));

                String storeName = rs.getString("first_name") + " " + rs.getString("last_name");
                p.setStoreName(storeName);

                FXMLLoader fxmlLoader = new FXMLLoader(
                        getClass().getResource("/fxml/store/ProductsContainer.fxml"));
                VBox productBox = fxmlLoader.load();

                productBox.setPrefWidth(COLUMN_WIDTH);
                productBox.setMaxWidth(COLUMN_WIDTH);
                productBox.setMinWidth(COLUMN_WIDTH);

                ProductsContainerController controller = fxmlLoader.getController();
                controller.setData(p);

                if (column == COLUMN_COUNT) {
                    column = 0;
                    ++row;
                }

                productsContainer.add(productBox, column++, row);
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private List<Products> getAllProducts() {
        List<Products> list = new ArrayList<>();

        String sql = """
                    SELECT
                        p.product_id,
                        p.name AS product_name,
                        p.price,
                        p.seller_id,
                        u.first_name,
                        u.last_name
                    FROM product p
                    JOIN user u ON p.seller_id = u.user_id
                    ORDER BY p.name ASC
                    LIMIT 48
                """;

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Products p = new Products();
                p.setProductId(rs.getInt("product_id"));
                p.setProductName(rs.getString("product_name"));
                p.setProductPrice(rs.getString("price"));
                p.setSellerId(rs.getInt("seller_id"));

                String storeName = rs.getString("first_name") + " " + rs.getString("last_name");
                p.setStoreName(storeName);

                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
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
    void handleInventoryClick(ActionEvent event) {
        loadScene(event, "/fxml/store/Inventory.fxml");
    }

    @FXML
    void handleLogOutButton(ActionEvent event) {
        SettingsWindowHelper.logout((Stage) ((Node) event.getSource()).getScene().getWindow());
    }

    @FXML
    void handleOpenSettings(ActionEvent event) {
        SettingsWindowHelper.openSettings((Node) event.getSource());
    }

    @FXML
    void handleProfileClick(ActionEvent event) {
        loadScene(event, "/fxml/store/Profile.fxml");
    }

    @FXML
    void handleShopsClick(ActionEvent event) {
        loadScene(event, "/fxml/store/Shops.fxml");
    }

    private void loadScene(ActionEvent event, String fxmlPath) {
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
