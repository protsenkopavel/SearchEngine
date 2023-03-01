package SearchEngineApp.data.statistic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Total
{
    private long sites;
    private long pages;
    private long lemmas;
    private boolean isIndexing;

    public Total() {}
}
