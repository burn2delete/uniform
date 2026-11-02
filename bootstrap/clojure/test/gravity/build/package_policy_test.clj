(ns gravity.build.package-policy-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is testing]]
            [gravity.build.package-policy :as policy]))

;; Synthetic facts are deliberately non-admitting, including :verified labels.
(def record
  {:package "gravity/http" :version "2.1.4" :registry :gravity-public
   :content-hash "sha256:http-214" :provenance "prov:gravity-http"
   :capabilities [:http/client] :safety :reviewed
   :yank-status :active :revocation-status :clear
   :signature-status :verified :provenance-status :verified})

(def request
  {:record record :locked-records [record] :mode :select
   :registry :gravity-public :visibility :public :registry-grants {}
   :requested-range "^2.1.0" :constraint-chain ["app -> gravity/http ^2.1.0"]
   :profile :hosted :target :jvm-21
   :policy {:id :release :yanked :reject :revoked :reject}})

(defn- historical [record]
  (assoc request :record record :locked-records [record]
         :mode :historical-rebuild
         :policy {:id :archive :yanked :historical-rebuild
                  :revoked :historical-rebuild}))

(deftest policy-permission-never-authenticates-or-admits
  (doseq [status [:verified :unverified :missing :invalid]]
    (let [result (policy/decide
                  (assoc-in request [:record :signature-status] status))]
      (is (= :permit (:policy-decision result)))
      (is (= :supplied (:status-origin result)))
      (is (= status (get-in result [:record :signature-status])))
      (is (= :not-evaluated (:admission result)))
      (is (= :non-admitting (:authority result)))
      (is (= #{} (:effects result) (:granted-capabilities result))))))

(deftest default-yank-and-revocation-refusal-is-attributed
  (doseq [[field value rule] [[:yank-status :yanked :yanked]
                            [:revocation-status :revoked :revoked]
                            [:signature-status :revoked :revoked]
                            [:provenance-status :revoked :revoked]]]
    (let [input (assoc-in request [:record field] value)
          result (policy/decide input)
          diagnostic (first (:diagnostics result))]
      (is (= :reject (:policy-decision result)))
      (is (= "PKG5006" (:diagnostic diagnostic)))
      (is (= rule (:policy-rule diagnostic)))
      (is (= {:package "gravity/http" :requested-range "^2.1.0"
              :selected-version "2.1.4"
              :constraint-chain ["app -> gravity/http ^2.1.0"]
              :profile :hosted :target :jvm-21 :capability nil
              :registry :gravity-public :policy-id :release}
             (dissoc diagnostic :diagnostic :policy-rule)))
      (is (= (:locked-records input) (:locked-records result)))
      (is (= (:record input) (:record result))))))

(deftest historical-exception-requires-policy-mode-and-exact-locked-record
  (let [revoked (assoc record :yank-status :yanked :revocation-status :revoked)
        input (historical revoked)
        result (policy/decide input)]
    (is (= :permit (:policy-decision result)))
    (is (= :explicit-locked-historical-rebuild (:policy-rule result)))
    (is (= revoked (:record result)))
    (is (= [revoked] (:locked-records result)))
    ;; A later yank/revocation cannot rewrite the earlier locked status facts.
    (let [later (policy/decide (assoc input :locked-records [record]))]
      (is (= :permit (:policy-decision later)))
      (is (= revoked (:record later)))
      (is (= [record] (:locked-records later))))
    (doseq [changed [(assoc input :mode :select)
                     (assoc input :locked-records [])
                     (assoc-in input [:locked-records 0 :content-hash] "other")
                     (assoc-in input [:locked-records 0 :version] "2.1.3")
                     (assoc-in input [:policy :revoked] :reject)
                     (assoc-in input [:policy :yanked] :reject)]]
      (is (= :reject (:policy-decision (policy/decide changed)))))))

(deftest malformed-policy-status-and-context-fail-closed
  (doseq [input [(dissoc request :policy)
                 (assoc request :policy true)
                 (assoc-in request [:policy :revoked] true)
                 (assoc-in request [:policy :allow-revoked] true)
                 (dissoc request :mode)
                 (dissoc request :constraint-chain)
                 (assoc request :locked-records nil)
                 (assoc request :mode :historical-rebuild :record true)
                 (assoc request :mode :historical-rebuild :locked-records [true])
                 (update request :record dissoc :signature-status)
                 (update request :record dissoc :provenance-status)
                 (assoc-in request [:record :revocation-status] false)
                 (assoc-in request [:record :yank-status] "active")
                 (assoc-in request [:record :signature-status] :unknown)
                 (assoc-in request [:record :provenance-status] :unknown)
                 (assoc-in request [:record :yank-status] :unknown)
                 (assoc-in request [:record :revocation-status] :unknown)]]
    (testing (pr-str input)
      (let [result (policy/decide input)]
        (is (= :reject (:policy-decision result)))
        (is (= "PACKAGE-POLICY-INPUT"
               (get-in result [:diagnostics 0 :diagnostic])))))))

(deftest registry-authority-precedes-metadata-and-has-no-effects
  (let [calls (atom 0)
        input (assoc request :visibility :private
                     :record (fn [] (swap! calls inc)))]
    (doseq [grants [nil {} {:other :read} {:gravity-public true}
                    {:gravity-public :write}]]
      (let [result (policy/decide (assoc input :registry-grants grants))]
        (is (= :reject (:policy-decision result)))
        (is (= "PKG5005" (get-in result [:diagnostics 0 :diagnostic])))
        (is (not (contains? result :record)))
        (is (not (contains? result :locked-records)))
        (is (= #{} (:effects result)))))
    (is (zero? @calls)))
  (is (= :permit
         (:policy-decision
          (policy/decide (assoc request :visibility :private
                               :registry-grants {:gravity-public :read})))))
  (doseq [visibility [nil true :latent :unknown]]
    (is (= :reject
           (:policy-decision (policy/decide (assoc request :visibility visibility)))))))

(deftest deterministic-report-and-complete-supplied-history
  (let [old (assoc record :version "2.1.3" :custom-history {:yank-log ["old"]})
        revoked (assoc record :yank-status :yanked :revocation-status :revoked)
        input (assoc request :record revoked :locked-records [old record old])
        result (policy/decide input)]
    (is (= result (policy/decide input)))
    (is (= result (edn/read-string (pr-str result))))
    (is (= [:yanked :revoked] (mapv :policy-rule (:diagnostics result))))
    (is (identical? (:locked-records input) (:locked-records result)))
    (is (= [old record old] (:locked-records result)))
    (is (= record (:record request)))))

(deftest mismatched-registry-and-malformed-input-reports-do-not-echo-metadata
  (let [private-record (assoc record :registry :private-registry
                              :package "private/secret"
                              :private-metadata "private-internal-value")]
    (doseq [input [(assoc request :record private-record)
                   (assoc request :locked-records [record private-record])
                   (assoc request :locked-records {:private private-record})]]
      (let [result (policy/decide input)
            encoded (pr-str result)]
        (is (= :reject (:policy-decision result)))
        (is (not (contains? result :record)))
        (is (not (contains? result :locked-records)))
        (is (not (.contains encoded "private/secret")))
        (is (not (.contains encoded "private-internal-value")))))
    (is (= "PKG5005"
           (get-in (policy/decide (assoc request :record private-record))
                   [:diagnostics 0 :diagnostic])))))
