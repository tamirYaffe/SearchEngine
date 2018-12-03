package SearchEngineTools.ParsingTools.Term;

public abstract class ATerm  implements Comparable<ATerm>{

    private int occurrences=0;
    protected String term;

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
        return getTerm().hashCode();
    }

    public int compareTo(ATerm other){
        return this.getTerm().compareTo(other.getTerm());
    }
}
