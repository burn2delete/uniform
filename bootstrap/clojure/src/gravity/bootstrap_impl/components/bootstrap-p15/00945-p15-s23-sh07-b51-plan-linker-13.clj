
(defn- p15-s23-sh07-b51-authenticated-c5-request-adapter
  [module-ordinal source descriptors manifest sh05-artifact
   base-request-function]
  (when-not (p15-s23-sh07-b51-link-manifest-valid? descriptors manifest)
    (p15-s23-sh07-b51-c5-authority-fail!
     :unverified-m0-m1
     {:module-ordinal module-ordinal}))
  (let [authority-rows
        (p15-s23-sh07-b51-c5-authority-rows*
         module-ordinal source descriptors manifest)
        caller (nth descriptors module-ordinal)
        base-request
        (base-request-function (:path source) sh05-artifact)
        external-symbols (set (map #(get-in % [:external :symbol])
                                   authority-rows))
        base-binding-symbols
        (set (concat (map :name (:definitions base-request))
                     (map :name (:import-bindings base-request))))]
    (when-not
     (and (= (:module caller) (get-in base-request [:module :namespace]))
          (= (:artifact-id sh05-artifact)
             (get-in base-request [:module :c4-artifact-id]))
          (empty? (clojure.set/intersection external-symbols
                                            base-binding-symbols)))
      (p15-s23-sh07-b51-c5-authority-fail!
       :unverified-m0-m1-or-caller-request
       {:module-ordinal module-ordinal
        :external-symbols (vec (sort-by str external-symbols))}))
    (merge
     {:source-path (:path source)
      :sh05-artifact-id (:artifact-id sh05-artifact)
      :base-request-id
      (p15-s23-sh07-b51-canonical-id
       [:gravity/sh07-b51-sh06-base-request-v1 base-request])
      :caller-module-source-id (:module-source-id caller)
      :manifest-id (:manifest-id manifest)}
     (p15-s23-sh07-b51-c5-request-carriers
      caller manifest authority-rows))))

(defn- p15-s23-sh07-b51-apply-c5-request-adapter
  [adapter source-path sh05-artifact base-request]
  (let [imports (:imports adapter)
        import-bindings (:import-bindings adapter)
        dependency-edges (:dependency-edges adapter)
        manifest-id (:manifest-id adapter)
        caller-id (:caller-module-source-id adapter)
        referred (vec (mapcat :refer imports))
        binding-names (mapv :name import-bindings)
        expected-dependency-edges
        (mapv
         (fn [dependency]
           {:from (get-in base-request [:module :namespace])
            :to (:namespace dependency)
            :kind :namespace
            :boundary (:boundary dependency)
            :semantic-span (:semantic-span dependency)
            :source-span (:source-span dependency)})
         imports)]
    (when-not
     (and (p15-s23-sh07-b51-exact-keys?
           adapter
           #{:source-path :sh05-artifact-id :base-request-id
             :caller-module-source-id :manifest-id :imports
             :import-bindings :dependency-edges})
          (= source-path (:source-path adapter))
          (= (:artifact-id sh05-artifact) (:sh05-artifact-id adapter))
          (= (:base-request-id adapter)
             (p15-s23-sh07-b51-canonical-id
              [:gravity/sh07-b51-sh06-base-request-v1 base-request]))
          (p15-s23-sh07-b51-digest-id? caller-id)
          (p15-s23-sh07-b51-digest-id? manifest-id)
          (vector? imports) (vector? import-bindings)
          (vector? dependency-edges)
          (= dependency-edges expected-dependency-edges)
          (= (set referred) (set binding-names))
          (= (count referred) (count (set referred)))
          (= (count binding-names) (count (set binding-names)))
          (every?
           #(and (= manifest-id (get-in % [:semantic-span :manifest-id]))
                 (= caller-id
                    (get-in % [:semantic-span
                               :caller-module-source-id])))
           imports)
          (every?
           #(and (= manifest-id (get-in % [:semantic-span :manifest-id]))
                 (= caller-id
                    (get-in % [:semantic-span
                               :caller-module-source-id])))
           import-bindings))
      (p15-s23-sh07-b51-c5-authority-fail!
       :request-replay-mismatch
       {:source-path source-path
        :expected-source-path (:source-path adapter)}))
    (-> base-request
        (update :imports into imports)
        (update :import-bindings into import-bindings)
        (update :dependency-edges into dependency-edges))))

(defn- p15-s23-sh07-b51-c5-scoped-request-function
  [adapter base-request-function]
  (fn [source-path candidate-sh05-artifact]
    (let [base-request
          (base-request-function source-path candidate-sh05-artifact)]
      (if (and (= source-path (:source-path adapter))
               (= (:artifact-id candidate-sh05-artifact)
                  (:sh05-artifact-id adapter))
               (= (:base-request-id adapter)
                  (p15-s23-sh07-b51-canonical-id
                   [:gravity/sh07-b51-sh06-base-request-v1 base-request])))
        (p15-s23-sh07-b51-apply-c5-request-adapter
         adapter source-path candidate-sh05-artifact base-request)
        base-request))))

