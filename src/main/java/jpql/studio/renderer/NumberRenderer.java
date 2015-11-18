package jpql.studio.renderer;

import java.text.NumberFormat;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Victor
 */
public class NumberRenderer extends DefaultTableCellRenderer{
    private final NumberFormat numberFormat;

    public NumberRenderer(NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
        setVerticalAlignment(DefaultTableCellRenderer.RIGHT);
    }

    @Override
    protected void setValue(Object value) {
        setText(value == null? "": numberFormat.format((Number)value));
    }
    
}
