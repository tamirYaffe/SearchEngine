package View;

import Controller.Controller;
import Model.Model;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Model model = new Model();
        Controller controller = new Controller();
        //--------------
        primaryStage.setTitle("Search Engine");
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("/View.fxml").openStream());
        Scene scene = new Scene(root, 500, 400);
        primaryStage.setScene(scene);
        //--------------
        View view = fxmlLoader.getController();
        view.setParameters(controller, primaryStage);
        //--------------
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
