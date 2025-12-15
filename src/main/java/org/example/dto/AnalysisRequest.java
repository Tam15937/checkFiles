package org.example.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AnalysisRequest {
    private String originalDir;
    private String damagedDir;
}
