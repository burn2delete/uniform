(ns gravity.self-hosting.sh22-symbol-carrier-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

;; Carrier validation does not render Symbols or account for their spelling size.
(def ^:private root
  (delay (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh22_symbol_carrier_test.clj")))]
           (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file))))))
(defn- path [relative] (str (io/file @root relative)))
(defn- compile-plan [relative]
  (let [source (path relative) emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! source :jvm))]
    (b/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source))))
(def ^:private engine (delay (compile-plan "bootstrap/gravity/src/gravity/stdlib/self_hosting_core.gravity")))
(defn- fixture-path [family extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-22/" family "/coordinator-symbol-carrier." extension))
(def ^:private fixtures
  (delay (into {} (for [family ["accepted" "rejected"] extension ["gravity" "qst"]]
                   [[family extension] (compile-plan (fixture-path family extension))]))))
(defn- invoke [plan function arguments]
  (b/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh22-symbol-carrier :compiler-artifact-plan? true} plan function arguments))
(defn- call [function arguments] (invoke @engine function arguments))
(defn- census [value] (call 'sh22-carrier-preflight [value]))
(defn- handle [input] (call 'sh22-handle-request [input]))
(defn- verified? [input result] (= :passed (:status (call 'sh22-verify-result [input result]))))
(defn- observe [value thunk]
  @engine
  (let [dispatch (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-invoke-builtin)
        original @dispatch observations (atom {:render 0 :units 0})
        result (with-redefs-fn
                 {dispatch (fn [& inputs]
                             (let [[callee arguments] (take-last 2 inputs)]
                               (when (some #(identical? value %) arguments)
                                 (case callee
                                   str (swap! observations update :render inc)
                                   bootstrap-symbol-utf16-unit-count (swap! observations update :units inc)
                                   nil)))
                             (apply original inputs))}
                 thunk)]
    [result @observations]))

(deftest sh22-native-symbol-validation-does-not-render-or-count-spelling
  (doseq [value ['compiler/entry (symbol "unicode" "\ud83d\ude00")
                 (with-meta 'compiler/entry {:inert '(unresolved/call)})]]
    (let [[result observed] (observe value #(census value))]
      (is (= {:status :accepted} result))
      (is (= {:render 0 :units 0} observed)))))

(deftest sh22-symbol-validation-has-no-size-or-shared-occurrence-quota
  (doseq [size [65536 65537]]
    (let [value (symbol (apply str (repeat size "s")))
          [result observed] (observe value #(census [value value]))]
      (is (= {:status :accepted} result))
      (is (= {:render 0 :units 0} observed)))))

(deftest sh22-symbol-producers-compose-through-persistent-library-results
  (doseq [family ["accepted" "rejected"]]
    (is (= (slurp (path (fixture-path family "gravity")))
           (slurp (path (fixture-path family "qst"))))))
  (doseq [extension ["gravity" "qst"]]
    (let [input (invoke (get @fixtures ["accepted" extension]) 'sh22-symbol-carrier-request [])
          value (get-in input [:arguments 1])
          [result observed] (observe value #(handle input))
          nth-input (assoc input :operation :vector-nth :arguments [(:value result) 0]
                          :effects #{} :allocation-policy :none)
          selected (handle nth-input)]
      (is (= 'compiler/entry value))
      (is (= :accepted (:status result) (:status selected)))
      (is (= [value] (:value result)))
      (is (= value (:value selected)))
      (is (= {:render 0 :units 0} observed))
      (is (verified? input result))
      (is (verified? nth-input selected)))))

(deftest sh22-symbol-requests-and-results-have-no-scalar-quota
  (doseq [extension ["gravity" "qst"] size [35000 65537]]
    (let [base (invoke (get @fixtures ["accepted" extension]) 'sh22-symbol-carrier-request [])
          value (symbol (apply str (repeat size "s")))
          input (invoke (get @fixtures ["rejected" extension]) 'sh22-symbol-carrier-wide-request [base value])
          [result observed] (observe value #(handle input))]
      (is (true? (call 'sh22-request-common-valid? [input])))
      (is (= :accepted (:status result)))
      (is (= [value] (:value result)))
      (is (= {:render 0 :units 0} observed))
      (is (verified? input result))
      (is (not (verified? input (assoc result :value [:forged])))))))
