(ns gravity.self-hosting.sh07-b51-attempt32-tier0-materializer-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap :as bootstrap]
            [gravity.digest :as digest]
            [gravity.tools.sh07-b51-attempt30-independent-census :as checker-b]
            [gravity.tools.sh07-b51-attempt30-producer-census :as checker-a]
            [gravity.tools.sh07-b51-attempt32-checker-identity :as identity]
            [gravity.tools.sh07-b51-attempt32-materialize :as cli]
            [gravity.tools.sh07-b51-attempt32-tier0-materializer :as tier0])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files OpenOption Paths StandardOpenOption]
           [java.nio.file.attribute FileAttribute]))

(defn- repository-root []
  (.normalize (.toAbsolutePath (Paths/get "." (make-array String 0)))))

(defonce ^:private actual-materializer-closure
  (delay (identity/source-closure-manifest
          (repository-root) tier0/materializer-source-paths)))

(defonce ^:private actual-materialized
  (delay
    (tier0/materialize-current
     {:repository-root (repository-root)
      :snapshot {:commit (apply str (repeat 40 "a"))
                 :tree (apply str (repeat 40 "b"))}
      :completed-at "2026-09-08T00:00:00Z"
      :evidence-relative-root "docs/artifacts/phase-15/evidence/test"})))

