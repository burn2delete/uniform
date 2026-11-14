(ns gravity.self-hosting.sh08-collection-call-inference-kernel-test
  "L5/C7 inference kernels execute current Gravity on normalized DATA."
  (:require [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh08-function-call-kernel-support :as support]))

(def ^:private finite-map
  {:kind :gravity.type/map :shape :literal
   :member-types [:gravity.type/keyword :gravity.type/integer]})

(defn- with-later-caller
  ([input caller-first?] (with-later-caller input caller-first? [finite-map]))
  ([input caller-first? argument-types]
  (let [callers (mapv (fn [index]
                        {:core-node-id (support/sha (+ 600 (* 10 index)))
                         :argument-node-ids [(support/sha (+ 601 (* 10 index)))]})
                      (range (count argument-types)))
        caller-edges (mapv #(hash-map :call-core-node-id (:core-node-id %)
                                     :classification :local-function
                                     :callee-function-syntax-id (support/sha 602)) callers)
        parameter (support/sha 603)
        types (into (:types input)
                    (map (fn [call type] [(first (:argument-node-ids call)) type])
                         callers argument-types))
        functions {(support/sha 602)
                   {:function-id (support/sha 602)
                    :parameters [{:binding-id parameter :type :gravity.type/unknown}]}}
        calls (if caller-first? (conj callers (:call input)) (into [(:call input)] callers))
        edges (if caller-first? (conj caller-edges (:edge input)) (into [(:edge input)] caller-edges))
        result (support/invoke-c7
                'sh08-ft-call-constraints-with-context
                [calls edges (:nodes input) types {} functions
                 {:ordering-core (:core input) :ordering-bindings (:bindings input)}])]
    (assoc result :observed-parameter (get (:binding-types result) parameter)))))

(defn- call-constraints [receiver caller-first?]
  (with-later-caller (support/map-keys-input receiver) caller-first?))

(deftest unresolved-map-keys-does-not-starve-later-call-evidence
  (let [before (call-constraints :gravity.type/unknown false)
        after (call-constraints :gravity.type/unknown true)]
    (is (= finite-map (:observed-parameter before) (:observed-parameter after)))
    ;; This is still an unresolved replay, never an accepted keys application.
    (is (= [:map-keys-finite-keyword-map-type-required]
           (mapv :reason (:diagnostics before))
           (mapv :reason (:diagnostics after))))
    (is (= ["C7-VERIFY"] (mapv :rule (:diagnostics before))))
    (is (= 1 (count (:constraints before)) (count (:constraints after))))))

(deftest known-invalid-map-keys-preserves-first-rejection
  (doseq [[receiver reason]
          [[:gravity.type/string :map-keys-finite-keyword-map-type-required]
           [:gravity.type/map :private-map-keys-source-mismatch]
           [(assoc finite-map :member-types
                   [:gravity.type/string :gravity.type/integer])
            :map-keys-finite-keyword-map-type-required]]]
    (let [result (call-constraints receiver false)]
      (is (nil? (:observed-parameter result)))
      (is (empty? (:constraints result)))
      (is (= [reason]
             (mapv :reason (:diagnostics result)))))))

(deftest later-call-evidence-retains-conflicts-and-requires-a-witness
  (let [input (support/map-keys-input :gravity.type/unknown)
        incompatible (assoc finite-map :member-types
                            [:gravity.type/keyword :gravity.type/string])
        conflict (with-later-caller input false [finite-map incompatible])
        missing (with-later-caller input false [])]
    (is (= finite-map (:observed-parameter conflict)))
    (is (= [:map-keys-finite-keyword-map-type-required :call-argument-type-mismatch]
           (mapv :reason (:diagnostics conflict))))
    (is (= incompatible (:actual (last (:diagnostics conflict)))))
    (is (nil? (:observed-parameter missing)))
    (is (empty? (:constraints missing)))
    (is (= [:map-keys-finite-keyword-map-type-required]
           (mapv :reason (:diagnostics missing))))))

(deftest unresolved-core-set-does-not-starve-later-call-evidence
  (let [input (support/list-set-input :gravity.type/unknown)
        before (with-later-caller input false)
        after (with-later-caller input true)]
    (is (= finite-map (:observed-parameter before) (:observed-parameter after)))
    ;; The existing Set constructor owner handles its operand diagnostics.
    (is (= []
           (mapv :reason (:diagnostics before))
           (mapv :reason (:diagnostics after))))
    (is (= 1 (count (:constraints before)) (count (:constraints after))))))

(deftest unresolved-unary-helper-distinguishes-missing-and-transferring-types
  (let [invoke #(support/invoke-c7 'sh08-ft-unresolved-unary-argument? [%1 %2])
        call {:argument-node-ids [:argument]}]
    (doseq [types [{} {:argument nil} {:argument :gravity.type/unknown}]]
      (is (true? (invoke call types))))
    (doseq [type [:gravity.type/never :gravity.type/string :gravity.type/map finite-map
                  (assoc finite-map :member-types
                         [:gravity.type/keyword :gravity.type/unknown])]]
      (is (false? (invoke call {:argument type}))))
    (doseq [[bad-call bad-types] [[nil {}] [[] {}] [call []] [call nil]
                                 [{:argument-node-ids []} {}]
                                 [{:argument-node-ids [:argument :other]} {}]
                                 [{:argument-node-ids '(:argument)} {}]
                                 [{:argument-node-ids [99]} {}]
                                 [{:argument-node-ids [nil]} {}]]]
      (is (false? (invoke bad-call bad-types))))
    (let [callbacks (atom 0)
          opaque (proxy [Object] []
                   (hashCode [] (swap! callbacks inc) (throw (AssertionError. "unexpected hash")))
                   (equals [_] (swap! callbacks inc) (throw (AssertionError. "unexpected equality")))
                   (toString [] (swap! callbacks inc) (throw (AssertionError. "unexpected formatting"))))]
      (doseq [[bad-call bad-types] [[opaque {}] [call opaque]
                                   [call {:argument opaque}]
                                   [{:argument-node-ids [opaque]} {}]
                                   [{:argument-node-ids
                                     (lazy-seq (swap! callbacks inc)
                                               (throw (AssertionError. "unexpected seq")))} {}]]]
        (is (false? (invoke bad-call bad-types))))
      (is (zero? @callbacks)))))

(deftest malformed-or-spoofed-calls-do-not-defer-validation
  (doseq [base [(support/map-keys-input :gravity.type/unknown)]
          change [#(assoc-in % [:bindings :ordering-binding :binding-class] :namespace)
                  #(assoc-in % [:bindings :ordering-binding :namespace] 'other.core)
                  #(assoc-in % [:call :argument-node-ids] [])
                  #(assoc-in % [:call :argument-node-ids] [:arg0 :other])
                  #(assoc-in % [:call :argument-node-ids] [99])
                  #(assoc-in % [:types :arg0] :gravity.type/integer)
                  #(assoc-in % [:types :arg0] :gravity.type/never)]]
    (let [result (with-later-caller (change base) false)]
      (is (nil? (:observed-parameter result)))
      (is (empty? (:constraints result)))
      (is (seq (:diagnostics result))))))

(defn- fixture [family basename extension]
  (support/path (str "bootstrap/clojure/fixtures/self-hosting/sh-08/"
                     family "/" basename extension)))

(deftest collection-caller-source-extension-parity
  (doseq [[family basenames]
          [["accepted" ["collection-caller-inference"]]
           ["rejected" ["collection-caller-string-key"]]]
          basename basenames]
    (is (= (slurp (fixture family basename ".gravity"))
           (slurp (fixture family basename ".qst"))))))
