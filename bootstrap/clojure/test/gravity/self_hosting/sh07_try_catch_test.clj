(ns gravity.self-hosting.sh07-try-catch-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh07_try_catch_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07-B8 test source is not on the classpath"
                      {:id "SH07-B8-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-B8-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b8")
(def ^:private extensions [".gravity" ".qst"])
(def ^:private accepted-basenames
  ["top-level-typed-catch"
   "function-typed-catch"
   "conditional-protected"
   "nested-typed-catches"])
(def ^:private handler-shape-remediation
  "Provide exactly one protected expression followed by one or more typed catch clauses.")
(def ^:private catch-shape-remediation
  "Use a resolved type symbol, one catch-local binding symbol, and exactly one handler expression.")
(def ^:private rejected-oracles
  {"missing-catch"
   {:rule "C6-CORE-SHAPE"
    :reason :try-catch-required
    :remediation handler-shape-remediation}
   "empty-protected-expression"
   {:rule "C6-CORE-SHAPE"
    :reason :try-protected-expression-required
    :remediation
    "Provide exactly one protected expression before the typed catch clauses."}
   "empty-handler-expression"
   {:rule "C6-CORE-SHAPE"
    :reason :catch-handler-expression-required
    :remediation
    "Provide exactly one handler expression after the catch-local binding."}
   "non-symbol-error-type"
   {:rule "C6-CORE-SHAPE"
    :reason :catch-type-symbol-required
    :remediation catch-shape-remediation}
   "non-symbol-catch-binding"
   {:rule "C6-CORE-SHAPE"
    :reason :catch-binding-symbol-required
    :remediation catch-shape-remediation}
   "protected-sequencing-deferred"
   {:rule "C6-LOWERING-GAP"
    :reason :try-protected-sequencing-deferred
    :remediation
    "Wrap protected sequencing in an explicit do form; SH-07-B11 accepts one protected expression."}
   "handler-sequencing-deferred"
   {:rule "C6-LOWERING-GAP"
    :reason :catch-handler-sequencing-deferred
    :remediation
    "Wrap handler sequencing in an explicit do form; SH-07-B11 accepts one handler expression per catch clause."}
   "finally-remains-deferred"
   {:rule "C6-LOWERING-GAP"
    :reason :finally-clause-deferred
    :remediation
    "Omit finally in SH-07-B11; defer cleanup lowering to its owning slice."}})
(def ^:private error-handler-record-keys
  #{:ordinal :clause-ordinal :clause-count :handler-child-index
    :core-node-id :form-id :syntax-id
    :protected-core-node-id :handler-core-node-id
    :catch-clause-form-id :catch-clause-syntax-id
    :error-type-form-id :error-type-syntax-id :error-type-binding-id
    :catch-binding-form-id :catch-binding-syntax-id
    :catch-binding-id :catch-binding-scope-id
    :catch-binding-use-syntax-ids
    :candidate-error-transfers
    :evaluation-region :evaluation-owner-function-syntax-id
    :ordered-steps :construction-order :runtime-evaluation
    :runtime-reachability :selection-policy :result-policy
    :authenticated-sh06-artifact-id
    :sh06-semantic-projection-id
    :type-coverage-legality :result-type-join-legality
    :effect-registry-legality
    :effect-profile-capability-legality
    :profile-error-lowering-legality
    :ownership-legality :safety-classification})
