package com.mysql.jdbc.exceptions;

import java.sql.SQLSyntaxErrorException;

public class MySQLSyntaxErrorException extends SQLSyntaxErrorException {

    public MySQLSyntaxErrorException() {
        super();
    }

    public MySQLSyntaxErrorException(String reason) {
        super(reason);
    }

    public MySQLSyntaxErrorException(String reason, String sqlState) {
        super(reason, sqlState);
    }

    public MySQLSyntaxErrorException(String reason, String sqlState, int vendorCode) {
        super(reason, sqlState, vendorCode);
    }
}
