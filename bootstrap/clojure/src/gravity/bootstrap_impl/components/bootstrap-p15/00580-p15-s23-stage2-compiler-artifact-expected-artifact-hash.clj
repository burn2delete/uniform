

(defn p15-s23-stage2-compiler-artifact-expected-artifact-hash
  []
  (str "sha256:"
       (sha256-hex
        (pr-str
         (c-backend-canonical-value
          {:source-content-hash
           p15-s23-stage2-compiler-artifact-expected-source-content-hash
           :semantic-hash
           p15-s23-stage2-compiler-artifact-expected-semantic-hash})))))

(def p15-s23-stage2-compiler-artifact-record-keys
  #{:artifact :source-content-hash :semantic-hash :artifact-hash :functions
    :invoked? :generic-bridge-residual? :plan-assembly-function
    :plan-assembly-artifact-hash :plan-assembly-source-content-hash
    :plan-assembly-semantic-hash :plan-assembly-invoked?
    :plan-assembly-generic-bridge-residual? :clojure-seed-boundary?
    :self-hosted?})

(defn p15-s23-stage2-compiler-artifact-record-authentic?
  [record]
  (and (map? record)
       (= p15-s23-stage2-compiler-artifact-record-keys
          (set (keys record)))
       (= :gravity/p15-s23-stage2-expression-lowering-binding
          (:artifact record))
       (= p15-s23-stage2-compiler-artifact-required-functions
          (:functions record))
       (= p15-s23-stage2-compiler-artifact-expected-source-content-hash
          (:source-content-hash record))
       (= p15-s23-stage2-compiler-artifact-expected-semantic-hash
          (:semantic-hash record))
       (= (p15-s23-stage2-compiler-artifact-expected-artifact-hash)
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
       (false? (:self-hosted? record))))

(defn p15-s23-stage2-compiler-artifact-record-matches-plan?
  [record plan]
  (let [compiler (:compiler plan)]
    (and (= (:artifact-hash record)
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
              :plan-assembly-generic-bridge-residual?])))))

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

(defn p15-s23-stage2-compiler-artifact-plan
  [emitter artifact-source artifact-text]
  (let [macro-artifact (macro-source-artifact artifact-source artifact-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        function-table (stage0-function-table module)
        module (assoc module :function-table function-table)
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
                    :generic-bridge-residual? true}
         :module (select-keys module
                              [:module :profile :target :effects
                               :capabilities :exports :safety])
         :functions functions
         :instruction-summary instruction-summary
         :effect-summary {:declared (:effects module)
                          :inferred #{}
                          :capabilities (:capabilities module)}}]
    (assoc plan-base
           :plan-id (str "sha256:"
                         (sha256-hex
                          (pr-str (c-backend-canonical-value plan-base)))))))

(defn p15-s23-stage2-compiler-artifact-semantic-input
  [plan]
  {:kind :gravity/p15-s23-stage2-expression-lowering-artifact
   :module (select-keys (:module plan)
                        [:module :profile :target :effects
                         :capabilities :exports :safety])
   :functions (c-backend-canonical-value (:functions plan))
   :instruction-summary (:instruction-summary plan)
   :effect-summary (:effect-summary plan)})