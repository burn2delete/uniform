(let [authority-token (Object.)
      cache-state (atom nil)]

  ;; Bootstrap cache tests resolve these private inspection hooks by symbol.
  #_{:clj-kondo/ignore [:unused-private-var]}
  (defn- p15-s23-c6c10-clear-compiled-binding-cache!
    []
    (reset! cache-state nil)
    :cleared)

  #_{:clj-kondo/ignore [:unused-private-var]}
  (defn- p15-s23-c6c10-compiled-binding-cache-state
    []
    (let [entry @cache-state]
      {:entry-count (if (nil? entry) 0 1)
       :occupied? (some? entry)
       :schema-version (:schema-version entry)
       :cache-key (:cache-key entry)}))

  (defn- p15-s23-c6c10-valid-compiled-binding-cache-entry?
    [entry cache-key]
    (and (map? entry)
         (not (record? entry))
         (nil? (meta entry))
         (= #{:schema-version :authority-token :cache-key :binding}
            (set (keys entry)))
         (= 1 (:schema-version entry))
         (identical? authority-token (:authority-token entry))
         (= cache-key (:cache-key entry))
         (map? (:binding entry))
         (not (record? (:binding entry)))
         (nil? (meta (:binding entry)))
         (not (contains? (:binding entry) :request-source))
         (not (contains? (:binding entry) :source-path))
         (p15-s23-c6c10-source-binding-valid?
          (:binding entry) (p15-s23-c6c10-source-program-version cache-key))))

  (defn- p15-s23-c6c10-cached-compiled-binding!
    [request-source inputs]
    (let [cache-key
          (p15-s23-c6c10-compiled-binding-cache-key inputs)
          entry @cache-state]
      (if (p15-s23-c6c10-valid-compiled-binding-cache-entry?
           entry cache-key)
        (do
          (p15-s23-c6c10-record-compiled-binding-metric! :cache-hit)
          (:binding entry))
        (locking cache-state
          (let [entry @cache-state]
            (if (p15-s23-c6c10-valid-compiled-binding-cache-entry?
                 entry cache-key)
              (do
                (p15-s23-c6c10-record-compiled-binding-metric! :cache-hit)
                (:binding entry))
              (do
                (p15-s23-c6c10-record-compiled-binding-metric! :cache-miss)
                (when (and (some? entry)
                           (= cache-key (:cache-key entry)))
                  (reset! cache-state nil))
                (let [binding
                      (p15-s23-c6c10-validate-source-binding!
                       (p15-s23-c6c10-compile-authenticated-source-binding
                        request-source inputs)
                       (p15-s23-c6c10-source-program-version inputs))
                      cached-binding
                      (dissoc binding :request-source :source-path)]
                  (reset!
                   cache-state
                   {:schema-version 1
                    :authority-token authority-token
                    :cache-key cache-key
                    :binding cached-binding})
                  cached-binding))))))))

(defn p15-s23-c6c10-source-binding!
  ([request-source] (p15-s23-c6c10-source-binding! request-source 1))
  ([request-source version]
  ;; Raw module bytes and the Gravity plan-emitter are authenticated before
  ;; every lookup.  Only the immutable compiled compiler program is reused;
  ;; user ingress, builder/verifier execution, digest sealing, diagnostics,
  ;; and target artifacts remain fresh replay boundaries.
  (let [inputs
        (p15-s23-c6c10-authenticated-source-binding-inputs!
         request-source version)
        cached-binding
        (p15-s23-c6c10-cached-compiled-binding!
         request-source inputs)
        current-source-path
        (get-in inputs [:source-snapshot :source-path])]
    (p15-s23-c6c10-validate-source-binding!
     (assoc cached-binding
            :request-source request-source
            :source-path current-source-path) version)))))

(def p15-s23-c6c10-digest-request-keys
  #{:algorithm :depends-on :encoding :key :ordinal :preimage})
