package jpql.studio;

import java.beans.Beans;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Victor
 */
public class QueryResult {
    private final long time;
    private final List<? extends Object> resultList;
    private Class[] types;
    private boolean isArray;

    public QueryResult(long time, List<? extends Object> resultList) {
        this.time = time;
        this.resultList = resultList;
        if(!resultList.isEmpty()) {
            Object row=resultList.get(0);
            if(row instanceof Object[]){
                isArray=true;
                Object[] array=(Object[])row;
                types=new Class[array.length];
                for(int i=0; i < array.length; i++) {
                    types[i]=array[i].getClass();
                }
            }else{
                types=new Class[]{row.getClass()};
            }
        }
    }
    
    public boolean containsArrays(){
        return isArray;
    }
    
    public boolean isEmpty(){
        return resultList.isEmpty();
    }
        
    public Class[] getTypes(){
        return types;
    }
    
    public long getTime() {
        return time;
    }

    public List<? extends Object> getResultList() {
        return resultList;
    }
    
}
