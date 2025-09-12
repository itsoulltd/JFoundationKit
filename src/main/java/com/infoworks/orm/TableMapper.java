package com.infoworks.orm;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public interface TableMapper {
    default Table table(ResultSet rs) throws SQLException {
        if (rs == null) return new Table();
        final Table table = new Table();
        ResultSetMetaData rsmd = rs.getMetaData();
        final int columnCount = rsmd.getColumnCount();
        int rowidx = 0;
        while (rs.next()){
            try {
                Row row = row(rs, columnCount, rowidx++);
                table.add(row);
            } catch (SQLException e) { throw new RuntimeException(e); }
        }
        return table;
    }

    /**
     * Row row(rs, columnCount, rowIdx)
     * @param rs
     * @param columnCount
     * @param rowIdx
     * @return
     * @throws SQLException
     */
    Row row(ResultSet rs, int columnCount, int rowIdx) throws SQLException;
}
