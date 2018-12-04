package View;

import Controller.Controller;
import Model.Model;
import SearchEngineTools.Indexer;
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
        Scene scene = new Scene(root, 800, 800);
        primaryStage.setScene(scene);
        //--------------
        View view = fxmlLoader.getController();
        Indexer indexer = new Indexer(1048576 * 10);

        view.setParameters(controller, primaryStage,indexer);
        //--------------
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