(deftest checker-identities-bind-real-role-distinct-source-closures
  (let [root (repository-root)
        a (identity/checker-identity-manifest root :producer-census)
        b (identity/checker-identity-manifest
           root :independent-verifier-census)
        coordinates [:source-sha256 :semantic-id :executable-id
                     :dependency-closure-id]
        local-paths
        #(mapv :relative-path (get-in % [:dependencies :local-source-records]))]
    (is (= :source-first-exhaustive
           (get-in a [:checker :algorithm-role])))
    (is (= :m2-first-independent-exhaustive
           (get-in b [:checker :algorithm-role])))
    (is (every? #(not= (get-in a [:checker %]) (get-in b [:checker %]))
                coordinates))
    (is (= a (identity/checker-identity-manifest root :producer-census)))
    (is (= b (identity/checker-identity-manifest
              root :independent-verifier-census)))
    (is (every? #(not (.startsWith ^String % "/"))
                (concat (local-paths a) (local-paths b))))
    (is (= ["bootstrap/clojure/src/gravity/c2_artifact_identity/canonical.clj"
            "bootstrap/clojure/src/gravity/digest.clj"]
           (local-paths b)))
    (is (not (some #{"bootstrap/clojure/src/gravity/bootstrap_impl/load_order.edn"}
                   (local-paths a))))))

(defn- checker [role]
  (let [prefix (case role :producer-census "a" "b")
        digest #(str "sha256:" prefix (apply str (repeat 63 %)))]
    {:artifact-id (digest "1") :source-sha256 (digest "2")
     :semantic-id (digest "3") :executable-id (digest "4")
     :dependency-closure-id (digest "5")
     :algorithm-role
     (case role :producer-census :source-first-exhaustive
                :m2-first-independent-exhaustive)}))

(defn- certificate [role checker-value]
  (let [claim {:normalized-census-id "sha256:common"
               :physical-census {:definitions 1}
               :category-schedule {:categories 7}
               :category-commitments [{:category :roots}]}]
    {:role role :checker checker-value :claim-id "sha256:claim"
     :claim claim :profile-bounds {:bound 1}
     :result {:normalized-census-id "sha256:common"}}))

(deftest materializer-requires-independent-exact-census-agreement
  (let [ca (checker :producer-census)
        cb (checker :independent-verifier-census)
        envelope-a {:checker ca} envelope-b {:checker cb}
        output-a {:status :accepted
                  :certificate (certificate :producer-census ca)}
        output-b {:status :accepted
                  :certificate
                  (certificate :independent-verifier-census cb)}]
    (with-redefs [checker-a/checker-output-valid? (constantly true)
                  checker-b/valid-output? (constantly true)
                  bootstrap/p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?
                  (constantly true)]
      (is (= [(:certificate output-a) (:certificate output-b)]
             (tier0/validate-certificate-pair!
              envelope-a output-a envelope-b output-b)))
      (testing "a shared checker dependency identity fails closed"
        (let [shared (get ca :dependency-closure-id)
              changed-checker (assoc cb :dependency-closure-id shared)
              changed (assoc output-b :certificate
                             (certificate :independent-verifier-census
                                          changed-checker))]
          (is (= :checker-identities-not-independent
                 (try
                   (tier0/validate-certificate-pair!
                    envelope-a output-a
                    (assoc envelope-b :checker changed-checker) changed)
                   nil
                   (catch clojure.lang.ExceptionInfo failure
                     (:reason (ex-data failure))))))))
      (testing "a different normalized census fails closed"
        (let [changed (assoc-in output-b
                                [:certificate :claim :normalized-census-id]
                                "sha256:different")]
          (is (= :checker-common-claim-mismatch
                 (try
                   (tier0/validate-certificate-pair!
                    envelope-a output-a envelope-b changed)
                   nil
                   (catch clojure.lang.ExceptionInfo failure
                     (:reason (ex-data failure)))))))))))

(deftest evidence-encoding-is-deterministic-readable-and-path-independent
  (let [left {:z #{:b :a} :a [{:y 2 :x 1}]}
        right (array-map :a [(array-map :x 1 :y 2)] :z #{:a :b})
        left-bytes (cli/deterministic-edn-bytes left)
        right-bytes (cli/deterministic-edn-bytes right)]
    (is (java.util.Arrays/equals left-bytes right-bytes))
    (is (= left (edn/read-string (String. left-bytes StandardCharsets/UTF_8))))
    (is (not (.contains (String. left-bytes StandardCharsets/UTF_8)
                        (str (repository-root)))))))

(deftest materializer-source-closure-binds-the-complete-executable
  (is (= 3 (count tier0/materializer-source-paths)))
  (let [root (repository-root)
        closure @actual-materializer-closure
        paths (set (map :relative-path (:local-source-records closure)))
        data-root (.resolve
                   root "bootstrap/clojure/src/gravity/bootstrap_impl/data")
        data-paths
        (->> (file-seq (.toFile data-root))
             (filter #(.isFile ^java.io.File %))
             (map #(str (.relativize root (.toPath ^java.io.File %))))
             set)]
    (is (every? paths tier0/materializer-source-paths))
    (is (contains? paths "deps.edn"))
    (is (= 118 (count data-paths)))
    (is (= data-paths
           (set (filter #(.startsWith
                          ^String %
                          "bootstrap/clojure/src/gravity/bootstrap_impl/data/")
                        paths))))
    (is (contains? paths
                   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt30_producer_census.clj"))
    (is (contains? paths
                   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt30_independent_census.clj"))
    (is (contains? paths
                   "bootstrap/clojure/src/gravity/bootstrap_impl/load_order.edn"))
    (is (contains? paths
                   "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj"))
    (is (contains? paths
                   "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00947-p15-s23-sh07-b51-linked-carrier-v4.clj"))))

(deftest every-materializer-closure-member-changes-a-bound-identity
  (let [closure @actual-materializer-closure
        checker-manifest {:identity-manifest-id "sha256:checker"}
        commitments (tier0/materializer-dependency-commitments closure)
        base-id
        (tier0/materializer-tool-closure-id-from-commitments
         :producer-census checker-manifest commitments)
        mutations
        (for [[records-key commitment-key domain]
              [[:local-source-records :local-source-record-ids
                :gravity/sh07-b51-materializer-local-source-v1]
               [:external-runtime-records :external-runtime-record-ids
                :gravity/sh07-b51-materializer-external-runtime-v1]]
              [ordinal record] (map-indexed vector (get closure records-key))]
          [commitment-key ordinal
           (identity/canonical-id
            {:domain domain :schema-version 1
             :record (assoc record :mutation-sentinel ordinal)})])
        results
        (doall
         (pmap
          (fn [[commitment-key ordinal changed-commitment]]
            [commitment-key ordinal changed-commitment
             (tier0/materializer-tool-closure-id-from-commitments
              :producer-census checker-manifest
              (assoc-in commitments [commitment-key ordinal]
                        changed-commitment))])
          mutations))]
    (doseq [[commitment-key ordinal changed-commitment changed-id] results]
        (is (not= (get-in commitments [commitment-key ordinal])
                  changed-commitment))
        (is (not= base-id changed-id)))))

(deftest materializer-bootstrap-call-surface-is-exact-and-pin-free
  (let [path
        "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_tier0_materializer.clj"
        forms (bootstrap/read-forms path (slurp path))
        symbols (->> (tree-seq coll? seq forms)
                     (filter symbol?)
                     (filter #(= "bootstrap" (namespace %)))
                     set)]
    (is (= '#{bootstrap/p15-s23-sh07-b51-module-specs
              bootstrap/read-forms
              bootstrap/p15-s23-sh07-b51-attempt32-architecture-report-sha256
              bootstrap/p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?
              bootstrap/p15-s23-sh07-b51-current-bundle-core-products
              bootstrap/p15-s23-sh07-b51-build-linked-carrier-v4-candidate
              bootstrap/p15-s23-sh07-b51-linked-carrier-v4-valid?
              bootstrap/p15-s23-sh07-b51-v4-candidate-pin-map}
           symbols))
    (is (not-any? #(or (.contains (name %) "expected")
                       (.contains (name %) "authoritative")
                       (.contains (name %) "current-linked"))
                  symbols))))

(deftest v3-receipt-builtin-is-exactly-closed-by-both-checkers
  (is (= {'sh07-canonical-text-compare 2
          'sh07-declared-digest-hash 1
          'sh07-observe-utf8-byte-count 3
          'sh07-b51-admitted-executable-carrier-validation? 1}
         checker-a/internal-builtin-arities
         checker-b/internal-builtin-arities))
  (is (= (set checker-a/internal-builtins)
         (set checker-b/internal-builtins)
         (set (keys checker-a/internal-builtin-arities))))
  (let [products (bootstrap/p15-s23-sh07-b51-current-bundle-core-products)
        rows (filter #(= 'sh07-b51-admitted-executable-carrier-validation?
                         (:builtin %))
                     (mapcat :builtin-calls
                             (:module-source-descriptors products)))]
    (is (seq rows))
    (is (every? #(= [1 true] [(:arity %) (:compiler-internal? %)]) rows))))

(deftest independent-checker-reconstructs-authentic-legacy-m2-plan-ids
  (let [plans (:module-plans
               (bootstrap/p15-s23-sh07-b51-current-bundle-core-products))
        m2-identity-value
        (deref
         (ns-resolve
          'gravity.tools.sh07-b51-attempt30-independent-census
          'm2-identity-value))]
    (is (= 7 (count plans)))
    (doseq [plan plans]
      (let [preimage
            (m2-identity-value
             (-> plan
                 (dissoc :plan-id)
                 (update :source #(if (map? %) (dissoc % :path) %))
                 (update :module #(if (map? %)
                                    (dissoc % :source-path)
                                    %))))
            legacy-id
            (str "sha256:"
                 (digest/sha256-hex
                  (pr-str preimage)))
            double-canonical-id (bootstrap/reader-canonical-hash preimage)]
        (is (= legacy-id (:plan-id plan)))
        (is (= (:plan-id plan) (checker-b/m2-plan-id plan)))
        (is (not= (:plan-id plan) double-canonical-id))
        (let [metadata-plan
              (walk/postwalk
               (fn [value]
                 (if (symbol? value)
                   (with-meta value {:must-not-affect-c4 true})
                   value))
               plan)]
          (is (some #(and (symbol? %)
                          (= true (:must-not-affect-c4 (meta %))))
                    (tree-seq coll? seq metadata-plan)))
          (is (= (:plan-id plan) (checker-b/m2-plan-id metadata-plan))))))))

(defn- rejection-reason [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo failure
      (:reason (ex-data failure)))))

(defn- private-function [namespace symbol]
  (deref (ns-resolve namespace symbol)))

(defn- child-names [directory]
  (with-open [stream (Files/list directory)]
    (set (map #(str (.getFileName %))
              (iterator-seq (.iterator stream))))))

(deftest evidence-reader-requires-exactly-one-plain-edn-form
  (is (= {:a 1} (#'cli/read-evidence
                 (.getBytes "{:a 1}\n" StandardCharsets/UTF_8))))
  (is (= :evidence-edn-cardinality
         (rejection-reason
          #(#'cli/read-evidence
            (.getBytes "" StandardCharsets/UTF_8)))))
  (is (= :evidence-edn-cardinality
         (rejection-reason
          #(#'cli/read-evidence
            (.getBytes "{:a 1} {:b 2}" StandardCharsets/UTF_8))))))

(defn- delete-output-directory! [directory file-names]
  (doseq [name file-names]
    (Files/deleteIfExists (.resolve directory name)))
  (Files/deleteIfExists directory))

(defn- write-publication! [bundle]
  (let [directory (Files/createTempDirectory
                   (repository-root) ".sh07-binding-replay-reload-"
                   (make-array FileAttribute 0))]
    (doseq [[name bytes] (:files bundle)]
      (Files/write (.resolve directory name) bytes
                   (into-array OpenOption
                               [StandardOpenOption/CREATE_NEW
                                StandardOpenOption/WRITE])))
    directory))

(defn- validate-publication [materialized bundle directory]
  (cli/reload-validate-output!
   directory materialized (:values bundle) (:files bundle)
   (:descriptors bundle)))

(defn- coherently-change-receipt [bundle change]
  (let [receipt-name (:receipt tier0/output-file-names)
        changed-base
        (-> (:receipt bundle) change (dissoc :identity-receipt-id))
        changed-receipt
        (assoc changed-base :identity-receipt-id
               (identity/canonical-id changed-base))
        changed-bytes (cli/deterministic-edn-bytes changed-receipt)
        changed-descriptor
        (#'cli/artifact-descriptor (:evidence-relative-root changed-receipt)
                                   receipt-name changed-bytes)]
    (-> bundle
        (assoc :receipt changed-receipt)
        (assoc-in [:values :receipt] changed-receipt)
        (assoc-in [:files receipt-name] changed-bytes)
        (assoc-in [:descriptors :receipt] changed-descriptor))))

(deftest coherent-receipt-recomputation-cannot-invent-materialized-joins
  (let [materialized @actual-materialized
        bundle (cli/publication-bundle materialized)
        wrong "sha256:0000000000000000000000000000000000000000000000000000000000000000"
        changes
        [#(assoc-in % [:product-identities :m1-id] wrong)
         #(assoc-in % [:checker-identities :producer-census :source-sha256]
                    wrong)
         #(assoc-in % [:checker-identity-manifest-ids :producer-census] wrong)
         #(assoc-in % [:census-identities :claim-id] wrong)
         #(assoc-in % [:candidate-semantic-coordinates :expected-m3-id] wrong)
         #(assoc-in % [:binding-replay-architecture-report :source-sha256]
                    wrong)
         #(assoc-in % [:materializer-dependency-closure
                       :local-source-records 0 :source-sha256]
                    wrong)]]
    (doseq [change changes]
      (let [changed (coherently-change-receipt bundle change)
            directory (write-publication! changed)]
        (try
          (is (= :evidence-receipt-materialized-join
                 (rejection-reason
                  #(validate-publication materialized changed directory))))
          (finally
            (delete-output-directory! directory (keys (:files changed)))))))))

(deftest all-seven-artifacts-reject-truncation-mutation-and-swap
  (let [materialized @actual-materialized
        bundle (cli/publication-bundle materialized)
        directory (write-publication! bundle)
        artifacts (vec (keys (:files bundle)))]
    (try
      (is (true? (validate-publication materialized bundle directory)))
      (doseq [[ordinal name] (map-indexed vector artifacts)]
        (let [path (.resolve directory name)
              original (get-in bundle [:files name])
              other-name (nth artifacts (mod (inc ordinal) (count artifacts)))
              other (get-in bundle [:files other-name])
              changed (aclone original)]
          (aset-byte changed 0 (unchecked-byte (bit-xor 1 (aget changed 0))))
          (doseq [mutant [(byte-array (dec (alength original))) changed other]]
            (Files/write path mutant
                         (into-array OpenOption
                                     [StandardOpenOption/TRUNCATE_EXISTING
                                      StandardOpenOption/WRITE]))
            (is (= :evidence-reload-mismatch
                   (rejection-reason
                    #(validate-publication materialized bundle directory))))
            (Files/write path original
                         (into-array OpenOption
                                     [StandardOpenOption/TRUNCATE_EXISTING
                                      StandardOpenOption/WRITE])))))
      (is (= :evidence-artifact-descriptor
             (rejection-reason
              #(cli/reload-validate-output!
                directory materialized (:values bundle) (:files bundle)
                (assoc-in (:descriptors bundle) [:receipt :sha256]
                          "sha256:wrong")))))
      (finally
        (delete-output-directory! directory (keys (:files bundle)))))))

(deftest real-semantic-reload-rejects-checker-pair-carrier-and-pin-mutations
  (let [materialized @actual-materialized
        mutations
        [(assoc-in materialized [:outputs :producer-census :status] :rejected)
         (assoc-in materialized [:outputs :independent-verifier-census :status]
                   :rejected)
         (assoc-in materialized [:certificates 0 :certificate-id]
                   "sha256:wrong")
         (assoc materialized :carrier {})
         (assoc-in materialized [:pin-map :expected-m3-id] "sha256:wrong")
         (assoc-in materialized
                   [:materializer-dependency-closure
                    :local-source-records 0 :source-sha256]
                   "sha256:wrong")]]
    (doseq [changed mutations]
      (let [bundle (cli/publication-bundle changed)
            directory (write-publication! bundle)]
        (try
          (is (some? (rejection-reason
                      #(validate-publication changed bundle directory))))
          (finally
            (delete-output-directory! directory (keys (:files bundle)))))))))

(deftest final-snapshot-recheck-is-between-staged-reload-and-atomic-move
  (let [path
        "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_materialize.clj"
        forms (bootstrap/read-forms path (slurp path))
        publish (first (filter #(and (seq? %) (= 'defn- (first %))
                                     (= 'publish-files! (second %)))
                               forms))
        nodes (vec (tree-seq coll? seq publish))
        positions
        (fn [head]
          (vec (keep-indexed
                (fn [position node]
                  (when (and (seq? node) (= head (first node))) position))
                nodes)))
        reloads (positions 'reload-validate-output!)
        snapshot-checks (positions 'validate-snapshot!)
        moves (positions 'Files/move)]
    (is (= 3 (count reloads)))
    (is (= 2 (count snapshot-checks)))
    (is (= 1 (count moves)))
    (is (< (second reloads) (last snapshot-checks)
           (first moves) (last reloads)))))

(deftest snapshot-validation-rejects-head-tree-and-worktree-mutation
  (let [root (repository-root)
        snapshot {:commit (apply str (repeat 40 "a"))
                  :tree (apply str (repeat 40 "b"))}
        command-output (ns-resolve
                        'gravity.tools.sh07-b51-attempt32-materialize
                        'command-output)
        run (fn [responses]
              (with-redefs-fn
                {command-output
                 (fn [_ arguments]
                   (get responses arguments ""))}
                #(cli/validate-snapshot! root snapshot)))]
    (is (= :snapshot-commit-mismatch
           (rejection-reason
            #(run {["git" "rev-parse" "HEAD"]
                   (apply str (repeat 40 "c"))}))))
    (is (= :snapshot-tree-mismatch
           (rejection-reason
            #(run {["git" "rev-parse" "HEAD"] (:commit snapshot)
                   ["git" "rev-parse" "HEAD^{tree}"]
                   (apply str (repeat 40 "d"))
                   ["git" "rev-parse" (str (:commit snapshot) "^{tree}")]
                   (:tree snapshot)}))))
    (is (= :dirty-worktree
           (rejection-reason
            #(run {["git" "rev-parse" "HEAD"] (:commit snapshot)
                   ["git" "rev-parse" "HEAD^{tree}"] (:tree snapshot)
                   ["git" "rev-parse" (str (:commit snapshot) "^{tree}")]
                   (:tree snapshot)
                   ["git" "status" "--porcelain=v1"
                    "--untracked-files=all"] " M source.clj"}))))))

(deftest source-and-evidence-symlink-components-fail-closed
  (let [root (Files/createTempDirectory
              (repository-root) ".sh07-symlink-boundary-"
              (make-array FileAttribute 0))
        outside (Files/createTempDirectory
                 (repository-root) ".sh07-symlink-outside-"
                 (make-array FileAttribute 0))
        real (Files/createDirectory (.resolve root "real")
                                    (make-array FileAttribute 0))
        source (.resolve real "source.clj")
        ancestor (.resolve root "ancestor")
        escape (.resolve root "escape")
        leaf (.resolve root "leaf.clj")
        publish! (private-function
                  'gravity.tools.sh07-b51-attempt32-materialize
                  'publish-files!)
        resolve-file! (private-function
                       'gravity.tools.sh07-b51-attempt32-checker-identity
                       'resolve-file)]
    (Files/write source (.getBytes "(ns test.source)\n"
                                    StandardCharsets/UTF_8)
                 (into-array OpenOption [StandardOpenOption/CREATE_NEW
                                         StandardOpenOption/WRITE]))
    (Files/createSymbolicLink ancestor real (make-array FileAttribute 0))
    (Files/createSymbolicLink escape outside (make-array FileAttribute 0))
    (Files/createSymbolicLink leaf source (make-array FileAttribute 0))
    (try
      (is (false? (identity/symlink-free-path? leaf)))
      (is (false? (identity/symlink-free-path?
                   (.resolve ancestor "source.clj"))))
      (is (false? (identity/symlink-free-path?
                   (.resolve escape "outside.edn"))))
      (is (thrown? clojure.lang.ExceptionInfo (resolve-file! root "leaf.clj")))
      (is (thrown? clojure.lang.ExceptionInfo
                   (resolve-file! root "ancestor/source.clj")))
      (is (= :evidence-output-parent
             (rejection-reason
              #(publish! root "ancestor/new" nil nil nil {} {}))))
      (is (= :evidence-output-parent
             (rejection-reason
              #(publish! root "escape/new" nil nil nil {} {}))))
      (let [target (.resolve root "target")]
        (Files/createSymbolicLink target real (make-array FileAttribute 0))
        (is (= :evidence-output-exists
               (rejection-reason
                #(publish! root "target" nil nil nil
                           {"only.edn" (byte-array 0)} {}))))
        (Files/deleteIfExists target))
      (let [target (Files/createDirectory (.resolve root "leaf-target")
                                          (make-array FileAttribute 0))
            external (.resolve outside "external.edn")
            artifact (.resolve target "only.edn")]
        (Files/write external (byte-array 0)
                     (into-array OpenOption [StandardOpenOption/CREATE_NEW
                                             StandardOpenOption/WRITE]))
        (Files/createSymbolicLink artifact external (make-array FileAttribute 0))
        (is (= :evidence-output-exists
               (rejection-reason
                #(publish! root "leaf-target" nil nil nil
                           {"only.edn" (byte-array 0)} {}))))
        (Files/deleteIfExists artifact)
        (Files/deleteIfExists target)
        (Files/deleteIfExists external))
      (finally
        (Files/deleteIfExists leaf)
        (Files/deleteIfExists escape)
        (Files/deleteIfExists ancestor)
        (Files/deleteIfExists source)
        (Files/deleteIfExists real)
        (Files/deleteIfExists root)
        (Files/deleteIfExists outside)))))

(deftest existing-identical-rechecks-snapshot-after-reload
  (let [root (Files/createTempDirectory
              (repository-root) ".sh07-existing-recheck-"
              (make-array FileAttribute 0))
        target (Files/createDirectory (.resolve root "evidence")
                                      (make-array FileAttribute 0))
        bytes (.getBytes "{:artifact :fixture}\n" StandardCharsets/UTF_8)
        file (.resolve target "only.edn")
        publish! (private-function
                  'gravity.tools.sh07-b51-attempt32-materialize
                  'publish-files!)
        reload-var (ns-resolve
                    'gravity.tools.sh07-b51-attempt32-materialize
                    'reload-validate-output!)
        validate-var (ns-resolve
                      'gravity.tools.sh07-b51-attempt32-materialize
                      'validate-snapshot!)
        events (atom [])]
    (Files/write file bytes
                 (into-array OpenOption [StandardOpenOption/CREATE_NEW
                                         StandardOpenOption/WRITE]))
    (try
      (with-redefs-fn
        {reload-var (fn [& _] (swap! events conj :seven-artifact-reload) true)
         validate-var
         (fn [& _]
           (swap! events conj :final-snapshot-recheck)
           (throw (ex-info "snapshot changed after initial validation"
                           {:reason :dirty-worktree})))}
        #(is (= :dirty-worktree
                (rejection-reason
                 (fn []
                   (publish! root "evidence" {:commit "initial" :tree "initial"}
                             nil nil {"only.edn" bytes} nil))))))
      (is (= [:seven-artifact-reload :final-snapshot-recheck] @events))
      (is (java.util.Arrays/equals bytes (Files/readAllBytes file)))
      (finally
        (Files/deleteIfExists file)
        (Files/deleteIfExists target)
        (Files/deleteIfExists root)))))

(deftest publication-rechecks-existing-output-and-never-overwrites-a-race
  (let [materialized @actual-materialized
        bundle (cli/publication-bundle materialized)
        root (Files/createTempDirectory
              (repository-root) ".sh07-publish-roundtrip-"
              (make-array FileAttribute 0))
        publish! (private-function
                  'gravity.tools.sh07-b51-attempt32-materialize
                  'publish-files!)
        validate-var (ns-resolve
                      'gravity.tools.sh07-b51-attempt32-materialize
                      'validate-snapshot!)
        reload-var (ns-resolve
                    'gravity.tools.sh07-b51-attempt32-materialize
                    'reload-validate-output!)
        reload-count (atom 0)
        snapshot-count (atom 0)
        invoke #(publish! root "evidence" (:snapshot materialized)
                          materialized (:values bundle) (:files bundle)
                          (:descriptors bundle))]
    (try
      (with-redefs-fn
        {validate-var (fn [& _]
                        (swap! snapshot-count inc)
                        (:snapshot materialized))
         reload-var (fn [& arguments]
                      (swap! reload-count inc)
                      (is (= 5 (count arguments)))
                      true)}
        #(do (is (= :published (invoke)))
             (is (= :already-identical (invoke)))))
      (is (= 3 @reload-count))
      (is (= 2 @snapshot-count))
      (with-redefs-fn
        {reload-var (fn [& _] true)
         validate-var
         (fn [& _]
           (throw (ex-info "snapshot changed after initial validation"
                           {:reason :dirty-worktree})))}
        #(is (= :dirty-worktree (rejection-reason invoke))))
      (let [target (.resolve root "raced")
            marker (.resolve target "owner")]
        (with-redefs-fn
          {reload-var (fn [& _] true)
           validate-var
           (fn [& _]
             (Files/createDirectory target (make-array FileAttribute 0))
             (Files/write marker (.getBytes "owner" StandardCharsets/UTF_8)
                          (into-array OpenOption
                                      [StandardOpenOption/CREATE_NEW
                                       StandardOpenOption/WRITE]))
             (:snapshot materialized))}
          #(is (= :evidence-output-race
                  (rejection-reason
                   (fn []
                     (publish! root "raced" (:snapshot materialized)
                               materialized (:values bundle)
                               (:files bundle) (:descriptors bundle)))))))
        (is (= "owner" (slurp (str marker))))
        (is (not-any? #(.startsWith ^String % ".sh07-attempt32-tier0-")
                      (child-names root)))
        (Files/deleteIfExists marker)
        (Files/deleteIfExists target))
      (finally
        (delete-output-directory! (.resolve root "evidence")
                                  (keys (:files bundle)))
        (Files/deleteIfExists root)))))

(deftest final-recheck-failure-publishes-nothing-and-cleans-staging
  (let [materialized @actual-materialized
        bundle (cli/publication-bundle materialized)
        root (Files/createTempDirectory
              (repository-root) ".sh07-publish-recheck-"
              (make-array FileAttribute 0))
        publish! (private-function
                  'gravity.tools.sh07-b51-attempt32-materialize
                  'publish-files!)
        validate-var (ns-resolve
                      'gravity.tools.sh07-b51-attempt32-materialize
                      'validate-snapshot!)
        reload-var (ns-resolve
                    'gravity.tools.sh07-b51-attempt32-materialize
                    'reload-validate-output!)]
    (try
      (with-redefs-fn
        {reload-var (fn [& _] true)
         validate-var (fn [& _]
                        (throw (ex-info "mutated" {:reason :dirty-worktree})))}
        #(is (= :dirty-worktree
                (rejection-reason
                 (fn []
                   (publish! root "evidence" (:snapshot materialized)
                             materialized (:values bundle) (:files bundle)
                             (:descriptors bundle)))))))
      (is (not (Files/exists (.resolve root "evidence")
                             (make-array java.nio.file.LinkOption 0))))
      (is (empty? (child-names root)))
      (finally (Files/deleteIfExists root)))))
