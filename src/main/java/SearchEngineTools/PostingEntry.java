package SearchEngineTools;

public class PostingEntry {
    private int docID;
    private int termTF;
    private int sizeInBytes;

    public PostingEntry(int docID, int termTF) {
        this.docID = docID;
        this.termTF = termTF;
        String docIDS=""+docID;
        String termTFS=""+termTF;
        sizeInBytes+=docIDS.length()+termTFS.length()+2;
    }

    public int getSizeInBytes() {
        return sizeInBytes;
    }

    public int getDocID() {
        return docID;
    }

    public int getTermTF() {
        return termTF;
    }

    @Override
    public String toString() {
        return docID+" "+termTF;
    }

    public void setDocID(int docID) {
        this.docID = docID;
    }

    public int compareTo(PostingEntry other){
        return this.docID-other.docID;
    }
}
