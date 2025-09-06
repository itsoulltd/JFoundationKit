package com.infoworks.orm;

import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;

public class TableMapperTest {

    @Test
    public void basicTest() throws SQLException {
        ResultSet set = null; //Select * From Person;
        Table persons = new PersonTableMapper().table(set);
        assertTrue(persons.getRows().isEmpty());
    }

}

class PersonTableMapper implements TableMapper {
    @Override
    public Row row(ResultSet rs, int columnCount, int rowIdx) throws SQLException {
        Row row = new Row()
                .add("name", rs.getString("name"))
                .add("age", rs.getInt("age"))
                .add("email", rs.getString("email"))
                .add("gender", rs.getString("gender"));
        return row;
    }
}