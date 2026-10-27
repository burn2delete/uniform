(ns gravity.c3-reader-lossless-span-test
  "Existing lossless literals must consume the current Reader seed shape."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c2-reader-product-projection :as projection]
            [gravity.syntax-object-stream :as stream]
            [gravity.c3-literal-projection :as literals]
            [gravity.c3-literal-projection-test :as fixture]))

(defn- sample [raw wrapped?]
  (let [deferred? (= "1/0" raw)
        value (if deferred? (literals/c3-deferred-ratio-descriptor-from-raw raw) 1/2)
        span (assoc fixture/ratio-span :byte-start (if wrapped? 10 0) :byte-end (if wrapped? 13 3))
        ratio (assoc fixture/ratio-form :raw raw :value value :span span)
        token (assoc fixture/ratio-token :raw raw :lexeme raw :decoded value :span span)
        prefix-span (assoc fixture/ratio-span :byte-end 1)
        root (if wrapped?
               {:form-id :wrapper :kind :metadata-wrapper :children [:form-0]
                :span (assoc span :byte-start 0) :raw (str "^:private " raw)
                :value value :expanded-form value :metadata {:private true} :abbrev :metadata
                :surface-span prefix-span
                :generated-origin [{:child-form-id :form-0 :from prefix-span :reason :metadata}]}
               ratio)
        literal {:literal-id :lit-0 :form-id :form-0 :decoded value :raw raw :span span
                 :facts {:numerator-spelling "1" :denominator-spelling (if deferred? "0" "2") :exact? true}}
        forms (if wrapped? [root ratio] [ratio])
        products {:form-tree forms :root-form-ids [(:form-id root)]
                  :parsed-records [{:form-id (:form-id root) :form value}]
                  :sh03-reader-result
                  {:top-level-form-ids ["form/0"]
                   :form-tree [{:form-id "form/0"
                                :raw {:artifact :gravity/source-slice :schema-version 1 :encoding :utf-8
                                      :byte-start 0 :byte-end (if wrapped? 13 3)
                                      :scalar-start 0 :scalar-end (if wrapped? 13 3)}}]}}
        seed (first (projection/with-operations {:syntax-object-stream stream/syntax-object-stream}
                      #(projection/c2-syntax-seed-stream "ratio.gravity" products nil)))
        artifact (assoc fixture/ratio-artifact :form-tree forms :token-stream [token]
                        :syntax-seed-stream [seed] :literal-decoding-records [literal]
                        :semantic-error-deferment-record
                        {:deferred-literal-records (if deferred? [{:form-id :form-0 :kind :ratio
                                                                 :raw raw :value value :span span}] [])})]
    {:seed seed :root root :artifact artifact :status (if deferred? :deferred :accepted)}))
(defn- descriptor [{:keys [seed root artifact]}]
  (literals/c3-lossless-literal-descriptor seed root artifact {:authentic? true}))

(deftest current-seed-scalars-preserve-exact-and-deferred-lossless-descriptors
  (doseq [raw ["1/2" "1/0"] wrapped? [false true]]
    (let [{:keys [seed root artifact status] :as case} (sample raw wrapped?)
          result (descriptor case)]
      (is (= status (:semantic-validation result)))
      (is (= raw (:raw result)))
      (is (= result (:reader-literal-descriptor
                     (literals/c3-source-facts seed root artifact {:authentic? true}))))
      (is (= (if wrapped? :metadata-wrapper :ratio)
             (literals/with-operations {:form-kind (constantly :legacy-fallback)}
               #(literals/c3-source-form-kind seed root artifact {:authentic? true})))))))

(deftest actual-retained-occurrence-keeps-all-legacy-and-explicit-scalar-fields
  (let [{:keys [seed root artifact] :as case} (sample "1/2" false)
        changed-seeds [(update-in seed [:span :scalar-end] inc)
                       (update seed :span dissoc :scalar-start :scalar-end)
                       (update-in seed [:span :byte-end] inc)
                       (assoc-in seed [:span :extra] true)
                       (assoc-in seed [:span :form-index] 1)
                       (assoc seed :syntax-id "substituted")]]
    (is (= :accepted (:semantic-validation (descriptor case))))
    (doseq [changed changed-seeds]
      (is (nil? (literals/c3-lossless-literal-descriptor changed root artifact {:authentic? true}))))
    (doseq [changed [nil [] (list seed)
                     [(assoc seed :form-id :other)]
                     [(assoc seed :syntax-id "substituted")]
                     [(assoc-in seed [:span :extra] true)]
                     [(update-in seed [:span :byte-end] inc)]
                     [(update seed :span dissoc :scalar-start)]
                     [(assoc-in seed [:span :scalar-start] -1)]]]
      (is (nil? (literals/c3-lossless-literal-descriptor
                  seed root (assoc artifact :syntax-seed-stream changed) {:authentic? true}))))
    (is (nil? (literals/c3-lossless-literal-descriptor
                (assoc seed :span nil) root (assoc artifact :syntax-seed-stream nil) {:authentic? true})))) )

(deftest absent-retained-stream-preserves-original-hosted-compatibility-shape
  (let [{:keys [seed root artifact]} (sample "1/2" false)
        artifact (dissoc artifact :syntax-seed-stream)
        legacy (update seed :span dissoc :scalar-start :scalar-end)]
    (is (= :accepted (:semantic-validation
                      (literals/c3-lossless-literal-descriptor legacy root artifact {:authentic? true}))))
    (is (nil? (literals/c3-lossless-literal-descriptor seed root artifact {:authentic? true})))))

(deftest integrity-callback-stays-fresh-and-retains-second-call-failure
  (let [{:keys [seed root artifact]} (sample "1/2" false)
        calls (atom 0) sentinel (ex-info "second fresh integrity callback rejects" {})]
    (literals/with-operations
      {:c3-c2-reader-integrity-report (fn [actual]
                                      (is (identical? artifact actual))
                                      (if (= 1 (swap! calls inc)) {:authentic? true} (throw sentinel)))}
      #(do (is (= :accepted (:semantic-validation
                             (literals/c3-lossless-literal-descriptor seed root artifact nil))))
           (is (identical? sentinel
                 (try (literals/c3-lossless-literal-descriptor seed root artifact nil) nil
                      (catch Throwable error error))))))
    (is (= 2 @calls))
    (is (nil? (literals/c3-lossless-literal-descriptor seed root artifact {:authentic? false})))) )

(deftest opaque-artifacts-retain-the-original-field-callback-path
  (let [{:keys [seed root artifact]} (sample "1/2" false)
        calls (atom []) legacy (update seed :span dissoc :scalar-start :scalar-end)
        foreign (proxy [java.util.HashMap] []
                  (get [key] (swap! calls conj key) (get artifact key))
                  (containsKey [_] (throw (ex-info "new artifact lookup is forbidden" {}))))
        result (literals/c3-lossless-literal-descriptor legacy root foreign {:authentic? true})]
    (is (= :accepted (:semantic-validation result)))
    (is (= [:form-tree :token-stream :literal-decoding-records :semantic-error-deferment-record] @calls))))

(deftest native-artifact-field-selection-never-hashes-or-compares-opaque-keys
  (doseq [retained? [false true]]
    (let [{:keys [seed root artifact]} (sample "1/2" false)
          calls (atom 0) active? (atom false) target :syntax-seed-stream
          opaque (proxy [Object clojure.lang.IHashEq] []
                   (hashCode [] (when @active? (swap! calls inc)) (.hashCode target))
                   (hasheq [] (when @active? (swap! calls inc)) (.hasheq ^clojure.lang.IHashEq target))
                   (equals [_] (when @active? (swap! calls inc)) false)
                   (toString [] (swap! calls inc) "opaque"))
          artifact (cond-> (into artifact (map #(vector (keyword (str "other" %)) %) (range 20)))
                     (not retained?) (dissoc :syntax-seed-stream))
          artifact (assoc artifact opaque :not-a-stream)
          seed (if retained? seed (update seed :span dissoc :scalar-start :scalar-end))]
      (reset! calls 0) (reset! active? true)
      (let [result (literals/c3-lossless-literal-descriptor seed root artifact {:authentic? true})]
        (is (= :accepted (:semantic-validation result)))
        (is (zero? @calls))))))
