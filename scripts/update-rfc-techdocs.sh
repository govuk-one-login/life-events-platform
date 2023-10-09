# We are temporarily publishing some RFCs in our techdocs as they aren't published elsewhere
ROOT_DIR="$( git rev-parse --show-toplevel )"
if [ -z "$ARCH_DIR" ]; then
  ARCH_DIR="$(mktemp -d)"
  CREATED=1

  if [[ -z "${ARCH_TOKEN}" ]]; then
    GIT_URI="git@github.com:"
  else
    GIT_URI="https://${ARCH_TOKEN}@github.com/"
  fi

  git clone --depth 1 "${GIT_URI}alphagov/digital-identity-architecture.git" "$ARCH_DIR"
fi

DATA_MODEL_ERB="$ROOT_DIR/techdocs/source/data-model.html.md.erb"
echo "---
title: Data model for life events
weight: 15
---" > "$DATA_MODEL_ERB"

CALLOUT_SUBS="s/> \[\!NOTE\]/> ⓘ Note\n>/
s/>[ ]?\*\*Note\*\*/> ⓘ Note\n>/
s/> \[\!IMPORTANT\]/> ⚠ Important\n>/
/> \[\!WARNING\]$/{
  N
  s/> \[\!WARNING\]\n> (.*)/<%= warning_text\('\1'\) %>\n/
}
"

REDUCE_HEADINGS="s/^#### /##### /
s/^### /#### /
s/^## /### /
s/^# /## /
"

sed -E """
s/RFC [0-9]* //
$CALLOUT_SUBS
s/\[address structure RFC\]\(0020-address-structure.md\)/[address structure section](#address-structure)/
s/\[core identity representation RFC\]\(0011-identity-representation.md#4-names\)/[core identity atttributes section](#4-names)/

""" "$ARCH_DIR/rfc/0056-physical-data-model-death-notification-event.md" >> "$DATA_MODEL_ERB"

echo "

" >> "$DATA_MODEL_ERB"
sed -E """
s/RFC [0-9]* //
$CALLOUT_SUBS
$REDUCE_HEADINGS
""" "$ARCH_DIR/rfc/0020-address-structure.md" >> "$DATA_MODEL_ERB"

echo "

" >> "$DATA_MODEL_ERB"
sed -E """
s/RFC 0011 Data representation for identity and identity confidence/Core identity attributes/
$CALLOUT_SUBS
$REDUCE_HEADINGS
""" "$ARCH_DIR/rfc/0011-identity-representation.md" >> "$DATA_MODEL_ERB"

if [ $CREATED ]; then
  rm -rf "$ARCH_DIR"
fi
