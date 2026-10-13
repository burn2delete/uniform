(ns gravity.tools.sh07-b51-attempt30-independent-census
  (:require [gravity.c2-artifact-identity.canonical :as canonical]
            [gravity.digest :as digest]))

;; Attempt 30 checker B.  This namespace intentionally owns its traversal,
;; ordering, quote handling, reachability, row construction, and digest
;; schedule.  The only semantic primitive shared with the implementation is
;; the normative reader-canonical scalar hash.

(def input-keys
  #{:architecture-report-sha256 :snapshot-commit :snapshot-tree
    :predecessor-executable-descriptor-id :module-source-identities
    :module-source-byte-counts :emitter-source-id :emitter-semantic-id
    :m0s :m1 :m2s :bundle-core :source-forms :checker :provenance})

(def checker-keys
  #{:artifact-id :source-sha256 :semantic-id :executable-id
    :dependency-closure-id :algorithm-role})

(def input-identity-keys
  #{:architecture-report-sha256 :snapshot-commit :snapshot-tree
    :predecessor-executable-descriptor-id :module-source-identities
    :module-source-byte-counts :emitter-source-id :emitter-semantic-id
    :m0-ids :m1-id :m2-ids :bundle-core-id})

(def provenance-input-keys
  #{:invocation-id :tool-closure-id :reproduction-command-id
    :evidence-relative-path :completed-at})

(def certificate-provenance-keys
  #{:invocation-id :tool-closure-id :raw-transcript-artifact-id
    :m2-transcript-artifact-id :reproduction-command-id
    :evidence-relative-path :completed-at})

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
  #{:artifact :schema-version :node-count :row-count
    :artifact-id :record-root})

(def full-transcript-keys
  #{:artifact :schema-version :checker :inputs :node-count :row-count
    :record-root :records :artifact-id})

(def accepted-output-keys
  #{:status :certificate :raw-source-transcript-artifact
    :m2-transcript-artifact})

(def rejection-output-keys #{:status :rejection-evidence})

(def rejection-keys
  #{:artifact :schema-version :status :role :algorithm-role :reason
    :rejection-id})

(def profile-bounds
  (array-map
   :certificate-maximum-nodes 2048
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
   :offline-transcript-maximum-bytes 268435456))

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
  '[sh07-canonical-text-compare sh07-declared-digest-hash
    sh07-observe-utf8-byte-count
    sh07-b51-admitted-executable-carrier-validation?])

(def internal-builtin-arities
  {'sh07-canonical-text-compare 2
   'sh07-declared-digest-hash 1
   'sh07-observe-utf8-byte-count 3
   'sh07-b51-admitted-executable-carrier-validation? 1})

(def m0-keys
  #{:artifact :schema-version :module-source-id :module-ordinal :module
    :logical-source-path :owner :source-byte-count :source-sha256 :profile
    :target :effects :capabilities :safety :definitions :exports
    :core-binding-count :lexical-binding-count :external-bindings
    :aggregate-binding-count :builtin-calls})

(def m1-keys #{:artifact :schema-version :manifest-id :module-source-ids :edges})

(def m1-edge-keys
  #{:ordinal :caller-module-source-id :caller-function
    :caller-function-source-hash :callsite-path :callee-module-source-id
    :callee-function :callee-function-source-hash :arity :owner-edge
    :authorization})

(def definition-row-keys #{:name :arity :visibility :source-hash})

(def external-binding-keys
  #{:symbol :callee-module-ordinal :callee-module :callee-source-sha256
    :callee-definition-source-hash :arity :owner-edge})

(def builtin-row-keys
  #{:caller :caller-function-source-hash :builtin :arity
    :compiler-internal?})

(def bundle-core-keys
  #{:domain :schema-version :manifest-id :module-plans :routes
    :public-roots})

(def provenance-map-keys
  #{:artifact :manifest-id :module-source-id :source-sha256 :function
    :function-source-hash :callee})

(def source-record-keys
  #{:record-ordinal :module-ordinal :caller :path :callsite-path
    :node-kind :quoted-subtree-stop :possible-call-head
    :call-classification :callee :arity})

(def m2-record-keys
  #{:record-ordinal :module-ordinal :instruction-ordinal :caller :path
    :op :call-classification :callee :arity :external-link
    :provenance-present :provenance})

(defn- exact-keys? [value expected-keys]
  (and (map? value) (= expected-keys (set (keys value)))))

