package edu.dlut.software.cage.deeprouterec.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrainPassbyDataRedis {
    private String cityCode;
    private Set<String> train_passing_by;

}
