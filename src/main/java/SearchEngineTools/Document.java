package SearchEngineTools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Document {
    //static vars
    public static String corpusPath;

    private int docNum;
    private String path;
    private Long startLine;
    private Long numOfLines;
    private int max_tf;
    private int numOfUniqeTerms;
    private  String docCity;

    public Document(int docNum) {
        this.docNum=docNum;
    }

    public List<String> getDocumentsLines() {
        readDocPointerInfo();
        List<String> fileList = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(path));
            for (int i = 1; i < startLine; i++) {
                reader.readLine();
            }
            for (int i = 0; i < numOfLines; i++) {
                fileList.add(reader.readLine());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
    }

    private void readDocPointerInfo() {
        String[] line ;
        try (BufferedReader br = new BufferedReader(new FileReader("Documents.txt"))) {
            for (int i = 0; i < docNum-1; i++)
                br.readLine();
            line=br.readLine().split(" ");
            String fileName=line[0];
            startLine= Long.valueOf(line[1]);
            numOfLines= Long.valueOf(line[2]);
            path=corpusPath+fileName;
            getDocumentsLines();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDocInfo(int termOccurrences) {
        if(termOccurrences>max_tf)
            max_tf=termOccurrences;
        numOfUniqeTerms++;
    }
    public void writeDocInfoToDisk(){
        try(FileWriter fw = new FileWriter("DocumentsInfo.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            String toWrite=max_tf+" "+numOfUniqeTerms;
            if(docCity!=null)
                toWrite+=" "+docCity;
            out.println(toWrite);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDocCity(String docCity) {
        this.docCity = docCity;
    }
}
