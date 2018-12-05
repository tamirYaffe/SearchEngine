package SearchEngineTools.ParsingTools;

public class TextTokenListThatDoesntRemoveS extends DocumentTokenList {


    protected String removeUnnecessaryChars(String sToken){
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
        /*if(sToken.length()>=2 && sToken.substring(sToken.length()-2,sToken.length()).equals("'s"))
            sToken = sToken.substring(0,sToken.length()-2);*/
        return sToken.length()>0 ? sToken : null;
    }
}
