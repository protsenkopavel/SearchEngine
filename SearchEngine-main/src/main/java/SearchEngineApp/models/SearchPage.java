package SearchEngineApp.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchPage
{
    private WebPage webPage;
    private String title;
    private String snippet;
    private float absoluteRelevance;
    private double relevance;

    public SearchPage() {}
}

