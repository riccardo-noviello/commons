package com.riccardonoviello.simplesqlmappertest.model;

import com.riccardonoviello.simplesqlmapper.core.Column;
import com.riccardonoviello.simplesqlmapper.core.Entity;

/**
 * 
 * @author novier
 */
@Entity(name="sqlmapper.addresses")
public class Address {
    
    @Column(name="address_id", primary=true)
    private Long id;
    
    @Column(name="house_number")
    private String houseNumber;
    
    @Column(name="street")
    private String street;
    
    @Column(name="town")
    private String town;
    
    @Column(name="postcode")
    private String postcode;
    
    @Column(name="country")
    private String country;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
    
}
