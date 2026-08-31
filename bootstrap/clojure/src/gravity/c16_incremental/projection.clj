(ns gravity.c16-incremental.projection
  (:require [clojure.string :as str]))

(defn source-overrides [module]
  (or (get-in module [:metadata :compiler :c16-incremental])
      (get-in module [:metadata :compiler :verification])
      {}))

(defn stage-cache-key [stage source-hash dependency-hash]
  {:artifact :gravity/cache-key
   :stage stage
   :source source-hash
   :reader "sha256:c16-reader-options"
   :syntax "sha256:c16-syntax-stream"
   :macro-expansion "sha256:c16-macro-expansion"
   :namespace "sha256:c16-namespace-analysis"
   :profile "sha256:c16-profile-manifest"
   :target "sha256:c16-target-request"
   :compiler "sha256:gravity-stage0-clojure"
   :pass-contract (str "sha256:c16-pass-" (name stage))
   :dependencies dependency-hash
   :build-effects "sha256:c16-replay-record"
   :capabilities "sha256:c16-capability-policy"
   :language-facets "sha256:c16-facets"
   :policy "sha256:c16-policy"})

(defn diagnostic-stream
  [{:keys [diagnostic-ids diagnostic-messages sha256-hex source-span]}
   source-path input-id]
  {:artifact :gravity/c16-incremental-diagnostic-stream
   :status :complete
   :stage :c16-incremental-compilation
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic id
            :cache-key (str "sha256:" (sha256-hex id))
            :artifact-id input-id
            :stage :c16-incremental-compilation
            :invalidating-input (keyword (str/lower-case
                                          (str/replace id #"C16-" "")))
            :source-span (source-span source-path 0)
            :profile :hosted
            :target :jvm
            :remediation (get diagnostic-messages id)})
         diagnostic-ids)})
