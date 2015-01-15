
package com.riccardonoviello.simplesqlmappertest.dao;

import com.riccardonoviello.simplesqlmapper.core.SimpleDaoImpl;
import com.riccardonoviello.simplesqlmappertest.model.Job;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author novier
 */
@Repository
public class JobDaoImpl extends SimpleDaoImpl<Job> implements JobDao {
        
}
