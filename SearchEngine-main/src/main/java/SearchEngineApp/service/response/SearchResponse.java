package SearchEngineApp.service.response;

import SearchEngineApp.data.search.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SearchResponse implements Response {

    private boolean result = true;
    private int count;
    private int offset = 0;
    private int limit = 5;
    private List<Data> data;

    public SearchResponse(int count, List<Data> data) {
        this.count = count;
        this.data = data;
    }

    @Override
    public boolean getResult() {
        return result;
    }

}
