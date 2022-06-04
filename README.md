#### 자바 설치

`sudo yum install java-11-amazon-corretto.x86_64`

#### 아마존 ec2 무료 티어 메모리 부족 현상 해결하기

1. 스왑 파일 생성
   `sudo dd if=/dev/zero of=/swapfile bs=128M count=16`

2. 스왑 파일에 대한 읽기 및 쓰기 권한을 업데이트
   `sudo chmod 600 /swapfile`

3. Linux 스왑 영역을 설정
   `sudo mkswap /swapfile`

4. 스왑 공간에 스왑 파일을 추가하여 스왑 파일을 즉시 사용할 수 있도록 만든다
   `sudo swapon -s`

5. 절차가 성공했는지 확인
   `sudo vi /etc/fstab`

6. /etc/fstab 파일을 편집하여 부팅 시 스왑 파일을 활성화
   `/swapfile swap swap defaults 0 0`
   편집기에서 파일을 연 후 파일 끝에 다음 줄을 새로 추가하고 파일을 저장한 다음 종료한다.

7. free를 다시 입력하여 메모리를 확인해본다.
   `free`

#### Jenkins 설치

wget으로 젠킨스를 yum 저장소에 다운

`sudo wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo`

젠킨스 저장소 키를 등록

`sudo rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io.key`

등록이 끝났으니 젠킨스를 설치하시면 됩니다.

`sudo yum install jenkins -y`

설치가 끝났으면 젠킨스를 실행해주시고

`sudo systemctl start jenkins`

젠킨스가 정상적으로 실행되었는지 아래 명령어로 확인해주세요

`sudo systemctl status jenkins`

Active: active (running) 이면 정상적으로 실행된 겁니다.

#### 젠킨스 프록시 설정

젠킨스 설치가 끝났으면

nginx를 통해 프록시까지 등록해보겠습니다.

우선 아래 명령어를 통해 amazon-linux-extras에서 nginx를 지원하는지 확인해주세요

`amazon-linux-extras list | grep nginx`

```
$ amazon-linux-extras list | grep nginx
 38  nginx1                   available    [ =stable ]
```

nginx가 아닌 nginx1이므로 nginx1로 설치합니다.

`sudo yum clean metadata && sudo amazon-linux-extras install nginx1`

설치가 다되면 nginx.conf 파일에서 프록시를 설정합니다.

`sudo vim /etc/nginx/nginx.conf`

```
server {
	listen       80;
	listen       [::]:80;
	server_name  _;
	root         /usr/share/nginx/html;

	# Load configuration files for the default server block.
	include /etc/nginx/default.d/*.conf;
	
    /////
    이곳
    /////
    
	error_page 404 /404.html;
	location = /40x.html {
	}

	error_page 500 502 503 504 /50x.html;
	location = /50x.html {
	}
}
```

위에 이곳이라고 쓰여있는 부분에 아래 글을 붙여 넣어 주세요

```
location / {
		proxy_pass http://localhost:8080;
		proxy_set_header X-Real-IP $remote_addr;
		proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
		proxy_set_header Host $http_host;
}
```

다 되셨으면 nginx를 실행하면 됩니다.

`sudo systemctl start nginx`

실행이 제대로 됐는지는 아래 명령어로 확인합니다.

`sudo systemctl status nginx`

두 번째 줄에 active (running)이 뜨면 정상적으로 작동된 겁니다.

#### 젠킨스 설정

ec2서버로 접근해보면 password를 입력하라는 화면이 나옵니다.

