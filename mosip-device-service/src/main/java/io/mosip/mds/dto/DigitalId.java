package io.mosip.mds.dto;

import java.util.Date;

public class DigitalId {

    public String serialNo;
    public String make;
    public String model;
    public String type;
    public String deviceSubType;
    public String deviceProvider;
    public String deviceProviderId;
    public Date dateTime;
    /*
    serialNo - Serial number of the device. This value should be same as printed on the device (Refer Physical ID)
make - Brand name. This value should be same as printed on the device (Refer Physical ID)
model - Model of the device. This value should be same as printed on the device (Refer Physical ID)
type - ["Fingerprint", "Iris", "Face"], //More types will be added.
deviceSubType - subtype is based on the type. 
    Fingerprint - "Slap", "Single", "Touchless"
    Iris - "Single", "Double",
    Face - "Full face"
deviceProvider - Device provider name, This would be a legal entity in the country,
deviceProviderId: Device provider Id issued by MOSIP adopters
dateTime:  ISO format with timezone.  The time during the issuance of this identity
    */

}
