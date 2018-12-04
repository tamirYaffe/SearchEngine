package SearchEngineTools.ParsingTools;

import java.util.Collection;
import java.util.List;

public interface ITokenList {

    Token peek();
    Token pop();
    void prepend(List<Token> tokens);
    void append(List<Token> tokens);
    void clear();
    boolean isEmpty();
    Token get(int index);
    boolean has(int index);
    void initialize(List<String> documentLines, Collection<Character> currencySymbols, Collection<Character> delimitersToSplitWordBy);

    void prependValidTokens(List<Token> toPrepend);
}
