package SearchEngineTools.ParsingTools.Term;

import SearchEngineTools.ParsingTools.Parse;
import eu.fayder.restcountries.v1.domain.Country;
import eu.fayder.restcountries.v1.rest.CountryService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CityTerm extends WordTerm {
    private static Parse parse = new Parse();
    private static CountryService countryService= CountryService.getInstance();
    private ATerm statePopulation;
    private ATerm countryCurrency;
    private List<Integer> positions;
    boolean isOrigin=false;

    public List<Integer> getPositions(){
        return positions;
    }


    public CityTerm(String cityName){
        super(cityName);
        Country country = countryService.getByCapital(cityName).get(0);
        addPopulationTerm(country);
        addCurrency(country);
        positions = new ArrayList<>();
    }

    public CityTerm(String cityName, int position){
        this(cityName);
        addPosition(position);
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
        countryCurrency = currencyTerms.iterator().next();
    }


    @Override
    protected String createTerm() {
        return term;
    }


    public String getStatePopulation(){
        return statePopulation.getTerm();
    }

    public String getCountryCurrency(){
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
                "\nCountry: "+countryService.getByCapital(term).get(0).getName()+"\nPopulation size: "+getStatePopulation()+
                "\nCurrency: "+getCountryCurrency()+"\nPositions:";
        for (int i = 0; i < positions.size(); i++) {
            toReturn+=","+positions.get(i);
        }
        return toReturn;
    }

}
