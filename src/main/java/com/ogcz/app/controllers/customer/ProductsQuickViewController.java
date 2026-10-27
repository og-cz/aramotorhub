package com.ogcz.app.controllers.customer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ogcz.app.database.CartService;
import com.ogcz.app.database.DatabaseConnection;
import com.ogcz.app.database.Products;
import com.ogcz.app.database.Shops;
import com.ogcz.app.database.UserSession;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ProductsQuickViewController {

    @FXML
    private Label nameLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private BorderPane rootPane;

    @FXML
    private Spinner<Integer> spinnerQuantity;

    @FXML
    private Label storeLabel;

    private Products selectedProduct;

    @FXML
    void handleAddToCartClick(ActionEvent event) {
        if (selectedProduct == null)
            return;

        int quantity = spinnerQuantity.getValue();
        String error = CartService.addToCart(UserSession.getCurrentUser().getUserId(),
                selectedProduct.getProductId(), quantity);

        Alert alert = new Alert(error == null ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
        alert.setTitle("ARA Motorhub");
        alert.setContentText(error == null ? "Product added in the cart successfully!" : error);
        alert.setHeaderText(null);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/assets/z_favicon.png").toString()));
        alert.showAndWait();
    }

    @FXML
    void handleViewShop(ActionEvent event) {
        if (selectedProduct == null)
            return;

        int sellerId = selectedProduct.getSellerId();

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT first_name, last_name, email, address, barangay FROM user WHERE user_id = ?")) {
            stmt.setInt(1, sellerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Shops shop = new Shops();
                shop.setShopId(sellerId);
                shop.setShopName(rs.getString("first_name") + " " + rs.getString("last_name"));
                shop.setShopEmail(rs.getString("email"));
                shop.setShopAddress(rs.getString("address"));
                shop.setShopBarangay(rs.getString("barangay"));

                Stage popupStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                popupStage.close();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer/ShopsQuickView.fxml"));
                Parent root = loader.load();

                ShopsQuickViewController controller = loader.getController();
                controller.setShopDetails(shop);

                Stage mainStage = UserSession.getMainStage();
                mainStage.setScene(new Scene(root));
                mainStage.show();
            } else {
                System.out.println("Seller not found with ID: " + sellerId);
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void setProductData(Products product) {
        this.selectedProduct = product;
        nameLabel.setText(product.getProductName());
        priceLabel.setText(product.getProductPrice());
        storeLabel.setText(product.getStoreName());

        int max = product.getStock() > 0 ? product.getStock() : 99;
        spinnerQuantity
                .setValueFactory(new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, 1));
    }
}
