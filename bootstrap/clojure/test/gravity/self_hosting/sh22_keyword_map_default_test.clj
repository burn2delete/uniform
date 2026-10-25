(ns gravity.self-hosting.sh22-keyword-map-default-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

;; STD2/STD3 explicit-default semantics over evaluated request DATA. This does
;; not prove source argument evaluation, compiler linkage or SH-22 completion.
(def ^:private root
  (delay
    (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh22_keyword_map_default_test.clj")))]
      (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file))))))
(defn- path [relative] (str (io/file @root relative)))
(defn- compile-plan [relative]
  (let [source (path relative)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source))))
(def ^:private engine
  (delay (compile-plan "bootstrap/gravity/src/gravity/stdlib/self_hosting_core.gravity")))
(defn- fixture-path [family extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-22/" family "/coordinator-keyword-map-default." extension))
(def ^:private fixtures
  (delay (into {} (for [family ["accepted" "rejected"] extension ["gravity" "qst"]]
                   [[family extension] (compile-plan (fixture-path family extension))]))))
(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh22-standard-library-leaf :compiler-artifact-plan? true} plan function arguments))
(defn- call [function arguments] (invoke @engine function arguments))
(defn- request [extension function] (invoke (get @fixtures ["accepted" extension]) function []))
(defn- ordered [entries] {:artifact :gravity/ordered-map :entries entries})
(defn- handle [input] (call 'sh22-handle-request [input]))
(defn- verified? [input result] (= :passed (:status (call 'sh22-verify-result [input result]))))
(defn- observe [input]
  (let [scoped (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        original @scoped counts (atom {:census 0 :unique 0 :lookup 0})
        result (with-redefs-fn
                 {scoped (fn [& args]
                           (when-let [kind ({'sh22-keyword-map-entries? :census
                                             'sh22-entries-well-formed? :unique
                                             'sh22-entry-index :lookup} (nth args 2))]
                             (swap! counts update kind inc))
                           (apply original args))}
                 #(handle input))]
    [result @counts]))
(def ^:private lookup-facts
  {:collection-kind :ordered-map :key-family :keyword :lookup :explicit-default
   :equality :keyword-value :safety-outcome :proven-safe :allocation :none
   :persistent-value-semantics true})

(deftest sh22-keyword-map-default-surface-and-paired-fixtures
  (is (= :gravity/stage2-compiler-artifact-plan (:kind @engine)))
  (doseq [family ["accepted" "rejected"]]
    (is (= (slurp (path (fixture-path family "gravity")))
           (slurp (path (fixture-path family "qst"))))))
  (doseq [plan (vals @fixtures)] (is (= :gravity/stage2-compiler-artifact-plan (:kind plan))))
  (let [policy (call 'sh22-library-policy []) manifest (call 'sh22-module-manifest [])
        export (first (filter #(= :ordered-map-get-default-keyword (:name %)) (:exports manifest)))]
    (is (contains? (:operation-set policy) :ordered-map-get-default-keyword))
    (is (= (:operation-set policy) (set (map :name (:exports manifest)))))
    (is (= :experimental (:stability export)))
    (is (= #{:meta} (:profiles export)))
    (is (= #{} (:effects export) (:capabilities export)))
    (is (= :none (:allocation export)))
    (is (= "(OrderedMap Keyword a) Keyword a -> (Result a CollectionError)" (:type export)))))

(deftest sh22-keyword-map-default-preserves-presence-and-inert-values
  (doseq [extension ["gravity" "qst"]]
    (let [base (request extension 'sh22-keyword-map-default-request)
          full (mapv #(vector (keyword "slot" (str "k" %)) %) (range 256))]
      (doseq [[entries key default expected]
              [[[] :missing 0 0] [[] :missing [] []] [[] :missing false false] [[] :missing nil nil]
               [[[:value false]] :value :fallback false] [[[:value nil]] :value :fallback nil]
               [[[:value 0]] :value 99 0] [[[:value []]] :value :fallback []]
               [[[:value {:nested [false nil]}]] :value nil {:nested [false nil]}]
               [[[:reader 1] [:syntax 2]] :missing {:fallback true} {:fallback true}]
               [[[:left/name false] [:right/name nil]] (keyword "left" "name") 99 false]
               [full :slot/k0 :fallback 0] [full :slot/k128 :fallback 128]
               [full :slot/k255 :fallback 255] [full :missing :fallback :fallback]]]
        (let [input (assoc base :arguments [(ordered entries) key default]) result (handle input)]
          (is (= :accepted (:status result)))
          (is (= expected (:value result)))
          (is (= lookup-facts (:facts result)))
          (is (= [(ordered entries) key default] (:arguments input) (get-in result [:identity-input :arguments])))
          (is (verified? input result))))
      (let [calls (atom 0) payload (fn [] (swap! calls inc))]
        (doseq [[entries key default expected]
                [[[[:stored payload]] :stored :fallback payload]
                 [[[:stored 1]] :missing payload payload]
                 [[[:stored nil] [:other payload]] :stored payload nil]]]
          (let [input (assoc base :arguments [(ordered entries) key default]) result (handle input)]
            (is (= :accepted (:status result)))
            (is (= expected (:value result)))
            (is (verified? input result))
            (is (zero? @calls)))))
      ;; The existing two-argument API retains its Option envelope and arity.
      (let [option-base (request extension 'sh22-keyword-map-option-request)]
        (doseq [[entries key expected]
                [[[[:reader false]] :reader {:artifact :gravity/option :variant :some :value false}]
                 [[[:reader nil]] :reader {:artifact :gravity/option :variant :some :value nil}]
                 [[] :reader {:artifact :gravity/option :variant :none}]]]
          (let [input (assoc option-base :arguments [(ordered entries) key]) result (handle input)]
            (is (= expected (:value result)))
            (is (verified? input result))))))))

(deftest sh22-keyword-map-default-composes-real-counter-results
  ;; Mirrors emitter p15-s23-summary-instruction's get-default/add/assoc motif.
  (doseq [extension ["gravity" "qst"]]
    (let [base (request extension 'sh22-keyword-map-default-request)
          assoc-base (request extension 'sh22-keyword-map-assoc-request)
          add-base (request extension 'sh22-keyword-map-counter-add-request)]
      (doseq [[seed-key seed-value query-key expected]
              [[:call 0 :call 1] [:call 41 :call 42] [:call 41 :return 1]
               [:call 9223372036854775806 :call Long/MAX_VALUE]]]
        (let [old (ordered [[:retained nil]])
              seed-input (assoc assoc-base :arguments [old seed-key seed-value]) seeded (handle seed-input)
              lookup-input (assoc base :arguments [(:value seeded) query-key 0]) looked-up (handle lookup-input)
              add-input (assoc add-base :arguments [(:value looked-up) 1]) added (handle add-input)
              update-input (assoc assoc-base :arguments [(:value seeded) query-key (get-in added [:value :value])])
              updated (handle update-input)
              check-input (assoc base :arguments [(:value updated) query-key :missing]) checked (handle check-input)]
          (is (= :accepted (:status seeded) (:status looked-up) (:status added) (:status updated) (:status checked)))
          (is (= :ok (get-in added [:value :variant])))
          (is (= expected (get-in added [:value :value]) (:value checked)))
          (is (= [[:retained nil]] (:entries old)))
          (is (= [[:retained nil] [seed-key seed-value]] (get-in seeded [:value :entries])))
          (doseq [[input result] [[seed-input seeded] [lookup-input looked-up] [add-input added]
                                  [update-input updated] [check-input checked]]]
            (is (verified? input result))))))))

(deftest sh22-keyword-map-default-rejects-invalid-domains-before-lookup
  (doseq [extension ["gravity" "qst"]]
    (let [base (request extension 'sh22-keyword-map-default-request)
          full (mapv #(vector (keyword "slot" (str "k" %)) %) (range 256))]
      (doseq [[entries key reason census unique?]
              (concat
               [[[[:reader 1] [:syntax 2] [:reader 3]] :reader :expected-ordered-map 1 true]
                [[[:reader 1] [:reader 2]] :missing :expected-ordered-map 1 true]]
               (for [bad-key [nil false 0 "late" 'late [] {}]]
                 [[[:reader 1] [bad-key 2]] :reader :expected-keyword-ordered-map 1 false])
               (for [bad-pair [nil [] [:syntax] [:syntax 2 :extra] '(:syntax 2) {:syntax 2}]]
                 [[[:reader 1] bad-pair] :reader :expected-keyword-ordered-map 1 false])
               (for [bad-needle [nil false 0 "reader" 'reader [] {}]]
                 [[[:reader 1]] bad-needle :expected-keyword 1 false]))]
        (let [input (assoc base :arguments [(ordered entries) key :fallback]) [result counts] (observe input)]
          (is (= :rejected (:status result)))
          (is (= "STD10003"
                 (get-in result [:diagnostics 0 :rule])))
          (is (= reason (get-in result [:diagnostics 0 :reason])))
          (is (= census (:census counts)))
          (is (= unique? (pos? (:unique counts))))
          (is (zero? (:lookup counts)))
          (is (verified? input result))))
      (doseq [bad [nil [] #{:reader} {:artifact :other :entries []}
                   {:artifact :gravity/ordered-map :entries '()}
                   {:artifact :gravity/ordered-map :entries [] :extra true}]]
        (let [[result counts] (observe (assoc base :arguments [bad :reader 0]))]
          (is (= :expected-ordered-map (get-in result [:diagnostics 0 :reason])))
          (is (= {:census 0 :unique 0 :lookup 0} counts))))
      (doseq [[function reason] [['sh22-keyword-map-default-duplicate-request :expected-ordered-map]
                                ['sh22-keyword-map-default-late-key-request :expected-keyword-ordered-map]
                                ['sh22-keyword-map-default-malformed-request :expected-keyword-ordered-map]]]
        (let [input (invoke (get @fixtures ["rejected" extension]) function [base]) [result counts] (observe input)]
          (is (= :rejected (:status result)))
          (is (= reason (get-in result [:diagnostics 0 :reason])))
          (is (zero? (:lookup counts)))
          (is (verified? input result)))))))

(deftest sh22-keyword-map-default-authority-and-substitution-fail-closed
  (doseq [extension ["gravity" "qst"]]
    (let [base (request extension 'sh22-keyword-map-default-request)]
      (doseq [[input rule reason]
              (concat (for [arguments [[] [(ordered [])] [(ordered []) :reader]
                                       [(ordered []) :reader 0 :extra]]]
                        [(assoc base :arguments arguments) "STD1001" :invalid-arity])
                      [[(assoc base :effects #{:memory/allocate}) "STD1002" :undeclared-library-effect]
                       [(assoc base :capabilities #{:fs/read}) "STD1003" :unexpected-library-capability]
                       [(assoc base :allocation-policy :bounded-by-result) "STD1001" :allocation-policy-mismatch]
                       [(assoc base :experimental-opt-in false) "STD1001" :malformed-library-request]
                       [(assoc base :profile :hosted) "STD1001" :malformed-library-request]])]
        (let [[result counts] (observe input)]
          (is (= :rejected (:status result)))
          (is (= rule (get-in result [:diagnostics 0 :rule])))
          (is (= reason (get-in result [:diagnostics 0 :reason])))
          (is (= {:census 0 :unique 0 :lookup 0} counts))
          (is (verified? input result))))
      (let [result (handle base)]
        (doseq [changed [(assoc result :value :fallback)
                         (assoc result :operation :ordered-map-get)
                         (assoc-in result [:facts :lookup] :truthiness)
                         (assoc-in result [:facts :allocation] :bounded-by-result)
                         (assoc-in result [:identity-input :arguments 2] :other)
                         (assoc-in result [:provenance :source-span :end] 65)]]
          (let [report (call 'sh22-verify-result [base changed])]
            (is (= :rejected (:status report)))
            (is (= :library-result-substitution (get-in report [:diagnostics 0 :reason])))))))))
