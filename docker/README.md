## Running ICE in Docker

ICE can be deployed using Docker. The `Dockerfile` in this directory will build an image using a
specific branch of a specific git repository, and deploy the built WAR file to Tomcat. The
`docker-compose.yml` will launch ICE with a Postgres database, with persistent volumes for the
database, search index, and blast index.

### Building the ICE image

To build ICE with the `HEAD` of `master` on the JBEI GitHub repository, just run:

    docker build -t jbei/ice .

To build with a different branch or repository, pass in parameters with `--build-arg` flags. The
`Dockerfile` recognizes these flags:
- `GIT_BRANCH`: the branch name to checkout after clone
- `GIT_URL`: the repository location for clone
- `ICE_VERSION`: currently serves only to skip the Docker image cache when using the same
  `GIT_BRANCH` and `GIT_URL` in later builds. This only affects the cache for the clone operation,
  where `docker build --no-cache` will ignore all cached layers.

To build daily images based on the `dev` branch, use a build command similar to this:

    docker build -t "jbei/ice:$(date "+%Y%m%d")-dev" \
        --build-arg "GIT_BRANCH=dev" \
        --build-arg "ICE_VERSION=$(date "+%Y%m%d")"
        .

### Launching ICE

The `docker-compose.yml` file here will launch ICE on port 9999 on the host loopback interface
(e.g. [http://localhost:9999/](http://localhost:9999/)). The instance will initially have no data
or configuration. The default login uses `Administrator` for both the username and password.
Launch using the command:

    docker-compose up
