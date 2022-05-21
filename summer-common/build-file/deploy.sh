jarN=$1
dockerN=$2
jar_env=$3

if [[ -z ${jar_env} ]] ; then
    jar_env=dev
fi

STARTED=`docker ps | grep $dockerN | wc -l`
echo "started: $STARTED"
if [ $STARTED -ge 1 ];then
  docker stop $dockerN
fi
if [ $STARTED -ge 1 ];then
  docker stop $dockerN
fi
STOPPED=`docker ps -a | grep $dockerN | wc -l`
echo "stopped: $STOPPED"
if [ $STOPPED -ge 1 ];then
  docker rm $dockerN
fi

echo "string ..."

docker run -d --restart always \
       --net=host  \
       -v /data/deploys/${jarN}:/mnt/${jarN} \
       --name=${dockerN} \
       -e JAVA_NAME=${jarN} \
       -e JAVA_EVN=${jar_env} \
       sacher/spring-boot:font

port=`echo ${dockerN} | awk -F "-" '{print $NF}'`
ip=127.0.0.1

echo "======================================"
echo "curl http://$ip:$port/actuator/health"
echo "======================================"

# check running
flag=0
index=50
while [ $flag = 0 ] && [ $index -gt 1 ]; do
  sleep 2
  flag=$(curl -s http://$ip:$port/actuator/health | jq -r '.status' | grep 'UP' | wc -l)
  index=$(($index - 1))
done

if [ $flag = 1 ]; then
  echo "deploy success"
else
  tail -f /data/deploys/$jarN/nohup.out
fi
