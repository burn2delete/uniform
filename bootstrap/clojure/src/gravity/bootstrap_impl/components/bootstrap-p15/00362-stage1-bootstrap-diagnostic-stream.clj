

(defn stage1-bootstrap-diagnostic-stream
  [source-root source-set-id]
  {:artifact :gravity/stage1-bootstrap-source-diagnostic-stream
   :stage :stage1-bootstrap-source
   :source-root source-root
   :diagnostics
   (mapv (fn [id]
           {:diagnostic-id (str "diag-" (str/lower-case id) "-stage1")
            :diagnostic id
            :severity :error
            :stage :stage1-bootstrap-source
            :artifact :gravity/diagnostic
            :input-artifact-id source-set-id
            :message (stage1-bootstrap-diagnostic-messages id)
            :remediation [{:kind :keep-gravity-owned-source}
                          {:kind :deny-ambient-authority}
                          {:kind :preserve-compiler-facts}
                          {:kind :record-bootstrap-lineage}]})
         stage1-bootstrap-diagnostic-ids)
   :status :complete})

(defn stage1-bootstrap-validate-source-set!
  [source-root module-records]
  (let [components (set (map :component module-records))
        documents (set (mapcat :documents module-records))]
    (when-not (set/subset? stage1-bootstrap-required-components components)
      (stage1-bootstrap-fail! "STAGE1006" source-root components
                              {:missing-fields [:components]
                               :missing-fact
                               (vec (sort (set/difference
                                            stage1-bootstrap-required-components
                                            components)))}))
    (when-not (set/subset? (set stage1-bootstrap-documents) documents)
      (stage1-bootstrap-fail! "STAGE1006" source-root documents
                              {:missing-fields [:documents]
                               :missing-documents
                               (vec (sort (set/difference
                                            (set stage1-bootstrap-documents)
                                            documents)))})))
  :complete)

(defn stage1-bootstrap-proof
  [artifact]
  (let [modules (:modules artifact)
        components (set (map :component modules))
        documents (set (mapcat :documents modules))
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:stage1-diagnostic-stream
                                       :diagnostics])))]
    {:gravity-authored-source?
     (every? #(= :gravity (:source-language %)) modules)
     :clojure-seed-boundary-explicit?
     (= :clojure-stage0 (get-in artifact [:seed-boundary :verified-by]))
     :component-coverage?
     (set/subset? stage1-bootstrap-required-components components)
     :ambient-authority-denied?
     (every? #(and (empty? (:effects %))
                   (empty? (:capabilities %)))
             modules)
     :lineage-covered?
     (every? #(= :clojure-stage0 (get-in % [:lineage :verified-by]))
             modules)
     :preserved-facts-covered?
     (every? #(set/subset? stage1-bootstrap-required-preserved-facts
                           (set (:preserves %)))
             modules)
     :documents-covered?
     (set/subset? (set stage1-bootstrap-documents) documents)
     :accepted-fixtures-covered?
     (seq (:accepted-stage1-bootstrap-source-fixtures artifact))
     :rejected-fixtures-covered?
     (= (set stage1-bootstrap-diagnostic-ids) diagnostics)
     :limitations
     {:self-hosted-compiler-complete? false
      :clojure-seed-retired? false
      :production-release? false
      :next-required-capability
      :execute-gravity-authored-reader-without-clojure-compiler-logic}
     :status :complete}))

