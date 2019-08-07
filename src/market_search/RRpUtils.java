package market_search;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RRpUtils {

    public static int getTotalCount( String response ) {

        Pattern pattern = Pattern.compile( "\"total_count\":\\d+" );
        Matcher matcher = pattern.matcher( response );
        int totalCount = 0;
        if( matcher.find() ) {

            totalCount = Integer.parseInt( matcher.group().replaceAll( "\"total_count\":",
                    "" ) );
        }
        return totalCount;
    }

    public static String createUrlRequest( int i ) {

        int startIndex = ( 100 * i );
        return "https://steamcommunity.com/market/search/render?appid=570&sort_column=name&sort_dir=asc&" +
                "start=" + startIndex + "&count=100&l=english&norender=1";
    }

    String changeHashNameToName( String hashName ) {

        String decodedName  = null;
        try {
            decodedName = URLDecoder.decode( hashName, "UTF-8" );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decodedName.replaceAll( "'", "''" );
    }
}
