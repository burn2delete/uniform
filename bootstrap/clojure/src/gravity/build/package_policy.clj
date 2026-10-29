(ns gravity.build.package-policy)

;; PKG5 policy only. Uses PKG4/PKG5 lock records (:package, :version,
;; :registry, :content-hash, :provenance). No resolver or verifier is present.
(defn- nonempty-string? [value]
  (and (string? value) (not (empty? value))))

(defn- package-record? [record]
  (and (map? record)
       (every? #(nonempty-string? (get record %))
               [:package :version :content-hash :provenance])
       (keyword? (:registry record))))

(defn- policy? [policy]
  (and (map? policy)
       (= #{:id :yanked :revoked} (set (keys policy)))
       (keyword? (:id policy))
       (#{:reject :historical-rebuild} (:yanked policy))
       (#{:reject :historical-rebuild} (:revoked policy))))

(defn- statuses? [record]
  (and (#{:active :yanked :unknown} (:yank-status record))
       (#{:clear :revoked :unknown} (:revocation-status record))
       (#{:verified :unverified :missing :invalid :revoked :unknown}
        (:signature-status record))
       (#{:verified :unverified :missing :invalid :revoked :unknown}
        (:provenance-status record))))

(defn decide
  "Evaluate only supplied yank/revocation policy facts; never admit an artifact.
  :record uses the existing P12 lock-record shape plus explicit status fields.
  :locked-records is retained verbatim. Historical exceptions require the same
  locked record (apart from current status facts) and :mode :historical-rebuild.
  No status is authenticated here.
  Registry visibility/grants are caller policy inputs, not package metadata.
  The caller must authorize registry access before obtaining private metadata."
  [{:keys [record locked-records mode policy registry visibility registry-grants
           requested-range constraint-chain profile target]}]
  (let [record-scope? (fn [value]
                        (or (not (map? value))
                            (= registry (:registry value))))
        access? (and (keyword? registry)
                     (record-scope? record)
                     (or (not (vector? locked-records))
                         (every? record-scope? locked-records))
                     (or (= :public visibility)
                         (and (= :private visibility)
                              (= :read (get registry-grants registry)))))
        base {:artifact :gravity/package-policy-decision
              :authority :non-admitting
              :admission :not-evaluated
              :status-origin :supplied
              :registry registry
              :effects #{}
              :granted-capabilities #{}}]
    ;; Do not return supplied private records/history on an access denial.
    (if-not access?
      (assoc base :policy-decision :reject
             :diagnostics [{:diagnostic "PKG5005"
                            :registry registry
                            :policy-rule :registry-read-grant-required}])
      (let [context {:package (:package record)
                     :requested-range requested-range
                     :selected-version (:version record)
                     :constraint-chain constraint-chain
                     :profile profile :target target
                     :capability nil :registry registry
                     :policy-id (:id policy)}
            report (assoc base :record record :locked-records locked-records
                          :context context :policy policy :mode mode)
            invalid (cond
                      (not (policy? policy)) :explicit-policy-required
                      (not (#{:select :historical-rebuild} mode)) :invalid-mode
                      (not (and (package-record? record)
                                (= registry (:registry record))))
                      :package-record-required
                      (not (and (vector? locked-records)
                                (every? package-record? locked-records)))
                      :locked-records-required
                      (not (and (nonempty-string? requested-range)
                                (vector? constraint-chain)
                                (seq constraint-chain)
                                (every? nonempty-string? constraint-chain)
                                (keyword? profile) (keyword? target)))
                      :constraint-context-required
                      (not (statuses? record)) :invalid-status
                      (some #{:unknown}
                            (map #(get record %) [:yank-status :revocation-status
                                                  :signature-status :provenance-status]))
                      :unresolved-status)
            revoked? (some #{:revoked}
                           (map #(get record %) [:revocation-status :signature-status
                                                 :provenance-status]))
            restricted (cond-> []
                         (= :yanked (:yank-status record)) (conj :yanked)
                         revoked? (conj :revoked))
            status-fields [:yank-status :revocation-status
                           :signature-status :provenance-status]
            historical? (and (nil? invalid)
                             (= :historical-rebuild mode)
                             (some #(= (apply dissoc record status-fields)
                                       (apply dissoc % status-fields))
                                   locked-records))
            refused (filterv #(not (and historical?
                                        (= :historical-rebuild (get policy %))))
                             restricted)]
        (cond
          invalid
          (assoc base :policy-decision :reject
                 :diagnostics [{:registry registry
                                :diagnostic "PACKAGE-POLICY-INPUT"
                                :policy-rule invalid}])

          (seq refused)
          (assoc report :policy-decision :reject
                 :diagnostics (mapv #(assoc context :diagnostic "PKG5006"
                                           :policy-rule %)
                                    refused))

          :else
          (assoc report :policy-decision :permit
                 :policy-rule (if (seq restricted)
                                :explicit-locked-historical-rebuild
                                :no-yank-or-revocation)
                 :diagnostics []))))))
