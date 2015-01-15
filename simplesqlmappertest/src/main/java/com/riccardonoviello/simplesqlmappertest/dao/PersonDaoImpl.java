package com.riccardonoviello.simplesqlmappertest.dao;

import com.riccardonoviello.simplesqlmappertest.model.Person;
import com.riccardonoviello.simplesqlmapper.core.SimpleDaoImpl;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author novier
 */
@Repository
public class PersonDaoImpl extends SimpleDaoImpl<Person> implements PersonDao {
}
