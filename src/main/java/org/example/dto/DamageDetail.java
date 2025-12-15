package org.example.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DamageDetail {
    private long offset;
    private int originalByte;
    private int damagedByte;
    private String hexOriginal;
    private String hexDamaged;
}
