package SearchEngineApp.service.impl;

import SearchEngineApp.data.statistic.Detailed;
import SearchEngineApp.service.response.StatisticResponse;
import SearchEngineApp.data.statistic.Statistics;
import SearchEngineApp.data.statistic.Total;
import SearchEngineApp.models.Site;
import SearchEngineApp.service.LemmaService;
import SearchEngineApp.service.SiteService;
import SearchEngineApp.service.StatisticsService;
import SearchEngineApp.service.WebPageService;
import SearchEngineApp.service.response.Response;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteService siteService;
    private final WebPageService pageService;
    private final LemmaService lemmaService;

    public StatisticsServiceImpl (SiteService siteService, WebPageService pageService, LemmaService lemmaService) {
        this.siteService = siteService;
        this.pageService = pageService;
        this.lemmaService = lemmaService;
    }

    @Override
    public Response getStatistics () {
        Statistics statistics = new Statistics();
        try {
            Total total = new Total();
            List<Site> siteList = siteService.getAllSites();
            total.setSites(siteList.size());
            total.setPages(pageService.pageCount());
            total.setLemmas(lemmaService.lemmaCount());
            total.setIndexing(siteService.countStatusIndexing() > 0);
            statistics.setTotal(total);

            for (Site site : siteList) {
                Detailed detailed = new Detailed();
                detailed.setUrl(site.getUrl());
                detailed.setName(site.getName());
                detailed.setStatus(site.getStatus());
                detailed.setStatusTime(site.getStatusTime());
                detailed.setError(site.getLastError());
                detailed.setPages(site.getPageList().size());
                detailed.setLemmas(site.getLemmaList().size());
                statistics.setDetailed(detailed);
            }
        }
        catch (Exception ex) {
            return new StatisticResponse(false, statistics);
        }
        return new StatisticResponse(true, statistics);
    }
}
