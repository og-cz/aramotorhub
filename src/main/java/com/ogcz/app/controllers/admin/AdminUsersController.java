package com.ogcz.app.controllers.admin;

import com.ogcz.app.database.DatabaseConnection;
import com.ogcz.app.database.User;
import com.ogcz.app.database.UserService;
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

import java.io.IOException;
import java.sql.*;

public class AdminUsersController {

    @FXML
    private TableColumn<User, String> addressColummSLR, addressColumnCST;
    @FXML
    private TextField addressField;

    @FXML
    private TableColumn<User, String> barangayColumnCST, barangayColumnSLR;
    @FXML
    private SplitMenuButton barangayFieldMenu;

    @FXML
    private TableView<User> customerTable, sellerTable;

    @FXML
    private TableColumn<User, String> districtColumnCST, districtColumnSLR;
    @FXML
    private SplitMenuButton districtFieldMenu;

    @FXML
    private TableColumn<User, String> emailColumnCST, emailColumnSLR;
    @FXML
    private TextField emailField;

    @FXML
    private TableColumn<User, String> firstNameColumnCST, firstNameColumnSLR;
    @FXML
    private TextField firstNameField;

    @FXML
    private Button handleBackClick;

    @FXML
    private TableColumn<User, String> lastNameColumnCST, lastNameColumnSLR;
    @FXML
    private TextField lastNameField;

    @FXML
    private TableColumn<User, String> passwordColumnCST, passwordColumnSLR;
    @FXML
    private TextField passwordField;

    @FXML
    private TableColumn<User, String> roleColumnCST, roleColumnSLR;
    @FXML
    private SplitMenuButton roleFieldMenu;

    @FXML
    private TableColumn<User, Integer> userIDColCST, userIDCoSLR;

    private final ObservableList<User> customers = FXCollections.observableArrayList();
    private final ObservableList<User> sellers = FXCollections.observableArrayList();

    private User selectedUser = null;

    @FXML
    public void initialize() {
        setupTable(customerTable, userIDColCST, emailColumnCST, passwordColumnCST, firstNameColumnCST,
                lastNameColumnCST, addressColumnCST, districtColumnCST, barangayColumnCST, roleColumnCST, customers);
        setupTable(sellerTable, userIDCoSLR, emailColumnSLR, passwordColumnSLR, firstNameColumnSLR, lastNameColumnSLR,
                addressColummSLR, districtColumnSLR, barangayColumnSLR, roleColumnSLR, sellers);

        setupSplitMenu(districtFieldMenu, "Sampaloc");
        setupSplitMenu(barangayFieldMenu, "Barangay 395", "Barangay 396", "Barangay 397", "Barangay 398",
                "Barangay 399", "Barangay 400");
        setupSplitMenu(roleFieldMenu, "CUSTOMER", "SELLER");

        loadUsers();
    }

    private void setupSplitMenu(SplitMenuButton menu, String... options) {
        for (String option : options) {
            MenuItem item = new MenuItem(option);
            item.setOnAction(e -> menu.setText(option));
            menu.getItems().add(item);
        }
    }

