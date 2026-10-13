
(defn- sh06-authenticated-sh05-parent!
  "Validate the complete SH-05 owner artifact before SH-06 consumes it.

  An SH-05 id by itself is not a sufficient parent edge.  This gate replays the
  owner verifier, checks the owner id and accepted/proof statuses, verifies the
  nested C3 owner, and joins source/path provenance across both artifacts.
  "
  [source-path sh05-artifact]
  (let [boundary (:gravity-macro-boundary sh05-artifact)
        c3-artifact (:authenticated-sh04-artifact boundary)
        c2-artifact (:c2-reader-artifact c3-artifact)
        source-unit (:source-unit-record c2-artifact)
        verification
        (try
          (sh05-macro-artifact-verification sh05-artifact)
          (catch InterruptedException interrupted
            (.interrupt (Thread/currentThread))
            (throw interrupted))
          (catch Exception _ {:status :failed}))
        owner-id
        (try
          (sh05-macro-artifact-id sh05-artifact)
          (catch Exception _ nil))
        c3-id
        (try
          (c3-artifact-id c3-artifact)
          (catch Exception _ nil))
        c3-verified?
        (try
          (c3-syntax-validate! source-path c3-artifact)
          true
          (catch InterruptedException interrupted
            (.interrupt (Thread/currentThread))
            (throw interrupted))
          (catch Exception _ false))
        passed?
        (and (map? sh05-artifact)
             (= :gravity/sh05-macro-expansion-artifact
                (:kind sh05-artifact))
             (= :accepted (:status sh05-artifact))
             (= :passed (:status verification))
             (= :complete
                (get-in sh05-artifact [:capability-based-proof :status]))
             (= (:artifact-id sh05-artifact) owner-id)
             (map? boundary)
             (= :SH-05 (:slice boundary))
             (= :gravity-source (:owner boundary))
             (= :accepted
                (get-in boundary [:authenticated-envelope :status]))
             (= source-path (get-in sh05-artifact [:provenance :source-path]))
             (= source-path (get-in boundary
                                    [:authenticated-envelope-descriptor
                                     :actual-path-provenance :source-path]))
             (map? c3-artifact)
             (= :gravity/stage0-c3-syntax-object-artifact (:kind c3-artifact))
             (= (:artifact-id c3-artifact) c3-id)
             c3-verified?
             (= :accepted (get-in c3-artifact
                                  [:gravity-syntax-boundary
                                   :authenticated-envelope :status]))
             (= source-path (get-in c3-artifact
                                    [:c2-reader-artifact :source-unit-record
                                     :path]))
             (= source-path (get-in c3-artifact
                                    [:gravity-syntax-boundary
                                     :authenticated-envelope-descriptor
                                     :actual-path-provenance :source-path]))
             (= source-path (:path source-unit))
             (= (:source-id source-unit)
                (get-in sh05-artifact [:provenance :source-id]))
             (= (:artifact-id c3-artifact)
                (get-in sh05-artifact [:sh04-syntax-artifact :artifact-id])))]
    (when-not passed?
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" source-path :authenticated-sh05-macro-parent
       {:source-path source-path
        :observed-source-path (get-in sh05-artifact [:provenance :source-path])
        :artifact-kind (:kind sh05-artifact)
        :artifact-status (:status sh05-artifact)
        :artifact-id (:artifact-id sh05-artifact)
        :recomputed-artifact-id owner-id
        :verification-status (:status verification)
        :c3-artifact-id (:artifact-id c3-artifact)
        :recomputed-c3-artifact-id c3-id
        :c3-verified? c3-verified?}
       {:required [:complete-sh05-artifact-identity
                   :passed-sh05-verifier
                   :complete-sh05-capability-proof
                   :authenticated-c3-parent
                   :source-path-provenance]}))
    sh05-artifact))

