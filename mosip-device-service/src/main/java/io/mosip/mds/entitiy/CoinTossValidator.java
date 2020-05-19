package io.mosip.mds.entitiy;


import io.mosip.mds.dto.ValidateResponseRequestDto;

public class CoinTossValidator extends Validator {

    public CoinTossValidator()
    {
        super("CoinTossValidator", "Randomly succeeding or failing validator");
    }


    @Override
    protected Boolean DoValidate(ValidateResponseRequestDto response) {
        
        if(System.currentTimeMillis() % 2 == 1)
        {
            Errors.add("Validation failed due to odd time of run!");
            return false;
        }
        return true;
    }
}