package com.infoworks.db;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class SQLConnector {

    public static void executeScripts(SQLDriverClass driverClass, String dbName, File file) throws SQLException {
        SQLScriptExecutor runner = new SQLScriptExecutor();
        Connection conn = createConnection(driverClass, dbName);
        file = (file == null) ? new File(String.format("%s.sql", dbName)) : file;
        String[] cmds = runner.commands(runner.createStream(file));
        runner.execute(cmds, conn);
    }

    public static Connection createExecutor(SQLDriverClass driverClass, String dbName) throws SQLException {
        Connection conn = createConnection(driverClass, dbName);
        return conn;
    }

    public static Connection createConnection(SQLDriverClass driverClass, String dbName) throws SQLException {
        //Input validation:
        Connection conn = null;
        driverClass = (driverClass == null) ? SQLDriverClass.H2_EMBEDDED : driverClass;
        if (dbName == null || dbName.isEmpty()) throw new SQLException("Database Name (dbName) is null or empty.");
        //Create connections:
        if (driverClass == SQLDriverClass.MYSQL) {
            String connectionStr = driverClass.urlSchema() + "localhost:3306" + driverClass.pathPrefix() + dbName.trim();
            conn = DriverManager.getConnection(connectionStr, "root", "root@123");
        } else if (driverClass == SQLDriverClass.H2_FILE) {
            dbName = (!looksLikeAPath(dbName)) ? String.format("~/%s", dbName) : dbName;
            String linkQuery = ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE";
            String connectionStr = driverClass.urlSchema() + driverClass.pathPrefix() + dbName.trim() + linkQuery;
            conn = DriverManager.getConnection(connectionStr, "sa", "sa");
        } else {
            String linkQuery = ";DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE";
            String connectionStr = driverClass.urlSchema() + driverClass.pathPrefix() + dbName.trim() + linkQuery;
            conn = DriverManager.getConnection(connectionStr, "sa", "");
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
