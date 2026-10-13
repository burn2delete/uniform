
(defn- build-normalized-mask-files
  [bytes-by-path coordinates spans]
  (mapv
   (fn [path]
     (let [path-spans
           (sort-by :rhs-start-byte >
                    (keep-indexed
                     (fn [ordinal span]
                       (when (= path (:path (nth coordinates ordinal))) span))
                     spans))
           masked
           (reduce
            (fn [bytes {:keys [rhs-start-byte rhs-end-byte]}]
              (replace-byte-span bytes rhs-start-byte rhs-end-byte
                                 masked-rhs-bytes))
            (get bytes-by-path path) path-spans)]
       {:path path :masked-byte-count (alength ^bytes masked)
        :masked-raw-sha256 (codec/R masked)}))
   (vec (distinct (map :path coordinates)))))

(declare build-pre-masked-root)

(defn build-runtime-masked-root
  "Construct the exact runtime dual-image mask from authenticated C/T bytes.

  Positional arguments are `bytes-by-path`, `decoded-static`, Q's
  `pre-masked-root`, the exact four-entry `pin-map`, and the raw or already
  quoted carrier value.  A one-map arity accepts the same values under those
  five closed keys.  The constructor independently rebuilds PRE, applies all
  five post RHS replacements in descending byte order, computes post spans and
  normalized files, and only returns when the pre/post roots and Q projection
  agree byte-for-byte."
  ([bytes-by-path decoded-static pre-masked-root pin-map quoted-carrier]
   (validate-mask-source-input! bytes-by-path decoded-static)
   (validate-pre-masked-root! pre-masked-root)
   (validate-runtime-pin-map! pin-map)
   (let [pre-recomputed (build-pre-masked-root bytes-by-path decoded-static)]
     (when-not (Arrays/equals ^bytes (codec/W pre-recomputed)
                              ^bytes (codec/W pre-masked-root))
       (fail! "SH07-B51-MASKED-ROOT" :pre-masked-root-mismatch))
     (let [coordinates (:coordinates pre-masked-root)
           pre-spans (:preimage-spans pre-masked-root)
           rhs-bytes
           (conj
            (mapv (fn [pin-key]
                    (.getBytes (pr-str (get pin-map pin-key))
                               StandardCharsets/UTF_8))
                  runtime-pin-order)
            (quoted-carrier-bytes! quoted-carrier))
           post-bytes
           (replace-runtime-rhs! bytes-by-path coordinates pre-spans rhs-bytes)
           post-spans
           (mapv
            (fn [ordinal]
              (let [coordinate (nth coordinates ordinal)
                    source (get post-bytes (:path coordinate))]
                (mask-span-for-rhs! source ordinal coordinate
                                    (nth rhs-bytes ordinal))))
            (range 5))
           post-files
           (build-normalized-mask-files post-bytes coordinates post-spans)
           post-root (masked-source-root coordinates post-files)
           runtime-root
           {:coordinates coordinates
            :preimage-spans (:preimage-spans pre-masked-root)
            :postimage-spans post-spans
            :preimage-files (:preimage-files pre-masked-root)
            :postimage-files post-files
            :preimage-root (:preimage-root pre-masked-root)
            :postimage-root post-root
            :equal (= (:preimage-root pre-masked-root) post-root)}]
       (validate-runtime-masked-root! runtime-root pre-masked-root)))
   )
  ([input]
   (exact-keys! input
                #{:bytes-by-path :decoded-static :pre-masked-root :pin-map
                  :quoted-carrier}
                "SH07-B51-MASKED-ROOT" :runtime-mask-input-keys)
   (build-runtime-masked-root
    (:bytes-by-path input) (:decoded-static input)
    (:pre-masked-root input) (:pin-map input) (:quoted-carrier input))))

(defn build-runtime-masked-root!
  "Compatibility bang-form for callers that treat the constructor as an
  admission operation.  It has the same exact arities and return value."
  [& arguments]
  (apply build-runtime-masked-root arguments))

