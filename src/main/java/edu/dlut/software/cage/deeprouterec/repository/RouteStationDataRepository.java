package edu.dlut.software.cage.deeprouterec.repository;

import edu.dlut.software.cage.deeprouterec.domain.StationDataRedis;
import edu.dlut.software.cage.deeprouterec.domain.TrainDataRedis;
import edu.dlut.software.cage.deeprouterec.domain.TrainPassbyDataRedis;
import java.util.Set;

public interface RouteStationDataRepository {
    //todo Batch Insert
    void putStations(String key, StationDataRedis stations);

    void putTrainsInfo(String key, TrainDataRedis trains);

    StationDataRedis getStationCodeByName(String name);

    TrainDataRedis getStartByTrainId(String trainId);

    void putPassBy(String key, TrainPassbyDataRedis trainPassbyDataRedis);

    TrainPassbyDataRedis getPassBy(String key);

    Set<String> getAllKeys(String prefix);
}
