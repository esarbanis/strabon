#!/bin/bash
LOC="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

ENDPOINT="http://localhost:8080/endpoint"
DB="endpoint"
#GRIDURL="http://kk.di.uoa.gr/grid_4.nt"
GRIDURL="http://jose.di.uoa.gr/rdf/coastline/grid_4.nt"
#INIT="http://jose.di.uoa.gr/rdf/Kallikratis-Coastline.ntriples"
INIT="../Kalli_coast.sql"

#CHECKDIR="/home/konstantina/allhot/"
#CHECKDIR="${HOME}/teleios/nkua/Hotspots/"

POSTGISTEMPLATE="postgistemplate"
#POSTGISTEMPLATE="template_postgis"

#dataDir="http://localhost/noa-teleios/out_triples/"
#dataDir="http://kk.di.uoa.gr/out_triples/"
#dataDir="http://godel.di.uoa.gr/allhot/"
dataDir="http://jose.di.uoa.gr/rdf/hotspots/20"
name="HMSG2_IR_039_s7_"
suffix=".hotspots.nt"

HOTSPOTS_URL="http://jose.di.uoa.gr/rdf/hotspots"

logFile="chain.log"
#countWTime="/usr/bin/time -p   %e"
#echo > ${logFile}

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

# find out the postgres service to use
postgres=$(ls -1 /etc/init.d/| grep postgres | head -1)

tomcat=
function chooseTomcat()
{
	if test -s /etc/fedora-release ; then
		tomcat="tomcat"
	#elif test -s /etc/centos-release ; then
	#elif test -s /etc/yellowdog-release ; then
	#elif test -s /etc/redhat-release ; then
	#elif test -s /etc/SuSE-release ; then
	#elif test -s /etc/gentoo-release ; then
	elif test -s /etc/lsb-release ; then # Ubuntu
			tomcat="tomcat7"
	elif test -s /etc/debian_version ; then
			tomcat="tomcat"
	fi

	# check for service availability
	if ! test -e "/etc/init.d/${tomcat}"; then
		tomcat=
	fi
}

insertMunicipalities=`cat ${LOC}/insertMunicipalities.sparql` 
deleteSeaHotspots=`cat ${LOC}/deleteSeaHotspots.sparql` # | sed 's/\"/\\\"/g'`
refinePartialSeaHotspots=`cat ${LOC}/refinePartialSeaHotspots.sparql` # | sed 's/\"/\\\"/g'`
refineTimePersistence=`cat ${LOC}/refineTimePersistence.sparql` # | sed 's/\"/\\\"/g'`
invalidForFires=`cat ${LOC}/landUseInvalidForFires.sparql`
discover=`cat ${LOC}/discover.sparql`
#InsertMunicipalities =`cat ${LOC}/InsertMunicipalities.sparql` # | sed 's/\"/\\\"/g'`

# Initialize (stop tomcat, restart postgres, drop/create database, start tomcat)
chooseTomcat
echo "stopping tomcat"
if test -z "${tomcat}"; then
	# work-around for babis (standalone tomcat, with start_tomcat.sh and stop_tomcat.sh scripts)
	stop_tomcat.sh
else
	sudo service ${tomcat} stop
fi

sudo service ${postgres} restart

# get the main version of postgres
POSTGRES_MAIN_VERSION=$(sudo service ${postgres} status | grep -o '.\..' | cut -b 1)

echo "Dropping endpoint database";
dropdb -U postgres ${DB}

echo "Creating endpoint database"
createdb -U postgres ${DB} 

# load data
curl -s  http://dev.strabon.di.uoa.gr/rdf/Kallikratis-Coastline-Corine-dump-postgres-${POSTGRES_MAIN_VERSION}.tgz | tar xz -O | psql -U postgres -d ${DB}
psql ${DB} -U postgres -c 'VACUUM ANALYZE '

echo "starting tomcat"
if test -z "${tomcat}"; then
	# work-around for babis (standalone tomcat, with start_tomcat.sh and stop_tomcat.sh scripts)
	start_tomcat.sh
else
	sudo service ${tomcat} start
fi

echo "initializing database"
echo "S M D IF R TP" >stderr.txt


#./scripts/endpoint query ${ENDPOINT} "SELECT (COUNT(*) AS ?C) WHERE {?s ?p ?o}"
#sudo -u postgres psql -d endpoint -c 'CREATE INDEX datetime_values_idx_value ON datetime_values USING btree(value)';
#sudo -u postgres psql -d endpoint -c 'VACUUM ANALYZE;';

#echo "Continue?"
#read a

#for y in 2008; do
for y in 2007 2008 2010 2011 ;do
	# get hotpost URLS
	for hot in $(curl -s ${HOTSPOTS_URL}/${y}/ | grep -o '>HMSG2.*\.nt' | colrm 1 1); do
		file="${HOTSPOTS_URL}/${y}/${hot}"

		# get time information for acquisition
		year=${y}
		month=$(expr substr ${hot} 19 2)
		day=$(expr substr ${hot} 21 2)
		time2=$(expr substr ${hot} 24 2)
		time2="${time2}:$(expr substr ${hot} 26 2)"

		# store file
		echo -n "storing " $file; echo; echo; 
		# ${countTime} ./strabon -db endpoint store $file

		tmr1=$(timer)
		../endpoint store ${ENDPOINT} N-Triples -u ${file}
		tmr2=$(timer)
		printf '%s ' $((tmr2-tmr1)) >> stderr.txt

		# sudo -u postgres psql -d endpoint -c 'VACUUM ANALYZE;';

		echo;echo;echo;echo "File ${file} stored!" >> ${logFile}

		# insertMunicipalities
		echo -n "inserting Municipalities " ;echo; echo; echo;
		# query=`echo "${insertMunicipalities}" `
		# ${countTime} ./strabon -db endpoint update "${query}"

		tmr1=$(timer)

		query=`echo "${insertMunicipalities}" | sed "s/TIMESTAMP/20${year}-${month}-${day}T${time2}:00/g" | \
		sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
		sed "s/SENSOR/MSG2/g"`

		../endpoint update ${ENDPOINT} "${query}"
		
		tmr2=$(timer)
