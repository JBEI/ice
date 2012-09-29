cd lib
mvn install:install-file -Dfile=bytecode.jar -DgroupId=org.jbei.ice -DartifactId=bytecode -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=biojava-1.7-all.jar -DgroupId=org.jbei.ice -DartifactId=biojava-all -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=biojava.jar -DgroupId=org.jbei.ice -DartifactId=biojava -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=backport-util-concurrent.jar -DgroupId=org.jbei.ice -DartifactId=backport-util-concurrent -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=flex-messaging-common.jar -DgroupId=org.jbei.ice -DartifactId=flex-messaging-common -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=libSBOLj.jar -DgroupId=org.jbei.ice -DartifactId=libSBOLj -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=flex-messaging-core.jar -DgroupId=org.jbei.ice -DartifactId=flex-messaging-core -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=flex-messaging-opt.jar -DgroupId=org.jbei.ice -DartifactId=flex-messaging-opt -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=flex-messaging-proxy.jar -DgroupId=org.jbei.ice -DartifactId=flex-messaging-proxy -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=flex-messaging-remoting.jar -DgroupId=org.jbei.ice -DartifactId=flex-messaging-remoting -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=org.eclipse.mylyn.wikitext.core_1.8.0.I20120924-2338.jar -DgroupId=org.jbei.ice -DartifactId=org.eclipse.mylyn.wikitext.core -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=org.eclipse.mylyn.wikitext.mediawiki.core_1.8.0.I20120924-2338.jar -DgroupId=org.jbei.ice -DartifactId=org.eclipse.mylyn.wikitext.mediawiki.core -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=org.eclipse.mylyn.wikitext.confluence.core_1.8.0.I20120924-2338.jar -DgroupId=org.jbei.ice -DartifactId=org.eclipse.mylyn.wikitext.confluence.core -Dversion=SNAPSHOT -Dpackaging=jar
