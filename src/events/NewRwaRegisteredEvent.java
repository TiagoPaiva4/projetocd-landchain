/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package events;

import rwa.RWARecord;
/**
 *
 * @author Tiago Paiva
 */
public class NewRwaRegisteredEvent implements Event {

    private final RWARecord record;
    private final long timestamp;

    public NewRwaRegisteredEvent(RWARecord record) {
        this.record = record;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String getType() {
        return "NEW_RWA_REGISTERED";
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public RWARecord getRecord() {
        return record;
    }
}