#! /bin/sh

MCSIM_HOME=/mc.sim
LOG_DIR=${MCSIM_HOME}/tmp
MCSIM_VERSION=1.0-SNAPSHOT

if [[ ! -e ${LOG_DIR} ]]; then
    mkdir -p ${LOG_DIR}
fi

cd ${MCSIM_HOME}
nohup java -cp .:lib:mc.sim-server-${MCSIM_VERSION}.jar -Dlog4j.configurationFile="file:${MCSIM_HOME}/properties/log4j2.xml" com.nuevatel.mcsim.server.ServerApp $1 $2 $3 > ${LOG_DIR}/mc.sim-server.tmp &
