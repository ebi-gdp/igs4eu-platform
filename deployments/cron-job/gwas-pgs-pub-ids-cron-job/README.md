# Cron job

Cron job to update PGS Ids & Publication Metadata from PGS Catalog APIs.

# Build & push docker image
Navigate to `01-build` & to build the Docker image, run the following command

```
# command
docker build -t dockerhub.ebi.ac.uk/gdp/igs4eu-platform:pgs-ids-cron-job-1.0.0-{dev} --build-arg TARGET_PLATFORM=linux/amd64 .

# example
docker build -t dockerhub.ebi.ac.uk/gdp/igs4eu-platform:pgs-ids-cron-job-1.0.0-dev --build-arg TARGET_PLATFORM=linux/amd64 .
```

Docker push - make sure you have rights to push image to gitlab container registry

```
# command
docker push dockerhub.ebi.ac.uk/gdp/igs4eu-platform:pgs-ids-cron-job-1.0.0-{env}

# example
docker push dockerhub.ebi.ac.uk/gdp/igs4eu-platform:pgs-ids-cron-job-1.0.0-dev
```

## Config files
Cron job can be deployed using `helm` charts.

| Values file        | Env.   | Purpose                     |
|--------------------|--------|-----------------------------|
| `values-dev.yaml`  | `dev`  | Values for `dev` instance.  |
| `values-prod.yaml` | `prod` | Values for `prod` instance. |

You can add more `values-{env}.yaml` files according to env.

Update `values-{env}.yaml` file with `imageVersion` used to build docker image e.g. `1.0.0`.
`imagePostfixTag` with `{env}` e.g. `dev`.

# Deployment
Navigate to `02-deployment` & then run following

## Development cluster
```
helm upgrade --install gwas-pgs-ids-pub-data-cronjob ./helm-charts -f ./helm-charts/values-dev.yaml
```

## Production cluster
```
helm upgrade --install gwas-pgs-ids-pub-data-cronjob ./helm-charts -f ./helm-charts/values-prod.yaml
```

**Delete release**

```
# command
helm delete {release-name}

# example
helm delete gwas-ids-cronjob
```

# Check deployment status
```
# command
kubectl -n {env} get CronJob

# example
kubectl -n intervene-dev get CronJob
```
