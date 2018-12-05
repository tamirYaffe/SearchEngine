package SearchEngineTools.ParsingTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ATokenList implements ITokenList {

    protected List<Token> appended;
    protected List<Token> prepended;
    protected Collection<Character> currencySymbols;
    protected Collection<Character> delimitersToSplitWordBy;
    protected Token next;

    public ATokenList(){
        initializeAllFields();
    }

    protected void initializeAllFields(){
        prepended = new ArrayList<>();
        appended = new ArrayList<>();
        next = null;
    }

    @Override
    public Token peek() {
        return next;
    }

    protected abstract void createNextToken();

    @Override
    public Token pop() {
        if(next==null)
            createNextToken();
        Token token = next;
        createNextToken();
        return token;
    }

    @Override
    public void prepend(List<Token> tokens){
        for (Token t:tokens) {
            removeUnnecessaryChars(t);
            String tokenString = t.getTokenString();
            if(tokenString==null || tokenString.length()==0)
                tokens.remove(t);
        }
        prependValidTokens(tokens);
    }


    @Override
    public void append(List<Token> tokens) {
        for (Token t:tokens) {
            removeUnnecessaryChars(t);
        }
        appended.addAll(tokens);
    }

    @Override
    public abstract void clear();

    @Override
    public abstract boolean isEmpty();

    @Override
    public Token get(int index) {
        if(index<0)
            throw  new IndexOutOfBoundsException();
        else if(index==0)
            return next;
        else {
            List<Token> toPrepend = new ArrayList<>(index+1);
            for (int i = 0; i <= index; i++) {
                Token token = pop();
                if(token!=null)
                    toPrepend.add(token);
                else
                    break;
            }
            Token toReturn = toPrepend.size()>=index+1 ? toPrepend.get(index-1) : null;
            prependValidTokens(toPrepend);
            return toReturn;
        }
    }

   /* private void prependValidTokens(List<Token> validTokens){
        prepended.addAll(0, validTokens);
        if(next!=null && !prepended.contains(next)){
            prepended.add(next);
            next=prepended.remove(0);
        }
    }*/

    @Override
    public void prependValidTokens(List<Token> t){

    }
    @Override
    public boolean has(int index) {
        return get(index)!=null;
    }

    public abstract void initialize(List<String> documentLines, Collection<Character> currencySymbols, Collection<Character> delimitersToSplitWordBy);

    protected void removeUnnecessaryChars(Token t){
        t.setTokenString(removeUnnecessaryChars(t.getTokenString()));
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
