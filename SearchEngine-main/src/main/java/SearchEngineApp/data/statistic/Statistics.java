package SearchEngineApp.data.statistic;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Statistics
{
    private Total total;
    private List<Detailed> detailed = new ArrayList<>();

    public Statistics(){}

    public void setDetailed(Detailed detail) {
        detailed.add(detail);
    }
}
