import time
import sys
import optparse
import MySQLdb
import re, string



def setup( host, username, password, database,port=3306):
    """
    setup connection to mysql
    """
    connection = MySQLdb.connect ( host, username, password , database ,port = int(port))
    return connection

def parse_row(row, delim, delchars):
    """
    parse a mysql row into a `delim` delimited
    string.
    """
    def strip_nonprintable(v):
        return "".join(i for i in v if (ord(i)>31 and ord(i)<128))
    return delim.join([strip_nonprintable(str(k)) for k in row])


def stream_query ( connection, query , delimiter = '\001', replace = "\t\n\r" ):
    """
    execute the query on the connection
    and print out the result set to stdout.
    """
    idx = 0
    connection.query(query)
    res = connection.use_result()
    row = res.fetch_row()
    while row:
        print (  parse_row ( row[0] , delimiter, replace ) )
        row = res.fetch_row()
        idx+=1

if __name__ == "__main__":
    parser = optparse.OptionParser()
    # i want -h for myself! 
    parser.remove_option('--help')

    parser.add_option("-d","--database",dest="db", help = "name of database")
    parser.add_option("-h","--host",dest="host", help = "name of host")
    parser.add_option("-u","--username",dest="username", help = "username")
    parser.add_option("-p","--password",dest="pwd", help = "password")
    parser.add_option("-t","--table",dest="table",help = "destination table for select * query") 
    parser.add_option("-q","--query",dest="query", help = "query to execute")
    parser.add_option("-P","--port",dest="port", help = "port", default=3306)
    parser.add_option("--delimiter",dest="output_delimiter", action = "store_true", help = "use tab delimiter to use to separate columns")
    parser.add_option("--reserved_chars",dest="reserved_chars", help = "reserved characters to strip from output")

    opts, args = parser.parse_args()
    
    if opts.query:
        query = opts.query
    else:
        if not opts.table:
            parser.error("if no query is specified, atleast a table name should be")
        query = "select * from %s" % opts.table 
    conn_obj = setup ( host = opts.host, username = opts.username , password = opts.pwd, database = opts.db ,port=opts.port)
    if opts.reserved_chars:
        reserved_chars = opts.reserved_chars
    else:
        reserved_chars = ""
    
    if opts.output_delimiter:
        delim = '\t'
        rep = "\t\r\n" 
    else:
        delim= '\001'
        rep =""

    stream_query( conn_obj, query, delim, rep )



