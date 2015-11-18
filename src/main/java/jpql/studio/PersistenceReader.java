package jpql.studio;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.netbeans.api.project.Project;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Victor
 */
public class PersistenceReader {
    
    /**
     * First, try to read from binary output, then from source.
     * @param project 
     */
    public List<String> getPersistenceUnitNames(Project project, ClassLoader classLoader) {
        try {
            //        FileObject[] sourceDirs=project.getSourcesDirs();
//        FileObject[] binaryDirs=project.getBinaryDirs();
//        for (FileObject dir : sourceDirs) {
//            FileObject persistenceFile = dir.getFileObject("META-INF/persistence.xml");
//            if(persistenceFile != null) {
//                
//            }
//        }
            
            List<String> unitnames = new LinkedList<>();
            Enumeration<URL> resources = classLoader.getResources("META-INF/persistence.xml");
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(resource.openStream());
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                XPathExpression expr = xpath.compile("persistence/persistence-unit/@name");
                NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                for (int i = 0; i < nl.getLength(); i++) {
                    Node node = nl.item(i);
                    unitnames.add(node.getNodeValue());
                }
            }
            return unitnames;
        } catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException ex) {
            throw new PersistenteReaderException(ex);
        }
    }
    
    public ComboBoxModel<String> getPersistenceUnitnamesModel(Project project, ClassLoader classLoader)  {
        DefaultComboBoxModel<String> model=new DefaultComboBoxModel<>();
        //TODO when file changes reload model
        getPersistenceUnitNames(project, classLoader).forEach((unitname)-> {
            model.addElement(unitname);
        });
//        FileChangeListener fileChangeListener=new FileChangeAdapter(){
//            
//            public void change(){
//                if(outDir is changed) {
//                    model.removeAllElements();
//                    getPersistenceUnitNames(project).forEach((unitname)-> {
//                        model.addElement(unitname);
//                    });
//                }
//            }
//
//            @Override
//            public void fileFolderCreated(FileEvent fe) {
//                if(fe.getFile().getName().equals(outputDir)){
//                    
//                }
//            }
//            
//        };
//        project.getProjectDirectory().addFileChangeListener(fileChangeListener);
//        throw new UnsupportedOperationException("Not yet implemented");
        return model;
    }
    
}
