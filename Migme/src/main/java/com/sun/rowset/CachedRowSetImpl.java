package com.sun.rowset;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Ref;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Array;
import java.sql.RowId;
import java.sql.NClob;
import java.sql.SQLXML;
import java.sql.Savepoint;


import java.math.BigDecimal;
import java.io.*;
import java.net.URL;
import java.util.*;
import javax.sql.RowSet;

public class CachedRowSetImpl implements ResultSet, RowSet {
    private ResultSet delegate;

    public CachedRowSetImpl() {}

    public void populate(ResultSet rs) throws SQLException {
        this.delegate = rs;
    }

    private ResultSet d() throws SQLException {
        if (delegate == null) throw new SQLException("Not populated");
        return delegate;
    }

    public boolean next() throws SQLException { return delegate != null && d().next(); }
    public void close() throws SQLException { if (delegate != null) d().close(); }
    public boolean wasNull() throws SQLException { return delegate != null && d().wasNull(); }
    public String getString(int col) throws SQLException { return d().getString(col); }
    public boolean getBoolean(int col) throws SQLException { return d().getBoolean(col); }
    public byte getByte(int col) throws SQLException { return d().getByte(col); }
    public short getShort(int col) throws SQLException { return d().getShort(col); }
    public int getInt(int col) throws SQLException { return d().getInt(col); }
    public long getLong(int col) throws SQLException { return d().getLong(col); }
    public float getFloat(int col) throws SQLException { return d().getFloat(col); }
    public double getDouble(int col) throws SQLException { return d().getDouble(col); }
    public BigDecimal getBigDecimal(int col, int scale) throws SQLException { return d().getBigDecimal(col, scale); }
    public byte[] getBytes(int col) throws SQLException { return d().getBytes(col); }
    public Date getDate(int col) throws SQLException { return d().getDate(col); }
    public Time getTime(int col) throws SQLException { return d().getTime(col); }
    public Timestamp getTimestamp(int col) throws SQLException { return d().getTimestamp(col); }
    public InputStream getAsciiStream(int col) throws SQLException { return d().getAsciiStream(col); }
    public InputStream getUnicodeStream(int col) throws SQLException { return d().getUnicodeStream(col); }
    public InputStream getBinaryStream(int col) throws SQLException { return d().getBinaryStream(col); }
    public String getString(String col) throws SQLException { return d().getString(col); }
    public boolean getBoolean(String col) throws SQLException { return d().getBoolean(col); }
    public byte getByte(String col) throws SQLException { return d().getByte(col); }
    public short getShort(String col) throws SQLException { return d().getShort(col); }
    public int getInt(String col) throws SQLException { return d().getInt(col); }
    public long getLong(String col) throws SQLException { return d().getLong(col); }
    public float getFloat(String col) throws SQLException { return d().getFloat(col); }
    public double getDouble(String col) throws SQLException { return d().getDouble(col); }
    public BigDecimal getBigDecimal(String col, int scale) throws SQLException { return d().getBigDecimal(col, scale); }
    public byte[] getBytes(String col) throws SQLException { return d().getBytes(col); }
    public Date getDate(String col) throws SQLException { return d().getDate(col); }
    public Time getTime(String col) throws SQLException { return d().getTime(col); }
    public Timestamp getTimestamp(String col) throws SQLException { return d().getTimestamp(col); }
    public InputStream getAsciiStream(String col) throws SQLException { return d().getAsciiStream(col); }
    public InputStream getUnicodeStream(String col) throws SQLException { return d().getUnicodeStream(col); }
    public InputStream getBinaryStream(String col) throws SQLException { return d().getBinaryStream(col); }
    public SQLWarning getWarnings() throws SQLException { return delegate != null ? d().getWarnings() : null; }
    public void clearWarnings() throws SQLException { if (delegate != null) d().clearWarnings(); }
    public String getCursorName() throws SQLException { return d().getCursorName(); }
    public ResultSetMetaData getMetaData() throws SQLException { return d().getMetaData(); }
    public Object getObject(int col) throws SQLException { return d().getObject(col); }
    public Object getObject(String col) throws SQLException { return d().getObject(col); }
    public int findColumn(String col) throws SQLException { return d().findColumn(col); }
    public Reader getCharacterStream(int col) throws SQLException { return d().getCharacterStream(col); }
    public Reader getCharacterStream(String col) throws SQLException { return d().getCharacterStream(col); }
    public BigDecimal getBigDecimal(int col) throws SQLException { return d().getBigDecimal(col); }
    public BigDecimal getBigDecimal(String col) throws SQLException { return d().getBigDecimal(col); }
    public boolean isBeforeFirst() throws SQLException { return delegate == null || d().isBeforeFirst(); }
    public boolean isAfterLast() throws SQLException { return delegate != null && d().isAfterLast(); }
    public boolean isFirst() throws SQLException { return delegate != null && d().isFirst(); }
    public boolean isLast() throws SQLException { return delegate != null && d().isLast(); }
    public void beforeFirst() throws SQLException { if (delegate != null) d().beforeFirst(); }
    public void afterLast() throws SQLException { if (delegate != null) d().afterLast(); }
    public boolean first() throws SQLException { return delegate != null && d().first(); }
    public boolean last() throws SQLException { return delegate != null && d().last(); }
    public int getRow() throws SQLException { return delegate != null ? d().getRow() : 0; }
    public boolean absolute(int row) throws SQLException { return delegate != null && d().absolute(row); }
    public boolean relative(int rows) throws SQLException { return delegate != null && d().relative(rows); }
    public boolean previous() throws SQLException { return delegate != null && d().previous(); }
    public void setFetchDirection(int dir) throws SQLException { if (delegate != null) d().setFetchDirection(dir); }
    public int getFetchDirection() throws SQLException { return delegate != null ? d().getFetchDirection() : ResultSet.FETCH_FORWARD; }
    public void setFetchSize(int rows) throws SQLException { if (delegate != null) d().setFetchSize(rows); }
    public int getFetchSize() throws SQLException { return delegate != null ? d().getFetchSize() : 0; }
    public int getType() throws SQLException { return delegate != null ? d().getType() : ResultSet.TYPE_FORWARD_ONLY; }
    public int getConcurrency() throws SQLException { return delegate != null ? d().getConcurrency() : ResultSet.CONCUR_READ_ONLY; }
    public boolean rowUpdated() throws SQLException { return false; }
    public boolean rowInserted() throws SQLException { return false; }
    public boolean rowDeleted() throws SQLException { return false; }
    public void updateNull(int col) throws SQLException {}
    public void updateBoolean(int col, boolean x) throws SQLException {}
    public void updateByte(int col, byte x) throws SQLException {}
    public void updateShort(int col, short x) throws SQLException {}
    public void updateInt(int col, int x) throws SQLException {}
    public void updateLong(int col, long x) throws SQLException {}
    public void updateFloat(int col, float x) throws SQLException {}
    public void updateDouble(int col, double x) throws SQLException {}
    public void updateBigDecimal(int col, BigDecimal x) throws SQLException {}
    public void updateString(int col, String x) throws SQLException {}
    public void updateBytes(int col, byte[] x) throws SQLException {}
    public void updateDate(int col, Date x) throws SQLException {}
    public void updateTime(int col, Time x) throws SQLException {}
    public void updateTimestamp(int col, Timestamp x) throws SQLException {}
    public void updateAsciiStream(int col, InputStream x, int l) throws SQLException {}
    public void updateBinaryStream(int col, InputStream x, int l) throws SQLException {}
    public void updateCharacterStream(int col, Reader x, int l) throws SQLException {}
    public void updateObject(int col, Object x, int scale) throws SQLException {}
    public void updateObject(int col, Object x) throws SQLException {}
    public void updateNull(String col) throws SQLException {}
    public void updateBoolean(String col, boolean x) throws SQLException {}
    public void updateByte(String col, byte x) throws SQLException {}
    public void updateShort(String col, short x) throws SQLException {}
    public void updateInt(String col, int x) throws SQLException {}
    public void updateLong(String col, long x) throws SQLException {}
    public void updateFloat(String col, float x) throws SQLException {}
    public void updateDouble(String col, double x) throws SQLException {}
    public void updateBigDecimal(String col, BigDecimal x) throws SQLException {}
    public void updateString(String col, String x) throws SQLException {}
    public void updateBytes(String col, byte[] x) throws SQLException {}
    public void updateDate(String col, Date x) throws SQLException {}
    public void updateTime(String col, Time x) throws SQLException {}
    public void updateTimestamp(String col, Timestamp x) throws SQLException {}
    public void updateAsciiStream(String col, InputStream x, int l) throws SQLException {}
    public void updateBinaryStream(String col, InputStream x, int l) throws SQLException {}
    public void updateCharacterStream(String col, Reader x, int l) throws SQLException {}
    public void updateObject(String col, Object x, int scale) throws SQLException {}
    public void updateObject(String col, Object x) throws SQLException {}
    public void insertRow() throws SQLException {}
    public void updateRow() throws SQLException {}
    public void deleteRow() throws SQLException {}
    public void refreshRow() throws SQLException {}
    public void cancelRowUpdates() throws SQLException {}
    public void moveToInsertRow() throws SQLException {}
    public void moveToCurrentRow() throws SQLException {}
    public Statement getStatement() throws SQLException { return delegate != null ? d().getStatement() : null; }
    public Object getObject(int col, Map<String,Class<?>> map) throws SQLException { return d().getObject(col, map); }
    public Ref getRef(int col) throws SQLException { return d().getRef(col); }
    public Blob getBlob(int col) throws SQLException { return d().getBlob(col); }
    public Clob getClob(int col) throws SQLException { return d().getClob(col); }
    public Array getArray(int col) throws SQLException { return d().getArray(col); }
    public Object getObject(String col, Map<String,Class<?>> map) throws SQLException { return d().getObject(col, map); }
    public Ref getRef(String col) throws SQLException { return d().getRef(col); }
    public Blob getBlob(String col) throws SQLException { return d().getBlob(col); }
    public Clob getClob(String col) throws SQLException { return d().getClob(col); }
    public Array getArray(String col) throws SQLException { return d().getArray(col); }
    public Date getDate(int col, Calendar cal) throws SQLException { return d().getDate(col, cal); }
    public Date getDate(String col, Calendar cal) throws SQLException { return d().getDate(col, cal); }
    public Time getTime(int col, Calendar cal) throws SQLException { return d().getTime(col, cal); }
    public Time getTime(String col, Calendar cal) throws SQLException { return d().getTime(col, cal); }
    public Timestamp getTimestamp(int col, Calendar cal) throws SQLException { return d().getTimestamp(col, cal); }
    public Timestamp getTimestamp(String col, Calendar cal) throws SQLException { return d().getTimestamp(col, cal); }
    public URL getURL(int col) throws SQLException { return d().getURL(col); }
    public URL getURL(String col) throws SQLException { return d().getURL(col); }
    public void updateRef(int col, Ref x) throws SQLException {}
    public void updateRef(String col, Ref x) throws SQLException {}
    public void updateBlob(int col, Blob x) throws SQLException {}
    public void updateBlob(String col, Blob x) throws SQLException {}
    public void updateClob(int col, Clob x) throws SQLException {}
    public void updateClob(String col, Clob x) throws SQLException {}
    public void updateArray(int col, Array x) throws SQLException {}
    public void updateArray(String col, Array x) throws SQLException {}
    public RowId getRowId(int col) throws SQLException { return d().getRowId(col); }
    public RowId getRowId(String col) throws SQLException { return d().getRowId(col); }
    public void updateRowId(int col, RowId x) throws SQLException {}
    public void updateRowId(String col, RowId x) throws SQLException {}
    public int getHoldability() throws SQLException { return delegate != null ? d().getHoldability() : 0; }
    public boolean isClosed() throws SQLException { return delegate == null || d().isClosed(); }
    public void updateNString(int col, String s) throws SQLException {}
    public void updateNString(String col, String s) throws SQLException {}
    public void updateNClob(int col, NClob nc) throws SQLException {}
    public void updateNClob(String col, NClob nc) throws SQLException {}
    public NClob getNClob(int col) throws SQLException { return d().getNClob(col); }
    public NClob getNClob(String col) throws SQLException { return d().getNClob(col); }
    public SQLXML getSQLXML(int col) throws SQLException { return d().getSQLXML(col); }
    public SQLXML getSQLXML(String col) throws SQLException { return d().getSQLXML(col); }
    public void updateSQLXML(int col, SQLXML x) throws SQLException {}
    public void updateSQLXML(String col, SQLXML x) throws SQLException {}
    public String getNString(int col) throws SQLException { return d().getNString(col); }
    public String getNString(String col) throws SQLException { return d().getNString(col); }
    public Reader getNCharacterStream(int col) throws SQLException { return d().getNCharacterStream(col); }
    public Reader getNCharacterStream(String col) throws SQLException { return d().getNCharacterStream(col); }
    public void updateNCharacterStream(int col, Reader r, long l) throws SQLException {}
    public void updateNCharacterStream(String col, Reader r, long l) throws SQLException {}
    public void updateAsciiStream(int col, InputStream x, long l) throws SQLException {}
    public void updateBinaryStream(int col, InputStream x, long l) throws SQLException {}
    public void updateCharacterStream(int col, Reader x, long l) throws SQLException {}
    public void updateAsciiStream(String col, InputStream x, long l) throws SQLException {}
    public void updateBinaryStream(String col, InputStream x, long l) throws SQLException {}
    public void updateCharacterStream(String col, Reader x, long l) throws SQLException {}
    public void updateBlob(int col, InputStream x, long l) throws SQLException {}
    public void updateBlob(String col, InputStream x, long l) throws SQLException {}
    public void updateClob(int col, Reader r, long l) throws SQLException {}
    public void updateClob(String col, Reader r, long l) throws SQLException {}
    public void updateNClob(int col, Reader r, long l) throws SQLException {}
    public void updateNClob(String col, Reader r, long l) throws SQLException {}
    public void updateNCharacterStream(int col, Reader r) throws SQLException {}
    public void updateNCharacterStream(String col, Reader r) throws SQLException {}
    public void updateAsciiStream(int col, InputStream x) throws SQLException {}
    public void updateBinaryStream(int col, InputStream x) throws SQLException {}
    public void updateCharacterStream(int col, Reader x) throws SQLException {}
    public void updateAsciiStream(String col, InputStream x) throws SQLException {}
    public void updateBinaryStream(String col, InputStream x) throws SQLException {}
    public void updateCharacterStream(String col, Reader x) throws SQLException {}
    public void updateBlob(int col, InputStream x) throws SQLException {}
    public void updateBlob(String col, InputStream x) throws SQLException {}
    public void updateClob(int col, Reader r) throws SQLException {}
    public void updateClob(String col, Reader r) throws SQLException {}
    public void updateNClob(int col, Reader r) throws SQLException {}
    public void updateNClob(String col, Reader r) throws SQLException {}
    public <T> T getObject(int col, Class<T> type) throws SQLException { return d().getObject(col, type); }
    public <T> T getObject(String col, Class<T> type) throws SQLException { return d().getObject(col, type); }
    public <T> T unwrap(Class<T> iface) throws SQLException { return null; }
    public boolean isWrapperFor(Class<?> iface) throws SQLException { return false; }
    // RowSet methods
    public String getUrl() throws SQLException { return null; }
    public void setUrl(String url) throws SQLException {}
    public String getDataSourceName() { return null; }
    public void setDataSourceName(String name) throws SQLException {}
    public String getUsername() { return null; }
    public void setUsername(String name) throws SQLException {}
    public String getPassword() { return null; }
    public void setPassword(String password) throws SQLException {}
    public int getTransactionIsolation() { return 0; }
    public void setTransactionIsolation(int level) throws SQLException {}
    public Map<String,Class<?>> getTypeMap() throws SQLException { return null; }
    public void setTypeMap(Map<String,Class<?>> map) throws SQLException {}
    public String getCommand() { return null; }
    public void setCommand(String cmd) throws SQLException {}
    public boolean isReadOnly() { return true; }
    public void setReadOnly(boolean value) throws SQLException {}
    public int getMaxFieldSize() throws SQLException { return 0; }
    public void setMaxFieldSize(int max) throws SQLException {}
    public int getMaxRows() throws SQLException { return 0; }
    public void setMaxRows(int max) throws SQLException {}
    public boolean getEscapeProcessing() throws SQLException { return false; }
    public void setEscapeProcessing(boolean enable) throws SQLException {}
    public int getQueryTimeout() throws SQLException { return 0; }
    public void setQueryTimeout(int seconds) throws SQLException {}
    public void setType(int type) throws SQLException {}
    public void setConcurrency(int concurrency) throws SQLException {}
    public void setNull(int pi, int sqlType) throws SQLException {}
    public void setNull(String pn, int sqlType) throws SQLException {}
    public void setNull(int pi, int sqlType, String typeName) throws SQLException {}
    public void setNull(String pn, int sqlType, String typeName) throws SQLException {}
    public void setBoolean(int pi, boolean x) throws SQLException {}
    public void setBoolean(String pn, boolean x) throws SQLException {}
    public void setByte(int pi, byte x) throws SQLException {}
    public void setByte(String pn, byte x) throws SQLException {}
    public void setShort(int pi, short x) throws SQLException {}
    public void setShort(String pn, short x) throws SQLException {}
    public void setInt(int pi, int x) throws SQLException {}
    public void setInt(String pn, int x) throws SQLException {}
    public void setLong(int pi, long x) throws SQLException {}
    public void setLong(String pn, long x) throws SQLException {}
    public void setFloat(int pi, float x) throws SQLException {}
    public void setFloat(String pn, float x) throws SQLException {}
    public void setDouble(int pi, double x) throws SQLException {}
    public void setDouble(String pn, double x) throws SQLException {}
    public void setBigDecimal(int pi, BigDecimal x) throws SQLException {}
    public void setBigDecimal(String pn, BigDecimal x) throws SQLException {}
    public void setString(int pi, String x) throws SQLException {}
    public void setString(String pn, String x) throws SQLException {}
    public void setBytes(int pi, byte[] x) throws SQLException {}
    public void setBytes(String pn, byte[] x) throws SQLException {}
    public void setDate(int pi, Date x) throws SQLException {}
    public void setTime(int pi, Time x) throws SQLException {}
    public void setTimestamp(int pi, Timestamp x) throws SQLException {}
    public void setTimestamp(String pn, Timestamp x) throws SQLException {}
    public void setAsciiStream(int pi, InputStream x, int l) throws SQLException {}
    public void setAsciiStream(String pn, InputStream x, int l) throws SQLException {}
    public void setBinaryStream(int pi, InputStream x, int l) throws SQLException {}
    public void setBinaryStream(String pn, InputStream x, int l) throws SQLException {}
    public void setCharacterStream(int pi, Reader r, int l) throws SQLException {}
    public void setCharacterStream(String pn, Reader r, int l) throws SQLException {}
    public void setObject(int pi, Object x, int targetSqlType, int scale) throws SQLException {}
    public void setObject(String pn, Object x, int targetSqlType, int scale) throws SQLException {}
    public void setObject(int pi, Object x, int targetSqlType) throws SQLException {}
    public void setObject(String pn, Object x, int targetSqlType) throws SQLException {}
    public void setObject(int pi, Object x) throws SQLException {}
    public void setObject(String pn, Object x) throws SQLException {}
    public void setRef(int pi, Ref ref) throws SQLException {}
    public void setBlob(int pi, Blob x) throws SQLException {}
    public void setClob(int pi, Clob x) throws SQLException {}
    public void setArray(int pi, Array array) throws SQLException {}
    public void setDate(int pi, Date x, Calendar cal) throws SQLException {}
    public void setDate(String pn, Date x) throws SQLException {}
    public void setDate(String pn, Date x, Calendar cal) throws SQLException {}
    public void setTime(int pi, Time x, Calendar cal) throws SQLException {}
    public void setTime(String pn, Time x) throws SQLException {}
    public void setTime(String pn, Time x, Calendar cal) throws SQLException {}
    public void setTimestamp(int pi, Timestamp x, Calendar cal) throws SQLException {}
    public void setTimestamp(String pn, Timestamp x, Calendar cal) throws SQLException {}
    public void clearParameters() throws SQLException {}
    public void execute() throws SQLException {}
    public void addRowSetListener(javax.sql.RowSetListener listener) {}
    public void removeRowSetListener(javax.sql.RowSetListener listener) {}
    public void setNString(int pi, String x) throws SQLException {}
    public void setNString(String pn, String x) throws SQLException {}
    public void setNClob(int pi, NClob x) throws SQLException {}
    public void setNClob(String pn, NClob x) throws SQLException {}
    public void setNClob(int pi, Reader r) throws SQLException {}
    public void setNClob(String pn, Reader r) throws SQLException {}
    public void setSQLXML(int pi, SQLXML x) throws SQLException {}
    public void setSQLXML(String pn, SQLXML x) throws SQLException {}
    public void setRowId(int pi, RowId x) throws SQLException {}
    public void setRowId(String pn, RowId x) throws SQLException {}
    public void setAsciiStream(int pi, InputStream x) throws SQLException {}
    public void setAsciiStream(String pn, InputStream x) throws SQLException {}
    public void setBinaryStream(int pi, InputStream x) throws SQLException {}
    public void setBinaryStream(String pn, InputStream x) throws SQLException {}
    public void setCharacterStream(int pi, Reader r) throws SQLException {}
    public void setCharacterStream(String pn, Reader r) throws SQLException {}
    public void setNCharacterStream(int pi, Reader r) throws SQLException {}
    public void setNCharacterStream(String pn, Reader r) throws SQLException {}
    public void setBlob(int pi, InputStream x) throws SQLException {}
    public void setBlob(String pn, InputStream x) throws SQLException {}
    public void setBlob(String pn, Blob x) throws SQLException {}
    public void setClob(int pi, Reader r) throws SQLException {}
    public void setClob(String pn, Clob x) throws SQLException {}
    public void setClob(String pn, Reader r) throws SQLException {}
}
