package SearchEngineApp.utils;

import SearchEngineApp.models.*;
import SearchEngineApp.service.*;
import SearchEngineApp.service.impl.IndexingServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

@Component
public class IndexPagesUtil
{
    private static final Logger log = Logger.getLogger(IndexPagesUtil.class);
    private static LemmaService lemmaService;
    private static IndexService indexService;
    private static SiteService siteService;
    private static FieldService fieldService;
    private static WebPageService pageService;

    public IndexPagesUtil (LemmaService lemmaService, IndexService indexService,
                           SiteService siteService, FieldService fieldService, WebPageService pageService) {
        IndexPagesUtil.lemmaService = lemmaService;
        IndexPagesUtil.indexService = indexService;
        IndexPagesUtil.siteService = siteService;
        IndexPagesUtil.fieldService = fieldService;
        IndexPagesUtil.pageService = pageService;
    }

    public static synchronized void startIndexing(Site site) throws Exception {
        List<WebPage> webPageList = pageService.getAllBySite(site.getId());
        if(fieldService.getSize() == 0) {
            writingField();
        }
        List<Field> fieldList = fieldService.getFields();
        if(webPageList.size() > 0) {
            List<Future<Object>> futures = new ArrayList<>();
            ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(3);
            for (WebPage webPage : webPageList) {
                    IndexPageRunnable indexPageRunnable = new IndexPageRunnable(siteService, lemmaService, indexService);
                    indexPageRunnable.setWebPage(webPage);
                    indexPageRunnable.setFieldList(fieldList);
                    futures.add(executor.submit(indexPageRunnable,(Object)null));
            }
            for (Future<Object> future : futures) {
                if(IndexingServiceImpl.isRun) {
                    future.get();
                }
                else {
                    executor.shutdownNow();
                    boolean result = false;
                    while(!result) {
                        executor.shutdownNow();
                        result = executor.awaitTermination(30, TimeUnit.SECONDS);
                    }
                    throw new Exception("Процесс остановлен пользователем");
                }
            }
            executor.shutdown();
            site.setAllParameters(Status.INDEXED, new Date(), null);
        }
        else {
            log.error("У сайта '" + site.getUrl() + "' нет страниц для индексации");
            throw new Exception("Нет страниц для индексации");
        }
        siteService.saveSite(site);
    }

    private static void writingField () {
        Field fieldTitle = new Field("title", "title", 1.0f);
        Field fieldBody = new Field("body", "body", 0.8f);
        fieldService.saveField(fieldTitle);
        fieldService.saveField(fieldBody);

    }
}
