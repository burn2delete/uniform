(declare c-backend-stage2-plan-emitter-source-rule!
         p15-s23-stage2-compiler-expected-source-content-hash
         p15-s23-stage2-compiler-expected-source-byte-count
         p15-s23-stage2-compiler-artifact-binding-from-source!
         p15-s23-stage2-compiler-artifact-binding!)

(defn- p15-s23-stage2-validate-current-expression-program! [source-path plan]
  (let [contract (p15-s23-stage2-expression-source-contract 2)
        functions (:functions plan)
        digest (fn [value] (str "sha256:" (sha256-hex (pr-str (c-backend-canonical-value value)))))
        semantic (digest (p15-s23-stage2-compiler-artifact-semantic-input plan))
        observed {:source-content-hash (get-in plan [:source :sha256])
                  :semantic-hash semantic
                  :artifact-hash (digest {:source-content-hash (get-in plan [:source :sha256]) :semantic-hash semantic})
                  :functions-semantic-hash (digest functions)
                  :function-count (count functions)
                  :function-names-hash (digest (vec (sort-by str (keys functions))))
                  :instruction-summary (:instruction-summary plan)
                  :required-functions
                  (into {} (map (fn [[name _]] [name (select-keys (get functions name) [:arity :params])]))
                        (:required-functions contract))
                  :required-export-semantic-hashes
                  (into {} (map (fn [[name _]] [name (digest (get functions name))])) (:required-functions contract))
                  :declared-plan-compiler-labels (:compiler plan)}]
    (when-not (= observed (select-keys contract (keys observed)))
      (p15-s23-stage2-plan-emitter-fail! "P15S23Q002" source-path nil
       {:missing-fact :stage2-current-expression-complete-program-identities}))
    (when-not (= (:plan-id plan) (digest (dissoc plan :plan-id)))
      (p15-s23-stage2-plan-emitter-fail! "P15S23Q002" source-path nil
       {:missing-fact :current-expression-recomputable-plan-id}))
    plan))

(declare c-backend-stage2-plan-emitter-source-rule!
         p15-s23-stage2-compiler-expected-source-content-hash
         p15-s23-stage2-compiler-expected-source-byte-count)

(defn- p15-s23-stage2-current-expression-source-snapshot! [request-source]
  (let [contract (p15-s23-stage2-expression-source-contract 2)
        path (.toPath (java.io.File. (p15-s23-stage2-compiler-artifact-source-path)))
        links (into-array java.nio.file.LinkOption [java.nio.file.LinkOption/NOFOLLOW_LINKS])
        before (java.nio.file.Files/readAttributes path java.nio.file.attribute.BasicFileAttributes links)]
    (when-not (and (.isRegularFile before) (not (.isSymbolicLink before))
                   (= (:source-byte-count contract) (.size before)))
      (p15-s23-stage2-plan-emitter-fail! "P15S23Q002" request-source nil
       {:missing-fact :current-expression-source-regular-file}))
    (let [source (p15-s23-sh07-b51-compatibility-source-record (str path))
          after (java.nio.file.Files/readAttributes path java.nio.file.attribute.BasicFileAttributes links)]
      (when-not (and (.isRegularFile after) (not (.isSymbolicLink after))
                     (= (.fileKey before) (.fileKey after))
                     (= (.lastModifiedTime before) (.lastModifiedTime after))
                     (= (.size before) (.size after) (alength ^bytes (:bytes source)))
                     (= (:source-content-hash contract) (str "sha256:" (sha256-bytes-hex (:bytes source)))))
        (p15-s23-stage2-plan-emitter-fail! "P15S23Q002" request-source nil
         {:missing-fact :stable-current-expression-source-snapshot}))
      source)))

