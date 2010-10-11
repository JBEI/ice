Welcome to the JBEI Registry Software Version 2 (ICE). I hope you find
our tools useful.

CONTENTS:

1. Requirements
2. Setting up Maven
3. Build Instructions for Deployment
4. Setup
5. Contributing Code

===============
1. Requirements
===============

JBEIR dependns on external software. The only requirement to run JBEIR
is Postgres and a java based web server like Tomcat or Jetty. To make
significant modifications or customizations, we use the optional
development tools below.

1.1 Postgres
------------

JBEIR requires postgres to run. It is relatively easy to set up and
use.  There are plenty of walk throughs on the internet to guide you.
We recommend (and use) pgadmin software.

TODO: Write walkthrough here. 

1.2 Adobe Flex SDK (For Development only)
-------------------------------------

We include a compiled version of our Flex based application in our
sources.  However, if you would like to build your own, you need
Adobe's SDK. The SDK is open source. Sadly, the IDE plug-ins are not.

1.3 Maven (For Development only)
----------------------------

We use Maven to help manage our dependencies. 

1.4 Eclipse (For Development only)
-----------------------------

We use Eclipse as our IDE. For debugging, we use run-jetty-run plugin. 

===================
2. Setting up Maven
===================

2.1. Install maven2 (via apt-get or download).
2.2. JBEIR ICE's pom.xml has all the dependencies defined. However,
      there are some libraries that are not in any public maven repos,
      namely biojava and flex components. So these have to be
      installed to your local maven repo.

Run the following command in the lib/ directory of your sources (Section 3.1):

# cd lib

mvn install:install-file -Dfile=bytecode.jar -DgroupId=org.jbei.ice -DartifactId=bytecode -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=biojava-1.7-all.jar -DgroupId=org.jbei.ice -DartifactId=biojava-all -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=biojava.jar -DgroupId=org.jbei.ice -DartifactId=biojava -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=backport-util-concurrent.jar -DgroupId=org.jbei.ice -DartifactId=backport-util-concurrent -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=flex-messaging-common.jar -DgroupId=org.jbei.ice -DartifactId=flex-messaging-common -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=flex-messaging-core.jar -DgroupId=org.jbei.ice -DartifactId=flex-messaging-core -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=flex-messaging-opt.jar -DgroupId=org.jbei.ice -DartifactId=flex-messaging-opt -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=flex-messaging-proxy.jar -DgroupId=org.jbei.ice -DartifactId=flex-messaging-proxy -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=flex-messaging-remoting.jar -DgroupId=org.jbei.ice -DartifactId=flex-messaging-remoting -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=org.eclipse.mylyn.wikitext.core_1.4.0.I20100805-0500-e3x.jar -DgroupId=org.jbei.ice -DartifactId=org.eclipse.mylyn.wikitext.core -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=org.eclipse.mylyn.wikitext.mediawiki.core_1.4.0.I20100805-0500-e3x.jar -DgroupId=org.jbei.ice -DartifactId=org.eclipse.mylyn.wikitext.mediawiki.core -Dversion=SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=org.eclipse.mylyn.wikitext.confluence.core_1.4.0.I20100805-0500-e3x.jar -DgroupId=org.jbei.ice -DartifactId=org.eclipse.mylyn.wikitext.confluence.core -Dversion=SNAPSHOT -Dpackaging=jar

Now you should have all the necessary libraries in your local maven cache.

======================================
3. Building from Source for Deployment
======================================

3.1. If you would like to keep track of current development, building
     from sources is a must.
     Download sources from svn:
     $ svn checkout http://gd-ice.googlecode.com/svn/trunk/ice gd-ice-build

3.1.1 [optional: git] I prefer to use git-svn to talk to the svn repo, as git
      gives me greater flexibility in managing local branches.  With
      git, the commands are:

      $ git svn init http://gd-ice.googlecode.com/svn/trunk/ice gd-ice-build
      $ cd gd-ice-build
      $ git svn fetch
      $ git checkout -f
    
      [Note: I also recommend running this command a few times in
      different directories, because svn doesn't give identical
      results every time (Yes, svn is *unreliable*). Make sure the git
      checksome for the last version is the same between checkouts
      before continuing (by running git log)]
    
3.1.2 [git] Let's create a build branch that's separate from the svn.
      $ git checkout -b build

3.2. Now, edit the relavent config and settings files:
     src/main/java/hibernate.cfg.xml
     src/main/java/jbeir.properties
     src/main/resources/log4j.properties
     src/main/webapp/WEB-INF/web.xml
    
3.2.1 [git] Commit those changes to your local branch:
      $ git add -u
      $ git commit -m "build: productions settings"

3.3. Build!
     $ mvn package

3.4. Now you should have target/gd-ice-1.0-SNAPSHOT.war and
     target/gd-ice-1.0-SNAPSHOT. Deploy these to your webserver install.
   
3.5. Updating
3.6.1 If using svn, just svn update and hope / check that your local settings
      are not over written by new defaults or new options.
3.7.2 [git] If using git:

      $ git svn fetch
      $ git svn rebase

      Git will tell you if your settings don't apply. Fix those conflicts
      and git add -u, git rebase --continue.

      For best results, instead of adding all the changes to a single git
      branch commit, commit each file change into individual commits, so
      they can be individually tracked.
  
====================
4. Setup
====================
When the site is installed and deployed, log in using the default 
Administrator account. It is 'Administrator' with the password 
'Administrator'.
    
====================
5. Contributing Code
====================

We will gladly accept patches to our code. Just send us an email
and/or a patch file.

We ask that you conform to our formatting guidelines. Simply set up
your Eclipse to use the included "Registry Team Formatter.xml", and
auto format your file.
