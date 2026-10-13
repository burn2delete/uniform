(ns gravity.self-hosting.sh07-b51-v9-sh06-external-binding-seam-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [gravity.tools.sh07-b51-attempt32-checker-child :as child]
            [gravity.tools.sh07-b51-attempt32-checker-identity :as identity]
            [gravity.tools.sh07-b51-git-evidence :as git-evidence]
            [gravity.tools.sh07-b51-v9-codec :as codec]
            [gravity.bootstrap :as bootstrap]))

(defn- required-var [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "required SH06 lexical seam is absent"
                      {:symbol symbol}))))

(defn- invoke-private [symbol & arguments]
  (apply (required-var symbol) arguments))

(defn- fixture-git! [arguments input environment]
  (let [{:keys [exit out err]}
        (apply shell/sh (concat ["/usr/bin/git"] arguments
                               [:in (or input "")
                                :env (merge (into {} (System/getenv)) environment)]))]
    (when-not (zero? exit) (throw (ex-info err {:arguments arguments})))
    (str/trim out)))

(defn- bounded-source-text [spec]
  (let [ordinal (:module-ordinal spec)
        callees @(required-var 'p15-s23-sh07-b51-root8-external-callees)
        arities {'sh07-b51-resolve-digest-preimage 3
                 'sh07-b51-build-template 1 'sh07-b51-verify-template 3
                 'sh07-b51-resolve-template 3 'sh07-b51-verify-resolved 4
                 'sh07-b51-build-final-artifact 7
                 'sh07-b51-build-independent-verifier-binding 6
                 'sh07-b51-finalize-rejection 1}
        definition (fn [name arity body]
                     (list 'defn name
                           (mapv #(symbol (str "p" %)) (range arity)) body))
        header (list 'ns (:module spec) (list :profile :meta)
                     (list :target :jvm) (list :exports (:exports spec))
                     (list :effects #{}) (list :capabilities #{})
                     (list :safety :safe))
        public (if (= ordinal 6)
                 [(definition 'sh07-b51-finalize-rejection 1
                              (cons 'do (map #(list % nil) callees)))]
                 (mapv #(definition % (arities %) 0) (:exports spec)))
        provider (fn [n] (symbol (str "fixture-provider-" n)))
        extra (if (< ordinal 4)
                (concat
                  [(definition (provider ordinal) 0 0)]
                  (when (zero? ordinal)
                    (mapv #(definition % 1 0)
                          (remove #{'sh07-b51-build-template} callees)))
                  (mapv (fn [n]
                          (definition (symbol (str "fixture-caller-" n)) 0
                            (cons 'do (repeat (if (= n 11) 15 32)
                                              (list (provider (mod (inc ordinal) 4)))))))
                        (range 12))) [])]
    (str (str/join "\n" (map pr-str (concat [header] public extra))) "\n")))

(defn- bounded-content! []
  ;; Git object storage authenticates the fixture bytes; no checkout is edited.
  (let [index (java.nio.file.Files/createTempFile
               "sh07-handoff-index" ".tmp"
               (make-array java.nio.file.attribute.FileAttribute 0))
        environment {"GIT_INDEX_FILE" (str index)}
        base (fixture-git! ["rev-parse" "HEAD"] nil {})]
    (java.nio.file.Files/deleteIfExists index)
    (try
      (fixture-git! ["read-tree" base] nil environment)
      (doseq [spec bootstrap/p15-s23-sh07-b51-module-specs]
        (let [oid (fixture-git! ["hash-object" "-w" "--stdin"]
                               (bounded-source-text spec) environment)]
          (fixture-git! ["update-index" "--add" "--cacheinfo"
                         (str "100644," oid "," (:logical-source-path spec))]
                        nil environment)))
      (let [tree (fixture-git! ["write-tree"] nil environment)
            commit (fixture-git! ["commit-tree" tree "-p" base]
                     "Bounded source-handoff test; no implementation authority\n"
                     environment)]
        {:commit commit :tree tree})
      (finally (java.nio.file.Files/deleteIfExists index)))))

(defn- coherent-event-mutation [products ordinal]
  (let [events (assoc-in (get-in products [:source-read-receipt :events])
                         [ordinal :product-id] (codec/H [:hostile ordinal]))
        root (codec/H [:gravity/sh07-b51-source-event-root-v1 events])
        carrier (:sh06-authority-carrier products)
        event-ids (mapv :product-id (subvec events 17 24))
        link-id (codec/H
                  [:gravity/sh07-b51-sh06-link-authority-id-e2-v1
                   {:resolution-artifact-ids
                    (mapv :artifact-id (:resolution-artifacts carrier))
                    :semantic-projection-ids (:semantic-projection-ids carrier)
                    :event-product-ids event-ids
                    :manifest-id (get-in products [:m1 :manifest-id])
                    :resolution-plan-authority (:resolution-plan-authority carrier)
                    :external-binding-authorities
                    (:external-binding-authorities carrier)}])
        base (assoc (dissoc carrier :carrier-id)
                     :link-authority-event-product-ids event-ids
                     :link-authority-id link-id)
        carrier (assoc base :carrier-id
                       (codec/H [:gravity/sh07-b51-sh06-authority-carrier-id-e2-v1 base]))]
    (-> products
        (assoc-in [:source-read-receipt :events] events)
        (assoc-in [:source-read-receipt :event-root] root)
        (assoc-in [:source-semantic-carrier :source-events] events)
        (assoc-in [:source-semantic-carrier :c2-snapshot :event-root] root)
        (assoc :sh06-authority-carrier carrier))))

(defn- checker-source-preflight! [content]
  ;; Cache real C/T objects before session closure. This is envelope evidence,
  ;; not a PRE discharge or whole-object admission claim.
  (let [paths (distinct (concat
                         (filter #(str/ends-with? % ".clj") git-evidence/owned-c-paths)
                         [git-evidence/architecture-report-path
                          "bootstrap/clojure/src/gravity/c2_artifact_identity/canonical.clj"
                          "bootstrap/clojure/src/gravity/digest.clj"]))
        objects
        (into {}
          (for [path paths
                :let [row (fixture-git! ["ls-tree" (:tree content) "--" path] nil {})
                      [_ mode oid observed-path]
                      (re-matches #"([0-9]+) blob ([0-9a-f]{40})\t(.+)" row)
                      {:keys [exit out]} (shell/sh "/usr/bin/git" "cat-file" "blob" oid)
                      bytes (.getBytes ^String out java.nio.charset.StandardCharsets/UTF_8)]]
            (do
              (when-not (and (= path observed-path) (= "100644" mode) (zero? exit))
                (throw (ex-info "missing authentic checker source" {:path path})))
              [path {:path path :mode mode :blob-oid oid :byte-count (alength bytes)
                     :raw-sha256 (codec/R bytes) :bytes bytes}])))
        specs @(ns-resolve 'gravity.tools.sh07-b51-attempt32-checker-child
                          'external-classpath-specs)
        classpath (str/split (System/getProperty "java.class.path")
                            (re-pattern java.io.File/pathSeparator))
        external
        (reduce merge {}
          (for [spec specs
                :let [path (first (filter #(str/ends-with? % (str (:version spec) ".jar"))
                                          classpath))]]
            (with-open [jar (java.util.jar.JarFile. path)]
              (into {}
                (for [entry (enumeration-seq (.entries jar))
                      :when (and (not (.isDirectory entry))
                                 (contains? #{"clojure/core.clj" "clojure/set.clj"
                                              "clojure/string.clj" "clojure/walk.clj"
                                              "clojure/edn.clj"} (.getName entry)))
                      :let [bytes (with-open [input (.getInputStream jar entry)]
                                    (.readAllBytes input))
                            namespace (-> (.getName entry) (str/replace #"\.clj$" "")
                                          (str/replace "/" ".") symbol)]]
                  [namespace {:namespace namespace :resource (.getName entry)
                              :byte-count (alength bytes)
                              :source-sha256 (codec/R bytes)}])))))]
    {:source-authority {:content content :source-objects objects
                        :external-source-records external}
     :pre-object-snapshot
     {:owned-c-descriptors (mapv #(dissoc % :bytes) (vals objects))}}))

(declare c5-failure?)

(deftest bounded-authentic-producer-closes-without-source-reacquisition
  ;; This exercises real source production. The role seam is test-only;
  ;; no semantic product or authenticity validator is replaced.
  (let [{:keys [commit tree]} (bounded-content!)
        content {:commit commit :tree tree}
        preflight (checker-source-preflight! content)
        manifest (identity/checker-identity-manifest (:source-authority preflight)
                                                    :producer-census)
        request {:content content
                 :evidence {:architecture-report-sha256
                            (codec/R (get-in preflight [:source-authority :source-objects
                                                        git-evidence/architecture-report-path :bytes]))}}
        closed? (atom false) acquisitions (atom 0)
        acquire-var (required-var 'p15-s23-sh07-b51-acquire-source-entry)
        git-var (required-var 'p15-s23-sh07-b51-git-bytes)
        live-var (required-var 'p15-s23-sh07-b51-compatibility-source-record)
        finalize-var (required-var 'p15-s23-sh07-b51-finalize-source-session!)
        acquire @acquire-var git @git-var live @live-var finalize @finalize-var
        guard (fn [f]
                (fn [& args]
                  (when @closed?
                    (throw (ex-info "source acquisition after closure"
                                    {:reason :post-final-acquisition})))
                  (apply f args)))
        valid? (fn [products]
                 (bootstrap/p15-s23-sh07-b51-sh06-authority-carrier-valid?
                  commit tree products (:sh06-authority-carrier products)))]
    (with-redefs-fn
      {(required-var 'p15-s23-sh07-b51-process-role) (constantly :producer-census)
       acquire-var (guard (fn [& args] (swap! acquisitions inc)
                            (apply acquire args)))
       git-var (guard git) live-var (guard live)
       finalize-var (fn [] (let [receipt (finalize)]
                            (reset! closed? true) receipt))}
      (fn []
        (let [products (bootstrap/p15-s23-sh07-b51-build-receipted-bundle-core-products
                        commit tree)
              carrier (:sh06-authority-carrier products)]
          (println :authentic-handoff-observation
                   {:content content :product-count (count products)
                    :carrier-count (count carrier)
                    :edge-count (count (:expected-edge-census carrier))
                    :role (get-in products [:source-read-receipt :role])
                    :counters (get-in products [:source-read-receipt :counters])})
          (is (= 11 (count products)))
          (is (= 16 (count carrier)))
          (is (= 7 (count (:external-binding-authorities carrier))))
          (is (= 1480 (count (:expected-edge-census carrier))))
          (is (= 13 @acquisitions))
          (is (= {:acquire-count 13 :decode-count 13
                  :event-count 40 :finalize-count 1}
                 (get-in products [:source-read-receipt :counters])))
          (is (true? @closed?))
          (is (true? (valid? products)))
          (is (= products (child/validate-receipted-products!
                           codec/H :producer-census request products)))
          (let [envelope (child/build-checker-envelope
                           codec/H :producer-census request products manifest preflight)]
            (is (= 21 (count envelope)))
            (is (= carrier (:sh06-authority-carrier envelope)))
            (is (= 7 (count (:semantic-algorithm-source-descriptors envelope))))
            (is (= (:checker manifest) (:checker envelope))))
          (let [transaction (first (:module-transactions products))
                c2 (:c2-reader-artifact transaction)
                path (get-in transaction [:source-descriptor :path])
                context (invoke-private 'p15-s23-sh07-b51-sh06-context
                                        (first (:external-binding-authorities carrier))
                                        (:sh06-resolution-plan-authority products))
                snapshot (fn [artifact]
                           (invoke-private 'sh06-authenticated-source-snapshot!
                                           path artifact :test false context))]
            (is (= (get-in transaction [:source-descriptor :byte-count])
                   (:byte-count (snapshot c2))))
            (doseq [bad [(assoc-in c2 [:source-unit-record :byte-count] -1)
                         (assoc-in c2 [:source-unit-record :bytes-hash] (codec/H :wrong))
                         (assoc-in c2 [:source-unit-record :path] "wrong.gravity")]]
              (is (c5-failure? #(snapshot bad)))))
          (doseq [key (keys products)]
            (is (false? (valid? (dissoc products key))) key))
          (doseq [key (keys carrier)]
            (is (false? (valid? (update products :sh06-authority-carrier dissoc key))) key))
          (doseq [invalid
                  [(assoc products :unexpected true)
                   (assoc-in products [:sh06-authority-carrier :unexpected] true)
                   (assoc-in products [:module-transactions 0 :module-ordinal] 1)
                   (assoc-in products [:module-transactions 0 :unexpected] true)
                   (assoc-in products [:source-read-receipt :finalized] false)
                   (assoc-in products [:source-read-receipt :counters :finalize-count] 2)
                   (assoc-in products [:source-read-receipt :tree] commit)
                   (assoc-in products [:sh06-resolution-plan-authority :content :tree] commit)
                   (coherent-event-mutation products 17)
                   (coherent-event-mutation products 25)
                   (coherent-event-mutation products 39)]]
            (is (false? (valid? invalid))))
          (is (= 13 @acquisitions)))))))

(defn- c5-failure? [thunk]
  (try
    (thunk)
    false
    (catch clojure.lang.ExceptionInfo error
      (= "C5-UNRESOLVED" (:id (ex-data error))))))

(deftest proof-shape-consumer-verifies-authentic-git-proof
  (let [commit (fixture-git! ["rev-parse" "HEAD"] nil {})
        tree (fixture-git! ["rev-parse" "HEAD^{tree}"] nil {})
        content {:commit commit :tree tree}
        spec (first @(required-var 'p15-s23-sh07-b51-source-specs))
        entry (invoke-private 'p15-s23-sh07-b51-acquire-source-entry commit tree spec)
        proof (:content-object-proof entry)
        check (fn [c p]
                (invoke-private 'sh06-resolution-content-proof-tree! (:path spec) c p))]
    (is (true? (invoke-private 'p15-s23-sh07-b51-validate-content-object-proof!
                               content proof)))
    (is (true? (check content proof)))
    (doseq [key (keys content)]
      (is (c5-failure? #(check (dissoc content key) proof))))
    (doseq [key (keys proof)]
      (is (c5-failure? #(check content (dissoc proof key)))))
    (doseq [[c p] [[(assoc content :unexpected true) proof]
                   [content (assoc proof :unexpected true)]
                   [content (update-in proof [:raw-bytes 0] bit-xor 1)]
                   [(assoc content :tree commit) proof]]]
      (is (c5-failure? #(check c p))))))

(deftest strict-context-arities-are-explicit-and-nil-fails-closed
  (testing "the governed lexical arities are present"
    (is (= '([source-path sh05-artifact]
             [source-path sh05-artifact context])
           (:arglists (meta (required-var 'sh06-resolution-request)))))
    (is (= '([artifact] [artifact context])
           (:arglists
            (meta (required-var 'sh06-resolution-artifact-verification)))))
    (is (= '([resolution-artifact] [resolution-artifact context])
           (:arglists (meta (required-var 'sh07-core-authenticated-request)))))
    (is (= '([sh05-artifact]
             [source-path sh05-artifact]
             [source-path sh05-artifact context])
           (:arglists
            (meta
             (required-var
              'sh06-resolution-artifact-from-sh05-artifact))))))
  (testing "nil never selects an ordinary implementation"
    (is (c5-failure?
         #(bootstrap/sh06-resolution-request "module.gravity" nil nil)))
    (is (c5-failure?
         #(bootstrap/sh06-resolution-artifact-from-sh05-artifact
           "module.gravity" nil nil)))
    (is (c5-failure?
         #(bootstrap/sh06-resolution-context-root nil)))
    (is (c5-failure?
         #(bootstrap/sh06-resolution-context-plan-binding!
           "module.gravity" nil)))))

(defn- digest [character]
  (str "sha256:" (apply str (repeat 64 character))))

(defn- m0-row [ordinal module owner]
  {:module-source-id (digest (char (+ (int \a) ordinal)))
   :module-ordinal ordinal :module module :owner owner
   :logical-source-path (str "module-" ordinal ".gravity")
   :profile :meta :target :jvm :effects #{} :capabilities #{}
   :safety :safe :definitions [] :external-bindings []})

(defn- minimal-authority-family []
  (let [symbol 'provider
        source-hash (digest \f)
        caller-base (m0-row 0 'gravity.caller :r1-producer)
        callee-base (m0-row 1 'gravity.provider :r1-producer)
        external {:symbol symbol :callee-module-ordinal 1
                  :callee-module (:module callee-base)
                  :callee-source-sha256 (digest \b)
                  :callee-definition-source-hash source-hash
                  :arity 1 :owner-edge :producer-internal}
        caller (assoc caller-base :external-bindings [external])
        callee (assoc callee-base
                      :source-sha256 (digest \b)
                      :definitions [{:name symbol :arity 1
                                     :visibility :private
                                     :source-hash source-hash}])
        descriptors (into [caller callee]
                          (mapv (fn [ordinal]
                                  (m0-row ordinal
                                          (symbol (str "gravity.m" ordinal))
                                          :r1-producer))
                                (range 2 7)))
        edge {:ordinal 0
              :caller-module-source-id (:module-source-id caller)
              :caller-function 'caller
              :caller-function-source-hash (digest \e)
              :callsite-path [0]
              :callee-module-source-id (:module-source-id callee)
              :callee-function symbol
              :callee-function-source-hash source-hash
              :arity 1 :owner-edge :producer-internal
              :authorization :authenticated-b51-namespace-external}
        manifest {:manifest-id (digest \9) :edges [edge]}]
    {:descriptors descriptors :manifest manifest}))

(deftest visibility-is-granted-only-by-an-exact-authenticated-edge
  (let [{:keys [descriptors manifest]} (minimal-authority-family)
        project
        #(invoke-private 'sh06-resolution-c5-expected-projection!
                         "module-0.gravity" 0 descriptors %)]
    (testing "an exact M1 grant promotes the provider into a public SH06 import"
      (let [projection (project manifest)
            binding (first (:import-bindings projection))]
        (is (= 'provider (:name binding)))
        (is (= :public (:visibility binding)))
        (is (= :import (:binding-class binding)))
        (is (= {:source :external-binding} (:source-span binding)))
        (is (= [0]
               (get-in binding
                       [:semantic-span :authorized-edge-ordinals])))))
    (testing "removing authorization rejects before a projection is returned"
      (is (c5-failure?
           #(project (assoc-in manifest [:edges 0 :authorization]
                               :unauthorized)))))
    (testing "changing the owner edge also rejects"
      (is (c5-failure?
           #(project (assoc-in manifest [:edges 0 :owner-edge]
                               :root6-internal)))))))

(deftest ordinary-and-strict-paths-have-no-ambient-authority-seam
  (let [bounded (meta (required-var
                       'sh06-resolution-artifact-verification-bounded*))
        candidate (meta (required-var
                         'sh06-resolution-source-artifact-candidate))]
    (is (:private bounded))
    (is (:private candidate))
    (is (= '([artifact construction?]
             [artifact construction? context])
           (:arglists bounded)))
    (is (= '([source-path sh05-artifact legacy?]
             [source-path sh05-artifact legacy? context])
           (:arglists candidate)))))
