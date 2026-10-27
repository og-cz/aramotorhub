package com.ogcz.app.controllers.admin;

import com.ogcz.app.database.DatabaseConnection;
import com.ogcz.app.database.Products;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.sql.*;

public class AdminProductController {

    @FXML
    private TableView<Products> productsTable;
    @FXML
    private TableColumn<Products, Integer> productIdColumn;
    @FXML
    private TableColumn<Products, Integer> sellerIdColumn;
    @FXML
    private TableColumn<Products, Integer> categoryIdProductsColumn;
    @FXML
    private TableColumn<Products, String> productNameColumn;
    @FXML
    private TableColumn<Products, String> descriptionColumn;
    @FXML
    private TableColumn<Products, String> priceColumn;
    @FXML
    private TableColumn<Products, Integer> stockColumn;
    @FXML
    private TableColumn<Products, String> statusColumn;
    @FXML
    private TableColumn<Products, Timestamp> createdAtColumn;

    @FXML
    private TextField productNameField;
    @FXML
    private TextField descriptionProductField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField stockField;
    @FXML
    private TextField sellerIDField;
    @FXML
    private SplitMenuButton statusMenuButton;

    private String selectedStatus = "AVAILABLE";

    private final ObservableList<Products> productsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadProducts();
        setupStatusMenu();
    }

    private void setupTable() {
        productIdColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getProductId()).asObject());
        sellerIdColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getSellerId()).asObject());
        categoryIdProductsColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getCategoryId()).asObject());
        productNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getProductName()));
        descriptionColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
        priceColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getProductPrice()));
        stockColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getStock()).asObject());
        statusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        createdAtColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getCreatedAt()));

        productNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        productNameColumn.setOnEditCommit(e -> {
            Products p = e.getRowValue();
            p.setProductName(e.getNewValue());
            updateProduct(p);
        });

        descriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        descriptionColumn.setOnEditCommit(e -> {
            Products p = e.getRowValue();
            p.setDescription(e.getNewValue());
            updateProduct(p);
        });

        priceColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        priceColumn.setOnEditCommit(e -> {
            Products p = e.getRowValue();
            if (!e.getNewValue().trim().matches("\\d+")) {
                showAlert(Alert.AlertType.ERROR, "Invalid Price", "Price must be a whole number.");
                productsTable.refresh();
                return;
            }
            p.setProductPrice(e.getNewValue().trim());
            updateProduct(p);
        });

        stockColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        stockColumn.setOnEditCommit(e -> {
            Products p = e.getRowValue();
            p.setStock(e.getNewValue());
            updateProduct(p);
        });

        statusColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        statusColumn.setOnEditCommit(e -> {
            Products p = e.getRowValue();
            String status = e.getNewValue().trim().toUpperCase();
            if (!status.equals("AVAILABLE") && !status.equals("OUT_OF_STOCK")) {
                showAlert(Alert.AlertType.ERROR, "Invalid Status", "Status must be AVAILABLE or OUT_OF_STOCK.");
                productsTable.refresh();
                return;
            }
            p.setStatus(status);
            updateProduct(p);
        });

        productsTable.setEditable(true);
        productsTable.setItems(productsList);
    }

    private void setupStatusMenu() {
        MenuItem available = new MenuItem("AVAILABLE");
        MenuItem unavailable = new MenuItem("OUT_OF_STOCK");

        available.setOnAction(e -> {
            selectedStatus = "AVAILABLE";
            statusMenuButton.setText("AVAILABLE");
        });

        unavailable.setOnAction(e -> {
            selectedStatus = "OUT_OF_STOCK";
            statusMenuButton.setText("OUT_OF_STOCK");
        });

        statusMenuButton.getItems().addAll(available, unavailable);
        statusMenuButton.setText("AVAILABLE");
    }

    private void loadProducts() {
        productsList.clear();
        productsList.addAll(Products.getAllProducts());
    }

    @FXML
    void handleCreateClick(ActionEvent event) {
        String name = productNameField.getText();
        String description = descriptionProductField.getText();
        String price = priceField.getText();
        String stockStr = stockField.getText();
        String sellerIdStr = sellerIDField.getText();

        if (name.isEmpty() || price.isEmpty() || stockStr.isEmpty() || sellerIdStr.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Fields", "Please complete all required fields.");
            return;
        }

        String sql = "INSERT INTO product (name, description, price, stock, status, seller_id, category_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            int priceValue = Integer.parseInt(price.trim());
            int stock = Integer.parseInt(stockStr.trim());
            int sellerId = Integer.parseInt(sellerIdStr.trim());

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setInt(3, priceValue);
            stmt.setInt(4, stock);
            stmt.setString(5, selectedStatus);
            stmt.setInt(6, sellerId);
            stmt.setInt(7, 8); // there's no category picker on this form yet

            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Product created successfully.");
            loadProducts();
            clearFields();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Price, stock and seller ID must be whole numbers.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create product. Check that the seller ID exists.");
        }
    }

    @FXML
    void handleDeleteClick(ActionEvent event) {
        Products selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to delete.");
            return;
        }

        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement cartStmt = conn.prepareStatement("DELETE FROM cart_item WHERE product_id = ?");
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM product WHERE product_id = ?")) {
                cartStmt.setInt(1, selected.getProductId());
                cartStmt.executeUpdate();
                stmt.setInt(1, selected.getProductId());
                stmt.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

            productsList.remove(selected);
            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Product deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to delete product. It may already be part of an order, set it to OUT_OF_STOCK instead.");
        }
    }

    @FXML
    void handleUpdateClick(ActionEvent event) {
        Products selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to update.");
            return;
        }

        updateProduct(selected);
    }

    private void updateProduct(Products p) {
        String sql = """
                UPDATE product SET name = ?, description = ?, price = ?, stock = ?, status = ?
                WHERE product_id = ?
                """;
        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getProductName());
            stmt.setString(2, p.getDescription());
            stmt.setString(3, p.getProductPrice());
            stmt.setInt(4, p.getStock());
            stmt.setString(5, p.getStatus());
            stmt.setInt(6, p.getProductId());

            stmt.executeUpdate();
            productsTable.refresh();

            showAlert(Alert.AlertType.INFORMATION, "Updated", "Product updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update product.");
        }
    }

    private void clearFields() {
        productNameField.clear();
        descriptionProductField.clear();
        priceField.clear();
        stockField.clear();
        sellerIDField.clear();
        statusMenuButton.setText("AVAILABLE");
        selectedStatus = "AVAILABLE";
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void handleUser(ActionEvent e) {
       switchScene(e, "/fxml/admin/AdminUsers.fxml");
    }

    @FXML
    void handleCategory(ActionEvent e) {
          switchScene(e, "/fxml/admin/AdminCategory.fxml");
    }

    @FXML
    void handleOrder(ActionEvent e) {
          switchScene(e, "/fxml/admin/AdminOrder.fxml");
    }

    @FXML
    void handleBack(ActionEvent e) {
          switchScene(e, "/fxml/admin/Admin.fxml");
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
