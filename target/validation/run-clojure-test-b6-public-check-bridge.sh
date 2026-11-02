#!/usr/bin/env bash
set -u
cd /Users/mattr/code/matt/gravity || exit 99
export PATH="/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"
/opt/homebrew/bin/clojure -M:test > target/validation/clojure-test-b6-public-check-bridge.log 2>&1
printf "%s\n" "$?" > target/validation/clojure-test-b6-public-check-bridge.status
