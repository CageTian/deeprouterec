package edu.dlut.software.cage.deeprouterec.controller;

import edu.dlut.software.cage.deeprouterec.service.RouteDataUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;

@RestController
@Slf4j
public class RouteDataUpdateController {

    private RouteDataUpdateService routeDataUpdateService;

    @Autowired
    public RouteDataUpdateController(RouteDataUpdateService routeDataUpdateService) {
        this.routeDataUpdateService = routeDataUpdateService;
    }

    @PostMapping(value = "/initdata", produces = "application/json")
    public ResponseEntity<Void> initData(
            @RequestParam(name = "route_data", required = false,
                    defaultValue = "sta_train") String initType
    ) throws IOException {
        switch (initType) {
            case "station":
                routeDataUpdateService.putStations();
                break;
            case "trains":
                routeDataUpdateService.putTrains();
                break;
            case "sta_train":
                routeDataUpdateService.putStations();
                routeDataUpdateService.putTrains();
                break;
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/tickets", produces = "application/json")
    public ResponseEntity<Void> putTickets(
            @RequestParam(name = "city", required = false) String city
    ) {
        if (city.equals("all")) {
            //todo
            System.out.println("haha");
        } else {
            routeDataUpdateService.putCityTickets(city);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/passby", produces = "application/json")
    public ResponseEntity<Void> putPassBy(
            @RequestParam(name = "city", required = false,
                    defaultValue = "all") String city
    ) {
        HttpStatus status = HttpStatus.OK;
        if (city.equals("all")) {
            routeDataUpdateService.putPassByBatch();
        } else {
            status = routeDataUpdateService.putPassBy(city);
        }
        return new ResponseEntity<>(status);
    }
}
