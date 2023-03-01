package SearchEngineApp.service;

import SearchEngineApp.models.Field;
import java.util.List;

public interface FieldService
{
    void saveField(Field field);
    List<Field> getFields();
    long getSize();

}
