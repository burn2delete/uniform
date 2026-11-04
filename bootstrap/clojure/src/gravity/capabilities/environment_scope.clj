(ns gravity.capabilities.environment-scope
  "Pure SAFE10 environment-key assessment over explicit decoded public policy.
  This bootstrap carrier issues no grant, selection or usable capability handle."
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [gravity.providers.declarations :as declarations]))

(def policy-layers
  "SAFE10 Grant Intersection order; each layer is mandatory."
  [:source :function :package :dependency :workspace :profile :invocation
   :runtime-provider])

(defn- text? [x] (and (string? x) (not (str/blank? x))))
(defn- reference? [x] (or (qualified-keyword? x) (qualified-symbol? x)))
(defn- public-reference [x] (if (symbol? x) (with-meta x nil) x))
(defn- key-name? [x]
  ;; Names are public, values are never inputs. No platform case folding.
  (and (string? x) (seq x) (not (re-find #"[\u0000=]" x))
       (.canEncode (.newEncoder java.nio.charset.StandardCharsets/UTF_8) x)))
(defn- key-set? [x] (and (set? x) (every? key-name? x)))
(defn- span? [x]
  (and (map? x) (every? #(and (integer? %) (pos? %))
                        [(:line x) (:column x)])))

(def ^:private binding-fields
  [[:principal reference?] [:capability keyword?]
   [:provider-id qualified-symbol?] [:provider-version text?]
   [:phase #{:build :runtime}] [:profile keyword?] [:target keyword?]])
(def ^:private request-fields
  (into binding-fields [[:source-id text?] [:source-span span?]
                       [:requested-keys key-set?]]))
(def ^:private policy-fields
  (into binding-fields [[:policy-source reference?] [:allowed-keys key-set?]
                       [:denied-keys key-set?] [:redaction-policy reference?]]))

(defn- public-request [request]
  (if-not (map? request)
    {}
    (into {}
          (keep (fn [[field valid?]]
                  (let [value (get request field)]
                    (when (valid? value)
                      [field (case field
                               :source-span {:line (:line value) :column (:column value)}
                               :requested-keys (vec (sort value))
                               (public-reference value))]))))
          request-fields)))

(defn- diagnostic [context id path reason & [layer]]
  {:id id :message "Environment scope assessment refused"
   :path path :reason reason :policy-layer layer :grant-id nil
   :context context})

(defn- shape-errors [data fields path context]
  (if-not (map? data)
    [(diagnostic context "SAFE10-SCOPE" path :invalid-map)]
    (into
     (if (seq (set/difference (set (keys data)) (set (map first fields))))
       [(diagnostic context "SAFE10-SCOPE" path :unexpected-fields)] [])
     (keep (fn [[field valid?]]
             (when-not (and (contains? data field) (valid? (get data field)))
               (diagnostic context (if (= :phase field) "SAFE10-PHASE" "SAFE10-SCOPE")
                           (conj path field)
                           (if (contains? data field) :invalid-field :missing-field)))))
     fields)))

(defn- policy-errors [policies request context]
  (if-not (map? policies)
    [(diagnostic context "SAFE10-DENIED" [:policies] :invalid-policy-map)]
    (into
     (if (seq (set/difference (set (keys policies)) (set policy-layers)))
       [(diagnostic context "SAFE10-DENIED" [:policies] :unsupported-policy-layer)] [])
     (mapcat
      (fn [layer]
        (let [policy (get policies layer) path [:policies layer]]
          (if-not (contains? policies layer)
            [(diagnostic context "SAFE10-DENIED" path :missing-policy-layer layer)]
            (let [errors (shape-errors policy policy-fields path context)]
              (if (seq errors)
                (mapv #(assoc % :policy-layer layer) errors)
                (keep (fn [[field _]]
                        (when (not= (get request field) (get policy field))
                          (assoc (diagnostic context
                                             (case field
                                               :phase "SAFE10-PHASE"
                                               (:provider-id :provider-version) "SAFE10-PROVIDER"
                                               "SAFE10-DENIED")
                                             (conj path field) :identity-mismatch layer)
                                 :policy-source (public-reference (:policy-source policy)))))
                      binding-fields))))))
      policy-layers))))

(defn- provider-errors [request declaration declaration-context context]
  ;; Always use the real producer, never a supplied record or validity flag.
  (let [produced (declarations/record-declaration declaration declaration-context)]
    (cond
      (not= :valid (:status produced))
      [(diagnostic context "SAFE10-PROVIDER" [:declaration] :invalid-declaration)]

      (or (not= (:provider-id request) (:provider-id declaration))
          (not= (:provider-version request) (:version declaration)))
      [(diagnostic context "SAFE10-PROVIDER" [:declaration] :provider-identity-mismatch)]

      (or (not= :env/read (:capability request))
          (not (contains? (:implements declaration) :env/read)))
      [(diagnostic context "SAFE10-PROVIDER" [:declaration :implements]
                   :unsupported-capability)]

      (or (not= (:profile request) (:profile declaration-context))
          (not= (:target request) (:target declaration-context)))
      [(diagnostic context "SAFE10-PROVIDER" [:declaration-context]
                   :applicability-mismatch)]

      (not (contains? (:profiles declaration) (:profile request)))
      [(diagnostic context "SAFE10-PROVIDER" [:declaration :profiles] :unsupported-profile)]

      (not (contains? (:targets declaration) (:target request)))
      [(diagnostic context "SAFE10-PROVIDER" [:declaration :targets] :unsupported-target)]

      :else [])))

(defn- intersect-policies [request policies context]
  (let [universe (apply set/union (:requested-keys request)
                        (mapcat #(let [policy (get policies %)]
                                   [(:allowed-keys policy) (:denied-keys policy)])
                                policy-layers))
        trace (reduce
               (fn [rows layer]
                 (let [{:keys [allowed-keys denied-keys policy-source redaction-policy]}
                       (get policies layer)
                       before (if (seq rows) (set (:effective-keys (peek rows))) universe)
                       after (set/difference (set/intersection before allowed-keys)
                                             denied-keys)]
                   (conj rows {:layer layer :policy-source (public-reference policy-source)
                               :redaction-policy (public-reference redaction-policy)
                               :narrowed-keys (vec (sort (set/difference universe allowed-keys)))
                               :denied-keys (vec (sort denied-keys))
                               :removed-keys (vec (sort (set/difference before after)))
                               :effective-keys (vec (sort after))})))
               [] policy-layers)
        effective (:effective-keys (peek trace))
        scope {:requested-keys (:requested-keys context) :effective-keys effective}
        diagnostics
        (vec (mapcat
              (fn [layer]
                (let [{:keys [allowed-keys denied-keys policy-source]} (get policies layer)
                      denied (set/intersection (:requested-keys request) denied-keys)
                      outside (set/difference (:requested-keys request) allowed-keys denied-keys)]
                  (keep (fn [[keys id reason]]
                          (when (seq keys)
                            (assoc (diagnostic context id [:policies layer] reason layer)
                                   :policy-source (public-reference policy-source) :keys (vec (sort keys))
                                   :scope scope)))
                        [[denied "SAFE10-DENIED" :explicit-deny]
                         [outside "SAFE10-SCOPE" :requested-key-outside-intersection]])))
              policy-layers))]
    {:status (if (seq diagnostics) :rejected :within-scope)
     :diagnostics diagnostics
     :assessment {:schema :gravity.capabilities/environment-scope-v1
                  :request context :effective-keys effective :layers trace
                  :redaction-obligations
                  (mapv #(select-keys % [:layer :policy-source :redaction-policy]) trace)
                  :redaction-enforced? false}}))

(defn assess-scope
  "Assess [request policies declaration declaration-context] without any action.
  All eight policies explicitly bind the same principal/provider/phase/profile/
  target. Public key sets are intersected and explicit denials always survive.
  Redaction references remain obligations, not evidence of their enforcement.
  Inputs are decoded data from a trusted bootstrap caller, not authenticated
  policies or arbitrary host objects. No output authorizes environment access."
  [request policies declaration declaration-context]
  (let [context (public-request request)
        request-errors (shape-errors request request-fields [:request] context)
        errors (if (seq request-errors)
                 request-errors
                 (into (provider-errors request declaration declaration-context context)
                       (policy-errors policies request context)))]
    (if (seq errors)
      {:status :rejected :diagnostics errors}
      (intersect-policies request policies context))))
