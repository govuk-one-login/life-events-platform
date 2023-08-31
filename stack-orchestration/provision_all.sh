AWS_ACCOUNT=${1}

PROVISION_COMMAND="../../di-devplatform-deploy/stack-orchestration-tool/provisioner.sh"

export AUTO_APPLY_CHANGESET=true
export SKIP_AWS_AUTHENTICATION=true
export AWS_PAGER=""

./decrypt_vpc_parameters.sh

## Provision dependencies
for dir in configuration/"$AWS_ACCOUNT"/*/; do
  STACK=$(basename "$dir")
  if [[ $STACK != "sam-deploy-pipeline" ]]; then
    $PROVISION_COMMAND "$AWS_ACCOUNT" "$STACK" "$STACK" LATEST &
  fi
done

## Provision secure pipelines
$PROVISION_COMMAND "$AWS_ACCOUNT" sam-deploy-pipeline sam-deploy-pipeline LATEST
