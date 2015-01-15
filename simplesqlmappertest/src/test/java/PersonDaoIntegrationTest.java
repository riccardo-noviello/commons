
import com.riccardonoviello.simplesqlmappertest.dao.JobDao;
import com.riccardonoviello.simplesqlmappertest.dao.PersonDao;
import com.riccardonoviello.simplesqlmappertest.model.Address;
import com.riccardonoviello.simplesqlmappertest.model.Job;
import com.riccardonoviello.simplesqlmappertest.model.Person;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author novier
 */
public class PersonDaoIntegrationTest extends GenericIntegrationTest {

    @Autowired
    PersonDao personDao;

    @Autowired
    JobDao jobDao;

    @Before
    public void testAutowired() {
        assertNotNull(personDao);
        assertNotNull(jobDao);
    }

    @Test
    public void testInsertAndDelete() {
        Person p1 = new Person();
        p1.setFirstname("Bob");
        p1.setLastname("Sponge");
        p1.setAge(40);
        p1.setBirthday(new Date(15, 05, 1974));

        Long id = (Long) personDao.persist(p1);
        assertNotNull(id);

        // find a person through the api
        Person result = personDao.findByPrimaryKey(id);
        assertNotNull(result);
        assertEquals("Bob", result.getFirstname());
        assertEquals("Sponge", result.getLastname());

        // delete
        personDao.delete(result);
        Person newresult = personDao.findByPrimaryKey(id);
        assertNull(newresult);
    }

    @Test
    public void testUpdate() {
        Person p1 = new Person();
        p1.setFirstname("Andrew");
        p1.setLastname("Sponge");
        p1.setAge(40);
        p1.setBirthday(new Date(15, 05, 1970));

        Long id = (Long) personDao.persist(p1);
        assertNotNull(id);

        // find a person through the api
        Person result = personDao.findByPrimaryKey(id);
        assertNotNull(result);
        assertEquals("Andrew", result.getFirstname());
        assertEquals("Sponge", result.getLastname());
        assertTrue(result.getAge() == 40);

        // oops the age is wrong, better to update it
        result.setAge(44);
        personDao.persist(result);

        // let's find out if the change has been made
        Person updatedresult = personDao.findByPrimaryKey(id);
        assertNotNull(updatedresult);
        assertEquals("Andrew", result.getFirstname());
        assertEquals("Sponge", result.getLastname());
        assertTrue(updatedresult.getAge() == 44);

        // delete
        personDao.delete(updatedresult);

    }

    @Test
    public void testInsertWithOneToOneRelatioship() {
        Person p1 = new Person();
        p1.setFirstname("Bob");
        p1.setLastname("Sponge");
        p1.setAge(40);
        p1.setBirthday(new Date(15, 05, 1974));

        Address a1 = new Address();
        a1.setCountry("United Kingdom");
        a1.setHouseNumber("56");
        a1.setStreet("fake street");
        a1.setPostcode("AB12 5DZ");
        a1.setTown("GothamCity");

        p1.setAddress(a1);

        Long id = (Long) personDao.persist(p1);
        assertNotNull(id);

        // find a person through the api
        Person result = personDao.findByPrimaryKey(id);
        assertNotNull(result);
        assertEquals("Bob", result.getFirstname());
        assertEquals("Sponge", result.getLastname());
        assertNotNull(result.getAddress());
        assertNotNull(result.getAddressId());

        assertEquals(result.getAddress().getHouseNumber(), "56");
        assertEquals(result.getAddress().getStreet(), "fake street");
        assertEquals(result.getAddress().getTown(), "GothamCity");

        // delete person
        personDao.delete(result);
        Person newresult = personDao.findByPrimaryKey(id);
        assertNull(newresult);

        // we don't need to ensure the address has been deleted, this policy is up to the foreign key configuration
    }

