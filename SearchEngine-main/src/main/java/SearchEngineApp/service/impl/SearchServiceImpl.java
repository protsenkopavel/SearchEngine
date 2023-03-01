package SearchEngineApp.service.impl;

import SearchEngineApp.data.search.SearchData;
import SearchEngineApp.models.Site;
import SearchEngineApp.service.SearchService;
import SearchEngineApp.service.SiteService;
import SearchEngineApp.service.response.FalseResponse;
import SearchEngineApp.service.response.Response;
import SearchEngineApp.utils.SearchTextUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SearchServiceImpl implements SearchService {

    private final Logger log = Logger.getLogger(SearchServiceImpl.class);
    private final SiteService siteService;

    public SearchServiceImpl(SiteService siteService) {
        this.siteService = siteService;
    }

    @Override
    public Response startSearch(SearchData searchData) throws IOException {
        if(searchData.getSite() == null) {
            log.info("Поиск по запросу : '" + searchData.getQuery() + "' на всех сайтах");
            if(siteService.countStatusIndexing() == 0 && siteService.countStatusFailed() == 0) {
                return SearchTextUtil.startSearch(searchData.getQuery(), null);
            } else {
                log.warn("Ошибка поиска. Не все сайты проиндексированы");
                return new FalseResponse("Ошибка поиска. Не все сайты проиндексированы.");
            }
        } else  {
            log.info("Поиск по запросу '" + searchData.getQuery() + "' на сайте '" + searchData.getSite() + "'");
            Site site = siteService.getSite(searchData.getSite());
            if(site == null) {
                log.warn("Ошибка поиска. Указанный сайт '" + searchData.getSite() + "' не найден");
                return new FalseResponse("Указанный сайт не наден. Поиск не возможен");
            } else {
                return SearchTextUtil.startSearch(searchData.getQuery(), site);
            }
        }

//        int offset = searchData.getOffset() == null ? 0 : searchData.getOffset();
//        int limit = searchData.getLimit() == null ? 20 : searchData.getLimit();

    }
}
