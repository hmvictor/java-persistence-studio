package jpql.studio;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.openide.util.Exceptions;

/**
 *
 * @author Victor
 */
public class EntityManagerFactorySuplier implements Supplier<EntityManagerFactory>{
    private final ClassLoader classloader;
    private final String unitName;
    private final DatabaseConnection connection;

    public EntityManagerFactorySuplier(ClassLoader classloader, String unitName, DatabaseConnection connection) {
        this.classloader=classloader;
        this.unitName=unitName;
        this.connection=connection;
    }

    @Override
    public EntityManagerFactory get() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classloader);
        Map<String, String> map = new HashMap();
        map.put("javax.persistence.jdbc.url", connection.getDatabaseURL());
        map.put("javax.persistence.jdbc.password", connection.getUser());
        map.put("javax.persistence.jdbc.user", connection.getPassword());
        map.put("javax.persistence.schema-generation.database.action", "none");
        try {
//            Class<?> persistenceClass = classloader.loadClass("javax.persistence.Persistence");
//            Method method=persistenceClass.getMethod("createEntityManagerFactory", String.class, Map.class);
//            System.out.println(method.invoke(null, unitName, map).getClass().getClassLoader());
//            System.out.println(EntityManagerFactory.class.getClassLoader());
//            return (EntityManagerFactory) method.invoke(null, unitName, map);
            return Persistence.createEntityManagerFactory(unitName, map);
        /*} catch (ClassNotFoundException | IllegalArgumentException | SecurityException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        } */}finally{
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }
    
}
