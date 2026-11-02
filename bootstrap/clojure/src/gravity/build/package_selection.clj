(ns gravity.build.package-selection
  (:require [clojure.set :as set]
            [clojure.walk :as walk]
            [gravity.build.package-policy :as policy]))

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
  [{:keys [constraints candidates locked-records mode context allowed-effects
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
                                (policy/decide
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
                  :diagnostics diagnostics)))))))
