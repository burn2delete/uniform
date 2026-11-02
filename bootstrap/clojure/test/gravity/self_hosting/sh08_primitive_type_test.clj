(ns gravity.self-hosting.sh08-primitive-type-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh08_primitive_type_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-08 test source is not on the classpath"
                {:id "SH08-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH08-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private c7-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity")

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-08")

(defn- fixture-path
  [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- compile-plan
  []
  (let [source-path (path c7-source-relative-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c7-plan (delay (compile-plan)))

(defn- invoke-c7
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh08-primitive-type-leaf
    :compiler-artifact-plan? true}
   @c7-plan function arguments))

(defn- sh07-artifact
  [family basename extension]
  (bootstrap/sh07-core-file-artifact
   (fixture-path family basename extension)))

(defn- canonical-core
  [artifact]
  (get-in artifact
          [:gravity-core-boundary :canonical-core-artifact]))

(def ^:private accepted-gravity
  (delay (sh07-artifact
          "accepted" "primitive-if-join" ".gravity")))

(def ^:private accepted-qst
  (delay (sh07-artifact
          "accepted" "primitive-if-join" ".qst")))

(def ^:private rejected-gravity
  (delay (sh07-artifact
          "rejected" "if-branch-type-mismatch" ".gravity")))

(def ^:private rejected-qst
  (delay (sh07-artifact
          "rejected" "if-branch-type-mismatch" ".qst")))

(defn- type-result
  [artifact]
  (invoke-c7 'sh08-type-core-artifact
             [(canonical-core artifact)]))

(defn- fact-by-node-id
  [result]
  (into {} (map (juxt :core-node-id identity)) (:type-facts result)))

(defn- alternate-sha
  [digit]
  (str "sha256:" (apply str (repeat 64 digit))))

(def ^:private expected-literal-types
  {:nil :gravity.type/nil
   :boolean :gravity.type/bool
   :integer :gravity.type/integer
   :decimal :gravity.type/decimal
   :ratio :gravity.type/ratio
   :character :gravity.type/character
   :string :gravity.type/string
   :keyword :gravity.type/keyword})

(deftest sh08-fixtures-are-paired-and-c7-source-is-executable
  (is (= (slurp (fixture-path
                 "accepted" "primitive-if-join" ".gravity"))
         (slurp (fixture-path
                 "accepted" "primitive-if-join" ".qst"))))
  (is (= (slurp (fixture-path
                 "rejected" "if-branch-type-mismatch" ".gravity"))
         (slurp (fixture-path
                 "rejected" "if-branch-type-mismatch" ".qst"))))
  (is (= :gravity/stage2-compiler-artifact-plan (:kind @c7-plan)))
  (is (= :meta (get-in @c7-plan [:module :profile])))
  (is (= :jvm (get-in @c7-plan [:module :target])))
  (doseq [function
          '[sh08-primitive-type-from-literal-kind
            sh08-primitive-type-from-collection-kind
            sh08-type-core-artifact
            sh08-verify-type-result]]
    (is (map? (get-in @c7-plan [:functions function])) function))
  (is (= :gravity.type/integer
         (invoke-c7
          'sh08-primitive-type-from-literal-kind [:integer])))
  (is (= {:kind :gravity.type/vector
          :element :gravity.type/unknown
          :shape :literal}
         (invoke-c7
          'sh08-primitive-type-from-collection-kind [:vector]))))

(deftest structurally-validated-sh07-shaped-core-receives-primitive-type-facts
  (let [gravity-artifact @accepted-gravity
        qst-artifact @accepted-qst
        gravity-core (canonical-core gravity-artifact)
        qst-core (canonical-core qst-artifact)
        gravity (type-result gravity-artifact)
        qst (type-result qst-artifact)
        facts (fact-by-node-id gravity)
        nodes (:nodes gravity-core)
        type-table (:type-table gravity)]
    (is (= :complete
           (get-in gravity-artifact
                   [:capability-based-proof :status])))
    (is (= :complete
           (get-in qst-artifact
                   [:capability-based-proof :status])))
    (is (= :gravity/sh07-canonical-core-artifact
           (:artifact gravity-core)
           (:artifact qst-core)))
    (is (= :accepted (:status gravity)))
    (is (= :accepted (:status qst)))
    (is (= :gravity/sh08-primitive-typed-core-template
           (:artifact gravity)))
    (is (= 2 (:schema-version gravity)))
    (is (= :coordinator-adapter-required
           (:authentication-status gravity)))
    (is (= :coordinator-digest-required
           (:identity-resolution gravity)))
    (is (= (:identity-input gravity)
           (:artifact-id-request gravity)))
    (is (= :gravity/typed-core
           (get-in gravity [:typed-core :artifact])))
    (is (= (count nodes) (count type-table) (count facts)))
    (is (= (set (map :node-id nodes))
           (set (keys type-table))
           (set (keys facts))))
    (doseq [node nodes]
      (let [node-id (:node-id node)
            fact (get facts node-id)]
        (is (= node-id (:core-node-id fact)))
        (is (= (get-in node [:source :syntax-id]) (:syntax-id fact)))
        (is (= (get-in node [:source :semantic-span])
               (:source-span fact)))
        (is (= (get-in node [:source :origin-chain])
               (:origin-chain fact)))
        (is (= (get-in node [:source :generated-origin])
               (:generated-origin fact)))
        (is (= :meta (:profile fact)))
        (is (= :jvm (:target fact)))
        (is (= (get-in node [:preserved-declarations :effects])
               (:effects fact)
               (:effects (:module gravity))))
        (is (= (get-in node
                       [:preserved-declarations :capabilities])
               (:capabilities fact)
               (:capabilities (:module gravity))))
        (is (= :gravity/sh08-primitive-type-fact-v1
               (get-in fact [:fact-id-request :domain])))
        (is (= :pending-sh10 (:ownership fact)))
        (is (= :inferred (:status fact)))
        (is (= (:type fact) (get type-table node-id)))))
    (doseq [node (filter #(= :literal (:core-form %)) nodes)]
      (is (= (get expected-literal-types
                  (get-in node [:attributes :literal-kind]))
             (get type-table (:node-id node)))
          node))
    (doseq [node (filter #(= :collection-literal
                             (:core-form %))
                         nodes)]
      (let [kind (get-in node [:attributes :literal-kind])
            descriptor (get type-table (:node-id node))]
        (is (= ({:vector :gravity.type/vector
                 :map :gravity.type/map
                 :set :gravity.type/set}
                kind)
               (:kind descriptor)))
        (is (= (count (:children node))
               (count (:member-types descriptor))))
        (is (= (mapv type-table (:children node))
               (:member-types descriptor)))))
    (doseq [node (filter #(= :if (:core-form %)) nodes)]
      (let [[_ then-id else-id] (:children node)]
        (is (= (get type-table then-id)
               (get type-table else-id)
               (get type-table (:node-id node))))))
    (doseq [node (filter #(= :def (:core-form %)) nodes)]
      (is (= (get type-table (first (:children node)))
             (get type-table (:node-id node)))))
    (is (= (:identity-input gravity) (:identity-input qst)))
    (is (= gravity qst))
    (is (= :passed
           (:status
            (invoke-c7
             'sh08-verify-type-result
             [gravity-core gravity]))))
    (is (= (:artifact-id gravity-core)
           (:sh07-shaped-artifact-id gravity)
           (get-in gravity [:typed-core :core-input])))
    (is (= :primitive-literals-vector-map-set-definitions-equal-if-joins
           (:scope gravity)))
    (is (some #{:authenticated-coordinator-adapter}
              (:pending gravity)))
    (is (some #{:resolved-typed-artifact-digest}
              (:pending gravity)))
    (is (some #{:functions} (:pending gravity)))
    (is (some #{:calls} (:pending gravity)))))

(deftest structurally-validated-core-rejects-mismatched-if-branches
  (doseq [artifact [@rejected-gravity @rejected-qst]]
    (let [core (canonical-core artifact)
          result (type-result artifact)
          diagnostic (first (:diagnostics result))
          if-node
          (first (filter #(= :if (:core-form %)) (:nodes core)))]
      (is (= :complete
             (get-in artifact [:capability-based-proof :status])))
      (is (= :rejected (:status result)))
      (is (= 1 (count (:diagnostics result))))
      (is (= "C7-TYPE-MISMATCH" (:rule diagnostic)))
      (is (= :type-checking (:stage diagnostic)))
      (is (= (:node-id if-node) (:core-node-id diagnostic)))
      (is (= (get-in if-node [:source :syntax-id])
             (:syntax-id diagnostic)))
      (is (= (get-in if-node [:source :semantic-span])
             (:source-span diagnostic)))
      (is (= :gravity.type/integer (:expected-type diagnostic)))
      (is (= :gravity.type/keyword (:actual-type diagnostic)))
      (is (= :conditional-branch-type-mismatch
             (:reason diagnostic)))
      (is (= :meta (:profile diagnostic)))
      (is (= :jvm (:target diagnostic)))))
  (is (= (type-result @rejected-gravity)
         (type-result @rejected-qst))))

(deftest sh08-slice-fails-closed-on-malformed-local-carriers
  (let [core (canonical-core @accepted-gravity)
        nodes (:nodes core)
        first-node (first nodes)
        last-node (last nodes)
        wrong-kind
        (invoke-c7
         'sh08-type-core-artifact
         [(assoc core :artifact :gravity/not-sh07-core)])
        reversed
        (invoke-c7
         'sh08-type-core-artifact
         [(assoc core :nodes (vec (reverse (:nodes core))))])
        unknown-literal-index
        (first
         (keep-indexed
          #(when (= :literal (:core-form %2)) %1)
          (:nodes core)))
        unknown-literal-core
        (update-in
         core
         [:nodes unknown-literal-index :attributes :literal-kind]
         (constantly :future-literal))
        unknown-literal
        (invoke-c7
         'sh08-type-core-artifact [unknown-literal-core])
        duplicate-id
        (invoke-c7
         'sh08-type-core-artifact
         [(update core :nodes
                  #(assoc % (dec (count %))
                          (assoc last-node
                                 :node-id (:node-id first-node))))])
        missing-id
        (invoke-c7
         'sh08-type-core-artifact
         [(update-in core [:nodes 0] dissoc :node-id)])
        extra-node-field
        (invoke-c7
         'sh08-type-core-artifact
         [(assoc-in core [:nodes 0 :unexpected] true)])
        def-index
        (first
         (keep-indexed
          #(when (= :def (:core-form %2)) %1)
          nodes))
        dangling-child
        (invoke-c7
         'sh08-type-core-artifact
         [(assoc-in core [:nodes def-index :children 0]
                    (alternate-sha "f"))])
        child-bound
        (invoke-c7
         'sh08-type-core-artifact
         [(assoc-in core [:nodes def-index :children]
                    (vec (repeat 1025 (:node-id first-node))))])
        orphan
        (invoke-c7
         'sh08-type-core-artifact
         [(update core :root-core-node-ids
                  #(vec (rest %)))])
        missing-root
        (invoke-c7
         'sh08-type-core-artifact
         [(assoc core :root-core-node-ids
                 [(alternate-sha "e")])])
        malformed-module
        (invoke-c7
         'sh08-type-core-artifact
         [(assoc-in core [:module :effects] #{:not-a-vector})])
        forged-empty
        (invoke-c7
         'sh08-type-core-artifact
         [{:artifact :gravity/sh07-canonical-core-artifact
           :artifact-id (alternate-sha "d")
           :module (:module core)
           :nodes []
           :root-core-node-ids []}])]
    (is (= :rejected (:status wrong-kind)))
    (is (= "C7-VERIFY"
           (get-in wrong-kind [:diagnostics 0 :rule])))
    (is (= :sh07-shaped-core-artifact-required
           (get-in wrong-kind [:diagnostics 0 :reason])))
    (is (= :rejected (:status reversed)))
    (is (= "C7-VERIFY"
           (get-in reversed [:diagnostics 0 :rule])))
    (is (= :child-id-missing-or-not-before-parent
           (get-in reversed [:diagnostics 0 :reason])))
    (is (= :rejected (:status unknown-literal)))
    (is (= "C7-ANNOTATION"
           (get-in unknown-literal [:diagnostics 0 :rule])))
    (is (= :unsupported-literal-kind
           (get-in unknown-literal [:diagnostics 0 :reason])))
    (doseq [[label result reason]
            [["duplicate" duplicate-id :duplicate-core-node-id]
             ["missing id" missing-id :malformed-core-node]
             ["extra node field" extra-node-field :malformed-core-node]
             ["dangling child" dangling-child
              :child-id-missing-or-not-before-parent]
             ["child bound" child-bound :malformed-core-node]
             ["orphan" orphan :orphan-core-node]
             ["missing root" missing-root
              :malformed-or-unresolved-roots]
             ["module" malformed-module :malformed-core-module]
             ["empty forged carrier" forged-empty
              :empty-core-node-vector]]]
      (is (= :rejected (:status result)) label)
      (is (= "C7-VERIFY"
             (get-in result [:diagnostics 0 :rule]))
          label)
      (is (= reason
             (get-in result [:diagnostics 0 :reason]))
          label)
      (is (= :gravity/sh08-primitive-type-diagnostic-v2
             (get-in result
                     [:diagnostics 0
                      :diagnostic-id-request :domain]))
          label))
    (is (= :rejected
           (:status
            (invoke-c7
             'sh08-verify-type-result
             [core (assoc (type-result @accepted-gravity)
                          :status :substituted)]))))))

(deftest sh08-local-identity-does-not-claim-upstream-authentication
  (let [core (canonical-core @accepted-gravity)
        original (type-result @accepted-gravity)
        substituted-core
        (assoc core :artifact-id (alternate-sha "c"))
        substituted
        (invoke-c7
         'sh08-type-core-artifact [substituted-core])]
    (is (= :accepted (:status original)))
    (is (= :accepted (:status substituted)))
    (is (not= (:artifact-id-request original)
              (:artifact-id-request substituted)))
    (is (= :passed
           (:status
            (invoke-c7
             'sh08-verify-type-result
             [substituted-core substituted]))))
    (is (= :rejected
           (:status
            (invoke-c7
             'sh08-verify-type-result
             [core substituted]))))
    (is (= :coordinator-adapter-required
           (:authentication-status substituted)))
    (is (some #{:authenticated-coordinator-adapter}
              (:pending substituted)))))
