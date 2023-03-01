package SearchEngineApp.service.impl;

import SearchEngineApp.repository.IndexRepository;
import SearchEngineApp.models.Index;
import SearchEngineApp.models.Lemma;
import SearchEngineApp.service.IndexService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IndexServiceImpl implements IndexService
{
    private final IndexRepository indexRepository;

    public IndexServiceImpl(IndexRepository indexRepository) {
        this.indexRepository = indexRepository;
    }

    @Override
    public synchronized void saveIndex(Index index) {
        indexRepository.save(index);
    }

    @Override
    public synchronized void resetRanks(List<Lemma> lemmaList) {
        List<Long> lemmaIdList = new ArrayList<>();
        lemmaList.forEach(lemma -> lemmaIdList.add(lemma.getId()));
        List<Index> indexList = indexRepository.findAllByLemmaIdIn(lemmaIdList);
        indexRepository.deleteAll(indexList);
    }

    @Override
    public Index getIndex(long lemmaId, long pageId) {
        return indexRepository.findByLemmaIdAndPageId(lemmaId, pageId);
    }


    @Override
    public List<Long> getPages(long lemmaId) {
        List<Index> indexList = indexRepository.findAllByLemmaId(lemmaId);
        List<Long> pageIdList = new ArrayList<>();
        indexList.forEach(index -> pageIdList.add(index.getPageId()));
        return pageIdList;
    }

    @Override
    public List<Index> getIndexes(Lemma lemma, List<Long> pageList) {
        return indexRepository.findAllByLemmaIdAndPageIdIn(lemma.getId(), pageList);
    }


}
