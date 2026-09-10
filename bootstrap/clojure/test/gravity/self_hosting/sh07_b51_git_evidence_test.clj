(ns gravity.self-hosting.sh07-b51-git-evidence-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [gravity.tools.sh07-b51-git-evidence :as evidence]
            [gravity.tools.sh07-b51-v9-codec :as codec])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files OpenOption Path StandardOpenOption]))

(def ^:private no-open-options (make-array OpenOption 0))
(def ^:private strict-read-wire (:strict-read-wire codec/transport-api))

(defn- git!
  [^Path directory & arguments]
  (let [command (into [evidence/git-executable "-C" (str directory)]
                      arguments)
        process (.start (doto (ProcessBuilder. ^java.util.List command)
                          (.redirectErrorStream true)))
        bytes (.readAllBytes (.getInputStream process))
        exit (.waitFor process)]
    (when-not (zero? exit)
      (throw (ex-info "test Git command failed"
                      {:command command :exit exit
                       :output (String. bytes StandardCharsets/UTF_8)})))
    (clojure.string/trim-newline
     (String. bytes StandardCharsets/UTF_8))))

(defn- write-bytes!
  [^Path root relative bytes]
  (let [path (.resolve root relative)]
    (Files/createDirectories (.getParent path)
                             (make-array java.nio.file.attribute.FileAttribute
                                         0))
    (Files/write path bytes
                 (into-array OpenOption
                             [StandardOpenOption/CREATE
                              StandardOpenOption/TRUNCATE_EXISTING
                              StandardOpenOption/WRITE]))
    path))

(defn- commit!
  [root message]
  (git! root "add" "-A")
  (git! root "commit" "-m" message)
  (git! root "rev-parse" "HEAD"))

(defn- initialized-repository!
  []
  (let [root (Files/createTempDirectory
              "sh07-b51-git-evidence-"
              (make-array java.nio.file.attribute.FileAttribute 0))]
    (git! root "init")
    (git! root "config" "user.name" "Gravity Test")
    (git! root "config" "user.email" "gravity@example.invalid")
    (write-bytes! root "seed.edn" (codec/W {:seed true}))
    (write-bytes! root "docs/artifacts/phase-15/evidence/.keep"
                  (.getBytes "tracked parent\n" StandardCharsets/UTF_8))
    (let [commit (commit! root "content")
          tree (git! root "show" "-s" "--format=%T" commit)]
      {:root root :content {:commit commit :tree tree}})))

(defn- payload-values []
  (mapv (fn [ordinal] {:payload ordinal :accepted true}) (range 6)))

(defn- stage!
  [values]
  (let [root (Files/createTempDirectory
              "sh07-b51-stage-"
              (make-array java.nio.file.attribute.FileAttribute 0))
        descriptors
        (mapv
         (fn [[ordinal name kind] value]
           (let [bytes (codec/W value)]
             (write-bytes! root name bytes)
             {:ordinal ordinal :name name :kind kind
              :relative-path (str evidence/evidence-relative-root "/" name)
              :wire-codec :gravity/sh07-b51-edn-wire-v1
              :byte-count (alength ^bytes bytes)
              :raw-sha256 (codec/R bytes)
              :value-id
              (codec/H
               [:gravity/sh07-b51-staged-payload-value-v1
                {:ordinal ordinal :name name :kind kind :value value}])}))
         evidence/payload-specs values)]
    {:root root :descriptors descriptors}))

(defn- typed-sha [label]
  (codec/H [:gravity/test-sh07-b51-v9 label]))

(defn- identity-receipt
  [content]
  {:artifact :gravity/sh07-b51-checker-process-identity-receipt-v1
   :schema-version 1
   :content content
   :semantic-ordinals (vec (range 11))
   :coordinator-events []
   :coordinator-event-root (typed-sha :coordinator-events)
   :child-process-receipts []
   :launcher-receipt {:scope :test-seam}
   :coordinator-receipt {:scope :test-seam}
   :payload-joins {:scope :test-seam}
   :evidence-eligibility {:scope :test-seam}
   :pre-discharge-id (typed-sha :pre-discharge)
   :runtime-contract-discharge {:scope :test-seam}
   :receipt-id-normalization {:scope :test-seam}
   :self-id-omits [[:receipt-id]]
   :receipt-id (typed-sha :normalized-receipt)
   :nonclaims evidence/nonclaims})

