#!/bin/bash
DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

service postgresql stop
sync && echo 3 > /proc/sys/vm/drop_caches;
service postgresql start
echo "clearing cache... `date`"
sleep 1;
