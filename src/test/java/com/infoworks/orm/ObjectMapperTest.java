package com.infoworks.orm;

import com.infoworks.data.impl.Person;
import com.infoworks.db.SQLConnector;
import com.infoworks.db.SQLDriverClass;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ObjectMapperTest {

    @Test
    public void basicTest() throws SQLException {
        ResultSet set = null; //Select * From Person;
        List<Person> persons = new PersonObjectMapper().objects(set);
        assertTrue(persons.isEmpty());
    }

    private static String QUERY = "SELECT uuid,name,age,gender,email FROM Person;";

    private static String[] PersonColNames = {"uuid", "name", "age", "gender", "email"};
    private static String[] PersonInsertQuery = {
            "INSERT INTO Person (uuid,name,age,gender,email) VALUES ('00992334','Cris',23,'MALE','Cris@g.com');"
            , "INSERT INTO Person (uuid,name,age,gender,email) VALUES ('00342334','Adams',32,'MALE','Adams@g.com');"
            , "INSERT INTO Person (uuid,name,age,gender,email) VALUES ('00242334','James',21,'MALE','James@g.com');"
            , "INSERT INTO Person (uuid,name,age,gender,email) VALUES ('00122334','Hayes',44,'MALE','Hayes@g.com');"
            , "INSERT INTO Person (uuid,name,age,gender,email) VALUES ('00562334','Andy',36,'MALE','Andy@g.com');"
    };

    @Test
    public void basicJdbcAndObjectMapperTest() throws SQLException {
        //First initiate database with tables:
        //JsqlConnector.executeScripts(DriverClass.H2_EMBEDDED, "testDB", new File("testDB-v1.4.200.sql"));
        SQLConnector.executeScripts(SQLDriverClass.H2_EMBEDDED, "testDB", new File("testDB-v2.2.220.sql"));
        //Now try to connect the database:testDB and insert seed rows:
        insertSeedData();
        //Now begin the actual test:
        try(Connection connection = SQLConnector.createConnection(SQLDriverClass.H2_EMBEDDED, "testDB");
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(QUERY)) {
            //Test Mapping:
            List<Person> persons = new PersonObjectMapper().objects(rs);
            Assert.assertNotNull(persons);
            Assert.assertFalse(persons.isEmpty());
            persons.forEach(person -> System.out.println(person.toString()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertSeedData() throws SQLException{
        try(Connection connection = SQLConnector.createConnection(SQLDriverClass.H2_EMBEDDED, "testDB");
            Statement stmt = connection.createStatement()) {
            //
            AtomicInteger insertCount = new AtomicInteger(0);
            Arrays.stream(PersonInsertQuery).forEach(query -> {
                try {
                    int result = stmt.executeUpdate(query);
                    insertCount.incrementAndGet();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            });
            System.out.println("Inserted records into the table, Count: " + insertCount.get());
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