
package jpql.studio;

import java.util.List;
import javax.persistence.Query;

/**
 *
 * @author Victor
 */
public class QueryExecutor {
    private final Query query;
    private final int firstResult;
    private final int maxResults;

    public QueryExecutor(Query query) {
        this(query, 0, Integer.MAX_VALUE);
    }
    
    public QueryExecutor(Query query, int firstResult, int maxResults) {
        this.query = query;
        this.firstResult=firstResult;
        this.maxResults=maxResults;
    }
    
    public QueryResult getResult(){
        long startTime=System.currentTimeMillis();
        if(firstResult != 0){
            query.setFirstResult(firstResult);
        }
        if(maxResults < Integer.MAX_VALUE){
            query.setMaxResults(maxResults);
        }
        List resultList = query.getResultList();
        long time=System.currentTimeMillis() - startTime;
        return new QueryResult(time, resultList);
    }
    
}