(def ^:private error-handler-attribute-keys
  #{:protected-child-index :handler-count :handler-child-indexes
    :handler-clauses :evaluation-order :runtime-reachability
    :selection-policy :result-policy
    :authenticated-sh06-artifact-id
    :sh06-semantic-projection-id
    :type-coverage-legality :result-type-join-legality
    :effect-registry-legality
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
        "Required SH-07-B8 coordinator adapter is absent"
        {:id "SH07-B8-ADAPTER-ABSENT"
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
    (get (second clause) :sh07-b8)))

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
       (ex-info "SH-07-B8 records are not uniquely identifiable"
                {:id "SH07-B8-AMBIGUOUS-INDEX"
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

(defn- node-descendants
  [nodes root-id]
  (loop [pending [root-id] seen #{}]
    (if (empty? pending)
      seen
      (let [node-id (peek pending)
            remaining (pop pending)]
        (if (contains? seen node-id)
          (recur remaining seen)
          (recur (into remaining (:children (get nodes node-id)))
                 (conj seen node-id)))))))

(deftest sh07-b8-fixtures-are-paired-complete-and-byte-identical
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

(deftest sh07-b8-accepted-pairs-use-v11-and-path-neutral-products
  (doseq [basename accepted-basenames]
    (let [gravity (file-artifact "accepted" basename ".gravity")
          qst (file-artifact "accepted" basename ".qst")]
      (testing basename
        (is (= :accepted (:status gravity) (:status qst)))
        (is (= (:artifact-id gravity) (:artifact-id qst)))
        (is (= (identity-input gravity) (identity-input qst)))
        (is (= (:error-handlers (core gravity))
               (:error-handlers (core qst))))
        (is (= 13 (:schema-version (request gravity))
               (:schema-version (request qst))))
        (is (= :sh07-b12-meta-jvm-core
               (:scope (request gravity))
               (:scope (request qst))))
        (is (= "SH-07-B12" (:task gravity) (:task qst)))
        (is (= :c6-gravity-core-lowering-b12
               (get-in gravity [:pass :name])
               (get-in qst [:pass :name])))))))

(deftest sh07-b8-error-handler-products-have-exact-bidirectional-shape
  (doseq [basename accepted-basenames
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          core-artifact (core artifact)
          records (:error-handlers core-artifact)
          transfers (:error-transfers core-artifact)
          transfer-index (exactly-once-index transfers :ordinal)
          nodes (exactly-once-index (:nodes core-artifact) :node-id)
          source-map
          (exactly-once-index (:source-map core-artifact)
                              :core-node-id)
          bindings
          (exactly-once-index (:binding-table (request artifact))
                              :binding-id)
          forms
          (exactly-once-index (:forms (request artifact)) :form-id)
          resolutions (:resolution-table (request artifact))
          lineage (:lineage (request artifact))
          maximum
          (get-in artifact
                  [:gravity-core-boundary :raw-template-result
                   :bounds :maximum-error-handler-records])]
      (testing (str basename extension)
        (is (seq records))
        (is (= (vec (range (count records)))
               (mapv :ordinal records)))
        (is (= 1024 maximum))
        (is (<= (count records) maximum))
        (is (=
             (mapv
              #(assoc % :authenticated-sh06-artifact-id
                      (:sh06-semantic-projection-id %))
              records)
             (:error-handlers (identity-input artifact))))
        (doseq [record records]
          (let [node (get nodes (:core-node-id record))
                protected-id (:protected-core-node-id record)
                handler-id (:handler-core-node-id record)
                protected-tree (node-descendants nodes protected-id)
                source (get source-map (:core-node-id record))
                attributes (:attributes node)
                catch-clause (get forms (:catch-clause-form-id record))
                error-type (get forms (:error-type-form-id record))
                catch-binding-form
                (get forms (:catch-binding-form-id record))
                error-type-binding
                (get bindings (:error-type-binding-id record))
                catch-binding (get bindings (:catch-binding-id record))
                shared-keys
                [:runtime-reachability :selection-policy :result-policy
                 :authenticated-sh06-artifact-id
                 :sh06-semantic-projection-id
                 :type-coverage-legality :result-type-join-legality
                 :effect-registry-legality
                 :effect-profile-capability-legality
                 :profile-error-lowering-legality
                 :ownership-legality :safety-classification]]
            (is (= error-handler-record-keys
                   (set (keys record))))
            (is (= error-handler-attribute-keys
                   (set (keys attributes))))
            (is (= (select-keys record shared-keys)
                   (select-keys attributes shared-keys)))
            (is (= :try (:core-form node)))
            (is (= [protected-id handler-id] (:children node)))
            (is (= [{:index 0 :core-node-id protected-id}]
                   (get-in node [:evaluation :order])))
            (is (= :protected-then-ordered-typed-handler-candidates
                   (get-in node [:evaluation :kind])
                   (:evaluation-order attributes)))
            (is (= 0 (:protected-child-index attributes)))
            (is (= 1 (:handler-count attributes)))
            (is (= [1] (:handler-child-indexes attributes)))
            (is (= [(select-keys
                     record
                     [:clause-ordinal :handler-child-index
                      :catch-clause-form-id :catch-clause-syntax-id
                      :error-type-form-id :error-type-syntax-id
                      :error-type-binding-id
                      :catch-binding-form-id
                      :catch-binding-syntax-id
                      :catch-binding-id :catch-binding-scope-id])]
                   (:handler-clauses attributes)))
            (is (= 0 (:clause-ordinal record)))
            (is (= 1 (:clause-count record)
                   (:handler-child-index record)))
            (is (= (:form-id record)
                   (get-in node [:source :form-id])
                   (:form-id source)))
            (is (= (:syntax-id record)
                   (get-in node [:source :syntax-id])
                   (:syntax-id source)))
            (is (= [:evaluate-protected
                    :conditionally-select-source-ordered-typed-handler]
                   (:ordered-steps record)))
            (is (= :protected-then-handlers-source-order-then-try
                   (:construction-order record)))
            (is (= :protected-then-conditional-handler-chain
                   (:runtime-evaluation record)))
            (is (= :not-asserted-by-sh07-b9
                   (:runtime-reachability record)))
            (is (= :source-ordered-typed-handler-candidates
                   (:selection-policy record)))
            (is (= :protected-or-selected-handler-last
                   (:result-policy record)))
            (is (= (:authenticated-sh06-artifact-id lineage)
                   (:authenticated-sh06-artifact-id record)))
            (is (= (:sh06-semantic-projection-id lineage)
                   (:sh06-semantic-projection-id record)))
            (is (= :pending-sh08
                   (:type-coverage-legality record)
                   (:result-type-join-legality record)))
            (is (= :pending-sh09
                   (:effect-registry-legality record)
                   (:effect-profile-capability-legality record)
                   (:profile-error-lowering-legality record)))
            (is (= :pending-sh10 (:ownership-legality record)))
            (is (= :pending-sh11
                   (:safety-classification record)))
            (is (= :list (:kind catch-clause)))
            (is (= (:catch-clause-syntax-id record)
                   (:syntax-id catch-clause)))
            (is (= :symbol (:kind error-type)))
            (is (= (:error-type-syntax-id record)
                   (:syntax-id error-type)))
            (is (= :type (:kind error-type-binding)))
            (is (= :core (:binding-class error-type-binding)))
            (is (= (:value error-type)
                   (:name error-type-binding)))
            (is (some
                 #(and (= (:error-type-syntax-id record)
                          (:reference-syntax-id %))
                       (= :type (:position %))
                       (= (:error-type-binding-id record)
                          (:binding-id %)))
                 resolutions))
            (is (= :symbol (:kind catch-binding-form)))
            (is (= (:catch-binding-syntax-id record)
                   (:syntax-id catch-binding-form)))
            (is (map? catch-binding))
            (is (= :local (:kind catch-binding)))
            (is (= :lexical (:binding-class catch-binding)))
            (is (= (:value catch-binding-form)
                   (:name catch-binding)))
            (is (= (:catch-binding-syntax-id record)
                   (:definition-syntax-id catch-binding)))
            (is (= (:catch-binding-scope-id record)
                   (:scope-id catch-binding)
                   (:scope-id catch-binding-form)))
            (doseq [use-syntax-id
                    (:catch-binding-use-syntax-ids record)]
              (is (some
                   #(and (= use-syntax-id
                            (:reference-syntax-id %))
                         (= :expression (:position %))
                         (= (:catch-binding-id record)
                            (:binding-id %)))
                   resolutions)))
            (doseq [candidate (:candidate-error-transfers record)]
              (let [transfer (get transfer-index (:ordinal candidate))]
                (is (= #{:ordinal :core-node-id}
                       (set (keys candidate))))
                (is (= (:core-node-id candidate)
                       (:core-node-id transfer)))
                (is (contains? protected-tree
                               (:core-node-id candidate)))))))))))

