(ns gravity.self-hosting.sh07-selected-owner-primitive-use-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

;; C01/BOOT2/BOOT8: actual seed plans and potential executable occurrences,
;; not an execution trace, Gravity compilation, or migration acceptance.
(def owner-paths
  ["bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity"
   "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"
   "bootstrap/gravity/src/gravity/bootstrap/syntax.gravity"
   "bootstrap/gravity/src/gravity/macro.gravity"
   "bootstrap/gravity/src/gravity/resolution.gravity"
   "bootstrap/gravity/src/gravity/checked_core.gravity"])

(defn- root []
  (loop [directory (io/file (System/getProperty "user.dir"))]
    (cond (.isFile (io/file directory "deps.edn")) directory
          (.getParentFile directory) (recur (.getParentFile directory))
          :else (throw (ex-info "Repository absent" {:id "SH07-C01-ROOT"})))))
(defn- path [relative] (.getCanonicalPath (io/file (root) relative)))
(defn- hash-value [value]
  (str "sha256:" (b/sha256-hex (pr-str (b/c-backend-canonical-value value)))))
(defn- file-id [relative]
  (let [bytes (java.nio.file.Files/readAllBytes (.toPath (io/file (path relative))))]
    {:path relative :bytes (alength bytes) :sha256 (str "sha256:" (b/sha256-bytes-hex bytes))}))
(defn- require! [condition reason]
  (when-not condition (throw (ex-info "C01 source/plan join rejected" {:id "SH07-C01-JOIN" :reason reason}))))

(defn- function-signature [definition function]
  (require! (and definition function
                 (= (:params definition) (:params function))
                 (= (:arity definition) (count (:params definition)))) :function-signature)
  {:parameters (:params definition) :fixed-arity (:arity definition)})

(defn- validate-local-calls! [uses plan definitions]
  (doseq [use uses :when (= :function-call (:op use))]
    (let [callee (:callee use) function (get-in plan [:functions callee])]
      (require! function :missing-local-callee)
      (require! (= (:arity use) (:fixed-arity (function-signature (get definitions callee) function)))
                :local-call-arity)))
  true)

(defn- children [form instruction source-path plan-path]
  (let [pairs (fn [forms nodes source-slots plan-slots]
                (require! (= (count forms) (count nodes) (count source-slots) (count plan-slots)) :child-count)
                (mapv (fn [f n s p] [f n (into source-path s) (into plan-path p)])
                      forms nodes source-slots plan-slots))
        indexed (fn [forms nodes source-start field]
                  (pairs forms nodes (mapv vector (range source-start (+ source-start (count forms))))
                         (mapv #(vector field %) (range (count nodes)))))]
    (case (:op instruction)
      (:literal :local) []
      :quote (do (require! (and (= 'quote (first form)) (= 2 (count form))
                               (= (second form) (:value instruction))) :quote) [])
      (:builtin-call :function-call :recur :println)
      (indexed (vec (rest form)) (:args instruction) 1 :args)
      :throw [[(second form) (:value instruction) (conj source-path 1) (conj plan-path :value)]]
      :do (indexed (vec (rest form)) (:body instruction) 1 :body)
      :if (pairs (vec (rest form)) (mapv instruction [:test :then :else])
                 [[1] [2] [3]] [[:test] [:then] [:else]])
      (:let :loop)
      (into (pairs (vec (take-nth 2 (rest (second form)))) (mapv :expr (:bindings instruction))
                   (mapv #(vector 1 %) (range 1 (count (second form)) 2))
                   (mapv #(vector :bindings % :expr) (range (count (:bindings instruction)))))
            (indexed (vec (drop 2 form)) (:body instruction) 2 :body))
      :vector-literal (indexed form (:items instruction) 0 :items)
      :set-literal (indexed (vec (sort-by pr-str form)) (:items instruction) 0 :items)
      :map-literal
      (let [entries (vec (sort-by (fn [[k v]] (pr-str [k v])) form))]
        (pairs (vec (mapcat identity entries)) (vec (mapcat (juxt :key :value) (:entries instruction)))
               (vec (mapcat (fn [i] [[:map-entry i :key] [:map-entry i :value]]) (range (count entries))))
               (vec (mapcat (fn [i] [[:entries i :key] [:entries i :value]]) (range (count entries))))))
      (require! false :unsupported-instruction))))

(defn- occurrence-map [definition function]
  (require! (= (:params definition) (:params function)) :parameters)
  (require! (= (count (:body definition)) (count (:instructions function))) :body-count)
  (loop [pending (mapv (fn [i form node] [form node [i] [:instructions i]])
                      (range) (:body definition) (:instructions function))
         rows [] positions [] summary {}]
    (require! (<= (+ (count pending) (reduce + 0 (vals summary))) 262144) :walk-bound)
    (if (empty? pending) {:uses rows :instruction-occurrences positions :instructions summary}
      (let [[form instruction source-path plan-path] (peek pending)
            op (:op instruction) callee (:function instruction)
            call? (contains? #{:builtin-call :function-call} op)]
        (when call? (require! (and (seq? form) (= callee (first form))) :callee))
        (recur (into (pop pending) (children form instruction source-path plan-path))
               (cond-> rows call? (conj {:op op :callee callee :arity (count (:args instruction))
                                        :source-expression-path source-path :plan-path plan-path}))
               (conj positions [op source-path plan-path (b/form-kind form)])
               (update summary op (fnil inc 0)))))))

(defn- validate-source-leaves! [definition function literals]
  (loop [pending (mapv (fn [i form node] [form node [i] [:instructions i]])
                      (range) (:body definition) (:instructions function)) counts {}]
    (if (empty? pending) counts
      (let [[form instruction source-path plan-path] (peek pending) op (:op instruction)]
        (case op
          :local (require! (and (symbol? form) (= form (:name instruction))) :local-name)
          :literal (let [constant? (symbol? form)]
                     (when constant? (require! (contains? literals form) :literal-definition))
                     (require! (= (if constant? (get literals form) form) (:value instruction)) :literal-value))
          :quote (require! (and (= 2 (count form)) (= (second form) (:value instruction))) :quote)
          nil)
        (recur (into (pop pending) (children form instruction source-path plan-path))
               (if (contains? #{:literal :local :quote} op) (update counts op (fnil inc 0)) counts))))))

(defn- build-plan [relative text emitter]
  (let [source (path relative)]
    (case relative
      "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"
      (:plan (b/sh03-reader-build-plan!
              "<sh07-c01>" {:source-path source :bytes (.getBytes text java.nio.charset.StandardCharsets/UTF_8)
                            :source-byte-count (:bytes (file-id relative))
                            :source-content-hash (:sha256 (file-id relative))}))
      "bootstrap/gravity/src/gravity/bootstrap/syntax.gravity" (:plan (b/sh04-syntax-build-binding! "<sh07-c01>"))
      "bootstrap/gravity/src/gravity/macro.gravity" (:plan (b/sh05-macro-build-binding! "<sh07-c01>"))
      "bootstrap/gravity/src/gravity/resolution.gravity" (:plan (b/sh06-resolution-build-binding! "<sh07-c01>"))
      "bootstrap/gravity/src/gravity/checked_core.gravity" (:plan (b/sh07-core-build-binding!))
      ;; Same raw seed plan as SH02 source binding before its byte-count augmentation.
      (b/p15-s23-stage2-compiler-artifact-plan emitter source text))))

(defn- owner-map [relative emitter]
  (println {:c01-phase :compile-owner :source relative}) (flush)
  (let [input (file-id relative) text (slurp (path relative) :encoding "UTF-8")
        records (b/read-source-form-records (path relative) text)
        macro (b/macro-source-artifact-from-records (path relative) text records)
        definitions (b/stage0-function-table (assoc (:module macro) :forms (:expanded-forms macro)))
        literals (b/p15-s23-stage2-module-literals (:expanded-forms macro))
        plan (build-plan relative text emitter)
        spans (into {} (keep (fn [record]
                              (let [form (:form record)]
                                (when (and (seq? form) (= 'defn (first form))) [(second form) (:span record)])))) records)
        functions (mapv (fn [[name function]]
                          (let [definition (get definitions name) joined (occurrence-map definition function)]
                            (require! (and definition (get spans name)) :original-definition)
                            (merge (function-signature definition function)
                                   {:function name :definition-span (get spans name)
                                    :definition-hash (hash-value definition) :function-plan-hash (hash-value function)} joined)))
                        (:functions plan))]
    (require! (= input (file-id relative)) :source-drift)
    (require! (= (:sha256 input) (get-in plan [:source :sha256])) :source-plan-hash)
    {:input input :plan plan :definitions definitions :spans spans :literals literals
     :source-declarations
     {:namespace {:form-hash (hash-value (:form (first records))) :span (:span (first records))
                  :normalized-module (:module plan)}
      :data (mapv (fn [record]
                    (let [form (:form record) name (second form)]
                      (require! (and (= 3 (count form)) (contains? literals name)) :data-declaration)
                      {:name name :definition-span (:span record)
                       :initializer-kind (b/form-kind (nth form 2))
                       :initializer-hash (hash-value (nth form 2)) :value-hash (hash-value (get literals name))}))
                  (filter #(and (seq? (:form %)) (= 'def (first (:form %)))) records))}
     :row {:input input :module (:module plan) :plan-id (:plan-id plan) :compiler (:compiler plan)
           :plan-construction (if (= relative (first owner-paths)) :sh02-raw-before-byte-count-binding :owning-builder)
           :functions functions :instruction-summary (:instruction-summary plan)
           :outside-selected-plan (mapv (fn [name] {:function name :definition-span (get spans name)
                                                   :reason :outside-sh03-prefix-closure})
                                       (sort (remove (set (keys (:functions plan))) (keys definitions))))}}))

(defn- validate-owner! [{:keys [input plan row definitions spans literals]}]
  (require! (= input (:input row) (file-id (:path input))) :changed-source)
  (require! (= (:sha256 input) (get-in plan [:source :sha256])) :source-plan-hash)
  (require! (= (:plan-id plan) (:plan-id row)
               ((if (:sh03-reader plan) b/reader-canonical-hash hash-value) (dissoc plan :plan-id))) :plan-identity)
  (require! (and (= (count (:functions plan)) (count (:functions row)))
                 (= (set (keys (:functions plan))) (set (map :function (:functions row))))) :function-population)
  (doseq [function (:functions row)]
    (require! (= (:definition-span function) (get spans (:function function))) :definition-span)
    (require! (= (:definition-hash function) (hash-value (get definitions (:function function)))) :definition-identity)
    (require! (= (select-keys function [:parameters :fixed-arity])
                 (function-signature (get definitions (:function function))
                                     (get-in plan [:functions (:function function)]))) :function-signature)
    (require! (= (:function-plan-hash function) (hash-value (get-in plan [:functions (:function function)]))) :function-identity)
    (require! (= (select-keys function [:uses :instruction-occurrences :instructions])
                 (occurrence-map (get definitions (:function function)) (get-in plan [:functions (:function function)]))) :source-occurrence)
    (validate-source-leaves! (get definitions (:function function)) (get-in plan [:functions (:function function)]) literals)
    (doseq [use (:uses function)]
      (let [node (get-in (get-in plan [:functions (:function function)]) (:plan-path use))]
        (require! (= [(:op use) (:callee use) (:arity use)] [(:op node) (:function node) (count (:args node))]) :instruction-use)
        (when (= :function-call (:op use)) (require! (contains? (:functions plan) (:callee use)) :missing-local-callee))))
    (validate-local-calls! (:uses function) plan definitions))
  true)

(defn- validate-population! [owners]
  (require! (= owner-paths (mapv #(get-in % [:input :path]) owners)) :missing-duplicate-or-reordered-owner)
  (doseq [owner owners] (validate-owner! owner)) true)

(defn- dependency-map []
  (let [seed (io/file (path "bootstrap/clojure/src"))
        inputs (mapv (fn [file]
                       (file-id (str "bootstrap/clojure/src/" (.relativize (.toPath seed) (.toPath file)))))
                     (sort-by str (filter #(.isFile %) (file-seq seed))))]
    {:seed-resource-count (count inputs) :seed-resource-content-hash (hash-value inputs)
     :selected-inputs (mapv file-id ["bootstrap/clojure/src/gravity/bootstrap.clj"
                                    "bootstrap/clojure/src/gravity/bootstrap_impl/loader.clj"
                                    "bootstrap/clojure/src/gravity/bootstrap_impl/load_order.edn"
                                    "bootstrap/gravity/p15_s23/compiler.gravity"
                                    "bootstrap/gravity/p15_s23/emitter.gravity" "deps.edn"])
     :compiler-rule-role :interpreted-data :emitter-role :freshness-not-executed-lowering
     :clojure-version (clojure-version) :java-version (System/getProperty "java.runtime.version")
     :java-vm (System/getProperty "java.vm.name") :classpath (System/getProperty "java.class.path")
     :jvm-arguments (vec (.getInputArguments (java.lang.management.ManagementFactory/getRuntimeMXBean)))}))

(defn- body-source-map [body]
  (loop [pending (mapv (fn [i value] [value [i]]) (range) body) rows [] bindings []]
    (if (empty? pending) {:occurrences rows :binding-patterns bindings}
      (let [[value source-path] (peek pending) list-form? (seq? value)
            op (when list-form? (first value))
            indexed (fn [values start]
                      (mapv (fn [i v] [v (conj source-path i)]) (range start (+ start (count values))) values))
            binding? (contains? '#{let loop} op) bound (when binding? (second value))
            _ (when binding? (require! (and (vector? bound) (even? (count bound))
                                            (every? symbol? (take-nth 2 bound))) :source-binding-pattern))
            children
            (cond
              (and list-form? (= 'quote op)) (do (require! (= 2 (count value)) :source-quote) [])
              (and list-form? (contains? '#{fn var set! try throw match catch finally} op))
              (do (require! false :source-family-unclassified) [])
              binding? (into (mapv (fn [i v] [v (conj source-path 1 i)])
                                  (range 1 (count bound) 2) (take-nth 2 (rest bound)))
                             (indexed (vec (drop 2 value)) 2))
              list-form? (do (require! (symbol? op) :source-operator-unclassified) (indexed (vec (rest value)) 1))
              (map? value) (vec (mapcat (fn [i [k v]] [[k (conj source-path :map-entry i :key)]
                                                       [v (conj source-path :map-entry i :value)]])
                                       (range) (sort-by (fn [[k v]] (pr-str [k v])) value)))
              (set? value) (indexed (vec (sort-by pr-str value)) 0)
              (vector? value) (indexed value 0)
              :else [])]
        (recur (into (pop pending) children) (conj rows [(b/form-kind value) op source-path])
               (if binding? (into bindings (mapv (fn [i name] {:path (conj source-path 1 i) :kind (b/form-kind name)})
                                                 (range 0 (count bound) 2) (take-nth 2 bound))) bindings))))))

(defn full-body-family-map [owners]
  ;; This is a source-body inventory. Unseen special forms fail the inventory,
  ;; not the compiler; their bodies need a contract-specific traversal first.
  (mapv
   (fn [{:keys [input definitions spans row]}]
     (let [selected (set (map :function (:functions row)))
           names (set (keys definitions))
           builtins (set (map :callee (filter #(= :builtin-call (:op %)) (mapcat :uses (:functions row)))))
           special '#{quote if do let loop recur}
           functions (mapv (fn [[name definition]]
                             (merge {:function name :definition-hash (hash-value definition)
                                     :parameters (:params definition) :fixed-arity (:arity definition)
                                     :definition-span (get spans name) :selected-plan? (contains? selected name)}
                                    (body-source-map (:body definition)))) (sort-by (comp str key) definitions))
           summarize (fn [functions]
                       (let [occurrences (mapcat :occurrences functions) operators (frequencies (keep second occurrences))]
                         (require! (every? #(or (contains? names %) (contains? builtins %) (contains? special %))
                                          (keys operators)) :source-call-unclassified)
                         {:functions (count functions) :source-positions (count occurrences)
                          :source-kinds (frequencies (map first occurrences))
                          :core-operators (into (sorted-map) (filter #(contains? special (key %)) operators))
                          :builtin-calls (reduce + 0 (map val (filter #(contains? builtins (key %)) operators)))
                          :local-calls (reduce + 0 (map val (filter #(contains? names (key %)) operators)))
                          :binding-pattern-kinds (frequencies (map :kind (mapcat :binding-patterns functions)))}))
           selected-functions (filterv :selected-plan? functions)
           outside-functions (filterv #(not (:selected-plan? %)) functions)
           function-index (into {} (map (juxt :function identity)) functions)]
       (doseq [function (:functions row)]
         (let [walk (get function-index (:function function))]
           (require! (= (count (:occurrences walk)) (count (:instruction-occurrences function))
                        (count (set (map #(nth % 2) (:occurrences walk))))) :source-position-bijection)
           (require! (= (into {} (map (fn [[kind _ source-path]] [source-path kind]) (:occurrences walk)))
                        (into {} (map (fn [[_ source-path _ kind]] [source-path kind]) (:instruction-occurrences function))))
                     :source-plan-position-kind)))
       {:input input :selected (summarize selected-functions) :outside (summarize outside-functions)
        :outside-functions outside-functions})) owners))

(def observed-owners (atom []))
(def stopped-frontier (atom nil))
(def current-owners
  (delay (let [emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! (path (first owner-paths)) :jvm))]
           (mapv (fn [relative]
                   (try (let [owner (owner-map relative emitter)] (swap! observed-owners conj owner) owner)
                        (catch clojure.lang.ExceptionInfo error
                          (reset! stopped-frontier {:source relative :diagnostic (select-keys (ex-data error)
                              [:id :operator :function :reason :missing-fields :source-span])})
                          (throw error)))) owner-paths))))

(defn source-declaration-and-leaf-map [owners]
  (mapv (fn [owner]
          {:input (:input owner) :source-declarations (:source-declarations owner)
           :leaf-checks (apply merge-with +
                              (map (fn [[name function]]
                                     (validate-source-leaves! (get (:definitions owner) name) function (:literals owner)))
                                   (get-in owner [:plan :functions])))}) owners))

(defn selected-owner-use-map
  ([] (selected-owner-use-map @current-owners))
  ([owners]
  (let [owners owners
        uses (mapcat #(mapcat :uses (get-in % [:row :functions])) owners)]
    ;; An execution record supplies its actual revision. This projection must
    ;; not stamp a new construction with the original map's historical base.
    {:status (if (= 6 (count owners)) :mapped-selected-plans :stopped-subset)
     :stopped-frontier @stopped-frontier
     :scope :selected-seed-plan-potential-use :dependencies (dependency-map)
     :owners (mapv :row owners)
     :instruction-occurrence-fields [:opcode :source-expression-path :plan-path :source-form-kind]
     :instruction-occurrence-order :worklist-visitation-not-evaluation-order
     :builtin-occurrences (count (filter #(= :builtin-call (:op %)) uses))
     :used-builtins (vec (sort (set (map :callee (filter #(= :builtin-call (:op %)) uses)))))
     :primitive-obligations
     (mapv (fn [callee] {:primitive callee :implementation-owner :temporary-seed-runtime
                        :status :owning-accepted-rejected-case-join-open
                        :locations :owners-functions-uses :contracts ["L2" "BOOT2"]
                        :existing-check-owner "bootstrap/clojure/test/gravity/self_hosting/sh01_stage2_runtime_iteration_test.clj"})
           (sort (set (map :callee (filter #(= :builtin-call (:op %)) uses)))))
     :governing-contracts ["L2" "C6" "BOOT2" "BOOT8"]
     :existing-check-owners ["sh01_stage2_runtime_iteration_test.clj" "sh03_reader_adapter_test.clj"
                             "sh04_syntax_adapter_test.clj" "sh05_macro_adapter_test.clj"
                             "sh06_resolution_adapter_test.clj" "sh07_checked_core_test.clj"]
     :open-obligations [:runtime-primitive-accepted-rejected-cases :actual-invocation-reachability
                        :source-to-core-product-joins :external-toolchain-migration-identity]
     :nonclaims [:all-functions-executed :full-reader-compiled :gravity-compiled :core-closure
                 :checked-types-effects :native :migration :self-hosting :release]})))

(deftest actual-selected-owner-seed-plans-and-primitive-occurrences
  (let [owners @current-owners report (selected-owner-use-map)]
    (is (validate-population! owners))
    (is (= 6 (count (:owners report))))
    (is (not (contains? report :base-revision)))
    (is (pos? (:builtin-occurrences report)))
    (doseq [owner owners]
      (is (= (:instruction-summary (:plan owner))
             (apply merge-with + (map :instructions (get-in owner [:row :functions])))))
      (let [functions (get-in owner [:row :functions]) rows (mapcat :instruction-occurrences functions)]
        (is (= (:instruction-summary (:plan owner)) (frequencies (map first rows))))
        (doseq [function functions]
          (let [positions (:instruction-occurrences function)]
            (require! (= (count positions) (count (set (map second positions)))
                         (count (set (map #(nth % 2) positions)))) :instruction-position-bijection)))))))

(deftest changed-missing-duplicate-and-forged-plan-joins-fail-closed
  (let [owners @current-owners owner (first owners)]
    (doseq [changed [(subvec owners 1) (assoc owners 1 owner)
                     (assoc-in owners [0 :input :sha256] "sha256:changed")
                     (assoc-in owners [0 :plan :plan-id] "sha256:changed")
                     (assoc-in owners [0 :row :functions 0 :function-plan-hash] "sha256:changed")
                     (assoc-in owners [0 :row :functions 0 :definition-hash] "sha256:changed")
                     (update-in owners [0 :row :functions 0 :fixed-arity] inc)
                     (update-in owners [0 :row :functions 0 :parameters] #(conj % 'forged))
                     (update-in owners [0 :row :functions] #(conj % (first %)))
                     (update-in owners [0 :row :functions 0 :instruction-occurrences] pop)
                     (update-in owners [0 :row :functions 0 :instruction-occurrences] #(conj % (first %)))
                     (assoc-in owners [0 :row :functions 0 :instruction-occurrences 0 0] :unowned)
                     (assoc-in owners [0 :row :functions 0 :instruction-occurrences 0 1] [:wrong-source-position])
                     (assoc-in owners [0 :row :functions 0 :instruction-occurrences 0 2] [:instructions 999999])]]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"C01 source/plan join rejected" (validate-population! changed))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"C01 source/plan join rejected"
                          (children '(unowned) {:op :unowned} [] [])))
    (is (= [] (children '(quote (not-a-primitive)) {:op :quote :value '(not-a-primitive)} [] [])))))

(deftest instruction-positions-stop-at-quoted-data-and-retain-reference-kind
  (let [payload '(unresolved/call (throw unknown))
        quoted (occurrence-map {:params [] :body [(list 'quote payload)]}
                               {:params [] :instructions [{:op :quote :value payload}]})
        constant (occurrence-map {:params [] :body ['catalog]}
                                 {:params [] :instructions [{:op :literal :value payload}]})]
    (is (= [] (:uses quoted)))
    (is (= [[:quote [0] [:instructions 0] :list]] (:instruction-occurrences quoted)))
    (is (= {:quote 1} (:instructions quoted)))
    (is (= [[:literal [0] [:instructions 0] :symbol]] (:instruction-occurrences constant)))
    (is (= [] (:uses constant)))))

(deftest local-call-signatures-join-real-seed-declarations
  ;; This checks C01 records against seed plans, not B48 compiler legality.
  (let [emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! (path (first owner-paths)) :jvm))]
    (doseq [[arguments expected] [["1 2" nil] ["1" :local-call-arity] ["1 2 3" :local-call-arity]]]
      (let [source "sh07-c01-signature-case.gravity"
            text (str "(ns sh07.c01.signatures (:profile :meta) (:target :jvm) (:exports [target caller]))\n"
                      "(defn target [left right] left)\n(defn caller [] (target " arguments "))\n")
            records (b/read-source-form-records source text)
            macro (b/macro-source-artifact-from-records source text records)
            definitions (b/stage0-function-table (assoc (:module macro) :forms (:expanded-forms macro)))
            plan (b/p15-s23-stage2-compiler-artifact-plan emitter source text)
            target (get-in plan [:functions 'target])
            uses (:uses (occurrence-map (get definitions 'caller) (get-in plan [:functions 'caller])))
            failure (try (validate-local-calls! uses plan definitions) nil
                         (catch clojure.lang.ExceptionInfo error (ex-data error)))]
        (is (= {:parameters '[left right] :fixed-arity 2} (function-signature (get definitions 'target) target)))
        (is (= expected (:reason failure)))
        (when expected (is (= "SH07-C01-JOIN" (:id failure))))))))

(deftest full-source-body-families-retain-uncompiled-reader-definitions
  (let [owners @current-owners families (full-body-family-map owners)]
    (is (= owner-paths (mapv #(get-in % [:input :path]) families)))
    (is (= [964 44 61087 455]
           (mapv (fn [ks] (reduce + (map #(get-in % ks) families)))
                 [[:selected :functions] [:outside :functions] [:selected :source-positions] [:outside :source-positions]])))
    (doseq [[owner family] (map vector owners families)]
      (is (= (reduce + (vals (:instruction-summary (:plan owner)))) (get-in family [:selected :source-positions])))
      (is (= (set (map :function (get-in owner [:row :outside-selected-plan])))
             (set (map :function (:outside-functions family)))))
      (is (every? #(and (:definition-span %) (:definition-hash %)
                       (= (:fixed-arity %) (count (:parameters %)))) (:outside-functions family))))
    (is (= 0 (reduce + (map #(get-in % [:outside :builtin-calls]) families))))
    (is (= 35 (reduce + (map #(get-in % [:outside :local-calls]) families))))))

(deftest source-body-family-walk-distinguishes-bindings-and-quoted-data
  (let [body '[(let [x (quote (try (set! x bad)))]
                {:result x :data (quote (fn [x] (match x)))})]
        walk (body-source-map body) rows (:occurrences walk)]
    (is (= {'let 1 'quote 2} (frequencies (keep second rows))))
    (is (= [{:path [0 1 0] :kind :symbol}] (:binding-patterns walk)))
    (is (= 1 (count (filter #(= :symbol (first %)) rows))))
    (is (not-any? #(= [0 1 0] (nth % 2)) rows))
    (doseq [form '[(quote) (quote x y) (let [x] x) (let [[x] []] x) (fn [x] x) ((factory) 1)]]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"C01 source/plan join rejected" (body-source-map [form]))))))

(deftest all-source-data-declarations-and-selected-leaves-are-joined
  (let [owners @current-owners rows (source-declaration-and-leaf-map owners)]
    (is (= owner-paths (mapv #(get-in % [:input :path]) rows)))
    (is (= 37 (reduce + (map #(count (get-in % [:source-declarations :data])) rows))))
    (is (= {:literal 21902 :local 14750 :quote 78} (apply merge-with + (map :leaf-checks rows))))
    (doseq [[owner row] (map vector owners rows)]
      (is (= (get-in owner [:plan :module]) (get-in row [:source-declarations :namespace :normalized-module])))
      (is (get-in row [:source-declarations :namespace :span]))
      (is (= (set (keys (:literals owner))) (set (map :name (get-in row [:source-declarations :data]))))))))

(deftest coherently-resealed-real-source-leaf-substitutions-are-rejected
  (let [owner (first @current-owners)
        options (for [[i function] (map-indexed vector (get-in owner [:row :functions]))
                      [op _ plan-path _] (:instruction-occurrences function)
                      :let [node (get-in (:plan owner) (into [:functions (:function function)] plan-path))]
                      :when (or (and (= :literal op) (boolean? (:value node)))
                                (and (= :local op) (some #{(:name node)} (:parameters function))
                                     (< 1 (count (:parameters function)))))]
                  {:index i :function (:function function) :op op :path plan-path
                   :field (if (= :local op) :name :value)
                   :replacement (if (= :local op) (first (remove #{(:name node)} (:parameters function))) (not (:value node)))})]
    (is (validate-owner! owner))
    (doseq [[op reason] [[:literal :literal-value] [:local :local-name]]]
      (let [case (first (filter #(= op (:op %)) options))
            _ (require! case :actual-leaf-case-absent)
            plan (assoc-in (:plan owner) (into [:functions (:function case)] (conj (:path case) (:field case))) (:replacement case))
            plan (assoc plan :plan-id (hash-value (dissoc plan :plan-id)))
            changed (-> owner (assoc :plan plan) (assoc-in [:row :plan-id] (:plan-id plan))
                        (assoc-in [:row :functions (:index case) :function-plan-hash] (hash-value (get-in plan [:functions (:function case)]))))
            rejection (try (validate-owner! changed) nil (catch clojure.lang.ExceptionInfo error (ex-data error)))]
        (is (not= (:plan-id (:plan owner)) (:plan-id plan)))
        (is (= (:input owner) (:input changed)))
        (is (= (get-in owner [:row :functions (:index case) :definition-hash])
               (get-in changed [:row :functions (:index case) :definition-hash])))
        (is (= (get-in owner [:row :functions (:index case) :instruction-occurrences])
               (get-in changed [:row :functions (:index case) :instruction-occurrences])))
        (is (= ["SH07-C01-JOIN" reason] ((juxt :id :reason) rejection)))))))

(deftest selected-owner-observed-arity-runtime-cases
  ;; The reviewed map supplies actual required arities, not runtime authority.
  ;; Normal values below exercise those arities; source value domains stay open.
  (let [report (edn/read-string (slurp (path "docs/artifacts/phase-15/reports/sh07-c01-selected-owner-use-map-v1.edn")))
        uses (mapcat #(mapcat :uses (:functions %)) (:owners report))
        required (set (map (juxt :callee :arity) (filter #(= :builtin-call (:op %)) uses)))
        plan {:compiler-artifact-plan? true :kind :gravity/stage2-compiler-artifact-plan
              :module {:profile :meta} :compiler {:stage :p15-s23-stage2-expression-lowering}
              :source {:path "sh07-c01-observed-arities.gravity"}}
        generic #(b/p15-s23-stage2-runtime-invoke-builtin plan %1 %2)
        interpreted #(b/p15-s23-stage2-runtime-execute-instruction
                       {:engine :sh07-c01-observed-arities} plan {}
                       {:op :builtin-call :function %1 :args (mapv (fn [value] {:op :literal :value value}) %2)})
        failure (fn [thunk] (try (thunk) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))
        assoc-cases (mapv (fn [arity]
                            (let [entries (mapv (fn [i] [(keyword (str "slot" i)) i]) (range (quot (dec arity) 2)))]
                              ['assoc (into [{:retained :before}] (mapcat identity entries))
                               (into {:retained :before} entries)])) [7 9 19 21])
        cases (into [['<= [1 2] true] ['<= [2 1] false] ['> [2 1] true] ['> [1 2] false]
                     ['>= [2 2] true] ['>= [1 2] false] ['conj [[] :new] [:new]]
                     ['conj [#{:seen} :new] #{:seen :new}] ['conj ['() :new] '(:new)] ['list [] '()]] assoc-cases)]
    (doseq [owner (:owners report)] (is (= (:input owner) (file-id (get-in owner [:input :path])))))
    (is (= '#{[<= 2] [> 2] [>= 2] [assoc 7] [assoc 9] [assoc 19] [assoc 21] [conj 2] [list 0]}
           (set (map (fn [[callee args]] [callee (count args)]) cases))))
    (doseq [[callee args expected] cases]
      (is (contains? required [callee (count args)]) [callee :not-an-observed-source-arity])
      (is (= expected (generic callee args)) [callee args :generic])
      (is (= expected (interpreted callee args)) [callee args :interpreted]))
    (doseq [[callee args] (into [['<= [7]] ['> [7]] ['>= [7]] ['conj []]]
                               (mapv (fn [[_ args]] ['assoc (conj args :dangling)]) assoc-cases))]
      (let [direct (failure #(generic callee args)) lowered (failure #(interpreted callee args))]
        (is (= "L2-BUILTIN-ARITY" (:id direct)) [callee args])
        (is (= direct lowered) [callee :diagnostic-parity])
        (is (= (count args) (:actual-arity direct)) [callee :actual-arity])))
    (let [direct (failure #(generic 'conj [7 :new])) lowered (failure #(interpreted 'conj [7 :new]))]
      (is (= "L2-BUILTIN-ERROR" (:id direct)))
      (is (= direct lowered)))))
