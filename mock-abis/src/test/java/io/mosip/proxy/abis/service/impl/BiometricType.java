package io.mosip.proxy.abis.service.impl;

/**
 * BiometricType enumeration class for biometric types
 */
public class BiometricType {

    private String type;

    /**
     * Constructor for BiometricType
     */
    public BiometricType() {
        // Default constructor
    }

    /**
     * Sets the value of biometric type
     *
     * @param type the biometric type value
     * @return this BiometricType object
     */
    public BiometricType value(String type) {
        this.type = type;
        return this;
    }

    /**
     * Gets the value of biometric type
     *
     * @return the biometric type value
     */
    public String value() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }
}