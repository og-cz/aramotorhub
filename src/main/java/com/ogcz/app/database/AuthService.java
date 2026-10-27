package com.ogcz.app.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthService {

    private String firstName, lastName, password, email;

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    private String address, district, barangay, role;

    public void setRole(String role) {
        this.role = role;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public User loginAndGetUser(String email, String password) {
        User user = null;

        String sql = "SELECT user_id, email, password, first_name, last_name, address, district, barangay, role "
                + "FROM user WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            // mysql compares strings case-insensitively, so check the password again here
            if (rs.next() && rs.getString("password").equals(password)) {
                int dbUserId = rs.getInt("user_id");
                String dbEmail = rs.getString("email");
                String dbPassword = rs.getString("password");
                String dbFirstName = rs.getString("first_name");
                String dbLastName = rs.getString("last_name");
                String dbAddress = rs.getString("address");
                String dbDistrict = rs.getString("district");
                String dbBarangay = rs.getString("barangay");
                String dbRole = rs.getString("role");

                user = new User(dbUserId, dbEmail, dbPassword, dbFirstName, dbLastName, dbAddress, dbDistrict,
                        dbBarangay, dbRole);
            }
            rs.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM user WHERE email = ?";

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String registerNewUser() {
        String generatedUserId = null;

        String sql = "INSERT INTO user (email, password, first_name, last_name, address, district, barangay, role)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setString(3, firstName);
            stmt.setString(4, lastName);
            stmt.setString(5, address);
            stmt.setString(6, district);
            stmt.setString(7, barangay);
            stmt.setString(8, role);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new Exception("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    generatedUserId = id + getRoleSuffix(role);
                } else {
                    throw new Exception("Creating user failed, no ID obtained.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return generatedUserId;
    }

    private String getRoleSuffix(String role) {
        switch (role.toUpperCase()) {
            case "CUSTOMER":
                return "CST";
            case "SELLER":
                return "SLR";
            case "ADMIN":
                return "ADM";
            default:
                return "";
        }
    }
}
