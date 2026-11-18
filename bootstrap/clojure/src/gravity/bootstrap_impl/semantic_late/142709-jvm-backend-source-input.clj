; Semantic decomposition of committed HEAD reader line 142709.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-jvm-backend-source-artifact-source-input
 [state]
 (clojure.core/let
  [{:keys
    [packet plan source-map plan-hash source-hash staged-paths]}
   state
   source-map-text
   (pr-str (c-backend-canonical-value source-map))
   _
   (jvm-backend-write-file! (.toPath (java.io.File. (:source-map staged-paths))) source-map-text)
   content-hashes
   (into
    {}
    (map
     (fn
      [[kind path]]
      [kind
       (str
        "sha256:"
        (sha256-bytes-hex (java.nio.file.Files/readAllBytes (.toPath (java.io.File. path)))))]))
    (select-keys
     staged-paths
     [:java-source :module-source :class-file :module-class :jar :source-map]))
   expected-input
   (jvm-backend-source-input-projection packet source-hash plan-hash)
   expected-effects
   (:effect-summary plan)
   expected-capabilities
   (get-in plan [:module :capabilities])]
  (clojure.core/assoc
   state
   :source-map-text
   source-map-text
   :content-hashes
   content-hashes
   :expected-input
   expected-input
   :expected-effects
   expected-effects
   :expected-capabilities
   expected-capabilities)))
