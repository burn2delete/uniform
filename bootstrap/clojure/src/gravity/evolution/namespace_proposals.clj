(ns gravity.evolution.namespace-proposals
  "Pure public proposal construction under GOV10 and PKG9, never approval.
  The decoded carrier is defined in specs/evolution-workflow/plan.md."
  (:require [clojure.string :as str]))

(defn- refuse! [diagnostic rule]
  (throw (ex-info "Namespace reservation proposal refused"
                  {:diagnostic diagnostic :rule rule})))

(defn- unicode! [^String value]
  (loop [index 0]
    (when (< index (.length value))
      (let [c (.charAt value index)]
        (cond
          (Character/isHighSurrogate c)
          (if (and (< (inc index) (.length value))
                   (Character/isLowSurrogate (.charAt value (inc index))))
            (recur (+ index 2))
            (refuse! "PKG9002" :invalid-unicode))
          (Character/isLowSurrogate c) (refuse! "PKG9002" :invalid-unicode)
          :else (recur (inc index))))))
  value)

(defn- freeze
  "Inspect exact native classes before dispatch; detach all backing containers.
  Metadata and foreign values are rejected without rendering supplied data."
  [value ^java.util.IdentityHashMap ancestors]
  (let [kind (class value)
        native-map? (contains? #{clojure.lang.PersistentArrayMap
                                clojure.lang.PersistentHashMap} kind)]
    (cond
      (or native-map? (= kind clojure.lang.PersistentVector))
      (do
        (when (or (some? (meta value)) (.containsKey ancestors value))
          (refuse! "PKG9002" :untrusted-carrier))
        (.put ancestors value true)
        (try
          (if native-map?
            (reduce (fn [out [key item]]
                      (when-not (= clojure.lang.Keyword (class key))
                        (refuse! "PKG9002" :untrusted-carrier))
                      (assoc out key (freeze item ancestors)))
                    (sorted-map) value)
            (mapv #(freeze % ancestors) value))
          (finally (.remove ancestors value))))

      (= kind String) (unicode! value)
      (or (nil? value) (= kind Boolean) (= kind clojure.lang.Keyword)) value
      :else (refuse! "PKG9002" :untrusted-carrier))))

(defn- text? [value]
  (and (string? value) (not (str/blank? value))))

(defn- public-fields! [value fields]
  (when-not (and (map? value) (= :public (:visibility value))
                 (every? fields (keys value)))
    (refuse! "PKG9002" :public-fields-required)))

(defn- reference! [value diagnostic rule]
  (when (map? value)
    (public-fields! value #{:id :visibility}))
  (when-not (and (map? value) (text? (:id value)))
    (refuse! diagnostic rule)))

(defn- provenance! [value]
  (when-not (and (vector? value) (seq value))
    (refuse! "GOV10002" :public-provenance-required))
  (doseq [reference value]
    (reference! reference "GOV10002" :public-provenance-required)))

(defn- history! [value]
  (when-not (vector? value)
    (refuse! "GOV10007" :audit-history-required))
  (doseq [record value]
    (public-fields! record #{:id :visibility :action :actor :reason
                            :policy-reference :provenance})
    (when-not (every? #(text? (get record %))
                      [:id :action :actor :reason :policy-reference])
      (refuse! "GOV10007" :audit-record-required))
    (provenance! (:provenance record))))

(defn propose-reservation
  "Return a deterministic proposed artifact, or a value-free rejection.
  Public declarations and references are supplied facts, not authenticated
  ownership, policy disposition, a security review or publication authority."
  [input]
  (try
    (let [request (freeze input (java.util.IdentityHashMap.))]
      (public-fields! request #{:visibility :registry :namespace :requester
                                :reason :policy-reference :provenance
                                :audit-history :status})
      (when (contains? request :status)
        (refuse! "GOV10007" :caller-status-forbidden))
      (doseq [field [:registry :namespace :requester]]
        (reference! (get request field) "GOV10001" :identity-required))
      (when-not (and (text? (:reason request))
                     (text? (:policy-reference request)))
        (refuse! "GOV10007" :reason-and-policy-required))
      (provenance! (:provenance request))
      (history! (:audit-history request))
      (sorted-map :artifact :gravity/namespace-reservation-proposal
                  :schema :gravity/namespace-reservation-proposal-v1
                  :status :proposed :authority :non-authorizing
                  :request request))
    (catch clojure.lang.ExceptionInfo error
      {:status :rejected :diagnostics [(ex-data error)]})))
