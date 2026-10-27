package com.ogcz.app.controllers.admin;

import com.ogcz.app.database.DatabaseConnection;
import com.ogcz.app.database.Order;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class AdminOrderController {

    @FXML
    private TableColumn<Order, Integer> orderIdColumn;

    @FXML
    private TableColumn<Order, Integer> customerIdColumn;

    @FXML
    private TableColumn<Order, String> orderAddressColumn;

    @FXML
    private TableColumn<Order, String> orderStatusColumn;

    @FXML
    private TableColumn<Order, Double> totalPriceColumn;

    @FXML
    private TableColumn<Order, Timestamp> orderCreatedAtColumn;

    @FXML
    private TableView<Order> orderTable;

    private ObservableList<Order> orderList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        orderAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        orderCreatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        loadOrders();
    }

    private void loadOrders() {
        orderList.clear();
        String sql = "SELECT * FROM `order`";
        try (Connection conn = DatabaseConnection.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getString("address"),
                        rs.getString("status"),
                        rs.getDouble("total_price"),
                        rs.getTimestamp("created_at"));
                orderList.add(order);
            }
            orderTable.setItems(orderList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to load orders.");
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select an order to delete.");
            return;
        }

        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement itemsStmt = conn.prepareStatement("DELETE FROM order_item WHERE order_id = ?");
                    PreparedStatement pstmt = conn.prepareStatement("DELETE FROM `order` WHERE order_id = ?")) {
                itemsStmt.setInt(1, selectedOrder.getOrderId());
                itemsStmt.executeUpdate();
                pstmt.setInt(1, selectedOrder.getOrderId());
                pstmt.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Order deleted successfully.");
            loadOrders();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to delete the order.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    @FXML
    void handleBack(ActionEvent e) {
        switchScene(e, "/fxml/admin/Admin.fxml");
    }

    @FXML
    void handleCategory(ActionEvent e) {
        switchScene(e, "/fxml/admin/AdminCategory.fxml");
    }

    @FXML
    void handleProduct(ActionEvent e) {
        switchScene(e, "/fxml/admin/AdminProduct.fxml");
    }

    @FXML
    void handleUser(ActionEvent e) {
        switchScene(e, "/fxml/admin/AdminUsers.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
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
