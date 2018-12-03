package SearchEngineTools.ParsingTools.Term;

import eu.fayder.restcountries.v1.domain.Country;
import eu.fayder.restcountries.v1.rest.CountryService;

import java.util.List;

public class CityTerm extends ATerm{
    String capitalCity;
    private String state;
    private String popSize;
    private String coin;
    private List<Integer>positions;

    public List<Integer> getPositions() {
        return positions;
    }

    public String getState() {
        return state;
    }

    public String getPopSize() {
        return popSize;
    }

    public String getCoin() {
        return coin;
    }

    public CityTerm(String capitalCity){
        this.capitalCity=capitalCity;
        Country country= CountryService.getInstance().getByCapital("capitalCity").get(0);
        state=country.getName();
        popSize=""+country.getPopulation();
        coin=country.getCurrencies().get(0);
    }

    @Override
    protected String createTerm() {
        return capitalCity;
    }
}
