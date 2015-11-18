package jpql.studio;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 *
 * @author Victor
 */
public class PositiveIntegerVerifier extends InputVerifier{

    @Override
    public boolean verify(JComponent input) {
        String text = ((JTextField)input).getText();
        if(text.trim().isEmpty()) {
            return true;
        }
        try{
            Integer.parseInt(text);
            return true;
        }catch(NumberFormatException ex) {
            return false;
        }
    }
    
}
