package com.infoworks.db;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLConnectorTest {

    @Test
    public void h2DbInMemConnectionTest() {
        try(Connection connection = SQLConnector.createConnection(SQLDriverClass.H2_EMBEDDED, "testDB")) {
            Assert.assertTrue(connection != null);
            System.out.println("Connection was created.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void h2DbNewOrExistFileConnectionTest() {
        try(Connection connection = SQLConnector.createConnection(SQLDriverClass.H2_FILE, "testH2DB")) {
            Assert.assertTrue(connection != null);
            System.out.println("Connection was created.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void h2DbNewOrExistFileAsPathConnectionTest() {
        try(Connection connection = SQLConnector.createConnection(SQLDriverClass.H2_FILE, "../../target/testH2DB")) {
            Assert.assertTrue(connection != null);
            System.out.println("Connection was created.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
