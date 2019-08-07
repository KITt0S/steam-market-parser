package proxies_search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class CheckThread extends Thread {

    private static final String checkUrl = "https://steamcommunity.com/market/search/render?appid=570&sort_column=" +
            "name&sort_dir=asc&start=0&count=100&l=english&norender=1";

    private String name;
    private List<FreeIp> proxies;

    public CheckThread( String name ) {

        this.name = name;
    }

    public void setProxies(List<FreeIp> proxies) {

        this.proxies = proxies;
    }

    @Override
    public void run() {

        for (int i = 0; i < proxies.size(); ) {


            FreeIp p = proxies.get( i );
            try {

                HttpURLConnection proxyConn = ( HttpURLConnection ) new URL( checkUrl ).openConnection( p.getProxy() );
                proxyConn.setDoInput( true );
                proxyConn.setDoOutput( false );
                proxyConn.setRequestMethod( "GET" );
                proxyConn.setConnectTimeout( 5000 );
                proxyConn.setReadTimeout( 10000);
                try (InputStream proxyInput = proxyConn.getInputStream();
                     BufferedReader reader = new BufferedReader( new InputStreamReader( proxyInput ) ) ) {

                    String text = reader.readLine();
                    if ( text.startsWith( "{\"success\":true" ) ) {

                        p.setGood( true );
                    }
                } catch ( IOException e ) {

                    System.out.println( p + ": " + e.getMessage() );
                    p.setGood( false );
                } finally {

                    proxyConn.disconnect();
                }
            } catch ( IOException e ) {

                System.out.println( p + ": " + e.getMessage() );
            }
            i++;
        }
        System.out.println( name + " is done!" );
    }
}
