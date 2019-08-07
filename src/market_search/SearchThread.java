package market_search;

import mysql_utils.MySQLUtils;
import proxies_search.FreeIp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchThread extends Thread {

    private static int floorCount = 31154;

    private Map.Entry<FreeIp, Boolean> freeIp;

    private int fIndex;
    private int lIndex;
    private int ne = 0;

    public SearchThread(Map.Entry<FreeIp, Boolean> proxy, int threadNum ) {

        this.freeIp = proxy;
        this.freeIp.setValue( false );
        setName( threadNum );
    }

    private void setName( int threadNum ) {

        setName( "SearchThread-" + threadNum  );
    }

    void setIndexes( int firstIndex, int lastIndex ) {

        this.fIndex = firstIndex;
        this.lIndex = lastIndex;
    }

    @Override
    public void run() {

        fillTable();
    }

    public void fillTable() {

        for ( int i = fIndex; i < lIndex; ) {

            try {

                String urlLine = RRpUtils.createUrlRequest( i );
                HttpURLConnection httpUrlConn = null;

                if( freeIp.getKey().getProxy() == null ) {

                    httpUrlConn = ( HttpURLConnection ) new URL( urlLine ).openConnection();
                } else {

                    httpUrlConn = ( HttpURLConnection ) new URL( urlLine ).openConnection( freeIp.getKey().getProxy() );
                }
                httpUrlConn.setDoInput( true );
                httpUrlConn.setDoOutput( false );
                httpUrlConn.setConnectTimeout( 5000 );
                httpUrlConn.setReadTimeout( 10000);
                try ( InputStream httpInputStream = httpUrlConn.getInputStream();
                      BufferedReader reader = new BufferedReader( new InputStreamReader( httpInputStream ) ) ) {

                    String response = reader.readLine();
                    int totalCount = RRpUtils.getTotalCount(response);
                    if ( totalCount >= floorCount) {

                        addItemsToTable( response );
                        i++;
                        ne = 0;
                    }
                } catch ( IOException e ) {

                    ne++;
                    System.out.println( this.getName() + ": number of exceptions is " + ne );
                    if( ne > 2 ) {

                        freeIp.getKey().setGood( false );
                        updateProxy();
                        ne = 0;
                    }
                } finally {

                    httpUrlConn.disconnect();
                }
            } catch (IOException e) {

                e.printStackTrace();
            }
            try {
                sleep( 8000 );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        freeIp.setValue( true );
        System.out.println( this.getName() + " is done.");
    }

    private void addItemsToTable(String response ) {

        Pattern pattern = Pattern.compile( "(\"market_hash_name\":\".*?\"})|" +
                "(\"sell_price\":\\d+)" );
        Matcher matcher = pattern.matcher( response );
        String price = null;
        while( matcher.find() ) {

            String matchedText = matcher.group();
            if( matchedText.contains( "\"sell_price\":" ) ) {

                price = matchedText.replaceAll( "\"sell_price\":", "" );
            } else if( matchedText.contains( "\"market_hash_name\":" ) && price != null && !price.equals( "0" ) ) {

                String itemName = matchedText.replaceAll( "\"market_hash_name\":\"", "" ).
                        replaceFirst( "\"}", "" ).replaceAll( "'", "''");
                MarketItem mi = new MarketItem( itemName, new BigDecimal( price ) );
                MySQLUtils.addSimpleEntry( mi );
            }
        }
    }

    private boolean updateProxy() {

        boolean isUpdated = false;
        boolean isPossible = false;
        while ( true ) {

            Iterator<Map.Entry<FreeIp, Boolean>> iter = MarketFinder.getInstance().proxyMap.entrySet().iterator();

            while( iter.hasNext() ) {

                Map.Entry<FreeIp, Boolean> entry = iter.next();
                if( entry.getKey().isGood() ) {

                    isPossible = true;
                    if (entry.getValue()) {

                        freeIp = entry;
                        freeIp.setValue( false );
                        isUpdated = true;
                        break;
                    }
                }
            }
            if( !isPossible ) {

                System.out.println( "All proxies are bad" );
                return false;
            }
            if( isUpdated ) {

                System.out.println(  getName() + " is updated to " + freeIp.getKey() + "." );
                return true;
            }
        }
    }
}
