(ns gravity.c6-core-lowering-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c6-core-lowering :as c6]))

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
