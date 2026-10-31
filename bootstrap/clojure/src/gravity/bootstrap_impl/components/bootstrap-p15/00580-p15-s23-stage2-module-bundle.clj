(def p15-s23-stage2-module-boundary
  {:revision-status :development-unadmitted :compiled-by :clojure-stage0-seed
   :execution :clojure-stage2-interpreter :routing-owner :clojure-stage0-seed
   :effects :not-checked :self-hosted? false
   :pending [:checked-imported-user-calls :body-effects :module-initialization :target-lowering :migration]})

(defn p15-s23-stage2-module-routes [caller callees]
  (let [bindings (into {} (map (juxt :binding-id identity) (get-in caller [:artifact :binding-table :bindings])))
        resolutions (into {} (map (juxt :syntax-id identity)
                                  (get-in caller [:artifact :gravity-resolution-boundary :resolved-analysis :resolution-table])))
        refs (get-in caller [:request :references])]
    (vec
     (mapcat
      (fn [[name definition]]
        (let [owner (p15-s23-stage2-module-one!
                     (filterv #(and (= :namespace (:binding-class %)) (= name (:name %))) (vals bindings)) :caller-function-binding)
              syntax (p15-s23-stage2-module-one!
                      (filterv #(= (:syntax/id %) (:definition-syntax-id owner)) (:syntax caller)) :caller-function-syntax)
              owned (filterv #(= (:source-span %) (:span syntax)) refs)
              imported (filterv #(= :import (:binding-class (get bindings (:binding-id (get resolutions (:syntax-id %)))))) owned)
              sites (p15-s23-stage2-module-call-sites (assoc definition :source-path (:source-path caller) :p15-s23-stage2-map-order (:map-order caller)))
              map-index (when (and (seq sites) (some (fn [[[owner _] _]] (= name owner)) (:map-order caller)))
                          (p15-s23-stage2-module-map-reference-index caller syntax definition))
              candidates (group-by :symbol imported)
              counters (atom {})]
          (when-not (every? #(and (= :operator (:position %)) (namespace (:symbol %))) imported)
            (p15-s23-stage2-module-fail! :qualified-direct-import-calls-only {:function name}))
          (when-not (if map-index
                      (and (= (count sites) (count imported))
                           (= (set (map :syntax-id (vals map-index))) (set (map :syntax-id imported))))
                      (= (frequencies (map :symbol sites)) (frequencies (map :symbol imported))))
            (p15-s23-stage2-module-fail! :source-callsite-resolution-coverage {:function name}))
          (mapv
           (fn [{:keys [path symbol form]}]
             (let [ordinal (when-not map-index (get @counters symbol 0))
                   _ (when-not map-index (swap! counters update symbol (fnil inc 0)))
                   reference (if map-index (get map-index path) (nth (get candidates symbol) ordinal))
                   _ (when-not (= symbol (:symbol reference))
                       (p15-s23-stage2-module-fail! :map-reference-callsite-mismatch {:callsite path :symbol symbol}))
                   binding (get bindings (:binding-id (get resolutions (:syntax-id reference))))
                   callee (get callees (get-in binding [:semantic-span :ordinary-module-artifact-id]))
                   _ (when-not callee (p15-s23-stage2-module-fail! :missing-dependency-plan {:symbol symbol}))
                   original (p15-s23-stage2-module-origin! caller binding callee)
                   target (get-in callee [:functions (:name original)])
                   arity (count (:params target))
                   route {:caller-module-id (:module-id caller) :caller-function name :callsite-path path
                          :symbol symbol :reference-syntax-id (:syntax-id reference) :import-binding-id (:binding-id binding)
                          :callee-module-id (:module-id callee) :callee-binding-id (:binding-id original)
                          :callee-function (:name original) :arity arity :origin (:semantic-span binding)}]
               (when-not (= arity (count (rest form)))
                 (fail! "L2-FUNCTION-ARITY" "Compiler-artifact imported call has wrong arity"
                        {:function symbol :expected-arity arity :actual-arity (count (rest form))
                         :source-span (:source-span reference)}))
               (assoc route :route-id (reader-canonical-hash {:domain :gravity/stage2-module-route-v1 :route route})))) sites)))
      (sort-by (comp str key) (:functions caller))))))

(defn p15-s23-stage2-module-compile-call [emitter module locals form child-module]
  (let [route (get (:p15-s23-stage2-module-routes module)
                   [(:p15-s23-sh07-enclosing-function module) (:p15-s23-sh07-callsite-path module)])]
    (when-not (= (:symbol route) (first form))
      (p15-s23-stage2-module-fail! :compiled-callsite-symbol {}))
    {:op :compiler-module-call :route-id (:route-id route) :function (first form)
     :caller-function (:caller-function route) :callsite-path (:callsite-path route)
     :args (mapv (fn [index arg] (p15-s23-stage2-seed-compile-expr emitter (child-module index) locals arg))
                 (range 1 (count form)) (rest form))}))

(defn p15-s23-stage2-module-instruction-children [instruction]
  (case (:op instruction)
    (:println :builtin-call :function-call :compiler-module-call :recur) (:args instruction)
    :do (:body instruction)
    :throw [(:value instruction)]
    :if [(:test instruction) (:then instruction) (:else instruction)]
    (:let :loop) (concat (map :expr (:bindings instruction)) (:body instruction))
    (:vector-literal :set-literal) (:items instruction)
    :map-literal (mapcat (juxt :key :value) (:entries instruction))
    []))

(defn p15-s23-stage2-module-instructions [plan]
  (loop [pending (vec (mapcat :instructions (vals (:functions plan)))) result []]
    (if (empty? pending) result
      (let [instruction (peek pending)]
        (when (or (some #(contains? instruction %) [:external-link :compiler-internal-caller :compiler-internal-provenance
                                                  :source-authority :special-role :p15-s23-sh07-source-authority])
                  (= 'sh07-b51-admitted-executable-carrier-validation? (:function instruction)))
          (p15-s23-stage2-module-fail! :admission-instruction-forbidden {}))
        (when (= :throw (:op instruction)) (p15-s23-stage2-module-fail! :single-source-throw-only {}))
        (recur (into (pop pending) (p15-s23-stage2-module-instruction-children instruction)) (conj result instruction))))))

(defn p15-s23-stage2-module-entry [source plan]
  (p15-s23-stage2-module-instructions plan)
  {:module-id (:module-id source) :resolution-artifact (:artifact source)
   :plan plan :exports (p15-s23-stage2-module-exports source)})

(defn p15-s23-stage2-module-identity [emitter root entries routes]
  {:domain :gravity/stage2-compiler-artifact-module-bundle-v1 :root-module-id root
   :emitter emitter :boundary p15-s23-stage2-module-boundary :routes routes
   :modules (mapv (fn [entry]
                    {:module-id (:module-id entry) :plan-id (get-in entry [:plan :plan-id])
                     :module (get-in entry [:resolution-artifact :gravity-resolution-boundary :resolved-analysis :module-contract])
                     :exports (mapv #(dissoc % :source-span) (:exports entry))}) entries)})

(declare sh06-resolution-carrier-validation sh06-resolution-artifact-bounds p15-s23-sh02-sha256-id?)

(defn p15-s23-stage2-module-closure [artifact]
  ;; DATA preflight and deduplication only; module-source still freshly verifies
  ;; every unique artifact, including its retained C5 dependency verification.
  (when-not (= :passed (:status (sh06-resolution-carrier-validation artifact sh06-resolution-artifact-bounds)))
    (p15-s23-stage2-module-fail! :bounded-module-artifact-carrier {}))
  (let [enqueue
        (fn [[pending seen] children]
          (when-not (vector? children)
            (p15-s23-stage2-module-fail! :bounded-module-dependencies {}))
          (reduce
           (fn [[pending seen] child]
             (let [id (:artifact-id child)]
               (when-not (p15-s23-sh02-sha256-id? id)
                 (p15-s23-stage2-module-fail! :module-artifact-id-required {}))
               (if-let [previous (get seen id)]
                 (do (when-not (= previous child)
                       (p15-s23-stage2-module-fail! :conflicting-module-artifact {:module-id id}))
                     [pending seen])
                 [(conj pending child) (assoc seen id child)])))
           [pending seen] children))]
    (loop [[pending seen] (enqueue [[] {}] [artifact])]
      (if (empty? pending)
        (into [artifact] (map second) (sort-by key (dissoc seen (:artifact-id artifact))))
        (let [current (peek pending)
              children (get-in current [:gravity-resolution-boundary :module-inputs :module-artifacts])]
          (recur (enqueue [(pop pending) seen] children)))))))

(defn p15-s23-stage2-module-route-index [routes]
  (let [index (into {} (map (fn [r] [[(:caller-function r) (:callsite-path r)] r]) routes))]
    (when-not (= (count routes) (count index)) (p15-s23-stage2-module-fail! :duplicate-callsite-route {}))
    index))

(defn p15-s23-stage2-module-bundle [emitter source-path source-text inputs]
  (when-not (and (map? inputs) (= #{:caller-resolution-artifact :dependency-plans} (set (keys inputs)))
                 (vector? (:dependency-plans inputs)) (<= (count (:dependency-plans inputs)) 256))
    (p15-s23-stage2-module-fail! :closed-module-inputs-required {}))
  (let [artifacts (p15-s23-stage2-module-closure (:caller-resolution-artifact inputs))
        plans (:dependency-plans inputs)
        _ (when-not (and (= (dec (count artifacts)) (count plans))
                         (= (count plans) (count (set (map :plan-id plans)))))
            (p15-s23-stage2-module-fail! :source-emitter-or-dependency-set {}))
        sources (mapv p15-s23-stage2-module-source artifacts)
        caller (first sources) callees (subvec sources 1)]
    (when-not (and (= source-path (:source-path caller)) (= source-text (:source-text caller))
                   (= emitter (:emitter (c-backend-stage2-plan-emitter-source-rule! source-path :jvm)))
                   (= (count callees) (count plans) (count (set (map :module-id callees))))
                   (= (count sources) (count (set (map #(get-in % [:module :namespace]) sources)))
                      (count (set (map :source-path sources)))))
      (p15-s23-stage2-module-fail! :source-emitter-or-dependency-set {}))
    (doseq [source sources] (p15-s23-stage2-module-shadowing-check! source emitter))
    (let [by-id (into {} (map (juxt :module-id identity) sources))
          routes (reduce (fn [result source]
                           (let [next-routes (p15-s23-stage2-module-routes source by-id)]
                             (when (> (+ (count result) (count next-routes)) 65536)
                               (p15-s23-stage2-module-fail! :module-route-limit {}))
                             (into result next-routes))) [] sources)
          indexes (into {} (map (fn [source]
                                  [(:module-id source)
                                   (p15-s23-stage2-module-route-index
                                    (filterv #(= (:module-id source) (:caller-module-id %)) routes))]) sources))
          dependencies
          (mapv (fn [source]
                  (let [actual (p15-s23-stage2-module-one!
                                (filterv #(= (get-in % [:module :module]) (get-in source [:module :namespace])) plans)
                                :unique-independent-module-plan)
                        expected (p15-s23-stage2-compiler-artifact-plan-raw
                                  emitter (:source-path source) (:source-text source) (get indexes (:module-id source)) (:map-order source))]
                    (when-not (= actual expected)
                      (p15-s23-stage2-module-fail! :independent-raw-plan-mismatch {:module-id (:module-id source)}))
                    (p15-s23-stage2-module-entry source actual))) callees)
          plan (p15-s23-stage2-compiler-artifact-plan-raw emitter source-path source-text (get indexes (:module-id caller)) (:map-order caller))
          entries (vec (sort-by :module-id (conj dependencies (p15-s23-stage2-module-entry caller plan))))
          identity (p15-s23-stage2-module-identity emitter (:module-id caller) entries routes)]
      {:kind :gravity/stage2-compiler-artifact-module-bundle :schema-version 1 :root-module-id (:module-id caller)
       :emitter emitter :modules entries :routes routes :identity-input identity
       :bundle-id (reader-canonical-hash identity) :boundary p15-s23-stage2-module-boundary})))

(defn p15-s23-stage2-module-validate-bundle! [bundle]
  (when-not (and (map? bundle) (= #{:kind :schema-version :root-module-id :emitter :modules :routes :identity-input :bundle-id :boundary}
                                 (set (keys bundle)))
                 (= :gravity/stage2-compiler-artifact-module-bundle (:kind bundle)) (= 1 (:schema-version bundle))
                 (vector? (:modules bundle)) (<= 1 (count (:modules bundle)) 257)
                 (vector? (:routes bundle)) (<= (count (:routes bundle)) 65536)
                 (every? #(and (map? %) (= #{:module-id :resolution-artifact :plan :exports} (set (keys %)))) (:modules bundle)))
    (p15-s23-stage2-module-fail! :closed-module-bundle-required {}))
  (let [root (p15-s23-stage2-module-one! (filterv #(= (:module-id %) (:root-module-id bundle)) (:modules bundle)) :root-module)
        c5 (:resolution-artifact root)
        path (get-in c5 [:provenance :source-path])
        text (c2-reader-artifact-source-text path
               (get-in c5 [:sh05-macro-artifact :gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact]))
        expected (p15-s23-stage2-module-bundle (:emitter bundle) path text
                   {:caller-resolution-artifact c5
                    :dependency-plans (mapv :plan (remove #(= (:module-id %) (:root-module-id bundle)) (:modules bundle)))})]
    (when-not (= expected bundle) (p15-s23-stage2-module-fail! :module-bundle-reconstruction-mismatch {}))
    bundle))
