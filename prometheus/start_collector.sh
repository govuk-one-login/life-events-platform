envsubst < adot-config.template.yaml > adot-config.yaml
/awscollector --config=/etc/ecs/adot-config.yaml
