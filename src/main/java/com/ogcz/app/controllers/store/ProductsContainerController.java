package com.ogcz.app.controllers.store;

import com.ogcz.app.database.Products;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class ProductsContainerController {

    @FXML
    private ImageView productImage;

    @FXML
    private Label productName;

    @FXML
    private Label productPrice;

    @FXML
    private Label storeName;

    public void setData(Products product) {
        productName.setText(product.getProductName());
        productPrice.setText(product.getProductPrice());
        storeName.setText(product.getStoreName()); // full name of seller

    }



    @FXML
    void addToCartHandle(ActionEvent event) {

        showAlert("You can't add your own products to the cart.");

    }

    @FXML
    void quickViewHandle(ActionEvent event) {

        showAlert("Quick View is disabled in seller mode.");

    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notice");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
