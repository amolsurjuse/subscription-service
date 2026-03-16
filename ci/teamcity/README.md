# TeamCity pipeline

Provision the TeamCity pipeline with:

```bash
TEAMCITY_TOKEN='<token>' ./ci/teamcity/setup_pipeline.sh
```

The pipeline builds the jar, builds and pushes the Docker image, and then tries to update the dev image tag in `k8s-platform`. If the `subscription-service` deploy version file does not exist yet, the final step exits successfully and logs that the update was skipped.
