ICE
===

Inventory of Composable Elements (ICE) is an open source registry
software for biological parts developed by the `Joint
BioEnergy Institute <http://www.jbei.org/>`__. It is a Web application used by laboratories to track and search their
constructs.

ICE is distributed under the Modified BSD license. See LICENSE.txt and LIBRARY_LICENSES.txt for details.

You can try out the software at our `Public
Registry <http://public-registry.jbei.org>`__.

Documentation for ICE Users
-------------

Read the `Manual <https://jbei.github.io/ice/>`__ for installation and usage instructions.


Documentation for ICE developers
--------------------------------

Requirements
~~~~~~~~~~~~
To set up a development environment for ICE you will need to install:

* `BLAST+ <http://blast.ncbi.nlm.nih.gov/Blast.cgi?PAGE_TYPE=BlastDocs&DOC_TYPE=Download>`__ ≥ 2.2.28
* `Java JDK 7 <http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html>`__
* `Maven <https://maven.apache.org/download.cgi>`__  ≥ 3.0

If you use a Debian-like operating system, the following command will install all the requirements

::

  sudo apt-get install ncbi-blast+ default-jdk maven


Set Up
~~~~~~
1. Checkout the code and enter the repository

::

  git checkout https://github.com/JBEI/ice.git
  cd ice

2. Since the application requires an SSL certificate, generate one that the Jetty Web server can use this command. When prompted for a password, enter **changeit**

::

  keytool -genkey -alias tomcat -keyalg RSA -keystore ./.keystore

3. Start Jetty

::

  mvn jetty:run

4. Point your browser to https://localhost:8443/ to access the application

Links
-----

* `ICE Google Group <http://groups.google.com/group/gd-ice>`__
* `Releases <https://github.com/JBEI/ice/releases>`__: Download the war file associated with the latest release
* `REST API WADL <https://public-registry.jbei.org/rest/application.wadl>`__

Related Projects
----------------

`The VectorEditor project <https://github.com/JBEI/vectoreditor/>`__ is
used in ICE to display and edit sequences. It also contains other
modules such as sequence checker.

|Build Status|

.. |Build Status| image:: https://travis-ci.org/JBEI/ice.svg?branch=dev
   :target: https://travis-ci.org/JBEI/ice
