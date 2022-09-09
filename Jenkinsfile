#!/usr/bin/env groovy

buildRepo([
    "build": [
        "docker": [
            "dockerfile": "docker/Dockerfile",
            "repo": "ice",
        ],
    ],
    "email": ["cc": "ese-iops@lbl.gov"],
])
swarmDeploy()
