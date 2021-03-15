envValue=$1
APP_NAME=$2
PEN_NAMESPACE=$3
COMMON_NAMESPACE=$4
APP_NAME_UPPER=${APP_NAME^^}
TZVALUE="America/Vancouver"
SOAM_KC_REALM_ID="master"
KCADM_FILE_BIN_FOLDER="/tmp/keycloak-9.0.3/bin"
SOAM_KC=soam-$envValue.apps.silver.devops.gov.bc.ca

oc project $PEN_NAMESPACE-$envValue
SPLUNK_TOKEN=$(oc -o json get configmaps "${APP_NAME}"-"${envValue}"-setup-config | sed -n "s/.*\"SPLUNK_TOKEN_${APP_NAME_UPPER}\": \"\(.*\)\"/\1/p")
NATS_CLUSTER=educ_nats_cluster
NATS_URL="nats://nats.${COMMON_NAMESPACE}-${envValue}.svc.cluster.local:4222"
oc project $PEN_NAMESPACE-tools

###########################################################
#Setup for config-map
###########################################################
SPLUNK_URL="gww.splunk.educ.gov.bc.ca"
FLB_CONFIG="[SERVICE]
   Flush        1
   Daemon       Off
   Log_Level    debug
   HTTP_Server   On
   HTTP_Listen   0.0.0.0
   HTTP_Port     2020
[INPUT]
   Name   tail
   Path   /mnt/log/*
   Mem_Buf_Limit 20MB
[FILTER]
   Name record_modifier
   Match *
   Record hostname \${HOSTNAME}
[OUTPUT]
   Name   stdout
   Match  *
[OUTPUT]
   Name  splunk
   Match *
   Host  $SPLUNK_URL
   Port  443
   TLS         On
   TLS.Verify  Off
   Message_Key $APP_NAME
   Splunk_Token $SPLUNK_TOKEN
"

echo
echo Creating config map $APP_NAME-config-map
oc create -n "$PEN_NAMESPACE"-"$envValue" configmap $APP_NAME-config-map --from-literal=TZ=$TZVALUE  --from-literal=SPRING_WEB_LOG_LEVEL=DEBUG --from-literal=APP_LOG_LEVEL=INFO  --from-literal=SPRING_SHOW_REQUEST_DETAILS=false --from-literal=TOKEN_ISSUER_URL="https://$SOAM_KC/auth/realms/$SOAM_KC_REALM_ID" --from-literal=URL_REDIS="redis.$PEN_NAMESPACE-$envValue.svc.cluster.local:6379" --from-literal=URL_API_STUDENT_V1="http://student-api-master.$COMMON_NAMESPACE-$envValue.svc.cluster.local:8080/api/v1/student" --dry-run -o yaml | oc apply -f -
echo
echo Setting environment variables for $APP_NAME-main application
oc project $PEN_NAMESPACE-$envValue
oc set env --from=configmap/$APP_NAME-config-map dc/$APP_NAME-main

echo Creating config map "$APP_NAME"-flb-sc-config-map
oc create -n "$PEN_NAMESPACE"-"$envValue" configmap "$APP_NAME"-flb-sc-config-map --from-literal=fluent-bit.conf="$FLB_CONFIG" --dry-run -o yaml | oc apply -f -
