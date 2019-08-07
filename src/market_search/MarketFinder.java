package market_search;

import mysql_utils.MySQLUtils;
import proxies_search.FreeIp;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MarketFinder {

    private static MarketFinder instance;
    private int nPages;
    Map<FreeIp, Boolean> proxyMap = Collections.synchronizedMap( new HashMap<>() );

    public static synchronized MarketFinder getInstance() {

        if( instance == null ) {

            instance = new MarketFinder();
        }
        return instance;
    }

    private MarketFinder() {}

    public void setProxies( List<FreeIp> proxyList ) {

        for (FreeIp p : proxyList) {

            proxyMap.put(p, true);
        }
    }

    public void createTable() {

        MySQLUtils.createTable();
    }

    public void fillTable() {

        int nThreads = getNThreads();
        int nPages = 312;
        ExecutorService service = Executors.newFixedThreadPool( nThreads );
        int step = ( int ) Math.floor( ( float ) nPages / ( float ) nThreads );
        int rest = nPages - step * nThreads;
        int i = 0;
        Iterator<Map.Entry<FreeIp, Boolean>> iterator = proxyMap.entrySet().iterator();
        while ( iterator.hasNext() ) {

            Map.Entry<FreeIp, Boolean> entry = iterator.next();
            SearchThread thread = new SearchThread( entry, i );;
            if( i < nThreads ) {

                if( i < rest ) {

                    thread.setIndexes( ( step + 1 ) * i, ( step + 1 ) * ++i );
                } else {

                    thread.setIndexes( step * i + rest, step * ++i + rest );
                }
                service.submit( thread );
            } else {

                break;
            }
        }
        service.shutdown();
        try {

            service.awaitTermination( Long.MAX_VALUE, TimeUnit.MILLISECONDS );
        } catch ( InterruptedException e ) {

            e.printStackTrace();
        }
        System.out.println( "Task is completed! Table is filled with prices." );
    }


    private int getNThreads() {

        int nThreads = 0;
        if( proxyMap.size() <= 2 ) {

            nThreads = 1;
        } else if( proxyMap.size() < 40 ) {

            nThreads = ( int ) Math.floor( ( float ) proxyMap.size() / 2f );
        } else nThreads = 20;
        return nThreads;
    }
}
