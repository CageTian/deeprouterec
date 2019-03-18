package edu.dlut.software.cage.deeprouterec

import com.mongodb.MongoClient
import edu.dlut.software.cage.deeprouterec.domain.StationDataRedis
import edu.dlut.software.cage.deeprouterec.domain.TicketsDataMongo
import edu.dlut.software.cage.deeprouterec.domain.TicketsInfoMongo
import edu.dlut.software.cage.deeprouterec.domain.TrainDataRedis
import edu.dlut.software.cage.deeprouterec.domain.TrainPassbyDataRedis
import edu.dlut.software.cage.deeprouterec.repository.MongoTicketsDataRepository
import edu.dlut.software.cage.deeprouterec.repository.MongoTicketsDataRepositoryImp
import edu.dlut.software.cage.deeprouterec.repository.RouteStationDataRepository
import edu.dlut.software.cage.deeprouterec.repository.RouteStationDataRepositoryImp
import edu.dlut.software.cage.deeprouterec.service.RouteDataUpdateService
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoDbFactory
import org.springframework.data.redis.core.RedisTemplate
import spock.lang.Specification
import sun.util.resources.LocaleData

import java.time.LocalDate

class RouteDataUpdateServiceTest extends Specification{
    def "get tickets api test"() {
        given:
        RouteStationDataRepository repository = Mock()
        RouteDataUpdateService routeDataUpdateService = new RouteDataUpdateService(repository)

        when:
//        routeDataUpdateService.getRestTickets("2019-03-01", "SHH", "BJP")
        routeDataUpdateService.getAllPassbyTicketInfo("曲靖")

        then:
        noExceptionThrown()
    }

    def "get all tickets api test"() {
        given:
        RouteStationDataRepository repository = new RouteStationDataRepositoryImp(
                new RedisTemplate<String, StationDataRedis>(),
                new RedisTemplate<String, TrainDataRedis>(),
                new RedisTemplate<String, TrainPassbyDataRedis>())
        MongoTicketsDataRepository mongoTicketsDataRepository = new MongoTicketsDataRepositoryImp(
                new MongoTemplate(new MongoClient("localhost", 27017), "train_tickets")
        )
        RouteDataUpdateService routeDataUpdateService = new RouteDataUpdateService(repository, mongoTicketsDataRepository)

        when:
//        routeDataUpdateService.getRestTickets("2019-03-01", "SHH", "BJP")
        List<TicketsDataMongo> tickets = new ArrayList<>()
        Map<String, TicketsInfoMongo> route = new HashMap<>()
        route.put("G123", TicketsInfoMongo.builder().restTickets(23)
                .start("昆明").trainId("G123").build())
        tickets.add(TicketsDataMongo.builder()
                .now_date(LocalDate.now().toString())
        .search_date(LocalDate.now().toString())
        .terminal("曲靖")
        .ticket_info(route).build())
        routeDataUpdateService.putCityTickets(tickets)

        then:
        noExceptionThrown()
    }
}
