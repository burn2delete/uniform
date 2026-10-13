
(defn- sh06-resolution-plan-authority-binding!
  [source-path context]
  (when-not
   (and (some? context)
        (sh06-resolution-exact-keys?
         context #{:external-binding-authority :resolution-plan-authority}))
    (sh06-resolution-c5-fail! source-path :missing-or-malformed-context {}))
  (let [authority (:resolution-plan-authority context)
        content (:content authority)
        descriptor (:source-descriptor authority)
        binding (:plan-binding authority)]
    (when-not
     (and
      (sh06-resolution-exact-keys?
       authority #{:schema :content :content-object-proof :source-descriptor
                   :source-object-proof :plan-binding :self-id-omits
                   :authority-id})
      (= :gravity/sh07-b51-sh06-resolution-plan-authority-v1
         (:schema authority))
      (= [[:authority-id]] (:self-id-omits authority))
      (sh06-resolution-sha256-id? (:authority-id authority))
      (= (:authority-id authority)
         (reader-canonical-hash
          [:gravity/sh07-b51-sh06-resolution-plan-authority-v1
           (dissoc authority :authority-id)]))
      (sh06-resolution-exact-keys?
       descriptor #{:ordinal :kind :module-ordinal :path :mode :blob-oid
                    :byte-count :raw-sha256})
      (= 12 (:ordinal descriptor))
      (= :sh06-resolution (:kind descriptor))
      (nil? (:module-ordinal descriptor))
      (= sh06-resolution-source-relative-path (:path descriptor))
      (= sh06-resolution-expected-source-byte-count (:byte-count descriptor))
      (= sh06-resolution-expected-source-content-hash
         (:raw-sha256 descriptor))
      (sh06-resolution-exact-keys?
       binding #{:source-path :source-byte-count :source-content-hash
                 :plan-semantic-hash :functions-semantic-hash :function-count
                 :function-names-hash :function-shapes-hash
                 :public-function-hashes :public-function-shapes :artifact
                 :status :semantic-authority :compiled-by :executed-by
                 :generic-bridge-residual? :self-hosted? :plan})
      (= sh06-resolution-source-relative-path (:source-path binding))
      (= sh06-resolution-expected-source-byte-count
         (:source-byte-count binding))
      (= sh06-resolution-expected-source-content-hash
         (:source-content-hash binding))
      (= :gravity/sh06-pinned-resolution-plan-binding (:artifact binding))
      (= :complete (:status binding))
      (= :gravity-source (:semantic-authority binding))
      (= :clojure-stage0-seed (:compiled-by binding))
      (= :clojure-stage2-generic-rule-runner (:executed-by binding))
      (true? (:generic-bridge-residual? binding))
      (false? (:self-hosted? binding)))
     (sh06-resolution-c5-fail! source-path :resolution-plan-authority-shape {}))
    (sh06-resolution-content-proof-tree!
     source-path content (:content-object-proof authority))
    (let [bytes (sh06-resolution-source-proof-bytes!
                 sh06-resolution-source-relative-path content descriptor
                 (:source-object-proof authority))
          plan (:plan binding)
          plan-base (dissoc plan :plan-id)
          expected-plan-id
          (c4-artifact-id (c-backend-canonical-value plan-base))
          identities (sh06-resolution-plan-identities plan)]
      (when-not
       (and (= sh06-resolution-expected-source-byte-count (count bytes))
            (= sh06-resolution-expected-source-content-hash
               (str "sha256:"
                    (sha256-bytes-hex
                     (sh06-resolution-byte-array bytes))))
            (= :gravity/stage2-compiler-artifact-plan (:kind plan))
            (true? (:compiler-artifact-plan? plan))
            (= 'gravity.resolution (get-in plan [:module :module]))
            (= :meta (get-in plan [:module :profile]))
            (= :jvm (get-in plan [:module :target]))
            (= #{} (get-in plan [:module :effects]))
            (= #{} (get-in plan [:module :capabilities]))
            (= :safe (get-in plan [:module :safety]))
            (= (:byte-count descriptor) (get-in plan [:source :byte-count]))
            (= sh06-resolution-expected-plan-id
               expected-plan-id (:plan-id plan))
            (= sh06-resolution-expected-plan-semantic-hash
               (:plan-semantic-hash identities)
               (:plan-semantic-hash binding))
            (= sh06-resolution-expected-functions-semantic-hash
               (:functions-semantic-hash identities)
               (:functions-semantic-hash binding))
            (= sh06-resolution-expected-function-count
               (:function-count identities) (:function-count binding))
            (= sh06-resolution-expected-function-names-hash
               (:function-names-hash identities)
               (:function-names-hash binding))
            (= sh06-resolution-expected-function-shapes-hash
               (:function-shapes-hash identities)
               (:function-shapes-hash binding))
            (= sh06-resolution-public-function-hashes
               (:public-function-hashes identities)
               (:public-function-hashes binding))
            (= sh06-resolution-public-function-shapes
               (:public-function-shapes identities)
               (:public-function-shapes binding)))
        (sh06-resolution-c5-fail!
         source-path :resolution-plan-authority-current {})))
    binding))

(defn sh06-resolution-context-plan-binding!
  [source-path context]
  (sh06-resolution-plan-authority-binding! source-path context))
