package SearchEngineTools.ParsingTools;

import SearchEngineTools.ParsingTools.Term.ATerm;
import SearchEngineTools.ParsingTools.Term.WordTerm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ParseThatDoesntBreakUpWords extends Parse {

    protected List<ATerm> getFinalWordTermList(Token token, ITokenList tokens, Collection<Character> delimitersToIgnore) {
        List<ATerm> toReturn = new ArrayList<>();
        WordTerm wordTerm = createWordTerm(token);
        if(wordTerm!=null)
            toReturn.add(wordTerm);
        return toReturn;
    }

    protected List<ATerm> getHyphenatedTokens(Token token, ITokenList tokens) {
        List<ATerm> toReturn = new ArrayList<>(1);
        List<Token> tokensToPrepend = new ArrayList<>(2);
        toReturn.add(createWordTerm(token));
        String tokenString = token.getTokenString();
        int tokenPosition = token.getPosition();
        int firstHyphen = tokenString.indexOf('-');
        do{
            Token t = new Token(tokenString.substring(0,firstHyphen),tokenPosition);
            tokenString = firstHyphen>=tokenString.length()-1 ? null : tokenString.substring(firstHyphen+1);
            tokensToPrepend.add(t);
            firstHyphen = tokenString==null ? -1 : tokenString.indexOf('-');
        }while (firstHyphen!=-1);
        tokensToPrepend.add(new Token(tokenString,tokenPosition));
        tokens.prepend(tokensToPrepend);
        return toReturn;
    }
}
