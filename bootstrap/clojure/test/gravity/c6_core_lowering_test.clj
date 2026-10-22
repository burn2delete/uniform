(ns gravity.c6-core-lowering-test
  (:require [clojure.test :refer [deftest is testing]]
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
        original lowering/c6-flatten-core]
    ;; Fixed tiny shapes, including repeated object occurrences: no deduplication.
    (doseq [root [nil a branch chain]
            flatten [lowering/c6-flatten-core c6/c6-flatten-core]]
      (let [expected (recursive-flatten-oracle root)
            calls (atom [])
            actual (with-redefs [lowering/c6-flatten-core
                                (fn [node]
                                  (swap! calls conj node)
                                  (original node))]
                     ;; Resolve the leaf Var after interposition, not saved above.
                     (if (identical? flatten original)
                       (lowering/c6-flatten-core root)
                       (flatten root)))]
        (is (vector? actual))
        (is (= expected actual))
        (is (every? true? (map identical? expected actual)))
        (is (= 1 (count @calls)))
        (is (identical? root (first @calls)))))
    (let [stream [(syntax "ns" '(ns gravity.c6-test))
                  (assoc (syntax "generated" '(let [x (if true 1 2)] (do x 3)))
                         :generated-origin [{:macro 'example :syntax-id "macro-0"}])
                  (syntax "plain" '(def answer 42))]
          build #(c6/c6-lowering-artifact "c6-test.gravity" module c5-artifact stream)
          expected (c6/with-operations {:c6-flatten-core recursive-flatten-oracle} build)
          calls (atom [])
          actual (with-redefs [lowering/c6-flatten-core
                              (fn [node]
                                (swap! calls conj (:node-id node))
                                (original node))]
                   (build))
          roots (get-in actual [:core-ast-module :roots])]
      (is (= expected actual))
      (is (= (:artifact-id expected) (:artifact-id actual)))
      (is (= (concat roots roots) @calls))
      (is (= :passed (get-in actual [:core-verifier-report :status])))
      (is (= (:c5-name-resolution-artifact expected)
             (:c5-name-resolution-artifact actual)))
      (is (= (:desugaring-trace expected) (:desugaring-trace actual))))))

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
