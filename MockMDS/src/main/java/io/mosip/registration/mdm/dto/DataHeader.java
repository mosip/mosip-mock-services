package io.mosip.registration.mdm.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class DataHeader {
	private String alg;
	private String typ;
	private List<String> x5c;

	public DataHeader() {
		alg = "RS256";
		typ = "jwt";
		x5c = new ArrayList<>();
	}
}