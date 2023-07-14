#!/usr/bin/env bash

set -eu

script_path="$(dirname "$0")"
readonly script_path

function main() {
  local status=0
  local stack_name

  for stack_path in $("$script_path/find_stack_directories.sh"); do
    stack_name="$(basename "$stack_path")"
    echo "Linting stack '${stack_name}'..." 1>&2
    cfn-lint "${stack_path}/**/template.yaml" || status=$?
  done

  return $status
}

main "$@"
