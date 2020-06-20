package io.mosip.registration.mdm.dto;

import java.util.ArrayList;
import java.util.List;

public class DataHeader {

    public String alg;
    public String typ;
    public List<String> x5c;

    public DataHeader()
    {
        alg = "RS256";
        typ = "jwt";
        x5c = new ArrayList<>();
    }
}