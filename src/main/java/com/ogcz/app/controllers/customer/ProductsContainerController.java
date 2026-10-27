package com.ogcz.app.controllers.customer;

import java.io.IOException;

import com.ogcz.app.database.CartService;
import com.ogcz.app.database.Products;
import com.ogcz.app.database.UserSession;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class ProductsContainerController {

    private Products product;

    @FXML
    private ImageView productImage;

    @FXML
    private Label productName;

    @FXML
    private Label productPrice;

    @FXML
    private Label storeName;

    public void setData(Products product) {
        this.product = product;
        productName.setText(product.getProductName());
        productPrice.setText(product.getProductPrice());
        storeName.setText(product.getStoreName());
    }

    @FXML
    void addToCartHandle(ActionEvent event) {
        String error = CartService.addToCart(UserSession.getCurrentUser().getUserId(), product.getProductId(), 1);

        Alert alert = new Alert(error == null ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
        alert.setTitle("ARA Motorhub");
        alert.setContentText(error == null ? "Product added in the cart successfully!" : error);
        alert.setHeaderText(null);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/assets/z_favicon.png").toString()));
        alert.showAndWait();
    }

    @FXML
    void quickViewHandle(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer/ProductsQuickView.fxml"));
            Parent root = loader.load();

            ProductsQuickViewController controller = loader.getController();
            controller.setProductData(product);

            Stage stage = new Stage();
            stage.setResizable(false);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/assets/z_favicon.png")));
            stage.setTitle("ARA Motorhub: Quick View");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
