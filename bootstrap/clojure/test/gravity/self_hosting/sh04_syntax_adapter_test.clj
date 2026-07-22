(ns gravity.self-hosting.sh04-syntax-adapter-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh04_syntax_adapter_test.clj")]
    (when-not resource
      (throw (ex-info "SH-04 test source is not on the classpath"
                      {:id "SH04-TEST-SOURCE"})))
    (loop [path (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? path)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH04-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve path "deps.edn")))
        path

        :else
        (recur (.getParent path))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-04")

(def ^:private syntax-source-relative-path
  "bootstrap/gravity/src/gravity/bootstrap/syntax.gravity")

(def ^:private accepted-fixtures
  #{"source-syntax" "template-descriptor"})

(def ^:private rejected-fixtures
  #{"capture-unintentional"
    "fact-stale-version"
    "hygiene-duplicate-mark"
    "hygiene-marks-not-vector"
    "hygiene-rename-target-not-introduced"
    "id-request-substitution"
    "metadata-non-map"
    "origin-missing-producer"
    "serialization-substitution"
    "shape-extra-key"
    "span-backwards-column"
    "span-reversed"})

(def ^:private c3-rules
  #{"C3-SHAPE" "C3-ID" "C3-SPAN" "C3-ORIGIN"
    "C3-HYGIENE" "C3-CAPTURE" "C3-METADATA"
    "C3-FACT-STALE" "C3-SERIALIZE"})

(def ^:private authoritative-result-kind
  :gravity/sh04-syntax-object-artifact)

(def ^:private boundary-adapter-contract
  :gravity/sh04-to-c3-syntax-products-v1)

(def ^:private sealed-artifact-kind
  :gravity/sh04-syntax-products)

(def ^:private uncredited-facade
  {:module 'gravity.compiler.c3-syntax-object-model
   :source-path
   "bootstrap/gravity/src/gravity/compiler/c3_syntax_object_model.gravity"
   :status :compatibility-only
   :authentication-credit? false
   :authoritative-result? false
   :self-hosting-credit? false
   :seed-retirement-credit? false
   :release-credit? false})

