(ns gravity.self-hosting.sh03-canonical-unmetered-cycle-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.digest :as digest]))

(def ^:private path "/declared/unmetered-cycle.gravity")
(defn- operation [name] (deref (ns-resolve 'gravity.bootstrap name)))

(defn- failure [f]
  (try (f) nil
       (catch clojure.lang.ExceptionInfo error (ex-data error))
       (catch Throwable error {:unexpected-throwable (.getName (class error))})))

(defn- self-map []
  ;; Public native constructor retains the supplied Object[]; no proxy or
  ;; custom collection is needed to demonstrate an actual identity cycle.
  (let [backing (object-array [:self nil])
        value (clojure.lang.PersistentArrayMap. backing)]
    (aset backing 1 value)
    value))

(defn- mutual-maps []
  (let [a (object-array [:next nil]) b (object-array [:next nil])
        first-map (clojure.lang.PersistentArrayMap. a)
        second-map (clojure.lang.PersistentArrayMap. b)]
    (aset a 1 second-map) (aset b 1 first-map)
    first-map))

(defn- wrapped-cycle [wrap]
  (let [backing (object-array [:bridge nil])
        value (clojure.lang.PersistentArrayMap. backing)]
    (aset backing 1 (wrap value))
    value))

(defn- self-key-map []
  (let [backing (object-array [nil :value])
        value (clojure.lang.PersistentArrayMap. backing)]
    (aset backing 0 value)
    value))

(defn- captured-record [value]
  (let [validated ((operation 'sh03-reader-canonical-stream-validation!) path value)
        writer (java.io.StringWriter.)
        emitted ((operation 'sh03-reader-canonical-stream-write!) path value writer)]
    {:text (.toString writer) :validation validated :emission emitted}))

(deftest unmetered-canonical-rejects-actual-ancestor-cycles-in-both-passes
  (doseq [value [(self-map) (mutual-maps)
                 (wrapped-cycle vector) (wrapped-cycle list)
                 (wrapped-cycle hash-set) (self-key-map)]]
    (let [native (failure #(bootstrap/p15-s23-c6c10-canonical-record path value))
          validation (failure #((operation 'sh03-reader-canonical-stream-validation!) path value))
          emission (failure #((operation 'sh03-reader-canonical-stream-write!)
                              path value (java.io.StringWriter.)))]
      ;; Never print/compare the cyclic input: its normal hash/equality or
      ;; printer can recurse. Only finite structured failure data is compared.
      (is (= "C6-VERIFY" (:id native) (:id validation) (:id emission)))
      (is (= :cyclic-canonical-carrier (:missing-fact native)
             (:missing-fact validation) (:missing-fact emission)))
      (is (= native validation emission)))))

(deftest unmetered-canonical-shared-acyclic-aliases-are-fully-revisited
  (let [shared (array-map :x [1 "λ🙂"])
        equal-distinct (clojure.lang.PersistentArrayMap. (object-array [:x [1 "λ🙂"]]))]
    (doseq [value [[shared shared] [shared equal-distinct] {:a shared :b shared}
                   (list shared shared) #{[1] (list 2)} {[:compound :key] shared}]]
      (let [expected (bootstrap/p15-s23-c6c10-canonical-record path value)
            actual (captured-record value)]
        (is (= (:text expected) (:text actual)))
        (is (= {:status :passed} (:validation actual) (:emission actual)))
        (is (= (str "sha256:" (digest/sha256-hex (:text expected)))
               ((operation 'sh03-reader-canonical-stream-digest) path value)))))
    (let [header (ns-resolve 'gravity.bootstrap 'sh03-reader-canonical-stream-header!)
          original @header visits (atom 0)]
      (with-redefs-fn
        {header (fn [source value]
                  (when (identical? shared value) (swap! visits inc))
                  (original source value))}
        #(captured-record [shared shared]))
      ;; Each occurrence is validated again in BOTH passes, not deduplicated.
      (is (= 4 @visits)))))

(deftest unmetered-canonical-metadata-and-unicode-precede-cycle-descent
  (let [backing (object-array [:self nil])
        value (with-meta (clojure.lang.PersistentArrayMap. backing) {:present true})]
    (aset backing 1 value)
    (let [native (failure #(bootstrap/p15-s23-c6c10-canonical-record path value))
          actual (failure #((operation 'sh03-reader-canonical-stream-validation!) path value))]
      (is (= :metadata-free-canonical-value (:missing-fact native)))
      (is (= native actual))))
  (let [bad (String. (char-array [(char 0xD800)]))
        backing (object-array [:unicode bad :self nil])
        value (clojure.lang.PersistentArrayMap. backing)]
    (aset backing 3 value)
    (let [native (failure #(bootstrap/p15-s23-c6c10-canonical-record path value))
          actual (failure #((operation 'sh03-reader-canonical-stream-validation!) path value))]
      (is (= :well-formed-unicode-scalar-string (:missing-fact native)))
      (is (= native actual)))))

(deftest unmetered-canonical-opens-no-output-before-complete-validation
  (doseq [value [(self-map) (wrapped-cycle hash-set)
                 (with-meta [1] {:present true})
                 (String. (char-array [(char 0xDC00)]))]]
    (let [opened (atom 0)
          actual (with-redefs [digest/sha256-output-hex
                              (fn [_] (swap! opened inc) "must-not-open")
                              bootstrap/p15-s23-c6c10-host-fail!
                              (fn [rule source missing subject]
                                ;; Isolate the semantic refusal from the
                                ;; diagnostic's legitimate generic hash sink.
                                (throw (ex-info "First semantic refusal"
                                                {:id rule :source source
                                                 :missing-fact missing :subject subject})))]
                   (failure #((operation 'sh03-reader-canonical-stream-digest) path value)))]
      (is (= "C6-VERIFY" (:id actual)))
      (is (zero? @opened)))))

(deftest unmetered-canonical-failed-pass-does-not-poison-later-independent-passes
  (dotimes [_ 2]
    (is (= :cyclic-canonical-carrier
           (:missing-fact (failure #((operation 'sh03-reader-canonical-stream-validation!)
                                    path (self-map)))))))
  (let [value (array-map :a [1 2] :b "safe")
        first-pass (captured-record value)
        second-pass (captured-record value)]
    (is (= first-pass second-pass))
    (is (= (:text (bootstrap/p15-s23-c6c10-canonical-record path value))
           (:text first-pass)))))

(deftest unmetered-canonical-passes-own-fresh-identity-contexts-and-clean-up
  (let [context (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-ancestors)
        header (ns-resolve 'gravity.bootstrap 'sh03-reader-canonical-stream-header!)
        original @header external (java.util.IdentityHashMap.) sentinel (Object.)
        observed (atom []) value (array-map :a [1 2])]
    (.put external sentinel true)
    (with-bindings {context external}
      (with-redefs-fn
        {header (fn [source item]
                  (when (identical? item value) (swap! observed conj @context))
                  (original source item))}
        #(do (captured-record value) (captured-record value))))
    (is (= 4 (count @observed)))
    (let [seen (java.util.IdentityHashMap.)]
      (doseq [active @observed]
        (is (not (identical? external active)))
        (is (not (.containsKey seen active)))
        (is (.isEmpty ^java.util.IdentityHashMap active))
        (.put seen active true)))
    (is (= 1 (.size external)))
    (is (.containsKey external sentinel)))
  (let [context (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-ancestors)
        header (ns-resolve 'gravity.bootstrap 'sh03-reader-canonical-stream-header!)
        original @header observed (atom [])]
    (with-redefs-fn
      {header (fn [source value]
                (swap! observed conj @context)
                (original source value))}
      (fn []
       (doseq [value [(self-map) (wrapped-cycle hash-set) (self-key-map)]]
         (is (= :cyclic-canonical-carrier
                (:missing-fact (failure #((operation 'sh03-reader-canonical-stream-validation!)
                                         path value)))))
         (is (= :cyclic-canonical-carrier
                (:missing-fact (failure #((operation 'sh03-reader-canonical-stream-write!)
                                         path value (java.io.StringWriter.)))))))))
    (is (seq @observed))
    (doseq [active @observed]
      (is (.isEmpty ^java.util.IdentityHashMap active)))))

(deftest unmetered-canonical-fallback-preserves-reader-ancestor-markers
  (let [context (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-ancestors)
        fallback (ns-resolve 'gravity.bootstrap 'sh03-reader-canonical-fallback-form!)
        original @fallback observed (atom [])]
    (with-redefs-fn
      {fallback
       (fn [source value]
         (let [active ^java.util.IdentityHashMap @context
               before (vec (.keySet active))]
           (try (original source value)
                (finally
                  (swap! observed conj
                         (and (seq before)
                              (every? #(.containsKey active %) before)))))))}
      #(doseq [value [(array-map :parent #{[1] (list 2)})
                     (array-map [:compound :key] :safe)
                     (wrapped-cycle hash-set) (self-key-map)]]
         (failure (fn [] ((operation 'sh03-reader-canonical-stream-validation!) path value)))
         (failure (fn [] ((operation 'sh03-reader-canonical-stream-write!)
                          path value (java.io.StringWriter.))))))
    (is (seq @observed))
    (is (every? true? @observed))))
