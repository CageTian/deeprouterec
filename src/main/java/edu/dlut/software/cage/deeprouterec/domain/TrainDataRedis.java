package edu.dlut.software.cage.deeprouterec.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrainDataRedis {
    private String station_train_code;
    private String start;
    private String terminal;
    private String train_no;
    private String type;
    private String date;

}
