package SearchEngineTools.ParsingTools;

import SearchEngineTools.ParsingTools.Term.*;
import javafx.util.Pair;
import sun.awt.Mutex;

import java.util.*;

public class Parse {

    //all words that depict a value after the number, and the values they represent
    //for example <m, 1000000>, <Thousand, 1000>
    private Map<String, Value> valuesAfterNumber;

    //all frases that depict a currency and their currency symbol
    //for example <U.S dollar, $>, <Dollars, $>
    private ParsingHashMap currencyTypes;

    //all currency symbols the parser will recognize
    //for example $
    private Collection<Character> currencySymbols;

    //months and their values
    //for example <december, 12>
    private Map<String, Integer> months;

    //months and last day
    //i.g <1,31>, <2,29>
    private Map<Integer, Integer> lastDayInMonth;


    //all the words that represent percentages
    //for example %, percent, etc...
    private Collection<String> percentWords;

    //stop words to be removed
    protected Collection<String> stopWords;

    //characters to be removed from beginning and end of words
    private Collection<Character> necessaryChars;

    private ParsingHashMap years;

    private List<String> delimeters;

    private Collection<Character> delimitersToSplitWordBy;

    private Collection<String> cityNames;

    //private static CountryService countryService = CountryService.getInstance();

    protected Mutex mutex = new Mutex();

    private DocumentTextTokenList tokenList;


    /**
     * Initializes parser
     * @param stopWords - words to ignore
     */
    public Parse(Collection<String> stopWords){
        initializeDataStructures();
        this.stopWords = stopWords;
        this.tokenList = new DocumentTextTokenList();
    }
    /**
     * default constructor, no list of stop words
     */
    //initializes data structures
    public Parse(){
        initializeDataStructures();
        this.stopWords = new ArrayList<>();
    }
    /**
     * all strings that represent value, (i.g thousand, million)
     * @return
     */
    private Collection<String> getValueKeywords(){
        return valuesAfterNumber.keySet();
    }

    /**
     *
     * @returnall strings that represent a currency
     * for example: US Dollar, Dollar, etc...
     */
    private Collection<String> getCurrencyStrings(){
        return  currencyTypes.keySet();
    }

    /**
     *
     * @return all symbols that represent currency (i.g $)
     */
    private Collection<Character> getCurrencySymbols(){
        return currencySymbols;
    }

    private Collection<String> getMonthWords(){
        return months.keySet();
    }




    //initiazlize diffrent data structures
    //////////////////////////////////////////////////////
    private void initializeValuesAfterNumber(){
        this.valuesAfterNumber = new HashMap<>();
        valuesAfterNumber.put("thousand", Value.THOUSAND);
        valuesAfterNumber.put("Thousand", Value.THOUSAND);
        valuesAfterNumber.put("Million", Value.MILLION);
        valuesAfterNumber.put("million", Value.MILLION);
        valuesAfterNumber.put("m", Value.MILLION);
        valuesAfterNumber.put("M", Value.MILLION);
        valuesAfterNumber.put("billion", Value.BILLION);
        valuesAfterNumber.put("Billion", Value.BILLION);
        valuesAfterNumber.put("bn", Value.BILLION);
        valuesAfterNumber.put("trillion", Value.TRILLION);
        valuesAfterNumber.put("Trillion", Value.TRILLION);
    }

    private void initializeCurrencyTypes() {
        this.currencyTypes = new ParsingHashMap();
        currencyTypes.put("Dollars","Dollars");
        currencyTypes.put("U.S Dollars","Dollars");
        currencyTypes.put("$","Dollars");
    }
    private void initializeDataStructures(){
        initializeValuesAfterNumber();
        initializeCurrencyTypes();
        initializeCurrencySymbols();
        initializeMonths();
        initializeLastDaysInMonth();
        initializePercentWords();
        initializeNecessaryChars();
        initializeYears();
        initializeDelimitersToSplitWordBy();
    }

    private void initializeDelimitersToSplitWordBy() {
        this.delimitersToSplitWordBy = new ArrayList<>();
        delimitersToSplitWordBy.add('-');
    }

    private void initializeNecessaryChars() {
        necessaryChars = new HashSet<>();
        necessaryChars.add('+');
        necessaryChars.add('-');
        necessaryChars.addAll(currencySymbols);
    }

    private void initializePercentWords() {
        this.percentWords = new HashSet();
        percentWords.add("%");
        percentWords.add("percent");
        percentWords.add("Percent");
        percentWords.add("PERCENT");
        percentWords.add("percentage");
        percentWords.add("Percentage");
        percentWords.add("PERCENTAGE");
    }

    private void initializeLastDaysInMonth() {
        this.lastDayInMonth = new HashMap<>();
        lastDayInMonth.put(1,31);
        lastDayInMonth.put(2,29);
        lastDayInMonth.put(3,31);
        lastDayInMonth.put(4,31);
        lastDayInMonth.put(5,30);
        lastDayInMonth.put(6,31);
        lastDayInMonth.put(7,31);
        lastDayInMonth.put(8,31);
        lastDayInMonth.put(9,30);
        lastDayInMonth.put(10,31);
        lastDayInMonth.put(11,30);
        lastDayInMonth.put(12,31);
    }

    private void initializeMonths() {
        this.months = new HashMap<>();
        String [] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
        for (int i = 0; i < months.length; i++) {
            this.months.put(months[i],i+1);
            this.months.put(months[i].toLowerCase(),i+1);
            this.months.put(months[i].toUpperCase(),i+1);
        }
    }

    private void initializeCurrencySymbols(){
        this.currencySymbols = new HashSet<>();
        currencySymbols.add('$');
    }

    private void initializeYears(){
        this.years = new ParsingHashMap();
        String AD = "AD";
        String BC = "BC";
        years.put("AD",AD);
        years.put("A.D",AD);
        years.put("A.D.E",AD);
        years.put("ADE",AD);
        years.put("Year of our Lord",AD);
        years.put("Year of our Lourd",AD);
        years.put("BC",BC);
        years.put("B.C",BC);
        years.put("B.C.E",BC);
        years.put("BCE",BC);
        years.put("Before Christ",BC);
    }

    /////////////////////////////////////////////

    private class OccurrencesListPair{

        private ATerm term;
        private int occurrences;

        OccurrencesListPair(ATerm term, int occurrences){
            this.term=term;
            this.occurrences=occurrences;
        }

        void addOccurrences(int occurrencesToAdd){
            occurrences+=occurrencesToAdd;
        }

        void setOccurrences(int occurrences){
            this.occurrences=occurrences;
        }

        public int getOccurrences() {
            return occurrences;
        }

        public ATerm getTerm(){
            return term;
        }

        public void setTerm(ATerm term){
            this.term=term;
        }
    }

    public Collection<ATerm> parseDocument(List<String> document){
        if(tokenList==null)
            tokenList = new DocumentTextTokenList();
        tokenList.initialize(document,currencySymbols,delimitersToSplitWordBy);
        List<String> cityNames = new ArrayList<>();
        CityTerm documentCity = tokenList.getCityTerm();
        if(documentCity!=null)
            cityNames.add(documentCity.getTerm());
        setCityNames(cityNames);
        return parseDocument(tokenList);
    }

    public Collection<ATerm> parseText(List<String> text){
        ITokenList tokenList = tokenizeText(text);
        return parseDocument(tokenList);
    }

    private ITokenList tokenizeText(List<String> text){
        List<String> newText = new ArrayList<>(text.size());
        for (int i = 0; i < text.size(); i++) {
            String s = text.get(i);
            String[] split = s.split(" ");
            for (int j = 0; j < split.length; j++) {
                newText.add(split[j]);
            }
        }
        return new StringListAsTokenList(newText);
    }
    private Collection<ATerm> parseDocument(ITokenList tokenList){
        Map<ATerm,OccurrencesListPair> occurrencesOfTerms = new HashMap<>();
        addAllTermsToOccurrancesOfTerms(occurrencesOfTerms,tokenList);
        Collection<ATerm> toReturn = getFinalTermCollection(occurrencesOfTerms);
        return toReturn;
    }

    private Collection<ATerm> getFinalTermCollection(Map<ATerm, OccurrencesListPair> occurrencesOfTerms) {
        for (ATerm term:occurrencesOfTerms.keySet()) {
            term = occurrencesOfTerms.get(term).getTerm();
            term.setOccurrences(occurrencesOfTerms.get(term).occurrences);
        }
        return occurrencesOfTerms.keySet();
    }

    private void addAllTermsToOccurrancesOfTerms(Map<ATerm, OccurrencesListPair> occurrencesOfTerms, ITokenList tokenList) {
        while (!tokenList.isEmpty()){
            getNextTerm(occurrencesOfTerms,tokenList);
        }
    }

