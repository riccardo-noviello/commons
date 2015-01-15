
import com.riccardonoviello.simplesqlmapper.core.QueryUtils;
import com.riccardonoviello.simplesqlmappertest.model.Job;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author novier
 */
public class QueryUtilsTest {

    @Test
    public void parseQueryInsert() {
        String expected = "INSERT INTO persons (firstname, lastname, birthdate, age ) VALUES (?, ?, ?, ?) ";
        String query = "INSERT INTO persons (firstname, lastname, birthdate, age, , ) VALUES (?, ?, ?, ?) ";
        query = QueryUtils.removeUnusedCommas(query);
        System.out.println(query);
        assertEquals(expected, query);
    }

    @Test
    public void parseQueryUpdate_doubleCommas() {
        String expected = " UPDATE persons SET firstname=?, lastname=?, birthdate=?, age=? WHERE id = ?";
        String query = " UPDATE persons SET firstname=?, lastname=?, birthdate=?, age=?, ,  WHERE id = ?";
        query = QueryUtils.removeUnusedCommas(query);
        System.out.println(query);
        assertEquals(expected, query);
    }
    
    @Test
    public void parseQueryUpdate_oneComma() {
        String expected = " UPDATE persons SET firstname=?, lastname=?, birthdate=?, age=? WHERE id = ?";
        String query = " UPDATE persons SET firstname=?, lastname=?, birthdate=?, age=?,   WHERE id = ?";
        query = QueryUtils.removeUnusedCommas(query);
        System.out.println(query);
        assertEquals(expected, query);
    }

    @Test
    public void enclosedClassExtractionTest() {

        List<Job> integers = new ArrayList<Job>();
        String clazz = findEnclosedClass(integers.getClass());
        System.out.println(clazz);
        assertEquals(Integer.class, clazz);

    }

    public String findEnclosedClass(Class<?> o) {
        try {
            Type type = o.asSubclass(o);
            if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            System.out.println("raw type: " + pt.getRawType());
            System.out.println("owner type: " + pt.getOwnerType());
            System.out.println("actual type args:");
            for (Type t : pt.getActualTypeArguments()) {
                System.out.println("    " + t);
            }
        }
               
        } catch (SecurityException ex) {
          
        }
          return null;
    }
    

}
