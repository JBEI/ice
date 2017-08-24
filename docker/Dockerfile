FROM maven:3.5 as maven

ARG ICE_VERSION=5.3.2
ARG GIT_BRANCH=master
ARG GIT_URL=https://github.com/JBEI/ice.git
RUN git clone --depth 1 -b ${GIT_BRANCH} ${GIT_URL} ice \
    && echo "Cache busting with version ${ICE_VERSION}"

# overwriting the git version of hibernate config
COPY hibernate.cfg.xml /ice/src/main/resources/hibernate.cfg.xml

# comment out the section of web.xml that auto-redirects to HTTPS port
# when ICE upgrades Maven WAR Plugin, use -Dproject.build.finalName=ice
RUN cd /ice \
    && sed -i.bak -E \
        -e 's/<security-constraint>/<!--<security-constraint>/;' \
        -e 's/<\/security-constraint>/<\/security-constraint>-->/;' \
        /ice/src/main/webapp/WEB-INF/web.xml \
    && rm /ice/src/main/webapp/WEB-INF/web.xml.bak \
    && mvn -Dwar.warName=ice package

# -----

FROM tomcat:8

RUN apt-get clean && apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y -q \
        ncbi-blast+ \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf /usr/local/tomcat/webapps/*
COPY --from=maven /ice/target/ice.war /usr/local/tomcat/webapps/ROOT.war
