package SearchEngineApp.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.concurrent.CopyOnWriteArraySet;

@Getter
@Setter
@Entity
@Table(name = "Page")
public class WebPage implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "path", columnDefinition = "text")
    private String path;

    @Column(name = "code")
    private int code;

    @Column(name = "content", columnDefinition = "mediumtext")
    private String content;

    @ManyToOne
    @JoinColumn(name = "site_id", referencedColumnName = "id")
    @JsonIgnore
    private Site site;

    @Transient
    private CopyOnWriteArraySet<String> urlList = new CopyOnWriteArraySet<>();

    public void setUrlList(String string) {
        urlList.add(string);
    }

    public  WebPage() {}

    public WebPage(String path, Site site) {
        this.path = path;
        this.site = site;
    }
}
