package SearchEngineApp.config;

import SearchEngineApp.data.sites.SitesData;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "config")
public class SearchConfig
{
    private String webinterface;
    private String agent;
    private List<SitesData> sites;

}
