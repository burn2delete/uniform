(ns gravity.c6-core-lowering-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is testing]]
            [gravity.c6-core-lowering :as c6]
            [gravity.c6-core-lowering.context :as context]
            [gravity.c6-core-lowering.lowering :as lowering]))

(def module
  {:module 'gravity.c6-test
   :source-path "c6-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{}
   :safety :safe
   :metadata {}})

(def c5-artifact
  {:kind :gravity/stage0-c5-name-resolution-artifact
   :artifact-id "sha256:c5"
   :namespace-analysis {:namespace 'gravity.c6-test}
   :binding-table {:bindings [{:binding-id "binding-0"}]}
   :alias-table {}
   :dependency-graph {:nodes [] :edges []}})

(defn syntax
  [id form]
  {:syntax-id id
   :form form
   :span {:source "c6-test.gravity" :form-index id}
   :origin :source
   :generated-origin []
   :metadata {}})

(defn- recursive-flatten-oracle [node]
  ;; Original algorithm, including override dispatch and returned child vectors.
  (vec (cons node
             (mapcat #(context/invoke-op :c6-flatten-core
                                        recursive-flatten-oracle %)
                     (context/invoke-op :c6-core-child-nodes
                                        lowering/c6-core-child-nodes
                                        (:children node))))))

