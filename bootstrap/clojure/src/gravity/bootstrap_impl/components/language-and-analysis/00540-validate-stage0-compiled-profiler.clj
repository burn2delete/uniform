

(defn validate-stage0-compiled-profiler!
  [module profiler-report]
  (gravity.tooling/native-validate-profiler! {:fail (fn [] compiled-tooling-fail!) :present (fn [] p13-present?)} module profiler-report))

(defn validate-stage0-compiled-safety-audit!
  [module safety-audit-report]
  (gravity.tooling/native-validate-safety-audit! {:fail (fn [] compiled-tooling-fail!) :present (fn [] p13-present?)} module safety-audit-report))