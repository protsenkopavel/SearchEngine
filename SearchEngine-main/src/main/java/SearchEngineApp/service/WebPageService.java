package SearchEngineApp.service;

import SearchEngineApp.models.WebPage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

public interface WebPageService
{
    void saveAllPage(CopyOnWriteArraySet<WebPage> pages);
    List<WebPage> getAllBySite(long siteId);
    List<WebPage> getAllWebPages(List<Long> pageList);
    Long pageCount();
    void resetPages(List<WebPage> pageList);
}
