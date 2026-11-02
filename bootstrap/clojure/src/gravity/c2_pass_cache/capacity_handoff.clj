;; Semantic C2 pass-cache component loaded by gravity.c2-pass-cache.
;; It deliberately evaluates in the facade namespace to retain private test seams.
(def ^:private prevalidated-carrier-fields
  #{:artifact :schema-version :state :exact-artifact :c2-artifact-id
    :boundary-projection-id :semantic-key-id :storage-key-id
    :producer-binding-id :validation-binding-id :carrier-id})

(def ^:private capacity-exception-fields
  #{:id :artifact :schema-version :stage :operation :phase
    :invocation-token :carrier-id :resource :maximum :observed
    :semantic-key-id :storage-key-id :c2-artifact-id})

(def ^:private capacity-handoff-fields
  #{:artifact :schema-version :phase :invocation-token :capacity-exception
    :lookup-variant :cache-key-id})

(defn- carrier-projection
  [carrier]
  (dissoc carrier :exact-artifact :carrier-id))

(defn- prevalidated-carrier!
  [artifact legacy-key request validation-binding-id operations]
  (let [artifact-id ((:artifact-id-of operations) artifact)
        boundary-id ((:boundary-projection-id-of operations) artifact)
        _ (ensure-sha256-id! :artifact-id artifact-id)
        _ (ensure-sha256-id! :boundary-projection-id boundary-id)
        base {:artifact :gravity/c2-pass-cache-prevalidated-carrier
              :schema-version 1
              :state :semantic-validated
              :exact-artifact artifact
              :c2-artifact-id artifact-id
              :boundary-projection-id boundary-id
              :semantic-key-id (:semantic-key-id legacy-key)
              :storage-key-id (:storage-key-id legacy-key)
              :producer-binding-id (:producer-binding-id request)
              :validation-binding-id validation-binding-id}
        carrier-id (canonical-content-id
                    {:domain :gravity/c2-pass-cache-prevalidated-carrier-v1
                     :carrier (carrier-projection base)})]
    (assoc base :carrier-id carrier-id)))

(defn- capacity-operation-phase
  [operation]
  (case operation
    :lookup-or-compute :fresh-envelope-serialization
    :store :direct-store-envelope-serialization
    nil))

(defn- valid-prevalidated-carrier?
  [state legacy-key generic-key]
  (let [carrier (:carrier @state)]
    (and (map? carrier)
         (= prevalidated-carrier-fields (set (keys carrier)))
         (= :gravity/c2-pass-cache-prevalidated-carrier (:artifact carrier))
         (= 1 (:schema-version carrier))
         (= :semantic-validated (:state carrier))
         (some? (:exact-artifact carrier))
         (every? sha256-id?
                 (map carrier
                      [:c2-artifact-id :boundary-projection-id
                       :semantic-key-id :storage-key-id :producer-binding-id
                       :validation-binding-id :carrier-id]))
         (= (:semantic-key-id legacy-key) (:semantic-key-id carrier))
         (= (:storage-key-id legacy-key) (:storage-key-id carrier))
         (= (:producer-binding-id generic-key)
            (:producer-binding-id carrier))
         (= (:carrier-id carrier)
            (canonical-content-id
             {:domain :gravity/c2-pass-cache-prevalidated-carrier-v1
              :carrier (carrier-projection carrier)})))))

(defn- valid-capacity-exception?
  [error state legacy-key operation]
  (let [data (ex-data error)
        carrier (:carrier @state)
        token (:invocation-token @state)]
    (and (instance? clojure.lang.ExceptionInfo error)
         (identical? error (:capacity-exception @state))
         (= capacity-exception-fields (set (keys data)))
         (= "C16-POLICY" (:id data))
         (= :gravity/c2-pass-cache-envelope-capacity-exception
            (:artifact data))
         (= 1 (:schema-version data))
         (= :c2-reader (:stage data))
         (= operation (:operation data))
         (= (capacity-operation-phase operation) (:phase data))
         (identical? token (:invocation-token data))
         (= (:carrier-id carrier) (:carrier-id data))
         (contains? #{:canonical-bytes}
                    (:resource data))
         (integer? (:maximum data))
         (pos? (:maximum data))
         (integer? (:observed data))
         (> (:observed data) (:maximum data))
         (= (:semantic-key-id legacy-key) (:semantic-key-id data))
         (= (:storage-key-id legacy-key) (:storage-key-id data))
         (= (:c2-artifact-id carrier) (:c2-artifact-id data)))))

(defn- throw-capacity-exception!
  [state legacy-key operation resource maximum observed]
  (let [carrier (:carrier @state)
        data {:id "C16-POLICY"
              :artifact :gravity/c2-pass-cache-envelope-capacity-exception
              :schema-version 1
              :stage :c2-reader
              :operation operation
              :phase (capacity-operation-phase operation)
              :invocation-token (:invocation-token @state)
              :carrier-id (:carrier-id carrier)
              :resource resource
              :maximum maximum
              :observed observed
              :semantic-key-id (:semantic-key-id legacy-key)
              :storage-key-id (:storage-key-id legacy-key)
              :c2-artifact-id (:c2-artifact-id carrier)}
        error (ex-info "fresh C2 artifact exceeds cache envelope capacity" data)]
    (swap! state assoc :capacity-exception error)
    (throw error)))

