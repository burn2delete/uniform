;; Stage and seed dependencies qualify reuse without calling replaceable code.
;; The legitimate scoped binding remains an explicit epoch input.
(let
 [canonical-operations
  resolution-stage-defaults
  canonical-binding-root
  ^{:line 42, :column 34}
  (.getRawRoot #'*sh06-resolution-operation-binding*)
  canonical-roots
  ^{:line 44, :column 11}
  (into
   {}
   ^{:line 44, :column 20}
   (map
    ^{:line 44, :column 25}
    (fn
     [v]
     [v ^{:line 44, :column 36} (.getRawRoot ^clojure.lang.Var v)]))
   ^{:line 45, :column 17}
   (concat
    ^{:line 45, :column 25}
    (vals ^{:line 45, :column 31} (ns-publics 'gravity.resolution))
    ^{:line 46, :column 25}
    (filter var? ^{:line 46, :column 38} (vals canonical-operations))
    [#'resolution-stage-operations
     #'resolution-stage-defaults
     #'resolution-stage-input
     #'sh06-resolution-cached-binding
     #'sh06-resolution-expected-source-byte-count
     #'sh06-resolution-expected-source-content-hash
     #'sh06-resolution-public-function-shapes
     #'sh06-resolution-source-relative-path
     #'known-source-profiles
     #'sh06-resolution-all-core-symbols
     #'sh06-resolution-special-symbols
     #'sh06-resolution-type-symbols
     #'sh06-resolution-boundary-keys
     #'sh06-resolution-envelope-stage
     #'sh06-resolution-sealed-artifact-kind
     #'c5-resolution-diagnostic-ids
     #'c5-resolution-governing-document
     #'sh06-resolution-adapter-contract
     #'sh06-resolution-artifact-keys
     #'sh06-resolution-execution-boundary-contract
     #'sh06-resolution-pass-contract
     #'gravity.resolution/verify*
     #'gravity.resolution/verify-contained*
     #'gravity.resolution/verify-current*]))]
 ^{:line 158, :column 1}
 (defn
  sh06-resolution-artifact-verification
  ^{:line 159, :column 3}
  ([artifact]
   ^{:line 160, :column 4}
   (let
    [source-path
     ^{:line 160, :column 22}
     (or
      ^{:line 160, :column 26}
      (get-in artifact [:provenance :source-path])
      "<sh06-verification>")
     plan-binding
     ^{:line 161, :column 23}
     (sh06-resolution-current-binding! source-path)]
    ^{:line 162, :column 5}
    (binding
     [*sh06-resolution-operation-binding* plan-binding]
     ^{:line 163, :column 6}
     (let
      [report
       ^{:line 164, :column 4}
       (sh07-proof-transaction-report
        :sh06
        :final
        {:verifier-root
         ^{:line 17, :column 4}
         (let
          [entries
           ^{:line 17, :column 18}
           (.iterator
            ^{:line 17, :column 29}
            (.entrySet ^java.util.Map canonical-roots))]
          ^{:line 18, :column 6}
          (if
           ^{:line 18, :column 10}
           (and
            ^{:line 18, :column 15}
            (identical?
             ^{:line 18, :column 27}
             (.getRawRoot #'resolution-stage-defaults)
             canonical-operations)
            ^{:line 19, :column 15}
            (identical?
             ^{:line 19, :column 27}
             (.getRawRoot #'*sh06-resolution-operation-binding*)
             canonical-binding-root)
            ^{:line 20, :column 15}
            (loop
             []
             ^{:line 21, :column 17}
             (if
              ^{:line 21, :column 21}
              (.hasNext entries)
              ^{:line 22, :column 19}
              (let
               [entry
                ^{:line 22, :column 31}
                (.next entries)
                v
                ^{:line 22, :column 49}
                (.getKey ^java.util.Map$Entry entry)]
               ^{:line 23, :column 21}
               (if
                ^{:line 23, :column 25}
                (and
                 ^{:line 23, :column 30}
                 (identical?
                  ^{:line 23, :column 42}
                  (.getValue ^java.util.Map$Entry entry)
                  ^{:line 24, :column 42}
                  (.getRawRoot ^clojure.lang.Var v))
                 ^{:line 25, :column 30}
                 (nil?
                  ^{:line 25, :column 36}
                  (.getThreadBinding ^clojure.lang.Var v)))
                ^{:line 26, :column 23}
                (recur)
                false))
              true)))
           ^{:line 27, :column 8}
           (.get #'sh06-resolution-artifact-verification-contained)
           ^{:line 28, :column 8}
           (Object.))),
         :report-schema-version 1,
         :check-catalog-domain :gravity/sh06-final-verification-checks,
         :source-content-hash
         ^{:line 170, :column 27}
         (:source-content-hash plan-binding),
         :plan-semantic-hash
         ^{:line 171, :column 26}
         (:plan-semantic-hash plan-binding),
         :functions-semantic-hash
         ^{:line 173, :column 6}
         (:functions-semantic-hash plan-binding),
         :reader-invocation
         ^{:line 174, :column 25}
         (sh06-resolution-reader-invocation),
         :resolution-operation-binding plan-binding}
        artifact
        (fn*
         []
         ^{:line 176, :column 6}
         (sh06-resolution-artifact-verification-contained
          artifact
          false)))]
      ^{:line 177, :column 7}
      (sh06-resolution-binding-current! source-path plan-binding)
      report))))
  ^{:line 179, :column 3}
  ([artifact context]
   ^{:line 180, :column 4}
   (let
    [validation
     ^{:line 180, :column 21}
     (sh06-resolution-context-validation artifact context)]
    ^{:line 181, :column 6}
    (if-not
     ^{:line 181, :column 14}
     (= :passed ^{:line 181, :column 25} (:status validation))
     ^{:line 182, :column 8}
     (assoc
      ^{:line 182, :column 15}
      (sh06-resolution-contained-verification-failure
       ^{:line 183, :column 16}
       (ex-info
        "SH-06 context validation failed"
        {:id "C5-UNRESOLVED"}))
      :context-validation
      validation)
     ^{:line 186, :column 8}
     (sh07-proof-transaction-report
      :sh06
      :final
      {:source-content-hash
       sh06-resolution-expected-source-content-hash,
       :resolution-operation-binding
       *sh06-resolution-operation-binding*,
       :external-binding-authority-id
       ^{:line 200, :column 10}
       (:external-binding-authority-id validation),
       :resolution-plan-authority-id
       ^{:line 198, :column 10}
       (:resolution-plan-authority-id validation),
       :reader-invocation
       ^{:line 201, :column 29}
       (sh06-resolution-reader-invocation),
       :functions-semantic-hash
       sh06-resolution-expected-functions-semantic-hash,
       :report-schema-version 1,
       :verifier-root
       ^{:line 17, :column 4}
       (let
        [entries
         ^{:line 17, :column 18}
         (.iterator
          ^{:line 17, :column 29}
          (.entrySet ^java.util.Map canonical-roots))]
        ^{:line 18, :column 6}
        (if
         ^{:line 18, :column 10}
         (and
          ^{:line 18, :column 15}
          (identical?
           ^{:line 18, :column 27}
           (.getRawRoot #'resolution-stage-defaults)
           canonical-operations)
          ^{:line 19, :column 15}
          (identical?
           ^{:line 19, :column 27}
           (.getRawRoot #'*sh06-resolution-operation-binding*)
           canonical-binding-root)
          ^{:line 20, :column 15}
          (loop
           []
           ^{:line 21, :column 17}
           (if
            ^{:line 21, :column 21}
            (.hasNext entries)
            ^{:line 22, :column 19}
            (let
             [entry
              ^{:line 22, :column 31}
              (.next entries)
              v
              ^{:line 22, :column 49}
              (.getKey ^java.util.Map$Entry entry)]
             ^{:line 23, :column 21}
             (if
              ^{:line 23, :column 25}
              (and
               ^{:line 23, :column 30}
               (identical?
                ^{:line 23, :column 42}
                (.getValue ^java.util.Map$Entry entry)
                ^{:line 24, :column 42}
                (.getRawRoot ^clojure.lang.Var v))
               ^{:line 25, :column 30}
               (nil?
                ^{:line 25, :column 36}
                (.getThreadBinding ^clojure.lang.Var v)))
              ^{:line 26, :column 23}
              (recur)
              false))
            true)))
         ^{:line 27, :column 8}
         (.get #'sh06-resolution-artifact-verification-contained)
         ^{:line 28, :column 8}
         (Object.))),
       :sh06-context-root
       ^{:line 196, :column 29}
       (:context-root validation),
       :check-catalog-domain :gravity/sh06-final-verification-checks,
       :plan-semantic-hash sh06-resolution-expected-plan-semantic-hash}
      artifact
      (fn*
       []
       ^{:line 203, :column 10}
       (sh06-resolution-artifact-verification-contained
        artifact
        false
        context))))))
  ^{:line 205, :column 3}
  ([artifact context invocation]
   ^{:line 206, :column 4}
   (if
    ^{:line 206, :column 8}
    (nil? context)
    ^{:line 207, :column 6}
    (sh06-resolution-artifact-verification artifact context)
    ^{:line 208, :column 6}
    (sh06-resolution-with-reader-invocation
     ^{:line 209, :column 7}
     (or
      ^{:line 209, :column 11}
      (get-in artifact [:provenance :source-path])
      "<sh06-verification>")
     invocation
     (fn*
      []
      ^{:line 210, :column 8}
      (if
       ^{:line 210, :column 12}
       (= :gravity.bootstrap/legacy context)
       ^{:line 211, :column 10}
       (sh06-resolution-artifact-verification artifact)
       ^{:line 212, :column 10}
       (sh06-resolution-artifact-verification artifact context))))))))

