
import com.riccardonoviello.simplesqlmappertest.config.MainApp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Spring Boot Integration Test Configuration
 * @author novier
 */
@RunWith(SpringJUnit4ClassRunner.class) 
@SpringApplicationConfiguration(classes = MainApp.class) 
@WebAppConfiguration 
@IntegrationTest("server.port:0")
public class GenericIntegrationTest {

    @Test
    public void appContextLoaderTest(){
    }
}
