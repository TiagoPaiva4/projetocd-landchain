/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package events;

/**
 *
 * @author Tiago Paiva
 */
public class RentDistributionEvent implements Event {

    private final String propertyId;
    private final double amount;
    private final long timestamp;

    public RentDistributionEvent(String propertyId, double amount) {
        this.propertyId = propertyId;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String getType() {
        return "RENT_DISTRIBUTION";
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public double getAmount() {
        return amount;
    }
}