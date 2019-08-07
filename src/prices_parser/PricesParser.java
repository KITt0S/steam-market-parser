package prices_parser;

import market_search.MarketItem;
import mysql_utils.MySQLUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PricesParser {

    private static PricesParser instance;

    public static synchronized PricesParser getInstance() {

        if( instance == null ) {

            instance = new PricesParser();
        }
        return instance;
    }

    private PricesParser(){}

    public void getItemsWithRaisingPrice() {

        List<String> allItemNames = MySQLUtils.getItemNames();
        for ( String name : allItemNames ) {

            List<BigDecimal> priceHistory = MySQLUtils.getItemPriceHistory( name );
            BigDecimal maxPriceGain = findPriceGain( priceHistory );
            if( maxPriceGain.compareTo( new BigDecimal( "0.1" ) ) > -1 ) {

                System.out.println( name + ": item have raised for " + maxPriceGain.toString() );
            }
        }
    }

    private BigDecimal findPriceGain( List<BigDecimal> priceHistory ) {

        List<BigDecimal> priceGainList = new ArrayList<>();
        for (int i = 0; i < priceHistory.size(); i++) {

            BigDecimal priceDelta = priceHistory.get( 0 ).subtract( priceHistory.get( i ) );
            BigDecimal priceGain = priceDelta.divide( priceHistory.get( i ) ).setScale( 2, RoundingMode.FLOOR );
            priceGainList.add( priceGain );
        }
        priceGainList.sort( new Comparator<BigDecimal>() {
            @Override
            public int compare(BigDecimal o1, BigDecimal o2) {

                return o2.compareTo(o1);
            }
        });
        return priceGainList.get( 0 );
    }
}