(defn- fixture-path
  [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- fixture-basenames
  [family extension]
  (let [directory (io/file (path (str fixture-root "/" family)))]
    (->> (.listFiles directory)
         (filter #(.isFile %))
         (map #(.getName %))
         (filter #(.endsWith ^String % extension))
         (map #(subs % 0 (- (count %) (count extension))))
         set)))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- delete-tree!
  [tree]
  (when tree
    (doseq [file (reverse (file-seq (io/file tree)))]
      (io/delete-file file true))))

(defn- parsed-values
  [source-path]
  (let [artifact (bootstrap/compiler-c2-reader-file-artifact source-path)]
    (when-not (= :gravity/stage0-c2-reader-document-artifact
                 (:kind artifact))
      (throw (ex-info "SH-04 fixture did not pass the Gravity reader"
                      {:id "SH04-FIXTURE-READER"
                       :path source-path
                       :kind (:kind artifact)})))
    (:parsed-semantic-values artifact)))

(defn- fixture-value
  [family basename extension]
  (let [source-path (fixture-path family basename extension)
        values (parsed-values source-path)]
    (when-not (= 1 (count values))
      (throw (ex-info "SH-04 data fixture must contain one form"
                      {:id "SH04-FIXTURE-SHAPE"
                       :path source-path
                       :form-count (count values)})))
    (first values)))

(def ^:private accepted-gravity-values
  (delay
    (into {}
          (map (fn [basename]
                 [basename
                  (parsed-values
                   (fixture-path "accepted" basename ".gravity"))]))
          accepted-fixtures)))

(def ^:private accepted-descriptor
  (delay
    (let [values (get @accepted-gravity-values "template-descriptor")]
      (when-not (= 1 (count values))
        (throw (ex-info "SH-04 descriptor fixture must contain one form"
                        {:id "SH04-DESCRIPTOR-SHAPE"
                         :form-count (count values)})))
      (first values))))

(def ^:private rejected-scenarios
  (delay
    (into {}
          (map (fn [basename]
                 [basename
                  (fixture-value "rejected" basename ".gravity")]))
          rejected-fixtures)))

(def ^:private rejected-qst-scenarios
  (delay
    (into {}
          (map (fn [basename]
                 [basename
                  (fixture-value "rejected" basename ".qst")]))
          rejected-fixtures)))

(defn- public-artifact
  [extension]
  (let [source-path
        (fixture-path "accepted" "source-syntax" extension)]
    {:path source-path
     :bytes (source-bytes source-path)
     :c2-artifact (bootstrap/compiler-c2-reader-file-artifact source-path)
     :artifact (bootstrap/compiler-c3-syntax-file-artifact source-path)}))

(def ^:private public-gravity-artifact
  (delay (public-artifact ".gravity")))

(def ^:private public-qst-artifact
  (delay (public-artifact ".qst")))

(def ^:private public-artifacts
  (delay {".gravity" @public-gravity-artifact
          ".qst" @public-qst-artifact}))

(defn- compile-plan
  [source-path target]
  (let [source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path target))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private syntax-plan
  (delay (compile-plan (path syntax-source-relative-path) :jvm)))

(defn- invoke-syntax
  [function-name arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh04-syntax-leaf
    :compiler-artifact-plan? true}
   @syntax-plan function-name arguments))

(defn- captured-invocation
  [function-name arguments]
  (try
    {:result (invoke-syntax function-name arguments)}
    (catch Throwable error
      {:host-error (.getName (class error))
       :message (.getMessage error)})))

(defn- canonical-id
  [value]
  (bootstrap/p15-s23-c6c10-canonical-digest
   "<sh04-syntax-adapter-test>" value))

(defn- sha256-id?
  [value]
  (boolean (and (string? value)
                (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- contains-string-fragment?
  [value fragment]
  (cond
    (string? value) (str/includes? value fragment)
    (map? value) (boolean
                  (some #(contains-string-fragment? % fragment)
                        (concat (keys value) (vals value))))
    (coll? value) (boolean
                   (some #(contains-string-fragment? % fragment) value))
    :else false))

(defn- resolve-digest-requests!
  [source-label requests]
  (let [request-count (count requests)]
    (reduce
     (fn [resolved request]
       (let [ordinal (:ordinal request)
             resolved-preimage
             (bootstrap/p15-s23-c6c10-resolve-digest-references!
              source-label (:preimage request)
              request-count ordinal resolved)]
         (when-not (= ordinal (count resolved))
           (throw (ex-info "SH-04 digest requests are not ordered"
                           {:id "SH04-DIGEST-ORDER"
                            :ordinal ordinal})))
         (conj resolved (canonical-id resolved-preimage))))
     [] requests)))

(defn- resolve-template!
  [source-label template requests resolved-digests]
  (bootstrap/p15-s23-c6c10-resolve-digest-references!
   source-label template (count requests) nil resolved-digests))

(defn- resolve-builder-result!
  [raw-result]
  (let [requests (:digest-requests raw-result)]
    (when-not (and (= :accepted (:status raw-result))
                   (= :gravity/sh04-syntax-template-result
                      (:artifact raw-result))
                   (= 2 (count requests)))
      (throw (ex-info "Invalid SH-04 builder result"
                      {:id "SH04-BUILDER-RESULT"
                       :result raw-result})))
    (let [resolved-digests
          (resolve-digest-requests! "<sh04-syntax-builder>" requests)
          syntax
          (resolve-template! "<sh04-syntax-builder>"
                             (:syntax-template raw-result)
                             requests resolved-digests)
          verification
          (invoke-syntax
           'c3-syntax-verify-resolved
           [syntax requests resolved-digests
            (:reader-binding raw-result)
            (:reader-source-revision raw-result)])]
      (when-not (= :passed (:status verification))
        (throw (ex-info "SH-04 resolved syntax did not verify"
                        {:id "SH04-RESOLVED-SYNTAX"
                         :verification verification})))
      {:raw raw-result
       :syntax syntax
       :resolved-digests resolved-digests
       :verification-report verification
       :product {:syntax-object syntax
                 :digest-requests requests
                 :resolved-digests resolved-digests}})))

(defn- build-and-resolve!
  [descriptor]
  (resolve-builder-result!
   (invoke-syntax 'c3-syntax-build-template [descriptor])))

(defn- build-stream-and-resolve!
  [resolved-products reader-binding reader-source-revision root-syntax-ids]
  (let [raw
        (invoke-syntax
         'c3-syntax-stream-build-template
         [resolved-products reader-binding reader-source-revision
          root-syntax-ids])
        requests (:digest-requests raw)]
    (when-not (and (= :accepted (:status raw))
                   (= :gravity/sh04-syntax-stream-template-result
                      (:artifact raw))
                   (= 1 (count requests)))
      (throw (ex-info "Invalid SH-04 stream builder result"
                      {:id "SH04-STREAM-BUILDER-RESULT"
                       :result raw})))
    (let [resolved-digests
          (resolve-digest-requests! "<sh04-syntax-stream>" requests)
          stream
          (resolve-template! "<sh04-syntax-stream>"
                             (:stream-template raw)
                             requests resolved-digests)
          verification
          (invoke-syntax
           'c3-syntax-stream-verify-resolved
           [stream requests resolved-digests])]
      (when-not (= :passed (:status verification))
        (throw (ex-info "SH-04 resolved stream did not verify"
                        {:id "SH04-RESOLVED-STREAM"
                         :verification verification})))
      {:raw raw
       :stream stream
       :resolved-digests resolved-digests
       :verification-report verification})))

(defn- result-rule
  [result]
  (get-in result [:diagnostics 0 :rule]))

(defn- diagnostic-template?
  [diagnostic]
  (= #{:artifact :rule :severity :lifecycle :stage :primary :related
       :origin-chain :profile :target :facts :remediation
       :diagnostic-id-request}
     (set (keys diagnostic))))

(defn- scenario-result
  [descriptor scenario]
  (case (:phase scenario)
    :build
    (invoke-syntax
     'c3-syntax-build-template
     [(assoc-in descriptor (:path scenario) (:value scenario))])

    :verify
    (let [raw (invoke-syntax 'c3-syntax-build-template [descriptor])
          changed (assoc-in raw (:path scenario) (:value scenario))]
      (invoke-syntax
       'c3-syntax-verify-template
       [(:syntax-template raw) (:digest-requests changed)]))

    :serialize
    (let [product (build-and-resolve! descriptor)
          stream
          (build-stream-and-resolve!
           [(:product product)]
           (:reader-binding descriptor)
           (:reader-source-revision descriptor)
           [(:syntax-id (:syntax product))])
          changed
          (assoc-in (:stream stream) (:path scenario) (:value scenario))]
      (invoke-syntax
       'c3-syntax-serialize-template
       [changed (:digest-requests (:raw stream))
        (:resolved-digests stream)]))

    (throw (ex-info "Unknown SH-04 scenario phase"
                    {:id "SH04-SCENARIO-PHASE"
                     :scenario scenario}))))

(defn- serialization-id
  [serialization]
  (canonical-id (:payload-id-request serialization)))

(defn- stable-test-id
  [family index]
  (canonical-id {:domain :gravity/sh04-dedicated-test-id
                 :family family
                 :index index}))

(defn- generated-hygiene
  []
  {:marks [:macro-mark]
   :lexical-scopes [:call-site-scope :introduced-scope]
   :renames {'tmp 'tmp__sh04__1}
   :introduced-identifiers ['tmp__sh04__1]
   :captures [{:identifier 'captured-value
               :macro-api 'gravity.syntax/capture
               :call-site-namespace 'self-hosting.sh04.template-descriptor
               :intentional? true
               :authority-bearing? false
               :policy-result :not-required}]
   :macro-definition-namespace 'gravity.bootstrap.syntax
   :macro-call-site-namespace 'self-hosting.sh04.template-descriptor})

(defn- generated-object
  [base index prior-id]
  (let [producer-id (stable-test-id :depth-producer 0)
        generated-span {:kind :generated
                        :producer-id producer-id
                        :ordinal index}
        syntax-id (stable-test-id :depth index)
        form-id (keyword (str "generated-" index))]
    (-> base
        (assoc :syntax-id syntax-id)
        (assoc :form {:kind :generated-form
                      :value (list 'generated index)
                      :raw (str "(generated " index ")")})
        (assoc :span {:primary generated-span
                      :all [generated-span]})
        (assoc :source {:source-id (get-in base [:source :source-id])
                        :form-id form-id
                        :token-range []})
        (assoc :phase :macro-expanded)
        (assoc :metadata {:generated true :index index})
        (assoc :hygiene (generated-hygiene))
        (assoc :origin
               [{:kind :generated
                 :span generated-span
                 :producer {:kind :macro
                            :name 'gravity.bootstrap.syntax/depth-case
                            :identity producer-id
                            :source-id (get-in base [:source :source-id])
                            :generated-form-id form-id}
                 :producer-version "SH-04"
                 :input-syntax-ids [prior-id]
                 :generation-reason :bounded-depth-test
                 :build-effects []}])
        (assoc :facts [])
        (assoc-in [:ownership :form-id] form-id)
        (assoc :prior-syntax-ids [prior-id]))))

(defn- reference-chain
  [base object-count]
  (loop [index 1
         objects [(assoc base :syntax-id (stable-test-id :depth 0))]]
    (if (= index object-count)
      objects
      (let [prior-id (:syntax-id (peek objects))]
        (recur (inc index)
               (conj objects (generated-object base index prior-id)))))))

(defn- capture-record
  [index]
  {:identifier (symbol (str "capture-" index))
   :macro-api 'gravity.syntax/capture
   :call-site-namespace 'self-hosting.sh04.bounds
   :intentional? true
   :authority-bearing? false
   :policy-result :not-required})

(defn- fact-record
  [index]
  {:producer-stage :reader
   :fact-kind (keyword (str "fact-" index))
   :value index
   :version 1
   :invalidated-by [:macro-expansion]})

(defn- generated-product!
  [base-build descriptor index]
  (let [base (:syntax base-build)
        producer-id (stable-test-id :stream-generated-producer index)
        form-id (keyword (str "stream-generated-" index))
        producer {:kind :macro
                  :name 'gravity.bootstrap.syntax/stream-generated-case
                  :identity producer-id
                  :version "SH-04"
                  :source-id (get-in base [:source :source-id])
                  :generated-form-id form-id}
        generated-span {:kind :generated
                        :producer-id producer-id
                        :ordinal index}
        raw
        (invoke-syntax
         'c3-generated-syntax-template
         [(:syntax-id base)
          {:kind :generated-form
           :value (list 'generated-stream-form index)
           :raw (str "(generated-stream-form " index ")")}
          generated-span producer :stream-product-test
          (:namespace base) (:profile base)
          {:generated true :stream-index index}
          (generated-hygiene)
          [{:producer-stage :macro-expansion
            :fact-kind :stream-generated-origin-checked
            :value true
            :version 1
            :invalidated-by [:name-resolution]}]
          (:reader-binding descriptor)
          (:reader-source-revision descriptor)])]
    (assoc (resolve-builder-result! raw)
           :producer producer
           :generated-span generated-span)))

(deftest sh04-fixture-inventory-is-co-canonical
  (testing "the checked-in inventory is explicit and symmetric"
    (is (= accepted-fixtures (fixture-basenames "accepted" ".gravity")))
    (is (= accepted-fixtures (fixture-basenames "accepted" ".qst")))
    (is (= rejected-fixtures (fixture-basenames "rejected" ".gravity")))
    (is (= rejected-fixtures (fixture-basenames "rejected" ".qst"))))
  (testing "every co-canonical pair has identical bytes and reader values"
    (doseq [[family basenames]
            [["accepted" accepted-fixtures]
             ["rejected" rejected-fixtures]]
            basename (sort basenames)]
      (let [gravity-path (fixture-path family basename ".gravity")
            qst-path (fixture-path family basename ".qst")]
        (is (java.util.Arrays/equals (source-bytes gravity-path)
                                    (source-bytes qst-path))
            (str family "/" basename)))))
  (testing "both members of every pair are executable Gravity reader input"
    (is (= accepted-fixtures (set (keys @accepted-gravity-values))))
    (is (every? seq (vals @accepted-gravity-values)))
    (is (= rejected-fixtures (set (keys @rejected-scenarios))))
    (is (= rejected-fixtures (set (keys @rejected-qst-scenarios))))
    (is (= @rejected-scenarios @rejected-qst-scenarios)))
  (testing "rejected scenarios cover the normative C3 catalog exactly"
    (is (= c3-rules
           (set (map :expected-rule (vals @rejected-scenarios)))))))

(deftest sh04-gravity-module-exposes-the-authoritative-function-contract
  (let [plan @syntax-plan
        functions (:functions plan)]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind plan)))
    (is (= {:arity 1 :params ['descriptor]}
           (select-keys (get functions 'c3-syntax-build-template)
                        [:arity :params])))
    (is (= {:arity 12
            :params ['base-syntax-id 'form 'generated-span 'producer
                     'generation-reason 'namespace-context 'profile
                     'metadata 'hygiene 'facts 'reader-binding
                     'reader-source-revision]}
           (select-keys (get functions 'c3-generated-syntax-template)
                        [:arity :params])))
    (is (= {:arity 2 :params ['syntax 'digest-requests]}
           (select-keys (get functions 'c3-syntax-verify-template)
                        [:arity :params])))
    (is (= {:arity 5
            :params ['syntax 'digest-requests 'resolved-digests
                     'reader-binding 'reader-source-revision]}
           (select-keys (get functions 'c3-syntax-verify-resolved)
                        [:arity :params])))
    (is (= {:arity 4
            :params ['resolved-products 'reader-binding
                     'reader-source-revision 'root-syntax-ids]}
           (select-keys (get functions 'c3-syntax-stream-build-template)
                        [:arity :params])))
    (is (= {:arity 3
            :params ['resolved-stream 'digest-requests
                     'resolved-digests]}
           (select-keys (get functions 'c3-syntax-stream-verify-resolved)
                        [:arity :params])))
    (is (= {:arity 3
            :params ['resolved-stream 'digest-requests
                     'resolved-digests]}
           (select-keys (get functions 'c3-syntax-serialize-template)
                        [:arity :params])))
    (is (= {:arity 1 :params ['carrier]}
           (select-keys (get functions 'c3-syntax-deserialize-template)
                        [:arity :params])))
    (is (= {:arity 1 :params ['syntax-objects]}
           (select-keys (get functions 'c3-syntax-graph-verify-template)
                        [:arity :params])))
    (is (= #{} (get-in plan [:module :effects])))
    (is (= #{} (get-in plan [:module :capabilities])))
    (is (= :safe (get-in plan [:module :safety])))
    (is (= 'gravity.bootstrap.syntax
           (get-in plan [:module :module])))
    (is (= "sha256:afcab42f39743e1609657e389ee478a79f8e98cabcc6fe331c2168106e584553"
           (get-in plan [:source :sha256])))))

(deftest sh04-gravity-build-verify-and-serialize-are-stable-and-path-neutral
  (let [descriptor
        @accepted-descriptor
        relocated
        (-> descriptor
            (assoc-in [:span :source]
                      "/checkout-b/src/template-descriptor.qst")
            (assoc-in [:origin 0 :span :source]
                      "/checkout-b/src/template-descriptor.qst"))
        first-build (build-and-resolve! descriptor)
        repeated-build (build-and-resolve! descriptor)
        relocated-build (build-and-resolve! relocated)
        raw (:raw first-build)
        syntax (:syntax first-build)
        relocated-syntax (:syntax relocated-build)
        template-verification
        (invoke-syntax 'c3-syntax-verify-template
                       [(:syntax-template raw) (:digest-requests raw)])
        first-stream
        (build-stream-and-resolve!
         [(:product first-build)]
         (:reader-binding descriptor)
         (:reader-source-revision descriptor)
         [(:syntax-id syntax)])
        repeated-stream
        (build-stream-and-resolve!
         [(:product repeated-build)]
         (:reader-binding descriptor)
         (:reader-source-revision descriptor)
         [(:syntax-id (:syntax repeated-build))])
        relocated-stream
        (build-stream-and-resolve!
         [(:product relocated-build)]
         (:reader-binding relocated)
         (:reader-source-revision relocated)
         [(:syntax-id relocated-syntax)])
        serialization
        (invoke-syntax 'c3-syntax-serialize-template
                       [(:stream first-stream)
                        (get-in first-stream [:raw :digest-requests])
                        (:resolved-digests first-stream)])
        relocated-serialization
        (invoke-syntax 'c3-syntax-serialize-template
                       [(:stream relocated-stream)
                        (get-in relocated-stream [:raw :digest-requests])
                        (:resolved-digests relocated-stream)])]
    (is (= #{:artifact :schema-version :status :syntax-template
             :reader-binding :reader-source-revision :ownership-product
             :digest-requests :diagnostics :bounds :execution-boundary
             :containment}
           (set (keys raw))))
    (is (= :accepted (:status raw)))
    (is (= first-build repeated-build))
    (is (= first-stream repeated-stream))
    (is (sha256-id? (:syntax-id syntax)))
    (is (= (:syntax-id syntax) (:syntax-id relocated-syntax)))
    (is (= (get-in first-stream [:stream :artifact-id])
           (get-in relocated-stream [:stream :artifact-id])))
    (is (= (get-in descriptor [:span :source])
           (get-in syntax [:span :primary :source])))
    (is (= (get-in relocated [:span :source])
           (get-in relocated-syntax [:span :primary :source])))
    (is (= :passed (:status template-verification)))
    (is (= :passed (get-in first-build [:verification-report :status])))
    (is (= :passed (get-in first-stream [:verification-report :status])))
    (is (= #{:artifact :schema-version :status :checks :containment
             :diagnostics}
           (set (keys template-verification))))
    (is (= #{:artifact :schema-version :status :stream-template
             :digest-requests :verification-reports :containment
             :diagnostics}
           (set (keys (:raw first-stream)))))
    (is (= :accepted (:status serialization)))
    (is (= #{:artifact :schema-version :status :encoding :carrier
             :semantic-payload :payload-id-request :containment
             :diagnostics}
           (set (keys serialization))))
    (is (= :gravity/sh04-syntax-stream-carrier-v1
           (first (:carrier serialization))))
    (is (= 4 (count (:carrier serialization))))
    (let [deserialized
          (invoke-syntax 'c3-syntax-deserialize-template
                         [(:carrier serialization)])]
      (is (= :accepted (:status deserialized)))
      (is (= :gravity/sh04-syntax-stream-deserialization
             (:artifact deserialized)))
      (is (= (:semantic-payload serialization)
             (:semantic-payload deserialized)))
      (is (= false
             (get-in deserialized
                     [:containment :downstream-artifacts-forbidden]))))
    (is (sha256-id? (serialization-id serialization)))
    (is (= (serialization-id serialization)
           (serialization-id relocated-serialization)))
    (is (not (contains-string-fragment?
              (:payload-id-request serialization) "/checkout-a/")))
    (is (not (contains-string-fragment?
              (:payload-id-request relocated-serialization) "/checkout-b/")))
    (is (not= (:semantic-payload serialization)
              (:semantic-payload relocated-serialization)))))

(deftest sh04-reader-binding-and-resolved-identity-mutations-fail-closed
  (let [descriptor @accepted-descriptor
        valid (build-and-resolve! descriptor)
        missing-binding
        (invoke-syntax 'c3-syntax-build-template
                       [(dissoc descriptor :reader-binding)])
        binding-substitution
        (assoc-in descriptor [:reader-binding :token-stream-id]
                  (stable-test-id :binding-substitution 0))
        substitution-raw
        (invoke-syntax 'c3-syntax-build-template [binding-substitution])
        substitution-requests (:digest-requests substitution-raw)
        substitution-digests
        (resolve-digest-requests! "<sh04-binding-substitution>"
                                  substitution-requests)
        substitution-syntax
        (resolve-template! "<sh04-binding-substitution>"
                           (:syntax-template substitution-raw)
                           substitution-requests substitution-digests)
        substitution-verification
        (invoke-syntax
         'c3-syntax-verify-resolved
         [substitution-syntax substitution-requests substitution-digests
          (:reader-binding binding-substitution)
          (:reader-source-revision binding-substitution)])
        revision-substitution
        (invoke-syntax
         'c3-syntax-build-template
         [(assoc-in descriptor
                    [:reader-source-revision :semantic-binding-id]
                    (stable-test-id :revision-substitution 0))])
        replay-source-id (stable-test-id :replayed-source 0)
        replay-binding-id (stable-test-id :replayed-binding 0)
        replayed
        (-> descriptor
            (assoc-in [:reader-binding :semantic-source-id]
                      replay-source-id)
            (assoc-in [:reader-binding :semantic-binding-id]
                      replay-binding-id)
            (assoc-in [:reader-source-revision :semantic-binding-id]
                      replay-binding-id))
        replay-result
        (invoke-syntax 'c3-syntax-build-template [replayed])
        retained-id-mutation
        (assoc-in (:syntax valid) [:metadata :retained-id-mutation] true)
        mutation-verification
        (invoke-syntax
         'c3-syntax-verify-resolved
         [retained-id-mutation
          (get-in valid [:raw :digest-requests])
          (:resolved-digests valid)
          (:reader-binding descriptor)
          (:reader-source-revision descriptor)])]
    (is (= :rejected (:status missing-binding)))
    (is (= "C3-ID" (result-rule missing-binding)))
    (is (= true
           (get-in missing-binding
                   [:containment :downstream-artifacts-forbidden])))
    (is (= :accepted (:status substitution-raw)))
    (is (= :failed (:status substitution-verification)))
    (is (= "C3-ID" (result-rule substitution-verification)))
    (is (= :reader-semantic-binding-id-mismatch
           (get-in substitution-verification
                   [:diagnostics 0 :facts :reason])))
    (is (= :rejected (:status revision-substitution)))
    (is (= "C3-ID" (result-rule revision-substitution)))
    (is (= :rejected (:status replay-result)))
    (is (= "C3-ID" (result-rule replay-result)))
    (is (= :failed (:status mutation-verification)))
    (is (= "C3-ID" (result-rule mutation-verification)))
    (is (= :semantic-field-or-request-mutation
           (get-in mutation-verification
                   [:diagnostics 0 :facts :reason])))
    (is (every?
         true?
         (map #(get-in % [:containment
                          :downstream-artifacts-forbidden])
              [missing-binding substitution-verification
               revision-substitution replay-result
               mutation-verification])))))

(deftest sh04-generated-syntax-preserves-hygiene-capture-and-origins
  (let [descriptor
        @accepted-descriptor
        base-build (build-and-resolve! descriptor)
        base (:syntax base-build)
        producer-id (stable-test-id :generated-producer 0)
        producer {:kind :macro
                  :name 'gravity.bootstrap.syntax/generated-case
                  :identity producer-id
                  :version "SH-04"
                  :source-id (get-in base [:source :source-id])
                  :generated-form-id :generated-form-0}
        generated-span {:kind :generated
                        :producer-id producer-id
                        :ordinal 0}
        generated-raw
        (invoke-syntax
         'c3-generated-syntax-template
         [(:syntax-id base)
          {:kind :generated-form
           :value '(do tmp__sh04__1)
           :raw "(do tmp__sh04__1)"}
          generated-span
          producer
          :macro-expansion
          (:namespace base)
          (:profile base)
          {:generated true}
          (generated-hygiene)
          [{:producer-stage :macro-expansion
            :fact-kind :generated-origin-checked
            :value true
            :version 1
            :invalidated-by [:name-resolution]}]
          (:reader-binding descriptor)
          (:reader-source-revision descriptor)])
        generated-build (resolve-builder-result! generated-raw)
        generated (:syntax generated-build)
        graph
        (invoke-syntax 'c3-syntax-graph-verify-template [[base generated]])
        stream
        (build-stream-and-resolve!
         [(:product base-build) (:product generated-build)]
         (:reader-binding descriptor)
         (:reader-source-revision descriptor)
         [(:syntax-id base)])
        resolved-stream (:stream stream)]
    (is (= :accepted (:status generated-raw)))
    (is (sha256-id? (:syntax-id generated)))
    (is (= :generated-form (get-in generated [:form :kind])))
    (is (= :macro-expanded (:phase generated)))
    (is (= [:macro-mark] (get-in generated [:hygiene :marks])))
    (is (= [:call-site-scope :introduced-scope]
           (get-in generated [:hygiene :lexical-scopes])))
    (is (= {'tmp 'tmp__sh04__1}
           (get-in generated [:hygiene :renames])))
    (is (= [(:syntax-id base)] (:prior-syntax-ids generated)))
    (is (= [(:syntax-id base)]
           (get-in generated [:origin 0 :input-syntax-ids])))
    (is (= (dissoc producer :version)
           (get-in generated [:origin 0 :producer])))
    (is (= generated-span (get-in generated [:span :primary])))
    (is (= generated-span (get-in generated [:origin 0 :span])))
    (is (= :macro-expansion
           (get-in generated [:origin 0 :generation-reason])))
    (is (= :passed (:status graph)))
    (is (= 2 (:object-count graph)))
    (is (= 2 (:maximum-reference-depth graph)))
    (is (= [(:syntax-id base)] (:root-syntax-ids resolved-stream)))
    (is (= [base generated] (:syntax-object-stream resolved-stream)))
    (is (= :gravity/sh04-hygiene-context-map
           (get-in resolved-stream [:hygiene-context-map :artifact])))
    (is (= :gravity/sh04-metadata-ledger
           (get-in resolved-stream [:metadata-ledger :artifact])))
    (is (= :gravity/sh04-fact-invalidation-ledger
           (get-in resolved-stream [:fact-invalidation-ledger :artifact])))
    (is (= :gravity/sh04-origin-chain-graph
           (get-in resolved-stream [:origin-chain-graph :artifact])))
    (is (= :gravity-source
           (get-in resolved-stream [:ownership-product :owner])))
    (is (= 'gravity.bootstrap.syntax
           (get-in resolved-stream [:ownership-product :module])))))

(deftest sh04-complete-stream-products-roundtrip-and-mutations-fail-closed
  (let [descriptor @accepted-descriptor
        first-build (build-and-resolve! descriptor)
        second-span
        (assoc (:span descriptor)
               :byte-start 14 :byte-end 28
               :scalar-start 14 :scalar-end 28
               :column-start 15 :column-end 29)
        second-descriptor
        (-> descriptor
            (assoc :form {:kind :symbol
                          :value 'second-fixture-value
                          :raw "second-fixture"})
            (assoc :span second-span)
            (assoc-in [:source :form-id] :form-1)
            (assoc-in [:origin 0 :span] second-span)
            (assoc :metadata {:slice :SH-04 :fixture :second-root}))
        second-build (build-and-resolve! second-descriptor)
        generated-build (generated-product! first-build descriptor 0)
        products [(:product first-build)
                  (:product second-build)
                  (:product generated-build)]
        syntaxes (mapv :syntax [first-build second-build generated-build])
        root-ids (mapv :syntax-id [(:syntax first-build)
                                   (:syntax second-build)])
        stream-build
        (build-stream-and-resolve!
         products (:reader-binding descriptor)
         (:reader-source-revision descriptor) root-ids)
        stream (:stream stream-build)
        requests (get-in stream-build [:raw :digest-requests])
        digests (:resolved-digests stream-build)
        serialization
        (invoke-syntax 'c3-syntax-serialize-template
                       [stream requests digests])
        deserialization
        (invoke-syntax 'c3-syntax-deserialize-template
                       [(:carrier serialization)])
        dropped-root
        (invoke-syntax
         'c3-syntax-stream-build-template
         [products (:reader-binding descriptor)
          (:reader-source-revision descriptor) [(first root-ids)]])
        reordered-roots
        (invoke-syntax
         'c3-syntax-stream-build-template
         [products (:reader-binding descriptor)
          (:reader-source-revision descriptor) (vec (reverse root-ids))])
        generated-as-root
        (invoke-syntax
         'c3-syntax-stream-build-template
         [products (:reader-binding descriptor)
          (:reader-source-revision descriptor)
          (conj root-ids (:syntax-id (:syntax generated-build)))])
        extra-product-field
        (invoke-syntax
         'c3-syntax-stream-build-template
         [(assoc-in products [0 :unexpected] true)
          (:reader-binding descriptor)
          (:reader-source-revision descriptor) root-ids])
        derived-ledger-mutation
        (invoke-syntax
         'c3-syntax-stream-verify-resolved
         [(assoc-in stream [:metadata-ledger :entries 0 :action]
                    :substituted)
          requests digests])
        retained-object-id-mutation
        (invoke-syntax
         'c3-syntax-stream-verify-resolved
         [(assoc-in stream
                    [:resolved-object-products 0 :syntax-object
                     :metadata :substituted]
                    true)
          requests digests])
        carrier-tag-mutation
        (invoke-syntax
         'c3-syntax-deserialize-template
         [(assoc (:carrier serialization) 0
                 :gravity/sh04-substituted-carrier-v1)])
        carrier-width-mutation
        (invoke-syntax
         'c3-syntax-deserialize-template
         [(pop (:carrier serialization))])]
    (is (= :gravity/sh04-syntax-object-artifact (:artifact stream)))
    (is (= :accepted (:status stream)))
    (is (sha256-id? (:artifact-id stream)))
    (is (= syntaxes (:syntax-object-stream stream)))
    (is (= root-ids (:root-syntax-ids stream)))
    (is (= (mapv (fn [product]
                   {:syntax-object (:syntax-object product)
                    :resolved-digests (:resolved-digests product)})
                 products)
           (:resolved-object-products stream)))
    (is (= (mapv :hygiene syntaxes)
           (mapv #(dissoc % :syntax-id)
                 (get-in stream [:hygiene-context-map :contexts]))))
    (is (= (mapv :metadata syntaxes)
           (mapv :metadata (get-in stream [:metadata-ledger :entries]))))
    (is (= (mapv :origin syntaxes)
           (mapv :origin (get-in stream [:origin-chain-graph :nodes]))))
    (is (= (mapv :ownership syntaxes)
           (get-in stream [:ownership-product :syntax-ownership])))
    (is (= 3 (count (get-in stream
                            [:fact-invalidation-ledger :entries]))))
    (is (= :gravity-source
           (get-in stream [:ownership-product :owner])))
    (is (= 'gravity.bootstrap.syntax
           (get-in stream [:ownership-product :module])))
    (is (= :accepted (:status serialization)))
    (is (= :accepted (:status deserialization)))
    (is (= (:semantic-payload serialization)
           (:semantic-payload deserialization)))
    (doseq [[label result expected-rule]
            [[:dropped-root dropped-root "C3-SHAPE"]
             [:reordered-roots reordered-roots "C3-SHAPE"]
             [:generated-as-root generated-as-root "C3-SHAPE"]
             [:extra-product-field extra-product-field "C3-ID"]
             [:derived-ledger-mutation derived-ledger-mutation "C3-ID"]
             [:retained-object-id-mutation
              retained-object-id-mutation "C3-ID"]
             [:carrier-tag-mutation carrier-tag-mutation "C3-SERIALIZE"]
             [:carrier-width-mutation carrier-width-mutation
              "C3-SERIALIZE"]]]
      (is (contains? #{:rejected :failed} (:status result)) (str label))
      (is (= expected-rule (result-rule result)) (str label " " result))
      (is (= true
             (get-in result
                     [:containment :downstream-artifacts-forbidden]))
          (str label)))))

(deftest sh04-stream-v2-identity-is-bounded-and-order-sensitive-at-declared-capacity
  (let [descriptor @accepted-descriptor
        product-id (fn [index]
                     (canonical-id {:domain :sh04-synthetic-product
                                    :index index}))
        products
        (fn [size]
          (mapv (fn [index]
                  (let [syntax-id (product-id index)]
                    {:syntax-object {:syntax-id syntax-id}
                     :resolved-digests
                     [(canonical-id {:domain :sh04-synthetic-binding
                                     :index index})
                      syntax-id]}))
                (range size)))
        preimage
        (fn [candidate-products candidate-root-ids size]
          (invoke-syntax
           'c3-stream-identity-preimage
           [candidate-products
            (:reader-binding descriptor)
            (:reader-source-revision descriptor)
            candidate-root-ids
            {:object-count size :maximum-reference-depth 1}
            {:contexts (vec (repeat size {}))}
            {:entries (vec (repeat size {}))}
            {:entries (vec (repeat size {}))}
            {:nodes (vec (repeat size {}))}
            {:syntax-ownership (vec (repeat size {}))}]))
        substitute-id
        (fn [candidate-products index label]
          (let [syntax-id
                (canonical-id {:domain :sh04-synthetic-substitution
                               :position label})]
            (-> candidate-products
                (assoc-in [index :syntax-object :syntax-id] syntax-id)
                (assoc-in [index :resolved-digests 1] syntax-id))))
        substitute-root-id
        (fn [candidate-root-ids index label]
          (assoc candidate-root-ids index
                 (canonical-id {:domain :sh04-synthetic-root-substitution
                                :position label})))
        products-300 (products 300)
        root-ids-300
        (mapv #(get-in % [:syntax-object :syntax-id]) products-300)
        baseline-300 (preimage products-300 root-ids-300 300)
        baseline-300-digest
        (bootstrap/p15-s23-c6c10-canonical-digest
         "<sh04-stream-v2-identity-test-300>" baseline-300)
        chunks-300 (:resolved-product-identity-chunks baseline-300)
        root-chunks-300 (:root-syntax-id-chunks baseline-300)
        products-2048 (products 2048)
        root-ids-2048
        (mapv #(get-in % [:syntax-object :syntax-id]) products-2048)
        baseline-2048 (preimage products-2048 root-ids-2048 2048)
        baseline-2048-record
        (bootstrap/p15-s23-c6c10-canonical-record
         "<sh04-stream-v2-identity-test-2048>" baseline-2048)
        baseline-2048-digest
        (bootstrap/p15-s23-c6c10-canonical-digest
         "<sh04-stream-v2-identity-test-2048>" baseline-2048)
        chunks-2048 (:resolved-product-identity-chunks baseline-2048)
        root-chunks-2048 (:root-syntax-id-chunks baseline-2048)
        middle 150
        candidates
        {:product-reordered [(vec (reverse products-300)) root-ids-300]
         :product-deleted [(pop products-300) root-ids-300]
         :product-duplicate
         [(assoc products-300 middle (first products-300)) root-ids-300]
         :product-first-id
         [(substitute-id products-300 0 :first) root-ids-300]
         :product-middle-id
         [(substitute-id products-300 middle :middle) root-ids-300]
         :product-last-id
         [(substitute-id products-300 299 :last) root-ids-300]
         :root-reordered [products-300 (vec (reverse root-ids-300))]
         :root-deleted [products-300 (pop root-ids-300)]
         :root-duplicate
         [products-300 (assoc root-ids-300 middle (first root-ids-300))]
         :root-first-id
         [products-300 (substitute-root-id root-ids-300 0 :first)]
         :root-middle-id
         [products-300 (substitute-root-id root-ids-300 middle :middle)]
         :root-last-id
         [products-300 (substitute-root-id root-ids-300 299 :last)]}]
    (is (= :gravity/sh04-resolved-syntax-stream-v2
           (:domain baseline-300)))
    (is (= 300 (:resolved-product-count baseline-300)))
    (is (= 19 (count chunks-300)))
    (is (= 19 (count root-chunks-300)))
    (is (= 12 (:item-count (last chunks-300))))
    (is (= 12 (:item-count (last root-chunks-300))))
    (is (= (vec (range 300))
           (mapv :ordinal (mapcat :items chunks-300))))
    (is (= root-ids-300
           (mapv :id (mapcat :items root-chunks-300))))
    (is (= :gravity/sh04-resolved-syntax-stream-v2
           (:domain baseline-2048)))
    (is (= 2048 (:resolved-product-count baseline-2048)))
    (is (= 128 (count chunks-2048)))
    (is (= 128 (count root-chunks-2048)))
    (is (= (vec (range 128)) (mapv :chunk-index chunks-2048)))
    (is (= (mapv #(* 16 %) (range 128))
           (mapv :start-ordinal chunks-2048)))
    (is (every? #(= 16 (:item-count %)) chunks-2048))
    (is (every? #(= 16 (:item-count %)) root-chunks-2048))
    (is (= (vec (range 2048))
           (mapv :ordinal (mapcat :items chunks-2048))))
    (is (= root-ids-2048
           (mapv :id (mapcat :items root-chunks-2048))))
    (let [{:keys [nodes maximum-depth maximum-width scalar-bytes
                  maximum-scalar-bytes maximum-integer-bits]}
          (:stats baseline-2048-record)]
      (is (<= nodes 65536))
      (is (<= maximum-depth 64))
      (is (<= maximum-width 128))
      (is (<= scalar-bytes (* 8 1024 1024)))
      (is (<= maximum-scalar-bytes 65536))
      (is (<= maximum-integer-bits 256)))
    (is (= baseline-2048-digest
           (bootstrap/p15-s23-c6c10-canonical-digest
            "<sh04-stream-v2-identity-test-2048>"
            (preimage products-2048 root-ids-2048 2048))))
    (doseq [[label [candidate-products candidate-root-ids]] candidates]
      (is (not= baseline-300-digest
                (bootstrap/p15-s23-c6c10-canonical-digest
                 "<sh04-stream-v2-identity-test-300>"
                 (preimage candidate-products candidate-root-ids
                           (count candidate-products))))
          (name label)))))

(deftest sh04-gravity-rejects-each-structural-scenario-with-stable-rules
  (let [descriptor
        @accepted-descriptor]
    (doseq [basename (sort rejected-fixtures)]
      (let [gravity-path (fixture-path "rejected" basename ".gravity")
            qst-path (fixture-path "rejected" basename ".qst")
            gravity-descriptor
            (-> descriptor
                (assoc-in [:span :source] gravity-path)
                (assoc-in [:origin 0 :span :source] gravity-path))
            qst-descriptor
            (-> descriptor
                (assoc-in [:span :source] qst-path)
                (assoc-in [:origin 0 :span :source] qst-path))
            gravity-scenario
            (get @rejected-scenarios basename)
            qst-scenario
            (get @rejected-qst-scenarios basename)
            gravity-diagnostic-descriptor
            (if (= :build (:phase gravity-scenario))
              (assoc-in gravity-descriptor
                        (:path gravity-scenario) (:value gravity-scenario))
              gravity-descriptor)
            qst-diagnostic-descriptor
            (if (= :build (:phase qst-scenario))
              (assoc-in qst-descriptor
                        (:path qst-scenario) (:value qst-scenario))
              qst-descriptor)
            gravity-invocation
            (try
              {:result (scenario-result gravity-descriptor gravity-scenario)}
              (catch Throwable error
                {:host-error (.getName (class error))
                 :message (.getMessage error)}))
            qst-invocation
            (try
              {:result (scenario-result qst-descriptor qst-scenario)}
              (catch Throwable error
                {:host-error (.getName (class error))
                 :message (.getMessage error)}))
            gravity-result (:result gravity-invocation)
            qst-result (:result qst-invocation)
            gravity-diagnostic (first (:diagnostics gravity-result))
            qst-diagnostic (first (:diagnostics qst-result))
            expected-rule (:expected-rule gravity-scenario)]
        (is (nil? (:host-error gravity-invocation)) basename)
        (is (nil? (:host-error qst-invocation)) basename)
        (is (= gravity-scenario qst-scenario) basename)
        (is (contains? #{:rejected :failed} (:status gravity-result))
            basename)
        (is (= (:status gravity-result) (:status qst-result)) basename)
        (is (= expected-rule (result-rule gravity-result)) basename)
        (is (= expected-rule (result-rule qst-result)) basename)
        (is (= :gravity/diagnostic-template
               (:artifact gravity-diagnostic))
            basename)
        (is (= :gravity/diagnostic-template (:artifact qst-diagnostic))
            basename)
        (is (diagnostic-template? gravity-diagnostic)
            basename)
        (is (diagnostic-template? qst-diagnostic)
            basename)
        (is (= gravity-path (get-in gravity-diagnostic [:primary :span :source]))
            basename)
        (is (= qst-path (get-in qst-diagnostic [:primary :span :source]))
            basename)
        (doseq [[diagnostic expected-descriptor]
                [[gravity-diagnostic gravity-diagnostic-descriptor]
                 [qst-diagnostic qst-diagnostic-descriptor]]]
          (is (= (get-in expected-descriptor [:form :kind])
                 (get-in diagnostic [:facts :form-kind]))
              basename)
          (is (= (:phase expected-descriptor)
                 (get-in diagnostic [:facts :phase]))
              basename)
          (is (= (get-in expected-descriptor [:origin 0 :producer])
                 (get-in diagnostic [:facts :producer]))
              basename)
          (is (= (:hygiene expected-descriptor)
                 (get-in diagnostic [:facts :hygiene-summary]))
              basename)
          (is (= (get-in diagnostic [:facts :form-kind])
                 (get-in diagnostic [:primary :form-kind]))
              basename)
          (is (= (get-in diagnostic [:facts :phase])
                 (get-in diagnostic [:primary :phase]))
              basename)
          (is (= (get-in diagnostic [:facts :producer])
                 (get-in diagnostic [:primary :producer]))
              basename)
          (is (= (get-in diagnostic [:facts :hygiene-summary])
                 (get-in diagnostic [:primary :hygiene-summary]))
              basename))
        (is (= true
               (get-in gravity-result
                       [:containment :downstream-artifacts-forbidden]))
            basename)
        (is (= true
               (get-in qst-result
                       [:containment :downstream-artifacts-forbidden]))
            basename)))))

(deftest sh04-graph-validation-is-bounded-and-fails-closed
  (let [descriptor
        @accepted-descriptor
        base-build (build-and-resolve! descriptor)
        base (:syntax base-build)
        base (assoc base :syntax-id (stable-test-id :base 0))
        base-product (:product base-build)
        synthetic-product
        (fn [index]
          (let [syntax-id (stable-test-id :node index)]
            {:syntax-object (assoc (:syntax-object base-product)
                                   :syntax-id syntax-id)
             :digest-requests (:digest-requests base-product)
             :resolved-digests
             [(first (:resolved-digests base-product)) syntax-id]}))
        products-2049 (mapv synthetic-product (range 2049))
        syntaxes-2049 (mapv :syntax-object products-2049)
        exact-nodes (subvec syntaxes-2049 0 2048)
        accepted-300 (subvec syntaxes-2049 0 300)
        over-nodes syntaxes-2049
        exact-products (subvec products-2049 0 2048)
        accepted-products-300 (subvec products-2049 0 300)
        exact-root-ids (mapv :syntax-id exact-nodes)
        accepted-root-ids-300 (mapv :syntax-id accepted-300)
        over-root-ids (mapv :syntax-id over-nodes)
        chain-64 (reference-chain base 64)
        chain-65 (reference-chain base 65)
        dangling-id (stable-test-id :dangling 0)
        generated (generated-object base 1 (:syntax-id base))
        dangling
        (-> generated
            (assoc-in [:origin 0 :input-syntax-ids] [dangling-id])
            (assoc :prior-syntax-ids [dangling-id]))
        duplicate-id (assoc generated :syntax-id (:syntax-id base))
        self-reference (assoc base :prior-syntax-ids [(:syntax-id base)])
        unresolved (assoc base :syntax-id {:digest-ref 1})
        id-a (stable-test-id :cycle 0)
        id-b (stable-test-id :cycle 1)
        cycle-a
        (-> (generated-object base 10 id-b)
            (assoc :syntax-id id-a)
            (assoc-in [:origin 0 :input-syntax-ids] [id-b])
            (assoc :prior-syntax-ids [id-b]))
        cycle-b
        (-> (generated-object base 11 id-a)
            (assoc :syntax-id id-b)
            (assoc-in [:origin 0 :input-syntax-ids] [id-a])
            (assoc :prior-syntax-ids [id-a]))
        cases
        [[:exact-depth chain-64 :passed nil]
         [:over-depth chain-65 :failed "C3-ORIGIN"]
         [:accepted-300 accepted-300 :passed nil]
         [:exact-nodes exact-nodes :passed nil]
         [:over-nodes over-nodes :failed "C3-SHAPE"]
         [:dangling [base dangling] :failed "C3-ORIGIN"]
         [:duplicate-id [base duplicate-id] :failed "C3-ID"]
         [:self-reference [self-reference] :failed "C3-ORIGIN"]
         [:unresolved-id [unresolved] :failed "C3-ID"]
         [:two-node-cycle [cycle-a cycle-b] :failed "C3-ORIGIN"]]
        bounds (invoke-syntax 'c3-bounds-record [])
        stream-300
        (invoke-syntax
         'c3-syntax-stream-build-template
         [accepted-products-300
          (:reader-binding descriptor)
          (:reader-source-revision descriptor)
          accepted-root-ids-300])
        stream-at-limit
        (invoke-syntax
         'c3-syntax-stream-build-template
         [exact-products
          (:reader-binding descriptor)
          (:reader-source-revision descriptor)
          exact-root-ids])
        stream-over-limit
        (invoke-syntax
         'c3-syntax-stream-build-template
         [products-2049
          (:reader-binding descriptor)
          (:reader-source-revision descriptor)
          over-root-ids])]
    (is (= 2048 (:maximum-syntax-objects bounds)))
    (doseq [[label objects expected-status expected-rule] cases]
      (let [invocation
            (captured-invocation 'c3-syntax-graph-verify-template [objects])
            result (:result invocation)]
        (is (nil? (:host-error invocation)) (str label " " invocation))
        (is (= expected-status (:status result)) (str label " " result))
        (is (= expected-rule (result-rule result)) (str label " " result))
        (is (= (= expected-status :failed)
               (get-in result
                       [:containment :downstream-artifacts-forbidden]))
            (str label " " result))))
    (is (= 64
           (:maximum-reference-depth
            (invoke-syntax 'c3-syntax-graph-verify-template [chain-64]))))
    (is (= 300
           (:object-count
            (invoke-syntax 'c3-syntax-graph-verify-template
                           [accepted-300]))))
    (is (= 2048
           (:object-count
            (invoke-syntax 'c3-syntax-graph-verify-template [exact-nodes]))))
    (doseq [[label result expected-status]
            [[:stream-300 stream-300 :accepted]
             [:stream-at-limit stream-at-limit :accepted]
             [:stream-over-limit stream-over-limit :rejected]]]
      (is (= expected-status (:status result)) (str label " " result))
      (is (= (= expected-status :rejected)
             (get-in result
                     [:containment :downstream-artifacts-forbidden]))
          (str label " " result)))
    (is (= "C3-SHAPE" (result-rule stream-over-limit)))
    (is (= :syntax-node-limit
           (get-in stream-over-limit [:diagnostics 0 :facts :reason])))
    (let [graph-over
          (invoke-syntax 'c3-syntax-graph-verify-template [over-nodes])]
      (is (= "C3-SHAPE" (result-rule graph-over)))
      (is (= :syntax-node-limit
             (get-in graph-over [:diagnostics 0 :facts :reason]))))))

(deftest sh04-width-bounds-accept-the-limit-and-reject-the-next-value
  (let [descriptor
        @accepted-descriptor
        marks-64 (mapv #(keyword (str "mark-" %)) (range 64))
        marks-65 (conj marks-64 :mark-64)
        captures-32 (mapv capture-record (range 32))
        captures-33 (conj captures-32 (capture-record 32))
        metadata-64
        (into {} (map (fn [index]
                        [(keyword (str "meta-" index)) index])
                      (range 64)))
        metadata-65 (assoc metadata-64 :meta-64 64)
        facts-64 (mapv fact-record (range 64))
        facts-65 (conj facts-64 (fact-record 64))
        origin (first (:origin descriptor))
        origins-32 (vec (repeat 32 origin))
        origins-33 (conj origins-32 origin)
        cases
        [[:marks-at-limit
          (assoc-in descriptor [:hygiene :marks] marks-64)
          :accepted nil]
         [:marks-over-limit
          (assoc-in descriptor [:hygiene :marks] marks-65)
          :rejected "C3-HYGIENE"]
         [:captures-at-limit
          (assoc-in descriptor [:hygiene :captures] captures-32)
          :accepted nil]
         [:captures-over-limit
          (assoc-in descriptor [:hygiene :captures] captures-33)
          :rejected "C3-CAPTURE"]
         [:metadata-at-limit
          (assoc descriptor :metadata metadata-64)
          :accepted nil]
         [:metadata-over-limit
          (assoc descriptor :metadata metadata-65)
          :rejected "C3-METADATA"]
         [:facts-at-limit
          (assoc descriptor :facts facts-64)
          :accepted nil]
         [:facts-over-limit
          (assoc descriptor :facts facts-65)
          :rejected "C3-FACT-STALE"]
         [:origins-at-limit
          (assoc descriptor :origin origins-32)
          :accepted nil]
         [:origins-over-limit
          (assoc descriptor :origin origins-33)
          :rejected "C3-ORIGIN"]]]
    (doseq [[label candidate expected-status expected-rule] cases]
      (let [invocation
            (captured-invocation 'c3-syntax-build-template [candidate])
            result (:result invocation)]
        (is (nil? (:host-error invocation)) (str label " " invocation))
        (is (= expected-status (:status result)) (str label " " result))
        (is (= expected-rule (result-rule result)) (str label " " result))
        (is (= (= expected-status :rejected)
               (get-in result
                       [:containment :downstream-artifacts-forbidden]))
            (str label " " result))))))

(deftest sh04-public-c3-preserves-exact-spans-metadata-and-pair-identity
  (let [artifacts @public-artifacts]
    (doseq [[extension {:keys [path bytes artifact]}] artifacts]
      (is (= :gravity/stage0-c3-syntax-object-artifact (:kind artifact))
          extension)
      (is (= :passed
             (get-in artifact [:syntax-verification-report :status]))
          extension)
      (let [source-syntax
            (remove #(= :generated-form (get-in % [:form :kind]))
                    (:syntax-object-stream artifact))]
        (doseq [syntax source-syntax]
          (let [span (get-in syntax [:span :primary])
                byte-start (:byte-start span)
                byte-end (:byte-end span)
                raw (get-in syntax [:form :raw])
                sliced
                (String.
                 (java.util.Arrays/copyOfRange
                  bytes (int byte-start) (int byte-end))
                 java.nio.charset.StandardCharsets/UTF_8)]
            (is (= path (:source span)) extension)
            (is (<= 0 byte-start byte-end (alength bytes)) extension)
            (is (= raw sliced) (str extension " " (:syntax/id syntax)))
            (is (pos-int? (:line-start span)) extension)
            (is (pos-int? (:column-start span)) extension)
            (is (pos-int? (:line-end span)) extension)
            (is (pos-int? (:column-end span)) extension)))
        (let [unicode-syntax
              (first (filter #(str/includes? (get-in % [:form :raw])
                                             "supplementary")
                             source-syntax))
              raw (get-in unicode-syntax [:form :raw])
              byte-count
              (alength (.getBytes raw
                                 java.nio.charset.StandardCharsets/UTF_8))
              scalar-count (.codePointCount raw 0 (.length raw))
              span (get-in unicode-syntax [:span :primary])]
          (is (some? unicode-syntax) extension)
          (is (> byte-count scalar-count) extension)
          (is (= byte-count (- (:byte-end span) (:byte-start span)))
              extension)))
      (is (some #(= {:sh04/fixture true
                     :doc "Metadata survives C3 construction."}
                    (:metadata %))
                (:syntax-object-stream artifact))
          extension)
      (is (some #(seq (get-in % [:hygiene :marks]))
                (:syntax-object-stream artifact))
          extension)
      (is (some #(seq (get-in % [:hygiene :captures]))
                (:syntax-object-stream artifact))
          extension))
    (let [gravity (get-in artifacts [".gravity" :artifact])
          qst (get-in artifacts [".qst" :artifact])]
      (is (= (:artifact-id gravity) (:artifact-id qst)))
      (is (= (mapv :syntax/id (:syntax-object-stream gravity))
             (mapv :syntax/id (:syntax-object-stream qst))))
      (is (= (get-in gravity
                     [:gravity-syntax-boundary :resolved-syntax-result
                      :artifact-id])
             (get-in qst
                     [:gravity-syntax-boundary :resolved-syntax-result
                      :artifact-id])))
      (is (= (get-in gravity
                     [:gravity-syntax-boundary :reader-semantic-binding])
             (get-in qst
                     [:gravity-syntax-boundary :reader-semantic-binding])))
      (is (= (get-in gravity
                     [:gravity-syntax-boundary :reader-source-revision])
             (get-in qst
                     [:gravity-syntax-boundary :reader-source-revision])))
      (is (= (get-in gravity
                     [:gravity-syntax-boundary
                      :gravity-syntax-serialization :payload-id-request])
             (get-in qst
                     [:gravity-syntax-boundary
                      :gravity-syntax-serialization
                      :payload-id-request])))
      (is (= (get-in gravity [:syntax-serialization-fixture :hash])
             (get-in qst [:syntax-serialization-fixture :hash])))
      (is (= (get-in gravity
                     [:gravity-syntax-boundary :authenticated-envelope
                      :semantic-envelope-id])
             (get-in qst
                     [:gravity-syntax-boundary :authenticated-envelope
                      :semantic-envelope-id])))
      (is (not=
           (get-in gravity
                   [:gravity-syntax-boundary :authenticated-envelope
                    :provenance-binding-id])
           (get-in qst
                   [:gravity-syntax-boundary :authenticated-envelope
                    :provenance-binding-id]))))))

(deftest sh04-c3-preserves-an-ordinary-digest-reference-shaped-map
  (let [source-text
        (str "(ns sh04.digest-reference-map"
             " (:profile :meta) (:target :jvm))\n"
             "(defn preserve-digest-reference-map [] {:digest-ref 0})\n")
        body-form
        '(defn preserve-digest-reference-map [] {:digest-ref 0})
        gravity-path "/sh04/exact-map/source.gravity"
        qst-path "/sh04/exact-map/source.qst"
        gravity-first
        (bootstrap/compiler-c3-syntax-source-artifact
         gravity-path source-text)
        gravity-second
        (bootstrap/compiler-c3-syntax-source-artifact
         gravity-path source-text)
        qst-artifact
        (bootstrap/compiler-c3-syntax-source-artifact qst-path source-text)]
    (doseq [[extension artifact]
            [[".gravity" gravity-first] [".qst" qst-artifact]]]
      (is (= :gravity/stage0-c3-syntax-object-artifact (:kind artifact))
          extension)
      (is (= body-form
             (get-in artifact [:c2-reader-artifact
                               :parsed-semantic-values 1]))
          extension)
      (is (= body-form
             (get-in artifact [:syntax-object-stream 1 :form :value]))
          extension)
      (is (= {:digest-ref 0}
             (last (get-in artifact
                           [:syntax-object-stream 1 :form :value])))
          extension)
      (is (= :passed
             (get-in artifact [:syntax-verification-report :status]))
          extension)
      (is (= :complete
             (get-in artifact [:capability-based-proof :status]))
          extension)
      (is (sha256-id? (:artifact-id artifact)) extension)
      (is (sha256-id?
           (get-in artifact
                   [:gravity-syntax-boundary :resolved-syntax-result
                    :artifact-id]))
          extension))
    (is (= gravity-first gravity-second))
    (is (= (:artifact-id gravity-first) (:artifact-id qst-artifact)))
    (is (= (get-in gravity-first
                   [:gravity-syntax-boundary :resolved-syntax-result
                    :artifact-id])
           (get-in qst-artifact
                   [:gravity-syntax-boundary :resolved-syntax-result
                    :artifact-id])))
    (is (= gravity-path
           (get-in gravity-first
                   [:gravity-syntax-boundary
                    :authenticated-envelope-descriptor
                    :actual-path-provenance :source-path])))
    (is (= qst-path
           (get-in qst-artifact
                   [:gravity-syntax-boundary
                    :authenticated-envelope-descriptor
                    :actual-path-provenance :source-path])))))

(deftest sh04-public-c3-is-checkout-path-neutral-with-actual-path-provenance
  (let [checkout-a
        (.toFile (java.nio.file.Files/createTempDirectory
                  "gravity-sh04-checkout-a-"
                  (make-array java.nio.file.attribute.FileAttribute 0)))
        checkout-b
        (.toFile (java.nio.file.Files/createTempDirectory
                  "gravity-sh04-checkout-b-"
                  (make-array java.nio.file.attribute.FileAttribute 0)))
        source-text
        (slurp (fixture-path "accepted" "source-syntax" ".gravity"))]
    (try
      (let [directory-a (io/file checkout-a "src")
            directory-b (io/file checkout-b "src")
            _ (.mkdirs directory-a)
            _ (.mkdirs directory-b)
            source-a (io/file directory-a "source-syntax.gravity")
            source-b (io/file directory-b "source-syntax.qst")
            _ (spit source-a source-text)
            _ (spit source-b source-text)
            path-a (.getCanonicalPath source-a)
            path-b (.getCanonicalPath source-b)
            artifact-a (bootstrap/compiler-c3-syntax-file-artifact path-a)
            artifact-b (bootstrap/compiler-c3-syntax-file-artifact path-b)
            boundary-a (:gravity-syntax-boundary artifact-a)
            boundary-b (:gravity-syntax-boundary artifact-b)
            descriptor-a (:authenticated-envelope-descriptor boundary-a)
            descriptor-b (:authenticated-envelope-descriptor boundary-b)]
        (is (not= path-a path-b))
        (is (= (:artifact-id artifact-a) (:artifact-id artifact-b)))
        (is (= (mapv :syntax/id (:syntax-object-stream artifact-a))
               (mapv :syntax/id (:syntax-object-stream artifact-b))))
        (is (= (get-in boundary-a [:resolved-syntax-result :artifact-id])
               (get-in boundary-b [:resolved-syntax-result :artifact-id])))
        (is (= (get-in boundary-a
                       [:gravity-syntax-serialization :payload-id-request])
               (get-in boundary-b
                       [:gravity-syntax-serialization :payload-id-request])))
        (is (= (:reader-semantic-binding boundary-a)
               (:reader-semantic-binding boundary-b)))
        (is (= (get-in boundary-a
                       [:authenticated-envelope :semantic-envelope-id])
               (get-in boundary-b
                       [:authenticated-envelope :semantic-envelope-id])))
        (is (not= (get-in boundary-a
                          [:authenticated-envelope :provenance-binding-id])
                  (get-in boundary-b
                          [:authenticated-envelope :provenance-binding-id])))
        (is (= path-a
               (get-in descriptor-a
                       [:actual-path-provenance :source-path])))
        (is (= path-b
               (get-in descriptor-b
                       [:actual-path-provenance :source-path])))
        (doseq [syntax (:syntax-object-stream artifact-a)]
          (when (not= :generated-form (get-in syntax [:form :kind]))
            (is (= path-a (get-in syntax [:span :primary :source])))))
        (doseq [syntax (:syntax-object-stream artifact-b)]
          (when (not= :generated-form (get-in syntax [:form :kind]))
            (is (= path-b (get-in syntax [:span :primary :source]))))))
      (finally
        (delete-tree! checkout-a)
        (delete-tree! checkout-b)))))

(deftest sh04-public-boundary-credits-only-the-gravity-syntax-authority
  (doseq [extension [".gravity" ".qst"]]
    (let [artifact (get-in @public-artifacts [extension :artifact])
          boundary (:gravity-syntax-boundary artifact)
          envelope (:authenticated-envelope boundary)
          result (:resolved-syntax-result boundary)
          facade (:uncredited-compatibility-facade boundary)]
      (is (= :SH-04 (:slice boundary)) extension)
      (is (= :gravity-source (:owner boundary)) extension)
      (is (= boundary-adapter-contract (:adapter-contract boundary))
          extension)
      (is (= false (:target-source-reread? boundary)) extension)
      (is (= true (:clojure-adapter-residual? boundary)) extension)
      (is (= false (:self-hosted? boundary)) extension)
      (is (map? (:plan-binding boundary)) extension)
      (is (= authoritative-result-kind (:artifact result)) extension)
      (is (= :accepted (:status result)) extension)
      (is (= :passed
             (get-in boundary [:resolved-stream-verification :status]))
          extension)
      (is (= :accepted
             (get-in boundary [:gravity-syntax-serialization :status]))
          extension)
      (is (= :accepted
             (get-in boundary [:gravity-syntax-deserialization :status]))
          extension)
      (is (= (get-in boundary
                     [:gravity-syntax-serialization :semantic-payload])
             (get-in boundary
                     [:gravity-syntax-deserialization :semantic-payload]))
          extension)
      (is (= :gravity/sh04-hygiene-context-map
             (get-in result [:hygiene-context-map :artifact]))
          extension)
      (is (= :gravity/sh04-metadata-ledger
             (get-in result [:metadata-ledger :artifact]))
          extension)
      (is (= :gravity/sh04-fact-invalidation-ledger
             (get-in result [:fact-invalidation-ledger :artifact]))
          extension)
      (is (= :gravity/sh04-origin-chain-graph
             (get-in result [:origin-chain-graph :artifact]))
          extension)
      (is (= :gravity-source
             (get-in result [:ownership-product :owner]))
          extension)
      (is (= 'gravity.bootstrap.syntax
             (get-in result [:ownership-product :module]))
          extension)
      (is (= (mapv :ownership (:syntax-object-stream result))
             (get-in result
                     [:ownership-product :syntax-ownership]))
          extension)
      (is (= (:reader-semantic-binding boundary)
             (:reader-binding result))
          extension)
      (is (= (:reader-source-revision boundary)
             (:reader-source-revision result))
          extension)
      (is (map? (:reader-authentication-provenance boundary)) extension)
      (is (= :gravity/sh02-stage-authenticated-envelope
             (:artifact envelope))
          extension)
      (is (= :accepted (:status envelope)) extension)
      (is (= :c3-syntax (:stage envelope)) extension)
      (is (= sealed-artifact-kind
             (get-in envelope [:sealed-artifact :artifact-kind]))
          extension)
      (is (= :gravity-source (:semantic-authority envelope)) extension)
      (is (= :template-replay-passed
             (get-in envelope [:gravity-template-replay :status]))
          extension)
      (is (= uncredited-facade
             (select-keys facade (keys uncredited-facade)))
          extension)
      (is (every? false?
                  (map #(get facade %)
                       [:authentication-credit? :authoritative-result?
                        :self-hosting-credit? :seed-retirement-credit?
                        :release-credit?]))
          extension))))

(deftest sh04-public-authenticity-check-rejects-binding-substitution-and-replay
  (let [c2-artifact (:c2-artifact @public-gravity-artifact)
        c3-artifact (:artifact @public-gravity-artifact)
        syntax-stream (:syntax-object-stream c3-artifact)
        boundary (:gravity-syntax-boundary c3-artifact)
        alternate-binding-id (stable-test-id :public-binding-alternate 0)
        alternate-source-id (stable-test-id :public-source-alternate 0)
        missing-binding (dissoc boundary :reader-semantic-binding)
        substituted-binding
        (assoc-in boundary
                  [:reader-semantic-binding :token-stream-id]
                  (stable-test-id :public-token-substitution 0))
        replayed-binding
        (-> boundary
            (assoc-in [:reader-semantic-binding :semantic-source-id]
                      alternate-source-id)
            (assoc-in [:reader-semantic-binding :semantic-binding-id]
                      alternate-binding-id)
            (assoc-in [:reader-source-revision :semantic-binding-id]
                      alternate-binding-id))
        authentic?
        (fn [candidate]
          (bootstrap/c3-syntax-stream-reader-products-authentic?
           syntax-stream c2-artifact candidate))]
    (is (true? (authentic? boundary)))
    (is (false? (authentic? missing-binding)))
    (is (false? (authentic? substituted-binding)))
    (is (false? (authentic? replayed-binding)))
    (is (= :accepted
           (get-in boundary [:authenticated-envelope :status])))
    (is (= :passed
           (get-in boundary [:resolved-stream-verification :status])))))

(deftest sh04-public-authenticity-rejects-descriptor-envelope-substitution
  (let [source-path (:path @public-gravity-artifact)
        c2-artifact (:c2-artifact @public-gravity-artifact)
        artifact (:artifact @public-gravity-artifact)
        syntax-stream (:syntax-object-stream artifact)
        boundary (:gravity-syntax-boundary artifact)
        descriptor (:authenticated-envelope-descriptor boundary)
        substituted-descriptor
        (assoc-in descriptor [:actual-path-provenance :source-path]
                  (str source-path ".substituted"))
        substituted-envelope
        (bootstrap/p15-s23-stage2-sh02-descriptor-envelope
         :c3-syntax sealed-artifact-kind substituted-descriptor source-path)
        substituted-boundary
        (assoc boundary
               :authenticated-envelope-descriptor substituted-descriptor
               :authenticated-envelope substituted-envelope)
        substituted-artifact
        (assoc artifact :gravity-syntax-boundary substituted-boundary)
        authentic?
        (bootstrap/c3-syntax-stream-reader-products-authentic?
         syntax-stream c2-artifact substituted-boundary)
        proof (bootstrap/c3-syntax-capability-proof substituted-artifact)]
    (is (= :accepted (:status substituted-envelope)))
    (is (not= descriptor substituted-descriptor))
    (is (not= (:authenticated-envelope boundary) substituted-envelope))
    (is (false? authentic?))
    (is (= :failed (:status proof)))
    (is (false? (:reader-products-authentic? proof)))))

(deftest sh04-p15-c3-requires-fresh-authenticated-sh03-c2-equality
  (let [source-path
        (fixture-path "accepted" "source-syntax" ".gravity")
        source-text "1"
        c2-artifact
        (bootstrap/p15-s23-source-syntax-c2-artifact
         source-path source-text)
        c3-artifact
        (bootstrap/p15-s23-source-syntax-c3-artifact
         source-path c2-artifact)
        original-envelope-id
        (get-in c2-artifact
                [:gravity-reader-boundary :authenticated-envelope
                 :semantic-envelope-id])
        alternate-envelope-id
        (stable-test-id :p15-sh03-envelope-substitution 0)
        substituted
        (assoc-in c2-artifact
                  [:gravity-reader-boundary :authenticated-envelope
                   :semantic-envelope-id]
                  alternate-envelope-id)
        diagnostic
        (try
          (bootstrap/p15-s23-source-syntax-c3-artifact
           source-path substituted)
          nil
          (catch InterruptedException interrupted
            (.interrupt (Thread/currentThread))
            (throw interrupted))
          (catch clojure.lang.ExceptionInfo error
            (ex-data error)))]
    (is (re-matches #"sha256:[0-9a-f]{64}" original-envelope-id))
    (is (re-matches #"sha256:[0-9a-f]{64}" alternate-envelope-id))
    (is (not= original-envelope-id alternate-envelope-id))
    (is (= c2-artifact
           (assoc-in substituted
                     [:gravity-reader-boundary :authenticated-envelope
                      :semantic-envelope-id]
                     original-envelope-id)))
    (is (= :gravity/stage0-c3-syntax-object-artifact (:kind c3-artifact)))
    (is (= :passed (get-in c3-artifact
                           [:syntax-verification-report :status])))
    (is (= true
           (get-in c3-artifact
                   [:p15-compatibility-boundary
                    :exact-precomputed-c2-consumed?])))
    (is (= (:artifact-id c2-artifact)
           (get-in c3-artifact
                   [:p15-compatibility-boundary
                    :supplied-c2-artifact-id])))
    (is (= (:artifact-id c2-artifact)
           (get-in c3-artifact
                   [:p15-compatibility-boundary
                    :authoritative-c2-artifact-id])))
    (is (= "C3-FACT-STALE" (:id diagnostic)) diagnostic)
    (is (= :c3-syntax-object (:diagnostic-family diagnostic)) diagnostic)
    (is (= :syntax-object-model (:stage diagnostic)) diagnostic)
    (is (= source-path (get-in diagnostic [:source-span :source])) diagnostic)
    (is (= [:fresh-authenticated-sh03-c2-artifact-equality]
           (:missing-fields diagnostic))
        diagnostic)))

(deftest sh04-emitted-c3-provenance-alteration-invalidates-proof-not-identity
  (let [artifact (:artifact @public-gravity-artifact)
        qst-artifact (:artifact @public-qst-artifact)
        original-proof (bootstrap/c3-syntax-capability-proof artifact)
        sh03-authentication
        (get-in artifact [:c2-reader-artifact :sh03-reader-authentication])
        provenance
        (get-in artifact
                [:gravity-syntax-boundary
                 :reader-authentication-provenance])
        reader-binding
        (get-in artifact
                [:gravity-syntax-boundary :reader-semantic-binding])
        concrete-reader-result-id (:reader-result-id sh03-authentication)
        concrete-envelope-id (:semantic-envelope-id sh03-authentication)
        concrete-envelope (:actual-sh03-authenticated-envelope provenance)
        concrete-envelope-descriptor
        (:actual-sh03-envelope-descriptor provenance)
        concrete-envelope-verification
        (bootstrap/p15-s23-stage2-sh02-descriptor-envelope-verify!
         concrete-envelope :c2-reader :gravity/sh03-reader-products
         concrete-envelope-descriptor (:path @public-gravity-artifact))
        alternate-envelope-id
        (stable-test-id :emitted-c3-lineage-alteration 0)
        altered
        (-> artifact
            (assoc-in [:c2-reader-artifact :sh03-reader-authentication
                       :semantic-envelope-id]
                      alternate-envelope-id)
            (assoc-in [:gravity-syntax-boundary
                       :reader-authentication-provenance
                       :actual-sh03-semantic-envelope-id]
                      alternate-envelope-id))
        altered-proof (bootstrap/c3-syntax-capability-proof altered)
        original-recomputed-id (bootstrap/c3-artifact-id artifact)
        altered-recomputed-id (bootstrap/c3-artifact-id altered)]
    (is (= :complete (:status original-proof)))
    (is (true? (:reader-products-authentic? original-proof)))
    (is (true? (:syntax-verifier-current? original-proof)))
    (is (= (:artifact-id artifact) original-recomputed-id))
    (is (re-matches #"sha256:[0-9a-f]{64}" concrete-reader-result-id))
    (is (re-matches #"sha256:[0-9a-f]{64}" concrete-envelope-id))
    (is (not= concrete-envelope-id alternate-envelope-id))
    (is (= :accepted (:status concrete-envelope)))
    (is (= :passed concrete-envelope-verification))
    (is (= concrete-envelope-id (:semantic-envelope-id concrete-envelope)))
    (is (= concrete-reader-result-id
           (:actual-sh03-reader-result-id provenance)))
    (is (= concrete-envelope-id
           (:actual-sh03-semantic-envelope-id provenance)))
    (is (= reader-binding
           (get-in qst-artifact
                   [:gravity-syntax-boundary :reader-semantic-binding])))
    (is (re-matches #"sha256:[0-9a-f]{64}"
                    (:reader-result-id reader-binding)))
    (is (re-matches #"sha256:[0-9a-f]{64}"
                    (:authenticated-envelope-id reader-binding)))
    (is (= alternate-envelope-id
           (get-in altered
                   [:c2-reader-artifact :sh03-reader-authentication
                    :semantic-envelope-id])))
    (is (= alternate-envelope-id
           (get-in altered
                   [:gravity-syntax-boundary
                    :reader-authentication-provenance
                    :actual-sh03-semantic-envelope-id])))
    (is (false? (:reader-products-authentic? altered-proof)))
    (is (false? (:syntax-verifier-current? altered-proof)))
    (is (= :failed (:status altered-proof)))
    (is (= original-recomputed-id altered-recomputed-id))))

(deftest sh04-emitted-c3-rejects-cross-source-sh03-envelope-replacement
  (let [source-path
        (fixture-path "accepted" "source-syntax" ".gravity")
        c2-b (bootstrap/compiler-c2-reader-source-artifact source-path "2")
        c3-a (bootstrap/compiler-c3-syntax-source-artifact source-path "1")
        original-proof (bootstrap/c3-syntax-capability-proof c3-a)
        boundary-b (:gravity-reader-boundary c2-b)
        envelope-b (:authenticated-envelope boundary-b)
        descriptor-b (:authenticated-envelope-descriptor boundary-b)
        sh03-authentication-b
        {:reader-result-id
         (get-in boundary-b
                 [:resolved-reader-result :incremental-reader-hashes
                  :reader-result])
         :semantic-envelope-id (:semantic-envelope-id envelope-b)
         :provenance-binding-id (:provenance-binding-id envelope-b)}
        provenance-b
        {:actual-sh03-reader-result-id
         (:reader-result-id sh03-authentication-b)
         :actual-sh03-semantic-envelope-id
         (:semantic-envelope-id sh03-authentication-b)
         :actual-sh03-provenance-binding-id
         (:provenance-binding-id sh03-authentication-b)
         :actual-sh03-authenticated-envelope envelope-b
         :actual-sh03-envelope-descriptor descriptor-b
         :actual-sh03-semantic-product-binding
         (bootstrap/sh04-syntax-descriptor-sh03-product-binding
          descriptor-b)}
        replaced
        (-> c3-a
            (assoc-in [:c2-reader-artifact :sh03-reader-authentication]
                      sh03-authentication-b)
            (update-in [:gravity-syntax-boundary
                        :reader-authentication-provenance]
                       merge provenance-b))
        replaced-proof (bootstrap/c3-syntax-capability-proof replaced)
        original-recomputed-id (bootstrap/c3-artifact-id c3-a)
        replaced-recomputed-id (bootstrap/c3-artifact-id replaced)]
    (is (= :complete (:status original-proof)))
    (is (true? (:reader-products-authentic? original-proof)))
    (is (true? (:syntax-verifier-current? original-proof)))
    (is (not= (get-in c3-a
                      [:c2-reader-artifact :sh03-reader-authentication])
              sh03-authentication-b))
    (is (= sh03-authentication-b
           (get-in replaced
                   [:c2-reader-artifact :sh03-reader-authentication])))
    (is (= envelope-b
           (get-in replaced
                   [:gravity-syntax-boundary
                    :reader-authentication-provenance
                    :actual-sh03-authenticated-envelope])))
    (is (= descriptor-b
           (get-in replaced
                   [:gravity-syntax-boundary
                    :reader-authentication-provenance
                    :actual-sh03-envelope-descriptor])))
    (is (= (:actual-sh03-semantic-product-binding provenance-b)
           (get-in replaced
                   [:gravity-syntax-boundary
                    :reader-authentication-provenance
                    :actual-sh03-semantic-product-binding])))
    (is (false? (:reader-products-authentic? replaced-proof)))
    (is (false? (:syntax-verifier-current? replaced-proof)))
    (is (= :failed (:status replaced-proof)))
    (is (= original-recomputed-id replaced-recomputed-id))))

(deftest sh04-capability-proof-rejects-top-level-gravity-product-substitution
  (let [artifact (:artifact @public-gravity-artifact)
        boundary (:gravity-syntax-boundary artifact)
        valid-proof (:capability-based-proof artifact)
        ledger-mutated
        (assoc-in artifact
                  [:gravity-metadata-ledger :entries 0 :action]
                  :substituted)
        ownership-mutated
        (assoc-in artifact
                  [:gravity-syntax-ownership-product
                   :syntax-ownership 0 :form-id]
                  :substituted-form)
        ledger-proof (bootstrap/c3-syntax-capability-proof ledger-mutated)
        ownership-proof
        (bootstrap/c3-syntax-capability-proof ownership-mutated)]
    (is (= :complete (:status valid-proof)))
    (is (true? (:gravity-authoritative-products-current? valid-proof)))
    (is (= boundary (:gravity-syntax-boundary ledger-mutated)))
    (is (= boundary (:gravity-syntax-boundary ownership-mutated)))
    (is (= :failed (:status ledger-proof)))
    (is (false?
         (:gravity-authoritative-products-current? ledger-proof)))
    (is (= :failed (:status ownership-proof)))
    (is (false?
         (:gravity-authoritative-products-current? ownership-proof)))
    (is (not= (:gravity-metadata-ledger artifact)
              (:gravity-metadata-ledger ledger-mutated)))
    (is (not= (:gravity-syntax-ownership-product artifact)
              (:gravity-syntax-ownership-product ownership-mutated)))))
