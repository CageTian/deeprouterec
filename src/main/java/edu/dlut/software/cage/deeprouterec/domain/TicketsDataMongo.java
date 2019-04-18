package edu.dlut.software.cage.deeprouterec.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.redis.core.index.Indexed;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@Document(collection = "train_tickets")
public class TicketsDataMongo {
    @Indexed
    private String now_date;
    private String terminal;
    private String search_date;
    private Set<TicketsInfoMongo> ticket_info;
}
