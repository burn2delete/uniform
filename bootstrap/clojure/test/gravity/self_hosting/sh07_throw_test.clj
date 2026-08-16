(ns gravity.self-hosting.sh07-throw-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh07_throw_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07-B7 test source is not on the classpath"
                      {:id "SH07-B7-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-B7-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b7")
(def ^:private extensions [".gravity" ".qst"])
(def ^:private accepted-basenames
  ["top-level-throw"
   "function-throw"
   "conditional-throws"
   "nested-throw-value"])
(def ^:private shape-remediation
  "Provide a bounded, delimiter-linked SH-06 form graph with exact core-form shape.")
(def ^:private gap-remediation
  "Use only the declared bounded SH-07 core subset; defer unsupported lowering families to their owning slices.")
(def ^:private recur-remediation
  "Place recur in tail position inside the nearest loop or fixed-arity function target and pass exactly the target arity.")
(def ^:private rejected-oracles
  {"throw-zero-arity"
   {:rule "C6-CORE-SHAPE"
    :reason :throw-arity
    :facts {:actual-operand-count 0
            :required-operand-count 1}
    :remediation shape-remediation}
   "throw-many-arity"
   {:rule "C6-CORE-SHAPE"
    :reason :throw-arity
    :facts {:actual-operand-count 2
            :required-operand-count 1}
    :remediation shape-remediation}
   "recur-in-throw-value"
   {:rule "C6-VERIFY"
    :reason :recur-tail-position-required
    :semantic-rule "L2-RECUR-TARGET"
    :remediation recur-remediation}
   "try-remains-deferred"
   {:rule "C6-CORE-SHAPE"
    :reason :try-catch-required
    :remediation
    "Provide exactly one protected expression followed by exactly one typed catch clause."}})
(def ^:private error-transfer-record-keys
  #{:ordinal :core-node-id :form-id :syntax-id
    :value-core-node-id :evaluated-children
    :evaluation-region :evaluation-owner-function-syntax-id
    :ordered-steps :construction-order :runtime-reachability
    :transfer-policy :result-policy :required-effect
    :authenticated-sh06-artifact-id
    :sh06-semantic-projection-id
    :type-legality :effect-registry-legality
    :effect-profile-capability-legality
    :profile-error-lowering-legality
    :ownership-legality :safety-classification})
(def ^:private error-transfer-attribute-keys
  #{:value-child-index :evaluation-order
    :construction-order :runtime-reachability
    :transfer-policy :result-policy :required-effect
    :authenticated-sh06-artifact-id
    :sh06-semantic-projection-id
    :type-legality :effect-registry-legality
    :effect-profile-capability-legality
    :profile-error-lowering-legality
    :ownership-legality :safety-classification})
