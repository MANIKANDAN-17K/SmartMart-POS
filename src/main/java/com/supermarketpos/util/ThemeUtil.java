package com.supermarketpos.util;

import javafx.scene.Scene;

public class ThemeUtil {

    private static Scene appScene;

    public static void setScene(Scene scene) {
        appScene = scene;
    }

    public static void applyTheme(String theme) {
        if (appScene == null) return;
        appScene.getStylesheets().clear();
        appScene.getStylesheets().add(ThemeUtil.class.getResource("/css/common.css").toExternalForm());
        if ("dark".equals(theme)) {
            appScene.getStylesheets().add(ThemeUtil.class.getResource("/css/dark-theme.css").toExternalForm());
        } else {
            appScene.getStylesheets().add(ThemeUtil.class.getResource("/css/light-theme.css").toExternalForm());
        }
    }
}