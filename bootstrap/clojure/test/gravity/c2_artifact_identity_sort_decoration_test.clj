(ns gravity.c2-artifact-identity-sort-decoration-test
  (:require [clojure.test :refer [deftest is]] [clojure.walk :as walk]
            [gravity.c2-artifact-identity :as identity]))

;; Immutable pre-experiment canonical.clj (04973047) is the differential oracle.
;; Only private sorting decorations differ from this reference.
(defn- previous-projection [canonical-value value]
  (cond
    (map? value)
    (let [decorated (mapv (fn [[key item]]
                            (let [entry [(canonical-value key) (canonical-value item)]]
                              [(pr-str entry) entry])) value)]
      [:map (->> decorated (sort-by first) (mapv second))])
    (set? value)
    (let [decorated (mapv (fn [item]
                            (let [entry (canonical-value item)] [(pr-str entry) entry])) value)]
      [:set (mapv second (sort-by first decorated))])
    (vector? value) [:vector (mapv canonical-value value)]
    (seq? value) [:list (mapv canonical-value value)]
    :else value))
(defn- previous [value] (previous-projection previous value))
(defn- digest [text]
  (let [bytes (.digest (java.security.MessageDigest/getInstance "SHA-256")
                       (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8))]
    (format "%064x" (java.math.BigInteger. 1 bytes))))
(defn- run-hash [project value]
  (binding [*print-length* nil *print-level* nil *print-meta* true]
    (let [encoded (pr-str (project value))]
      {:encoded encoded :digest (str "sha256:" (digest encoded))})))

(deftest decorations-never-change-normalized-carriers-or-full-encoded-preimages
  (let [bad-unit (String. (char-array [(char 0xd800)]))
        samples [(array-map :b [1 {:z #{:b :a}}] :a '(2 3))
                 (sorted-map "a" {:nested [true nil]} "b" (list 'x))
                 #{[:nested {:x 1}] '(2 3)}
                 [1N 1/2 1.00M \newline "quote\"slash\\line\n\u00e9" bad-unit
                  (with-meta 'example/value {:tag 'annotated})]
                 (with-meta (array-map :x (with-meta ['a] {:drop :vector})) {:drop :map})]]
    (doseq [value samples readable [true false] namespace-maps [true false]
            print-meta [true false] print-length [nil 1] print-level [nil 1]]
      (binding [*print-readably* readable *print-namespace-maps* namespace-maps
                *print-meta* print-meta *print-length* print-length *print-level* print-level]
        (let [expected (previous value) actual (identity/reader-canonical-value value)]
          (is (= expected actual))
          (is (= (pr-str expected) (pr-str actual)))
          (is (not-any? #(instance? clojure.lang.MapEntry %)
                        (tree-seq coll? seq actual))))
        (let [expected (run-hash previous value) preimage (atom nil)
              actual (identity/with-operations
                       {:sha256-hex (fn [text] (reset! preimage text) (digest text))}
                       #(identity/reader-canonical-hash value))]
          (is (= (:encoded expected) @preimage))
          (is (= (:digest expected) actual)))))))

(deftest sort-printer-call-order-and-thrown-object-are-conserved
  (let [value (array-map :b #{"a" "b"} :a {:x [1 2]}) original pr-str
        run (fn [project throw-at sentinel]
              (let [calls (atom [])]
                (try
                  (let [result
                        (with-redefs [clojure.core/pr-str
                                      (fn [& args]
                                        (swap! calls conj args)
                                        (if (= throw-at (count @calls)) (throw sentinel)
                                          (apply original args)))]
                          (project value))]
                    {:result result :calls @calls})
                  (catch Throwable error {:error error :calls @calls}))))]
    (let [a (run previous nil nil) b (run identity/reader-canonical-value nil nil)]
      (is (= a b))
      (is (= 5 (count (:calls b))))
      (is (every? #(not (instance? clojure.lang.MapEntry (first %))) (:calls b))))
    (doseq [throw-at (range 1 6)]
      (let [sentinel (ex-info "owned printer failure" {:call throw-at})
            a (run previous throw-at sentinel) b (run identity/reader-canonical-value throw-at sentinel)]
        (is (identical? sentinel (:error a)))
        (is (identical? sentinel (:error b)))
        (is (= (:calls a) (:calls b)))))))

(deftest full-entry-order-and-stable-ties-survive-canonical-key-collisions
  (let [input (array-map :a 2 :b 1) original pr-str]
    (letfn [(old [value] (if (keyword? value) :same (previous-projection old value)))
            (current [value]
              (if (keyword? value) :same
                (identity/call-entrypoint-body :reader-canonical-value identity/reader-canonical-value [value])))
            (run-current [value]
              (identity/with-operations {:reader-canonical-value current} #(current value)))]
      (is (= [:map [[:same 1] [:same 2]]] (old input) (run-current input)))
      (is (= [:set [:same :same]] (old #{:a :b}) (run-current #{:a :b})))
      (with-redefs [clojure.core/pr-str
                    (fn [& args]
                      (if (and (= 1 (count args)) (vector? (first args)) (= :same (ffirst args)))
                        "same-printed-sort-key" (apply original args)))]
        (is (= [:map [[:same 2] [:same 1]]] (old input) (run-current input)))))))

(deftest final-string-printer-failure-precedes-the-injected-hash-callback
  (let [sentinel (ex-info "final encoded string" {:owned true}) calls (atom []) original pr-str
        result (try
                 (with-redefs [clojure.core/pr-str
                               (fn [& args]
                                 (swap! calls conj :print)
                                 (if (= :map (ffirst args)) (throw sentinel) (apply original args)))]
                   (identity/with-operations
                    {:sha256-hex (fn [_] (swap! calls conj :hash) "unreached")}
                    #(identity/reader-canonical-hash {:a 1})))
                 nil (catch Throwable error error))]
    (is (identical? sentinel result))
    (is (= [:print :print] @calls))))
