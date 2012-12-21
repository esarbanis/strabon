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
# Author: George Garbis <ggarbis@di.uoa.gr>
# Author: Charalampos (Babis) Nikolaou <charnik@di.uoa.gr>
# Author: Manos Karpathiotakis <mk@di.uoa.gr>
# Author: Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
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

	echo "Service ${postgres} received command: $1"
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
            if test "${options}" = "spatial"; then
                options="-T template_postgis"
            elif test ! -z "${options}"; then
				echo "ERROR: only spatial is allowed for create option"
				echo "options: ${options}"
				exit -1
			fi
			echo "Creating database ${db}... with options ${options}"
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
				psql -U postgres ${db} -c 'VACUUM ANALYZE' 
                echo "VACUUM ANALYZE ${db}"
			else
				psql -U postgres ${db} -c 'VACUUM' 
                echo "VACUUM ${db}"
			fi
			;;
		runscript)
			if test ! -f "${options}"; then
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
		handlePostgresDatabase runscript ${db} ${bgFile}	
	elif test "${bgFile:0:7}" = "http://"; then
		#curl -s ${bgFile} | tar xz -O | psql -d ${DB}
		wget ${bgFile} -O /tmp/bgFile$$.tar.gz
		tar xz -O /tmp/bgFile$$.tar.gz
		
		handlePostgresDatabase runscript ${db} /tmp/bgFile$$.tar.tz
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
	options=$3

	endpointScript=${loc}/../../scripts/endpoint
	case ${command} in
		store)
			url=${options}

			tmr1=$(timer)
			${endpointScript} store ${endpoint} N-Triples -u ${url}
			tmr2=$(timer)

    		# execute an explicit VACUUM ANALYZE when a query takes longer than it should
    		duration=$((tmr2-tmr1))
    		if test ${duration} -ge 30000; then
                handlePostgresDatabase vacuum ${db} analyze
#    			psql -U postgres ${DB} -c 'VACUUM ANALYZE' 
#    			echo "Explicit VACUUM ANALYZE"
        	    tmr2=$(timer)
    		fi
			printf '%s ' $((tmr2-tmr1)) >> ${logFile}
			;;
		query)
			query=${options}
			tmr1=$(timer)
			${endpointScript} query ${endpoint} "${query}"
			tmr2=$(timer)
			printf '%s ' $((tmr2-tmr1)) >> ${logFile} 
			;;
		update)
			update=${options}
			tmr1=$(timer)
			${endpointScript} update ${endpoint} "${update}"
			tmr2=$(timer)
			printf '%s ' $((tmr2-tmr1)) >> ${logFile}
			;;
		*)
			echo "ERROR: Unknown endpoint command"
			exit -1
			;;
	esac
}

# default values
endpoint="http://pathway.di.uoa.gr:8080/endpoint"
db="NOA2012"
hotspotsURL="http://pathway.di.uoa.gr/hotspots"
bgFile="http://dev.strabon.di.uoa.gr/rdf/Kallikratis-Coastline-Corine-dump-postgres-9.tgz"
logFile="runChain.log"

chain="DynamicThresholds"
persistence=10
repeatInPers=3

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
echo "hotspots: ${hotspotsURL}"
echo "background: ${bgFile}"
echo "logFile: ${logFile}"

instantiate=${loc}/instantiate.sh

#Initialize (stop tomcat, restart postgres, drop/create database, start tomcat)
handleTomcatService stop
handlePostgresService restart

handlePostgresDatabase drop ${db}
handlePostgresDatabase create ${db}

storeBackgroundData ${db} ${bgFile} # ~/Temp/Kallikratis-Coastline-Corine-postgres-9.sql

handleTomcatService start

#${loc}/../../scripts/endpoint query ${endpoint} size 
#exit -1
echo "Timestamp Store Municipalities DeleteInSea InvalidForFires DeleteReflections RefineInCoast TimePersistence" > ${logFile}

years="2012" #"2007 2008 2010 2011"
for y in ${years}; do
    hotspots="`ls /var/www/hotspots/${y} | sort | grep -o 'HMSG.*\.nt'`"
	# get hotpost URLS
