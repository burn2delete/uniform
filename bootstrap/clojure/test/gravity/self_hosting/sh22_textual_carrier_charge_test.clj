(ns gravity.self-hosting.sh22-textual-carrier-charge-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

;; BOOT2/STD4/D8/D9: this counts native UTF16 carrier units, not Unicode scalars.
;; Logical observation fee: result 7, common plan paths 30, Keyword extra 37.
(def ^:private fee (+ 7 30 37))
(def ^:private root
  (delay (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh22_textual_carrier_charge_test.clj")))]
           (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file))))))
(defn- path [relative] (str (io/file @root relative)))
(defn- compile-plan [relative]
  (let [source (path relative) emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! source :jvm))]
    (b/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source))))
(def ^:private engine (delay (compile-plan "bootstrap/gravity/src/gravity/stdlib/self_hosting_core.gravity")))
(defn- fixture-path [family extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-22/" family "/coordinator-textual-carrier." extension))
(def ^:private fixtures
  (delay (into {} (for [family ["accepted" "rejected"] extension ["gravity" "qst"]]
                   [[family extension] (compile-plan (fixture-path family extension))]))))
(defn- invoke [plan function arguments]
  (b/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh22-textual-carrier :compiler-artifact-plan? true} plan function arguments))
(defn- call [function arguments] (invoke @engine function arguments))
(defn- census [value] (call 'sh22-carrier-preflight [value]))
(defn- handle [input] (call 'sh22-handle-request [input]))
(defn- verified? [input result] (= :passed (:status (call 'sh22-verify-result [input result]))))

(defn- observe
  ([values thunk] (observe values {} thunk))
  ([values options thunk]
   @engine
   (let [dispatch (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-invoke-builtin)
         execute (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-instruction-scoped)
         original-dispatch @dispatch original-execute @execute
         observations (atom {:render 0 :vec 0 :headers [] :funding [] :injected 0})
         result
         (with-redefs-fn
           {dispatch
            (fn [& inputs]
              (let [[callee arguments] (take-last 2 inputs)
                    selected? (some (fn [value] (some #(identical? value %) arguments)) values)]
                (when selected?
                  (case callee
                    str (swap! observations update :render inc)
                    bootstrap-native-carrier-header (swap! observations update :headers conj (first arguments))
                    nil))
                (when (and (= 'vec callee) (some string? arguments))
                  (swap! observations update :vec inc))
                (if (and selected? (= 'bootstrap-native-carrier-header callee) (contains? options :header))
                  (:header options)
                  (apply original-dispatch inputs))))
            execute
            (fn [runtime plan env instruction scope]
              (when (and (= :builtin-call (:op instruction))
                         (= 'bootstrap-native-carrier-header (:function instruction)))
                (swap! observations update :funding conj
                       {:before (get env 'observation-left)
                        :after (get env 'next-observation-left)
                        :fee (get env 'header-fee)}))
              ;; Test-local exhaustion only; the production API has no budget option.
              (let [inject? (and (contains? options :pool) (= :loop (:op instruction))
                                 (some #(= 'observation-left (:name %)) (:bindings instruction)))
                    instruction
                    (if inject?
                      (do (swap! observations update :injected inc)
                          (update instruction :bindings
                                  #(mapv (fn [binding]
                                           (if (= 'observation-left (:name binding))
                                             (assoc binding :expr {:op :literal :value (:pool options)})
                                             binding)) %)))
                      instruction)]
                (original-execute runtime plan env instruction scope)))}
           thunk)]
     [result @observations])))

(defn- paid? [{:keys [before after] observed-fee :fee}]
  (and (= fee observed-fee) (integer? before) (integer? after)
       (<= 0 after) (= fee (- before after))))

(deftest sh22-native-textual-census-does-not-render-or-vectorize
  (doseq [value ["" "plain" "\ud83d\ude00" "\ud800" :name :ns/name
                 (keyword "" "name") (keyword "ns" "")
                 (keyword "ns" "\ud83d\ude00")]]
    (let [expected (.length ^String (str value))
          [result seen] (observe [value] #(census value))]
      (is (= {:status :accepted :nodes 1 :scalar-units expected} result))
      (is (= 0 (:render seen) (:vec seen)))
      (is (= 1 (count (:headers seen)) (count (:funding seen))))
      (is (every? paid? (:funding seen)))
      (is (= (* 65536 fee) (get-in seen [:funding 0 :before]))))))

(deftest sh22-textual-census-retains-inclusive-and-shared-occurrence-limits
  (doseq [make-value [identity #(keyword (subs % 1))]
          size [65536 65537]]
    (let [value (make-value (apply str (repeat size "s")))
          [result seen] (observe [value] #(census value))]
      (is (= (if (= size 65536) :accepted :rejected) (:status result)))
      (is (= (if (= size 65536) size :carrier-scalar-bound)
             (if (= size 65536) (:scalar-units result) (:reason result))))
      (is (= 0 (:render seen) (:vec seen)))
      (is (= 1 (count (:headers seen))))
      (is (every? paid? (:funding seen)))))
  (doseq [value [(apply str (repeat 32768 "s")) (keyword (apply str (repeat 32767 "k")))]]
    (let [[result seen] (observe [value] #(census [value value]))]
      (is (= {:status :accepted :nodes 3 :scalar-units 65536} result))
      (is (= 2 (count (:headers seen))))
      (is (every? #(identical? value %) (:headers seen)))
      (is (= (mapv :after (butlast (:funding seen)))
             (mapv :before (rest (:funding seen)))))
      (is (= :carrier-scalar-bound (:reason (census [value value "x"])))))))

(deftest sh22-header-pool-refuses-before-unfunded-observation
  (doseq [[pool headers status] [[0 0 :rejected] [(dec fee) 0 :rejected]
                                [fee 1 :rejected] [(* 2 fee) 2 :accepted]]]
    (let [value "shared" [result seen] (observe [value] {:pool pool} #(census [value value]))]
      (is (= 1 (:injected seen)))
      (is (= headers (count (:headers seen)) (count (:funding seen))))
      (is (= status (:status result)))
      (is (every? paid? (:funding seen)))
      (when (= :rejected status)
        (is (= :carrier-header-observation-bound (:reason result)))))))

(deftest sh22-textual-header-metrics-must-be-exact-kind-and-nonnegative-integer
  (doseq [value ["text" :name]
          header [[:unsupported nil nil nil nil nil]
                  [:symbol false nil 4 nil nil]
                  [(if (string? value) :string :keyword) false nil nil nil nil]
                  [(if (string? value) :string :keyword) false nil -1 nil nil]
                  [(if (string? value) :string :keyword) false nil 1/2 nil nil]]]
    (let [[result seen] (observe [value] {:header header} #(census value))]
      (is (= {:status :rejected :reason :carrier-invalid-textual-header} result))
      (is (= 0 (:render seen) (:vec seen)))
      (is (= 1 (count (:headers seen))))
      (is (every? paid? (:funding seen)))))
  (let [[result _] (observe ["text"] {:header [:string false nil 9223372036854775808N nil nil]}
                            #(census "text"))]
    (is (= {:status :rejected :reason :carrier-scalar-bound} result))))

(deftest sh22-keyword-native-null-spelling-and-unsupported-backing
  ;; Test-only field mutation creates native host contours absent from ordinary
  ;; Keyword construction. It grants no payload legality or production reflection.
  (let [value (keyword (str "sh22-textual-native-" (System/nanoTime)))
        field (.getField clojure.lang.Keyword "sym")
        cache (.getDeclaredField clojure.lang.Keyword "_str")
        original (.get field value)]
    (is (.trySetAccessible field))
    (is (.trySetAccessible cache))
    (try
      (doseq [namespace [nil "" "ns"]]
        (.set field value (clojure.lang.Symbol/intern namespace nil))
        (.set cache value nil)
        (let [expected (.length ^String (str value))
              [result seen] (observe [value] #(census value))]
          (is (= {:status :accepted :nodes 1 :scalar-units expected} result))
          (is (= 0 (:render seen) (:vec seen)))
          (is (every? paid? (:funding seen)))))
      (.set field value nil)
      (.set cache value nil)
      (let [[result seen] (observe [value] #(census value))]
        (is (= {:status :rejected :reason :carrier-invalid-textual-header} result))
        (is (= 0 (:render seen) (:vec seen)))
        (is (every? paid? (:funding seen))))
      (finally (.set field value original) (.set cache value nil)))))

(deftest sh22-other-scalar-and-structural-census-behavior-is-preserved
  (doseq [value [nil true \a (int 7) (long -8) 1/2 9223372036854775808N 'ns/name]]
    (let [[result seen] (observe [value] #(census value))]
      (is (= {:status :accepted :nodes 1 :scalar-units (.length ^String (str value))} result))
      (is (empty? (:headers seen)))
      (is (empty? (:funding seen)))))
  (is (= :carrier-width-bound (:reason (census (vec (repeat 513 ""))))))
  (is (= :carrier-depth-bound (:reason (census (nth (iterate vector "") 33)))))
  (let [execute (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-instruction-scoped)
        original @execute result
        (with-redefs-fn
          {execute (fn [runtime plan env instruction scope]
                     (original runtime plan env
                       (if (and (= :loop (:op instruction))
                                (some #(= 'nodes (:name %)) (:bindings instruction)))
                         (update instruction :bindings
                           #(mapv (fn [binding] (if (= 'nodes (:name binding))
                                                (assoc binding :expr {:op :literal :value 65536})
                                                binding)) %))
                         instruction) scope))}
          #(census "unvisited"))]
    (is (= {:status :rejected :reason :carrier-node-bound} result))))

(deftest sh22-textual-producers-compose-through-persistent-library-results
  (doseq [family ["accepted" "rejected"]]
    (is (= (slurp (path (fixture-path family "gravity"))) (slurp (path (fixture-path family "qst"))))))
  (doseq [extension ["gravity" "qst"] value ["compiler-entry" :compiler/entry]]
    (let [input (invoke (get @fixtures ["accepted" extension]) 'sh22-textual-carrier-request [value])
          [result seen] (observe [value] #(handle input))
          nth-input (assoc input :operation :vector-nth :arguments [(:value result) 0]
                           :effects #{} :allocation-policy :none)
          selected (handle nth-input)]
      (is (= :accepted (:status result) (:status selected)))
      (is (= [value] (:value result)))
      (is (= value (:value selected)))
      (is (= 0 (:render seen) (:vec seen)))
      (is (pos? (count (:headers seen))))
      (is (every? paid? (:funding seen)))
      (is (verified? input result))
      (is (verified? nth-input selected)))))

(deftest sh22-textual-request-and-result-refusals-stay-bounded
  (doseq [extension ["gravity" "qst"] make-value [identity #(keyword (subs % 1))]
          [size rule reason valid-input?]
          [[35000 "STD10003" :result-carrier-bound-exceeded true]
           [65537 "STD1001" :malformed-library-request false]]]
    (let [base (invoke (get @fixtures ["accepted" extension]) 'sh22-textual-carrier-request ["entry"])
          value (make-value (apply str (repeat size "t")))
          input (invoke (get @fixtures ["rejected" extension]) 'sh22-textual-carrier-wide-request [base value])
          [result seen] (observe [value] #(handle input))]
      (is (= valid-input? (call 'sh22-request-common-valid? [input])))
      (is (= :rejected (:status result)))
      (is (= rule (get-in result [:diagnostics 0 :rule])))
      (is (= reason (get-in result [:diagnostics 0 :reason])))
      (is (= 0 (:render seen) (:vec seen)))
      (is (pos? (count (:headers seen))))
      (is (every? paid? (:funding seen)))
      (is (= :accepted (:status (census result))))
      (is (verified? input result))
      (is (not (verified? input (assoc result :status :accepted)))))))
