(ns gravity.self-hosting.sh07-b51-v9-sh06-external-binding-seam-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
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
        extra (case ordinal
                0 (into (mapv #(definition % 1 0)
                              (remove #{'sh07-b51-build-template} callees))
                        (mapv (fn [n]
                                (definition (symbol (str "fixture-caller-" n)) 0
                                  (cons 'do (repeat (if (= n 45) 28 32)
                                                    '(fixture-provider)))))
                              (range 46)))
                1 [(definition 'fixture-provider 0 0)] [])]
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

(deftest bounded-authentic-producer-closes-without-source-reacquisition
  ;; This exercises real source production. The role seam is test-only;
  ;; no semantic product or authenticity validator is replaced.
  (let [{:keys [commit tree]} (bounded-content!)
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
      {(required-var 'p15-s23-sh07-b51-process-role) (constantly :m)
       acquire-var (guard (fn [& args] (swap! acquisitions inc)
                            (apply acquire args)))
       git-var (guard git) live-var (guard live)
       finalize-var (fn [] (let [receipt (finalize)]
                            (reset! closed? true) receipt))}
      (fn []
        (let [products (bootstrap/p15-s23-sh07-b51-build-receipted-bundle-core-products
                        commit tree)
              carrier (:sh06-authority-carrier products)]
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
