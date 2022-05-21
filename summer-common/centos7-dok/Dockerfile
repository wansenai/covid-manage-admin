FROM centos:7

MAINTAINER sacher <sacherer@163.com>

ADD ./jdk1.8.0_271 /usr/local
ENV JAVA_HOME /usr/local/jdk1.8.0_271
ENV JRE_HOME /usr/local/jdk1.8.0_271/jre
ENV PATH $JAVA_HOME/bin:$JRE_HOME/bin:$PATH

RUN mv /etc/localtime /etc/localtime.bak && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
#modify Character set
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US.UTF-8
ENV LC_ALL en_US.UTF-8