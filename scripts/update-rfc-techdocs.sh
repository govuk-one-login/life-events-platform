# We are temporarily publishing some RFCs in our techdocs as they aren't published elsewhere
ROOT_DIR="$( git rev-parse --show-toplevel )"
ARCH_DIR="$(mktemp -d)"

git clone --depth 1 git@github.com:alphagov/digital-identity-architecture.git -b 0056-physical-data-model-death-registration-event "$ARCH_DIR"

DATA_MODEL_ERB="$ROOT_DIR/techdocs/source/data-model.html.md.erb"
echo "---
title: Data model for life events
weight: 15
---" > "$DATA_MODEL_ERB"
cat "$ARCH_DIR/rfc/0056-physical-data-model-death-notification-event.md" >> "$DATA_MODEL_ERB"
sed -i "" "
s/RFC [0-9]* //
s/\> \[\!NOTE\]//
s/\> \[\!IMPORTANT\]//
/\> \[\!WARNING\]$/{
  N
  s/\> \[\!WARNING\]\n> \(.*\)/<%= warning_text\('\1'\) %>\n/
}
" "$DATA_MODEL_ERB"

rm -rf "$ARCH_DIR"
