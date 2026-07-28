(ns gravity.self-hosting.sh07-module-fragment-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_module_fragment_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07-B15 test source is not on the classpath"
        {:id "SH07-B13-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-B13-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b13")
(def ^:private authoritative-root
  "bootstrap/gravity/src/gravity/bootstrap")
(def ^:private extensions [".gravity" ".qst"])
(def ^:private fragment-record-keys
  #{:fragment-id :ordinal :root-form-ids :form-ids
    :local-binding-ids :external-binding-ids
    :resolution-reference-syntax-ids :alias-names
    :root-node-ids :content-id})
(def ^:private fragment-semantic-partition-keys
  [:ordinal :root-form-ids :form-ids
   :local-binding-ids :external-binding-ids
   :resolution-reference-syntax-ids :alias-names])
(def ^:private fragment-coverage-keys
  #{:root-form-count :form-count :local-binding-count :resolution-count
    :fragment-count :covered-root-form-ids :covered-form-ids
    :covered-local-binding-ids
    :covered-resolution-reference-syntax-ids})
(def ^:private module-assembly-keys
  #{:module-id :ordered-fragment-ids :root-form-ids
    :source-revision-id :sh06-semantic-projection-id
    :alias-table-id :content-id})

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- fixture-path
  [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- authoritative-path
  [basename]
  (path (str authoritative-root "/" basename ".gravity")))

