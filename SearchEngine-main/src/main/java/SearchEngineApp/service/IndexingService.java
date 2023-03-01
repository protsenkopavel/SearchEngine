package SearchEngineApp.service;

import SearchEngineApp.service.response.Response;

public interface IndexingService
{
    Response startAllIndexing() throws Exception;
    Response stopIndexing() throws InterruptedException;
    Response startSingleIndexing(String url);
}