#	for hot in $(curl -s ${hotspotsURL}/${y}/ | grep -o '>HMSG.*\.nt' | colrm 1 1); do
	for hot in ${hotspots}; do
		file="${hotspotsURL}/${y}/${hot}"

		time_status=$(echo ${hot} | egrep -o '[[:digit:]]{6}_[[:digit:]]{4}')
		
		# get sensor
		sensor=$(echo ${hot} | grep -o 'MSG.')

		# get satellite and set number of acquisitions per hour
		if test "${sensor}" = "MSG1"; then
			sensor="MSG1_RSS"
		fi

		# get time information for acquisition and construct timestamp
		year="20$(expr substr ${time_status} 1 2)"
		month=$(expr substr ${time_status} 3 2)
		day=$(expr substr ${time_status} 5 2)
		time2=$(expr substr ${time_status} 8 2)
		time2="${time2}:$(expr substr ${time_status} 10 2)"

		# construct timestamp
		timestamp="${year}-${month}-${day}T${time2}:00"
		# print timestamp
		echo -n "${timestamp} " >> ${logFile}
        
		handleStrabonEndpoint ${endpoint} store ${file}
        echo "Store ${file}" ; # read t

		# Insert Municipalities	
		update="`${instantiate} -t ${timestamp} -c ${chain} -s ${sensor} ${loc}/insertMunicipalities.rq`"
#        echo "Insert Municipalities: ${update}" ; read t
        handleStrabonEndpoint ${endpoint} update "${update}"


		# Delete Sea Hotspots
		update="`${instantiate} -t ${timestamp} -c ${chain} -s ${sensor} ${loc}/deleteSeaHotspots.rq`"
#       echo "Delete Sea Hotspots: ${update}" ; read t
		handleStrabonEndpoint ${endpoint} update "${update}"

		# Invalid For Fires
		update="`${instantiate} -t ${timestamp} -c ${chain} -s ${sensor} ${loc}/landUseInvalidForFires.rq`"
#       echo "Invalid For Fires: ${update}" ; read t
		handleStrabonEndpoint ${endpoint} update "${update}"
 
        # Delete Reflections
    	minTime=`date --date="${year}-${month}-${day} ${time2}:00 EEST -60 minutes" +%Y-%m-%dT%H:%M:00`
		update="`${instantiate} -t ${timestamp} -c ${chain} -s ${sensor} -m ${minTime} ${loc}/deleteReflections.rq`"
        echo "Delete Reflections: ${update}" ;
		handleStrabonEndpoint ${endpoint} update "${update}"

		# Refine Partial Sea Hotspots
		update="`${instantiate} -t ${timestamp} -c ${chain} -s ${sensor} ${loc}/refinePartialSeaHotspots.rq`"
#       echo "Refine Partial Sea Hotspots: ${update}" ; read t
		handleStrabonEndpoint ${endpoint} update "${update}"

		# Refine Time Persistence
		minTime=`date --date="${year}-${month}-${day} ${time2}:00 EEST -30 minutes" +%Y-%m-%dT%H:%M:00`
		update="`${instantiate} -t ${timestamp} -c ${chain} -s ${sensor} -m ${minTime} ${loc}/refineTimePersistence.rq`"
#       echo "Refine Time Persistence: ${update}" ; read t
		handleStrabonEndpoint ${endpoint} update "${update}"

##		#sudo -u postgres psql -d ${DB} -c 'VACUUM ANALYZE;';
        
		# Discover
		minTime=`date --date="${year}-${month}-${day} 00:00 EEST" +%Y-%m-%dT%H:%M:00`
		maxTime=`date --date="${year}-${month}-${day} 23:59 EEST" +%Y-%m-%dT%H:%M:00`
        query="`${instantiate} -c ${chain} -s ${sensor} -m ${minTime} -M ${maxTime} ${loc}/discover.rq`"
       echo "Discover: ${query}" ; read t
		handleStrabonEndpoint ${endpoint} query "${query}" | tee /home/ggarbis/discover.log
    
		# Discover Fires
		minTime=`date --date="${year}-${month}-${day} 00:00 EEST" +%Y-%m-%dT%H:%M:00`
		maxTime=`date --date="${year}-${month}-${day} 23:59 EEST" +%Y-%m-%dT%H:%M:00`
        query="`${instantiate} -c ${chain} -s ${sensor} -m ${minTime} -M ${maxTime} -p 10 -r 3 ${loc}/discoverFires.rq`"
        echo "Discover Fires: ${query}" ; #read t
		handleStrabonEndpoint ${endpoint} query "${query}" #| tee /home/ggarbis/discoverFires.log
#    
        echo >> ${logFile}    
	done
done
