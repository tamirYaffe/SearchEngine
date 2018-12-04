package SearchEngineTools.ParsingTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DocumentTokenList implements ITokenList {

    private Token next;
    private int wordCount;
    private String currentLine;
    private List<String> documentLines;
    private List<Token> prepended;
    private List<Token> appended;
    private boolean isText;

    Collection<Character> delimitersToSplitWordBy;

    Collection<Character> currencySymbols;

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
        wordCount++;
        return tokenString==null ? null : new Token(tokenString,wordCount-1);
    }

    private String getNextTextLine() {
        if(isText){
            currentLine = documentLines.remove(0);
            if(currentLine.equals("</TEXT>")){
                isText = false;
                wordCount++;
                return getNextTextLine();
            }
            else
                return currentLine;
        }
        else {
            while (!documentLines.isEmpty() && !isText){
                currentLine = documentLines.remove(0);
                if(currentLine.equals("<TEXT>")) {
                    isText = true;
                    wordCount++;
                    return getNextTextLine();
                }
            }
        }
        return null;
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

    protected String removeUnnecessaryChars(String sToken) {
        if(sToken==null || sToken.equals(""))
            return null;
        int firstNecessary = 0;
        int lastNecessary = sToken.length()-1;
        //find first necessary index
        boolean foundFirstIndex = (Character.isDigit(sToken.charAt(firstNecessary)) || Character.isLetter(sToken.charAt(firstNecessary)));
        while (!foundFirstIndex && firstNecessary<sToken.length()){
            foundFirstIndex = (Character.isDigit(sToken.charAt(firstNecessary)) || Character.isLetter(sToken.charAt(firstNecessary)))||
                    (firstNecessary>sToken.length()-1 && delimitersToSplitWordBy.contains(sToken.charAt(firstNecessary)) && Character.isDigit(sToken.charAt(firstNecessary+1)));
            if(!foundFirstIndex)
                firstNecessary++;
        }
        if(firstNecessary>lastNecessary)
            return null;
        while (lastNecessary>0 && !(Character.isDigit(sToken.charAt(lastNecessary)) ||//first digit is not digit
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
}
