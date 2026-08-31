; Semantic decomposition of committed HEAD reader line 141626.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-js-ts-backend-source-artifact-output
 [state]
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
     plan-hash]}
   state
   javascript
   (js-ts-backend-source plan)
   writes-stdout?
   (pos? (get-in plan [:instruction-summary :println] 0))
   source-map
   (js-ts-backend-source-map-source javascript source-text)
   package-metadata
   (js-ts-backend-package-source writes-stdout?)
   js-hash
   (str "sha256:" (sha256-hex javascript))
   declaration-hash
   (str "sha256:" (sha256-hex js-ts-backend-declaration-source))
   source-map-hash
   (str "sha256:" (sha256-hex source-map))
   package-hash
   (str "sha256:" (sha256-hex package-metadata))
   source-hash
   (str "sha256:" (sha256-hex source-text))
   expected-output
   (:stdout runtime-record)
   expected-bytes
   (c-backend-runtime-bytes expected-output)]
  (clojure.core/assoc
   state
   :javascript
   javascript
   :writes-stdout?
   writes-stdout?
   :source-map
   source-map
   :package-metadata
   package-metadata
   :js-hash
   js-hash
   :declaration-hash
   declaration-hash
   :source-map-hash
   source-map-hash
   :package-hash
   package-hash
   :source-hash
   source-hash
   :expected-output
   expected-output
   :expected-bytes
   expected-bytes)))
