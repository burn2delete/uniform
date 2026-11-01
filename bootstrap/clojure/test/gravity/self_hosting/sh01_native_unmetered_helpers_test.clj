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
