package SearchEngineTools.ParsingTools.Term;

import java.util.ArrayList;
import java.util.List;

public class NumberTerm extends ATerm{


    private List<Character> numberWithoutDecimal;
    private int locationOfDecimal;
    private boolean containsDecimal;
    private boolean isNegative;


    public NumberTerm(NumberTerm other){
        this.numberWithoutDecimal = other.getNumberWithoutDecimal();
        this.locationOfDecimal = other.getLocationOfDecimal();
        containsDecimal = other.containsDecimal;
        this.isNegative=other.isNegative;
        this.term = null;

    }

    public NumberTerm(String s){
        locationOfDecimal = Integer.MAX_VALUE;
        String number = removeCommas(s);
        //check if negative number
        int firstDigit;
        if(s.charAt(0)=='-'){
            isNegative = true;
            firstDigit=1;
        }
        else {
            isNegative = false;
            firstDigit=0;
        }
        if(number.contains(".")){
            numberWithoutDecimal = new ArrayList<>(number.length()-1);
            containsDecimal =true;
        }
        else{
            numberWithoutDecimal = new ArrayList<>(number.length());
            containsDecimal = false;
        }
        for (int i = firstDigit; i < number.length(); i++) {
            if(number.charAt(i)=='.') {
                locationOfDecimal = i;
            }
            else {
                numberWithoutDecimal.add(number.charAt(i));
            }
        }
        this.term = null;
    }

    private static String removeCommas(String s) {
        StringBuilder toReturn = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if(c==',')
                continue;
            toReturn.append(c);
        }
        return toReturn.toString();
    }


    public boolean isWholeNumber(){
        return !this.containsDecimal;
    }


    public void multiply(Value multiplyBy){
        int digitsToAdd=0;
        switch (multiplyBy){
            case TRILLION:
                digitsToAdd=12;
                break;
            case BILLION:
                digitsToAdd=9;
                break;
            case MILLION:
                digitsToAdd=6;
                break;
            case THOUSAND:
                digitsToAdd=3;
                break;
        }
        //add digits
        for (int i = 0; i < digitsToAdd; i++) {
            numberWithoutDecimal.add('0');
        }
        //move decimal
        if(locationOfDecimal!=Integer.MAX_VALUE) {
            this.locationOfDecimal += digitsToAdd;
        }
        this.term = null;
    }

    public List<Character> getNumberWithoutDecimal() {
        List<Character> toReturn = new ArrayList<>();
        for (int i = 0; i < numberWithoutDecimal.size(); i++) {
            toReturn.add(numberWithoutDecimal.get(i));
        }
        return toReturn;
    }

    public int getLocationOfDecimal() {
        return locationOfDecimal;
    }

    /**
     * removes unnecessary digits and decimal point before printing term
     * @param s numberterm to print
     * @return valid string for term
     */
    private static String removeUnnecessaryDigits(String s){
        int lastNecessaryIndx = s.length()-1;
        boolean necessary = false;
        while (!necessary && lastNecessaryIndx>0){
            if(s.charAt(lastNecessaryIndx)=='0'){
                lastNecessaryIndx--;
            }
            else if(s.charAt(lastNecessaryIndx)=='.'){
                lastNecessaryIndx--;
                necessary=true;
            }
            else {
                necessary = true;
            }
        }
        String toReturn = s.substring(0,lastNecessaryIndx+1);
        return toReturn;
    }

    public static boolean isInteger (NumberTerm numberTerm){
        return numberTerm.locationOfDecimal==Integer.MAX_VALUE;
    }



    public String getValue(){
        return getValue(locationOfDecimal);
    }

    public float getValueOfNumber(){
        StringBuilder val = new StringBuilder();
        val.append("0");
        for (int i = 0; i < this.numberWithoutDecimal.size(); i++) {
            if(i == locationOfDecimal)
                val.append(".");
            val.append(numberWithoutDecimal.get(i));
        }
        return Float.parseFloat(val.toString());
    }

    private String getValue(int locationOfDecimal){
        StringBuilder value = new StringBuilder();
        int digitsToPrint = containsDecimal ? this.numberWithoutDecimal.size()+1 : this.numberWithoutDecimal.size();
        boolean containsDecimal = false;
        for (int i = 0, j=0; i < digitsToPrint; i++) {
            if(i==locationOfDecimal){
                value.append('.');
                containsDecimal = true;
            }
            else {
                value.append(this.numberWithoutDecimal.get(j));
                j++;
            }
        }
        return containsDecimal ? removeUnnecessaryDigits(value.toString()) : value.toString();
    }

    protected String createTerm(){
        String afterNumber = "";
        int digitsBeforeDecimal = containsDecimal ? locationOfDecimal : numberWithoutDecimal.size();
        if(digitsBeforeDecimal >= 10){
            afterNumber = "B";
            digitsBeforeDecimal-=9;
        }
        else if(digitsBeforeDecimal >= 7){
            afterNumber = "M";
            digitsBeforeDecimal-=6;
        }
        else if(digitsBeforeDecimal >= 4){
            afterNumber = "K";
            digitsBeforeDecimal-=3;
        }


        String beforeNumber = isNegative ? "-" : "";

        return beforeNumber+getValue(digitsBeforeDecimal)+afterNumber;
    }

    public String getTerm(){
        if(term==null)
            term = createTerm();
        return super.getTerm();
    }

    public boolean isNegative() {
        return isNegative;
    }
}
