package SearchEngineTools.ParsingTools.Term;

public class PercentageTerm extends NumberTerm {

    /**
     * Constructor for the numberTerm class
     * @param term
     */
    public PercentageTerm(NumberTerm term){
        super(term);
    }

    @Override
    protected String createTerm() {
        return super.createTerm()+"%";
    }
}
