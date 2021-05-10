package io.mosip.proxy.abis.entity;

public class IdentifyDelayResponse {
    private int delayResponse = 0;

    private IdentityResponse identityResponse;

    public IdentifyDelayResponse(IdentityResponse ir, int d){
        super();
        this.delayResponse = d;
        this.identityResponse = ir;
    }

    public int getDelayResponse(){
        return this.delayResponse;
    }

    public IdentityResponse getIdentityResponse(){
        return this.identityResponse;
    }
}
