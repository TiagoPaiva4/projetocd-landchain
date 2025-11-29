package events;

import java.io.Serializable;
import java.util.Date;

public class RentDistributionEvent implements Event, Serializable {

    private final String assetID;
    private final double amount;
    private final long timestamp;

    public RentDistributionEvent(String assetID, double amount) {
        this.assetID = assetID;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }

    public String getAssetID() {
        return assetID;
    }

    public double getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getType() {
        return "RENT_DISTRIBUTION";
    }

    public long getTimestampMillis() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "RentDistributionEvent{ assetID='" + assetID + "', amount=" + amount +
               ", timestamp=" + new Date(timestamp) + " }";
    }
}
