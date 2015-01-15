
package com.riccardonoviello.simplesqlmappertest.model;

import com.riccardonoviello.simplesqlmapper.core.Column;
import com.riccardonoviello.simplesqlmapper.core.Entity;
import com.riccardonoviello.simplesqlmapper.core.Relationship;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author novier
 */
@Entity(name="sqlmapper.persons")
public class Person {

    @Column(name="id", primary=true)
    private Long id;
    
    @Column(name="firstname")
    private String firstname;
    
    @Column(name="lastname")
    private String lastname;
    
    @Column(name="birthdate")
    private Date birthday;
    
    @Column(name="age")
    private int age;
    
    @Column(name="address")
    private Long addressId;
    
    @Relationship(single=true, member="addressId")
    private Address address;
    
    @Relationship(multiple = true, member="personId", column="person_id")
    private List<Job> jobHistory;
    
    public Long getId() {
        return id;
    }
    
    public Person(){}
    
    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Job> getJobHistory() {
        return jobHistory;
    }

    public void setJobHistory(List<Job> jobHistory) {
        this.jobHistory = jobHistory;
    }
    
    
    
}
