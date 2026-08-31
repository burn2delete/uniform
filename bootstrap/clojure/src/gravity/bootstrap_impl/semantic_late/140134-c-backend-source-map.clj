; Semantic decomposition of committed HEAD reader line 140134.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-c-backend-source-artifact-source-map
 [state]
 (clojure.core/let
  [{:keys
    [source-path
     source-text
     target
     dialect
     emit-dir
     compile?
     lowering-mode
     runtime-derived?
     executable-path
     c-source-path
     manifest-path
     source-map-path
     provenance-path
     shared-packet
     macro-artifact
     module
     stage2-rule
     stage2-compiler-artifact-record
     stage2-compiler-artifact-source-path
     stage2-runtime-rule
     stage2-driver-rule
     plan
     stage2-driver-run
     stage2-runtime-execution
     closed-plan-validation
     closed-plan-execution
     closed-plan-invocation
     closed-plan-target-record
     clojure-stage0-output
     stdout
     c-source
     source-hash
     plan-input
     plan-hash
     stage2-runtime-execution-record
     c-source-hash
     output-hash]}
   state
   source-map
   {:kind :gravity/c-backend-source-map,
    :schema-version "gravity.c.source-map/v1",
    :entries
    [{:generated-line 4,
      :generated-column 3,
      :source-path source-path,
      :source-span (source-span source-path 0),
      :origin-chain [{:kind :source-unit, :path source-path}]}]}
   source-map-hash
   (str
    "sha256:"
    (sha256-hex
     (pr-str
      (c-backend-canonical-value
       (update
        source-map
        :entries
        (fn
         [entries]
         (mapv
          (fn
           [entry]
           (->
            entry
            (dissoc :source-path)
            (update :source-span (fn* [p1__1200#] (when p1__1200# (dissoc p1__1200# :source))))
            (update
             :origin-chain
             (fn [chain] (mapv (fn* [p1__1201#] (dissoc p1__1201# :path)) chain)))))
          entries)))))))]
  (clojure.core/assoc state :source-map source-map :source-map-hash source-map-hash)))
