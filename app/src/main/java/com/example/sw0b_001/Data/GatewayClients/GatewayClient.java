package com.example.sw0b_001.Data.GatewayClients;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value={"MSISDN"}, unique = true)})
public class GatewayClient {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name="type")
    String type;

    @ColumnInfo(name="MSISDN")
    String MSISDN;

    @ColumnInfo(name="default")
    boolean isDefault = false;

    @ColumnInfo(name="operator_name")
    String operatorName;

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    @ColumnInfo(name="operator_id")
    String operatorId;

    @ColumnInfo(name="country")
    String country;

    @ColumnInfo(name="last_ping_session")
    double lastPingSession = 0.0;

    public GatewayClient(String type, String MSISDN, String operatorName, String country, boolean isDefault) {
        this.type = type;
        this.MSISDN = MSISDN;
        this.operatorName = operatorName;
        this.isDefault = isDefault;
        this.country = country;
    }

    public GatewayClient() {}

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return this.country;
    }

    public String getMSISDN() {
        return MSISDN;
    }

    public void setMSISDN(String MSISDN) {
        this.MSISDN = MSISDN;
    }

    public String getOperatorName() {
        return this.operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public boolean isDefault() {
        return this.isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setLastPingSession(double lastPingSession) {
        this.lastPingSession = lastPingSession;
    }

    public double getLastPingSession() {
        return this.lastPingSession;
    }
}
