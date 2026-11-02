(ns gravity.reader.pure-integration-test
  "UNRUN genuine normal pure integration definition. Loading this namespace
  loads Bootstrap; do not use --list as a source-only inspection method."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.digest :as digest]
            [gravity.reader.dispatch :as dispatch]
            [gravity.syntax :as syntax]))

(def ^:private fixture-root "bootstrap/clojure/fixtures/reader-extension-binding/")
(def ^:private payload "registered pure payload")

(defn- failure-data [operation]
  (try (operation) {:unexpected-success true}
       (catch clojure.lang.ExceptionInfo error (ex-data error))))

(defn- inputs [stem extension]
  (let [root (.getCanonicalPath (io/file "."))
        relative (str fixture-root stem extension)
        path (.getCanonicalPath (io/file root relative))
        text (slurp path :encoding "UTF-8")]
    {:path path :text text
     :reader (.getCanonicalPath (io/file root bootstrap/sh03-reader-source-relative-path))
     :options (assoc bootstrap/standard-reader-options :extension-policy (dispatch/pure-policy-id))
     :context {:project-root-id (bootstrap/reader-project-root-id root)
               :project-root-path root :project-relative-path relative}}))

(defn- normal-products [{:keys [path text options context reader]}]
  ;; Existing five-argument constructor. It executes the real current Reader
  ;; producer/verifier and normal adapter; no raw result or handler replacement.
  (bootstrap/c2-reader-products path text options context reader))

(deftest normal-pure-current-source
  (let [pair (mapv #(inputs "normal-pure" %) [".qst" ".gravity"])]
    (is (= (:text (first pair)) (:text (second pair))))
    (doseq [{:keys [path text options context reader] :as input} pair]
      (testing path
        (let [products (normal-products input)
              unit (:source-unit products)
              report (:sh03-reader-verification-report products)
              results (:pure-extension-results products)
              _ (is (= 1 (count results)))
              result (first results)
              tagged (filterv #(= :tagged-literal (:kind %))
                              (get-in products [:sh03-reader-result :form-tree]))
              _ (is (= 1 (count tagged)))
              form (first tagged)
              implementation (select-keys (:sh03-reader-plan-binding products)
                                          [:source-content-hash :plan-semantic-hash
                                           :functions-semantic-hash])
              fresh (dispatch/verify-pure-result! unit 'text form payload implementation result)]
          (is (= :accepted (:status report)))
          (is (true? (:verified? report)))
          (is (= (bootstrap/c2-source-unit-record path text options context) unit))
          (is (= (str "sha256:" (digest/sha256-bytes-hex
                                 (.getBytes text java.nio.charset.StandardCharsets/UTF_8)))
                 (:bytes-hash unit)))
          (is (= "sha256:14688533f0d95fd32a06c0972bd006acb929938ca1a002d4d2c90c0e205c5c7c"
                 (dispatch/pure-policy-id) (:extension-policy unit)
                 (get-in result [:input :policy-id])))
          (is (= (:source-id unit) (get-in result [:input :seed :source-id])))
          (is (= (:form-id form) (get-in result [:input :seed :form-id])))
          (is (= (first (:children form)) (get-in result [:input :payload-form-id])))
          (is (= (:span form) (get-in result [:input :seed :span])))
          (is (= implementation (get-in result [:input :implementation])))
          (is (= "sha256:a4617e1a36c6a73e20b741db3b01394e6a391fc741a7e99a15eb091338f77041"
                 (:source-content-hash implementation)))
          (is (= 'gravity.reader/read-text (get-in result [:input :handler])
                 (get-in result [:invocation :handler])))
          (is (= payload (last (:parsed-values products))
                 (get-in result [:returned-seed :datum])))
          (is (= result fresh))
          (is (not (identical? result fresh)))
          (is (empty? (:extension-dispatch-observations products)))
          ;; Actual companion at its explicitly unsupported downstream boundary.
          ;; No private authority token, sealed-product constructor or C2 target.
          (is (= [:authenticated-pure-extension-results]
                 (:missing-fields
                   (failure-data #(syntax/reader-artifact-view
                                    {:pure-extension-results results} nil {})))))
          (doseq [[field changed] [[[:returned-seed :datum] "tampered"]
                                   [[:result-hash] "sha256:changed"]
                                   [[:invocation :handler] 'gravity.reader/read-input]]]
            (is (= :pure-result-reconstruction
                   (:reason
                     (failure-data #(dispatch/verify-pure-result!
                                      unit 'text form payload implementation
                                      (assoc-in result field changed)))))))
          (doseq [[field changed] [[[:input :policy-id] "sha256:changed"]
                                   [[:input :source-content-hash] "sha256:changed"]
                                   [[:input :implementation :source-content-hash] "sha256:changed"]
                                   [[:input :seed :datum] "substituted"]]]
            (is (= :pure-result-binding
                   (:reason
                     (failure-data #(dispatch/verify-pure-result!
                                      unit 'text form payload implementation
                                      (assoc-in result field changed)))))))
          ;; Capturable actual identity data, never a seal or admission token.
          (println (pr-str {:case path :reader-source reader
                            :source-id (:source-id unit) :bytes-hash (:bytes-hash unit)
                            :policy-id (get-in result [:input :policy-id])
                            :implementation implementation :input-hash (:input-hash result)
                            :result-hash (:result-hash result) :fresh-comparison :equal}))))))
  (doseq [stem ["normal-unknown" "normal-effect"] extension [".qst" ".gravity"]]
    (testing (str stem extension)
      ;; These real normal calls must reject at the raw Reader boundary before
      ;; adaptation can dispatch the preceding text tag. No provider is bound.
      (is (= "C2-EXTENSION" (:id (failure-data #(normal-products (inputs stem extension)))))))))
