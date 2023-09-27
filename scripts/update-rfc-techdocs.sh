# We are temporarily publishing some RFCs in our techdocs as they aren't published elsewhere
ROOT_DIR="$( git rev-parse --show-toplevel )"
ARCH_DIR="$(mktemp -d)"

if [[ -z "${ARCH_TOKEN}" ]]; then
  GIT_URI="git@github.com:"
else
  GIT_URI="https://${ARCH_TOKEN}@github.com/"
fi

git clone --depth 1 "${GIT_URI}alphagov/digital-identity-architecture.git" "$ARCH_DIR"

DATA_MODEL_ERB="$ROOT_DIR/techdocs/source/data-model.html.md.erb"
echo "---
title: Data model for life events
weight: 15
---" > "$DATA_MODEL_ERB"

sed "
s/RFC [0-9]* //
s/> \[\!NOTE\]//
s/> \[\!IMPORTANT\]//
/> \[\!WARNING\]$/{
  N
  s/> \[\!WARNING\]\n> \(.*\)/<%= warning_text\('\1'\) %>\n/
}
" "$ARCH_DIR/rfc/0056-physical-data-model-death-notification-event.md" >> "$DATA_MODEL_ERB"

rm -rf "$ARCH_DIR"
