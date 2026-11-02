(ns gravity.self-hosting.canonical-keyword-scalar-path-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private path "/declared/canonical-keyword.gravity")
(defn- operation [name] (ns-resolve 'gravity.bootstrap name))
;; Use the same production identity entrypoint as Reader digest resolution.
(defn- form [value] (bootstrap/sh03-reader-canonical-identity path value))
(defn- capture [f]
  (try {:value (f)}
       (catch clojure.lang.ExceptionInfo error
         {:message (.getMessage error) :data (ex-data error)})))

(deftest keyword-forms-and-full-errors-match-generic-child-traversal
  (doseq [value [:plain :qualified/name (keyword "lambda" "face")
                 (keyword "\u03bb" "\uD83D\uDE42")]]
    (is (= [:keyword (namespace value) (name value)] (form value)))
    (is (= [:vector [(form value)]] (form [value]))))
  (let [high (str (char 0xD800)) low (str (char 0xDC00))]
    (doseq [[value missing]
            [[(keyword "") :canonical-keyword-components]
             [(keyword "" "name") :canonical-keyword-components]
             [(keyword "bad ns" "name") :canonical-keyword-components]
             [(keyword "bad/ns" "name") :canonical-keyword-components]
             [(keyword "ns" "bad/name") :canonical-keyword-components]
             [(keyword "ns" "bad\nname") :canonical-keyword-components]
             [(keyword high "name") :well-formed-unicode-scalar-string]
             [(keyword "ns" low) :well-formed-unicode-scalar-string]
             [(keyword high "bad/name") :canonical-keyword-components]]]
      (let [root (capture #(form value)) child (capture #(form [value]))]
        ;; Includes stable diagnostic identity, facts, source and remediation.
        (is (= root child))
        (is (= "C6-VERIFY" (get-in root [:data :id])))
        (is (= missing (get-in root [:data :missing-fact])))))))

(deftest keyword-validation-stays-fresh-and-retains-original-exceptions
  (doseq [validator ['p15-s23-c6c10-canonical-carrier
                     'p15-s23-c6c10-canonical-scalar-form]]
    (let [target (operation validator) original @target calls (atom 0)
          rejected (ex-info "Second validation rejected" {:validator validator})]
      (with-redefs-fn
        {target (fn [source value]
                  (if (and (identical? :repeat value) (= 2 (swap! calls inc)))
                    (throw rejected) (original source value)))}
        #(do (is (= [:keyword nil "repeat"] (form :repeat)))
             (is (identical? rejected
                             (try (form :repeat) nil (catch Throwable e e))))
             (is (= 2 @calls)))))))

(deftest keyword-validation-order-and-ancestor-context-are-preserved
  (let [carrier (operation 'p15-s23-c6c10-canonical-carrier)
        scalar (operation 'p15-s23-c6c10-canonical-scalar-form)
        named (operation 'p15-s23-c6c10-valid-named-component?)
        unicode (operation 'p15-s23-c6c10-require-unicode-string!)
        ancestors (operation 'p15-s23-c6c10-canonical-ancestors)
        originals (zipmap [carrier scalar named unicode] (map deref [carrier scalar named unicode]))]
    (doseq [provided? [false true]]
      (let [active (java.util.IdentityHashMap.) marker (Object.) seen (atom [])
            wrappers (into {} (for [[target original] originals]
                                [target (fn [& args]
                                          (swap! seen conj [target @ancestors (last args)])
                                          (apply original args))]))]
        (.put active marker true)
        (.put active :qualified/name true)
        (with-bindings {ancestors (when provided? active)}
          (with-redefs-fn wrappers #(is (= [:keyword "qualified" "name"] (form :qualified/name))))
          (is (identical? (when provided? active) @ancestors)))
        (is (= [carrier scalar named named unicode unicode] (mapv first @seen)))
        (is (= [:qualified/name :qualified/name "name" "qualified" "qualified" "name"]
               (mapv #(nth % 2) @seen)))
        (is (every? #(instance? java.util.IdentityHashMap (second %)) @seen))
        (is (every? #(identical? (second (first @seen)) (second %)) @seen))
        (when provided? (is (identical? active (second (first @seen)))))
        (is (= 2 (.size active)))
        (is (true? (.get active marker)))
        (is (true? (.get active :qualified/name)))))))

(deftest carrier-rejection-precedes-scalar-and-restores-binding
  (let [carrier (operation 'p15-s23-c6c10-canonical-carrier)
        scalar (operation 'p15-s23-c6c10-canonical-scalar-form)
        ancestors (operation 'p15-s23-c6c10-canonical-ancestors)
        active (java.util.IdentityHashMap.) original-error (ex-info "carrier rejected" {:first true})]
    (with-bindings {ancestors active}
      (with-redefs-fn
        {carrier (fn [& _] (throw original-error))
         scalar (fn [& _] (throw (AssertionError. "Scalar ran after carrier rejection")))}
        #(is (identical? original-error (try (form :x) nil (catch Throwable e e)))))
      (is (identical? active @ancestors))
      (is (zero? (.size active))))))

(deftest nonkeyword-and-both-collision-contracts-remain-active
  (is (= [:vector [[:nil] [:boolean true] [:integer "1"] [:symbol nil "s"]
                    [:string "text"] [:list [[:keyword nil "x"]]]]]
         (form [nil true 1 's "text" (list :x)])))
  (let [unique (operation 'p15-s23-c6c10-unique-resolved-values!)]
    (doseq [[values canonical-count host-count]
            [[[[1] (list 1)] 2 1] [[1 (bigint 1)] 1 1]]]
      (let [result (capture #(unique path :collision-free-resolved-map-keys values))]
        (is (= "C6-VERIFY" (get-in result [:data :id])))
        (is (= :collision-free-resolved-map-keys (get-in result [:data :missing-fact])))
        (is (= canonical-count (get-in result [:data :facts :canonical-identity-count])))
        (is (= host-count (get-in result [:data :facts :host-equality-count])))))))

(deftest scalar-rejection-preserves-caller-ancestor-markers
  (let [scalar (operation 'p15-s23-c6c10-canonical-scalar-form)
        ancestors (operation 'p15-s23-c6c10-canonical-ancestors)
        active (java.util.IdentityHashMap.) marker (Object.)
        original-error (ex-info "Scalar rejected" {:scalar true})]
    (.put active marker true)
    (.put active :x true)
    (with-bindings {ancestors active}
      (with-redefs-fn
        {scalar (fn [& _]
                  (is (identical? active @ancestors))
                  (throw original-error))}
        #(is (identical? original-error (try (form :x) nil (catch Throwable e e)))))
      (is (identical? active @ancestors)))
    (is (= 2 (.size active)))
    (is (true? (.get active marker)))
    (is (true? (.get active :x)))))

(deftest resolved-value-digest-collisions-still-use-exact-canonical-identity
  (let [unique (operation 'p15-s23-c6c10-unique-resolved-values!)
        digest (operation 'p15-s23-c6c10-streaming-digest)
        cases [[] [nil] [1 2 :a "a" [3] (list 4) 2/3 1.01M]
               [[1] (list 1)] [1.0M 1.00M] [(int 1) (long 1) (bigint 1)]
               [(array-map :a 1 :b 2) (array-map :b 2 :a 1)]
               [1 1 2 2 3]]]
    (doseq [values cases
            missing [:collision-free-resolved-map-keys :collision-free-resolved-set-items]]
      (let [canonical-count (count (distinct (mapv form values)))
            host-count (count (distinct values))
            expected (capture #(unique path missing values))
            collided (with-redefs-fn {digest (constantly "forced-digest-collision")}
                       #(capture (fn [] (unique path missing values))))]
        (is (= expected collided))
        (if (= canonical-count host-count (count values))
          (is (= {:value values} collided))
          (do
            (is (= "C6-VERIFY" (get-in collided [:data :id])))
            (is (= missing (get-in collided [:data :missing-fact])))
            (is (= {:value-count (count values) :canonical-identity-count canonical-count
                    :host-equality-count host-count :missing-fact missing}
                   (get-in collided [:data :facts])))))))))

(deftest distinct-digest-buckets-do-not-retain-normalized-value-trees
  (let [values (mapv (fn [i] {:id i :body ["payload" (list :value i)]}) (range 100))
        identity (operation 'p15-s23-c6c10-canonical-identity)]
    (with-redefs-fn
      {identity (fn [& _] (throw (ex-info "Unneeded full canonical identity" {:tripwire true})))}
      #(is (identical? values (bootstrap/p15-s23-c6c10-unique-resolved-values!
                              path :collision-free-resolved-map-keys values))))))

(deftest all-values-validate-before-digest-and-uniqueness-comparisons
  (let [digest (operation 'p15-s23-c6c10-streaming-digest)
        calls (atom 0)]
    (doseq [invalid [(with-meta [] {:unexpected true}) (Object.)
                     (String. (char-array [(char 0xD800)]))]]
      (let [values [:duplicate :duplicate invalid]
            expected (capture #(mapv form values))
            actual (with-redefs-fn
                     {digest (fn [& _] (swap! calls inc) (throw (ex-info "Digest opened early" {})))}
                     #(capture (fn [] (bootstrap/p15-s23-c6c10-unique-resolved-values!
                                      path :collision-free-resolved-map-keys values))))]
        (is (= expected actual))
        (is (zero? @calls))))))

(deftest resolved-values-preserve-deep-structured-inputs
  (let [values [(nth (iterate vector :left) 1025) (nth (iterate vector :right) 1025)]]
    (is (identical? values (bootstrap/p15-s23-c6c10-unique-resolved-values!
                            path :collision-free-resolved-set-items values)))))

(deftest digest-reference-resolution-retains-map-and-set-collision-diagnostics
  (let [digest (str "sha256:" (apply str (repeat 64 "a")))
        resolve #(bootstrap/p15-s23-c6c10-resolve-digest-references! path % 1 nil [digest])]
    (doseq [[value missing] [[(array-map {:digest-ref 0} :reference digest :literal)
                             :collision-free-resolved-map-keys]
                            [(hash-set {:digest-ref 0} digest) :collision-free-resolved-set-items]]]
      (let [result (capture #(resolve value))]
        (is (= "C6-VERIFY" (get-in result [:data :id])))
        (is (= missing (get-in result [:data :missing-fact])))
        (is (= [2 1 1] ((juxt :value-count :canonical-identity-count :host-equality-count)
                        (get-in result [:data :facts]))))))
    (is (= {digest [:payload]} (resolve {{:digest-ref 0} [:payload]})))
    (is (= #{digest} (resolve #{{:digest-ref 0}})))))
