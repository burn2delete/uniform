(ns gravity.tools.sh07-b51-attempt30-producer-census
  "Offline source-first exhaustive census checker for SH-07 B51 Attempt 30.

  This namespace is intentionally self-contained.  It shares only the
  repository's canonical reader encoding and SHA-256 scalar primitive.  In
  particular it does not call the Attempt-28 census, carrier traversal,
  reachability, row, normalization, or digest-schedule implementation."
  (:require [clojure.set :as set]
            [gravity.c2-artifact-identity.canonical :as canonical]
            [gravity.digest :as digest]))

(def input-keys
  #{:architecture-report-sha256 :snapshot-commit :snapshot-tree
    :predecessor-executable-descriptor-id :module-source-identities
    :module-source-byte-counts :emitter-source-id :emitter-semantic-id
    :m0s :m1 :m2s :bundle-core :source-forms :checker :provenance})

(def checker-keys
  #{:artifact-id :source-sha256 :semantic-id :executable-id
    :dependency-closure-id :algorithm-role})

(def input-provenance-keys
  #{:invocation-id :tool-closure-id :reproduction-command-id
    :evidence-relative-path :completed-at})

(def certificate-provenance-keys
  (conj input-provenance-keys
        :raw-transcript-artifact-id :m2-transcript-artifact-id))

(def certificate-input-keys
  #{:architecture-report-sha256 :snapshot-commit :snapshot-tree
    :predecessor-executable-descriptor-id :module-source-identities
    :module-source-byte-counts :emitter-source-id :emitter-semantic-id
    :m0-ids :m1-id :m2-ids :bundle-core-id})

(def certificate-keys
  #{:artifact :domain :schema-version :status :role
    :architecture-report-sha256 :claim-id :claim :assumptions :checker
    :inputs :result :provenance :raw-source-transcript :m2-transcript
    :profile-bounds :evidence-root :certificate-id})

(def claim-keys
  #{:domain :normalized-census-id :external-bijection-commitment
    :builtin-parity-commitments :provenance-completeness-commitment
    :physical-census :category-schedule :category-commitments})

(def assumptions-keys
  #{:sha256-collision-resistance :canonical-reader-hash-id
    :source-emitter-bytes-immutable :checker-closures-independent
    :host-pin-boundary-id :runtime-token-boundary-id})

(def result-keys
  #{:status :normalized-census-id :definition-count :local-count
    :external-count :builtin-count :semantic-row-count
    :external-bijection-complete :builtin-parity-complete
    :provenance-complete :ordering-complete :reachability-complete})

(def transcript-descriptor-keys
  #{:artifact :schema-version :node-count :row-count :artifact-id
    :record-root})

(def profile-bounds
  {:certificate-maximum-nodes 2048
   :certificate-maximum-depth 12
   :certificate-maximum-width 35
   :certificate-maximum-collection-count 64
   :certificate-maximum-scalar-utf8 4096
   :certificate-maximum-canonical-reader-bytes 262144
   :outer-request-maximum-collection-count 65536
   :carrier-maximum-nodes 8388608
   :carrier-maximum-depth 256
   :carrier-maximum-width 65536
   :carrier-maximum-collection-count 524288
   :carrier-partition-maximum-collection-count 65536
   :carrier-maximum-scalar-utf8 65536
   :offline-transcript-maximum-nodes 262144
   :offline-transcript-maximum-records 262144
   :offline-transcript-maximum-call-rows 65536
   :offline-transcript-maximum-bytes 268435456})

(def category-schedule
  (array-map
   :rows-per-leaf 16
   :fold-fanout 16
   :maximum-fold-levels 3
   :category-count 7
   :maximum-total-rows 65536
   :maximum-row-nodes 48
   :maximum-row-depth 6
   :maximum-row-width 8
   :maximum-row-scalar-utf8 4096
   :maximum-row-c11-bytes 8192
   :maximum-row-canonical-reader-bytes 9216
   :maximum-leaf-nodes 1024
   :maximum-leaf-depth 12
   :maximum-leaf-width 16
   :maximum-leaf-scalar-utf8 4096
   :maximum-leaf-c11-bytes 196608
   :maximum-leaf-canonical-reader-bytes 212992
   :maximum-fold-nodes 512
   :maximum-fold-depth 8
   :maximum-fold-width 16
   :maximum-fold-scalar-utf8 4096
   :maximum-fold-c11-bytes 65536
   :maximum-fold-canonical-reader-bytes 73728
   :maximum-commitment-nodes 64
   :maximum-commitment-depth 6
   :maximum-commitment-width 16
   :maximum-commitment-scalar-utf8 4096
   :maximum-commitment-c11-bytes 16384
   :maximum-commitment-canonical-reader-bytes 18432
   :maximum-root-nodes 512
   :maximum-root-depth 8
   :maximum-root-width 64
   :maximum-root-scalar-utf8 4096
   :maximum-root-c11-bytes 65536
   :maximum-root-canonical-reader-bytes 73728))

(def categories
  [:roots :members :functions :local-runtime-call-edges
   :external-runtime-call-edges :referenced-builtins :module-contracts])

(def internal-builtins
  '[sh07-canonical-text-compare
    sh07-declared-digest-hash
    sh07-observe-utf8-byte-count
    sh07-b51-admitted-executable-carrier-validation?])

(def internal-builtin-arities
  {'sh07-canonical-text-compare 2
   'sh07-declared-digest-hash 1
   'sh07-observe-utf8-byte-count 3
   'sh07-b51-admitted-executable-carrier-validation? 1})

(def special-forms '#{defn ns if let loop recur do quote})

(def m0-keys
  #{:artifact :schema-version :module-source-id :module-ordinal :module
    :logical-source-path :owner :source-byte-count :source-sha256 :profile
    :target :effects :capabilities :safety :definitions :exports
    :core-binding-count :lexical-binding-count :external-bindings
    :aggregate-binding-count :builtin-calls})

(def definition-keys #{:name :arity :visibility :source-hash})
(def builtin-call-keys
  #{:caller :caller-function-source-hash :builtin :arity
    :compiler-internal?})
(def external-binding-keys
  #{:symbol :callee-module-ordinal :callee-module :callee-source-sha256
    :callee-definition-source-hash :arity :owner-edge})
(def m1-keys #{:artifact :schema-version :manifest-id :module-source-ids :edges})
(def external-row-keys
  #{:ordinal :caller-module-source-id :caller-function
    :caller-function-source-hash :callsite-path :callee-module-source-id
    :callee-function :callee-function-source-hash :arity :owner-edge
    :authorization})
(def bundle-core-keys
  #{:domain :schema-version :manifest-id :module-plans :routes
    :public-roots})
(def module-plan-coordinate-keys
  #{:module-ordinal :module-source-id :plan-id})
(def route-keys #{:edge-ordinal :caller-plan-id :callee-plan-id})
(def public-root-keys #{:root-ordinal :function :arity :plan-id})

(def physical-census-keys
  #{:module-count :module-censuses :physical-definition-count
    :reachable-definition-count :physical-local-runtime-callsite-count
    :reachable-local-runtime-callsite-count
    :physical-external-runtime-callsite-count
    :reachable-external-runtime-callsite-count
    :physical-referenced-builtin-occurrence-count
    :reachable-referenced-builtin-occurrence-count :semantic-row-count
    :module-binding-occupancies :maximum-binding-occupancy :binding-ceiling})

(def module-census-keys
  #{:module-ordinal :module :logical-source-path :owner :source-byte-count
    :source-sha256 :module-source-id :plan-id :physical-definition-count
    :reachable-definition-count :physical-local-runtime-callsite-count
    :reachable-local-runtime-callsite-count
    :physical-external-runtime-callsite-count
    :reachable-external-runtime-callsite-count
    :physical-referenced-builtin-occurrence-count
    :reachable-referenced-builtin-occurrence-count :core-binding-count
    :lexical-binding-count :external-binding-count :aggregate-binding-count})

(def raw-record-keys
  #{:node-ordinal :module-ordinal :caller :caller-function-source-hash
    :source-path :callsite-path :node-kind :quoted-subtree-stop
    :possible-call-head :classification :callee :arity
    :physical-ordinal :semantic-row})

(def m2-record-keys
  #{:node-ordinal :module-ordinal :instruction-ordinal :caller
    :caller-function-source-hash :path :op :call-classification :callee
    :arity :external-link :provenance-present :provenance
    :occurrence-ordinal :semantic-row})

(defn canonical-value [value]
  (canonical/reader-canonical-value canonical-value value))

(defn canonical-hash [value]
  (canonical/reader-canonical-hash digest/sha256-hex canonical-value value))

(defn- exact-keys? [value expected-keys]
  (and (map? value) (= expected-keys (set (keys value)))))

