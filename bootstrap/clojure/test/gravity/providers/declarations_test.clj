(ns gravity.providers.declarations-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.providers.declarations :as declarations]))

;; Data-only filesystem example; no filesystem service or grant is installed.
(def filesystem
  {:provider-id 'example.fs/recorded :version "1.2.0" :kind :filesystem
   :implements #{:fs/read :fs/stat} :profiles #{:hosted :meta} :targets #{:jvm}
   :runtime-effects #{:filesystem/read} :build-effects #{:build/read-file}
   :requires #{:memory/arena} :contracts ['gravity.contracts/FileSystem]
   :failures #{'example.fs/NotFound 'example.fs/Denied}
   :scope-policy :example.fs/grant-scoped
   :blocking :example.fs/may-block :concurrency :example.fs/thread-safe
   :resource-behavior :example.fs/caller-closes-handles
   :memory-behavior :example.fs/arena-buffers
   :determinism :example.fs/content-dependent :replay :example.fs/recorded-inputs
   :artifact-schema :gravity.provider/fs-v1
   :conformance-suite :gravity.conformance/fs :trust-level :local-dev
   :signature-policy :example.policy/development
   :metadata {:unsafe-island :example.fs/native-adapter
              :runtime-checked [:bounds] :ai/human-review :required}})