(def ^:private public-artifact-keys
  #{:kind :status :slice :task :document-set :governing-document
    :artifact-id :sh06-resolution-artifact :gravity-core-boundary
    :provenance :pass :execution-boundary :capability-based-proof
    :diagnostics})

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- fixture-path
  [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- fixture-basenames
  [family extension]
  (let [directory (io/file (path (str fixture-root "/" family)))]
    (->> (.listFiles directory)
         (filter #(.isFile %))
         (map #(.getName %))
         (filter #(str/ends-with? % extension))
         (map #(subs % 0 (- (count %) (count extension))))
         set)))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

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
        "Required SH-07-B7 coordinator adapter is absent"
        {:id "SH07-B7-ADAPTER-ABSENT"
         :symbol symbol
         :required-signatures
         {'sh07-core-source-artifact '[source-path source-text]
          'sh07-core-file-artifact '[source-path]
          'sh07-core-artifact-verification '[artifact]
          'sh07-core-artifact-identity-input '[artifact]
          'sh07-core-verification-checks
          '[artifact expected upstream-verification]
          'sh07-core-from-authenticated-request
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
        clause
        (some #(when (and (seq? %) (= :metadata (first %))) %)
              (drop 2 ns-form))]
    (get (second clause) :sh07-b7)))

(defn- core
  [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))

(defn- request
  [artifact]
  (get-in artifact [:gravity-core-boundary :authenticated-core-request]))

(defn- identity-input
  [artifact]
  ((required-var 'sh07-core-artifact-identity-input) artifact))

(defn- exactly-once-index
  [records key-name]
  (let [index (into {} (map (juxt key-name identity)) records)]
    (when-not (= (count records) (count index))
      (throw
       (ex-info "SH-07-B7 records are not uniquely identifiable"
                {:id "SH07-B7-AMBIGUOUS-INDEX"
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
      {:raw-host-error {:class (.getName (class throwable))
                        :message (.getMessage throwable)}})))

(defn- diagnostic-data
  [result]
  (let [data (:exception-data result)
        value (:value result)]
    (or (when (= :gravity/sh07-core-diagnostic (:artifact data)) data)
        (when (= :gravity/sh07-core-diagnostic
                 (get-in data [:diagnostic :artifact]))
          (:diagnostic data))
        (when (= :gravity/sh07-core-diagnostic (:artifact value)) value)
        (when (= :gravity/sh07-core-diagnostic
                 (get-in value [:diagnostic :artifact]))
          (:diagnostic value)))))

(deftest sh07-b7-fixtures-are-paired-complete-and-byte-identical
  (doseq [family ["accepted" "rejected"]
          extension extensions]
    (is (= (if (= family "accepted")
             (set accepted-basenames)
             (set (keys rejected-oracles)))
           (fixture-basenames family extension))))
  (doseq [family ["accepted" "rejected"]
          basename (if (= family "accepted")
                     accepted-basenames
                     (keys rejected-oracles))]
    (is (= (seq (source-bytes
                 (fixture-path family basename ".gravity")))
           (seq (source-bytes
                 (fixture-path family basename ".qst")))))))

(deftest sh07-b7-accepted-pairs-use-current-schema-and-path-neutral-products
  (doseq [basename accepted-basenames]
    (let [gravity (file-artifact "accepted" basename ".gravity")
          qst (file-artifact "accepted" basename ".qst")]
      (testing basename
        (is (= :accepted (:status gravity) (:status qst)))
        (is (= (:artifact-id gravity) (:artifact-id qst)))
        (is (= (identity-input gravity) (identity-input qst)))
        (is (= (:error-transfers (core gravity))
               (:error-transfers (core qst))))
        (is (= 15 (:schema-version (request gravity))
               (:schema-version (request qst))))
        (is (= :sh07-b15-keyword-map-lookup
               (:scope (request gravity))
               (:scope (request qst))))
        (is (= "SH-07-B47" (:task gravity) (:task qst)))
        (is (= :c6-gravity-core-lowering-b47
               (get-in gravity [:pass :name])
               (get-in qst [:pass :name])))))))

(deftest sh07-b7-throw-products-have-exact-shape-and-pending-legality
  (doseq [basename accepted-basenames
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          core-artifact (core artifact)
          records (:error-transfers core-artifact)
          bounds
          (get-in artifact
                  [:gravity-core-boundary
                   :raw-template-result :bounds])
          maximum-error-transfer-records
          (:maximum-error-transfer-records bounds)
          nodes (exactly-once-index (:nodes core-artifact) :node-id)
          source-map
          (exactly-once-index (:source-map core-artifact)
                              :core-node-id)
          lineage (:lineage (request artifact))]
      (testing (str basename extension)
        (is (seq records))
        (is (= (vec (range (count records)))
               (mapv :ordinal records)))
        (is (= 1024 maximum-error-transfer-records))
        (is (<= (count records)
                maximum-error-transfer-records))
        (doseq [record records]
          (let [node (get nodes (:core-node-id record))
                source (get source-map (:core-node-id record))
                value-node-id (:value-core-node-id record)
                attributes (:attributes node)
                shared-keys
                [:construction-order :runtime-reachability
                 :transfer-policy :result-policy :required-effect
                 :authenticated-sh06-artifact-id
                 :sh06-semantic-projection-id
                 :type-legality :effect-registry-legality
                 :effect-profile-capability-legality
                 :profile-error-lowering-legality
                 :ownership-legality :safety-classification]]
            (is (= error-transfer-record-keys
                   (set (keys record))))
            (is (= error-transfer-attribute-keys
                   (set (keys attributes))))
            (is (= (select-keys record shared-keys)
                   (select-keys attributes shared-keys)))
            (is (= :throw (:core-form node)))
            (is (= (:form-id record)
                   (get-in node [:source :form-id])
                   (:form-id source)))
            (is (= (:syntax-id record)
                   (get-in node [:source :syntax-id])
                   (:syntax-id source)))
            (is (= [value-node-id] (:children node)))
            (is (= [{:index 0 :core-node-id value-node-id}]
                   (get-in node [:evaluation :order])))
            (is (= :value-then-transfer
                   (get-in node [:evaluation :kind])
                   (:evaluation-order attributes)))
            (is (= (:evaluation-region record)
                   (get-in node [:evaluation :region])))
            (is (= (:evaluation-owner-function-syntax-id record)
                   (get-in node
                           [:evaluation
                            :owner-function-syntax-id])))
            (is (= [value-node-id] (:evaluated-children record)))
            (is (= [:evaluate-value :transfer-error]
                   (:ordered-steps record)))
            (is (= :postorder-after-value
                   (:construction-order record)))
            (is (= :not-asserted-by-sh07-b7
                   (:runtime-reachability record)))
            (is (= :error-transfer
                   (:transfer-policy record)))
            (is (= :never (:result-policy record)))
            (is (= :error/throw (:required-effect record)))
            (is (= (:authenticated-sh06-artifact-id lineage)
                   (:authenticated-sh06-artifact-id record)))
            (is (= (:sh06-semantic-projection-id lineage)
                   (:sh06-semantic-projection-id record)))
            (is (= :pending-sh08 (:type-legality record)))
            (is (= :pending-sh09
                   (:effect-registry-legality record)
                   (:effect-profile-capability-legality record)
                   (:profile-error-lowering-legality record)))
            (is (= :pending-sh10 (:ownership-legality record)))
            (is (= :pending-sh11
                   (:safety-classification record)))))))))

(deftest sh07-b7-function-branch-and-nested-order-are-explicit
  (doseq [extension extensions]
    (let [function-artifact
          (file-artifact "accepted" "function-throw" extension)
          function-record
          (first (:error-transfers (core function-artifact)))
          conditional
          (file-artifact "accepted" "conditional-throws" extension)
          conditional-records (:error-transfers (core conditional))
          nested
          (file-artifact "accepted" "nested-throw-value" extension)
          nested-core (core nested)
          nested-records (:error-transfers nested-core)
          nodes (exactly-once-index (:nodes nested-core) :node-id)
          inner (first nested-records)
          outer (second nested-records)
          outer-node (get nodes (:core-node-id outer))]
      (is (some? (:evaluation-owner-function-syntax-id
                  function-record)))
      (is (= :function-body
             (:evaluation-region function-record)))
      (is (= [:then :else]
             (mapv #(get-in % [:evaluation-region :role])
                   conditional-records)))
      (is (every?
           #(= :conditional-branch
               (get-in % [:evaluation-region :kind]))
           conditional-records))
      (is (every?
           #(= :selected-exactly-once
               (get-in % [:evaluation-region :execution]))
           conditional-records))
      (is (= [0 1] (mapv :ordinal nested-records)))
      (is (= (:core-node-id inner)
             (:value-core-node-id outer)))
      (is (= [(:core-node-id inner)]
             (:evaluated-children outer)
             (:children outer-node)))
      (is (= [:postorder-after-value :postorder-after-value]
             (mapv :construction-order nested-records)))
      (is (= [:not-asserted-by-sh07-b7
              :not-asserted-by-sh07-b7]
             (mapv :runtime-reachability nested-records))))))

(deftest sh07-b7-fresh-cross-root-identity-retains-actual-path
  (let [fixture
        (fixture-path "accepted" "nested-throw-value" ".gravity")
        temp-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh07-b7-cross-root-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        left-path (.resolve temp-root "left/throw.gravity")
        right-path (.resolve temp-root "right/throw.qst")]
    (try
      (doseq [target [left-path right-path]]
        (java.nio.file.Files/createDirectories
         (.getParent target)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write
         target
         (source-bytes fixture)
         (make-array java.nio.file.OpenOption 0)))
      (let [left
            ((required-var 'sh07-core-file-artifact) (str left-path))
            right
            ((required-var 'sh07-core-file-artifact) (str right-path))]
        (is (= :accepted (:status left) (:status right)))
        (is (= (:artifact-id left) (:artifact-id right)))
        (is (= (identity-input left) (identity-input right)))
        (is (= (count (:error-transfers (core left)))
               (count (:error-transfers (core right)))))
        (is (= (str left-path)
               (get-in left [:provenance :source-path])
               (get-in (core left)
                       [:provenance :actual-source-path])))
        (is (= (str right-path)
               (get-in right [:provenance :source-path])
               (get-in (core right)
                       [:provenance :actual-source-path])))
        (is (not= (get-in left [:provenance :source-path])
                  (get-in right [:provenance :source-path]))))
      (finally
        (delete-tree! temp-root)))))

(deftest sh07-b7-rejections-are-structured-and-oracle-bound
  (doseq [[basename oracle] rejected-oracles
          extension extensions]
    (testing (str basename extension)
      (let [source-path (fixture-path "rejected" basename extension)
            declared (fixture-oracle basename extension)
            result
            (diagnostic-result
             #((required-var 'sh07-core-file-artifact) source-path))
            diagnostic (diagnostic-data result)]
        (is (= {:expected-rule (:rule oracle)
                :expected-stage :core-lowering
                :expected-severity :error
                :expected-reason (:reason oracle)
                :expected-remediation (:remediation oracle)}
               declared))
        (is (nil? (:raw-host-error result)))
        (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
        (is (= (:rule oracle) (:rule diagnostic)))
        (is (= :core-lowering (:stage diagnostic)))
        (is (= :error (:severity diagnostic)))
        (is (= (:reason oracle)
               (get-in diagnostic [:facts :reason])))
        (doseq [[key expected] (:facts oracle)]
          (is (= expected
                 (get-in diagnostic
                         [:facts :rule-specific key]))))
        (when (:semantic-rule oracle)
          (is (= (:semantic-rule oracle)
                 (get-in diagnostic
                         [:facts :semantic-rule]))))
        (is (= (:remediation oracle) (:remediation diagnostic)))
        (is (= source-path (get-in diagnostic [:source-span :source])))
        (is (true? (get-in diagnostic [:facts :fail-closed])))))))

(deftest sh07-b7-authenticated-input-substitution-fails-before-lowering
  (let [artifact
        (file-artifact "accepted" "top-level-throw" ".gravity")
        authenticated (request artifact)
        substituted
        (assoc-in
         authenticated
         [:lineage :sh06-semantic-projection-id]
         "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
        result
        (diagnostic-result
         #((required-var 'sh07-core-from-authenticated-request)
           (:sh06-resolution-artifact artifact)
           substituted))
        diagnostic (diagnostic-data result)]
    (is (not= authenticated substituted))
    (is (nil? (:raw-host-error result)))
    (is (= :gravity/sh07-core-diagnostic
           (:artifact diagnostic)))
    (is (= "C6-VERIFY" (:rule diagnostic)))
    (is (= :authenticated-sh06-projection-mismatch
           (get-in diagnostic [:facts :reason])))
    (is (true? (get-in diagnostic [:facts :fail-closed])))))

(deftest sh07-b7-error-transfer-alterations-fail-replay
  (let [artifact
        (file-artifact "accepted" "nested-throw-value" ".gravity")
        second-node-id
        (get-in artifact
                [:gravity-core-boundary :canonical-core-artifact
                 :error-transfers 1 :value-core-node-id])
        first-node-id
        (get-in artifact
                [:gravity-core-boundary :canonical-core-artifact
                 :error-transfers 0 :core-node-id])
        node-index
        (first
         (keep-indexed
          (fn [index node]
            (when (= first-node-id (:node-id node)) index))
          (get-in artifact
                  [:gravity-core-boundary
                   :canonical-core-artifact :nodes])))
        alterations
        {"result policy"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :error-transfers 0 :result-policy]
          :value)
         "required effect"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :error-transfers 0 :required-effect]
          :pure)
         "construction ordinal"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :error-transfers 0 :ordinal]
          9)
         "value child"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :error-transfers 0 :value-core-node-id]
          second-node-id)
         "record removal"
         (update-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :error-transfers]
          pop)
         "node transfer policy"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :nodes node-index :attributes :transfer-policy]
          :host-exception)}]
    (doseq [[label altered] alterations]
      (testing label
        (let [checks
              ((required-var 'sh07-core-verification-checks)
               altered artifact {:status :passed})
              failed
              (set (for [[check passed?] checks
                         :when (not (true? passed?))]
                     check))]
          (is (not= artifact altered))
          (is (contains? failed :canonical-core-replays?))
          (is (contains? failed :error-transfers-replay?))
          (is (contains? failed :authoritative-products-replay?))
          (is (= :failed
                 (:status
                  ((required-var
                    'sh07-core-artifact-verification)
                   altered)))))))))

(deftest sh07-b7-public-proof-is-bounded-and-does-not-claim-handlers
  (doseq [basename accepted-basenames]
    (let [artifact (file-artifact "accepted" basename ".gravity")
          proof (:capability-based-proof artifact)
          boundary (:gravity-core-boundary artifact)
          canonical (:canonical-core-artifact boundary)
          pending
          (get-in artifact
                  [:execution-boundary
                   :pending-lowering-families])]
      (testing basename
        (is (= public-artifact-keys (set (keys artifact))))
        (is (= :gravity/sh07-core-artifact (:kind artifact)))
        (is (= :accepted (:status artifact)))
        (is (= :SH-07 (:slice artifact)))
        (is (= ["L2" "L3" "L6" "L7" "L9" "C5" "C6"]
               (:document-set artifact)))
        (is (= :gravity/sh07-to-c6-core-products-v16
               (:adapter-contract boundary)))
        (is (= :gravity/sh07-core-capability-proof
               (:artifact proof)))
        (is (= :complete (:status proof)))
        (is (= [] (:failed-checks proof)))
        (is (true? (:error-transfers-replay? proof)))
        (is (= :passed
               (get-in boundary [:template-verification :status])))
        (is (= :passed
               (get-in boundary [:resolved-verification :status])))
        (is (every? (set pending)
                    [:try-finally
                     :try-protected-sequencing
                     :try-handler-sequencing]))
        (is (not-any? #{:try-handlers} pending))
        (is (not-any? #{:exceptions}
                      pending))
        (is (= [:types :effects :ownership :safety]
               (:pending-fact-families canonical)))))))
