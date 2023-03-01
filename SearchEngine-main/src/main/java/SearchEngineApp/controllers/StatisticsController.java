package SearchEngineApp.controllers;

import SearchEngineApp.service.StatisticsService;
import SearchEngineApp.service.response.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.FileReader;
import java.io.IOException;

@Controller
public class StatisticsController
{
    private StatisticsService service;

    public StatisticsController(StatisticsService service) {
        this.service = service;
    }

    @GetMapping(value = "/statistics")
    public ResponseEntity<Object> statistic(){
        Response response = service.getStatistics();
        return ResponseEntity.ok().body(response);
    }
}
