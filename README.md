# Project Base

주의!!
첫 컴파일시 web 디렉토리 이동 후 web library 다운로드를 위해, 'yarn' 명령 수행.


AetherIT 기본 프로젝트 템플릿

프로젝트 적용시 수정해야 할 내용

* Dockerfile : 
    1. 경로를 나타내는 '/project/base'
* pom.xml :
    1. aritifactid 의 'project.base'
    2. plugin 의 '/project/base' 경로
* config/logback-spring.xml
    1. file appender 의 파일 이름 'base'
    2. logger 이름의 io.aetherit.project.base
* doc/sql/CreateTables.sql and DropTables.sql
    1. USE의 데이타베이스 이름
    2. base_users 테이블 명
* application.properties
    1. tomcat, mybatis, application 설정의 'project.base'
* ApplicationProperties
    1. @ConfigurationProperties prefix 의 'project.base.application'
* DatabaseProperties
    1. @ConfigurationProperties prefix 의 'project.base.mybatis'
* TomcatProperties
    1. @ConfigurationProperties prefix 의 'project.base.tomcat'
* Application 클래스
    1. scanPackage 명의 project.base
* DatabaseConfiguration 클래스
    1. scanmapper 의 패키지 명
* package 이름 변경 (IDE의 refactor 기능 이용)
    1. 'project.base'
* Class 이름 변경 (IDE의 refactor 기능 이용)
    1. model/BaseUser
    2. model/BaseUserToken
    3. model/BaseSimpleUser
    4. model/support/BaseUserType
    5. exception/BaseException
* mybatis-config.xml
    1. typeAlias 의 BaseUser
    2. typeAlias 의 BaseUserType
* UserMapper
    1. resultmap id 및 type
    2. sql문의 테이블 명 base_users
    3. sql문의 resultmap id 및 parameter type

Usage:
`docker run -d --name project.base
  -p 8000:8000
  -p 8080:8080
  -e TZ=Asia/Seoul
  -e DB_URL=jdbc:mariadb://localhost:3306/base
  -e DB_USERNAME=aetherit
  -e DB_PASSWORD=dnflRlfl
  aetherit/project.base:development`
  
DB Usage:
`docker run -d --name base.db
 -p 3306:3306
 -e TZ=Asia/Seoul
 -e MYSQL_ROOT_PASSWORD=dnflRlfl1535
 -e MYSQL_DATABASE=base
 -e MYSQL_USER=aetherit
 -e MYSQL_PASSWORD=dnflRlfl
 mariadb:10.4.12
 --character-set-server=utf8mb4
 --collation-server=utf8mb4_unicode_ci`
 