(ns gravity.self-hosting.stage2-module-map-order-test
  (:require [clojure.java.io :as io] [clojure.test :refer [deftest is]] [gravity.bootstrap :as bootstrap]))

;; L2 expression/diagnostic order is distinct from STD3 Map enumeration.
;; Temporary seed module development only; no checked-effect or admission claim.
(defn- diagnostic [f] (try (f) nil (catch clojure.lang.ExceptionInfo e (ex-data e))))
(defn- fixture [name extension]
  (loop [root (.getParentFile (io/file (.toURI (io/resource "gravity/self_hosting/stage2_module_map_order_test.clj"))))]
    (if (.isFile (io/file root "deps.edn"))
      (.getPath (io/file root (str "bootstrap/clojure/fixtures/self-hosting/sh-08/modules/" name extension)))
      (recur (.getParentFile root)))))

(defn- normalized-source [body source-keys]
  ;; Supplied-record kernel, NOT a genuine C5 artifact or verifier substitute.
  (let [records (atom []) counter (atom 0)
        form (list 'defn 'main [] body)]
    (letfn [(tree [value parent]
              (let [id (keyword (str "form-" (swap! counter inc)))
                    kind (cond (map? value) :map (set? value) :set (vector? value) :vector
                               (seq? value) :list (keyword? value) :keyword (symbol? value) :symbol :else :literal)
                    values (cond (map? value) (mapcat #(vector % (get value %))
                                                      (if (= (set source-keys) (set (keys value))) source-keys (keys value)))
                                 (coll? value) value :else [])
                    children (mapv #(tree % id) values)]
                (swap! records conj {:form-id id :parent-form-id parent :kind kind :value value :children children}) id))]
      (let [root (tree form nil) syntax {:syntax/id :root :source {:form-id root}
                                        :form (list 'def 'main (list 'fn [] body))}]
        {:source-path "normalized-map.gravity" :functions {'main {:name 'main :params [] :body [body]}}
         :syntax [syntax] :artifact {:sh05-macro-artifact
                                    {:macro-expansion-trace [{:macro 'defn :output-syntax-id :root}]
                                     :gravity-macro-boundary {:authenticated-sh04-artifact
                                                              {:c2-reader-artifact {:form-tree @records}}}}}}))))
(defn- context [source]
  {:name 'main :source-path (:source-path source) :p15-s23-sh07-enclosing-function 'main
   :p15-s23-sh07-callsite-path [0] :p15-s23-stage2-module-routes {}
   :p15-s23-stage2-map-order (bootstrap/p15-s23-stage2-module-map-order source)})
(def ^:private emitter {:special-form-rules {'println {:op :println} 'do {:op :do} 'quote {:op :quote}}})

(deftest stage2-module-map-order-fixture-parity
  (doseq [name ["module-map-order-root" "module-map-order-leaf"]]
    (let [g (fixture name ".gravity") q (fixture name ".qst")]
      (is (= (slurp g) (slurp q)))
      (is (= (bootstrap/read-forms g (slurp g)) (bootstrap/read-forms q (slurp q)))))))

(deftest stage2-module-map-order-development-kernel
  (let [keys [:z :b :y :a :x :c :w :d :v]
        pairs (mapv #(vector % (list 'do (list 'println (name %)) %)) keys)]
    (doseq [value [(into {} pairs) (into (sorted-map) pairs) (apply array-map (mapcat identity pairs))]]
      (let [source (normalized-source value keys) module (context source)
            compiled (bootstrap/p15-s23-stage2-seed-compile-collection emitter module #{} value)]
        (is (= keys (mapv #(get-in % [:key :value]) (:entries compiled))))
        (is (= keys (mapv #(get-in % [:value :body 1 :value]) (:entries compiled))))
        (is (= (zipmap keys keys) (into {} (map (fn [entry] [(get-in entry [:key :value]) (get-in entry [:value :body 1 :value])])) (:entries compiled))))
        (is (= :map-source-order-mismatch
               (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-map-entries
                                    (assoc-in module [:p15-s23-stage2-map-order ['main [0]] :entry-order] [0 0]) value [0])) [:facts :reason])))))
    (let [value {:z :last :a :first} source (normalized-source value [:z :a])
          path [:artifact :sh05-macro-artifact :gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :form-tree]
          records (get-in source path) index (first (keep-indexed #(when (= :map (:kind %2)) %1) records))
          map-record (get records index) children (:children map-record)
          key-index (first (keep-indexed #(when (= (nth children 2) (:form-id %2)) %1) records))]
      (doseq [[label altered] [[:missing (assoc-in source (into path [index :children 0]) :missing)]
                               [:duplicate-child (assoc-in source (into path [index :children]) [(first children) (first children)])]
                               [:collapsed-key (assoc-in source (into path [key-index :value]) (:value (first (filter #(= (first children) (:form-id %)) records))))]]]
        (is (= "BOOT2006" (:id (diagnostic #(context altered)))) label))
      (is (= :map-source-distinct-literal-key-bijection
             (get-in (diagnostic #(context (normalized-source {0 true} []))) [:facts :reason]))))
    (let [quoted '(quote {:z (println "never") :a (leaf/hidden :data)}) source (normalized-source quoted [:z :a])]
      (is (= {} (:p15-s23-stage2-map-order (context source))))
      (is (= {:op :quote :value (second quoted)}
             (bootstrap/p15-s23-stage2-seed-compile-expr emitter (context source) #{} quoted))))
    (doseq [wrap [(fn [value] (list 'let ['x value] 'x))
                  (fn [value] (list 'loop ['x value] 'x)) vector
                  (fn [value] (list 'quote value))]]
      (let [value (wrap {:z :last :a :first}) source (normalized-source value [:z :a])
            module (context source) rows (vals (:p15-s23-stage2-map-order module))
            compiled (bootstrap/p15-s23-stage2-seed-compile-expr emitter module #{} value)
            maps (filter #(= :map-literal (:op %))
                         (bootstrap/p15-s23-stage2-module-instructions {:functions {'main {:instructions [compiled]}}}))]
        (is (every? #(= [:z :a] (mapv (fn [slot] (nth (:host-keys %) slot)) (:entry-order %))) rows))
        (is (= (if (and (seq? value) (= 'quote (first value))) 0 1) (count rows) (count maps)))
        (is (every? #(= [:z :a] (mapv (fn [entry] (get-in entry [:key :value])) (:entries %))) maps))))
    (doseq [body ['{:key (leaf/value)} '#{(leaf/value)} '(fn [] (leaf/value))]]
      (is (= :unsupported-nested-module-call
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-call-sites {:name 'main :params [] :body [body]})) [:facts :reason]))))
    (let [module {:p15-s23-sh07-callsite-path [99]} seen (atom []) value {:z :last :a :first}]
      (with-redefs [bootstrap/p15-s23-stage2-seed-compile-expr
                    (fn [_ actual _ item] (swap! seen conj actual) {:op :literal :value item})]
        (is (= [:a :z] (mapv #(get-in % [:key :value])
                             (:entries (bootstrap/p15-s23-stage2-seed-compile-collection emitter module #{} value))))))
      (is (every? #(identical? module %) @seen)))))

(deftest stage2-module-map-order-diagnostic-kernel
  (doseq [[value keys expected] [[{:a '(recur) :z '(throw)} [:z :a] "L2-THROW-ILLEGAL"]
                                  [{:a '(throw) :z '(recur)} [:z :a] "L2-RECUR-TARGET"]]]
    (let [body (list 'do value '(loop [x] x)) source (normalized-source body keys)
          module (context source) definition (merge module (get-in source [:functions 'main]))
          compiled (diagnostic #(bootstrap/p15-s23-stage2-seed-compile-function emitter module definition))
          original (diagnostic #(bootstrap/p15-s23-stage2-seed-validate-tail-body! definition [body] 0 true))
          walker (diagnostic #(bootstrap/p15-s23-stage2-module-call-sites definition))]
      (is (= expected (:id compiled)))
      (is (= compiled original walker))
      (is (= "normalized-map.gravity" (get-in compiled [:source-span :source]))))))

(deftest stage2-module-map-order-authenticated-composition
  ;; Only G constructs actual C5/bundles; Q has parser parity above.
  (let [leaf-path (fixture "module-map-order-leaf" ".gravity") leaf-text (slurp leaf-path)
        path (fixture "module-map-order-root" ".gravity") text (slurp path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        _ (println :map-order-phase :leaf-c5) leaf (bootstrap/compiler-c5-resolution-file-artifact leaf-path)
        _ (println :map-order-phase :root-c5)
        caller (bootstrap/compiler-c5-resolution-file-artifact path {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [leaf]})
        _ (println :map-order-phase :leaf-bundle)
        leaf-bundle (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter leaf-path leaf-text
                     {:caller-resolution-artifact leaf :dependency-plans []})
        independent (:plan (first (:modules leaf-bundle)))
        legacy (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter leaf-path leaf-text)
        inputs {:caller-resolution-artifact caller :dependency-plans [independent]}
        _ (println :map-order-phase :root-bundle)
        bundle (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path text inputs)
        root (first (filter #(= (:root-module-id bundle) (:module-id %)) (:modules bundle)))]
    (is (= :accepted (:status leaf) (:status caller)))
    (is (= [] (:routes leaf-bundle)))
    (is (not= independent legacy))
    (is (= independent (:plan (first (filter #(= (:artifact-id leaf) (:module-id %)) (:modules bundle))))))
    (is (= 1 (count (:routes bundle))))
    (is (= :not-checked (get-in bundle [:boundary :effects])))
    (is (= (str "sha256:" (bootstrap/sha256-hex text)) (get-in root [:plan :source :sha256])))
    (let [result (atom nil) _ (println :map-order-phase :runtime)
          output (with-out-str (reset! result (bootstrap/p15-s23-stage2-runtime-execute-function
                                               {:engine :map-order-test} bundle 'main [])))]
      (is (= [:v :z :a '{:inert (println "NEVER") :private (leaf/hidden :data)}] @result))
      (is (= "leaf-z\nleaf-b\nleaf-y\nleaf-a\nleaf-x\nleaf-c\nleaf-w\nleaf-d\nleaf-v\nroot-z\nroot-a\n" output)))
    (is (= :independent-raw-plan-mismatch
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path text
                                (assoc inputs :dependency-plans [legacy]))) [:facts :reason])))
    (let [entry-index (first (keep-indexed #(when (= (:artifact-id leaf) (:module-id %2)) %1) (:modules bundle)))
          plan-path [:modules entry-index :plan] plan (get-in bundle plan-path)
          instructions (get-in plan [:functions 'packet :instructions])
          map-path [:functions 'packet :instructions 0 :body 0 :body 0 :else]
          changed-plan (update-in plan (conj map-path :entries) #(vec (reverse %)))
          base (dissoc changed-plan :plan-id)
          changed-plan (assoc base :plan-id (str "sha256:" (bootstrap/sha256-hex (pr-str (bootstrap/c-backend-canonical-value base)))))
          changed (assoc-in bundle plan-path changed-plan)
          identity (bootstrap/p15-s23-stage2-module-identity emitter (:root-module-id bundle) (:modules changed) (:routes changed))
          forged (assoc changed :identity-input identity :bundle-id (bootstrap/reader-canonical-hash identity))
          failure (atom nil) output (with-out-str (reset! failure (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function
                                                                              {:engine :map-order-test} forged 'main []))))]
      (is (seq instructions))
      (is (= :map-literal (get-in plan (conj map-path :op))))
      (is (not= (:plan-id plan) (:plan-id changed-plan)))
      (is (= :independent-raw-plan-mismatch (get-in @failure [:facts :reason])))
      (is (= "" output)))))
