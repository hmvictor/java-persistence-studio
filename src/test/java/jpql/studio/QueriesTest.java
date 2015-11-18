package jpql.studio;

import java.util.Arrays;
import java.util.Collection;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Victor
 */
@RunWith(Parameterized.class)
public class QueriesTest {
    private final String text;
    private final int position;
    private final String expectedQuery;

    public QueriesTest(String text, int position, String expectedQuery) {
        this.text = text;
        this.position = position;
        this.expectedQuery = expectedQuery;
    }

    @Test
    public void testQueryExtraction(){
        assertThat(Queries.getQuery(text, position), is(expectedQuery));
    }
    
    @Parameterized.Parameters
    public static Collection<Object[]> getData(){
        return Arrays.asList(
                new Object[]{"select p from Pelicula p", 5, "select p from Pelicula p"}, 
                new Object[]{";select p from Pelicula p;", 5, "select p from Pelicula p"},
                new Object[]{";select p from Pelicula p", 5, "select p from Pelicula p"},
                new Object[]{"select p from Pelicula p;", 5, "select p from Pelicula p"},
                new Object[]{"", 0, ""},
                new Object[]{";", 0, ""},
                new Object[]{";;", 1, ""});
    }
    
    
}