(deftest sh07-b8-catch-locals-candidates-and-nested-order-are-explicit
  (doseq [extension extensions]
    (let [top (file-artifact "accepted"
                             "top-level-typed-catch" extension)
          function (file-artifact "accepted"
                                  "function-typed-catch" extension)
          conditional (file-artifact "accepted"
                                     "conditional-protected" extension)
          nested (file-artifact "accepted"
                                "nested-typed-catches" extension)
          function-record (first (:error-handlers (core function)))
          function-nodes
          (exactly-once-index (:nodes (core function)) :node-id)
          function-protected
          (get function-nodes
               (:protected-core-node-id function-record))
          function-handler
          (get function-nodes
               (:handler-core-node-id function-record))
          conditional-record (first (:error-handlers (core conditional)))
          nested-records (:error-handlers (core nested))]
      (is (= [0]
             (mapv :ordinal
                   (:candidate-error-transfers
                    (first (:error-handlers (core top)))))))
      (is (some? (:evaluation-owner-function-syntax-id
                  function-record)))
      (is (= :function-body
             (:evaluation-region function-record)))
      (is (= :do (:core-form function-protected)
             (:core-form function-handler)))
      (is (= :left-to-right
             (get-in function-protected [:evaluation :kind])
             (get-in function-handler [:evaluation :kind])))
      (is (= (:children function-protected)
             (mapv :core-node-id
                   (get-in function-protected
                           [:evaluation :order]))))
      (is (= (:children function-handler)
             (mapv :core-node-id
                   (get-in function-handler
                           [:evaluation :order]))))
      (is (= [0 1]
             (mapv :ordinal
                   (:candidate-error-transfers conditional-record))))
      (is (= [0 1] (mapv :ordinal nested-records)))
      (is (= [[0] [0 1]]
             (mapv
              #(mapv :ordinal (:candidate-error-transfers %))
              nested-records)))
      (doseq [record
              (concat (:error-handlers (core top))
                      (:error-handlers (core function))
                      (:error-handlers (core conditional))
                      nested-records)]
        (is (= 1 (count (:catch-binding-use-syntax-ids record))))
        (is (not= (:catch-binding-syntax-id record)
                  (first (:catch-binding-use-syntax-ids record))))))))

