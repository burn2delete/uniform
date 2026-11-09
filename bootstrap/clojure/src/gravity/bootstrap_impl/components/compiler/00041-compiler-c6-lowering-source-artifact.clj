

(defn- compiler-c6-lowering-source-artifact*
  [source-path source-text route repository-root]
  (when (and (= :development-current route)
             (not (or (string? repository-root)
                      (instance? java.io.File repository-root)
                      (instance? java.nio.file.Path repository-root))))
    (throw
     (ex-info
      "C6 development-current route requires an explicit repository root"
      {:id "C6-SOURCE-ADOPTION-ROOT"
       :route route
       :repository-root repository-root})))
  (source-input/with-context
   (fn []
     (let [current-invocation
           (when (= :development-current route)
             (let [root-file (if (instance? java.nio.file.Path repository-root)
                               (.toFile ^java.nio.file.Path repository-root)
                               (java.io.File. repository-root))
                   canonical-path
                   (fn [relative-path]
                     (.getCanonicalPath (java.io.File. root-file relative-path)))]
               {:reader-source-path
                (canonical-path
                 "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
                :authenticated-envelope-options {:source :development-current}
                :syntax-source-selection
                {:schema :gravity/sh04-development-syntax-selection-v1
                 :source-path
                 (canonical-path
                  "bootstrap/gravity/src/gravity/bootstrap/syntax.gravity")}
                :macro-source-mode :development-current}))
           c5-artifact (or *compiler-c6-authenticated-resolution-input*
                            (if current-invocation
                              (compiler-c5-resolution-source-artifact
                               source-path source-text
                               {:artifact :gravity/sh06-module-inputs
                                :schema-version 1
                                :module-artifacts []}
                               (:reader-source-path current-invocation)
                               current-invocation)
                              (compiler-c5-resolution-source-artifact source-path
                                                                       source-text)))
          sh06-input? (= :gravity/sh06-resolution-artifact (:kind c5-artifact))
          sh06-report (when sh06-input?
                        (if current-invocation
                          (sh06-resolution-artifact-verification
                           c5-artifact :gravity.bootstrap/legacy current-invocation)
                          (sh06-resolution-artifact-verification c5-artifact)))
          _ (when (and sh06-input? (not= :passed (:status sh06-report)))
              (c6-lowering-fail! "C6-VERIFY" source-path
                                 {:stage :core-lowering}
                                 {:missing-fields [:fresh-authenticated-sh06-resolution]}))
          expanded-stream (if sh06-input?
                            (mapv (fn [syntax]
                                    (-> syntax
                                        (assoc :syntax-id (:syntax/id syntax)
                                               :generated-origin (:origin syntax))
                                        (dissoc :origin)))
                                  (get-in c5-artifact
                                          [:sh05-macro-artifact :expanded-syntax-stream]))
                            (get-in c5-artifact
                                    [:c4-macro-expansion-artifact :expanded-syntax-stream]))
          records (when-not sh06-input?
                    (read-source-form-records source-path source-text))
          forms (mapv :form (if sh06-input? expanded-stream records))
          _ (validate-ns-syntax! source-path forms)
          module (if sh06-input?
                   (source-input/with-product-mode
                     :c6-expanded
                     #(parse-module source-path forms))
                   (parse-module source-path forms))]
       (c6-call-route route repository-root c6/c6-lowering-artifact
                      source-path module c5-artifact expanded-stream)))))

(defn compiler-c6-lowering-source-artifact
  [source-path source-text]
  (compiler-c6-lowering-source-artifact*
   source-path source-text :default nil))

(defn compiler-c6-lowering-source-artifact-development-current
  "Run C6 with current-source classifier/evaluation-order operations.

  The repository root is explicit and the source plan is rebuilt for this
  invocation.  This development route does not alter the default pinned
  two-argument entrypoint or establish migration authority."
  [source-path source-text repository-root]
  (compiler-c6-lowering-source-artifact*
   source-path source-text :development-current repository-root))
