; Semantic decomposition of committed HEAD reader line 150856.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh05-macro-artifact-verification*-expansion-replay
 [state]
 (clojure.core/let
  [{:keys
    [artifact
     allow-missing-proof?
     source-path
     boundary
     c3-artifact
     c3-boundary
     gravity-verifiers
     output-envelope-ok?
     binding
     embedded-descriptor
     expected-descriptor
     descriptor-current?
     sh03-current?
     sh04-current?
     template-passed?
     resolved-passed?]}
   state
   runs
   (:expansion-runs boundary)
   input-forms
   (get-in c3-artifact [:c2-reader-artifact :parsed-semantic-values])
   input-syntax
   (sh05-source-syntax-stream c3-artifact)
   input-module
   (parse-module source-path input-forms)
   expected-expanded-stream
   (loop
    [forms input-forms syntaxes input-syntax remaining-runs runs result []]
    (if-let
     [form (first forms)]
     (let
      [syntax (first syntaxes) run (when (sh05-defn-form? form) (first remaining-runs))]
      (recur
       (rest forms)
       (rest syntaxes)
       (if run (rest remaining-runs) remaining-runs)
       (conj result (sh05-expanded-syntax-object syntax form run))))
     (when (and (empty? syntaxes) (empty? remaining-runs)) result)))
   expected-trace
   (mapv
    (fn* [p1__1275#] (sh05-package-trace p1__1275# (:profile input-module) (:target input-module)))
    runs)
   first-run
   (first runs)
   first-run-shortcuts-current?
   (if
    first-run
    (=
     (select-keys
      boundary
      [:raw-template-result :resolved-expansion :digest-requests :resolved-digests])
     (select-keys
      first-run
      [:raw-template-result :resolved-expansion :digest-requests :resolved-digests]))
    (every?
     nil?
     ((juxt :raw-template-result :resolved-expansion :digest-requests :resolved-digests)
      boundary)))
   run-count-current?
   (= (:expanded-defn-count artifact) (count runs))
   run-storage-exact?
   (every?
    (fn
     [run]
     (and
      (=
       #{:template-verification
         :raw-template-result
         :resolved-expansion
         :digest-requests
         :resolved-digests
         :resolved-verification}
       (set (keys run)))
      (=
       #{:schema-version :status :artifact :expansion-template}
       (set (keys (:raw-template-result run))))
      (= #{:rule :schema-version :status :artifact} (set (keys (:template-verification run))))
      (= #{:rule :schema-version :status :artifact} (set (keys (:resolved-verification run))))))
    runs)
   plan-binding-current?
   (=
    (select-keys
     (:plan-binding boundary)
     [:source-byte-count
      :source-content-hash
      :plan-semantic-hash
      :functions-semantic-hash
      :function-count
      :function-names-hash
      :function-shapes-hash
      :public-function-hashes
      :public-function-shapes])
    (select-keys
     binding
     [:source-byte-count
      :source-content-hash
      :plan-semantic-hash
      :functions-semantic-hash
      :function-count
      :function-names-hash
      :function-shapes-hash
      :public-function-hashes
      :public-function-shapes]))
   pinned-version?
   (every?
    (fn*
     [p1__1276#]
     (= sh05-macro-version (get-in p1__1276# [:resolved-expansion :macro-version])))
    runs)
   grants-current?
   (every?
    (fn
     [run]
     (let
      [log (get-in run [:resolved-expansion :build-effect-log])]
      (and
       (set/subset? (set (:requested log)) (set (:declared log)))
       (set/subset? (set (:declared log)) (set (:granted log))))))
    runs)
   trace-current?
   (and
    template-passed?
    resolved-passed?
    (= expected-trace (:macro-expansion-trace artifact))
    (=
     (:macro-expansion-trace-id artifact)
     (sh05-macro-trace-id source-path (:macro-expansion-trace artifact))))
   output-current?
   (and
    template-passed?
    resolved-passed?
    (= expected-expanded-stream (:expanded-syntax-stream artifact))
    (= (:expanded-defn-count artifact) (count runs))
    (= (:expanded-forms artifact) (mapv :form (subvec (:expanded-syntax-stream artifact) 1)))
    (=
     (:expanded-syntax-stream-id artifact)
     (sh05-expanded-syntax-stream-id source-path (:expanded-syntax-stream artifact))))
   generated-origins-current?
   (=
    (:generated-origin-source-map artifact)
    (mapv
     (fn* [p1__1277#] (select-keys p1__1277# [:output-syntax-id :generated-origin]))
     (:macro-expansion-trace artifact)))
   input-lineage-current?
   (= (get-in artifact [:sh04-syntax-artifact :artifact-id]) (:artifact-id c3-artifact))]
  (clojure.core/assoc
   state
   :runs
   runs
   :input-forms
   input-forms
   :input-syntax
   input-syntax
   :input-module
   input-module
   :expected-expanded-stream
   expected-expanded-stream
   :expected-trace
   expected-trace
   :first-run
   first-run
   :first-run-shortcuts-current?
   first-run-shortcuts-current?
   :run-count-current?
   run-count-current?
   :run-storage-exact?
   run-storage-exact?
   :plan-binding-current?
   plan-binding-current?
   :pinned-version?
   pinned-version?
   :grants-current?
   grants-current?
   :trace-current?
   trace-current?
   :output-current?
   output-current?
   :generated-origins-current?
   generated-origins-current?
   :input-lineage-current?
   input-lineage-current?)))
