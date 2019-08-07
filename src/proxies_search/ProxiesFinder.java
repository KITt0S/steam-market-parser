package proxies_search;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.*;

public class ProxiesFinder {

    private static String proxiesUrl = "https://free-proxy-list.net/";

    public static List<FreeIp> getGoodProxies() {

        List<FreeIp> allProxies = parseWebsite();

        List<FreeIp> freeProxies = getEliteAndAnonymousProxies( selectProxiesByPort(
                selectProxiesByCode( allProxies, "UA", "RU", "PL" ), 80, 8080 ) );
        System.out.println( "Number of proxies that need to be checked: " + freeProxies.size() );
        int nThreads = 20;
        int step = ( int ) Math.floor( ( float ) freeProxies.size() / nThreads);
        int rest = freeProxies.size() - step * nThreads;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

        for (int i = 0; i < nThreads; ) {

            CheckThread chThread = new CheckThread( "CheckThread-" + i );
            if( i < rest ) {

                chThread.setProxies( freeProxies.subList( ( step + 1 ) * i, ( step + 1 ) * ++i ));
            } else {

                chThread.setProxies( freeProxies.subList( step * i + rest, step * ++i + rest ) );
            }
            executorService.submit( chThread );
        }
        executorService.shutdown();
        try {

            executorService.awaitTermination( Long.MAX_VALUE, TimeUnit.MILLISECONDS );
        } catch ( InterruptedException e ) {

            e.printStackTrace();
        }
        List<FreeIp> goodProxies = new ArrayList<>();
        goodProxies.add( new FreeIp() ); //insert in list clear ip
        for (FreeIp p :
                freeProxies) {

            if( p.isGood() ) {

                goodProxies.add( p );
            }
        }
        System.out.println("List of proxies is ready! Number of proxies is " + goodProxies.size() + ".");
        return goodProxies;
    }

    private static List<FreeIp> getEliteAndAnonymousProxies(List<FreeIp> proxies ) {

        List<FreeIp> selectedProxies = new ArrayList<>();
        for( FreeIp p : proxies ) {

            String hideType = p.getHideType();
            if( hideType.equals( "elite proxy" ) || hideType.equals( "anonymous" ) ) {

                selectedProxies.add( p );
            }
        }
        return selectedProxies;
    }

    private static List<FreeIp> selectProxiesByPort(List<FreeIp> proxies, int... excludePorts ) {

        List<FreeIp> selectedProxies = new ArrayList<>();
        for ( FreeIp p : proxies ) {

            InetSocketAddress address = ( InetSocketAddress ) p.getProxy().address();
            int port = address.getPort();
            for (int i = 0; i < excludePorts.length; i++) {

                if( port == excludePorts[ i ] ) {

                    break;
                }
                if( i == excludePorts.length - 1 ) {

                    selectedProxies.add( p );
                }
            }
        }
        return selectedProxies;
    }

    private static List<FreeIp> selectProxiesByCode( List<FreeIp> unslcPrxs, String... codes ) {

        if( codes.length == 0 ) {

            return unslcPrxs;
        } else {

            List<FreeIp> selectedProxies = new ArrayList<>();
            for ( FreeIp p :
                    unslcPrxs ) {

                String cc = p.getCountryCode();
                for (String code :
                        codes) {

                    if( cc.equals( code ) ) {

                        selectedProxies.add( p );
                    }
                }

            }
            return selectedProxies;
        }
    }

    private static List<FreeIp> parseWebsite() {


        Document doc = null;
        try {

            doc = Jsoup.connect( proxiesUrl ).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> ips = getIPs( doc );
        List<Integer> ports = getPorts( doc );
        List<String> codes = getCodes( doc );
        List<String> types = getProxyTypes( doc );
        List<FreeIp> proxies = new ArrayList<>();
        for (int i = 0; i < ips.size(); i++) {

            FreeIp mp = new FreeIp( Proxy.Type.HTTP, new InetSocketAddress( ips.get( i ), ports.get( i ) ),
                    codes.get( i ), types.get( i ) );
            proxies.add( mp );
        }
        return proxies;
    }

    private static List<String> getIPs( Document document ) {

        List<String> ipList = new ArrayList<>();
        Elements ips = document.select("td:matchesOwn(^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$)" );
        Iterator<Element> elementIterator = ips.iterator();
        while (elementIterator.hasNext()) {

            ipList.add(elementIterator.next().text() );
        }
        return ipList;
    }

    private static List<Integer> getPorts( Document document ) {

        List<Integer> portsList = new ArrayList<>();
        Elements ports = document.select( "td:matchesOwn(^\\d+$)" );
        for (Element p :
                ports) {

            portsList.add( Integer.parseInt( p.text() ) );
        }
        return portsList;
    }

    private static List<String> getCodes( Document document ) {

        List<String> codesList = new ArrayList<>();
        Elements codes = document.select( "td:matches(^([A-Z]{2}$|--))");
        for (Element c :
                codes) {

            codesList.add( c.text() );
        }
        return  codesList;
    }

    private static List<String> getProxyTypes( Document document ) {

        Elements proxyTypes = document.select( "td:matchesOwn(elite proxy|anonymous|transparent)");
        return proxyTypes.eachText();
    }
}
