FROM maven:3-openjdk-16 as maven

COPY ./pom.xml /tmp
WORKDIR /tmp

# attempting to grab all dependencies
# NOTE: go-offline does *NOT* prevent re-download of core Maven plugins
# Including a package goal here w/o source will force refresh Maven
RUN mvn dependency:go-offline package

COPY . /tmp

RUN sed -i.bak -E \
        -e 's|<security-constraint>|<!--<security-constraint>|;' \
        -e 's|</security-constraint>|</security-constraint>-->|;' \
        /tmp/src/main/webapp/WEB-INF/web.xml \
    && rm /tmp/src/main/webapp/WEB-INF/web.xml.bak \
    && cp /tmp/docker/hibernate.cfg.xml /tmp/src/main/resources/hibernate.cfg.xml \
    && cp /tmp/docker/logback.xml /tmp/src/main/resources/logback.xml \
    && mvn -DskipTests=true package

# -----

FROM tomcat:9-jdk17

RUN apt-get clean \
    && apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y -q --no-install-recommends \
        ncbi-blast+ \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf /usr/local/tomcat/webapps/* \
    && sed -i.bak -E \
        -e '\|^</Context>$|i <Resources cachingAllowed="true" cacheMaxSize="100000" />' \
        /usr/local/tomcat/conf/context.xml \
    && rm /usr/local/tomcat/conf/context.xml.bak

# Override CATALINA_OPTS environment to change from default db connection
ENV CATALINA_OPTS="-Dice.db.url=jdbc:postgresql://postgres/ice -Dice.db.user=iceuser -Dice.db.pass=icepass"

COPY --from=maven /tmp/target/ice-*.war /usr/local/tomcat/webapps/ROOT.war
