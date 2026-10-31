(ns gravity.reader.pure-text
  "Fixed policy and projection of verified Gravity pure-text records.
  These helpers do not authenticate caller data or execute a host handler."
  (:require [gravity.c2-artifact-identity.canonical :as canonical]
            [gravity.digest :as digest]))

(def policy
  {:policy :gravity/sealed-pure-text-reader :version 1
   :registered-tags ['text] :ambient-authority :denied
   :text-handler 'sh03-reader-read-text})

(defn policy-id []
  (letfn [(normalize [value]
            (canonical/reader-canonical-value normalize value))]
    (canonical/reader-canonical-hash digest/sha256-hex normalize policy)))

(defn options? [options] (= (policy-id) (:extension-policy options)))

(defn raw-policy? [policy]
  (= :gravity/sealed-pure-text-reader (:extension-policy policy)))

(defn admission-required? [artifact]
  (or (options? (get-in artifact [:source-unit-record :reader-options]))
      (contains? (:gravity-reader-boundary artifact) :pure-text-reconstruction-input)
      (some #(= 2 (:schema-version %)) (:reader-extension-invocation-records artifact))
      (some #(= 'text (:tag %)) (:form-tree artifact))
      (some #(contains? (:value %) :pure-text-record-id)
            (get-in artifact [:gravity-reader-boundary :authenticated-envelope-descriptor
                              :semantic-projections]))))

(defn- refuse! []
  (throw (ex-info "Invalid verified Gravity pure-text projection"
                  {:id "C2-HASH" :missing-fields [:gravity-pure-text-result-binding]})))

(defn record!
  "Validate the projection joins after the normal raw Reader verifier.
  Source/plan authenticity must come from that owning invocation, not this API."
  [result]
  (let [records (:reader-extension-invocation-records result)
        record (first records)
        forms (:form-tree result)
        by-id (into {} (map (juxt :form-id identity) forms))
        tagged (filterv #(= :tagged-literal (:kind %)) forms)
        policy (get-in result [:source-unit :reader-options])]
    (when-not
     (and (raw-policy? policy)
          (= 1 (count records))
          (= #{:artifact :schema-version :policy :tag :tag-codepoints :handler
               :handler-policy :build-effects :capabilities :profiles :invocations :status}
             (set (keys record)))
          (= :gravity/reader-extension-invocation (:artifact record))
          (= 2 (:schema-version record))
          (= policy (:policy record))
          (= {:kind :symbol :name-codepoints [116 101 120 116]} (:tag record))
          (= [116 101 120 116] (:tag-codepoints record))
          (= 'sh03-reader-read-text (:handler record))
          (= {:registered? true :enabled? true :input :syntax-seed :output :syntax-seed
              :ambient-authority-denied? true} (:handler-policy record))
          (= #{} (:build-effects record) (:capabilities record))
          (= #{:meta} (:profiles record))
          (= (if (seq tagged) :invoked :registered-not-invoked) (:status record))
          (= (mapv :form-id tagged) (mapv :form-id (:invocations record))))
      (refuse!))
    (doseq [[form invocation] (map vector tagged (:invocations record))]
      (let [payload (by-id (first (:children form)))
            seed (:input-seed invocation)]
        (when-not
         (and (= [116 101 120 116] (:tag-codepoints form))
              (= 1 (count (:children form)))
              (= :string (:kind payload))
              (= #{:form-id :payload-form-id :span :raw :input-seed :returned-seed :invocation}
                 (set (keys invocation)))
              (= (:form-id form) (:form-id invocation))
              (= (:form-id payload) (:payload-form-id invocation))
              (= (:span form) (:span invocation))
              (= (:raw form) (:raw invocation))
              (= #{:source-id :source-content-id :form-id :payload-form-id :span :datum}
                 (set (keys seed)))
              (= (get-in result [:source-unit :source-id]) (:source-id seed))
              (= (get-in result [:source-unit :bytes-hash]) (:source-content-id seed))
              (= (:form-id form) (:form-id seed))
              (= (:form-id payload) (:payload-form-id seed))
              (= (:span form) (:span seed))
              (= #{:kind :decoded-codepoints} (set (keys (:datum seed))))
              (= :string (get-in seed [:datum :kind]))
              (= seed (:returned-seed invocation))
              (= {:handler 'sh03-reader-read-text :policy policy :build-effects #{}
                  :capabilities #{} :status :invoked} (:invocation invocation)))
          (refuse!))))
    record))

(defn returned-data! [result]
  (into {} (map (fn [invocation]
                 [(:form-id invocation) (get-in invocation [:returned-seed :datum])])
               (:invocations (record! result)))))