(defn- fixture-basenames
  [family extension]
  (let [directory (io/file (path (str fixture-root "/" family)))]
    (if-not (.isDirectory directory)
      #{}
      (->> (.listFiles directory)
           (filter #(.isFile %))
           (map #(.getName %))
           (filter #(str/ends-with? % extension))
           (map #(subs % 0 (- (count %) (count extension))))
           set))))

(defn- accepted-fixtures
  []
  (fixture-basenames "accepted" ".gravity"))

(defn- rejected-fixtures
  []
  (fixture-basenames "rejected" ".gravity"))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- source-text
  [source-path]
  (String. (source-bytes source-path)
           java.nio.charset.StandardCharsets/UTF_8))

(defn- delete-tree!
  [root-path]
  (when (java.nio.file.Files/exists
         root-path
         (make-array java.nio.file.LinkOption 0))
    (with-open [stream
                (java.nio.file.Files/walk
                 root-path
                 (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))

(defn- required-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info
        "Required SH-07-B15 coordinator adapter is absent"
        {:id "SH07-B13-ADAPTER-ABSENT"
         :symbol symbol
         :required-signatures
         {'sh07-core-source-artifact '[source-path source-text]
          'sh07-core-file-artifact '[source-path]
          'sh07-core-artifact-verification '[artifact]
          'sh07-core-capability-based-proof '[artifact]
          'sh07-core-artifact-identity-input '[artifact]
          'sh07-core-verification-checks
          '[artifact expected upstream-verification]
          'sh07-core-run-request-for-test
          '[resolution-artifact authenticated-request]}}))))

(def ^:private artifacts (atom {}))
(def ^:private c2-artifacts (atom {}))

(defn- file-artifact
  [family basename extension]
  (let [key [family basename extension]]
    (or (get @artifacts key)
        (let [artifact
              ((required-var 'sh07-core-file-artifact)
               (fixture-path family basename extension))]
          (swap! artifacts assoc key artifact)
          artifact))))

(defn- authoritative-artifact
  [basename]
  (let [key [:authoritative basename]]
    (or (get @artifacts key)
        (let [artifact
              ((required-var 'sh07-core-file-artifact)
               (authoritative-path basename))]
          (swap! artifacts assoc key artifact)
          artifact))))

(defn- direct-artifact
  [family basename extension]
  (let [source-path (fixture-path family basename extension)]
    ((required-var 'sh07-core-source-artifact)
     source-path
     (source-text source-path))))

(defn- c2-artifact
  [basename extension]
  (let [key [basename extension]]
    (or (get @c2-artifacts key)
        (let [artifact
              (bootstrap/compiler-c2-reader-file-artifact
               (fixture-path "rejected" basename extension))]
          (swap! c2-artifacts assoc key artifact)
          artifact))))

(defn- fixture-oracle
  [basename extension]
  (let [artifact (c2-artifact basename extension)
        ns-form (first (:parsed-semantic-values artifact))
        metadata-clause
        (some #(when (and (seq? %) (= :metadata (first %))) %)
              (drop 2 ns-form))]
    (get (second metadata-clause) :sh07-b13)))

(defn- core
  [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))

(defn- request
  [artifact]
  (get-in artifact [:gravity-core-boundary :authenticated-core-request]))

(defn- identity-input
  [artifact]
  ((required-var 'sh07-core-artifact-identity-input) artifact))

(defn- sha256-id?
  [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- unique-vector?
  [values]
  (and (vector? values)
       (= (count values) (count (set values)))))

(defn- exactly-once-index
  [records key-name]
  (let [index (into {} (map (juxt key-name identity)) records)]
    (when-not (= (count records) (count index))
      (throw
       (ex-info
        "SH-07-B15 records are not uniquely identifiable"
        {:id "SH07-B13-AMBIGUOUS-INDEX"
         :key key-name
         :record-count (count records)
         :unique-count (count index)})))
    index))

(defn- diagnostic-result
  [operation]
  (try
    {:value (operation)}
    (catch clojure.lang.ExceptionInfo exception
      {:exception-data (ex-data exception)})
    (catch Throwable throwable
      {:raw-host-error
       {:class (.getName (class throwable))
        :message (.getMessage throwable)}})))

(defn- diagnostic-data
  [result]
  (let [data (:exception-data result)
        value (:value result)]
    (or (when (= :gravity/sh07-core-diagnostic (:artifact data)) data)
        (when (and (string? (:id data))
                   (keyword? (:stage data)))
          (assoc data :rule (:id data)))
        (when (= :gravity/sh07-core-diagnostic
                 (get-in data [:diagnostic :artifact]))
          (:diagnostic data))
        (when (= :gravity/sh07-core-diagnostic (:artifact value)) value)
        (when (= :gravity/sh07-core-diagnostic
                 (get-in value [:diagnostic :artifact]))
          (:diagnostic value)))))

(defn- verification-failures
  [altered expected]
  (set
   (for [[check passed?]
         ((required-var 'sh07-core-verification-checks)
          altered expected {:status :passed})
         :when (not (true? passed?))]
     check)))

(defn- run-request
  [resolution-artifact authenticated-request]
  (diagnostic-result
   #((required-var 'sh07-core-run-request-for-test)
     resolution-artifact authenticated-request)))

(defn- structural-request
  [authenticated-request]
  (let [projection-input
        ((required-var 'sh07-core-projection-binding-input)
         authenticated-request)
        rebound
        (assoc
         authenticated-request
         :projection-binding
         (bootstrap/reader-canonical-hash projection-input))]
    (diagnostic-result
     #((required-var 'sh07-core-run-structural-request-for-test)
       rebound))))

(defn- fragment-for-form
  [artifact form-id]
  (first
   (filter #(some #{form-id} (:form-ids %))
           (:fragment-manifest (request artifact)))))

(defn- assert-sequence-contract
  [values]
  (is (unique-vector? values))
  (is (not-any? nil? values)))

(deftest sh07-b13-fixtures-are-dynamically-discovered-paired-and-bounded
  (let [accepted (accepted-fixtures)
        rejected (rejected-fixtures)]
    (is (seq accepted))
    (is (seq rejected))
    (is (= accepted (fixture-basenames "accepted" ".qst")))
    (is (= rejected (fixture-basenames "rejected" ".qst")))
    (doseq [family ["accepted" "rejected"]
            basename (sort
                      (if (= family "accepted") accepted rejected))]
      (testing (str family "/" basename)
        (is (= (seq (source-bytes
                     (fixture-path family basename ".gravity")))
               (seq (source-bytes
                     (fixture-path family basename ".qst")))))))
    (doseq [required
            ["deterministic-top-level-fragments"
             "cross-fragment-reference"
             "alias-root-aligned-fragments"]]
      (is (contains? accepted required)))))

(deftest sh07-b13-direct-and-public-routing-use-v14
  (doseq [basename (sort (accepted-fixtures))
          extension extensions]
    (let [direct (direct-artifact "accepted" basename extension)
          public (file-artifact "accepted" basename extension)]
      (testing (str basename extension)
        (is (= :accepted (:status direct) (:status public)))
        (is (= (:artifact-id direct) (:artifact-id public)))
        (is (= (identity-input direct) (identity-input public)))
        (is (= 15 (:schema-version (request direct))
               (:schema-version (request public))))
        (is (= :sh07-b15-keyword-map-lookup
               (:scope (request direct))
               (:scope (request public))))
        (is (= "SH-07-B15" (:task direct) (:task public)))
        (is (= :c6-gravity-core-lowering-b15
               (get-in direct [:pass :name])
               (get-in public [:pass :name])))
        (is (= :gravity/sh07-to-c6-core-products-v15
               (get-in direct
                       [:gravity-core-boundary :adapter-contract])
               (get-in public
                       [:gravity-core-boundary :adapter-contract])))))))

(deftest sh07-b13-fragment-manifest-is-exact-root-aligned-and-exhaustive
  (doseq [basename (sort (accepted-fixtures))
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          authenticated-request (request artifact)
          fragments (:fragment-manifest authenticated-request)
          core-fragments (:fragment-manifest (core artifact))
          top-level (:top-level-form-ids authenticated-request)
          forms (mapv :form-id (:forms authenticated-request))
          bindings
          (mapv
           :binding-id
           (filter
            #(= (get-in authenticated-request [:module :namespace])
                (:namespace %))
            (:binding-table authenticated-request)))
          resolutions
          (mapv :reference-syntax-id
                (:resolution-table authenticated-request))]
      (testing (str basename extension)
        (is (vector? fragments))
        (is (seq fragments))
        (is (= core-fragments
               (:fragment-manifest (identity-input artifact))))
        (is (= (mapv #(select-keys
                       % fragment-semantic-partition-keys)
                     fragments)
               (mapv #(select-keys
                       % fragment-semantic-partition-keys)
                     core-fragments)))
        (is (= (mapv :root-form-ids fragments)
               (mapv :root-node-ids fragments)))
        (is (= (:root-core-node-ids (core artifact))
               (vec (mapcat :root-node-ids core-fragments))))
        (is (not= (mapv :root-node-ids fragments)
                  (mapv :root-node-ids core-fragments)))
        (is (= (vec (range (count fragments)))
               (mapv :ordinal fragments)))
        (is (= top-level (vec (mapcat :root-form-ids fragments))))
        (is (= forms (vec (mapcat :form-ids fragments))))
        (is (= (set bindings)
               (set (mapcat :local-binding-ids fragments))))
        (is (= (set resolutions)
               (set
                (mapcat
                 :resolution-reference-syntax-ids fragments))))
        (doseq [fragment fragments]
          (is (= fragment-record-keys (set (keys fragment))))
          (is (sha256-id? (:fragment-id fragment)))
          (is (sha256-id? (:content-id fragment)))
          (doseq [key-name
                  [:root-form-ids :form-ids :local-binding-ids
                   :external-binding-ids
                   :resolution-reference-syntax-ids :alias-names
                   :root-node-ids]]
            (assert-sequence-contract (get fragment key-name)))
          (is (seq (:root-form-ids fragment)))
          (is (= (count (:root-form-ids fragment))
                 (count (:root-node-ids fragment))))
          (is (every? (set (:form-ids fragment))
                      (:root-form-ids fragment)))
          (is (empty?
               (set
                (filter
                 (set (:local-binding-ids fragment))
                 (:external-binding-ids fragment))))))))))

(deftest sh07-b13-fragment-coverage-is-exact-ordered-and-complete
  (doseq [basename (sort (accepted-fixtures))
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          authenticated-request (request artifact)
          fragments (:fragment-manifest authenticated-request)
          coverage (:fragment-coverage (core artifact))
          local-bindings
          (filterv
           #(= (get-in authenticated-request [:module :namespace])
               (:namespace %))
           (:binding-table authenticated-request))]
      (testing (str basename extension)
        (is (= fragment-coverage-keys (set (keys coverage))))
        (is (= coverage (:fragment-coverage (identity-input artifact))))
        (is (= (count (:top-level-form-ids authenticated-request))
               (:root-form-count coverage)))
        (is (= (count (:forms authenticated-request))
               (:form-count coverage)))
        (is (= (count local-bindings)
               (:local-binding-count coverage)))
        (is (= (count (:resolution-table authenticated-request))
               (:resolution-count coverage)))
        (is (= (count fragments) (:fragment-count coverage)))
        (is (= (:top-level-form-ids authenticated-request)
               (:covered-root-form-ids coverage)
               (vec (mapcat :root-form-ids fragments))))
        (is (= (mapv :form-id (:forms authenticated-request))
               (:covered-form-ids coverage)
               (vec (mapcat :form-ids fragments))))
        (is (= (mapv :binding-id local-bindings)
               (:covered-local-binding-ids coverage)
               (vec (mapcat :local-binding-ids fragments))))
        (is (= (mapv :reference-syntax-id
                     (:resolution-table authenticated-request))
               (:covered-resolution-reference-syntax-ids coverage)
               (vec
                (mapcat
                 :resolution-reference-syntax-ids fragments))))))))

(deftest sh07-b13-module-assembly-manifest-is-exact-and-bound
  (doseq [basename (sort (accepted-fixtures))
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          authenticated-request (request artifact)
          fragments (:fragment-manifest authenticated-request)
          assembly (:module-assembly-manifest (core artifact))
          lineage (:lineage authenticated-request)]
      (testing (str basename extension)
        (is (= module-assembly-keys (set (keys assembly))))
        (is (= assembly
               (:module-assembly-manifest (identity-input artifact))))
        (is (= (mapv :fragment-id fragments)
               (:ordered-fragment-ids assembly)))
        (is (= (:top-level-form-ids authenticated-request)
               (:root-form-ids assembly)))
        (is (sha256-id? (:module-id assembly)))
        (is (= (get-in authenticated-request
                       [:module :source-revision-id])
               (:source-revision-id lineage)
               (:source-revision-id assembly)))
        (is (= (:sh06-semantic-projection-id lineage)
               (:sh06-semantic-projection-id assembly)))
        (is (= (:alias-table-id lineage)
               (:alias-table-id assembly)))
        (is (sha256-id? (:content-id assembly)))))))

(deftest sh07-b13-real-authoritative-modules-cover-monolithic-and-fragmented
  (let [diagnostics (authoritative-artifact "diagnostics")
        syntax (authoritative-artifact "syntax")
        diagnostics-fragments
        (:fragment-manifest (request diagnostics))
        syntax-fragments (:fragment-manifest (request syntax))]
    (is (= :accepted (:status diagnostics) (:status syntax)))
    (is (= 1 (count diagnostics-fragments)))
    (is (< 1 (count syntax-fragments)))
    (is (= 1 (get-in (core diagnostics)
                     [:fragment-coverage :fragment-count])))
    (is (< 1 (get-in (core syntax)
                     [:fragment-coverage :fragment-count])))
    (is (= :passed
           (:status
            ((required-var 'sh07-core-artifact-verification)
             diagnostics))))
    (is (= :passed
           (:status
            ((required-var 'sh07-core-artifact-verification)
             syntax))))))

(deftest sh07-b13-cross-fragment-definitions-and-references-bind
  (doseq [extension extensions]
    (let [artifact
          (file-artifact "accepted" "cross-fragment-reference" extension)
          core-artifact (core artifact)
          definition
          (first
           (filter #(= "source-value" (str (:name %)))
                   (:definitions core-artifact)))
          use
          (first
           (filter #(= 'source-value (:symbol %))
                   (:reference-uses core-artifact)))
          definition-form
          (first
           (filter #(= (:syntax-id definition) (:syntax-id %))
                   (:forms (request artifact))))
          definition-fragment
          (fragment-for-form artifact (:form-id definition-form))
          use-fragment (fragment-for-form artifact (:form-id use))]
      (testing extension
        (is (map? definition))
        (is (map? use))
        (is (map? definition-form))
        (is (= (:binding-id definition) (:binding-id use)))
        (is (map? definition-fragment))
        (is (map? use-fragment))
        (is (not= (:fragment-id definition-fragment)
                  (:fragment-id use-fragment)))
        (is (some #{(:binding-id definition)}
                  (:external-binding-ids use-fragment)))))))

(deftest sh07-b13-b12-alias-value-and-call-survive-fragment-assembly
  (doseq [extension extensions]
    (let [artifact
          (file-artifact
           "accepted" "alias-root-aligned-fragments" extension)
          authenticated-request (request artifact)
          aliases (set (map :alias (:alias-table authenticated-request)))
          alias-resolutions
          (filterv
           #(= :alias-qualified-required-binding
               (:resolution-order %))
           (:resolution-table authenticated-request))
          positions (set (map :position alias-resolutions))
          fragments (:fragment-manifest authenticated-request)]
      (testing extension
        (is (= #{:expression :operator} positions))
        (is (seq (:calls (core artifact))))
        (is (every?
             (set (mapcat
                   :resolution-reference-syntax-ids fragments))
             (map :reference-syntax-id alias-resolutions)))
        (is (= aliases (set (mapcat :alias-names fragments))))
        (is (= (:alias-table authenticated-request)
               (:declared-alias-table (core artifact))))))))

(deftest sh07-b13-identities-are-deterministic-path-neutral-and-provenanced
  (let [fixture
        (fixture-path
         "accepted" "deterministic-top-level-fragments" ".gravity")
        temp-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh07-b13-cross-root-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        left-path (.resolve temp-root "left/module.gravity")
        right-path (.resolve temp-root "right/module.qst")]
    (try
      (doseq [target [left-path right-path]]
        (java.nio.file.Files/createDirectories
         (.getParent target)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write
         target
         (source-bytes fixture)
         (make-array java.nio.file.OpenOption 0)))
      (let [left ((required-var 'sh07-core-file-artifact) (str left-path))
            repeated
            ((required-var 'sh07-core-file-artifact) (str left-path))
            right ((required-var 'sh07-core-file-artifact) (str right-path))]
        (is (= :accepted (:status left) (:status right)))
        (is (= left repeated))
        (is (= (:artifact-id left) (:artifact-id right)))
        (is (= (identity-input left) (identity-input right)))
        (is (= (:fragment-manifest (request left))
               (:fragment-manifest (request right))))
        (is (= (:fragment-coverage (core left))
               (:fragment-coverage (core right))))
        (is (= (:module-assembly-manifest (core left))
               (:module-assembly-manifest (core right))))
        (is (= (str left-path)
               (get-in left [:provenance :source-path])
               (get-in (core left) [:provenance :actual-source-path])))
        (is (= (str right-path)
               (get-in right [:provenance :source-path])
               (get-in (core right) [:provenance :actual-source-path])))
        (is (not= (get-in left [:provenance :source-path])
                  (get-in right [:provenance :source-path]))))
      (finally
        (delete-tree! temp-root)))))

(deftest sh07-b13-fragment-request-alterations-fail-closed
  (let [artifact
        (file-artifact
         "accepted" "deterministic-top-level-fragments" ".gravity")
        authenticated-request (request artifact)
        fragments (:fragment-manifest authenticated-request)
        first-fragment (first fragments)
        second-fragment (second fragments)
        alternate-fragment
        (:fragment-manifest
         (request
          (file-artifact
           "accepted" "cross-fragment-reference" ".gravity")))
        cases
        (cond->
         {"missing"
          (assoc authenticated-request :fragment-manifest
                 (subvec fragments 1))
          "duplicate"
          (update authenticated-request :fragment-manifest
                  conj first-fragment)
          "reordered"
          (assoc authenticated-request :fragment-manifest
                 (vec (reverse fragments)))
          "root overlap"
          (assoc-in authenticated-request
                    [:fragment-manifest 1 :root-form-ids]
                    (:root-form-ids first-fragment))
          "form overlap"
          (assoc-in authenticated-request
                    [:fragment-manifest 1 :form-ids]
                    (:form-ids first-fragment))
          "fragment substitution"
          (assoc authenticated-request :fragment-manifest
                 alternate-fragment)}
          second-fragment
          (assoc
           "ordinal substitution"
           (assoc-in authenticated-request
                     [:fragment-manifest 1 :ordinal]
                     0)))]
    (is (< 1 (count fragments)))
    (doseq [[label altered-request] cases]
      (testing label
        (let [result
              (run-request
               (:sh06-resolution-artifact artifact)
               altered-request)
              diagnostic (diagnostic-data result)]
          (is (nil? (:raw-host-error result)))
          (is (= :gravity/sh07-core-diagnostic
                 (:artifact diagnostic)))
          (is (= "C6-VERIFY" (:rule diagnostic)))
          (is (= true
                 (get-in diagnostic [:facts :fail-closed]))))))))

(deftest sh07-b13-gravity-validates-fragment-plan-and-module-anchors
  (let [artifact
        (file-artifact
         "accepted" "deterministic-top-level-fragments" ".gravity")
        authenticated-request (request artifact)
        fragments (:fragment-manifest authenticated-request)
        first-forms (get-in fragments [0 :form-ids])
        second-forms (get-in fragments [1 :form-ids])
        first-bindings (get-in fragments [0 :local-binding-ids])
        first-resolutions
        (get-in fragments [0 :resolution-reference-syntax-ids])
        zero-id (str "sha256:" (apply str (repeat 64 "0")))
        cases
        (cond->
         {"planned root-node substitution"
          (assoc-in authenticated-request
                    [:fragment-manifest 0 :root-node-ids 0]
                    zero-id)
          "fragment content substitution"
          (assoc-in authenticated-request
                    [:fragment-manifest 0 :content-id]
                    zero-id)
          "fragment identity substitution"
          (assoc-in authenticated-request
                    [:fragment-manifest 0 :fragment-id]
                    zero-id)
          "module content substitution"
          (assoc-in authenticated-request
                    [:module-assembly-manifest :content-id]
                    zero-id)
          "module identity substitution"
          (assoc-in authenticated-request
                    [:module-assembly-manifest :module-id]
                    zero-id)
          "module order substitution"
          (update-in authenticated-request
                     [:module-assembly-manifest
                      :ordered-fragment-ids]
                     #(vec (reverse %)))
          "alias partition substitution"
          (assoc-in authenticated-request
                    [:fragment-manifest 0 :alias-names]
                    ['not-declared])}

          (and (< 1 (count first-forms))
               (seq second-forms))
          (assoc
           "form boundary shift"
           (-> authenticated-request
               (assoc-in
                [:fragment-manifest 0 :form-ids]
                (pop first-forms))
               (assoc-in
                [:fragment-manifest 1 :form-ids]
                (into [(peek first-forms)] second-forms))))

          (seq first-bindings)
          (assoc
           "local binding partition substitution"
           (-> authenticated-request
               (update-in
                [:fragment-manifest 0 :local-binding-ids]
                #(vec (rest %)))
               (update-in
                [:fragment-manifest 1 :local-binding-ids]
                #(into [(first first-bindings)] %))))

          (seq first-resolutions)
          (assoc
           "resolution partition substitution"
           (-> authenticated-request
               (update-in
                [:fragment-manifest 0
                 :resolution-reference-syntax-ids]
                #(vec (rest %)))
               (update-in
                [:fragment-manifest 1
                 :resolution-reference-syntax-ids]
                #(into [(first first-resolutions)] %)))))]
    (is (< 1 (count fragments)))
    (doseq [[label altered-request] cases]
      (testing label
        (let [result (structural-request altered-request)
              diagnostic (diagnostic-data result)]
          (is (nil? (:raw-host-error result)))
          (is (= :gravity/sh07-core-diagnostic
                 (:artifact diagnostic)))
          (is (= "C6-VERIFY" (:rule diagnostic)))
          (is (= true
                 (get-in diagnostic [:facts :fail-closed]))))))))

(deftest sh07-b13-module-resolvers-use-module-aggregate-bounds
  (let [source-path
        (fixture-path
         "accepted" "deterministic-top-level-fragments" ".gravity")
        execute
        (fn [function]
          ((required-var 'sh07-core-execute!)
           source-path function [(vec (repeat 1025 {})) []]))]
    (is (= {:status :rejected
            :reason :match-branch-record-shape}
           (execute 'sh07-resolve-module-match-branch-vector)))
    (is (= {:status :rejected
            :reason :match-decision-skeleton-shape}
           (execute
            'sh07-resolve-module-match-decision-skeleton-vector)))
    (is (= {:status :rejected
            :reason :match-pattern-record-shape}
           (execute 'sh07-resolve-module-match-pattern-vector)))))

(deftest sh07-b13-stale-sh06-and-alias-binding-resolution-fail-closed
  (let [alias-artifact
        (file-artifact
         "accepted" "alias-root-aligned-fragments" ".gravity")
        alternate-artifact
        (file-artifact
         "accepted" "cross-fragment-reference" ".gravity")
        authenticated-request (request alias-artifact)
        alias-resolution-index
        (first
         (keep-indexed
          (fn [index resolution]
            (when (= :alias-qualified-required-binding
                     (:resolution-order resolution))
              index))
          (:resolution-table authenticated-request)))
        resolution
        (get (:resolution-table authenticated-request)
             alias-resolution-index)
        binding-index
        (first
         (keep-indexed
          (fn [index binding]
            (when (= (:binding-id resolution) (:binding-id binding))
              index))
          (:binding-table authenticated-request)))
        cases
        {"stale SH06 artifact"
         [(:sh06-resolution-artifact alternate-artifact)
          authenticated-request]
         "alias binding substitution"
         [(:sh06-resolution-artifact alias-artifact)
          (assoc-in authenticated-request
                    [:binding-table binding-index :namespace]
                    'self-hosting.sh07-b13.changed)]
         "alias resolution substitution"
         [(:sh06-resolution-artifact alias-artifact)
          (assoc-in authenticated-request
                    [:resolution-table alias-resolution-index
                     :resolution-order]
                    :fully-qualified-namespace-binding)]}]
    (is (integer? alias-resolution-index))
    (is (integer? binding-index))
    (doseq [[label [resolution-artifact altered-request]] cases]
      (testing label
        (let [result (run-request resolution-artifact altered-request)
              diagnostic (diagnostic-data result)]
          (is (nil? (:raw-host-error result)))
          (is (= :gravity/sh07-core-diagnostic
                 (:artifact diagnostic)))
          (is (= "C6-VERIFY" (:rule diagnostic)))
          (is (= true
                 (get-in diagnostic [:facts :fail-closed]))))))))

(deftest sh07-b13-fragment-module-and-coverage-products-fail-replay
  (let [artifact
        (file-artifact
         "accepted" "deterministic-top-level-fragments" ".gravity")
        cases
        {"fragment removal"
         {:expected-check :fragment-manifest-replay?
          :artifact
          (update-in
           artifact
           [:gravity-core-boundary :canonical-core-artifact
            :fragment-manifest]
           pop)}
         "coverage substitution"
         {:expected-check :fragment-coverage-replay?
          :artifact
          (update-in
           artifact
           [:gravity-core-boundary :canonical-core-artifact
            :fragment-coverage :fragment-count]
           inc)}
         "module order substitution"
         {:expected-check :module-assembly-manifest-replay?
          :artifact
          (update-in
           artifact
           [:gravity-core-boundary :canonical-core-artifact
            :module-assembly-manifest :ordered-fragment-ids]
           #(vec (reverse %)))}
         "module content substitution"
         {:expected-check :module-replay?
          :artifact
          (assoc-in
           artifact
           [:gravity-core-boundary :canonical-core-artifact
            :module-assembly-manifest :content-id]
           (str "sha256:" (apply str (repeat 64 "0"))))}}]
    (doseq [[label {:keys [expected-check artifact]}] cases]
      (testing label
        (let [altered artifact
              expected
              (file-artifact
               "accepted"
               "deterministic-top-level-fragments"
               ".gravity")
              failed (verification-failures altered expected)]
          (is (not= expected altered))
          (is (contains? failed expected-check))
          (is (= :failed
                 (:status
                  ((required-var 'sh07-core-artifact-verification)
                   altered)))))))))

(deftest sh07-b13-public-replay-and-capability-proof-pass
  (doseq [artifact
          (concat
           (for [basename (sort (accepted-fixtures))]
             (file-artifact "accepted" basename ".gravity"))
           [(authoritative-artifact "diagnostics")
            (authoritative-artifact "syntax")])]
    (let [report
          ((required-var 'sh07-core-artifact-verification) artifact)
          proof
          ((required-var 'sh07-core-capability-based-proof) artifact)]
      (is (= :gravity/sh07-core-artifact-verification
             (:artifact report)))
      (is (= :passed (:status report)))
      (is (= [] (:failed-checks report)))
      (is (= :gravity/sh07-core-capability-proof
             (:artifact proof)))
      (is (= :complete (:status proof)))
      (is (= [] (:failed-checks proof))))))

(deftest sh07-b13-rejected-fixtures-follow-declared-oracles
  (doseq [basename (sort (rejected-fixtures))
          extension extensions]
    (testing (str basename extension)
      (let [source-path (fixture-path "rejected" basename extension)
            oracle (fixture-oracle basename extension)
            result
            (diagnostic-result
             #((required-var 'sh07-core-file-artifact) source-path))
            diagnostic (diagnostic-data result)]
        (is (map? oracle))
        (is (nil? (:raw-host-error result)))
        (is (= (:expected-rule oracle) (:rule diagnostic)))
        (is (= (:expected-stage oracle) (:stage diagnostic)))
        (is (= (:expected-severity oracle) (:severity diagnostic)))
        (is (= (:expected-reason oracle)
               (get-in diagnostic [:facts :reason])))
        (is (= (:expected-remediation oracle)
               (:remediation diagnostic)))
        (is (= source-path
               (get-in diagnostic [:source-span :source])))
        (is (= true
               (get-in diagnostic [:facts :fail-closed])))))))

(deftest sh07-b13-rejected-corpus-covers-size-and-form-boundaries
  (let [oracles
        (for [basename (sort (rejected-fixtures))]
          (fixture-oracle basename ".gravity"))
        reasons (set (map :expected-reason oracles))]
    (is (some
         #(or (str/includes? (name %) "oversized")
              (str/includes? (name %) "root-limit")
              (str/includes? (name %) "fragment-limit")
              (str/includes? (name %) "fragment-root-form-bound"))
         reasons))
    (is (some
         #(or (str/includes? (name %) "unsupported")
              (str/includes? (name %) "lowering-gap"))
         reasons))))

(deftest sh07-b13-claim-boundary-remains-honest
  (let [artifact
        (file-artifact
         "accepted" "deterministic-top-level-fragments" ".gravity")
        pending
        (set
         (get-in artifact
                 [:execution-boundary :pending-lowering-families]))]
    (is (false? (get-in artifact
                        [:execution-boundary :sh07-complete?])))
    (is (false? (get-in artifact
                        [:execution-boundary :self-hosted?])))
    (is (false? (get-in artifact
                        [:gravity-core-boundary :self-hosted?])))
    (is (true? (get-in artifact
                       [:gravity-core-boundary
                        :clojure-adapter-residual?])))
    (doseq [family
            [:cross-file-module-linking
             :incremental-fragment-cache
             :parallel-fragment-lowering
             :whole-program-execution
             :keyword-default-value-lookup
             :general-callable-keywords
             :var-profile-legality-sh09
             :destructuring-bindings
             :variadic-function-recur
             :general-recursion
             :try-finally
             :variable-width-vector-patterns
             :guard-patterns
             :match-exhaustiveness
             :match-result-type-join]]
      (is (contains? pending family)))))
