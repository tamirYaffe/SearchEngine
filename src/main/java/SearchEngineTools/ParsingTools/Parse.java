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

    private DocumentTokenList tokenList;


    /**
     * Initializes parser
     * @param stopWords - words to ignore
     */
    public Parse(Collection<String> stopWords){
        initializeDataStructures();
        this.stopWords = stopWords;
        this.tokenList = new DocumentTokenList();
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
            this.months.put(months[i].toUpperCase(),i+1);
        }
    }

    private void initializeCurrencySymbols(){
        this.currencySymbols = new HashSet<>();
        currencySymbols.add('$');
    }



    /////////////////////////////////////////////

    /*private class OccurrencesListPair{

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
    }*/

    public Collection<ATerm> parseDocument(List<String> document){
        if(tokenList==null)
            tokenList = new DocumentTokenList();
        tokenList.initialize(document,currencySymbols,delimitersToSplitWordBy);
        List<String> cityNames = new ArrayList<>();
        /*CityTerm documentCity = tokenList.getCityTerm();
        if(documentCity!=null)
            cityNames.add(documentCity.getTerm());*/
        setCityNames(cityNames);
        return parse(tokenList);

    }

    public Collection<ATerm> parseText(List<String> text){
        ITokenList tokenList = tokenizeText(text);
        return parse(tokenList);
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
    public Collection<ATerm> parse(ITokenList tokenList){
        //Map<ATerm,OccurrencesListPair> occurrencesOfTerms = new HashMap<>();
        Map<String,ATerm> occurrencesAndPositionsOfTerms = new HashMap<>();
        addAllTermsToOccurrancesOfTerms(occurrencesAndPositionsOfTerms,tokenList);
        Collection<ATerm> toReturn = getFinalTermCollection(occurrencesAndPositionsOfTerms);
        return toReturn;
    }

    private Collection<ATerm> getFinalTermCollection(Map<String, ATerm> occurrencesOfTerms) {
        ArrayList<ATerm> toReturn = new ArrayList<>(occurrencesOfTerms.size());
        for (String termString:occurrencesOfTerms.keySet()) {
            ATerm term = occurrencesOfTerms.get(termString);
            toReturn.add(term);
        }
        return toReturn;
        /*for (ATerm term:occurrencesOfTerms.keySet()) {
            term = occurrencesOfTerms.get(term).getTerm();
            term.setOccurrences(occurrencesOfTerms.get(term).occurrences);
        }
        return occurrencesOfTerms.keySet();*/
    }

    private void addAllTermsToOccurrancesOfTerms(Map<String, ATerm> occurrencesOfTerms, ITokenList tokenList) {
        while (!tokenList.isEmpty()){
            getNextTerm(occurrencesOfTerms,tokenList);
        }
    }

    private void getNextTerm(Map<String,ATerm> occurrencesOfTerms,ITokenList tokenList){
        ATerm nextTerm = null;
        //if list is empty, no tokens
        if(tokenList.isEmpty())
            return;
        Token token = tokenList.pop();
        if(token==null)
            return;
        String tokenString = token.getTokenString();
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

    private void AddNextNumberTerm(ITokenList tokenList, ATerm nextTerm, Map<String,ATerm> occurrencesOfTerms){
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
        tokens.prependValidTokens(toPrepend);
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

    private void addTermToOccurrencesList(ATerm term, Map<String, ATerm> occurrencesList){
        if(term instanceof WordTerm){
            addWordTermToOccurrencesList((WordTerm) term,occurrencesList,Character.isLowerCase(term.getTerm().charAt(0)));
        }
        else {
            addTermToOccurrencesList(term, occurrencesList, true);
        }
    }

    private void addTermToOccurrencesList(ATerm term,Map<String,ATerm> occurrencesList,boolean noSpecialCase){
        if(term instanceof WordTerm && !noSpecialCase)
            addTermToOccurrencesList(term,occurrencesList);
        else{
            String termString = term.getTerm();
            if(occurrencesList.keySet().contains(termString)){
                ATerm termInList = occurrencesList.get(termString);
                termInList.incrementOccurrences();
                termInList.addPositions(term);
            }
            else {
                term.setOccurrences(1);
                occurrencesList.put(termString,term);
            }
        }
    }

    private void addWordTermToOccurrencesList(WordTerm term, Map<String,ATerm> occurrencesOfTerms, boolean isLowerCase){
        if(term instanceof CityTerm){
            addTermToOccurrencesList(term,occurrencesOfTerms,true);
            return;
        }
        String termString = term.getTerm();
        String upperCaseTermString = termString.toUpperCase();
        String lowerCaseTermString = termString.toLowerCase();
        boolean existsLowercase = occurrencesOfTerms.containsKey(lowerCaseTermString);
        boolean existsUppercase = occurrencesOfTerms.containsKey(upperCaseTermString);


        if(isLowerCase && existsUppercase){
            WordTerm old = (WordTerm) occurrencesOfTerms.get(upperCaseTermString);
            old.toLowerCase();
            old.incrementOccurrences();
            occurrencesOfTerms.remove(upperCaseTermString);
            occurrencesOfTerms.put(lowerCaseTermString,old);
        }
        else if(isLowerCase || existsLowercase){
            term.toLowerCase();
            addTermToOccurrencesList(term,occurrencesOfTerms,true);
        }
        else {
            term.toUperCase();
            addTermToOccurrencesList(term, occurrencesOfTerms, true);
        }
    }

    /*private void addCityTermToOccurrencesList(CityTerm term, Map<String, ATerm> occurrencesOfTerms, boolean isLowerCase) {
        if (occurrencesOfTerms.keySet().contains(term)){
            OccurrencesListPair occurrencesListPair = occurrencesOfTerms.get(term);
            CityTerm cityTerm = (CityTerm) occurrencesListPair.getTerm();
            int occurrencesToAdd = term.getOccurrences()+1;
            occurrencesListPair.addOccurrences(occurrencesToAdd);
        }
        else
            occurrencesOfTerms.put(term,new OccurrencesListPair(term,1));
    }*/

    private void addWordTerm(ITokenList tokens, Token token, Map<String, ATerm> occurrencesOfTerms){
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
    protected List<ATerm> getFinalWordTermList(Token token, ITokenList tokens, Collection<Character> delimitersToIgnore) {
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
    protected List<ATerm> getHyphenatedTokens(Token token, ITokenList tokens) {
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
}
