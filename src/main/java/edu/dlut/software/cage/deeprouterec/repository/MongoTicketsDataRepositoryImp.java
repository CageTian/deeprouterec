package edu.dlut.software.cage.deeprouterec.repository;

import edu.dlut.software.cage.deeprouterec.domain.TicketsDataMongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class MongoTicketsDataRepositoryImp implements MongoTicketsDataRepository {

    private MongoTemplate mongoTemplate;

    @Autowired
    MongoTicketsDataRepositoryImp(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void batchInsert(List<TicketsDataMongo> ticketsDataMongo) {
        mongoTemplate.insert(ticketsDataMongo, TicketsDataMongo.class);
    }

    @Override
    public void batchUpdate(List<TicketsDataMongo> ticketsDataMongo) {
    }

    @Override
    public void deleteByCity(String city) {
        Query query = Query.query(Criteria.where("terminal").is(city));
        mongoTemplate.remove(query, TicketsDataMongo.class);
    }

    @Override
    public TicketsDataMongo searchByCity(String city) {
        return null;
    }
}
