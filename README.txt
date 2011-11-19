Welcome to the JBEI Registry Software Version 2 (ICE). We hope you
find our tools useful. More information, feature requests, bug
reports, as well as discussions are available from our Google Project
page: http://code.google.com/p/gd-ice/

CONTENTS:

1. Requirements
2. Getting the Source Code
3. Using Maven
4. Website Setup
5. Configuring for Development
6. Contributing Code
7. Credits

===============
1. Requirements
===============

ICE depends on external software. The minimum requirement to run ICE
is Postgres and a Java based web server like Tomcat or Jetty. To use
BLAST to search sequences, it must be installed on your system. Then,
update the jbeir.properties file to tell ICE where it is.

To make significant modifications or customizations, we use the
optional development tools mentioned below.

1.1 Java

ICE requires Java 1.6 SE (also known as Java 6). Please download the
Oracle SDK version (or OpenJDK), instead of the GJC version. Most
of our testing is done against the Oracle version.  

1.1.1 Debian/Ubuntu intall instructions
The package name is "sun-java6-jdk". You may have to add external software
repositories to be able to install it. To use sun java as default, you
may have to run as root (type 'sudo su' or just 'su' depending on system)
    # update-alternatives --list java
to see the available java on your system. To change it, run
    # update-alternatives --config java
and choose "java-6-sun".

1.1.2 Windows
Download from 
http://www.oracle.com/technetwork/java/javase/downloads/index.html

1.2 Postgres

ICE requires postgres to run. It is relatively easy to set up and use.
There are plenty of walk throughs on the internet to guide you.  We
recommend (and use) the pgadmin3 software. Please be ware: If different
versions of postgres is installed on your machine, they may be listening
on different ports (5432 and/or 5433).

1.2.1 Debian/Ubuntu install instructions

1.2.1.1 Install the postgresql package:
        # apt-get install postgresql
1.2.1.2 Configure root password for the root database user (called postgres)
        # su postgres
        $ psql postgres
        postgres=# 
        Type in '\password postgres' without the quotes to change the
        password. Follow the prompts. Type '\q' to quit.

	It is possible the database is named template1 instead of postgres.
	In this case, type psql template1
1.2.1.3 Create a new database user
        $ createuser --pwprompt test_user
        [ type in the password. Select no to all options ]
1.2.1.4 Create a new database and assign it to the user
        $ createdb --owner test_user test_registry
1.2.1.4 PgadminIII (Optional)
        # apt-get install pgadmin3
        Make sure to configure the correct port (see above).

1.2.2 Windows instructions
Postgresql has a windows download link with pgAdmin3 built in. See 
http://www.postgresql.org/download/windows
Choose the latest 8.x series (currently 8.4)

Follow the prompts and set up the administrator password, and use the default
port (5432). 

After the installation, launch pgAdmin III program, and connect to the 
database using the administrator password. Right click on Login Roles,
select New Login Role, and enter test_user as the Role name and enter a
password. 

To create a new database, right click on Databases, select New Database.
Name it test_registry, and change the owner to test_user, then click Ok.

1.3 BLAST
    ICE needs to have ncbi-blast installed to use the nice BLAST features.
    On Ubuntu/Debian systems run
    # apt-get install blast2

    TODO: Blast on Windows?

1.2 Adobe Flex SDK (For Development only)

We include compiled versions of our Flex applications in our sources.
However, if you would like to build your own, you need Adobe's
SDK. The SDK is mostly open source and freely available, which means
you can compile our Flex tools without buying anything.  The IDE is
sadly not free.

1.3 Maven (For Building and Development)

We use Maven to help manage our dependencies.

1.4 Eclipse (For Development only)

We use Eclipse as our IDE. See Section 5 for setup instructions.


==========================
2. Getting the Source Code
==========================

2.1. If you would like to keep track of current development, building
     from sources is a must.
     Download the sources from the git repository:
     $ git clone https://code.google.com/p/gd-ice gd-ice-build

     From now on, the gd-ice-build directory will be referred to as
     the Root ICE directory.
     
     Under Windows, TortoiseGit is a nice client.

2.1.1 
      Let's create a build branch that's separate from the main.
      $ git checkout -b build

