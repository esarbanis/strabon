#! /bin/bash
# 
# This Source Code Form is subject to the terms of the Mozilla Public 
# License, v. 2.0. If a copy of the MPL was not distributed with this file, you
# can obtain one at http://mozilla.org/MPL/2.0/. 
# 
# Copyright (C) 2010, 2011, 2012, Pyravlos Team 
# 
# http://www.strabon.di.uoa.gr/ 
#

# Command name
cmd="$(basename ${0})" 
# Get the directory where the script resides
loc="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

function help() {                                                               
    echo "Usage: ${cmd} [OPTIONS] "                        
    echo                                                                        
    echo "Execute NOA chain with refinements and measure time."
    echo                                                                        
    echo "OPTIONS can be any of the following"                                  
    echo "  -d,--db   		: PostGIS database"                
    echo "  -e,--endpoint   : Strabon Endpoint"
    echo "  -h,--hotposts	: URL where hotspots are stored"
	echo "  -b,--background	: Background data"                                           
	echo "  -l,--log		: Log file"                                           
}

# If no arguments are given it returns miliseconds from 1970-01-01 00:00:00 UTC
# Else if a time (miliseconds form ...) is given it returns the delta between
# given time and current time
function timer()
{
   if [[ $# -eq 0 ]]; then
       t=$(date '+%s%N')
       t=$((t/1000000))
       echo $t
   else
       local  stime=$1
       etime=$(date '+%s%N')
       etime=$((etime/1000000))

       if [[ -z "$stime" ]]; then stime=$etime; fi
       dt=$((etime - stime)) #dt in milliseconds
       dM=$((dt%1000))
       Dt=$((dt/1000)) #delta t in seconds
       ds=$((Dt % 60))
       dm=$(((Dt / 60) % 60))
       dh=$((Dt / 3600))
       printf '%d:%02d:%02d.%03d' $dh $dm $ds $dM
   fi
}

# Handle the postgres service
# -$1: Command for the service
function handlePostgresService()
{
	# find out the postgres service to use
	postgres=$(ls -1 /etc/init.d/| grep postgres | head -1)

	echo "Postgres service received command: $1"
	sudo service ${postgres} $1
}

# Handled a postgres database
# -$1: Command (create/drop/store)
# -$2: Dump file to store (if store is given as $1)
#	   'spatial' to create a spatial database (if create is given as $1)
function handlePostgresDatabase() {
	local command=$1
	local db=$2
	shift; shift
	local options="$*"
	case "${command}" in
		create)
			echo "Creating database ${db}..."
			createdb -U postgres ${db} ${options}	
			;;
		drop)
			if test ! -z "${options}"; then
				echo "ERROR: dropdb takes no extra options"
				echo "options: ${options}"
				exit -1
			fi
			echo "Dropping database ${db}..."
			dropdb -U postgres ${db} 
			;;
		vacuum)
			if test "${options}" = "analyze"; then
				psql -U postgres ${DB} -c 'VACUUM ANALYZE' 
			else
				psql -U postgres ${DB} -c 'VACUUM' 
			fi
			;;
		run)
			if test ! [ -f "${options}" ]; then
				echo "ERROR: No dump file to run"
				exit -1
			fi
			echo "Storing dump file ${options} in database ${db}..."
			psql ${db} -f ${options}
			;;
	esac
}

# Handle the tomcat service
# -$1: Command for the service
function handleTomcatService()
{
	# find out the tomcat service to use
	if test -s /etc/fedora-release ; then
		tomcat="tomcat"
	#elif test -s /etc/centos-release ; then
	#elif test -s /etc/yellowdog-release ; then
	#elif test -s /etc/redhat-release ; then
	#elif test -s /etc/SuSE-release ; then
	#elif test -s /etc/gentoo-release ; then
	elif test -s /etc/lsb-release ; then # Ubuntu
		tomcat=$(ls -1 /etc/init.d/| grep tomcat | head -1)
	elif test -s /etc/debian_version ; then
		tomcat="tomcat"
	fi

	# check for service availability
	if ! test -e "/etc/init.d/${tomcat}"; then
		echo "ERROR: No tomcat service found"
		exit -1
	fi

	echo "Service ${tomcat} received command: $1"
	sudo service ${tomcat} $1
}

# get the main version of postgres
function getPostgresMainVersion() {
	echo $(sudo service ${postgres} status | grep -o '.\..' | cut -b 1)
}

# It stores the backgroud data
# - $1: database
# - $2: backgound data file
function storeBackgroundData() {
	local db=$1
	local bgFile=$2

	if test -f ${bgFile}; then
		handlePostgresDatabase run ${db} ${bgFile}	
	elif test "${bgFile:0:7}" = "http://"; then
		#curl -s ${bgFile} | tar xz -O | psql -d ${DB}
		wget ${bgFile} -O /tmp/bgFile$$.tar.gz
		tar xz -O /tmp/bgFile$$.tar.gz
		
		handlePostgresDatabase run ${db} /tmp/bgFile$$.tar.tz
		rm /tmp/bgFiile$$.tar.gz
	else
		echo "Backgound file not foung"
		exit -1
	fi
	handlePostgresDatabase vacuum ${db} analyze
}

