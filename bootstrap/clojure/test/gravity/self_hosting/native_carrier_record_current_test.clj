(ns gravity.self-hosting.native-carrier-record-current-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh08-function-call-kernel-support :as kernel-support]))

(defn- invoke-c7 [function arguments]
  (kernel-support/invoke-c7 function arguments))

(defn- observed-c7 [function arguments results]
  (let [scoped (ns-resolve 'gravity.bootstrap
                           'p15-s23-stage2-runtime-execute-function-scoped)
        original @scoped
        owner (Thread/currentThread)
        calls (atom [])
        result
        (with-redefs-fn
          {scoped
           (fn [runtime plan callee args scope]
             (if (and (identical? owner (Thread/currentThread))
                      (= :gravity-sh08-function-call-type-leaf (:engine runtime)))
               (do
                 (swap! calls conj callee)
                 (if (contains? results callee)
                   (get results callee)
                   (original runtime plan callee args scope)))
               (original runtime plan callee args scope)))}
          #(invoke-c7 function arguments))]
    {:result result :calls @calls}))

(deftest imported-core-native-validation-precedes-producers-and-inference
  ;; Authentication and inference are declared oracles; the actual native DATA
  ;; guard, diagnostics and memo transitions run in the current Gravity source.
  (let [request {:b47-context {:artifact-id :current-module}}
        memo (zipmap (range 1025) (repeat {:request {} :result {}}))
        core {:module {:namespace 'example.normalized} :nodes []}
        inferred {:status :accepted :scope :normalized-inference-control}
        observe #(observed-c7
                   'sh08-im-derive [request memo {:dependency-module true}]
                   {'sh08-ft-authenticated-core
                    {:status :accepted :core % :binding-table []}
                    'sh08-ft-function-type-core-artifact-with-imports inferred})
        callbacks (atom [])
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks conj :hash)
                   (throw (AssertionError. "core hash")))
                 (equals [_] (swap! callbacks conj :equals)
                   (throw (AssertionError. "core equality")))
                 (toString [] (swap! callbacks conj :render)
                   (throw (AssertionError. "core rendering"))))
        positive (observe core)]
    (is (= inferred (get-in positive [:result :result])))
    (is (= 1026 (count (get-in positive [:result :memo]))))
    (doseq [[bad expected]
            [[(with-meta core {:unexpected true}) :verification-carrier-metadata]
             [(assoc core :unused opaque) :verification-native-carrier]]]
      (let [{:keys [result calls]} (observe bad)]
        (is (= :rejected (get-in result [:result :status])))
        (is (= :module-type-producer-core-invalid
               (get-in result [:result :diagnostics 0 :reason])))
        (is (= expected (get-in result [:result :diagnostics 0 :actual-type :reason])))
        (is (not-any? #{'sh08-im-producers
                        'sh08-ft-function-type-core-artifact-with-imports} calls))
        (is (= request (get-in result [:memo :current-module :request])))))
    (is (empty? @callbacks))))

(deftest imported-result-retains-native-data-postcondition
  (let [request {:b47-context {:artifact-id :current-module}}
        accepted {:status :accepted :scope :direct-imported-functions-types-only}
        callbacks (atom [])
        opaque (proxy [Object] []
                 (toString [] (swap! callbacks conj :render)
                   (throw (AssertionError. "result rendering"))))
        observe #(get (observed-c7
                        'sh08-function-type-core-artifact [request]
                        {'sh08-im-derive {:result %}}) :result)]
    (is (= accepted (observe accepted)))
    (doseq [bad [(with-meta accepted {:unexpected true})
                 (assoc accepted :unused opaque)]]
      (let [result (observe bad)]
        (is (= :rejected (:status result)))
        (is (= :imported-type-result-carrier-invalid
               (get-in result [:diagnostics 0 :reason])))))
    (is (empty? @callbacks))))

;; The retired carrier-preflight alias is absent on current main. Exercise its
;; native DATA semantics directly, without restoring accounting or that alias.

(deftest carrier-native-safety-development-kernel
  (let [data #(invoke-c7 'sh08-vf-original-data [%])
        callbacks (atom [])
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks conj :hash) (throw (AssertionError. "candidate hash")))
                 (equals [_] (swap! callbacks conj :equals) (throw (AssertionError. "candidate equality")))
                 (toString [] (swap! callbacks conj :render) (throw (AssertionError. "candidate rendering"))))
        foreign (proxy [clojure.lang.APersistentMap] []
                  (count [] (swap! callbacks conj :count) (throw (AssertionError. "candidate count")))
                  (seq [] (swap! callbacks conj :seq) (throw (AssertionError. "candidate seq"))))
        lazy-tail (lazy-seq (swap! callbacks conj :forced) (list :value))
        invalid-symbol (clojure.lang.Symbol/intern nil nil)
        odd-map (clojure.lang.PersistentArrayMap. (object-array [:odd]))
        nil-map (clojure.lang.PersistentArrayMap. nil)
        backing (object-array [:self nil])
        cyclic (clojure.lang.PersistentArrayMap. backing)]
    (aset backing 1 cyclic)
    (doseq [value [opaque foreign {:unused opaque} [opaque] (list opaque)
                  (clojure.lang.Cons. :head lazy-tail)
                  (with-meta [] {:unused opaque})
                  invalid-symbol (list invalid-symbol)
                  (clojure.lang.PersistentArrayMap. (object-array [invalid-symbol :value]))
                  odd-map nil-map]]
      (is (= :rejected (:status (data value)))))
    (is (= {:status :rejected :reason :verification-native-cycle} (data cyclic)))
    (doseq [[namespace name] [[nil ""] ["" ""] ["qualified" ""] [nil "valid"]]]
      (is (= :accepted (:status (data (clojure.lang.Symbol/intern namespace name))))))
    (is (empty? @callbacks))))

(deftest carrier-native-extents-development-kernel
  ;; Historical thresholds are regression inputs, not current admission policy.
  (let [check #(invoke-c7 'sh08-vf-original-data [%])
        deep (reduce (fn [value _] [value]) :leaf (range 192))
        wide (vec (repeat 65537 :member))
        shared (vec (repeat 1024 :member))
        many (vec (repeat 1024 shared))
        scalar (String. (char-array 16777217 \x))
        callbacks (atom [])
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks conj :hash) (throw (AssertionError. "carrier hash")))
                 (equals [_] (swap! callbacks conj :equals) (throw (AssertionError. "carrier equality")))
                 (toString [] (swap! callbacks conj :render) (throw (AssertionError. "carrier rendering"))))]
    (doseq [value [deep wide many scalar [shared shared]]]
      (is (= {:status :accepted} (check value))))
    ;; Indexed children are pushed in order and visited from the front: index 0
    ;; is visited after every later sibling by the preserved native traversal.
    (doseq [value [(reduce (fn [value _] [value]) opaque (range 192))
                  (assoc wide 0 opaque)
                  (assoc many 0 (assoc shared 0 opaque))
                  [opaque scalar]]]
      (is (= {:status :rejected :reason :verification-native-carrier} (check value))))
    (is (= {:status :accepted} (check (sorted-map :key :value))))
    (is (= {:status :rejected :reason :verification-ratio-denominator-zero}
           (check (clojure.lang.Ratio. java.math.BigInteger/ONE java.math.BigInteger/ZERO))))
    (doseq [value [(subvec [1 2] 0 1)
                  (sorted-map-by (fn [& _]
                                   (swap! callbacks conj :comparator)
                                   (throw (AssertionError. "carrier comparator"))))]]
      (is (= {:status :rejected :reason :verification-native-carrier} (check value))))
    (is (empty? @callbacks))))

(deftest sh08-carrier-preflight-unmetered-development-kernel
  (let [check #(invoke-c7 'sh08-vf-original-data [%])
        deep (reduce (fn [value _] [value]) :leaf (range 192))
        shared {:leaf [:value]}
        callbacks (atom [])
        opaque (proxy [Object] []
                 (toString [] (swap! callbacks conj :render) (throw (AssertionError. "carrier rendering")))
                 (hashCode [] (swap! callbacks conj :hash) (throw (AssertionError. "carrier hashing"))))
        lazy-tail (lazy-seq (swap! callbacks conj :forced) (list :value))
        backing (object-array [:self nil])
        cyclic (clojure.lang.PersistentArrayMap. backing)]
    (aset backing 1 cyclic)
    (doseq [value [deep (vec (repeat 70000 :member)) [shared shared] (sorted-map "key" :value)]]
      (is (= {:status :accepted} (check value))))
    (doseq [[value reason] [[cyclic :verification-native-cycle]
                            [opaque :verification-native-carrier]
                            [{:unused opaque} :verification-native-carrier]
                            [(with-meta [] {:unused opaque}) :verification-carrier-metadata]
                            [(clojure.lang.Cons. :head lazy-tail) :verification-native-carrier]]]
      (is (= {:status :rejected :reason reason} (check value))))
    (is (empty? @callbacks))))

(deftest sh08-verification-receipt-physical-checks-development-kernel
  ;; This validates receipt DATA and its complete catalog, not B47 authority.
  (let [catalog (invoke-c7 'sh08-ft-b47-check-catalog [])
        checks (zipmap catalog (repeat true))
        id "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        receipt {:status :passed :checks checks :failed-checks []
                 :receipt-context :gravity/sh07-b47-verification-v16
                 :opaque-provenance-binding-id id :verification-digest id}
        valid? #(invoke-c7 'sh08-ft-verification-receipt-valid? [(assoc receipt :checks %)])
        all-true? #(invoke-c7 'sh08-ft-all-checks-true? [%])
        entries (mapcat identity checks)
        duplicate (clojure.lang.PersistentArrayMap. (object-array (concat entries entries)))
        callbacks (atom [])
        opaque (proxy [Object] []
                 (toString [] (swap! callbacks conj :render) (throw (AssertionError. "check rendering")))
                 (hashCode [] (swap! callbacks conj :hash) (throw (AssertionError. "check hashing"))))
        foreign (proxy [clojure.lang.APersistentMap] []
                  (count [] (swap! callbacks conj :count) (throw (AssertionError. "check count")))
                  (seq [] (swap! callbacks conj :seq) (throw (AssertionError. "check seq"))))]
    (doseq [value [checks (into (sorted-map) checks)]]
      (is (true? (all-true? value)))
      (is (true? (valid? value))))
    (doseq [value ["checks" nil [] (list true) {} duplicate
                   (assoc checks (first catalog) false)
                   (dissoc checks (first catalog)) (assoc checks :forged true)
                   (sorted-map "string-key" true) {:unused opaque} foreign
                   (with-meta checks {:unused opaque})]]
      (is (false? (valid? value))))
    (is (false? (all-true? duplicate)))
    (is (empty? @callbacks))))

(deftest verification-record-entry-order-development-kernel
  ;; Receipt DATA only; no coordinator witness or source authority is created.
  (let [catalog (invoke-c7 'sh08-ft-b47-check-catalog [])
        checks (zipmap catalog (repeat true))
        id "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        receipt {:status :passed :checks checks :failed-checks []
                 :receipt-context :gravity/sh07-b47-verification-v16
                 :opaque-provenance-binding-id id :verification-digest id}
        all-true? #(invoke-c7 'sh08-ft-all-checks-true? [%])
        valid? #(invoke-c7 'sh08-ft-verification-receipt-valid? [(assoc receipt :checks %)])
        callbacks (atom [])
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks conj :hash) (throw (AssertionError. "receipt key hash")))
                 (equals [_] (swap! callbacks conj :equals) (throw (AssertionError. "receipt key equality")))
                 (toString [] (swap! callbacks conj :render) (throw (AssertionError. "receipt rendering"))))
        entries (vec (mapcat identity checks))
        key (first catalog)
        duplicate (clojure.lang.PersistentArrayMap. (object-array (concat entries [key true])))
        hostile-key (clojure.lang.PersistentArrayMap. (object-array [opaque true]))
        cases [duplicate hostile-key (clojure.lang.PersistentArrayMap. (object-array [key]))
               (clojure.lang.PersistentArrayMap. nil)
               (with-meta checks {})
               (sorted-map-by (fn [& _]
                                (swap! callbacks conj :comparator)
                                (throw (AssertionError. "receipt comparator"))))]]
    (doseq [value [checks (into (sorted-map) checks)]]
      (is (true? (all-true? value)))
      (is (true? (valid? value))))
    (doseq [value cases]
      (is (false? (all-true? value)))
      (is (false? (valid? value))))
    (doseq [value [false nil 0 1 :true "true"]]
      (let [changed (assoc checks key value)]
        (is (false? (all-true? changed)))
        (is (false? (valid? changed)))))
    (is (false? (valid? (dissoc checks key))))
    (is (false? (valid? (assoc checks :unknown-catalog-entry true))))
    (is (empty? @callbacks))))
