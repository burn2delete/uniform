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
