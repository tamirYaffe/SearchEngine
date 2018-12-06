package View;

import SearchEngineTools.Document;
import SearchEngineTools.Indexer;
import SearchEngineTools.ReadFile;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * A controller class for view.xml
 */
public class View {
    private Stage primaryStage;
    private ReadFile readFile;
    private Indexer indexer;
    private String fileSeparator=System.getProperty("file.separator");
    private boolean useStemming=false;
    private Thread runningIndex;


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
    public Menu menu_languages;
    public JTextArea jTextArea;

    /**
     * setter
     * @param primaryStage
     * @param indexer
     */
    public void setParameters(Stage primaryStage, Indexer indexer) {
        this.primaryStage=primaryStage;
        this.indexer=indexer;
    }

    /**
     * Opens the file system for the user to choose the corpus path.
     */
    public void onClickCorpusFileSystem(){
        actionAllButtons(true);
        String selectedDirectory=openFileSystem();
        if(selectedDirectory!=null)
            tf_corpusPath.setText(selectedDirectory);
        actionAllButtons(false);
    }

    /**
     * Opens the file system for the user to choose the posting files path.
     */
    public void onClickPostingListFileSystem(){
        actionAllButtons(true);
        String selectedDirectory=openFileSystem();
        if(selectedDirectory!=null)
            tf_postingListPath.setText(selectedDirectory);
        actionAllButtons(false);
    }

    /**
     * Starting to index in a new thread.
     */
    public void onClickStartIndex(){
        actionAllButtons(true);
        if(tf_corpusPath.getText().length()==0 || tf_postingListPath.getText().length()==0){
            displayErrorMessage("Add path to input corpus and output posting files");
            actionAllButtons(false);
            return;
        }
        runningIndex=new Thread(()->{
            String corpusPath=tf_corpusPath.getText();
            String postingFilesPath=tf_postingListPath.getText();
            if(cb_useStemming.isSelected())
                useStemming=true;
            Document.setUseStemming(useStemming);
            indexer.setPostingFilesPath(postingFilesPath);
            indexer.setIsStemming(useStemming);
            readFile=new ReadFile(indexer, corpusPath,postingFilesPath,useStemming);
            readFile.deletePrevFiles();
            long startTime = System.nanoTime();
            int numOfFiles=readFile.listAllFiles();
            indexer.writeCityIndex();
            long endTime = System.nanoTime();
            long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.

            //need to change
            System.out.println("seconds: "+duration/1000000000);
            System.out.println(numOfFiles);
        });
        runningIndex.start();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Started Indexing...");
        alert.setHeaderText(null);
        alert.setContentText("Please wait until buttons become enable again.");
        alert.showAndWait();

        //wait for index to finish to activate all buttons.
        Thread thread=new Thread(()->{
            try {
                runningIndex.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            addLanguages();
            actionAllButtons(false);
        });
        thread.start();
    }

    /**
     * Loads the dictionary from the input posting files path to the indexer memory.
     * dictionary load is determine by the use stemming check box.
     */
    public void onClickLoadDictionary(){
        actionAllButtons(true);
        Map<String, Pair<Integer,Integer>> dictionary=new HashMap<>();
        int postingListPointer=0;
        try {
            String fileName;
            if(useStemming)
                fileName="dictionaryStemming.txt";
            else
                fileName="dictionary.txt";
            BufferedReader reader = new BufferedReader(new FileReader(tf_postingListPath.getText()+fileSeparator+fileName));
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

    /**
     * Shows the dictionary to the user.
     * dictionary showing is determine by the use stemming check box.
     */
    public void onClickShowDictionary(){
        actionAllButtons(true);
        try {
            String fileName;
            if(useStemming)
                fileName="dictionaryStemming.txt";
            else
                fileName="dictionary.txt";
            String filePath=tf_postingListPath.getText()+fileSeparator+fileName;
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            jTextArea=new JTextArea();
            jTextArea.read(reader,null);
            jTextArea.setEditable(false);
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

    /**
     * Deletes all posting files and in memory program vars.
     */
    public void onClickSDeleteAll(){
        actionAllButtons(true);
        System.out.println(indexer.getDictionarySize());
        indexer.clear();
        //readFile.clear();
        System.out.println(indexer.getDictionarySize());
        deletePostingFiles();
        menu_languages.getItems().clear();
        actionAllButtons(false);
    }



    //private methods

    /**
     * Adds the corpus files languages to the menu.
     */
    private void addLanguages() {
        Collection<String>languages=readFile.getLanguages();
        ArrayList<MenuItem>items=new ArrayList<>();
        for(String language:languages)
            items.add(new MenuItem(language));
        if(languages!=null)
            menu_languages.getItems().addAll(items);
    }

    /**
     * Opens the file system and returns the user chosen path.
     * @return
     */
    private String openFileSystem(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory =
                directoryChooser.showDialog(primaryStage);
        if(selectedDirectory==null)
            return null;
        return selectedDirectory.getAbsolutePath();
    }

    /**
     * Disable or enable all buttons according to the input disable.
     * @param disable- the action we wish to perform on the buttons.
     */
    private void actionAllButtons(boolean disable){
        btn_corpusFileSystem.setDisable(disable);
        btn_postingListFileSystem.setDisable(disable);
        btn_deleteAll.setDisable(disable);
        btn_loadDictionary.setDisable(disable);
        btn_showDictionary.setDisable(disable);
        btn_startIndex.setDisable(disable);
        cb_useStemming.setDisable(disable);
        tf_corpusPath.setDisable(disable);
        tf_postingListPath.setDisable(disable);
    }

    /**
     * Displaying an error message for the user, with given msg.
     * @param msg- the message we wish to present.
     */
    private void displayErrorMessage(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText("Ooops, there was an error!");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Delete all posting files.
     */
    private void deletePostingFiles() {
        String path=tf_postingListPath.getText();
        if(path.length()==0)
            return;
        File dir = new File(path);
        for (File file : dir.listFiles())
            if (!file.isDirectory())
                file.delete();
    }

}
