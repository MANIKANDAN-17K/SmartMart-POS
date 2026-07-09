package com.supermarketpos;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("SmartMart POS");
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: #2c3e50;");

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #ecf0f1;");
        root.getChildren().add(label);

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("SmartMart POS");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}