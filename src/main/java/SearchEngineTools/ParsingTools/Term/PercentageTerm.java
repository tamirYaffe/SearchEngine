package SearchEngineTools.ParsingTools.Term;

public class PercentageTerm extends NumberTerm {

    public PercentageTerm(NumberTerm term){
        super(term);
    }

    @Override
    protected String createTerm() {
        return super.createTerm()+"%";
    }
}
