package com.ogcz.app.controllers.customer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.ogcz.app.SettingsWindowHelper;
import com.ogcz.app.database.CartService;
import com.ogcz.app.database.DatabaseConnection;
import com.ogcz.app.database.UserSession;
import com.ogcz.app.database.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CartController {

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private VBox vboxCartProducts;

    @FXML
    private Label labelTotalPrice;

    private double totalPrice = 0;

    @FXML
    void handleCheckoutClick(ActionEvent event) {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User not logged in.");
            return;
        }

        int customerId = currentUser.getUserId();
        String address = currentUser.getAddress();

        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);

            try {
                int cartId = CartService.getOrCreateCartId(conn, customerId);

                // lock the products so two checkouts can't oversell the same stock
                String cartItemsSql = """
                            SELECT ci.product_id, ci.quantity, p.name, p.price, p.stock, p.status
                            FROM cart_item ci
                            JOIN product p ON ci.product_id = p.product_id
                            WHERE ci.cart_id = ?
                            FOR UPDATE
                        """;

                List<int[]> items = new ArrayList<>(); // product_id, quantity, price
                double total = 0;

                try (PreparedStatement stmt = conn.prepareStatement(cartItemsSql)) {
                    stmt.setInt(1, cartId);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        int quantity = rs.getInt("quantity");
                        int stock = rs.getInt("stock");

                        if (!"AVAILABLE".equalsIgnoreCase(rs.getString("status")) || quantity > stock) {
                            conn.rollback();
                            showAlert(Alert.AlertType.WARNING, "ARA Motorhub",
                                    rs.getString("name") + " only has " + Math.max(stock, 0)
                                            + " left in stock. Please update your cart.");
                            return;
                        }

                        items.add(new int[] { rs.getInt("product_id"), quantity, rs.getInt("price") });
                        total += (double) rs.getInt("price") * quantity;
                    }
                }

                if (items.isEmpty()) {
                    conn.rollback();
                    showAlert(Alert.AlertType.WARNING, "ARA Motorhub", "Your cart is empty. Add items before checking out.");
                    return;
                }

                String insertOrderSQL = "INSERT INTO `order` (customer_id, address, total_price, status) VALUES (?, ?, ?, 'PENDING')";
                int orderId;
                try (PreparedStatement stmt = conn.prepareStatement(insertOrderSQL,
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, customerId);
                    stmt.setString(2, address);
                    stmt.setDouble(3, total);
                    stmt.executeUpdate();

                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (!generatedKeys.next()) {
                        throw new SQLException("Creating order failed, no ID obtained.");
                    }
                    orderId = generatedKeys.getInt(1);
                }

                String insertItemSQL = "INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";
                String updateStockSQL = """
                            UPDATE product
                            SET stock = stock - ?,
                                status = IF(stock <= 0, 'OUT_OF_STOCK', status)
                            WHERE product_id = ?
                        """;
                try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSQL);
                        PreparedStatement stockStmt = conn.prepareStatement(updateStockSQL)) {
                    for (int[] item : items) {
                        itemStmt.setInt(1, orderId);
                        itemStmt.setInt(2, item[0]);
                        itemStmt.setInt(3, item[1]);
                        itemStmt.setDouble(4, item[2]);
                        itemStmt.addBatch();

                        stockStmt.setInt(1, item[1]);
                        stockStmt.setInt(2, item[0]);
                        stockStmt.addBatch();
                    }
                    itemStmt.executeBatch();
                    stockStmt.executeBatch();
                }

                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM cart_item WHERE cart_id = ?")) {
                    stmt.setInt(1, cartId);
                    stmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

            vboxCartProducts.getChildren().clear();
            totalPrice = 0;
            labelTotalPrice.setText("0.00");

            showAlert(Alert.AlertType.INFORMATION, "ARA Motorhub", "Order Successful");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "ARA Motorhub", "An error occurred during checkout. Please try again.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void handleHomeButton(ActionEvent event) {
        switchScene(event, "/fxml/customer/HomeMain.fxml");
    }

    @FXML
    void handleLogOutButton(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SettingsWindowHelper.logout(currentStage);
    }

    @FXML
    void handleOpenSettings(ActionEvent event) {
        SettingsWindowHelper.openSettings((Node) event.getSource());
    }

    @FXML
    void handleShopsClick(ActionEvent event) {
        switchScene(event, "/fxml/customer/Shops.fxml");
    }

    @FXML
    void handleProductsClick(ActionEvent event) {
        switchScene(event, "/fxml/customer/Products.fxml");
    }

    @FXML
    private void handleProfileClick(ActionEvent event) {
        switchScene(event, "/fxml/customer/Profile.fxml");
    }

    @FXML
    void handleFavoriteClick(ActionEvent event) {
        switchScene(event, "/fxml/customer/Favorite.fxml");
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

    public void initialize() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            System.out.println("No user logged in.");
            return;
        }

        int userId = currentUser.getUserId();

        try (Connection conn = DatabaseConnection.connect()) {
            int cartId = CartService.getOrCreateCartId(conn, userId);

            String sql = """
                        SELECT p.product_id, p.name AS product_name, p.price,
                               u.first_name AS store_first_name,
                               u.last_name AS store_last_name,
                               u.address AS store_address,
                               ci.quantity, p.stock
                        FROM cart_item ci
                        JOIN product p ON ci.product_id = p.product_id
                        JOIN user u ON p.seller_id = u.user_id
                        WHERE ci.cart_id = ?
                    """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, cartId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int productId = rs.getInt("product_id");
                    String productName = rs.getString("product_name");
                    String initials = getInitials(productName);
                    String price = rs.getString("price");

                    String storeFirstName = rs.getString("store_first_name");
                    String storeLastName = rs.getString("store_last_name");
                    String storeName = storeFirstName + " " + storeLastName;

                    String storeAddress = rs.getString("store_address");
                    int quantity = rs.getInt("quantity");
                    int stock = rs.getInt("stock");

                    addCartItem(productName, initials, price, storeName, storeAddress, quantity, stock, productId, cartId);
                }

                recalculateTotalPrice();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // ComboBox category logic
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
                openCategoriesPage(selectedCategory);
            }
        });
    }

    private void openCategoriesPage(String category) {
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

    private void recalculateTotalPrice() {
        double total = 0;
        for (Node node : vboxCartProducts.getChildren()) {
            FXMLLoader loader = (FXMLLoader) node.getUserData();
            CartProductContainerController controller = loader.getController();
            total += controller.getSubtotal();
        }
        totalPrice = total;
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        labelTotalPrice.setText(numberFormat.format(totalPrice));
    }

    private void addCartItem(String productName, String initials, String price, String storeName, String storeAddress,
                             int quantity, int stock, int productId, int cartId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer/CartProductContainer.fxml"));
            Node node = loader.load();

            CartProductContainerController controller = loader.getController();
            controller.setProductDetails(productName, initials, price, storeName, storeAddress, quantity,
                    stock, productId, cartId, this::recalculateTotalPrice);

            node.setUserData(loader);
            vboxCartProducts.getChildren().add(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getInitials(String productName) {
        String[] words = productName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < Math.min(2, words.length); i++) {
            if (!words[i].isEmpty()) {
                initials.append(Character.toUpperCase(words[i].charAt(0)));
            }
        }
        return initials.toString();
    }
}
