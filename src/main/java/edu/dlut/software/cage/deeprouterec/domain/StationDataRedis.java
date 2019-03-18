package edu.dlut.software.cage.deeprouterec.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationDataRedis {
    private String cityCode;
    private String name;
    private String spelling;
}
