package SearchEngineTools.ParsingTools;

import SearchEngineTools.ParsingTools.Term.ATerm;
import SearchEngineTools.ParsingTools.Term.WordTerm;

import java.util.*;

public class ParseWithStemming extends Parse {

    private Stemmer stemmer;

    public ParseWithStemming(){
        super();
        stemmer = new Stemmer();
    }

    public ParseWithStemming(List<String> stopWords){
        super(stopWords);
        stemmer = new Stemmer();
    }

    @Override
    protected WordTerm createWordTerm(String s) {
        if(stopWords.contains(s.toLowerCase()))
            return null;
        boolean isUpperCase = false;
        for (int i = 0; i < s.length(); i++) {
            if(Character.isLetter(s.charAt(i)) && Character.isUpperCase(s.charAt(i))){
                isUpperCase = true;
                break;
            }
        }
        s = s.toLowerCase();
        s = stemmer.stem(s);
        if(isUpperCase)
            s = s.toUpperCase();
        return new WordTerm(s);
    }

    /*public Collection<ATerm> parseDocument(List<String> document){
        Map<ATerm,Integer> occurrencesOfTerms = new HashMap<>();
        List<ATerm> terms=new ArrayList<>();
        List<String> tokens=tokenize(document);
        //do text operations(remove unnecessary chars, change words by the assignment rules).
        //removeUnnecessaryChars(tokens);
        //get terms
        List<ATerm> next = null;
        do{
            next = getNextTerm(tokens);
            if(next!=null) {
                //allow stemming.
                handleAll(next, occurrencesOfTerms);
            }
        }while (next != null);

        return getFinalList(occurrencesOfTerms);
    }

    private void handleAll(List<ATerm> toHandle, Map<ATerm,Integer> occurances) {
        for (ATerm term:toHandle) {
            if (term instanceof WordTerm){
                //remove stop words
                boolean isLowerCase = isLowerCase((WordTerm) term);
                if(this.stopWords.contains(term.getTerm())){
                    //remove term and continue
                    toHandle.remove(term);
                    continue;
                }
                //stem
                stemTerm((WordTerm) term);
                //add to occurrance list
                addWordTermToList((WordTerm) term,occurances, isLowerCase);
            }
            else
                addToOccurancesList(term,occurances);
        }

    }

    private void stemTerm(WordTerm term){
        (term).toLowerCase();
        (term).setTerm(stemmer.stem(term.getTerm()));
    }

    private boolean isLowerCase(WordTerm term){
        if(Character.isLetter(term.getTerm().charAt(0)) && Character.isLowerCase(term.getTerm().charAt(0)))
            return true;
        return false;
    }

    protected Collection<ATerm> getFinalList(Map<ATerm,Integer> occurrances){
        Collection<ATerm> finalList = occurrances.keySet();
        for (ATerm term:finalList) {
            term.setOccurrences(occurrances.get(term));
        }
        return finalList;
    }*/
}
