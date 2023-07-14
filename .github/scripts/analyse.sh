#!/usr/bin/env bash

set -eu

script_path="$(dirname "$0")"
readonly script_path

function main() {
  local status=0
  local stack_name

  for stack_path in $("$script_path/find_stack_directories.sh"); do
    stack_name="$(basename "$stack_path")"
    echo "Analysing stack '${stack_name}'..." 1>&2
    checkov \
        -d "${stack_path}" \
        --framework cloudformation -o github_failed_only \
        --skip-path "^.*/test.*.yaml$" \
        || status=$?
  done

  return $status
}

main "$@"
