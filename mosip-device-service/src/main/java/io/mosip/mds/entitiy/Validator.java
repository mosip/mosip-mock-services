package io.mosip.mds.entitiy;

import java.util.ArrayList;
import java.util.List;

import io.mosip.mds.dto.ValidateResponseRequestDto;

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

    public List<String> Errors = new ArrayList<>(); 

    public ValidationStatus Status = ValidationStatus.Pending;

    public void Validate(ValidateResponseRequestDto response)
    {
        try{
            Status = DoValidate(response)?ValidationStatus.Passed:ValidationStatus.Failed;
        }
        catch(Exception ex)
        {
            Status = ValidationStatus.InternalException;
            Errors.add(ex.toString());
        }
    }

    protected abstract Boolean DoValidate(ValidateResponseRequestDto response);

}