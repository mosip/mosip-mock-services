package io.mosip.mock.mv.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ResponseWrapper<T> {
	private String id;
	private String version;
	private String responsetime;
	private T response;

	private List<ErrorDTO> errors = new ArrayList<>();

}
