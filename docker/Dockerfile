FROM maven:3-openjdk-16 as maven

LABEL maintainer="William Morrell <WCMorrell@lbl.gov>"

ARG ICE_VERSION=5.5.2
ARG GIT_BRANCH=master
ARG GIT_URL=https://github.com/JBEI/ice.git
RUN git clone --depth 1 -b "${GIT_BRANCH}" "${GIT_URL}" ice \
    && git -C ice rev-parse --short HEAD > ice.hash \
    && echo "Building ICE ${ICE_VERSION} ($(cat ice.hash))" \
    && java -version

# overwriting the git version of hibernate config
COPY hibernate.cfg.xml /ice/src/main/resources/hibernate.cfg.xml

WORKDIR /ice

# comment out the section of web.xml that auto-redirects to HTTPS port
# when ICE upgrades Maven WAR Plugin, use -Dproject.build.finalName=ice
RUN sed -i.bak -E \
        -e 's/<security-constraint>/<!--<security-constraint>/;' \
        -e 's/<\/security-constraint>/<\/security-constraint>-->/;' \
        /ice/src/main/webapp/WEB-INF/web.xml \
    && rm /ice/src/main/webapp/WEB-INF/web.xml.bak \
    && mvn -Dwar.warName=ice package

# -----

FROM tomcat:9-jdk16

RUN apt-get clean && apt-get update \
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

COPY --from=maven /ice/target/ice.war /usr/local/tomcat/webapps/ROOT.war
COPY entrypoint.sh /usr/local/bin/

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["catalina.sh", "run"]
