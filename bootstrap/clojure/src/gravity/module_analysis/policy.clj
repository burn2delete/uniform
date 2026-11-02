(ns gravity.module-analysis.policy)

(def function-operation-keys
  #{:fail! :source-span :ns-form? :bootstrap-target-supported?
    :validate-ns-syntax! :syntax-object-stream :sha256-hex :require-ns
    :parse-clause :single-clause-value :clause-args :parse-options
    :parse-dependency-entry :parse-dependencies :top-level-definition
    :definition-table :collect-symbols :collect-code-symbols :infer-effects
    :required-capabilities-for-effects :profile-direct-import-allowed?
    :assert-unique-aliases! :assert-referred-names-unambiguous!
    :assert-qualified-symbols-resolve! :assert-profile-boundaries!
    :assert-namespace-effect-and-capability! :parse-module :uses-println?
    :validate-module-effects! :module-source-artifact-from-records})

(def scalar-operation-keys
  #{:known-source-profiles :supported-profiles :supported-targets
    :effect-capability :profile-direct-imports})

(def operation-keys
  (into function-operation-keys scalar-operation-keys))

(def known-source-profiles-default
  #{:core :hardware :firmware :kernel :native :hosted
    :distributed :ai :meta :gpu :formal})

(def supported-profiles-default #{:hosted})
(def supported-targets-default #{:jvm})

(def effect-capability-default
  {:io/write :io/stdout
   :network/listen :network/listener})

(def profile-direct-imports-default
  {:core #{:core}
   :meta #{:core :meta}
   :hosted #{:core :hosted}
   :native #{:core :native}
   :firmware #{:core :firmware}
   :kernel #{:core :kernel}
   :hardware #{:core :hardware}
   :distributed #{:core :distributed}
   :ai #{:core :distributed :ai}
   :gpu #{:core :gpu}
   :formal #{:core :formal}})

(defn valid-keyword-set?
  [value]
  (and (set? value) (seq value) (every? keyword? value)))

(defn valid-effect-capability?
  [value]
  (and (map? value)
       (every? keyword? (keys value))
       (every? keyword? (vals value))))

(defn valid-profile-direct-imports?
  [value]
  (and (map? value)
       (every? keyword? (keys value))
       (every? valid-keyword-set? (vals value))))

(defn default-fail!
  [id message data]
  (throw (ex-info message
                  (merge {:id id :message message :bootstrap-stage :stage0}
                         data))))

(defn default-source-span
  [source-path form-index]
  {:source source-path :form-index form-index})

(defn default-ns-form?
  [form]
  (and (seq? form) (= 'ns (first form))))

(defn default-sha256-hex
  [text]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256")
                        (.getBytes ^String text "UTF-8"))]
    (apply str (map #(format "%02x" (bit-and % 0xff)) digest))))

(defn unsupported-operation
  [key]
  (fn [& _]
    (throw (ex-info (str "gravity.module-analysis requires operation " key)
                    {:operation key}))))

(defn validate-operations!
  [operations]
  (when-not (map? operations)
    (throw (ex-info "Gravity module-analysis operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "Gravity module-analysis operations contain unknown keys"
                      {:unknown-keys unknown :allowed-keys operation-keys}))))
  (doseq [key function-operation-keys :when (contains? operations key)]
    (when-not (fn? (get operations key))
      (throw (ex-info "Gravity module-analysis operation must be a function"
                      {:operation key :value (get operations key)}))))
  (doseq [[key value] [[:known-source-profiles (:known-source-profiles operations)]
                       [:supported-profiles (:supported-profiles operations)]
                       [:supported-targets (:supported-targets operations)]]
          :when (contains? operations key)]
    (when-not (valid-keyword-set? value)
      (throw (ex-info "Gravity module-analysis scalar operation must be a non-empty keyword set"
                      {:operation key :value value}))))
  (when (and (contains? operations :effect-capability)
             (not (valid-effect-capability? (:effect-capability operations))))
    (throw (ex-info "Gravity module-analysis effect-capability operation must map keywords to keywords"
                    {:operation :effect-capability
                     :value (:effect-capability operations)})))
  (when (and (contains? operations :profile-direct-imports)
             (not (valid-profile-direct-imports?
                   (:profile-direct-imports operations))))
    (throw (ex-info "Gravity module-analysis profile-direct-imports operation must map keywords to non-empty keyword sets"
                    {:operation :profile-direct-imports
                     :value (:profile-direct-imports operations)})))
  operations)
