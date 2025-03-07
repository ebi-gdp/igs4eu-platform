# Cron job

Cron job to update PGS Ids & Publication Metadata from PGS Catalog APIs.

# Script
`run.sh` file contains instructions to fetch & push PGS data to Redis. This script fetches PGS Ids & Publications data from `https://www.pgscatalog.org` and `https://ftp.ebi.ac.uk/pub/databases/spot/pgs`.
Modify this file to change behavior of the script.

## Key points
Inside `run.sh` look for `redis_host`, value of the variable changes according to env. e.g. dev, test & prod. Default configured for dev env, controlled via environment variable `NAMESPACE` via helm charts.

# Build & push docker image
Navigate to `01-build` & build the Docker image, run the following command

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
Navigate to `02-deployment`, you can change cronjob schedule time, navigate to `templates/02-cron-job.yaml`, change `schedule` to required crontab regular expression.
Default setting is `15 1 * * *`. Runs at midnight 1:15.

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