(defn- digest-id? [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- raw-git-id? [value]
  (and (string? value) (boolean (re-matches #"[0-9a-f]{40}" value))))

(defn- canonical-value [value]
  (canonical/reader-canonical-value canonical-value value))

(defn- h [value]
  (canonical/reader-canonical-hash digest/sha256-hex canonical-value value))

(defn- reject! [reason]
  (throw (ex-info "Attempt 30 checker B rejected its input"
                  {:reason reason})))

(defn- ensure! [condition reason]
  (when-not condition (reject! reason)))

(defn- utf8-count [value]
  (alength (.getBytes ^String value java.nio.charset.StandardCharsets/UTF_8)))

(defn value-profile [root]
  (loop [pending [[root 0]]
         nodes 0
         depth 0
         width 0
         collections 0
         scalar-utf8 0]
    (if (empty? pending)
      {:nodes nodes :depth depth :width width
       :collection-count collections :scalar-utf8 scalar-utf8
       :canonical-reader-bytes (utf8-count (pr-str root))}
      (let [[[value level] & more] pending
            collection? (coll? value)
            children (cond
                       (map? value) (mapcat identity value)
                       collection? value
                       :else [])
            item-width (if collection? (count value) 0)
            scalar-size (if collection? 0 (utf8-count (pr-str value)))]
        (recur (into (vec more) (map #(vector % (inc level)) children))
               (inc nodes) (max depth level) (max width item-width)
               (if collection? (inc collections) collections)
               (max scalar-utf8 scalar-size))))))

(defn certificate-profile-within? [profile]
  (and (map? profile)
       (<= (:nodes profile) (:certificate-maximum-nodes profile-bounds))
       (<= (:depth profile) (:certificate-maximum-depth profile-bounds))
       (<= (:width profile) (:certificate-maximum-width profile-bounds))
       (<= (:collection-count profile)
           (:certificate-maximum-collection-count profile-bounds))
       (<= (:scalar-utf8 profile)
           (:certificate-maximum-scalar-utf8 profile-bounds))
       (<= (:canonical-reader-bytes profile)
           (:certificate-maximum-canonical-reader-bytes profile-bounds))))

(defn transcript-counts-within?
  [{:keys [node-count record-count call-row-count byte-count]}]
  (and (every? #(and (integer? %) (<= 0 %))
               [node-count record-count call-row-count byte-count])
       (<= node-count (:offline-transcript-maximum-nodes profile-bounds))
       (<= record-count (:offline-transcript-maximum-records profile-bounds))
       (<= call-row-count
           (:offline-transcript-maximum-call-rows profile-bounds))
       (<= byte-count (:offline-transcript-maximum-bytes profile-bounds))))

(defn- locally-ordered-key [value]
  ;; This checker deliberately does not call the producer's collection sorter.
  ;; The recursive type tags make map/set visitation stable for fixed forms.
  (cond
    (nil? value) [0 ""]
    (false? value) [1 "0"]
    (true? value) [1 "1"]
    (number? value) [2 (pr-str value)]
    (char? value) [3 (pr-str value)]
    (string? value) [4 value]
    (keyword? value) [5 (str value)]
    (symbol? value) [6 (str value)]
    (vector? value) [7 (mapv locally-ordered-key value)]
    (list? value) [8 (mapv locally-ordered-key value)]
    (set? value) [9 (vec (sort (map locally-ordered-key value)))]
    (map? value) [10 (vec (sort (map (fn [[key item]]
                                       [(locally-ordered-key key)
                                        (locally-ordered-key item)])
                                     value)))]
    :else [11 (pr-str value)]))

(defn- compare-path [left right]
  (let [size-order (compare (count left) (count right))]
    (if (zero? size-order)
      (loop [xs left ys right]
        (if (empty? xs)
          0
          (let [coordinate-order (compare (first xs) (first ys))]
            (if (zero? coordinate-order)
              (recur (rest xs) (rest ys))
              coordinate-order))))
      size-order)))

(defn- definition-from-form [form]
  (when (and (seq? form) (= 'defn (first form))
             (symbol? (second form)) (vector? (nth form 2 nil))
             (every? symbol? (nth form 2)) (seq (drop 3 form)))
    (let [[_ name params & body] form]
      {:name name :arity (count params) :params params :body body})))

(defn- definition-id [definition]
  (h (select-keys definition [:name :arity :params :body])))

(defn- module-definitions [forms]
  (mapv definition-from-form
        (filter #(and (seq? %) (= 'defn (first %))) forms)))

(def syntax-heads
  '#{ns def defn fn if do let loop recur quote var set! try throw match})

(defn- source-child-coordinates [value]
  (cond
    (seq? value) (mapv (fn [ordinal child] [[ordinal] child])
                       (range) value)
    (vector? value) (mapv (fn [ordinal child] [[ordinal] child])
                          (range) value)
    (map? value)
    (vec
     (mapcat
      (fn [entry-ordinal [key item]]
        [[[entry-ordinal 0] key] [[entry-ordinal 1] item]])
      (range)
      (sort-by (fn [[key item]]
                 [(locally-ordered-key key) (locally-ordered-key item)])
               value)))
    (set? value)
    (mapv (fn [ordinal item] [[ordinal] item])
          (range) (sort-by locally-ordered-key value))
    :else []))

(defn- source-node-kind [value]
  (cond
    (seq? value) :list
    (vector? value) :vector
    (map? value) :map
    (set? value) :set
    :else :scalar))

(defn- source-call-classification
  [caller head module-ordinal owner-by-function builtin-symbols]
  (cond
    (nil? caller) :outside-definition
    (not (symbol? head)) :not-symbol-head
    (contains? syntax-heads head) :syntax
    (contains? owner-by-function head)
    (if (= module-ordinal (get owner-by-function head)) :local :external)
    (contains? builtin-symbols head) :builtin
    :else :unresolved))

(defn- scan-source
  [source-forms m0s]
  (let [definitions-by-module (mapv module-definitions source-forms)
        names (mapcat #(map :name %) definitions-by-module)
        _ (ensure! (= (count names) (count (set names)))
                   :duplicate-source-definition)
        owner-by-function
        (into {} (for [[module-ordinal definitions]
                       (map-indexed vector definitions-by-module)
                       definition definitions]
                   [(:name definition) module-ordinal]))
        builtin-symbols
        (set (map :builtin (mapcat :builtin-calls m0s)))
        records (transient [])
        calls (transient [])
        record-ordinal (volatile! 0)]
    (doseq [[module-ordinal forms] (map-indexed vector source-forms)
            [form-ordinal form] (map-indexed vector forms)]
      (let [top-definition (definition-from-form form)
            caller (:name top-definition)]
        (letfn [(visit [value path callsite-path]
                  (let [sequence? (seq? value)
                        head (when sequence? (first value))
                        quote-stop? (and sequence? (= 'quote head))
                        classification
                        (if sequence?
                          (source-call-classification
                           caller head module-ordinal owner-by-function
                           builtin-symbols)
                          :not-a-call-head)
                        call? (contains? #{:local :external :builtin}
                                         classification)
                        record
                        {:record-ordinal @record-ordinal
                         :module-ordinal module-ordinal
                         :caller caller
                         :path path
                         :callsite-path callsite-path
                         :node-kind (source-node-kind value)
                         :quoted-subtree-stop quote-stop?
                         :possible-call-head (boolean sequence?)
                         :call-classification classification
                         :callee (when call? head)
                         :arity (when call? (count (rest value)))}]
                    (vswap! record-ordinal inc)
                    (conj! records record)
                    (when call?
                      (conj! calls
                             {:module-ordinal module-ordinal
                              :caller caller :callsite-path callsite-path
                              :kind classification :callee head
                              :arity (count (rest value))}))
                    (when (= :unresolved classification)
                      (reject! :unresolved-source-call-head))
                    (when-not quote-stop?
                      (doseq [[coordinates child]
                              (source-child-coordinates value)]
                        (visit child (into path coordinates)
                               (when callsite-path
                                 (into callsite-path coordinates)))))))]
          (if top-definition
            (do
              ;; Record the defn and its name/parameter children without
              ;; assigning semantic callsite paths.
              (let [children (source-child-coordinates form)]
                (conj! records
                       {:record-ordinal @record-ordinal
                        :module-ordinal module-ordinal :caller caller
                        :path [form-ordinal] :callsite-path nil
                        :node-kind :list :quoted-subtree-stop false
                        :possible-call-head true :call-classification :syntax
                        :callee nil :arity nil})
                (vswap! record-ordinal inc)
                (doseq [[coordinates child] children
                        :let [child-ordinal (first coordinates)]]
                  (if (>= child-ordinal 3)
                    (visit child (into [form-ordinal] coordinates)
                           [(- child-ordinal 3)])
                    (visit child (into [form-ordinal] coordinates) nil)))))
            (visit form [form-ordinal] nil)))))
    {:definitions-by-module definitions-by-module
     :owner-by-function owner-by-function
     :records (persistent! records)
     :calls (persistent! calls)}))

(defn- instruction-children [instruction]
  (let [op (:op instruction)]
    (case op
      :literal (do (ensure! (= #{:op :value} (set (keys instruction)))
                            :malformed-m2-literal) [])
      :quote (do (ensure! (= #{:op :value} (set (keys instruction)))
                          :malformed-m2-quote) [])
      :local (do (ensure! (= #{:op :name} (set (keys instruction)))
                          :malformed-m2-local) [])
      :if (do (ensure! (= #{:op :test :then :else}
                           (set (keys instruction))) :malformed-m2-if)
              [(:test instruction) (:then instruction) (:else instruction)])
      :do (do (ensure! (and (= #{:op :body} (set (keys instruction)))
                            (vector? (:body instruction))) :malformed-m2-do)
              (:body instruction))
      :let (do (ensure! (and (= #{:op :bindings :body}
                                 (set (keys instruction)))
                             (vector? (:bindings instruction))
                             (vector? (:body instruction))
                             (every? #(exact-keys? % #{:name :expr})
                                     (:bindings instruction)))
                        :malformed-m2-let)
               (into (mapv :expr (:bindings instruction)) (:body instruction)))
      :loop (do (ensure! (and (= #{:op :binding-count :bindings :body}
                                  (set (keys instruction)))
                              (integer? (:binding-count instruction))
                              (= (:binding-count instruction)
                                 (count (:bindings instruction)))
                              (vector? (:bindings instruction))
                              (vector? (:body instruction))
                              (every? #(exact-keys? % #{:name :expr})
                                      (:bindings instruction)))
                         :malformed-m2-loop)
                (into (mapv :expr (:bindings instruction)) (:body instruction)))
      :recur
      (do (ensure! (and (= #{:op :args} (set (keys instruction)))
                        (vector? (:args instruction))) :malformed-m2-recur)
          (:args instruction))
      :builtin-call
      (do (ensure!
           (and (vector? (:args instruction))
                (or (= #{:op :function :args} (set (keys instruction)))
                    (= #{:op :function :args :compiler-internal-caller
                         :compiler-internal-provenance}
                       (set (keys instruction))))) :malformed-m2-builtin-call)
          (:args instruction))
      :function-call
      (do (ensure!
           (and (vector? (:args instruction))
                (or (= #{:op :function :args} (set (keys instruction)))
                    (= #{:op :function :args :external-link}
                       (set (keys instruction))))) :malformed-m2-function-call)
          (:args instruction))
      (:vector-literal :set-literal)
      (do (ensure! (and (= #{:op :items} (set (keys instruction)))
                        (vector? (:items instruction)))
                   :malformed-m2-collection)
          (:items instruction))
      :map-literal
      (do (ensure! (and (= #{:op :entries} (set (keys instruction)))
                        (vector? (:entries instruction))
                        (every? #(exact-keys? % #{:key :value})
                                (:entries instruction)))
                   :malformed-m2-map)
          (vec (mapcat (fn [entry] [(:key entry) (:value entry)])
                       (:entries instruction))))
      (reject! :unknown-m2-operation))))

(defn- scan-m2 [m2s]
  (let [function-owner
        (into {} (for [[module-ordinal plan] (map-indexed vector m2s)
                       function (keys (:functions plan))]
                   [function module-ordinal]))
        _ (ensure! (= (reduce + (map #(count (:functions %)) m2s))
                       (count function-owner)) :duplicate-m2-definition)
        records (transient [])
        calls (transient [])
        global-record (volatile! 0)]
    (doseq [[module-ordinal plan] (map-indexed vector m2s)]
      (let [instruction-ordinal (volatile! 0)
            ordered-functions (sort-by str (keys (:functions plan)))]
        (doseq [[function-coordinate caller]
                (map-indexed vector ordered-functions)]
          (let [definition (get-in plan [:functions caller])]
            (ensure! (and (map? definition) (vector? (:instructions definition)))
                     :malformed-m2-definition)
            (letfn [(visit [instruction path]
                      (ensure! (and (map? instruction) (keyword? (:op instruction)))
                               :malformed-m2-instruction)
                      (let [op (:op instruction)
                            callee (when (contains? #{:function-call
                                                     :builtin-call} op)
                                     (:function instruction))
                            kind (case op
                                   :function-call
                                   (if (= module-ordinal
                                          (get function-owner callee))
                                     :local :external)
                                   :builtin-call
                                   (if (contains? (set internal-builtins) callee)
                                     :internal-builtin :builtin)
                                   :not-a-call)
                            provenance? (contains? instruction
                                                   :compiler-internal-provenance)
                            link (get instruction :external-link)
                            own-ordinal @instruction-ordinal]
                        (ensure! (or (not= :function-call op)
                                     (contains? function-owner callee))
                                 :unresolved-m2-function)
                        (ensure! (or (not= :external kind) (map? link))
                                 :missing-m2-external-link)
                        (ensure! (or (not= :local kind) (nil? link))
                                 :unexpected-m2-external-link)
                        (ensure! (= provenance? (= :internal-builtin kind))
                                 :m2-provenance-presence-mismatch)
                        (vswap! instruction-ordinal inc)
                        (conj! records
                               {:record-ordinal @global-record
                                :module-ordinal module-ordinal
                                :instruction-ordinal own-ordinal
                                :caller caller :path path :op op
                                :call-classification kind :callee callee
                                :arity (when callee (count (:args instruction)))
                                :external-link link
                                :provenance-present provenance?
                                :provenance
                                (get instruction :compiler-internal-provenance)})
                        (vswap! global-record inc)
                        (when (contains? #{:local :external :builtin
                                          :internal-builtin} kind)
                          (conj! calls
                                 {:module-ordinal module-ordinal
                                  :instruction-ordinal own-ordinal
                                  :caller caller :kind kind :callee callee
                                  :arity (count (:args instruction))
                                  :external-link link
                                  :provenance
                                  (get instruction
                                       :compiler-internal-provenance)}))
                        (doseq [[child-ordinal child]
                                (map-indexed vector
                                             (instruction-children instruction))]
                          (visit child (conj path child-ordinal)))))]
              (doseq [[top-ordinal instruction]
                      (map-indexed vector (:instructions definition))]
                (visit instruction [function-coordinate top-ordinal])))))))
    {:owner-by-function function-owner
     :records (persistent! records)
     :calls (persistent! calls)}))

(defn- m2-identity-value [value]
  ;; M2 predates Attempt 30 and retains its existing C4-compatible identity.
  ;; Reconstruct that value locally; do not call the producer/compiler
  ;; normalizer whose independence this checker exists to test.
  (cond
    (map? value)
    (into (sorted-map-by #(compare (pr-str %1) (pr-str %2)))
          (map (fn [[key item]] [(m2-identity-value key)
                                 (m2-identity-value item)])) value)
    (set? value) (vec (sort-by pr-str (map m2-identity-value value)))
    (vector? value) (mapv m2-identity-value value)
    (seq? value) (mapv m2-identity-value value)
    :else value))

(defn m2-plan-id [plan]
  ;; M2 plan IDs were created by C4 over an already canonicalized value.  Do
  ;; not pass that value through the reader identity domain a second time.
  (str "sha256:"
       (digest/sha256-hex
        (pr-str
         (m2-identity-value
          (-> plan
              (dissoc :plan-id)
              (update :source #(if (map? %) (dissoc % :path) %))
              (update :module #(if (map? %)
                                 (dissoc % :source-path)
                                 %))))))))

(defn- check-and-derive-inputs [envelope]
  (ensure! (exact-keys? envelope input-keys) :input-schema)
  (let [{:keys [m0s m1 m2s bundle-core source-forms checker provenance]}
        envelope]
    (ensure! (and (vector? m0s) (= 7 (count m0s))
                  (vector? m2s) (= 7 (count m2s))
                  (vector? source-forms) (= 7 (count source-forms))
                  (every? vector? source-forms)) :seven-module-cardinality)
    (ensure! (and (exact-keys? checker checker-keys)
                  (= :m2-first-independent-exhaustive
                     (:algorithm-role checker))
                  (every? digest-id?
                          ((juxt :artifact-id :source-sha256 :semantic-id
                                 :executable-id :dependency-closure-id)
                           checker))
                  (= 5 (count (set ((juxt :artifact-id :source-sha256
                                           :semantic-id :executable-id
                                           :dependency-closure-id)
                                    checker))))) :checker-identity)
    (ensure! (exact-keys? provenance provenance-input-keys)
             :provenance-input-schema)
    (ensure! (and (digest-id? (:architecture-report-sha256 envelope))
                  (raw-git-id? (:snapshot-commit envelope))
                  (raw-git-id? (:snapshot-tree envelope))
                  (digest-id? (:predecessor-executable-descriptor-id envelope)))
             :snapshot-identity)
    (ensure! (every? #(exact-keys? % m0-keys) m0s) :m0-schema)
    (ensure! (= (vec (range 7)) (mapv :module-ordinal m0s))
             :m0-module-order)
    (ensure! (every? #(and (= :gravity/sh07-b51-module-source-v1
                               (:artifact %))
                            (= 1 (:schema-version %))
                            (= (:module-source-id %)
                               (h (dissoc % :module-source-id)))) m0s)
             :m0-own-id)
    (ensure!
     (every?
      (fn [m0]
        (and (vector? (:definitions m0))
             (every? #(and (exact-keys? % definition-row-keys)
                           (symbol? (:name %))
                           (integer? (:arity %)) (<= 0 (:arity %))
                           (contains? #{:public :private} (:visibility %))
                           (digest-id? (:source-hash %))) (:definitions m0))
             (vector? (:external-bindings m0))
             (every? #(exact-keys? % external-binding-keys)
                     (:external-bindings m0))
             (vector? (:builtin-calls m0))
             (every? #(and (exact-keys? % builtin-row-keys)
                           (symbol? (:builtin %))
                           (integer? (:arity %)) (<= 0 (:arity %))
                           (boolean? (:compiler-internal? %))
                           (= (contains? internal-builtin-arities (:builtin %))
                              (:compiler-internal? %))
                           (or (not (:compiler-internal? %))
                               (= (get internal-builtin-arities (:builtin %))
                                  (:arity %))))
                     (:builtin-calls m0))
             (= (:aggregate-binding-count m0)
                (+ (:core-binding-count m0) (count (:definitions m0))
                   (:lexical-binding-count m0)
                   (count (:external-bindings m0)))))) m0s)
     :m0-row-schema)
    (ensure! (and (exact-keys? m1 m1-keys)
                  (= :gravity/sh07-b51-link-manifest-v1 (:artifact m1))
                  (= 1 (:schema-version m1))
                  (= (:manifest-id m1) (h (dissoc m1 :manifest-id))))
             :m1-own-id)
    (ensure! (and (vector? (:edges m1))
                  (every? #(exact-keys? % m1-edge-keys) (:edges m1))
                  (= (vec (range (count (:edges m1))))
                     (mapv :ordinal (:edges m1)))) :m1-edges)
    (ensure! (= (mapv :module-source-id m0s) (:module-source-ids m1))
             :m0-m1-join)
    (ensure! (every? #(and (map? %) (map? (:functions %))
                           (digest-id? (:plan-id %))
                           (= (:plan-id %) (m2-plan-id %))) m2s)
             :m2-own-id)
    (ensure! (every? true?
                     (map (fn [m0 m2]
                            (and (= (:module-source-id m0)
                                    (:sh07-b51-module-source-id m2))
                                 (= (:manifest-id m1)
                                    (:sh07-b51-link-manifest-id m2))
                                 (= (:module m0) (get-in m2 [:module :module]))
                                 (= (:source-sha256 m0)
                                    (get-in m2 [:source :sha256]))))
                          m0s m2s)) :m0-m1-m2-join)
    (ensure! (and (exact-keys? bundle-core bundle-core-keys)
                  (= :gravity/sh07-b51-execution-bundle-core-id-v1
                     (:domain bundle-core))
                  (= 1 (:schema-version bundle-core))
                  (= (:manifest-id m1) (:manifest-id bundle-core))
                  (= 7 (count (:module-plans bundle-core)))
                  (= 8 (count (:public-roots bundle-core))))
             :bundle-core-schema)
    (let [derived-source-identities (mapv :source-sha256 m0s)
          derived-source-bytes (mapv :source-byte-count m0s)
          derived-m0-ids (mapv :module-source-id m0s)
          derived-m2-ids (mapv :plan-id m2s)
          compiler-source-ids
          (set (map #(get-in % [:compiler :source-content-hash]) m2s))
          compiler-semantic-ids
          (set (map #(get-in % [:compiler :semantic-hash]) m2s))]
      (ensure! (= derived-source-identities
                  (:module-source-identities envelope))
               :source-identity-join)
      (ensure! (= derived-source-bytes (:module-source-byte-counts envelope))
               :source-byte-count-join)
      (ensure! (= #{(:emitter-source-id envelope)} compiler-source-ids)
               :emitter-source-join)
      (ensure! (= #{(:emitter-semantic-id envelope)} compiler-semantic-ids)
               :emitter-semantic-join)
      (ensure! (every? digest-id? derived-source-identities)
               :source-sha256-shape)
      (ensure! (every? #(and (integer? %) (<= 0 %)) derived-source-bytes)
               :source-byte-count-shape)
      (ensure! (= (mapv (fn [m0 m2]
                           {:module-ordinal (:module-ordinal m0)
                            :module-source-id (:module-source-id m0)
                            :plan-id (:plan-id m2)}) m0s m2s)
                  (:module-plans bundle-core)) :bundle-module-plan-join)
      (let [m0-by-id (into {} (map (juxt :module-source-id identity)) m0s)
            m2-by-id (into {} (map (juxt :plan-id identity)) m2s)
            edge-by-ordinal (into {} (map (juxt :ordinal identity))
                                  (:edges m1))]
        (ensure!
         (and (vector? (:routes bundle-core))
              (= (count (:edges m1)) (count (:routes bundle-core)))
              (= (vec (range (count (:routes bundle-core))))
                 (mapv :edge-ordinal (:routes bundle-core)))
              (every?
               (fn [route]
                 (let [edge (get edge-by-ordinal (:edge-ordinal route))
                       caller-plan (get m2-by-id (:caller-plan-id route))
                       callee-plan (get m2-by-id (:callee-plan-id route))]
                   (and (= #{:edge-ordinal :caller-plan-id :callee-plan-id}
                           (set (keys route))) edge caller-plan callee-plan
                        (= (:caller-module-source-id edge)
                           (:sh07-b51-module-source-id caller-plan))
                        (= (:callee-module-source-id edge)
                           (:sh07-b51-module-source-id callee-plan)))))
               (:routes bundle-core))) :bundle-route-join)
        (ensure!
         (and (vector? (:public-roots bundle-core))
              (= (vec (range 1 9))
                 (mapv :root-ordinal (:public-roots bundle-core)))
              (every?
               (fn [root]
                 (let [plan (get m2-by-id (:plan-id root))
                       m0 (when plan (get m0-by-id
                                          (:sh07-b51-module-source-id plan)))
                       definition (when plan
                                    (get-in plan [:functions (:function root)]))]
                   (and (= #{:root-ordinal :function :arity :plan-id}
                           (set (keys root))) plan m0 definition
                        (= (:arity root) (:arity definition))
                        (contains? (set (:exports m0)) (:function root)))))
               (:public-roots bundle-core))) :bundle-root-join))
      {:architecture-report-sha256 (:architecture-report-sha256 envelope)
       :snapshot-commit (:snapshot-commit envelope)
       :snapshot-tree (:snapshot-tree envelope)
       :predecessor-executable-descriptor-id
       (:predecessor-executable-descriptor-id envelope)
       :module-source-identities derived-source-identities
       :module-source-byte-counts derived-source-bytes
       :emitter-source-id (:emitter-source-id envelope)
       :emitter-semantic-id (:emitter-semantic-id envelope)
       :m0-ids derived-m0-ids
       :m1-id (:manifest-id m1)
       :m2-ids derived-m2-ids
       :bundle-core-id (h bundle-core)})))

(defn- verify-source-m2-call-parity! [source-scan m2-scan]
  (let [project-source
        (fn [call]
          [(:module-ordinal call) (:caller call) (:kind call)
           (:callee call) (:arity call)])
        project-m2
        (fn [call]
          [(:module-ordinal call) (:caller call)
           (if (= :internal-builtin (:kind call)) :builtin (:kind call))
           (:callee call) (:arity call)])]
    (ensure! (= (frequencies (map project-source (:calls source-scan)))
                (frequencies (map project-m2 (:calls m2-scan))))
             :source-m2-call-parity)))

(defn- verify-definitions!
  [source-scan m0s m2s]
  (doseq [module-ordinal (range 7)]
    (let [source-defs (nth (:definitions-by-module source-scan) module-ordinal)
          m0 (nth m0s module-ordinal)
          m2 (nth m2s module-ordinal)
          expected
          (mapv (fn [definition]
                  {:name (:name definition) :arity (:arity definition)
                   :visibility (if (contains? (set (:exports m0))
                                              (:name definition))
                                 :public :private)
                   :source-hash (definition-id definition)}) source-defs)]
      (ensure! (= expected (:definitions m0)) :source-m0-definition-join)
      (ensure! (= (set (map :name source-defs))
                  (set (keys (:functions m2)))) :source-m2-definition-set)
      (doseq [definition source-defs]
        (let [compiled (get-in m2 [:functions (:name definition)])]
          (ensure! (= (select-keys definition [:name :arity :params :body])
                      (select-keys compiled [:name :arity :params :body]))
                   :source-m2-definition-join))))))

(defn- normalized-external-source-rows
  [source-scan m0s]
  (let [definition-by-name
        (into {} (for [m0 m0s, definition (:definitions m0)]
                   [(:name definition) [m0 definition]]))
        rows
        (for [call (:calls source-scan) :when (= :external (:kind call))]
          (let [[caller-m0 caller-definition]
                (get definition-by-name (:caller call))
                [callee-m0 callee-definition]
                (get definition-by-name (:callee call))
                binding
                (first (filter #(= (:callee call) (:symbol %))
                               (:external-bindings caller-m0)))]
            (ensure! (and caller-m0 callee-m0 binding)
                     :source-external-binding-missing)
            (let [expected-owner-edge
                  (cond
                    (= (:owner caller-m0) (:owner callee-m0))
                    (case (:owner caller-m0)
                      :r1-producer :producer-internal
                      :r6-independent-verifier :root6-internal
                      nil)
                    (and (= :r8-finalizer (:owner caller-m0))
                         (= :r1-producer (:owner callee-m0)))
                    :root8-to-producer-interface
                    :else nil)]
              (ensure! (and expected-owner-edge
                            (= expected-owner-edge (:owner-edge binding))
                            (= (:module-ordinal callee-m0)
                             (:callee-module-ordinal binding))
                          (= (:module callee-m0) (:callee-module binding))
                          (= (:source-sha256 callee-m0)
                             (:callee-source-sha256 binding))
                          (= (:source-hash callee-definition)
                             (:callee-definition-source-hash binding))
                          (= (:arity call) (:arity binding)
                             (:arity callee-definition)))
                       :source-external-binding-mismatch))
            {:caller-module-source-id (:module-source-id caller-m0)
             :caller-function (:caller call)
             :caller-function-source-hash (:source-hash caller-definition)
             :callsite-path (:callsite-path call)
             :callee-module-source-id (:module-source-id callee-m0)
             :callee-function (:callee call)
             :callee-function-source-hash (:source-hash callee-definition)
             :arity (:arity call) :owner-edge (:owner-edge binding)
             :authorization :authenticated-b51-namespace-external}))
        ordered
        (vec
         (sort (fn [left right]
                 (let [module-order
                       (compare (:module-ordinal
                                 (first (get definition-by-name
                                             (:caller-function left))))
                                (:module-ordinal
                                 (first (get definition-by-name
                                             (:caller-function right)))))
                       caller-order (compare (str (:caller-function left))
                                             (str (:caller-function right)))]
                   (cond
                     (not (zero? module-order)) module-order
                     (not (zero? caller-order)) caller-order
                     :else (compare-path (:callsite-path left)
                                         (:callsite-path right)))))
               rows))]
    (doseq [m0 m0s]
      (let [module-id (:module-source-id m0)
            called (mapv :callee-function
                         (filter #(= module-id (:caller-module-source-id %))
                                 ordered))
            bound (mapv :symbol (:external-bindings m0))]
        (ensure! (= (set called) (set bound)) :external-binding-coverage)
        (ensure! (= (count bound) (count (set bound)))
                 :duplicate-external-binding)))
    (mapv #(assoc %1 :ordinal %2) ordered (range))))

(defn- verify-external-bijection!
  [source-scan m0s m1 m2-scan]
  (let [source-rows (normalized-external-source-rows source-scan m0s)
        m1-rows (:edges m1)
        m2-external (filterv #(= :external (:kind %)) (:calls m2-scan))
        m1-by-ordinal (into {} (map (juxt :ordinal identity) m1-rows))]
    (ensure! (= source-rows m1-rows) :source-m1-external-bijection)
    (ensure! (= (count m1-rows) (count m2-external))
             :m1-m2-external-count)
    (doseq [call m2-external]
      (let [link (:external-link call)
            edge (get m1-by-ordinal (:edge-ordinal link))]
        (ensure! (and (= #{:artifact :manifest-id :edge-ordinal}
                          (set (keys link)))
                      (= :gravity/sh07-b51-external-call-v1 (:artifact link))
                      (= (:manifest-id m1) (:manifest-id link))
                      (= (:caller call) (:caller-function edge))
                      (= (:callee call) (:callee-function edge))
                      (= (:arity call) (:arity edge)))
                 :m1-m2-external-link)))
    (ensure! (= (count m2-external)
                (count (set (map #(get-in % [:external-link :edge-ordinal])
                                 m2-external))))
             :duplicate-m2-external-edge)
    {:source-rows source-rows :m1-rows m1-rows}))

(defn- scalar-sort-key [row]
  (mapv (comp locally-ordered-key row)
        [:caller :caller-function-source-hash :builtin :arity
         :compiler-internal?]))

(defn- add-duplicate-ordinals [occurrences]
  (loop [remaining (sort-by scalar-sort-key occurrences)
         previous nil duplicate-ordinal 0
         result []]
    (if (empty? remaining)
      result
      (let [row (first remaining)
            coordinates (scalar-sort-key row)
            duplicate (if (= previous coordinates) (inc duplicate-ordinal) 0)]
        (recur (rest remaining) coordinates duplicate
               (conj result (assoc row :duplicate-ordinal duplicate)))))))

(defn- normalized-m2-builtin-occurrences [m2-scan m0s module-ordinal]
  (let [definition-hash
        (into {} (map (juxt :name :source-hash)
                      (:definitions (nth m0s module-ordinal))))]
    (add-duplicate-ordinals
     (mapv (fn [call]
             {:caller (:caller call)
              :caller-function-source-hash
              (get definition-hash (:caller call))
              :builtin (:callee call) :arity (:arity call)
              :compiler-internal? (= :internal-builtin (:kind call))})
           (filter #(and (= module-ordinal (:module-ordinal %))
                         (contains? #{:builtin :internal-builtin} (:kind %)))
                   (:calls m2-scan))))))

(defn- normalized-m0-builtin-occurrences [m0]
  (add-duplicate-ordinals (mapv identity (:builtin-calls m0))))

(defn- verify-builtin-parity!
  [m2-scan m0s inputs]
  (mapv
   (fn [module-ordinal]
     (let [m0 (nth m0s module-ordinal)
           m2-occurrences
           (normalized-m2-builtin-occurrences m2-scan m0s module-ordinal)
           m0-occurrences (normalized-m0-builtin-occurrences m0)]
       (ensure! (= m0-occurrences m2-occurrences) :m0-m2-builtin-parity)
       (let [root
             (h {:domain :gravity/sh07-b51-ordered-builtin-multiset-root-v1
                 :schema-version 1 :module-ordinal module-ordinal
                 :occurrences m2-occurrences})]
         (h {:domain :gravity/sh07-b51-builtin-parity-commitment-v1
             :schema-version 1 :module-ordinal module-ordinal
             :m0-id (nth (:m0-ids inputs) module-ordinal)
             :m2-id (nth (:m2-ids inputs) module-ordinal)
             :occurrence-count (count m2-occurrences)
             :ordered-multiset-root root :parity-complete true}))))
   (range 7)))

(defn- provenance-commitment!
  [m2-scan m0s m1 inputs]
  (let [internal-set (set internal-builtins)
        occurrences
        (mapv
         (fn [call]
           (let [module-ordinal (:module-ordinal call)
                 m0 (nth m0s module-ordinal)
                 definition
                 (first (filter #(= (:caller call) (:name %))
                                (:definitions m0)))
                 provenance (:provenance call)]
             (ensure! (and (= provenance-map-keys (set (keys provenance)))
                           (= :gravity/sh07-compiler-internal-call-provenance-v2
                              (:artifact provenance))
                           (= (:manifest-id m1) (:manifest-id provenance))
                           (= (:module-source-id m0)
                              (:module-source-id provenance))
                           (= (:source-sha256 m0) (:source-sha256 provenance))
                           (= (:caller call) (:function provenance))
                           (= (:source-hash definition)
                              (:function-source-hash provenance))
                           (= (:callee call) (:callee provenance)))
                      :compiler-internal-provenance)
             {:module-ordinal module-ordinal
              :instruction-ordinal (:instruction-ordinal call)
              :provenance provenance}))
         (filterv #(= :internal-builtin (:kind %)) (:calls m2-scan)))
        observed (set (map #(get-in % [:provenance :callee]) occurrences))]
    (ensure! (= internal-set observed) :internal-builtin-set)
    (ensure! (= occurrences
                (vec (sort-by (juxt :module-ordinal :instruction-ordinal)
                              occurrences))) :provenance-order)
    (let [root
          (h {:domain :gravity/sh07-b51-provenance-occurrence-root-v1
              :schema-version 1 :occurrences occurrences})]
      (h {:domain :gravity/sh07-b51-provenance-completeness-commitment-v1
          :schema-version 1 :m0-ids (:m0-ids inputs) :m1-id (:m1-id inputs)
          :m2-ids (:m2-ids inputs) :internal-builtins internal-builtins
          :occurrence-count (count occurrences) :occurrence-root root
          :provenance-complete true}))))

(defn- reachable-functions! [bundle-core m2-scan]
  (let [known (set (keys (:owner-by-function m2-scan)))
        starts (mapv :function (:public-roots bundle-core))
        by-caller (group-by :caller
                            (filter #(contains? #{:local :external} (:kind %))
                                    (:calls m2-scan)))]
    (ensure! (every? known starts) :unknown-public-root)
    (loop [queue (into clojure.lang.PersistentQueue/EMPTY starts)
           reached #{}]
      (if (empty? queue)
        reached
        (let [function (peek queue)
              remaining (pop queue)]
          (if (contains? reached function)
            (recur remaining reached)
            (let [callees (map :callee (get by-caller function []))]
              (ensure! (every? known callees) :unresolved-reachable-call)
              (recur (into remaining callees) (conj reached function)))))))))

(defn- qualified-coordinate [m0s m2s module-ordinal function]
  {:module-ordinal module-ordinal
   :module-source-id (:module-source-id (nth m0s module-ordinal))
   :plan-id (:plan-id (nth m2s module-ordinal))
   :function function})

(defn- category-rows
  [m0s m1 m2s bundle-core m2-scan reachable]
  (let [owner (:owner-by-function m2-scan)
        coordinate #(qualified-coordinate m0s m2s %1 %2)
        members (sort-by (fn [function] [(get owner function) (str function)])
                         reachable)
        member-rows
        (mapv (fn [ordinal function]
                {:member-ordinal ordinal
                 :coordinate (coordinate (get owner function) function)})
              (range) members)
        definition-hash
        (into {} (for [m0 m0s, definition (:definitions m0)]
                   [[(:module-ordinal m0) (:name definition)]
                    (:source-hash definition)]))
        function-rows
        (mapv (fn [ordinal function]
                (let [module-ordinal (get owner function)]
                  {:member-ordinal ordinal
                   :coordinate (coordinate module-ordinal function)
                   :definition-source-hash
                   (get definition-hash [module-ordinal function])}))
              (range) members)
        reachable-calls (filterv #(contains? reachable (:caller %))
                                 (:calls m2-scan))
        local-calls (filterv #(= :local (:kind %)) reachable-calls)
        local-rows
        (mapv (fn [ordinal call]
                {:edge-ordinal ordinal
                 :caller (coordinate (:module-ordinal call) (:caller call))
                 :callee (coordinate (get owner (:callee call)) (:callee call))
                 :arity (:arity call) :source-callsite-ordinal ordinal})
              (range) local-calls)
        reachable-external
        (filterv #(contains? reachable (:caller-function %)) (:edges m1))
        routes (into {} (map (juxt :edge-ordinal identity)
                             (:routes bundle-core)))
        m0-by-id (into {} (map (juxt :module-source-id identity)) m0s)
        external-rows
        (mapv
         (fn [ordinal edge]
           (let [caller (get m0-by-id (:caller-module-source-id edge))
                 callee (get m0-by-id (:callee-module-source-id edge))]
             {:semantic-edge-ordinal ordinal
              :m1-edge-ordinal (:ordinal edge) :manifest-id (:manifest-id m1)
              :caller (coordinate (:module-ordinal caller)
                                  (:caller-function edge))
              :callee (coordinate (:module-ordinal callee)
                                  (:callee-function edge))
              :arity (:arity edge) :owner-edge (:owner-edge edge)
              :route (get routes (:ordinal edge))}))
         (range) reachable-external)
        builtin-calls
        (filterv #(contains? #{:builtin :internal-builtin} (:kind %))
                 reachable-calls)
        builtin-rows
        (mapv (fn [ordinal call]
                {:occurrence-ordinal ordinal
                 :caller (coordinate (:module-ordinal call) (:caller call))
                 :builtin (:callee call) :arity (:arity call)
                 :compiler-internal? (= :internal-builtin (:kind call))
                 :source-callsite-ordinal ordinal})
              (range) builtin-calls)
        root-rows
        (mapv (fn [root]
                (let [module-ordinal (get owner (:function root))]
                  {:root-ordinal (:root-ordinal root)
                   :coordinate (coordinate module-ordinal (:function root))
                   :arity (:arity root)})) (:public-roots bundle-core))]
    (array-map
     :roots root-rows :members member-rows :functions function-rows
     :local-runtime-call-edges local-rows
     :external-runtime-call-edges external-rows
     :referenced-builtins builtin-rows
     :module-contracts
     (mapv (fn [m0] {:module-ordinal (:module-ordinal m0)
                     :module-source-id (:module-source-id m0)}) m0s))))

(defn- class-profile-within? [value prefix]
  (let [profile (value-profile value)
        key-for (fn [suffix] (keyword (str "maximum-" prefix "-" suffix)))]
    (and (<= (:nodes profile) (get category-schedule (key-for "nodes")))
         (<= (:depth profile) (get category-schedule (key-for "depth")))
         (<= (:width profile) (get category-schedule (key-for "width")))
         (<= (:scalar-utf8 profile)
             (get category-schedule (key-for "scalar-utf8")))
         (<= (utf8-count (pr-str value))
             (get category-schedule (key-for "c11-bytes")))
         (<= (:canonical-reader-bytes profile)
             (get category-schedule (key-for "canonical-reader-bytes"))))))

(defn- category-commitment [category rows]
  (doseq [row rows]
    (ensure! (class-profile-within? row "row") :category-row-bound))
  (let [leaf-groups (if (empty? rows) [[]] (partition-all 16 rows))
        leaves
        (mapv (fn [ordinal group]
                {:domain :gravity/sh07-b51-linked-closure-leaf-v2
                 :schema-version 2 :category category :shard-ordinal ordinal
                 :first-row-ordinal (* ordinal 16) :row-count (count group)
                 :category-row-count (count rows) :rows (vec group)})
              (range) leaf-groups)]
    (doseq [leaf leaves]
      (ensure! (class-profile-within? leaf "leaf") :category-leaf-bound))
    (loop [level 1
           children
           (mapv (fn [ordinal leaf]
                   {:child-ordinal ordinal
                    :first-row-ordinal (:first-row-ordinal leaf)
                    :row-count (:row-count leaf) :digest (h leaf)})
                 (range) leaves)]
      (ensure! (<= level 3) :category-fold-level-bound)
      (let [groups (partition-all 16 children)
            folds
            (mapv
             (fn [group-ordinal group]
               {:domain :gravity/sh07-b51-linked-closure-fold-v2
                :schema-version 2 :category category :level level
                :group-ordinal group-ordinal
                :first-child-ordinal (* group-ordinal 16)
                :child-count (count group)
                :row-count (reduce + (map :row-count group))
                :children (vec group)})
             (range) groups)]
        (doseq [fold folds]
          (ensure! (class-profile-within? fold "fold") :category-fold-bound))
        (if (= 1 (count folds))
          (let [commitment
                {:category category :row-count (count rows)
                 :leaf-count (count leaves) :fold-level-count level
                 :root-digest (h (first folds))}]
            (ensure! (class-profile-within? commitment "commitment")
                     :category-commitment-bound)
            commitment)
          (recur
           (inc level)
           (mapv (fn [ordinal fold]
                   {:child-ordinal ordinal
                    :first-row-ordinal
                    (:first-row-ordinal (first (:children fold)))
                    :row-count (:row-count fold) :digest (h fold)})
                 (range) folds)))))))

(defn- build-category-commitments [rows]
  (mapv #(category-commitment % (get rows %)) categories))

(def physical-census-keys
  #{:module-count :module-censuses :physical-definition-count
    :reachable-definition-count :physical-local-runtime-callsite-count
    :reachable-local-runtime-callsite-count
    :physical-external-runtime-callsite-count
    :reachable-external-runtime-callsite-count
    :physical-referenced-builtin-occurrence-count
    :reachable-referenced-builtin-occurrence-count :semantic-row-count
    :module-binding-occupancies :maximum-binding-occupancy :binding-ceiling})

(defn- physical-census
  [m0s m2s m2-scan reachable rows]
  (let [calls (:calls m2-scan)
        owner (:owner-by-function m2-scan)
        module-censuses
        (mapv
         (fn [module-ordinal]
           (let [m0 (nth m0s module-ordinal)
                 plan (nth m2s module-ordinal)
                 module-calls (filter #(= module-ordinal (:module-ordinal %))
                                      calls)
                 reached-calls (filter #(contains? reachable (:caller %))
                                       module-calls)
                 count-kind (fn [kind xs] (count (filter #(= kind (:kind %)) xs)))
                 builtin-count (fn [xs] (count (filter
                                                #(contains? #{:builtin
                                                              :internal-builtin}
                                                            (:kind %)) xs)))]
             {:module-ordinal module-ordinal :module (:module m0)
              :logical-source-path (:logical-source-path m0) :owner (:owner m0)
              :source-byte-count (:source-byte-count m0)
              :source-sha256 (:source-sha256 m0)
              :module-source-id (:module-source-id m0) :plan-id (:plan-id plan)
              :physical-definition-count (count (:definitions m0))
              :reachable-definition-count
              (count (filter #(and (= module-ordinal (get owner %))
                                   (contains? reachable %)) (keys owner)))
              :physical-local-runtime-callsite-count
              (count-kind :local module-calls)
              :reachable-local-runtime-callsite-count
              (count-kind :local reached-calls)
              :physical-external-runtime-callsite-count
              (count-kind :external module-calls)
              :reachable-external-runtime-callsite-count
              (count-kind :external reached-calls)
              :physical-referenced-builtin-occurrence-count
              (builtin-count module-calls)
              :reachable-referenced-builtin-occurrence-count
              (builtin-count reached-calls)
              :core-binding-count (:core-binding-count m0)
              :lexical-binding-count (:lexical-binding-count m0)
              :external-binding-count (count (:external-bindings m0))
              :aggregate-binding-count (:aggregate-binding-count m0)}))
         (range 7))
        total (fn [key] (reduce + (map key module-censuses)))
        occupancies (mapv :aggregate-binding-count module-censuses)]
    (ensure! (every? #(<= % 2440) occupancies) :binding-ceiling)
    {:module-count 7 :module-censuses module-censuses
     :physical-definition-count (total :physical-definition-count)
     :reachable-definition-count (total :reachable-definition-count)
     :physical-local-runtime-callsite-count
     (total :physical-local-runtime-callsite-count)
     :reachable-local-runtime-callsite-count
     (total :reachable-local-runtime-callsite-count)
     :physical-external-runtime-callsite-count
     (total :physical-external-runtime-callsite-count)
     :reachable-external-runtime-callsite-count
     (total :reachable-external-runtime-callsite-count)
     :physical-referenced-builtin-occurrence-count
     (total :physical-referenced-builtin-occurrence-count)
     :reachable-referenced-builtin-occurrence-count
     (total :reachable-referenced-builtin-occurrence-count)
     :semantic-row-count (reduce + (map count (vals rows)))
     :module-binding-occupancies occupancies
     :maximum-binding-occupancy (apply max occupancies)
     :binding-ceiling 2440}))

(defn- transcript-artifact
  [artifact record-domain checker inputs records row-count]
  (let [record-root
        (h {:domain record-domain :schema-version 1
            :checker-algorithm-role (:algorithm-role checker)
            :records records})
        base {:artifact artifact :schema-version 1 :checker checker
              :inputs inputs :node-count (count records) :row-count row-count
              :record-root record-root}
        artifact-id
        (h {:domain :gravity/sh07-b51-census-transcript-artifact-id-v1
            :schema-version 1 :artifact artifact :checker checker
            :inputs inputs :node-count (count records) :row-count row-count
            :record-root record-root})]
    (assoc base :records records :artifact-id artifact-id)))

(defn transcript-descriptor [artifact]
  (select-keys artifact [:artifact :schema-version :node-count :row-count
                         :artifact-id :record-root]))

(defn- transcript-within! [artifact]
  (ensure!
   (transcript-counts-within?
    {:node-count (:node-count artifact)
     :record-count (count (:records artifact))
     :call-row-count (:row-count artifact)
     :byte-count (utf8-count (pr-str artifact))})
   :offline-transcript-bound)
  artifact)

(defn- assumptions []
  {:sha256-collision-resistance :assumed
   :canonical-reader-hash-id
   :gravity/sh07-declared-digest-hash-reader-canonical-hash-v1
   :source-emitter-bytes-immutable true
   :checker-closures-independent true
   :host-pin-boundary-id
   (h {:domain :gravity/sh07-b51-host-pin-boundary-id-v1
       :schema-version 1 :component :p15-s23-sh07-b51-plan-linker
       :pin-key-names
       [:expected-m3-id :expected-certificate-a-id
        :expected-certificate-b-id :expected-semantic-root]
       :caller-supplied false})
   :runtime-token-boundary-id
   (h {:domain :gravity/sh07-b51-runtime-token-boundary-id-v1
       :schema-version 1 :component :p15-s23-sh07-b51-runtime-context
       :token-kind :private-object-identity :caller-supplied false})})

(defn- accepted-products [envelope]
  ;; Deliberately M2-first: no source form is traversed until the complete M2
  ;; instruction space has been scanned under the closed rules above.
  (let [inputs (check-and-derive-inputs envelope)
        m0s (:m0s envelope) m1 (:m1 envelope) m2s (:m2s envelope)
        bundle-core (:bundle-core envelope) checker (:checker envelope)
        m2-scan (scan-m2 m2s)
        source-scan (scan-source (:source-forms envelope) m0s)
        _ (ensure! (= (:owner-by-function m2-scan)
                      (:owner-by-function source-scan))
                   :source-m2-owner-map)
        _ (verify-source-m2-call-parity! source-scan m2-scan)
        _ (verify-definitions! source-scan m0s m2s)
        external (verify-external-bijection! source-scan m0s m1 m2-scan)
        builtin-commitments (verify-builtin-parity! m2-scan m0s inputs)
        provenance-commitment
        (provenance-commitment! m2-scan m0s m1 inputs)
        reachable (reachable-functions! bundle-core m2-scan)
        rows (category-rows m0s m1 m2s bundle-core m2-scan reachable)
        commitments (build-category-commitments rows)
        physical (physical-census m0s m2s m2-scan reachable rows)
        normalized-id
        (h {:domain :gravity/sh07-b51-normalized-census-id-v1
            :schema-version 1 :physical-census physical
            :ordered-category-row-roots (mapv :root-digest commitments)
            :category-row-counts (mapv :row-count commitments)})
        external-commitment
        (h {:domain
            :gravity/sh07-b51-external-call-bijection-commitment-v1
            :schema-version 1 :m1-id (:m1-id inputs)
            :physical-external-count (count (:source-rows external))
            :ordered-source-external-root
            (h {:domain :gravity/sh07-b51-ordered-source-external-root-v1
                :schema-version 1 :records (:source-rows external)})
            :ordered-m1-root
            (h {:domain :gravity/sh07-b51-ordered-m1-external-root-v1
                :schema-version 1 :records (:m1-rows external)})
            :bijection-complete true})
        claim {:domain :gravity/sh07-b51-common-census-claim-v1
               :normalized-census-id normalized-id
               :external-bijection-commitment external-commitment
               :builtin-parity-commitments builtin-commitments
               :provenance-completeness-commitment provenance-commitment
               :physical-census physical :category-schedule category-schedule
               :category-commitments commitments}
        assumptions-value (assumptions)
        raw-calls (count (filter #(contains? #{:local :external :builtin}
                                             (:call-classification %))
                                 (:records source-scan)))
        m2-calls (count (:calls m2-scan))
        raw-artifact
        (transcript-within!
         (transcript-artifact
          :gravity/sh07-b51-raw-source-census-transcript-v1
          :gravity/sh07-b51-raw-source-census-record-root-v1 checker inputs
          (:records source-scan) raw-calls))
        m2-artifact
        (transcript-within!
         (transcript-artifact
          :gravity/sh07-b51-m2-census-transcript-v1
          :gravity/sh07-b51-m2-census-record-root-v1 checker inputs
          (:records m2-scan) m2-calls))
        raw-descriptor (transcript-descriptor raw-artifact)
        m2-descriptor (transcript-descriptor m2-artifact)
        result {:status :accepted :normalized-census-id normalized-id
                :definition-count (:physical-definition-count physical)
                :local-count (:physical-local-runtime-callsite-count physical)
                :external-count
                (:physical-external-runtime-callsite-count physical)
                :builtin-count
                (:physical-referenced-builtin-occurrence-count physical)
                :semantic-row-count (:semantic-row-count physical)
                :external-bijection-complete true :builtin-parity-complete true
                :provenance-complete true :ordering-complete true
                :reachability-complete true}
        provenance
        (assoc (:provenance envelope)
               :raw-transcript-artifact-id (:artifact-id raw-artifact)
               :m2-transcript-artifact-id (:artifact-id m2-artifact))
        claim-id
        (h {:domain :gravity/sh07-b51-common-census-claim-id-v1
            :schema-version 1
            :architecture-report-sha256
            (:architecture-report-sha256 envelope)
            :inputs inputs :assumptions assumptions-value :claim claim
            :profile-bounds profile-bounds})
        evidence-root
        (h {:domain
            :gravity/sh07-b51-independent-verifier-census-evidence-root-v1
            :schema-version 1 :checker checker :inputs inputs
            :raw-source-transcript raw-descriptor
            :m2-transcript m2-descriptor :result result
            :reproduction-command-id (:reproduction-command-id provenance)
            :profile-bounds profile-bounds})
        certificate-base
        {:artifact :gravity/sh07-b51-compile-census-certificate
         :domain :gravity/sh07-b51-independent-verifier-census-certificate-v1
         :schema-version 1 :status :accepted
         :role :independent-verifier-census
         :architecture-report-sha256
         (:architecture-report-sha256 envelope)
         :claim-id claim-id :claim claim :assumptions assumptions-value
         :checker checker :inputs inputs :result result
         :provenance provenance :raw-source-transcript raw-descriptor
         :m2-transcript m2-descriptor :profile-bounds profile-bounds
         :evidence-root evidence-root}
        certificate (assoc certificate-base :certificate-id
                           (h certificate-base))]
    (ensure! (certificate-profile-within? (value-profile certificate))
             :certificate-profile-bound)
    {:status :accepted :certificate certificate
     :raw-source-transcript-artifact raw-artifact
     :m2-transcript-artifact m2-artifact}))

(defn- rejection [reason]
  (let [base {:artifact :gravity/sh07-b51-census-checker-rejection-evidence-v1
              :schema-version 1 :status :rejected
              :role :independent-verifier-census
              :algorithm-role :m2-first-independent-exhaustive
              :reason reason}]
    {:status :rejected
     :rejection-evidence (assoc base :rejection-id (h base))}))

(defn produce-certificate [envelope]
  (try
    (accepted-products envelope)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch clojure.lang.ExceptionInfo error
      (rejection (or (:reason (ex-data error)) :checker-b-failure)))
    (catch Throwable _
      (rejection :checker-b-failure))))

(defn- dense-records? [records key-name]
  (= (vec (range (count records))) (mapv key-name records)))

(defn- valid-full-transcript?
  [artifact expected-kind record-domain checker inputs record-keys]
  (and (exact-keys? artifact full-transcript-keys)
       (= expected-kind (:artifact artifact))
       (= 1 (:schema-version artifact))
       (= checker (:checker artifact)) (= inputs (:inputs artifact))
       (vector? (:records artifact))
       (= (:node-count artifact) (count (:records artifact)))
       (every? #(exact-keys? % record-keys) (:records artifact))
       (dense-records? (:records artifact) :record-ordinal)
       (= (:record-root artifact)
          (h {:domain record-domain :schema-version 1
              :checker-algorithm-role (:algorithm-role checker)
              :records (:records artifact)}))
       (= (:artifact-id artifact)
          (h {:domain :gravity/sh07-b51-census-transcript-artifact-id-v1
              :schema-version 1 :artifact expected-kind :checker checker
              :inputs inputs :node-count (:node-count artifact)
              :row-count (:row-count artifact)
              :record-root (:record-root artifact)}))
       (transcript-counts-within?
        {:node-count (:node-count artifact)
         :record-count (count (:records artifact))
         :call-row-count (:row-count artifact)
         :byte-count (utf8-count (pr-str artifact))})))

(defn certificate-valid?
  "Validate checker B's compact certificate without invoking either offline
  scanner.  Immutable host pins remain a separate admission obligation."
  [certificate]
  (try
    (let [claim (:claim certificate)
          assumptions-value (:assumptions certificate)
          checker (:checker certificate)
          inputs (:inputs certificate)
          result (:result certificate)
          provenance (:provenance certificate)
          raw (:raw-source-transcript certificate)
          m2 (:m2-transcript certificate)
          physical (:physical-census claim)
          commitments (:category-commitments claim)]
      (boolean
       (and (exact-keys? certificate certificate-keys)
            (= :gravity/sh07-b51-compile-census-certificate
               (:artifact certificate))
            (= :gravity/sh07-b51-independent-verifier-census-certificate-v1
               (:domain certificate))
            (= [1 :accepted :independent-verifier-census]
               [(:schema-version certificate) (:status certificate)
                (:role certificate)])
            (digest-id? (:architecture-report-sha256 certificate))
            (exact-keys? checker checker-keys)
            (= :m2-first-independent-exhaustive (:algorithm-role checker))
            (exact-keys? inputs input-identity-keys)
            (= (:architecture-report-sha256 certificate)
               (:architecture-report-sha256 inputs))
            (= 7 (count (:module-source-identities inputs))
               (count (:module-source-byte-counts inputs))
               (count (:m0-ids inputs)) (count (:m2-ids inputs)))
            (every? digest-id?
                    (concat (:module-source-identities inputs)
                            (:m0-ids inputs) (:m2-ids inputs)
                            [(:architecture-report-sha256 inputs)
                             (:predecessor-executable-descriptor-id inputs)
                             (:emitter-source-id inputs)
                             (:emitter-semantic-id inputs) (:m1-id inputs)
                             (:bundle-core-id inputs)]))
            (exact-keys? claim claim-keys)
            (= :gravity/sh07-b51-common-census-claim-v1 (:domain claim))
            (exact-keys? assumptions-value assumptions-keys)
            (= (assumptions) assumptions-value)
            (exact-keys? result result-keys)
            (= :accepted (:status result))
            (every? true?
                    ((juxt :external-bijection-complete
                           :builtin-parity-complete :provenance-complete
                           :ordering-complete :reachability-complete) result))
            (exact-keys? physical physical-census-keys)
            (vector? commitments) (= 7 (count commitments))
            (= categories (mapv :category commitments))
            (= category-schedule (:category-schedule claim))
            (= (:normalized-census-id claim) (:normalized-census-id result)
               (h {:domain :gravity/sh07-b51-normalized-census-id-v1
                   :schema-version 1 :physical-census physical
                   :ordered-category-row-roots (mapv :root-digest commitments)
                   :category-row-counts (mapv :row-count commitments)}))
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
            (exact-keys? provenance certificate-provenance-keys)
            (exact-keys? raw transcript-descriptor-keys)
            (exact-keys? m2 transcript-descriptor-keys)
            (= (:artifact-id raw) (:raw-transcript-artifact-id provenance))
            (= (:artifact-id m2) (:m2-transcript-artifact-id provenance))
            (= (:claim-id certificate)
               (h {:domain :gravity/sh07-b51-common-census-claim-id-v1
                   :schema-version 1
                   :architecture-report-sha256
                   (:architecture-report-sha256 certificate)
                   :inputs inputs :assumptions assumptions-value :claim claim
                   :profile-bounds profile-bounds}))
            (= (:evidence-root certificate)
               (h {:domain
                   :gravity/sh07-b51-independent-verifier-census-evidence-root-v1
                   :schema-version 1 :checker checker :inputs inputs
                   :raw-source-transcript raw :m2-transcript m2 :result result
                   :reproduction-command-id
                   (:reproduction-command-id provenance)
                   :profile-bounds profile-bounds}))
            (= profile-bounds (:profile-bounds certificate))
            (= (:certificate-id certificate)
               (h (dissoc certificate :certificate-id)))
            (certificate-profile-within? (value-profile certificate)))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(declare valid-rejection-output?)

(defn checker-output-valid?
  "Validate checker B's closed accepted output and both governed transcripts,
  or its closed rejection evidence, without rescanning source or M2."
  [output]
  (try
    (case (:status output)
      :accepted
      (let [certificate (:certificate output)
            checker (:checker certificate)
            inputs (:inputs certificate)
            raw (:raw-source-transcript-artifact output)
            m2 (:m2-transcript-artifact output)]
        (boolean
         (and (exact-keys? output accepted-output-keys)
              (certificate-valid? certificate)
              (valid-full-transcript?
               raw :gravity/sh07-b51-raw-source-census-transcript-v1
               :gravity/sh07-b51-raw-source-census-record-root-v1
               checker inputs source-record-keys)
              (valid-full-transcript?
               m2 :gravity/sh07-b51-m2-census-transcript-v1
               :gravity/sh07-b51-m2-census-record-root-v1
               checker inputs m2-record-keys)
              (= (transcript-descriptor raw)
                 (:raw-source-transcript certificate))
              (= (transcript-descriptor m2) (:m2-transcript certificate)))))
      :rejected (boolean (valid-rejection-output? output))
      false)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn- valid-accepted-output? [envelope output]
  (try
    (let [certificate (:certificate output)
          raw (:raw-source-transcript-artifact output)
          m2 (:m2-transcript-artifact output)
          inputs (check-and-derive-inputs envelope)
          checker (:checker envelope)
          claim (:claim certificate)
          result (:result certificate)
          certificate-base (dissoc certificate :certificate-id)]
      (and (exact-keys? output accepted-output-keys)
           (= :accepted (:status output))
           (certificate-valid? certificate)
           (exact-keys? certificate certificate-keys)
           (= :gravity/sh07-b51-compile-census-certificate
              (:artifact certificate))
           (= :gravity/sh07-b51-independent-verifier-census-certificate-v1
              (:domain certificate))
           (= [1 :accepted :independent-verifier-census]
              [(:schema-version certificate) (:status certificate)
               (:role certificate)])
           (= (:architecture-report-sha256 envelope)
              (:architecture-report-sha256 certificate)
              (:architecture-report-sha256 inputs))
           (= checker (:checker certificate))
           (= inputs (:inputs certificate))
           (exact-keys? claim claim-keys)
           (exact-keys? (:assumptions certificate) assumptions-keys)
           (= (assumptions) (:assumptions certificate))
           (exact-keys? result result-keys)
           (= :accepted (:status result))
           (every? true? ((juxt :external-bijection-complete
                                :builtin-parity-complete
                                :provenance-complete :ordering-complete
                                :reachability-complete) result))
           (= (:normalized-census-id claim) (:normalized-census-id result))
           (exact-keys? (:physical-census claim) physical-census-keys)
           (= (:definition-count result)
              (get-in claim [:physical-census :physical-definition-count]))
           (= (:local-count result)
              (get-in claim
                      [:physical-census
                       :physical-local-runtime-callsite-count]))
           (= (:external-count result)
              (get-in claim
                      [:physical-census
                       :physical-external-runtime-callsite-count]))
           (= (:builtin-count result)
              (get-in claim
                      [:physical-census
                       :physical-referenced-builtin-occurrence-count]))
           (= (:semantic-row-count result)
              (get-in claim [:physical-census :semantic-row-count])
              (reduce + (map :row-count (:category-commitments claim))))
           (= category-schedule (:category-schedule claim))
           (= categories (mapv :category (:category-commitments claim)))
           (= (:normalized-census-id claim)
              (h {:domain :gravity/sh07-b51-normalized-census-id-v1
                  :schema-version 1
                  :physical-census (:physical-census claim)
                  :ordered-category-row-roots
                  (mapv :root-digest (:category-commitments claim))
                  :category-row-counts
                  (mapv :row-count (:category-commitments claim))}))
           (valid-full-transcript?
            raw :gravity/sh07-b51-raw-source-census-transcript-v1
            :gravity/sh07-b51-raw-source-census-record-root-v1 checker inputs
            source-record-keys)
           (valid-full-transcript?
            m2 :gravity/sh07-b51-m2-census-transcript-v1
            :gravity/sh07-b51-m2-census-record-root-v1 checker inputs
            m2-record-keys)
           (= (transcript-descriptor raw)
              (:raw-source-transcript certificate))
           (= (transcript-descriptor m2) (:m2-transcript certificate))
           (exact-keys? (:provenance certificate) certificate-provenance-keys)
           (= (:artifact-id raw)
              (get-in certificate
                      [:provenance :raw-transcript-artifact-id]))
           (= (:artifact-id m2)
              (get-in certificate [:provenance :m2-transcript-artifact-id]))
           (= (:claim-id certificate)
              (h {:domain :gravity/sh07-b51-common-census-claim-id-v1
                  :schema-version 1
                  :architecture-report-sha256
                  (:architecture-report-sha256 certificate)
                  :inputs inputs :assumptions (:assumptions certificate)
                  :claim claim :profile-bounds profile-bounds}))
           (= (:evidence-root certificate)
              (h {:domain
                  :gravity/sh07-b51-independent-verifier-census-evidence-root-v1
                  :schema-version 1 :checker checker :inputs inputs
                  :raw-source-transcript (transcript-descriptor raw)
                  :m2-transcript (transcript-descriptor m2) :result result
                  :reproduction-command-id
                  (get-in certificate
                          [:provenance :reproduction-command-id])
                  :profile-bounds profile-bounds}))
           (= (:certificate-id certificate) (h certificate-base))
           (= profile-bounds (:profile-bounds certificate))
           (certificate-profile-within? (value-profile certificate))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn- contains-certificate-id-key? [value]
  (cond
    (map? value) (or (contains? value :certificate-id)
                     (some contains-certificate-id-key? (vals value)))
    (coll? value) (some contains-certificate-id-key? value)
    :else false))

(defn- valid-rejection-output? [output]
  (let [evidence (:rejection-evidence output)]
    (and (exact-keys? output rejection-output-keys)
         (= :rejected (:status output))
         (exact-keys? evidence rejection-keys)
         (= :gravity/sh07-b51-census-checker-rejection-evidence-v1
            (:artifact evidence))
         (= [1 :rejected :independent-verifier-census
             :m2-first-independent-exhaustive]
            [(:schema-version evidence) (:status evidence) (:role evidence)
             (:algorithm-role evidence)])
         (keyword? (:reason evidence))
         (= (:rejection-id evidence) (h (dissoc evidence :rejection-id)))
         (not (contains-certificate-id-key? output)))))

(defn valid-output? [envelope output]
  (case (:status output)
    :accepted (boolean (valid-accepted-output? envelope output))
    :rejected (boolean (valid-rejection-output? output))
    false))
