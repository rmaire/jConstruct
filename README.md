# jConstruct
mvn package
mvn install:install-file -DgroupId=ch.uprisesoft -DartifactId=jconstruct -Dversion=0.1.2 -Dpackaging=jar -Dfile=target/jconstruct-0.1.2.jar

mvn deploy -Dserver.username=<user> -Dserver.password=<pw> -Dregistry=https://maven.pkg.github.com/rmaire -Dtoken=GH_TOKEN

