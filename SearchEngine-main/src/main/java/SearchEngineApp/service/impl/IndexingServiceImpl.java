package SearchEngineApp.service.impl;

import SearchEngineApp.config.SearchConfig;
import SearchEngineApp.data.sites.SitesData;
import SearchEngineApp.models.Lemma;
import SearchEngineApp.models.Site;
import SearchEngineApp.models.Status;
import SearchEngineApp.models.WebPage;
import SearchEngineApp.service.*;
import SearchEngineApp.service.response.FalseResponse;
import SearchEngineApp.service.response.Response;

import SearchEngineApp.service.response.TrueResponse;
import SearchEngineApp.utils.ParseSiteUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

@Service
public class IndexingServiceImpl implements IndexingService {

    public static volatile boolean isRun;

    private final Logger log = Logger.getLogger(IndexingServiceImpl.class);

    private final SearchConfig searchConfig;
    private final SiteService siteService;
    private final LemmaService lemmaService;
    private final IndexService indexService;
    private final WebPageService pageService;


    public IndexingServiceImpl(SiteService siteService, LemmaService lemmaService,
                               IndexService indexService, SearchConfig searchConfig, WebPageService pageService) {
        this.searchConfig = searchConfig;
        this.siteService = siteService;
        this.lemmaService = lemmaService;
        this.indexService = indexService;
        this.pageService = pageService;
    }

    @Override
    public Response startAllIndexing() throws Exception {
        if (siteService.countStatusIndexing() > 0) {
            log.warn("Ошибка при запуске индексации. Индексация уже запущена");
            return new FalseResponse("Индексация уже запущена");
        } else {
            isRun = true;
            log.info("Запущена индексация ВСЕХ САЙТОВ");
            List<Future<Object>> futures = new ArrayList<>();
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
            for (SitesData siteData : searchConfig.getSites()) {
                Site site = siteFromSiteData(siteData);
                try {
                    ParseSiteUtil parseSiteUtil = new ParseSiteUtil(pageService, searchConfig, siteService);
                    parseSiteUtil.setSite(site);
                    futures.add(executor.submit(parseSiteUtil, (Object) null));
                } catch (Exception ex) {
                    log.error("Ошибка при индексации сайта '" + site.getUrl() + "': " + ex.getMessage());
                    site.setAllParameters(Status.FAILED, new Date(), "Ошибка при индексации сайта '" + site.getUrl() + "': " + ex.getMessage());
                    siteService.saveSite(site);
                    ex.printStackTrace();
                }
            }
            for (Future<Object> future : futures) {
                if (IndexingServiceImpl.isRun) {
                    future.get();
                } else {
                    executor.shutdownNow();
                    boolean result = executor.awaitTermination(30, TimeUnit.SECONDS);
                    if (result) {
                        throw new Exception("Процесс остановлен пользователем");
                    } else {
                        return new FalseResponse("Потоки не завершены");
                    }
                }
            }
            executor.shutdown();
            return new TrueResponse();
        }
    }

    @Override
    public Response startSingleIndexing(String url) {
        SitesData newSiteData = null;
        for (SitesData sitesData : searchConfig.getSites()) {
            if (removeSlash(sitesData.getUrl()).equals(removeSlash(url))) {
                newSiteData = sitesData;
                break;
            }
        }
        if (newSiteData != null) {
            isRun = true;
            Site site = siteService.getSite(removeSlash(newSiteData.getUrl()));
            if (site != null) {
                if (site.getStatus().equals(Status.INDEXING)) {
                    log.warn("Ошибка при запуске индексации. Индексация страницы уже запущена");
                    return new FalseResponse("Данная страница индексируется в данный момент");
                }
                reindexingSite(site);
            } else {
                log.info("Сайт '" + removeSlash(newSiteData.getUrl() + "' добавлен в очередь на ИНДЕКСАЦИЮ"));
                site = new Site(removeSlash(newSiteData.getUrl()), newSiteData.getName());
            }
            site.setAllParameters(Status.INDEXING, new Date(), null);
            siteService.saveSite(site);
            try {
                ParseSiteUtil parseSiteUtil = new ParseSiteUtil(pageService, searchConfig, siteService);
                parseSiteUtil.setSite(site);
                new Thread(parseSiteUtil).start();
            } catch (Exception ex) {
                log.error("Ошибка при индексации сайта '" + site.getUrl() + "': " + ex.getMessage());
                site.setAllParameters(Status.FAILED, new Date(), "Ошибка при индексации сайта '" + site.getUrl() + "': " + ex.getMessage());
                siteService.saveSite(site);
                ex.printStackTrace();
            }
            return new TrueResponse();
        } else {
            return new FalseResponse("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
    }

    @Override
    public Response stopIndexing() {
        if (siteService.countStatusIndexing() > 0) {
            log.info("Остановка индексации сайтов.");
            try {
                Thread.sleep(500);
                IndexingServiceImpl.isRun = false;
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return new FalseResponse("Не удалось завершить все потоки");
            }
            List<Site> siteList = siteService.getAllIndexingSite();
            for (Site site : siteList) {
                site.setAllParameters(Status.FAILED, new Date(), "Процесс индексации остановлен пользователем");
                siteService.saveSite(site);
            }
            return new TrueResponse();
        } else {
            return new FalseResponse("Индексация не запущена");
        }
    }

    private Site siteFromSiteData(SitesData siteData) {
        Site site = siteService.getSite(removeSlash(siteData.getUrl()));
        if (site != null) {
            reindexingSite(site);
        } else {
            log.info("Сайт '" + removeSlash(siteData.getUrl() + "' добавлен в очередь на ИНДЕКСАЦИЮ"));
            site = new Site(removeSlash(siteData.getUrl()), siteData.getName());
        }
        site.setAllParameters(Status.INDEXING, new Date(), null);
        siteService.saveSite(site);
        return site;
    }

    private void reindexingSite(Site site) {
        log.info("Сайт '" + site.getUrl() + "' добавлен в очередь на ПЕРЕИНДЕКСАЦИЮ");
        List<Lemma> lemmaList = lemmaService.getLemmas(site.getId());
        List<WebPage> pageList = pageService.getAllBySite(site.getId());
        pageService.resetPages(pageList);
        lemmaService.resetLemmas(lemmaList);
        indexService.resetRanks(lemmaList);
    }

    private String removeSlash(String string) {
        if (string.charAt(string.length() - 1) == '/') {
            string = string.substring(0, string.length() - 1);
        }
        return string;
    }

}
