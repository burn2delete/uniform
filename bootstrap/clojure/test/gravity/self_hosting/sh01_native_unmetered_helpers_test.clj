(ns gravity.self-hosting.sh01-native-unmetered-helpers-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as b]
            [gravity.c6-c10.source-contract :as c6-contract]
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
    ;; The traversal does not consume one host stack frame per child.
    (is (= :vector (first (b/p15-s23-c6c10-canonical-form* path deep)))))
  (is (= (check [1 2 3])
         (b/p15-s23-trusted-carrier-validation [1 2 3] :default-only 0 0 0))))

(deftest native-helpers-retain-canonical-identity
  (let [value {:a [1 2] :b #{3 4}} result (record value)]
    (is (= [:map [[:entry [:keyword nil "a"] [:vector [[:integer "1"] [:integer "2"]]]]
                  [:entry [:keyword nil "b"] [:set [[:integer "3"] [:integer "4"]]]]]]
           (:form result)))
    (is (= #{:form :text} (set (keys result))))
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

(deftest native-helpers-return-semantic-results-without-accounting
  (let [deny (fn [& _] (throw (AssertionError. "accounting operation requested")))
        metric-vars (map #(ns-resolve 'gravity.bootstrap %)
                        '[utf8-byte-count p15-s23-stage2-native-carrier-header
                          p15-s23-stage2-native-integer-magnitude-bits
                          p15-s23-stage2-native-big-integer-magnitude-bits])
        value {:text "λ🙂" :integer (.shiftLeft BigInteger/ONE 4096)
               :ratio 3/4 :decimal (BigDecimal. BigInteger/ONE 65537)}]
    (with-redefs-fn (zipmap metric-vars (repeat deny))
      (fn []
        (is (= {:status :passed} (check value)))
        (is (= {:status :passed} (input value)))
        (is (= {:status :passed}
               (b/p15-s23-trusted-carrier-validation value :default-only (Object.) nil :unused)))
        (is (= #{:status :reason :class}
               (set (keys (check (Object.))))))
        (with-redefs [clojure.core/biginteger deny]
          (is (= {:status :passed} (input value))))
        (is (= #{:form :text} (set (keys (record value)))))
        (is (= {:status :passed}
               (b/p15-s23-c6c10-validate-builder-result-canonical-carrier!
                path {:digest-requests []})))))))

(defn- cross-fallback-parent [kind]
  (let [array (object-array [:placeholder :placeholder])
        backing (clojure.lang.PersistentArrayMap. array)
        parent (if (= :map kind) backing
                   (let [constructor (.getDeclaredConstructor clojure.lang.PersistentHashSet
                                       (into-array Class [clojure.lang.IPersistentMap clojure.lang.IPersistentMap]))]
                     (.setAccessible constructor true)
                     (.newInstance constructor (object-array [nil backing]))))
        compound [parent]]
    (aset array 0 compound)
    (aset array 1 compound)
    [parent compound]))

(deftest native-canonical-fallback-rejects-external-ancestor-cycles
  (doseq [kind [:map :set]]
    (let [[parent compound] (cross-fallback-parent kind)
          active (java.util.IdentityHashMap.)
          marker (Object.)]
      (.put active parent marker)
      (binding [b/p15-s23-c6c10-canonical-ancestors active]
        (is (= :cyclic-canonical-carrier
               (failure #(b/p15-s23-c6c10-canonical-form* path compound)))))
      (is (identical? marker (.get active parent)))
      (is (false? (.containsKey active compound)))))
  ;; Diagnostic hashing must not mistake its new empty maps for this ancestor.
  (let [active (java.util.IdentityHashMap.) value {} marker (Object.)]
    (.put active value marker)
    (binding [b/p15-s23-c6c10-canonical-ancestors active]
      (is (= :cyclic-canonical-carrier
             (failure #(b/p15-s23-c6c10-canonical-form* path value)))))
    (is (identical? marker (.get active value)))))

(deftest native-canonical-fallback-cleans-only-its-own-markers
  (let [active (java.util.IdentityHashMap.) caller (Object.) marker (Object.)
        shared {:a [1 2]} aliases [shared shared] malformed [shared (Object.)]]
    (.put active caller marker)
    (binding [b/p15-s23-c6c10-canonical-ancestors active]
      (is (= (b/p15-s23-c6c10-canonical-form* path [shared {:a [1 2]}])
             (b/p15-s23-c6c10-canonical-form* path aliases)))
      (is (= :canonical-value-domain
             (failure #(b/p15-s23-c6c10-canonical-form* path malformed))))
      (with-redefs [b/p15-s23-c6c10-canonical-sort-key
                    (fn [_] (throw (AssertionError. "output failure")))]
        (is (thrown? AssertionError (b/p15-s23-c6c10-canonical-form* path shared)))))
    (is (identical? marker (.get active caller)))
    (doseq [value [shared aliases malformed]]
      (is (false? (.containsKey active value)))))
  (is (nil? b/p15-s23-c6c10-canonical-ancestors)))

(deftest native-canonical-fallback-retains-malformed-carrier-priority
  (let [calls (atom 0) deny (fn [] (swap! calls inc) (throw (AssertionError. "foreign callback")))
        foreign (foreign-map deny)
        metadata (with-meta {} {:present true})
        comparator (sorted-map-by (fn [_ _] (deny)))
        active (java.util.IdentityHashMap.) marker (Object.)]
    (doseq [[value expected] [[foreign :exact-persistent-map-class]
                             [metadata :metadata-free-canonical-value]
                             [comparator :default-native-comparator]]]
      (.put active value marker)
      (binding [b/p15-s23-c6c10-canonical-ancestors active]
        (is (= expected (failure #(b/p15-s23-c6c10-canonical-form* path value)))))
      (is (identical? marker (.get active value))))
    (is (zero? @calls))))

(deftest native-canonical-independent-passes-have-no-shared-state
  (let [array (object-array [:a 1]) value (clojure.lang.PersistentArrayMap. array)
        validation (java.util.IdentityHashMap.) encoding (java.util.IdentityHashMap.)
        first-form (binding [b/p15-s23-c6c10-canonical-ancestors validation]
                     (b/p15-s23-c6c10-canonical-form* path value))]
    (aset array 1 2)
    (let [second-form (binding [b/p15-s23-c6c10-canonical-ancestors encoding]
                        (b/p15-s23-c6c10-canonical-form* path value))]
      (is (not= first-form second-form)))
    (is (.isEmpty validation))
    (is (.isEmpty encoding))
    (.put validation value :caller)
    (let [expected (b/p15-s23-c6c10-canonical-digest path value)]
      (binding [b/p15-s23-c6c10-canonical-ancestors validation]
        (is (= expected (b/p15-s23-c6c10-canonical-digest path value)))))
    (is (= :caller (.get validation value))))
  (is (nil? b/p15-s23-c6c10-canonical-ancestors)))

(deftest native-ratio-denominators-must-be-nonzero-with-exact-native-receivers
  (doseq [denominator [BigInteger/ONE (.negate BigInteger/ONE) (.shiftLeft BigInteger/ONE 4096)]]
    (let [value (clojure.lang.Ratio. BigInteger/ONE denominator)]
      (is (= {:status :passed} (check value)))
      (is (= {:status :passed} (input value)))
      (is (= [:ratio "1" (.toString denominator)] (:form (record value))))))
  (let [value (clojure.lang.Ratio. BigInteger/ONE BigInteger/ZERO)
        active (java.util.IdentityHashMap.)]
    (.put active value :caller)
    (is (= :untrusted-scalar-class (:reason (check value))))
    (is (= :input-carrier-value-domain (failure #(input value))))
    (binding [b/p15-s23-c6c10-canonical-ancestors active]
      (is (= :canonical-value-domain (failure #(record value)))))
    (is (= :caller (.get active value))))
  (let [calls (atom 0) deny (fn [] (swap! calls inc) (throw (AssertionError. "foreign ratio callback")))
        foreign (proxy [BigInteger] ["0"]
                  (signum [] (deny)) (toString [] (deny)) (bitLength [] (deny)) (abs [] (deny)))]
    (doseq [value [(clojure.lang.Ratio. BigInteger/ONE foreign)
                   (clojure.lang.Ratio. foreign BigInteger/ONE)]]
      (is (= :untrusted-scalar-class (:reason (check value))))
      (is (= :input-carrier-value-domain (failure #(input value))))
      (is (= :canonical-value-domain (failure #(record value)))))
    (is (zero? @calls))))

(defn- sealed-rejection-data [diagnostic]
  (let [sealed {:sealed-diagnostics [diagnostic]}]
    ;; The real sealer establishes canonical carrier validity before throwing.
    (b/p15-s23-c6c10-validate-builder-result-canonical-carrier!
     path (assoc sealed :digest-requests []))
    (try (b/p15-s23-c6c10-throw-sealed-rejection! path sealed)
         (catch clojure.lang.ExceptionInfo error (ex-data error)))))

(deftest native-diagnostic-preserves-long-valid-semantic-ids
  (let [name (str "id-" (apply str (repeat 600 "λ")))
        identities {:syntax-id (keyword name) :core-node-id (symbol name)
                    :operation-id name :origin-id (str name "-origin")}
        data (try (b/p15-s23-c6c10-host-fail! "C6-VERIFY" path :semantic-failure identities)
                  (catch clojure.lang.ExceptionInfo error (ex-data error)))]
    (doseq [[key value] identities]
      (is (identical? value (b/p15-s23-c6c10-diagnostic-semantic-id value)))
      (is (= value (get data key))))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:diagnostic-id data)))
    (let [diagnostic {:id "C7-VERIFY" :rule "C7-VERIFY"
                      :primary {:syntax-id (:syntax-id identities)
                                :core-node-id (:core-node-id identities)
                                :mir-operation-id (:operation-id identities)
                                :origin-id (:origin-id identities)}
                      :facts {:missing-fact :semantic-failure} :origin-chain []}
          sealed (sealed-rejection-data diagnostic)]
      (is (= identities (select-keys sealed (keys identities)))))))

(deftest native-sealed-diagnostic-preserves-complete-origin-chain
  (let [origin (mapv #(str "origin-" %) (range 129))
        diagnostic {:id "C7-VERIFY" :rule "C7-VERIFY" :primary {:span {}}
                    :facts {:missing-fact :semantic-failure} :origin-chain origin}
        data (sealed-rejection-data diagnostic)]
    (is (= origin (:generated-origin-chain data)))
    (is (= origin (:origin-chain (:sealed-diagnostic data))))
    (is (= (first origin) (:origin-id data)))
    (is (= :semantic-failure (:missing-fact data)))
    (is (= {:source path :file path} (:source-span data))))
  (let [data (sealed-rejection-data
              {:id "C7-VERIFY" :primary {} :facts {:missing-fact :semantic-failure}
               :origin-chain nil})]
    (is (= [] (:generated-origin-chain data)))
    (is (nil? (:origin-id data)))))

(deftest native-diagnostic-clamp-removal-keeps-canonical-and-callback-boundaries
  (let [calls (atom 0) deny (fn [] (swap! calls inc) (throw (AssertionError. "foreign ID callback")))
        foreign (proxy [Object clojure.lang.Named] []
                  (getName [] (deny)) (getNamespace [] (deny))
                  (toString [] (deny)) (hashCode [] (deny)) (equals [_] (deny)))
        foreign-map-value (foreign-map deny)
        cyclic (cycle-map)
        diagnostic {:id "C7-VERIFY" :primary {} :facts {:missing-fact :semantic-failure}
                    :origin-chain []}]
    (doseq [candidate [foreign foreign-map-value cyclic nil 1 true []]]
      (is (nil? (b/p15-s23-c6c10-diagnostic-semantic-id candidate))))
    (doseq [[candidate expected] [[(str (char 0xd800)) :well-formed-unicode-scalar-string]
                                 [(symbol "bad name") :canonical-symbol-components]
                                 [(keyword "bad name") :canonical-keyword-components]
                                 [foreign :canonical-value-domain]
                                 [foreign-map-value :exact-persistent-map-class]
                                 [cyclic :cyclic-canonical-carrier]]]
      (is (= expected
             (failure #(sealed-rejection-data (assoc-in diagnostic [:primary :syntax-id] candidate))))))
    (doseq [[origin expected] [[[cyclic] :cyclic-canonical-carrier]
                              [[foreign] :canonical-value-domain]
                              [[(str (char 0xd800))] :well-formed-unicode-scalar-string]]]
      (is (= expected
             (failure #(sealed-rejection-data (assoc diagnostic :origin-chain origin))))))
    (is (zero? @calls))))

(defn- foreign-list-tail [deny]
  (Proxy/newProxyInstance (.getClassLoader clojure.lang.ISeq)
    (into-array Class [clojure.lang.ISeq clojure.lang.IPersistentList clojure.lang.IObj])
    (reify InvocationHandler (invoke [_ _ _ _] (deny)))))

(deftest native-canonical-list-domain-does-not-admit-cons-or-force-foreign-tails
  (let [calls (atom 0) deny (fn [] (swap! calls inc) (throw (AssertionError. "list tail callback")))
        lazy-tail (lazy-seq (deny) (list :unreachable))
        foreign (foreign-list-tail deny)
        finite-cons (clojure.lang.Cons. :head (list :tail))
        lazy-cons (clojure.lang.Cons. :head lazy-tail)
        foreign-cons (clojure.lang.Cons. :head foreign)
        malformed-list (field! (list :head :tail) "_rest" foreign)]
    (doseq [value [finite-cons lazy-cons foreign-cons]]
      (is (false? (list? value)))
      (is (= :canonical-value-domain (failure #(record value))))
      (is (= :canonical-value-domain (failure #(input value)))))
    (is (= {:status :passed} (check finite-cons)))
    (doseq [value [lazy-cons foreign-cons]]
      (is (= :untrusted-list-tail-class (:reason (check value)))))
    (is (= :exact-persistent-list-class (failure #(record malformed-list))))
    (is (= :exact-persistent-carrier-class (failure #(input malformed-list))))
    (is (= [:list [[:keyword nil "head"] [:keyword nil "tail"]]]
           (:form (record (list :head :tail)))))
    (is (= {:status :passed} (input (list :head :tail))))
    (is (zero? @calls))))

(deftest native-private-list-cursor-uses-raw-cons-tail-after-exact-class-check
  ;; Cons is not in the public canonical domain. This explicit private-helper
  ;; domain override tests its advance operation without changing that policy.
  (let [calls (atom 0) deny (fn [] (swap! calls inc) (throw (AssertionError. "raw tail callback")))
        lazy-tail (lazy-seq (deny) (list :unreachable))
        foreign (foreign-list-tail deny)
        finite (clojure.lang.Cons. :head (list :tail))
        cyclic (clojure.lang.Cons. :head nil)
        classes (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-list-classes)
        cursor (deref (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-list-items))]
    (field! cyclic "_more" cyclic)
    (with-redefs-fn {classes (conj @classes "clojure.lang.Cons" (.getName (class foreign)))}
      (fn []
        (is (= [:head :tail] (cursor path finite)))
        (is (= [:head :tail] (cursor path (list :head :tail))))
        (doseq [value [(clojure.lang.Cons. :head lazy-tail)
                       (clojure.lang.Cons. :head foreign)]]
          (is (= :exact-persistent-list-class (failure #(cursor path value)))))
        (is (= :cyclic-canonical-carrier (failure #(cursor path cyclic))))
        (is (= :metadata-free-canonical-value
               (failure #(cursor path (with-meta (clojure.lang.Cons. :head lazy-tail) {:present true})))))))
    (is (zero? @calls))))

(defn- c11-carrier [value]
  (b/p15-s23-c11-mir-bounded-value! path :native-c11-test value))

(deftest native-c11-carriers-have-semantic-validation-without-accounting
  (let [deny (fn [& _] (throw (AssertionError. "obsolete runtime accounting")))
        shared {:text "λ🙂" :value 3/4}
        large [(vec (repeat 32769 nil)) (nth (iterate vector nil) 257)
               (apply str (repeat 65537 "x"))
               (.shiftLeft BigInteger/ONE 4097)
               (BigDecimal. BigInteger/ONE 65537)]]
    (with-redefs [b/p15-s23-reference-runtime-bounded-value! deny]
      (doseq [value (into large [[shared shared] (list shared shared)
                                (clojure.lang.Cons. shared (list shared))
                                (sorted-map :a 1) (sorted-set 1 2)
                                (float 1.25) (double 1.25)])]
        (is (= {:status :passed} (c11-carrier value)))
        (is (= {:status :passed}
               (b/p15-s23-c11-mir-bounded-value!
                path :native-c11-test value (Object.) nil)))))))

(deftest native-c11-carriers-reject-invalid-unicode-native-shapes-and-cycles
  (let [calls (atom 0)
        deny (fn [] (swap! calls inc) (throw (AssertionError. "foreign callback")))
        foreign (foreign-map deny)
        lazy-tail (lazy-seq (deny) (list :unreachable))]
    (doseq [value [(Object.) foreign [foreign] (cycle-map)
                   (with-meta [1] {:present true})
                   (sorted-map-by (fn [_ _] (deny)))
                   (clojure.lang.Cons. :head lazy-tail)
                   (clojure.lang.Ratio. BigInteger/ONE BigInteger/ZERO)]]
      (is (= :valid-native-c11-carrier (failure #(c11-carrier value)))))
    (doseq [value [(str (char 0xd800)) (char 0xd800)
                   (symbol (str "name" (char 0xd800)))
                   (keyword (str "name" (char 0xdc00)))]]
      (is (= :well-formed-unicode-c11-carrier (failure #(c11-carrier value)))))
    (is (zero? @calls))))

(deftest native-c11-scope-matches-executed-current-producer-contract
  ;; Execute the real Gravity producer, without changing its historical
  ;; default admission pins or claiming full upstream construction.
  (let [source-path (.getCanonicalPath
                     (java.io.File.
                      "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity"))
        emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! source-path :jvm))
        plan (b/p15-s23-stage2-compiler-artifact-plan emitter source-path (slurp source-path))
        producer (b/p15-s23-stage2-runtime-execute-function
                  {:engine :gravity-native-c11-compatibility-test
                   :compiler-artifact-plan? true}
                  plan 'c11-build-mir-pass-contract [])
        native (b/p15-s23-c11-mir-pass-contract-record)
        scope (b/p15-s23-c11-mir-scope-record)
        quotas [:maximum-module-carrier-nodes
                :maximum-final-artifact-carrier-nodes :maximum-carrier-depth]]
    (is (= :pinned-gravity-c11-source-byte-size
           (failure #(b/p15-s23-c11-mir-source-binding! path :jvm))))
    (is (= producer native))
    (is (nil? (b/p15-s23-c11-mir-require-strict-structure!
                path producer native :producer-contract-parity)))
    ;; The fixed entry/then/else/join constructor is a supported topology,
    ;; not a work allowance. The producer has separate closed nested variants.
    (is (= 1 (get-in producer [:scope :maximum-conditionals])
           (:maximum-conditionals scope)))
    (doseq [record [(:scope producer) (:scope native) scope]]
      (is (not-any? #(contains? record %) quotas)))
    (doseq [key quotas]
      (is (= :producer-contract-parity
             (failure #(b/p15-s23-c11-mir-require-strict-structure!
                         path producer (assoc-in native [:scope key] 1)
                         :producer-contract-parity)))))))

(deftest native-carrier-traversal-is-stack-safe-for-large-finite-shapes
  ;; Finite host-limit evidence, not a guarantee for arbitrary heap sizes.
  (doseq [value [(nth (iterate vector nil) 4096)
                 (into '() (range 32769))]]
    (is (= {:status :passed} (c11-carrier value))))
  (with-redefs [b/p15-s23-trusted-carrier-validation
                (fn [& _] (throw (StackOverflowError. "contained-native-test")))]
    (is (= :contained-c11-carrier-host-stack (failure #(c11-carrier nil))))))

(deftest native-carrier-validation-retains-current-mir-scope-and-ingress-semantics
  (let [scope (b/p15-s23-c11-mir-scope-record)
        contract-scope (:scope (b/p15-s23-c11-mir-pass-contract-record))
        quotas [:maximum-module-carrier-nodes
                :maximum-final-artifact-carrier-nodes :maximum-carrier-depth]]
    ;; Scope migration does not grant authority to an otherwise valid carrier.
    (is (not-any? #(contains? scope %) quotas))
    (is (not-any? #(contains? contract-scope %) quotas))
    (is (= 1 (:maximum-conditionals scope)))
    (doseq [value [(vec (repeat 32769 nil)) (nth (iterate vector nil) 4096)]]
      ;; Even a valid native carrier cannot supply absent Core authority.
      (let [report (try (b/p15-s23-stage2-c11-mir-verification-report
                         value nil {:source-path path})
                       nil
                       (catch clojure.lang.ExceptionInfo error (ex-data error)))]
        (is (= "C11-MODULE" (:id report)))
        (is (= :authenticated-checked-core-ingress-pair (:missing-fact report)))))))

;; Explicit C11 source-interface dispatch: no genuine source replay in these controls.
(defn
 c11-interface-diagnostic
 [f]
 (try
  (f)
  :unexpected-pass
  (catch
   clojure.lang.ExceptionInfo
   e
   (select-keys (ex-data e) [:id :missing-fact :reason]))))
(deftype
 C11InterfaceForeign
 [calls]
 clojure.lang.IMeta
 (meta [_] (swap! calls inc) nil)
 clojure.lang.ILookup
 (valAt [_ _] (swap! calls inc) nil)
 (valAt [_ _ _] (swap! calls inc) nil)
 Object
 (toString [_] (swap! calls inc) "foreign"))
(def c11-interface-header {:interface :gravity/c11-mir, :version 2})
(def
 c11-interface-v2
 {:kind :gravity/p15-s23-c11-authenticated-mir-artifact,
  :schema-version 2})
(defn
 c11-interface-rejection
 [reason]
 {:id "C11-VERIFY",
  :missing-fact :versioned-c11-interface,
  :reason reason})
(deftest
 c11-interface-public-arities
 (doseq
  [[name expected]
   [['p15-s23-stage2-c11-mir-artifact [2 3]]
    ['p15-s23-stage2-c11-mir-verification-report [3 4]]
    ['p15-s23-stage2-c11-mir-verify! [3 4]]
    ['p15-s23-stage2-c11-mir-authentic? [1 3 4]]]]
  (is
   (=
    expected
    (mapv
     count
     (:arglists (meta (ns-resolve 'gravity.bootstrap name))))))))
(deftest
 c11-interface-selector-before-core-access
 (let
  [calls (atom 0) foreign (C11InterfaceForeign. calls)]
  (doseq
   [[selector reason]
    [[foreign :interface-map-class]
     [(assoc c11-interface-header :version (int 2))
      :interface-version-class-or-value]
     [(assoc c11-interface-header :version 1)
      :explicit-interface-version]
     [(assoc c11-interface-header :version 3)
      :interface-version-class-or-value]
     [(assoc c11-interface-header :interface foreign) :interface-id]]]
   (is
    (=
     (c11-interface-rejection reason)
     (c11-interface-diagnostic
      (fn*
       []
       (b/p15-s23-stage2-c11-mir-artifact foreign foreign selector)))))
   (is
    (=
     (c11-interface-rejection reason)
     (c11-interface-diagnostic
      (fn*
       []
       (b/p15-s23-stage2-c11-mir-verification-report
        foreign
        foreign
        foreign
        selector))))))
  (is (= 0 @calls))))
(deftest
 c11-interface-mixed-version-before-payload-or-core
 (let
  [calls
   (atom 0)
   foreign
   (C11InterfaceForeign. calls)
   artifact
   (assoc
    c11-interface-v2
    :mir-module
    foreign
    :verification-report
    foreign)]
  (is
   (=
    (c11-interface-rejection :mixed-c11-interface-versions)
    (c11-interface-diagnostic
     (fn*
      []
      (b/p15-s23-stage2-c11-mir-verification-report
       artifact
       foreign
       foreign)))))
  (is
   (=
    (c11-interface-rejection :mixed-c11-interface-versions)
    (c11-interface-diagnostic
     (fn*
      []
      (b/p15-s23-stage2-c11-mir-verification-report
       (assoc artifact :schema-version 1)
       foreign
       foreign
       c11-interface-header)))))
  (is
   (false?
    (b/p15-s23-stage2-c11-mir-authentic? artifact foreign foreign)))
  (is (= 0 @calls))))
(deftest
 c11-interface-malformed-legacy-owning-diagnostic
 (doseq
  [artifact [nil {}]]
  (is
   (=
    {:id "C11-MODULE",
     :missing-fact :authenticated-checked-core-ingress-pair}
    (c11-interface-diagnostic
     (fn*
      []
      (b/p15-s23-stage2-c11-mir-verification-report
       artifact
       nil
       nil))))))
 (is (false? (b/p15-s23-stage2-c11-mir-authentic? c11-interface-v2))))
(deftest
 c11-interface-record-and-scope-bindings
 (is
  (not
   (contains?
    (b/p15-s23-c11-mir-scope-record)
    :supported-source-family)))
 (is
  (=
   :bool-branch-join
   (:supported-source-family (b/p15-s23-c11-mir-scope-record 2))))
 (is
  (=
   c11-interface-header
   (:source-interface (b/p15-s23-c11-mir-scope-record 2))))
 (is
  (=
   10
   (get-in
    (b/p15-s23-c11-mir-contract 1)
    [:required-functions 'c11-build-definitions :arity])))
 (is
  (=
   11
   (get-in
    (b/p15-s23-c11-mir-contract 2)
    [:required-functions 'c11-build-definitions :arity])))
 (is
  (=
   {:x :unchanged}
   (b/p15-s23-c11-mir-versioned-record {:x :unchanged} 1))))

(defn- c6-program-diagnostic [thunk]
  (try (thunk) nil
       (catch clojure.lang.ExceptionInfo error
         ((juxt :id :missing-fact) (ex-data error)))))

(deftest selected-current-c6-program-authenticates-before-cache-reuse
  (let [metrics (atom {})]
    (binding [b/*p15-s23-c6c10-compiled-binding-metrics* metrics]
      (let [original (b/p15-s23-c6c10-source-binding! path 2)
            again (b/p15-s23-c6c10-source-binding! path 2)]
        (is (= 2 (:source-contract-version original)))
        (is (= 170 (get-in original [:function-manifest :function-count])))
        (is (= [64 64 42] (mapv :count (get-in original [:function-manifest :chunks]))))
        (is (identical? (:plan original) (:plan again)))
        ;; The durable test also runs correctly after an earlier selected v2
        ;; binding in the same JVM. Both routes reauthenticate every lookup.
        (is (contains? #{{:source-authenticated 2 :emitter-authenticated 2
                          :cache-miss 1 :compile-start 1 :compile-complete 1 :cache-hit 1}
                         {:source-authenticated 2 :emitter-authenticated 2 :cache-hit 2}}
                       @metrics))
        (prn {:c6-selected-program-metrics @metrics})
        (doseq [key [:source-content-hash :source-byte-count :plan-semantic-hash
                     :functions-semantic-hash :builder-semantic-hash :verifier-semantic-hash
                     :function-shapes :function-manifest :source-contract-version]]
          (is (= ["C6-VERIFY" :exact-pinned-gravity-c6-c10-source-binding]
                 (c6-program-diagnostic
                  #(b/p15-s23-c6c10-validate-source-binding! (assoc original key :altered) 2)))))
        (is (= ["C6-VERIFY" :exact-pinned-gravity-c6-c10-source-binding]
               (c6-program-diagnostic #(b/p15-s23-c6c10-validate-source-binding! original))))
        (is (= ["C6-VERIFY" :exact-pinned-gravity-c6-c10-source-binding]
               (c6-program-diagnostic
                #(b/p15-s23-c6c10-validate-source-binding!
                  (dissoc original :source-contract-version) 2))))
        (is (= ["C6-VERIFY" :exact-pinned-gravity-c6-c10-source-binding]
               (c6-program-diagnostic
                #(b/p15-s23-c6c10-validate-source-binding!
                  (assoc original :source-contract-version (int 2)) 2))))
        (is (= ["C6-VERIFY" :explicit-c6-source-contract-version]
               (c6-program-diagnostic #(b/p15-s23-c6c10-source-binding! path (int 2)))))
        (is (= ["C6-VERIFY" :pinned-gravity-c6-c10-source-bytes]
               (c6-program-diagnostic #(b/p15-s23-c6c10-source-binding! path))))))))

(deftest selected-current-reader-plan-does-not-grant-envelope-authority
  (let [result (b/p15-s23-c6c10-current-reader-program! path)
        reader-binding (:binding result)
        reader-path (:source-path reader-binding)
        inputs-var (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-authenticated-source-binding-inputs!)
        inputs (@inputs-var path 2)
        validate @(ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-validate-current-reader-program!)
        emitter (:emitter-rule inputs)]
    (is (= :selected-source-reader-plan (:verification-scope result)))
    (is (false? (:reader-result-executed? result)))
    (is (false? (:envelope-state-observed? result)))
    (is (not (contains? result :admitted?)))
    (is (= {:reader-source-path reader-path
            :authenticated-envelope-options {:source :development-current}}
           (:invocation result)))
    (doseq [key (keys (:binding c6-contract/current-reader-program))]
      (is (= ["C6-VERIFY" :selected-current-reader-program-identities]
             (c6-program-diagnostic #(validate path reader-path (assoc reader-binding key :altered) emitter)))))
    (is (= ["C6-VERIFY" :selected-current-reader-source-path]
           (c6-program-diagnostic #(validate path (str reader-path ".substitution") reader-binding emitter))))
    (is (= ["C6-VERIFY" :selected-current-reader-export-shapes]
           (c6-program-diagnostic
            #(validate path reader-path
                       (assoc-in reader-binding [:plan :functions 'sh03-read-source-unit :arity] 2) emitter))))
    (doseq [changed [(assoc emitter :source-rule-hash "sha256:altered")
                     (assoc-in emitter [:rule-record :status] :failed)]]
      (is (= ["C6-VERIFY" :selected-current-reader-plan-emitter-join]
             (c6-program-diagnostic #(validate path reader-path reader-binding changed)))))
    (doseq [key [:emitter-source-rule-hash :emitter-source-content-hash :emitter-semantic-hash]]
      (is (= ["C6-VERIFY" :selected-current-reader-plan-emitter-join]
             (c6-program-diagnostic
              #(validate path reader-path (assoc-in reader-binding [:plan :compiler key] :altered) emitter)))))
    ;; A negative caller-control fixture only. Genuine owner execution above
    ;; produced the binding; no supplied binding is accepted by a public route.
    (let [calls (atom 0)
          reader-var (ns-resolve 'gravity.bootstrap 'sh03-reader-current-binding!)]
      (with-redefs-fn
        {inputs-var (fn [_ version]
                      (is (= 2 version))
                      (if (= 1 (swap! calls inc)) inputs
                          (assoc-in inputs [:source-snapshot :source-content-hash] :stale)))
         reader-var (fn [_ selected] (is (= reader-path selected)) reader-binding)}
        #(is (= ["C6-VERIFY" :stable-current-reader-dependency-inputs]
                (c6-program-diagnostic (fn [] (b/p15-s23-c6c10-current-reader-program! path)))))))))

(defn- expression-program-diagnostic [thunk]
  (try (thunk) nil
       (catch clojure.lang.ExceptionInfo error
         ((juxt :id :missing-fact) (ex-data error)))))

(deftest selected-expression-owner-authenticates-and-executes-ordinary-bool-lowering
  (let [rule (b/c-backend-stage2-plan-emitter-source-rule! path :jvm)
        emitter (:emitter rule)
        selected (b/p15-s23-stage2-compiler-artifact-binding! emitter path :jvm 2)
        plan (:plan selected)
        contract c6-contract/current-expression-program
        validate @(ns-resolve 'gravity.bootstrap 'p15-s23-stage2-validate-current-expression-program!)]
    (is (= 2 (:source-contract-version selected)))
    (is (= 692 (:function-count selected)))
    (is (= (:source-content-hash contract) (:source-content-hash selected)))
    (is (= (:semantic-hash contract) (:semantic-hash selected)))
    (is (= (:functions-semantic-hash contract) (:functions-semantic-hash selected)))
    (is (= (:artifact-hash contract) (:artifact-hash selected)
           (b/p15-s23-stage2-compiler-artifact-expected-artifact-hash 2)))
    (is (= (:declared-plan-compiler-labels contract) (:compiler plan)
           (:declared-plan-compiler-labels selected)))
    (is (false? (:invoked? selected)))
    ;; The current program must not gain compiler-only builtin authority from
    ;; its source name, selected binding, or compiler-artifact-plan flag.
    (is (false? (b/p15-s23-stage2-compiler-artifact-plan-context? plan)))
    (is (false? (b/p15-s23-stage2-runtime-augmented-plan-context?
                 {:engine :gravity-stage2-compiler-artifact-host-runner
                  :compiler-artifact-plan? true} plan)))
    (binding [b/*p15-s23-stage2-compiler-artifact-binding* selected]
      (let [module {:module 'expression.fixture :source-path path :profile :hosted
                    :target :jvm :effects #{} :capabilities #{} :safety :safe}
            result (b/p15-s23-stage2-compiler-artifact-invoke
                     'p15-s23-compile-expr [emitter module {} true])
            literal (:value result)]
        (is (= :complete (:status result)))
        (is (= :literal (:op literal)))
        (is (true? (:value literal))))
      ;; Expression-owner fixture only: no C2/C3/Core or public admission.
      (let [forms '[(ns expression.fixture (:profile :hosted) (:target :jvm)
                        (:effects #{}) (:capabilities #{}) (:safety :safe) (:exports [main]))
                    (defn main [] (if true true false))]
            module (b/parse-module path forms)
            emitted (b/p15-s23-stage2-construct-emitted-core-plan
                      emitter path (apply str (interpose "\n" (map pr-str forms))) module (b/stage0-function-table module))
            compiler (:compiler emitted)
            record {:artifact :gravity/p15-s23-stage2-expression-lowering-binding
                    :source-contract-version 2 :source-content-hash (:source-content-hash selected)
                    :semantic-hash (:semantic-hash selected) :artifact-hash (:artifact-hash selected)
                    :functions (:functions selected) :functions-semantic-hash (:functions-semantic-hash selected)
                    :function-count (:function-count selected)
                    :invoked? (:expression-lowering-invoked? compiler) :generic-bridge-residual? true
                    :plan-assembly-function (:plan-assembly-function compiler)
                    :plan-assembly-artifact-hash (:plan-assembly-artifact-hash compiler)
                    :plan-assembly-source-content-hash (:plan-assembly-source-content-hash compiler)
                    :plan-assembly-semantic-hash (:plan-assembly-semantic-hash compiler)
                    :plan-assembly-invoked? (:plan-assembly-invoked? compiler)
                    :plan-assembly-generic-bridge-residual? (:plan-assembly-generic-bridge-residual? compiler)
                    :clojure-seed-boundary? true :self-hosted? false}]
        (is (= 2 (:expression-lowering-source-contract-version compiler)))
        (is (= (:functions-semantic-hash selected) (:expression-lowering-functions-semantic-hash compiler)))
        (is (true? (:plan-assembly-invoked? compiler)))
        (is (b/p15-s23-stage2-compiler-artifact-record-authentic? record 2))
        (is (not (b/p15-s23-stage2-compiler-artifact-record-authentic? record)))
        (is (b/p15-s23-stage2-compiler-artifact-record-matches-plan? record emitted 2))
        (let [calls (atom 0)
              deny #(do (swap! calls inc) (throw (ex-info "unexpected callback" {})))
              foreign (foreign-map deny)
              inert (lazy-seq (deny))
              opaque (Object.)
              raw-map (fn [pairs] (clojure.lang.PersistentArrayMap. ^objects (object-array (mapcat identity pairs))))
              inert-plan (assoc emitted :functions inert :unconsumed opaque)]
          (is (b/p15-s23-stage2-compiler-artifact-record-matches-plan? record inert-plan 2))
          (is (not= :passed (:status (b/p15-s23-trusted-carrier-validation inert-plan :default-only))))
          (doseq [bad [foreign (raw-map [[:compiler compiler] [foreign :inert]])
                       (raw-map [[:compiler compiler] ["not-a-keyword" :inert]])
                       (raw-map [[:compiler compiler] [:compiler compiler]])
                       (with-meta emitted {:unconsumed opaque})
                       (clojure.lang.PersistentArrayMap. ^objects (object-array [:compiler]))
                       (field! (clojure.lang.PersistentArrayMap. ^objects (object-array 0)) "array" nil)
                       (assoc emitted :compiler foreign)
                       (assoc emitted :compiler (raw-map (concat (seq compiler) [[:plan-assembly-invoked? true]])))
                       (assoc emitted :compiler (clojure.lang.PersistentArrayMap. ^objects (object-array [:source])))
                       (dissoc emitted :compiler)]]
            (is (false? (b/p15-s23-stage2-compiler-artifact-record-matches-plan? record bad 2))))
          (is (false? (b/p15-s23-stage2-compiler-artifact-record-authentic?
                        (raw-map (concat (seq record) [[:source-contract-version 2]])) 2)))
          (is (zero? @calls)))
        (doseq [changed [(dissoc record :source-contract-version)
                         (assoc record :source-contract-version (int 2))
                         (assoc record :functions-semantic-hash "sha256:altered")
                         (assoc record :source-content-hash (get-in contract [:declared-plan-compiler-labels :source-content-hash]))]]
          (is (not (b/p15-s23-stage2-compiler-artifact-record-authentic? changed 2))))
        (is (not (b/p15-s23-stage2-compiler-artifact-record-matches-plan?
                   record (update emitted :compiler dissoc :expression-lowering-source-contract-version) 2)))))
    (doseq [changed [(assoc-in plan [:source :sha256] "sha256:substitution")
                     (assoc-in plan [:compiler :source-content-hash] (:source-content-hash selected))
                     (assoc-in plan [:functions 'p15-s23-compile-expr :arity] 3)
                     (assoc-in plan [:instruction-summary :literal] -1)]]
      (is (= ["P15S23Q002" :stage2-current-expression-complete-program-identities]
             (expression-program-diagnostic #(validate path changed)))))
    (is (= ["P15S23Q002" :current-expression-recomputable-plan-id]
           (expression-program-diagnostic #(validate path (assoc plan :plan-id "sha256:altered")))))
    (is (= ["P15S23Q002" :current-expression-plan-emitter-join]
           (expression-program-diagnostic
             #(b/p15-s23-stage2-compiler-artifact-binding! (assoc emitter :engine :altered) path :jvm 2))))
    (is (= ["P15S23Q002" :expression-lowering-source-contract-version]
           (expression-program-diagnostic
             #(b/p15-s23-stage2-compiler-artifact-binding! emitter path :jvm (int 2)))))
    (is (= ["P15S23Q002" :current-expression-selected-host-source-context]
           (expression-program-diagnostic #(b/p15-s23-stage2-compiler-artifact-binding! emitter path :js 2))))
    (let [calls (atom 0) foreign (foreign-map #(do (swap! calls inc) (throw (ex-info "callback invoked" {}))))]
      (is (= ["P15S23Q002" :current-expression-native-plan-emitter]
             (expression-program-diagnostic #(b/p15-s23-stage2-compiler-artifact-binding! foreign path :jvm 2))))
      (is (false? (b/p15-s23-stage2-compiler-artifact-record-authentic? foreign 2)))
      (is (false? (b/p15-s23-stage2-compiler-artifact-record-matches-plan? foreign foreign 2)))
      (is (zero? @calls)))
    (is (= ["P15S23Q002" :current-expression-native-plan-emitter]
           (expression-program-diagnostic
             #(b/p15-s23-stage2-compiler-artifact-binding! (with-meta emitter {:untrusted true}) path :jvm 2))))
    (is (= "sha256:e25ae2c9b7853627f8f9db45eb916b6b3775e28b51fbe6e29ecd734598ca8ac2"
           (b/p15-s23-stage2-compiler-artifact-expected-artifact-hash)))))

(defn- current-core-policy-fixture []
  ;; Synthetic DATA for relative validation. This is not a source artifact or
  ;; a verification report produced by the current Core owner.
  (let [core {:artifact-id "policy-control-core"}
        context {:source-path path}
        receipt
        {:source-interface {:interface :gravity/c6-c10 :version 2}
         :admission-status :development-unadmitted :verification-scope :selected-current-source-host-seed
         :reader-plan-binding (:binding c6-contract/current-reader-program)
         :reader-source-revision {:source-content-hash (get-in c6-contract/current-reader-program [:binding :source-content-hash])}
         :reader-result-identity {:actual-sh03-reader-result-id "result" :actual-sh03-semantic-product-binding {}
                                 :actual-c2-artifact-id "c2" :actual-reader-product-integrity-hash "integrity"
                                 :actual-reader-source-id "source"}
         :authenticated-envelope {:stage :c2-reader :status :accepted :source-rule {:admission-status :development-unadmitted}}
         :envelope-verification :passed :self-hosted? false
         :expression-binding (assoc (select-keys c6-contract/current-expression-program
                                     [:source-content-hash :semantic-hash :functions-semantic-hash :artifact-hash :function-count])
                                   :source-contract-version 2)}
        commitment (str "sha256:" (digest/sha256-hex "relative-policy-commitment-fixture"))
        receipt (-> receipt
                    (assoc :projection-contract b/p15-s23-c11-core-projection-contract
                           :owner-metadata-content-id commitment)
                    (assoc-in [:reader-plan-binding :instruction-summary-content-id] commitment)
                    (assoc-in [:reader-source-revision :function-shapes-content-id] commitment)
                    (assoc-in [:authenticated-envelope :source-rule :function-shapes-content-id] commitment)
                    (assoc-in [:authenticated-envelope :source-rule :owner-source-rule-content-id] commitment)
                    (assoc-in [:authenticated-envelope :gravity-template-replay :checks-content-id] commitment)
                    (assoc-in [:authenticated-envelope :gravity-template-replay :diagnostics-content-id] commitment)
                    (assoc-in [:expression-binding :functions-content-id] commitment)
                    (assoc-in [:expression-binding :declared-plan-compiler-labels-content-id] commitment)
                    (assoc-in [:expression-binding :owner-binding-content-id] commitment))
        bind-data (fn [policy]
                    (assoc policy :source-policy-binding-id
                      (b/p15-s23-c6c10-canonical-digest path
                       {:domain :gravity/c6-c10-source-policy-v2 :artifact-id (:artifact-id core)
                        :context context :policy (:source-policy-receipt policy)})))
        policy (bind-data
                {:source-interface (:source-interface receipt) :admission-status :development-unadmitted
                 :verification-scope :selected-current-source-host-seed :source-policy-receipt receipt
                 :compiler-program (assoc (select-keys c6-contract/current-compiler-program
                                           (rest b/p15-s23-c11-core-program-projection-keys)) :source-contract-version 2)})]
    {:core core :context context :policy policy :bind-data bind-data}))

(deftest current-core-policy-data-is-relative-and-binds-final-record-identities
  (let [{:keys [core context policy bind-data]} (current-core-policy-fixture)
        changed (bind-data (assoc-in policy [:source-policy-receipt :reader-result-identity :actual-c2-artifact-id] "changed"))
        report #(b/p15-s23-c11-independent-verifier-record {:status :passed} 2 %)
        pass #(b/p15-s23-c11-mir-pass-execution-record {} core (report %) 2 %)
        b1 #(b/p15-s23-c11-b1-candidate-record {:pass-execution-record (pass %)} core (report %) {} 2 %)]
    (is (= policy (b/p15-s23-c11-mir-require-core-policy-data! core context policy)))
    ;; Coherent hashes validate only relative DATA. Public C11 verification
    ;; must freshly reconstruct Core/source ground truth and reject this swap.
    (is (= changed (b/p15-s23-c11-mir-require-core-policy-data! core context changed)))
    (is (not= (:report-id (report policy)) (:report-id (report changed))))
    (is (not= (:record-id (pass policy)) (:record-id (pass changed))))
    (is (not= (:binding (b1 policy)) (:binding (b1 changed))))
    (doseq [bad [(dissoc policy :source-policy-binding-id)
                (assoc policy :extra true)
                (assoc policy :admission-status :admitted)
                (bind-data (update policy :source-policy-receipt dissoc :owner-metadata-content-id))
                (bind-data (assoc-in policy [:source-policy-receipt :projection-contract :core-carrier-policy] :default-only))
                (bind-data (assoc-in policy [:source-policy-receipt :projection-contract :schema-version] (int 1)))
                (bind-data (assoc-in policy [:source-policy-receipt :expression-binding :owner-binding-content-id] "not-a-digest"))
                (assoc-in policy [:source-interface :version] (int 2))
                (assoc-in policy [:source-policy-receipt :source-interface :version] (int 2))
                (assoc-in policy [:compiler-program :source-content-hash] "wrong")
                (assoc-in policy [:source-policy-receipt :reader-result-identity :actual-c2-artifact-id] "unsealed")]]
      (is (= :current-core-source-policy-data
             (failure #(b/p15-s23-c11-mir-require-core-policy-data! core context bad)))))
    (is (= :current-core-policy-binding-required (failure #(report nil))))
    (is (not (contains? (b/p15-s23-c11-independent-verifier-record {:status :passed} 1)
                       :checked-core-source-policy-binding)))
    (let [calls (atom 0) foreign (foreign-map #(do (swap! calls inc) (throw (ex-info "callback invoked" {}))))]
      (is (some? (failure #(b/p15-s23-c11-mir-require-core-policy-data! core context foreign))))
      (is (some? (failure #(b/p15-s23-c11-mir-core-policy-projection core context foreign))))
      (is (zero? @calls)))))
