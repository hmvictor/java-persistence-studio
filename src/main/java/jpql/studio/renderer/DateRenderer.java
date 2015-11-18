package jpql.studio.renderer;

import java.text.DateFormat;
import java.util.Date;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Victor
 */
public class DateRenderer extends DefaultTableCellRenderer{

    @Override
    protected void setValue(Object value) {
        setText(value == null ? "": DateFormat.getInstance().format((Date)value));
    }
    
}
