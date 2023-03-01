package SearchEngineApp.service.impl;

import SearchEngineApp.repository.FieldRepository;
import SearchEngineApp.models.Field;
import SearchEngineApp.service.FieldService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FieldServiceImpl implements FieldService
{

    private final FieldRepository fieldRepository;

    public FieldServiceImpl(FieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
    }

    @Override
    public synchronized void saveField(Field field) {
        fieldRepository.save(field);
    }

    @Override
    public List<Field> getFields() {
        List<Field> fieldList = new ArrayList<>();
        Iterable<Field> fields = fieldRepository.findAll();
        fields.forEach(fieldList::add);
        return fieldList;
    }

    @Override
    public long getSize() {
        return fieldRepository.count();
    }

}
