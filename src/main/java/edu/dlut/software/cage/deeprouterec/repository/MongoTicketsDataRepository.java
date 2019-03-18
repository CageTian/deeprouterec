package edu.dlut.software.cage.deeprouterec.repository;

import edu.dlut.software.cage.deeprouterec.domain.TicketsDataMongo;

import java.util.List;

public interface MongoTicketsDataRepository {
    void batchInsert(List<TicketsDataMongo> ticketsDataMongo);
    void batchUpdate(List<TicketsDataMongo> ticketsDataMongo);
    void deleteByCity(String city);
    TicketsDataMongo searchByCity(String city);

}
