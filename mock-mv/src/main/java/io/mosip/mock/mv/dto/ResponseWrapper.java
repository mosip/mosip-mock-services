package io.mosip.mock.mv.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ResponseWrapper<T> {
	private String id;
	private String version;
	private String responsetime;
	private T response;

	private List<ErrorDTO> errors = new ArrayList<>();
}
