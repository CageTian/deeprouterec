package edu.dlut.software.cage.deeprouterec.repository;

import constant.Constants;
import edu.dlut.software.cage.deeprouterec.domain.StationDataRedis;
import edu.dlut.software.cage.deeprouterec.domain.TrainDataRedis;
import edu.dlut.software.cage.deeprouterec.domain.TrainPassbyDataRedis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class RouteStationDataRepositoryImp implements RouteStationDataRepository {
    private RedisTemplate<String, StationDataRedis> stationDataRedisTemplate;
    private RedisTemplate<String, TrainDataRedis> trainDataRedisTemplate;
    private RedisTemplate<String, TrainPassbyDataRedis> trainPassbyDataRedisTemplate;


    @Autowired
    public RouteStationDataRepositoryImp (RedisTemplate<String, StationDataRedis> stationDataRedisTemplate,
                               RedisTemplate<String, TrainDataRedis> trainDataRedisTemplate,
                                          RedisTemplate<String, TrainPassbyDataRedis> trainPassbyDataRedisTemplate) {
        this.stationDataRedisTemplate = stationDataRedisTemplate;
        this.trainDataRedisTemplate = trainDataRedisTemplate;
        this.trainPassbyDataRedisTemplate = trainPassbyDataRedisTemplate;
    }

    @Override
    public void putStations(String key, StationDataRedis stations) {
        stationDataRedisTemplate.opsForValue().set(key, stations);
    }

    @Override
    public Set<String> getAllKeys(String prefix) {
        return stationDataRedisTemplate.keys(prefix+"*");
    }

    @Override
    public void putTrainsInfo(String key, TrainDataRedis trains) {
        trainDataRedisTemplate.opsForValue().set(key, trains);
    }

    @Override
    public StationDataRedis getStationCodeByName(String name) {
        return stationDataRedisTemplate.opsForValue().get(Constants.STA_PREFIX + name);
    }

    @Override
    public TrainDataRedis getStartByTrainId(String trainId) {
        return trainDataRedisTemplate.opsForValue().get(Constants.TRA_PREFIX + trainId);

    }

    @Override
    public void putPassBy(String key, TrainPassbyDataRedis passTrains) {
        System.out.println(key+":"+passTrains.toString());
        trainPassbyDataRedisTemplate.opsForValue().set(key, passTrains);
    }

    @Override
    public TrainPassbyDataRedis getPassBy(String key) {
        return trainPassbyDataRedisTemplate.opsForValue().get(Constants.PAS_PREFIX + key);

    }



}
