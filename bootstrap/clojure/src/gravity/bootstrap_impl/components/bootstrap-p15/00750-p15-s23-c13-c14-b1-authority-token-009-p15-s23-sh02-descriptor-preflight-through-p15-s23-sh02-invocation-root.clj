(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn- p15-s23-sh02-descriptor-preflight!
  [source-path descriptor]
  ;; Descriptor ingress is public and may be supplied by a later bootstrap
  ;; stage. Establish exact, metadata-free host carriers before any `count`,
  ;; `set`, `distinct`, tree walk, or reference-graph traversal.
  (p15-s23-sh02-require-bounded-carrier!
   source-path :sh02-descriptor descriptor)
  (let [bounds p15-s23-sh02-authenticated-envelope-bounds
        closure (:reference-closure descriptor)
        node-ids (:node-ids closure)
        edges (:edges closure)
        reachable
        (when (and (map? closure) (vector? edges))
          (p15-s23-sh02-reference-reachable-ids
           (:root-id closure) edges))
        logical-path (get-in descriptor
                             [:source-revision :logical-source-path])]
    (when-not
     (and (= bounds (:bounds descriptor))
          (vector? (:semantic-projections descriptor))
          (<= (count (:semantic-projections descriptor))
              (:maximum-semantic-projections bounds))
          (vector? (:fact-transitions descriptor))
          (<= (count (:fact-transitions descriptor))
              (:maximum-fact-transitions bounds))
          (vector? (:identity-subjects descriptor))
          (<= (count (:identity-subjects descriptor))
              (:maximum-identity-subjects bounds))
          (vector? (:lineage descriptor))
          (<= (count (:lineage descriptor))
              (:maximum-lineage-records bounds))
          (string? logical-path)
          (<= (count logical-path)
              (:maximum-logical-source-path-code-units bounds))
          (map? closure)
          (vector? node-ids)
          (vector? edges)
          (= (count node-ids) (:observed-node-count closure))
          (= (count edges) (:observed-edge-count closure))
          (= (count node-ids) (count (distinct node-ids)))
          (= (count edges) (count (distinct edges)))
          (= (set node-ids) reachable)
          (every? (fn [edge]
                    (and (map? edge)
                         (= #{:from :role :to} (set (keys edge)))
                         (contains? reachable (:from edge))
                         (contains? reachable (:to edge))))
                  edges)
          (<= (count node-ids) (:maximum-reference-nodes bounds))
          (<= (count edges) (:maximum-reference-edges bounds))
          (integer? (:observed-maximum-depth closure))
          (<= 0 (:observed-maximum-depth closure)
              (:maximum-reference-depth bounds))
          (= (:observed-maximum-depth closure)
             (p15-s23-sh02-reference-depth (:root-id closure) edges))
          (not-any? p15-s23-c6c10-digest-ref-shape?
                    (p15-s23-sh02-contained-values descriptor)))
      (p15-s23-sh02-fail!
       source-path descriptor :bounded-sh02-descriptor-subset
       {:stage (:stage descriptor)
        :observed-semantic-projections
        (when (vector? (:semantic-projections descriptor))
          (count (:semantic-projections descriptor)))
        :observed-fact-transitions
        (when (vector? (:fact-transitions descriptor))
          (count (:fact-transitions descriptor)))
        :observed-identity-subjects
        (when (vector? (:identity-subjects descriptor))
          (count (:identity-subjects descriptor)))
        :observed-reference-nodes
        (when (vector? node-ids) (count node-ids))
        :observed-reference-edges
        (when (vector? edges) (count edges))
        :observed-reference-depth (:observed-maximum-depth closure)
        :maximum-reference-nodes (:maximum-reference-nodes bounds)
        :maximum-reference-edges (:maximum-reference-edges bounds)
        :maximum-reference-depth (:maximum-reference-depth bounds)}))
    descriptor))

(defn- p15-s23-sh02-build-stage-envelope!
  [candidate stage packet descriptor binding source-path]
  (p15-s23-sh02-descriptor-preflight! source-path descriptor)
  (let [raw-result
        (p15-s23-c13-c14-b1-invoke!
         candidate source-path binding
         'authenticated-envelope-build-template
         [descriptor] "B1-METADATA")
        resolved
        (p15-s23-sh02-resolve-builder-result!
         source-path descriptor raw-result)
        verifier-result
        (p15-s23-c13-c14-b1-invoke!
         candidate source-path binding
         'authenticated-envelope-verify-template
         [descriptor (:artifact-template raw-result)
          (:digest-requests raw-result)]
         "B1-METADATA")
        _ (p15-s23-sh02-validate-verifier-result!
           source-path raw-result verifier-result)
        stage-envelope
        {:artifact :gravity/sh02-stage-authenticated-envelope
         :schema-version 1 :status :accepted
         :stage stage
         :sealed-artifact (:sealed-artifact resolved)
         :semantic-envelope-id (:semantic-envelope-id resolved)
         :provenance-binding-id (:provenance-binding-id resolved)
         :identity-checks (:identity-checks resolved)
         :request-count (:request-count resolved)
         :request-graph-id (:request-graph-id resolved)
         :gravity-template-replay
         (select-keys
          verifier-result p15-s23-sh02-template-replay-summary-keys)
         :source-rule (p15-s23-sh02-source-rule binding)
         :diagnostics []
         :semantic-authority :gravity-source
         :host-tcb
         {:carrier-validation :clojure-stage0
          :canonical-encoding :clojure-stage0
          :sha256 :clojure-stage0
          :digest-graph-resolution :clojure-stage0
          :template-instantiation :clojure-stage0
          :fresh-contextual-replay :clojure-stage0}
         :self-hosted? false}]
    (when-not
     (and (= p15-s23-sh02-stage-envelope-keys
             (set (keys stage-envelope)))
          (= stage (:stage stage-envelope))
          (= (:artifact (get packet stage))
             (get-in stage-envelope [:sealed-artifact :artifact-kind])))
      (p15-s23-sh02-fail!
       source-path stage-envelope :exact-sh02-stage-envelope
       {:stage stage}))
    stage-envelope))

(def p15-s23-sh02-final-artifact-keys
  #{:artifact :schema-version :status :packet-id :packet-semantic-id
    :envelopes :source-rule :actual-path-provenance :diagnostics
    :semantic-authority :host-tcb :scope :semantic-id :artifact-id
    :actual-path-binding-id})

