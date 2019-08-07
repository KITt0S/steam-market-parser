package proxies_search;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

public class FreeIp {

    private Proxy proxy;
    private String countryCode;
    private String hideType;
    private boolean good;

    public FreeIp( Proxy.Type type, SocketAddress sa, String countryCode, String hideType ) {

        proxy = new Proxy( type, sa );
        this.countryCode = countryCode;
        this.hideType = hideType;
    }

    public FreeIp(){}

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public boolean isGood() {
        return good;
    }

    public void setGood(boolean good) {
        this.good = good;
    }

    public String getHideType() {
        return hideType;
    }

    public void setHideType(String hideType) {
        this.hideType = hideType;
    }

    @Override
    public String toString() {

        if( proxy != null ) {

            InetSocketAddress socketAddress = ( InetSocketAddress ) proxy.address();
            InetAddress inetAddress = socketAddress.getAddress();
            return inetAddress.getHostAddress() + ":" + socketAddress.getPort();
        } else {

            return "my ip";
        }
    }
}
