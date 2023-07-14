#!/usr/bin/env bash

set -eu

find . -type d -maxdepth 1 -regex '^\./[^.]*' ! -name 'scripts' ! -name 'tf-app'
