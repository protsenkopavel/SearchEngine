package SearchEngineApp.service;

import SearchEngineApp.models.Index;
import SearchEngineApp.models.Lemma;
import SearchEngineApp.models.Site;

import java.util.List;

public interface IndexService
{
    void saveIndex(Index index);
    Index getIndex(long lemmaId, long pageId);
    List<Long> getPages(long lemmaId);
    List<Index> getIndexes(Lemma lemma, List<Long> pageList);
    void resetRanks (List<Lemma> lemmaList);
}
