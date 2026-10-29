(require '[gravity.c6-c10.source-contract :as c6-source-contract]
         '[gravity.source-interface-header :as source-interface-header])
(declare p15-s23-trusted-carrier-validation)

(defn- p15-s23-stage2-expression-source-contract [version]
  (when-not (and (identical? Long (class version)) (contains? #{1 2} version))
    (p15-s23-stage2-plan-emitter-fail!
     "P15S23Q002" "<expression-source-contract>" nil
     {:missing-fact :expression-lowering-source-contract-version}))
  (case version
    1 {:version 1
       :source-content-hash p15-s23-stage2-compiler-artifact-expected-source-content-hash
       :semantic-hash p15-s23-stage2-compiler-artifact-expected-semantic-hash
       :required-functions p15-s23-stage2-compiler-artifact-required-functions}
    2 c6-source-contract/current-expression-program))

(defn p15-s23-stage2-compiler-artifact-expected-artifact-hash
  ([] (p15-s23-stage2-compiler-artifact-expected-artifact-hash 1))
  ([version]
  (let [contract (p15-s23-stage2-expression-source-contract version)]
  (str "sha256:"
       (sha256-hex
        (pr-str
         (c-backend-canonical-value
          {:source-content-hash
           (:source-content-hash contract)
           :semantic-hash
           (:semantic-hash contract)})))))))

(def p15-s23-stage2-compiler-artifact-record-keys
  #{:artifact :source-content-hash :semantic-hash :artifact-hash :functions
    :invoked? :generic-bridge-residual? :plan-assembly-function
    :plan-assembly-artifact-hash :plan-assembly-source-content-hash
    :plan-assembly-semantic-hash :plan-assembly-invoked?
    :plan-assembly-generic-bridge-residual? :clojure-seed-boundary?
    :self-hosted?})

(defn- p15-s23-stage2-native-keyword-pairs [value]
  (try
    (let [pairs (source-interface-header/pairs! value nil)]
      (loop [pending (seq pairs) seen #{}]
        (if-let [[key _] (first pending)]
          (when (and (source-interface-header/native-keyword? key) (not (contains? seen key)))
            (recur (next pending) (conj seen key)))
          pairs)))
    (catch clojure.lang.ExceptionInfo error
      (if (= :versioned-c11-interface (:missing-fact (ex-data error))) nil (throw error)))))

(defn p15-s23-stage2-compiler-artifact-record-authentic?
  ([record] (p15-s23-stage2-compiler-artifact-record-authentic? record 1))
  ([record version]
  (let [contract (p15-s23-stage2-expression-source-contract version)]
  (and (or (= 1 version)
           (and (some? (p15-s23-stage2-native-keyword-pairs record))
                (= :passed (:status (p15-s23-trusted-carrier-validation record :default-only)))))
       (map? record)
       (= (cond-> p15-s23-stage2-compiler-artifact-record-keys
            (= 2 version) (conj :source-contract-version :functions-semantic-hash :function-count))
          (set (keys record)))
       (or (= 1 version)
           (and (identical? Long (class (:source-contract-version record)))
                (== 2 (.longValue ^Long (:source-contract-version record)))
                (= (:functions-semantic-hash contract) (:functions-semantic-hash record))
                (= (:function-count contract) (:function-count record))))
       (= :gravity/p15-s23-stage2-expression-lowering-binding
          (:artifact record))
       (= (:required-functions contract)
          (:functions record))
       (= (:source-content-hash contract)
          (:source-content-hash record))
       (= (:semantic-hash contract)
          (:semantic-hash record))
       (= (p15-s23-stage2-compiler-artifact-expected-artifact-hash version)
          (:artifact-hash record))
       (true? (:invoked? record))
       (true? (:generic-bridge-residual? record))
       (= p15-s23-stage2-compiler-artifact-plan-assembly-function
          (:plan-assembly-function record))
       (= (:artifact-hash record) (:plan-assembly-artifact-hash record))
       (= (:source-content-hash record)
          (:plan-assembly-source-content-hash record))
       (= (:semantic-hash record) (:plan-assembly-semantic-hash record))
       (true? (:plan-assembly-invoked? record))
       (true? (:plan-assembly-generic-bridge-residual? record))
       (true? (:clojure-seed-boundary? record))
       (false? (:self-hosted? record))))))

(defn- p15-s23-stage2-plan-compiler-view [plan]
  ;; This predicate compares compiler metadata, not executable plan admission.
  ;; The native root is inspected before lookup; unconsumed function bodies
  ;; and other payloads remain inert and acquire no carrier/safety approval.
  (when-let [pairs (p15-s23-stage2-native-keyword-pairs plan)]
    (let [compiler (some (fn [[key value]] (when (identical? :compiler key) value)) pairs)]
      (when (and (some? (p15-s23-stage2-native-keyword-pairs compiler))
                 (= :passed (:status (p15-s23-trusted-carrier-validation compiler :default-only))))
        compiler))))

(defn p15-s23-stage2-compiler-artifact-record-matches-plan?
  ([record plan] (p15-s23-stage2-compiler-artifact-record-matches-plan? record plan 1))
  ([record plan version]
  (p15-s23-stage2-expression-source-contract version)
  (and (or (= 1 version)
           (p15-s23-stage2-compiler-artifact-record-authentic? record version))
  (let [compiler (if (= 1 version) (:compiler plan) (p15-s23-stage2-plan-compiler-view plan))]
    (and (or (= 1 version)
             (and (p15-s23-stage2-compiler-artifact-record-authentic? record version)
                  (identical? Long (class (:expression-lowering-source-contract-version compiler)))
                  (== 2 (.longValue ^Long (:expression-lowering-source-contract-version compiler)))
                  (= (:functions-semantic-hash record) (:expression-lowering-functions-semantic-hash compiler))
                  (= (:function-count record) (:expression-lowering-function-count compiler))))
         (= (:artifact-hash record)
            (:expression-lowering-artifact-hash compiler))
         (= (:source-content-hash record)
            (:expression-lowering-source-content-hash compiler))
         (= (:semantic-hash record)
            (:expression-lowering-semantic-hash compiler))
         (= (select-keys
             record
             [:plan-assembly-function :plan-assembly-artifact-hash
              :plan-assembly-source-content-hash
              :plan-assembly-semantic-hash :plan-assembly-invoked?
              :plan-assembly-generic-bridge-residual?])
            (select-keys
             compiler
             [:plan-assembly-function :plan-assembly-artifact-hash
              :plan-assembly-source-content-hash
              :plan-assembly-semantic-hash :plan-assembly-invoked?
              :plan-assembly-generic-bridge-residual?])))))))

(defn p15-s23-stage2-compiler-artifact-source-path
  []
  (let [compiler-source (c-backend-resolve-p15-s23-compiler-source-path)
        compiler-file (java.io.File. compiler-source)
        sibling (java.io.File.
                 (or (.getParentFile compiler-file)
                     (java.io.File. "."))
                 "emitter.gravity")]
    (if (.isFile compiler-file)
      (.getPath sibling)
      p15-s23-stage2-compiler-artifact-source-relative-path)))

(defn p15-s23-stage2-compiler-artifact-augmented-emitter
  [emitter]
  (update-in emitter [:call-rules :builtin-functions]
             (fn [functions]
               (vec (sort-by str
                             (set/union (set functions)
                                        p15-s23-stage2-compiler-artifact-builtins))))))

(declare p15-s23-stage2-module-bundle p15-s23-stage2-module-literals)
(defn- p15-s23-stage2-seed-throw-plan-boundary [plan]
  (if (pos? (get-in plan [:instruction-summary :throw] 0))
    (-> plan
        (assoc-in [:compiler :owner] :clojure-stage0-seed)
        (assoc-in [:compiler :lowering-boundary] :temporary-single-source-seed-throw)
        (assoc-in [:effect-summary :inferred] :pending-seed-effect-inference)
        (assoc-in [:effect-summary :source-operation-occurrences] {:throw (get-in plan [:instruction-summary :throw])})
        (assoc-in [:effect-summary :legality] :not-checked))
    plan))
(defn p15-s23-stage2-compiler-artifact-plan-raw
  ([emitter artifact-source artifact-text module-routes]
   (p15-s23-stage2-compiler-artifact-plan-raw emitter artifact-source artifact-text module-routes nil))
  ([emitter artifact-source artifact-text module-routes map-order]
  (let [records (read-source-form-records artifact-source artifact-text)
        macro-artifact (macro-source-artifact-from-records artifact-source artifact-text records)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        function-table (stage0-function-table module)
        _ (validate-seed-source-headers! artifact-source (map :form records))
        literals (p15-s23-stage2-module-literals (:forms module))
        source-authority
        (when (= 'gravity.checked-core (:module module))
          (p15-s23-sh07-compiler-source-authority
           artifact-source artifact-text module function-table))
        module (cond-> (assoc module :function-table function-table :p15-s23-stage2-module-literals literals)
                 module-routes (assoc :p15-s23-stage2-module-routes module-routes)
                 map-order (assoc :p15-s23-stage2-map-order map-order)
                 source-authority
                 (assoc :p15-s23-sh07-source-authority source-authority))
        artifact-emitter
        (p15-s23-stage2-compiler-artifact-augmented-emitter emitter)
        functions
        (into (sorted-map)
              (map (fn [[name definition]]
                     [name (p15-s23-stage2-seed-compile-function
                            artifact-emitter module definition)]))
              function-table)
        instruction-summary
        (apply merge-with +
               (mapcat (fn [[_ function]]
                         (map stage0-instruction-summary
                              (:instructions function)))
                       functions))
        plan-base
        {:kind :gravity/stage2-compiler-artifact-plan
         :compiler-artifact-plan? true
         :entrypoint 'main
         :source {:sha256 (str "sha256:" (sha256-hex artifact-text))}
         :compiler {:owner :gravity-source
                    :stage :p15-s23-stage2-expression-lowering
                    :compiled-by :clojure-stage0-seed
                    :generic-bridge-residual? true
                    :artifact-hash
                    (p15-s23-stage2-compiler-artifact-expected-artifact-hash)
                    :source-content-hash
                    p15-s23-stage2-compiler-artifact-expected-source-content-hash
                    :semantic-hash
                    p15-s23-stage2-compiler-artifact-expected-semantic-hash}
         :module (select-keys module
                              [:module :profile :target :effects
                               :capabilities :exports :safety])
         :functions functions
         :instruction-summary instruction-summary
         :effect-summary {:declared (:effects module)
                          :inferred #{}
                          :capabilities (:capabilities module)}}
        plan-base (p15-s23-stage2-seed-throw-plan-boundary plan-base)]
    (assoc plan-base
           :plan-id (str "sha256:"
                         (sha256-hex
                          (pr-str (c-backend-canonical-value plan-base))))))))

(defn p15-s23-stage2-compiler-artifact-plan
  ([emitter artifact-source artifact-text]
   (p15-s23-stage2-compiler-artifact-plan-raw emitter artifact-source artifact-text nil))
  ([emitter artifact-source artifact-text module-inputs]
   (p15-s23-stage2-module-bundle emitter artifact-source artifact-text module-inputs)))

(defn p15-s23-stage2-compiler-artifact-semantic-input
  [plan]
  {:kind :gravity/p15-s23-stage2-expression-lowering-artifact
   :module (select-keys (:module plan)
                        [:module :profile :target :effects
                         :capabilities :exports :safety])
   :functions (c-backend-canonical-value (:functions plan))
   :instruction-summary (:instruction-summary plan)
   :effect-summary (:effect-summary plan)})
