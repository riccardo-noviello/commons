# Commons projects

<h3>Description</h3>
<p>This repository contains common libraries. This means they are individual projects made with the intention to be re-usable in other projects.</p>
<p>These projects include:</p>
<table>
<tr>
<td>simpleimapclient</td><td>The project exposes some basic features to connect to an IMAP Mail server and read, send emails</td>
</tr>
<tr>
<td>simplesqlmapper</td><td>The Simple SQL mapper is a work is a simple SQL ORM</td>
</tr>
<tr>
<td>simplesqlmappertest</td><td>This is a Spring Boot app containing the tests for the Simple SQL mapper ORM</td>
</tr>
<tr>
<td>zipencrypt</td><td>This is just an utility class I used in the past to Zip, unZip files and folders. It can also perform a simple AES-256 encryption</td>
</tr>
</table>

<h3>Java SQL ORM</h3>
<b>How it works:</b>
<p>
It works very simalarly to Hibernate. <b>First</b>, we start by defining a Pojo, for example <b>"Person"</b>:
</p></br>

<code>
// The Entity describes the table name "person" on our schema "sqlmapper"
@Entity(name="sqlmapper.persons")
public class Person {

    // The column name must be specified here, as well as specifying the primary key
    @Column(name="id", primary=true)
    private Long id;
    
    @Column(name="firstname")
    private String firstname;
    
    // ... more attributes
    
    // One To One relationship: the Address is stored somewhere else and we have
    // the member "addressId" which we have to refer to on the Address class 
    @Relationship(single=true, member="addressId")
    private Address address;
    
    // Similarly we have a One to Many relationship for Jobs. This time we not only need to specify the 
    // member "personId" which is the Person's class Primary key, but we also need to provide the name of the table:
    // this was a design choice of making code easier to develop.
    @Relationship(multiple = true, member="personId", column="person_id")
    private List<Job> jobHistory;
    
    // ... getters and setters
</code>
</br></br>
<p><b>Second</b> we describe the <b>DAO</b></p>

<code>
**
 * The DAO for the Person Class. This just extends the SimpleDao class providing
 * a type Person, which is the object we want to persist.
 *
 * @author novier
 */
public interface PersonDao extends SimpleDao<Person> {

}

</code>
