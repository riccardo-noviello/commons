package com.riccardonoviello.simpleimapclient.models;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author novier
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
public class MailAddress implements Serializable {

    private static final long serialVersionUID = 1L;

    private String address;
    private String personal;
    private String team;
    private Long teamId;
    private Long contactId;

    public MailAddress() {
    }

    public MailAddress(String a, String p) {
        this.address = a;
        this.personal = p;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPersonal() {
        return personal;
    }

    public void setPersonal(String personal) {
        this.personal = personal;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

}
