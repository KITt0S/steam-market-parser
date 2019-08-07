package market_search;

import java.math.BigDecimal;

public class MarketItem implements Cloneable {

    private String name;
    private BigDecimal price;

    MarketItem() {

    }

    @Override
    protected Object clone() throws CloneNotSupportedException {

        return super.clone();
    }

    public MarketItem(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

}