package org.example.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FileComparisonResult {
    private String filename;
    private boolean damaged;
    private List<DamageDetail> damages;
}
