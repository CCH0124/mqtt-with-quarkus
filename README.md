# mqtt-with-quarkus

## EMQX Operator

[Office Operator](https://github.com/emqx/emqx-operator)

```bash
$ helm repo add jetstack https://charts.jetstack.io
$ helm repo update
$ helm upgrade --install cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --set installCRDs=true
```

```bash
$ helm upgrade --install emqx-operator emqx/emqx-operator  --namespace emqx-operator-system --create-namespace  --version 2.2.3
```
