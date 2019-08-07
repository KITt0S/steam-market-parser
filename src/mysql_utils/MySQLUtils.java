package mysql_utils;

import market_search.MarketItem;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MySQLUtils {

    private static final String url = "jdbc:mysql://localhost:3306/steamstore?useUnicode=true&useJDBCCompliantTimezone" +
            "Shift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private static final String user = "root";
    private static final String password = "root";

    private static Statement statement;

    static {

        try {

            Connection connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createTable() {

        String query = "CREATE TABLE IF NOT EXISTS dota_items (item_name varchar(255)," +" price integer, sample_date DATE, " +
                "primary key(item_name, sample_date)" + ");";
        try {
            statement.execute( query );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addSimpleEntry( MarketItem item ) {

        if( item != null ) {

            try {

                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
                String currentDate = dateFormat.format( date );
                String entry = "INSERT INTO dota_items(item_name, price, sample_date) VALUES ('" +
                        item.getName() + "', " + item.getPrice().toString() + ", '" + currentDate + "') " +
                        "ON DUPLICATE KEY UPDATE item_name='" + item.getName() + "', price=" + item.getPrice().
                        toString() + ", sample_date='" + currentDate + "';";
                statement.execute( entry );
            } catch (SQLException e) {

                System.out.println( item.getName() + " - " + item.getPrice() );
                e.printStackTrace();
            }
        }
    }

    public static List<BigDecimal> getItemPriceHistory( String itemName ) {

        try {

            String request = "SELECT price FROM dota_items WHERE item_name='" + itemName + "' order by sample_date DESC;";
            ResultSet price = statement.executeQuery( request );
            List<BigDecimal> itemPriceHistory = new ArrayList<>();
            while ( price.next() ) {

                itemPriceHistory.add( price.getBigDecimal( "price" ).movePointLeft( 2 ) );
            }
            return itemPriceHistory;
        } catch ( SQLException e ) {

            e.printStackTrace();
        }
        return null;
    }

    public static BigDecimal getItemCurrentPrice( String itemName ) {

        List<BigDecimal> itemPriceHistory = getItemPriceHistory( itemName );
        if( itemPriceHistory != null && !itemPriceHistory.isEmpty() ) {

            return itemPriceHistory.get( 0 );
        }
        return null;
    }

    public static List<String> getItemNames( ) {

        try {

            String request = "SELECT DISTINCT item_name FROM dota_items order by name ASC;";
            ResultSet namesSet = statement.executeQuery( request );
            List<String> names = new ArrayList<>();
            while( namesSet.next() ) {

                names.add( namesSet.getString( "item_name" ) );
            }
            return names;
        } catch ( SQLException e ) {

            e.printStackTrace();
        }
        return null;
    }
}