![](https://velog.velcdn.com/images/gkwlsdn95/post/0c8d9626-e26e-4588-8301-b0426acf37b6/image.png)

cat으로 비밀번호를 확인합니다.

`sudo cat /var/lib/jenkins/secrets/initialAdminPassword`

```
$ sudo cat /var/lib/jenkins/secrets/initialAdminPassword
dbba7159c12f4419a540a4eaa123edfc
```

비밀번호를 복사 붙여넣기 하고 다음으로 넘어가면

아래 화면이 나오는데 여기서 Install suggested plugins를 선택해주세요

플러그인 설치가 끝나는걸 기다리시면 됩니다.
설치가 완료되면 젠킨스를 접속할 때 사용할 계정을 생성해주세요

#### 젠킨스와  Github ssh 연동

현재 사용자를 젠킨스로 전환합니다.
`sudo -u jenkins /bin/bash`
```
$ sudo -u jenkins /bin/bash
bash-4.2$
```

```
$ mkdir /var/lib/jenkins/.ssh
$ cd /var/lib/jenkins/.ssh
```
디렉토리를 생성합니다.
이동하고 나서 ssh 키를 생성하면 됩니다.
```
ssh-keygen -t rsa -f /var/lib/jenkins/.ssh/{프로젝트명}
ex) ssh-keygen -t rsa -f /var/lib/jenkins/.ssh/spring-project
```
비밀번호는 입력하지 않고 Enter로 넘기시면 됩니다.
![](https://velog.velcdn.com/images/gkwlsdn95/post/ab3fd12f-fc56-4d32-88a1-dbd9fd501405/image.png)
깃허브에 접속하고 나서

프로젝트 레포지토리 -> Settings -> Deploy keys -> Add deploy key 순으로 클릭
생성한 공개키 코드를 복사하기 위해 cat을 사용합니다.
`cat /var/lib/jenkins/.ssh/backProject.pub`
![](https://velog.velcdn.com/images/gkwlsdn95/post/ccb6284a-6141-4c67-bbe9-b8e26c1fd33d/image.png)
![](https://velog.velcdn.com/images/gkwlsdn95/post/77631e15-a319-40c2-ae38-e43574a74120/image.png)
생성된 공개키를 복사하여 Key부분에 붙여넣고 title까지 입력 후 Add Key를 누릅니다.
![](https://velog.velcdn.com/images/gkwlsdn95/post/911c2b50-4421-466f-ac79-80bbcce26ab9/image.png)
공개키 등록이 되었으면 젠킨스로 이동하여 비밀키를 등록해야 합니다.
![](https://velog.velcdn.com/images/gkwlsdn95/post/adf1d306-15fd-4190-9f61-a740cf4699b7/image.png)
비밀키는 아까 생성한 키 파일에서 복사합니다.
`cat /var/lib/jenkins/.ssh/backProject`
```
-----BEGIN RSA PRIVATE KEY-----
MIIEpAIBAAKCAQEAuROTaeQ6EV5EqTH1cOHR+evHEY2I5FmP+bfZKKrv/fctGv2U
2l0du07hvkKQAk6FQXjY5axi9t9YEk0Zz8b0Jl8NIhSRUuugU4XgdbW2HMpa21wv
kt4H4q+U+XFyT4QzcEwi/SJUw87SQmLs84elo89lY3NvkAS//UXYz28TSRAQWv42
l4oIZV8U5yFKP0GZ40xHtXCFV4SWuM+m2deZJTteAHK71kw5Z8tCLL2sJAXTu/ui
R880K4JWJ6tM+Hngf1xpPYIokifirnkaP4p0J0GTSOUVl17+4FUegsa2zGje2MxM
xqlYiNOGbziebouDcOIYE1YIlhkbJXlaDNVzewIDAQABAoIBAQCh6Is8xuk5yoM8
lTUIBLYB8o8bPvtz8RowNvLmTexX/AXcp9yxz2Y0N8TrAiYjrneLGGJ/QnjtSeQt
Vn/vMjji3KKLnvynUvTWPLdpoobn1ur3HkWMMWsql7o3IlUDVT7+zcyKMJ+BEQ0G
AK8UOSXiEYy8HP+LOWyP8KvEhIKVlde3c4ojr2ZZ5ws2SHc2FErJzrU+GS7IS4Jw
```
우선 깃이 설치되어있지 않다면 깃을 설치해주세요
`sudo yum install git`
설치가 되었으면 젠킨스로가서 새로운 Item을 생성합니다.
![](https://velog.velcdn.com/images/gkwlsdn95/post/ae1a80df-0624-4773-ba35-7a027fee0ad2/image.png)
![](https://velog.velcdn.com/images/gkwlsdn95/post/06cdb70f-491f-4f2a-9626-5cfa50d31e39/image.png)
![](https://velog.velcdn.com/images/gkwlsdn95/post/dc756d09-ec63-4cbd-9fc4-855e8867b831/image.png)
소스코드 관리에서 연동할 프로젝트의 깃 레파지토리를 작성하고 Crredentials에는 금방 만든 Username을 선택

![](https://velog.velcdn.com/images/gkwlsdn95/post/1f3f3b6a-1f99-40bb-afc7-f6e34ef214c3/image.png)
main 또는 master 입력

저장하시고 대시보드에 가보시면 프로젝트가 생성되어있는데

프로젝트명 우측 화살표 클릭하고 build now를 클릭합니다.
![](https://velog.velcdn.com/images/gkwlsdn95/post/bda55ed3-4776-4493-a688-2d666708d2bd/image.png)
SUCCESS가 뜨면 성공입니다.

#### Webhook
프로젝트 레파지토리에 푸시를 하면 Github Webhook을 이용해

젠킨스에 polling 하여 빌드를 자동으로 해주는 환경을 구축해야 합니다.
![](https://velog.velcdn.com/images/gkwlsdn95/post/5eec1e77-de0c-4a7a-b425-be918955bc26/image.png)
우선 두 가지 플러그인을 젠킨스에 설치해야 합니다.

1. GitHub Integration

2. Publish Over SSH

플러그인 관리 -> 설치 가능에서 두가지 모두 설치하시면 됩니다.

설치가 완료되시면 젠킨스 프로젝트를 클릭하시고
구성 -> 빌드 유발 탭

GitHub hook trigger for GITScm polling 항목을 체크해주세요
![](https://velog.velcdn.com/images/gkwlsdn95/post/8048609d-9b77-462b-b6dd-f697e130cef3/image.png)

깃헙 프로젝트 레파지토리 -> settings에서 Webhook을 추가해야 합니다.
![](https://velog.velcdn.com/images/gkwlsdn95/post/b7d58d63-654b-4610-b163-1c6adc9c9819/image.png)
Payload URL : http://{aws public ip:포트}/github-webhook/

Content type : application/json
![](https://velog.velcdn.com/images/gkwlsdn95/post/a378268e-22a2-49be-9474-8a9a9946179c/image.png)

![](https://velog.velcdn.com/images/gkwlsdn95/post/a72cdeda-9d73-4710-a89f-3f2b172e228d/image.png)
초록색 체크표시가 떠야 정상적으로 웹훅이 등록이 된 겁니다.
위에 설정이 모두 끝나셨으면 테스트용 푸시를 해주세요
![](https://velog.velcdn.com/images/gkwlsdn95/post/ca3c0589-1977-4f4b-a73f-5dea57b7b184/image.png)
웹훅도 빌드도 성공하면 끝입니다!

#### Build

![](https://velog.velcdn.com/images/gkwlsdn95/post/19259c26-45c9-4118-8688-b1ada9525e7d/image.png)

#### 빌드 후 조치
먼저 설정하기 전에 배포용 쉘을 작성하겠습니다.
배포할 서버의 ec2에
webapps 디렉토리를 만들고 그 안에 쉘을 만들고 내용을 작성하겠습니다.
```
sudo mkdir /home/ec2-user/webapps
sudo vi /home/ec2-user/webapps/start.sh
```
```
#!/bin/bash

REPOSITORY={jar파일이 생성된 경로}
#ex) REPOSITORY=/home/ec2-user/jenkins-home
echo "REPOSITORY = $REPOSITORY"
cd $REPOSITORY

PROJECT_NAME={프로젝트 명}
#ex) PROJECT_NAME=react-back
echo "PROJECT_NAME = $PROJECT_NAME"

PROJECT_PID=$(pgrep -f $PROJECT_NAME)
echo "PROJECT_PID = $PROJECT_PID"

if [ -z $PROJECT_PID ]; then
    echo "no running project"
else
    kill -9 $PROJECT_PID
    sleep 3
fi

JAR_NAME=$(ls $REPOSITORY/ | grep $PROJECT_NAME | tail -n 1)
echo "JAR_NAME = $JAR_NAME"

java -jar $REPOSITORY/$JAR_NAME &
```
![](https://velog.velcdn.com/images/gkwlsdn95/post/b398c383-4a77-40d9-89e3-f7e01ab7f09a/image.png)
고급 탭에 들어간 후
![](https://velog.velcdn.com/images/gkwlsdn95/post/11f3bd64-24ab-4d9c-8a99-4cc112e6e420/image.png)

다음으로 Publish over SSH도 설정을 해야 합니다.

Jenkins관리 -> 시스템 구성 -> Publish over SSH
![](https://velog.velcdn.com/images/gkwlsdn95/post/2e6003ca-e984-4389-9f11-34255510e0e0/image.png)
```
key : aws 인스턴스 접속할 때 쓰던 pem키의 내용을 적으시면 됩니다.
hostname : aws인스턴스 공인 아이피
username : aws 유저 네임
Remote Directory : 생성한 배포파일이 업로드 될 경로
```

build now를 누르고 성공 확인!

![](https://velog.velcdn.com/images/gkwlsdn95/post/da56e21f-3a11-407d-bead-a00ae1fc9d65/image.png)

### 무중단 배포 환경

서버 디렉토리 구조
![](https://velog.velcdn.com/images/gkwlsdn95/post/f4026c54-8503-4082-a9ed-3b0d9b222746/image.png)

#### deploy.sh
```
SCRIPT_DIR=/home/ec2-user/scripts
PROJECT_NAME=demo

$SCRIPT_DIR/stop.sh | sudo tee -a /home/ec2-user/deploy.out
$SCRIPT_DIR/start.sh | sudo tee -a /home/ec2-user/deploy.out
$SCRIPT_DIR/health.sh | sudo tee -a /home/ec2-user/deploy.out
```

#### deploy.out
![](https://velog.velcdn.com/images/gkwlsdn95/post/ce7b7efc-80b0-46e9-8172-27e781594f58/image.png)


#### nohup.out
![](https://velog.velcdn.com/images/gkwlsdn95/post/c56960f3-a272-4863-a255-b0e77df17a4a/image.png)

#### health.sh
```
#!/usr/bin/env bash
ABSPATH=$(readlink -f $0)
ABSDIR=$(dirname $ABSPATH)
source ${ABSDIR}/profile.sh
source ${ABSDIR}/switch.sh

IDLE_PORT=$(find_idle_port)

echo "> Health Check Start!"
echo "> IDLE PORT: $IDLE_PORT"
echo "> curl -s http://localhost:$IDLE_PORT/profile"

REPOSITORY=/home/ec2-user
MV_JAR_NAME_1=$(ls -tr $REPOSITORY/*.jar | tail -n 2 | head -n 1)

echo "> Previous Version"
echo "  -> $MV_JAR_NAME_1"

for RETRY_COUNT in {1..15}
do
    RESPONSE=$(curl -s http://localhost:${IDLE_PORT}/profile)
    UP_COUNT=$(echo ${RESPONSE} | grep 'real' | wc -l)
    echo "${UP_COUNT}"
    if [ ${UP_COUNT} -ge 1 ]
    then
        echo "> Health Check 성공"
        rm -rf $REPOSITORY/pre_version/*
        mv $MV_JAR_NAME_1 $REPOSITORY/pre_version/
        switch_proxy
        break
    else
        echo "> Health check의 응답을 알 수 없거나 혹은 실행 상태가 아닙니다."
        echo "> Health Check: ${RESPONSE}"
    fi

    if [ ${RETRY_COUNT} -eq 15 ]
    then
        echo "> Health check 실패."
        echo "> 엔진엑스에 연결하지 않고 배포를 종료합니다."
        exit 1
    fi

    echo "> Health check 연결 실패. 재시도..."
    sleep 10
done

```

#### profile.sh
```
#!/usr/bin/env bash
# 쉬고 있는 profile 찾기
function find_idle_profile()
{
    RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/profile)
    if [ ${RESPONSE_CODE} -ge 400 ] # 400 보다 크면(40x, 50x)에러
    then
        CURRENT_PROFILE=real2
    else
        CURRENT_PROFILE=$(curl -s http://localhost/profile)
    fi

    if [ ${CURRENT_PROFILE} == real1 ]
    then
        IDLE_PROFILE=real2
    else
        IDLE_PROFILE=real1
    fi
    echo "${IDLE_PROFILE}"
}

# 쉬고 있는 profile의 port찾기
function find_idle_port()
{
    IDLE_PROFILE=$(find_idle_profile)
    if [ ${IDLE_PROFILE} == real1 ]
    then
        echo "8081"
    else
        echo "8082"
    fi
}
```

#### start.sh
```
#!/usr/bin/env bash
ABSPATH=$(readlink -f $0)
ABSDIR=$(dirname $ABSPATH)
source ${ABSDIR}/profile.sh

REPOSITORY=/home/ec2-user

JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)

echo "> JAR NAME: $JAR_NAME"
echo "> $JAR_NAME 에 실행 권한 추가"
chmod +x $JAR_NAME

echo "> $JAR_NAME 실행"

IDLE_PROFILE=$(find_idle_profile)
echo "> $JAR_NAME을 $IDLE_PROFILE로 실행 합니다."

cp $REPOSITORY/nohup.out $REPOSITORY/pre_log/prelog.out
rm -f $REPOSITORY/nohup.out

nohup java -jar \
-Dspring.config.location=classpath:/application.properties,\
classpath:/application-$IDLE_PROFILE.properties \
-Dspring.profiles.active=$IDLE_PROFILE \
$JAR_NAME>$REPOSITORY/nohup.out 2>&1 &

echo "nohup java -jar \
-Dspring.config.location=classpath:/application.properties,\
classpath:/application-$IDLE_PROFILE.properties \
-Dspring.profiles.active=$IDLE_PROFILE \
$JAR_NAME>$REPOSITORY/nohup.out 2>&1 &"
```

#### stop.sh
```
#!/usr/bin/env bash
ABSPATH=$(readlink -f $0)
ABSDIR=$(dirname $ABSPATH)
source ${ABSDIR}/profile.sh

IDLE_PORT=$(find_idle_port)

echo "> ${IDLE_PORT} 에서 구동중인 어플리케이션 pid 확인"
IDLE_PID=$(sudo lsof -ti tcp:${IDLE_PORT})
if [ -z ${IDLE_PID} ]
then
    echo "> 현재 구동 중인 어플리케이션이 없으므로 종료하지 않습니다."
else
    echo "> kill -15 $IDLE_PID"
    kill -15 ${IDLE_PID}
    sleep 10
fi
```

#### switch.sh
```
#!/usr/bin/env bash
ABSPATH=$(readlink -f $0)
ABSDIR=$(dirname $ABSPATH)
source ${ABSDIR}/profile.sh

function switch_proxy(){
    IDLE_PORT=$(find_idle_port)

    echo "> 전환할 port: $IDLE_PORT"
    echo "> Port 전환"
    echo "set \$service_url http://127.0.0.1:${IDLE_PORT};" | sudo tee /etc/nginx/conf.d/service-url.inc

    echo "> 엔진엑스 Reload"
    sudo service nginx reload
    echo "> Realod 완료"
}
```

#### test.sh

```
#!/usr/bin/env bash
ABSPATH=$(readlink -f $0)
ABSDIR=$(dirname $ABSPATH)
source ${ABSDIR}/profile.sh

REPOSITORY=/home/ec2-user
echo "> curl -s http://localhost:$IDLE_PORT/profile"
MV_JAR_NAME_1=$(ls -tr $REPOSITORY/*.jar | tail -n 2 | head -n 1)
echo "$MV_JAR_NAME_1"

JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)

echo "> JAR NAME: $JAR_NAME"
echo "> $JAR_NAME 에 실행 권한 추가"

# nohup java -jar \
# -Dspring.config.location=classpath:/application.properties,\
# classpath:/application-$IDLE_PROFILE.properties \ #, 추가하고 공백제거해야함
# -Dspring.profiles.active=$IDLE_PROFILE \
# $JAR_NAME>$REPOSITORY/nohup.out 2>&1 &
```

참고

1. [[AWS] EC2(Amazon Linux) JAVA 11 설치하기 / ec2 jdk11 설치](https://cloud-oky.tistory.com/3286)

2. [아마존 ec2 무료 티어 메모리 부족 현상](https://okky.kr/article/884329)
3. [jenkins 설치하기](https://choiiii-dev.tistory.com/13?category=1022112)