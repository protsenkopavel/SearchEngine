package SearchEngineApp.utils;

import SearchEngineApp.data.search.Data;
import SearchEngineApp.data.search.SearchData;
import SearchEngineApp.models.*;
import SearchEngineApp.service.IndexService;
import SearchEngineApp.service.LemmaService;
import SearchEngineApp.service.SiteService;
import SearchEngineApp.service.WebPageService;
import SearchEngineApp.service.response.FalseResponse;
import SearchEngineApp.service.response.Response;
import SearchEngineApp.service.response.SearchResponse;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class SearchTextUtil {

    private static final Logger log = Logger.getLogger(SearchTextUtil.class);
    private static IndexService indexService;
    private static WebPageService webPageService;
    private static LemmaService lemmaService;
    private static SiteService siteService;

    public SearchTextUtil (IndexService indexService, WebPageService webPageService, LemmaService lemmaService, SiteService siteService) {
        SearchTextUtil.indexService = indexService;
        SearchTextUtil.webPageService = webPageService;
        SearchTextUtil.lemmaService = lemmaService;
        SearchTextUtil.siteService = siteService;
    }

    public static Response startSearch(String text, Site site) throws IOException {
        long startTime = System.currentTimeMillis();
        List<Lemma> lemmas = getLemmas(text, site);
        if(lemmas == null) {
            log.warn("Ошибка поиска. Некорректный запрос : '" + text + "'");
            return new FalseResponse("Некорректный запрос");
        } else {
            List<Long> pages = getPages(lemmas);
            if(pages.size() == 0) {
                log.warn("Поиск по запросу : '" + text + "' не дал результатов");
                return new FalseResponse("Поиск не дал результатов");
            } else {
                List<SearchPage> searchPages = getSearchPages(lemmas, pages, text);
                List<Data> dataList = new ArrayList<>();
                for(SearchPage searchPage : searchPages) {
                    Data data = new Data();
                    data.setSite(searchPage.getWebPage().getSite().getUrl());
                    data.setSiteName(searchPage.getWebPage().getSite().getName());
                    data.setUri(searchPage.getWebPage().getPath());
                    data.setTitle(searchPage.getTitle());
                    data.setSnippet(searchPage.getSnippet());
                    data.setRelevance(searchPage.getRelevance());
                    dataList.add(data);
                }
                log.info("Количество страниц по поисковому запросу '" + text + "' = " + searchPages.size() + " стр.\n" +
                        "Затраченное время на поиск = " + (System.currentTimeMillis() - startTime) / 1000 + "сек.");
                return new SearchResponse(searchPages.size(), dataList);
            }
        }
    }

    private static List<SearchPage> getSearchPages (List<Lemma> searchLemmas, List<Long> searchPages, String searchText) throws IOException {
        List<SearchPage> searchPageList = new ArrayList<>();
        List<Index> indexList = indexService.getIndexes(searchLemmas.get(searchLemmas.size()-1), searchPages);
        List<WebPage> webPageList = webPageService.getAllWebPages(searchPages);

        float maxRelevance = 0;
        for (WebPage page : webPageList) {
            float rank = 0;
            for (Lemma lemma : searchLemmas) {
                for(Index index: indexList) {
                    if(index.getPageId() == page.getId() && index.getLemmaId() == lemma.getId()) {
                        rank += index.getRank();
                    }
                }
            }
            SearchPage searchPage = new SearchPage();
            searchPage.setWebPage(page);
            searchPage.setAbsoluteRelevance(rank);

            Document doc = Jsoup.parse(searchPage.getWebPage().getContent());

            String titleText = doc.getElementsByTag("title").text();
            String bodyText = doc.getElementsByTag("body").text();

            searchPage.setTitle(getSnippet(searchText, titleText));
            searchPage.setSnippet(getSnippet(searchText, bodyText));

            searchPageList.add(searchPage);
            maxRelevance = Math.max(maxRelevance, rank);
        }

        List<SearchPage> searchPagesSort = new ArrayList<>();
        for (SearchPage searchPage : searchPageList) {
            searchPage.setRelevance(searchPage.getAbsoluteRelevance() / maxRelevance);
            if (searchPagesSort.isEmpty()) {
                searchPagesSort.add(searchPage);
            } else {
                for (int i = 0; i < searchPagesSort.size(); i++) {
                    if (searchPagesSort.get(i).getRelevance() < searchPage.getRelevance()) {
                        searchPagesSort.add(i, searchPage);
                        break;
                    } else if (i == searchPagesSort.size() - 1) {
                        searchPagesSort.add(searchPage);
                        break;
                    }
                }
            }
        }
        return searchPagesSort;
    }

    private static List<Lemma> getLemmas (String text, Site site) throws IOException {
        List<Lemma> searchLemmas = new ArrayList<>();
        List<String> lemmaNames = new ArrayList<>();
        List<Lemma> lemmaList;

        HashMap<String,Integer> map = CreateLemmasUtil.createLemmasWithCount(text);
        if(map.size() > 0) {
            for(Map.Entry<String, Integer> entry : map.entrySet()) {
                lemmaNames.add(entry.getKey());
            }
            if(site == null) {
                lemmaList = lemmaService.getLemmas(lemmaNames);
            } else {
                lemmaList = lemmaService.getLemmasFromSite(lemmaNames, site);
            }
            for (Lemma lemma: lemmaList) {
                if (lemma == null) {
                    searchLemmas = null;
                    break;
                } else if (searchLemmas.isEmpty()) {
                    searchLemmas.add(lemma);
                } else {
                    for (int i = 0; i < searchLemmas.size(); i++) {
                        if (searchLemmas.get(i).getFrequency() > lemma.getFrequency()) {
                            searchLemmas.add(i, lemma);
                            break;
                        } else if (i == searchLemmas.size() - 1) {
                            searchLemmas.add(lemma);
                            break;
                        }
                    }
                }
            }
        }
        else {
            searchLemmas = null;
        }
        return searchLemmas;
    }

    private static List<Long> getPages (List<Lemma> searchLemmas) {
        List<Long> searchPages = new ArrayList<>();
            for (Lemma lemma : searchLemmas) {
                List<Long> pages = indexService.getPages(lemma.getId());
                if (searchPages.isEmpty()) {
                    searchPages.addAll(pages);
                } else {
                    searchPages.removeIf(pageId -> !pages.contains(pageId));
                    if (searchPages.isEmpty()) {
                        break;
                    }
                }
            }
        return searchPages;
    }

    private static String getSnippet (String searchText, String mainText) throws IOException {
        mainText = mainText.toLowerCase().replace('ё', 'е');
        String[] textArray = mainText.split("(?!^)\\b");
        TreeMap<Integer,List<String>> lemmasIndexes = CreateLemmasUtil.getIndexLemmas(textArray);
        List<List<String>> lemmasFromText = CreateLemmasUtil.createLemmas(searchText);

        List<Integer> indexes = new ArrayList<>();
        for(List<String> lemmasSearch : lemmasFromText) {
            for(Map.Entry<Integer,List<String>> lemmasDoc : lemmasIndexes.entrySet()) {
                if(lemmasSearch.equals(lemmasDoc.getValue())) {
                    indexes.add(lemmasDoc.getKey());
                }
            }
        }
        StringBuilder builder = new StringBuilder();
        if(indexes.size() > 0) {
            Collections.sort(indexes);
            TreeMap<Integer, Integer> startEnd = getStartEndIndexes(indexes);
            for (Map.Entry<Integer,Integer> entry : startEnd.entrySet()) {
                builder.append(getBoldText(entry.getKey(), entry.getValue(), textArray, indexes));
            }
        }
        else {
            builder.append(mainText);
        }
        return builder.toString();
    }

    private static TreeMap<Integer, Integer> getStartEndIndexes (List<Integer> indexes) {
        TreeMap<Integer, Integer> startEnd = new TreeMap<>();
        int start = 0;
        int end = 0;
        for(int i = 0; i < indexes.size(); i++) {
            if(start == 0 && end == 0) {
                start = indexes.get(i);
                end = indexes.get(i);
            }
            if (i + 1 < indexes.size()) {
                if (indexes.get(i + 1) - indexes.get(i) <= 30) {
                    end = indexes.get(i + 1);
                }
                else {
                    startEnd.put(start, end);
                    start = 0;
                    end = 0;
                }
            } else {
                startEnd.put(start,end);
                start = 0;
                end = 0;
            }
        }
        return startEnd;
    }

    private static String getBoldText (int start, int end, String[] stringArray, List<Integer> indexes) {
        int startIndex = Math.max(start - 30, 0);
        int endIndex = Math.min(end + 20, stringArray.length);
        for (int i = start - 1; i > startIndex; i--) {
            if (stringArray[i].matches("[.!?;>\"«»\\-_–]+\\s*") && stringArray[i + 1].matches("^[A-ZА-ЯЁ][a-zа-яё]*")) {
                startIndex = i + 1;
                break;
            }
        }
        for (int i = end + 1; i < endIndex; i++) {
            if (stringArray[i].matches(".*\\.\\s*") && !stringArray[i+1].matches("[а-яё]")){
                endIndex = i + 1;
                break;
            }
        }
        StringBuilder builder = new StringBuilder();
        List<Integer> indexlist = new ArrayList<>();
        for (Integer index : indexes) {
            if (index >= startIndex && index <= endIndex) {
                indexlist.add(index);
            }
        }

        if(startIndex == start - 30) {
            builder.append("...");
        }
        for(Integer index : indexlist) {
            for (int i = startIndex; i < index; i++) {
                builder.append(stringArray[i]);
            }
            builder.append("<b>");
            builder.append(stringArray[index]);
            builder.append("</b>");
            startIndex = index+1;
        }
        for (int i = startIndex; i < endIndex; i++) {
            builder.append(stringArray[i]);
        }
        if(endIndex == end + 20) {
            builder.append("... ");
        }
        return builder.toString();
    }

}