(def p15-s23-sh02-final-scope
  {:reusable-envelope? true
   :stages [:c13 :b1]
   :semantic-root-path-neutral? true
   :physical-provenance-separate? true
   :whole-compiler? false
   :public-release? false
   :self-hosted? false})

(defn p15-s23-sh02-final-semantic-input
  [artifact]
  {:artifact :gravity/sh02-reusable-authenticated-envelopes
   :schema-version 1
   :packet-id (:packet-id artifact)
   :packet-semantic-id (:packet-semantic-id artifact)
   :envelopes
   (into
    (sorted-map)
    (map
     (fn [[stage envelope]]
       [stage
        {:stage (:stage envelope)
         :artifact-kind (get-in envelope [:sealed-artifact :artifact-kind])
         :semantic-envelope-id (:semantic-envelope-id envelope)}]))
    (:envelopes artifact))
   :source-rule (:source-rule artifact)
   :semantic-authority (:semantic-authority artifact)
   :host-tcb (:host-tcb artifact)
   :scope (:scope artifact)})

(defn- p15-s23-sh02-final-semantic-id
  [artifact]
  (p15-s23-c6c10-canonical-digest
   "<sh02-final-artifact>"
   {:domain :gravity/sh02-reusable-envelope-set-v1
    :semantic-input (p15-s23-sh02-final-semantic-input artifact)}))

(defn- p15-s23-sh02-final-artifact-id
  [semantic-id]
  (p15-s23-c6c10-canonical-digest
   "<sh02-final-artifact>"
   {:domain :gravity/sh02-reusable-envelope-artifact-v1
    :schema-version 1 :semantic-id semantic-id}))

(defn- p15-s23-sh02-final-actual-path-binding-id
  [semantic-id actual-path-provenance]
  (p15-s23-c6c10-canonical-digest
   "<sh02-final-artifact>"
   {:domain :gravity/sh02-reusable-envelope-provenance-v1
    :semantic-id semantic-id
    :actual-path-provenance actual-path-provenance}))

(defn- p15-s23-sh02-workspace-root
  [candidate source-path]
  (let [deps-path
        (p15-s23-c13-c14-b1-resolve-source-path
         candidate source-path "deps.edn")]
    (.getCanonicalPath (.getParentFile (java.io.File. deps-path)))))

(defn- p15-s23-sh02-invocation-root
  []
  (.getCanonicalPath (java.io.File. (System/getProperty "user.dir")))))
