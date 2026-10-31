(ns gravity.reader.native-admission-test
  "Affected SOURCE/MODELED ingress tests. Native validator and host ingress
  bodies are isolated from source; lower construction/envelope/opaque ports
  are synthetic. No Bootstrap, Syntax, Reader or Gravity runtime is loaded."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.reader.pure-text :as reader-pure-text]))

(def ^:private root "bootstrap/clojure/src/gravity/bootstrap_impl/components/")

(declare p15-s23-trusted-carrier-validation sh03-c3-precomputed-c2-verify!
         reader-artifact-view)

(defn- source-forms [path]
  (with-open [r (java.io.PushbackReader. (io/reader path))]
    (binding [*read-eval* false]
      (loop [out []]
        (let [f (read {:eof ::end} r)]
          (if (= ::end f) out (recur (conj out f))))))))

(defn- definition [path name]
  (or (some #(when (and (seq? %) (contains? #{'def 'defn 'defn-} (first %))
                        (= name (second %))) %)
            (tree-seq coll? seq (source-forms path)))
      (throw (ex-info "Missing source definition" {:name name}))))

(defn- install! [path names]
  (binding [*ns* (the-ns 'gravity.reader.native-admission-test)]
    (doseq [name names] (eval (definition path name)))))

;; Use the owning class-first native DATA validator, not a mock that simply
;; rejects everything. Only its exact definitions and fixed reflection helpers
;; are installed, without the containing checked-core authority closure.
(install!
 (str root "bootstrap-p15/00588-p15-s23-stage2-native-carrier-header.clj")
 '[p15-s23-stage2-native-hashset-field p15-s23-stage2-native-hashset-field-once
   p15-s23-stage2-native-arraymap-field p15-s23-stage2-native-arraymap-field-once])
(install!
 (str root "bootstrap-p15/00700-static-rebuild-token-009-p15-s23-stage2-closed-checked-core-verify-through-p15-s23-trusted-carrier-vector-classes.clj")
 '[p15-s23-trusted-carrier-map-classes])
(install!
 (str root "bootstrap-p15/00700-static-rebuild-token-010-p15-s23-trusted-carrier-set-classes-through-p15-s23-trusted-carrier-validation.clj")
 '[p15-s23-trusted-carrier-set-classes p15-s23-trusted-carrier-list-classes
   p15-s23-trusted-carrier-scalar-classes p15-s23-host-native-field
   p15-s23-host-arraymap-storage-valid? p15-s23-host-carrier-header
   p15-s23-trusted-carrier-validation])

(declare sh03-reader-internal-product-authority sh03-reader-verification-invocation!
         sh03-reader-authenticated-envelope-options reader-pure-source-artifact
         p15-s23-sh02-stage-envelope-keys p15-s23-stage2-sh02-descriptor-envelope-verify!
         c2-reader-fail! source-span standard-reader-options)

(install! (str root "self-hosting-sh03-sh07/00850-sh03-c3-precomputed-c2-verify.clj")
          '[sh03-c3-precomputed-c2-verify!])
(install! "bootstrap/clojure/src/gravity/syntax.clj" '[reader-artifact-view])

(deftype CallbackCarrier [calls]
  clojure.lang.ILookup
  (valAt [_ _] (swap! calls conj :lookup) nil)
  (valAt [_ _ default] (swap! calls conj :lookup) default)
  clojure.lang.Seqable
  (seq [_] (swap! calls conj :seq) nil)
  clojure.lang.IMeta
  (meta [_] (swap! calls conj :meta) nil)
  clojure.lang.IHashEq
  (hasheq [_] (swap! calls conj :hash) (hash :source-unit-record))
  Object
  (equals [_ _] (swap! calls conj :equality) false)
  (hashCode [_] (swap! calls conj :hash-code) (.hashCode :source-unit-record)))

(defn- failure [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (ex-data e))))

(def ^:private authority (Object.))
(def ^:private standard-options
  {:retain-comments true :enabled-features #{} :extension-policy "standard-model"})
(def ^:private envelope
  {:status :accepted :stage :c2-reader :sealed-artifact {:artifact-kind :gravity/sh03-reader-products}})
(def ^:private standard-artifact
  {:kind :gravity/stage0-c2-reader-document-artifact
   :source-unit-record {:path "pair.gravity" :reader-options standard-options}
   :reader-extension-invocation-records [] :form-tree []
   :gravity-reader-boundary
   {:slice :SH-03 :owner :gravity-source :adapter-contract :gravity/sh03-to-c2-reader-products-v2
    :plan-binding {:source :current} :authenticated-envelope envelope
    :authenticated-envelope-descriptor {}}})
(def ^:private pure-artifact
  (-> standard-artifact
      (assoc-in [:source-unit-record :reader-options :extension-policy] (reader-pure-text/policy-id))
      (assoc :reader-extension-invocation-records [{:schema-version 2}])
      (assoc-in [:gravity-reader-boundary :pure-text-reconstruction-input]
                {:source-text "original" :project-context {}})))
(def ^:private invocation {:reader-source-path "current.gravity"})

(defn- with-ports [calls fresh f]
  (let [validate p15-s23-trusted-carrier-validation
        native (fn [data policy] (swap! calls conj :native-validation) (validate data policy))
        fail (fn [id _ _ facts] (throw (ex-info id (assoc facts :id id))))
        opaque (fn [& _] (swap! calls conj :opaque))
        ports {:p15-s23-trusted-carrier-validation native
               :sh03-c3-precomputed-c2-verify! opaque
               :sh03-reader-internal-product-authority authority
               :sh04-syntax-registered-literal-registry (fn [& _] (swap! calls conj :registry) {:bindings []})
               :sh04-syntax-semantic-source-id (fn [& _] :source)
               :sh04-syntax-reader-binding (fn [& _] {})
               :p15-s23-c6c10-canonical-digest (fn [& _] :digest)
               :sh04-syntax-current-sh03-product-binding (fn [& _] {})}]
    (with-redefs [p15-s23-trusted-carrier-validation native
                  c2-reader-fail! fail source-span (fn [& _] {})
                  standard-reader-options standard-options
                  sh03-reader-internal-product-authority authority
                  p15-s23-sh02-stage-envelope-keys (set (keys envelope))
                  sh03-reader-authenticated-envelope-options (fn [& _] (swap! calls conj :source))
                  sh03-reader-verification-invocation! (fn [& _] (swap! calls conj :binding) {:binding {:source :current}})
                  reader-pure-source-artifact (fn [& _] (swap! calls conj :fresh) fresh)
                  p15-s23-stage2-sh02-descriptor-envelope-verify! (fn [& _] (swap! calls conj :envelope) :passed)]
      (f ports))))

(deftest unsupported-marker-carriers-never-invoke-callbacks-or-lower-ports
  (let [callbacks (atom []) foreign (CallbackCarrier. callbacks)
        stripped (-> pure-artifact
                     (assoc-in [:source-unit-record :reader-options] standard-options)
                     (update :gravity-reader-boundary dissoc :pure-text-reconstruction-input))
        candidates [foreign
                    (assoc stripped :reader-extension-invocation-records [foreign])
                    (assoc stripped :reader-extension-invocation-records foreign)
                    (assoc stripped :reader-extension-invocation-records [{:schema-version foreign}])
                    (assoc stripped :form-tree [foreign])
                    (assoc-in stripped [:source-unit-record :reader-options] foreign)
                    (assoc-in stripped [:source-unit-record :reader-options :extension-policy] foreign)
                    (assoc-in stripped [:gravity-reader-boundary :authenticated-envelope-descriptor
                                       :semantic-projections] [foreign])
                    (assoc-in stripped [:gravity-reader-boundary :authenticated-envelope-descriptor
                                       :semantic-projections] [{:value foreign}])
                    (with-meta stripped {:foreign foreign})
                    ;; Foreign map keys must not be compared during lookup.
                    (into (assoc stripped foreign :collision)
                          {:a 1 :b 1 :c 1 :d 1 :e 1 :f 1 :g 1 :h 1})]]
    (doseq [artifact candidates endpoint [:c3 :view]]
      (reset! callbacks [])
      (let [calls (atom [])]
        (with-ports calls pure-artifact
          (fn [ports]
            (is (= [:native-pure-text-admission-input]
                   (:missing-fields
                    (failure #(if (= endpoint :c3)
                                (sh03-c3-precomputed-c2-verify! authority "pair.gravity" artifact invocation)
                                (reader-artifact-view artifact invocation ports))))))
            (is (= [:native-validation] @calls))
            (is (empty? @callbacks))))))
    ;; Positive controls prove each carrier method is observable.
    (is (nil? (get foreign :x)))
    (is (nil? (seq foreign)))
    (is (nil? (meta foreign)))
    (is (= (hash :source-unit-record) (hash foreign)))
    (is (false? (.equals foreign :x)))
    (is (= (.hashCode :source-unit-record) (.hashCode foreign)))
    (is (= #{:lookup :seq :meta :hash :equality :hash-code} (set @callbacks)))))

(deftest native-default-and-pure-fresh-routing-survive
  (doseq [artifact [standard-artifact
                    (assoc standard-artifact
                           :parsed-semantic-values [(java.util.Date. 0) (java.util.UUID. 0 0)]
                           :form-tree [{:kind :tagged-literal :tag 'inst :value (java.util.Date. 0)}
                                       {:kind :tagged-literal :tag 'uuid :value (java.util.UUID. 0 0)}])]]
    (let [calls (atom [])]
      (with-ports calls artifact
        (fn [ports]
          (is (= artifact (sh03-c3-precomputed-c2-verify! authority "pair.gravity" artifact invocation)))
          (is (= [:binding :envelope] @calls))
          (reset! calls [])
          (reader-artifact-view artifact invocation ports)
          (is (= [:opaque :registry] @calls))))))
  (let [calls (atom [])]
    (with-ports calls pure-artifact
      (fn [ports]
        (is (= pure-artifact (sh03-c3-precomputed-c2-verify! authority "pair.gravity" pure-artifact invocation)))
        (is (= [:native-validation :source :fresh :binding :envelope] @calls))
        (reset! calls [])
        (let [ports (assoc ports :sh03-c3-precomputed-c2-verify!
                           (fn [& args] (swap! calls conj :opaque)
                             (apply sh03-c3-precomputed-c2-verify! args)))]
          (reader-artifact-view pure-artifact invocation ports)
          (is (= [:native-validation :opaque :native-validation :source :fresh :binding :envelope :registry]
                 @calls))
          (reset! calls [])
          (is (= [:pure-text-fresh-reader-reconstruction]
                 (:missing-fields (failure #(reader-artifact-view pure-artifact nil ports)))))
          (is (= [:native-validation :opaque :native-validation] @calls)))
        (reset! calls [])
        (is (= [:pure-text-fresh-reader-reconstruction]
               (:missing-fields (failure #(sh03-c3-precomputed-c2-verify!
                                           authority "pair.gravity" pure-artifact nil)))))
        (is (= [:native-validation] @calls)))))
  (doseq [artifact [(assoc-in standard-artifact [:source-unit-record :reader-options :extension-policy]
                              (reader-pure-text/policy-id))
                    (assoc-in standard-artifact [:gravity-reader-boundary :pure-text-reconstruction-input] nil)
                    (assoc standard-artifact :reader-extension-invocation-records [{:schema-version 2}])
                    (assoc standard-artifact :form-tree [{:tag 'text}])
                    (assoc-in standard-artifact [:gravity-reader-boundary :authenticated-envelope-descriptor
                                                 :semantic-projections] [{:value {:pure-text-record-id "marker"}}])]]
    (is (true? (reader-pure-text/admission-required? artifact)))
    (let [calls (atom [])]
      (with-ports calls pure-artifact
        (fn [_]
          (is (= [:pure-text-fresh-reader-reconstruction]
                 (:missing-fields (failure #(sh03-c3-precomputed-c2-verify!
                                             authority "pair.gravity" artifact nil)))))
          (is (= [:native-validation] @calls))))))
  (let [stripped (-> pure-artifact
                     (assoc-in [:source-unit-record :reader-options] standard-options)
                     (update :gravity-reader-boundary dissoc :pure-text-reconstruction-input))
        calls (atom [])]
    (with-ports calls pure-artifact
      (fn [_]
        (is (true? (reader-pure-text/admission-required? stripped)))
        (is (= [:pure-text-original-source-input]
               (:missing-fields (failure #(sh03-c3-precomputed-c2-verify!
                                           authority "pair.gravity" stripped invocation)))))))))

(def ^:private options-prefix
  (let [constructor (definition (str root "compiler/00829-compiler-c2-reader-source-artifact.clj")
                                'compiler-c2-reader-source-artifact)
        arity (last constructor)]
    (binding [*ns* (the-ns 'gravity.reader.native-admission-test)]
      (eval (list* 'fn (first arity) (concat (take 3 (rest arity)) ['(source-span :lower-port)]))))))

(deftest options-are-validated-before-policy-lookup-or-equality
  (let [callbacks (atom []) foreign (CallbackCarrier. callbacks)
        select reader-pure-text/options?
        candidates [foreign
                    {:extension-policy foreign}
                    (assoc standard-options :enabled-features foreign)
                    (assoc standard-options :extra [foreign])
                    (with-meta standard-options {:foreign foreign})
                    (assoc standard-options foreign true)]]
    (doseq [options candidates]
      (reset! callbacks [])
      (let [calls (atom [])]
        (with-ports calls nil
          (fn [_]
            (with-redefs [reader-pure-text/options? (fn [opts] (swap! calls conj :selection) (select opts))
                          source-span (fn [& _] (swap! calls conj :lower-port))]
              (is (= [:native-reader-options]
                     (:missing-fields (failure #(options-prefix "pair.gravity" "original" {} nil nil
                                                                 "current.gravity" invocation options)))))
              (is (= [:native-validation] @calls))
              (is (empty? @callbacks)))))))
    (doseq [options [standard-options (assoc standard-options :extension-policy (reader-pure-text/policy-id))]]
      (let [calls (atom [])]
        (with-ports calls nil
          (fn [_]
            (with-redefs [source-span (fn [& _] (swap! calls conj :lower-port))]
              (options-prefix "pair.gravity" "original" {} nil nil "current.gravity" invocation options)
              (is (= :native-validation (first @calls)))
              (is (= :lower-port (last @calls)))))))))
  (is (nil? (find-ns 'gravity.bootstrap)))
  (is (nil? (find-ns 'gravity.syntax))))
