
(defn p15-s23-sh07-b51-resolution-plan-authority-valid?
  [authority]
  (try
    (let [content (:content authority)
          descriptor (:source-descriptor authority)
          proof (:source-object-proof authority)
          expected-id
          (p15-s23-sh07-b51-canonical-id
           [:gravity/sh07-b51-sh06-resolution-plan-authority-v1
            (dissoc authority :authority-id)])]
      (p15-s23-sh07-b51-validate-content-object-proof!
       content (:content-object-proof authority))
      (p15-s23-sh07-b51-validate-source-object-proof!
       content descriptor proof)
      (boolean
       (and (p15-s23-sh07-b51-exact-keys?
             authority p15-s23-sh07-b51-resolution-plan-authority-keys)
            (= :gravity/sh07-b51-sh06-resolution-plan-authority-v1
               (:schema authority))
            (= [[:authority-id]] (:self-id-omits authority))
            (= expected-id (:authority-id authority))
            (p15-s23-sh07-b51-resolution-plan-binding-valid?
             descriptor proof (:plan-binding authority)))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-current-sh06-resolution-binding
  [entry emitter]
  (let [state (p15-s23-sh07-b51-active-source-session)
        descriptor (:descriptor entry)
        bytes (:bytes entry)
        text (:text entry)
        content {:commit (:commit state) :tree (:tree state)}]
    (when-not
     (and (p15-s23-sh07-b51-exact-keys?
           entry #{:descriptor :bytes :text
                   :content-object-proof :source-object-proof})
          (= descriptor (nth (:descriptors state) 12 nil))
          (= entry (nth (:entries state) 12 nil))
          (identical? emitter
                      (p15-s23-sh07-b51-session-checked-product
                       :current-emitter))
          (p15-s23-sh07-b51-byte-array? bytes)
          (= text (p15-s23-sh07-b51-strict-utf8
                   bytes :resolution-plan-source-text))
          (= sh06-resolution-expected-source-byte-count
             (:byte-count descriptor) (alength ^bytes bytes))
          (= sh06-resolution-expected-source-content-hash
             (:raw-sha256 descriptor)
             (p15-s23-sh07-b51-raw-sha256 bytes)))
      (p15-s23-sh07-b51-governed-read-fail!
       :sh06-resolution-plan-source {:descriptor descriptor}))
    (p15-s23-sh07-b51-validate-content-object-proof!
     content (:content-object-proof entry))
    (let [proven-bytes
          (p15-s23-sh07-b51-validate-source-object-proof!
           content descriptor (:source-object-proof entry))]
      (when-not (p15-s23-sh07-b51-byte-array-equal? bytes proven-bytes)
        (p15-s23-sh07-b51-governed-read-fail!
         :sh06-resolution-plan-source-proof {})))
    (let [raw-plan
          (p15-s23-stage2-compiler-artifact-plan
           emitter (:path descriptor) text)
          identities (sh06-resolution-plan-identities raw-plan)
          authenticated-plan-base
          (assoc-in (dissoc raw-plan :plan-id)
                    [:source :byte-count] (:byte-count descriptor))
          plan
          (assoc authenticated-plan-base :plan-id
                 (c4-artifact-id
                  (c-backend-canonical-value authenticated-plan-base)))
          binding
          (merge
           {:source-path (:path descriptor)
            :source-byte-count (:byte-count descriptor)
            :source-content-hash (:raw-sha256 descriptor)
            :artifact :gravity/sh06-pinned-resolution-plan-binding
            :status :complete
            :semantic-authority :gravity-source
            :compiled-by :clojure-stage0-seed
            :executed-by :clojure-stage2-generic-rule-runner
            :generic-bridge-residual? true
            :self-hosted? false
            :plan plan}
           identities)
          authority-base
          {:schema :gravity/sh07-b51-sh06-resolution-plan-authority-v1
           :content content
           :content-object-proof (:content-object-proof entry)
           :source-descriptor descriptor
           :source-object-proof (:source-object-proof entry)
           :plan-binding binding
           :self-id-omits [[:authority-id]]}
          authority
          (assoc authority-base :authority-id
                 (p15-s23-sh07-b51-canonical-id
                  [:gravity/sh07-b51-sh06-resolution-plan-authority-v1
                   authority-base]))]
      (when-not (p15-s23-sh07-b51-resolution-plan-authority-valid?
                 authority)
        (p15-s23-sh07-b51-governed-read-fail!
         :sh06-resolution-plan-authority {}))
      authority)))

(defn p15-s23-sh07-b51-build-module-transaction
  [commit tree module-ordinal source c5-preimage resolution-plan-authority]
  (let [descriptor (:source-descriptor source)
        source-path (:path descriptor)
        source-text (:text source)
        project-context
        (p15-s23-sh07-b51-project-context commit tree source-path)
        c2-artifact
        (compiler-c2-reader-source-artifact
         source-path source-text project-context)
        c3-artifact
        (compiler-c3-syntax-source-artifact
         source-path source-text project-context c2-artifact
         sh03-reader-internal-product-authority)
        sh05-artifact
        (sh05-macro-artifact-from-c3-artifact*
         source-path c3-artifact true)
        base-request (sh06-resolution-request source-path sh05-artifact)
        c5-authority
        (p15-s23-sh07-b51-c5-external-binding-authority
         module-ordinal source sh05-artifact base-request c5-preimage)
        sh06-context
        (p15-s23-sh07-b51-sh06-context
         c5-authority resolution-plan-authority)
        resolution-artifact
        (sh06-resolution-artifact-from-sh05-artifact
         source-path sh05-artifact sh06-context)
        resolution-verification
        (sh06-resolution-artifact-verification
         resolution-artifact sh06-context)
        request
        (sh07-core-authenticated-request resolution-artifact sh06-context)
        raw (sh07-core-execute!
             source-path 'sh07-build-core-template [request])
        template (:core-template raw)
        digest-requests (:digest-requests raw)
        template-verification
        (sh07-core-execute!
         source-path 'sh07-verify-core-template
         [request template digest-requests])
        resolved-digests
        (sh07-core-digest-requests source-path digest-requests)
        resolution
        (sh07-core-execute!
         source-path 'sh07-resolve-core-template
         [template resolved-digests])
        resolved (:value resolution)
        bad-resolved (assoc-in resolved [:module :namespace] 'gravity.altered)
        pass-actual
        (sh07-core-execute!
         source-path 'sh07-verify-core-resolved
         [request resolved digest-requests resolved-digests])
        reject-actual
        (sh07-core-execute!
         source-path 'sh07-verify-core-resolved
         [request bad-resolved digest-requests resolved-digests])
        form-ids (mapv :form-id (:forms request))
        node-form-ids (mapv :form-id (:nodes resolved))]
    (when-not
     (and (= module-ordinal (:module-ordinal descriptor))
          (= source-path (:path descriptor))
          (= (:raw-sha256 descriptor)
             (get-in c2-artifact [:source-unit-record :bytes-hash]))
          (= (:project-root-id project-context)
             (get-in c2-artifact [:source-unit-record :project-root]))
          (= {:path nil :project-root-id (:project-root-id project-context)}
             (get-in c2-artifact [:source-unit-record :project-root-record]))
          (= :passed (:status resolution-verification))
          (= base-request
             (sh06-resolution-request source-path sh05-artifact))
          (= (:authority-id c5-authority)
             (get-in sh06-context
                     [:external-binding-authority :authority-id]))
          (= (:authority-id resolution-plan-authority)
             (get-in sh06-context
                     [:resolution-plan-authority :authority-id]))
          (= source-path (get-in request [:provenance :actual-source-path]))
          (= :accepted (:status raw))
          (= :passed (:status template-verification))
          (= :accepted (:status resolution))
          (= p15-s23-sh07-b51-resolved-pass pass-actual)
          (= p15-s23-sh07-b51-resolved-reject reject-actual)
          (not= 'gravity.altered (get-in resolved [:module :namespace]))
          (empty? (:diagnostics resolved))
          (= (count (:nodes resolved)) (count (:forms request)))
          (= (count form-ids) (count (set form-ids)))
          (= (set form-ids) (set node-form-ids))
          (= (count node-form-ids) (count (set node-form-ids)))
          (<= (count digest-requests) (+ (count (:forms request)) 4)))
      (p15-s23-sh07-b51-governed-read-fail!
       :module-transaction
       {:module-ordinal module-ordinal
        :resolution-status (:status resolution-verification)
        :template-status (:status raw)
        :template-verification (:status template-verification)
        :resolution (:status resolution)}))
    {:module-ordinal module-ordinal
     :source-descriptor descriptor
     :resolution-artifact resolution-artifact
     :c2-reader-artifact c2-artifact
     :authenticated-core-request request
     :resolved-digests resolved-digests
     :module-core (:nodes resolved)
     :template-carrier template
     :generated-digest-carrier digest-requests
     :resolved-core-carrier resolved
     :module-digest-requests digest-requests}))

(defn- p15-s23-sh07-b51-source-authority-product-from-m0-m1
  [descriptor link-manifest]
  {:artifact :gravity/sh07-compiler-source-authority-v2
   :manifest-id (:manifest-id link-manifest)
   :module-source-id (:module-source-id descriptor)
   :logical-source-path (:logical-source-path descriptor)
   :source-sha256 (:source-sha256 descriptor)
   :module (:module descriptor)
   :function-source-hashes
   (into (sorted-map)
         (map (juxt :name :source-hash))
         (:definitions descriptor))})
