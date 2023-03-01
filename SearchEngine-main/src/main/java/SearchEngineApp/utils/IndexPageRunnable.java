package SearchEngineApp.utils;

import SearchEngineApp.models.*;
import SearchEngineApp.service.IndexService;
import SearchEngineApp.service.LemmaService;
import SearchEngineApp.service.SiteService;
import SearchEngineApp.service.impl.IndexingServiceImpl;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Component
public class IndexPageRunnable implements Runnable{

    private static final Logger log = Logger.getLogger(IndexPageRunnable.class);

    private WebPage webPage;
    private List<Field> fieldList;

    private SiteService siteService;
    private LemmaService lemmaService;
    private IndexService indexService;

    public IndexPageRunnable(SiteService siteService, LemmaService lemmaService, IndexService indexService) {
        this.siteService = siteService;
        this.lemmaService = lemmaService;
        this.indexService = indexService;
    }

    public void setWebPage(WebPage webPage) {
        this.webPage = webPage;
    }

    public void setFieldList(List<Field> fieldList) {
        this.fieldList = fieldList;
    }

    @Override
    public void run() {
        try {
                Site site = siteService.getSite(webPage.getSite().getUrl());
                site.setStatusTime(new Date());
                siteService.saveSite(site);
                List<String> lemmaList = new ArrayList<>();
                Document doc = Jsoup.parse(webPage.getContent());
                for (Field field : fieldList) {
                    Elements el = doc.getElementsByTag(field.getSelector());
                    for (Map.Entry<String, Integer> entry : CreateLemmasUtil.createLemmasWithCount(el.eachText().get(0)).entrySet()) {
                        if(IndexingServiceImpl.isRun) {
                            if (!lemmaList.contains(entry.getKey())) {
                                lemmaList.add(entry.getKey());
                                Lemma lemma = new Lemma(entry.getKey(), 1, webPage.getSite());
                                lemma = lemmaService.saveLemma(lemma);
                                Index index = new Index(webPage.getId(),lemma.getId(), field.getWeight() * entry.getValue());
                                indexService.saveIndex(index);
                            } else {
                                Lemma lemma = lemmaService.getLemma(entry.getKey(), webPage.getSite().getId());
                                Index index = indexService.getIndex(lemma.getId(), webPage.getId());
                                float rank = index.getRank();
                                index.setRank(rank + entry.getValue() * field.getWeight());
                                indexService.saveIndex(index);
                            }
                        }
                        else {
                            throw new Exception("Процесс остановлен пользователем");
                        }
                    }
                }
        } catch (Exception iex) {
            log.error("Ошибка при индексации страницы: '" + webPage.getSite().getUrl() + webPage.getPath() +
                     "' : " + iex.getMessage());
            iex.printStackTrace();
        }
    }
}
