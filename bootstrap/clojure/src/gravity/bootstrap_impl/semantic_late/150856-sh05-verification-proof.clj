; Semantic decomposition of committed HEAD reader line 150856.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh05-macro-artifact-verification*-proof
 [state]
 (clojure.core/let
  [{:keys
    [artifact
     allow-missing-proof?
     source-path
     boundary
     c3-artifact
     c3-boundary
     gravity-verifiers
     output-envelope-ok?
     binding
     embedded-descriptor
     expected-descriptor
     descriptor-current?
     sh03-current?
     sh04-current?
     template-passed?
     resolved-passed?
     runs
     input-forms
     input-syntax
     input-module
     expected-expanded-stream
     expected-trace
     first-run
     first-run-shortcuts-current?
     run-count-current?
     run-storage-exact?
     plan-binding-current?
     pinned-version?
     grants-current?
     trace-current?
     output-current?
     generated-origins-current?
     input-lineage-current?
     c3-syntax-by-id
     expected-reader-binding
     expected-reader-revision
     expected-definition-span
     run-provenance-current?
     provenance-current?
     graph-valid?
     graph-current?
     exact-shape?
     artifact-id-current?]}
   state
   base-checks
   {:expanded-graph-current? graph-current?,
    :macro-version-current? pinned-version?,
    :actual-path-provenance-current? provenance-current?,
    :trace-replay-current? trace-current?,
    :exact-artifact-shape? exact-shape?,
    :macro-run-storage-exact? run-storage-exact?,
    :sh04-syntax-lineage-current? sh04-current?,
    :expanded-graph-valid? graph-valid?,
    :generated-origins-current? generated-origins-current?,
    :sh03-reader-lineage-current? sh03-current?,
    :macro-output-current? output-current?,
    :artifact-id-current? artifact-id-current?,
    :macro-plan-binding-current? plan-binding-current?,
    :build-grants-current? grants-current?,
    :first-run-compatibility-view-current? first-run-shortcuts-current?,
    :sh02-envelope-descriptor-current? descriptor-current?,
    :macro-run-provenance-current? run-provenance-current?,
    :macro-run-count-current? run-count-current?,
    :fresh-sh02-envelope-verified? output-envelope-ok?,
    :input-lineage-current? input-lineage-current?}
   base-failed-checks
   (vec
    (keep
     (fn
      [[check-name passed?]]
      (when-not passed? (keyword (str/replace (clojure.core/name check-name) #"\?$" ""))))
     base-checks))
   expected-embedded-proof
   (assoc
    base-checks
    :embedded-capability-proof-current?
    true
    :artifact
    :gravity/sh05-macro-capability-proof
    :status
    (if (empty? base-failed-checks) :complete :failed)
    :failed-checks
    base-failed-checks)
   embedded-proof-current?
   (or
    (and allow-missing-proof? (nil? (:capability-based-proof artifact)))
    (= expected-embedded-proof (:capability-based-proof artifact)))
   checks
   (assoc base-checks :embedded-capability-proof-current? embedded-proof-current?)
   failed-checks
   (vec
    (keep
     (fn
      [[check-name passed?]]
      (when-not passed? (keyword (str/replace (clojure.core/name check-name) #"\?$" ""))))
     checks))]
  (clojure.core/assoc
   state
   :base-checks
   base-checks
   :base-failed-checks
   base-failed-checks
   :expected-embedded-proof
   expected-embedded-proof
   :embedded-proof-current?
   embedded-proof-current?
   :checks
   checks
   :failed-checks
   failed-checks)))
