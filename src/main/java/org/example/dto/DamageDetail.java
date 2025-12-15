package org.example.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class DamageDetail {
    private long offset;
    private int originalByte;
    private int damagedByte;
    private String hexOriginal;
    private String hexDamaged;
}
