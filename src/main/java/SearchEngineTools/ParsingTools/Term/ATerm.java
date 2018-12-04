package SearchEngineTools.ParsingTools.Term;

import java.util.ArrayList;
import java.util.List;

public abstract class ATerm  implements Comparable<ATerm>{

    private int occurrences=0;
    protected String term;
    private List<Integer> positions;

    public void setOccurrences(int occurrences){
        this.occurrences = occurrences;
    }

    public int getOccurrences(){
        return occurrences;
    }

    public String toString(){
        return "Term: "+getTerm()+"~ Occurrences: "+getOccurrences();
    }

    public String getTerm(){
        if(term == null)
            term = createTerm();
        return term;
    }

    protected abstract String createTerm();


    public boolean equals(Object other){
        if(other instanceof ATerm)
            return this.getTerm().equals(((ATerm) other).getTerm());
        return false;
    }

    @Override
    public int hashCode() {
        String toHash = this.getTerm();
        return toHash.hashCode();
    }

    public int compareTo(ATerm other){
        return this.getTerm().compareTo(other.getTerm());
    }

    public void addPositions(ATerm other){
        if(other.positions==null)
            return;
        else {
            if(positions==null)
                positions=new ArrayList<>(other.positions.size());
            positions.addAll(other.positions);
        }
    }

    public void incrementOccurrences(){
        occurrences++;
    }
}
