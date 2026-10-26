(ns gravity.self-hosting.sh07-proof-census
  "Reports authenticated SH-07 request identities and source-product
  cardinalities without invoking SH-07 checked-core lowering."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [gravity.bootstrap :as bootstrap]))

(def ^:private contract-resource "gravity/self_hosting/sh07_proof_contract.edn")

(defn- contract []
  (let [resource (io/resource contract-resource)]
    (when-not resource
      (throw (ex-info "SH-07 proof contract is absent"
                      {:id "SH07-PROOF-CONTRACT-ABSENT" :resource contract-resource})))
    (edn/read-string (slurp resource))))

(defn- current-authenticated-request [path source]
  (let [reader-path (bootstrap/sh03-reader-resolve-source-path)
        invocation {:reader-source-path reader-path
                    :authenticated-envelope-options {:source :development-current}}
        resolved (bootstrap/compiler-c5-resolution-source-artifact
                  path source
                  {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
                  reader-path invocation)]
    (bootstrap/sh07-core-authenticated-request resolved :gravity.bootstrap/legacy invocation)))

(defn census
  "Summarize a fresh authenticated request without lowering Core.
  The optional current Reader mode is development-only, not historical admission."
  ([requested-path] (census requested-path {}))
  ([requested-path options]
  (when-not (or (= {} options) (= {:reader :development-current} options))
    (throw (ex-info "Census options must be {} or {:reader :development-current}"
                    {:id "CORE-REQUEST-CENSUS-OPTIONS" :stage :read-source :severity :error})))
  (let [proof-contract (contract)
        selected (or (get-in proof-contract [:authoritative-modules (keyword requested-path)])
                     requested-path)
        current? (= {:reader :development-current} options)
        path (if current? (.getCanonicalPath (io/file selected)) selected)
        source (if current? (bootstrap/read-gravity-source-text path)
                   (slurp path :encoding "UTF-8"))
        started (System/nanoTime)
        request (if current? (current-authenticated-request path source)
                    (bootstrap/sh07-core-authenticated-request
                     (bootstrap/sh06-resolution-source-artifact path source)))
        origin-expectation (:macro-origin-expectation request)]
    {:artifact :gravity/sh07-proof-census
     :schema-version 2
     :status :authenticated-request
     :source-path path
     :request {:schema-version (:schema-version request)
               :scope (:scope request)
               :source-revision-id (get-in request [:lineage :source-revision-id])
               :sh06-semantic-projection-id
               (get-in request [:lineage :sh06-semantic-projection-id])}
     :measurements
     {:forms (count (:forms request))
      :top-level-forms (count (:top-level-form-ids request))
      :bindings (count (:binding-table request))
      :aliases (count (:alias-table request))
      :resolutions (count (:resolution-table request))
      :fragments (count (:fragment-manifest request))
      :module-effects (count (get-in request [:module :effects]))
      :module-capabilities (count (get-in request [:module :capabilities]))
      :module-exports (count (get-in request [:module :exports]))
      :macro-expansion-trace-entries (count (:macro-expansion-trace request))
      :macro-origin-trace-entries (count (:macro-origin-traces request))
      :macro-origin-expanded-definitions (:expanded-defn-count origin-expectation)
      :macro-origin-expected-inputs (count (:expected-input-syntax-ids origin-expectation))
      :macro-origin-expected-outputs (count (:expected-output-def-syntax-ids origin-expectation))
      :macro-origin-expected-introduced-functions
      (count (:expected-introduced-fn-syntax-ids origin-expectation))}
     :elapsed-ms (long (/ (- (System/nanoTime) started) 1000000))
     :performed-sh07-lowering? false})))

(defn -main [& arguments]
  (when-not (= 1 (count arguments))
    (throw (ex-info "Expected one authoritative module name or source path"
                    {:id "SH07-PROOF-CENSUS-USAGE" :arguments (vec arguments)
                     :available (vec (sort (map name (keys (:authoritative-modules (contract))))))})))
  (println (pr-str (census (first arguments)))))
