package ro.mta.se.lab;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ro.mta.se.lab.controller.Controller;
import ro.mta.se.lab.model.Model;
import ro.mta.se.lab.view.View;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Model converterModel = new Model();
        Controller converterController = new Controller(converterModel);
        View converterView = new View(converterController);


        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getClass().getResource("/sample.fxml"));
        fxmlLoader.setController(converterController);
        Parent root = fxmlLoader.load();

        primaryStage.setTitle("Converter");
        primaryStage.setScene(new Scene(root, 550, 400));
        primaryStage.setResizable(false);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);

    }
}
