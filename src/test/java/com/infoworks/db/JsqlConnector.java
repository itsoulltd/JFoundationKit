package com.infoworks.db;

import com.it.soul.lab.connect.DriverClass;
import com.it.soul.lab.connect.JDBConnection;
import com.it.soul.lab.connect.io.ScriptRunner;
import com.it.soul.lab.sql.QueryExecutor;
import com.it.soul.lab.sql.SQLExecutor;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

public final class JsqlConnector {

    public static void executeScripts(DriverClass driverClass, String dbName, File file) throws SQLException {
        ScriptRunner runner = new ScriptRunner();
        Connection conn = createConnection(driverClass, dbName);
        file = (file == null) ? new File(String.format("%s.sql", dbName)) : file;
        String[] cmds = runner.commands(runner.createStream(file));
        runner.execute(cmds, conn);
    }

    public static QueryExecutor createExecutor(DriverClass driverClass, String dbName) throws SQLException {
        Connection conn = createConnection(driverClass, dbName);
        return new SQLExecutor(conn);
    }

    public static Connection createConnection(DriverClass driverClass, String dbName) throws SQLException {
        //Input validation:
        Connection conn;
        driverClass = (driverClass == null) ? DriverClass.H2_EMBEDDED : driverClass;
        if (dbName == null || dbName.isEmpty()) throw new SQLException("Database Name (dbName) is null or empty.");
        //Create connections:
        if (driverClass == DriverClass.MYSQL){
            conn = new JDBConnection.Builder(driverClass)
                    .host("localhost", "3306")
                    .database(dbName)
                    .credential("root","root@123")
                    .build();
        } else if (driverClass == DriverClass.H2_FILE) {
            dbName = (!looksLikeAPath(dbName)) ? String.format("~/%s", dbName) : dbName;
            conn = new JDBConnection.Builder(driverClass)
                    .database(dbName)
                    .credential("sa", "sa")
                    .query(";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE")
                    .build();
        } else {
            conn = new JDBConnection.Builder(driverClass)
                    .database(dbName)
                    .credential("sa", "")
                    .query(";DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE")
                    .build();
        }
        return conn;
    }

    public static boolean looksLikeAPath(String path) {
        // Example heuristic:
        return path.matches(".*[\\\\/].*")
                || path.matches("^[a-zA-Z]:\\\\.*");
    }

    public static boolean isPath(String input) {
        try {
            // Basic structural check
            Path path = Paths.get(input);
            return path.getNameCount() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
