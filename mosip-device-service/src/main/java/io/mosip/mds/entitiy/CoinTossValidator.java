package io.mosip.mds.entitiy;

import java.util.ArrayList;
import java.util.List;

import io.mosip.mds.dto.ValidateResponseRequestDto;

public class CoinTossValidator extends Validator {

    public CoinTossValidator()
    {
        super("CoinTossValidator", "Randomly succeeding or failing validator");
    }


    @Override
    protected List<String> DoValidate(ValidateResponseRequestDto response) {
        List<String> errors = new ArrayList<>();
        if(System.currentTimeMillis() % 2 == 1)
        {
            errors.add("Validation failed due to odd time of run!");
            return errors;
        }
        return errors;
    }
}