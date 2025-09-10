package com.infoworks.orm;

import com.infoworks.data.impl.Person;
import com.infoworks.db.JsqlConnector;
import com.it.soul.lab.connect.DriverClass;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
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

    @Test
    public void basicJdbcAndObjectMapperTest() throws SQLException {
        //First initiate database with tables:
        JsqlConnector.executeScripts(DriverClass.H2_EMBEDDED, "testDB", new File("testDB.sql"));
        //Now try to connect the database:testDB and insert seed rows:
        insertSeedData();
        //Now begin the actual test:
        try(Connection connection = JsqlConnector.createConnection(DriverClass.H2_EMBEDDED, "testDB")) {
            Assert.assertTrue(connection != null);
            System.out.println("Connection was created.");
            //TODO:
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertSeedData() throws SQLException{
        try(Connection connection = JsqlConnector.createConnection(DriverClass.H2_EMBEDDED, "testDB")) {
            Assert.assertTrue(connection != null);
            System.out.println("Connection was created.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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