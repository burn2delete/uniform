(ns gravity.self-hosting.sh07-b47-function-call-recursion-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_b47_function_call_recursion_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07-B47 function/call test source is not on the classpath"
        {:id "SH07-B47-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-B47-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  (str "bootstrap/clojure/fixtures/self-hosting/sh-07/"
       "b47-function-call-recursion/accepted"))
(def ^:private basename "function-call-recursion")
(def ^:private extensions [".gravity" ".qst"])
(def ^:private expected-function-names
  '#{ordered-target acyclic-caller self-recursive-call
     self-recursive-recur mutual-left mutual-right
     namespace-target lexical-shadow-call})
(def ^:private function-record-keys
  #{:ordinal :function-core-node-id :function-form-id :function-syntax-id
    :body-core-node-id :parameter-binding-ids :fixed-arity
    :definition-kind :definition-binding-id :definition-core-node-id
    :definition-name})
(def ^:private call-edge-keys
  #{:ordinal :call-core-node-id
    :caller-function-syntax-id :caller-function-core-node-id
    :callee-binding-id :callee-definition-syntax-id
    :callee-function-syntax-id :callee-function-core-node-id
    :argument-core-node-ids :ordered-evaluation-node-ids
    :evaluation-order :classification})
(def ^:private recursion-component-keys
  #{:ordinal :kind :function-syntax-ids :function-core-node-ids
    :internal-call-edge-ordinals})
