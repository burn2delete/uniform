(ns gravity.self-hosting.sh07-proof-census
  "Builds the authenticated SH-07 request and reports every relevant resource
  dimension without invoking SH-07 checked-core lowering."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [gravity.bootstrap :as bootstrap]))

(def ^:private contract-resource
  "gravity/self_hosting/sh07_proof_contract.edn")

(defn- contract
  []
  (let [resource (io/resource contract-resource)]
    (when-not resource
      (throw
       (ex-info "SH-07 proof contract is absent"
                {:id "SH07-PROOF-CONTRACT-ABSENT"
                 :resource contract-resource})))
    (edn/read-string (slurp resource))))

(defn- source-path
  [argument proof-contract]
  (or (get-in proof-contract
              [:authoritative-modules (keyword argument)])
      argument))

(defn- aggregate?
  [value]
  (or (map? value)
      (vector? value)
      (set? value)
      (list? value)
      (and (coll? value) (not (string? value)))))

(defn- aggregate-children
  [value]
  (cond
    (map? value) (mapcat (fn [[key child]] [key child]) value)
    (set? value) (sort-by pr-str value)
    (coll? value) value
    :else []))

(defn- exact-scalar-byte-count
  [value]
  (alength
   (.getBytes
    (cond
      (string? value) value
      (char? value) (str value)
      :else (pr-str value))
    java.nio.charset.StandardCharsets/UTF_8)))

(defn- executable-scalar-byte-count
  [value]
  ;; Mirror gravity.checked-core/sh07-utf8-byte-count exactly. The stage2
  ;; subset deliberately charges the conservative four-byte upper bound for
  ;; every host character unit.
  (* 4 (count (vec (str value)))))

(defn carrier-census
  [value]
  (let [frontier (java.util.ArrayDeque.)]
    (.push frontier [value 0])
    (loop [nodes 0
           aggregates 0
           executable-scalar-bytes 0
           exact-utf8-scalar-bytes 0
           maximum-depth 0
           maximum-width 0]
      (if (.isEmpty frontier)
        {:nodes nodes
         :aggregate-nodes aggregates
         :executable-scalar-bytes executable-scalar-bytes
         :exact-utf8-scalar-bytes exact-utf8-scalar-bytes
         :maximum-depth maximum-depth
         :maximum-width maximum-width}
        (let [[current depth] (.pop frontier)]
          (if (aggregate? current)
            (let [children (vec (aggregate-children current))]
              (doseq [child children]
                (.push frontier [child (inc depth)]))
              (recur
               (inc nodes)
               (inc aggregates)
               executable-scalar-bytes
               exact-utf8-scalar-bytes
               (max maximum-depth depth)
               (max maximum-width (count children))))
            (recur
             (inc nodes)
             aggregates
             (+ executable-scalar-bytes
                (executable-scalar-byte-count current))
             (+ exact-utf8-scalar-bytes
                (exact-scalar-byte-count current))
             (max maximum-depth depth)
             maximum-width)))))))

(defn- maximum-count
  [fragments key-name]
  (reduce max 0 (map #(count (get % key-name)) fragments)))

(defn- maximum-form-depth
  [forms root-form-ids]
  (let [form-by-id (into {} (map (juxt :form-id identity)) forms)]
    (loop [frontier (mapv #(vector % 1) root-form-ids)
           visited #{}
           maximum-depth 0]
      (if (empty? frontier)
        (if (= (count visited) (count forms))
          maximum-depth
          Long/MAX_VALUE)
        (let [[form-id depth] (peek frontier)
              form (get form-by-id form-id)]
          (if (or (nil? form) (contains? visited form-id))
            Long/MAX_VALUE
            (recur
             (into
              (pop frontier)
              (map #(vector % (inc depth))
                   (:child-form-ids form)))
             (conj visited form-id)
             (max maximum-depth depth))))))))

(defn- violations
  [measurements bounds]
  (vec
   (keep
    (fn [[measurement bound-key]]
      (let [observed (get measurements measurement)
            maximum (get bounds bound-key)]
        (when (and (number? observed)
                   (number? maximum)
                   (> observed maximum))
          {:measurement measurement
           :observed observed
           :bound bound-key
           :maximum maximum})))
    {:forms :maximum-module-forms
     :predicted-maximum-core-nodes :maximum-module-core-nodes
     :fragments :maximum-fragments
     :top-level-forms :maximum-top-level-forms
     :resolutions :maximum-module-resolutions
     :bindings :maximum-bindings
     :aliases :maximum-alias-records
     :maximum-fragment-forms :maximum-fragment-forms
     :maximum-fragment-root-forms :maximum-top-level-forms
     :maximum-fragment-local-bindings :maximum-bindings
     :maximum-fragment-external-bindings :maximum-bindings
     :maximum-fragment-aliases :maximum-alias-records
     :maximum-fragment-resolutions :maximum-fragment-resolutions
     :maximum-form-children :maximum-form-children
     :maximum-form-depth :maximum-form-depth
     :maximum-origin-chain-entries :maximum-origin-entries
     :maximum-generated-origin-entries :maximum-origin-entries
     :maximum-metadata-entries :maximum-metadata-entries
     :module-effects :maximum-effects
     :module-capabilities :maximum-capabilities
     :module-exports :maximum-exports
     :maximum-binding-effects :maximum-effects
     :maximum-binding-capabilities :maximum-capabilities
     :maximum-binding-profile-set :maximum-effects
     :maximum-binding-target-set :maximum-effects
     :maximum-binding-semantic-span-entries
     :maximum-metadata-entries
     :maximum-alias-targets :maximum-capabilities
     :macro-expansion-trace-entries :maximum-module-forms
     :macro-origin-trace-entries :maximum-top-level-forms
     :macro-origin-expanded-definitions :maximum-top-level-forms
     :macro-origin-expected-inputs :maximum-top-level-forms
     :macro-origin-expected-outputs :maximum-top-level-forms
     :macro-origin-expected-introduced-functions
     :maximum-top-level-forms
     :predicted-maximum-digest-requests
     :maximum-module-digest-requests
     :carrier-nodes :maximum-module-carrier-nodes
     :carrier-depth :maximum-module-carrier-depth
     :carrier-width :maximum-module-carrier-width
     :carrier-scalar-bytes :maximum-module-scalar-bytes})))

(defn census
  [requested-path]
  (let [proof-contract (contract)
        path (source-path requested-path proof-contract)
        source (slurp path)
        started (System/nanoTime)
        sh06 (bootstrap/sh06-resolution-source-artifact path source)
        request (bootstrap/sh07-core-authenticated-request sh06)
        fragments (:fragment-manifest request)
        forms (:forms request)
        bindings (:binding-table request)
        module (:module request)
        origin-expectation (:macro-origin-expectation request)
        carrier (carrier-census request)
        measurements
        {:forms (count forms)
         :top-level-forms (count (:top-level-form-ids request))
         :bindings (count bindings)
         :aliases (count (:alias-table request))
         :resolutions (count (:resolution-table request))
         :fragments (count fragments)
         :maximum-fragment-forms
         (maximum-count fragments :form-ids)
         :maximum-fragment-root-forms
         (maximum-count fragments :root-form-ids)
         :maximum-fragment-local-bindings
         (maximum-count fragments :local-binding-ids)
         :maximum-fragment-external-bindings
         (maximum-count fragments :external-binding-ids)
         :maximum-fragment-aliases
         (maximum-count fragments :alias-names)
         :maximum-fragment-resolutions
         (maximum-count fragments
                        :resolution-reference-syntax-ids)
         :maximum-form-children
         (maximum-count forms :child-form-ids)
         :maximum-form-depth
         (maximum-form-depth forms (:top-level-form-ids request))
         :maximum-origin-chain-entries
         (maximum-count forms :origin-chain)
         :maximum-generated-origin-entries
         (maximum-count forms :generated-origin)
         :maximum-metadata-entries
         (maximum-count forms :metadata)
         :module-effects (count (:effects module))
         :module-capabilities (count (:capabilities module))
         :module-exports (count (:exports module))
         :maximum-binding-effects
         (maximum-count bindings :effects)
         :maximum-binding-capabilities
         (maximum-count bindings :capabilities)
         :maximum-binding-profile-set
         (maximum-count bindings :profile-set)
         :maximum-binding-target-set
         (maximum-count bindings :target-set)
         :maximum-binding-semantic-span-entries
         (maximum-count bindings :semantic-span)
         :maximum-alias-targets
         (maximum-count (:alias-table request) :targets)
         :macro-expansion-trace-entries
         (count (:macro-expansion-trace request))
         :macro-origin-trace-entries
         (count (:macro-origin-traces request))
         :macro-origin-expanded-definitions
         (:expanded-defn-count origin-expectation)
         :macro-origin-expected-inputs
         (count (:expected-input-syntax-ids origin-expectation))
         :macro-origin-expected-outputs
         (count (:expected-output-def-syntax-ids origin-expectation))
         :macro-origin-expected-introduced-functions
         (count (:expected-introduced-fn-syntax-ids
                 origin-expectation))
         :predicted-maximum-core-nodes (count forms)
         :predicted-maximum-digest-requests
         (+ (count forms) 4)
         :carrier-nodes (:nodes carrier)
         :carrier-depth (:maximum-depth carrier)
         :carrier-width (:maximum-width carrier)
         :carrier-scalar-bytes
         (:executable-scalar-bytes carrier)
         :carrier-exact-utf8-scalar-bytes
         (:exact-utf8-scalar-bytes carrier)}
        elapsed-ms
        (long (/ (- (System/nanoTime) started) 1000000))]
    {:artifact :gravity/sh07-proof-census
     :schema-version 1
     :status (if (empty? (violations measurements
                                     (:bounds proof-contract)))
               :within-declared-bounds
               :over-declared-bounds)
     :source-path path
     :request
     {:schema-version (:schema-version request)
      :scope (:scope request)
      :source-revision-id
      (get-in request [:lineage :source-revision-id])
      :sh06-semantic-projection-id
      (get-in request [:lineage :sh06-semantic-projection-id])}
     :measurements measurements
     :declared-bounds (:bounds proof-contract)
     :violations (violations measurements (:bounds proof-contract))
     :elapsed-ms elapsed-ms
     :performed-sh07-lowering? false}))

(defn -main
  [& arguments]
  (when-not (= 1 (count arguments))
    (let [available (vec (sort (map name
                                    (keys (:authoritative-modules
                                           (contract))))))]
      (throw
       (ex-info
        "Expected one authoritative module name or source path"
        {:id "SH07-PROOF-CENSUS-USAGE"
         :arguments (vec arguments)
         :available available}))))
  (let [result (census (first arguments))]
    (println (pr-str result))
    (when-not (= :within-declared-bounds (:status result))
      (System/exit 1))))