    private void setupTable(
            TableView<User> table,
            TableColumn<User, Integer> idCol,
            TableColumn<User, String> emailCol,
            TableColumn<User, String> passwordCol,
            TableColumn<User, String> firstNameCol,
            TableColumn<User, String> lastNameCol,
            TableColumn<User, String> addressCol,
            TableColumn<User, String> districtCol,
            TableColumn<User, String> barangayCol,
            TableColumn<User, String> roleCol,
            ObservableList<User> dataList) {

        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        passwordCol.setCellValueFactory(new PropertyValueFactory<>("password"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        districtCol.setCellValueFactory(new PropertyValueFactory<>("district"));
        barangayCol.setCellValueFactory(new PropertyValueFactory<>("barangay"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        emailCol.setCellFactory(TextFieldTableCell.forTableColumn());
        passwordCol.setCellFactory(TextFieldTableCell.forTableColumn());
        firstNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        lastNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        addressCol.setCellFactory(TextFieldTableCell.forTableColumn());
        districtCol.setCellFactory(TextFieldTableCell.forTableColumn());
        barangayCol.setCellFactory(TextFieldTableCell.forTableColumn());
        roleCol.setCellFactory(TextFieldTableCell.forTableColumn());

        emailCol.setOnEditCommit(e -> e.getRowValue().setEmail(e.getNewValue()));
        passwordCol.setOnEditCommit(e -> e.getRowValue().setPassword(e.getNewValue()));
        firstNameCol.setOnEditCommit(e -> e.getRowValue().setFirstName(e.getNewValue()));
        lastNameCol.setOnEditCommit(e -> e.getRowValue().setLastName(e.getNewValue()));
        addressCol.setOnEditCommit(e -> e.getRowValue().setAddress(e.getNewValue()));
        districtCol.setOnEditCommit(e -> e.getRowValue().setDistrict(e.getNewValue()));
        barangayCol.setOnEditCommit(e -> e.getRowValue().setBarangay(e.getNewValue()));
        roleCol.setOnEditCommit(e -> e.getRowValue().setRole(e.getNewValue()));

        table.setEditable(true);
        table.setItems(dataList);

        table.setOnMouseClicked(event -> {
            if (!table.getSelectionModel().isEmpty()) {
                selectedUser = table.getSelectionModel().getSelectedItem();
                populateFieldsFromSelectedUser();
            }
        });
    }

    private void populateFieldsFromSelectedUser() {
        if (selectedUser != null) {
            emailField.setText(selectedUser.getEmail());
            passwordField.setText(selectedUser.getPassword());
            firstNameField.setText(selectedUser.getFirstName());
            lastNameField.setText(selectedUser.getLastName());
            addressField.setText(selectedUser.getAddress());
            districtFieldMenu.setText(selectedUser.getDistrict());
            barangayFieldMenu.setText(selectedUser.getBarangay());
            roleFieldMenu.setText(selectedUser.getRole());
        }
    }

    private void loadUsers() {
        customers.clear();
        sellers.clear();

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user");
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("address"),
                        rs.getString("district"),
                        rs.getString("barangay"),
                        rs.getString("role"));
                if (user.getRole().equalsIgnoreCase("CUSTOMER"))
                    customers.add(user);
                else if (user.getRole().equalsIgnoreCase("SELLER"))
                    sellers.add(user);
            }

        } catch (SQLException e) {
            showAlert("Error", "Failed to load users: " + e.getMessage());
        }
    }

    private boolean fieldsAreValid() {
        if (emailField.getText().isBlank() || passwordField.getText().isBlank()
                || firstNameField.getText().isBlank() || lastNameField.getText().isBlank()) {
            showAlert("Warning", "Please fill in the email, password and name.");
            return false;
        }
        String role = roleFieldMenu.getText();
        if (!role.equalsIgnoreCase("CUSTOMER") && !role.equalsIgnoreCase("SELLER")) {
            showAlert("Warning", "Please pick a role.");
            return false;
        }
        return true;
    }

    @FXML
    void handleCreate(ActionEvent event) {
        if (!fieldsAreValid()) {
            return;
        }

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO user (email, password, first_name, last_name, address, district, barangay, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, emailField.getText());
            stmt.setString(2, passwordField.getText());
            stmt.setString(3, firstNameField.getText());
            stmt.setString(4, lastNameField.getText());
            stmt.setString(5, addressField.getText());
            stmt.setString(6, districtFieldMenu.getText());
            stmt.setString(7, barangayFieldMenu.getText());
            stmt.setString(8, roleFieldMenu.getText());

            stmt.executeUpdate();
            loadUsers();
            clearFields();
            showAlert("Success", "User created successfully.");

        } catch (SQLException e) {
            showAlert("Error", "Could not create user: " + e.getMessage());
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        if (selectedUser == null) {
            showAlert("Warning", "Please select a user to delete.");
            return;
        }

        if (new UserService().deleteUser(selectedUser.getUserId())) {
            loadUsers();
            clearFields();
            showAlert("Success", "User deleted successfully.");
        } else {
            showAlert("Error", "Could not delete user.");
        }
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        if (selectedUser == null) {
            showAlert("Warning", "Please select a user to update.");
            return;
        }
        if (!fieldsAreValid()) {
            return;
        }

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE user SET email = ?, password = ?, first_name = ?, last_name = ?, address = ?, district = ?, barangay = ?, role = ? WHERE user_id = ?")) {

            stmt.setString(1, emailField.getText());
            stmt.setString(2, passwordField.getText());
            stmt.setString(3, firstNameField.getText());
            stmt.setString(4, lastNameField.getText());
            stmt.setString(5, addressField.getText());
            stmt.setString(6, districtFieldMenu.getText());
            stmt.setString(7, barangayFieldMenu.getText());
            stmt.setString(8, roleFieldMenu.getText());
            stmt.setInt(9, selectedUser.getUserId());

            stmt.executeUpdate();
            loadUsers();
            clearFields();
            showAlert("Success", "User updated successfully.");

        } catch (SQLException e) {
            showAlert("Error", "Could not update user: " + e.getMessage());
        }
    }

    @FXML
    public void handleProductsClick(ActionEvent e) {
        switchScene(e, "/fxml/admin/AdminProduct.fxml");
    }

    @FXML
    public void handleOrderClick(ActionEvent e) {
        switchScene(e, "/fxml/admin/AdminOrder.fxml");
    }

    @FXML
    public void handleCategoryClick(ActionEvent e) {
        switchScene(e, "/fxml/admin/AdminCategory.fxml");
    }

    @FXML
    public void handleBackClick(ActionEvent e) {
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

    private void clearFields() {
        emailField.clear();
        passwordField.clear();
        firstNameField.clear();
        lastNameField.clear();
        addressField.clear();
        districtFieldMenu.setText("District");
        barangayFieldMenu.setText("Barangay");
        roleFieldMenu.setText("Role");
        selectedUser = null;
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
