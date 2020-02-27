## ICE: Inventory of Composable Elements
ICE is a registry platform that provides robust data storage for DNA components, integrated tools for part characterization, as well as mechanisms for secure access and information sharing with other users and software tools.

ICE is open source and distributed under the Modified BSD license. You can try it out at [https://public-registry.jbei.org](https://public-registry.jbei.org) by creating a free account

## Major Features
* Support for storing biological parts in addition to plasmids, microbial strains and *Arabidopsis* seeds. Supported sequence formats are genbank, FASTA and SBOL
* Full text and BLAST search capabilities
* Advanced collection management
* Graphical application and other tool integration for sequence design, annotation and verification
* Real time DNA editing with live vector map display and sophisticated feature annotation
* Opt-in community collaboration capabilities that enable dataset publishing and sharing across multiple ICE instances
* Granular read and write permissions for entries or collections across users and groups 
* Automatic Sequence Annotation

## Installation
### Production
To install in a production environment, please [consult our manual](http://ice.jbei.org/install) for detailed setup and configuration instructions.

### Development
To set up a development environment or local machine installation make sure you have the following dependencies installed

* [Java JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Maven 3](https://maven.apache.org/download.cgi)
* [Git](https://git-scm.com/downloads)

Command line installation steps (linux environment):

* Clone this repo
     
        git clone https://github.com/JBEI/ice.git ./ice
        cd ice
        
* Build ICE. This may take a few minutes to download additional dependencies

        mvn clean install -DskipTests=true
        
* Start the embedded undertow server 
        
        java -cp "./target/ice-{version-number}-classes.jar:./target/ice-{version-number}/WEB-INF/lib/*" DevelopmentServer
        
  Where "ice-{version-number}" is the version of ICE that was cloned. e.g. "ice-5.6.0"

* Access the application at [http://localhost:8080](http://localhost:8080) and login using username and password "Administrator"

* Ctrl + C to stop the application


## Links
* [Documentation](http://ice.jbei.org/) including user manual and API documentation

## Build Status:
[![Build Status](https://travis-ci.org/JBEI/ice.svg?branch=master)](https://travis-ci.org/JBEI/ice)

## Related Projects
[Open Vector Editor](https://github.com/TeselaGen/openVectorEditor) is used in ICE to display and edit sequences
