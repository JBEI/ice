cd lib
mvn install:install-file -Dfile=bytecode.jar -DgroupId=org.jbei.ice -DartifactId=bytecode -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=biojava-1.7-all.jar -DgroupId=org.jbei.ice -DartifactId=biojava-all -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=biojava.jar -DgroupId=org.jbei.ice -DartifactId=biojava -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=backport-util-concurrent.jar -DgroupId=org.jbei.ice -DartifactId=backport-util-concurrent -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=libSBOLj-0.7.0-SNAPSHOT.jar -DgroupId=org.jbei.ice -DartifactId=libSBOLj -Dversion=0.7.0 -Dpackaging=jar