(def context
  {:source-id "fixture:filesystem-declaration-v1"
   :source-span {:line 15 :column 3}
   :manifest-entry [:providers 'example.fs/recorded]
   :profile :hosted :target :jvm :signature-policy-required? true})

(deftest complete-filesystem-record-preserves-all-explicit-fields
  (let [result (declarations/record-declaration filesystem context)]
    (is (= :valid (:status result)))
    (is (= [] (:diagnostics result)))
    (is (= filesystem (get-in result [:record :declaration])))
    (is (= context (get-in result [:record :provenance])))
    (is (= #{:schema :declaration :provenance} (set (keys (:record result)))))
    (is (= result (declarations/record-declaration filesystem context)))
    (is (not= (:runtime-effects filesystem) (:build-effects filesystem)))
    (is (not= (:implements filesystem) (:runtime-effects filesystem)))))

(deftest missing-required-fields-never-produce-a-record
  (doseq [field (keys filesystem)
          :when (not= :metadata field)]
    (testing (name field)
      (let [result (declarations/record-declaration (dissoc filesystem field) context)]
        (is (= :invalid (:status result)))
        (is (not (contains? result :record)))
        (is (= [{:id "L15-CONTRACT" :message "Invalid explicit provider declaration"
                 :path [:declaration field] :reason :missing-field
                 :context context}]
               (mapv #(dissoc % :expected) (:diagnostics result))))))))

(deftest validator-and-producer-share-precise-failures
  (doseq [[declaration path reason expected]
          [[(dissoc filesystem :version) [:declaration :version]
            :missing-field :nonblank-string]
           [(dissoc filesystem :contracts) [:declaration :contracts]
            :missing-field :nonempty-contract-vector]
           [(assoc filesystem :runtime-effects [:filesystem/read])
            [:declaration :runtime-effects] :invalid-field :keyword-set]]]
    (let [result {:status :invalid
                  :diagnostics [{:id "L15-CONTRACT"
                                 :message "Invalid explicit provider declaration"
                                 :path path :reason reason :expected expected
                                 :context context}]}]
      (is (= result (declarations/validate-declaration declaration context)))
      (is (= result (declarations/record-declaration declaration context)))))
  (is (= {:status :valid :diagnostics []}
         (declarations/validate-declaration filesystem context)))
  ;; An explicit non-replayability declaration is not a release-use decision.
  (is (= :example.fs/non-replayable
         (get-in (declarations/record-declaration
                  (assoc filesystem :replay :example.fs/non-replayable) context)
                 [:record :declaration :replay]))))

(deftest malformed-fields-and-effect-carriers-reject
  (doseq [[field values]
          [[:provider-id [nil :example.fs/recorded 'unqualified]]
           [:version [nil "" "  " 1]]
           [:kind [nil "filesystem"]]
           [:implements [#{} [:fs/read] #{"fs/read"}]]
           [:profiles [#{} [:hosted] #{nil}]]
           [:targets [#{} [:jvm] #{"jvm"}]]
           [:runtime-effects [nil [:filesystem/read] #{"secret-effect"}]]
           [:build-effects [nil :build/read-file #{false}]]
           [:requires [nil [:memory/arena] #{"memory/arena"}]]
           [:contracts [nil [] #{'gravity.contracts/FileSystem} ['FileSystem]]]
           [:failures [nil ['example.fs/Denied] #{"Denied"}]]
           [:scope-policy [nil :unqualified {}]]
           [:blocking [nil {}]] [:concurrency [nil false]]
           [:resource-behavior [nil {}]] [:memory-behavior [nil []]]
           [:determinism [nil true]] [:replay [nil {}]]
           [:artifact-schema [nil "fs-v1"]] [:conformance-suite [nil {}]]
           [:trust-level [nil :implicitly-trusted]]
           [:signature-policy [nil false :not-applicable]]]
          value values]
    (let [result (declarations/record-declaration (assoc filesystem field value) context)]
      (is (= :invalid (:status result)) (str field))
      (is (not (contains? result :record)))
      (is (= [[:declaration field]] (mapv :path (:diagnostics result))))
      (is (= [:invalid-field] (mapv :reason (:diagnostics result)))))))

(deftest explicit-empty-sets-and-conditional-signature-policy
  (let [declaration (-> filesystem
                        (assoc :runtime-effects #{} :build-effects #{}
                               :requires #{} :failures #{})
                        (dissoc :signature-policy))
        optional (assoc context :signature-policy-required? false)]
    (is (= :valid (:status (declarations/record-declaration declaration optional))))
    (is (not (contains? (get-in (declarations/record-declaration declaration optional)
                              [:record :declaration]) :signature-policy)))
    (is (= [[:declaration :signature-policy]]
           (mapv :path (:diagnostics (declarations/record-declaration declaration context))))))
  (doseq [trust [:trusted-core :trusted-package :local-dev :sandboxed :untrusted]]
    (is (= trust (get-in (declarations/record-declaration
                         (assoc filesystem :trust-level trust) context)
                        [:record :declaration :trust-level])))))

(deftest supplied-authority-or-completeness-cannot-fill-missing-fields
  (let [claims {:declaration-complete? true :trusted? true :status :granted
                :conformance-verified? true}
        claimed (merge filesystem claims)]
    (is (= claimed (get-in (declarations/record-declaration claimed context)
                          [:record :declaration])))
    (doseq [field [:version :contracts :trust-level :resource-behavior :replay]]
      (let [result (declarations/record-declaration (dissoc claimed field) context)]
        (is (= :invalid (:status result)))
        (is (not (contains? result :record)))
        (is (= #{:status :diagnostics} (set (keys result))))))))

(deftest diagnostics-retain-public-provenance-without-payloads
  (let [secret "SECRET-CONTENT-DO-NOT-EMIT"
        input (-> filesystem (dissoc :version :contracts :resource-behavior :replay)
                  (assoc :build-effects #{secret} :secret secret
                         :scope {:credential secret}))
        source (-> context (assoc :source-text secret :payload {:secret secret})
                   (assoc-in [:source-span :source-text] secret))
        result (declarations/record-declaration input source)]
    (is (= [[:declaration :version] [:declaration :build-effects]
            [:declaration :contracts] [:declaration :resource-behavior]
            [:declaration :replay]] (mapv :path (:diagnostics result))))
    (is (every? #(= context (:context %))
                (:diagnostics result)))
    (is (not (str/includes? (pr-str result) secret)))
    (is (not (contains? result :record)))
    (is (= result (declarations/record-declaration input source)))))

(deftest provenance-and-applicability-must-be-explicit
  (doseq [source [nil {} (dissoc context :source-id)
                  (dissoc context :signature-policy-required?)
                  (assoc context :signature-policy-required? nil)
                  (dissoc context :source-span :manifest-entry)
                  (assoc context :source-span {:line 0 :column 1})
                  (assoc context :manifest-entry [:providers {:secret "hidden"}])
                  (assoc context :profile "hosted")]]
    (let [result (declarations/record-declaration filesystem source)]
      (is (= :invalid (:status result)))
      (is (not (contains? result :record)))
      (is (every? #(= :context (first (:path %))) (:diagnostics result)))
      (is (not (str/includes? (pr-str result) "hidden")))))
  (doseq [location [:source-span :manifest-entry]]
    (is (= :valid (:status (declarations/record-declaration
                           filesystem (dissoc context location))))))
  (doseq [declaration [nil [] "secret declaration"]]
    (let [result (declarations/record-declaration declaration context)]
      (is (= [[:declaration]] (mapv :path (:diagnostics result))))
      (is (not (str/includes? (pr-str result) "secret declaration"))))))
