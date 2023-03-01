package SearchEngineApp.repository;

import SearchEngineApp.models.WebPage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebPageRepository extends CrudRepository<WebPage,Long>
{
    List<WebPage> findAllBySiteId(long idSite);
    List<WebPage> findAllByIdIn(List<Long> pageList);

}
