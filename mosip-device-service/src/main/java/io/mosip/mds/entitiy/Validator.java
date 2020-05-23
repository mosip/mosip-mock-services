package io.mosip.mds.entitiy;

import java.util.List;

import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.postresponse.ValidationResult;

public abstract class Validator {

    public enum ValidationStatus
    {
        Pending,
        Passed,
        Failed,
        InternalException
    };

    public Validator(String Name, String Description)
    {
        this.Name = Name;
        this.Description = Description;
    }

    public String Name;

    public String Description;

    public ValidationResult Validate(ValidateResponseRequestDto response)
    {
        ValidationResult validationResult = new ValidationResult();
        validationResult.validationName = Name;
        validationResult.validationDescription = Description;
        ValidationStatus status = ValidationStatus.Pending;
        try{
            List<String> errors = DoValidate(response);
            status = (errors == null || errors.size() == 0)?ValidationStatus.Passed:ValidationStatus.Failed;
            if(status != ValidationStatus.Passed)
                validationResult.errors.addAll(errors);
        }
        catch(Exception ex)
        {
            status = ValidationStatus.InternalException;
            validationResult.errors.add(ex.toString());
        }
        validationResult.status = status.name();
        return validationResult;
    }

    protected abstract List<String> DoValidate(ValidateResponseRequestDto response);

}