(defn- p15-s23-sh07-b51-session-checked-core-binding
  [emitter]
  (let [{:keys [descriptor bytes text]}
        (p15-s23-sh07-b51-session-checked-product :checked-core-entry)
        source-path (:path descriptor)
        source-hash (p15-s23-sh07-b51-raw-sha256 bytes)
        plan (p15-s23-stage2-compiler-artifact-plan
              emitter source-path text)
        identities (sh07-core-plan-identities plan)]
    (when-not
     (and (= sh07-core-expected-source-byte-count (alength bytes))
          (= sh07-core-expected-source-content-hash source-hash)
          (= sh07-core-expected-plan-semantic-hash
             (:plan-semantic-hash identities))
          (= sh07-core-expected-functions-semantic-hash
             (:functions-semantic-hash identities))
          (= sh07-core-expected-function-count (:function-count identities))
          (= sh07-core-expected-function-names-hash
             (:function-names-hash identities))
          (= sh07-core-expected-function-shapes-hash
             (:function-shapes-hash identities))
          (= sh07-core-public-function-hashes
             (:public-function-hashes identities))
          (= sh07-core-public-function-shapes
             (:public-function-shapes identities)))
      (p15-s23-sh07-b51-governed-read-fail!
       :checked-core-binding
       {:source-path source-path :source-hash source-hash}))
    (merge
     {:artifact :gravity/sh07-pinned-core-plan-binding
      :source-path source-path
      :source-byte-count (alength bytes)
      :source-content-hash source-hash
      :plan plan
      :semantic-authority :gravity-source
      :compiled-by :clojure-stage0-seed
      :executed-by :clojure-stage2-generic-rule-runner
      :generic-bridge-residual? true
      :self-hosted? false}
     identities)))

(defn- p15-s23-sh07-b51-resolution-plan-binding-valid?
  [descriptor source-object-proof binding]
  (try
    (let [bytes
          (p15-s23-sh07-b51-unsigned-byte-array
           (:blob-bytes source-object-proof) :resolution-plan-source)
          identities (sh06-resolution-plan-identities (:plan binding))
          plan-base (dissoc (:plan binding) :plan-id)
          expected-plan-id
          (c4-artifact-id (c-backend-canonical-value plan-base))]
      (boolean
       (and
        (p15-s23-sh07-b51-exact-keys?
         binding p15-s23-sh07-b51-resolution-plan-binding-keys)
        (= {:ordinal 12 :kind :sh06-resolution :module-ordinal nil
            :path sh06-resolution-source-relative-path
            :mode "100644"
            :blob-oid "16ef2b638298a8e934fcd0b39131072305fbcfd5"
            :byte-count sh06-resolution-expected-source-byte-count
            :raw-sha256 sh06-resolution-expected-source-content-hash}
           descriptor)
        (= (:source-path binding) (:path descriptor))
        (= (:source-byte-count binding) (:byte-count descriptor)
           (alength ^bytes bytes))
        (= (:source-content-hash binding) (:raw-sha256 descriptor)
           (p15-s23-sh07-b51-raw-sha256 bytes))
        (= :gravity/sh06-pinned-resolution-plan-binding (:artifact binding))
        (= :complete (:status binding))
        (= :gravity-source (:semantic-authority binding))
        (= :clojure-stage0-seed (:compiled-by binding))
        (= :clojure-stage2-generic-rule-runner (:executed-by binding))
        (true? (:generic-bridge-residual? binding))
        (false? (:self-hosted? binding))
        (= :gravity/stage2-compiler-artifact-plan
           (get-in binding [:plan :kind]))
        (true? (get-in binding [:plan :compiler-artifact-plan?]))
        (= 'gravity.resolution (get-in binding [:plan :module :module]))
        (= :meta (get-in binding [:plan :module :profile]))
        (= :jvm (get-in binding [:plan :module :target]))
        (= #{} (get-in binding [:plan :module :effects]))
        (= #{} (get-in binding [:plan :module :capabilities]))
        (= :safe (get-in binding [:plan :module :safety]))
        (= (:byte-count descriptor) (get-in binding [:plan :source :byte-count]))
        (= expected-plan-id (get-in binding [:plan :plan-id])
           sh06-resolution-expected-plan-id)
        (= (select-keys binding
                        [:plan-semantic-hash :functions-semantic-hash
                         :function-count :function-names-hash
                         :function-shapes-hash :public-function-hashes
                         :public-function-shapes])
           identities)
        (= sh06-resolution-expected-plan-semantic-hash
           (:plan-semantic-hash identities))
        (= sh06-resolution-expected-functions-semantic-hash
           (:functions-semantic-hash identities))
        (= sh06-resolution-expected-function-count
           (:function-count identities))
        (= sh06-resolution-expected-function-names-hash
           (:function-names-hash identities))
        (= sh06-resolution-expected-function-shapes-hash
           (:function-shapes-hash identities))
        (= sh06-resolution-public-function-hashes
           (:public-function-hashes identities))
        (= sh06-resolution-public-function-shapes
           (:public-function-shapes identities)))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))
