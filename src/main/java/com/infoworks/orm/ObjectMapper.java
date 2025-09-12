package com.infoworks.orm;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author towhid
 * @since 19-Aug-19
 */
public interface ObjectMapper<R> {
    default List<R> objects(ResultSet rs) throws SQLException {
        if (rs == null) return new ArrayList<>();
        List<R> collection = new ArrayList<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        final int columnCount = rsmd.getColumnCount();
        int rowIdx = 0;
        while (rs.next()){
            try {
                R entity = object(rs, columnCount, rowIdx++);
                collection.add(entity);
            } catch (SQLException e) { throw new RuntimeException(e); }
        }
        return collection;
    }

    /**
     * R objects(rs, columnCount, rowIdx)
     * @param rs
     * @param columnCount
     * @param rowIdx
     * @return
     * @throws SQLException
     */
    R object(ResultSet rs, int columnCount, int rowIdx) throws SQLException;
}
