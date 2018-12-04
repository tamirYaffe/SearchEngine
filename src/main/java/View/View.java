package View;

import Controller.Controller;
import SearchEngineTools.Indexer;
import SearchEngineTools.ReadFile;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class View {
    private Controller m_controller;
    private Stage primaryStage;
    private Indexer indexer;
    private String fileSeparator=System.getProperty("file.separator");


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
    public TextArea ta_showDictionary;

    public void setParameters(Controller controller, Stage primaryStage, Indexer indexer) {
        this.m_controller=controller;
        this.primaryStage=primaryStage;
        this.indexer=indexer;
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
        if(tf_corpusPath.getText()==null || tf_postingListPath.getText()==null){
            showErrorMessege();
            return;
        }
        Thread thread=new Thread(()->{
            String corpusPath=tf_corpusPath.getText();
            String postingFilesPath=tf_postingListPath.getText();
            boolean useStemming=false;
            if(cb_useStemming.isSelected())
                useStemming=true;
            indexer.setPostingFilesPath(postingFilesPath);
            ReadFile readFile=new ReadFile(indexer, corpusPath,postingFilesPath,useStemming);
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

    private void showErrorMessege() {

    }

    public void onClickLoadDictionary(){
        Map<String, Pair<Integer,Integer>> dictionary=new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(tf_postingListPath.getText()+fileSeparator+"dictionary.txt"));
            String line;
            while (( line=reader.readLine())!=null){
                dictionary.put(line.split(" ")[0],new Pair<>(Integer.valueOf(line.split(" ")[1]),Integer.valueOf(line.split(" ")[2])) );
            }
        } catch (IOException e) {
            displayErrorMessage();
        }
        //load dictionary to index dictionary
        indexer.setDictionary(dictionary);
    }

    private void displayErrorMessage() {
    }

    public void onClickShowDictionary(){
        for (int i = 0; i < 100; i++) {
            ta_showDictionary.appendText("line: "+i);
            ta_showDictionary.appendText(System.getProperty("line.separator"));
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(tf_postingListPath.getText()+fileSeparator+"dictionary.txt"));
            String line;
            while (( line=reader.readLine())!=null){
                //dictionary.put(line.split(" ")[0],new Pair<>(Integer.valueOf(line.split(" ")[1]),Integer.valueOf(line.split(" ")[2])) );
                //ta_showDictionary.appendText();
                ta_showDictionary.appendText(System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            displayErrorMessage();
        }

    }

    public void onClickSDeleteAll(){

    }

}
