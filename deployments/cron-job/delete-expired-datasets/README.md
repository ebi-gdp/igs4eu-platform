# Cron job

Cron job to trigger delete expired datasets API.

## Config files
Cron job can be deployed using `helm` charts.

| Values file        | Env.   | Purpose                     |
|--------------------|--------|-----------------------------|
| `values-dev.yaml`  | `dev`  | Values for `dev` instance.  |
| `values-prod.yaml` | `prod` | Values for `prod` instance. |

Values file requires `basic-auth` value, this is a combination of

```
# Refer username & password from pipeline-manager service's basic auth details
# username=pipeline-manager basic auth username
# password=pipeline-manager basic auth password
# basicAuth: base64(Basic base64(username:password))
```

You can add more `values-{env}.yaml` files according to env.

# Deployment

## Development cluster
```
helm upgrade --install dataset-cronjob ./helm-charts -f ./helm-charts/values-dev.yaml
```

## Production Cluster
```
helm upgrade --install dataset-cronjob ./helm-charts -f ./helm-charts/values-prod.yaml
```

# Check deployment status
```
# command
kubectl -n {env} get CronJob

# example
kubectl -n intervene-dev get CronJob
```
