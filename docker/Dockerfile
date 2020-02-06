FROM tomcat:9

RUN apt-get clean \
    && apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y -q --no-install-recommends \
        ncbi-blast+=2.6.0-1 \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf /usr/local/tomcat/webapps/*

COPY ./target/ice-5.7.0.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8443
