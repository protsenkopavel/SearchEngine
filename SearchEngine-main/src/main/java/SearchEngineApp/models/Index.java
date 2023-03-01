package SearchEngineApp.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "Indexes")
public class Index implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "page_id")
    private long pageId;

    @Column(name = "lemma_id")
    private long lemmaId;

    @Column(name = "ranks")
    private float rank;

    public Index() {}

    public Index(long pageId, long lemmaId, float rank) {
        this.pageId = pageId;
        this.lemmaId = lemmaId;
        this.rank = rank;
    }
}
