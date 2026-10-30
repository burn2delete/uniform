;; SH-05 seed receipt lifetime and compatibility wiring. Rule algorithms live in gravity.macro.
(declare sh05-macro-artifact-verification* sh05-macro-artifact-verification
         sh05-macro-capability-based-proof-for-construction sh05-macro-capability-based-proof)
(let
 [canonical-operations
  ^{:line 129, :column 40}
  (macro-stage-operations)
  canonical-stage-root
  gravity.macro/verify
  canonical-roots
  (into
   {}
   ^{:line 133, :column 24}
   (map
    ^{:line 133, :column 29}
    (fn [v] [v ^{:line 133, :column 40} (var-get v)]))
   (remove
    (hash-set
     #'sh05-macro-artifact-verification*
     #'sh05-macro-artifact-verification
     #'sh05-macro-capability-based-proof-for-construction
     #'sh05-macro-capability-based-proof)
    (concat
     ^{:line 141, :column 36}
     (vals ^{:line 141, :column 42} (ns-publics 'gravity.macro))
     ^{:line 142, :column 36}
     (filter var? ^{:line 142, :column 49} (vals canonical-operations))
     [#'macro-stage-operations
      #'macro-stage-defaults
      #'macro-run-storage/exact?]
     [#'c4-macro-diagnostic-ids
      #'c4-macro-governing-document
      #'sh05-identity-chunk-width
      #'sh05-macro-adapter-contract
      #'sh05-macro-artifact-keys
      #'sh05-macro-cached-binding
      #'sh05-macro-envelope-stage
      #'sh05-macro-expected-function-count
      #'sh05-macro-expected-function-names-hash
      #'sh05-macro-expected-function-shapes-hash
      #'sh05-macro-expected-functions-semantic-hash
      #'sh05-macro-expected-plan-semantic-hash
      #'sh05-macro-expected-source-byte-count
      #'sh05-macro-expected-source-content-hash
      #'sh05-macro-pinned-plan-identities
      #'sh05-macro-public-function-hashes
      #'sh05-macro-public-function-shapes
      #'sh05-macro-sealed-artifact-kind
      #'sh05-macro-source-relative-path
      #'sh05-macro-version])))]
 ^{:line 117, :column 4}
 (defn
  sh05-macro-artifact-verification*
  ^{:line 118, :column 6}
  ([artifact allow-missing-proof?]
   ^{:line 119, :column 7}
   (sh05-macro-artifact-verification*
    artifact
    allow-missing-proof?
    nil))
  ^{:line 120, :column 6}
  ([artifact allow-missing-proof? invocation]
   ^{:line 121, :column 7}
   (gravity.macro/verify
    artifact
    allow-missing-proof?
    invocation
    ^{:line 121, :column 70}
    (macro-stage-operations))))
 (defn
  sh05-macro-artifact-verification
  ([artifact] (sh05-macro-artifact-verification artifact nil))
  ([artifact invocation]
   (when
    invocation
    (sh03-reader-authenticated-envelope-options
     (get-in artifact [:provenance :source-path])
     invocation))
   (sh07-proof-transaction-report
    :sh05
    :final
    {:verifier-root
     ^{:line 99, :column 4}
     (let
      [entries
       ^{:line 99, :column 18}
       (.iterator
        ^{:line 99, :column 29}
        (.entrySet ^java.util.Map canonical-roots))]
      ^{:line 100, :column 6}
      (if
       ^{:line 100, :column 10}
       (and
        ^{:line 100, :column 15}
        (identical? gravity.macro/verify canonical-stage-root)
        ^{:line 101, :column 15}
        (identical? macro-stage-defaults canonical-operations)
        ^{:line 102, :column 15}
        (loop
         []
         ^{:line 103, :column 17}
         (if
          ^{:line 103, :column 21}
          (.hasNext entries)
          ^{:line 104, :column 19}
          (let
           [entry
            ^{:line 104, :column 31, :tag java.util.Map$Entry}
            (.next entries)]
           ^{:line 105, :column 21}
           (and
            ^{:line 105, :column 26}
            (nil?
             ^{:line 105, :column 32}
             (.getThreadBinding
              ^{:line 105, :column 51, :tag clojure.lang.Var}
              (.getKey entry)))
            ^{:line 106, :column 26}
            (identical?
             ^{:line 106, :column 38}
             (.getValue entry)
             ^{:line 106, :column 56}
             (.deref
              ^{:line 106, :column 64, :tag clojure.lang.Var}
              (.getKey entry)))
            ^{:line 107, :column 26}
            (recur)))
          true)))
       ^{:line 109, :column 8}
       (var-get #'sh05-macro-artifact-verification*)
       ^{:line 110, :column 8}
       (Object.))),
     :report-schema-version 1,
     :invocation invocation,
     :check-catalog-domain :gravity/sh05-final-verification-checks,
     :source-content-hash sh05-macro-expected-source-content-hash,
     :plan-semantic-hash sh05-macro-expected-plan-semantic-hash,
     :functions-semantic-hash
     sh05-macro-expected-functions-semantic-hash}
    artifact
    (fn*
     []
     (sh05-macro-artifact-verification* artifact false invocation)))))
 (defn
  sh05-macro-capability-based-proof-for-construction
  ([artifact]
   (sh05-macro-capability-based-proof-for-construction artifact nil))
  ([artifact invocation]
   (when
    invocation
    (sh03-reader-authenticated-envelope-options
     (get-in artifact [:provenance :source-path])
     invocation))
   (let
    [report
     (sh07-proof-transaction-report
      :sh05
      :construction
      {:verifier-root
       ^{:line 99, :column 4}
       (let
        [entries
         ^{:line 99, :column 18}
         (.iterator
          ^{:line 99, :column 29}
          (.entrySet ^java.util.Map canonical-roots))]
        ^{:line 100, :column 6}
        (if
         ^{:line 100, :column 10}
         (and
          ^{:line 100, :column 15}
          (identical? gravity.macro/verify canonical-stage-root)
          ^{:line 101, :column 15}
          (identical? macro-stage-defaults canonical-operations)
          ^{:line 102, :column 15}
          (loop
           []
           ^{:line 103, :column 17}
           (if
            ^{:line 103, :column 21}
            (.hasNext entries)
            ^{:line 104, :column 19}
            (let
             [entry
              ^{:line 104, :column 31, :tag java.util.Map$Entry}
              (.next entries)]
             ^{:line 105, :column 21}
             (and
              ^{:line 105, :column 26}
              (nil?
               ^{:line 105, :column 32}
               (.getThreadBinding
                ^{:line 105, :column 51, :tag clojure.lang.Var}
                (.getKey entry)))
              ^{:line 106, :column 26}
              (identical?
               ^{:line 106, :column 38}
               (.getValue entry)
               ^{:line 106, :column 56}
               (.deref
                ^{:line 106, :column 64, :tag clojure.lang.Var}
                (.getKey entry)))
              ^{:line 107, :column 26}
              (recur)))
            true)))
         ^{:line 109, :column 8}
         (var-get #'sh05-macro-artifact-verification*)
         ^{:line 110, :column 8}
         (Object.))),
       :report-schema-version 1,
       :invocation invocation,
       :check-catalog-domain
       :gravity/sh05-construction-verification-checks,
       :source-content-hash sh05-macro-expected-source-content-hash,
       :plan-semantic-hash sh05-macro-expected-plan-semantic-hash,
       :functions-semantic-hash
       sh05-macro-expected-functions-semantic-hash}
      artifact
      (fn*
       []
       (sh05-macro-artifact-verification* artifact true invocation)))
     checks
     (:checks report)]
    (assoc
     checks
     :artifact
     :gravity/sh05-macro-capability-proof
     :status
     (if (= :passed (:status report)) :complete :failed)
     :failed-checks
     (:failed-checks report)))))
 (defn
  sh05-macro-capability-based-proof
  ([artifact] (sh05-macro-capability-based-proof artifact nil))
  ([artifact invocation]
   (let
    [report
     (sh05-macro-artifact-verification artifact invocation)
     checks
     (:checks report)]
    (assoc
     checks
     :artifact
     :gravity/sh05-macro-capability-proof
     :status
     (if (= :passed (:status report)) :complete :failed)
     :failed-checks
     (:failed-checks report))))))
