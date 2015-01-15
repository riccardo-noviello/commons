
package com.riccardonoviello.simplesqlmappertest.controller;

import com.riccardonoviello.simplesqlmappertest.model.Person;
import com.riccardonoviello.simplesqlmappertest.dao.PersonDao;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * TODO LIST:
 * 
 * TODO: Replace JDBC template with custom code
 * 
 * @author novier
 */
@Controller
public class HomeController {

    private final static Logger logger = Logger.getLogger(HomeController.class.getName());

    @Autowired
    PersonDao dao;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public ModelAndView hello() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("hello");
        String str = "Hello World!";
        mav.addObject("message", str);
        return mav;
    }

    @RequestMapping(value = "/test/insert", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String testInsert() {
        StringWriter writer = new StringWriter();

        // insert a Person
        Person p1 = new Person();
        p1.setBirthday(new Date(13, 01, 1986));
        p1.setFirstname("Riccardo");
        p1.setLastname("Noviello");
        p1.setAge(28);

        Long id = (Long) dao.persist(p1);
        writer.append("\n\n### Person 1 persisted with id " + id);

        // Insert another person
        Person p2 = new Person();
        p2.setBirthday(new Date(03, 03, 1981));
        p2.setFirstname("Marco");
        p2.setLastname("Noviello");
        p2.setAge(32);

        Long id2 = (Long) dao.persist(p2);

        writer.append("\n\n### Person 2 persisted with id " + id2);

        return writer.toString();
    }
    
    @RequestMapping(value = "/test/search", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String testSearch() {
        StringWriter writer = new StringWriter();

        // Find a Person By id
        Person foundPerson = dao.findByPrimaryKey(1);
        writer.append("\n\n### Serch by id - Found person is: " + foundPerson.getFirstname() + foundPerson.getLastname());

        // Find All by Age
        Person foundPersonByAge = dao.findByUniqueRef("age", 32);
        writer.append("\n\n### Serach by age - Found person is: " + foundPersonByAge.getFirstname() + foundPersonByAge.getLastname());

        // Find All
        List<Person> persons = dao.findAll();
        for(Person item : persons){
            writer.append("\n\n### Find All - Found person is: " + item.getFirstname() + item.getLastname());
        }
        
        return writer.toString();
    }

}
