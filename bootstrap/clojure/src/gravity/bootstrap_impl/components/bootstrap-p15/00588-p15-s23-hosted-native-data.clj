(declare p15-s23-trusted-carrier-validation)

(defn- p15-s23-hosted-native-view-reason
  "Check native view links without reading unused backing payload or metadata."
  [value ancestors]
  (let [seen (java.util.IdentityHashMap.)]
    (loop [current value views []]
      (let [kind (class current)]
        (cond
          (or (.containsKey seen current)
              (and (not (identical? current value))
                   (.containsKey ^java.util.IdentityHashMap ancestors current)))
          :cyclic-vector-backing

          (identical? clojure.lang.PersistentVector kind)
          (loop [remaining (rseq views) extent (.count ^clojure.lang.PersistentVector current)]
            (if (neg? extent)
              :invalid-vector-view-extent
              (when-let [[start end] (first remaining)]
                (if (<= 0 start end extent)
                  (recur (next remaining) (- end start))
                  :invalid-vector-view-extent))))

          (identical? clojure.lang.APersistentVector$SubVector kind)
          (let [^clojure.lang.APersistentVector$SubVector view current]
            (.put seen current true)
            (recur (.-v view) (conj views [(.-start view) (.-end view)])))

          :else :untrusted-vector-backing)))))

(defn- p15-s23-stage2-hosted-native-data
  [plan value]
  (when-not (and (true? (:compiler-artifact-plan? plan))
                 (= :gravity/stage2-compiler-artifact-plan (:kind plan))
                 (= :hosted (get-in plan [:module :profile]))
                 (= :jvm (get-in plan [:module :target])))
    (throw (ex-info "hosted-native-data-preflight requires a hosted/JVM compiler plan"
                    {:id "HOSTED-NATIVE-DATA" :reason :hosted-jvm-plan-required})))
  (try
    ;; This is a representation check, never an initializer/type/source verdict.
    ;; The policy is explicit; existing Native callers retain their strict domain.
    (select-keys (p15-s23-trusted-carrier-validation value :default-only :hosted-initializer)
                 [:status :reason])
    (catch InterruptedException error
      (.interrupt (Thread/currentThread)) (throw error))
    (catch Exception _
      ;; Do not format an untrusted value or the host exception payload.
      {:status :rejected :reason :malformed-native-data})))