2.2. Now, edit the relavent config and settings files:
2.2.1 src/main/java/hibernate.cfg.xml
2.2.1.1 Change this line:
        <property name="hibernate.connection.url">jdbc:postgresql://localhost/test_registry</property>
        to point to your database by replacing "test_registry" with
        the name of your database.
        
2.2.1.2 Change this line:
        <property name="hibernate.connection.username">test_user</property>
        Replace "test_user" with the name of the database user.
2.2.1.3 Change this line:
        <property name="hibernate.connection.password">t@stuz@r</property>
        Replace the string with your database password for your database
        user.
2.2.2 src/main/java/jbeir.properties
      At minimum, change SMTP_HOST to use your mail host, and change
      ADMIN_EMAIL and MODERATOR_EMAIL to your address. Please change
      SITE_SECRET and SECRET_KEY. These strings are used as
      cryptographic salts in various places.
      Any *_DIRECTORY setting with /tmp/ should be changed if you want
      to use these features. Double check the location of blast programs.
      Hopefully, other settings are self explanatory.
      For Windows, replace the directory names with Windows style folder
      names. For example, /tmp/attachments becomes c:\tmp\attachments
      Make sure c:\tmp exists.
2.2.3 src/main/resources/log4j.properties
      Change the file location if you want to keep log files between
      server reboots. The FILE directive should be in unix format, even
      for Windows. 
2.2.4 src/main/webapp/WEB-INF/web.xml
      This file can be left alone. If you must run your site on
      http instead of https (we do not recommend this), comment out
      the <security-constraint> section. By default, all the flex
      components are built with the assumption of using https. If
      you are using http only, they must be rebuilt from sources.
2.2.5 Commit those changes to your local branch:
      $ git add -u
      $ git commit -m "build: productions settings"
   
2.3. Updating
2.3.1 
      $ git fetch origin
      $ git rebase origin

      Git will tell you if your settings don't apply. Fix those
      conflicts and git add -u, git rebase --continue.

      For best results, instead of adding all the changes to a single
      git branch commit, commit each file change into individual
      commits, so they can be individually tracked.

==============
3. Using Maven
==============

Maven provides a convenient way to download dependencies and build
packages.

3.1. Install maven2 (via 'apt-get install maven2' or download).
3.1.1 For Windows, follow the installation instructions on the maven
      website. Add the appropriate environment variables. 

3.2. ICE's pom.xml has all the dependencies defined. However, there
     are some libraries that are not in any public maven repositories,
     namely biojava and flex components. We have provided these in the
     lib/ directory so they can be installed to your local maven
     repository.

Run the following commands in the lib/ directory of your sources. Make 
sure the long commands are not broken up into multiple lines. (both linux
and Windows)

$ cd lib

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

Now you should have all the necessary libraries in your local maven
cache.

3.3 You can run a functional server from the sources.

    ICE uses https by default, so a signed certificate is required.
    To generate a *temporary* self-signed certificate, run

    For Windows:
    %JAVA_HOME%\bin\keytool.exe -genkey -alias tomcat -keyalg RSA -keystore .keystore
    For Unix:
    keytool -genkey -alias tomcat -keyalg RSA -keystore .keystore 

    By default, the path to the .keystore file is where this
    README.txt is, and the default password is "changeit", as can be
    seen in jetty-debug.xml. Update the file to reflect the actual
    location of the file and the password.

    Now try to run the ICE server:
    $ mvn jetty:run -Dmaven.test.skip=true

    If everything is set up properly, this command will populate your
    database with the appropriate tables, and run a server on port
    8443 (https). Point your browser to https://localhost:8443. Don't
    forget the https bit.

    The first time maven is run, it will download all the dependent
    packages, which can take a long time.

    To stop the server, press Ctrl-C

    Hint (unix): To capture all the output that pass by use the 'script'
    utility 
    $ script capturefile.txt
    $ mvn jetty:run
    $ exit
    
    Now all the output has been captured to a file, which can be
    examined.
    
3.4 You can try to build a deployable war file by typing
    $ mvn package

    Now you should have target/gd-ice-1.0-SNAPSHOT.war and
    target/gd-ice-1.0-SNAPSHOT. Deploy one of these to your webserver
    install.

      
================
4. Website Setup
================

When the site is installed and deployed, log in using the default
Administrator account. It is 'Administrator' with the password
'Administrator'. Please change the default password.  Administrator's
page is located at https://yoursite/admin.

