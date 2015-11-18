package jpql.studio;

import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Victor
 */
public class QueryCreator {
    private final EntityManager entityManager;
    private final String queryContent;

    public QueryCreator(EntityManager entityManager, String queryContent) {
        Objects.requireNonNull(entityManager, "entityManager is null");
        Objects.requireNonNull(queryContent, "queryContent is null");
        this.entityManager = entityManager;
        this.queryContent = queryContent;
    }
    
    public Query create(){
        return entityManager.createQuery(queryContent);
    }
    
}
