package com.ogcz.app.controllers.customer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

import com.ogcz.app.SettingsWindowHelper;
import com.ogcz.app.database.DatabaseConnection;

public class HomeMainController implements Initializable {

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private Button electricalButton, engineButton, homeButton, nextButton, oilsButton, profileButton, shopButton,
            suspensionButtom, wheelsButton;

    @FXML
    void handleCategoryButtonClick(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        String categoryName = clicked.getText();
        openCategoriesPage(categoryName);
    }

    @FXML
    private void handleCartButton(ActionEvent event) {
        System.out.println("handleCartButton triggered");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer/Cart.fxml"));
            Parent newRoot = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene newScene = new Scene(newRoot);
            currentStage.setScene(newScene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleShopClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer/Shops.fxml"));
            Parent newRoot = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene newScene = new Scene(newRoot);
            currentStage.setScene(newScene);
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoryComboBox.setPromptText("Category");

        try {
            Connection conn = DatabaseConnection.connect(); // Use your reusable method
            if (conn == null) {
                System.out.println("Database connection failed.");
                return;
            }

            String query = "SELECT name FROM category";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                categoryComboBox.getItems().add(rs.getString("name"));
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Listener: When an item is selected, open Categories.fxml
        categoryComboBox.setOnAction(event -> {
            String selectedCategory = categoryComboBox.getValue();
            if (selectedCategory != null && !selectedCategory.isEmpty()) {
                openCategoriesPage(selectedCategory);
            }
        });
    }

    private void openCategoriesPage(String category) {
        System.out.println("Opening category: " + category);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer/Categories.fxml"));
            Parent newRoot = loader.load();

            // ✅ Set category in controller
            CategoriesController controller = loader.getController();
            controller.setSelectedCategory(category);

            Stage currentStage = (Stage) categoryComboBox.getScene().getWindow();
            Scene newScene = new Scene(newRoot);
            currentStage.setScene(newScene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleImageClick(MouseEvent event) {
        System.out.println("Next home page clicked!");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer/HomeNext.fxml"));
            Parent newRoot = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene newScene = new Scene(newRoot);
            currentStage.setScene(newScene);
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenSettings(javafx.event.ActionEvent event) {
        SettingsWindowHelper.openSettings((Node) event.getSource());
    }

    @FXML
    private void handleProfileClick(ActionEvent event) {
        System.out.println("handleProfileClick triggered");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer/Profile.fxml"));
            Parent newRoot = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene newScene = new Scene(newRoot);
            currentStage.setScene(newScene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
