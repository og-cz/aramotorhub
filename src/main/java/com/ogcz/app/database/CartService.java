package com.ogcz.app.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CartService {

    // a customer only ever has one cart, items get cleared on checkout
    public static int getOrCreateCartId(Connection conn, int customerId) throws SQLException {
        String findSql = "SELECT cart_id FROM cart WHERE customer_id = ? ORDER BY created_at DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(findSql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cart_id");
                }
            }
        }

        String createSql = "INSERT INTO cart (customer_id) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(createSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, customerId);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create new cart.");
    }

    // returns null when added, otherwise the reason it wasn't
    public static String addToCart(int customerId, int productId, int quantity) {
        try (Connection conn = DatabaseConnection.connect()) {
            int stock;
            String status;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT stock, status FROM product WHERE product_id = ?")) {
                stmt.setInt(1, productId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return "This product no longer exists.";
                    }
                    stock = rs.getInt("stock");
                    status = rs.getString("status");
                }
            }

            if (!"AVAILABLE".equalsIgnoreCase(status) || stock <= 0) {
                return "Sorry, this product is out of stock.";
            }

            int cartId = getOrCreateCartId(conn, customerId);

            int inCart = 0;
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT quantity FROM cart_item WHERE cart_id = ? AND product_id = ?")) {
                stmt.setInt(1, cartId);
                stmt.setInt(2, productId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        inCart = rs.getInt("quantity");
                    }
                }
            }

            int newQuantity = inCart + quantity;
            if (newQuantity > stock) {
                return "Only " + stock + " left in stock" + (inCart > 0 ? " (you already have " + inCart + " in your cart)." : ".");
            }

            String sql = inCart > 0
                    ? "UPDATE cart_item SET quantity = ? WHERE cart_id = ? AND product_id = ?"
                    : "INSERT INTO cart_item (quantity, cart_id, product_id) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, newQuantity);
                stmt.setInt(2, cartId);
                stmt.setInt(3, productId);
                stmt.executeUpdate();
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return "Failed to add product to cart.";
        }
    }
}