    @Test
    public void testJobInsertion() {
        Job job = new Job();
        job.setTitle("Java developer");
        job.setStart(new Date(15, 06, 2014));
        
         Address a1 = new Address();
        a1.setCountry("United Kingdom");
        a1.setHouseNumber("99");
        a1.setStreet("unknown road");
        a1.setPostcode("CF10 4HU");
        a1.setTown("Cardiff");
        
        job.setWorkplace(a1);

        Long id = (Long) jobDao.persist(job);
        Job result = jobDao.findByPrimaryKey(id);
        assertNotNull(result);
        assertNotNull(result.getWorkplace());
        assertEquals(result.getWorkplace().getStreet(), "unknown road");

        // delete
        jobDao.delete(result);
        Job resultAfterDeletion = jobDao.findByPrimaryKey(id);
        assertNull(resultAfterDeletion);

    }

    @Test
    public void testOneToManyRelationship() {
        Person p1 = new Person();
        p1.setFirstname("Bobby");
        p1.setLastname("Marley");
        p1.setAge(20);
        p1.setBirthday(new Date(1994, 1, 2));
        
        List<Job> jobsList = new ArrayList<Job>();
        
        Job job1 = new Job();
        job1.setTitle("Java developer");
        job1.setStart(new Date(2014, 6, 15));
        jobsList.add(job1);
        
        Job job2 = new Job();
        job2.setTitle("Java EE developer");
        job2.setStart(new Date(2013, 9, 2));
        jobsList.add(job2);
        
        p1.setJobHistory(jobsList);
        
        Long personId = (Long) personDao.persist(p1);
        
        Person result = personDao.findByPrimaryKey(personId);
        assertNotNull(result);
        assertNotNull(result.getJobHistory());
        assertTrue(result.getJobHistory().size()==2);
        
        // delete person
        personDao.delete(result);
        // delete jobs
        for(Job job : result.getJobHistory()){
            jobDao.delete(job);
        }
    }
    
    @Test
    public void testNestedRelationship_oneToMany_oneToOne() {
        Person p1 = new Person();
        p1.setFirstname("Bobby");
        p1.setLastname("Marley");
        p1.setAge(20);
        p1.setBirthday(new Date(1994, 1, 2));
        
        List<Job> jobsList = new ArrayList<Job>();
        
        Job job1 = new Job();
        job1.setTitle("Java developer");
        job1.setStart(new Date(2014, 6, 15));
        
        Address add1 = new Address();
        add1.setPostcode("NP12 345");
        add1.setHouseNumber("45");
        add1.setStreet("Stow Hill");
        add1.setTown("Newport");
        add1.setCountry("United Kingdom");
        job1.setWorkplace(add1);
        jobsList.add(job1);
        
        Job job2 = new Job();
        job2.setTitle("Java EE developer");
        job2.setStart(new Date(2013, 9, 2));
        
        Address add2 = new Address();
        add2.setPostcode("NP99 555");
        add2.setHouseNumber("105");
        add2.setStreet("Cardiff Road");
        add2.setTown("Newport");
        add2.setCountry("United Kingdom");
        job2.setWorkplace(add2);
        jobsList.add(job2);
        
        p1.setJobHistory(jobsList);
        
        Long personId = (Long) personDao.persist(p1);
        
        Person result = personDao.findByPrimaryKey(personId);
        assertNotNull(result);
        assertNotNull(result.getJobHistory());
        assertTrue(result.getJobHistory().size()==2);
        
        assertEquals(result.getJobHistory().get(0).getWorkplace().getStreet(), "Stow Hill");
        assertEquals(result.getJobHistory().get(1).getWorkplace().getStreet(), "Cardiff Road");
        
        // delete person
        personDao.delete(result);
        // delete jobs
        for(Job job : result.getJobHistory()){
            jobDao.delete(job);
        }
    }
    
    @Test
    public void daoFunctionalTest(){
        List<Person> persons = personDao.findAll();
        
    }
}