(defn- p15-s23-stage2-current-expression-binding!
  [emitter source-path target version]
   (let [contract (p15-s23-stage2-expression-source-contract version)]
     (if (= 1 version)
       (p15-s23-stage2-compiler-artifact-binding! emitter source-path target)
       (do
         (when-not (string? source-path)
           (p15-s23-stage2-plan-emitter-fail! "P15S23Q002" "<expression-source-contract>" nil
            {:missing-fact :current-expression-request-source-context}))
         (when (or (not (identical? :jvm target)) *p15-s23-sh07-b51-governed-source-session*)
           (p15-s23-stage2-plan-emitter-fail! "P15S23Q002" source-path nil
            {:missing-fact :current-expression-selected-host-source-context}))
         (when-not (= :passed (:status (p15-s23-trusted-carrier-validation emitter :default-only)))
           (p15-s23-stage2-plan-emitter-fail! "P15S23Q002" source-path nil
            {:missing-fact :current-expression-native-plan-emitter}))
         (let [rule (c-backend-stage2-plan-emitter-source-rule! source-path target)
               digest (fn [value] (str "sha256:" (sha256-hex (pr-str (c-backend-canonical-value value)))))
               expected-rule (get-in contract [:plan-emitter :source-rule-hash])
               _ (when-not (and (= (get-in contract [:plan-emitter :source-content-hash])
                                   p15-s23-stage2-compiler-expected-source-content-hash)
                                (= (get-in contract [:plan-emitter :source-byte-count])
                                   p15-s23-stage2-compiler-expected-source-byte-count)
                                (= :complete (:status (:rule-record rule)))
                                (= expected-rule (digest emitter) (digest (:emitter rule)) (:source-rule-hash rule)))
                   (p15-s23-stage2-plan-emitter-fail! "P15S23Q002" source-path nil
                    {:missing-fact :current-expression-plan-emitter-join}))
               before (p15-s23-stage2-current-expression-source-snapshot! source-path)
               binding (p15-s23-stage2-compiler-artifact-binding-from-source!
                        (:emitter rule) source-path target (:path before) (:text before) version)
               after (p15-s23-stage2-current-expression-source-snapshot! source-path)]
           (when-not (and (= (:path before) (:path after))
                          (java.util.Arrays/equals ^bytes (:bytes before) ^bytes (:bytes after)))
             (p15-s23-stage2-plan-emitter-fail! "P15S23Q002" source-path nil
              {:missing-fact :stable-current-expression-source-compilation}))
           binding)))))

;; The fixed v2 binding above remains the historical, fail-closed handoff.  A
;; public current-source construction must not be made to pass by rewriting
;; that record after the expression emitter changes.  It instead derives a
;; fresh, authenticated binding from the source snapshot selected by the
;; current public C6 owner.  The current-expression contract supplies only
;; the reviewed ABI and plan-emitter join; all emitter identities below are
;; observations of this snapshot.
(defn- p15-s23-stage2-current-expression-development-source-snapshot!
  [request-source]
  (let [path (.toPath (java.io.File.
                       (p15-s23-stage2-compiler-artifact-source-path)))
        links (into-array java.nio.file.LinkOption
                          [java.nio.file.LinkOption/NOFOLLOW_LINKS])
        before (java.nio.file.Files/readAttributes
                path java.nio.file.attribute.BasicFileAttributes links)]
    (when-not (and (.isRegularFile before)
                   (not (.isSymbolicLink before)))
      (p15-s23-stage2-plan-emitter-fail!
       "P15S23Q002" request-source nil
       {:missing-fact :current-expression-source-regular-file}))
    (let [source (p15-s23-sh07-b51-compatibility-source-record (str path))
          after (java.nio.file.Files/readAttributes
                 path java.nio.file.attribute.BasicFileAttributes links)
          source-content-hash
          (str "sha256:" (sha256-bytes-hex (:bytes source)))]
      (when-not (and (.isRegularFile after)
                     (not (.isSymbolicLink after))
                     (= (.fileKey before) (.fileKey after))
                     (= (.lastModifiedTime before) (.lastModifiedTime after))
                     (= (.size before) (.size after)
                        (alength ^bytes (:bytes source))))
        (p15-s23-stage2-plan-emitter-fail!
         "P15S23Q002" request-source nil
         {:missing-fact :stable-current-expression-source-snapshot}))
      (assoc source
             :source-byte-count (alength ^bytes (:bytes source))
             :source-content-hash source-content-hash))))