(defn- build-pre-masked-root
  [bytes-by-path decoded-static]
  (let [coordinates-and-spans
        (mapv
         (fn [coordinate-ordinal spec]
           (let [{:keys [path form-ordinal owner]} spec
                 text (get decoded-static path)
                 form (source-form-at! text form-ordinal)
                 owner-name (name owner)
                 _ (when-not
                    (and (seq? form) (= 'def (first form))
                         (= owner-name (name (second form)))
                         (= :gravity/provisional-no-authority (nth form 2 nil))
                         (= 3 (count form)))
                     (fail! "SH07-B51-GIT-SNAPSHOT" :masked-form
                            {:coordinate-ordinal coordinate-ordinal
                             :form-ordinal form-ordinal :form form}))
                 left (str "(def ^:private " owner-name "\n  ")
                 left-bytes (.getBytes left StandardCharsets/UTF_8)
                 full (byte-array (+ 1 (alength left-bytes)
                                     (alength provisional-rhs-bytes) 2))
                 _ (aset-byte full 0 (byte 10))
                 _ (System/arraycopy left-bytes 0 full 1
                                     (alength left-bytes))
                 rhs-in-full (+ 1 (alength left-bytes))
                 _ (System/arraycopy provisional-rhs-bytes 0 full rhs-in-full
                                     (alength provisional-rhs-bytes))
                 _ (aset-byte full (+ rhs-in-full
                                      (alength provisional-rhs-bytes))
                              (byte (int \))))
                 _ (aset-byte full (dec (alength full)) (byte 10))
                 source (get bytes-by-path path)
                 offsets (byte-match-offsets source full)
                 _ (when-not (= 1 (count offsets))
                     (fail! "SH07-B51-GIT-SNAPSHOT" :masked-occurrence-census
                            {:coordinate-ordinal coordinate-ordinal
                             :count (count offsets)}))
                 form-start (inc (first offsets))
                 rhs-start (+ form-start (alength left-bytes))
                 rhs-end (+ rhs-start (alength provisional-rhs-bytes))
                 form-end (inc rhs-end)
                 coordinate
                 (assoc spec
                        :left-delimiter left :right-delimiter ")"
                        :preceding-byte 10 :following-byte 10
                        :masked-rhs-byte-count (alength masked-rhs-bytes))
                 span
                 {:coordinate-ordinal coordinate-ordinal
                  :form-start-byte form-start
                  :rhs-start-byte rhs-start :rhs-end-byte rhs-end
                  :form-end-byte form-end
                  :rhs-byte-count (alength provisional-rhs-bytes)
                  :rhs-raw-sha256 (codec/R provisional-rhs-bytes)}]
             {:coordinate coordinate :span span}))
         (range) pre-mask-specs)
        coordinates (mapv :coordinate coordinates-and-spans)
        spans (mapv :span coordinates-and-spans)
        files
        (mapv
         (fn [path]
           (let [path-spans
                 (sort-by :rhs-start-byte >
                          (keep-indexed
                           (fn [ordinal span]
                             (when (= path (:path (nth coordinates ordinal)))
                               span))
                           spans))
                 masked
                 (reduce
                  (fn [bytes {:keys [rhs-start-byte rhs-end-byte]}]
                    (replace-byte-span bytes rhs-start-byte rhs-end-byte
                                       masked-rhs-bytes))
                  (get bytes-by-path path) path-spans)]
             {:path path :masked-byte-count (alength ^bytes masked)
              :masked-raw-sha256 (codec/R masked)}))
         (vec (distinct (map :path coordinates))))
        root (codec/H [:gravity/sh07-b51-masked-source-root-v1
                       {:coordinates coordinates :files files}])]
    {:coordinates coordinates :preimage-spans spans
     :preimage-files files :preimage-root root}))

(def ^:private i9-lifecycle-paths
  (vec
   (concat
    owned-c-paths
    [eligibility-receipt-path
     "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/i9-integration-receipt.edn"
     "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/i9-integration-discharge.edn"
     (governance-path-spec
      "sh07-b51-git-object-evidence-publication-implementation-v9")])))

(def ^:private g9-lifecycle-paths
  ["docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9/g9-publication-tuple.edn"
   "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/g9-lifecycle-receipt.edn"
   "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/g9-formal-review.md"
   "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/g9-publication-discharge.edn"
   (governance-path-spec "sh07-b51-git-object-evidence-publication-g9-v9")])

(def ^:private p9-lifecycle-paths
  ["bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj"
   "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00947-p15-s23-sh07-b51-linked-carrier-v4-05.clj"
   "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/p9-pin-publication-receipt.edn"
   "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/p9-formal-review.md"
   "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/p9-final-discharge.edn"
   (governance-path-spec "sh07-b51-git-object-evidence-publication-p9-v9")])

(def ^:private read-only-paths
  ["bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_runner.clj"
   "bootstrap/clojure/test/gravity/self_hosting_test_runner.clj"
   "bootstrap/gravity/src/gravity/checked_core.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/root8.gravity"
   "contracts/verification-catalog.edn"
   "contracts/workstream-governance.json"
   "contracts/workstream-ledger.json"
   "docs/self-hosting-slice-backlog.md"
   "docs/self-hosting-slice-ownership.edn"
   "docs/workstream-governance.md"
   "docs/workstream-ledger-v2.md"])

(defn- allocation [ordinal stage paths]
  {:ordinal ordinal :stage stage :owner :master-coordinator :paths paths})

(defn- build-v9-ownership-plan-e4 []
  (let [base
        {:schema :gravity/sh07-b51-v9-ownership-plan-e4-v1
         :allocations
         [(allocation 0 :a9 a9-paths-e1)
          (allocation 1 :i9 i9-lifecycle-paths)
          (allocation 2 :e9 evidence-paths)
          (allocation 3 :g9 g9-lifecycle-paths)
          (allocation 4 :p9 p9-lifecycle-paths)]
         :semantic-review-owner :sh-core
         :reservation-required-stages [:i9 :e9 :g9 :p9]
         :handoff-required-stages [:i9 :g9 :p9]
         :read-only-prerequisite-stages [:a9]
         :read-only-paths read-only-paths
         :open-exit-gates
         [:core :evaluation :arity :mutation :recursion :exception :pattern]
         :aggregate-sh07-state :partial
         :self-id-omits [[:plan-id]]}]
    (assoc base :plan-id
           (codec/H [:gravity/sh07-b51-v9-ownership-plan-e4-v1 base]))))

(def governance-rules-e1
  {:permitted-transitions
   [:draft :frozen :review-pending :accepted :integration-eligible :integrated]
   :admission-requirements
   [:exact-base :exact-candidate :clean-named-branch :owned-paths
    :governing-contracts :validation :independent-acceptance
    :residual-boundaries :nonclaims]
   :external-discharge-order
   [:pre-evidence :runtime :post-i9 :post-g9 :post-p9]
   :final-cleanup-required-stages [:i9 :g9 :p9]
   :aggregate-sh07-state :partial})
