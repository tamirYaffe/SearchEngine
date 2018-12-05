package SearchEngineTools.ParsingTools.Term;

public class CompoundFractionCurrencyTerm extends CurrencyTerm {

    private FractionTerm fraction;

    public CompoundFractionCurrencyTerm(CompoundFractionTerm compoundFractionTerm, String currency){
        super(compoundFractionTerm.getWholeNumber(), currency);
        this.fraction = new FractionTerm(compoundFractionTerm.getNumerator(), compoundFractionTerm.getDenominator());
    }

    public CompoundFractionCurrencyTerm(NumberTerm numberTerm, String currency, FractionTerm fraction){
        super(numberTerm, currency);
        this.fraction = fraction;
    }

    @Override
    protected String createTerm() {
        return this.numberTerm.getTerm()+" "+fraction.getTerm()+" "+currency;
    }
}
