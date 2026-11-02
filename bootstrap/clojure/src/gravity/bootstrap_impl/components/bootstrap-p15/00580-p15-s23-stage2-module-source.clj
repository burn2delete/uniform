(declare sh06-resolution-artifact-verification
         c2-reader-artifact-source-text c-backend-stage2-plan-emitter-source-rule!
         p15-s23-stage2-module-bundle p15-s23-stage2-module-routes)

(defn p15-s23-stage2-module-fail! [reason facts]
  (fail! "BOOT2006" "Unadmitted compiler-artifact module input is inconsistent or unsupported"
         {:stage :compiler-artifact-module-development
          :source-span {:source (:source-path facts)}
          :facts (assoc facts :reason reason)
          :remediation "Supply ordinary verified C5 artifacts and independent current seed plans; this is not checked program execution."}))

(defn p15-s23-stage2-module-one! [rows reason]
  (when-not (= 1 (count rows))
    (p15-s23-stage2-module-fail! reason {:count (count rows)}))
  (first rows))

(defn p15-s23-stage2-module-fixed-definition? [form]
  (and (seq? form) (= 'defn (first form))
       (symbol? (second form)) (nil? (namespace (second form)))
       (vector? (nth form 2 nil))
       (every? #(and (symbol? %) (nil? (namespace %)) (not (contains? '#{:- &} %))) (nth form 2))
       (= (count (nth form 2)) (count (set (nth form 2))))
       (seq (drop 3 form)) (not= ':- (nth form 3 nil))))

(defn p15-s23-stage2-module-literal! [value]
  (gravity.bootstrap.module-literals/materialize! #'p15-s23-stage2-module-fail! value))

(defn p15-s23-stage2-module-literals [forms]
  (let [declarations (filterv #(and (seq? %) (contains? '#{def defn} (first %))) forms)
        names (mapv second declarations)]
    (when-not (= (count names) (count (set names)))
      (p15-s23-stage2-module-fail! :duplicate-module-definition {}))
    (into (sorted-map)
          (keep (fn [form]
                  (when (and (= 'def (first form)) (not (def-function-form? form)))
                    (when-not (and (= 3 (count form)) (symbol? (second form)) (nil? (namespace (second form))))
                      (p15-s23-stage2-module-fail! :literal-data-declaration-required {}))
                    [(second form) (p15-s23-stage2-module-literal! (nth form 2))]))) declarations)))

(defn p15-s23-stage2-module-declarations! [source forms]
  (let [bindings (get-in source [:artifact :binding-table :bindings])
        module (:module source)]
    (doseq [form forms]
      (let [name (second form)
            binding (p15-s23-stage2-module-one!
                     (filterv #(and (= :namespace (:binding-class %)) (= name (:name %))) bindings)
                     :unique-declaration-binding)
            syntax (p15-s23-stage2-module-one!
                    (filterv #(= (:syntax/id %) (:definition-syntax-id binding)) (:syntax source))
                    :declaration-syntax)]
        (when-not (and (= form (:form syntax))
                       (= (if (contains? (:functions source) name) :function :var) (:kind binding))
                       (= (:namespace module) (:namespace binding)) (= (:package module) (:package binding))
                       (contains? #{[:meta :jvm] [:hosted :jvm]} [(:profile module) (:target module)])
                       (= [(:profile module)] (:profile-set binding)) (= [(:target module)] (:target-set binding))
                       (= (if (some #{name} (:exports module)) :public :private) (:visibility binding))
                       (= (get-in source [:request :module :c4-artifact-id]) (:definition-artifact-id binding)))
          (p15-s23-stage2-module-fail! :declaration-binding-mismatch {:symbol name}))))))

(defn p15-s23-stage2-module-source [artifact]
  (let [boundary (:gravity-resolution-boundary artifact)
        request (:authenticated-resolution-request boundary)]
    (when-not (and (= :gravity/sh06-resolution-artifact (:kind artifact))
                   (= :accepted (:status artifact)) (= :gravity-source (:owner boundary))
                   (= :gravity/sh06-ordinary-module-resolution-request (:artifact request))
                   (= 2 (:schema-version request))
                   (= :passed (:status (sh06-resolution-artifact-verification artifact))))
      (p15-s23-stage2-module-fail! :verified-ordinary-c5-required {}))
    (let [sh05 (:sh05-macro-artifact artifact)
          c2 (get-in sh05 [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact])
          path (get-in artifact [:provenance :source-path])
          text (c2-reader-artifact-source-text path c2)
          forms (:parsed-semantic-values c2)
          module (:module request)
          macro (macro-source-artifact path text)
          expanded (vec (remove #(and (seq? %) (= 'ns (first %)))
                                (map :form (:expanded-syntax-stream sh05))))]
      (when-not (and (string? path) (seq forms)
                     (= ['ns (:namespace module)] (vec (take 2 (first forms))))
                     (contains? #{[:meta :jvm] [:hosted :jvm]} [(:profile module) (:target module)])
                     (= (:namespace module) (get-in macro [:module :module]))
                     (= expanded (:expanded-forms macro)))
        (p15-s23-stage2-module-fail! :source-or-expansion-mismatch {:source-path path}))
      (when-not (every? #(or (p15-s23-stage2-module-fixed-definition? %)
                             (and (seq? %) (= 'def (first %)))) (rest forms))
        (p15-s23-stage2-module-fail! :function-only-unannotated-seed-subset {:source-path path}))
      (doseq [form (rest forms) :when (= 'def (first form))]
        (when-not (= 3 (count form)) (p15-s23-stage2-module-fail! :literal-data-declaration-required {}))
        (p15-s23-stage2-module-literal! (nth form 2)))
      (let [source {:artifact artifact :module-id (:artifact-id artifact) :module module
                    :source-path path :source-text text :macro macro
                    :literals (p15-s23-stage2-module-literals expanded)
                    :functions (stage0-function-table (assoc (:module macro) :forms (:expanded-forms macro)))
                    :syntax (:expanded-syntax-stream sh05) :request request}]
        (p15-s23-stage2-module-declarations! source expanded)
        (assoc source :map-order (p15-s23-stage2-module-map-order source))))))

(defn p15-s23-stage2-module-shadowing-check! [source emitter]
  (let [bindings (into {} (map (juxt :binding-id identity) (get-in source [:artifact :binding-table :bindings])))
        resolved (into {} (map (juxt :syntax-id identity)
                              (get-in source [:artifact :gravity-resolution-boundary :resolved-analysis :resolution-table])))
        emitter (p15-s23-stage2-compiler-artifact-augmented-emitter emitter)
        reserved (into (set (get-in emitter [:call-rules :builtin-functions]))
                       (concat (keys (:special-form-rules emitter))
                               (keys (get-in emitter [:call-rules :compiler-internal-builtin-arities]))
                               '[println do if let loop recur throw quote host-reflect]))]
    (doseq [reference (get-in source [:request :references])
            :when (and (= :operator (:position reference))
                       (or (contains? reserved (:symbol reference)) (contains? (:functions source) (:symbol reference))
                           (contains? (:literals source) (:symbol reference))))]
      (let [binding (get bindings (:binding-id (get resolved (:syntax-id reference))))]
        (when (= 'throw (:symbol reference))
          (p15-s23-stage2-module-fail! :single-source-throw-only {:source-path (:source-path source)}))
        (when-not (if (contains? reserved (:symbol reference))
                    (= :core (:binding-class binding))
                    (and (= :namespace (:binding-class binding)) (= :function (:kind binding))
                         (= (get-in source [:module :namespace]) (:namespace binding))
                         (= (:symbol reference) (:name binding))))
          (p15-s23-stage2-module-fail! :unsupported-seed-operator-shadowing
                                      {:symbol (:symbol reference) :binding-id (:binding-id binding)}))))))

(defn p15-s23-stage2-module-exports [source]
  (let [artifact (:artifact source) module (:module source)]
    (mapv (fn [name]
            (let [binding (p15-s23-stage2-module-one!
                           (filterv #(and (= :namespace (:binding-class %)) (= name (:name %)))
                                    (get-in artifact [:binding-table :bindings])) :unique-export-binding)]
              (when-not (and (= :public (:visibility binding))
                             (case (:kind binding)
                               :function (contains? (:functions source) name)
                               :var (contains? (:literals source) name)
                               false))
                (p15-s23-stage2-module-fail! :exported-seed-declaration-required {:symbol name}))
              binding)) (:exports module))))

(defn p15-s23-stage2-module-call-sites [definition]
  (letfn [(walk [form path blocked?]
            (cond
              (and (seq? form) (= 'quote (first form))) []
              (seq? form)
              (let [op (first form)
                    qualified? (and (symbol? op) (namespace op))
                    blocked? (or blocked? (= 'fn op))]
                (when (and qualified? blocked?)
                  (p15-s23-stage2-module-fail! :unsupported-nested-module-call {:callsite path :symbol op}))
                (if (contains? '#{let loop} op)
                  (let [bindings (second form)]
                    (when-not (and (vector? bindings) (even? (count bindings))
                                   (every? symbol? (take-nth 2 bindings)))
                      (if (= 'loop op)
                        (p15-s23-stage2-seed-validate-tail-body!
                         definition (:body definition) (count (:params definition)) true)
                        (p15-s23-stage2-module-fail! :symbol-only-sequential-let-required {:callsite path})))
                    (into (vec (mapcat (fn [index] (walk (nth bindings index) (conj path 1 index) blocked?))
                                       (range 1 (count bindings) 2)))
                          (mapcat (fn [index body] (walk body (conj path index) blocked?))
                                  (range 2 (count form)) (drop 2 form))))
                  (into (if qualified? [{:function (:name definition) :path path :symbol op :form form}] [])
                        (mapcat (fn [index arg] (walk arg (conj path index) blocked?))
                                (range 1 (count form)) (rest form)))))
              (vector? form)
              (vec (mapcat (fn [index item] (walk item (conj path index) blocked?)) (range (count form)) form))
              (and (not blocked?) (map? form) (contains? definition :p15-s23-stage2-map-order))
              (vec (mapcat (fn [[slot _ value]] (walk value (conj path (inc (* 2 slot))) blocked?))
                           (p15-s23-stage2-module-map-entries
                            (assoc definition :p15-s23-sh07-enclosing-function (:name definition)) form path)))
              (coll? form)
              (mapcat #(walk % path true) (if (map? form) (mapcat identity form) form))
              :else []))]
    (vec (mapcat (fn [index form] (walk form [index] false)) (range) (:body definition)))))

(defn p15-s23-stage2-module-origin! [caller binding callee]
  (let [origin (:semantic-span binding)
        original (p15-s23-stage2-module-one!
                   (filterv #(= (:binding-id %) (:origin-binding-id origin))
                            (get-in callee [:artifact :binding-table :bindings])) :original-binding)
        module (:module callee)]
    (when-not (and (= :import (:binding-class binding))
                   (= (:module-id callee) (:ordinary-module-artifact-id origin) (:definition-artifact-id binding))
                   (= (:origin-analysis-id origin) (get-in callee [:artifact :gravity-resolution-boundary :resolved-analysis :artifact-id]))
                   (= (:origin-definition-artifact-id origin) (:definition-artifact-id original))
                   (= (:origin-definition-syntax-id origin) (:definition-syntax-id original))
                   (= (:origin-definition origin)
                      (select-keys original [:name :kind :namespace :package :visibility :profile-set :target-set :type-ref :effects :capabilities :safety]))
                   (= :namespace (:binding-class original)) (= :public (:visibility original) (:visibility binding))
                   (= :function (:kind original) (:kind binding))
                   (= (:name binding) (:name original)) (= (:namespace binding) (:namespace original) (:namespace module))
                   (= (:package binding) (:package original) (:package module))
                   (contains? #{[:meta :jvm] [:hosted :jvm]} [(:profile module) (:target module)])
                   (= [(:profile module)] (:profile-set original) (:profile-set binding))
                   (= [(:target module)] (:target-set original) (:target-set binding))
                   (some #{(:name original)} (:exports module))
                   (= [(:profile (:module caller)) (:target (:module caller))] [(:profile module) (:target module)]))
      (p15-s23-stage2-module-fail! :imported-function-origin {:symbol (:name binding)}))
    original))
