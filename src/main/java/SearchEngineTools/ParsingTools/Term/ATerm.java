package SearchEngineTools.ParsingTools.Term;

import java.util.ArrayList;
import java.util.List;

public abstract class ATerm  implements Comparable<ATerm>{

    private int occurrences=0;
    protected String term;
    private List<Integer> positions;
    protected boolean isNumber;

    /**
     * Set how many times term appeared in document
     * @param occurrences of ATerm in document
     */
    public void setOccurrences(int occurrences){
        this.occurrences = occurrences;
    }

    /**
     * get how many times term appeared in document
     * @return
     */
    public int getOccurrences(){
        return occurrences;
    }

    public String toString(){
        return "Term: "+getTerm()+"~ Occurrences: "+getOccurrences();
    }

    /**
     * get Term of ATerm
     * @return term
     */
    public String getTerm(){
        if(term == null)
            term = createTerm();
        return term;
    }

    /**
     * create this ATerm's term
     * @return
     */
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

    /**
     * Natural ordering by lexicographical order of terms
     * @param other
     * @return
     */
    public int compareTo(ATerm other){
        return this.getTerm().compareTo(other.getTerm());
    }

    /**
     * Add positions of other term
     * @param other
     */
    public void addPositions(ATerm other){
        if(other.positions==null)
            return;
        else {
            if(positions==null)
                positions=new ArrayList<>(other.positions.size());
            positions.addAll(other.positions);
        }
    }

    /**
     * increment occurrences of ATerm in document by 1
     */
    public void incrementOccurrences(){
        occurrences++;
    }

    /**
     * True if Number. Else False
     * @return
     */
    public boolean isNumber(){
        return isNumber;
    }
}
