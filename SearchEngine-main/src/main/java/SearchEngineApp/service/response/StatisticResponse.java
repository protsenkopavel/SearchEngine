package SearchEngineApp.service.response;

import SearchEngineApp.data.statistic.Statistics;
import SearchEngineApp.service.response.Response;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class StatisticResponse implements Response
{
    private boolean result;
    private Statistics statistics;


    public StatisticResponse(boolean result, Statistics statistics) {
        this.result = result;
        this.statistics = statistics;
    }

    @Override
    public boolean getResult() {
        return result;
    }
}
