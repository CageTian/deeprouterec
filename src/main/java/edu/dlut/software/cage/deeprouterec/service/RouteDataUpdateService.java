package edu.dlut.software.cage.deeprouterec.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import constant.Constants;
import edu.dlut.software.cage.deeprouterec.domain.*;
import edu.dlut.software.cage.deeprouterec.repository.MongoTicketsDataRepository;
import edu.dlut.software.cage.deeprouterec.repository.RouteStationDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RouteDataUpdateService {

    private RouteStationDataRepository repository;
    private MongoTicketsDataRepository mongoTicketsDataRepository;
    @Value("${my_properties.request_gap}")
    private long requestGap;

    @Autowired
    public RouteDataUpdateService(RouteStationDataRepository repository,
                                  MongoTicketsDataRepository mongoTicketsDataRepository) {
        this.repository = repository;
        this.mongoTicketsDataRepository = mongoTicketsDataRepository;
    }

    public void putPassByBatch() {
        Set<String> stations = repository.getAllKeys(Constants.STA_PREFIX);
        log.info("before filer station size:" + stations.size());
        Set<String> cache = repository.getAllKeys(Constants.PAS_PREFIX)
                .stream().map(
                        s1 -> s1.replaceFirst(Constants.PAS_PREFIX, Constants.STA_PREFIX))
                .collect(Collectors.toSet());

        stations.removeAll(cache);
        log.info("after filer station size:" + stations.size());
        stations.forEach(s -> putPassBy(s.replaceFirst(Constants.STA_PREFIX, "")));
    }

    public HttpStatus putPassBy(String city) {
        if (city.isEmpty())
            throw new MissingFormatArgumentException("city is empty");
        String doc;
        StationDataRedis stations = repository.getStationCodeByName(city);
        if (stations == null) {
            log.error("cityname: [" + city + "] can not find");
            return HttpStatus.BAD_GATEWAY;
        }
        String citycode = stations.getCityCode();
        try {
            doc = Jsoup.connect(Constants.PAS_API_URL).ignoreContentType(true)
                    .header("Accept", "application/json, text/javascript, */*; q=0.01")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Content-Length", "22")
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("Host", "www.12306.cn")
                    .header("Origin", "https://www.12306.cn")
                    .header("Referer", "https://www.12306.cn/index/")
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .data("train_station_code", citycode).post().text();
        } catch (IOException e) {
            log.error("cityname: [" + city + "] " + e.getMessage());
            return HttpStatus.ACCEPTED;
        }
        JSONObject jsonObject = JSON.parseObject(doc);
        int httpStatus = jsonObject.getInteger("httpstatus");
        if (httpStatus != 200) {
            log.error(city + ", City code" + citycode + "passby api error code: " + httpStatus);
        }
        Set<String> collection = new HashSet<>();
        jsonObject.getJSONArray("data").forEach(a -> collection.add((String) a));
        log.info(city + " size of pass by: " + collection.size());
        repository.putPassBy(Constants.PAS_PREFIX + city, new TrainPassbyDataRedis(citycode, collection));
        return HttpStatus.OK;
    }

    public void putStations() throws IOException {
        String doc = Jsoup.connect(Constants.STA_URL).get().text();
        String pattern = "var station_names ='(.*?)'";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(doc);
        if (!m.find()) {
            log.warn("Stations url parse failed");
            return;
        }
        String[] rowData = m.group(1).split("\\|");
        for (int i = 0; i < rowData.length; i++) {
            if (i % 5 == 4) {
                StationDataRedis model = StationDataRedis.builder()
                        .spelling(rowData[i - 1])
                        .cityCode(rowData[i - 2])
                        .name(rowData[i - 3]).build();
                log.info(model.toString());
                repository.putStations(Constants.STA_PREFIX + model.getName(), model);
            }
        }
    }

    public void putTrains() throws IOException {
        String doc = Jsoup.connect(Constants.TRA_URL).maxBodySize(50 * 1024 * 1024).get().text();
        doc = doc.replaceFirst("var train_list =", "");

//        System.out.println(doc);
        JSONObject jsonObject = JSONObject.parseObject(doc);
//        System.out.println(jsonObject.toString());
        jsonObject.values().forEach(route -> {
            ((JSONObject) route).values().forEach(route_list -> {
                ((JSONArray) route_list).forEach(value -> {
                    String[] tmp = ((JSONObject) value).get("station_train_code").toString().split("\\(|\\)|-");
                    if (tmp.length != 3) {
                        throw new ExceptionInInitializerError("error train_list.js format line:" + value);
                    }
                    TrainDataRedis trainDataRedis = TrainDataRedis.builder()
                            .station_train_code(tmp[0])
                            .start(tmp[1])
                            .terminal(tmp[2])
                            .build();
                    log.info(trainDataRedis.toString());
                    repository.putTrainsInfo(Constants.TRA_PREFIX + tmp[0], trainDataRedis);
                });
            });
        });
    }

    // 插入当前日期，cityName下未来30天的的所有列车的余票数据
    public void put30DaysCityTickets(String cityName) {
        String now_date = LocalDate.now().toString();
        LocalDate date = LocalDate.now();
        List<TicketsDataMongo> ticketsDataMongoList = new ArrayList<>();
        for (int i = 1; i < 30; i++) {
            TicketsDataMongo ticketsData = TicketsDataMongo.builder()
                    .now_date(now_date)
                    .search_date(date.toString())
                    .terminal(cityName)
                    .build();
            ticketsData.setTicket_info(getAllTrainTickets(ticketsData));
            ticketsDataMongoList.add(ticketsData);
            date = date.plusDays(1);
        }
        mongoTicketsDataRepository.batchInsert(ticketsDataMongoList);
    }

    // 获得指定日期终点的所有列车的余票信息
    private Set<TicketsInfoMongo> getAllTrainTickets(TicketsDataMongo ticketsDataMongo) {
        Set<String> stationKeys = new HashSet<>();
        Set<TicketsInfoMongo> res = new HashSet<>();
        Set<String> startStationName = getAllStartStations(ticketsDataMongo.getTerminal());
        //合并多个起始站点列车余票信息
        startStationName.forEach(start -> {
            Set<TicketsInfoMongo> tmp = new HashSet<>();
            try {
                tmp = getRestTickets(ticketsDataMongo.getSearch_date(),
                        name2Code(start), name2Code(ticketsDataMongo.getTerminal()));
                Thread.sleep(requestGap);
            } catch (Exception e) {
                log.error("getRestTickets erro:,date={}, start={}, terminal={}, detail:{}",
                        ticketsDataMongo.getSearch_date(), start, ticketsDataMongo.getTerminal(), e.getMessage());
            }
            tmp.forEach(data -> {
                if (!stationKeys.contains(data.getTrainId())) {
                    res.add(data);
                    stationKeys.add(data.getTrainId());
                } else {
                    //todo:票数多优先？
                }
            });
        });
        StringBuffer stringBuffer = new StringBuffer();
        res.forEach(s -> stringBuffer.append("|").append(s.getTrainId()).append(":")
                .append(s.getRestTickets()).append("/").append(s.getTotalTickets()));
        log.debug(ticketsDataMongo.getSearch_date() + ":" + ticketsDataMongo.getTerminal()
                + res.size() + "," + stringBuffer.toString());
        return res;
    }

    //获得指定日期起点终点的列车余票信息
    @Retryable(value = {IOException.class})
    private Set<TicketsInfoMongo> getRestTickets(String date, String start, String terminal) throws IOException {
        String doc = Jsoup.connect(Constants.TIC_API_URL).ignoreContentType(true)
                .header("Accept", "*/*")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7")
                .header("Connection", "keep-alive")
                .header("Host", "kyfw.12306.cn")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")
                .header("X-Requested-With", "XMLHttpRequest")
                .data("leftTicketDTO.train_date", date)
                .data("leftTicketDTO.from_station", start)
                .data("leftTicketDTO.to_station", terminal)
                .data("purpose_codes", "ADULT").get().text();
        log.trace(doc);
        JSONObject jsonObject = JSON.parseObject(doc);
        if (jsonObject.isEmpty() || !jsonObject.containsKey("data")) {
            log.error("error response of {}, {}, {}, detail:{}", date, start, terminal, doc);
        }
        jsonObject = jsonObject.getJSONObject("data");
        String httpStatus = jsonObject.getString("flag");
        if (!httpStatus.equals("1")) {
            log.error(date + ", start: " + start + " terminal: " + terminal + " flag error: " + doc);
        }
        Set<TicketsInfoMongo> res = new HashSet<>();
        jsonObject.getJSONArray("result").forEach(a -> {
            String[] train = a.toString().split("\\|");
            if (train.length != 39) {
                log.warn("error length:{}", a.toString());
                return;
            }
            int rest = 0;
            int total = 0;
            for (int i = 21; i <= 32; i++) {
                switch (train[i]) {
                    case "":
                        break;
                    case "无":
                        total += Constants.HAVE_TICKETS;
                    case "有":
                    case "*":
                        rest += Constants.HAVE_TICKETS;
                        total += Constants.HAVE_TICKETS;
                        break;
                    default:
                        rest += Integer.valueOf(train[i]);
                        total += Constants.HAVE_TICKETS;
                }
            }
            TicketsInfoMongo tic = TicketsInfoMongo.builder()
                    .trainId(train[3])
                    .start(train[6])
                    .restTickets(rest)
                    .totalTickets(total)
                    .build();
            log.trace(tic.toString());
            res.add(tic);
        });
        return res;
    }

    // 站点名称转换三字码
    private String name2Code(String cityName) {
        return repository.getStationCodeByName(cityName).getCityCode();
    }

    // 获取所有起始站点名称，并去重
    private Set<String> getAllStartStations(String cityName) {
        TrainPassbyDataRedis trainPassbyDataRedis = repository.getPassBy(cityName);
        Set<String> startNameSet = new HashSet<>();
        if (trainPassbyDataRedis == null) {
            log.error("trainPassbyDataRedis == null");
            return startNameSet;
        }
        trainPassbyDataRedis.getTrain_passing_by().forEach(train -> {
            TrainDataRedis trainDataRedis = repository.getStartByTrainId(train);
            if (trainDataRedis == null) {
                log.error(train + ": repository.getStartByTrainId == null");
                return;
            }
            // repeat start station name check.
            String startName = trainDataRedis.getStart();
            startNameSet.add(startName);
        });
        StringBuffer stringBuffer = new StringBuffer();
        startNameSet.forEach(s -> stringBuffer.append("|").append(s));
        log.trace("getAllStartStations:size" + startNameSet.size() + ":" + stringBuffer.toString());
        return startNameSet;
    }
//    // 获得所有余票信息
//    public List<TicketsDataMongo> getAllPassbyTicketInfo(String cityName) {
//        TrainPassbyDataRedis trainPassbyDataRedis = repository.getPassBy(cityName);
//        if (trainPassbyDataRedis == null) {
//            log.error("trainPassbyDataRedis == null");
//            return null;
//        }
//        String cityCode = trainPassbyDataRedis.getCityCode();
//        Map<String, Map<String, TicketsInfoMongo>> ticInfo = new HashMap<>();
//        LocalDate date = LocalDate.now();
//        for (int i = 1; i < 30; i++) {
//            ticInfo.put(date.toString(), new HashMap<>());
//            date = date.plusDays(1);
//        }
//        // 获取所有起始站点名称，并去重
//        Set<String> startNameSet = new HashSet<>();
//        trainPassbyDataRedis.getTrain_passing_by().forEach(train -> {
//            TrainDataRedis trainDataRedis = repository.getStartByTrainId(train);
//            if (trainDataRedis == null) {
//                log.error(train + "repository.getStartByTrainId == null");
//                return;
//            }
//            // repeat start station name check.
//            String startName = trainDataRedis.getStart();
//            if (startNameSet.contains(startName)) {
//                return;
//            } else {
//                startNameSet.add(startName);
//            }
//
//            // update no repeated routes for 30 days.
//            ticInfo.forEach((tk, tv) -> {
//                Map<String, TicketsInfoMongo> ticketsInfoMongos;
//                try {
//                    String startCode = repository.getStationCodeByName(startName).getCityCode();
//                    ticketsInfoMongos = getRestTickets(tk, startCode, cityCode);
//                } catch (IOException e) {
//                    log.error("getRestTickets erro:,date={}, start={}, terminal={}, detail:{}", tk,
//                            startName, cityName, e.getMessage());
//                    return;
//                }
//                ticketsInfoMongos.forEach((ik, iv) -> {
//                    if (!tv.containsKey(ik) || iv.getRestTickets() < tv.get(ik).getRestTickets()) {
//                        tv.put(ik, iv);
//                    }
//                });
////                tv.putAll(ticketsInfoMongos);
//            });
//
//        });
//
//        String now_date = LocalDate.now().toString();
//        StringBuffer stringBuffer = new StringBuffer("29 days data size:");
//        List<TicketsDataMongo> res = new ArrayList<>();
//        for (Map.Entry<String, Map<String, TicketsInfoMongo>> entry : ticInfo.entrySet()) {
//            TicketsDataMongo tic = TicketsDataMongo.builder()
//                    .search_date(entry.getKey())
//                    .now_date(now_date)
//                    .ticket_info(entry.getValue())
//                    .terminal(cityName)
//                    .build();
//            stringBuffer.append(tic.getTicket_info().size()).append("|");
//            res.add(tic);
//        }
//        stringBuffer.append("start station name:");
//        startNameSet.forEach(name -> stringBuffer.append(name).append("|"));
//        log.info("mango size:{}, {}", res.size(), stringBuffer.toString());
//        return res;
//    }
}
