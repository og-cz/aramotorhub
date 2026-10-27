package com.ogcz.app.database;

import javafx.stage.Stage;

public class UserSession {
    private static User currentUser;
    private static Stage mainStage;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clearSession() {
        currentUser = null;
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void setMainStage(Stage mainStage) {
        UserSession.mainStage = mainStage;
    }
}
