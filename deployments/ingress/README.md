# Kubernetes ingress config

This document outlines the Ingress configuration for managing front-end and back-end traffic. Allows external traffic to hit frontend & backend services.

## **Ingress Configuration**

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: app-ingress-front-back-end-config
  namespace: {replace-with-namespace}
  annotations:
    # Assigns a static IP address in GCP e.g. intervene-dev-static-ip => https://console.cloud.google.com/networking/addresses/list?referrer=search&inv=1&invt=AbrVWQ&project=prj-ext-dev-intervene-413412
    kubernetes.io/ingress.global-static-ip-name: {replace-with-static-ip-name}

    # Uses a managed SSL certificate.
    networking.gke.io/managed-certificates: managed-cert

    # Enforces TLS security protocols.
    nginx.ingress.kubernetes.io/ssl-protocols: "TLSv1.2 TLSv1.3"

    # Enables regex support in path matching.
    nginx.ingress.kubernetes.io/use-regex: "true"

    # Redirects all HTTP requests to HTTPS.
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"

    # Specifies the Ingress class as GCE, required for GCP managed certs.
    kubernetes.io/ingress.class: "gce"

    # Custom Nginx server configuration, HSTS config.
    nginx.ingress.kubernetes.io/server-snippet: |
      rewrite ^/(.*)/$ /$1 permanent;
      add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload" always;
spec:
  rules:
  # Hostname e.g. gcp.geneticscores.org, calculate.geneticscores.org.  
  - host: {replace-with-hostname}
    http:
      paths:
      # Route all requests prefixed with /bff. Routes all backend requests.
      - path: /bff/
        pathType: Prefix
        backend:
          service:
            name: backend-for-frontend
            port:
              number: 8080
      # Route all remaining requests that are not prefixed with /bff. Routes frontend requests.
      - path: /
        pathType: Prefix
        backend:
          service:
            name: igs4eu-frontend
            port:
              number: 3000
```

## Deployment
Currently, 2 environments are running, Dev & Prod. Test env. development is in progress. Modify/add according to exiting Or new environments!

### Development
```bash
# Navigate to
cd igs4eu-platform/deployments/ingress/config/gcp

# Then run
kubectl apply -f ssl/managed-cert-dev.yaml

# Now deploy ingress config
kubectl apply -f ingress-ip-frontend-and-backend-gcp-dev-env-geneticscores-gcp-cert.yaml

# Check status of deployment
kubectl -n intervene-dev get ingress
```

### Production
```bash
# Navigate to
cd igs4eu-platform/deployments/ingress/config/gcp

# Then run
kubectl apply -f ssl/managed-cert-prod.yaml

# Now deploy ingress config
kubectl apply -f ingress-ip-frontend-and-backend-gcp-prod-env-geneticscores-gcp-cert.yaml

# Check status of deployment
kubectl -n intervene-prod get ingress
```

After successful deployment, wait for ingress config to set up & running. You can check the status on GCP console

Dev => [GCP Dev Ingress Status](https://console.cloud.google.com/kubernetes/ingresses?inv=1&invt=AbrVWQ&project=prj-ext-dev-intervene-413412).

Prod => [GCP Prod Ingress Status](https://console.cloud.google.com/kubernetes/ingresses?inv=1&invt=AbrVWQ&project=prj-ext-prod-intervene-437413).
