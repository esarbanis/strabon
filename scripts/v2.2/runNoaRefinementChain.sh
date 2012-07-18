#!/bin/bash
LOC="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

ENDPOINT="http://localhost:8080/endpoint"
DB="endpoint"
#GRIDURL="http://kk.di.uoa.gr/grid_4.nt"
#GRIDURL="http://jose.di.uoa.gr/rdf/coastline/grid_4.nt"
#INIT="http://jose.di.uoa.gr/rdf/Kallikratis-Coastline.ntriples"
#INIT="../Kalli_coast.sql"

#CHECKDIR="/home/konstantina/allhot/"
#CHECKDIR="${HOME}/teleios/nkua/Hotspots/"

#POSTGISTEMPLATE="postgistemplate"
POSTGISTEMPLATE="template_postgis"

#dataDir="http://localhost/noa-teleios/out_triples/"
#dataDir="http://kk.di.uoa.gr/out_triples/"
#dataDir="http://godel.di.uoa.gr/allhot/"
dataDir="http://jose.di.uoa.gr/rdf/hotspots/20"
name="HMSG2_IR_039_s7_"
suffix=".hotspots.nt"

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
}

insertMunicipalities=`cat ${LOC}/InsertMunicipalities.sparql` 
deleteSeaHotspots=`cat ${LOC}/DeleteInSea.sparql` # | sed 's/\"/\\\"/g'`
refinePartialSeaHotspots=`cat ${LOC}/Refine.sparql` # | sed 's/\"/\\\"/g'`
refineTimePersistence=`cat ${LOC}/TimePersistence.sparql` # | sed 's/\"/\\\"/g'`
invalidForFires=`cat ${LOC}/InvalidForFires.sparql`
discover=`cat ${LOC}/discover.sparql`
#InsertMunicipalities =`cat ${LOC}/InsertMunicipalities.sparql` # | sed 's/\"/\\\"/g'`


# Initialize
chooseTomcat
sudo service postgresql restart
echo "Dropping endpoint database";
sudo -u postgres dropdb ${DB}
echo "Creating endpoint database"
sudo -u postgres createdb ${DB} 
sudo -u postgres sh -c "curl -s  http://dev.strabon.di.uoa.gr/rdf/Kallikratis-Coastline-Corine-dump.tgz|tar xz -O|psql -d ${DB}"
sudo -u postgres psql ${DB} -c 'VACUUM ANALYZE '
echo "restarting tomcat"
sudo service ${tomcat} restart

echo "initializing database"
echo "Store Municipalities DeleteInSea Corine RefineInCoastline TimePersistence" >stderr.txt


#./scripts/endpoint query ${ENDPOINT} "SELECT (COUNT(*) AS ?C) WHERE {?s ?p ?o}"
#sudo -u postgres psql -d endpoint -c 'CREATE INDEX datetime_values_idx_value ON datetime_values USING btree(value)';
#sudo -u postgres psql -d endpoint -c 'VACUUM ANALYZE;';

#echo "Continue?"
#read a



#for y in 7 8 10 11 ;do
#for mon in `seq 4 10`; do
#for d in `seq 1 30`; do
#for h in `seq 0 23 `; do
#    for m in `seq 0 15 45`; do
for y in 7  ;do
for mon in 8; do
for d in 25; do
for h in `seq 0 23 `; do
    for m in `seq 0 15 45`; do
            time=`printf "%02d%02d\n" $h $m`
            time2=`printf "%02d:%02d\n" $h $m`
	    day=`printf "%02d" $d`
            month=`printf "%02d" $mon`
            year=`printf "%02d" $y`
            file=${dataDir}${year}/${name}${year}${month}${day}_${time}$suffix
#            file=${dataUrl}${name}_${time}$suffix

   		 check=${dataDir}${year}/${name}${year}${month}${day}_${time}$suffix
		 wget -q --spider $check
   	
	      if [[   $? -ne 0 ]];
              then echo "FILE" $check "NOT EXISTS" ; continue
	      fi


            # store file
            echo -n "storing " $file; echo; echo; 
	  # echo "Hotspot : " $h:$m >> stderr.txt
#            ${countTime} ./strabon -db endpoint store $file

 tmr1=$(timer)
            ../endpoint store ${ENDPOINT} N-Triples -u ${file}
 tmr2=$(timer)
printf '%s ' $((tmr2-tmr1)) >>stderr.txt

	   # sudo -u postgres psql -d endpoint -c 'VACUUM ANALYZE;';

            echo;echo;echo;echo "File ${file} stored!" >> ${logFile}

            # insertMunicipalities
            echo -n "inserting Municipalities " ;echo; echo; echo;
           # query=`echo "${insertMunicipalities}" `
#            ${countTime} ./strabon -db endpoint update "${query}"

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
#            ${countTime} ./strabon -db endpoint update "${query}"

tmr1=$(timer)
  ../endpoint update ${ENDPOINT} "${query}"

tmr2=$(timer)
printf '%s ' $((tmr2-tmr1)) >>stderr.txt

            echo;echo;echo;echo "File ${file} deleteSeaHotspots done!"
#            echo "Continue?"
#            read a
            # invalidForFires
            echo -n "invalidForFires 20${year}-${month}-${day}T${time2}:00 "  ; echo; echo ; echo;
            query=`echo "${invalidForFires}" | sed "s/TIMESTAMP/20${year}-${month}-${day}T${time2}:00/g" | \
                sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
                sed "s/SENSOR/MSG2/g" |\
		sed "s/SAT/METEOSAT9/g"`
#            ${countTime} ./strabon -db endpoint update "${query}"
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
#            ${countTime} ./strabon -db endpoint update "${query}"
tmr1=$(timer)
              ../endpoint update ${ENDPOINT} "${query}"
tmr2=$(timer)
printf '%s ' $((tmr2-tmr1)) >>stderr.txt

            echo "File ${file} refinePartialSeaHotspots done!"
#            echo "Continue?"
#            read a

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
#            echo "Continue?"
#            read a
    done #minutes
done #hours
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
            
done #days
done #months
done #years
