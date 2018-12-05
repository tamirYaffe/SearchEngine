package SearchEngineTools.ParsingTools.Term;

public class YearTerm extends ATerm {

    private int month;
    private int year;

    public YearTerm(int month,int year){
        this.month=month;
        this.year=year;
    }

    @Override
    protected String createTerm() {
        return year+"-"+month;
    }
}
