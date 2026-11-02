; Semantic decomposition of committed HEAD reader line 150856.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh05-macro-artifact-verification*-provenance
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
     resolved-passed?
     runs
     input-forms
     input-syntax
     input-module
     expected-expanded-stream
     expected-trace
     first-run
     first-run-shortcuts-current?
     run-count-current?
     run-storage-exact?
     plan-binding-current?
     pinned-version?
     grants-current?
     trace-current?
     output-current?
     generated-origins-current?
     input-lineage-current?]}
   state
   c3-syntax-by-id
   (into {} (map (juxt :syntax/id identity) (:syntax-object-stream c3-artifact)))
   expected-reader-binding
   (:reader-semantic-binding c3-boundary)
   expected-reader-revision
   (:reader-source-revision c3-boundary)
   expected-definition-span
   {:source sh05-macro-source-relative-path,
    :byte-start 0,
    :byte-end sh05-macro-expected-source-byte-count}
   run-provenance-current?
   (every?
    (fn
     [run]
     (let
      [raw-expansion
       (get-in run [:raw-template-result :expansion-template])
       resolved
       (:resolved-expansion run)
       input-id
       (:input-syntax-id resolved)
       input-syntax
       (get c3-syntax-by-id input-id)
       raw-provenance
       (:provenance raw-expansion)
       resolved-provenance
       (:provenance resolved)
       digest-provenance
       (get-in run [:digest-requests 1 :preimage :provenance])]
      (and
       input-syntax
       (=
        source-path
        (:actual-source-path raw-provenance)
        (:actual-source-path resolved-provenance)
        (:actual-source-path digest-provenance))
       (=
        (:span input-syntax)
        (:call-site-span raw-provenance)
        (:call-site-span resolved-provenance)
        (:call-site-span digest-provenance))
       (=
        (:origin input-syntax)
        (:input-origin-chain raw-provenance)
        (:input-origin-chain resolved-provenance)
        (:input-origin-chain digest-provenance))
       (=
        expected-definition-span
        (:definition-span raw-provenance)
        (:definition-span resolved-provenance)
        (:definition-span digest-provenance))
       (=
        expected-reader-binding
        (:reader-binding raw-expansion)
        (:reader-binding resolved)
        (:reader-binding raw-provenance)
        (:reader-binding resolved-provenance)
        (:reader-binding digest-provenance))
       (=
        expected-reader-revision
        (:reader-source-revision raw-expansion)
        (:reader-source-revision resolved)
        (:reader-source-revision raw-provenance)
        (:reader-source-revision resolved-provenance)
        (:reader-source-revision digest-provenance)))))
    runs)
   provenance-current?
   (and
    (= source-path (get-in artifact [:provenance :source-path]))
    (=
     (get-in c3-artifact [:c2-reader-artifact :source-unit-record :source-id])
     (get-in artifact [:provenance :source-id])))
   graph-valid?
   (sh05-expanded-graph-valid? (:expanded-syntax-graph artifact))
   graph-current?
   (=
    (:expanded-syntax-graph artifact)
    (sh05-expanded-graph (:expanded-syntax-stream artifact) (:macro-expansion-trace artifact)))
   exact-shape?
   (and (= sh05-macro-artifact-keys (set (keys artifact))) template-passed? resolved-passed?)
   artifact-id-current?
   (= (:artifact-id artifact) (sh05-macro-artifact-id artifact))]
  (clojure.core/assoc
   state
   :c3-syntax-by-id
   c3-syntax-by-id
   :expected-reader-binding
   expected-reader-binding
   :expected-reader-revision
   expected-reader-revision
   :expected-definition-span
   expected-definition-span
   :run-provenance-current?
   run-provenance-current?
   :provenance-current?
   provenance-current?
   :graph-valid?
   graph-valid?
   :graph-current?
   graph-current?
   :exact-shape?
   exact-shape?
   :artifact-id-current?
   artifact-id-current?)))
