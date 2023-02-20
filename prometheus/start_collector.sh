#!/bin/sh
envsubst < /etc/ecs/adot-config.template.yaml > /etc/ecs/adot-config.yaml
/awscollector --config=/etc/ecs/adot-config.yaml
