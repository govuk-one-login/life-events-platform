# We are temporarily publishing some RFCs in our techdocs as they aren't published elsewhere
ROOT_DIR="$( git rev-parse --show-toplevel )"

DATA_MODEL_ERB="$ROOT_DIR/techdocs/source/data-model.html.md.erb"
echo "---
title: Data model for life events
weight: 20
---" > $DATA_MODEL_ERB
cat $ROOT_DIR/techdocs/digital-identity-architecture/rfc/0056-physical-data-model-death-notification-event.md >> $DATA_MODEL_ERB
sed -iE "
s/RFC [0-9]* //
s/\> \[\!NOTE\]//
s/\> \[\!IMPORTANT\]//
/\> \[\!WARNING\]$/{
  N
  s/\> \[\!WARNING\]\n> \(.*\)/<%= warning_text\('\1'\) %>\n/
}
" $DATA_MODEL_ERB
