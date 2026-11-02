(ns gravity.c7-type-checker.verification
  "Compatibility entry points for hosted seed verification owned by gravity.types."
  (:require [gravity.types :as types]))

(defn typed-core-verifier-report
  [nodes type-facts constraints functions dynamic cast generic dispatch schema layout]
  (types/typed-core-verifier-report
   nodes type-facts constraints functions dynamic cast generic dispatch schema layout))

(defn type-capability-proof [diagnostic-ids artifact]
  (types/type-capability-proof diagnostic-ids artifact))

(defn validate! [type-capability-proof type-fail! source-path artifact]
  (types/validate! type-capability-proof type-fail! source-path artifact))