    private void getNextTerm(Map<ATerm,OccurrencesListPair> occurrencesOfTerms,ITokenList tokenList){
        ATerm nextTerm = null;
        //if list is empty, no tokens
        if(tokenList.isEmpty())
            return;
        Token token = tokenList.pop();
        if(token==null)
            return;
        String tokenString = token.getTokenString();
        if(tokenString==null)
            throw new NullPointerException();
        //if is number
        if(tokenString!=null && isNumber(tokenString)) {
            nextTerm = new NumberTerm(tokenString);
            AddNextNumberTerm(tokenList, nextTerm,occurrencesOfTerms);
        }
        //word
        else {
            addWordTerm(tokenList, token,occurrencesOfTerms);
        }
    }
    private boolean isNumber(String s){
        float [] floats = getNumberValue(s);
        return floats != null;
    }

    private float[] getNumberValue(String s){
        //check if it is already a number
        try {
            float toReturn = Float.parseFloat(s);
            float[] floats = {toReturn};
            return floats;
        }
        //not a double
        catch (Exception e){
            //check if is because of commas
            String [] split = s.split(",");
            StringBuilder toCheck = new StringBuilder();
            for (int i = 0; i < split.length; i++) {
                toCheck.append(split[i]);
            }
            //check if it is a number
            try {
                float toReturn = Float.parseFloat(toCheck.toString());
                float[] floats = {toReturn};
                return floats;
            }
            catch (Exception e2){
                return null;
            }
        }
    }

    private void AddNextNumberTerm(ITokenList tokenList, ATerm nextTerm, Map<ATerm,OccurrencesListPair> occurrencesOfTerms){
        //get next word
        if(!tokenList.isEmpty()) {
            Token nextToken = tokenList.peek();
            String nextTokenString = nextToken==null ? null : nextToken.getTokenString();
            //check if percentage
            if (nextTokenString!=null && percentWords.contains(nextTokenString)) {
                nextTerm = new PercentageTerm((NumberTerm)nextTerm);
                tokenList.pop();
            }
            //check if month or year
            else if(nextTokenString!=null &&
                    (((NumberTerm) nextTerm).isInteger(((NumberTerm) nextTerm))) //number is integer
                    && getMonthWords().contains(nextTokenString) && ((((NumberTerm) nextTerm).getValueOfNumber()>0) && //number is at least one
                        ((NumberTerm) nextTerm).getValueOfNumber()<=lastDayInMonth.get(months.get(nextTokenString))))
            { //number is smaller than last day in month
                    nextTerm = new DateTerm(months.get(nextTokenString),(int)((NumberTerm) nextTerm).getValueOfNumber());
                    tokenList.pop();
            }
            else {
                boolean isFraction = false;
                //check if value
                if (nextTokenString!=null && getValueKeywords().contains(nextTokenString)) {
                    Value val = valuesAfterNumber.get(nextTokenString);
                    ((NumberTerm) nextTerm).multiply(val);
                    //remove keyword after use
                    tokenList.pop();

                }
                //check if fraction
                else if(nextTokenString!=null && isFraction(nextTokenString)){
                    nextTerm = new CompoundFractionTerm((NumberTerm)nextTerm,getFractionTerm(nextTokenString));
                    tokenList.pop();
                    isFraction = true;
                }
                //check if currency
                Pair<String,Integer> currencyNameAndLocation = null;
                if(!tokenList.isEmpty()) {
                    currencyNameAndLocation = getNextRelevantTerm(tokenList,currencyTypes);
                }
                if(currencyNameAndLocation != null){
                    nextTerm = isFraction ? new CompoundFractionCurrencyTerm((CompoundFractionTerm)nextTerm, this.currencyTypes.get(currencyNameAndLocation.getKey()))
                            : new CurrencyTerm((NumberTerm)nextTerm, currencyTypes.get(currencyNameAndLocation.getKey()));
                    for (int i = 0; i<=currencyNameAndLocation.getValue(); i++){
                        tokenList.pop();
                    }
                }
            }
        }
        //no suitable next word found, return number
        if(nextTerm.getTerm().equals("0.03K"))
            nextTerm=nextTerm;
        addTermToOccurrencesList(nextTerm,occurrencesOfTerms);
    }

    private boolean isFraction(String s){
        String[] split = null;
        if(s.contains("/")) {
            split = s.split("/");
        }
        //check two different parts
        if(split==null || split.length!=2)
            return false;
        //check both are numbers
        return isNumber(split[0]) && isNumber(split[1]);
    }