(def ^:private zero-id
  (str "sha256:" (apply str (repeat 64 "0"))))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- fixture-path
  [extension]
  (path (str fixture-root "/" basename extension)))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- required-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info
        "Required SH-07-B47 coordinator adapter is absent"
        {:id "SH07-B47-ADAPTER-ABSENT"
         :symbol symbol
         :required-signatures
         {'sh07-core-file-artifact '[source-path]
          'sh07-core-artifact-identity-input '[artifact]
          'sh07-core-verification-checks
          '[artifact expected upstream-verification]}}))))

(def ^:private artifacts (atom {}))

(defn- file-artifact
  [extension]
  (or (get @artifacts extension)
      (let [artifact
            ((required-var 'sh07-core-file-artifact)
             (fixture-path extension))]
        (swap! artifacts assoc extension artifact)
        artifact)))

(defn- core
  [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))

(defn- request
  [artifact]
  (get-in artifact [:gravity-core-boundary :authenticated-core-request]))

(defn- identity-input
  [artifact]
  ((required-var 'sh07-core-artifact-identity-input) artifact))

(defn- required-product
  [artifact product]
  (let [value (get (core artifact) product ::absent)]
    (when (= ::absent value)
      (throw
       (ex-info
        "Required SH-07-B47 canonical-core product is absent"
        {:id "SH07-B47-PRODUCT-ABSENT"
         :product product
         :required-products
         [:function-records :call-edges :recursion-components]})))
    (when-not (vector? value)
      (throw
       (ex-info
        "Required SH-07-B47 canonical-core product is not a vector"
        {:id "SH07-B47-PRODUCT-SHAPE"
         :product product
         :actual-type (some-> value class .getName)})))
    value))

(defn- exactly-once-index
  [records key-name]
  (let [index (into {} (map (juxt key-name identity)) records)]
    (when-not (= (count records) (count index))
      (throw
       (ex-info
        "SH-07-B47 product identifiers are ambiguous"
        {:id "SH07-B47-AMBIGUOUS-INDEX"
         :key key-name
         :record-count (count records)
         :unique-count (count index)})))
    index))

(defn- function-by-name
  [artifact]
  (let [records (required-product artifact :function-records)
        index (into {} (map (juxt :definition-name identity)) records)]
    (when-not (= (count records) (count index))
      (throw
       (ex-info
        "Named SH-07-B47 functions are not unique"
        {:id "SH07-B47-AMBIGUOUS-FUNCTION-NAME"})))
    index))

(defn- call-edge-name-view
  [artifact]
  (let [functions (required-product artifact :function-records)
        by-core (into {} (map (juxt :function-core-node-id
                                    :definition-name)
                              functions))
        by-syntax (into {} (map (juxt :function-syntax-id
                                      :definition-name)
                                functions))]
    (mapv
     (fn [edge]
       {:ordinal (:ordinal edge)
        :caller (get by-core (:caller-function-core-node-id edge))
        :callee (get by-syntax (:callee-function-syntax-id edge))
        :classification (:classification edge)})
     (required-product artifact :call-edges))))

(defn- component-name-view
  [artifact]
  (let [functions (required-product artifact :function-records)
        by-syntax (into {} (map (juxt :function-syntax-id
                                      :definition-name)
                                functions))]
    (mapv
     (fn [component]
       {:kind (:kind component)
        :functions (mapv by-syntax (:function-syntax-ids component))
        :internal-call-edge-ordinals
        (:internal-call-edge-ordinals component)})
     (required-product artifact :recursion-components))))

(defn- failed-verification-checks
  [altered expected]
  (set
   (for [[check passed?]
         ((required-var 'sh07-core-verification-checks)
          altered expected {:status :passed})
         :when (not (true? passed?))]
     check)))

(defn- update-product
  [artifact product update-function]
  (update-in artifact
             [:gravity-core-boundary :canonical-core-artifact product]
             update-function))

(defn- execute-core
  [function arguments]
  (bootstrap/sh07-core-execute!
   (fixture-path ".gravity") function arguments))

(defn- coordinated-call-and-edge-substitution
  [carrier]
  (let [replacement-call (get-in carrier [:calls 1])
        replacement-edge (get-in carrier [:call-edges 1])
        call-fields
        (select-keys
         replacement-call
         [:operator-node-id :operator-binding-id
          :argument-node-ids :ordered-evaluation-node-ids])
        edge-fields
        (select-keys
         replacement-edge
         [:callee-binding-id :callee-definition-syntax-id
          :callee-function-syntax-id :callee-function-core-node-id
          :argument-core-node-ids :ordered-evaluation-node-ids])]
    (-> carrier
        (update-in [:calls 0] merge call-fields)
        (update-in [:call-edges 0] merge edge-fields))))

(defn- duplicate-call-and-edge-substitution
  [carrier]
  (let [replacement-call (get-in carrier [:calls 1])
        replacement-edge
        (assoc (get-in carrier [:call-edges 1]) :ordinal 0)]
    (-> carrier
        (assoc-in [:calls 0] replacement-call)
        (assoc-in [:call-edges 0] replacement-edge))))

(deftest sh07-b47-fixtures-are-paired-path-neutral-and-deterministic
  (let [gravity-path (fixture-path ".gravity")
        qst-path (fixture-path ".qst")]
    (is (= (seq (source-bytes gravity-path))
           (seq (source-bytes qst-path))))
    (let [gravity (file-artifact ".gravity")
          qst (file-artifact ".qst")
          repeated
          ((required-var 'sh07-core-file-artifact) gravity-path)]
      (is (= :accepted (:status gravity) (:status qst)))
      (is (= "SH-07-B47" (:task gravity) (:task qst)))
      (is (= :c6-gravity-core-lowering-b47
             (get-in gravity [:pass :name])
             (get-in qst [:pass :name])))
      (is (= :sh07-b15-keyword-map-lookup
             (get-in gravity
                     [:gravity-core-boundary
                      :authenticated-core-request :scope])
             (get-in qst
                     [:gravity-core-boundary
                      :authenticated-core-request :scope])))
      (is (= gravity repeated))
      (is (= (:artifact-id gravity) (:artifact-id qst)))
      (is (= (identity-input gravity) (identity-input qst)))
      (doseq [product [:function-records :call-edges
                       :recursion-components]]
        (is (= (required-product gravity product)
               (required-product qst product)))
        (is (= (required-product gravity product)
               (get (identity-input gravity) product))))
      (is (= gravity-path (get-in gravity [:provenance :source-path])))
      (is (= qst-path (get-in qst [:provenance :source-path])))
      (is (not= (get-in gravity [:provenance :source-path])
                (get-in qst [:provenance :source-path]))))))

(deftest sh07-b47-bound-diagnostics-are-versioned-and-path-neutral
  (let [gravity (file-artifact ".gravity")
        qst (file-artifact ".qst")
        build
        (fn [artifact]
          (execute-core
           'sh07-diagnostic
           [(request artifact) "C6-VERIFY" nil
            {:reason :function-record-bound}]))
        gravity-diagnostic (build gravity)
        qst-diagnostic (build qst)]
    (is (= :gravity/sh07-b47-c6-diagnostic-v16
           (get-in gravity-diagnostic [:request :preimage :domain])
           (get-in qst-diagnostic [:request :preimage :domain])))
    (is (= :sh07-b47-function-call-recursion-products
           (get-in gravity-diagnostic [:diagnostic :lowering-rule])
           (get-in qst-diagnostic [:diagnostic :lowering-rule])))
    (is (= (get-in gravity-diagnostic [:request :preimage])
           (get-in qst-diagnostic [:request :preimage])))
    (is (= (bootstrap/reader-canonical-hash
            (get-in gravity-diagnostic [:request :preimage]))
           (bootstrap/reader-canonical-hash
            (get-in qst-diagnostic [:request :preimage]))))
    (is (= (fixture-path ".gravity")
           (get-in gravity-diagnostic
                   [:diagnostic :source-span :source])))
    (is (= (fixture-path ".qst")
           (get-in qst-diagnostic
                   [:diagnostic :source-span :source])))))

(deftest sh07-b47-function-and-call-products-are-exact-and-linked
  (let [artifact (file-artifact ".gravity")
        core-artifact (core artifact)
        functions (required-product artifact :function-records)
        call-edges (required-product artifact :call-edges)
        nodes (exactly-once-index (:nodes core-artifact) :node-id)
        bindings (exactly-once-index (:binding-table (request artifact))
                                     :binding-id)
        calls (exactly-once-index (:calls core-artifact) :core-node-id)
        by-name (function-by-name artifact)]
    (is (= expected-function-names (set (keys by-name))))
    (is (= (vec (range (count functions))) (mapv :ordinal functions)))
    (is (= (vec (range (count call-edges))) (mapv :ordinal call-edges)))
    (doseq [function functions]
      (is (= function-record-keys (set (keys function))))
      (is (= :named-top-level (:definition-kind function)))
      (is (contains? expected-function-names (:definition-name function)))
      (is (= (:fixed-arity function)
             (count (:parameter-binding-ids function))))
      (is (every? bindings (:parameter-binding-ids function)))
      (is (= :fn
             (get-in nodes [(:function-core-node-id function)
                            :core-form])))
      (is (contains? nodes (:body-core-node-id function)))
      (is (= (:function-form-id function)
             (get-in nodes [(:function-core-node-id function)
                            :source :form-id])))
      (is (= (:function-syntax-id function)
             (get-in nodes [(:function-core-node-id function)
                            :source :syntax-id])))
      (is (contains? bindings (:definition-binding-id function))))
    (doseq [edge call-edges]
      (let [call (get calls (:call-core-node-id edge))]
        (is (= call-edge-keys (set (keys edge))))
        (is (= :operator-then-arguments (:evaluation-order edge)))
        (is (= (:argument-node-ids call)
               (:argument-core-node-ids edge)))
        (is (= (:ordered-evaluation-node-ids call)
               (:ordered-evaluation-node-ids edge)))
        (is (= (:callee-binding-id edge)
               (:operator-binding-id call)))
        (is (contains? bindings (:callee-binding-id edge)))
        (is (contains? nodes (:call-core-node-id edge)))
        (is (contains? #{:local-function :nonlocal-or-nonfunction}
                       (:classification edge)))
        (when (= :local-function (:classification edge))
          (is (contains? nodes (:callee-function-core-node-id edge))))))))

(deftest sh07-b47-call-graph-binds-recursion-shadowing-and-order
  (let [artifact (file-artifact ".gravity")
        by-name (function-by-name artifact)
        edges (call-edge-name-view artifact)
        edge-by-caller (group-by :caller edges)
        components (component-name-view artifact)
        component-by-kind (group-by :kind components)
        mutual-left (get by-name 'mutual-left)
        mutual-right (get by-name 'mutual-right)
        shadow-edge (first (get edge-by-caller 'lexical-shadow-call))
        raw-shadow-edge
        (nth (required-product artifact :call-edges)
             (:ordinal shadow-edge))
        bindings (exactly-once-index (:binding-table (request artifact))
                                     :binding-id)
        ordered-edge (first (get edge-by-caller 'acyclic-caller))
        raw-ordered-edge
        (nth (required-product artifact :call-edges)
             (:ordinal ordered-edge))
        recur-function (get by-name 'self-recursive-recur)
        recur-target
        (first
         (filter #(= (:function-core-node-id recur-function)
                     (:owner-core-node-id %))
                 (:recur-targets (core artifact))))]
    (testing "acyclic and forward local calls preserve callee identity"
      (is (= ['ordered-target]
             (mapv :callee (get edge-by-caller 'acyclic-caller))))
      (is (= ['mutual-right]
             (mapv :callee (get edge-by-caller 'mutual-left))))
      (is (= ['mutual-left]
             (mapv :callee (get edge-by-caller 'mutual-right))))
      (is (< (:ordinal mutual-left) (:ordinal mutual-right))
          "mutual-left calls a function defined later in source order"))
    (testing "ordinary self-call and recur remain distinct products"
      (is (= ['self-recursive-call]
             (mapv :callee (get edge-by-caller 'self-recursive-call))))
      (is (nil? (get edge-by-caller 'self-recursive-recur)))
      (is (= :function (:target-kind recur-target)))
      (is (= 1 (:arity recur-target)))
      (is (= 1
             (count
              (filter #(= (:target-id recur-target) (:target-id %))
                      (:recur-transfers (core artifact)))))))
    (testing "recursion components contain only cyclic local call edges"
      (is (= 1 (count (get component-by-kind :self-recursive))))
      (is (= ['self-recursive-call]
             (:functions
              (first (get component-by-kind :self-recursive)))))
      (is (= 1 (count (get component-by-kind :mutually-recursive))))
      (is (= #{'mutual-left 'mutual-right}
             (set
              (:functions
               (first (get component-by-kind :mutually-recursive))))))
      (is (not-any? #{'self-recursive-recur 'acyclic-caller}
                    (mapcat :functions components))))
    (testing "lexical operator shadowing cannot become a local function edge"
      (is (= :nonlocal-or-nonfunction (:classification shadow-edge)))
      (is (nil? (:callee-function-syntax-id raw-shadow-edge)))
      (is (nil? (:callee-function-core-node-id raw-shadow-edge)))
      (is (= :lexical
             (get-in bindings
                     [(:callee-binding-id raw-shadow-edge)
                      :binding-class]))))
    (testing "argument and evaluation order are exact"
      (is (= 3 (count (:argument-core-node-ids raw-ordered-edge))))
      (is (= 4 (count (:ordered-evaluation-node-ids raw-ordered-edge))))
      (is (= (:argument-core-node-ids raw-ordered-edge)
             (subvec (:ordered-evaluation-node-ids raw-ordered-edge) 1))))))

(deftest sh07-b47-products-replay-and-alterations-fail-closed
  (let [artifact (file-artifact ".gravity")
        functions (required-product artifact :function-records)
        call-edges (required-product artifact :call-edges)
        components (required-product artifact :recursion-components)
        mutual-index
        (first
         (keep-indexed
          (fn [index component]
            (when (= :mutually-recursive (:kind component)) index))
          components))
        alterations
        {"function arity"
         (update-product
          artifact :function-records
          #(update-in % [0 :fixed-arity] inc))

         "call callee"
         (update-product
          artifact :call-edges
          #(assoc-in % [0 :callee-binding-id] zero-id))

         "recursion member order"
         (update-product
          artifact :recursion-components
          #(update-in % [mutual-index :function-syntax-ids]
                      (comp vec reverse)))}]
    (is (seq functions))
    (is (seq call-edges))
    (is (some? mutual-index))
    (doseq [[label altered] alterations]
      (testing label
        (is (not= artifact altered))
        (let [failed (failed-verification-checks altered artifact)]
          (is (seq failed)
              "Every B47 product alteration must fail replay.")
          (is (contains? failed :canonical-core-replays?)))))))

(deftest sh07-b47-owning-verifiers-reject-internally-valid-substitutions
  (let [artifact (file-artifact ".gravity")
        boundary (:gravity-core-boundary artifact)
        request (:authenticated-core-request boundary)
        raw (:raw-template-result boundary)
        template (:core-template raw)
        resolved (:canonical-core-artifact boundary)
        digests (:resolved-digests boundary)
        raw-functions (:function-records template)
        raw-edges (:call-edges template)
        raw-components (:recursion-components template)
        resolved-functions (:function-records resolved)
        resolved-edges (:call-edges resolved)
        resolved-components (:recursion-components resolved)
        replacement-raw-node
        (first
         (remove
          #{(get-in raw-functions [0 :body-core-node-id])}
          (mapv :node-id (:nodes template))))
        replacement-resolved-node
        (first
         (remove
          #{(get-in resolved-functions [0 :body-core-node-id])}
          (mapv :node-id (:nodes resolved))))
        mutual-index
        (first
         (keep-indexed
          (fn [index component]
            (when (= :mutually-recursive (:kind component)) index))
          raw-components))
        outside-edge-ordinal
        (first
         (remove
          (set
           (get-in raw-components
                   [mutual-index :internal-call-edge-ordinals]))
          (mapv :ordinal raw-edges)))
        raw-alterations
        [(assoc-in template
                   [:function-records 0 :body-core-node-id]
                   replacement-raw-node)
         (assoc-in template
                   [:call-edges 0 :call-core-node-id]
                   (get-in raw-edges [1 :call-core-node-id]))
         (assoc-in template
                   [:recursion-components mutual-index
                    :internal-call-edge-ordinals 0]
                   outside-edge-ordinal)
         (assoc-in template
                   [:identity-preimage :function-records 0
                    :body-core-node-id]
                   replacement-raw-node)
         (coordinated-call-and-edge-substitution template)
         (duplicate-call-and-edge-substitution template)]
        altered-resolved
        [(assoc-in resolved
                   [:function-records 0 :body-core-node-id]
                   replacement-resolved-node)
         (assoc-in resolved
                   [:call-edges 0 :call-core-node-id]
                   (get-in resolved-edges [1 :call-core-node-id]))
         (assoc-in resolved
                   [:recursion-components mutual-index
                   :internal-call-edge-ordinals 0]
                   outside-edge-ordinal)
         (coordinated-call-and-edge-substitution resolved)
         (duplicate-call-and-edge-substitution resolved)]]
    (is (some? replacement-raw-node))
    (is (some? replacement-resolved-node))
    (is (integer? mutual-index))
    (is (integer? outside-edge-ordinal))
    (is (true?
         (execute-core
          'sh07-function-products-coherent?
          [(:nodes resolved) (:definitions resolved)
           (:binding-table request) (:calls resolved)
           resolved-functions resolved-edges resolved-components])))
    (doseq [candidate altered-resolved]
      (is (false?
           (execute-core
            'sh07-function-products-coherent?
            [(:nodes candidate) (:definitions candidate)
             (:binding-table request)
             (:calls candidate) (:function-records candidate)
             (:call-edges candidate) (:recursion-components candidate)]))))
    (doseq [candidate raw-alterations]
      (is (= {:status :rejected
              :reason :function-product-coherence}
             (execute-core
              'sh07-resolve-core-template [candidate digests]))))
    (doseq [candidate raw-alterations]
      (is (= :rejected
             (:status
              (execute-core
               'sh07-verify-core-template
               [request candidate (:digest-requests raw)])))))
    (doseq [candidate altered-resolved]
      (is (= :rejected
             (:status
              (execute-core
               'sh07-verify-core-resolved
               [request candidate (:digest-requests raw) digests])))))))
