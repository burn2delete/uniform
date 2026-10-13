
(defn- source-indexes [source-forms m0s]
  (let [definitions-by-module
        (mapv (fn [ordinal forms]
                (let [definitions (keep definition-from-form forms)]
                  (require! (= (count forms) (inc (count definitions)))
                            :unexpected-top-level-source-form
                            {:module-ordinal ordinal})
                  (require! (= (count definitions)
                               (count (filter #(and (seq? %)
                                                   (= 'defn (first %)))
                                              forms)))
                            :invalid-source-definition
                            {:module-ordinal ordinal})
                  (require! (= (count definitions)
                               (count (set (map :name definitions))))
                            :duplicate-source-definition
                            {:module-ordinal ordinal})
                  (into {} (map (juxt :name identity)) definitions)))
              (range) source-forms)
        all-names (mapcat keys definitions-by-module)]
    (require! (= (count all-names) (count (set all-names)))
              :duplicate-cross-module-definition)
    {:definitions-by-module definitions-by-module
     :owner-by-function
     (into {}
           (for [[ordinal definitions] (map-indexed vector definitions-by-module)
                 function (keys definitions)]
             [function ordinal]))
     :definition-hash
     (into {}
           (for [[ordinal definitions] (map-indexed vector definitions-by-module)
                 [function definition] definitions]
             [[ordinal function] (source-definition-hash definition)]))
     :builtin-symbols
     (set (map :builtin (mapcat :builtin-calls m0s)))}))

(defn scan-raw-source [source-forms m0s]
  (require! (and (vector? source-forms) (= 7 (count source-forms))
                 (every? vector? source-forms))
            :source-forms-shape)
  (let [indexes (source-indexes source-forms m0s)
        records (atom [])
        physical (atom -1)]
    (doseq [[module-ordinal forms] (map-indexed vector source-forms)]
      (let [descriptor (nth m0s module-ordinal)
            definitions (get-in indexes [:definitions-by-module module-ordinal])
            ns-forms (filter #(and (seq? %) (= 'ns (first %))) forms)]
        (require! (= 1 (count ns-forms)) :source-namespace-count
                  {:module-ordinal module-ordinal})
        (require! (= {:module (:module descriptor)
                      :profile (:profile descriptor)
                      :target (:target descriptor)
                      :exports (:exports descriptor)
                      :effects (:effects descriptor)
                      :capabilities (:capabilities descriptor)
                      :safety (:safety descriptor)}
                     (namespace-declaration (first ns-forms)))
                  :source-namespace-join {:module-ordinal module-ordinal})
        (doseq [[top-ordinal form] (map-indexed vector forms)]
          (if-let [definition (definition-from-form form)]
            (let [caller (:name definition)
                  hash (get-in indexes [:definition-hash
                                        [module-ordinal caller]])
                  context {:module-ordinal module-ordinal
                           :caller caller
                           :caller-function-source-hash hash
                           :owner-by-function (:owner-by-function indexes)
                           :local-functions (set (keys definitions))
                           :builtins (:builtin-symbols indexes)}
                  row {:node-ordinal (count @records)
                       :module-ordinal module-ordinal
                       :caller caller
                       :caller-function-source-hash hash
                       :source-path [top-ordinal]
                       :callsite-path nil
                       :node-kind :list
                       :quoted-subtree-stop false
                       :possible-call-head true
                       :classification :definition
                       :callee 'defn
                       :arity (count (rest form))
                       :physical-ordinal (swap! physical inc)
                       :semantic-row true}]
              (swap! records conj row)
              (doseq [[header-index header]
                      [[0 'defn] [1 caller] [2 (:params definition)]]]
                (scan-raw-node! records physical [top-ordinal header-index]
                                nil header
                                (assoc context :caller nil
                                               :caller-function-source-hash nil)
                                indexes))
              (doseq [[body-index body]
                      (map-indexed vector (:body definition))]
                (scan-raw-node! records physical
                                [top-ordinal (+ 3 body-index)] [body-index]
                                body context indexes)))
            (scan-raw-node!
             records physical [top-ordinal] nil form
             {:module-ordinal module-ordinal :caller nil
              :caller-function-source-hash nil
              :owner-by-function (:owner-by-function indexes)
              :local-functions #{} :builtins (:builtin-symbols indexes)}
             indexes)))))
    {:records @records
     :row-count (count (filter :semantic-row @records))
     :indexes indexes}))

(defn- instruction-children [instruction]
  (let [op (:op instruction)]
    (case op
      :println (:args instruction)
      :do (:body instruction)
      :if [(:test instruction) (:then instruction) (:else instruction)]
      :let (concat (map :expr (:bindings instruction)) (:body instruction))
      :loop (concat (map :expr (:bindings instruction)) (:body instruction))
      :recur (:args instruction)
      :builtin-call (:args instruction)
      :function-call (:args instruction)
      :vector-literal (:items instruction)
      :set-literal (:items instruction)
      :map-literal (mapcat (fn [entry] [(:key entry) (:value entry)])
                           (:entries instruction))
      (:literal :local :quote) []
      (fail! :unknown-m2-operation {:op op}))))

(defn- valid-instruction-shape? [instruction]
  (and (map? instruction)
       (keyword? (:op instruction))
       (case (:op instruction)
         (:literal :quote) (exact-keys? instruction #{:op :value})
         :local (and (exact-keys? instruction #{:op :name})
                     (symbol? (:name instruction)))
         (:println :recur :builtin-call :function-call)
         (vector? (:args instruction))
         :do (and (exact-keys? instruction #{:op :body})
                  (vector? (:body instruction)))
         :if (and (exact-keys? instruction #{:op :test :then :else})
                  (every? map? [(:test instruction) (:then instruction)
                                (:else instruction)]))
         (:let :loop)
         (and (vector? (:bindings instruction))
              (every? #(and (exact-keys? % #{:name :expr})
                            (symbol? (:name %)) (map? (:expr %)))
                      (:bindings instruction))
              (vector? (:body instruction))
              (exact-keys? instruction
                           (if (= :loop (:op instruction))
                             #{:op :binding-count :bindings :body}
                             #{:op :bindings :body})))
         (:vector-literal :set-literal)
         (and (exact-keys? instruction #{:op :items})
              (vector? (:items instruction)))
         :map-literal
         (and (exact-keys? instruction #{:op :entries})
              (vector? (:entries instruction))
              (every? #(and (exact-keys? % #{:key :value})
                            (map? (:key %)) (map? (:value %)))
                      (:entries instruction)))
         false)))

(defn- scan-m2-instruction!
  [records module-instruction occurrence instruction context path]
  (require! (valid-instruction-shape? instruction) :m2-instruction-shape
            {:module-ordinal (:module-ordinal context)
             :caller (:caller context) :path path})
  (let [{:keys [module-ordinal caller caller-function-source-hash
                owner-by-function builtin-symbols]} context
        op (:op instruction)
        callee (when (contains? #{:function-call :builtin-call} op)
                 (:function instruction))
        classification
        (case op
          :function-call
          (let [callee-module (get owner-by-function callee)]
            (cond
              (nil? callee-module) :unknown-function-call
              (= module-ordinal callee-module) :local-runtime-call
              :else :external-runtime-call))
          :builtin-call
          (if (contains? builtin-symbols callee)
            :builtin-call :unknown-builtin-call)
          :not-a-call)
        semantic? (contains? #{:local-runtime-call :external-runtime-call
                               :builtin-call} classification)
        occurrence-ordinal (when semantic? (swap! occurrence inc))
        instruction-ordinal (swap! module-instruction inc)
        provenance (:compiler-internal-provenance instruction)
        row {:node-ordinal (count @records)
             :module-ordinal module-ordinal
             :instruction-ordinal instruction-ordinal
             :caller caller
             :caller-function-source-hash caller-function-source-hash
             :path path
             :op op
             :call-classification classification
             :callee callee
             :arity (when semantic? (count (:args instruction)))
             :external-link (:external-link instruction)
             :provenance-present (contains? instruction
                                            :compiler-internal-provenance)
             :provenance provenance
             :occurrence-ordinal occurrence-ordinal
             :semantic-row semantic?}]
    (require! (not (contains? #{:unknown-function-call :unknown-builtin-call}
                              classification))
              classification {:module-ordinal module-ordinal
                              :caller caller :callee callee})
    (case classification
      :local-runtime-call
      (require! (exact-keys? instruction #{:op :function :args})
                :m2-local-call-schema)
      :external-runtime-call
      (require! (exact-keys? instruction
                             #{:op :function :args :external-link})
                :m2-external-call-schema)
      :builtin-call
      (if (contains? (set internal-builtins) callee)
        (require! (and (exact-keys?
                        instruction
                        #{:op :function :args :compiler-internal-caller
                          :compiler-internal-provenance})
                       (= caller (:compiler-internal-caller instruction)))
                  :m2-internal-call-schema)
        (require! (exact-keys? instruction #{:op :function :args})
                  :m2-builtin-call-schema))
      :not-a-call
      (case op
        (:println :recur) (require! (exact-keys? instruction #{:op :args})
                                    :m2-noncall-schema)
        nil))
    (swap! records conj row)
    (doseq [[ordinal child] (map-indexed vector
                                         (instruction-children instruction))]
      (require! (map? child) :m2-child-instruction-shape
                {:module-ordinal module-ordinal :caller caller
                 :path (conj path ordinal)})
      (scan-m2-instruction! records module-instruction occurrence child context
                            (conj path ordinal)))))