# Handle Stabon Endpoint
# - $1: endpoint
# - $2: command (store/query)
# - $2: file/query
function handleStrabonEndpoint(){
	endpoint=$1
	command=$2
	option=$3

	endpointScript=${loc}/../../scripts/endpoint
	case ${command} in
		store)
			url=${options}

			# print timestamp
			echo -n "${TIMESTAMP} " >> ${logFile}

			tmr1=$(timer)
			${endpointScript} store ${endpoint} N-Triples -u ${url}
			tmr2=$(timer)
			printf '%s ' $((tmr2-tmr1)) >> ${logFile}
			;;
		query)
			query=${option}

			tmr1=$(timer)
			${endpointScript} query ${endpoint} "${query}"
			tmr2=$(timer)
			printf '%s ' $((tmr2-tmr1)) >> ${logFile} 
			;;
		update)
			update=${option}
			tmr1=$(timer)
			${endpointScript} update ${endpoint} "${update}"
			tmr2=$(timer)
			printf '%s ' $((tmr2-tmr1)) >> stderr.txt
			;;
		*)
			echo "ERROR: ..."
			exit -1
			;;
	esac
}

# $1: queryFile
function makeUpdate() {
	queryFile=$1
	instansiateScript=${loc}/../scripts/instantiate.sh
}
# default values
endpoint="http://pathway.di.uoa.gr:8080/endpoint"
db="NOA2012"
hotspots_url="http://pathway.di.uoag.r/hotspots/2012"
bgFile="http://dev.strabon.di.uoa.gr/rdf/Kallikratis-Coastline-Corine-dump-postgres-9.tgz"
logFile="runChain.log"

# read script options
while test $# -gt 0 -a "X${1:0:1}" == "X-"; do
    case "${1}" in
        --help)
            help
            exit 0
            ;;
        -e|--endpoint)
            shift
			endpoint=${1}
            shift
            ;;
        -d|--db)
            shift
			db=${1}
			shift
			;;
		-h|--hotspots)
            shift
			hotspots_url=${1}
			shift
			;;
		-b|--background)
            shift
			bgFile=${1}
			shift
			;;
		-l|--log)
            shift
			logFile=${1}
			shift
			;;
		*)
			echo "unknown argument ${1}"
			help
			exit -1
			;;
	esac
done

echo "endpoint: ${endpoint}"
echo "db: ${db}"
echo "hotspots: ${hotspots_url}"
echo "background: ${bgFile}"
echo "logFile: ${logfile}"


# Get queries and updates
insertMunicipalities=`cat ${loc}/insertMunicipalities.rq`
deleteSeaHotspots=`cat ${loc}/deleteSeaHotspots.rq` # | sed 's/\"/\\\"/g'`
refinePartialSeaHotspots=`cat ${loc}/refinePartialSeaHotspots.rq` # | sed 's/\"/\\\"/g'`
invalidForFires=`cat ${loc}/landUseInvalidForFires.rq`
refineTimePersistence=`cat ${loc}/refineTimePersistence.rq` # | sed 's/\"/\\\"/g'`
discover=`cat ${loc}/discover.rq`
InsertMunicipalities=`cat ${loc}/InsertMunicipalities.sparql` # | sed 's/\"/\\\"/g'`


#Initialize (stop tomcat, restart postgres, drop/create database, start tomcat)
handleTomcatService stop
handlePostgresService restart

# get the main version of postgres
postgres_main_version=$(getPostgresMainVersion)

handlePostgresDatabase drop ${db}
handlePostgresDatabase create ${db}

storeBackgroundData ${db} ${bgFile}

handleTomcatService start

echo "Timestamp Store Municipalities DeleteInSea InvalidForFires RefineInCoast
TimePersistence" $ > stderr.txt

years="2007 2008 2010 2011"
for year in "${years}"; do
for month in `seq 5 9`; do
for day in `seq 1 31`; do
for hour in `seq 0 23`; do
for minute in `seq 0 60 5`; do

		month=$(printf "%02d" ${month})
		day=$(printf "%02d" ${day})k
		hour=$(printf "%02d" ${hour})
		minute=$(printf "%02d" ${minute})k

		# construct timestamp
		timestamp="${year}-${month}-${day}T${hour}:${minute}:00"

		fileURL="HMSG1_RSS_039_s7_${year}${month}${day}_${hour}${minute}.hotspots.nt"
		handleStrabonEndpoint store ${fileURL} ${fileURL}

##		# sudo -u postgres psql -d endpoint -c 'VACUUM ANALYZE;';


