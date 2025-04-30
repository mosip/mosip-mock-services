package io.mosip.proxy.abis.service.impl;

/**
 * DTO class for Reference ID in ABIS
 */
public class ReferenceId {

    private String referenceId;

    /**
     * Default constructor
     */
    public ReferenceId() {
        // Default constructor
    }

    /**
     * Gets the reference ID
     *
     * @return the reference ID
     */
    public String getReferenceId() {
        return referenceId;
    }

    /**
     * Sets the reference ID
     *
     * @param referenceId the reference ID to set
     */
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
}