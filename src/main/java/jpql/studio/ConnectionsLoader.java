
package jpql.studio;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;

/**
 *
 * @author Victor
 */
public class ConnectionsLoader {
    
    private void loadConnections(DefaultComboBoxModel<DatabaseConnection> model){
        for (DatabaseConnection connection : ConnectionManager.getDefault().getConnections()) {
            model.addElement(connection);
        }
    }
    
    public ComboBoxModel<DatabaseConnection> createConnectionsModel() {
        DefaultComboBoxModel<DatabaseConnection> model=new DefaultComboBoxModel<>();
        ConnectionManager.getDefault().addConnectionListener(() -> {
            model.removeAllElements();
            loadConnections(model);
        });
        loadConnections(model);
        return model;
    }
    
}
