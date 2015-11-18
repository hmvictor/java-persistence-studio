package jpql.studio;

import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Victor
 */
public class QueryResultModel extends AbstractTableModel {
    private final QueryResult queryResult;
    private final int columnCount;

    public QueryResultModel(QueryResult queryResult) {
        this.queryResult=queryResult;
        //TODO  getTypes==null
        columnCount=queryResult.getTypes().length;
    }

    @Override
    public int getRowCount() {
        return queryResult.getResultList().size();
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return String.valueOf(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return queryResult.getTypes()[columnIndex];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(queryResult.containsArrays()){
            return ((Object[])queryResult.getResultList().get(rowIndex))[columnIndex];
        }else{
            if(columnIndex > 0){
                throw new IllegalArgumentException();
            }
            return queryResult.getResultList().get(rowIndex);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

}
