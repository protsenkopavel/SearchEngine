package SearchEngineApp.data.statistic;

import SearchEngineApp.models.Status;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Detailed
{
    private String url;
    private String name;
    private Status status;
    private Date statusTime;
    private String error;
    private long pages;
    private long lemmas;

    public Detailed(){}

}
