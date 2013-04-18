#!/bin/bash
# check your ~/.m2/settings.xml for the correct values
DEPLOY_REPO_ID="strabon.testing"
DEPLOY_REPO_URL="http://maven.strabon.di.uoa.gr/content/repositories/testing/"
TEMP_DIR="/tmp/deploy-local-repo-$$"

mkdir ${TEMP_DIR}
if [[ ! -d "${TEMP_DIR}" ]] ; then
	echo "Could not create temporary directory."
	echo "Aborting..."
	exit
fi


for d in `find ${HOME}/.m2/repository -type d` ;
do
	#for each directory
	cd ${d}
	children=`find . -type d|wc -l`
	if [[ "${children}" -ne "1" ]] ; then
		# if the directory has more subdirectories, move one
		continue;
	fi

	countPoms=`ls -1 *.pom 2>/dev/null|wc -l`
	countJars=`ls -1 *.pom 2>/dev/null|wc -l`

	if [[ "${countPoms}" -gt "1" ]] && [[ "${countJars}" -gt "1" ]] ; then
		echo "Found ${countPoms} poms and ${countJars} jars in directory '${d}'."
		echo "Aborting..."
		exit;
	elif [[ "${countPoms}" -eq "0" ]] ; then
		echo "No .pom file found in directory '${d}'."
		echo "Aborting..."
		exit;
	fi

	pomFile=`ls -1 *.pom 2>/dev/null`
	jarFile=`ls -1 *.jar 2>/dev/null`
	cp ${pomFile} ${TEMP_DIR}/${pomFile} 2>/dev/null
	cp ${jarFile} ${TEMP_DIR}/${jarFile} 2>/dev/null


	if [[ "${countPoms}" -eq "1" ]] && [[ "${countJars}" -eq "1" ]] ; then
		# deploy the local jar file to the remote repo
		mvn deploy:deploy-file \
			-DrepositoryId=${DEPLOY_REPO_ID} \
			-Durl=${DEPLOY_REPO_URL} \
			-DpomFile=${TEMP_DIR}/${pomFile} \
			-Dfile=${TEMP_DIR}/${jarFile};
	elif [[ "${countPoms}" -eq "1" ]] && [[ "${countJars}" -eq "0" ]] ; then
		# deploy the local pom file to the remote repo
		mvn deploy:deploy-file \
			-DrepositoryId=${DEPLOY_REPO_ID} \
			-Durl=${DEPLOY_REPO_URL} \
			-DpomFile=${TEMP_DIR}/${pomFile} \
			-Dfile=${TEMP_DIR}/${pomFile};
	else
		echo "Found ${countPoms} poms and ${countJars} jars in directory '${d}'."
		echo "What should I do?"
		echo "Aborting..."
		exit;
	fi

	pomFile=`ls -1 *.pom`
	jarFile=`ls -1 *.jar`

	cp ${pomFile} ${TEMP_DIR}/${pomFile}
	cp ${jarFile} ${TEMP_DIR}/${jarFile}

	# deploy the local file to the remote repo
	mvn deploy:deploy-file \
		-DrepositoryId=${DEPLOY_REPO_ID} \
		-Durl=${DEPLOY_REPO_URL} \
		-DpomFile=${TEMP_DIR}/${pomFile} \
		-Dfile=${TEMP_DIR}/${jarFile};

	# grooming
	rm ${TEMP_DIR}/*
done


# grooming
rm -rf ${TEMP_DIR}
