(ns gravity.mir.related-location-containment-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as b]))

(def ^:private source-path "src/related-locations.gravity")
(def ^:private containment-boundary :contained-public-c11-verifier-diagnostic)

(defn- type-diagnostic []
  (b/p15-s23-c11-mir-diagnostic-record
   "C11-TYPE" source-path
   {:source-span {:source source-path :form-index 0}
    :operation-id :result
    :origin-chain [:source-form :expanded-result]}
   {:missing-fact :operation-type}))

(defn- related-locations [diagnostic]
  (let [artifact (get-in diagnostic [:primary :artifact])]
    (mapv (fn [form-index]
            (let [generated? (even? form-index)]
              {:role (if generated? :generated-by :call-site)
               :span {:source (if generated? "generated/related-locations.gravity" source-path)
                      :form-index form-index}
               :artifact artifact}))
          (range 17 0 -1))))

(defn- contain [diagnostic]
  (let [exception (try
                    (b/p15-s23-c11-mir-throw-record! diagnostic)
                    (catch clojure.lang.ExceptionInfo error error))]
    (try
      (b/p15-s23-c11-mir-contain-exception! source-path containment-boundary exception)
      nil
      (catch clojure.lang.ExceptionInfo error error))))

(deftest containment-preserves-ordered-related-locations
  (let [diagnostic (type-diagnostic)
        observation {:kind :diagnostic-boundary-observation
                     :status :recorded
                     :observed-at :c11-authenticated-mir}]
    (doseq [[label related] [["17 locations" (related-locations diagnostic)]
                             ["empty vector" []]]]
      (testing label
        (let [original (assoc diagnostic :related related)
              public (ex-data (contain original))]
          (is (vector? (:related public)))
          (is (= (update original :redactions conj observation)
                 (select-keys public (keys original)))))))))

(deftest containment-rejects-malformed-related-locations
  (let [diagnostic (type-diagnostic)
        related (related-locations diagnostic)
        private-marker "private-related-location-marker"]
    (doseq [[label malformed]
            [["nil collection" nil]
             ["non-map seventeenth entry" (assoc related 16 private-marker)]
             ["unknown field in seventeenth entry"
              (assoc-in related [16 :private-note] private-marker)]]]
      (testing label
        (let [error (contain (assoc diagnostic :related malformed))
              public (ex-data error)]
          (is (= {:id "C11-VERIFY" :rule "C11-VERIFY" :severity :internal-error
                  :missing-fact containment-boundary :related []}
                 (select-keys public [:id :rule :severity :missing-fact :related])))
          (is (some #(and (= :contained-host-error-hash (:kind %))
                          (= :redacted (:status %)))
                    (:redactions public)))
          (is (not (str/includes? (pr-str error) private-marker))))))))
