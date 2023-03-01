package SearchEngineApp.service;

import SearchEngineApp.data.search.SearchData;
import SearchEngineApp.service.response.Response;

import java.io.IOException;

public interface SearchService {

    Response startSearch(SearchData searchData) throws IOException;
}
