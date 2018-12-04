package SearchEngineTools;

import SearchEngineTools.ParsingTools.Term.ATerm;
import SearchEngineTools.ParsingTools.Term.CityTerm;
import SearchEngineTools.ParsingTools.Term.WordTerm;
import com.google.gson.internal.LinkedHashTreeMap;
import javafx.util.Pair;
import sun.awt.Mutex;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Indexer {
    //dictionary that holds term->idf. used also for word check for big first word.
    private Map<String, Pair<Integer,Integer>> dictionary;
    //dictionary and posting list in one hash
    private Map<String, PostingList> tempInvertedIndex;
    //dictionary and posting list in one hash for cities.
    private Map<String, List<CityPostingEntry>> tempCityInvertedIndex;


    private int memoryBlockSize;
    private int usedMemory;
    private int termsNum;
    private String postingFilesPath;

    public void setDictionary(Map<String, Pair<Integer, Integer>> dictionary) {
        this.dictionary = dictionary;
    }

    private String fileSeparator=System.getProperty("file.separator");


    //writeThread
    private AtomicInteger blockNum=new AtomicInteger();
    private Mutex mutex=new Mutex();


    public Indexer() {
        dictionary = new LinkedHashMap<>();
        tempInvertedIndex = new HashMap<>();
        tempCityInvertedIndex=new LinkedHashMap<>();
    }

    public Indexer(int memoryBlockSize,String postingFilesPath) {
        this();
        this.memoryBlockSize = memoryBlockSize;
        this.postingFilesPath=postingFilesPath;
    }

    public Indexer(int memoryBlockSize) {
        this();
        this.memoryBlockSize = memoryBlockSize;
    }

    public void setPostingFilesPath(String postingFilesPath) {
        this.postingFilesPath = postingFilesPath;
    }



    public int getTermsNum() {
        return termsNum;
    }

    /**
     * Creates the dictionary and posting files.
     *
     * @param terms - list of the document terms(after parse).
     * @param docID
     */
    public  void  createInvertedIndex(Iterator<ATerm> terms, int docID) {
        //System.out.println("started indexing: "+docID);
        Document document=new Document(docID);
        while (terms.hasNext()) {
            ATerm aTerm = terms.next();
            if(aTerm.getTerm().equals("."))
                System.out.println("liad");
            if (aTerm instanceof WordTerm && Character.isLetter(aTerm.getTerm().charAt(0)))
                handleCapitalWord(aTerm);
            String term = aTerm.getTerm();
            if(aTerm instanceof CityTerm){
                document.setDocCity(term);
                addToCityIndex(aTerm,docID);
            }
            int termOccurrences=aTerm.getOccurrences();
            document.updateDocInfo(termOccurrences);
            //add or update dictionary.
            if (!dictionary.containsKey(term)){
                dictionary.put(term, new Pair<>(1,-1));
                termsNum++;
                //System.out.println("termsNum: "+termsNum+ " dictionary size: "+dictionary.size());
            }
            else
                dictionary.replace(term,new Pair<>(dictionary.get(term).getKey()+1,-1));
            PostingList postingsList;
            PostingEntry postingEntry = new PostingEntry(docID, termOccurrences);
            if (!tempInvertedIndex.containsKey(term)) {
                postingsList = new PostingList();
                tempInvertedIndex.put(term, postingsList);
                usedMemory += term.length() + 1;
            } else {
                postingsList = tempInvertedIndex.get(term);
            }
            int addedMemory=postingsList.add(postingEntry);
            if(addedMemory!=-1)
                usedMemory+=addedMemory;
            if (usedMemory > memoryBlockSize) {
                sortAndWriteInvertedIndexToDisk();
                //init dictionary and posting lists.
                tempInvertedIndex.clear();
                usedMemory = 0;
            }
        }
        document.writeDocInfoToDisk(postingFilesPath);
        //System.out.println("finish: " + docID);
    }

    public void mergeBlocks() throws IOException {
        System.out.println("starting merge");
        System.out.println("dictionary size: "+getDictionarySize());
        int postingListIndex=0;
        BufferedReader[] readers = new BufferedReader[blockNum.get()];
        PostingListComparator comparator=new PostingListComparator();
        PriorityQueue<Pair<String, Integer>> queue = new PriorityQueue<>(comparator);
        String pathName=postingFilesPath+fileSeparator+"postingLists.txt";
        File file = new File(pathName);
        FileWriter fw = new FileWriter(file,true);
        BufferedWriter bw = new BufferedWriter(fw);
        String curPostingList;
        List<String> bufferPostingLists=new ArrayList<>();
        //create readers and init queue.
        for (int i = 0; i < blockNum.get(); i++) {
            String fileName = "blocks"+fileSeparator+"block" + i + ".txt";
            readers[i] = new BufferedReader(new FileReader(fileName));
            queue.add(new Pair<>(readers[i].readLine(), i));
        }
        while (!queue.isEmpty()) {
            curPostingList=getNextPostingList(queue,readers);
            curPostingList=checkForMergeingPostingLines(queue,readers,curPostingList);
            String term=extractTerm(curPostingList);
            //termsNum++;
            dictionary.replace(term,new Pair<>(dictionary.get(term).getKey(),postingListIndex++));
            //write to buffer posting lists
            bufferPostingLists.add(curPostingList);
            usedMemory+=curPostingList.length()-term.length();
            //check size of buffer
            if (usedMemory > memoryBlockSize) {
                writeBufferPostingListsToDisk(bw,bufferPostingLists);
                //init dictionary and posting lists.
                bufferPostingLists.clear();
                usedMemory = 0;
            }

        }
        //writing buffer remaining posting lists
        if(usedMemory>0){
            writeBufferPostingListsToDisk(bw,bufferPostingLists);
        }
        bw.close();
//        writeDictionaryToDisk();
        sortAndWriteDictionaryToDisk();
    }

    public  void sortAndWriteInvertedIndexToDisk() {
        mutex.lock();
        Map<String, PostingList> toWrite=tempInvertedIndex;
        tempInvertedIndex=new HashMap<>();
        Thread thread=new Thread(()->{
            if(usedMemory==0)
                return;
            System.out.println("writing to disk: blockNum" +blockNum.get()+" "+ usedMemory+" bytes");
            String fileName = "blocks"+fileSeparator+"block" + blockNum.get() + ".txt";
            blockNum.getAndIncrement();
            try (FileWriter fw = new FileWriter(fileName);
                 BufferedWriter bw = new BufferedWriter(fw)) {

                List<String> keys = new ArrayList<>(toWrite.keySet());
                Collections.sort(keys);
                for (String key : keys) {
                    bw.write(key+";");
                    bw.write(""+toWrite.get(key));
                    bw.newLine();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        toWrite.clear();
        System.out.println("finished writing blockNum"+blockNum.get());
        mutex.unlock();
    }

    public Map<String, Pair<Integer, Integer>> getDictionary() {
        return dictionary;
    }

    public int getDictionarySize(){
        if(dictionary==null)
            return 0;
        return dictionary.size();
    }

    public void loadDictionaryFromDisk(String path){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while (( line=reader.readLine())!=null){
                dictionary.put(line.split(" ")[0],new Pair<>(Integer.valueOf(line.split(" ")[1]),Integer.valueOf(line.split(" ")[2])) );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addToCityIndex(ATerm aTerm, int docID) {
        CityTerm cityTerm= (CityTerm) aTerm;
        List<CityPostingEntry> postingsList;
        CityPostingEntry postingEntry=new CityPostingEntry(docID,cityTerm.getPositions());
        String term=aTerm.getTerm();
        if (!tempCityInvertedIndex.containsKey(term)) {
            postingsList = new ArrayList<>();
            tempCityInvertedIndex.put(term, postingsList);
        } else {
            postingsList = tempCityInvertedIndex.get(term);
        }
        postingsList.add(postingEntry);
    }

    //<editor-fold desc="Private functions">
    private String extractTerm(String postingList) {
        return postingList.substring(0,postingList.indexOf(";"));
    }

    private void writeBufferPostingListsToDisk(BufferedWriter bw, List<String> bufferPostingLists) throws IOException {

        for(String postingList:bufferPostingLists){
            bw.write(postingList.substring(postingList.indexOf(";")+1));
//            bw.write(postingList);
            bw.newLine();
        }
    }


    /**
     * remove top of queue,and add the next line from the removed line block.
     * @param queue
     * @param readers
     * @return
     * @throws IOException
     */
    private String getNextPostingList(PriorityQueue<Pair<String, Integer>> queue, BufferedReader[] readers) throws IOException {
        String postingList;
        while (true) {
            Pair<String,Integer> postingListPair=queue.poll();
            postingList = postingListPair.getKey();
            int blockIndex=postingListPair.getValue();

            String nextPostingList=readers[blockIndex].readLine();
            if(nextPostingList!=null)
                queue.add(new Pair<>(nextPostingList,blockIndex));
            //handling words lower/upper case
            String term=extractTerm(postingList);
            if(Character.isUpperCase(term.charAt(0)) && dictionary.containsKey(term.toLowerCase())){
                //change posting list
                String updatedPostingList=term.toLowerCase()+postingList.substring(postingList.indexOf(";"));
                // add to queue
                queue.add(new Pair<>(updatedPostingList,blockIndex));
            }
            else
                break;

        }
        return postingList;
    }

    private String checkForMergeingPostingLines(PriorityQueue<Pair<String, Integer>> queue, BufferedReader[] readers, String curPostingList) throws IOException {
        if(queue.isEmpty())
            return curPostingList;
        String nextPostingList=queue.peek().getKey();
        while (extractTerm(curPostingList).equals(extractTerm(nextPostingList))){
            curPostingList=mergePostingLists(curPostingList,nextPostingList);
            getNextPostingList(queue,readers);
            if(queue.isEmpty())
                break;
            nextPostingList=queue.peek().getKey();
        }
        return curPostingList;
    }

    private String mergePostingLists(String postingList1, String postingList2) {
        String term=extractTerm(postingList1);
        postingList1=postingList1.substring(postingList1.indexOf(";")+1);
        postingList2=postingList2.substring(postingList2.indexOf(";")+1);
        int lastDocID1=PostingList.calculateLastDocID(postingList1);
        String firstDocID=postingList2.substring(0,postingList2.indexOf(" "));
        int firstDocID2=Integer.parseInt(firstDocID)-lastDocID1;
        firstDocID=""+firstDocID2;
        postingList2=firstDocID+postingList2.substring(postingList2.indexOf(" "));
        return term+";"+postingList1+" "+postingList2;
    }

    private String mergeAndSortPostingLists(String postingList1, String postingList2) {
        String term=extractTerm(postingList1);
        PostingList postingList_1=new PostingList(postingList1);
        PostingList postingList_2=new PostingList(postingList2);
        return term+";"+PostingList.mergeLists(postingList_1,postingList_2);
    }

    private void handleCapitalWord(ATerm aTerm) {
        String term = aTerm.getTerm();
        String termLowerCase = term.toLowerCase();
        String termUpperCase = term.toUpperCase();
        if (term.equals(""))
            return;
        //term is upper case.
        if (Character.isUpperCase(term.charAt(0))) {
            if (dictionary.containsKey(termLowerCase)) {
                ((WordTerm) aTerm).toLowerCase();
            }
        }
        //term is lower case.
        else {
            if (dictionary.containsKey(termUpperCase)) {
                //change termUpperCase in dictionary to termLowerCase
                Pair<Integer,Integer> dictionaryPair = dictionary.remove(termUpperCase);
                dictionary.put(termLowerCase, dictionaryPair);
            }
        }
    }

    private void writeDictionaryToDisk() {
        String pathName=postingFilesPath+fileSeparator+"dictionary.txt";
        File file = new File(pathName);
        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {
            for(Map.Entry entry: dictionary.entrySet()){
                bw.write(entry.getKey()+" "+((Pair<Integer,Integer>)entry.getValue()).getKey()+" "+((Pair<Integer,Integer>)entry.getValue()).getValue());
                bw.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sortAndWriteDictionaryToDisk() {
        String pathName=postingFilesPath+fileSeparator+"dictionary.txt";
        File file = new File(pathName);
        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {

            List<String> keys = new ArrayList<>(dictionary.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                Pair<Integer,Integer>dictionaryPair=dictionary.get(key);
                bw.write(key+" "+dictionaryPair.getKey()+" "+dictionaryPair.getValue());
                bw.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>
}
