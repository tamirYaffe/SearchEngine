package View;

import Controller.Controller;
import SearchEngineTools.ParsingTools.Parse;
import SearchEngineTools.ParsingTools.ParseWithStemming;
import SearchEngineTools.ParsingTools.Term.ATerm;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ParseTest extends Application {

    private static Parse parser = new Parse();
    public static Stage primaryStage;
    public javafx.scene.control.Button btn_Parse;
    public javafx.scene.control.CheckBox CheckBox_AllowStemming;
    public javafx.scene.control.TextArea textArea_ToParse;
    public javafx.scene.control.TextArea textArea_Results;
    Controller view;
    Toolkit toolkit = Toolkit.getDefaultToolkit();


    public static void main(String[] args){
        launch(args);
    }



    public void parse(ActionEvent actionEvent) {
        btn_Parse.setDisable(true);
        String sTextToParse = textArea_ToParse.getText();
        String[] saTextToParse = sTextToParse.split("\n");
        List<String> lsTextToParse = new ArrayList<>(saTextToParse.length);
        for (int i = 0; i < saTextToParse.length; i++) {
            lsTextToParse.add(saTextToParse[i]);
        }
        Collection<ATerm> parseResults = parser.parseDocument(lsTextToParse);
        StringBuilder resultsToDisplay = new StringBuilder();
        for (ATerm term:parseResults) {
            resultsToDisplay.append(term.toString()).append("\n");
        }
        textArea_Results.setText(resultsToDisplay.toString());
        btn_Parse.setDisable(false);


    }

    public void switchParser(ActionEvent actionEvent) {
        if(parser instanceof ParseWithStemming)
            parser = new Parse();
        else
            parser = new ParseWithStemming();
    }

    private void SetStageCloseEvent(Stage primaryStage) {
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent windowEvent) {
                windowEvent.consume();
                primaryStage.close();
            }
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ParseTest.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        //textArea_Results.setEditable(false);
    }
}
