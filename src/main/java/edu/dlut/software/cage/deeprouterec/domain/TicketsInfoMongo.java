package edu.dlut.software.cage.deeprouterec.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document
public class TicketsInfoMongo {
    private String trainId;
    private String start;
    private int restTickets;
}
