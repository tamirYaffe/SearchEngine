package SearchEngineTools;

import SearchEngineTools.ParsingTools.Parse;
import SearchEngineTools.ParsingTools.Term.ATerm;
import javafx.util.Pair;
import sun.awt.Mutex;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class ReadFile {
    private static int numOfDocs;
    private Parse parse;
    private Indexer indexer;
    private HashSet<String> stopWords = new HashSet<>();

    //for documents
    private List<String> documentsBuffer = new ArrayList<>();
    private int documentBufferSize;

    //threads
    private ConcurrentBuffer<Pair<Iterator<ATerm>, Integer>> PIBuffer = new ConcurrentBuffer<>(Integer.MAX_VALUE);
    private ConcurrentBuffer<Pair<List<String>, Integer>> RPBuffer = new ConcurrentBuffer<>(4);
    private Mutex mutex = new Mutex();
    //    private ExecutorService threadPool=Executors.newCachedThreadPool();
//    private ExecutorService threadPool = Executors.newFixedThreadPool(2);
//    private ExecutorService threadPool=Executors.newWorkStealingPool();
    private List<Thread> threads=new ArrayList<>();

    public ReadFile() {
        parse = new Parse();
        indexer = new Indexer(1048576 * 10);
    }

    public int listAllFiles(String path) {
        createStopWords(path);
        Document.corpusPath = path;
        startIndexThread();
        //startParseThreads();
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            paths.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    try {
                        if (!filePath.toString().contains("stop_words")) {
                            divideFileToDocs(readContent(filePath), filePath);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        //stopping parse
        /*
        RPBuffer.add(new Pair<>(new ArrayList<String>(),-1));
        RPBuffer.add(new Pair<>(new ArrayList<String>(),-1));
        for(Thread t:threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        */
        writeDocumentsToDisk();
        System.out.println("stoping indexer");
        PIBuffer.add(new Pair<>(null, -1));
        //write remaining posting lists to disk
        mutex.lock();
        indexer.sortAndWriteInvertedIndexToDisk();
        try {
            indexer.mergeBlocks();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("terms count:"+indexer.getTermsNum());
        System.out.println("Dictionary size: "+indexer.getDictionarySize());
        return numOfDocs;
    }

    private void startParseThreads() {
        Thread parse1=new Thread(()-> runParse());
        Thread parse2=new Thread(()-> runParse());
        parse1.start();
        parse2.start();
        threads.add(parse1);
        threads.add(parse2);
    }

    private void runParse() {
        while (true){
            Pair<List<String>, Integer> nextDoc = RPBuffer.get();
            if(nextDoc.getValue()==-1)
                break;
            Collection<ATerm> terms = parse.parseDocument(extractDocText(nextDoc.getKey()));
            PIBuffer.add(new Pair(terms.iterator(), nextDoc.getValue()));
            nextDoc.getKey().clear();
            //System.out.println("finish parse doc: " + nextDoc.getValue());
        }
    }

    private void writeRemainingDocuments() {
    }

    private void createStopWords(String path) {
        File root = new File(path);
        String fileName = "stop_words.txt";
        try {
            boolean recursive = true;

            Collection files = FileUtils.listFiles(root, null, recursive);
            for (Iterator iterator = files.iterator(); iterator.hasNext(); ) {
                File file = (File) iterator.next();
                if (file.getName().equals(fileName))
                    readStopWords(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        parse.setStopWords(stopWords);
    }


    private List<String> readContent(Path filePath) throws IOException {

        BufferedReader br = null;
        FileReader fr = null;
        List<String> fileList = new ArrayList<>();
        String line;
        try {
            fr = new FileReader(filePath.toString());
            br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                fileList.add(line);
            }
        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                if (br != null)
                    br.close();

                if (fr != null)
                    fr.close();

            } catch (IOException ex) {

                ex.printStackTrace();

            }

        }
        return fileList;
    }

    private void divideFileToDocs(List<String> fileList, Path filePath) {
        List<String> docLines = new ArrayList<>();
        String docName;
        int startLineNumInt = 0;
        int endLineNumInt = 0;
        int numOfLinesInt = 0;
        int s = 0;
        for (String line : fileList) {
//            if(numOfDocs>145)
//                return;
            docLines.add(line);
            endLineNumInt++;
            numOfLinesInt++;
            if (line.contains("<DOCNO>"))
                docName = extractDocID(line);
            if (line.equals("</DOC>")) {
                createDoc(filePath, startLineNumInt, numOfLinesInt, numOfDocs);
                //startParseThread(docLines, numOfDocs);
                /*
                List<String> docLinesClone = new ArrayList<>();
                for(String l:docLines)
                    docLinesClone.add(l);
                RPBuffer.add(new Pair<>(docLinesClone, numOfDocs));
                */
                Collection<ATerm> terms = parse.parseDocument(extractDocText(docLines));
                PIBuffer.add(new Pair(terms.iterator(), numOfDocs));
                System.out.println("finish parse doc: " + numOfDocs);
                startLineNumInt = endLineNumInt + 1;
                numOfLinesInt = 0;
                docLines.clear();
                numOfDocs++;
                //System.out.println("num of docs: " + numOfDocs);
            }
        }

    }

    private void startParseThread(List<String> doc, int docID) {
        Runnable r = new MyRunnable(extractDocText(doc), docID);
        //threadPool.execute(r);
    }

    private void startIndexThread() {
        System.out.println("starting indexing");
        Thread createIndex = new Thread(() -> {
            mutex.lock();
            while (true) {
                Pair<Iterator<ATerm>, Integer> toIndex = PIBuffer.get();
                if (toIndex.getValue() == -1) {
                    break;
                }
                indexer.createInvertedIndex(toIndex.getKey(), toIndex.getValue());
            }
            mutex.unlock();
        });
        createIndex.start();
//        threadPool.execute(createIndex);
    }

    private String extractDocID(String line) {
        String ans = line.substring(7, line.length() - 8);
        return ans;
    }

    private void createDoc(Path filePath, int startLineNum, int numOfLines, int docID) {
        String fileName = extractFileName(filePath.toString());
        String documentLine = fileName + " " + startLineNum + " " + numOfLines;
        documentBufferSize += documentLine.length() + 1;
        documentsBuffer.add(documentLine);
        if (documentBufferSize > 1048576 * 5) {
            writeDocumentsToDisk();
            documentsBuffer.clear();
            documentBufferSize = 0;
        }
    }

    private void writeDocumentsToDisk() {
        try (FileWriter fw = new FileWriter("Documents.txt", true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            for (int i = 0; i < documentsBuffer.size(); i++) {
                bw.write(documentsBuffer.get(0));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String extractFileName(String path) {
//      return path.split("corpus")[1];
        String[] splitPath;
        String fileName;
        if (path.contains("\\")) {
            splitPath = path.split("\\\\");
            fileName = "\\" + splitPath[splitPath.length - 1] + "\\" + splitPath[splitPath.length - 2];
        } else {
            splitPath = path.split("/");
            fileName = "/" + splitPath[splitPath.length - 1] + "/" + splitPath[splitPath.length - 2];
        }
        return fileName;
    }

    public static void deletePrevFiles() {
        try {
            Files.delete(Paths.get("dictionary.txt"));
            Files.delete(Paths.get("postingLists.txt"));
            Files.delete(Paths.get("Documents.txt"));
            Files.delete(Paths.get("DocumentsInfo.txt"));
            File dir = new File("blocks");
            for (File file : dir.listFiles())
                if (!file.isDirectory())
                    file.delete();
        } catch (IOException e) {
            System.out.println("all files did not deleted");
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////
    /////***this functions may be moved to the Parse class.***////////////////////////

    public List<String> readDocument(String path) {
        List<String> lineList = null;
        try {
            lineList = Files.readAllLines(Paths.get(path), Charset.forName("UTF-8"));

        } catch (IOException e) {
            //for foemer file coding.
            try {
                lineList = Files.readAllLines(Paths.get(path), Charset.forName("ISO-8859-1"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return extractDocText(lineList);


    }

    public List<String> extractDocText(List<String> lineList) {
        List<String> fileText = new ArrayList<>();
        boolean isText = false;
        for (int i = 0; i < lineList.size(); i++) {
            String line = lineList.get(i);
            try {
                if (line.equals("<TEXT>")) {
                    isText = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (line.equals("</TEXT>"))
                isText = false;
            if (isText)
                fileText.add(line);
        }
        if (!fileText.isEmpty())
            fileText.remove(0);
        return fileText;
    }

    public List<String> extractDocCity(List<String> lineList) {
        for (int i = 0; i < lineList.size(); i++) {
            String line = lineList.get(i);
            if (line.contains("<F P=104>")) {
                line = line.split(">")[1];
                if (!line.equals(""))
                    System.out.println(line.substring(2).split(" ")[0] + " " + line.substring(2).split(" ")[1]);
            }
        }
        return null;
    }

    private void readStopWords(File filePath) {
        BufferedReader br = null;
        FileReader fr = null;
        String line;
        try {
            fr = new FileReader(filePath);
            br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                stopWords.add(line);
            }
        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                if (br != null)
                    br.close();

                if (fr != null)
                    fr.close();

            } catch (IOException ex) {

                ex.printStackTrace();

            }

        }
    }


    /////runnable class for multithreading the parse
    public class MyRunnable implements Runnable {
        private List<String> doc;
        private int docID;

        public MyRunnable(List<String> doc, int docID) {
            this.docID = docID;
            this.doc = doc;
        }

        public void run() {
            Collection<ATerm> terms = parse.parseDocument(doc);
            PIBuffer.add(new Pair(terms.iterator(), docID));
            System.out.println("finish parse doc: " + docID);
        }
    }
}


