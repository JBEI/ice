FROM maven:3-jdk-13 as maven

COPY . /tmp

WORKDIR /tmp

RUN sed -i.bak -E \
        -e 's|<security-constraint>|<!--<security-constraint>|;' \
        -e 's|</security-constraint>|</security-constraint>-->|;' \
        /tmp/src/main/webapp/WEB-INF/web.xml \
    && rm /tmp/src/main/webapp/WEB-INF/web.xml.bak \
    && mvn -Dwar.warName=ice -DskipTests=true package

# -----

FROM tomcat:9

RUN apt-get clean \
    && apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y -q --no-install-recommends \
        ncbi-blast+=2.6.0-1 \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf /usr/local/tomcat/webapps/*

COPY --from=maven /tmp/target/ice-5.7.0.war /usr/local/tomcat/webapps/ROOT.war
