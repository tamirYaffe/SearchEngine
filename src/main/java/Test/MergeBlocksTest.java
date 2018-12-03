package Test;

import SearchEngineTools.ConcurrentBuffer;
import SearchEngineTools.Indexer;
import SearchEngineTools.ReadFile;
import eu.fayder.restcountries.v1.domain.Country;
import eu.fayder.restcountries.v1.rest.CountryService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;

public class MergeBlocksTest {
    public static void main(String[] args) {
        for(Country country:CountryService.getInstance().getAll())
            System.out.println(country.getCapital());
    }
}
