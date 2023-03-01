package SearchEngineApp.repository;

import SearchEngineApp.models.Field;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldRepository extends CrudRepository<Field,Integer>
{

}