printf '%s ' $((tmr2-tmr1)) >>stderr.txt
		echo;echo;echo;echo "File ${file} inserted Municipalities!"
		
		# deleteSeaHotspots
		echo -n "Going to deleteSeaHotspots 20${year}-${month}-${day}T${time2}:00 " ;echo; echo; echo;
		query=`echo "${deleteSeaHotspots}" | sed "s/TIMESTAMP/20${year}-${month}-${day}T${time2}:00/g" | \
		sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
		sed "s/SENSOR/MSG2/g"`
		# ${countTime} ./strabon -db endpoint update "${query}"

		tmr1=$(timer)
		../endpoint update ${ENDPOINT} "${query}"

		tmr2=$(timer)
		printf '%s ' $((tmr2-tmr1)) >>stderr.txt
		echo;echo;echo;echo "File ${file} deleteSeaHotspots done!"

		# echo "Continue?"
		# read a
			# invalidForFires
		echo -n "invalidForFires 20${year}-${month}-${day}T${time2}:00 "  ; echo; echo ; echo;
		query=`echo "${invalidForFires}" | sed "s/TIMESTAMP/20${year}-${month}-${day}T${time2}:00/g" | \
		sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
		sed "s/SENSOR/MSG2/g" |\
		sed "s/SAT/METEOSAT9/g"`
		# ${countTime} ./strabon -db endpoint update "${query}"
		tmr1=$(timer)
		../endpoint update ${ENDPOINT} "${query}"
		tmr2=$(timer)
		printf '%s ' $((tmr2-tmr1)) >>stderr.txt
		echo "File ${file} invalidForFires done!"
 
		# refinePartialSeaHotspots
		echo -n "refinePartialSeaHotspots 20${year}-${month}-${day}T${time2}:00 "  ; echo; echo ; echo;
		query=`echo "${refinePartialSeaHotspots}" | sed "s/TIMESTAMP/20${year}-${month}-${day}T${time2}:00/g" | \
		sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
		sed "s/SENSOR/MSG2/g" |\
		sed "s/SAT/METEOSAT9/g"`
		# ${countTime} ./strabon -db endpoint update "${query}"
		tmr1=$(timer)
		../endpoint update ${ENDPOINT} "${query}"
		tmr2=$(timer)
		printf '%s ' $((tmr2-tmr1)) >>stderr.txt

		echo "File ${file} refinePartialSeaHotspots done!"
		# echo "Continue?"
		# read a

		# refineTimePersistence
		echo -n "Going to refineTimePersistence 20${year}-${month}-${day}T${time2}:00 ";echo;echo;echo; 
		min_acquisition_time=`date --date="20${year}-${month}-${day} ${time2}:00 EEST -30 minutes" +%Y-%m-%dT%H:%m:00`
		query=`echo "${refineTimePersistence}" | sed "s/TIMESTAMP/20${year}-${month}-${day}T${time2}:00/g" | \
		sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
		sed "s/SENSOR/MSG2/g" | \
		sed "s/ACQUISITIONS_IN_HALF_AN_HOUR/3.0/g" | \
		sed "s/MIN_ACQUISITION_TIME/${min_acquisition_time}/g" |\
		sed "s/SAT/METEOSAT9/g"`

		#sudo -u postgres psql -d ${DB} -c 'VACUUM ANALYZE;';

		tmr1=$(timer)
		../endpoint update ${ENDPOINT} "${query}"
		 tmr2=$(timer)
		printf '%s \n' $((tmr2-tmr1)) >>stderr.txt
		echo;echo;echo;echo "File ${file} timePersistence done!"
		# echo "Continue?"
		# read a


		# discover
		echo -n "Going to discover 20${year}-${month}-${day}T${time2}:00 ";echo;echo;echo; 
		min_acquisition_time=`date --date="20${year}-${month}-${day} 00:00 EEST" +%Y-%m-%dT%H:%m:00`
		max_acquisition_time=`date --date="20${year}-${month}-${day} 23:59 EEST" +%Y-%m-%dT%H:%m:00`
		query=`echo "${discover}" | \
			sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
			sed "s/SENSOR/MSG2/g" | \
			sed "s/MIN_ACQUISITION_TIME/${min_acquisition_time}/g" |\
			sed "s/MAX_ACQUISITION_TIME/${max_acquisition_time}/g"`
			
		tmr1=$(timer)
		../endpoint query ${ENDPOINT} "${query}"
		tmr2=$(timer)
		printf '%s \n' $((tmr2-tmr1)) >>discover.txt
		echo;echo;echo;echo "Discovered hotspots done!"

	done
done

