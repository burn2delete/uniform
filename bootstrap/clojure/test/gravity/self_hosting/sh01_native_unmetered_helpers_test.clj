(ns gravity.self-hosting.sh01-native-unmetered-helpers-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as b]
            [gravity.digest :as digest])
  (:import (java.lang.reflect InvocationHandler Proxy)
           (java.math BigDecimal BigInteger)))

(def ^:private path "<native-unmetered-helper-test>")
(defn- failure [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error (:missing-fact (ex-data error)))))
(defn- record [value] (b/p15-s23-c6c10-canonical-record path value))
(defn- check [value] (b/p15-s23-trusted-carrier-validation value :default-only))
(defn- input [value] (b/p15-s23-c6c10-carrier-validation path value))
(defn- field! [value name child]
  (let [field (.getDeclaredField (class value) name)]
    (.setAccessible field true)
    (.set field value child))
  value)
(defn- cycle-map []
  (let [array (object-array [:self nil]) value (clojure.lang.PersistentArrayMap. array)]
    (aset array 1 value)
    value))

(deftest native-helpers-accept-former-admission-boundaries
  (doseq [value [(vec (range 129)) (nth (iterate vector nil) 65)
                 (vec (repeat 65537 nil)) (apply str (repeat 65537 "x"))
                 (.shiftLeft BigInteger/ONE 256)
                 (/ (.shiftLeft BigInteger/ONE 256) 3N)
                 (BigDecimal. BigInteger/ONE 65537)
                 (BigDecimal. BigInteger/ONE Integer/MIN_VALUE)
                 (vec (repeat 129 (apply str (repeat 65536 "x"))))]]
    (is (= :passed (:status (check value))))
    (is (= :passed (:status (input value))))
    (is (map? (record value))))
  (let [deep (nth (iterate vector nil) 10000)]
    (is (= :passed (:status (check deep))))
    (is (= :passed (:status (input deep))))
    (let [stats (atom {:nodes 0 :maximum-depth 0 :maximum-width 0
                       :scalar-bytes 0 :maximum-scalar-bytes 0 :maximum-integer-bits 0})]
      ;; The traversal itself does not consume one host stack frame per child.
      (is (= :vector (first (b/p15-s23-c6c10-canonical-form* path stats 0 deep))))
      (is (= 10000 (:maximum-depth @stats)))))
  (is (= (check [1 2 3])
         (b/p15-s23-trusted-carrier-validation [1 2 3] :default-only 0 0 0))))

