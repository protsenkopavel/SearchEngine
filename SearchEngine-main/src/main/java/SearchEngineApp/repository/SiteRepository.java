package SearchEngineApp.repository;

import SearchEngineApp.models.Site;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteRepository extends CrudRepository<Site,Long> {

    Site findByUrl(String url);
    @Query(value = "SELECT count(s) FROM Site s WHERE s.status = 'INDEXING'")
    long countByStatusIndexing();
    @Query(value = "SELECT s FROM Site s WHERE s.status = 'INDEXING'")
    List<Site> findAllByStatusIndexing();
    @Query(value = "SELECT count(s) FROM Site s WHERE s.status = 'FAILED'")
    long countByStatusFailed();
}