##		# insertMunicipalities
##		echo -n "inserting Municipalities " ;echo; echo; echo;
##		# query=`echo "${insertMunicipalities}" `
##		# ${countTime} ./strabon -db endpoint update "${query}"
##
##		tmr1=$(timer)
##
##		query=`echo "${insertMunicipalities}" | sed "s/TIMESTAMP/${TIMESTAMP}/g" | \
##		sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
##		sed "s/SENSOR/${SENSOR}/g"`
##
##		${ENDPOINT_SCRIPT} update ${ENDPOINT} "${query}"
##		
##		tmr2=$(timer)
##		printf '%s ' $((tmr2-tmr1)) >>stderr.txt
##		echo;echo;echo;echo "File ${file} inserted Municipalities!"
##
##		# execute an explicit VACUUM ANALYZE when a query takes longer than it should
##		duration=$((tmr2-tmr1))
##		if test ${duration} -ge 30000; then
##			psql -U postgres ${DB} -c 'VACUUM ANALYZE' 
##			echo "Explicit VACUUM ANALYZE"
##		fi
##		
##		# deleteSeaHotspots
##		echo -n "Going to deleteSeaHotspots ${TIMESTAMP} " ;echo; echo; echo;
##		query=`echo "${deleteSeaHotspots}" | sed "s/TIMESTAMP/${TIMESTAMP}/g" | \
##		sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
##		sed "s/SENSOR/${SENSOR}/g"`
##		# ${countTime} ./strabon -db endpoint update "${query}"
##
##		tmr1=$(timer)
##		${ENDPOINT_SCRIPT} update ${ENDPOINT} "${query}"
##
##		tmr2=$(timer)
##		printf '%s ' $((tmr2-tmr1)) >>stderr.txt
##		echo;echo;echo;echo "File ${file} deleteSeaHotspots done!"
##
##		# invalidForFires
##		echo -n "invalidForFires ${TIMESTAMP} "  ; echo; echo ; echo;
##		query=`echo "${invalidForFires}" | sed "s/TIMESTAMP/${TIMESTAMP}/g" | \
##		sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
##		sed "s/SENSOR/${SENSOR}/g"` 
##
##		# ${countTime} ./strabon -db endpoint update "${query}"
##		tmr1=$(timer)
##		${ENDPOINT_SCRIPT} update ${ENDPOINT} "${query}"
##		tmr2=$(timer)
##		printf '%s ' $((tmr2-tmr1)) >>stderr.txt
##		echo "File ${file} invalidForFires done!"
## 
##		# refinePartialSeaHotspots
##		echo -n "refinePartialSeaHotspots ${TIMESTAMP} "  ; echo; echo ; echo;
##		query=`echo "${refinePartialSeaHotspots}" | sed "s/TIMESTAMP/${TIMESTAMP}/g" | \
##		sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
##		sed "s/SENSOR/${SENSOR}/g" |\
##		sed "s/SAT/${SAT}/g"`
##		# ${countTime} ./strabon -db endpoint update "${query}"
##		tmr1=$(timer)
##		${ENDPOINT_SCRIPT} update ${ENDPOINT} "${query}"
##		tmr2=$(timer)
##		printf '%s ' $((tmr2-tmr1)) >>stderr.txt
##
##		echo "File ${file} refinePartialSeaHotspots done!"
##		# echo "Continue?"
##		# read a
##
##		# refineTimePersistence
##		echo -n "Going to refineTimePersistence ${TIMESTAMP} ";echo;echo;echo; 
##		min_acquisition_time=`date --date="${year}-${month}-${day} ${time2}:00 EEST -30 minutes" +%Y-%m-%dT%H:%M:00`
##		query=`echo "${refineTimePersistence}" | sed "s/TIMESTAMP/${TIMESTAMP}/g" | \
##		sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
##		sed "s/SENSOR/${SENSOR}/g" | \
##		sed "s/ACQUISITIONS_IN_HALF_AN_HOUR/${N_ACQUISITIONS}/g" | \
##		sed "s/MIN_ACQUISITION_TIME/${min_acquisition_time}/g" |\
##		sed "s/SAT/${SAT}/g"`
##
##		#sudo -u postgres psql -d ${DB} -c 'VACUUM ANALYZE;';
##
##		tmr1=$(timer)
##		${ENDPOINT_SCRIPT} update ${ENDPOINT} "${query}"
##		 tmr2=$(timer)
##		printf '%s \n' $((tmr2-tmr1)) >>stderr.txt
##		echo;echo;echo;echo "File ${file} timePersistence done!"
##		# echo "Continue?"
##		# read a
##
##
##		# discover
##		echo -n "Going to discover ${TIMESTAMP} ";echo;echo;echo; 
##		min_acquisition_time=`date --date="${year}-${month}-${day} 00:00 EEST" +%Y-%m-%dT%H:%M:00`
##		max_acquisition_time=`date --date="${year}-${month}-${day} 23:59 EEST" +%Y-%m-%dT%H:%M:00`
##		query=`echo "${discover}" | \
##			sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
##			sed "s/SENSOR/${SENSOR}/g" | \
##			sed "s/MIN_ACQUISITION_TIME/${min_acquisition_time}/g" |\
##			sed "s/MAX_ACQUISITION_TIME/${max_acquisition_time}/g"`
##			
##		tmr1=$(timer)
##		${ENDPOINT_SCRIPT} query ${ENDPOINT} "${query}"
##		tmr2=$(timer)
##		printf '%s \n' $((tmr2-tmr1)) >>discover.txt
##		echo;echo;echo;echo "Discovered hotspots done!"
##	done
##done
##
