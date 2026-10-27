package com.ogcz.app.database;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class UserService {

    public boolean deleteUser(int userId) {
        // children first because of the foreign keys
        String[] steps = {
                // customer side
                "DELETE FROM cart_item WHERE cart_id IN (SELECT cart_id FROM cart WHERE customer_id = ?)",
                "DELETE FROM cart WHERE customer_id = ?",
                "DELETE FROM order_item WHERE order_id IN (SELECT order_id FROM `order` WHERE customer_id = ?)",
                "DELETE FROM `order` WHERE customer_id = ?",
                // seller side, their products can sit in other people's carts and orders
                "DELETE FROM cart_item WHERE product_id IN (SELECT product_id FROM product WHERE seller_id = ?)",
                "DELETE FROM order_item WHERE product_id IN (SELECT product_id FROM product WHERE seller_id = ?)",
                "DELETE FROM product WHERE seller_id = ?"
        };

        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);
            try {
                for (String sql : steps) {
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, userId);
                        stmt.executeUpdate();
                    }
                }

                int rowsDeleted;
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM user WHERE user_id = ?")) {
                    stmt.setInt(1, userId);
                    rowsDeleted = stmt.executeUpdate();
                }

                conn.commit();
                return rowsDeleted > 0;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE user SET email = ?, password = ?, first_name = ?, last_name = ?, "
                + "address = ?, district = ?, barangay = ?, role = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            stmt.setString(5, user.getAddress());
            stmt.setString(6, user.getDistrict());
            stmt.setString(7, user.getBarangay());
            stmt.setString(8, user.getRole());
            stmt.setInt(9, user.getUserId());

            int rowsAffected = stmt.executeUpdate();

            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