(deftest sh07-b8-fresh-cross-root-identity-retains-actual-path
  (let [fixture
        (fixture-path "accepted" "nested-typed-catches" ".gravity")
        temp-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh07-b8-cross-root-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        left-path (.resolve temp-root "left/handlers.gravity")
        right-path (.resolve temp-root "right/handlers.qst")]
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
        (is (= (:error-handlers (core left))
               (:error-handlers (core right))))
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

(deftest sh07-b8-rejections-are-structured-and-oracle-bound
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
        (is (= (:remediation oracle) (:remediation diagnostic)))
        (is (= source-path (get-in diagnostic [:source-span :source])))
        (is (true? (get-in diagnostic [:facts :fail-closed])))))))

(deftest sh07-b8-authenticated-input-substitution-fails-before-lowering
  (let [artifact
        (file-artifact "accepted" "top-level-typed-catch" ".gravity")
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

(deftest sh07-b8-error-handler-alterations-fail-replay
  (let [artifact
        (file-artifact "accepted" "nested-typed-catches" ".gravity")
        first-handler
        (get-in artifact
                [:gravity-core-boundary :canonical-core-artifact
                 :error-handlers 0])
        first-node-id (:core-node-id first-handler)
        handler-transfer
        (select-keys
         (get-in artifact
                 [:gravity-core-boundary :canonical-core-artifact
                  :error-transfers 1])
         [:ordinal :core-node-id])
        node-index
        (first
         (keep-indexed
          (fn [index node]
            (when (= first-node-id (:node-id node)) index))
          (get-in artifact
                  [:gravity-core-boundary
                   :canonical-core-artifact :nodes])))
        alterations
        {"selection policy"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :error-handlers 0 :selection-policy]
          :always)
         "catch binding"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :error-handlers 0 :catch-binding-id]
          (:error-type-binding-id first-handler))
         "candidate removal"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
          :error-handlers 0 :candidate-error-transfers]
          [])
         "handler-body candidate injection"
         (update-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :error-handlers 0 :candidate-error-transfers]
          conj handler-transfer)
         "handler removal"
         (update-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :error-handlers]
          pop)
         "node result policy"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :nodes node-index :attributes :result-policy]
          :protected-only)}]
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
          (is (contains? failed :error-handlers-replay?))
          (is (contains? failed :authoritative-products-replay?))
          (is (= :failed
                 (:status
                  ((required-var
                    'sh07-core-artifact-verification)
                   altered)))))))))

(deftest sh07-b8-public-proof-is-bounded-and-does-not-claim-later-facts
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
        (is (= :gravity/sh07-to-c6-core-products-v13
               (:adapter-contract boundary)))
        (is (= :gravity/sh07-core-capability-proof
               (:artifact proof)))
        (is (= :complete (:status proof)))
        (is (= [] (:failed-checks proof)))
        (is (true? (:error-handlers-replay? proof)))
        (is (= :passed
               (get-in boundary [:template-verification :status])))
        (is (= :passed
               (get-in boundary [:resolved-verification :status])))
        (is (not-any? #{:try-handlers} pending))
        (is (some #{:patterns} pending))
        (is (= [:types :effects :ownership :safety]
               (:pending-fact-families canonical)))))))
