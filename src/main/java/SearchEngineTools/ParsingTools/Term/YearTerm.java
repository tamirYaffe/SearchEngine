package SearchEngineTools.ParsingTools.Term;

public class YearTerm extends ATerm {

    private NumberTerm numberTerm;
    private String year;

    public YearTerm(NumberTerm numberTerm, String year){
        this.numberTerm = numberTerm;
        this.year = year;
    }

    @Override
    protected String createTerm() {
        return numberTerm.createTerm() + year;
    }
}
