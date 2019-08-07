import market_search.MarketFinder;
import prices_parser.PricesParser;
import proxies_search.FreeIp;
import proxies_search.ProxiesFinder;

import java.util.List;

/***
 *  Незавершенный проект парсера торговой площадки Steam
 */

public class MainClass {

    static MarketFinder marketFinder = MarketFinder.getInstance();
    static List<FreeIp> proxyList = ProxiesFinder.getGoodProxies(); // List of good proxies with my ip;
    //static PricesParser pricesParser = PricesParser.getInstance();

    public static void main(String[] args) {


        marketFinder.setProxies( proxyList );
        //marketFinder.createTable();
        //marketFinder.fillTable();
        //pricesParser.getItemsWithRaisingPrice();

    }
}