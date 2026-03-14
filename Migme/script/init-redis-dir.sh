#!/bin/bash
if [ $# -lt 2 ] ; then
echo "Usage: $0 HOST PORT"
exit 1
fi

HOST=$1
PORT=$2
cat << EOF | redis-cli -p $PORT
lpush R:WEIGHTS 10
hset R:MASTERS 1 $HOST:$PORT
hset R:SLAVES 1 $HOST:$PORT
EOF