(deftest native-helpers-retain-canonical-identity-and-observations
  (let [value {:a [1 2] :b #{3 4}} result (record value)]
    (is (= [:map [[:entry [:keyword nil "a"] [:vector [[:integer "1"] [:integer "2"]]]]
                  [:entry [:keyword nil "b"] [:set [[:integer "3"] [:integer "4"]]]]]]
           (:form result)))
    (is (= {:nodes 9 :maximum-depth 2 :maximum-width 2 :scalar-bytes 2
            :maximum-scalar-bytes 1 :maximum-integer-bits 3} (:stats result)))
    (is (= (str "sha256:" (digest/sha256-hex (:text result)))
           (b/p15-s23-c6c10-canonical-digest path value)))
    (is (= (:form result) (:form (record (into (sorted-map) value)))))
    (is (= (:form (record #{1 2})) (:form (record (sorted-set 2 1)))))
    (is (not= (:form (record [1 2])) (:form (record (list 1 2)))))
    (is (not= (:form (record 1.25M)) (:form (record 1.250M)))))
  (let [shared {:a [1 2]} value [shared shared]
        tail (list :tail) list-alias (conj tail tail)]
    (is (= :passed (:status (check value))))
    (is (= :passed (:status (input value))))
    (is (= (:form (record [shared {:a [1 2]}])) (:form (record value))))
    (is (= :passed (:status (check list-alias))))
    (is (= :passed (:status (input list-alias))))
    (is (map? (record list-alias))))
  (is (= :passed (:status (input 1.0))))
  (is (= :canonical-value-domain (failure #(record 1.0)))))

(deftest native-helpers-reject-true-cycles-without-structural-equality
  (let [map-cycle (cycle-map)
        list-cycle (list :head :tail)
        cons-cycle (clojure.lang.Cons. :head nil)]
    (field! list-cycle "_rest" list-cycle)
    (field! cons-cycle "_more" cons-cycle)
    (doseq [value [map-cycle list-cycle cons-cycle]]
      (is (= :cyclic-carrier (:reason (check value)))))
    (doseq [value [map-cycle list-cycle]]
      (is (= :cyclic-canonical-carrier (failure #(record value))))
      (is (= :cyclic-input-carrier (failure #(input value)))))))

(defn- foreign-map [deny]
  (Proxy/newProxyInstance (.getClassLoader clojure.lang.IPersistentMap)
    (into-array Class [clojure.lang.IPersistentMap clojure.lang.IObj])
    (reify InvocationHandler (invoke [_ _ _ _] (deny)))))

(deftest native-helpers-reject-foreign-callbacks-and-comparators-unforced
  (let [calls (atom 0)
        deny (fn [] (swap! calls inc) (throw (AssertionError. "foreign callback")))
        foreign (proxy [Object clojure.lang.Named clojure.lang.IObj] []
                  (getName [] (deny)) (getNamespace [] (deny)) (meta [] (deny))
                  (withMeta [_] (deny)) (hashCode [] (deny)) (equals [_] (deny))
                  (toString [] (deny)))
        opaque (foreign-map deny)
        lazy-tail (lazy-seq (deny) (list :unreachable))
        foreign-number (proxy [BigInteger] ["1"]
                         (abs [] (deny)) (bitLength [] (deny)) (toString [] (deny)))
        comparator (fn [_ _] (deny))]
    (doseq [value [foreign opaque foreign-number lazy-tail (array-map foreign 1) [foreign]]]
      (is (= :rejected (:status (check value))))
      (is (some? (failure #(record value))))
      (is (some? (failure #(input value)))))
    (doseq [value [(sorted-map-by comparator) (sorted-set-by comparator)]]
      (is (= :rejected (:status (check value))))
      (is (= :default-native-comparator (failure #(record value))))
      (is (= :default-native-comparator (failure #(input value)))))
    (doseq [kind [clojure.lang.PersistentTreeSet clojure.lang.PersistentHashSet]]
      (let [constructor (.getDeclaredConstructor kind
                          (into-array Class [clojure.lang.IPersistentMap clojure.lang.IPersistentMap]))
            _ (.setAccessible constructor true)
            value (.newInstance constructor (object-array [nil opaque]))]
        (is (= :rejected (:status (check value))))
        (is (= :exact-persistent-set-class (failure #(record value))))
        (is (= :exact-persistent-carrier-class (failure #(input value))))))
    (let [value (clojure.lang.Cons. :head lazy-tail)]
      (is (= :untrusted-list-tail-class (:reason (check value)))))
    (doseq [value [(with-meta [1] opaque) (with-meta {:a 1} opaque)
                   (with-meta #{1} opaque) (with-meta '(1) opaque)
                   (with-meta 'name opaque)]]
      (is (= :carrier-metadata (:reason (check value))))
      (is (= :metadata-free-canonical-value (failure #(record value))))
      (is (= :metadata-free-input-carrier (failure #(input value)))))
    (is (zero? @calls))))

(deftest native-helpers-retain-unicode-and-named-value-validity
  (doseq [[value missing] [[(str (char 0xd800)) :well-formed-unicode-scalar-string]
                            [(char 0xd800) :unicode-scalar-character]
                            [(keyword "bad name") :canonical-keyword-components]
                            [(symbol "bad name") :canonical-symbol-components]]]
    (is (= missing (failure #(record value)))))
  (doseq [value ["λ🙂" 'ns/name :ns/name '/ (subvec [0 1 2] 1 3)]]
    (is (map? (record value))))
  (let [calls (atom 0) backing (Proxy/newProxyInstance (.getClassLoader clojure.lang.IPersistentVector)
                               (into-array Class [clojure.lang.IPersistentVector])
                               (reify InvocationHandler
                                 (invoke [_ _ _ _] (swap! calls inc) (throw (AssertionError. "vector callback")))))
        view (clojure.lang.APersistentVector$SubVector. nil backing 0 1)]
    (is (= :exact-persistent-vector-class (failure #(record view))))
    (is (zero? @calls))))

(deftest native-helpers-reject-malformed-array-map-storage
  (doseq [array [nil (object-array [:odd]) (object-array [:a 1 :odd])]]
    (let [value (clojure.lang.PersistentArrayMap. array)]
      (is (= :untrusted-map-class-or-comparator (:reason (check value))))
      (is (= :exact-persistent-map-class (failure #(record value))))
      (is (= :exact-persistent-carrier-class (failure #(input value)))))))

(defn- request-chain [size]
  (let [root {:digest-ref (dec size)}
        template {:kind :gravity/test-chain :tip {:digest-ref (- size 2)} :artifact-id root}]
    {:status :accepted :artifact-template template
     :digest-requests
     (mapv (fn [ordinal]
             {:algorithm :sha256 :encoding :gravity/canonical-edn-v1
              :key ordinal :ordinal ordinal
              :depends-on (if (zero? ordinal) [] [(dec ordinal)])
              :preimage (cond
                          (= ordinal (dec size))
                          {:domain :gravity/c6-c10-checked-core-artifact-v1
                           :semantic-artifact (dissoc template :artifact-id)}
                          (zero? ordinal) {:chain-seed 0}
                          :else {:chain-link {:digest-ref (dec ordinal)}})})
           (range size))
     :digest-graph-root root :digest-graph-roots [root] :diagnostics []
     :authority {:semantic-owner :gravity-source
                 :host-role :generic-validation-hashing-and-instantiation
                 :scope :bounded-pure-c6-c10 :self-hosted? false}}))

(deftest native-digest-request-graph-retains-semantic-constraints-without-count-cap
  (let [graph (request-chain 2049)
        validate #(b/p15-s23-c6c10-validate-digest-request-graph! path %)]
    (is (= 2049 (:request-count (validate graph))))
    (is (= :exact-ordered-digest-request-schema
           (failure #(validate (assoc-in graph [:digest-requests 0 :ordinal] 1)))))
    (is (= :exact-prior-digest-request-dependency-closure
           (failure #(validate (assoc-in graph [:digest-requests 1 :depends-on] [])))))
    (is (= :bounded-prior-digest-reference
           (failure #(validate (assoc-in graph [:digest-requests 1 :preimage :chain-link :digest-ref] 1)))))
    (is (= :root-bound-gravity-artifact-template
           (failure #(validate (assoc-in graph [:artifact-template :tip :digest-ref] 0)))))))

(deftest native-source-context-accepts-large-valid-source-and-retains-binding
  (let [source (apply str (repeat 1048577 "a"))
        source-path (str (apply str (repeat 70000 "p")) ".gravity")
        target (keyword (apply str (repeat 257 "t")))
        context (b/p15-s23-stage2-gravity-checked-core-context source-path source target)]
    (is (= source (:source-text context)))
    (is (= (str "sha256:" (digest/sha256-hex source)) (:source-content-hash context)))
    (is (= context (b/p15-s23-c6c10-validate-public-context! context)))
    (is (= :exact-public-source-verification-context
           (failure #(b/p15-s23-c6c10-validate-public-context!
                      (assoc context :source-content-hash (str "sha256:" (apply str (repeat 64 "0")))))))))
  (is (= :well-formed-unicode-scalar-string
         (failure #(b/p15-s23-stage2-gravity-checked-core-context
                    (str (char 0xd800) ".gravity") "source" :jvm))))
  (doseq [[source-path source] [["file.txt" "source"] ["file.gravity" (str (char 0xd800))]]]
    (is (= :co-canonical-source-context
           (failure #(b/p15-s23-stage2-gravity-checked-core-context source-path source :jvm))))))

(deftest native-diagnostics-retain-complete-origin-chains
  (let [origin (vec (repeat 65 {:source "origin.gravity"}))
        data (try (b/p15-s23-c6c10-host-fail! "C6-VERIFY" path :semantic-failure
                                            {:generated-origin-chain origin})
                  (catch clojure.lang.ExceptionInfo error (ex-data error)))]
    (is (= origin (:generated-origin-chain data)))))
