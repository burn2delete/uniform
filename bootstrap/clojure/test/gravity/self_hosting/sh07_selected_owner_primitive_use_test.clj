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
         rows [] summary {}]
    (require! (<= (+ (count pending) (reduce + 0 (vals summary))) 262144) :walk-bound)
    (if (empty? pending) {:uses rows :instructions summary}
      (let [[form instruction source-path plan-path] (peek pending)
            op (:op instruction) callee (:function instruction)
            call? (contains? #{:builtin-call :function-call} op)]
        (when call? (require! (and (seq? form) (= callee (first form))) :callee))
        (recur (into (pop pending) (children form instruction source-path plan-path))
               (cond-> rows call? (conj {:op op :callee callee :arity (count (:args instruction))
                                        :source-expression-path source-path :plan-path plan-path}))
               (update summary op (fnil inc 0)))))))

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
        plan (build-plan relative text emitter)
        spans (into {} (keep (fn [record]
                              (let [form (:form record)]
                                (when (and (seq? form) (= 'defn (first form))) [(second form) (:span record)])))) records)
        functions (mapv (fn [[name function]]
                          (let [definition (get definitions name) joined (occurrence-map definition function)]
                            (require! (and definition (get spans name)) :original-definition)
                            (merge {:function name :definition-span (get spans name)
                                    :definition-hash (hash-value definition) :function-plan-hash (hash-value function)} joined)))
                        (:functions plan))]
    (require! (= input (file-id relative)) :source-drift)
    (require! (= (:sha256 input) (get-in plan [:source :sha256])) :source-plan-hash)
    {:input input :plan plan :definitions definitions :spans spans
     :row {:input input :module (:module plan) :plan-id (:plan-id plan) :compiler (:compiler plan)
           :plan-construction (if (= relative (first owner-paths)) :sh02-raw-before-byte-count-binding :owning-builder)
           :functions functions :instruction-summary (:instruction-summary plan)
           :outside-selected-plan (mapv (fn [name] {:function name :definition-span (get spans name)
                                                   :reason :outside-sh03-prefix-closure})
                                       (sort (remove (set (keys (:functions plan))) (keys definitions))))}}))

(defn- validate-owner! [{:keys [input plan row definitions spans]}]
  (require! (= input (:input row) (file-id (:path input))) :changed-source)
  (require! (= (:sha256 input) (get-in plan [:source :sha256])) :source-plan-hash)
  (require! (= (:plan-id plan) (:plan-id row)
               ((if (:sh03-reader plan) b/reader-canonical-hash hash-value) (dissoc plan :plan-id))) :plan-identity)
  (require! (and (= (count (:functions plan)) (count (:functions row)))
                 (= (set (keys (:functions plan))) (set (map :function (:functions row))))) :function-population)
  (doseq [function (:functions row)]
    (require! (= (:definition-span function) (get spans (:function function))) :definition-span)
    (require! (= (:function-plan-hash function) (hash-value (get-in plan [:functions (:function function)]))) :function-identity)
    (require! (= (select-keys function [:uses :instructions])
                 (occurrence-map (get definitions (:function function)) (get-in plan [:functions (:function function)]))) :source-occurrence)
    (doseq [use (:uses function)]
      (let [node (get-in (get-in plan [:functions (:function function)]) (:plan-path use))]
        (require! (= [(:op use) (:callee use) (:arity use)] [(:op node) (:function node) (count (:args node))]) :instruction-use)
        (when (= :function-call (:op use)) (require! (contains? (:functions plan) (:callee use)) :missing-local-callee)))))
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

(defn selected-owner-use-map
  ([] (selected-owner-use-map @current-owners))
  ([owners]
  (let [owners owners
        uses (mapcat #(mapcat :uses (get-in % [:row :functions])) owners)]
    {:base-revision "0067e9418de992900a95cd47d016029af62ff99c"
     :status (if (= 6 (count owners)) :mapped-selected-plans :stopped-subset)
     :stopped-frontier @stopped-frontier
     :scope :selected-seed-plan-potential-use :dependencies (dependency-map)
     :owners (mapv :row owners)
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
    (is (pos? (:builtin-occurrences report)))
    (doseq [owner owners]
      (is (= (:instruction-summary (:plan owner))
             (apply merge-with + (map :instructions (get-in owner [:row :functions]))))))))

(deftest changed-missing-duplicate-and-forged-plan-joins-fail-closed
  (let [owners @current-owners owner (first owners)]
    (doseq [changed [(subvec owners 1) (assoc owners 1 owner)
                     (assoc-in owners [0 :input :sha256] "sha256:changed")
                     (assoc-in owners [0 :plan :plan-id] "sha256:changed")
                     (assoc-in owners [0 :row :functions 0 :function-plan-hash] "sha256:changed")
                     (update-in owners [0 :row :functions] #(conj % (first %)))]]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"C01 source/plan join rejected" (validate-population! changed))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"C01 source/plan join rejected"
                          (children '(unowned) {:op :unowned} [] [])))
    (is (= [] (children '(quote (not-a-primitive)) {:op :quote :value '(not-a-primitive)} [] [])))))

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
