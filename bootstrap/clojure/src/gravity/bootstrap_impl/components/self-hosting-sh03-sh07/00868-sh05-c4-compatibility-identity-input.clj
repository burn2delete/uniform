

(defn sh05-c4-compatibility-identity-input
  [artifact]
  {:domain :gravity/sh05-c4-compatibility-artifact-v1
   :kind (:kind artifact)
   :task (:task artifact)
   :document-set (:document-set artifact)
   :governing-document (:governing-document artifact)
   :sh05-artifact-id
   (get-in artifact [:sh05-macro-expansion-artifact :artifact-id])
   :macro-expansion-input (:macro-expansion-input artifact)
   :macro-environment (:macro-environment artifact)
   :expanded-syntax-stream-id
   (get-in artifact
           [:sh05-macro-expansion-artifact :expanded-syntax-stream-id])
   :macro-expansion-trace-id
   (get-in artifact
           [:sh05-macro-expansion-artifact :macro-expansion-trace-id])
   :hygiene-capture-records (:hygiene-capture-records artifact)
   :build-effect-log (:build-effect-log artifact)
   :macro-safety-declarations (:macro-safety-declarations artifact)
   :capability-based-proof (:capability-based-proof artifact)
   :execution-boundary (:execution-boundary artifact)
   :diagnostics (:diagnostics artifact)})

(defn sh05-c4-compatibility-artifact-id
  [artifact]
  (reader-canonical-hash (sh05-c4-compatibility-identity-input artifact)))

(defn sh05-c4-compatibility-artifact
  [source-path sh05]
  (let [artifact-base
        {:kind :gravity/stage0-c4-macro-expansion-artifact
         :task "P06-D083"
         :document-set ["C4"]
         :governing-document c4-macro-governing-document
         :sh05-macro-expansion-artifact sh05
         :c3-syntax-object-artifact (:sh04-syntax-artifact sh05)
         :macro-expansion-input
         {:artifact :gravity/macro-expansion-input
          :syntax-root (get-in sh05
                               [:sh04-syntax-artifact :syntax-stream-id])
          :profile :meta :target :jvm :hermetic true}
         :macro-environment (:macro-environment sh05)
         :expanded-syntax-stream (:expanded-syntax-stream sh05)
         :macro-expansion-trace (:macro-expansion-trace sh05)
         :generated-origin-source-map (:generated-origin-source-map sh05)
         :hygiene-capture-records []
         :build-effect-log {:artifact :gravity/macro-build-effect-log
                            :records [] :status :complete}
         :macro-safety-declarations
         {:artifact :gravity/macro-safety-declaration-records
          :records [] :status :complete}
         :capability-based-proof (:capability-based-proof sh05)
         :execution-boundary
         {:macro-authority :gravity
          :c4-stage0-adapter :compatibility-only
          :target-source-reread? false
          :self-hosting-credit? false}
         :diagnostics []}
        artifact-id (sh05-c4-compatibility-artifact-id artifact-base)]
    (assoc artifact-base :artifact-id artifact-id)))

(defn sh05-authoritative-compiler-source-path?
  [source-path]
  (let [anchor (io/file (sh05-macro-resolve-source-path))
        repository-root
        (loop [directory (.getParentFile anchor)]
          (cond
            (nil? directory) nil
            (.isFile (io/file directory "deps.edn")) directory
            :else (recur (.getParentFile directory))))
        root
        (.toPath
         (.getCanonicalFile
          (io/file (or repository-root (io/file "."))
                   "bootstrap/gravity/src")))
        candidate (.toPath (.getCanonicalFile (io/file source-path)))]
    (.startsWith candidate root)))

(defn sh05-form-contains-legacy-macro-position?
  [form]
  (cond
    (seq? form)
    (or (contains? '#{when -> defmacro} (first form))
        (some sh05-form-contains-legacy-macro-position? form))

    (map? form)
    (some sh05-form-contains-legacy-macro-position?
          (mapcat identity form))

    (coll? form)
    (some sh05-form-contains-legacy-macro-position? form)

    :else false))

(defn sh05-bounded-authoritative-source?
  [source-path module forms]
  (and (sh05-authoritative-compiler-source-path? source-path)
       (empty? (:requires module))
       (empty? (:imports module))
       (every?
        (fn [form]
          (and (seq? form)
               (contains? '#{ns def defn} (first form))))
        forms)
       (not-any? sh05-form-contains-legacy-macro-position? forms)))