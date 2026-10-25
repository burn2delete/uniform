(ns gravity.self-hosting.sh22-textual-carrier-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

;; Native textual header validity and inert observation are independent of quotas.
(def ^:private root
  (delay (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh22_textual_carrier_test.clj")))]
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
         original-dispatch @dispatch
         observations (atom {:render 0 :vec 0 :headers []})
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
                  (apply original-dispatch inputs))))}
           thunk)]
     [result @observations])))

(deftest sh22-native-textual-validation-does-not-render-or-vectorize
  (doseq [value ["" "plain" "\ud83d\ude00" "\ud800" :name :ns/name
                 (keyword "" "name") (keyword "ns" "")
                 (keyword "ns" "\ud83d\ude00")]]
    (let [[result seen] (observe [value] #(census value))]
      (is (= {:status :accepted} result))
      (is (= 0 (:render seen) (:vec seen)))
      (is (= 1 (count (:headers seen)))))))

(deftest sh22-textual-validation-does-not-impose-size-or-occurrence-quotas
  (doseq [make-value [identity #(keyword (subs % 1))]
          size [65536 65537]]
    (let [value (make-value (apply str (repeat size "s")))
          [result seen] (observe [value] #(census [value value]))]
      (is (= {:status :accepted} result))
      (is (= 0 (:render seen) (:vec seen)))
      (is (= 2 (count (:headers seen))))
      (is (every? #(identical? value %) (:headers seen))))))

(deftest sh22-textual-headers-must-close-the-native-kind
  (doseq [value ["text" :name]
          header [[:unsupported nil nil nil nil nil]
                  [:symbol false nil 4 nil nil]]]
    (let [[result seen] (observe [value] {:header header} #(census value))]
      (is (= {:status :rejected :reason :carrier-invalid-textual-header} result))
      (is (= 0 (:render seen) (:vec seen)))
      (is (= 1 (count (:headers seen)))))))

(deftest sh22-keyword-native-null-spelling-and-unsupported-backing
  ;; Test-only host contours; no production reflection authority.
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
        (let [[result seen] (observe [value] #(census value))]
          (is (= {:status :accepted} result))
          (is (= 0 (:render seen) (:vec seen)))))
      (.set field value nil)
      (.set cache value nil)
      (let [[result seen] (observe [value] #(census value))]
        (is (= {:status :rejected :reason :carrier-invalid-textual-header} result))
        (is (= 0 (:render seen) (:vec seen))))
      (finally (.set field value original) (.set cache value nil)))))

(deftest sh22-validation-does-not-render-scalars-or-impose-structural-quotas
  (doseq [value [nil true \a (int 7) (long -8) 1/2 9223372036854775808N 'ns/name
                 (vec (repeat 513 "")) (nth (iterate vector "") 33)]]
    (let [[result seen] (observe [value] #(census value))]
      (is (= {:status :accepted} result))
      (is (= 0 (:render seen) (:vec seen))))))

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
      (is (verified? input result))
      (is (verified? nth-input selected)))))

(deftest sh22-textual-requests-and-results-have-no-scalar-quota
  (doseq [extension ["gravity" "qst"] make-value [identity #(keyword (subs % 1))]
          size [35000 65537]]
    (let [base (invoke (get @fixtures ["accepted" extension]) 'sh22-textual-carrier-request ["entry"])
          value (make-value (apply str (repeat size "t")))
          input (invoke (get @fixtures ["rejected" extension]) 'sh22-textual-carrier-wide-request [base value])
          [result seen] (observe [value] #(handle input))]
      (is (true? (call 'sh22-request-common-valid? [input])))
      (is (= :accepted (:status result)))
      (is (= [value] (:value result)))
      (is (= 0 (:render seen) (:vec seen)))
      (is (pos? (count (:headers seen))))
      (is (verified? input result))
      (is (not (verified? input (assoc result :value [:forged])))))))
