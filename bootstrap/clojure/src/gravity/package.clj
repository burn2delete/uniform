(ns gravity.package
  "Supplied package policy, direct exact selection, and compiled metadata checks."
  (:require [clojure.set :as set] [clojure.walk :as walk]))

(def ^:private namespace-contract
  {:namespace 'gravity.package
   :contract-boundary :supplied-package-policy-selection-and-compiled-metadata
   :governing-contracts '[D1 D2 D3 D8 D9 C1 C15 PKG1 PKG3 PKG4 PKG5 PKG6 PKG8 PKG9 PKG10 PKG11 PKG12 BOOT8 TEST2]
   :public-api {'decide-policy {:arglists '([request]) :admission :none}
                'select-exact {:arglists '([request] [decide-policy-operation request]) :admission :none}
                'check-project-manifest! {:arglists '([fail-var module subject]) :admission :none}
                'check-artifact-manifest! {:arglists '([fail-var module subject]) :admission :none}
                'check-package-operation! {:arglists '([fail-var module subject]) :admission :none}
                'check-resolution-report! {:arglists '([fail-var module subject]) :admission :none}
                'check-capability-manifest! {:arglists '([fail-var module subject]) :admission :none}
                'check-package-safety! {:arglists '([presence-var fail-var module subject]) :admission :none}
                'check-registry-record! {:arglists '([fail-var module subject]) :admission :none}
                'check-provenance-record! {:arglists '([fail-var module subject]) :admission :none}
                'check-target-matrix! {:arglists '([fail-var module subject]) :admission :none}
                'check-signing-sbom! {:arglists '([fail-var module subject]) :admission :none}}
   :artifact-inputs [:supplied-package-records :complete-supplied-lock-history :direct-exact-constraints
                     :registry-scope-and-grants :profile-target :status-policy :allowed-effects
                     :allowed-capabilities :unsafe-policy :policy-operation
                     :native-module :supplied-compiled-package-metadata
                     :caller-owned-failure-var :caller-owned-presence-var]
   :artifact-outputs [:supplied-policy-decision :exact-selection-report :selected-variant-data
                      :constraint-conflict :stable-pkg-diagnostics :nil-or-original-failure-result]
   :diagnostic-ownership {:owns ["PKG5001" "PKG5002" "PKG5003" "PKG5004" "PKG5005" "PKG5006"
                                  "PKG5007" "PKG1007" "PACKAGE-POLICY-INPUT"]
                          :retained-from :native-package-policy-and-selection
                          :private-registry-denial :before-metadata-reporting
                          :compiled-metadata-decisions ["PKG1006" "PKG3005" "PKG4001" "PKG5002" "PKG6004"
                                                        "PKG8001" "PKG9001" "PKG10001" "PKG11002" "PKG12002"]
                          :compiled-metadata-payload-and-transport 'gravity.bootstrap}
   :dependency-direction {:requires ['clojure.set 'clojure.walk]
                          :consumed-by ['gravity.build.package-policy 'gravity.build.package-selection 'gravity.bootstrap]
                          :forbids ['gravity.build.invocation 'gravity.bootstrap 'gravity.cli 'gravity.release]}
   :supported {'decide-policy {:profile-target :supplied-keywords :scope :status-policy-only}
               'select-exact {:constraints :direct-exact-numeric-three-component :registries :single
                              :profile-target [:hosted :jvm-21] :unsafe-policy :deny}}
   :unsupported [:ranges :features :transitive-resolution :fetch :install :lock-writes :latent-spaces
                 :signature-verification :provenance-authentication :artifact-admission :sbom
                 :publication :full-pkg-conformance :migration :release :self-hosting :seed-retirement]
   :compiled-metadata {:authority :non-admitting :inputs :unverified-native-data
                       :failure-var :dereferenced-at-original-failing-branch
                       :presence-var :dereferenced-before-subject-argument-observation
                       :aggregate-order-owner 'gravity.bootstrap
                       :source-and-final-identity-owner 'gravity.bootstrap
                       :proves-downloads? false :grants-capabilities? false}
   :status-origin :supplied :authority :non-admitting :granted-capabilities #{}
   :authenticates-inputs? false :performs-io? false
   :test-owner 'gravity.package.pipeline-test
   :compiled-metadata-test-owners '[gravity.package.native-metadata-test gravity.package.compiled-metadata-test]
   :bootstrap-hosted? true :clojure-seed-boundary? true :self-hosted? false})