(defn- sha256-id? [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- git-id? [value]
  (and (string? value) (boolean (re-matches #"[0-9a-f]{40}" value))))

(defn- nonnegative-integer? [value]
  (and (integer? value) (<= 0 value)))

(defn- fail! [reason & [context]]
  (throw (ex-info "Attempt30 producer census rejected"
                  {:reason reason :context (or context {})})))

(defn- require! [condition reason & [context]]
  (when-not condition (fail! reason context)))

(defn value-profile [root]
  (loop [pending [[root 0]]
         nodes 0
         depth 0
         width 0
         collections 0
         scalar-utf8 0]
    (if (empty? pending)
      {:nodes nodes
       :depth depth
       :width width
       :collection-count collections
       :scalar-utf8 scalar-utf8
       :canonical-reader-bytes
       (alength (.getBytes
                 (binding [*print-length* nil *print-level* nil *print-meta* true]
                   (pr-str (canonical-value root)))
                 java.nio.charset.StandardCharsets/UTF_8))}
      (let [[[value level] & remaining] pending
            collection? (coll? value)
            children (cond
                       (map? value) (mapcat identity value)
                       collection? value
                       :else [])
            item-width (if collection? (count value) 0)
            scalar-bytes
            (if collection?
              0
              (alength (.getBytes (pr-str value)
                                  java.nio.charset.StandardCharsets/UTF_8)))]
        (recur (into (vec remaining)
                     (map #(vector % (inc level)) children))
               (inc nodes)
               (max depth level)
               (max width item-width)
               (if collection? (inc collections) collections)
               (max scalar-utf8 scalar-bytes))))))

(defn certificate-profile-within-bounds? [profile]
  (and (exact-keys? profile
                    #{:nodes :depth :width :collection-count :scalar-utf8
                      :canonical-reader-bytes})
       (every? nonnegative-integer? (vals profile))
       (<= (:nodes profile) (:certificate-maximum-nodes profile-bounds))
       (<= (:depth profile) (:certificate-maximum-depth profile-bounds))
       (<= (:width profile) (:certificate-maximum-width profile-bounds))
       (<= (:collection-count profile)
           (:certificate-maximum-collection-count profile-bounds))
       (<= (:scalar-utf8 profile)
           (:certificate-maximum-scalar-utf8 profile-bounds))
       (<= (:canonical-reader-bytes profile)
           (:certificate-maximum-canonical-reader-bytes profile-bounds))))

(defn offline-transcript-within-bounds? [metrics]
  (and (exact-keys? metrics
                    #{:node-count :record-count :call-row-count :byte-count})
       (every? nonnegative-integer? (vals metrics))
       (<= (:node-count metrics)
           (:offline-transcript-maximum-nodes profile-bounds))
       (<= (:record-count metrics)
           (:offline-transcript-maximum-records profile-bounds))
       (<= (:call-row-count metrics)
           (:offline-transcript-maximum-call-rows profile-bounds))
       (<= (:byte-count metrics)
           (:offline-transcript-maximum-bytes profile-bounds))))

(defn- canonical-sort-key [value]
  (binding [*print-length* nil *print-level* nil *print-meta* true]
    (pr-str (canonical-value value))))

(defn- path-compare [left right]
  (let [count-order (compare (count left) (count right))]
    (if (zero? count-order)
      (loop [l left r right]
        (if (empty? l)
          0
          (let [order (compare (first l) (first r))]
            (if (zero? order)
              (recur (rest l) (rest r))
              order))))
      count-order)))

(defn- external-row-compare [module-by-source left right]
  (let [coordinates
        [[(:module-ordinal (get module-by-source (:caller-module-source-id left)))
          (:module-ordinal (get module-by-source (:caller-module-source-id right)))]
         [(str (:caller-function left)) (str (:caller-function right))]]]
    (or (some (fn [[a b]] (let [order (compare a b)]
                            (when-not (zero? order) order)))
              coordinates)
        (path-compare (:callsite-path left) (:callsite-path right))
        0)))

(defn- definition-from-form [form]
  (when (and (seq? form) (= 'defn (first form)) (<= 4 (count form)))
    (let [[_ name params & body] form]
      (when (and (symbol? name) (vector? params) (every? symbol? params)
                 (seq body))
        {:name name :params params :body body :arity (count params)}))))

(defn- namespace-declaration [form]
  (when (and (seq? form) (= 'ns (first form)) (symbol? (second form)))
    (let [clauses (drop 2 form)]
      (when (and (every? #(and (seq? %) (= 2 (count %))
                               (keyword? (first %))) clauses)
                 (= (count clauses) (count (set (map first clauses)))))
        (assoc (into {} (map (juxt first second)) clauses)
               :module (second form))))))

(defn- source-definition-hash [definition]
  (canonical-hash (select-keys definition [:name :arity :params :body])))

(defn- canonical-map-entries [value]
  (sort-by canonical-sort-key value))

(defn- canonical-set-items [value]
  (sort-by canonical-sort-key value))

(defn- node-kind [value]
  (cond
    (seq? value) :list
    (vector? value) :vector
    (map? value) :map
    (set? value) :set
    :else :scalar))

(defn- raw-classification [value caller module-ordinal owner-by-function
                           local-functions builtins]
  (if-not (and caller (seq? value) (symbol? (first value)))
    :not-a-call
    (let [head (first value)
          callee-module (get owner-by-function head)]
      (cond
        (= 'quote head) :quoted-subtree-stop
        (contains? special-forms head) :special-form
        (contains? local-functions head) :local-runtime-call
        (and callee-module (not= module-ordinal callee-module))
        :external-runtime-call
        (contains? builtins head) :builtin-call
        :else :unknown-call))))

(defn- scan-raw-node!
  [records physical source-node-path callsite-path value context indexes]
  (let [{:keys [module-ordinal caller caller-function-source-hash
                owner-by-function local-functions builtins]} context
        classification
        (raw-classification value caller module-ordinal owner-by-function
                            local-functions builtins)
        call? (contains? #{:local-runtime-call :external-runtime-call
                           :builtin-call} classification)
        semantic? (or call? (= :definition classification))
        physical-ordinal (when semantic? (swap! physical inc))
        possible? (and (seq? value) (symbol? (first value)))
        quoted? (= :quoted-subtree-stop classification)
        row {:node-ordinal (count @records)
             :module-ordinal module-ordinal
             :caller caller
             :caller-function-source-hash caller-function-source-hash
             :source-path source-node-path
             :callsite-path callsite-path
             :node-kind (node-kind value)
             :quoted-subtree-stop quoted?
             :possible-call-head possible?
             :classification classification
             :callee (when possible? (first value))
             :arity (when possible? (count (rest value)))
             :physical-ordinal physical-ordinal
             :semantic-row semantic?}]
    (swap! records conj row)
    (when (= :unknown-call classification)
      (fail! :unknown-source-call-head
             {:module-ordinal module-ordinal :caller caller
              :callsite-path callsite-path :callee (first value)}))
    (when-not quoted?
      (cond
        (seq? value)
        (doseq [[ordinal child] (map-indexed vector value)]
          (scan-raw-node! records physical
                          (conj source-node-path ordinal)
                          (when callsite-path (conj callsite-path ordinal))
                          child context indexes))

        (vector? value)
        (doseq [[ordinal child] (map-indexed vector value)]
          (scan-raw-node! records physical
                          (conj source-node-path ordinal)
                          (when callsite-path (conj callsite-path ordinal))
                          child context indexes))

        (map? value)
        (doseq [[entry-ordinal [key item]]
                (map-indexed vector (canonical-map-entries value))]
          (scan-raw-node! records physical
                          (conj source-node-path entry-ordinal 0)
                          (when callsite-path
                            (conj callsite-path entry-ordinal 0))
                          key context indexes)
          (scan-raw-node! records physical
                          (conj source-node-path entry-ordinal 1)
                          (when callsite-path
                            (conj callsite-path entry-ordinal 1))
                          item context indexes))

        (set? value)
        (doseq [[ordinal child]
                (map-indexed vector (canonical-set-items value))]
          (scan-raw-node! records physical
                          (conj source-node-path ordinal)
                          (when callsite-path (conj callsite-path ordinal))
                          child context indexes))))))

(defn- source-indexes [source-forms m0s]
  (let [definitions-by-module
        (mapv (fn [ordinal forms]
                (let [definitions (keep definition-from-form forms)]
                  (require! (= (count forms) (inc (count definitions)))
                            :unexpected-top-level-source-form
                            {:module-ordinal ordinal})
                  (require! (= (count definitions)
                               (count (filter #(and (seq? %)
                                                   (= 'defn (first %)))
                                              forms)))
                            :invalid-source-definition
                            {:module-ordinal ordinal})
                  (require! (= (count definitions)
                               (count (set (map :name definitions))))
                            :duplicate-source-definition
                            {:module-ordinal ordinal})
                  (into {} (map (juxt :name identity)) definitions)))
              (range) source-forms)
        all-names (mapcat keys definitions-by-module)]
    (require! (= (count all-names) (count (set all-names)))
              :duplicate-cross-module-definition)
    {:definitions-by-module definitions-by-module
     :owner-by-function
     (into {}
           (for [[ordinal definitions] (map-indexed vector definitions-by-module)
                 function (keys definitions)]
             [function ordinal]))
     :definition-hash
     (into {}
           (for [[ordinal definitions] (map-indexed vector definitions-by-module)
                 [function definition] definitions]
             [[ordinal function] (source-definition-hash definition)]))
     :builtin-symbols
     (set (map :builtin (mapcat :builtin-calls m0s)))}))

(defn scan-raw-source [source-forms m0s]
  (require! (and (vector? source-forms) (= 7 (count source-forms))
                 (every? vector? source-forms))
            :source-forms-shape)
  (let [indexes (source-indexes source-forms m0s)
        records (atom [])
        physical (atom -1)]
    (doseq [[module-ordinal forms] (map-indexed vector source-forms)]
      (let [descriptor (nth m0s module-ordinal)
            definitions (get-in indexes [:definitions-by-module module-ordinal])
            ns-forms (filter #(and (seq? %) (= 'ns (first %))) forms)]
        (require! (= 1 (count ns-forms)) :source-namespace-count
                  {:module-ordinal module-ordinal})
        (require! (= {:module (:module descriptor)
                      :profile (:profile descriptor)
                      :target (:target descriptor)
                      :exports (:exports descriptor)
                      :effects (:effects descriptor)
                      :capabilities (:capabilities descriptor)
                      :safety (:safety descriptor)}
                     (namespace-declaration (first ns-forms)))
                  :source-namespace-join {:module-ordinal module-ordinal})
        (doseq [[top-ordinal form] (map-indexed vector forms)]
          (if-let [definition (definition-from-form form)]
            (let [caller (:name definition)
                  hash (get-in indexes [:definition-hash
                                        [module-ordinal caller]])
                  context {:module-ordinal module-ordinal
                           :caller caller
                           :caller-function-source-hash hash
                           :owner-by-function (:owner-by-function indexes)
                           :local-functions (set (keys definitions))
                           :builtins (:builtin-symbols indexes)}
                  row {:node-ordinal (count @records)
                       :module-ordinal module-ordinal
                       :caller caller
                       :caller-function-source-hash hash
                       :source-path [top-ordinal]
                       :callsite-path nil
                       :node-kind :list
                       :quoted-subtree-stop false
                       :possible-call-head true
                       :classification :definition
                       :callee 'defn
                       :arity (count (rest form))
                       :physical-ordinal (swap! physical inc)
                       :semantic-row true}]
              (swap! records conj row)
              (doseq [[header-index header]
                      [[0 'defn] [1 caller] [2 (:params definition)]]]
                (scan-raw-node! records physical [top-ordinal header-index]
                                nil header
                                (assoc context :caller nil
                                               :caller-function-source-hash nil)
                                indexes))
              (doseq [[body-index body]
                      (map-indexed vector (:body definition))]
                (scan-raw-node! records physical
                                [top-ordinal (+ 3 body-index)] [body-index]
                                body context indexes)))
            (scan-raw-node!
             records physical [top-ordinal] nil form
             {:module-ordinal module-ordinal :caller nil
              :caller-function-source-hash nil
              :owner-by-function (:owner-by-function indexes)
              :local-functions #{} :builtins (:builtin-symbols indexes)}
             indexes)))))
    {:records @records
     :row-count (count (filter :semantic-row @records))
     :indexes indexes}))

(defn- instruction-children [instruction]
  (let [op (:op instruction)]
    (case op
      :println (:args instruction)
      :do (:body instruction)
      :if [(:test instruction) (:then instruction) (:else instruction)]
      :let (concat (map :expr (:bindings instruction)) (:body instruction))
      :loop (concat (map :expr (:bindings instruction)) (:body instruction))
      :recur (:args instruction)
      :builtin-call (:args instruction)
      :function-call (:args instruction)
      :vector-literal (:items instruction)
      :set-literal (:items instruction)
      :map-literal (mapcat (fn [entry] [(:key entry) (:value entry)])
                           (:entries instruction))
      (:literal :local :quote) []
      (fail! :unknown-m2-operation {:op op}))))

(defn- valid-instruction-shape? [instruction]
  (and (map? instruction)
       (keyword? (:op instruction))
       (case (:op instruction)
         (:literal :quote) (exact-keys? instruction #{:op :value})
         :local (and (exact-keys? instruction #{:op :name})
                     (symbol? (:name instruction)))
         (:println :recur :builtin-call :function-call)
         (vector? (:args instruction))
         :do (and (exact-keys? instruction #{:op :body})
                  (vector? (:body instruction)))
         :if (and (exact-keys? instruction #{:op :test :then :else})
                  (every? map? [(:test instruction) (:then instruction)
                                (:else instruction)]))
         (:let :loop)
         (and (vector? (:bindings instruction))
              (every? #(and (exact-keys? % #{:name :expr})
                            (symbol? (:name %)) (map? (:expr %)))
                      (:bindings instruction))
              (vector? (:body instruction))
              (exact-keys? instruction
                           (if (= :loop (:op instruction))
                             #{:op :binding-count :bindings :body}
                             #{:op :bindings :body})))
         (:vector-literal :set-literal)
         (and (exact-keys? instruction #{:op :items})
              (vector? (:items instruction)))
         :map-literal
         (and (exact-keys? instruction #{:op :entries})
              (vector? (:entries instruction))
              (every? #(and (exact-keys? % #{:key :value})
                            (map? (:key %)) (map? (:value %)))
                      (:entries instruction)))
         false)))

(defn- scan-m2-instruction!
  [records module-instruction occurrence instruction context path]
  (require! (valid-instruction-shape? instruction) :m2-instruction-shape
            {:module-ordinal (:module-ordinal context)
             :caller (:caller context) :path path})
  (let [{:keys [module-ordinal caller caller-function-source-hash
                owner-by-function builtin-symbols]} context
        op (:op instruction)
        callee (when (contains? #{:function-call :builtin-call} op)
                 (:function instruction))
        classification
        (case op
          :function-call
          (let [callee-module (get owner-by-function callee)]
            (cond
              (nil? callee-module) :unknown-function-call
              (= module-ordinal callee-module) :local-runtime-call
              :else :external-runtime-call))
          :builtin-call
          (if (contains? builtin-symbols callee)
            :builtin-call :unknown-builtin-call)
          :not-a-call)
        semantic? (contains? #{:local-runtime-call :external-runtime-call
                               :builtin-call} classification)
        occurrence-ordinal (when semantic? (swap! occurrence inc))
        instruction-ordinal (swap! module-instruction inc)
        provenance (:compiler-internal-provenance instruction)
        row {:node-ordinal (count @records)
             :module-ordinal module-ordinal
             :instruction-ordinal instruction-ordinal
             :caller caller
             :caller-function-source-hash caller-function-source-hash
             :path path
             :op op
             :call-classification classification
             :callee callee
             :arity (when semantic? (count (:args instruction)))
             :external-link (:external-link instruction)
             :provenance-present (contains? instruction
                                            :compiler-internal-provenance)
             :provenance provenance
             :occurrence-ordinal occurrence-ordinal
             :semantic-row semantic?}]
    (require! (not (contains? #{:unknown-function-call :unknown-builtin-call}
                              classification))
              classification {:module-ordinal module-ordinal
                              :caller caller :callee callee})
    (case classification
      :local-runtime-call
      (require! (exact-keys? instruction #{:op :function :args})
                :m2-local-call-schema)
      :external-runtime-call
      (require! (exact-keys? instruction
                             #{:op :function :args :external-link})
                :m2-external-call-schema)
      :builtin-call
      (if (contains? (set internal-builtins) callee)
        (require! (and (exact-keys?
                        instruction
                        #{:op :function :args :compiler-internal-caller
                          :compiler-internal-provenance})
                       (= caller (:compiler-internal-caller instruction)))
                  :m2-internal-call-schema)
        (require! (exact-keys? instruction #{:op :function :args})
                  :m2-builtin-call-schema))
      :not-a-call
      (case op
        (:println :recur) (require! (exact-keys? instruction #{:op :args})
                                    :m2-noncall-schema)
        nil))
    (swap! records conj row)
    (doseq [[ordinal child] (map-indexed vector
                                         (instruction-children instruction))]
      (require! (map? child) :m2-child-instruction-shape
                {:module-ordinal module-ordinal :caller caller
                 :path (conj path ordinal)})
      (scan-m2-instruction! records module-instruction occurrence child context
                            (conj path ordinal)))))

(defn scan-m2 [m2s indexes]
  (require! (and (vector? m2s) (= 7 (count m2s)) (every? map? m2s))
            :m2s-shape)
  (let [records (atom [])
        occurrence (atom -1)]
    (doseq [[module-ordinal plan] (map-indexed vector m2s)]
      (let [functions (:functions plan)
            expected (get-in indexes [:definitions-by-module module-ordinal])
            module-instruction (atom -1)]
        (require! (and (map? functions) (= (set (keys expected))
                                              (set (keys functions))))
                  :m2-function-set {:module-ordinal module-ordinal})
        (doseq [caller (sort-by str (keys functions))]
          (let [definition (get functions caller)
                source-definition (get expected caller)
                hash (get-in indexes [:definition-hash
                                      [module-ordinal caller]])]
            (require! (= (select-keys source-definition
                                      [:name :params :body :arity])
                         (select-keys definition [:name :params :body :arity]))
                      :m2-source-definition-join
                      {:module-ordinal module-ordinal :caller caller})
            (require! (vector? (:instructions definition))
                      :m2-function-instructions
                      {:module-ordinal module-ordinal :caller caller})
            (doseq [[root-ordinal instruction]
                    (map-indexed vector (:instructions definition))]
              (scan-m2-instruction!
               records module-instruction occurrence instruction
               {:module-ordinal module-ordinal :caller caller
                :caller-function-source-hash hash
                :owner-by-function (:owner-by-function indexes)
                :builtin-symbols (:builtin-symbols indexes)}
               [root-ordinal]))))))
    {:records @records
     :row-count (count (filter :semantic-row @records))}))

(defn- digest-vector? [value count]
  (and (vector? value) (= count (clojure.core/count value))
       (every? sha256-id? value)))

(defn- validate-checker! [checker]
  (require! (exact-keys? checker checker-keys) :checker-schema)
  (require! (= :source-first-exhaustive (:algorithm-role checker))
            :checker-algorithm-role)
  (require! (every? sha256-id?
                    ((juxt :artifact-id :source-sha256 :semantic-id
                           :executable-id :dependency-closure-id) checker))
            :checker-identities))

(defn- validate-m0s! [m0s source-forms indexes]
  (require! (and (vector? m0s) (= 7 (count m0s))) :m0-count)
  (doseq [[ordinal descriptor] (map-indexed vector m0s)]
    (require! (exact-keys? descriptor m0-keys) :m0-schema
              {:module-ordinal ordinal})
    (require! (and (= :gravity/sh07-b51-module-source-v1
                      (:artifact descriptor))
                   (= 1 (:schema-version descriptor))
                   (= ordinal (:module-ordinal descriptor))
                   (symbol? (:module descriptor))
                   (string? (:logical-source-path descriptor))
                   (nonnegative-integer? (:source-byte-count descriptor))
                   (sha256-id? (:source-sha256 descriptor))
                   (contains? #{:meta} (:profile descriptor))
                   (contains? #{:jvm} (:target descriptor))
                   (set? (:effects descriptor))
                   (set? (:capabilities descriptor))
                   (= :safe (:safety descriptor))
                   (vector? (:definitions descriptor))
                   (vector? (:exports descriptor))
                   (vector? (:external-bindings descriptor))
                   (vector? (:builtin-calls descriptor))
                   (every? nonnegative-integer?
                           ((juxt :core-binding-count :lexical-binding-count
                                  :aggregate-binding-count) descriptor))
                   (<= (:aggregate-binding-count descriptor) 2440)
                   (= (:aggregate-binding-count descriptor)
                      (+ (:core-binding-count descriptor)
                         (:lexical-binding-count descriptor)
                         (count (:definitions descriptor))
                         (count (:external-bindings descriptor))))
                   (= (:module-source-id descriptor)
                      (canonical-hash (dissoc descriptor :module-source-id))))
              :m0-value {:module-ordinal ordinal})
    (let [source-definitions
          (get-in indexes [:definitions-by-module ordinal])
          expected-definitions
          (mapv (fn [form]
                  (let [definition (definition-from-form form)
                        name (:name definition)]
                    {:name name
                     :arity (:arity definition)
                     :visibility (if (some #{name} (:exports descriptor))
                                   :public :private)
                     :source-hash
                     (get-in indexes [:definition-hash [ordinal name]])}))
                (filter definition-from-form (nth source-forms ordinal)))]
      (require! (= expected-definitions (:definitions descriptor))
                :m0-source-definitions {:module-ordinal ordinal})
      (require! (= (set (:exports descriptor))
                   (set (map :name (filter #(= :public (:visibility %))
                                          (:definitions descriptor)))))
                :m0-export-visibility {:module-ordinal ordinal})
      (require! (= (set (keys source-definitions))
                   (set (map :name (:definitions descriptor))))
                :m0-definition-set {:module-ordinal ordinal}))
    (doseq [row (:definitions descriptor)]
      (require! (and (exact-keys? row definition-keys)
                     (symbol? (:name row))
                     (nonnegative-integer? (:arity row))
                     (contains? #{:public :private} (:visibility row))
                     (sha256-id? (:source-hash row)))
                :m0-definition-row {:module-ordinal ordinal}))
    (doseq [row (:external-bindings descriptor)]
      (require! (and (exact-keys? row external-binding-keys)
                     (symbol? (:symbol row))
                     (nonnegative-integer? (:callee-module-ordinal row))
                     (< (:callee-module-ordinal row) 7)
                     (symbol? (:callee-module row))
                     (sha256-id? (:callee-source-sha256 row))
                     (sha256-id? (:callee-definition-source-hash row))
                     (nonnegative-integer? (:arity row))
                     (keyword? (:owner-edge row)))
                :m0-external-binding {:module-ordinal ordinal}))
    (doseq [row (:builtin-calls descriptor)]
      (require! (and (exact-keys? row builtin-call-keys)
                     (symbol? (:caller row))
                     (sha256-id? (:caller-function-source-hash row))
                     (symbol? (:builtin row))
                     (nonnegative-integer? (:arity row))
                     (boolean? (:compiler-internal? row))
                     (= (contains? internal-builtin-arities (:builtin row))
                        (:compiler-internal? row))
                     (or (not (:compiler-internal? row))
                         (= (get internal-builtin-arities (:builtin row))
                            (:arity row))))
                :m0-builtin-row {:module-ordinal ordinal}))))

(defn- validate-m1! [m0s m1]
  (require! (exact-keys? m1 m1-keys) :m1-schema)
  (require! (and (= :gravity/sh07-b51-link-manifest-v1 (:artifact m1))
                 (= 1 (:schema-version m1))
                 (= (mapv :module-source-id m0s) (:module-source-ids m1))
                 (vector? (:edges m1))
                 (= (:manifest-id m1)
                    (canonical-hash (dissoc m1 :manifest-id))))
            :m1-value)
  (require! (= (vec (range (count (:edges m1))))
               (mapv :ordinal (:edges m1)))
            :m1-dense-ordinals)
  (doseq [row (:edges m1)]
    (require! (and (exact-keys? row external-row-keys)
                   (nonnegative-integer? (:ordinal row))
                   (sha256-id? (:caller-module-source-id row))
                   (symbol? (:caller-function row))
                   (sha256-id? (:caller-function-source-hash row))
                   (vector? (:callsite-path row))
                   (every? nonnegative-integer? (:callsite-path row))
                   (sha256-id? (:callee-module-source-id row))
                   (symbol? (:callee-function row))
                   (sha256-id? (:callee-function-source-hash row))
                   (nonnegative-integer? (:arity row))
                   (keyword? (:owner-edge row))
                   (= :authenticated-b51-namespace-external
                      (:authorization row)))
              :m1-edge-row {:ordinal (:ordinal row)})))

(defn- validate-plan-joins! [m0s m1 m2s emitter-source-id emitter-semantic-id]
  (doseq [[ordinal [descriptor plan]]
          (map-indexed vector (map vector m0s m2s))]
    (require! (and (map? plan) (sha256-id? (:plan-id plan))
                   (= (:module-source-id descriptor)
                      (:sh07-b51-module-source-id plan))
                   (= (:manifest-id m1) (:sh07-b51-link-manifest-id plan))
                   (= (:source-sha256 descriptor) (get-in plan [:source :sha256]))
                   (= (:module descriptor) (get-in plan [:module :module]))
                   (= emitter-source-id (get-in plan [:compiler :source-content-hash]))
                   (= emitter-semantic-id (get-in plan [:compiler :semantic-hash])))
              :m2-tier0-join {:module-ordinal ordinal})))

(defn- validate-bundle-core! [m0s m1 m2s bundle]
  (require! (exact-keys? bundle bundle-core-keys) :bundle-core-schema)
  (require! (and (= :gravity/sh07-b51-execution-bundle-core-id-v1
                    (:domain bundle))
                 (= 1 (:schema-version bundle))
                 (= (:manifest-id m1) (:manifest-id bundle)))
            :bundle-core-value)
  (let [coordinates (:module-plans bundle)
        routes (:routes bundle)
        roots (:public-roots bundle)
        plan-by-id (into {} (map (juxt :plan-id identity)) m2s)
        m0-by-id (into {} (map (juxt :module-source-id identity)) m0s)]
    (require! (and (vector? coordinates) (= 7 (count coordinates))
                   (every? #(exact-keys? % module-plan-coordinate-keys)
                           coordinates)
                   (= (vec (range 7)) (mapv :module-ordinal coordinates))
                   (= coordinates
                      (mapv (fn [ordinal descriptor plan]
                              {:module-ordinal ordinal
                               :module-source-id (:module-source-id descriptor)
                               :plan-id (:plan-id plan)})
                            (range) m0s m2s)))
              :bundle-core-module-plans)
    (require! (and (vector? routes)
                   (= (count (:edges m1)) (count routes))
                   (= (vec (range (count routes)))
                      (mapv :edge-ordinal routes))
                   (every? #(exact-keys? % route-keys) routes))
              :bundle-core-routes)
    (doseq [[edge route] (map vector (:edges m1) routes)]
      (require! (and (= (:ordinal edge) (:edge-ordinal route))
                     (= (:caller-module-source-id edge)
                        (:sh07-b51-module-source-id
                         (get plan-by-id (:caller-plan-id route))))
                     (= (:callee-module-source-id edge)
                        (:sh07-b51-module-source-id
                         (get plan-by-id (:callee-plan-id route)))))
                :bundle-core-route {:edge-ordinal (:ordinal edge)}))
    (require! (and (vector? roots) (= 8 (count roots))
                   (= (vec (range 1 9)) (mapv :root-ordinal roots))
                   (every? #(exact-keys? % public-root-keys) roots))
              :bundle-core-public-roots)
    (doseq [root roots]
      (let [plan (get plan-by-id (:plan-id root))
            descriptor (get m0-by-id (:sh07-b51-module-source-id plan))
            definition (get-in plan [:functions (:function root)])]
        (require! (and plan descriptor definition
                       (= (:arity root) (:arity definition)))
                  :bundle-core-public-root
                  {:root-ordinal (:root-ordinal root)})))))

(defn- derive-certificate-inputs [input]
  (let [{:keys [m0s m1 m2s bundle-core]} input]
    {:architecture-report-sha256 (:architecture-report-sha256 input)
     :snapshot-commit (:snapshot-commit input)
     :snapshot-tree (:snapshot-tree input)
     :predecessor-executable-descriptor-id
     (:predecessor-executable-descriptor-id input)
     :module-source-identities (mapv :source-sha256 m0s)
     :module-source-byte-counts (mapv :source-byte-count m0s)
     :emitter-source-id (:emitter-source-id input)
     :emitter-semantic-id (:emitter-semantic-id input)
     :m0-ids (mapv :module-source-id m0s)
     :m1-id (:manifest-id m1)
     :m2-ids (mapv :plan-id m2s)
     :bundle-core-id (canonical-hash bundle-core)}))

(defn- validate-envelope! [input]
  (require! (exact-keys? input input-keys) :input-schema)
  (require! (and (sha256-id? (:architecture-report-sha256 input))
                 (git-id? (:snapshot-commit input))
                 (git-id? (:snapshot-tree input))
                 (sha256-id? (:predecessor-executable-descriptor-id input))
                 (digest-vector? (:module-source-identities input) 7)
                 (and (vector? (:module-source-byte-counts input))
                      (= 7 (count (:module-source-byte-counts input)))
                      (every? nonnegative-integer?
                              (:module-source-byte-counts input)))
                 (sha256-id? (:emitter-source-id input))
                 (sha256-id? (:emitter-semantic-id input))
                 (exact-keys? (:provenance input) input-provenance-keys))
            :input-scalar-envelope)
  (validate-checker! (:checker input))
  (let [indexes (source-indexes (:source-forms input) (:m0s input))]
    (validate-m0s! (:m0s input) (:source-forms input) indexes)
    (validate-m1! (:m0s input) (:m1 input))
    (validate-plan-joins! (:m0s input) (:m1 input) (:m2s input)
                          (:emitter-source-id input)
                          (:emitter-semantic-id input))
    (validate-bundle-core! (:m0s input) (:m1 input) (:m2s input)
                           (:bundle-core input))
    (let [derived (derive-certificate-inputs input)]
      (require! (= (:module-source-identities input)
                   (:module-source-identities derived))
                :module-source-identity-join)
      (require! (= (:module-source-byte-counts input)
                   (:module-source-byte-counts derived))
                :module-source-byte-count-join)
      {:indexes indexes :certificate-inputs derived})))

(defn- definition-map [m0s]
  (into {}
        (for [descriptor m0s, row (:definitions descriptor)]
          [[(:module-ordinal descriptor) (:name row)] row])))

(defn- raw-call-records [raw]
  (filterv #(contains? #{:local-runtime-call :external-runtime-call
                         :builtin-call} (:classification %))
           (:records raw)))

(defn- m2-call-records [m2]
  (filterv :semantic-row (:records m2)))

(defn- normalized-call-frequencies [records classification-key classification]
  (frequencies
   (map #(select-keys % [:module-ordinal :caller
                         :caller-function-source-hash :callee :arity])
        (filter #(= classification (classification-key %)) records))))

(defn- owner-edge [caller callee]
  (cond
    (= (:owner caller) (:owner callee))
    (case (:owner caller)
      :r1-producer :producer-internal
      :r6-independent-verifier :root6-internal
      :shared-owner
      (fail! :unknown-same-owner-edge {:owner (:owner caller)}))

    (and (= :r8-finalizer (:owner caller))
         (= :r1-producer (:owner callee)))
    :root8-to-producer-interface

    :else (fail! :forbidden-owner-edge
                 {:caller (:module-ordinal caller)
                  :callee (:module-ordinal callee)})))

(defn- external-source-records [raw m0s]
  (let [owner-by-function (get-in raw [:indexes :owner-by-function])
        m0-by-ordinal (vec m0s)
        definitions (definition-map m0s)
        raw-rows
        (mapv
         (fn [record]
           (let [caller-ordinal (:module-ordinal record)
                 callee-ordinal (get owner-by-function (:callee record))
                 caller (nth m0-by-ordinal caller-ordinal)
                 callee (nth m0-by-ordinal callee-ordinal)
                 caller-definition
                 (get definitions [caller-ordinal (:caller record)])
                 callee-definition
                 (get definitions [callee-ordinal (:callee record)])
                 bindings (filter #(= (:callee record) (:symbol %))
                                  (:external-bindings caller))
                 binding (first bindings)
                 edge (owner-edge caller callee)]
             (require! (= 1 (count bindings)) :external-binding-cardinality
                       {:module-ordinal caller-ordinal
                        :callee (:callee record)})
             (require!
              (= {:symbol (:callee record)
                  :callee-module-ordinal callee-ordinal
                  :callee-module (:module callee)
                  :callee-source-sha256 (:source-sha256 callee)
                  :callee-definition-source-hash (:source-hash callee-definition)
                  :arity (:arity record)
                  :owner-edge edge}
                 binding)
              :external-binding-join {:module-ordinal caller-ordinal
                                      :callee (:callee record)})
             {:caller-module-source-id (:module-source-id caller)
              :caller-function (:caller record)
              :caller-function-source-hash (:source-hash caller-definition)
              :callsite-path (:callsite-path record)
              :callee-module-source-id (:module-source-id callee)
              :callee-function (:callee record)
              :callee-function-source-hash (:source-hash callee-definition)
              :arity (:arity record)
              :owner-edge edge
              :authorization :authenticated-b51-namespace-external}))
         (filter #(= :external-runtime-call (:classification %))
                 (:records raw)))
        by-source (into {} (map (juxt :module-source-id identity)) m0s)
        ordered (sort #(neg? (external-row-compare by-source %1 %2)) raw-rows)]
    (mapv #(assoc %1 :ordinal %2) ordered (range))))

(defn- validate-call-parity! [raw m2 m0s m1]
  (let [raw-calls (raw-call-records raw)
        m2-calls (m2-call-records m2)
        local-raw (normalized-call-frequencies raw-calls :classification
                                                :local-runtime-call)
        local-m2 (normalized-call-frequencies m2-calls :call-classification
                                               :local-runtime-call)
        source-external (external-source-records raw m0s)
        m2-external (filterv #(= :external-runtime-call
                                 (:call-classification %)) m2-calls)]
    (require! (= local-raw local-m2) :source-m2-local-parity)
    (require! (= source-external (:edges m1)) :source-m1-external-bijection)
    (require! (= (count (:edges m1)) (count m2-external))
              :m1-m2-external-count)
    (doseq [record m2-external]
      (let [link (:external-link record)]
        (require! (and (exact-keys? link
                                   #{:artifact :manifest-id :edge-ordinal})
                       (= :gravity/sh07-b51-external-call-v1 (:artifact link))
                       (= (:manifest-id m1) (:manifest-id link))
                       (nonnegative-integer? (:edge-ordinal link))
                       (< (:edge-ordinal link) (count (:edges m1))))
                  :m2-external-link)
        (let [edge (nth (:edges m1) (:edge-ordinal link))]
          (require! (and (= (:caller record) (:caller-function edge))
                         (= (:callee record) (:callee-function edge))
                         (= (:arity record) (:arity edge))
                         (= (:caller-function-source-hash record)
                            (:caller-function-source-hash edge))
                         (= (:module-source-id
                             (nth m0s (:module-ordinal record)))
                            (:caller-module-source-id edge)))
                    :m2-external-edge-join
                    {:edge-ordinal (:edge-ordinal link)}))))
    (require! (= (vec (range (count (:edges m1))))
                 (sort (mapv #(get-in % [:external-link :edge-ordinal])
                             m2-external)))
              :m2-external-link-bijection)
    {:source-external source-external
     :m1-external (:edges m1)}))

(defn- builtin-base [row]
  (select-keys row [:caller :caller-function-source-hash :builtin :arity
                    :compiler-internal?]))

(defn- builtin-record-compare [left right]
  (loop [fields [:caller :caller-function-source-hash :builtin :arity
                 :compiler-internal?]]
    (if (empty? fields)
      0
      (let [field (first fields)
            order (compare (canonical-sort-key (get left field))
                           (canonical-sort-key (get right field)))]
        (if (zero? order) (recur (rest fields)) order)))))

(defn- normalized-builtin-occurrences [rows]
  (let [ordered (sort #(neg? (builtin-record-compare %1 %2))
                      (map builtin-base rows))]
    (loop [remaining ordered previous nil duplicate -1 result []]
      (if (empty? remaining)
        result
        (let [row (first remaining)
              ordinal (if (= row previous) (inc duplicate) 0)]
          (recur (rest remaining) row ordinal
                 (conj result (assoc row :duplicate-ordinal ordinal))))))))

(defn- validate-provenance! [m2 m0s m1]
  (let [internal-set (set internal-builtins)
        occurrences
        (mapv
         (fn [record]
           (let [descriptor (nth m0s (:module-ordinal record))
                 provenance (:provenance record)
                 expected
                 {:artifact :gravity/sh07-compiler-internal-call-provenance-v2
                  :manifest-id (:manifest-id m1)
                  :module-source-id (:module-source-id descriptor)
                  :source-sha256 (:source-sha256 descriptor)
                  :function (:caller record)
                  :function-source-hash (:caller-function-source-hash record)
                  :callee (:callee record)}]
             (require! (= expected provenance) :m2-internal-provenance
                       {:module-ordinal (:module-ordinal record)
                        :instruction-ordinal (:instruction-ordinal record)})
             {:module-ordinal (:module-ordinal record)
              :instruction-ordinal (:instruction-ordinal record)
              :provenance provenance}))
         (filter #(and (= :builtin-call (:call-classification %))
                       (contains? internal-set (:callee %)))
                 (:records m2)))]
    (doseq [record (filter #(= :builtin-call (:call-classification %))
                           (:records m2))]
      (if (contains? internal-set (:callee record))
        (require! (:provenance-present record) :m2-internal-provenance-missing)
        (require! (and (not (:provenance-present record))
                       (nil? (:provenance record)))
                  :m2-ordinary-provenance-present)))
    (require! (= internal-set (set (map #(get-in % [:provenance :callee])
                                        occurrences)))
              :internal-builtin-set)
    occurrences))

(defn- validate-builtin-parity! [raw m2 m0s]
  (let [raw-by-module (group-by :module-ordinal
                                (filter #(= :builtin-call (:classification %))
                                        (:records raw)))
        m2-by-module (group-by :module-ordinal
                               (filter #(= :builtin-call
                                           (:call-classification %))
                                       (:records m2)))]
    (mapv
     (fn [ordinal descriptor]
       (let [m0-occurrences
             (normalized-builtin-occurrences (:builtin-calls descriptor))
             raw-occurrences
             (normalized-builtin-occurrences
              (mapv (fn [record]
                      {:caller (:caller record)
                       :caller-function-source-hash
                       (:caller-function-source-hash record)
                       :builtin (:callee record) :arity (:arity record)
                       :compiler-internal?
                       (contains? (set internal-builtins) (:callee record))})
                    (get raw-by-module ordinal [])))
             m2-occurrences
             (normalized-builtin-occurrences
              (mapv (fn [record]
                      {:caller (:caller record)
                       :caller-function-source-hash
                       (:caller-function-source-hash record)
                       :builtin (:callee record) :arity (:arity record)
                       :compiler-internal?
                       (contains? (set internal-builtins) (:callee record))})
                    (get m2-by-module ordinal [])))]
         (require! (= m0-occurrences raw-occurrences)
                   :source-m0-builtin-parity {:module-ordinal ordinal})
         (require! (= m0-occurrences m2-occurrences)
                   :m0-m2-builtin-parity {:module-ordinal ordinal})
         (let [source-order
               (mapv (fn [record]
                       {:caller (:caller record)
                        :caller-function-source-hash
                        (:caller-function-source-hash record)
                        :builtin (:callee record)
                        :arity (:arity record)
                        :compiler-internal?
                        (contains? (set internal-builtins)
                                   (:callee record))})
                     (sort-by (juxt (comp str :caller)
                                    :physical-ordinal)
                              (get raw-by-module ordinal [])))]
           (require! (= source-order (:builtin-calls descriptor))
                     :source-m0-builtin-order {:module-ordinal ordinal}))
         m0-occurrences))
     (range) m0s)))

(defn- reachable-functions [bundle m2-records]
  (let [calls-by-caller
        (group-by :caller
                  (filter #(contains? #{:local-runtime-call
                                        :external-runtime-call}
                                      (:call-classification %))
                          m2-records))
        roots (mapv :function (:public-roots bundle))]
    (loop [pending roots reached #{}]
      (if (empty? pending)
        reached
        (let [function (peek pending)
              remaining (pop pending)]
          (if (contains? reached function)
            (recur remaining reached)
            (recur (into remaining (map :callee
                                        (get calls-by-caller function [])))
                   (conj reached function))))))))

(defn- coordinate [m0s m2s module-ordinal function]
  {:module-ordinal module-ordinal
   :module-source-id (:module-source-id (nth m0s module-ordinal))
   :plan-id (:plan-id (nth m2s module-ordinal))
   :function function})

(defn- semantic-category-rows [m0s m1 m2s bundle m2-records reachable]
  (let [owner (into {}
                    (for [descriptor m0s, definition (:definitions descriptor)]
                      [(:name definition) (:module-ordinal descriptor)]))
        ordered-members (sort-by (fn [function]
                                   [(get owner function) (str function)])
                                 reachable)
        definitions (definition-map m0s)
        members
        (mapv (fn [ordinal function]
                {:member-ordinal ordinal
                 :coordinate (coordinate m0s m2s (get owner function) function)})
              (range) ordered-members)
        functions
        (mapv (fn [ordinal function]
                (let [module-ordinal (get owner function)]
                  {:member-ordinal ordinal
                   :coordinate (coordinate m0s m2s module-ordinal function)
                   :definition-source-hash
                   (:source-hash (get definitions [module-ordinal function]))}))
              (range) ordered-members)
        reachable-calls (filter #(contains? reachable (:caller %)) m2-records)
        local-calls (filterv #(= :local-runtime-call
                                 (:call-classification %)) reachable-calls)
        local-rows
        (mapv (fn [ordinal row]
                {:edge-ordinal ordinal
                 :caller (coordinate m0s m2s (:module-ordinal row)
                                     (:caller row))
                 :callee (coordinate m0s m2s (get owner (:callee row))
                                     (:callee row))
                 :arity (:arity row)
                 :source-callsite-ordinal ordinal})
              (range) local-calls)
        reachable-external
        (filterv #(contains? reachable (:caller-function %)) (:edges m1))
        route-by-ordinal (into {} (map (juxt :edge-ordinal identity))
                               (:routes bundle))
        descriptor-by-id (into {} (map (juxt :module-source-id identity)) m0s)
        external-rows
        (mapv
         (fn [semantic-ordinal edge]
           (let [caller (get descriptor-by-id (:caller-module-source-id edge))
                 callee (get descriptor-by-id (:callee-module-source-id edge))]
             {:semantic-edge-ordinal semantic-ordinal
              :m1-edge-ordinal (:ordinal edge)
              :manifest-id (:manifest-id m1)
              :caller (coordinate m0s m2s (:module-ordinal caller)
                                  (:caller-function edge))
              :callee (coordinate m0s m2s (:module-ordinal callee)
                                  (:callee-function edge))
              :arity (:arity edge)
              :owner-edge (:owner-edge edge)
              :route (get route-by-ordinal (:ordinal edge))}))
         (range) reachable-external)
        builtin-rows-source
        (mapcat (fn [descriptor]
                  (filter #(contains? reachable (:caller %))
                          (:builtin-calls descriptor)))
                m0s)
        builtin-rows
        (mapv (fn [ordinal row]
                {:occurrence-ordinal ordinal
                 :caller (coordinate m0s m2s (get owner (:caller row))
                                     (:caller row))
                 :builtin (:builtin row)
                 :arity (:arity row)
                 :compiler-internal? (:compiler-internal? row)
                 :source-callsite-ordinal ordinal})
              (range) builtin-rows-source)
        plan-ordinal (into {} (map-indexed (fn [ordinal plan]
                                            [(:plan-id plan) ordinal]) m2s))
        root-rows
        (mapv (fn [root]
                {:root-ordinal (:root-ordinal root)
                 :coordinate (coordinate m0s m2s
                                         (get plan-ordinal (:plan-id root))
                                         (:function root))
                 :arity (:arity root)})
              (:public-roots bundle))]
    (array-map
     :roots root-rows
     :members members
     :functions functions
     :local-runtime-call-edges local-rows
     :external-runtime-call-edges external-rows
     :referenced-builtins builtin-rows
     :module-contracts
     (mapv (fn [descriptor]
             {:module-ordinal (:module-ordinal descriptor)
              :module-source-id (:module-source-id descriptor)})
           m0s))))

(defn- retained-profile-within? [value class-name]
  (let [profile (value-profile value)
        key-for (fn [suffix]
                  (keyword (str "maximum-" class-name "-" suffix)))
        c11-bytes
        (alength (.getBytes
                  (binding [*print-length* nil *print-level* nil
                            *print-meta* true]
                    (pr-str value))
                  java.nio.charset.StandardCharsets/UTF_8))]
    (and (<= (:nodes profile) (get category-schedule (key-for "nodes")))
         (<= (:depth profile) (get category-schedule (key-for "depth")))
         (<= (:width profile) (get category-schedule (key-for "width")))
         (<= (:scalar-utf8 profile)
             (get category-schedule (key-for "scalar-utf8")))
         (<= c11-bytes (get category-schedule (key-for "c11-bytes")))
         (<= (:canonical-reader-bytes profile)
             (get category-schedule (key-for "canonical-reader-bytes"))))))

(defn- category-product [category rows]
  (doseq [row rows]
    (require! (retained-profile-within? row "row")
              :category-row-profile {:category category}))
  (let [groups (if (empty? rows) [[]] (partition-all 16 rows))
        leaves
        (mapv (fn [ordinal group]
                (let [value
                      (array-map
                       :domain :gravity/sh07-b51-linked-closure-leaf-v2
                       :schema-version 2
                       :category category
                       :shard-ordinal ordinal
                       :first-row-ordinal (* 16 ordinal)
                       :row-count (count group)
                       :category-row-count (count rows)
                       :rows (vec group))]
                  (require! (retained-profile-within? value "leaf")
                            :category-leaf-profile {:category category})
                  value))
              (range) groups)]
    (loop [level 1
           children (mapv (fn [ordinal leaf]
                            {:child-ordinal ordinal
                             :first-row-ordinal (:first-row-ordinal leaf)
                             :row-count (:row-count leaf)
                             :digest (canonical-hash leaf)})
                          (range) leaves)
           folds []]
      (require! (<= level 3) :category-fold-level {:category category})
      (let [current
            (mapv
             (fn [group-ordinal group]
               (let [value
                     (array-map
                      :domain :gravity/sh07-b51-linked-closure-fold-v2
                      :schema-version 2
                      :category category
                      :level level
                      :group-ordinal group-ordinal
                      :first-child-ordinal (* 16 group-ordinal)
                      :child-count (count group)
                      :row-count (reduce + (map :row-count group))
                      :children (vec group))]
                 (require! (retained-profile-within? value "fold")
                           :category-fold-profile {:category category})
                 value))
             (range) (partition-all 16 children))
            all-folds (into folds current)]
        (if (= 1 (count current))
          {:leaves leaves
           :folds all-folds
           :commitment
           (let [commitment
                 (array-map
                  :category category
                  :row-count (count rows)
                  :leaf-count (count leaves)
                  :fold-level-count level
                  :root-digest (canonical-hash (first current)))]
             (require! (retained-profile-within? commitment "commitment")
                       :category-commitment-profile {:category category})
             commitment)}
          (recur
           (inc level)
           (mapv (fn [ordinal value]
                   {:child-ordinal ordinal
                    :first-row-ordinal
                    (:first-row-ordinal (first (:children value)))
                    :row-count (:row-count value)
                    :digest (canonical-hash value)})
                 (range) current)
           all-folds))))))

(defn- category-products [rows]
  (require! (= categories (vec (keys rows))) :category-order)
  (require! (<= (reduce + (map count (vals rows)))
                (:maximum-total-rows category-schedule))
            :category-row-total)
  (mapv #(category-product % (get rows %)) categories))

(defn- physical-census [m0s m1 m2s raw m2 reachable rows]
  (let [raw-calls (raw-call-records raw)
        m2-calls (m2-call-records m2)
        module-rows
        (mapv
         (fn [ordinal descriptor plan]
           (let [raw-module (filter #(= ordinal (:module-ordinal %)) raw-calls)
                 m2-module (filter #(= ordinal (:module-ordinal %)) m2-calls)
                 reachable-calls (filter #(contains? reachable (:caller %))
                                         m2-module)
                 external-source (filter #(= (:module-source-id descriptor)
                                             (:caller-module-source-id %))
                                         (:edges m1))
                 reachable-external
                 (filter #(contains? reachable (:caller-function %))
                         external-source)]
             (array-map
              :module-ordinal ordinal
              :module (:module descriptor)
              :logical-source-path (:logical-source-path descriptor)
              :owner (:owner descriptor)
              :source-byte-count (:source-byte-count descriptor)
              :source-sha256 (:source-sha256 descriptor)
              :module-source-id (:module-source-id descriptor)
              :plan-id (:plan-id plan)
              :physical-definition-count (count (:definitions descriptor))
              :reachable-definition-count
              (count (filter #(contains? reachable (:name %))
                             (:definitions descriptor)))
              :physical-local-runtime-callsite-count
              (count (filter #(= :local-runtime-call (:classification %))
                             raw-module))
              :reachable-local-runtime-callsite-count
              (count (filter #(= :local-runtime-call
                                 (:call-classification %)) reachable-calls))
              :physical-external-runtime-callsite-count
              (count external-source)
              :reachable-external-runtime-callsite-count
              (count reachable-external)
              :physical-referenced-builtin-occurrence-count
              (count (:builtin-calls descriptor))
              :reachable-referenced-builtin-occurrence-count
              (count (filter #(and (= :builtin-call
                                      (:call-classification %))
                                   (contains? reachable (:caller %)))
                             m2-module))
              :core-binding-count (:core-binding-count descriptor)
              :lexical-binding-count (:lexical-binding-count descriptor)
              :external-binding-count (count (:external-bindings descriptor))
              :aggregate-binding-count (:aggregate-binding-count descriptor))))
         (range) m0s m2s)
        sum-field #(reduce + (map % module-rows))
        occupancies (mapv :aggregate-binding-count module-rows)
        semantic-count (reduce + (map count (vals rows)))
        census
        (array-map
         :module-count 7
         :module-censuses module-rows
         :physical-definition-count (sum-field :physical-definition-count)
         :reachable-definition-count (sum-field :reachable-definition-count)
         :physical-local-runtime-callsite-count
         (sum-field :physical-local-runtime-callsite-count)
         :reachable-local-runtime-callsite-count
         (sum-field :reachable-local-runtime-callsite-count)
         :physical-external-runtime-callsite-count
         (sum-field :physical-external-runtime-callsite-count)
         :reachable-external-runtime-callsite-count
         (sum-field :reachable-external-runtime-callsite-count)
         :physical-referenced-builtin-occurrence-count
         (sum-field :physical-referenced-builtin-occurrence-count)
         :reachable-referenced-builtin-occurrence-count
         (sum-field :reachable-referenced-builtin-occurrence-count)
         :semantic-row-count semantic-count
         :module-binding-occupancies occupancies
         :maximum-binding-occupancy (apply max occupancies)
         :binding-ceiling 2440)]
    (require! (and (exact-keys? census physical-census-keys)
                   (every? #(exact-keys? % module-census-keys) module-rows)
                   (<= (:maximum-binding-occupancy census) 2440))
              :physical-census-schema)
    census))

(defn- transcript-evidence [artifact records row-count checker inputs]
  (let [raw? (= artifact :gravity/sh07-b51-raw-source-census-transcript-v1)
        root-domain (if raw?
                      :gravity/sh07-b51-raw-source-census-record-root-v1
                      :gravity/sh07-b51-m2-census-record-root-v1)
        record-keys (if raw? raw-record-keys m2-record-keys)]
    (require! (every? #(exact-keys? % record-keys) records)
              :transcript-record-schema {:artifact artifact})
    (require! (= (vec (range (count records))) (mapv :node-ordinal records))
              :transcript-node-ordinals {:artifact artifact})
    (let [record-root
          (canonical-hash
           {:domain root-domain
            :schema-version 1
            :checker-algorithm-role :source-first-exhaustive
            :records records})
          descriptor-base {:artifact artifact
                           :schema-version 1
                           :node-count (count records)
                           :row-count row-count
                           :record-root record-root}
          artifact-id
          (canonical-hash
           {:domain :gravity/sh07-b51-census-transcript-artifact-id-v1
            :schema-version 1
            :artifact artifact
            :checker checker
            :inputs inputs
            :node-count (count records)
            :row-count row-count
            :record-root record-root})
          descriptor (assoc descriptor-base :artifact-id artifact-id)
          evidence
          {:artifact (if raw?
                       :gravity/sh07-b51-raw-source-census-transcript-evidence-v1
                       :gravity/sh07-b51-m2-census-transcript-evidence-v1)
           :schema-version 1
           :checker-algorithm-role :source-first-exhaustive
           :descriptor descriptor
           :records records}
          metrics {:node-count (count records)
                   :record-count (count records)
                   :call-row-count row-count
                   :byte-count
                   (alength (.getBytes
                             (binding [*print-length* nil *print-level* nil
                                       *print-meta* true]
                               (pr-str evidence))
                             java.nio.charset.StandardCharsets/UTF_8))}]
      (require! (offline-transcript-within-bounds? metrics)
                :offline-transcript-bounds
                {:artifact artifact :metrics metrics})
      evidence)))

(defn- external-commitment [m1-id source-records m1-records]
  (let [source-root-preimage
        {:domain :gravity/sh07-b51-ordered-source-external-root-v1
         :schema-version 1
         :records source-records}
        m1-root-preimage
        {:domain :gravity/sh07-b51-ordered-m1-external-root-v1
         :schema-version 1
         :records m1-records}
        preimage
        {:domain :gravity/sh07-b51-external-call-bijection-commitment-v1
         :schema-version 1
         :m1-id m1-id
         :physical-external-count (count source-records)
         :ordered-source-external-root (canonical-hash source-root-preimage)
         :ordered-m1-root (canonical-hash m1-root-preimage)
         :bijection-complete true}]
    {:id (canonical-hash preimage)
     :preimage preimage
     :source-root-preimage source-root-preimage
     :m1-root-preimage m1-root-preimage}))

(defn- builtin-commitments [m0s m2s occurrences-by-module]
  (mapv
   (fn [ordinal descriptor plan occurrences]
     (let [root-preimage
           {:domain :gravity/sh07-b51-ordered-builtin-multiset-root-v1
            :schema-version 1
            :module-ordinal ordinal
            :occurrences occurrences}
           preimage
           {:domain :gravity/sh07-b51-builtin-parity-commitment-v1
            :schema-version 1
            :module-ordinal ordinal
            :m0-id (:module-source-id descriptor)
            :m2-id (:plan-id plan)
            :occurrence-count (count occurrences)
            :ordered-multiset-root (canonical-hash root-preimage)
            :parity-complete true}]
       {:id (canonical-hash preimage)
        :preimage preimage
        :root-preimage root-preimage}))
   (range) m0s m2s occurrences-by-module))

(defn- provenance-commitment [m0s m1 m2s occurrences]
  (let [root-preimage
        {:domain :gravity/sh07-b51-provenance-occurrence-root-v1
         :schema-version 1
         :occurrences occurrences}
        preimage
        {:domain :gravity/sh07-b51-provenance-completeness-commitment-v1
         :schema-version 1
         :m0-ids (mapv :module-source-id m0s)
         :m1-id (:manifest-id m1)
         :m2-ids (mapv :plan-id m2s)
         :internal-builtins internal-builtins
         :occurrence-count (count occurrences)
         :occurrence-root (canonical-hash root-preimage)
         :provenance-complete true}]
    {:id (canonical-hash preimage)
     :preimage preimage
     :root-preimage root-preimage}))

(defn- normalized-census-id [physical commitments]
  (canonical-hash
   {:domain :gravity/sh07-b51-normalized-census-id-v1
    :schema-version 1
    :physical-census physical
    :ordered-category-row-roots (mapv :root-digest commitments)
    :category-row-counts (mapv :row-count commitments)}))

(defn- host-pin-boundary-id []
  (canonical-hash
   {:domain :gravity/sh07-b51-host-pin-boundary-id-v1
    :schema-version 1
    :component :p15-s23-sh07-b51-plan-linker
    :pin-key-names [:expected-m3-id :expected-certificate-a-id
                    :expected-certificate-b-id :expected-semantic-root]
    :caller-supplied false}))

(defn- runtime-token-boundary-id []
  (canonical-hash
   {:domain :gravity/sh07-b51-runtime-token-boundary-id-v1
    :schema-version 1
    :component :p15-s23-sh07-b51-runtime-context
    :token-kind :private-object-identity
    :caller-supplied false}))

(defn- accepted-assumptions []
  {:sha256-collision-resistance :assumed
   :canonical-reader-hash-id
   :gravity/sh07-declared-digest-hash-reader-canonical-hash-v1
   :source-emitter-bytes-immutable true
   :checker-closures-independent true
   :host-pin-boundary-id (host-pin-boundary-id)
   :runtime-token-boundary-id (runtime-token-boundary-id)})

(defn- result-map [physical normalized-id]
  {:status :accepted
   :normalized-census-id normalized-id
   :definition-count (:physical-definition-count physical)
   :local-count (:physical-local-runtime-callsite-count physical)
   :external-count (:physical-external-runtime-callsite-count physical)
   :builtin-count (:physical-referenced-builtin-occurrence-count physical)
   :semantic-row-count (:semantic-row-count physical)
   :external-bijection-complete true
   :builtin-parity-complete true
   :provenance-complete true
   :ordering-complete true
   :reachability-complete true})

(defn- common-claim-id [architecture-report-sha256 inputs assumptions claim]
  (canonical-hash
   {:domain :gravity/sh07-b51-common-census-claim-id-v1
    :schema-version 1
    :architecture-report-sha256 architecture-report-sha256
    :inputs inputs
    :assumptions assumptions
    :claim claim
    :profile-bounds profile-bounds}))

(defn- evidence-root [checker inputs raw-descriptor m2-descriptor result
                      reproduction-command-id]
  (canonical-hash
   {:domain :gravity/sh07-b51-producer-census-evidence-root-v1
    :schema-version 1
    :checker checker
    :inputs inputs
    :raw-source-transcript raw-descriptor
    :m2-transcript m2-descriptor
    :result result
    :reproduction-command-id reproduction-command-id
    :profile-bounds profile-bounds}))

(defn- rejection [reason context]
  {:artifact :gravity/sh07-b51-census-checker-rejection-evidence-v1
   :schema-version 1
   :status :rejected
   :role :producer-census
   :checker-algorithm-role :source-first-exhaustive
   :reason (if (keyword? reason) reason :checker-failure)
   :context (if (map? context)
              (select-keys context
                           [:module-ordinal :caller :callee :ordinal
                            :edge-ordinal :root-ordinal :artifact :metrics])
              {})})

(defn- certificate-inputs-valid? [inputs]
  (and (exact-keys? inputs certificate-input-keys)
       (sha256-id? (:architecture-report-sha256 inputs))
       (git-id? (:snapshot-commit inputs))
       (git-id? (:snapshot-tree inputs))
       (sha256-id? (:predecessor-executable-descriptor-id inputs))
       (digest-vector? (:module-source-identities inputs) 7)
       (vector? (:module-source-byte-counts inputs))
       (= 7 (count (:module-source-byte-counts inputs)))
       (every? nonnegative-integer? (:module-source-byte-counts inputs))
       (sha256-id? (:emitter-source-id inputs))
       (sha256-id? (:emitter-semantic-id inputs))
       (digest-vector? (:m0-ids inputs) 7)
       (sha256-id? (:m1-id inputs))
       (digest-vector? (:m2-ids inputs) 7)
       (sha256-id? (:bundle-core-id inputs))))

(defn- checker-valid? [checker]
  (and (exact-keys? checker checker-keys)
       (= :source-first-exhaustive (:algorithm-role checker))
       (every? sha256-id?
               ((juxt :artifact-id :source-sha256 :semantic-id :executable-id
                      :dependency-closure-id) checker))))

(defn- transcript-descriptor-valid? [descriptor artifact]
  (and (exact-keys? descriptor transcript-descriptor-keys)
       (= artifact (:artifact descriptor))
       (= 1 (:schema-version descriptor))
       (nonnegative-integer? (:node-count descriptor))
       (nonnegative-integer? (:row-count descriptor))
       (<= (:row-count descriptor) (:node-count descriptor))
       (sha256-id? (:artifact-id descriptor))
       (sha256-id? (:record-root descriptor))))

(defn- category-commitments-valid? [commitments]
  (and (vector? commitments) (= 7 (count commitments))
       (= categories (mapv :category commitments))
       (every?
        #(and (exact-keys? %
                           #{:category :row-count :leaf-count
                             :fold-level-count :root-digest})
              (nonnegative-integer? (:row-count %))
              (pos-int? (:leaf-count %))
              (and (pos-int? (:fold-level-count %))
                   (<= (:fold-level-count %) 3))
              (sha256-id? (:root-digest %)))
        commitments)))

(defn- physical-census-valid? [physical]
  (and (exact-keys? physical physical-census-keys)
       (= 7 (:module-count physical))
       (vector? (:module-censuses physical))
       (= 7 (count (:module-censuses physical)))
       (= (vec (range 7))
          (mapv :module-ordinal (:module-censuses physical)))
       (every? #(exact-keys? % module-census-keys)
               (:module-censuses physical))
       (every? nonnegative-integer?
               ((juxt :physical-definition-count :reachable-definition-count
                      :physical-local-runtime-callsite-count
                      :reachable-local-runtime-callsite-count
                      :physical-external-runtime-callsite-count
                      :reachable-external-runtime-callsite-count
                      :physical-referenced-builtin-occurrence-count
                      :reachable-referenced-builtin-occurrence-count
                      :semantic-row-count :maximum-binding-occupancy
                      :binding-ceiling) physical))
       (= 2440 (:binding-ceiling physical))
       (<= (:maximum-binding-occupancy physical) 2440)
       (vector? (:module-binding-occupancies physical))
       (= 7 (count (:module-binding-occupancies physical)))
       (every? nonnegative-integer? (:module-binding-occupancies physical))))

(defn certificate-valid?
  "Validate the closed compact checker-A certificate without importing or
  invoking the offline scanners.  Host integration must additionally compare
  the certificate and its id with immutable expected literals."
  [certificate]
  (try
    (let [claim (:claim certificate)
          assumptions (:assumptions certificate)
          inputs (:inputs certificate)
          result (:result certificate)
          provenance (:provenance certificate)
          raw (:raw-source-transcript certificate)
          m2 (:m2-transcript certificate)
          physical (:physical-census claim)
          commitments (:category-commitments claim)
          normalized
          (when (and (physical-census-valid? physical)
                     (category-commitments-valid? commitments))
            (normalized-census-id physical commitments))
          expected-claim-id
          (when (and (certificate-inputs-valid? inputs)
                     (exact-keys? assumptions assumptions-keys)
                     (exact-keys? claim claim-keys))
            (common-claim-id (:architecture-report-sha256 certificate)
                             inputs assumptions claim))
          expected-evidence
          (when (and (checker-valid? (:checker certificate))
                     (transcript-descriptor-valid?
                      raw :gravity/sh07-b51-raw-source-census-transcript-v1)
                     (transcript-descriptor-valid?
                      m2 :gravity/sh07-b51-m2-census-transcript-v1)
                     (exact-keys? result result-keys)
                     (exact-keys? provenance certificate-provenance-keys))
            (evidence-root (:checker certificate) inputs raw m2 result
                           (:reproduction-command-id provenance)))]
      (boolean
       (and (exact-keys? certificate certificate-keys)
            (= :gravity/sh07-b51-compile-census-certificate
               (:artifact certificate))
            (= :gravity/sh07-b51-producer-census-certificate-v1
               (:domain certificate))
            (= 1 (:schema-version certificate))
            (= :accepted (:status certificate) (:status result))
            (= :producer-census (:role certificate))
            (sha256-id? (:architecture-report-sha256 certificate))
            (= (:architecture-report-sha256 certificate)
               (:architecture-report-sha256 inputs))
            (= :source-first-exhaustive
               (get-in certificate [:checker :algorithm-role]))
            (certificate-inputs-valid? inputs)
            (checker-valid? (:checker certificate))
            (physical-census-valid? physical)
            (category-commitments-valid? commitments)
            (= profile-bounds (:profile-bounds certificate))
            (= category-schedule (:category-schedule claim))
            (= :gravity/sh07-b51-common-census-claim-v1 (:domain claim))
            (= normalized (:normalized-census-id claim)
               (:normalized-census-id result))
            (= expected-claim-id (:claim-id certificate))
            (= expected-evidence (:evidence-root certificate))
            (= (:artifact-id raw) (:raw-transcript-artifact-id provenance))
            (= (:artifact-id m2) (:m2-transcript-artifact-id provenance))
            (transcript-descriptor-valid?
             raw :gravity/sh07-b51-raw-source-census-transcript-v1)
            (transcript-descriptor-valid?
             m2 :gravity/sh07-b51-m2-census-transcript-v1)
            (sha256-id? (:external-bijection-commitment claim))
            (digest-vector? (:builtin-parity-commitments claim) 7)
            (sha256-id? (:provenance-completeness-commitment claim))
            (= (:definition-count result)
               (:physical-definition-count physical))
            (= (:local-count result)
               (:physical-local-runtime-callsite-count physical))
            (= (:external-count result)
               (:physical-external-runtime-callsite-count physical))
            (= (:builtin-count result)
               (:physical-referenced-builtin-occurrence-count physical))
            (= (:semantic-row-count result) (:semantic-row-count physical)
               (reduce + (map :row-count commitments)))
            (every? true? ((juxt :external-bijection-complete
                                 :builtin-parity-complete
                                 :provenance-complete :ordering-complete
                                 :reachability-complete) result))
            (= (accepted-assumptions) assumptions)
            (certificate-profile-within-bounds? (value-profile certificate))
            (= (:certificate-id certificate)
               (canonical-hash (dissoc certificate :certificate-id))))))
    (catch Throwable _ false)))

(defn checker-output-valid?
  "Validate an accepted checker output and both governed full transcripts.
  This predicate recomputes transcript record roots and compact certificate
  identities but never rescans source or M2."
  [output]
  (try
    (let [certificate (:certificate output)
          raw-evidence (:raw-source-transcript-evidence output)
          m2-evidence (:m2-transcript-evidence output)
          valid-transcript?
          (fn [evidence raw?]
            (let [records (:records evidence)
                  descriptor (:descriptor evidence)
                  root-domain (if raw?
                                :gravity/sh07-b51-raw-source-census-record-root-v1
                                :gravity/sh07-b51-m2-census-record-root-v1)
                  expected-root
                  (canonical-hash
                   {:domain root-domain :schema-version 1
                    :checker-algorithm-role :source-first-exhaustive
                    :records records})
                  expected-artifact-id
                  (canonical-hash
                   {:domain
                    :gravity/sh07-b51-census-transcript-artifact-id-v1
                    :schema-version 1
                    :artifact (:artifact descriptor)
                    :checker (:checker certificate)
                    :inputs (:inputs certificate)
                    :node-count (count records)
                    :row-count (:row-count descriptor)
                    :record-root expected-root})
                  record-keys (if raw? raw-record-keys m2-record-keys)]
              (and (exact-keys? evidence
                                #{:artifact :schema-version
                                  :checker-algorithm-role :descriptor :records})
                   (vector? records)
                   (= 1 (:schema-version evidence))
                   (= :source-first-exhaustive
                      (:checker-algorithm-role evidence))
                   (every? #(exact-keys? % record-keys) records)
                   (= (vec (range (count records)))
                      (mapv :node-ordinal records))
                   (= expected-root (:record-root descriptor))
                   (= expected-artifact-id (:artifact-id descriptor))
                   (= (count records) (:node-count descriptor))
                   (= (count (filter :semantic-row records))
                      (:row-count descriptor))
                   (= descriptor
                      (if raw?
                        (:raw-source-transcript certificate)
                        (:m2-transcript certificate))))))]
      (boolean
       (and (exact-keys? output
                         #{:artifact :schema-version :status :certificate
                           :raw-source-transcript-evidence
                           :m2-transcript-evidence :commitment-evidence})
            (= :gravity/sh07-b51-producer-census-checker-output-v1
               (:artifact output))
            (= 1 (:schema-version output))
            (= :accepted (:status output))
            (certificate-valid? certificate)
            (valid-transcript? raw-evidence true)
            (valid-transcript? m2-evidence false))))
    (catch Throwable _ false)))

(defn- produce-accepted [input]
  (let [{:keys [indexes certificate-inputs]} (validate-envelope! input)
        raw (scan-raw-source (:source-forms input) (:m0s input))
        _ (require! (= indexes (:indexes raw)) :source-index-stability)
        m2 (scan-m2 (:m2s input) indexes)
        call-evidence (validate-call-parity! raw m2 (:m0s input) (:m1 input))
        builtin-occurrences (validate-builtin-parity! raw m2 (:m0s input))
        provenance-occurrences
        (validate-provenance! m2 (:m0s input) (:m1 input))
        reachable (reachable-functions (:bundle-core input) (:records m2))
        all-functions (set (keys (:owner-by-function indexes)))
        _ (require! (set/subset? reachable all-functions)
                    :reachability-unknown-function)
        rows (semantic-category-rows (:m0s input) (:m1 input) (:m2s input)
                                     (:bundle-core input) (:records m2)
                                     reachable)
        products (category-products rows)
        commitments (mapv :commitment products)
        physical (physical-census (:m0s input) (:m1 input) (:m2s input)
                                  raw m2 reachable rows)
        normalized-id (normalized-census-id physical commitments)
        external (external-commitment (:manifest-id (:m1 input))
                                      (:source-external call-evidence)
                                      (:m1-external call-evidence))
        builtin (builtin-commitments (:m0s input) (:m2s input)
                                     builtin-occurrences)
        provenance-commitment-value
        (provenance-commitment (:m0s input) (:m1 input) (:m2s input)
                               provenance-occurrences)
        claim {:domain :gravity/sh07-b51-common-census-claim-v1
               :normalized-census-id normalized-id
               :external-bijection-commitment (:id external)
               :builtin-parity-commitments (mapv :id builtin)
               :provenance-completeness-commitment
               (:id provenance-commitment-value)
               :physical-census physical
               :category-schedule category-schedule
               :category-commitments commitments}
        assumptions (accepted-assumptions)
        result (result-map physical normalized-id)
        raw-evidence
        (transcript-evidence
         :gravity/sh07-b51-raw-source-census-transcript-v1
         (:records raw) (:row-count raw) (:checker input) certificate-inputs)
        m2-evidence
        (transcript-evidence
         :gravity/sh07-b51-m2-census-transcript-v1
         (:records m2) (:row-count m2) (:checker input) certificate-inputs)
        provenance
        (assoc (:provenance input)
               :raw-transcript-artifact-id
               (get-in raw-evidence [:descriptor :artifact-id])
               :m2-transcript-artifact-id
               (get-in m2-evidence [:descriptor :artifact-id]))
        claim-id (common-claim-id (:architecture-report-sha256 input)
                                  certificate-inputs assumptions claim)
        evidence-id
        (evidence-root (:checker input) certificate-inputs
                       (:descriptor raw-evidence) (:descriptor m2-evidence)
                       result (:reproduction-command-id provenance))
        certificate-base
        {:artifact :gravity/sh07-b51-compile-census-certificate
         :domain :gravity/sh07-b51-producer-census-certificate-v1
         :schema-version 1
         :status :accepted
         :role :producer-census
         :architecture-report-sha256 (:architecture-report-sha256 input)
         :claim-id claim-id
         :claim claim
         :assumptions assumptions
         :checker (:checker input)
         :inputs certificate-inputs
         :result result
         :provenance provenance
         :raw-source-transcript (:descriptor raw-evidence)
         :m2-transcript (:descriptor m2-evidence)
         :profile-bounds profile-bounds
         :evidence-root evidence-id}
        certificate (assoc certificate-base :certificate-id
                           (canonical-hash certificate-base))
        output
        {:artifact :gravity/sh07-b51-producer-census-checker-output-v1
         :schema-version 1
         :status :accepted
         :certificate certificate
         :raw-source-transcript-evidence raw-evidence
         :m2-transcript-evidence m2-evidence
         :commitment-evidence
         {:external external
          :builtin builtin
          :provenance provenance-commitment-value
          :category-products products}}]
    (require! (certificate-valid? certificate) :constructed-certificate-invalid)
    (require! (checker-output-valid? output) :constructed-output-invalid)
    output))

(defn produce-certificate
  "Produce checker A's certificate and full transcript evidence from one
  immutable closed snapshot envelope.  Failure returns only closed rejection
  evidence and never exposes a partial accepted identity."
  [input]
  (try
    (produce-accepted input)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch clojure.lang.ExceptionInfo error
      (let [{:keys [reason context]} (ex-data error)]
        (rejection reason context)))
    (catch Throwable _
      (rejection :checker-failure {}))))
