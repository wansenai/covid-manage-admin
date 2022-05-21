##### Spring Boot 镜像生成（HOST:PORT 是镜像仓库地址）
````
cd spring-boot-dok
docker build -t HOST:PORT/tob/spring-boot:2.0 .

docker push HOST:PORT/tob/spring-boot:2.0

docker build -t sacher/spring-boot:fount .
````
##### 运行 Spring Boot Docker 容器
````
（必填）参数： JAR_NAME 需要运行的JAR名
（必填）参数： JAR_ENV  运行JAR的环境
（可选）参数： JAR_URL  JAR的下载路径, 如果不传这个参数请确保最新JAR已经存放在本地挂载目录$HOST_VOLUME中  
      注意： $HOST_VOLUME目录最后的子目录名为 $JAR_NAME 

### jvm optimization args       
（可选）参数： JVM_Xms  jvm 的最小 heap 大小，建议和-Xmx一样， 防止因为内存收缩／突然增大带来的性能影响。默认值1024M
（可选）参数： JVM_Xmx  jvm 的最大 heap 大小。默认值1024M
（可选）参数： JVM_Xmn  jvm 中 New Generation 的大小，这个参数很影响性能，如果你的程序需要比较多的临时内存，建议设置到512M，如果用的少，尽量降低这个数值，一般来说128／256足以使用了。默认值512M
（可选）参数： JVM_NewRatio  年老代和新生代的堆内存占用比例，即-XX:NewRatio=老年代/新生代，不允许-XX:Newratio值小于1，默认值 1
（可选）参数： JVM_MaxDirectMemorySize  Direct Memory使用到达了这个大小，就会强制触发Full GC。默认值1024M
      注意： 当系统运行中出现 JVM_Xmx + JVM_MaxDirectMemorySize 大于物理内存则会由于oom导致java进程退出

docker run -d --restart always 
       -p $port:$port \
       -v /mnt:$HOST_VOLUME
       --name=$JAR_NAME \
       --hostname=$HOST_NAME \
       -e JAR_NAME=$JAR_NAME \
       -e JAR_ENV=$JAR_ENV \
       -e JAR_URL=$JAR_URL \
       HOST:PORT/tob/spring-boot:2.0
