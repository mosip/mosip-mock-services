package io.mosip.proxy.abis.service.impl;

import java.util.List;

/**
 * DTO class for Gallery in ABIS
 */
public class Gallery {

    private List<ReferenceId> referenceIds;

    /**
     * Default constructor
     */
    public Gallery() {
        // Default constructor
    }

    /**
     * Gets the reference IDs in the gallery
     *
     * @return list of reference IDs
     */
    public List<ReferenceId> getReferenceIds() {
        return referenceIds;
    }

    /**
     * Sets the reference IDs in the gallery
     *
     * @param referenceIds list of reference IDs
     */
    public void setReferenceIds(List<ReferenceId> referenceIds) {
        this.referenceIds = referenceIds;
    }
}