(deftest default-flatten-uses-one-root-accumulator-with-original-products
  (let [leaf (fn [id] {:artifact :gravity/core-node :node-id id
                       :form :literal :children {} :value id})
        a (leaf "a") b (leaf "b")
        branch {:artifact :gravity/core-node :node-id "branch" :form 'do
                :children (array-map :left a :right [b a])}
        chain (reduce (fn [child index]
                        {:artifact :gravity/core-node :node-id (str "chain-" index)
                         :form 'do :children {:body [child]}})
                      branch (range 17))
        original-invoke context/invoke-op]
    ;; Fixed tiny shapes, including repeated object occurrences: no deduplication.
    (doseq [root [nil a branch chain]
            flatten [lowering/c6-flatten-core c6/c6-flatten-core]]
      (let [expected (recursive-flatten-oracle root)
            calls (atom [])
            actual (with-redefs [context/invoke-op
                                (fn [key fallback & args]
                                  (when (= :c6-flatten-core key)
                                    (swap! calls conj (first args)))
                                  (apply original-invoke key fallback args))]
                     (flatten root))]
        (is (vector? actual))
        (is (= expected actual))
        (is (every? true? (map identical? expected actual)))
        (is (empty? @calls))
        (is (identical? root (first actual)))))
    (let [stream [(syntax "ns" '(ns gravity.c6-test))
                  (assoc (syntax "generated" '(let [x (if true 1 2)] (do x 3)))
                         :generated-origin [{:macro 'example :syntax-id "macro-0"}])
                  (syntax "plain" '(def answer 42))]
          build #(c6/c6-lowering-artifact "c6-test.gravity" module c5-artifact stream)
          expected (c6/with-operations {:c6-flatten-core recursive-flatten-oracle} build)
          calls (atom [])
          actual (with-redefs [context/invoke-op
                              (fn [key fallback & args]
                                (when (= :c6-flatten-core key)
                                  (swap! calls conj (:node-id (first args))))
                                (apply original-invoke key fallback args))]
                   (build))
          roots (get-in actual [:core-ast-module :roots])]
      (is (= expected actual))
      (is (= (:artifact-id expected) (:artifact-id actual)))
      (is (= (concat roots roots) @calls))
      (is (= :passed (get-in actual [:core-verifier-report :status])))
      (is (= (:c5-name-resolution-artifact expected)
             (:c5-name-resolution-artifact actual)))
      (is (= (:desugaring-trace expected) (:desugaring-trace actual))))))

(deftest leaf-var-rebinding-retains-original-recursive-semantics
  (let [a {:artifact :gravity/core-node :node-id "a" :form :literal :children {}}
        b {:artifact :gravity/core-node :node-id "b" :form :literal :children {}}
        root {:artifact :gravity/core-node :node-id "root" :form 'do
              :children (array-map :first a :nested [b a])}
        replacement (assoc b :node-id "replacement")
        original-flatten lowering/c6-flatten-core
        original-children lowering/c6-core-child-nodes
        failure (ex-info "original leaf callback failure" {:id "C6-ORIGIN" :sentinel 19})]
    (doseq [public? [false true] throw? [false true]]
      (let [run (fn [oracle?]
                  (let [events (atom [])
                        algorithm (if oracle? recursive-flatten-oracle original-flatten)
                        callback (fn [node]
                                   (swap! events conj (:node-id node))
                                   (if (= "b" (:node-id node))
                                     (if throw? (throw failure) [replacement])
                                     (algorithm node)))
                        result (try
                                 (if oracle?
                                   (c6/with-operations {:c6-flatten-core callback}
                                     #(if public? (callback root) (algorithm root)))
                                   (with-redefs [lowering/c6-flatten-core callback]
                                     (if public? (c6/c6-flatten-core root)
                                         (original-flatten root))))
                                 (catch clojure.lang.ExceptionInfo error error))]
                    [result @events]))
            [expected expected-events] (run true)
            [actual actual-events] (run false)]
        (is (= expected-events actual-events))
        (if throw?
          (do (is (and (identical? failure expected) (identical? failure actual)))
              (is (= (ex-data expected) (ex-data actual))))
          (do (is (= expected actual))
              (is (every? true? (map identical? expected actual)))))))
    (doseq [public? [false true] throw? [false true]]
      (let [run (fn [oracle?]
                  (let [events (atom [])
                        callback (fn [value]
                                   (swap! events conj value)
                                   (if throw? (throw failure)
                                       (reverse (original-children value))))
                        result (try
                                 (if oracle?
                                   (c6/with-operations {:c6-core-child-nodes callback}
                                     #(recursive-flatten-oracle root))
                                   (with-redefs [lowering/c6-core-child-nodes callback]
                                     (if public? (c6/c6-flatten-core root)
                                         (original-flatten root))))
                                 (catch clojure.lang.ExceptionInfo error error))]
                    [result @events]))
            [expected expected-events] (run true)
            [actual actual-events] (run false)]
        (is (= expected-events actual-events))
        (if throw?
          (do (is (and (identical? failure expected) (identical? failure actual)))
              (is (= (ex-data expected) (ex-data actual))))
          (do (is (= expected actual))
              (is (every? true? (map identical? expected actual)))))))))

(deftest custom-flatten-operations-retain-recursion-results-and-failures
  (let [a {:artifact :gravity/core-node :node-id "a" :form :literal :children {}}
        b {:artifact :gravity/core-node :node-id "b" :form :literal :children {}}
        root {:artifact :gravity/core-node :node-id "root" :form 'do
              :children (array-map :first a :nested [b a])}
        replacement (assoc b :node-id "replacement")
        run (fn [algorithm mode]
              (let [events (atom [])
                    flatten (fn [node]
                              (swap! events conj [:flatten (:node-id node)])
                              (if (= "b" (:node-id node)) [replacement]
                                  (algorithm node)))
                    children (fn [value]
                               (swap! events conj [:children value])
                               (reverse (lowering/c6-core-child-nodes value)))
                    operations (case mode
                                 :flatten {:c6-flatten-core flatten}
                                 :children {:c6-core-child-nodes children}
                                 :both {:c6-flatten-core flatten
                                        :c6-core-child-nodes children})
                    result (c6/with-operations operations #(algorithm root))]
                [result @events]))]
    (doseq [mode [:flatten :children :both]]
      (let [[expected events] (run recursive-flatten-oracle mode)]
        (doseq [algorithm [lowering/c6-flatten-core c6/c6-flatten-core]]
          (let [[actual actual-events] (run algorithm mode)]
            (is (= expected actual))
            (is (every? true? (map identical? expected actual)))
            (is (= events actual-events))))))
    ;; Rebinding a facade Var is not the captured default function identity.
    (let [original-flatten c6/c6-flatten-core
          original-children c6/c6-core-child-nodes]
      (doseq [operation [:c6-flatten-core :c6-core-child-nodes]]
        (let [events (atom [])
              callback (if (= operation :c6-flatten-core)
                         (fn [node]
                           (swap! events conj [:flatten (:node-id node)])
                           (if (= "b" (:node-id node)) [replacement]
                               (original-flatten node)))
                         (fn [value]
                           (swap! events conj [:children value])
                           (reverse (original-children value))))
              expected (c6/with-operations {operation callback}
                         #(original-flatten root))
              expected-events @events
              _ (reset! events [])
              actual (if (= operation :c6-flatten-core)
                       (with-redefs [c6/c6-flatten-core callback]
                         (original-flatten root))
                       (with-redefs [c6/c6-core-child-nodes callback]
                         (original-flatten root)))]
          (is (= expected actual))
          (is (every? true? (map identical? expected actual)))
          (is (seq expected-events))
          (is (= expected-events @events)))))
    (let [failure (ex-info "original custom operation failure" {:id "C6-ORIGIN" :sentinel 7})]
      (doseq [operation [:c6-flatten-core :c6-core-child-nodes]
              algorithm [lowering/c6-flatten-core c6/c6-flatten-core]]
        (let [caught (try
                       (c6/with-operations {operation (fn [_] (throw failure))}
                         #(algorithm root))
                       nil
                       (catch clojure.lang.ExceptionInfo error error))]
          (is (identical? failure caught))
          (is (= {:id "C6-ORIGIN" :sentinel 7} (ex-data caught))))))
    ;; A rejected artifact retains its complete original diagnostic, not a cache.
    (doseq [candidate [(assoc-in module [:metadata :compiler :c6-lowering :fail] :origin)
                      module]]
      (let [c5 (if (= candidate module) (assoc c5-artifact :binding-table {:bindings []}) c5-artifact)
            stream [(syntax "ns" '(ns gravity.c6-test)) (syntax "value" 42)]
            build #(c6/c6-lowering-artifact "c6-test.gravity" candidate c5 stream)
            rejected (fn [thunk]
                       (try (thunk) {:unexpected-success true}
                            (catch clojure.lang.ExceptionInfo error (ex-data error))))
            expected (rejected #(c6/with-operations
                                 {:c6-flatten-core recursive-flatten-oracle} build))
            actual (rejected build)]
        (is (= expected actual))
        (is (contains? #{"C6-ORIGIN" "C6-VERIFY"} (:id actual)))
        (is (= "C6" (:document-id actual)))
        (is (nil? (:unexpected-success actual)))))))

(defn- original-two-flatten-trace [roots]
  ;; Frozen output oracle for the original projection, not a verifier/cache.
  {:artifact :gravity/c6-desugaring-trace
   :records
   (mapv (fn [root]
           {:surface-syntax (get-in root [:source :syntax-id])
            :surface-kind (:form root)
            :core-root (:node-id root)
            :introduced-forms
            (vec (keep #(when (:generated? %) (:form %))
                       (c6/c6-flatten-core root)))
            :preserved #{:source-spans :metadata :profile
                         :capabilities :effects :generated-origin}
            :introduced-origin
            (mapv (fn [node]
                    {:core-node (:node-id node)
                     :reason :surface-or-macro-desugar
                     :from (get-in node [:source :syntax-id])})
                  (filter :generated? (c6/c6-flatten-core root)))
            :evaluation-order (:evaluation-order root)
            :diagnostics []})
         roots)
   :status :complete})

(deftest desugaring-trace-reuses-one-original-flatten-per-root
  ;; C6 requires complete trace/origin/order preservation. Only the duplicate
  ;; root traversal is removed; recursive flattening and its returned nodes stay.
  (let [node (fn [id form generated? children]
               {:artifact :gravity/core-node :node-id id :form form
                :generated? generated? :children children
                :source {:syntax-id (str "syntax-" id)}
                :evaluation-order [:left :right]})
        plain (node "plain" :literal false {})
        generated (node "generated" 'quote true {})
        nested (node "nested" 'do true {:body [plain generated]})
        root (node "root" 'if false {:body [nested]})
        original-flatten c6/c6-flatten-core]
    (doseq [roots [[] [plain] [root] [plain root generated]]]
      (let [expected (original-two-flatten-trace roots)
            calls (atom []) depth (atom 0)
            actual
            (c6/with-operations
              {:c6-flatten-core
               (fn [candidate]
                 (when (zero? @depth) (swap! calls conj candidate))
                 (swap! depth inc)
                 (try (original-flatten candidate)
                      (finally (swap! depth dec))))}
              #(c6/c6-desugaring-trace roots))]
        (is (= expected actual))
        (is (= (mapv :node-id roots) (mapv :node-id @calls)))
        (is (every? true? (map identical? roots @calls)))
        (is (= (count roots) (count @calls)))
        (is (= (mapv #(get-in % [:source :syntax-id]) roots)
               (mapv :surface-syntax (:records actual))))
        (is (= (mapv :evaluation-order roots)
               (mapv :evaluation-order (:records actual))))))
    (let [record (first (:records (c6/c6-desugaring-trace [root])))]
      (is (= '[do quote] (:introduced-forms record)))
      (is (= ["nested" "generated"] (mapv :core-node (:introduced-origin record))))
      (is (= ["syntax-nested" "syntax-generated"]
             (mapv :from (:introduced-origin record))))
      (is (= #{:source-spans :metadata :profile :capabilities :effects :generated-origin}
             (:preserved record))))
    (let [stream [(syntax "ns" '(ns gravity.c6-test))
                  (assoc (syntax "generated" '(do (if true 1 2) 3))
                         :generated-origin [{:macro 'example :syntax-id "macro-0"}])
                  (syntax "plain" '(def answer 42))]
          build #(c6/c6-lowering-artifact "c6-test.gravity" module c5-artifact stream)
          expected (c6/with-operations {:c6-desugaring-trace original-two-flatten-trace} build)
          actual (build)]
      (is (= expected actual))
      (is (= (:artifact-id expected) (:artifact-id actual)))
      (is (= :passed (get-in actual [:core-verifier-report :status]))))))

(deftest contract-is-hosted-compatible-and-nonauthoritative
  (let [contract (c6/c6-engine-contract)
        publics (set (keys (ns-publics 'gravity.c6-core-lowering)))]
    (is (= :hosted-stage0-c6-core-lowering-engine
           (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-sh07-authority? contract)))
    (is (= :gravity.bootstrap-sh06-adapter (:authority-boundary contract)))
    (is (= ['c6-lowering-artifact] (:leaf-only-api contract)))
    (is (some #{:proof-authority} (:does-not-own contract)))
    (is (some #{:release} (:does-not-own contract)))
    (is (= #{'clojure.set 'gravity.digest}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (= #{'gravity.bootstrap 'gravity.diagnostics}
           (set (get-in contract [:dependency-direction :forbids]))))
    (is (= publics (set (keys (:public-api contract)))))
    (doseq [[name spec] c6/public-api
            :when (:arglists spec)]
      (is (= (:arglists spec)
             (:arglists (meta (get (ns-publics 'gravity.c6-core-lowering)
                                   name))))))
    (is (not (contains? (set (keys (ns-aliases
                                    'gravity.c6-core-lowering)))
                        'gravity.bootstrap)))
    (is (not (contains? (set (keys (ns-aliases
                                    'gravity.c6-core-lowering)))
                        'gravity.diagnostics)))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-supports-interposition
  (doseq [operations [nil
                      {:unknown identity}
                      {:c6-lower-form :callable-keyword}
                      {:core-forms #{:not-a-symbol}}
                      {:known-source-profiles #{}}
                      {:c6-lowering-diagnostic-ids [:not-a-string]}
                      {:c6-lowering-governing-document ""}
                      {:c6-lowering-rejected-designs [:not-a-map]}
                      {:c6-lowering-override-diagnostics {:gap :not-a-string}}
                      {:c6-domain-boundary-operators #{:not-a-symbol}}
                      {:c6-core-node-forms #{42}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c6/with-operations operations (constantly :unreachable)))))
  (let [calls (atom [])
        sentinel {:artifact :gravity/core-node :node-id "sentinel"}]
    (is (= [sentinel]
           (c6/with-operations
             {:c6-lower-form (fn [& args]
                               (swap! calls conj args)
                               sentinel)}
             #(c6/c6-lower-children (atom 0) module (syntax 0 '(do)) [1]))))
    (is (= 1 (count @calls))))
  (is (= :wasm
         (:target
          (c6/with-operations
            {:supported-targets #{:jvm :wasm}}
            #(c6/c6-core-node "node" :literal (syntax 0 1)
                              (assoc module :target :wasm)
                              {:surface-form 1}))))))

(deftest assembled-proof-is-not-reused-by-independent-validator
  (let [stream [(syntax "ns" '(ns gravity.c6-test))
                (syntax "value" '(def answer 42))]
        proof-inputs (atom [])
        nested-result (atom nil)
        original-proof c6/c6-lowering-capability-proof
        proof-operation
        (fn [candidate]
          (swap! proof-inputs conj candidate)
          (let [proof (original-proof candidate)]
            ;; Deliberately return an incomplete proof for the substituted
            ;; artifact. The validator must observe and reject this result.
            (if (= :forged (get-in candidate [:surface-to-core-map :status]))
              (assoc proof :source-to-core-map-present? false)
              proof)))
        validation-operation
        (fn [source-path artifact]
          (let [substituted (assoc-in artifact
                                      [:surface-to-core-map :status]
                                      :forged)]
            (reset! nested-result
                    (try
                      (c6/c6-lowering-validate! source-path substituted)
                      :unexpected-success
                      (catch clojure.lang.ExceptionInfo error
                        (ex-data error))))
            :complete))
        result
        (c6/with-operations
          {:c6-lowering-capability-proof proof-operation
           :c6-lowering-validate! validation-operation}
          #(c6/c6-lowering-artifact "c6-test.gravity" module c5-artifact stream))]
    (is (= :complete (get-in result [:capability-based-proof :status])))
    (is (= "C6-CORE-SHAPE" (:id @nested-result))
        "nested validation must reject the substituted artifact")
    (is (= 2 (count @proof-inputs))
        "the proof operation must receive both the assembled and substituted artifacts")
    (is (= :forged
           (get-in (second @proof-inputs) [:surface-to-core-map :status])))))

(deftest lowering-engine-emits-core-products
  (let [forms ['(quote value)
               '(if condition then else)
               '(do first second)
               '(let [x 1] x)
               '(fn [x] x)
               '(loop [x 1] (recur x))
               '(recur x)
               '(def answer 42)
               '(var answer)
               '(set! answer 43)
               '(try body (catch Error e e))
               '(throw error)
               '(match value pattern result)]
        roots (mapv (fn [[index form]]
                      (c6/c6-lower-form (atom 0) module
                                         (syntax index form) form))
                    (map-indexed vector forms))
        stream (vec (concat [(syntax "ns" '(ns gravity.c6-test))]
                            (map-indexed syntax forms)
                            [(syntax "domain" '(defschema User {}))]))
        artifact (c6/c6-lowering-artifact "c6-test.gravity" module
                                           c5-artifact stream)]
    (is (= '[quote if do let fn loop recur def var set! try throw match]
           (mapv :form roots)))
    (is (= :gravity/stage0-c6-core-lowering-artifact (:kind artifact)))
    (is (= :passed (get-in artifact [:core-verifier-report :status])))
    (is (= :complete (get-in artifact [:surface-to-core-map :status])))
    (is (= :complete (get-in artifact [:desugaring-trace :status])))
    (is (= :complete (get-in artifact [:evaluation-order-records :status])))
    (is (= [:schema-ir] (mapv :domain (:domain-boundary-records artifact))))
    (is (= (set c6/c6-lowering-diagnostic-ids)
           (set (map :diagnostic (:rejected-design-coverage artifact)))))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest c6-lossless-two-channel-feasibility-is-bounded-data-only
  ;; One fixed hosted product; these local views are not accepted artifacts.
  (let [limit (* 256 1024)
        bound! (fn [ok? phase]
                 (when-not ok? (throw (ex-info "bounded C6 feasibility stopped" {:phase phase}))))
        children (fn [value]
                   (cond (map? value) (mapcat identity value)
                         (coll? value) value
                         :else []))
        stats (fn [value maximum-depth]
                (loop [pending [[value 0]] visited 0 bytes 0 metadata-count 0 observed-depth 0]
                  (if-let [[value depth] (peek pending)]
                    (let [collection? (coll? value)
                          n (if collection? (count value) 0)
                          metadata (meta value)
                          child-count (+ (if (map? value) (* 2 n) n) (if metadata 1 0))
                          _ (bound! (and (<= depth maximum-depth) (<= n 128)
                                         (<= (+ visited (count pending) child-count) 32768)) :walk)
                          text (when-not collection?
                                 (cond (nil? value) "nil"
                                       (or (string? value) (symbol? value) (keyword? value)
                                           (integer? value) (boolean? value)) (str value)
                                       :else (throw (ex-info "unsupported feasibility scalar" {}))))
                          _ (when text
                              (bound! (and (<= (count text) 64)
                                           (every? #(<= (int %) 127) text)) :scalar))
                          bytes (+ bytes (if collection? (+ 4 (* (if (map? value) 4 2) n))
                                             (if (string? value) (+ 2 (* 6 (count text))) (count text))))
                          _ (bound! (<= bytes limit) :reservation)
                          tasks (map #(vector % (inc depth)) (cond-> (children value) metadata (concat [metadata])))]
                      (recur (into (pop pending) tasks) (inc visited) bytes
                             (+ metadata-count (if metadata 1 0)) (max observed-depth depth)))
                    {:visited visited :reservation bytes :metadata-count metadata-count
                     :maximum-depth observed-depth})))
        shared (with-meta (list 'do 7) {:mark :shared})
        left (with-meta (list 'do 11) {:mark :equal})
        right (with-meta (list 'do 11) {:mark :equal})
        datum (with-meta (array-map :artifact :gravity/core-node :node-id "quoted"
                                   :children [{:core-ref "not-an-edge"}]
                                   :surface-form {:surface-ref 0}) {:mark :quoted-map})
        quoted (with-meta (list 'quote (with-meta [datum 'do] {:mark :quoted-vector})) {:mark :quote})
        form (with-meta (list 'do (list 'mix left right) (list 'mix shared shared) quoted) {:mark :root})
        input-stats (stats form 8)
        _ (bound! (<= (:visited input-stats) 128) :surface-occurrences)
        _ (bound! (<= (* 1024 (:visited input-stats)) limit) :surface-table-reservation)
        surface-rows (atom [])
        surface-metadata (atom {})]
    (letfn [(kind [value]
              (cond (map? value) :map (vector? value) :vector (set? value) :set
                    (seq? value) :list (coll? value) (throw (ex-info "unsupported collection" {}))
                    :else :scalar))
            (rebuild [value values]
              (case (kind value)
                :map (into (empty value) (map vec (partition 2 values)))
                :vector (vec values) :set (into (empty value) values)
                :list (apply list values) :scalar value))
            (walk-data [f value path]
              (when (map? value)
                (bound! (every? #(or (keyword? %) (integer? %)) (keys value)) :metadata-locator-keys))
              (let [rebuilt (if (map? value)
                              (into (empty value)
                                    (map (fn [[key item]]
                                           [(walk-data f key (conj path [:key key]))
                                            (walk-data f item (conj path [:value key]))])) value)
                              (rebuild value (map-indexed #(walk-data f %2 (conj path %1))
                                                         (children value))))]
                (f rebuilt value path)))
            (strip-metadata [value]
              (let [ledger (atom {})
                    plain (walk-data (fn [rebuilt original path]
                                       (when-let [m (meta original)] (swap! ledger assoc path m))
                                       (if (instance? clojure.lang.IObj rebuilt) (with-meta rebuilt nil) rebuilt))
                                     value [])]
                {:data plain :metadata @ledger}))
            (restore-metadata [{:keys [data metadata]}]
              (walk-data (fn [rebuilt _ path]
                           (if (instance? clojure.lang.IObj rebuilt)
                             (with-meta rebuilt (get metadata path)) rebuilt)) data []))
            (surface! [value parent edge]
              (let [id (count @surface-rows)
                    _ (bound! (< id 128) :surface-table)
                    _ (swap! surface-rows conj nil)
                    row (if (= :scalar (kind value))
                          {:id id :parent parent :edge edge :kind :scalar
                           :value (if (instance? clojure.lang.IObj value) (with-meta value nil) value)}
                          {:id id :parent parent :edge edge :kind (kind value)
                           :items (mapv #(surface! %2 id %1) (range) (children value))})]
                (when-let [m (meta value)] (swap! surface-metadata assoc id m))
                (swap! surface-rows assoc id row)
                id))
            (core-children [node]
              (case (:form node)
                do (:children node) :call (get-in node [:children :arguments])
                quote [] :literal []
                (throw (ex-info "fixture needs a broader Core child grammar" {:form (:form node)}))))
            (count-core [value]
              (if (seq? value)
                (if (= 'quote (first value)) 1 (inc (reduce + (map count-core (rest value))))) 1))
            (count-nodes [node]
              (inc (reduce + (map count-nodes (core-children node)))))
            (core-depth [node]
              (inc (reduce max 0 (map core-depth (core-children node)))))
            (join! [node surface-id locators]
              (let [row (nth @surface-rows surface-id)
                    child-ids (if (seq (core-children node)) (subvec (:items row) 1) [])]
                (bound! (= (count child-ids) (count (core-children node))) :surface-child-join)
                (reduce (fn [result [child id]] (join! child id result))
                        (assoc locators (:node-id node) surface-id)
                        (map vector (core-children node) child-ids))))
            (project [node child? surface? locators]
              (let [convert #(if child? {:core-ref (:node-id %)} (project % false surface? locators))
                    child-view (case (:form node)
                                 do (mapv convert (:children node))
                                 :call (update (:children node) :arguments #(mapv convert %))
                                 quote (:children node) :literal (:children node)
                                 (throw (ex-info "unsupported fixture child view" {})))]
                (cond-> (assoc node :children child-view)
                  surface? (assoc :surface-form {:surface-ref (get locators (:node-id node))}))))
            (decode-surface [view id]
              (let [row (nth (:surfaces view) id)
                    values (map #(decode-surface view %) (:items row))
                    value (case (:kind row)
                            :scalar (:value row) :list (apply list values) :vector (vec values)
                            :set (set values) :map (into (array-map) (map vec (partition 2 values))))]
                (if (instance? clojure.lang.IObj value)
                  (with-meta value (get (:surface-metadata view) id)) value)))
            (decode-node [view index node child? surface?]
              (let [convert #(decode-node view index (if child? (get index (:core-ref %)) %) child? surface?)
                    child-view (case (:form node)
                                 do (mapv convert (:children node))
                                 :call (update (:children node) :arguments #(mapv convert %))
                                 quote (:children node) :literal (:children node))]
                (cond-> (assoc node :children child-view)
                  surface? (assoc :surface-form (decode-surface view (get-in node [:surface-form :surface-ref]))))))]
      (bound! (<= (count-core form) 64) :prepaid-core-rows)
      (let [root (c6/c6-lower-form (atom 0) module
                                  (assoc (syntax "feasibility" form) :metadata {:declared :preserved}
                                         :generated-origin [{:macro 'tiny :syntax-id "generated"}]) form)
            _ (stats root 64)
            _ (bound! (<= (core-depth root) 8) :core-depth)
            rows (c6/c6-flatten-core root)
            _ (bound! (<= (count rows) 64) :core-rows)
            _ (bound! (every? #(<= (count (:node-id %)) 32) rows) :core-reference-width)
            row-stats (stats rows 64)
            ;; Prepay fixed reference framing, occurrence rows, and metadata
            ;; path sidecars before allocating the four view-copy walks.
            view-reservation (+ 4096 (:reservation row-stats)
                                (* 128 (reduce + (map count-nodes rows)))
                                (* 1024 (:visited input-stats))
                                (* (:metadata-count row-stats)
                                   (+ 64 (* 96 (:maximum-depth row-stats)))))
            _ (bound! (and (<= view-reservation limit)
                           (<= (* 4 view-reservation) (* 2 1024 1024))) :prepaid-views)
            surface-root (surface! form nil 0)
            locators (join! root surface-root {})
            specs [[:original false false] [:child-only true false] [:surface-only false true] [:both true true]]
            raw-views (mapv (fn [[label child? surface?]]
                              (cond-> {:view label :roots [(:node-id root)]
                                       :rows (mapv #(project % child? surface? locators) rows)}
                                surface? (assoc :surface-roots [surface-root] :surfaces @surface-rows
                                                :surface-metadata @surface-metadata))) specs)
            ;; All source/Core caps precede the bounded copying walks. Metadata
            ;; sidecars and occurrence/reference/root tables are part of each view.
            raw-reservations (mapv #(stats % 64) raw-views)
            _ (bound! (<= (reduce + (map :reservation raw-reservations)) (* 2 1024 1024)) :copy-reservation)
            views (mapv strip-metadata raw-views)
            reservations (mapv #(stats % 64) views)
            _ (bound! (every? #(<= (:reservation %) view-reservation) reservations) :complete-view-reservations)
            _ (bound! (<= (reduce + (map :reservation reservations)) (* 2 1024 1024)) :total-reservation)
            original-metadata (:metadata (strip-metadata rows))
            observations
            (mapv (fn [[label child? surface?] encoded reservation]
                    (let [text (binding [*print-length* nil *print-level* nil *print-meta* false
                                         *print-dup* false *print-namespace-maps* false] (pr-str encoded))
                          bytes (alength (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8))
                          _ (bound! (<= bytes (:reservation reservation) limit) :printed-bytes)
                          view (restore-metadata (edn/read-string {:readers {}} text))
                          index (into {} (map (juxt :node-id identity)) (:rows view))
                          decoded (mapv #(decode-node view index % child? surface?) (:rows view))
                          _ (is (= rows decoded))
                          _ (is (= original-metadata (:metadata (strip-metadata decoded))))
                          _ (is (= (mapv :node-id rows) (mapv :node-id decoded)))
                          _ (is (= (mapv :source rows) (mapv :source decoded)))
                          _ (is (= (mapv :children rows) (mapv :children decoded)))
                          _ (is (= (mapv :surface-form rows) (mapv :surface-form decoded)))
                          _ (is (= rows (c6/c6-flatten-core (first decoded))))]
                      {:view label :utf8-bytes bytes :reserved-bytes (:reservation reservation)}))
                  specs views reservations)
            first-call (nth (:items (nth @surface-rows surface-root)) 1)
            second-call (nth (:items (nth @surface-rows surface-root)) 2)
            equal-ids (subvec (:items (nth @surface-rows first-call)) 1)
            shared-ids (subvec (:items (nth @surface-rows second-call)) 1)]
        (is (= left right))
        (is (not (identical? left right)))
        (is (identical? (second (nth form 2)) (nth (nth form 2) 2)))
        (is (= 12 (count rows)))
        (is (= rows (recursive-flatten-oracle root)))
        (is (= (mapv #(str "c6-core-" %) (range 12)) (mapv :node-id rows)))
        (is (and (identical? left (:surface-form (nth rows 2)))
                 (identical? right (:surface-form (nth rows 4)))
                 (not (identical? (:surface-form (nth rows 2)) (:surface-form (nth rows 4))))))
        (is (and (identical? shared (:surface-form (nth rows 7)))
                 (identical? shared (:surface-form (nth rows 9)))))
        (is (= ['mix 'mix] (mapv #(get-in % [:children :operator]) (filter #(= :call (:form %)) rows))))
        (is (empty? (core-children (last rows))))
        (is (not-any? #(= "quoted" (:node-id %)) rows))
        (is (= 2 (count (set equal-ids)) (count (set shared-ids))))
        (is (= (count rows) (count locators) (count (set (map :node-id rows)))))
        (is (= (range (count @surface-rows)) (map :id @surface-rows)))
        (doseq [{:keys [id parent edge]} (rest @surface-rows)]
          (is (= id (nth (:items (nth @surface-rows parent)) edge))))
        (is (= rows (:data (strip-metadata rows))))
        (is (seq original-metadata))
        (is (not= original-metadata (:metadata (strip-metadata (:data (strip-metadata rows))))))
        (is (= datum (-> (decode-surface (restore-metadata (last views)) surface-root) last second first)))
        (is (nil? (find-ns 'gravity.bootstrap)))
        (println (pr-str {:c6-lossless-two-channel-data-only true :core-rows (count rows)
                          :surface-occurrences (count @surface-rows) :input-walk (:visited input-stats)
                          :views observations :authoritative? false}))))))

(deftest c6-growing-surface-cost-is-bounded-data-only
  ;; Diagnostic views only: no artifact schema, decoder or production change.
  (let [maximum-bytes (* 256 1024)
        bound! (fn [ok? phase]
                 (when-not ok?
                   (throw (ex-info "bounded growing C6 observation stopped"
                                   {:phase phase}))))
        stats
        (fn [value maximum-depth surface?]
          (loop [pending [[value 0 surface?]] visited 0 core 0 surface 0 bytes 0]
            (if-let [[value depth surface?] (peek pending)]
              (let [visited (inc visited)
                    _ (bound! (and (<= visited 32768) (<= depth maximum-depth)) :walk)
                    core (+ core (if (and (map? value)
                                          (= :gravity/core-node (:artifact value))) 1 0))
                    surface (+ surface (if surface? 1 0))
                    collection? (coll? value)
                    _ (when collection? (bound! (<= (count value) 128) :width))
                    text (when-not collection?
                           (cond (nil? value) "nil"
                                 (or (string? value) (keyword? value) (symbol? value)
                                     (integer? value) (boolean? value)) (str value)
                                 :else (throw (ex-info "unexpected C6 observation scalar" {}))))
                    _ (when text
                        (bound! (and (<= (count text) 64)
                                     (every? #(<= (int %) 127) text)) :scalar))
                    bytes (+ bytes (if collection?
                                     (+ 4 (* (if (map? value) 4 2) (count value)))
                                     (if (string? value) (+ 2 (* 6 (count text)))
                                         (count text))))
                    children (cond
                               (map? value)
                               (mapcat (fn [[key item]]
                                         [[key (inc depth) surface?]
                                          [item (inc depth)
                                           (or surface? (= :surface-form key))]]) value)
                               collection? (map #(vector % (inc depth) surface?) value)
                               :else [])]
                (bound! (<= bytes maximum-bytes) :pre-print-reservation)
                (recur (into (pop pending) children) visited core surface bytes))
              {:visited visited :core-occurrences core :surface-occurrences surface
               :print-upper-bound bytes})))
        bytes (fn [value reservation]
                (let [text (binding [*print-length* nil *print-level* nil
                                     *print-meta* false *print-dup* false
                                     *print-namespace-maps* false]
                             (pr-str value))
                      _ (bound! (<= (count text) reservation maximum-bytes) :printed)
                      encoded (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8)]
                  (bound! (<= (alength encoded) reservation maximum-bytes) :utf8)
                  (alength encoded)))
        chain (fn [depth] (nth (iterate #(list 'do %) :bounded-data) depth))
        left (chain 1) right (chain 1)
        cases (concat (map #(vector % [(chain %)]) [1 2 4 8])
                      [[:equal-distinct [left right]]])
        observations
        (mapv
         (fn [[label forms]]
           (let [inputs (mapv #(stats % 8 true) forms)
                 input-occurrences (reduce + (map :surface-occurrences inputs))
                 _ (bound! (<= input-occurrences 128) :input-surface-occurrences)
                 counter (atom 0)
                 roots (mapv (fn [index form]
                               (c6/c6-lower-form counter module
                                                 (syntax (str label "-" index) form) form))
                             (range) forms)
                 root-stats (stats roots 64 false)
                 _ (bound! (<= (:core-occurrences root-stats) 64) :core-rows)
                 rows (vec (mapcat c6/c6-flatten-core roots))
                 _ (bound! (<= (count rows) 64) :flat-rows)
                 expected (vec (mapcat recursive-flatten-oracle roots))
                 ;; Only actual unary-do child nodes become IDs in this view.
                 ;; All surface payloads and all other fields remain untouched.
                 view (mapv (fn [row]
                              (update row :children
                                      #(if (vector? %) (mapv :node-id %) %))) rows)
                 original (stats rows 64 false)
                 referenced (stats view 64 false)
                 node-count (count rows)
                 same-payloads? (every? true?
                                       (map (fn [row projected]
                                              (and (= (dissoc row :children)
                                                      (dissoc projected :children))
                                                   (identical? (:surface-form row)
                                                               (:surface-form projected))
                                                   (identical? (:source row) (:source projected))))
                                            rows view))]
             (is (= (mapv #(str "c6-core-" %) (range node-count)) (mapv :node-id rows)))
             (is (= expected rows))
             (is (every? true? (map identical? expected rows)))
             (is (= (mapv :node-id rows) (mapv :node-id view)))
             (is (every? true? (map identical? roots (map #(nth rows %) (if (= 1 (count roots)) [0] [0 2])))))
             (is (every? true? (map identical? forms (map :surface-form roots))))
             (when (= :equal-distinct label)
               (is (not= (:source (first roots)) (:source (second roots)))))
             (is same-payloads?)
             (is (= node-count (count (set (map :node-id rows))) (:core-occurrences referenced)))
             (is (every? true? (map (fn [row projected]
                                     (= (mapv :node-id (c6/c6-core-child-nodes (:children row)))
                                        (vec (:children projected)))) rows view)))
             (is (> (:core-occurrences original) (:core-occurrences referenced)))
             (is (> (:surface-occurrences original) (:surface-occurrences referenced)
                    input-occurrences))
             (when (integer? label)
               (is (= (inc label) node-count))
               (is (= (* node-count node-count) (:surface-occurrences referenced))))
             (let [original-bytes (bytes rows (:print-upper-bound original))
                   referenced-bytes (bytes view (:print-upper-bound referenced))]
               (is (> original-bytes referenced-bytes))
               {:case label :input-surface-occurrences input-occurrences :rows node-count
                :original-core-occurrences (:core-occurrences original)
                :referenced-core-occurrences (:core-occurrences referenced)
                :original-surface-occurrences (:surface-occurrences original)
                :referenced-surface-occurrences (:surface-occurrences referenced)
                :original-table-utf8-bytes original-bytes
                :child-reference-table-utf8-bytes referenced-bytes})))
         cases)]
    (is (= left right))
    (is (not (identical? left right)))
    (is (= [4 9 25 81] (mapv :referenced-surface-occurrences (take 4 observations))))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (let [summary (pr-str {:c6-growing-surface-data-only true :authoritative? false
                          :view :child-references-only-not-an-accepted-artifact
                          :surface-unit :all-positions-under-each-surface-form
                          :nonclaims [:schema :timing :heap :speedup :checked-core-size :cause :self-hosting]
                          :cases observations})]
      (bound! (and (<= (count summary) (* 1024 1024))
                   (every? #(<= (int %) 127) summary)) :scalar-log)
      (println summary))))

(deftest c6-flat-nested-node-print-amplification-is-bounded-data-only
  ;; C6 preserves nested children in its flat table. These fixed DATA values
  ;; isolate printed occurrences, without compiler construction or proof credit.
  (let [maximum-bytes (* 256 1024)
        require-bound! (fn [passed? facts]
                         (when-not passed?
                           (throw (ex-info "bounded C6 DATA measurement stopped" facts))))
        observations
        (mapv
         (fn [size]
           (let [nodes
                 (reduce (fn [built index]
                           (conj built
                                 {:artifact :gravity/core-node
                                  :node-id (str "node-" index)
                                  :surface-form :bounded-data
                                  :children (if-let [child (peek built)]
                                              {:next child} {})}))
                         [] (range size))
                 root (peek nodes)
                 expected-flat (vec (reverse nodes))
                 flat (c6/c6-flatten-core root)
                 ;; Reject unexpected data before walking or printing it. The
                 ;; allowed vector contains only these exact eight-node-or-less chains.
                 _ (require-bound! (= expected-flat flat) {:size size :phase :flat-shape})
                 occurrences (fn [value]
                               (count (filter #(and (map? %)
                                                    (= :gravity/core-node (:artifact %)))
                                              (tree-seq coll? seq value))))
                 root-occurrences (occurrences [root])
                 flat-occurrences (occurrences flat)
                 expected-occurrences (quot (* size (inc size)) 2)
                 _ (require-bound! (= expected-occurrences flat-occurrences)
                                   {:size size :phase :occurrences})
                 ;; Each fixed ASCII node contributes fewer than 256 bytes,
                 ;; including its child-map framing; reserve another 256 for vectors.
                 print-bound (+ 256 (* 256 flat-occurrences))
                 _ (require-bound! (<= print-bound maximum-bytes)
                                   {:size size :phase :before-print})
                 byte-count
                 (fn [value]
                   (let [text (binding [*print-length* nil *print-level* nil
                                        *print-meta* false *print-dup* false
                                        *print-namespace-maps* false]
                                (pr-str value))]
                     (require-bound! (and (<= (count text) print-bound)
                                          (every? #(<= (int %) 127) text))
                                     {:size size :phase :before-utf8})
                     (let [bytes (alength (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8))]
                       (require-bound! (<= bytes maximum-bytes)
                                       {:size size :phase :utf8})
                       bytes)))
                 root-bytes (byte-count [root])
                 flat-bytes (byte-count flat)]
             (is (= size (count flat) (count (set (map :node-id flat)))))
             (is (= (mapv :node-id expected-flat) (mapv :node-id flat)))
             (is (= size root-occurrences))
             (is (= expected-occurrences flat-occurrences))
             (is (<= root-bytes maximum-bytes))
             (is (<= flat-bytes maximum-bytes))
             (is (if (= 1 size) (= root-bytes flat-bytes) (< root-bytes flat-bytes)))
             {:nodes size :flat-rows (count flat)
              :root-occurrences root-occurrences :flat-occurrences flat-occurrences
              :root-utf8-bytes root-bytes :flat-utf8-bytes flat-bytes}))
         [1 2 4 8])]
    (is (= [1 3 10 36] (mapv :flat-occurrences observations)))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (println (pr-str {:c6-structural-data-only true :authoritative? false
                      :nonclaims [:timing :heap :checked-core-size :cause :self-hosting]
                      :cases observations}))))

(deftest diagnostic-overrides-retain-c6-identities
  (doseq [[failure expected-id]
          [[:gap "C6-LOWERING-GAP"]
           [:core-shape "C6-CORE-SHAPE"]
           [:eval-order "C6-EVAL-ORDER"]
           [:origin "C6-ORIGIN"]
           [:effect-drop "C6-EFFECT-DROP"]
           [:unsafe-drop "C6-UNSAFE-DROP"]
           [:domain-boundary "C6-DOMAIN-BOUNDARY"]
           [:verify "C6-VERIFY"]]]
    (testing expected-id
      (let [failed-module (assoc-in module
                                    [:metadata :compiler :c6-lowering :fail]
                                    failure)
            error (try
                    (c6/c6-lowering-artifact
                     "c6-test.gravity" failed-module c5-artifact
                     [(syntax 0 '(ns gravity.c6-test)) (syntax 1 42)])
                    nil
                    (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))
        (is (= "C6" (:document-id (ex-data error))))))))
