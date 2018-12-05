package SearchEngineTools.ParsingTools.Term;

import SearchEngineTools.ParsingTools.Parse;
import eu.fayder.restcountries.v1.domain.Country;
import eu.fayder.restcountries.v1.rest.CountryService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CityTerm extends WordTerm {
    private Parse parse = new Parse();
    private ATerm statePopulation;
    private ATerm countryCurrency;
    private List<Integer> positions;
    boolean isOrigin=false;
    private Country country;

    public List<Integer> getPositions(){
        return positions;
    }


    public CityTerm(String cityName,Country country){
        super(cityName);
        this.country=country;
        addPopulationTerm(country);
        addCurrency(country);
        positions = new ArrayList<>();
    }


    private void addPopulationTerm(Country country){
        List<String> statePopulationList = new ArrayList<>();
        statePopulationList.add(country.getPopulation().toString());
        Collection<ATerm> statePopulationTerm = parse.parseText(statePopulationList);
        if(statePopulationTerm.size()==1){
            for (ATerm statePopulation:statePopulationTerm) {
                this.statePopulation = statePopulation;
            }
        }
    }

    private void addCurrency(Country country){
        List<String> currencyStringList = new ArrayList<>();
        currencyStringList.addAll(country.getCurrencies());
        Collection<ATerm> currencyTerms = parse.parseText(currencyStringList);
        for (ATerm term:currencyTerms) {
            countryCurrency=term;
        }
    }


    @Override
    protected String createTerm() {
        return term;
    }


    public String getStatePopulation(){
        if(statePopulation==null)
            addPopulationTerm(country);
        return statePopulation.getTerm();
    }

    public String getCountryCurrency(){
        if(countryCurrency==null)
            addCurrency(country);
        return countryCurrency.getTerm();
    }

    public void addPosition(int position){
        positions.add(position);
    }

    public void addAllPositions(CityTerm other){
        this.positions.addAll(other.positions);
    }

    public boolean equals(Object o){
        if(o instanceof CityTerm){
            CityTerm other = (CityTerm)o;
            return this.getTerm().equals(other.getTerm());
        }
        return false;
    }

    public void setAsOrigin(){
        isOrigin=true;
    }

    public boolean isOrigin(){
        return isOrigin;
    }

    public String toString(){
        String toReturn ="CityTerm:"+getTerm()+"~"+"\nOccurences: "+(getOccurrences())+"\nis origin:"+isOrigin+
                "\nCountry: "+country.getName()+"\nPopulation size: "+getStatePopulation()+
                "\nCurrency: "+getCountryCurrency()+"\nPositions:";
        for (int i = 0; i < positions.size(); i++) {
            toReturn+=","+positions.get(i);
        }
        return toReturn;
    }

}
