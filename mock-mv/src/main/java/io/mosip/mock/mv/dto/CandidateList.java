package io.mosip.mock.mv.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class CandidateList {
	private Integer count;
	private Map<String, String> analytics;
	private List<Candidate> candidates;
}