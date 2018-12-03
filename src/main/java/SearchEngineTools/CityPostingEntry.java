package SearchEngineTools;

import java.util.List;

public class CityPostingEntry {
    private int docID;
    private List<String> positions;

    public CityPostingEntry(int docID,List<Integer>positions){
        this.docID=docID;
        for(int position:positions)
            this.positions.add(0,""+position);
    }
    public int compareTo(CityPostingEntry other){
        return this.docID-other.docID;
    }

    public int getSizeInBytes() {
        return 0;
    }

    @Override
    public String toString() {
        String toString=docID+":";
        for(String position:positions)
            toString+=" "+position;
        return toString;
    }

}
