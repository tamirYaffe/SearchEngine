package View;

import Controller.Controller;
import SearchEngineTools.Indexer;
import SearchEngineTools.ReadFile;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class View {
    private Controller m_controller;
    private Stage primaryStage;
    private ReadFile readFile;
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
    public HBox hbox_bottom;
    public JTextArea jTextArea;

    public void setParameters(Controller controller, Stage primaryStage, Indexer indexer) {
        this.m_controller=controller;
        this.primaryStage=primaryStage;
        this.indexer=indexer;
    }

    public void onClickCorpusFileSystem(){
        actionAllButtons(true);
        String selectedDirectory=openFileSystem();
        if(selectedDirectory!=null)
            tf_corpusPath.setText(selectedDirectory);
        actionAllButtons(false);
    }

    public void onClickPostingListFileSystem(){
        actionAllButtons(true);
        String selectedDirectory=openFileSystem();
        if(selectedDirectory!=null)
            tf_postingListPath.setText(selectedDirectory);
        actionAllButtons(false);
    }

    public void onClickStartIndex(){
        actionAllButtons(true);
        if(tf_corpusPath.getText().length()==0 || tf_postingListPath.getText().length()==0){
            displayErrorMessage("Add path to input corpus and output posting files");
            actionAllButtons(false);
            return;
        }
        Thread thread=new Thread(()->{
            String corpusPath=tf_corpusPath.getText();
            String postingFilesPath=tf_postingListPath.getText();
            boolean useStemming=false;
            if(cb_useStemming.isSelected())
                useStemming=true;
            indexer.setPostingFilesPath(postingFilesPath);
            readFile=new ReadFile(indexer, corpusPath,postingFilesPath,useStemming);
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
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        actionAllButtons(false);
    }

    public void onClickLoadDictionary(){
        actionAllButtons(true);
        Map<String, Pair<Integer,Integer>> dictionary=new HashMap<>();
        int postingListPointer=0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(tf_postingListPath.getText()+fileSeparator+"dictionary.txt"));
            String line;
            while (( line=reader.readLine())!=null){
                dictionary.put(line.split(":")[0],new Pair<>(Integer.valueOf(line.split(":")[1]),postingListPointer++));
            }
        } catch (IOException e) {
            displayErrorMessage("load failed");
        }
        //load dictionary to index dictionary
        indexer.setDictionary(dictionary);
        System.out.println("dictionary lodad");
        actionAllButtons(false);
    }

    public void onClickShowDictionary(){
        actionAllButtons(true);
        try {
            String fileName=tf_postingListPath.getText()+fileSeparator+"dictionary.txt";
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            jTextArea=new JTextArea();
            jTextArea.read(reader,null);
            JFrame frame = new JFrame("TextArea Load");
            frame.getContentPane().add( new JScrollPane(jTextArea));
            frame.pack();
            frame.setLocationRelativeTo( null );
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        } catch (IOException e) {
            e.printStackTrace();
        }
        actionAllButtons(false);
    }

    public void onClickSDeleteAll(){
        actionAllButtons(true);
        System.out.println(indexer.getDictionarySize());
        indexer.clear();
        //readFile.clear();
        System.out.println(indexer.getDictionarySize());
        actionAllButtons(false);
    }

    private String openFileSystem(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory =
                directoryChooser.showDialog(primaryStage);
        if(selectedDirectory==null)
            return null;
        return selectedDirectory.getAbsolutePath();
    }

    private void actionAllButtons(boolean disable){
        btn_corpusFileSystem.setDisable(disable);
        btn_postingListFileSystem.setDisable(disable);
        btn_deleteAll.setDisable(disable);
        btn_loadDictionary.setDisable(disable);
        btn_showDictionary.setDisable(disable);
        btn_startIndex.setDisable(disable);
    }

    private void displayErrorMessage(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText("Ooops, there was an error!");
        alert.setContentText(msg);
        alert.showAndWait();
    }

}
