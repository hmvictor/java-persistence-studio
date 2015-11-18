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
public class QueriesTest3 {
    private final String text;
    private final String expectedCode;

    public QueriesTest3(String text, String expectedQuery) {
        this.text = text;
        this.expectedCode = expectedQuery;
    }

    @Test
    public void testQueryExtraction(){
        assertThat(Queries.getJavaCodeFromQuery(text), is(expectedCode));
    }
    
    @Parameterized.Parameters
    public static Collection<Object[]> getData(){
        return Arrays.asList(
                new Object[]{"select p from Pelicula p", "\"select p from Pelicula p\""}, 
                new Object[]{"select p\n from Pelicula p", "\"select p\\n\"+\n\" from Pelicula p\""});
    }
    
}
