package edu.dlut.software.cage.deeprouterec.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.redis.core.index.Indexed;

import java.util.Map;

@Data
@Builder
@Document(collection = "train_tickets")
public class TicketsDataMongo {
    private String terminal;
    @Indexed
    private String now_date;
    private String search_date;
    private Map<String, TicketsInfoMongo> ticket_info;


}
