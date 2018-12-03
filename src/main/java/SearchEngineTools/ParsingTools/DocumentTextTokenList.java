package SearchEngineTools.ParsingTools;

import SearchEngineTools.ParsingTools.Term.CityTerm;
import eu.fayder.restcountries.v1.rest.CountryService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DocumentTextTokenList extends ATokenList {

    private String[] currentSplitLine;
    private int currentLineWordCount=0;
    private int currentSplitLineIndex;
    private List<String> documentLines;
    private CountryService countryService = CountryService.getInstance();
    private CityTerm cityTerm;
    boolean isText = false;

    /**
     *
     */
    public DocumentTextTokenList(){
        prepended = new ArrayList<>();
        appended = new ArrayList<>();
        next = null;
    }

    @Override
    protected void initializeAllFields() {
        prepended = new ArrayList<>();
        appended = new ArrayList<>();
        next = null;
    }

    @Override
    protected void createNextToken() {
        boolean createdNext = false;
        next=null;
        while (!isEmpty() && !createdNext){
            getNextCandidateToken();
            if(next == null)
                continue;
            else {
                String nextString = next.getTokenString();
                nextString = removeUnnecessaryChars(nextString);
                if(nextString==null || nextString.length()==0)
                    continue;
                else {
                    next.setTokenString(nextString);
                    createdNext=true;
                    return;
                }
            }
        }
        next=null;
    }

    private void findNextRelevantLine() {
        String line;
        if(isText){
            currentLineWordCount += currentSplitLine==null ? 0 : currentSplitLine.length;
            line = documentLines.remove(0);
            if(line.equals("</TEXT>")){
                isText=false;
                currentSplitLine=null;
                currentLineWordCount+=1;
                findNextRelevantLine();
                return;
            }
            else {
                currentSplitLine = line.split(" ");
                currentSplitLineIndex=0;
                return;
            }
        }
        else {
            while (!documentLines.isEmpty() && !isText){
                line = documentLines.remove(0);
                if(line.contains("<F P=104>"))
                    extractCityTerm(line,currentLineWordCount);
                if(line.equals("<TEXT>")) {
                    isText = true;
                    currentLineWordCount += 1;
                    currentSplitLine = null;
                    findNextRelevantLine();
                    return;
                }
                else if(line!=null) {
                    currentLineWordCount+=line.split(" ").length;
                }
            }
        }
    }

    private void extractCityTerm(String line, int lineWordCount) {
        String[] split = line.split(" ");
        int cityindex=-1;
        for (int i = 0; i < split.length; i++) {
            if(split[i]!=null && split[i].equals("P=104>")){
                cityindex=i+1;
                break;
            }
        }
        if(cityindex!=-1) {
            StringBuilder cityName = new StringBuilder();
            for (int i = 0; cityindex + i < split.length; i++) {
                String currentString = split[cityindex + 1];
                if (currentString != null && currentString.length() > 0)
                    cityName.append(split[cityindex + i]);
                else
                    continue;
                if (countryService.getByCapital(cityName.toString()) != null && !countryService.getByCapital(cityName.toString()).isEmpty()) {
                    cityTerm = new CityTerm(cityName.toString());
                    return;
                } else {
                    cityName.append(" ");
                }
            }
        }
    }

    private void getNextCandidateToken() {
        if(!prepended.isEmpty())
            next = prepended.remove(0);
        else{
            next = removeNextTokenFromDocumentLines();
            if(next==null){
                next = !(appended==null || appended.isEmpty()) ? appended.remove(0) : null;
            }
        }
    }



    private Token removeNextTokenFromDocumentLines() {
        if(currentSplitLine==null)
            return null;
        Token toReturn = new Token(currentSplitLine[currentSplitLineIndex],currentLineWordCount+currentSplitLineIndex);
        currentSplitLineIndex++;
        if(currentSplitLineIndex==currentSplitLine.length)
            findNextRelevantLine();
        return toReturn;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean isEmpty() {
        return next==null && appended.isEmpty() && prepended.isEmpty() && documentLines.isEmpty();
    }

    @Override
    public void initialize(List<String> documentLines, Collection<Character> currencySymbols, Collection<Character> delimitersToSplitWordBy) {
        this.documentLines=documentLines;
        this.currencySymbols=currencySymbols;
        this.delimitersToSplitWordBy=delimitersToSplitWordBy;
        this.isText=false;
        findNextRelevantLine();
    }

    public CityTerm getCityTerm(){
        return cityTerm;
    }
    /**
     * @param documentLines - all lines in document
     * @param currencySymbols - all currency symbols
     * @param delimitersToSplitWordBy
     */
  /*  public void initialize(List<String> documentLines,Collection<Character> currencySymbols,Collection<Character> delimitersToSplitWordBy){
        this.documentLines=documentLines;
        this.currencySymbols = currencySymbols;
        this.delimitersToSplitWordBy = delimitersToSplitWordBy;
        findNextRelevantLine();
    }

    private void findNextRelevantLine() {
        String line;
        if(isText){
            currentLineWordCount += currentSplitLine==null ? 0 : currentSplitLine.length;
            line = documentLines.remove(0);
            if(line.equals("</TEXT>")){
                isText = false;
                currentSplitLine = null;
                currentLineWordCount+=1;
                findNextRelevantLine();
            }
            else {
                currentSplitLine = line.split(" ");
                currentSplitLineIndex = 0;
            }
        }
        else {
            while (!documentLines.isEmpty() && !isText){
                line = documentLines.remove(0);
                if(line.equals("<TEXT>")){
                    isText=true;
                    currentLineWordCount+=1;
                    currentSplitLine=null;
                    findNextRelevantLine();
                }
                else {
                    if(line!= null && line.length()!=0){
                        currentLineWordCount+=(line.split(" ")).length;
                    }
                }
            }
            currentSplitLine=null;
        }
    }

    @Override
    public Token peek() {
        return next;
    }

    protected void createNextToken() {
        if(!prepended.isEmpty())
            next = prepended.remove(0);
        else{
            next = removeNextTokenFromDocumentLines();
            if(next==null){
                next = !(appended==null || appended.isEmpty()) ? appended.remove(0) : null;
            }
        }
    }

    private Token removeNextTokenFromDocumentLines() {
        if(currentSplitLine==null)
            return null;
        Token toReturn = new Token(currentSplitLine[currentSplitLineIndex],currentLineWordCount+currentSplitLineIndex);
        currentSplitLineIndex++;
        if(currentSplitLineIndex==currentSplitLine.length)
            findNextRelevantLine();
        return toReturn;
    }

    @Override
    public Token pop() {
        Token popped = next;
        createNextToken();
        return popped;
    }

    @Override
    public void prepend(List<Token> tokens) {
        for (Token t:tokens) {
            removeUnnecessaryChars(t);
        }
        prepended.addAll(0,tokens);
    }


    @Override
    public void append(List<Token> tokens) {
        for (Token t:tokens) {
            removeUnnecessaryChars(t);
        }
        appended.addAll(tokens);
    }

    @Override
    public void clear() {
        this.prepended.clear();
        this.appended.clear();
        this.next=null;
        this.currentSplitLineIndex=0;
        this.currentLineWordCount=0;
        this.isText=false;
        this.cityTerm=null;
    }

    @Override
    public boolean isEmpty() {
        return next==null;
    }
*/





}
