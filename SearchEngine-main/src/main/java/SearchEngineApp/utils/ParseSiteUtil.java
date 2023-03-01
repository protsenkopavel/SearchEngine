package SearchEngineApp.utils;

import SearchEngineApp.config.SearchConfig;
import SearchEngineApp.models.Site;
import SearchEngineApp.models.Status;
import SearchEngineApp.models.WebPage;
import SearchEngineApp.service.SiteService;
import SearchEngineApp.service.WebPageService;
import SearchEngineApp.service.impl.IndexingServiceImpl;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinPool;

import java.util.regex.Pattern;

@Getter
@Setter
@Component
public class ParseSiteUtil implements Runnable
{
    private static final Logger log = Logger.getLogger(ParseSiteUtil.class);

    private Site site;
    private static WebPageService pageService;
    private static SearchConfig searchConfig;
    private static SiteService siteService;

    private static CopyOnWriteArraySet<String> allUrl;
    private static CopyOnWriteArraySet<WebPage> pageData;

    public ParseSiteUtil(WebPageService pageService, SearchConfig searchConfig, SiteService siteService){
        ParseSiteUtil.pageService = pageService;
        ParseSiteUtil.searchConfig = searchConfig;
        ParseSiteUtil.siteService = siteService;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    @Override
    public void run() {
        try {
            allUrl = new CopyOnWriteArraySet<>();
            pageData = new CopyOnWriteArraySet<>();
            if (IndexingServiceImpl.isRun) {
                log.info("НАЧАТА ИНДЕКСАЦИЯ сайта '" + site.getUrl() + "'");
                long startTime = System.currentTimeMillis();

                WebPage webPage = new WebPage("/", site);

                mapOfSiteForkJoinPool(webPage);

//                allUrl.clear();
                long parseTime = System.currentTimeMillis() - startTime;

                if (IndexingServiceImpl.isRun) {
                    pageService.saveAllPage(pageData);
//                    pageData.clear();
                    long insertParseTime = System.currentTimeMillis() - startTime;
                    try {
                        IndexPagesUtil.startIndexing(site);
                    }
                    catch (Exception ex) {
                        throw ex;
                    }

                    long indexTime = System.currentTimeMillis() - startTime;

                    log.info("Индексация сайта '" + site.getUrl() + "' закончена.\n" +
                            "Парсинг = " + parseTime / 1000 + " сек\n" +
                            "Запись в БД = " + (insertParseTime - parseTime) / 1000 + " сек\n" +
                            "Индексация = " + (indexTime - insertParseTime) / 1000 + " сек");
                    site.setAllParameters(Status.INDEXED, new Date(), null);
                } else {
                    throw new Exception("Процесс останолен пользователем");
                }
                siteService.saveSite(site);
            }
            else {
                throw new Exception("Процесс останолен пользователем");
            }
        }
        catch (Exception ex) {
            log.error("Ошибка при индексации сайта '" + site.getUrl() + "': " + ex.getMessage());
            site.setAllParameters(Status.FAILED, new Date(), "Ошибка при индексации. " + ex.getMessage());
            siteService.saveSite(site);
            ex.printStackTrace();
        }
    }

    private static void mapOfSiteForkJoinPool(WebPage webPage) {
        ForkJoinPool pool = new ForkJoinPool();
        WebPageValue mapOfSite = new WebPageValue(webPage);
        pool.execute(mapOfSite);
        pool.shutdown();
        mapOfSite.join();
    }

    protected static void parsePage(WebPage webPage) {
        try {
                Random random = new Random();
                String numberUserAgent = String.valueOf(random.nextInt());
                Thread.sleep(150);
                Connection.Response response = Jsoup.connect(getFullLink(webPage.getPath(), webPage.getSite()))
                        .userAgent(searchConfig.getAgent() + numberUserAgent)
                        .referrer("https://www.google.com/")
                        .timeout(1000000).maxBodySize(0).ignoreHttpErrors(true).followRedirects(true).execute();

                webPage.setCode(response.statusCode());
                Document doc = response.parse();
                webPage.setContent(doc.toString());
                pageData.add(webPage);

                findLinks(webPage, doc);
        } catch (Exception iex) {
            log.warn("Страница '" + webPage.getSite().getUrl() + webPage.getPath() + "' НЕ ДОБАВЛЕНА: " + iex.getMessage());
        }
    }

    private static void findLinks(WebPage webPage, Document doc) {
        Elements links = doc.select("a[href~=^" + webPage.getSite().getUrl() + "|^/.+]");
        for (Element link : links) {
            if(!allUrl.contains(getCorrectLink(link.attributes().get("href"), webPage.getSite()))){
                allUrl.add(getCorrectLink(link.attributes().get("href"), webPage.getSite()));
                if(isRelevant(getCorrectLink(link.attributes().get("href"), webPage.getSite()))) {
                    webPage.setUrlList(getCorrectLink(link.attributes().get("href"), webPage.getSite()));
                }
            }
        }
    }

    private static String getCorrectLink(String link, Site site) {
        if (link.startsWith(site.getUrl())) {
            link = link.replaceFirst(site.getUrl(),"");
        }
        if (link.contains("#")) {
            link = link.substring(0, link.indexOf('#'));
        } else if (link.contains("?")) {
            link = link.substring(0, link.indexOf('?'));
        }
        return link;
    }

    private static String getFullLink(String link, Site site) {
        return site.getUrl() + link;
    }

    private static boolean isRelevant (String string) {
        Pattern filter1 = Pattern.compile(".*/\\S+\\.(x?html?|php|jsp)$");
        Pattern filter2 = Pattern.compile(".*/[^.$&?\\s]*$");
        return filter1.matcher(string).matches() || filter2.matcher(string).matches();
    }


}
