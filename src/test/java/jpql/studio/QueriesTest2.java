package jpql.studio;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 *
 * @author Victor
 */
@RunWith(Parameterized.class)
public class QueriesTest2 {
    private final String text;
    private final String expectedQuery;

    public QueriesTest2(String text, String expectedQuery) {
        this.text = text;
        this.expectedQuery = expectedQuery;
    }

    @Test
    public void testQueryExtraction(){
        assertThat(Queries.getQueryFromJavaCode(text), is(expectedQuery));
    }
    
    @Parameterized.Parameters
    public static Collection<Object[]> getData(){
        return Arrays.asList(
                new Object[]{"\"select p from Pelicula p\"", "select p from Pelicula p"}, 
                new Object[]{"\"select p\"+\" from Pelicula p\"", "select p from Pelicula p"},
                new Object[]{"\"select p\" +\"\\n\"+ \" from Pelicula p\"", "select p\n from Pelicula p"},
                new Object[]{"select p from Pelicula p", "select p from Pelicula p"},
                new Object[]{"", ""});
    }
    
}
