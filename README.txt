Welcome to the JBEI Registry Software Version 2 (ICE). I hope you find
our tools useful. More information, feature requests, bug reports, as
well as discussions are available from our google project page:
http://code.google.com/p/gd-ice/

CONTENTS:

1. Requirements
2. Setting up Maven
3. Build Instructions for Deployment
4. Setup
5. Contributing Code

===============
1. Requirements
===============

JBEIR dependns on external software. The minimum requirement to run JBEIR
is Postgres and a java based web server like Tomcat or Jetty. To make
significant modifications or customizations, we use the optional
development tools below.

To use BLAST to search sequences, it must be installed on your system.
Then update the jbeir.properties file to tell jbeir where it is.

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
Adobe's SDK. The SDK is mostly open source and freely available, which
means you can compile our Flex tools without buying anything.  The IDE
is sadly not free.

1.3 Maven (For Development only)
----------------------------

We use Maven to help manage our dependencies. You can run a functional
server from the sources by running "mvn jetty:run" from the command line.
This requires that a .keystore file is configured. 

1.4 Eclipse (For Development only)
-----------------------------

We use Eclipse as our IDE. There are different ways to debug jbeir
in Eclipse, which are changing constantly. The most straight forward
but somewhat cumbersome way is to use the maven jetty plugin directly:

1. Adjust jetty-debug.xml for your keystore file and password.

2. In Eclipse, go to Run-> External Tools->External Tools Configuration
   and create a new setup. Enter your mvn location (/usr/bin/mvn for linux)
   and the arguments jetty:run
2.1 Select the Environment tab and add a new variable.
    MAVEN_OPTS: -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=y
3. In Eclipse, go to Run->Debug Configurations and create a new debug
   configuration of the type Remote Java Application. Use the standard
   connection and port 4000 (as specified in 3.1 above). Check Allow
   termination of remote VM. In the Source tab, add the jbeir src directory.
4. This will create what looks like another workspace. Click its properties,
   Java Build Path, select the Projects tab, and add the gd-ice project. This
   will link the sources.
5. Select Run->External Tools and run jetty
6. Select Run->Debug Configurations and select your debug configuration.

If run once, they can be launched by pushing the Run External and Debug
buttons.
   
===================
2. Setting up Maven
===================

2.1. Install maven2 (via apt-get or download).
2.2. JBEIR ICE's pom.xml has all the dependencies defined. However, there
     are some libraries that are not in any public maven repositories,
     namely biojava and flex components. We have provided these in the lib/
     directory so they can be installed to your local maven repository.

Run the following commands in the lib/ directory of your sources (Section 3.1):

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
    
      Let's create a build branch that's separate from the svn.
      $ git checkout -b build

3.2. Now, edit the relavent config and settings files:
3.2.1 src/main/java/hibernate.cfg.xml
3.2.1.1 Change this line:
        <property name="hibernate.connection.url">jdbc:postgresql://localhost/test_registry</property>
        to point to your database by replacing "test_registry" with the name
        of your database.
3.2.1.2 Change this line:
        <property name="hibernate.connection.username">test_user</property>
        Replace "test_user" with the name of the database user.
3.2.1.3 Change this line:
        <property name="hibernate.connection.password">t@stuz@r</property>
        Replace the string with your database password for your database
        user.
3.2.2 src/main/java/jbeir.properties
      At minimum, change SMTP_HOST to use your mail host, and change
      ADMIN_EMAIL and MODERATOR_EMAIL to your address. Please change
      SITE_SECRET and SECRET_KEY. These strings are used as cryptographic
      salts in various places.
      Any *_DIRECTORY setting with /tmp/ should be changed if you want to
      use these features. Hopefully, other settings are self explanatory.
3.2.3 src/main/resources/log4j.properties
      Change the file location if you want to keep log files between
      server reboots.
3.2.4 src/main/webapp/WEB-INF/web.xml
      This setting can be left alone. If you must run your site on
      http instead of https (we do not recommend this), comment out
      the <security-constraint> section.
3.2.5 [git] Commit those changes to your local branch:
      $ git add -u
      $ git commit -m "build: productions settings"

3.3. Build!
     $ mvn package

3.4. Now you should have target/gd-ice-1.0-SNAPSHOT.war and
     target/gd-ice-1.0-SNAPSHOT. Deploy these to your webserver install.
   
3.5. Updating
3.5.1 If using svn, just svn update and hope / check that your local settings
      are not over written by new defaults or new options.
3.5.2 [git] If using git:

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
'Administrator'. Please change the default password.

====================
5. Contributing Code
====================

We will gladly accept patches to our code. Just send us an email
and/or a patch file.

We ask that you conform to our formatting guidelines. Simply set up
your Eclipse to use the included "Registry Team Formatter.xml", and
auto format your file.
