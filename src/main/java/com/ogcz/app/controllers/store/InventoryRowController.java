package com.ogcz.app.controllers.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ogcz.app.database.DatabaseConnection;
import com.ogcz.app.database.Products;
import com.ogcz.app.database.UserSession;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class InventoryRowController {

    @FXML
    private CheckBox handleCheckBox;

    @FXML
    private Label labelCategory;

    @FXML
    private Label labelPrice;

    @FXML
    private Label labelProductName;

    @FXML
    private Label labelStatus;

    @FXML
    private Label labelStock;

    @FXML
    private StackPane rootStackPane;

    private int productId;

    public void setProduct(Products product) {
        this.productId = product.getProductId();
        labelProductName.setText(product.getProductName());
        labelPrice.setText("₱" + product.getProductPrice());
        labelStock.setText("Stock: " + product.getStock());
        labelStatus.setText(product.getStatus());
        labelCategory.setText("Category ID: " + product.getCategoryId());
    }

    @FXML
    void handleDeleteProduct(ActionEvent event) {
        String productName = labelProductName.getText();

        try (Connection conn = DatabaseConnection.connect()) {
            if (hasOrders(conn)) {
                showAlert("This product already has orders, so it can't be deleted. Mark it as out of stock instead.",
                        Alert.AlertType.WARNING);
                return;
            }

            conn.setAutoCommit(false);
            int affected;
            try (PreparedStatement cartStmt = conn.prepareStatement("DELETE FROM cart_item WHERE product_id = ?");
                    PreparedStatement stmt = conn.prepareStatement(
                            "DELETE FROM product WHERE product_id = ? AND seller_id = ?")) {
                cartStmt.setInt(1, productId);
                cartStmt.executeUpdate();

                stmt.setInt(1, productId);
                stmt.setInt(2, UserSession.getCurrentUser().getUserId());
                affected = stmt.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

            if (affected > 0) {
                if (rootStackPane.getParent() instanceof Pane pane) {
                    pane.getChildren().remove(rootStackPane);
                }
                showAlert("Product \"" + productName + "\" deleted.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("No product deleted.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("An error occurred while deleting product.", Alert.AlertType.ERROR);
        }
    }

    private boolean hasOrders(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM order_item WHERE product_id = ? LIMIT 1")) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @FXML
    void handleSetAvailable(ActionEvent event) {
        updateStatus("AVAILABLE");
    }

    @FXML
    void handleSetOutOfStock(ActionEvent event) {
        updateStatus("OUT_OF_STOCK");
    }

    private void updateStatus(String newStatus) {
        String productName = labelProductName.getText();

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE product SET status = ? WHERE product_id = ? AND seller_id = ?")) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, productId);
            stmt.setInt(3, UserSession.getCurrentUser().getUserId());
            int affected = stmt.executeUpdate();

            if (affected > 0) {
                labelStatus.setText(newStatus);
                showAlert("Product \"" + productName + "\" marked as " + newStatus + ".",
                        Alert.AlertType.INFORMATION);
            } else {
                showAlert("No product updated.", Alert.AlertType.WARNING);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("An error occurred while updating status.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("ARA Motorhub");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
