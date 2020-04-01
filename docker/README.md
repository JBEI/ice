## Running ICE in Docker

ICE can be deployed using Docker. The `Dockerfile` in this directory builds ICE
with Maven, then copies the WAR into a Tomcat image. The `docker-compose.yml`
will launch ICE with a Postgres database, with persistent volumes for the
database, search index, and blast index.

### Building the ICE image

To build ICE, just run from the project root:

```bash
docker build -t jbei/ice:latest -f docker/Dockerfile .
```


### Launching ICE

The `docker-compose.yml` file here will launch ICE on port 9999 on the host
loopback interface (e.g. [http://localhost:9999/](http://localhost:9999/)). The
instance will initially have no data or configuration. The default login uses
`Administrator` for both the username and password. Launch from the `docker`
directory using the command:

```bash
docker-compose up
```
