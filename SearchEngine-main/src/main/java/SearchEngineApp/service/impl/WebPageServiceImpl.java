package SearchEngineApp.service.impl;

import SearchEngineApp.models.WebPage;
import SearchEngineApp.repository.WebPageRepository;
import SearchEngineApp.service.WebPageService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class WebPageServiceImpl implements WebPageService {

    private final WebPageRepository pageRepository;

    public WebPageServiceImpl(WebPageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    @Override
    public synchronized void saveAllPage(CopyOnWriteArraySet<WebPage> pages) {
        pageRepository.saveAll(pages);
    }

    @Override
    public List<WebPage> getAllBySite(long siteId) {
        return pageRepository.findAllBySiteId(siteId);
    }

    @Override
    public List<WebPage> getAllWebPages(List<Long> pageList) {
        return pageRepository.findAllByIdIn(pageList);
    }

    @Override
    public Long pageCount() {
        return pageRepository.count();
    }

    @Override
    public synchronized void resetPages(List<WebPage> pageList) {
        pageRepository.deleteAll(pageList);
    }
}
