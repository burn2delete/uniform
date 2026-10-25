(ns gravity.self-hosting.sh07-b16-binding-bound-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_b16_binding_bound_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-07 B16 binding-bound test source is absent"
                {:id "SH07-B16-BINDING-BOUND-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH07-B16-BINDING-BOUND-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- compile-plan
  [relative-path]
  (let [source-path (path relative-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private checked-core-plan
  (delay
   (compile-plan
    "bootstrap/gravity/src/gravity/checked_core.gravity")))

(defn- invoke
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh07-b16-binding-bound-test
    :compiler-artifact-plan? true}
   @checked-core-plan function arguments))

(defn- ordinal-sha
  [ordinal]
  (str "sha256:" (format "%064x" (biginteger ordinal))))

(defn- source-forms
  [relative-path]
  (with-open [reader
              (clojure.lang.LineNumberingPushbackReader.
               (io/reader (path relative-path)))]
    (loop [forms []]
      (let [form (read {:eof ::eof} reader)]
        (if (= ::eof form)
          forms
          (recur (conj forms form)))))))

(defn- checked-core-binding-decomposition
  []
  (let [source-relative-path
        "bootstrap/gravity/src/gravity/checked_core.gravity"
        source-path (path source-relative-path)
        forms (source-forms source-relative-path)
        module (bootstrap/parse-module source-path forms)
        syntax-stream
        (mapv
         (fn [ordinal form]
           {:form form
            :syntax/id (ordinal-sha (inc ordinal))
            :span {:source source-path :form-index ordinal}})
         (range)
         forms)
        sh05-artifact
        {:artifact-id (ordinal-sha 3000)
         :expanded-syntax-stream syntax-stream}
        definitions
        (bootstrap/sh06-resolution-definition-records
         module sh05-artifact)
        core-bindings (bootstrap/sh06-resolution-core-records)
        analysis
        (bootstrap/sh06-resolution-analysis-inputs
         module sh05-artifact {})
        lexical-bindings
        (vec (mapcat :bindings (:lexical-scopes analysis)))
        binding-ids
        (vec
         (concat
          (map :definition-syntax-id definitions)
          (map :definition-syntax-id core-bindings)
          (map :binding-syntax-id lexical-bindings)))]
    {:module module
     :definitions definitions
     :core-bindings core-bindings
     :lexical-bindings lexical-bindings
     :binding-ids binding-ids}))

(defn- coordinator-request
  [bindings]
  (let [aliases []
        request
        {:lineage
         {:alias-table-id
          (bootstrap/reader-canonical-hash
           {:domain :gravity/sh07-sh06-alias-table-v1
            :aliases aliases})}
         :module {:namespace 'gravity.checked-core}
         :provenance {:actual-source-path "checked_core.gravity"}
         :forms []
         :binding-table bindings
         :alias-table aliases
         :resolution-table []
         :fragment-manifest []
         :macro-expansion-trace []
         :macro-origin-traces []
         :schema-version 15
         :scope :sh07-b15-keyword-map-lookup}]
    (assoc
     request
     :projection-binding
     (bootstrap/reader-canonical-hash
      (bootstrap/sh07-core-projection-binding-input request)))))

(defn- contained-coordinator-diagnostic
  [operation]
  (try
    (operation)
    nil
    (catch clojure.lang.ExceptionInfo exception
      (ex-data exception))))

(deftest sh07-b16-authentic-module-binding-decomposition-is-exact
  (let [{:keys [module definitions core-bindings lexical-bindings binding-ids]}
        (checked-core-binding-decomposition)]
    (is (= 'gravity.checked-core (:module module)))
    (is (= 308 (count definitions)))
    (is (= 262 (count core-bindings)))
    (is (= 1870 (count lexical-bindings)))
    (is (= 2440 (+ (count definitions)
                   (count core-bindings)
                   (count lexical-bindings))
           (count binding-ids)))
    (is (= (count binding-ids) (count (distinct binding-ids))))
    (is (every? #(re-matches #"sha256:[0-9a-f]{64}" %)
                binding-ids))))

(deftest sh07-source-local-bindings-follow-authenticated-definition-syntax
  (let [local-a (ordinal-sha 1)
        local-b (ordinal-sha 2)
        external (ordinal-sha 3)
        syntax-a (ordinal-sha 11)
        syntax-b (ordinal-sha 12)
        external-syntax (ordinal-sha 13)
        request
        {:module {:namespace 'gravity.bootstrap.reader}
         :forms [{:syntax-id syntax-a} {:syntax-id syntax-b}]
         :binding-table
         [{:binding-id local-a
           :namespace 'gravity.bootstrap.reader
           :definition-syntax-id syntax-a}
          {:binding-id local-b
           :namespace 'reader
           :definition-syntax-id syntax-b}
          {:binding-id external
           :namespace 'gravity.core
           :definition-syntax-id external-syntax}]}]
    (testing "qualified definitions remain source-local after name normalization"
      (is (= [local-a local-b]
             (invoke 'sh07-b13-local-binding-ids [request]))))
    (testing "binding-table order remains authoritative"
      (is (= [local-b local-a]
             (invoke
              'sh07-b13-local-binding-ids
              [(update request :binding-table
                       #(vec [(second %) (first %) (nth % 2)]))]))))))

(deftest sh07-b16-public-export-shapes-remain-stable
  (is (= :gravity/stage2-compiler-artifact-plan
         (:kind @checked-core-plan)))
  (doseq [[function parameters]
          {'sh07-build-core-template '[request]
           'sh07-verify-core-template
           '[request template digest-requests]
           'sh07-verify-core-resolved
           '[request resolved-core digest-requests resolved-digests]}]
    (let [compiled (get-in @checked-core-plan [:functions function])]
      (is (map? compiled) function)
      (is (= parameters (:params compiled)) function))))

(deftest binding-vector-shape-has-no-count-admission-quota
  (doseq [size [2440 2441 65537]]
    (is (= {:status :accepted}
           (invoke 'sh07-binding-vector-preflight [(vec (repeat size nil))]))))
  (is (= {:status :rejected :reason :binding-vector-required}
         (invoke 'sh07-binding-vector-preflight [:not-a-vector]))))

(deftest binding-id-validation-retains-digest-shape-without-count-quota
  (let [ids (mapv ordinal-sha (range 2049))]
    (is (true? (invoke 'sh07-binding-id-vector? [ids])))
    (is (false? (invoke 'sh07-binding-id-vector? [(conj ids :not-a-sha)])))))

(deftest request-preflight-retains-shapes-and-authenticated-projection
  (let [request (coordinator-request (vec (repeat 2441 nil)))]
    (is (= :passed (bootstrap/sh07-core-request-preflight! request)))
    (is (= :request-binding-table-vector-required
           (get-in (contained-coordinator-diagnostic
                    #(bootstrap/sh07-core-request-preflight! (assoc request :binding-table :malformed)))
                   [:facts :rule-specific :reason])))
    (is (= :request-projection-binding-mismatch
           (get-in (contained-coordinator-diagnostic
                    #(bootstrap/sh07-core-request-preflight! (assoc request :projection-binding "changed")))
                   [:facts :rule-specific :reason])))))

(defn- form-index [forms]
  {:forms {:by-id (into {} (map (juxt :form-id identity)) forms)}})

(deftest form-forest-retains-cycle-coverage-and-parent-invariants
  (let [forms (mapv (fn [id]
                      {:form-id id :parent-form-id (when (pos? id) (dec id))
                       :child-form-ids (if (= id 599) [] [(inc id)])})
                    (range 600))
        request {:forms forms :top-level-form-ids [0]}
        indexes (form-index forms)]
    (is (true? (invoke 'sh07-form-forest-valid? [request indexes])))
    (is (= 0 (invoke 'sh07-b13-form-root-id [indexes (peek forms)])))
    (is (false? (invoke 'sh07-form-forest-valid?
                         [(assoc request :top-level-form-ids [0 0]) indexes])))
    (is (false? (invoke 'sh07-form-forest-valid?
                         [(assoc request :top-level-form-ids [600]) indexes])))
    (is (false? (invoke 'sh07-form-forest-valid?
                         [(assoc request :top-level-form-ids [1]) indexes])))
    (let [cycle [{:form-id 0 :parent-form-id 0 :child-form-ids [0]}]
          cycle-index (form-index cycle)]
      (is (false? (invoke 'sh07-form-forest-valid?
                           [{:forms cycle :top-level-form-ids [0]} cycle-index])))
      (is (nil? (invoke 'sh07-b13-form-root-id [cycle-index (first cycle)]))))))

(deftest core-native-boundary-validates-before-metadata-stripping-and-execution
  (let [source "<core-native-boundary>"
        calls (atom 0)
        lazy-value (lazy-seq (swap! calls inc) (throw (AssertionError. "forced")))
        array (object-array [:self nil])
        cycle (clojure.lang.PersistentArrayMap. array)]
    (aset array 1 cycle)
    (doseq [[value expected] [[(with-meta [] {:origin :hidden}) :metadata-free-input-carrier]
                              [lazy-value :input-carrier-value-domain]
                              [cycle :cyclic-input-carrier]
                              [(str (char 0xd800)) :well-formed-unicode-scalar-string]]]
      (let [failure (contained-coordinator-diagnostic
                     #(bootstrap/sh07-core-execute! source 'sh07-binding-vector-preflight [value]))]
        (is (= "C6-VERIFY" (:id failure)))
        (is (= expected (:missing-fact failure)))))
    (is (zero? @calls))
    (is (= {:status :accepted}
           (bootstrap/sh07-core-execute! source 'sh07-binding-vector-preflight
                                         [(vec (repeat 65537 nil))])))))

(deftest decimal-descriptor-retains-exact-evidence-without-size-cap
  (doseq [value [(java.math.BigDecimal. java.math.BigInteger/ONE 65537)
                 (java.math.BigDecimal. (.shiftLeft java.math.BigInteger/ONE 300) 0)]]
    (let [evidence {:kind :decimal :decoded value :literal-id (ordinal-sha 1)
                    :form-id (ordinal-sha 2) :token-id (ordinal-sha 3)}
          result (bootstrap/sh07-core-decimal-descriptor "decimal.gravity" value evidence (ordinal-sha 4))]
      (is (= (.scale value) (:scale result)))
      (is (= (.toString (.unscaledValue value)) (:unscaled-value result)))
      (is (= :exact-decimal-c2-evidence
             (:reason (contained-coordinator-diagnostic
                       #(bootstrap/sh07-core-decimal-descriptor
                         "decimal.gravity" value (assoc evidence :decoded 0M) (ordinal-sha 4)))))))))