==============================
5. Configuring for Development
==============================

5.1 We use Eclipse IDE for ICE development. Go to
    http://www.eclipse.org and download the JAVA EE Edition of
    Eclipse. After Eclipse installation, install the M2Eclipse plugin:
    Go to the Help menu, select "Install New Software", select
    "Add...", and add a new software site (location is
    http://download.eclipse.org/technology/m2e/releases), then follow
    the prompts.

5.2 Once Eclipse is installed, import the root ICE directory (the one
    with .project, and this README.txt file) as an existing
    project. Select File from the menu, choose Import, then select
    Maven -> Existing Maven Projects, then select gd-ice as the Root.

    It may take a while for Eclipse to "update indexes" when run for
    the first time. You can see it's progress by clicking on the
    button next to the small progress bar. Be patient.
    
5.3 Debugging
    Select Run from the menu, then select Debug Configurations...
    Click on Maven Build, and click the New button.  Put "jetty run"
    in the name field, select the ICE root as the base directory by
    clicking on Browse Workspace, and put "jetty:run" as the
    goal. Select Apply then Debug. This should launch jetty, the same
    way as in Section 3.

5.3.1. To test a functioning debug set up, open up
       WicketApplication.java file by pressing Shift-Ctrl-R, and type
       in WicketApplication.java in the search box. Set a breakpoint
       on the line "mountPages();" (around line 53) by double clicking
       on the left margin, below the green triangle. Launch jetty run
       again, and Eclipse should stop at the specified line waiting
       for your input. Press the green Play button in the Debug pane
       to continue, red square to stop the program.  If you see the
       "Source not Found" error, click on the Edit Source Lookup Path
       button, click Add, select Project, then check the gd-ice
       project. Use F6 button to step through the source code.

5.3.2 Perspectives in Eclipse
      If you chose to allow Eclipse to "change to the debug
      perspective", you will see a very differently layed out
      screen. Perspectives can be changed by pressing the perspective
      buttons on the top right, (button tray can be resized) or by
      going to the Window-> Open Perspective menu. Typically, the
      JavaEE perspective and the Debug perspective are the most used.

5.3.3 Eclipse Tips
      Eclipse is a large and powerful program for software development.
      It has a steep learning curve, so here are some tips.

      Perspectives: These are groups of windows. They can be selected
      from the top right of the screen. Initially, Java EE is available,
      and when one starts to debug, a Debug perspective opens up. Each
      perspective can display whatever kinds of windows.

      Views: Views are kinds of windows. Editor, Project Explorer, Console,
      and Debug are some of these views. Any View can be added to any
      Perspective by selecting the Window menu -> Show View -> etc.
      When Eclipse is closed, the position and the layout of Views
      is preserved for each Perspective. 

      Sometimes Perspectives get confused. Select Window -> Reset
      Perspective to reset a customized Perspective.

      Sometimes Eclipse itself gets confused. It is a good idea to
      make a backup copy of a working version of the eclipse settings
      (.metadata folder in the workspace folder) periodically.  Same
      goes with .settings folder in the project directory.

      To show line numbers in the editor, go to Window -> Preferences
      -> General -> Editors -> Text Editors and select 'Show line
      numbers'.

      Eclipse has a very powerful automatic completion. Press
      Ctrl-Space to get context assistance, and Ctrl-1 for "quick
      fix", which can fix many different types of compilation errors
      almost automatically.
      
      Use the ICE source formatter. Go to Window -> Preferences ->
      Java -> Code Style -> Formatter and import the Registry Team
      Formatter.xml file. Format can then be performed by pressing
      Ctrl-Shift-F. Eclipse can be configured to automatically format
      Java code all the time by selecting Preferences -> Java ->
      Editor -> Save Actions and selecting Format source code on save.
      
      
====================
6. Contributing Code
====================

We will gladly accept patches to our code. Just send us an email
and/or a patch file.

We ask that you conform to our formatting guidelines. Simply set up
your Eclipse to use the included "Registry Team Formatter.xml", and
auto format your file.


==========
7. Credits
==========

Project Lead: Timothy Ham <tsham@lbl.gov>
Developer: Hector Plahar <haplahar@lbl.gov>

Alumni:
Developer: Zinovii Dmytriv
