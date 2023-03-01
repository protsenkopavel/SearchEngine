package SearchEngineApp.controllers;

import SearchEngineApp.data.search.SearchData;
import SearchEngineApp.service.SearchService;
import SearchEngineApp.service.response.FalseResponse;
import SearchEngineApp.service.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping(value = "/search")
    public ResponseEntity<Object> search(SearchData searchData) throws IOException {
        Response response;
        if(searchData.getQuery() == null || searchData.getQuery().length() == 0) {
            response = new FalseResponse("Задан пустой поисковый запрос");
        }
        else {
            response = searchService.startSearch(searchData);
        }
        return ResponseEntity.ok().body(response);
    }
}
