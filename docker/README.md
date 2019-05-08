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
        --build-arg "ICE_VERSION=$(date "+%Y%m%d")" \
        .

### Launching ICE

The `docker-compose.yml` file here will launch ICE on port 9999 on the host loopback interface
(e.g. [http://localhost:9999/](http://localhost:9999/)). The instance will initially have no data
or configuration. The default login uses `Administrator` for both the username and password.
Launch using the command:

    docker-compose up -d

### Setting HMAC authentication keys

The Docker image will look for keys saved using the `docker secret` commands, and install those
keys for use in Hash-based Message Authentication Code (HMAC) authentication to the REST API.
If the container is launched with a `ICE_HMAC_SECRETS` environment, it will split the value on
commas, then install keys with those names. If the name has a `:` colon character, the first
part of the name will be the name of the Docker secret, and the second part of the name is the
key ID used to identify the key in ICE. The following example will create an ICE service with
keys for both EDD and DIVA, with the REST API using key IDs of `edd` and `diva`, respectively.

    docker secret create edd_ice_key edd.key
    docker secret create diva diva.key
    docker service create \
        -e "ICE_HMAC_SECRETS=edd_ice_key:edd,diva" \
        --secret edd_ice_key \
        --secret diva \
        jbei/ice
