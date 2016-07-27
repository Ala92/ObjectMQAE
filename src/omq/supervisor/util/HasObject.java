package omq.supervisor.util;

import java.io.Serializable;
import java.util.ArrayList;

import omq.common.broker.Measurement;

/**
 *
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 *
 */
public class HasObject implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String brokerName;
    private String reference;
    private boolean slow;
    private ArrayList<String> slowBindings;
    
    private boolean hasObject;
    private boolean hasUserID;

    private Measurement measurement;

    public HasObject(String brokerName, String reference, boolean hasObject, Measurement measurement) {
        this.brokerName = brokerName;
        this.reference = reference;
        this.slowBindings = new ArrayList<String>();
        this.hasObject = hasObject;
        this.measurement = measurement;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean hasObject() {
        return hasObject;
    }

    public void setHasObject(boolean hasObject) {
        this.hasObject = hasObject;
    }

    public boolean hasUserID() {
        return hasUserID;
    }

    public void setHasUserID(boolean hasUserID) {
        this.hasUserID = hasUserID;
    }

    public ArrayList<String> getSlowBindings() {
        return slowBindings;
    }

    public void setSlowBindings(ArrayList<String> slowBindings) {
        this.slowBindings = slowBindings;
    }

    public Measurement getMeasurement() {
        return measurement;
    }

    public void setMeasurement(Measurement measurement) {
        this.measurement = measurement;
    }

}
