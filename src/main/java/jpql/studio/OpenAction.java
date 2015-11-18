package jpql.studio;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLClassLoader;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Tools",
        id = "jpql.studio.OpenAction"
)
@ActionRegistration(
        displayName = "#CTL_OpenAction"
)
@Messages("CTL_OpenAction=Open JQPL Editor")
@ActionReferences(value = {
    @ActionReference(path = "Projects/Actions", position = 8962, separatorBefore = 8956, separatorAfter = 8968),
    @ActionReference(path = "Menu/Tools", position = 8962, separatorBefore = 8956, separatorAfter = 8968)})
public final class OpenAction implements ActionListener {

    private final Project context;

    public OpenAction(Project context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
//        ClassLoader projectClassloader = new PersistenceClassloaderBuilder().getClassloader(context);
//        ClassLoader classLoader=new URLClassLoader(new URL[0], projectClassloader);
        QueryEditorTopComponent component = new QueryEditorTopComponent(context);
        component.open();
        component.requestActive();
    }
}
