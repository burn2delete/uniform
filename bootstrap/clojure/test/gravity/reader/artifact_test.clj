(ns gravity.reader.artifact-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.reader :as reader]))

(defn- fixture [disposition stem extension]
  (str "bootstrap/clojure/fixtures/" disposition "/" stem extension))

(defn- operations []
  {:read-source-form-records bootstrap/read-source-form-records
   :validate-ns-syntax! bootstrap/validate-ns-syntax!
   :reader-module-context bootstrap/reader-module-context
   :syntax-object-stream bootstrap/syntax-object-stream
   :l1-source-unit-artifacts bootstrap/l1-source-unit-artifacts
   :standard-reader-options bootstrap/standard-reader-options
   :gravity-source-extension bootstrap/gravity-source-extension
   :gravity-source-kind bootstrap/gravity-source-kind
   :utf8-byte-count bootstrap/utf8-byte-count})

(defn- direct-artifact [path text]
  (reader/read-source-artifact
   path text (bootstrap/reader-project-context-for-source path) (operations)))

(defn- rejection [thunk]
  (try (thunk) ::unexpected-success
       (catch clojure.lang.ExceptionInfo exception exception)))

(deftest paired-source-artifacts-preserve-the-existing-reader-product
  (doseq [[extension kind] [[".gravity" :gravity-branded-source]
                            [".qst" :qst-theory-source]]]
    (let [path (fixture "accepted" "surface-syntax" extension)
          text (slurp path)
          direct (direct-artifact path text)
          compatibility (bootstrap/read-source-artifact path text)
          file-artifact (bootstrap/read-file-artifact path)
          syntax (:syntax-object-stream direct)]
      (is (= direct compatibility file-artifact))
      (is (= direct (direct-artifact path text)))
      (is (= :gravity/stage0-reader-artifact (:kind direct)))
      (is (= {:path path :extension extension :encoding :utf-8
              :source-kind kind :byte-count (bootstrap/utf8-byte-count text)
              :form-count 2}
             (:source direct)))
      (is (= {:module 'syntax.surface :profile :hosted :target :jvm :safety :safe}
             (select-keys (:module-context direct)
                          [:module :profile :target :safety])))
      (is (= {:doc "metadata survives reading"} (:metadata (second syntax))))
      (is (= path (get-in (first syntax) [:span :source])))
      (is (= 0 (get-in (first syntax) [:span :byte-start])))
      (is (= 1 (get-in (first syntax) [:span :start :line])))
      (is (seq (:namespace-clause-syntax direct)))
      (is (not (contains? (:module-context direct) :namespace-clause-syntax)))
      (is (= [] (:diagnostics direct)))
      (is (= path (get-in direct [:source-unit-record :path])))
      (is (= extension (get-in direct [:source-unit-record :extension])))
      (is (= kind (get-in direct [:source-unit-record :source-kind])))
      (is (re-matches #"sha256:[0-9a-f]{64}"
                      (get-in direct [:source-unit-record :source-id]))))))

(deftest identity-remains-sensitive-to-source-options-and-project-context
  (let [path (fixture "accepted" "surface-syntax" ".gravity")
        text (slurp path)
        context (bootstrap/reader-project-context-for-source path)
        produce #(reader/read-source-artifact path %1 %2 %3)
        original (produce text context (operations))
        source-id #(get-in % [:source-unit-record :source-id])
        changed-text (produce (str text "\n") context (operations))
        changed-options
        (produce text context
                 (assoc-in (operations) [:standard-reader-options :retain-comments] false))
        changed-context
        (produce text (assoc context :project-root-id
                             (bootstrap/reader-canonical-hash {:project :other}))
                 (operations))]
    (is (= (source-id original) (source-id (produce text context (operations)))))
    (is (not= (source-id original) (source-id changed-text)))
    (is (not= (source-id original) (source-id changed-options)))
    (is (not= (source-id original) (source-id changed-context)))
    (is (= (:syntax-object-stream original) (:syntax-object-stream changed-text)))
    (is (= (:source original) (:source changed-options)))
    (is (not= (:source original) (:source changed-text)))))

(deftest real-invalid-inputs-retain-stage-owned-diagnostics
  (doseq [extension [".gravity" ".qst"]
          [stem expected] [["surface-syntax-l1-delimiter" "L1-DELIMITER"]]]
    (let [path (fixture "rejected" stem extension)
          text (slurp path)
          direct (rejection #(direct-artifact path text))
          compatibility (rejection #(bootstrap/read-source-artifact path text))]
      (is (instance? clojure.lang.ExceptionInfo direct))
      (is (= expected (:id (ex-data direct))))
      (is (= (ex-message direct) (ex-message compatibility)))
      (is (= (ex-data direct) (ex-data compatibility)))
      (is (= path (get-in (ex-data direct) [:source-span :source]))))))

(deftest namespace-shape-rejection-remains-with-the-reader-owner
  (let [path (fixture "rejected" "invalid-ns-shape" ".gravity")
        text (slurp path)
        direct (rejection #(direct-artifact path text))
        compatibility (rejection #(bootstrap/read-source-artifact path text))]
    (is (= "L1-NS-SHAPE" (:id (ex-data direct))))
    (is (= (ex-data direct) (ex-data compatibility)))))

(deftest compatibility-interposition-and-operation-order-survive-extraction
  (let [path (fixture "accepted" "surface-syntax" ".gravity")
        text (slurp path)
        calls (atom [])
        original (operations)
        observed
        (into {} (map (fn [[key operation]]
                        [key (if (fn? operation)
                               (fn [& args]
                                 (swap! calls conj [key args])
                                 (apply operation args))
                               operation)]) original))
        context (bootstrap/reader-project-context-for-source path)
        result (reader/read-source-artifact path text context observed)]
    (is (= result (bootstrap/read-source-artifact path text context)))
    (is (= [:read-source-form-records :validate-ns-syntax!
            :reader-module-context :syntax-object-stream :l1-source-unit-artifacts
            :gravity-source-extension :gravity-source-kind :utf8-byte-count]
           (mapv first @calls)))
    (is (= [path text bootstrap/standard-reader-options context]
           (second (nth @calls 4))))
    (testing "a rejection is propagated unchanged before later stages run"
      (let [sentinel (ex-info "owner rejection" {:id "L1-NS-SHAPE" :owner :reader})
            later-calls (atom 0)
            raised
            (with-redefs [bootstrap/validate-ns-syntax! (fn [& _] (throw sentinel))
                          bootstrap/syntax-object-stream
                          (fn [& _] (swap! later-calls inc))]
              (rejection #(bootstrap/read-source-artifact path text context)))]
        (is (identical? sentinel raised))
        (is (zero? @later-calls))))
    (testing "the compatibility wrapper enters the stage API"
      (let [seen (atom nil)
            sentinel {:kind :test-seam}
            result
            (with-redefs [reader/read-source-artifact
                          (fn [& args] (reset! seen args) sentinel)]
              (bootstrap/read-source-artifact path text context))]
        (is (identical? sentinel result))
        (is (= [path text context] (vec (take 3 @seen))))
        (is (= (operations) (last @seen)))))))

(deftest namespace-contract-is-narrow-and-preserves-compatibility-arities
  (let [contract (:gravity/namespace-contract (meta #'reader/read-source-artifact))]
    (is (= #{'read-source-artifact} (set (keys (ns-publics 'gravity.reader)))))
    (is (empty? (ns-aliases 'gravity.reader)))
    (is (= :stage0-reader-artifact-assembly (:contract-boundary contract)))
    (is (= ['clojure.core] (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.cli 'gravity.release]
           (get-in contract [:dependency-direction :forbids])))
    (is (= (set (:operation-inputs contract)) (set (keys (operations)))))
    (is (= [] (get-in contract [:diagnostic-ownership :owns])))
    (is (true? (:clojure-seed-boundary? contract)))
    (is (false? (:canonical-c2-reader? contract)))
    (is (false? (:authenticated-reader? contract)))
    (is (false? (:self-hosted? contract)))
    (is (= '([source-path source-text] [source-path source-text project-context])
           (:arglists (meta #'bootstrap/read-source-artifact))))))
