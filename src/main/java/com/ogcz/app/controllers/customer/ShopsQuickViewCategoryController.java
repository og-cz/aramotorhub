package com.ogcz.app.controllers.customer;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

import com.ogcz.app.SettingsWindowHelper;
import com.ogcz.app.database.DatabaseConnection;
import com.ogcz.app.database.Products;
import com.ogcz.app.database.Shops;

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

public class ShopsQuickViewCategoryController implements Initializable {

    private static final int TOTAL_WIDTH = 715;
    private static final int COLUMN_COUNT = 5;
    private static final int COLUMN_WIDTH = TOTAL_WIDTH / COLUMN_COUNT; // 141px

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

    private Shops shop;


     @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoryComboBox.setPromptText("Category");

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement("SELECT name FROM category");
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
                openCategoriesPage(selectedCategory);
            }
        });
    }

    private void openCategoriesPage(String category) {
        System.out.println("Opening category: " + category);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer/Categories.fxml"));
            Parent newRoot = loader.load();

            CategoriesController controller = loader.getController();
            controller.setSelectedCategory(category);

            Stage currentStage = (Stage) categoryComboBox.getScene().getWindow();
            currentStage.setScene(new Scene(newRoot));
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setShopDetails(Shops shop) {
        this.shop = shop;

        if (shop != null && shop.getShopId() > 0) {
            loadCategoryData(shop.getShopId());
        }

    }

    public void filterByCategory(String categoryName, Label sellerName) {
        if (shop == null || shop.getShopId() <= 0 || categoryName == null || categoryName.isEmpty()) {
            return;
        }

        String query = """
                    SELECT p.*
                    FROM product p
                    JOIN category c ON p.category_id = c.category_id
                    JOIN user u ON p.seller_id = u.user_id
                    WHERE p.seller_id = ? AND TRIM(c.name) = TRIM(?) AND p.status = 'AVAILABLE'
                    ORDER BY p.product_id
                """;

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, shop.getShopId());
            stmt.setString(2, categoryName);

            ResultSet rs = stmt.executeQuery();

            gridPane.getChildren().clear();
            gridPane.getColumnConstraints().clear();

            for (int i = 0; i < COLUMN_COUNT; i++) {
                ColumnConstraints column = new ColumnConstraints();
                column.setPrefWidth(COLUMN_WIDTH);
                column.setMinWidth(COLUMN_WIDTH);
                column.setMaxWidth(COLUMN_WIDTH);
                gridPane.getColumnConstraints().add(column);
            }

            int column = 0;
            int row = 1;

            while (rs.next()) {
                Products product = new Products();
                product.setProductId(rs.getInt("product_id"));
                product.setProductName(rs.getString("name"));
                product.setProductPrice("" + rs.getDouble("price"));
                product.setStock(rs.getInt("stock"));
                product.setSellerId(rs.getInt("seller_id"));
                product.setStoreName(sellerName.getText());

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/customer/ProductsContainer.fxml"));
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

    private void loadCategoryData(int sellerId) {
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



    @FXML
    void handleBackToShop(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer/ShopsQuickView.fxml"));
            Parent newRoot = loader.load();

            ShopsQuickViewController controller = loader.getController();
            controller.setShopDetails(this.shop);

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(newRoot));
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToCategory(String categoryName, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/customer/ShopsQuickViewCategory.fxml"));
            Parent newRoot = loader.load();

            ShopsQuickViewCategoryController controller = loader.getController();
            controller.setShopDetails(shop);
            controller.filterByCategory(categoryName, new Label(shop.getShopName()));

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(newRoot));
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     @FXML
    void handleCategoryBolts(ActionEvent event) {
        navigateToCategory("Bolts", event);
    }

    @FXML
    void handleCategoryElectrical(ActionEvent event) {
        navigateToCategory("Electrical", event);
    }

    @FXML
    void handleCategoryEngine(ActionEvent event) {
        navigateToCategory("Engine", event);
    }

    @FXML
    void handleCategoryExterior(ActionEvent event) {
        navigateToCategory("Exterior", event);
    }

    @FXML
    void handleCategoryOil(ActionEvent event) {
        navigateToCategory("Oil", event);
    }

    @FXML
    void handleCategorySuspension(ActionEvent event) {
        navigateToCategory("Suspension", event);
    }

    @FXML
    void handleCategoryTransmission(ActionEvent event) {
        navigateToCategory("Transmission", event);
    }

    @FXML
    void handleCategoryWheels(ActionEvent event) {
        navigateToCategory("Wheels", event);
    }

    @FXML
    void handleCartClick(ActionEvent event) {
        System.out.println("handleCartClick triggered");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer/Cart.fxml"));
            Parent newRoot = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(newRoot));
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleFavoriteClick(ActionEvent event) {
        System.out.println("handleFavoriteClick triggered");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer/Favorite.fxml"));
            Parent newRoot = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(newRoot));
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleHomeButton(ActionEvent event) {
        System.out.println("handleHomeButton triggered");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer/HomeMain.fxml"));
            Parent newRoot = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(newRoot));
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogOutButton(ActionEvent event) {
        System.out.println("Logout clicked");
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SettingsWindowHelper.logout(currentStage);
    }

    @FXML
    void handleOpenSettings(ActionEvent event) {
        SettingsWindowHelper.openSettings((Node) event.getSource());
    }

    @FXML
    void handleProfileClick(ActionEvent event) {
           try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer/Profile.fxml"));
            Parent newRoot = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(newRoot));
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleProductsClick(ActionEvent event) {
        System.out.println("handleProductsClick triggered");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer/Products.fxml"));
            Parent newRoot = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(newRoot));
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleShopsClick(ActionEvent event) {
        System.out.println("handleShopsClick triggered");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer/Shops.fxml"));
            Parent newRoot = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(newRoot));
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