(defn- p15-s23-stage2-current-expression-development-binding!
  [source-path]
  (let [contract (p15-s23-stage2-expression-source-contract 2)
        digest (fn [value]
                 (str "sha256:"
                      (sha256-hex
                       (pr-str (c-backend-canonical-value value)))))]
    (when-not (string? source-path)
      (p15-s23-stage2-plan-emitter-fail!
       "P15S23Q002" "<expression-source-contract>" nil
       {:missing-fact :current-expression-request-source-context}))
    (when *p15-s23-sh07-b51-governed-source-session*
      (p15-s23-stage2-plan-emitter-fail!
       "P15S23Q002" source-path nil
       {:missing-fact :current-expression-selected-host-source-context}))
    (let [rule (c-backend-stage2-plan-emitter-source-rule! source-path :jvm)
          _ (when-not (= :passed
                         (:status (p15-s23-trusted-carrier-validation
                                   (:emitter rule) :default-only)))
              (p15-s23-stage2-plan-emitter-fail!
               "P15S23Q002" source-path nil
               {:missing-fact :current-expression-native-plan-emitter}))
          expected-rule (get-in contract [:plan-emitter :source-rule-hash])
          _ (when-not
              (and (= (get-in contract [:plan-emitter :source-content-hash])
                      p15-s23-stage2-compiler-expected-source-content-hash)
                   (= (get-in contract [:plan-emitter :source-byte-count])
                      p15-s23-stage2-compiler-expected-source-byte-count)
                   (= :complete (:status (:rule-record rule)))
                   (= expected-rule
                      (digest (:emitter rule))
                      (:source-rule-hash rule)))
            (p15-s23-stage2-plan-emitter-fail!
             "P15S23Q002" source-path nil
             {:missing-fact :current-expression-plan-emitter-join}))
          before
          (p15-s23-stage2-current-expression-development-source-snapshot!
           source-path)
          plan
          (try
            (p15-s23-stage2-compiler-artifact-plan
             (:emitter rule) (:path before) (:text before))
            (catch clojure.lang.ExceptionInfo ex
              (let [data (ex-data ex)]
                (if (str/starts-with? (str (:id data)) "P15S23Q")
                  (throw ex)
                  (p15-s23-stage2-plan-emitter-fail!
                   "P15S23Q002" (:path before) nil
                   {:requested-source source-path
                    :target :jvm
                    :missing-fact :current-expression-compilation
                    :cause-diagnostic (:id data)}))))
            (catch Exception ex
              (p15-s23-stage2-plan-emitter-fail!
               "P15S23Q002" (:path before) nil
               {:requested-source source-path
                :target :jvm
                :missing-fact :current-expression-compilation
                :cause-message (.getMessage ex)})))
          functions (:functions plan)
          required-functions (:required-functions contract)
          observed-arities
          (into {}
                (map (fn [[name _]]
                       [name (select-keys (get functions name)
                                          [:arity :params])]))
                required-functions)
          missing-functions
          (set (for [[name shape] required-functions
                     :when (not= shape (get observed-arities name))]
                 name))
          source-content-hash (:source-content-hash before)
          semantic-hash (digest (p15-s23-stage2-compiler-artifact-semantic-input
                                 plan))
          functions-semantic-hash (digest functions)
          function-names-hash (digest (vec (sort-by str (keys functions))))
          required-export-semantic-hashes
          (into {}
                (map (fn [[name _]] [name (digest (get functions name))]))
                required-functions)
          artifact-hash
          (digest {:source-content-hash source-content-hash
                   :semantic-hash semantic-hash})
          source-program
          {:version 2
           :source-mode :development-current
           :source-path (:path before)
           :source-byte-count (:source-byte-count before)
           :source-content-hash source-content-hash
           :semantic-hash semantic-hash
           :functions-semantic-hash functions-semantic-hash
           :artifact-hash artifact-hash
           :function-count (count functions)
           :function-names-hash function-names-hash
           :instruction-summary (:instruction-summary plan)
           :required-functions required-functions
           :required-export-semantic-hashes required-export-semantic-hashes
           :plan-id (:plan-id plan)
           :declared-plan-compiler-labels (:compiler plan)}
          after
          (p15-s23-stage2-current-expression-development-source-snapshot!
           source-path)]
      (when-not (map? functions)
        (p15-s23-stage2-plan-emitter-fail!
         "P15S23Q002" (:path before) functions
         {:missing-fact :current-expression-function-shapes}))
      (when (seq missing-functions)
        (p15-s23-stage2-plan-emitter-fail!
         "P15S23Q002" (:path before) observed-arities
         {:requested-source source-path
          :target :jvm
          :missing-fact :current-expression-function-shape
          :missing-functions (vec (sort-by str missing-functions))}))
      (when-not (= source-content-hash (get-in plan [:source :sha256]))
        (p15-s23-stage2-plan-emitter-fail!
         "P15S23Q002" (:path before) (get-in plan [:source :sha256])
         {:requested-source source-path
          :target :jvm
          :missing-fact :current-expression-source-plan-join}))
      (when-not (= (:plan-id plan) (digest (dissoc plan :plan-id)))
        (p15-s23-stage2-plan-emitter-fail!
         "P15S23Q002" (:path before) (:plan-id plan)
         {:requested-source source-path
          :target :jvm
          :missing-fact :current-expression-recomputable-plan-id}))
      (when-not (and (= (:path before) (:path after))
                     (java.util.Arrays/equals ^bytes (:bytes before)
                                               ^bytes (:bytes after)))
        (p15-s23-stage2-plan-emitter-fail!
         "P15S23Q002" source-path nil
         {:missing-fact :stable-current-expression-source-compilation}))
      {:artifact :gravity/p15-s23-stage2-expression-lowering-binding
       :status :complete
       :source-path (:path before)
       :source-content-hash source-content-hash
       :semantic-hash semantic-hash
       :artifact-hash artifact-hash
       :functions observed-arities
       :plan plan
       :invoked? false
       :generic-bridge-residual? true
       :clojure-seed-boundary? true
       :self-hosted? false
       :source-contract-version 2
       :source-mode :development-current
       :source-program source-program
       :functions-semantic-hash functions-semantic-hash
       :function-count (count functions)
       :declared-plan-compiler-labels (:compiler plan)})))
