#!/bin/bash

name=sbt-launch
version=13.8
localJarName="${name}.jar"
remoteJarName="${name}-${version}.jar"
nexusUrl="https://artifact.dcj.ipt.homeoffice.gov.uk/nexus/service/local/repositories/thirdparty/content"
org="org/scala/sbt"
md5sum="00672c01d5beea62928e33cdeae7b46b"

filePresent() {
	[[ -f ${1} ]]
}
md5Match() {
	[[ "$(openssl md5 ${1}|awk '{print $2}')" == "${2}" ]]
}
downloadUrlAsFilename() {
	curl -o ${2} ${1}
}

if filePresent ${localJarName};then
	if md5Match ${localJarName} ${md5sum};then
		echo "Required ${name} jar already present and correct."
	else
		echo "Incorrect or corrupt ${name} jar present - removing..."
		rm -f ${localJarName}
	fi
fi

if ! filePresent ${localJarName} ]];then
	downloadUrlAsFilename "${nexusUrl}/${org}/${name}/${version}/${remoteJarName}" "${localJarName}"
	while ! md5Match ${localJarName} ${md5sum};do
		read response
		while [[ "${response}" != "n"
				&& "${response}" != "y"
				&& "${response}" != "N"
				&& "${response}" != "Y" ]];do
			echo -n "File did not download as expected, retry? y/n : "
			read response
			if [[ "${response}" == "n" || "${response}" == "N" ]];then
				echo "Okay."
				exit 1
			fi
		done
	done
fi



filePresent ~/.sbtconfig && . ~/.sbtconfig

 # -Dsbt.task.timings=true         \

# I have added the option -Dscalac.patmat.analysisBudget=off, to prevent this compiler warning:
#       Identifiers.scala:55: Cannot check match for unreachability.
# For avoidance of doubt, note that this DOES NOT prevent the compiler's checking for
# match may not be exhaustive. Which is good, because we want that check to be made.
# It also has the useful side effect of saving a few seconds on each compile.
java -ea                          \
  $SBT_OPTS                       \
  $JAVA_OPTS                      \
  -Dscalac.patmat.analysisBudget=off \
  -Djava.net.preferIPv4Stack=true \
  -XX:+AggressiveOpts             \
  -XX:+UseParNewGC                \
  -XX:+UseConcMarkSweepGC         \
  -XX:+CMSParallelRemarkEnabled   \
  -XX:+CMSClassUnloadingEnabled   \
  -XX:SurvivorRatio=128           \
  -XX:MaxTenuringThreshold=0      \
  -XX:-PrintGCDetails             \
  -XX:-PrintGCTimeStamps          \
  -Xloggc:/tmp/sbt_gc.log         \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:+UseGCLogFileRotation       \
  -XX:NumberOfGCLogFiles=2        \
  -XX:GCLogFileSize=1M            \
  -server                         \
  -XX:MaxMetaspaceSize=2g -Xms1024m -Xmx4g -XX:ReservedCodeCacheSize=512m \
  -jar ${localJarName} "$@"
