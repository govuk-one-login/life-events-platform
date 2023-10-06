AWS_ACCOUNT=${1}

PROVISION_COMMAND="../../di-devplatform-deploy/stack-orchestration-tool/provisioner.sh"

export AUTO_APPLY_CHANGESET=true
export SKIP_AWS_AUTHENTICATION=true
export AWS_PAGER=""

## Check template values are substituted
if [ -f configuration/"$AWS_ACCOUNT"/vpc/parameters.json ]; then
  if ! awk '/<SFTP_IP>/ {offset = match($0, /<SFTP_IP>/); print "SFTP_IP parameter must be substituted at configuration/'"$AWS_ACCOUNT"'/vpc/parameters.json:" NR ":" offset; exit 1}' configuration/"$AWS_ACCOUNT"/vpc/parameters.json;
  then
    exit 1
  fi
else
    echo "No VPC parameters found, skipping. See README for instructions on use of template files."
fi

## Provision dependencies
for dir in configuration/"$AWS_ACCOUNT"/*/; do
  STACK=$(basename "$dir")
  if [[ $STACK != "sam-deploy-pipeline" && -f configuration/$AWS_ACCOUNT/$STACK/parameters.json ]]; then
    $PROVISION_COMMAND "$AWS_ACCOUNT" "$STACK" "$STACK" LATEST &
  fi
done

## Provision secure pipelines
$PROVISION_COMMAND "$AWS_ACCOUNT" sam-deploy-pipeline sam-deploy-pipeline LATEST
