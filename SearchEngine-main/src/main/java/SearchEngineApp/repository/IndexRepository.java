package SearchEngineApp.repository;

import SearchEngineApp.models.Index;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndexRepository extends CrudRepository<Index, Long>
{
    List<Index> findAllByLemmaIdIn(List<Long> lemmaIdList);
    Index findByLemmaIdAndPageId(long lemmaId, long pageId);
    List<Index> findAllByLemmaId(long lemmaId);
    List<Index> findAllByLemmaIdAndPageIdIn(long lemmaId, List<Long> pageIdList);
}
