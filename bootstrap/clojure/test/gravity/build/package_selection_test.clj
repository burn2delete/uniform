(ns gravity.build.package-selection-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is]]
            [gravity.build.package-selection :as selection]))

(def package-record
  {:package "gravity/http" :version "2.1.4" :registry :public
   :content-hash "sha256:http-214" :provenance "prov:http"
   :capabilities [:http/client] :effects [:network/http]
   :safety :reviewed :unsafe-islands [] :features [] :dependencies []
   :variants [{:profile :hosted :target :jvm-21 :artifact "http:jvm"}]
   :yank-status :active :revocation-status :clear
   :signature-status :unverified :provenance-status :unverified})

(def constraint-record
  {:package "gravity/http" :version "2.1.4" :chain ["app" "gravity/http"]})

(def request
  {:constraints [constraint-record] :candidates [package-record]
   :locked-records [] :mode :select :unsafe-policy :deny
   :allowed-effects #{:network/http} :allowed-capabilities #{:http/client}
   :context {:registry :public :visibility :public :registry-grants {}
             :profile :hosted :target :jvm-21
             :policy {:id :standard :yanked :reject :revoked :reject}}})

(defn code [result] (get-in result [:diagnostics 0 :diagnostic]))

(deftest selects-real-record-and-variant-with-no-artifact-authority
  (let [result (selection/select-exact request)]
    (is (= :selected (:status result)))
    (is (= package-record (get-in result [:selections 0 :selected])))
    (is (= "http:jvm" (get-in result [:selections 0 :variant :artifact])))
    (is (= :permit (get-in result [:selections 0 :policy-decision :policy-decision])))
    (is (= :non-admitting (:authority result)))
    (is (= :not-evaluated (:admission result)))
    (is (= :supplied (:status-origin result)))
    (is (= #{} (:effects result) (:granted-capabilities result)))
    (is (= result (edn/read-string (pr-str result))))))

(deftest canonical-input-order-and-content-identities
  (let [other (assoc package-record :package "gravity/core" :version "1.0.0"
                     :content-hash "sha256:core-100")
        other-c (assoc constraint-record :package "gravity/core" :version "1.0.0")
        a (assoc request :candidates [other package-record]
                 :constraints [constraint-record other-c])
        b (assoc a :candidates [package-record other package-record]
                 :constraints [other-c constraint-record other-c])
        result (selection/select-exact a)]
    (is (= result (selection/select-exact b)))
    (is (= (pr-str result) (pr-str (selection/select-exact b))))
    (is (= ["gravity/core" "gravity/http"]
           (mapv #(get-in % [:selected :package]) (:selections result))))
    (is (= ["sha256:core-100" "sha256:http-214"]
           (mapv #(get-in % [:selected :content-hash]) (:selections result))))
    (let [changed (selection/select-exact
                   (assoc-in a [:candidates 0 :content-hash] "sha256:changed"))]
      (is (not= (:normalized-inputs result) (:normalized-inputs changed))))))

(deftest minimal-exact-conflict-and-unavailable-version-evidence
  (let [conflict (assoc constraint-record :version "2.1.3" :chain ["other-root"])
        third (assoc constraint-record :version "2.1.5" :chain ["third-root"])
        result (selection/select-exact
                (assoc request :constraints [third constraint-record conflict]))
        reversed (selection/select-exact
                  (assoc request :constraints [conflict constraint-record third]))]
    (is (= result reversed))
    (is (= :rejected (:status result)))
    (is (= "PKG5001" (code result)))
    (is (= [conflict constraint-record] (get-in result [:diagnostics 0 :constraints])))
    (is (= [] (:selections result))))
  (let [result (selection/select-exact (assoc request :candidates []))]
    (is (= "PKG5001" (code result)))
    (is (= [constraint-record] (get-in result [:diagnostics 0 :constraints])))))

(deftest unsupported-or-ambiguous-input-is-not-an-arbitrary-choice
  (doseq [bad [(assoc-in request [:constraints 0 :version] "^2.1.0")
               (assoc-in request [:constraints 0 :version] "2.1")
               (assoc-in request [:constraints 0 :version] "02.1.4")
               (assoc-in request [:constraints 0 :version] "2.1.4-beta")
               (assoc request :mode :release)
               (assoc request :fallback-target :host)
               (assoc request :unsafe-policy :allow)
               (assoc request :allowed-effects nil)
               (assoc-in request [:candidates 0 :features] [:client])
               (assoc-in request [:candidates 0 :dependencies] [constraint-record])
               (update-in request [:context] dissoc :policy)
               (update-in request [:candidates 0] dissoc :signature-status)
               (assoc request :candidates [package-record
                                          (assoc package-record :content-hash "different")])]]
    (is (= "PKG5007" (code (selection/select-exact bad)))))
  (is (= "PACKAGE-POLICY-INPUT"
         (code (selection/select-exact
                (assoc-in request [:candidates 0 :signature-status] :unknown))))))

(deftest authority-target-and-safety-cannot-be-expanded
  (doseq [[bad expected]
          [[(assoc request :allowed-capabilities #{}) "PKG5002"]
           [(assoc request :allowed-effects #{}) "PKG5002"]
           [(assoc-in request [:context :target] :wasm) "PKG5003"]
           [(assoc-in request [:context :profile] :kernel) "PKG5003"]
           [(assoc-in request [:candidates 0 :variants] []) "PKG5003"]
           [(assoc-in request [:candidates 0 :unsafe-islands] ["ffi/raw"]) "PKG1007"]
           [(assoc-in request [:candidates 0 :safety] :quarantined) "PKG1007"]]]
    (let [result (selection/select-exact bad)]
      (is (= expected (code result)))
      (is (= [] (:selections result)))
      (is (= #{} (:granted-capabilities result))))))

(deftest normalization-conserves-full-metadata-and-locked-history
  (let [variant {:profile :hosted :target :wasm :artifact "http:wasm"}
        input (-> request
                  (assoc-in [:candidates 0 :variants]
                            [variant (first (:variants package-record))])
                  (assoc :locked-records [package-record package-record]))
        reordered (-> input
                      (update-in [:candidates 0 :variants] #(vec (reverse %)))
                      (assoc-in [:candidates 0 :capabilities] [:http/client :http/client]))
        result (selection/select-exact input)]
    (is (= result (selection/select-exact reordered)))
    (is (= (pr-str result) (pr-str (selection/select-exact reordered))))
    (is (= [package-record package-record] (:locked-records result)))
    (is (= #{variant (first (:variants package-record))}
           (set (get-in result [:selections 0 :selected :variants]))))
    (is (= :unverified (get-in result [:selections 0 :selected :provenance-status])))
    (is (= "prov:http" (get-in result [:selections 0 :selected :provenance])))))

(deftest authority-refusal-identifies-the-missing-policy-authority
  (let [cap (first (:diagnostics (selection/select-exact (assoc request :allowed-capabilities #{}))))
        effect (first (:diagnostics (selection/select-exact (assoc request :allowed-effects #{}))))]
    (is (= "PKG5002" (:diagnostic cap) (:diagnostic effect)))
    (is (= :http/client (:capability cap)))
    (is (= [:http/client] (:denied-capabilities cap)))
    (is (= [] (:denied-effects cap)))
    (is (= :network/http (:effect effect)))
    (is (= [:network/http] (:denied-effects effect)))
    (is (= [] (:denied-capabilities effect)))
    (is (not= cap effect))))

(deftest registry-denial-precedes-input-reporting
  (doseq [bad [(assoc-in request [:context :visibility] :private)
               (assoc-in request [:context :visibility] :latent)
               (assoc-in request [:candidates 0 :registry] :secret)
               (assoc request :locked-records [(assoc package-record :registry :secret)])]]
    (let [result (selection/select-exact bad)]
      (is (= "PKG5005" (code result)))
      (is (not (contains? result :normalized-inputs)))
      (is (not (.contains (pr-str result) "gravity/http")))))
  (is (= :selected
         (:status (selection/select-exact
                   (-> request (assoc-in [:context :visibility] :private)
                       (assoc-in [:context :registry-grants] {:public :read})))))))

(deftest locked-selection-and-historical-yank-policy-join
  (is (= "PKG5004" (code (selection/select-exact (assoc request :mode :historical-rebuild)))))
  (let [input (assoc request :mode :locked :candidates [] :locked-records [package-record])
        result (selection/select-exact input)]
    (is (= :selected (:status result)))
    (is (= [package-record] (:locked-records result)))
    (is (= "sha256:http-214" (get-in result [:selections 0 :selected :content-hash])))
    (is (= "PKG5004" (code (selection/select-exact (assoc input :locked-records []))))))
  (let [revoked (assoc package-record :yank-status :yanked :revocation-status :revoked)
        input (assoc request :candidates [revoked] :locked-records [package-record])
        denied (selection/select-exact input)
        historical (-> input (assoc :mode :historical-rebuild)
                       (assoc-in [:context :policy] {:id :archive
                                                    :yanked :historical-rebuild
                                                    :revoked :historical-rebuild}))
        allowed (selection/select-exact historical)]
    (is (= ["PKG5006" "PKG5006"] (mapv :diagnostic (:diagnostics denied))))
    (is (= [package-record] (:locked-records denied)))
    (is (= :selected (:status allowed)))
    (is (= [package-record] (:locked-records allowed)))
    (is (= revoked (get-in allowed [:selections 0 :selected])))
    (is (= :rejected (:status (selection/select-exact (assoc historical :locked-records [])))))
    (is (= "PKG5006"
           (code (selection/select-exact
                  (assoc-in historical [:locked-records 0 :content-hash] "wrong")))))))