;; PKG5 policy only. Uses PKG4/PKG5 lock records (:package, :version,
;; :registry, :content-hash, :provenance). No resolver or verifier is present.
(defn- nonempty-string? [value]
  (and (string? value) (not (empty? value))))

(defn- policy-package-record? [record]
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

(defn decide-policy
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
                      (not (and (policy-package-record? record)
                                (= registry (:registry record))))
                      :package-record-required
                      (not (and (vector? locked-records)
                                (every? policy-package-record? locked-records)))
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

;; PKG5 direct exact-version selection. Supplied metadata is never authenticated.
(defn- text? [x] (and (string? x) (not (empty? x))))
(defn- exact-version? [x]
  (and (string? x) (boolean (re-matches #"(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)" x))))
(defn- closed? [x ks] (and (map? x) (= ks (set (keys x)))))
(defn- keyword-vector? [x] (and (vector? x) (every? keyword? x)))
(defn- keyword-set? [x] (and (set? x) (every? keyword? x)))
(defn- canonical [x]
  (walk/postwalk
   #(cond (map? %) (into (sorted-map) %)
          (set? %) (into (sorted-set) %)
          :else %) x))

(defn- constraint? [x]
  (and (closed? x #{:package :version :chain})
       (text? (:package x)) (exact-version? (:version x))
       (vector? (:chain x)) (seq (:chain x)) (every? text? (:chain x))))

(defn- variant? [x]
  (and (closed? x #{:profile :target :artifact})
       (keyword? (:profile x)) (keyword? (:target x)) (text? (:artifact x))))

(def ^:private record-keys
  #{:package :version :registry :content-hash :provenance :capabilities :effects
    :safety :unsafe-islands :variants :features :dependencies :yank-status
    :revocation-status :signature-status :provenance-status})

(defn- package-record? [x]
  (and (closed? x record-keys)
       (every? #(text? (get x %)) [:package :content-hash :provenance])
       (exact-version? (:version x)) (keyword? (:registry x))
       (keyword-vector? (:capabilities x)) (keyword-vector? (:effects x))
       (#{:unreviewed :reviewed :requires-recheck :quarantined :revoked
          :accepted-exception} (:safety x))
       (vector? (:unsafe-islands x)) (every? text? (:unsafe-islands x))
       (vector? (:variants x)) (every? variant? (:variants x))
       (= (count (:variants x))
          (count (set (map (juxt :profile :target) (:variants x)))))
       ;; These fields are explicit: this increment cannot solve them.
       (= [] (:features x)) (= [] (:dependencies x))
       (#{:active :yanked :unknown} (:yank-status x))
       (#{:clear :revoked :unknown} (:revocation-status x))
       (every? #(#{:verified :unverified :missing :invalid :revoked :unknown}
                  (get x %)) [:signature-status :provenance-status])))

(defn- normalize-record [x]
  (canonical (-> x
                 (update :effects #(vec (sort (distinct %))))
                 (update :capabilities #(vec (sort (distinct %))))
                 (update :variants #(vec (sort-by (juxt :profile :target) %))))))

(defn- identity-key [x] [(:package x) (:version x) (:registry x)])

(defn select-exact
  "Pure direct dependency selection; exact three-component numeric versions only.
  Hosted/JVM-21 only; no ranges, features, transitive graph, multiple registries,
  release or auth.
  :locked selects supplied locked records; :historical-rebuild selects current
  candidates matched to locked history by package-policy. No lock is rewritten."
  ([request] (select-exact #'decide-policy request))
  ([decide-policy-operation
    {:keys [constraints candidates locked-records mode context allowed-effects
           allowed-capabilities unsafe-policy] :as request}]
  (let [{:keys [registry visibility registry-grants profile target]} context
        base {:artifact :gravity/exact-package-selection
              :authority :non-admitting :admission :not-evaluated
              :status-origin :supplied :effects #{} :granted-capabilities #{}
              :selections []}
        reject (fn [code rule]
                 (assoc base :status :rejected
                        :diagnostics [{:diagnostic code :registry registry
                                       :policy-rule rule}]))
        scoped? (fn [x] (or (not (map? x)) (= registry (:registry x))))]
    ;; Check the single registry scope before echoing any package metadata.
    (cond
      (not (and (keyword? registry)
                (or (= :public visibility)
                    (and (= :private visibility)
                         (= :read (get registry-grants registry))))))
      (reject "PKG5005" :registry-read-grant-required)

      (or (and (vector? candidates) (not-every? scoped? candidates))
          (and (vector? locked-records) (not-every? scoped? locked-records)))
      (reject "PKG5005" :registry-record-scope-mismatch)

      (not (and
            (closed? request #{:constraints :candidates :locked-records :mode
                               :context :allowed-effects :allowed-capabilities
                               :unsafe-policy})
            (closed? context #{:registry :visibility :registry-grants
                               :profile :target :policy})
            (map? registry-grants)
            (every? keyword? (keys registry-grants))
            (every? #{:read :write} (vals registry-grants))
            (keyword? profile) (keyword? target)
            (closed? (:policy context) #{:id :yanked :revoked})
            (keyword? (get-in context [:policy :id]))
            (every? #{:reject :historical-rebuild}
                    (map (:policy context) [:yanked :revoked]))
            (#{:select :locked :historical-rebuild} mode)
            (= :deny unsafe-policy)
            (keyword-set? allowed-effects) (keyword-set? allowed-capabilities)
            (vector? constraints) (seq constraints) (every? constraint? constraints)
            (vector? candidates) (every? package-record? candidates)
            (vector? locked-records) (every? package-record? locked-records)))
      (reject "PKG5007" :unsupported-or-malformed-exact-input)

      (not= [:hosted :jvm-21] [profile target])
      (reject "PKG5003" :unsupported-profile-target-pair)

      :else
      (let [cs (vec (sort-by (juxt :package :version :chain) (distinct constraints)))
            records (vec (sort-by identity-key (distinct (map normalize-record candidates))))
            locks (vec (sort-by identity-key (distinct (map normalize-record locked-records))))
            source (if (= :locked mode) locks records)
            ;; Repeated identical candidate/lock identities are harmless. Distinct
            ;; status facts in historical mode are reconciled by package-policy.
            ambiguous-source? (some #(> (count %) 1) (vals (group-by identity-key source)))
            normalized (canonical (assoc request :constraints cs :candidates records))
            diagnose (fn [c code rule]
                       {:diagnostic code :package (:package c)
                        :requested-range (:version c) :constraint-chain (:chain c)
                        :profile profile :target target :registry registry
                        :policy-rule rule})
            results
            (mapv
             (fn [[package requests]]
               (let [c (first requests)
                     versions (distinct (map :version requests))
                     selected (first (filter #(and (= package (:package %))
                                                    (= (:version c) (:version %))) source))
                     variant (first (filter #(and (= profile (:profile %))
                                                   (= target (:target %))) (:variants selected)))
                     denied-capabilities (vec (sort (set/difference (set (:capabilities selected))
                                                                    allowed-capabilities)))
                     denied-effects (vec (sort (set/difference (set (:effects selected)) allowed-effects)))
                     diagnostic (fn [code rule]
                                  (assoc (diagnose c code rule)
                                         :selected-version (:version selected)))
                     decision (when selected
                                (decide-policy-operation
                                 (merge context {:record selected
                                                 :locked-records (mapv normalize-record locked-records)
                                                 :mode (if (= :locked mode) :select mode)
                                                 :requested-range (:version c)
                                                 :constraint-chain (:chain c)})))]
                 (cond
                   (> (count versions) 1)
                   {:diagnostics [(assoc (diagnostic "PKG5001" :conflicting-exact-versions)
                                         :selected-version nil
                                         :constraints [(first requests)
                                                       (first (filter #(not= (:version c) (:version %)) requests))])]}
                   (nil? selected)
                   {:diagnostics [(assoc (diagnostic (if (= :locked mode) "PKG5004" "PKG5001")
                                                     :exact-version-unavailable)
                                         :constraints [c])]}
                   (nil? variant)
                   {:diagnostics [(diagnostic "PKG5003" :profile-target-variant-missing)]}
                   (or (seq denied-capabilities) (seq denied-effects))
                   {:diagnostics [(assoc (diagnostic "PKG5002" :dependency-authority-expansion)
                                         :capability (first denied-capabilities)
                                         :effect (first denied-effects)
                                         :denied-capabilities denied-capabilities
                                         :denied-effects denied-effects)]}
                   (or (seq (:unsafe-islands selected)) (not= :reviewed (:safety selected)))
                   {:diagnostics [(diagnostic "PKG1007" :unsafe-or-unreviewed-dependency)]}
                   (= :reject (:policy-decision decision))
                   {:diagnostics (:diagnostics decision)}
                   (and (= :historical-rebuild mode)
                        (not-any? #(= (dissoc selected :yank-status :revocation-status
                                              :signature-status :provenance-status)
                                      (dissoc % :yank-status :revocation-status
                                              :signature-status :provenance-status)) locks))
                   {:diagnostics [(diagnostic "PKG5004" :historical-package-not-locked)]}
                   :else {:selected selected :variant variant :policy-decision decision
                          :diagnostics []})))
             (sort-by key (group-by :package cs)))
            diagnostics (vec (mapcat :diagnostics results))]
        (if ambiguous-source?
          (reject "PKG5007" :ambiguous-package-identity)
          (canonical
           (assoc base :status (if (seq diagnostics) :rejected :selected)
                  :normalized-inputs normalized
                  :locked-records locked-records
                  :selections (if (seq diagnostics) []
                                  (mapv #(select-keys % [:selected :variant :policy-decision]) results))
                  :diagnostics diagnostics))))))))

(defn check-project-manifest!
  "Check only the supplied compiled lockfile flag; do not authenticate a lockfile."
  [fail-var module subject]
  (when-not (true? (:lockfile-complete subject))
    ((deref fail-var)
     "PKG1006" module subject
     {:missing-fields [:lockfile-complete]})))

(defn check-artifact-manifest!
  "Check only supplied safety-evidence presence; do not verify safety evidence."
  [fail-var module subject]
  (when-not (get-in subject [:evidence :safety])
    ((deref fail-var)
     "PKG3005" module subject
     {:missing-fields [:safety-evidence]})))

(defn check-package-operation!
  "Check only the supplied download flag; do not fetch or verify a download."
  [fail-var module subject]
  (when-not (true? (:download-verified subject))
    ((deref fail-var)
     "PKG4001" module subject
     {:missing-fields [:download-verified]})))

(defn check-resolution-report!
  "Check only supplied capability compatibility; do not resolve dependencies."
  [fail-var module subject]
  (when-not (true? (:capability-compatible subject))
    ((deref fail-var)
     "PKG5002" module subject
     {:missing-fields [:capability-compatible]})))

(defn check-capability-manifest!
  "Reject supplied deny/request overlap; do not grant deployment authority."
  [fail-var module subject]
  (when (seq (set/intersection
              (get-in subject [:capabilities :denies])
              (get-in subject [:capabilities :requests])))
    ((deref fail-var)
     "PKG6004" module subject
     {:missing-fields [:denied-authority]})))

(defn check-package-safety!
  "Check only supplied audit metadata through the caller's existing presence operation."
  [presence-var fail-var module subject]
  (when-not ((deref presence-var) (:unsafe-audit-metadata subject))
    ((deref fail-var)
     "PKG8001" module subject
     {:missing-fields [:unsafe-audit-metadata]})))

(defn check-registry-record!
  "Check only a supplied registry flag; do not authorize or read a registry."
  [fail-var module subject]
  (when-not (true? (:access-grant subject))
    ((deref fail-var)
     "PKG9001" module subject
     {:missing-fields [:access-grant]})))

(defn check-provenance-record!
  "Check only supplied source-graph hash truthiness; do not authenticate provenance."
  [fail-var module subject]
  (when-not (:source-graph-hash subject)
    ((deref fail-var)
     "PKG10001" module subject
     {:missing-fields [:source-graph-hash]})))

(defn check-target-matrix!
  "Reject only the supplied implicit-host flag; do not validate target support."
  [fail-var module subject]
  (when (true? (:implicit-host-target? subject))
    ((deref fail-var)
     "PKG11002" module subject
     {:missing-fields [:explicit-target]})))

(defn check-signing-sbom!
  "Check only a supplied canonical-payload flag; do not sign or verify an SBOM."
  [fail-var module subject]
  (when-not (true? (get-in subject [:signature :canonical-payload]))
    ((deref fail-var)
     "PKG12002" module subject
     {:missing-fields [:canonical-payload]})))

(doseq [operation [#'decide-policy #'select-exact
                   #'check-project-manifest! #'check-artifact-manifest!
                   #'check-package-operation! #'check-resolution-report!
                   #'check-capability-manifest! #'check-package-safety! #'check-registry-record!
                   #'check-provenance-record! #'check-target-matrix! #'check-signing-sbom!]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
