cd lib
mvn install:install-file -Dfile=biojava-1.7-all.jar -DgroupId=org.jbei.ice -DartifactId=biojava-all -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=biojava.jar -DgroupId=org.jbei.ice -DartifactId=biojava -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=flex-messaging-common.jar -DgroupId=org.jbei.ice -DartifactId=flex-messaging-common -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=flex-messaging-core.jar -DgroupId=org.jbei.ice -DartifactId=flex-messaging-core -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=flex-messaging-opt.jar -DgroupId=org.jbei.ice -DartifactId=flex-messaging-opt -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=flex-messaging-proxy.jar -DgroupId=org.jbei.ice -DartifactId=flex-messaging-proxy -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=flex-messaging-remoting.jar -DgroupId=org.jbei.ice -DartifactId=flex-messaging-remoting -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=libSBOLj-0.7.0-SNAPSHOT.jar -DgroupId=org.jbei.ice -DartifactId=libSBOLj -Dversion=0.7.0 -Dpackaging=jar
