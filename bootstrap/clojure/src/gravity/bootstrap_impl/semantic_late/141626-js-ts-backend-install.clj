; Semantic decomposition of committed HEAD reader line 141626.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/let
 [semantic-late-js-ts-backend-source-artifact-packet
  semantic-late-js-ts-backend-source-artifact-packet
  semantic-late-js-ts-backend-source-artifact-output
  semantic-late-js-ts-backend-source-artifact-output
  semantic-late-js-ts-backend-source-artifact-node-parity
  semantic-late-js-ts-backend-source-artifact-node-parity
  semantic-late-js-ts-backend-source-artifact-manifest
  semantic-late-js-ts-backend-source-artifact-manifest
  semantic-late-js-ts-backend-source-artifact-provenance
  semantic-late-js-ts-backend-source-artifact-provenance
  semantic-late-js-ts-backend-source-artifact-artifact
  semantic-late-js-ts-backend-source-artifact-artifact]
 (defn
  js-ts-backend-source-artifact
  "Emit and execute the bounded Node 20 ES2022 ESM target from the genuine\n  stage2 compiler-driver packet.  All validation and differential execution\n  completes in a temporary directory before caller-visible files are moved\n  into place."
  ([source-path source-text] (js-ts-backend-source-artifact source-path source-text {}))
  ([source-path
    source-text
    {:keys [target output-path emit?], :or {target js-ts-backend-target, emit? false}}]
   (let
    [target (js-ts-backend-canonical-target target)]
    (when-not
     (= js-ts-backend-target target)
     (js-ts-backend-fail!
      "B6-TARGET"
      "JS/TS backend target is unsupported"
      source-path
      nil
      {:requested-target target,
       :supported-targets (vec (sort js-ts-backend-target-aliases)),
       :missing-fact :supported-target}))
    (when
     (and emit? (str/blank? (str output-path)))
     (js-ts-backend-fail!
      "C14-INPUT"
      "JS/TS target compilation requires an output path"
      source-path
      nil
      {:missing-fields [:output-path]}))
    (clojure.core/let
     [state-0
      {:source-path source-path,
       :source-text source-text,
       :target target,
       :output-path output-path,
       :emit? emit?}
      state-1
      (semantic-late-js-ts-backend-source-artifact-packet state-0)
      state-2
      (semantic-late-js-ts-backend-source-artifact-output state-1)
      state-3
      (semantic-late-js-ts-backend-source-artifact-node-parity state-2)
      state-4
      (semantic-late-js-ts-backend-source-artifact-manifest state-3)
      state-5
      (semantic-late-js-ts-backend-source-artifact-provenance state-4)
      state-6
      (semantic-late-js-ts-backend-source-artifact-artifact state-5)]
     (clojure.core/let
      [{:keys
        [source-path
         source-text
         target
         output-path
         emit?
         node-version
         packet
         compiler-artifact-record
         compiler-artifact-source-path
         driver-record
         runtime-record
         runtime-rule
         driver-rule
         closed-plan-runtime
         closed-runtime-context
         plan
         plan-hash
         javascript
         writes-stdout?
         source-map
         package-metadata
         js-hash
         declaration-hash
         source-map-hash
         package-hash
         source-hash
         expected-output
         expected-bytes
         temp-directory
         temp-module
         execution
         manifest-input
         manifest-hash
         manifest
         provenance-input
         provenance-hash
         paths
         provenance
         identity-input
         artifact]}
       state-6]
      (when
       emit?
       (js-ts-backend-stage-files!
        (str output-path)
        paths
        {:javascript javascript,
         :typescript-declarations js-ts-backend-declaration-source,
         :source-map source-map,
         :package-metadata package-metadata,
         :manifest (pr-str manifest),
         :provenance (pr-str provenance)}
        source-path))
      artifact))))))
