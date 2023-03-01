package SearchEngineApp.utils;

import SearchEngineApp.models.WebPage;
import SearchEngineApp.service.impl.IndexingServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

public class WebPageValue extends RecursiveAction {

    private final WebPage webPage;

    public WebPageValue(WebPage webPage) {
        this.webPage = webPage;
    }

    @Override
    protected void compute() {
        List<WebPageValue> taskList = new ArrayList<>();
        if (IndexingServiceImpl.isRun) {
            ParseSiteUtil.parsePage(webPage);
            for (String child : webPage.getUrlList()) {
                WebPage webPageNew = new WebPage(child, webPage.getSite());
                WebPageValue task = new WebPageValue(webPageNew);
                task.fork();
                taskList.add(task);
            }
            for (WebPageValue task : taskList) {
                task.join();
            }
        }
    }
}
