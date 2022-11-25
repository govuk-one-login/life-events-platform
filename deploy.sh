kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d; echo

kubectl port-forward svc/argocd-server -n argocd 8080:443

argocd login http://localhost:8080

argocd app create gdx-data-share-poc --repo https://github.com/alphagov/gdx-data-share-poc.git \
--path kube --dest-server https://kubernetes.default.svc --dest-namespace gdx-vision