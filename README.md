##### 克隆整个项项目

git clone --recursive git@gitee.com:sxjhome/private-education.git

##### 将所有子模块都切到master分支

git submodule foreach git checkout master

##### GIT 添加子模块
git submodule add <repository> <path> 
git add .
git commit -m "add submodule"
git push

##### 项目启动
需要在开发工具中添加参数 program arguments     --spring.profiles.active=dev
然后直接运行 Application 中的 main 方法即可

##### 切换远程仓库地址
git remote set-url origin SRV_URL
cd tob-common
git remote set-url origin git@gitee.com:Sacher/thinker-common.git 
修改 .gitmodules 文件
[submodule "thinker-common"]
	path = thinker-common
	url = git@gitee.com:Sacher/thinker-common.git 
	
##### 依赖注入注解（更偏重使用 @Inject）

@Inject     这是jsr330规范的实现，
@Resource   是jsr250的实现，这是多年前的规范，
@Autowired  是spring的实现，如果不用spring一般用不上这个

##### 项目打包
````
参数： BRANCH 需要打包的GIT分支，默认值：master

build.sh $JAR_NAME $dockerN $BANCH $ENV
````
###### 注意：项目打包时 GIT 用户必须可以克隆 education-serve 和 thinker-common 两个项目的权限

##### Docker运行项目（HOST:PORT 是镜像仓库地址）
````
（可选）参数： JAR_URL  JAR的下载路径, 如果不传这个参数请确保最新JAR已经存放在本地挂载目录$HOST_VOLUME中  
      注意： $HOST_VOLUME目录最后的子目录名为 $JAR_NAME 
  更多可选参数请参考： thinker-common/spring-boot-dok/README.md

删除之前启动的服务
docker  rm -f $DOCKER_NAME

创建需要挂载的目录, 目录以 education-serve 结尾
mkdir -p /data/download/jars/${BRANCH}/${JAR_NAME}

docker run -d --restart always \
       --net=host  \
       -v /data/deploys/${jarN}:/mnt/${jarN} \
       --name=${dockerN} \
       -e JAR_NAME=${JAR_NAME} \
       -e JAR_ENV=${jar_env} \
       sacher/spring-boot:test
````

##### 服务检测地址
```
    /actuator/health
```