(defn- add-worktree!
  [root commit suffix]
  (let [path (.resolve (.getParent root)
                       (str (.getFileName root) "-" suffix))]
    (git! root "worktree" "add" "--detach" (str path) commit)
    path))

(defn- failure-reason
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo error
      (:reason (ex-data error)))))

(defn- descriptor-at!
  [^Path root content ordinal kind module-ordinal path]
  (let [row (git! root "ls-tree" (:commit content) "--" path)
        match (re-matches #"([0-7]{6}) blob ([0-9a-f]{40})\t(.+)" row)
        bytes (Files/readAllBytes (.resolve root path))]
    (when-not match
      (throw (ex-info "test path is not a regular Git blob"
                      {:path path :row row})))
    {:ordinal ordinal :kind kind :module-ordinal module-ordinal
     :path path :mode (nth match 1) :blob-oid (nth match 2)
     :byte-count (alength ^bytes bytes) :raw-sha256 (codec/R bytes)}))

(defn- mutate-unsigned-byte
  [value index]
  (update value index #(bit-xor % 1)))

(defn- tree-entry-bytes
  [mode name oid-byte]
  (vec
   (concat
    (map #(bit-and (int %) 0xff)
         (.getBytes (str mode " " name "\u0000") StandardCharsets/UTF_8))
    (repeat 20 oid-byte))))

(deftest e2-git-object-proofs-are-complete-pure-and-fail-closed
  (let [{:keys [root content]} (initialized-repository!)
        path "docs/artifacts/phase-15/evidence/.keep"
        descriptor (descriptor-at! root content 12 :test nil path)
        content-proof
        (evidence/build-content-object-proof!
         {:repository-root root :content content})
        source-proof
        (evidence/build-source-object-proof!
         {:repository-root root :content content :descriptor descriptor})
        entry
        (evidence/build-governed-source-entry!
         {:repository-root root :content content :descriptor descriptor})
        parse-tree!
        (var-get (ns-resolve 'gravity.tools.sh07-b51-git-evidence
                             'parse-git-tree-bytes!))]
    (is (= #{:oid :raw-bytes} (set (keys content-proof))))
    (is (= #{:schema :path :root-tree :tree-chain :blob-bytes}
           (set (keys source-proof))))
    (is (= (count (str/split path #"/"))
           (count (:tree-chain source-proof))))
    (is (every? #(= #{:tree-oid :tree-bytes :component
                       :selected-mode :selected-oid}
                    (set (keys %)))
                (:tree-chain source-proof)))
    (is (= entry (evidence/validate-governed-source-entry! content entry)))
    (write-bytes! root path (.getBytes "uncommitted worktree mutation\n"
                                      StandardCharsets/UTF_8))
    (is (= content-proof
           (evidence/validate-content-object-proof! content content-proof)))
    (is (= source-proof
           (evidence/validate-source-object-proof!
            content descriptor source-proof)))
    (testing "commit, tree, component, mode, oid, blob, and C/T mutations reject"
      (is (some? (failure-reason
                  #(evidence/validate-content-object-proof!
                    content
                    (update content-proof :raw-bytes
                            mutate-unsigned-byte 0)))))
      (is (some? (failure-reason
                  #(evidence/validate-source-object-proof!
                    content descriptor
                    (update-in source-proof [:tree-chain 0 :tree-bytes]
                               mutate-unsigned-byte 0)))))
      (is (some? (failure-reason
                  #(evidence/validate-source-object-proof!
                    content descriptor
                    (assoc-in source-proof [:tree-chain 0 :component]
                              "wrong")))))
      (is (some? (failure-reason
                  #(evidence/validate-source-object-proof!
                    content descriptor
                    (assoc-in source-proof
                              [:tree-chain (dec (count (:tree-chain source-proof)))
                               :selected-mode]
                              "100755")))))
      (is (some? (failure-reason
                  #(evidence/validate-source-object-proof!
                    content descriptor
                    (assoc-in source-proof [:tree-chain 0 :selected-oid]
                              (apply str (repeat 40 "0")))))))
      (is (some? (failure-reason
                  #(evidence/validate-source-object-proof!
                    content descriptor
                    (update source-proof :blob-bytes mutate-unsigned-byte 0)))))
      (is (some? (failure-reason
                  #(evidence/validate-source-object-proof!
                    (assoc content :tree (apply str (repeat 40 "0")))
                    descriptor source-proof)))))
    (testing "the bounded parser rejects noncanonical order and unsafe modes"
      (is (= :tree-entry-order
             (failure-reason
              #(parse-tree!
                (vec (concat (tree-entry-bytes "100644" "z" 1)
                             (tree-entry-bytes "100644" "a" 2)))))))
      (is (= :forbidden-tree-mode
             (failure-reason
              #(parse-tree! (tree-entry-bytes "120000" "link" 1))))))))

(deftest e3-source-pre-and-ownership-censuses-are-exact
  (let [ownership-builder
        (var-get (ns-resolve 'gravity.tools.sh07-b51-git-evidence
                             'build-v9-ownership-plan-e3))
        plan (ownership-builder)]
    (is (= 13 (count evidence/source-specs)))
    (is (= [12 :sh06-resolution nil
            "bootstrap/gravity/src/gravity/resolution.gravity"]
           (nth evidence/source-specs 12)))
    (is (= evidence/resolution-source-descriptor
           {:ordinal 12
            :kind :sh06-resolution
            :module-ordinal nil
            :path "bootstrap/gravity/src/gravity/resolution.gravity"
            :mode "100644"
            :blob-oid "16ef2b638298a8e934fcd0b39131072305fbcfd5"
            :byte-count 77209
            :raw-sha256
            "sha256:001ef59741f17b98b37ee5bdb21e698cb1e6e56ce76c5f5fdd5f1fc9a4caeb56"}))
    (is (= 37 (count evidence/owned-c-paths)))
    (is (= 83 (count evidence/pre-c-paths)))
    (is (= {:sessions
            [{:participant :a :acquire-count 13 :decode-count 13
              :event-count 40 :finalize-count 1}
             {:participant :b :acquire-count 13 :decode-count 13
              :event-count 40 :finalize-count 1}
             {:participant :m :acquire-count 13 :decode-count 13
              :event-count 40 :finalize-count 1}]
            :acquire-count 39 :decode-count 39
            :event-count 120 :finalize-count 3}
           (evidence/build-source-session-aggregate!
            {:a evidence/source-session-counters-e2
             :b evidence/source-session-counters-e2
             :m evidence/source-session-counters-e2})))
    (is (some? (failure-reason
                #(evidence/build-source-session-aggregate!
                  {:a evidence/source-session-counters-e2
                   :b evidence/source-session-counters-e2
                   :m (update evidence/source-session-counters-e2
                              :event-count dec)}))))
    (is (= :gravity/sh07-b51-v9-ownership-plan-e3-v1 (:schema plan)))
    (is (= 41 (count (get-in plan [:allocations 1 :paths]))))
    (is (= 37 (- (count (get-in plan [:allocations 1 :paths])) 4)))
    (is (= (codec/H [:gravity/sh07-b51-v9-ownership-plan-e3-v1
                     (dissoc plan :plan-id)])
           (:plan-id plan)))))

(defn- runtime-mask-input
  "Small authenticated-source fixture for the PRE/runtime mask seam.

  The production C/T snapshot has the same five top-level definitions at
  their governed form ordinals; this fixture keeps all non-hole bytes stable
  while making the root construction fast and deterministic."
  []
  (let [evidence-ns (find-ns 'gravity.tools.sh07-b51-git-evidence)
        specs @(ns-resolve evidence-ns 'pre-mask-specs)
        pre-build (var-get (ns-resolve evidence-ns 'build-pre-masked-root))
        paths (vec (distinct (map :path specs)))
        form (fn [owner]
               (str "(def ^:private " (name owner)
                    "\n  :gravity/provisional-no-authority)\n"))
        path-text
        (fn [path]
          (let [owners-by-form
                (into {}
                      (map (juxt :form-ordinal :owner)
                           (filter #(= path (:path %)) specs)))
                maximum-form (apply max (keys owners-by-form))]
            (apply str
                   (map #(if-let [owner (get owners-by-form %)]
                           (form owner)
                           "(def x 0)\n")
                        (range (inc maximum-form))))))
        decoded-static (into {} (map (fn [path] [path (path-text path)]) paths))
        bytes-by-path
        (into {}
              (map (fn [[path text]]
                     [path (.getBytes ^String text StandardCharsets/UTF_8)])
                   decoded-static))
        pre (pre-build bytes-by-path decoded-static)
        pin-map
        {:expected-m3-id
         "sha256:1111111111111111111111111111111111111111111111111111111111111111"
         :expected-certificate-a-id
         "sha256:2222222222222222222222222222222222222222222222222222222222222222"
         :expected-certificate-b-id
         "sha256:3333333333333333333333333333333333333333333333333333333333333333"
         :expected-semantic-root
         "sha256:4444444444444444444444444444444444444444444444444444444444444444"}
        carrier {:domain :gravity/test-sh07-b51-carrier-v1
                 :schema-version 1
                 :pins pin-map
                 :rows [[:a 1] [:b 2]]}]
    {:bytes-by-path bytes-by-path
     :decoded-static decoded-static
     :pre-masked-root pre
     :pin-map pin-map
     :quoted-carrier carrier}))

(deftest constructs-and-validates-runtime-masked-root
  (let [input (runtime-mask-input)
        runtime (evidence/build-runtime-masked-root input)
        projection (select-keys runtime evidence/pre-masked-root-keys)]
    (is (= evidence/runtime-masked-root-keys (set (keys runtime))))
    (is (= true (:equal runtime)))
    (is (= (:pre-masked-root input) projection))
    (is (= (:preimage-root runtime) (:postimage-root runtime)))
    (is (= runtime
           (evidence/validate-runtime-masked-root!
            runtime (:pre-masked-root input))))
    (is (= [0 1 2 3 4]
           (mapv :coordinate-ordinal (:postimage-spans runtime))))
    (is (every? #(not= (get-in input [:pre-masked-root :preimage-spans %
                                       :rhs-raw-sha256])
                       (get-in runtime [:postimage-spans % :rhs-raw-sha256]))
                (range 5)))))

(deftest runtime-masked-root-rejects-legacy-mixed-and-unbound-values
  (let [input (runtime-mask-input)
        path (:path (first @(ns-resolve
                             (find-ns 'gravity.tools.sh07-b51-git-evidence)
                             'pre-mask-specs)))
        original-text (get-in input [:decoded-static path])
        mixed-text
        (str/replace-first
         original-text
         ":gravity/provisional-no-authority"
         "\"sha256:9999999999999999999999999999999999999999999999999999999999999999\"")
        mixed-input
        (-> input
            (assoc-in [:decoded-static path] mixed-text)
            (assoc-in [:bytes-by-path path]
                      (.getBytes ^String mixed-text StandardCharsets/UTF_8)))]
    (testing "PRE has no runtime or legacy keys"
      (is (some? (failure-reason
                  #(evidence/build-runtime-masked-root
                    (update input :pre-masked-root
                            assoc :runtime-masked-root {}))))))
    (testing "pin map is exactly the four digest holes"
      (is (some? (failure-reason
                  #(evidence/build-runtime-masked-root
                    (update input :pin-map assoc :masked-root
                            "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")))))
      (is (some? (failure-reason
                  #(evidence/build-runtime-masked-root
                    (update input :pin-map dissoc :expected-m3-id))))))
    (testing "a mixed C/T RHS cannot be treated as a runtime postimage"
      (is (some? (failure-reason
                  #(evidence/build-runtime-masked-root mixed-input)))))
    (testing "quoted carrier must be a closed carrier value"
      (is (some? (failure-reason
                  #(evidence/build-runtime-masked-root
                    (assoc input :quoted-carrier nil))))))
    (testing "runtime root cannot acquire the superseded ambiguous key"
      (let [runtime (evidence/build-runtime-masked-root input)]
                    (is (some? (failure-reason
                    #(evidence/validate-runtime-masked-root!
                      (assoc runtime :masked-root (:postimage-root runtime))))))))))

(defn- synthetic-eligibility-coordinate
  []
  {:schema :gravity/sh07-b51-external-review-object-v1
   :commit (apply str (repeat 40 "1"))
   :tree (apply str (repeat 40 "2"))
   :parents [(apply str (repeat 40 "1"))]
   :path evidence/eligibility-receipt-path
   :mode "100644"
   :type "blob"
   :blob-oid (apply str (repeat 40 "3"))
   :byte-count 17
   :raw-sha256
   "sha256:1111111111111111111111111111111111111111111111111111111111111111"
   :receipt-id
   "sha256:2222222222222222222222222222222222222222222222222222222222222222"})

(defn- synthetic-evidence-eligibility
  [pre-masked-root]
  (let [coordinate (synthetic-eligibility-coordinate)
        sha (fn [digit]
              (str "sha256:" (apply str (repeat 64 digit))))]
    {:coordinate coordinate
     :receipt-id (:receipt-id coordinate)
     :freeze-state :frozen
     :freeze-record-id (sha "3")
     :source-descriptors-root (sha "4")
     :pre-masked-root pre-masked-root
     :pre-discharge-id (sha "5")
     :participant-check-roots (mapv sha ["6" "7" "8" "9"])}))

(defn- synthetic-runtime-carriers
  [runtime-root]
  (assoc
   (into {} (map (fn [key] [key {:carrier key}])
                 evidence/runtime-carrier-keys))
   :runtime-masked-root runtime-root))

(deftest runtime-evidence-eligibility-and-phase-joins-are-closed
  (let [mask (runtime-mask-input)
        eligibility (synthetic-evidence-eligibility (:pre-masked-root mask))
        content {:commit (apply str (repeat 40 "a"))
                 :tree (apply str (repeat 40 "b"))}
        runtime-root (evidence/build-runtime-masked-root mask)
        carriers (synthetic-runtime-carriers runtime-root)
        phase (evidence/build-runtime-phase-evidence
               content eligibility carriers)]
    (is (= evidence/evidence-eligibility-keys
           (set (keys eligibility))))
    (is (= evidence/runtime-phase-carrier-keys
           (set (keys (:carriers phase)))))
    (is (= phase (evidence/validate-phase-evidence! phase)))
    (testing "E1 cannot acquire a runtime or future edge"
      (is (some? (failure-reason
                  #(evidence/validate-evidence-eligibility!
                    (assoc eligibility :runtime-contract-discharge {})))))
      (is (some? (failure-reason
                  #(evidence/validate-evidence-eligibility!
                    (dissoc eligibility :pre-discharge-id))))))
    (testing "runtime phase rejects enclosing identity/discharge cycles"
      (is (= :runtime-identity-cycle
             (failure-reason
              #(evidence/validate-phase-evidence!
                (update-in phase [:carriers :coordinator]
                           assoc :identity-receipt {}))))))))

(deftest runtime-prior-reference-is-q-bound-and-closed
  (let [mask (runtime-mask-input)
        eligibility (synthetic-evidence-eligibility (:pre-masked-root mask))
        id (apply str (repeat 40 "a"))
        sha "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        runtime-i9-suffix
        (subvec (vec (var-get
                      (ns-resolve 'gravity.tools.sh07-b51-git-evidence
                                  'i9-lifecycle-paths))) 37)
        g9-paths (vec (var-get
                       (ns-resolve 'gravity.tools.sh07-b51-git-evidence
                                   'g9-lifecycle-paths)))
        p9-paths (vec (var-get
                       (ns-resolve 'gravity.tools.sh07-b51-git-evidence
                                   'p9-lifecycle-paths)))
        forbidden-refs
        [[:e9 0] [:e9 1] [:e9 2] [:e9 3] [:e9 4] [:e9 5] [:e9 6]
         [:i9-suffix 1] [:i9-suffix 2] [:i9-suffix 3]
         [:g9 0] [:g9 1] [:g9 2] [:g9 3] [:g9 4]
         [:p9 0] [:p9 1] [:p9 2] [:p9 3] [:p9 4] [:p9 5]]
        path-for
        (fn [[domain ordinal]]
          (let [paths (case domain
                        :e9 evidence/evidence-paths
                        :i9-suffix runtime-i9-suffix
                        :g9 g9-paths
                        :p9 p9-paths)]
            (let [path (nth paths ordinal)]
              (if (string? path) path
                  (str (:directory path) "/" (:workstream-id path)
                       "-" (apply str (repeat 64 "f")) ".json")))))
        forbidden-observations
        (mapv
         (fn [ordinal path-ref]
           (let [path (path-for path-ref)]
             {:ordinal ordinal :path-ref path-ref :resolved-path path
              :argv [evidence/git-executable "-C" "/repo" "ls-tree" "-z"
                     (get-in eligibility [:coordinate :commit]) "--" path]
              :exit-code 0 :stdout-byte-count 0
              :stdout-raw-sha256
              "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
              :parsed-result []}))
         (range) forbidden-refs)
        prior-ref
        {:phase :runtime
         :container (select-keys (:coordinate eligibility)
                                 #{:commit :tree :parents :path :mode :type
                                   :blob-oid :byte-count :raw-sha256})
         :value-path [:pre-evidence-contract-discharge]
         :discharge-id (:pre-discharge-id eligibility)
         :lifecycle-ref
         {:stage :pre-evidence
          :path-ref [:i9-suffix 0]
          :container (select-keys (:coordinate eligibility)
                                  #{:commit :tree :parents :path :mode :type
                                    :blob-oid :byte-count :raw-sha256})
          :value-path []
          :required-result :evidence-eligible
          :receipt-id (:freeze-record-id eligibility)}
         :forbidden-path-observations forbidden-observations}]
    (is (= prior-ref
           (evidence/validate-runtime-prior-ref!
            prior-ref eligibility)))
    (is (some? (failure-reason
                #(evidence/validate-runtime-prior-ref!
                  (assoc prior-ref :value-path [:runtime]) eligibility))))
    (is (some? (failure-reason
                #(evidence/validate-runtime-prior-ref!
                  (assoc-in prior-ref [:lifecycle-ref :runtime-contract-discharge]
                            {}) eligibility))))))

(deftest strict-runtime-identity-receipt-requires-q-and-rejects-cycles
  (let [mask (runtime-mask-input)
        eligibility (synthetic-evidence-eligibility (:pre-masked-root mask))
        content {:commit (apply str (repeat 40 "a"))
                 :tree (apply str (repeat 40 "b"))}
        carriers {:content content
                  :evidence-eligibility eligibility
                  :coordinator {:normalized true}}
        discharge {:prior-ref {:discharge-id (:pre-discharge-id eligibility)}
                    :evidence {:carriers carriers}}
        normalization (var-get
                       (ns-resolve 'gravity.tools.sh07-b51-git-evidence
                                   'identity-receipt-normalization))
        events (vec (repeat 11 {:event :normalized}))
        payload-joins
        {:checker-identity-raw-sha256s
         (vec (repeat 2 (:raw-sha256 (:coordinate eligibility))))
         :checker-output-raw-sha256s
         (vec (repeat 2 (:raw-sha256 (:coordinate eligibility))))
         :certificate-pair-raw-sha256 (:raw-sha256 (:coordinate eligibility))
         :linked-carrier-raw-sha256 (:raw-sha256 (:coordinate eligibility))}
        base
        (assoc (identity-receipt content)
               :coordinator-events events
               :coordinator-event-root
               (codec/H [:gravity/sh07-b51-coordinator-event-root-v1 events])
               :child-process-receipts [{:child :a} {:child :b}]
               :launcher-receipt {:normalized true}
               :coordinator-receipt {:normalized true}
               :payload-joins payload-joins
               :evidence-eligibility eligibility
               :pre-discharge-id (:pre-discharge-id eligibility)
               :runtime-contract-discharge discharge
               :receipt-id-normalization normalization)
        with-id (fn [receipt]
                  (assoc receipt :receipt-id
                         (codec/H
                          [:gravity/sh07-b51-checker-process-identity-receipt-id-v1
                           (dissoc receipt :receipt-id)])))
        receipt (with-id base)
        cyclic
        (with-id
         (update-in receipt [:runtime-contract-discharge :evidence :carriers]
                    assoc :identity-receipt {}))]
    (testing "strict entrypoint does not invent Q/check values"
      (is (= :runtime-identity-context-required
             (failure-reason
              #(evidence/validate-runtime-identity-receipt!
                content receipt)))))
    (with-redefs [evidence/validate-evidence-eligibility!
                  (fn [& _] eligibility)
                  evidence/validate-runtime-contract-discharge!
                  (fn [& _] discharge)]
      (is (= receipt
             (evidence/validate-runtime-identity-receipt!
              content receipt {:eligibility-receipt {}
                               :participant-results []})))
      (is (= :runtime-identity-cycle
             (failure-reason
              #(evidence/validate-runtime-identity-receipt!
                content cyclic {:eligibility-receipt {}
                                :participant-results []})))))))

(deftest validates-distinct-clean-content-worktrees
  (let [{:keys [root content]} (initialized-repository!)
        publication (add-worktree! root (:commit content) "publication")
        result (evidence/validate-publication-worktrees!
                {:source-worktree root
                 :publication-worktree publication
                 :content content})]
    (is (= content
           (select-keys (:source-worktree result) [:commit :tree])
           (select-keys (:publication-worktree result) [:commit :tree])))
    (is (not= (get-in result [:source-worktree :canonical-path])
              (get-in result [:publication-worktree :canonical-path])))
    (is (= (get-in result [:source-worktree :git-common-directory])
           (get-in result [:publication-worktree :git-common-directory])))
    (is (= :none (:authority result)))
    (is (= :publication-worktree-separation
           (failure-reason
            #(evidence/validate-publication-worktrees!
              {:source-worktree root
               :publication-worktree root
               :content content}))))))

(deftest validates-six-canonical-m-payloads
  (let [content {:commit (apply str (repeat 40 "1"))
                 :tree (apply str (repeat 40 "2"))}
        values (payload-values)
        {:keys [root descriptors]} (stage! values)
        response-base
        {:schema :gravity/sh07-b51-m-response-v1
         :status :accepted
         :content content
         :payload-descriptors descriptors
         :staging-receipt
         {:staging-path (str (.normalize (.toAbsolutePath root)))
          :evidence-relative-root evidence/evidence-relative-root
          :directory-mode "0700"
          :symlink-free true
          :payload-names evidence/payload-names
          :payload-descriptors descriptors}
         :coordinator-receipt {:scope :test-seam}
         :source-read-receipt {:scope :test-seam}
         :counters {:scope :test-seam}
         :transport
         {:response-preimage-id nil
          :self-id-omits [[:transport :response-preimage-id]]}}
        response-id
        (codec/H
         [:gravity/sh07-b51-response-preimage-id-v1
          (update response-base :transport dissoc :response-preimage-id)])
        response (assoc-in response-base
                           [:transport :response-preimage-id] response-id)
        result (evidence/validate-m-response!
                {:content content :staging-root root
                 :response-bytes (codec/W response)})]
    (is (= values (mapv :value (:staged-payloads result))))
    (is (= response-id (:response-preimage-id result)))
    (is (= :none (:authority result)))
    (is (= "SH07-B51-GIT-INPUT-BLOB"
           (try
             (evidence/validate-m-response!
              {:content content :staging-root root
               :response-bytes
               (codec/W (assoc-in response [:payload-descriptors 0 :byte-count]
                                  0))})
             nil
             (catch clojure.lang.ExceptionInfo error
               (:id (ex-data error))))))))

(deftest new-publication-is-seven-create-new-files-with-identity-last
  (let [{:keys [root content]} (initialized-repository!)
        publication (add-worktree! root (:commit content) "publication-new")
        values (payload-values)
        stage (stage! values)
        receipt (identity-receipt content)
        result
        (evidence/publish-new-evidence!
         {:source-worktree root :publication-worktree publication
          :content content :staging-root (:root stage)
          :payload-descriptors (:descriptors stage)
          :identity-receipt receipt})
        final-root (.resolve publication evidence/evidence-relative-root)]
    (is (= {:reservation-created true
            :identity-receipt-created-last true
            :final-write-count 7
            :foreign-entry false
            :outcome :staged-new}
           (select-keys (:publication result)
                        [:reservation-created :identity-receipt-created-last
                         :final-write-count :foreign-entry :outcome])))
    (is (= (vec (sort evidence/evidence-names))
           (->> (iterator-seq (.iterator (Files/newDirectoryStream final-root)))
                (map #(.toString (.getFileName ^Path %))) sort vec)))
    (is (= receipt
           (strict-read-wire
            :response
            (Files/readAllBytes
             (.resolve final-root evidence/identity-receipt-name)))))
    (is (not (Files/exists
              (.resolve root evidence/evidence-relative-root)
              (make-array java.nio.file.LinkOption 0))))))

(deftest failed-publication-cleans-only-an-owned-prefix-and-quarantines-foreign-data
  (let [{:keys [root content]} (initialized-repository!)
        values (payload-values)
        stage (stage! values)
        receipt (identity-receipt content)
        create-var
        (ns-resolve 'gravity.tools.sh07-b51-git-evidence
                    'create-new-bytes!)
        original @create-var
        clean-publication (add-worktree! root (:commit content) "cleanup")
        clean-final (.resolve clean-publication evidence/evidence-relative-root)
        clean-calls (atom 0)]
    (testing "an unpublished exact prefix is removed"
      (is (= :publication-failed
             (with-redefs-fn
               {create-var
                (fn [path bytes]
                  (if (= 2 (swap! clean-calls inc))
                    (throw (ex-info "injected" {}))
                    (original path bytes)))}
               #(failure-reason
                 (fn []
                   (evidence/publish-new-evidence!
                    {:source-worktree root
                     :publication-worktree clean-publication
                     :content content :staging-root (:root stage)
                     :payload-descriptors (:descriptors stage)
                     :identity-receipt receipt}))))))
      (is (not (Files/exists clean-final
                             (make-array java.nio.file.LinkOption 0)))))
    (testing "a foreign entry leaves the reservation quarantined"
      (let [publication (add-worktree! root (:commit content) "quarantine")
            final-root (.resolve publication evidence/evidence-relative-root)
            calls (atom 0)]
        (is (= :publication-quarantined
               (with-redefs-fn
                 {create-var
                  (fn [path bytes]
                    (if (= 2 (swap! calls inc))
                      (do
                        (Files/write (.resolve (.getParent ^Path path)
                                               "foreign.edn")
                                     (.getBytes "foreign\n"
                                                StandardCharsets/UTF_8)
                                     (into-array OpenOption
                                                 [StandardOpenOption/CREATE_NEW
                                                  StandardOpenOption/WRITE]))
                        (throw (ex-info "injected" {})))
                      (original path bytes)))}
                 #(failure-reason
                   (fn []
                     (evidence/publish-new-evidence!
                      {:source-worktree root :publication-worktree publication
                       :content content :staging-root (:root stage)
                       :payload-descriptors (:descriptors stage)
                       :identity-receipt receipt}))))))
        (is (Files/exists (.resolve final-root "foreign.edn")
                          (make-array java.nio.file.LinkOption 0)))
        (is (not (Files/exists
                  (.resolve final-root evidence/identity-receipt-name)
                  (make-array java.nio.file.LinkOption 0))))))))