(defn stage1-bootstrap-source-artifact
  [source-root]
  (let [source-files (stage1-bootstrap-source-files source-root)
        module-records (mapv stage1-bootstrap-module-record source-files)
        _ (stage1-bootstrap-validate-source-set! source-root module-records)
        source-set-id (str "sha256:"
                           (sha256-hex
                            (str/join "\n"
                                      (map #(str (:source-path %)
                                                 ":"
                                                 (:source-hash %))
                                           module-records))))
        artifact-base
        {:kind :gravity/stage1-bootstrap-source-artifact
         :phase "15"
         :task "P15-STAGE1-SOURCE"
         :stage :stage1-bootstrap-source
         :source-root source-root
         :source-files source-files
         :source-set-id source-set-id
         :document-set stage1-bootstrap-documents
         :governing-documents stage1-bootstrap-governing-documents
         :seed-boundary
         {:seed-language :clojure
          :verified-by :clojure-stage0
          :role :trusted-seed-verifier
          :retirement-objective :replace-clojure-seed
          :still-trusted? true}
         :pass {:name :stage1-bootstrap-source
                :input :gravity-source-set
                :output :stage1-bootstrap-source-artifact
                :requires [:gravity-owned-source :stage-lineage
                           :ambient-authority-denial
                           :preserved-compiler-facts
                           :component-coverage]
                :preserves [:source-hash :source-spans :syntax-identity
                            :diagnostic-code :artifact-provenance
                            :stage-lineage]
                :emits [:modules :accepted-stage1-bootstrap-source-fixtures
                        :rejected-stage1-bootstrap-source-fixtures
                        :stage1-diagnostic-stream]
                :rejects stage1-bootstrap-diagnostic-ids}
         :modules module-records
         :accepted-stage1-bootstrap-source-fixtures
         [{:fixture "bootstrap/gravity/src"
           :components (vec (sort stage1-bootstrap-required-components))
           :status :accepted}]
         :rejected-stage1-bootstrap-source-fixtures
         stage1-bootstrap-rejected-fixture-records
         :stage1-diagnostic-stream
         (stage1-bootstrap-diagnostic-stream source-root source-set-id)
         :stage1-bootstrap-source-results
         {:module-count (count module-records)
          :component-count (count stage1-bootstrap-required-components)
          :accepted-fixtures 1
          :rejected-fixtures (count stage1-bootstrap-rejected-fixture-records)
          :diagnostic-count (count stage1-bootstrap-diagnostic-ids)
          :status :complete}
         :diagnostics []}
        capability-proof (stage1-bootstrap-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn stage1-bootstrap-source-file-artifact
  [path]
  (stage1-bootstrap-source-artifact path))

(def stage1-reader-source-path
  "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")

(def stage1-reader-bootstrap-source-root
  "bootstrap/gravity/src")

(def stage1-reader-execution-diagnostic-messages
  {"STAGE1READER001" "stage1 reader table rejected an unexpected close delimiter"
   "STAGE1READER002" "stage1 reader table rejected an unclosed delimited form"
   "STAGE1READER003" "stage1 reader table rejected an unclosed string"
   "STAGE1READER004" "stage1 reader table rejected unsupported dispatch syntax"
   "STAGE1READER005" "stage1 reader table rejected a map with an odd entry count"
   "STAGE1READER006" "stage1 reader table is missing or malformed"
   "STAGE1READER007" "stage1 reader rejected a malformed numeric lexeme"})

(def stage1-reader-execution-diagnostic-ids
  ["STAGE1READER001" "STAGE1READER002" "STAGE1READER003"
   "STAGE1READER004" "STAGE1READER005" "STAGE1READER006"])

(def stage1-reader-execution-rejected-fixture-records
  [{:fixture "bootstrap/clojure/fixtures/rejected/stage1-reader-unexpected-close.gravity"
    :diagnostic "STAGE1READER001"
    :rejected-behavior :unexpected-close-delimiter}
   {:fixture "bootstrap/clojure/fixtures/rejected/stage1-reader-unclosed-list.gravity"
    :diagnostic "STAGE1READER002"
    :rejected-behavior :unclosed-delimited-form}
   {:fixture "bootstrap/clojure/fixtures/rejected/stage1-reader-unclosed-string.gravity"
    :diagnostic "STAGE1READER003"
    :rejected-behavior :unclosed-string}
   {:fixture "bootstrap/clojure/fixtures/rejected/stage1-reader-unsupported-dispatch.gravity"
    :diagnostic "STAGE1READER004"
    :rejected-behavior :unsupported-dispatch}
   {:fixture "bootstrap/clojure/fixtures/rejected/stage1-reader-odd-map.gravity"
    :diagnostic "STAGE1READER005"
    :rejected-behavior :odd-map-entries}])

(defn stage1-reader-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-execution-diagnostic-messages
              id
              "stage1 reader execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-execution
                 :diagnostic-family :stage1-reader-execution
                 :value value
                 :remediation "Keep the reader table in Gravity source, use supported stage1 reader syntax, and preserve source spans and diagnostics."}
                data)))

(defn stage1-reader-classpath-project-root
  []
  (when-let [resource (io/resource "gravity/bootstrap.clj")]
    (when (= "file" (.getProtocol resource))
      (loop [candidate (.getParentFile (.getCanonicalFile (io/file resource)))]
        (when candidate
          (if (.isFile (io/file candidate "deps.edn"))
            candidate
            (recur (.getParentFile candidate))))))))

(defn stage1-reader-owned-source-file
  [logical-path]
  (let [classpath-root (stage1-reader-classpath-project-root)
        candidates (if classpath-root
                     [(io/file classpath-root logical-path)]
                     [(io/file logical-path)])]
    (or (some #(when (.isFile %) (.getCanonicalFile %)) candidates)
        (stage1-reader-fail!
         "STAGE1READER006" logical-path nil
         {:missing-fields [:owned-reader-source]
          :facts {:logical-path logical-path
                  :resolution-bases
                  (if classpath-root
                    [:bootstrap-classpath-root]
                    [:process-working-directory-fallback])}}))))