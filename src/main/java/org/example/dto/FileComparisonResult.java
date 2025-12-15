package org.example.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileComparisonResult {
    private String filename;
    private boolean damaged;
}
