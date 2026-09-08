(ns gravity.self-hosting.sh07-b51-attempt28-producer-replay-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.java.io :as io]
            [gravity.bootstrap :as bootstrap]))

(def producer-paths
  ["bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity"])

(defn read-all [path]
  (with-open [reader (java.io.PushbackReader. (io/reader path))]
    (loop [forms []]
      (let [form (read {:eof ::eof} reader)]
        (if (= form ::eof) forms (recur (conj forms form)))))))

(def producer-forms (delay (vec (mapcat read-all producer-paths))))

(defn producer-runtime []
  (let [definitions
        (filter #(and (seq? %) (= 'defn (first %))) @producer-forms)
        runtime-ns (create-ns (gensym "gravity.sh07.b51.attempt28.producer-"))]
    (binding [*ns* runtime-ns]
      (clojure.core/refer 'clojure.core)
      (intern runtime-ns 'sh07-declared-digest-hash
              bootstrap/reader-canonical-hash)
      (intern runtime-ns 'sh07-canonical-text-compare
              bootstrap/p15-s23-sh07-canonical-text-compare)
      (intern runtime-ns 'sh07-observe-utf8-byte-count
              bootstrap/p15-s23-sh07-observe-utf8-byte-count)
      (intern runtime-ns 'sort-by-pr-str
              (fn [values] (apply list (sort-by pr-str values))))
      (doseq [form definitions] (intern runtime-ns (second form)))
      (doseq [form definitions] (eval form)))
    {:namespace runtime-ns
     :invoke
     (fn [function & arguments]
       (apply (deref (ns-resolve runtime-ns function)) arguments))}))

(def runtime (delay (producer-runtime)))

(defn invoke [function & arguments]
  (apply (:invoke @runtime) function arguments))

(defn utf8-count [text]
  (alength (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8)))

(defn host-value-profile [root]
  (loop [pending [[root 0]] nodes 0 maximum-depth 0
         maximum-width 0 maximum-scalar 0]
    (if (empty? pending)
      {:nodes nodes :depth maximum-depth :width maximum-width
       :scalar-utf8 maximum-scalar
       :c11-utf8 (utf8-count (pr-str root))
       :canonical-reader-utf8
       (utf8-count (pr-str (bootstrap/reader-canonical-value root)))}
      (let [[[value depth] & remaining] pending
            aggregate? (coll? value)
            children (cond
                       (map? value) (mapcat identity value)
                       aggregate? value
                       :else [])
            scalar (if aggregate? 0 (utf8-count (pr-str value)))]
        (recur (into (vec remaining)
                     (map #(vector % (inc depth)) children))
               (inc nodes) (max maximum-depth depth)
               (max maximum-width (if aggregate? (count value) 0))
               (max maximum-scalar scalar))))))

(defn dummy-carrier []
  (let [descriptors
        (mapv (fn [ordinal]
                {:module-ordinal ordinal
                 :module-source-id
                 (str "sha256:" (apply str (repeat 64 (char (+ 97 ordinal)))) )})
              (range 7))
        plans
        (mapv (fn [ordinal]
                {:module-ordinal ordinal
                 :module-source-id (:module-source-id (nth descriptors ordinal))
                 :plan-id
                 (str "sha256:" (apply str (repeat 64 (char (+ 104 ordinal)))))})
              (range 7))]
    {:module-source-descriptors descriptors
     :module-plan-carriers plans
     :link-manifest {:manifest-id (str "sha256:" (apply str (repeat 64 \a)))}
     :execution-bundle {:bundle-id (str "sha256:" (apply str (repeat 64 \b)))}}))

(deftest linked-v3-leaf-fold-and-root-replay-is-bounded-and-deterministic
  (let [categories
        [:roots :members :functions :local-runtime-call-edges
         :external-runtime-call-edges :referenced-builtins :module-contracts]
        rows (zipmap categories (repeat []))
        carrier (dummy-carrier)
        first-replay
        (invoke 'sh07-b51-success-digest-replay-exact?
                {:mode :linked-v3-semantic-replay :rows rows} carrier)
        second-replay
        (invoke 'sh07-b51-success-digest-replay-exact?
                {:mode :linked-v3-semantic-replay :rows rows} carrier)]
    (is (= :accepted (:status first-replay)))
    (is (= first-replay second-replay))
    (is (= 7 (count (get-in first-replay
                            [:semantic-manifest :category-commitments]))))
    (is (= 0 (get-in first-replay [:semantic-manifest :total-row-count])))
    (is (every? #(= [0 1 1]
                    ((juxt :row-count :leaf-count :fold-level-count) %))
                (get-in first-replay
                        [:semantic-manifest :category-commitments])))
    (is (re-matches #"sha256:[0-9a-f]{64}"
                    (:semantic-root-hash first-replay)))))

(deftest six-dimensional-profile-matches-host-canonical-census
  (let [sample {:coordinate {:module-ordinal 3
                             :function 'sh07-b51-build-template}
                :rows [[:a 1] #{:b :c}]}
        gravity-profile (invoke 'sh07-b51-r1-linked-value-profile sample)
        host-profile (host-value-profile sample)]
    (is (= :accepted (:status gravity-profile)))
    (is (= (select-keys gravity-profile
                        [:nodes :depth :width :scalar :c11
                         :canonical-reader])
           {:nodes (:nodes host-profile)
            :depth (:depth host-profile)
            :width (:width host-profile)
            :scalar (:scalar-utf8 host-profile)
            :c11 (:c11-utf8 host-profile)
            :canonical-reader (:canonical-reader-utf8 host-profile)}))))

(deftest linked-semantic-call-order-comes-from-m2-and-m0
  (let [source-id (str "sha256:" (apply str (repeat 64 \a)))
        source-sha (str "sha256:" (apply str (repeat 64 \b)))
        definition-sha (str "sha256:" (apply str (repeat 64 \c)))
        plan-id (str "sha256:" (apply str (repeat 64 \d)))
        manifest-id (str "sha256:" (apply str (repeat 64 \e)))
        provenance
        {:artifact :gravity/sh07-compiler-internal-call-provenance-v2
         :manifest-id manifest-id
         :module-source-id source-id
         :source-sha256 source-sha
         :function 'caller
         :function-source-hash definition-sha
         :callee 'sh07-observe-utf8-byte-count}
        instructions
        [{:op :if
          :test {:op :function-call :function 'test-call :args []}
          :then {:op :builtin-call
                 :function 'sh07-observe-utf8-byte-count
                 :compiler-internal-caller 'caller
                 :compiler-internal-provenance provenance
                 :args [{:op :function-call :function 'nested-call :args []}]}
          :else {:op :let
                 :bindings [{:name 'value
                             :expr {:op :function-call
                                    :function 'binding-call :args []}}]
                 :body [{:op :builtin-call :function 'count :args []}]}}]
        descriptor
        {:module-ordinal 0 :module-source-id source-id
         :source-sha256 source-sha
         :definitions [{:name 'caller :source-hash definition-sha}]
         :builtin-calls
         [{:caller 'caller :caller-function-source-hash definition-sha
           :builtin 'sh07-observe-utf8-byte-count :arity 1
           :compiler-internal? true}
          {:caller 'caller :caller-function-source-hash definition-sha
           :builtin 'count :arity 0 :compiler-internal? false}]}
        plan-carriers
        [{:module-ordinal 0 :module-source-id source-id :plan-id plan-id
          :plan {:functions {'caller {:instructions instructions}}}}]
        walked
        (invoke 'sh07-b51-r1-linked-m2-function-calls
                descriptor 'caller {:instructions instructions})
        calls (:calls walked)
        builtin-calls (filterv #(= :builtin (:kind %)) calls)
        rows (invoke 'sh07-b51-r1-linked-builtin-rows
                     [descriptor] plan-carriers #{'caller})]
    (is (= :accepted (:status walked)))
    (is (= ['test-call 'sh07-observe-utf8-byte-count 'nested-call
            'binding-call 'count]
           (mapv :callee calls)))
    (is (= [0 1 2 3 4] (mapv :m2-occurrence-ordinal calls)))
    (is (true? (invoke 'sh07-b51-r1-linked-m0-m2-builtin-parity?
                       [descriptor] builtin-calls)))
    (testing "M0 order is independent of authenticated M2 occurrence order"
      (is (true? (invoke 'sh07-b51-r1-linked-m0-m2-builtin-parity?
                         [descriptor] (vec (reverse builtin-calls))))))
    (testing "the occurrence multiset remains a closed exact bijection"
      (is (false? (invoke 'sh07-b51-r1-linked-m0-m2-builtin-parity?
                          [descriptor] (pop builtin-calls))))
      (is (false? (invoke 'sh07-b51-r1-linked-m0-m2-builtin-parity?
                          [descriptor]
                          (assoc builtin-calls 1 (first builtin-calls)))))
      (is (false? (invoke 'sh07-b51-r1-linked-m0-m2-builtin-parity?
                          [descriptor]
                          (assoc-in builtin-calls [1 :arity] 1)))))
    (is (= ['sh07-observe-utf8-byte-count 'count]
           (mapv :builtin rows)))
    (is (= [0 1] (mapv :source-callsite-ordinal rows)))
    (is (true? (invoke 'sh07-b51-r1-linked-v2-provenance-authentic?
                       [descriptor] plan-carriers
                       {:manifest-id manifest-id})))
    (is (false?
         (invoke 'sh07-b51-r1-linked-v2-provenance-authentic?
                 [descriptor]
                 (assoc-in plan-carriers
                           [0 :plan :functions 'caller :instructions 0
                            :then :compiler-internal-provenance :manifest-id]
                           (str "sha256:" (apply str (repeat 64 \f))))
                 {:manifest-id manifest-id})))))

(deftest linked-source-call-census-normalizes-and-validates-root-body
  (let [descriptor {:module-ordinal 0}
        list-definition
        {:body (list (list 'callee 1) (list 'count []))}
        plans
        [{:module-ordinal 0
          :plan {:functions {'caller list-definition
                             'callee {:body (list nil)}}}}]
        walked
        (invoke 'sh07-b51-r1-linked-function-calls
                [descriptor] plans 'caller descriptor list-definition)]
    (testing "a retained list body is indexed exactly like an AST vector"
      (is (= :accepted (:status walked)))
      (is (= [['callee :function [0] 1]
              ['count :builtin [1] 1]]
             (mapv (juxt :callee :kind :path :arity) (:calls walked)))))
    (testing "absent, empty, and non-sequential root bodies fail closed"
      (doseq [body [nil [] {} 7]]
        (is (= {:status :rejected :reason :linked-call-body-shape}
               (invoke 'sh07-b51-r1-linked-function-calls
                       [descriptor] plans 'caller descriptor {:body body})))))))

(deftest linked-external-replay-canonicalizes-raw-source-order-to-m1
  (let [digest #(str "sha256:" (apply str (repeat 64 %)))
        caller-source (digest \a)
        callee-source (digest \b)
        caller-hash (digest \c)
        callee-hash (digest \d)
        caller-plan (digest \e)
        callee-plan (digest \f)
        descriptors
        [{:module-ordinal 0 :module-source-id caller-source
          :definitions [{:name 'caller :source-hash caller-hash}]
          :external-bindings
          [{:symbol 'callee :owner-edge :producer-internal}]}
         {:module-ordinal 1 :module-source-id callee-source
          :definitions [{:name 'callee :source-hash callee-hash}]
          :external-bindings []}]
        plans
        [{:module-ordinal 0 :plan-id caller-plan
          :plan {:functions {'caller {}}}}
         {:module-ordinal 1 :plan-id callee-plan
          :plan {:functions {'callee {}}}}]
        paths (conj (mapv vector (range 1422)) [0 0])
        calls
        (mapv (fn [path]
                {:kind :function :caller 'caller :callee 'callee
                 :arity 1 :path path})
              (reverse paths))
        edge
        (fn [ordinal path]
          {:ordinal ordinal
           :caller-module-source-id caller-source
           :caller-function 'caller
           :caller-function-source-hash caller-hash
           :callsite-path path
           :callee-module-source-id callee-source
           :callee-function 'callee
           :callee-function-source-hash callee-hash
           :arity 1 :owner-edge :producer-internal
           :authorization :authenticated-b51-namespace-external})
        edges (mapv edge (range) paths)
        routes (mapv (fn [ordinal]
                       {:edge-ordinal ordinal :caller-plan-id caller-plan
                        :callee-plan-id callee-plan})
                     (range 1423))
        authentic?
        #(invoke 'sh07-b51-r1-linked-external-authentic?
                 descriptors plans calls {:edges %1} {:routes %2})]
    (testing "M1 count-first path order is reconstructed independently"
      (is (true? (authentic? edges routes))))
    (testing "authenticated rows remain an exact ordered bijection"
      (is (false? (authentic? (vec (reverse edges)) routes)))
      (is (false? (authentic? (pop edges) (pop routes))))
      (is (false? (authentic? (assoc edges 1 (first edges)) routes))))))

(deftest producer-source-rejects-the-monolithic-carrier-seam
  (let [text (apply str (map slurp producer-paths))]
    (testing "linked-v3 is the only raw and executable carrier spelling"
      (is (not (re-find #"raw-adapter-request-v18" text)))
      (is (not (re-find #"executable-closure-carrier-v18" text)))
      (is (re-find #"raw-adapter-request-linked-v3" text))
      (is (re-find #"linked-executable-closure-carrier-v3" text)))
    (testing "the sole producer digest caller remains the admitted function"
      (let [callers
            (for [form @producer-forms
                  :when (and (seq? form) (= 'defn (first form)))
                  :when (some #(and (seq? %)
                                    (= 'sh07-declared-digest-hash (first %)))
                              (tree-seq coll? seq form))]
              (second form))]
        (is (= #{'sh07-b51-success-digest-replay-exact?
                 'sh07-b51-producer-decimal-projection-authentic?
                 'sh07-b51-producer-decimal-admission}
               (set callers)))))))
