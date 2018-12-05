package SearchEngineTools.ParsingTools;

import SearchEngineTools.ParsingTools.Term.CityTerm;
import eu.fayder.restcountries.v1.domain.Country;
import eu.fayder.restcountries.v1.rest.CountryService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DocumentTokenList implements ITokenList {

    private Token next;
    private int wordCount;
    protected String currentLine;
    protected List<String> documentLines;
    private List<Token> prepended;
    private List<Token> appended;
    private boolean isText;
    private CityTerm cityTerm;
    private CountryService countryService = CountryService.getInstance();

    Collection<Character> delimitersToSplitWordBy;

    Collection<Character> currencySymbols;

    /**
     * Constructer for the Document TokenList Class
     */
    public DocumentTokenList(){
        appended = new ArrayList<>();
        prepended = new ArrayList<>();
    }

    @Override
    public Token peek() {
        if(isEmpty())
            throw new NullPointerException();
        return next;
    }

    @Override
    public Token pop() {
        if(isEmpty())
            throw new NullPointerException();
        Token token = next;
        setNext();
        return token;
    }

    /**
     * set the next term
     */
    private void setNext(){
        if(!prepended.isEmpty()){
            next = prepended.remove(0);
        }
        else {
            next = getNextToken();
            if(next == null){
                next = appended.isEmpty() ? null : appended.remove(0);
            }
        }

    }

    /**
     * get the next token from the current document
     * @return
     */
    private Token getNextToken(){
        Token token=null;
        //get next line
        if(currentLine == null){
            currentLine = getNextTextLine();
            if(currentLine==null)
                return null;
        }
        while (currentLine!=null && token==null){
            token = getNextTokenFromCurrentLine();
        }
        return token;
    }

    /**
     * Gets the next token from current line
     * @return
     */
    private Token getNextTokenFromCurrentLine() {
        int indexOfFirstSpace = currentLine.indexOf(' ');
        String tokenString;
        //no space
        if(indexOfFirstSpace==-1){
            tokenString = removeUnnecessaryChars(currentLine);
            currentLine = getNextTextLine();
        }
        else {
            tokenString = removeUnnecessaryChars(currentLine.substring(0,indexOfFirstSpace));
            currentLine = indexOfFirstSpace>=currentLine.length() ? getNextTextLine() : currentLine.substring(indexOfFirstSpace+1);
        }
        Token toReturn = null;
        if(tokenString!=null){
            toReturn = new Token(tokenString,wordCount);
            wordCount++;
        }
        return toReturn;
    }

    /**
     * gets the next text line, sets cityTerm
     * @return
     */
    protected String getNextTextLine() {

        if(isText){
            currentLine = documentLines.remove(0);
            if(currentLine.equals("</TEXT>")){
                isText = false;
                String nextLine = getNextTextLine();
                return nextLine;
            }
            else
                return currentLine;
        }
        else {
            while (!documentLines.isEmpty() && !isText){
                currentLine = documentLines.remove(0);
                if(currentLine.contains("<F P=104>")) {
                    extractCityTerm(currentLine);
                    continue;
                }
                if(currentLine.equals("<TEXT>")) {
                    isText = true;
                    return getNextTextLine();
                }
            }
        }
        return null;
    }

    /**
     * extracts cityTerm frim line with appropriate tag
     * @param currentLine
     */
    private void extractCityTerm(String currentLine) {
        String lineWithoutTag = currentLine.length()>=10 ? currentLine.substring(9) : null;
        if(lineWithoutTag==null)
            return;
        String[] splitLineWithoutTag = lineWithoutTag.split(" ");
        String cityName="";
        boolean foundCity = false;
        String lastCityName = null;
        Country lastCountry = null;
        int usedAPI = 0;
        for (int i = 0; i < splitLineWithoutTag.length & usedAPI<3; i++) {
            String currentIndexString = splitLineWithoutTag[i];
            if(currentIndexString!=null && currentIndexString.length()>0){
                cityName += currentIndexString;
                List<Country> country = countryService.getByCapital(cityName);
                usedAPI++;
                if(country!=null && !country.isEmpty()) {
                    lastCityName=cityName;
                    lastCountry=country.get(0);
                    foundCity=true;
                    cityName+=" ";
                }
                else {
                    cityName+=" ";
                }
            }
        }
        if(foundCity){
            cityTerm = new CityTerm(lastCityName,lastCountry);
        }
    }

    @Override
    public void prepend(List<Token> tokens) {
        validateTokensList(tokens);
        prependValidTokens(tokens);
    }

    public void prependValidTokens(List<Token> tokens){
        if(next!=null)
            tokens.add(next);
        next = !tokens.isEmpty() ? tokens.remove(0) : next;
        prepended.addAll(0,tokens);
    }

    @Override
    public void append(List<Token> tokens) {
        validateTokensList(tokens);
        appended.addAll(tokens);
    }

    /**
     * checks that all Tokens in list are valid, removes invalid Tokens
     * @param tokens
     */
    private void validateTokensList(List<Token> tokens){
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            String tokenString = token.getTokenString();
            tokenString = removeUnnecessaryChars(tokenString);
            if(tokenString==null || tokenString.length()==0){
                tokens.remove(i);
                i--;
            }
            else {
                token.setTokenString(tokenString);
            }
        }
    }

    @Override
    public void clear() {
        this.prepended.clear();
        this.appended.clear();
        this.next=null;
        this.currentLine=null;
        this.wordCount=0;
        this.isText=false;
        this.documentLines=null;
        this.cityTerm=null;
    }

    @Override
    public boolean isEmpty() {
        return next==null;
    }

    @Override
    public Token get(int index) {
        if(index==0) {
            try {
                return peek();
            }
            catch (Exception e){
                return null;
            }
        }
        int amountOfTokensToAddToPrepended = index-prepended.size();
        List<Token> toPrepend = new ArrayList<>(amountOfTokensToAddToPrepended);
        while (amountOfTokensToAddToPrepended > 0){
            Token token = pop();
            toPrepend.add(token);
        }
        prepended.addAll(toPrepend);
        return prepended.get(index-1);
    }

    @Override
    public boolean has(int index) {
        return get(index)!=null;
    }

    @Override
    public void initialize(List<String> documentLines, Collection<Character> currencySymbols, Collection<Character> delimitersToSplitWordBy) {
        this.documentLines=documentLines;
        this.currencySymbols=currencySymbols;
        this.delimitersToSplitWordBy=delimitersToSplitWordBy;
        isText=false;
        setNext();
    }

    /**
     * removes unnecessary characters from beggining and end of string
     * @param sToken
     * @return
     */
    protected String removeUnnecessaryChars(String sToken) {
        if(sToken==null || sToken.equals(""))
            return null;
        int firstNecessary = 0;
        int lastNecessary = sToken.length()-1;
        //find first necessary index
        boolean foundFirstIndex = (Character.isDigit(sToken.charAt(firstNecessary)) || Character.isLetter(sToken.charAt(firstNecessary))
                || (sToken.length()>1 && currencySymbols.contains(sToken.charAt(firstNecessary)) && Character.isDigit(sToken.charAt(firstNecessary+1)))
                || (sToken.length()==1 && '%'==sToken.charAt(0)));
        while (!foundFirstIndex && firstNecessary<sToken.length()){
            foundFirstIndex = (Character.isDigit(sToken.charAt(firstNecessary)) || Character.isLetter(sToken.charAt(firstNecessary)))||
                    (firstNecessary>sToken.length()-1 && delimitersToSplitWordBy.contains(sToken.charAt(firstNecessary)) && Character.isDigit(sToken.charAt(firstNecessary+1)));
            if(!foundFirstIndex)
                firstNecessary++;
        }
        if(firstNecessary>lastNecessary)
            return null;
        while (lastNecessary>0 &&
                !(Character.isDigit(sToken.charAt(lastNecessary-1))&& sToken.charAt(lastNecessary)=='%')&&
                !(Character.isDigit(sToken.charAt(lastNecessary)) ||//first digit is not digit
                Character.isLetter(sToken.charAt(lastNecessary)) ||//first digit is not letter
                currencySymbols.contains(""+sToken.charAt(lastNecessary)))){ //first digit is not currency
            lastNecessary--;
        }
        if(firstNecessary>lastNecessary)
            return null;
        if(firstNecessary!=0 || lastNecessary!=sToken.length()-1)
            sToken = sToken.substring(firstNecessary,lastNecessary+1);
        if(sToken.length()>=2 && sToken.substring(sToken.length()-2,sToken.length()).equals("'s"))
            sToken = sToken.substring(0,sToken.length()-2);
        return sToken.length()>0 ? sToken : null;
    }

    public CityTerm getCityTerm() {
        return cityTerm;
    }
}
