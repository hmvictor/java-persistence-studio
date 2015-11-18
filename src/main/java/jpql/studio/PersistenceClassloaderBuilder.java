package jpql.studio;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;

/**
 *
 * @author Victor
 */
public class PersistenceClassloaderBuilder {

    /**
     * Gets the classloader for this project.
     * @param project
     * @return 
     */
    public ClassLoader getClassloader(Project project, ClassLoader parent){
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        //get main sources
        if(sourceGroups != null && sourceGroups.length > 0){
            ClassPath classPath = ClassPath.getClassPath(sourceGroups[0].getRootFolder(), ClassPath.EXECUTE);
            if(classPath == null) {
                throw new IllegalArgumentException("No classloader for project");
            }
            List<URL> urls = classPath.entries().stream().map(entry -> entry.getURL()).collect(Collectors.toList());
            ClassLoader classloader=new URLClassLoader(urls.toArray(new URL[0]), parent);
            return classloader;
        }else{
            throw new IllegalArgumentException("No classloader for project");
        }
    }
    
}