(defn- clean-miss-lookup?
  [looked-up generic-key]
  (let [evidence (:cache-evidence looked-up)]
    (and (= :miss (:status looked-up))
         (= :gravity/pass-cache-evidence (:artifact evidence))
         (integer? (:schema-version evidence))
         (pos? (:schema-version evidence))
         (= :miss (:status evidence))
         (= (:cache-key-id generic-key) (:cache-key-id evidence))
         (not (contains? evidence :rejected-entry-evidence)))))

(defn- rejected-entry-lookup?
  [looked-up generic-key]
  (let [evidence (:cache-evidence looked-up)]
    (and (= :rejected (:status looked-up))
         (= :gravity/pass-cache-evidence (:artifact evidence))
         (integer? (:schema-version evidence))
         (pos? (:schema-version evidence))
         (= :rejected (:status evidence))
         (= (:cache-key-id generic-key) (:cache-key-id evidence)))))

(defn- capacity-failure-handoff!
  [state legacy-key generic-key operation error looked-up]
  (if-not (and (= :lookup-or-compute operation)
               (valid-prevalidated-carrier? state legacy-key generic-key)
               (valid-capacity-exception? error state legacy-key operation))
    (throw error)
    (let [[variant rejected-evidence]
          (cond
            (clean-miss-lookup? looked-up generic-key) [:clean-miss nil]
            (rejected-entry-lookup? looked-up generic-key)
            [:rejected-entry (:cache-evidence looked-up)]
            :else (throw error))
          data (cond->
                {:artifact :gravity/pass-cache-capacity-handoff
                 :schema-version 1
                 :phase :fresh-producer-capacity-handoff
                 :invocation-token (:invocation-token @state)
                 :capacity-exception error
                 :lookup-variant variant
                 :cache-key-id (:cache-key-id generic-key)}
                 rejected-evidence
                 (assoc :rejected-entry-evidence rejected-evidence))
          wrapper (ex-info "C2 cache envelope capacity handoff" data)]
      (swap! state assoc :capacity-handoff wrapper)
      (throw wrapper))))

(defn- valid-capacity-handoff?
  [wrapper state legacy-key generic-key]
  (let [data (ex-data wrapper)
        variant (:lookup-variant data)
        expected-fields (cond-> capacity-handoff-fields
                          (= :rejected-entry variant)
                          (conj :rejected-entry-evidence))
        capacity (:capacity-exception data)
        rejected (:rejected-entry-evidence data)]
    (and (instance? clojure.lang.ExceptionInfo wrapper)
         (identical? wrapper (:capacity-handoff @state))
         (= expected-fields (set (keys data)))
         (= :gravity/pass-cache-capacity-handoff (:artifact data))
         (= 1 (:schema-version data))
         (= :fresh-producer-capacity-handoff (:phase data))
         (identical? (:invocation-token @state) (:invocation-token data))
         (identical? (:capacity-exception @state) capacity)
         (= (:cache-key-id generic-key) (:cache-key-id data))
         (contains? #{:clean-miss :rejected-entry} variant)
         (valid-prevalidated-carrier? state legacy-key generic-key)
         (valid-capacity-exception?
          capacity state legacy-key :lookup-or-compute)
         (if (= :clean-miss variant)
           (not (contains? data :rejected-entry-evidence))
           (and (= :gravity/pass-cache-evidence (:artifact rejected))
                (integer? (:schema-version rejected))
                (pos? (:schema-version rejected))
                (= :rejected (:status rejected))
                (= (:cache-key-id generic-key) (:cache-key-id rejected)))))))

(defn- capacity-withholding-result!
  [legacy-key generic-key state wrapper]
  (if-not (valid-capacity-handoff? wrapper state legacy-key generic-key)
    (throw wrapper)
    (let [data (ex-data wrapper)
          carrier (:carrier @state)
          capacity (:capacity-exception data)
          rejected? (= :rejected-entry (:lookup-variant data))
          evidence
          (lookup-evidence
           legacy-key :miss
           (if rejected?
             :rejected-entry-fresh-artifact-envelope-capacity
             :fresh-artifact-envelope-capacity)
           (cond->
            {:contained-diagnostic "C16-POLICY"
             :contained-capacity (dissoc (ex-data capacity)
                                         :invocation-token)
             :cache-publication :withheld
             :disk-candidate-publication :none
             :reader-executed? true
             :artifact-reused? false
             :artifact-id (:c2-artifact-id carrier)}
             rejected?
             (assoc :rejected-entry-evidence
                    (:rejected-entry-evidence data))))]
      {:artifact (:exact-artifact carrier)
       :cache-evidence evidence})))
