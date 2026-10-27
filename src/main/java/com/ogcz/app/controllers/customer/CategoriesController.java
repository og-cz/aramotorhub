package com.ogcz.app.controllers.customer;

import com.ogcz.app.SettingsWindowHelper;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CategoriesController implements Initializable {

    private String selectedCategory;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private Label categoryResult;
    @FXML
    private GridPane productsContainer;

    private static final int TOTAL_WIDTH = 715;
    private static final int COLUMN_COUNT = 5;
    private static final int COLUMN_WIDTH = TOTAL_WIDTH / COLUMN_COUNT;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoryComboBox.setPromptText("Category");

        try {
            var conn = com.ogcz.app.database.DatabaseConnection.connect();
            if (conn == null) {
                System.out.println("Database connection failed.");
                return;
            }

            var stmt = conn.prepareStatement("SELECT name FROM category");
            var rs = stmt.executeQuery();

            while (rs.next()) {
                categoryComboBox.getItems().add(rs.getString("name"));
            }

            rs.close();
            stmt.close();
            conn.close();
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
            Scene newScene = new Scene(newRoot);
            currentStage.setScene(newScene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSelectedCategory(String category) {
        this.selectedCategory = category;
        System.out.println("Category set: " + category);

        if (categoryResult != null) {
            categoryResult.setText(category);
        }

        loadProductsForCategory();
    }

    public void loadProductsForCategory() {
        List<Products> productList = Products.getProductsByCategory(selectedCategory);
        System.out.println("Products found: " + productList.size());

        productsContainer.getChildren().clear();
        productsContainer.getColumnConstraints().clear();
        productsContainer.setMaxWidth(200);
        productsContainer.setMinWidth(200);

        for (int i = 0; i < COLUMN_COUNT; i++) {
            ColumnConstraints column = new ColumnConstraints(COLUMN_WIDTH);
            productsContainer.getColumnConstraints().add(column);
        }

        int column = 0;
        int row = 0;

        try {
            for (Products product : productList) {
                FXMLLoader fxmlLoader = new FXMLLoader(
                        getClass().getResource("/fxml/customer/ProductsContainer.fxml"));
                VBox productBox = fxmlLoader.load();

                productBox.setPrefWidth(COLUMN_WIDTH);
                productBox.setMinWidth(COLUMN_WIDTH);
                productBox.setMaxWidth(COLUMN_WIDTH);

                ProductsContainerController controller = fxmlLoader.getController();
                controller.setData(product);

                productsContainer.add(productBox, column, row);
                /// GridPane.setMargin(productBox, new Insets()); // consistent with
                /// ProductsController

                column++;
                if (column >= COLUMN_COUNT) {
                    column = 0;
                    row++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Navigation Handlers
    @FXML
    void handleCartClick(ActionEvent event) {
        navigateTo(event, "/fxml/customer/Cart.fxml");
    }

    @FXML
    void handleFavoriteClick(ActionEvent event) {
        navigateTo(event, "/fxml/customer/Favorite.fxml");
    }

    @FXML
    void handleProductsClick(ActionEvent event) {
        navigateTo(event, "/fxml/customer/Products.fxml");
    }

    @FXML
    void handleHomeButton(ActionEvent event) {
        navigateTo(event, "/fxml/customer/HomeMain.fxml");
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
        navigateTo(event, "/fxml/customer/Profile.fxml");
    }

    @FXML
    void handleShopsClick(ActionEvent event) {
        navigateTo(event, "/fxml/customer/Shops.fxml");
    }

    private void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(newRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}