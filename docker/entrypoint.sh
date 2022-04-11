#!/bin/bash

set -o pipefail -e

# Create the rest-auth directory
mkdir -p data/rest-auth
# Attempt to link HMAC secrets
while read secret
do
    if [ -z "${secret}" ]; then
        break
    fi
    src_secret="${secret%%:*}"
    tgt_secret="${secret#*:}"
    if [ ! -r "/run/secrets/${src_secret}" ]; then
        echo "Cannot read secret ${src_secret}!"
    elif [ -f "data/rest-auth/${tgt_secret}" ]; then
        echo "REST HMAC key ${secret#*:} already exists!"
    else
        ln -s "/run/secrets/${src_secret}" "data/rest-auth/${tgt_secret}"
    fi
done < <(echo "${ICE_HMAC_SECRETS}" | tr ',' '\n')

exec "$@"
