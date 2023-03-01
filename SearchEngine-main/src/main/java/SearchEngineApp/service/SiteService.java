package SearchEngineApp.service;

import SearchEngineApp.models.Site;
import SearchEngineApp.models.Status;

import java.util.List;

public interface SiteService {

    void saveSite(Site site);
    Site getSite(String url);
    List<Site> getAllSites();
    long countStatusIndexing();
    long countStatusFailed();
    List<Site> getAllIndexingSite();

}