(defn- sh06-authenticated-source-snapshot!
  ([source-path c2-artifact phase legacy?]
   (if legacy?
     {:canonical-path source-path :byte-count nil :bytes-hash
      (get-in c2-artifact [:source-unit-record :bytes-hash])}
     (let [source-unit (:source-unit-record c2-artifact)
        expected-path (:path source-unit)
        expected-hash (:bytes-hash source-unit)
        expected-count (or (:byte-count source-unit)
                           (get-in c2-artifact
                                   [:gravity-reader-boundary
                                    :authenticated-envelope-descriptor
                                    :source-revision :source-byte-count]))
        snapshot (try
                   (c2-pass-cache/bounded-source-snapshot! source-path)
                   (catch Exception error
                     (sh06-resolution-boundary-fail!
                      "C5-UNRESOLVED" source-path phase
                      {:source-path source-path
                       :error-class (some-> error class .getName)
                       :error-data (ex-data error)}
                      {:required [:bounded-no-follow-source-snapshot]})))]
    (when-not (and (= source-path expected-path)
                   (= (:canonical-path snapshot)
                      (.getCanonicalPath (java.io.File. expected-path)))
                   (= expected-hash (:bytes-hash snapshot))
                   (or (nil? expected-count)
                       (= (long expected-count) (long (:byte-count snapshot)))))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" source-path phase
       {:source-path source-path :expected-path expected-path
        :observed-canonical-path (:canonical-path snapshot)
        :expected-bytes-hash expected-hash
        :observed-bytes-hash (:bytes-hash snapshot)
        :expected-byte-count expected-count
        :observed-byte-count (:byte-count snapshot)}
       {:required [:source-path-provenance :source-bytes-hash
                   :source-byte-count]}))
       snapshot)))
  ([source-path c2-artifact phase legacy? context]
   (let [source-unit (:source-unit-record c2-artifact)
         authority (:external-binding-authority context)
         preimage (:preimage authority)
         content (:content authority)
         descriptors (:source-descriptors preimage)
         descriptor (first (filter #(= source-path (:path %)) descriptors))
         ordinal (:module-ordinal authority)
         input (when (and (integer? ordinal) (<= 0 ordinal 6))
                 (nth (:module-source-inputs preimage) ordinal nil))
         expected-count
         (or (:byte-count source-unit)
             (get-in c2-artifact
                     [:gravity-reader-boundary
                      :authenticated-envelope-descriptor
                      :source-revision :source-byte-count]))]
     (when (nil? context)
       (sh06-resolution-c5-fail! source-path :missing-context {:phase phase}))
     (sh06-resolution-content-proof-tree!
      source-path content (:content-object-proof preimage))
     (when-not
      (and (not legacy?) descriptor input
           (= descriptor (:descriptor input))
           (= source-path (:path source-unit) (:path descriptor))
           (= (:raw-sha256 descriptor) (:bytes-hash source-unit))
           (= (:byte-count descriptor) expected-count))
       (sh06-resolution-c5-fail!
        source-path :contextual-source-snapshot
        {:phase phase :module-ordinal ordinal}))
     (sh06-resolution-source-proof-bytes!
      source-path content descriptor (:source-object-proof input))
     {:canonical-path source-path
      :byte-count (:byte-count descriptor)
      :bytes-hash (:raw-sha256 descriptor)
      :immutable-git-object? true})))

(defn- sh06-source-snapshot-stable!
  ([source-path before c2-artifact legacy?]
   (let [after (sh06-authenticated-source-snapshot!
                source-path c2-artifact :source-stability-after-c5 legacy?)]
    (when (and (not legacy?)
               (not= (select-keys before [:canonical-path :byte-count :bytes-hash])
                     (select-keys after [:canonical-path :byte-count :bytes-hash])))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" source-path :source-mutated-during-c5
       {:before (select-keys before [:canonical-path :byte-count :bytes-hash])
        :after (select-keys after [:canonical-path :byte-count :bytes-hash])}
       {:required [:stable-source-before-and-after-construction]}))
     after))
  ([source-path before c2-artifact legacy? context]
   (let [after (sh06-authenticated-source-snapshot!
                source-path c2-artifact :source-stability-after-c5
                legacy? context)]
     (when-not (= before after)
       (sh06-resolution-c5-fail!
        source-path :contextual-source-mutated
        {:before before :after after}))
     after)))
