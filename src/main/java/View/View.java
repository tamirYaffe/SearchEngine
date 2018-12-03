package View;

import Controller.Controller;
import SearchEngineTools.ReadFile;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class View {
    private Controller m_controller;
    private Stage primaryStage;

    //fxml widgets
    public TextField tf_corpusPath;
    public TextField tf_postingListPath;
    public Button btn_corpusFileSystem;
    public Button btn_postingListFileSystem;
    public CheckBox cb_useStemming;
    public Button btn_startIndex;
    public Button btn_loadDictionary;
    public Button btn_showDictionary;
    public Button btn_deleteAll;

    public void setParameters(Controller controller,Stage primaryStage) {
        this.m_controller=controller;
        this.primaryStage=primaryStage;
    }

    public void onClickCorpusFileSystem(){
        String selectedDirectory=openFileSystem();
        if(selectedDirectory!=null)
            tf_corpusPath.setText(selectedDirectory);
    }

    public void onClickPostingListFileSystem(){
        String selectedDirectory=openFileSystem();
        if(selectedDirectory!=null)
            tf_postingListPath.setText(selectedDirectory);
    }

    private String openFileSystem(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory =
                directoryChooser.showDialog(primaryStage);
        return selectedDirectory.getAbsolutePath();
    }

    public void onClickStartIndex(){
        Thread thread=new Thread(()->{
            String corpusPath=tf_corpusPath.getText();
            String postingFilesPath=tf_postingListPath.getText();
            boolean useStemming=false;
            if(cb_useStemming.isSelected())
                useStemming=true;
            ReadFile readFile=new ReadFile(corpusPath,postingFilesPath,useStemming);
            readFile.deletePrevFiles();
            long startTime = System.nanoTime();
            int numOfFiles=readFile.listAllFiles();
            long endTime = System.nanoTime();
            long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.

            //need to change
            System.out.println("seconds: "+duration/1000000000);
            System.out.println(numOfFiles);
        });
        thread.start();
    }

    public void onClickLoadDictionary(){

    }

    public void onClickShowDictionary(){

    }

    public void onClickSDeleteAll(){

    }

}