    private boolean isInteger(CharSequence s){
        String string;
        if(s instanceof String)
            string = (String)s;
        else{
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                sb.append(s.charAt(i));
            }
            string = sb.toString();
        }
        try {
            Integer.parseInt(string);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    private static Pair<String, Integer> getNextRelevantTerm(ITokenList tokens, ParsingHashMap toGetFrom){
        Pair<String,Integer> toReturn = null;
        String toCheck = "";
        Collection<String> keys = toGetFrom.keySet();
        List<Token> toPrepend = new ArrayList<>(toGetFrom.getWordsInLongestKey());
        for (int i = 0; i < toGetFrom.getWordsInLongestKey() && !tokens.isEmpty(); i++) {
            Token token = tokens.pop();
            if(token==null)
                break;
            toPrepend.add(token);
            String toAdd = token!=null ? copy(token.getTokenString()):null;
            if(i!=0)
                toCheck+=(" "+toAdd);
            else
                toCheck+=toAdd;
            if(keys.contains(toCheck)) {
                toReturn = new Pair<>(toCheck, i);
                break;
            }
        }
        tokens.prepend(toPrepend);
        return toReturn;
    }

    private static String copy(CharSequence sequence){
        if(sequence==null)
            return null;
        StringBuilder builder = new StringBuilder(sequence.length());
        String s = sequence.toString();
        for (int i = 0; i < s.length(); i++) {
            builder.append(s.charAt(i));
        }
        return builder.toString();
    }

    private FractionTerm getFractionTerm(String s){
        String[] split = s.split("/");
        NumberTerm numerator = new NumberTerm(split[0]);
        NumberTerm denominator = new NumberTerm(split[1]);
        return new FractionTerm(numerator,denominator);
    }

    private void addTermToOccurrencesList(ATerm term, Map<ATerm, OccurrencesListPair> occurrencesList){
        if(term instanceof WordTerm){
            addWordTermToOccurrencesList((WordTerm) term,occurrencesList,Character.isLowerCase(term.getTerm().charAt(0)));
        }
        else
            addTermToOccurrencesList(term,occurrencesList,true);
    }

    private void addTermToOccurrencesList(ATerm term,Map<ATerm,OccurrencesListPair> occurrencesList,boolean noSpecialCase){
        if(term instanceof WordTerm && !noSpecialCase)
            addTermToOccurrencesList(term,occurrencesList);
        else{
            if(occurrencesList.keySet().contains(term)){
                OccurrencesListPair pair = occurrencesList.get(term);
                pair.addOccurrences(1);
            }
            else {
                occurrencesList.put(term,new OccurrencesListPair(term,1));
            }
        }
    }

    private void addWordTermToOccurrencesList(WordTerm term, Map<ATerm,OccurrencesListPair> occurrencesOfTerms, boolean isLowerCase){
        if(term instanceof CityTerm){
            addCityTermToOccurrencesList((CityTerm) term,occurrencesOfTerms,isLowerCase);
            return;
        }
        term.toLowerCase();
        boolean existsLowercase = occurrencesOfTerms.containsKey(term);
        term.toUperCase();
        boolean existsUppercase = occurrencesOfTerms.containsKey(term);


        if(isLowerCase && existsUppercase){
            OccurrencesListPair occurrencesListPair = occurrencesOfTerms.get(term);
            occurrencesListPair.addOccurrences(1);
            occurrencesOfTerms.remove(term);
            term.toLowerCase();
            if(existsLowercase) {
                OccurrencesListPair correctPair = occurrencesOfTerms.get(term);
                correctPair.addOccurrences(occurrencesListPair.getOccurrences());
            }
            else {
                occurrencesListPair.setTerm(term);
                occurrencesOfTerms.put(term,occurrencesListPair);
            }
        }
        else if(isLowerCase){
            term.toLowerCase();
            addTermToOccurrencesList(term,occurrencesOfTerms,true);
        }
        else if (existsLowercase){
            term.toLowerCase();
            addTermToOccurrencesList(term,occurrencesOfTerms,true);
        }
        else
            addTermToOccurrencesList(term,occurrencesOfTerms,true);
    }

    private void addCityTermToOccurrencesList(CityTerm term, Map<ATerm, OccurrencesListPair> occurrencesOfTerms, boolean isLowerCase) {
        if (occurrencesOfTerms.keySet().contains(term)){
            OccurrencesListPair occurrencesListPair = occurrencesOfTerms.get(term);
            CityTerm cityTerm = (CityTerm) occurrencesListPair.getTerm();
            int occurrencesToAdd = term.getOccurrences()+1;
            occurrencesListPair.addOccurrences(occurrencesToAdd);
        }
        else
            occurrencesOfTerms.put(term,new OccurrencesListPair(term,1));
    }

    private void addWordTerm(ITokenList tokens, Token token, Map<ATerm, OccurrencesListPair> occurrencesOfTerms){
        ATerm nextTerm = null;
        String tokenString = token.getTokenString();
        //check percentage
        if(isPercentage(tokenString)){
            nextTerm = getPercentageTerm(tokenString);
            addTermToOccurrencesList(nextTerm,occurrencesOfTerms);
            return;
        }
        //check currency
        else if(isCurrency(tokenString)){
            nextTerm = getCurrencyTerm(tokenString,tokens);
            //check if next is value
            /*Token nextToken = tokens.isEmpty()?null : tokens.peek();
            String nextTokenString = nextToken==null ? null : nextToken.getTokenString();
            if(nextTokenString!=null && valuesAfterNumber.keySet().contains(nextTokenString)){
                ((CurrencyTerm)nextTerm).multiply(valuesAfterNumber.get(nextTokenString));
                tokens.pop();
            }*/
            addTermToOccurrencesList(nextTerm,occurrencesOfTerms);
            return;
        }
        //check month
        else if(getMonthWords().contains(tokenString)){
            Token nextToken = tokens.isEmpty() ? null : tokens.peek();
            String nextTokenString = nextToken==null ? null : nextToken.getTokenString();
            if(nextTokenString!=null && isNumber(nextTokenString) && isInteger(nextTokenString)){
                int monthPair = Integer.parseInt(nextTokenString);
                if(monthPair>0 && monthPair<lastDayInMonth.get(months.get(tokenString))){
                    nextTerm = new DateTerm(months.get(tokenString),monthPair);
                    addTermToOccurrencesList(nextTerm,occurrencesOfTerms);
                    tokens.pop();
                    return;
                }
                else {
                    nextTerm = new YearTerm(months.get(tokenString),monthPair);
                    addTermToOccurrencesList(nextTerm,occurrencesOfTerms);
                    tokens.pop();
                    return;
                }
            }
        }
        //check hyphenated word
        else if(isHyphenatedWord(tokenString)){
            List<ATerm> toAdd = getHyphenatedTokens(token,tokens);
            for (ATerm termToAdd:toAdd) {
                addTermToOccurrencesList(termToAdd,occurrencesOfTerms);
            }
            return;
        }
        boolean isNumber = false;
        boolean isFraction = false;
        //check number with value
        if(isNumberWithValue(tokenString)){
            isNumber =true;
            nextTerm = splitWord(tokenString);
            //if list is now empty, return, else switch token to next word
            if(tokens.isEmpty()){
                addTermToOccurrencesList(nextTerm,occurrencesOfTerms);
                return;
            }
            else{
                tokenString = tokens.get(0).getTokenString();
            }
        }
        //check fraction
        if(isFraction(tokenString)){
            isFraction = true;
            nextTerm = isNumber ? new CompoundFractionTerm((NumberTerm) nextTerm, getFractionTerm(tokenString)) : getFractionTerm(tokenString);
            if(isNumber)
                tokens.pop();
            isNumber = true;
            //if list is now empty, return, else switch token to next word
            if(tokens.isEmpty()){
                addTermToOccurrencesList(nextTerm,occurrencesOfTerms);
                return;
            }
            else{
                token = tokens.get(0);
            }
        }
        //check currency
        if(isNumber && getCurrencyStrings().contains(token)){
            nextTerm = isFraction ? new CompoundFractionCurrencyTerm((CompoundFractionTerm) nextTerm,tokenString) : new CurrencyTerm((NumberTerm) nextTerm,tokenString);
            tokens.pop();
            addTermToOccurrencesList(nextTerm,occurrencesOfTerms);
            return;
        }

        //split word by non numbers and letter
        List<ATerm> toAdd = getFinalWordTermList(token,tokens);
        for (ATerm termToAdd:toAdd) {
            addTermToOccurrencesList(termToAdd,occurrencesOfTerms);
        }
        return;
    }

    private PercentageTerm getPercentageTerm(String s){
        if(isPercentage(s)){
            NumberTerm term = new NumberTerm(s.substring(0,s.length()-1));
            return new PercentageTerm(term);
        }
        else if(isNumber(s)){
            NumberTerm term = new NumberTerm(s);
            return new PercentageTerm(term);
        }
        return null;
    }

    private boolean isPercentage(String token) {
        return (token.length()>1 && token.charAt(token.length()-1)=='%' && isNumber(token.substring(0,token.length()-1)));
    }

    private boolean isCurrency(String s){
        if(s.length()>1){
            char first = s.charAt(0);
            if(getCurrencySymbols().contains(first) && (isNumber(s.substring(1)) || isNumberWithValue(s.substring(1))))
                return true;
        }
        return false;
    }

    private List<ATerm> getFinalWordTermList(Token s, ITokenList tokens){
        return getFinalWordTermList(s,tokens,new ArrayList<>(0));
    }
    private List<ATerm> getFinalWordTermList(Token token, ITokenList tokens, Collection<Character> delimitersToIgnore) {
        List<ATerm> toReturn = new ArrayList<>();
        String s = token.getTokenString();
        int position = token.getPosition();
        //get all desired substrings
        //split the word into parts
        List<Pair<Integer,Integer>> desiredSubstrings = new ArrayList<>();
        for (int i = 0, firstDesiredIndex=0; i < s.length(); i++) {
            if(!Character.isLetter(s.charAt(i)) && !Character.isDigit(s.charAt(i)) && !delimitersToIgnore.contains(s.charAt(i))){
                int newFirstDesiredIndex;
                if(currencySymbols.contains(s.charAt(i))) {
                    if(i==firstDesiredIndex)
                        continue;
                    newFirstDesiredIndex = i;
                }
                else
                    newFirstDesiredIndex=i+1;

                if(i==s.length()-1){
                    if(i>firstDesiredIndex)
                        desiredSubstrings.add(new Pair<>(firstDesiredIndex,i));
                }
                else
                    desiredSubstrings.add(new Pair<>(firstDesiredIndex,i));
                firstDesiredIndex = newFirstDesiredIndex;
            }
            else if(i==s.length()-1)
                desiredSubstrings.add(new Pair<>(firstDesiredIndex,i+1));
        }
        //check if only one string
        if(desiredSubstrings.isEmpty())
            return toReturn;
        else if(desiredSubstrings.size()==1){
            token.setTokenString(s.substring(desiredSubstrings.get(0).getKey(),desiredSubstrings.get(0).getValue()));
            WordTerm term = createWordTerm(token);
            if(term!=null)
                toReturn.add(term);
            return toReturn;
        }
        List<Token> tokensToAdd = new ArrayList<>();

        for (Pair<Integer,Integer> substring:desiredSubstrings) {
            Token tokenToAdd = new Token(s.substring(substring.getKey(),substring.getValue()),position);
            if(tokenToAdd!= null)
                tokensToAdd.add(tokenToAdd);
        }
        //prepend desired tokens to list
        tokens.prepend(tokensToAdd);
        return toReturn;
    }

    protected WordTerm createWordTerm(Token token) {
        /*boolean isCityTerm = cityNames!=null && cityNames.contains(token.getTokenString());
        if(isCityTerm){
            return createCityTerm(token);
        }*/
        boolean isStopWord = isStopWord(token.getTokenString());
        if(!isStopWord)
            return new WordTerm(token.getTokenString());
        return null;
    }

    private CityTerm createCityTerm(Token token) {
        return new CityTerm(token.getTokenString(),token.getPosition());
    }

    protected boolean isStopWord(String s){
        mutex.lock();
        boolean isStopWord = stopWords.contains(s.toLowerCase());
        mutex.unlock();
        return isStopWord;
    }

    private boolean isHyphenatedWord(String token) {
        for (char delimiter:delimitersToSplitWordBy) {
            if(token.contains(""+delimiter)){
                String[] split = token.split(""+delimiter);
                return (split!=null && split.length>1 && split[0].length()>0 && split[1].length()>0);
            }

        }
        return false;
    }

    private CurrencyTerm getCurrencyTerm(String s, ITokenList tokens){
        Value val = null;
        if(!tokens.isEmpty() && getValueKeywords().contains(tokens.peek().getTokenString()))
            val = valuesAfterNumber.get(tokens.pop().getTokenString());
        if(isCurrency(s)){
            String currencySymbol = s.substring(0,1);
            String currency = currencyTypes.get(currencySymbol);
            String number = s.substring(1);
            boolean isNumberWithValue = isNumberWithValue(number);
            NumberTerm term = isNumberWithValue ? splitWord(number) : new NumberTerm(number);
            if(val!=null)
                term.multiply(val);
            return new CurrencyTerm(term,currency);
        }
        return null;
    }
    private List<ATerm> getHyphenatedTokens(Token token, ITokenList tokens) {
        List<ATerm> hyphenatedToken = getFinalWordTermList(token,tokens,delimitersToSplitWordBy);
        if(!(hyphenatedToken==null || hyphenatedToken.isEmpty())){
            hyphenatedToken.addAll(getFinalWordTermList(token,tokens));
        }
        return hyphenatedToken;
    }

    private boolean isNumberWithValue(String s){
        //get last index of number
        boolean number = true;
        int pointer = 0;
        while (pointer<s.length() && number){
            number = Character.isDigit(s.charAt(pointer)) || s.charAt(pointer)=='.';
            if(number)
                pointer++;
        }
        //check if is number and word after it represents value
        if(pointer>0 && pointer<s.length()){
            String numString = s.substring(0,pointer);
            String word = s.substring(pointer);
            if(isNumber(numString) && getValueKeywords().contains(word))
                return true;
            return false;
        }
        return false;
    }

    private NumberTerm splitWord(String s){
        boolean number = true;
        int pointer = 0;
        while (pointer<s.length() && number){
            number = Character.isDigit(s.charAt(pointer)) || s.charAt(pointer)=='.';
            if(number)
                pointer++;
        }
        String numString = s.substring(0,pointer);
        String word = s.substring(pointer);

        NumberTerm toReturn = new NumberTerm(numString);
        toReturn.multiply(valuesAfterNumber.get(word));
        return toReturn;
    }

    public ITokenList toTokenList(List<String> list){
        return new StringListAsTokenList(list);
    }

    public void setStopWords(Collection<String> stopWords){
        this.stopWords=stopWords;
    }

    public void setCityNames(Collection<String> cityNames){
        this.cityNames=cityNames;
    }

    private class StringListAsTokenList extends ATokenList {

        private List<String> list;
        private int listIndex = 0;

        StringListAsTokenList(List<String> list){
            this.list = list;
        }

        @Override
        protected void initializeAllFields() {
            prepended = new ArrayList<>();
            appended = new ArrayList<>();
        }

        @Override
        protected void createNextToken() {
            if(!prepended.isEmpty())
                next = prepended.remove(0);
            else{
                next = !list.isEmpty() ? new Token(list.remove(0),listIndex) : null;
                listIndex++;
                if(next==null){
                    next = !(appended==null || appended.isEmpty()) ? appended.remove(0) : null;
                }
            }
        }

        @Override
        public void clear() {
            list.clear();
            listIndex=0;
        }

        @Override
        public boolean isEmpty() {
            return next==null && prepended.isEmpty() && appended.isEmpty() && list.isEmpty();
        }

        @Override
        public void initialize(List<String> documentLines, Collection<Character> currencySymbols, Collection<Character> delimitersToSplitWordBy) {

        }
    }

    /*public Collection<ATerm> parseDocumentTextWithoutLocations(List<String> document){
        Map<ATerm,Integer> occurrencesOfTerms = new HashMap<>();
        List<String> tokens=tokenize(document);
        List<ATerm> terms=getAllTerms(tokens);

        return getFinalList(terms, occurrencesOfTerms);
        //remove stop words.
        //allow stemming.
    }

    private List<ATerm> getAllTerms(List<String> tokens){
        List<ATerm> next;
        List<ATerm> terms = new ArrayList<>();
        do{
            next = getNextTerm(tokens);
            if(next!=null) {
                terms.addAll(next);
            }
        }while (next != null);
        return terms;
    }

    public Collection<ATerm> parseDocument(List<String> documentLines){
        List<Pair<String,Integer>> tokensAndLocations = new ArrayList<>();
        List<ATerm> finalList = new ArrayList<>();
        Map<ATerm,Pair<ATerm,Integer>> occurrencesOfTerms = new HashMap<>();
        addCityNameAndText(documentLines,tokensAndLocations,occurrencesOfTerms);

        finalList.addAll(getAllTermsFromStringAndInteger(tokensAndLocations));

        return getFinalListFromTermAndInteger(finalList,occurrencesOfTerms);
    }

    private List<ATerm> getAllTermsFromStringAndInteger(List<Pair<String, Integer>> tokensAndLocations) {
        List<ATerm> next;
        List<ATerm> terms = new ArrayList<>();
        do{
            next = getNextTermFromStringAndInteger(tokensAndLocations);
            if(next!=null) {
                terms.addAll(next);
            }
        }while (next != null);
        return terms;
    }


    private void addCityNameAndText(List<String> documentLines, List<Pair<String,Integer>> fileTextAndLocations, Map<ATerm,Pair<ATerm,Integer>> addCityTermTo){
        int wordCount = 0;
        boolean isText = false;
        for (String documentLine:documentLines) {
            boolean changedWordCount = false;
            if(documentLine.contains("<F P=104>")){
                wordCount+=addCityTermAndChangeWordCount(documentLine,addCityTermTo);
                changedWordCount = true;
            }
            if (documentLine.equals("<TEXT>")) {
                isText = true;
                if(!changedWordCount)
                    wordCount+=1;
                changedWordCount = true;
            }
            if (documentLine.equals("</TEXT>")) {
                isText = false;
                if(!changedWordCount)
                    wordCount+=1;
                changedWordCount = true;
            }
            if (isText) {
                if(!changedWordCount)
                    wordCount+=addTextToFileTextAndLocations(documentLine,fileTextAndLocations,wordCount);
                changedWordCount = true;
            }
            if(!changedWordCount)
                wordCount+=getDocumentLineWordCount(documentLine);
        }

    }

    private int addTextToFileTextAndLocations(String textLine, List<Pair<String, Integer>> fileTextAndLocations, int wordCount) {
        String[] splitTextLine = textLine.split(" ");
        for (String word:splitTextLine) {
            if(word.length()==0){
                wordCount++;
                continue;
            }
            //add word to list
            word = removeUnnecessaryChars(word);
            if(word!=null && word.length()>0){
                fileTextAndLocations.add(new Pair<>(word,wordCount));
            }
            //increase wordCount
            wordCount++;
        }
        return getDocumentLineWordCount(textLine);
    }

    private int addCityTermAndChangeWordCount(String documentLine, Map<ATerm,Pair<ATerm,Integer>> addCityTermTo) {
        String[] splitLine = documentLine.split(" ");
        int firstCityIndex = 0;
        //find first index in array that may contain city name
        while (!splitLine[firstCityIndex].equals("P=104>"))
            firstCityIndex++;
        firstCityIndex++;
        while (splitLine[firstCityIndex].length()==0 && firstCityIndex<splitLine.length)
            firstCityIndex++;
        if(firstCityIndex==splitLine.length)
            return splitLine.length;

        //find city
        boolean foundCity = false;
        String cityName = splitLine[firstCityIndex];
        while (!foundCity && firstCityIndex<splitLine.length){
            List<Country> countryList = countryService.getByCapital(cityName);
            if(countryList!=null && !countryList.isEmpty()){
                CityTerm cityTerm = new CityTerm(cityName);
                cityTerm.setAsOrigin();
                addCityTermTo.put(cityTerm,new Pair<>(cityTerm,0));
                break;
            }
            while (splitLine[firstCityIndex].length()==0 && firstCityIndex<=splitLine.length)
                firstCityIndex++;
            if(firstCityIndex==splitLine.length)
                return splitLine.length;
            cityName+=" "+splitLine[firstCityIndex];
        }
        return splitLine.length;
    }

    private int getDocumentLineWordCount(String documentLine){
        if (documentLine.length()==0)
            return 0;
        String[] splitLine = documentLine.split(" ");
        return splitLine.length;
    }



    private void addToOccurancesList(ATerm term, Map<ATerm, Integer> occurances) {
        occurances.putIfAbsent(term,0);
        occurances.replace(term,occurances.get(term)+1);
    }

    private void addToOccurrencesListPairAndInteger(ATerm term, Map<ATerm, Pair<ATerm,Integer>> occurrences) {
        if(occurrences.keySet().contains(term)){
            Pair<ATerm,Integer> oldPair = occurrences.get(term);
            Pair<ATerm,Integer> newPair = new Pair<>(oldPair.getKey(),oldPair.getValue()+1);
            occurrences.replace(term,newPair);
        }
        else {
            occurrences.put(term,new Pair<>(term,1));
        }
    }

    private void addWordTermToList(WordTerm term, Map<ATerm,Integer> occurrencesOfTerms, boolean isLowerCase){
        term.toLowerCase();
        boolean existsLowercase = occurrencesOfTerms.containsKey(term);
        term.toUperCase();
        boolean existsUppercase = occurrencesOfTerms.containsKey(term);

        if(isLowerCase && existsUppercase){
            int occurrancesOfTerm = occurrencesOfTerms.get(term);
            occurrencesOfTerms.remove(term);
            term.toLowerCase();
            if(existsLowercase)
                occurrencesOfTerms.replace(term,occurrencesOfTerms.get(term)+occurrancesOfTerm+1);
            else
                occurrencesOfTerms.put(term,occurrancesOfTerm+1);
        }
        else if(isLowerCase){
            term.toLowerCase();
            addToOccurancesList(term, occurrencesOfTerms);
        }
        else if (existsLowercase){
            term.toLowerCase();
            addToOccurancesList(term,occurrencesOfTerms);
        }
        else
            addToOccurancesList(term,occurrencesOfTerms);
    }

    private void addWordTermToListTermAndInteger(WordTerm term, Map<ATerm,Pair<ATerm,Integer>> occurrencesOfTerms, boolean isLowerCase){
        if(term instanceof CityTerm){
            addCityTermToList((CityTerm) term,occurrencesOfTerms,isLowerCase);
            return;
        }
        term.toLowerCase();
        boolean existsLowercase = occurrencesOfTerms.containsKey(term);
        term.toUperCase();
        boolean existsUppercase = occurrencesOfTerms.containsKey(term);


        if(isLowerCase && existsUppercase){
            Pair<ATerm,Integer> oldPair = occurrencesOfTerms.get(term);
            int occurrancesOfTerm = oldPair.getValue()+1;
            occurrencesOfTerms.remove(term);
            term.toLowerCase();
            if(existsLowercase) {
                occurrancesOfTerm+=occurrencesOfTerms.get(term).getValue();
                occurrencesOfTerms.replace(term, new Pair<>(term,occurrancesOfTerm));
            }
            else
                occurrencesOfTerms.put(term,new Pair<>(term,occurrancesOfTerm));
        }
        else if(isLowerCase){
            term.toLowerCase();
            addToOccurrencesListPairAndInteger(term,occurrencesOfTerms);
        }
        else if (existsLowercase){
            term.toLowerCase();
            addToOccurrencesListPairAndInteger(term,occurrencesOfTerms);
        }
        else
            addToOccurrencesListPairAndInteger(term,occurrencesOfTerms);
    }

    private void addCityTermToList(CityTerm term, Map<ATerm, Pair<ATerm,Integer>> occurrencesOfTerms, boolean isLowerCase) {
        if(occurrencesOfTerms.keySet().contains(term)){
            Pair<ATerm,Integer> oldPair = occurrencesOfTerms.get(term);
            //get city
            CityTerm cityTerm = (CityTerm) oldPair.getKey();
            cityTerm.addAllPositions(term);

            //get occurrences
            int occurrences = oldPair.getValue()+1;

            occurrencesOfTerms.remove(term);
            occurrencesOfTerms.put(cityTerm,new Pair<>(cityTerm,occurrences));
        }
        else {
            occurrencesOfTerms.put(term,new Pair<>(term,1));
        }
    }


    Collection<ATerm> getFinalList(List<ATerm> from, Map<ATerm,Integer> occurrencesOfTerms){
        for (ATerm t:from) {
            if(t instanceof WordTerm){
                addWordTermToList((WordTerm) t,occurrencesOfTerms,Character.isLowerCase(t.getTerm().charAt(0)));
            }
            else{
                addToOccurancesList(t,occurrencesOfTerms);
            }
        }
        for (ATerm t:occurrencesOfTerms.keySet()) {
            t.setOccurrences(occurrencesOfTerms.get(t));
        }
        return occurrencesOfTerms.keySet();
    }

    Collection<ATerm> getFinalListFromTermAndInteger(List<ATerm> from, Map<ATerm,Pair<ATerm,Integer>> occurrencesOfTerms){
        for (ATerm t:from) {
            if(t instanceof WordTerm){
                addWordTermToListTermAndInteger((WordTerm) t,occurrencesOfTerms,Character.isLowerCase(t.getTerm().charAt(0)));
            }
            else{
                addToOccurrencesListPairAndInteger(t,occurrencesOfTerms);
            }
        }
        for (ATerm t:occurrencesOfTerms.keySet()) {
            t.setOccurrences(occurrencesOfTerms.get(t).getValue());
        }
        return occurrencesOfTerms.keySet();
    }
    protected void addTermsToList(List<ATerm> addFrom, Map<ATerm, Integer> occurrencesOfTerms){
        for (ATerm t: addFrom) {
            if(occurrencesOfTerms.containsKey(t)){
                int oldValue = occurrencesOfTerms.get(t);
                occurrencesOfTerms.replace(t,oldValue+1);
            }
            else {
                occurrencesOfTerms.put(t,1);
            }
        }
    }


    /**
     * removes unnecessary chars from the token list, e.g '.' ','
     * @param tokens- the token list we remove chars from
     */
    /*protected void removeUnnecessaryChars(List<String> tokens) {
        List<Character> unnecessaryChars=new ArrayList<>();
        unnecessaryChars.add('.');
        unnecessaryChars.add(',');
        tokens.replaceAll(token->{
            if(unnecessaryChars.contains(token.charAt(0)))
                token=token.substring(1);
            if(unnecessaryChars.contains(token.substring(token.length() - 1)))
                token=token.substring(0,token.length()-1);
            return token;
        });
    }*/

    /**
     * divided the input queue of documents lines into list of words/tokens.
     * @param document- the documents lines.
     * @return the queue of document words.
     */
   /* protected List<String> tokenize(List<String> document) {
        List<String> tokens=new LinkedList<>();
        document.forEach(line -> {
            String[]lineTokens = line.split(" ");
            //remove unnecessarry chars and add to list
            for (int i = 0; i < lineTokens.length; i++) {
                lineTokens[i] = removeUnnecessaryChars(lineTokens[i]);
                if(lineTokens[i]!=null)
                    tokens.add(lineTokens[i]);
            }

        });
        return tokens;
    }

    private String removeUnnecessaryChars(String lineToken) {
        if(lineToken==null || lineToken.equals(""))
            return null;
        int firstNecessary = 0;
        int lastNecessary = lineToken.length()-1;
        //find first necessary index
        boolean foundFirstIndex = (Character.isDigit(lineToken.charAt(firstNecessary)) || Character.isLetter(lineToken.charAt(firstNecessary)));
        while (!foundFirstIndex && firstNecessary<lineToken.length()){
            foundFirstIndex = (Character.isDigit(lineToken.charAt(firstNecessary)) || Character.isLetter(lineToken.charAt(firstNecessary)))||
                    (firstNecessary>lineToken.length()-1 && delimitersToSplitWordBy.contains(lineToken.charAt(firstNecessary)) && Character.isDigit(lineToken.charAt(firstNecessary+1)));
            if(!foundFirstIndex)
                firstNecessary++;
        }
        if(firstNecessary>lastNecessary)
            return null;
        while (lastNecessary>0 && !(Character.isDigit(lineToken.charAt(lastNecessary)) ||//first digit is not digit
                Character.isLetter(lineToken.charAt(lastNecessary)) ||//first digit is not letter
                currencySymbols.contains(""+lineToken.charAt(lastNecessary)))){ //first digit is not currency
            lastNecessary--;
        }
        if(firstNecessary>lastNecessary)
            return null;
        if(firstNecessary!=0 || lastNecessary!=lineToken.length()-1)
            lineToken = lineToken.substring(firstNecessary,lastNecessary+1);
        if(lineToken.length()>=2 && lineToken.substring(lineToken.length()-2,lineToken.length()).equals("'s"))
            lineToken = lineToken.substring(0,lineToken.length()-2);
        return lineToken.length()>0 ? lineToken : null;
    }
*/

    /**
     * returns number value for string if it is an array with single number, returns null if no such number
     * will ignore commas, and work for strings without letters
     * will work for 10.3,  100,000  , 5
     * will not work for 10m
     * @param s string to getValue of value
     * @return Value of s in number
     */
    /*private float[] getNumberValue(String s){
        //check if it is already a number
        try {
            float toReturn = Float.parseFloat(s);
            float[] floats = {toReturn};
            return floats;
        }
        //not a double
        catch (Exception e){
            //check if is because of commas
            String [] split = s.split(",");
            StringBuilder toCheck = new StringBuilder();
            for (int i = 0; i < split.length; i++) {
                toCheck.append(split[i]);
            }
            //check if it is a number
            try {
                float toReturn = Float.parseFloat(toCheck.toString());
                float[] floats = {toReturn};
                return floats;
            }
            catch (Exception e2){
                return null;
            }

        }
    }

    private boolean isNumber(String s){
        float [] floats = getNumberValue(s);
        return floats != null;
    }

    private boolean isCurrency(String s){
        if(s.length()>1){
            char first = s.charAt(0);
            if(getCurrencySymbols().contains(first) && isNumber(s.substring(1)))
                return true;
        }
        return false;
    }

    private boolean isFraction(String s){
        String[] split = null;
        if(s.contains("/")) {
            split = s.split("/");
        }
        //check two different parts
        if(split==null || split.length!=2)
            return false;
        //check both are numbers
        return isNumber(split[0]) && isNumber(split[1]);
    }

    private FractionTerm getFractionTerm(String s){
        String[] split = s.split("/");
        NumberTerm numerator = new NumberTerm(split[0]);
        NumberTerm denominator = new NumberTerm(split[1]);
        return new FractionTerm(numerator,denominator);
    }

    private void AddNextNumberTermFromStringAndInteger(List<Pair<String,Integer>> tokens, ATerm nextTerm, List<ATerm> toReturn){
        //get next word
        if(!tokens.isEmpty()) {
            String nextToken = tokens.get(0).getKey();
            //check year
            Pair<String,Integer> year = null;
            if(isInteger(((NumberTerm)nextTerm).getValue())){
                year=getNextRelevantTermFromStringAndInteger(tokens,years);
            }
            if(year!=null){
                nextTerm = new YearTerm((NumberTerm) nextTerm,this.years.get(year.getKey()));
                for (int i = 0; i <= year.getValue(); i++) {
                    tokens.remove(0);
                }
            }
            //check if percentage
            else if (percentWords.contains(nextToken)) {
                nextTerm = new PercentageTerm((NumberTerm)nextTerm);
                tokens.remove(0);
            }
            //check if month
            else if((((NumberTerm) nextTerm).isInteger(((NumberTerm) nextTerm))) //number is integer
                    && getMonthWords().contains(nextToken) && //next word is month
                    (((NumberTerm) nextTerm).getValueOfNumber()>0) && //number is at least one
                    ((NumberTerm) nextTerm).getValueOfNumber()<=lastDayInMonth.get(months.get(nextToken)) //number is smaller than last day in month
            ){
                nextTerm = new DateTerm(months.get(nextToken),(int)((NumberTerm) nextTerm).getValueOfNumber());
                tokens.remove(0);
            }
            else {
                boolean isFraction = false;
                //check if value
                if (getValueKeywords().contains(nextToken)) {
                    Value val = valuesAfterNumber.get(nextToken);
                    ((NumberTerm) nextTerm).multiply(val);
                    //remove keyword after use
                    tokens.remove(0);

                }
                //check if fraction
                else if(isFraction(nextToken)){
                    nextTerm = new CompoundFractionTerm((NumberTerm)nextTerm,getFractionTerm(nextToken));
                    tokens.remove(0);
                    isFraction = true;
                }
                //check if currency
                Pair<String,Integer> currencyNameAndLocation = null;
                if(!tokens.isEmpty()) {
                    currencyNameAndLocation = getNextRelevantTermFromStringAndInteger(tokens,currencyTypes);
                }
                if(currencyNameAndLocation != null){
                    nextTerm = isFraction ? new CompoundFractionCurrencyTerm((CompoundFractionTerm)nextTerm, this.currencyTypes.get(currencyNameAndLocation.getKey()))
                            : new CurrencyTerm((NumberTerm)nextTerm, currencyTypes.get(currencyNameAndLocation.getKey()));
                    for (int i = 0; i<=currencyNameAndLocation.getValue(); i++){
                        tokens.remove(0);
                    }
                }
            }
        }
        //no suitable next word found, return number
        toReturn.add(nextTerm);
    }

    private void AddNextNumberTerm(List<String> tokens, ATerm nextTerm, List<ATerm> toReturn){
        //get next word
        if(!tokens.isEmpty()) {
            String nextToken = tokens.get(0);
            //check year
            Pair<String,Integer> year = null;
            if(isInteger(((NumberTerm)nextTerm).getValue())){
                year=getNextRelevantTerm(tokens,years);
            }
            if(year!=null){
                nextTerm = new YearTerm((NumberTerm) nextTerm,this.years.get(year.getKey()));
                for (int i = 0; i <= year.getValue(); i++) {
                    tokens.remove(0);
                }
            }
            //check if percentage
            else if (percentWords.contains(nextToken)) {
                nextTerm = new PercentageTerm((NumberTerm)nextTerm);
                tokens.remove(0);
            }
            //check if month
            else if((((NumberTerm) nextTerm).isInteger(((NumberTerm) nextTerm))) //number is integer
                    && getMonthWords().contains(nextToken) && //next word is month
                    (((NumberTerm) nextTerm).getValueOfNumber()>0) && //number is at least one
                    ((NumberTerm) nextTerm).getValueOfNumber()<=lastDayInMonth.get(months.get(nextToken)) //number is smaller than last day in month
            ){
                nextTerm = new DateTerm(months.get(nextToken),(int)((NumberTerm) nextTerm).getValueOfNumber());
                tokens.remove(0);
            }
            else {
                boolean isFraction = false;
                //check if value
                if (getValueKeywords().contains(nextToken)) {
                    Value val = valuesAfterNumber.get(nextToken);
                    ((NumberTerm) nextTerm).multiply(val);
                    //remove keyword after use
                    tokens.remove(0);

                }
                //check if fraction
                else if(isFraction(nextToken)){
                    nextTerm = new CompoundFractionTerm((NumberTerm)nextTerm,getFractionTerm(nextToken));
                    tokens.remove(0);
                    isFraction = true;
                }
                //check if currency
                Pair<String,Integer> currencyNameAndLocation = null;
                if(!tokens.isEmpty()) {
                    currencyNameAndLocation = getNextRelevantTerm(tokens,currencyTypes);
                }
                if(currencyNameAndLocation != null){
                    nextTerm = isFraction ? new CompoundFractionCurrencyTerm((CompoundFractionTerm)nextTerm, this.currencyTypes.get(currencyNameAndLocation.getKey()))
                            : new CurrencyTerm((NumberTerm)nextTerm, currencyTypes.get(currencyNameAndLocation.getKey()));
                    for (int i = 0; i<=currencyNameAndLocation.getValue(); i++){
                        tokens.remove(0);
                    }
                }
            }
        }
        //no suitable next word found, return number
        toReturn.add(nextTerm);
    }

    private void addWordTerm(List<String> tokens, String token,List<ATerm> toReturn){
        ATerm nextTerm = null;
        //check percentage
        if(isPercentage(token)){
            nextTerm = getPercentageTerm(token);
            toReturn.add(nextTerm);
            return;
        }
        //check currency
        else if(isCurrency(token)){
            nextTerm = getCurrencyTerm(token,tokens);
            toReturn.add(nextTerm);
            return;
        }
        //check month
        else if(getMonthWords().contains(token)){
            String nextToken = tokens.isEmpty() ? null : tokens.get(0);
            if(nextToken!=null && isNumber(nextToken) && isInteger(nextToken)){
                int day = Integer.parseInt(nextToken);
                if(day>0 && day<lastDayInMonth.get(months.get(token))){
                    nextTerm = new DateTerm(months.get(token),day);
                    toReturn.add(nextTerm);
                    tokens.remove(0);
                    return;
                }
            }
        }
        //check hyphenated word
        else if(isHyphenatedWord(token)){
            toReturn.addAll(getHyphenatedTokens(token,tokens));
            return;
        }
        boolean isNumber = false;
        boolean isFraction = false;
        //check number with value
        if(isNumberWithValue(token)){
            isNumber =true;
            nextTerm = splitWord(token);
            //if list is now empty, return, else switch token to next word
            if(tokens.isEmpty()){
                toReturn.add(nextTerm);
                return;
            }
            else{
                token = tokens.get(0);
            }
        }
        //check fraction
        if(isFraction(token)){
            isFraction = true;
            nextTerm = isNumber ? new CompoundFractionTerm((NumberTerm) nextTerm, getFractionTerm(token)) : getFractionTerm(token);
            if(isNumber)
                tokens.remove(0);
            isNumber = true;
            //if list is now empty, return, else switch token to next word
            if(tokens.isEmpty()){
                toReturn.add(nextTerm);
                return;
            }
            else{
                token = tokens.get(0);
            }
        }
        //check currency
        if(isNumber && getCurrencyStrings().contains(token)){
            nextTerm = isFraction ? new CompoundFractionCurrencyTerm((CompoundFractionTerm) nextTerm,token) : new CurrencyTerm((NumberTerm) nextTerm,token);
            tokens.remove(0);
            toReturn.add(nextTerm);
            return;
        }

        //split word by non numbers and letter
        toReturn.addAll(getFinalWorTermList(token,tokens));
        return;
    }

    private void addWordTermFromStringAndInteger(List<Pair<String,Integer>> tokens,Pair<String,Integer> tokenPair,List<ATerm> toReturn){
        ATerm nextTerm=null;
        String token = tokenPair.getKey();
        int tokenPosition = tokenPair.getValue();

        //check percentage
        if(isPercentage(token)){
            nextTerm = getPercentageTerm(token);
            toReturn.add(nextTerm);
            return;
        }
        //check currency
        else if(isCurrency(token)){
            nextTerm = getCurrencyTermFromStringAndInteger(token,tokens);
            toReturn.add(nextTerm);
            return;
        }
        //check month
        else if(getMonthWords().contains(token)){
            String nextToken = tokens.isEmpty() ? null : tokens.get(0).getKey();
            if(nextToken!=null && isNumber(nextToken) && isInteger(nextToken)){
                int day = Integer.parseInt(nextToken);
                if(day>0 && day<lastDayInMonth.get(months.get(token))){
                    nextTerm = new DateTerm(months.get(token),day);
                    toReturn.add(nextTerm);
                    tokens.remove(0);
                    return;
                }
            }
        }
        //check hyphenated word
        else if(isHyphenatedWord(token)){
            toReturn.addAll(getHyphenatedTokensFromStringAndInteger(tokenPair,tokens));
            return;
        }
        else{
            //check cityTerm
            nextTerm = getCityTerm(tokens,tokenPosition,token,5);
            if(nextTerm!=null){
                toReturn.add(nextTerm);
                return;
            }
        }
        boolean isNumber = false;
        boolean isFraction = false;
        //check number with value
        if(isNumberWithValue(token)){
            isNumber =true;
            nextTerm = splitWord(token);
            //if list is now empty, return, else switch token to next word
            if(tokens.isEmpty()){
                toReturn.add(nextTerm);
                return;
            }
            else{
                token = tokens.get(0).getKey();
            }
        }
        //check fraction
        if(isFraction(token)){
            isFraction = true;
            nextTerm = isNumber ? new CompoundFractionTerm((NumberTerm) nextTerm, getFractionTerm(token)) : getFractionTerm(token);
            if(isNumber)
                tokens.remove(0);
            isNumber = true;
            //if list is now empty, return, else switch token to next word
            if(tokens.isEmpty()){
                toReturn.add(nextTerm);
                return;
            }
            else{
                token = tokens.get(0).getKey();
            }
        }
        //check currency
        if(isNumber && getCurrencyStrings().contains(token)){
            nextTerm = isFraction ? new CompoundFractionCurrencyTerm((CompoundFractionTerm) nextTerm,token) : new CurrencyTerm((NumberTerm) nextTerm,token);
            tokens.remove(0);
            toReturn.add(nextTerm);
            return;
        }

        //split word by non numbers and letter
        toReturn.addAll(getFinalWorTermListFromStringAntInteger(tokenPair,tokens));
        return;
    }

    private ATerm getCityTerm(List<Pair<String, Integer>> tokens,int tokenPosition, String token, int maxIterations) {
        List<Country> countries;
        int iterations = 0;
        String cityName = token;
        //get city term
        //if exists, break else return null
        while (true){
            countries = countryService.getByCapital(cityName);
            if(countries!=null && !countries.isEmpty()){
                CityTerm cityTerm = new CityTerm(cityName,tokenPosition);
                for (int i = 0; i < iterations; i++) {
                    tokens.remove(i);
                }
                return cityTerm;
            }
            iterations++;
            if(iterations>maxIterations || tokens.size()<(iterations+1))
                return null;
            cityName+=" "+tokens.get(iterations).getKey();
        }
        return null;
    }


    private boolean isCurrencyToken(String s) {
        if(s!=null && s.length()==1 && getCurrencySymbols().contains(s.charAt(0)))
            return true;
        return false;
    }

    private List<ATerm> getFinalWorTermList(String s, List<String> tokens){
        return getFinalWorTermList(s,tokens,new ArrayList<>(0));
    }
    private List<ATerm> getFinalWorTermList(String s, List<String> tokens, Collection<Character> delimitersToIgnore) {
        List<ATerm> toReturn = new ArrayList<>();
        //get all desired substrings
        //split the word into parts
        List<Pair<Integer,Integer>> desiredSubstrings = new ArrayList<>();
        for (int i = 0, firstDesiredIndex=0; i < s.length(); i++) {
            if(!Character.isLetter(s.charAt(i)) && !Character.isDigit(s.charAt(i)) && !delimitersToIgnore.contains(s.charAt(i))){
                int newFirstDesiredIndex;
                if(currencySymbols.contains(s.charAt(i))) {
                    if(i==firstDesiredIndex)
                        continue;
                    newFirstDesiredIndex = i;
                }
                else
                    newFirstDesiredIndex=i+1;

                if(i==s.length()-1){
                    if(i>firstDesiredIndex)
                        desiredSubstrings.add(new Pair<>(firstDesiredIndex,i));
                }
                else
                    desiredSubstrings.add(new Pair<>(firstDesiredIndex,i));
                firstDesiredIndex = newFirstDesiredIndex;
            }
            else if(i==s.length()-1)
                desiredSubstrings.add(new Pair<>(firstDesiredIndex,i+1));
        }
        //check if only one string
        if(desiredSubstrings.isEmpty())
            return toReturn;
        else if(desiredSubstrings.size()==1){
            WordTerm term = createWordTerm(s.substring(desiredSubstrings.get(0).getKey(),desiredSubstrings.get(0).getValue()));
            if(term!=null)
                toReturn.add(term);
            return toReturn;
        }
        List<String> tokensToAdd = new ArrayList<>();

        for (Pair<Integer,Integer> substring:desiredSubstrings) {
            String token = s.substring(substring.getKey(),substring.getValue());
            token = removeUnnecessaryChars(token);
            if(token!= null && token.length()>0)
                tokensToAdd.add(token);
        }
        //prepend desired tokens to list
        tokens.addAll(0,tokensToAdd);
        return toReturn;
    }

    private List<ATerm> getFinalWorTermListFromStringAntInteger(Pair<String,Integer> s, List<Pair<String,Integer>> tokens){
        return getFinalWorTermListFromStringAntInteger(s,tokens,new ArrayList<>(0));
    }

    private List<ATerm> getFinalWorTermListFromStringAntInteger(Pair<String,Integer> wordAndLocation, List<Pair<String,Integer>> tokens, Collection<Character> delimitersToIgnore) {
        List<ATerm> toReturn = new ArrayList<>();
        String s = wordAndLocation.getKey();
        int position = wordAndLocation.getValue();
        //get all desired substrings
        //split the word into parts
        List<Pair<Integer,Integer>> desiredSubstrings = new ArrayList<>();
        for (int i = 0, firstDesiredIndex=0; i < s.length(); i++) {
            if(!Character.isLetter(s.charAt(i)) && !Character.isDigit(s.charAt(i)) && !delimitersToIgnore.contains(s.charAt(i))){
                int newFirstDesiredIndex;
                if(currencySymbols.contains(s.charAt(i))) {
                    if(i==firstDesiredIndex)
                        continue;
                    newFirstDesiredIndex = i;
                }
                else
                    newFirstDesiredIndex=i+1;

                if(i==s.length()-1){
                    if(i>firstDesiredIndex)
                        desiredSubstrings.add(new Pair<>(firstDesiredIndex,i));
                }
                else
                    desiredSubstrings.add(new Pair<>(firstDesiredIndex,i));
                firstDesiredIndex = newFirstDesiredIndex;
            }
            else if(i==s.length()-1)
                desiredSubstrings.add(new Pair<>(firstDesiredIndex,i+1));
        }
        //check if only one string
        if(desiredSubstrings.isEmpty())
            return toReturn;
        else if(desiredSubstrings.size()==1){
            WordTerm term = createWordTerm(s.substring(desiredSubstrings.get(0).getKey(),desiredSubstrings.get(0).getValue()));
            if(term!=null)
                toReturn.add(term);
            return toReturn;
        }
        List<Pair<String,Integer>> tokensToAdd = new ArrayList<>();

        for (Pair<Integer,Integer> substring:desiredSubstrings) {
            String token = s.substring(substring.getKey(),substring.getValue());
            token = removeUnnecessaryChars(token);
            if(token!= null && token.length()>0)
                tokensToAdd.add(new Pair<>(token,position));
        }
        //prepend desired tokens to list
        tokens.addAll(0,tokensToAdd);
        return toReturn;
    }

    protected WordTerm createWordTerm(String s) {
        mutex.lock();
        boolean isStopWord = stopWords.contains(s.toLowerCase());
        mutex.unlock();
        if(!isStopWord)
            return new WordTerm(s);
        return null;
    }

    private boolean isStopWord(String token) {
        String lowerCaseToken = token.toLowerCase();
        return stopWords.contains(lowerCaseToken);
    }

    private List<ATerm> getNextTerm(List<String> tokens){
        List<ATerm> toReturn = new ArrayList<>();
        ATerm nextTerm = null;
        //if list is empty, no tokens
        if(tokens.size() == 0)
            return null;
        String token = tokens.get(0);
        tokens.remove(0);
        //if is number
        if(isNumber( token)) {
            nextTerm = new NumberTerm(token);
            AddNextNumberTerm(tokens,nextTerm,toReturn);
        }
        //word
        else {
            addWordTerm(tokens,token,toReturn);
        }
        return toReturn;
    }


    private List<ATerm> getNextTermFromStringAndInteger(List<Pair<String,Integer>> tokens){
        List<ATerm> toReturn = new ArrayList<>();
        ATerm nextTerm = null;
        //if list is empty, no tokens
        if(tokens.size() == 0)
            return null;
        Pair<String,Integer> tokenPair = tokens.get(0);
        String token = tokenPair.getKey();
        int tokenPosition = tokenPair.getValue();
        tokens.remove(0);
        //if is number
        if(isNumber(token)) {
            nextTerm = new NumberTerm(token);
            AddNextNumberTermFromStringAndInteger(tokens,nextTerm,toReturn);
        }
        //word
        else {
            addWordTermFromStringAndInteger(tokens,tokenPair,toReturn);
        }
        return toReturn;
    }

    private List<ATerm> getHyphenatedTokens(String token, List<String> tokens) {
        List<ATerm> hyphenatedToken = getFinalWorTermList(token,tokens,delimitersToSplitWordBy);
        if(!(hyphenatedToken==null || hyphenatedToken.isEmpty())){
            hyphenatedToken.addAll(getFinalWorTermList(token,tokens));
        }
        return hyphenatedToken;
    }

    private List<ATerm> getHyphenatedTokensFromStringAndInteger(Pair<String,Integer> token, List<Pair<String,Integer>> tokens) {
        List<ATerm> hyphenatedToken = getFinalWorTermListFromStringAntInteger(token,tokens,delimitersToSplitWordBy);
        if(!(hyphenatedToken==null || hyphenatedToken.isEmpty())){
            hyphenatedToken.addAll(getFinalWorTermListFromStringAntInteger(token,tokens));
        }
        return hyphenatedToken;
    }

    private boolean isHyphenatedWord(String token) {
        for (char delimiter:delimitersToSplitWordBy) {
            if(token.contains(""+delimiter)){
                String[] split = token.split(""+delimiter);
                return (split!=null && split.length>1 && split[0].length()>0 && split[1].length()>0);
            }

        }
        return false;
    }

    private boolean isPercentage(String token) {
        return (token.length()>1 && token.charAt(token.length()-1)=='%' && isNumber(token.substring(0,token.length()-1)));
    }

    private boolean isInteger(CharSequence s){
        String string;
        if(s instanceof String)
            string = (String)s;
        else{
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                sb.append(s.charAt(i));
            }
            string = sb.toString();
        }
        try {
            Integer.parseInt(string);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    private boolean isNumberWithValue(String s){
        //get last index of number
        boolean number = true;
        int pointer = 0;
        while (pointer<s.length() && number){
            number = Character.isDigit(s.charAt(pointer)) || s.charAt(pointer)=='.';
            if(number)
                pointer++;
        }
        //check if is number and word after it represents value
        if(pointer>0 && pointer<s.length()){
            String numString = s.substring(0,pointer+1);
            String word = s.substring(pointer+1);
            if(isNumber(numString) && getValueKeywords().contains(word))
                return true;
            return false;
        }
        return false;
    }

    private NumberTerm splitWord(String s){
        boolean number = true;
        int pointer = 0;
        while (pointer<s.length() && number){
            number = Character.isDigit(s.charAt(pointer)) || s.charAt(pointer)=='.';
            if(number)
                pointer++;
        }
        String numString = s.substring(0,pointer+1);
        String word = s.substring(pointer+1);

        NumberTerm toReturn = new NumberTerm(numString);
        toReturn.multiply(valuesAfterNumber.get(word));
        return toReturn;
    }

    private PercentageTerm getPercentageTerm(String s){
        if(isPercentage(s)){
            NumberTerm term = new NumberTerm(s.substring(0,s.length()-1));
            return new PercentageTerm(term);
        }
        else if(isNumber(s)){
            NumberTerm term = new NumberTerm(s);
            return new PercentageTerm(term);
        }
        return null;
    }

    private CurrencyTerm getCurrencyTerm(String s,List<String> tokens){
        Value val = null;
        if(!tokens.isEmpty() && getValueKeywords().contains(tokens.get(0)))
            val = valuesAfterNumber.get(tokens.remove(0));
        if(isCurrency(s)){
            String currencySymbol = s.substring(0,1);
            String currency = currencyTypes.get(currencySymbol);
            NumberTerm term = new NumberTerm(s.substring(1));
            if(val!=null)
                term.multiply(val);
            return new CurrencyTerm(term,currency);
        }
        return null;
    }

    private CurrencyTerm getCurrencyTermFromStringAndInteger(String s,List<Pair<String,Integer>> tokens){
        Value val = null;
        if(!tokens.isEmpty() && getValueKeywords().contains(tokens.get(0).getKey()))
            val = valuesAfterNumber.get(tokens.remove(0).getKey());
        if(isCurrency(s)){
            String currencySymbol = s.substring(0,1);
            String currency = currencyTypes.get(currencySymbol);
            NumberTerm term = new NumberTerm(s.substring(1));
            if(val!=null)
                term.multiply(val);
            return new CurrencyTerm(term,currency);
        }
        return null;
    }
    private static Pair<String, Integer> getNextRelevantTerm(List<String> tokens, ParsingHashMap toGetFrom){
        Pair<String,Integer> toReturn = null;
        String toCheck = "";
        Collection<String> keys = toGetFrom.keySet();
        for (int i = 0; i < toGetFrom.getWordsInLongestKey() && i<tokens.size(); i++) {
            String toAdd = tokens.get(i);
            if(i!=0)
                toCheck+=(" "+toAdd);
            else
                toCheck+=toAdd;
            if(keys.contains(toCheck)) {
                toReturn = new Pair<>(toCheck, i);
                break;
            }
        }
        return toReturn;
    }

    private static Pair<String, Integer> getNextRelevantTermFromStringAndInteger(List<Pair<String,Integer>> tokens, ParsingHashMap toGetFrom){
        Pair<String,Integer> toReturn = null;
        String toCheck = "";
        Collection<String> keys = toGetFrom.keySet();
        for (int i = 0; i < toGetFrom.getWordsInLongestKey() && i<tokens.size(); i++) {
            String toAdd = tokens.get(i).getKey();
            if(i!=0)
                toCheck+=(" "+toAdd);
            else
                toCheck+=toAdd;
            if(keys.contains(toCheck)) {
                toReturn = new Pair<>(toCheck, i);
                break;
            }
        }
        return toReturn;
    }*/

    /**
     * list of words and the value they represent
     * @param values
     */
    /*public void setValuesAfter(Map<String,Value> values){
        this.valuesAfterNumber = values;
    }*/

    /**
     * word will no longer be considered a value
     * @param valueToRemove
     */
    /*public void removeValue(String valueToRemove){
        valuesAfterNumber.remove(valueToRemove);
    }*/
    /**
     * add word that represents a value
     * i.g (thousand, THOUSAND)
     * @param word
     * @param value
     */
    /*public void addValueAfter(String word, Value value){
        this.valuesAfterNumber.put(word,value);
    }*/

    /**
     * set currency words. for example <U.S Dollar, Dollar>
     * @param currencyTypes, Map with of word as key, and the represented currency as value
     */
   /* public void setCurrencyTypes(Map<String,String> currencyTypes){
        if(currencyTypes instanceof ParsingHashMap){
            this.currencyTypes = (ParsingHashMap) currencyTypes;
        }
        else
            this.currencyTypes = new ParsingHashMap(currencyTypes);
    }*/


    /**
     * words that represent a percent
     * @param percentWords
     */
   /* public void setPercentWords(Collection<String> percentWords){
        this.percentWords = percentWords;
    }*/

    /**
     * chars not to ignore at the beginning and end of words, all other chars will be ignored
     * @param necessaryChars
     */
    /*public void setNecessaryChars(Collection<Character> necessaryChars){
        this.necessaryChars = necessaryChars;
    }*/

    /**
     * sets words that represent years, and the year they represent
     * @param years
     */
    /*public void setYears(Map<String,String> years){
        if (years instanceof ParsingHashMap)
            this.years = (ParsingHashMap) years;
        else
            this.years = new ParsingHashMap(years);
    }*/

  /*  public void setStopWords(Collection<String> stopWords){
        this.stopWords = stopWords;
    }*/




}
