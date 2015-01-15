package com.riccardonoviello.simplesqlmappertest.model;

import com.riccardonoviello.simplesqlmapper.core.Column;
import com.riccardonoviello.simplesqlmapper.core.Entity;
import com.riccardonoviello.simplesqlmapper.core.Relationship;
import java.util.Date;

/**
 * 
 * @author novier
 */
@Entity(name="sqlmapper.jobs")
public class Job {
    
    @Column(name="job_id", primary=true)
    private Long id;
    
    @Column(name="title")
    private String title;
    
    @Column(name="start")
    private Date start;
    
    @Column(name="end")
    private Date end;
    
    @Column(name="person_id")
    private Long personId;
    
    @Column(name="workplace")
    private Long workplaceId;
    
    @Relationship(single=true, member="workplaceId")
    private Address workplace;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public Long getWorkplaceId() {
        return workplaceId;
    }

    public void setWorkplaceId(Long workplaceId) {
        this.workplaceId = workplaceId;
    }

    public Address getWorkplace() {
        return workplace;
    }

    public void setWorkplace(Address workplace) {
        this.workplace = workplace;
    }
    
}
