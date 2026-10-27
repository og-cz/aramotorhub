package com.ogcz.app.controllers.customer;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.ogcz.app.SettingsWindowHelper;
import com.ogcz.app.database.Shops;
import com.ogcz.app.database.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ShopsController implements Initializable {

    @FXML
    private VBox shopCardLayout;

    @FXML
    private ComboBox<String> categoryComboBox;

    private List<Shops> nearestShop;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nearestShop = fetchShopsFromDB();

        nearestShop.sort(Comparator.comparingInt(shop -> {
            try {
                return Integer.parseInt(shop.getShopBarangay().replaceAll("\\D+", ""));
            } catch (NumberFormatException e) {
                return Integer.MAX_VALUE;
            }
        }));

        categoryComboBox.setPromptText("Category");

        try (Connection conn = DatabaseConnection.connect()) {
            if (conn == null) {
                System.out.println("Database connection failed.");
                return;
            }

            PreparedStatement stmt = conn.prepareStatement("SELECT name FROM category");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                categoryComboBox.getItems().add(rs.getString("name"));
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        categoryComboBox.setOnAction(event -> {
            String selectedCategory = categoryComboBox.getValue();
            if (selectedCategory != null && !selectedCategory.isEmpty()) {
                openCategoriesPage(selectedCategory);
            }
        });

        try {
            for (Shops shop : nearestShop) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/fxml/customer/ShopsCardLayout.fxml"));

                HBox cardBox = fxmlLoader.load();
                ShopsCardLayoutController controller = fxmlLoader.getController();
                controller.setData(shop);
                shopCardLayout.getChildren().add(cardBox);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Shops> fetchShopsFromDB() {
        List<Shops> shopsList = new ArrayList<>();

        String sql = "SELECT user_id, first_name, last_name, email, address, barangay " +
                "FROM user " +
                "WHERE role = 'SELLER' AND first_name IS NOT NULL AND last_name IS NOT NULL " +
                "AND first_name <> '' AND last_name <> '';";

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Shops shop = new Shops();
                shop.setShopId(rs.getInt("user_id"));
                shop.setShopName(rs.getString("first_name") + " " + rs.getString("last_name"));
                shop.setShopEmail(rs.getString("email"));
                shop.setShopAddress(rs.getString("address"));
                shop.setShopBarangay(rs.getString("barangay"));
                shop.setShopDistance(rs.getString("barangay"));

                shopsList.add(shop);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return shopsList;
    }

    @FXML
    void handleHomeButton(ActionEvent event) {
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
    private void handleOpenSettings(ActionEvent event) {
        SettingsWindowHelper.openSettings((Node) event.getSource());
    }

    @FXML
    private void handleLogOutButton(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SettingsWindowHelper.logout(currentStage);
    }

    @FXML
    private void handleProductsClick(ActionEvent event) {
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
    void handleFavoriteClick(ActionEvent event) {
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
    private void handleCartClick(ActionEvent event) {
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
    private void handleProfileClick(ActionEvent event) {
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
}
