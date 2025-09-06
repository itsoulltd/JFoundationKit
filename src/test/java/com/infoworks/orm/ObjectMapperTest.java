package com.infoworks.orm;

import com.infoworks.data.impl.Person;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class ObjectMapperTest {

    @Test
    public void basicTest() throws SQLException {
        ResultSet set = null; //Select * From Person;
        List<Person> persons = new PersonObjectMapper().objects(set);
        assertTrue(persons.isEmpty());
    }

}

class PersonObjectMapper implements ObjectMapper<Person> {
    @Override
    public Person object(ResultSet rs, int columnCount, int rowIdx) throws SQLException {
        Person person = new Person();
        person.setAge(rs.getInt("age"));
        person.setName(rs.getString("name"));
        person.setGender(rs.getString("gender"));
        person.setEmail(rs.getString("email"));
        //person.setCreateDate(rs.getTimestamp("createDate"));
        return person;
    }
}