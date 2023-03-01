package SearchEngineApp.data.search;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchData {

    private String query;
    private String site;
    private Integer offset;
    private Integer limit;

}
