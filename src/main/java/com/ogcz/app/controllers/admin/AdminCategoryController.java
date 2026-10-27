package com.ogcz.app.controllers.admin;

import com.ogcz.app.database.Category;
import com.ogcz.app.database.DatabaseConnection;
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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

import java.io.IOException;
import java.sql.*;

public class AdminCategoryController {

    @FXML
    private TextField categoryField;

    @FXML
    private TableColumn<Category, Integer> categoryIdColumn;

    @FXML
    private TableColumn<Category, String> categoryNameColumn;

    @FXML
    private TableView<Category> categoryTable;

    private ObservableList<Category> categoryList = FXCollections.observableArrayList();

    private Category selectedCategory = null;

    @FXML
    public void initialize() {
        categoryIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Enable editing in table
        categoryTable.setEditable(true);
        categoryNameColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));

        // Save edited name back into the object
        categoryNameColumn.setOnEditCommit(event -> {
            Category category = event.getRowValue();
            category.setName(event.getNewValue());
            categoryField.setText(event.getNewValue());
        });

        categoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedCategory = newVal;
            if (newVal != null) {
                categoryField.setText(newVal.getName());
            }
        });

        loadCategories();
    }

    private void loadCategories() {
        categoryList.clear();
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM category")) {

            while (rs.next()) {
                Category category = new Category(
                        rs.getInt("category_id"),
                        rs.getString("name")
                );
                categoryList.add(category);
            }
            categoryTable.setItems(categoryList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleCreate(ActionEvent event) {
        String name = categoryField.getText().trim();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Category name cannot be empty.");
            return;
        }

        String sql = "INSERT INTO category (name) VALUES (?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.executeUpdate();
            loadCategories();
            categoryField.clear();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Category created successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not create the category.");
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a category to delete.");
            return;
        }

        String sql = "DELETE FROM category WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, selected.getId());
            pstmt.executeUpdate();
            loadCategories();
            categoryField.clear();
            selectedCategory = null;
            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Category deleted successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not delete the category, it still has products in it.");
        }
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        if (selectedCategory == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a category to update.");
            return;
        }

        String name = categoryField.getText().trim();
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Category name cannot be empty.");
            return;
        }

        String sql = "UPDATE category SET name = ? WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setInt(2, selectedCategory.getId());
            pstmt.executeUpdate();
            loadCategories();
            categoryField.clear();
            selectedCategory = null;
            showAlert(Alert.AlertType.INFORMATION, "Updated", "Category updated successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not update the category.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void handleUser(ActionEvent e) {
       switchScene(e, "/fxml/admin/AdminUsers.fxml");
    }

    @FXML
    void handleProduct(ActionEvent e) {
          switchScene(e, "/fxml/admin/AdminProduct.fxml");
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
