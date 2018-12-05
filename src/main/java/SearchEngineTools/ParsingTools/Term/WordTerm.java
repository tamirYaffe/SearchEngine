package SearchEngineTools.ParsingTools.Term;

public class WordTerm extends ATerm {


    /**
     * Construct WordTerm from string
     * @param term
     */
    public WordTerm(String term){
        this.term=term;
        removePunctuation();
        isNumber=false;
    }

    @Override
    protected String createTerm() {
        return term;
    }

    /**
     * Set the term for this string
     * @param term
     */
    public void setTerm(String term){
        this.term=term;
        removePunctuation();
    }

    /**
     * Set term to all lower case letters
     */
    public void toLowerCase(){
        this.term = this.term.toLowerCase();
    }

    /**
     * Set term to all upper case letters
     */
    public void toUperCase(){
        this.term = this.term.toUpperCase();
    }


    private void removePunctuation(){
        int beginIndex = 0;
        int endIndex = term.length()-1;
        boolean isRelevantBeginning = false;
        boolean isRelevantEnd = false;

        //find relevant beginning and end indexes
        while (beginIndex< term.length() && !isRelevantBeginning){
            if(Character.isDigit(term.charAt(beginIndex)) || Character.isLetter(term.charAt(beginIndex)))
                isRelevantBeginning = true;
            else
                beginIndex++;
        }

        while (endIndex >= beginIndex && !isRelevantEnd){
            if(Character.isDigit(term.charAt(beginIndex)) || Character.isLetter(term.charAt(beginIndex)))
                isRelevantEnd = true;
            else
                endIndex--;
        }

        term = term.substring(beginIndex,endIndex+1);
    }
}
