(ns gravity.self-hosting.sh07-b51-attempt30-producer-census-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.tools.sh07-b51-attempt30-producer-census :as census]
            [gravity.tools.sh07-b51-attempt32-materialize :as materialize]))

(defn- digest [value]
  (census/canonical-hash {:fixture value}))

(defn- unit-precertificate [input]
  (let [product-id (census/canonical-hash
                    [:unit-lowering-product (:snapshot-tree input)])
        operations
        (mapv (fn [ordinal]
                {:ordinal ordinal
                 :operation-id (census/canonical-hash
                                [:unit-operation ordinal])
                 :lowering-product-id product-id}) (range 7))
        receipts (mapv (fn [operation]
                         {:operation-id (:operation-id operation)
                          :within-bounds true}) operations)
        base {:schema :gravity/sh07-b51-authentic-lowering-witness-v1
              :content {:commit (:snapshot-commit input)
                        :tree (:snapshot-tree input)}
              :operation-manifest operations
              :operation-schedule-id
              (census/canonical-hash [:unit-operation-schedule operations])
              :fuel-receipts receipts
              :l1 {} :l2 {} :c2 {} :c6 {} :boot2 {} :boot3 {} :boot4 {}}
        witness (assoc base :witness-id
                       (census/canonical-hash
                        [:gravity/sh07-b51-authentic-lowering-witness-v1 base]))]
    {:lowering-product {:product-id product-id}
     :semantic-witness witness :measurement-count 34}))

(defn- produce [input]
  (with-redefs [materialize/build-checker-precertificate unit-precertificate]
    (census/produce-certificate input)))

(deftest e5-checker-rejects-every-missing-or-extra-input-key
  (is (= 21 (count census/input-keys)))
  (is (= census/input-keys materialize/checker-semantic-input-keys-e3))
  (let [input (zipmap census/input-keys (repeat nil))]
    (doseq [invalid (concat (map #(dissoc input %) census/input-keys)
                           [(assoc input :unexpected true)
                            (assoc input :external-binding-authorities [])])]
      (is (= :input-schema
             (:reason (census/produce-certificate invalid)))))))

(defn- definition [form]
  (let [[_ name params & body] form]
    {:name name :params params :body body :arity (count params)}))

(defn- definition-hash [definition]
  (census/canonical-hash
   (select-keys definition [:name :arity :params :body])))

(defn- ns-form [module exports]
  (list 'ns module
        (list :profile :meta)
        (list :target :jvm)
        (list :exports exports)
        (list :effects #{})
        (list :capabilities #{})
        (list :safety :safe)))

(defn- fixture-forms []
  (let [module0 'fixture.sh07.m0
        modules (mapv #(symbol (str "fixture.sh07.m" %)) (range 7))]
    [(vector
      (ns-form module0 '[root0 helper])
      (list 'defn 'root0 []
            (list 'do
                  (list 'helper)
                  (list 'remote)
                  (list 'count [])
                  (list 'sh07-canonical-text-compare "a" "b")
                  (list 'sh07-declared-digest-hash {:x 1})
                  (list 'sh07-observe-utf8-byte-count "x" 0 1)
                  (list 'sh07-b51-admitted-executable-carrier-validation? :x)
                  (list 'quote (list 'remote))))
      (list 'defn 'helper [] 0))
     (vector (ns-form (nth modules 1) '[remote]) (list 'defn 'remote [] 1))
     (vector (ns-form (nth modules 2) '[root2]) (list 'defn 'root2 [] 2))
     (vector (ns-form (nth modules 3) '[root3]) (list 'defn 'root3 [] 3))
     (vector (ns-form (nth modules 4) '[root4]) (list 'defn 'root4 [] 4))
     (vector (ns-form (nth modules 5) '[root5]) (list 'defn 'root5 [] 5))
     (vector (ns-form (nth modules 6) '[root6]) (list 'defn 'root6 [] 6))]))

(defn- source-material [forms]
  (mapv
   (fn [ordinal module-forms]
     (let [definitions (mapv definition (rest module-forms))]
       {:ordinal ordinal
        :module (second (first module-forms))
        :forms module-forms
        :source-sha256 (digest [:source ordinal module-forms])
        :source-byte-count (count (.getBytes (pr-str module-forms) "UTF-8"))
        :definitions definitions
        :definition-rows
        (mapv (fn [definition]
                {:name (:name definition)
                 :arity (:arity definition)
                 :visibility :public
                 :source-hash (definition-hash definition)})
              definitions)}))
   (range) forms))

(defn- fixture-m0s [material]
  (let [callee (nth material 1)
        remote-definition (first (:definition-rows callee))]
    (mapv
     (fn [{:keys [ordinal module source-sha256 source-byte-count
                  definition-rows]}]
       (let [external
             (if (zero? ordinal)
               [{:symbol 'remote
                 :callee-module-ordinal 1
                 :callee-module (:module callee)
                 :callee-source-sha256 (:source-sha256 callee)
                 :callee-definition-source-hash
                 (:source-hash remote-definition)
                 :arity 0
                 :owner-edge :producer-internal}]
               [])
             builtins
             (if (zero? ordinal)
               [{:caller 'root0
                 :caller-function-source-hash
                 (:source-hash (first definition-rows))
                 :builtin 'count :arity 1 :compiler-internal? false}
                {:caller 'root0
                 :caller-function-source-hash
                 (:source-hash (first definition-rows))
                 :builtin 'sh07-canonical-text-compare
                 :arity 2 :compiler-internal? true}
                {:caller 'root0
                 :caller-function-source-hash
                 (:source-hash (first definition-rows))
                 :builtin 'sh07-declared-digest-hash
                 :arity 1 :compiler-internal? true}
                {:caller 'root0
                 :caller-function-source-hash
                 (:source-hash (first definition-rows))
                 :builtin 'sh07-observe-utf8-byte-count
                 :arity 3 :compiler-internal? true}
                {:caller 'root0
                 :caller-function-source-hash
                 (:source-hash (first definition-rows))
                 :builtin 'sh07-b51-admitted-executable-carrier-validation?
                 :arity 1 :compiler-internal? true}]
               [])
             base
             {:artifact :gravity/sh07-b51-module-source-v1
              :schema-version 1
              :module-ordinal ordinal
              :module module
              :logical-source-path (str "fixture/m" ordinal ".gravity")
              :owner :r1-producer
              :source-byte-count source-byte-count
              :source-sha256 source-sha256
              :profile :meta
              :target :jvm
              :effects #{}
              :capabilities #{}
              :safety :safe
              :definitions definition-rows
              :exports (mapv :name definition-rows)
              :core-binding-count 0
              :lexical-binding-count 0
              :external-bindings external
              :aggregate-binding-count (+ (count definition-rows)
                                          (count external))
              :builtin-calls builtins}]
         (assoc base :module-source-id (census/canonical-hash base))))
     material)))

(defn- fixture-m1 [m0s]
  (let [caller (first m0s)
        callee (second m0s)
        base
        {:artifact :gravity/sh07-b51-link-manifest-v1
         :schema-version 1
         :module-source-ids (mapv :module-source-id m0s)
         :edges
         [{:ordinal 0
           :caller-module-source-id (:module-source-id caller)
           :caller-function 'root0
           :caller-function-source-hash
           (:source-hash (first (:definitions caller)))
           :callsite-path [0 2]
           :callee-module-source-id (:module-source-id callee)
           :callee-function 'remote
           :callee-function-source-hash
           (:source-hash (first (:definitions callee)))
           :arity 0
           :owner-edge :producer-internal
           :authorization :authenticated-b51-namespace-external}]}]
    (assoc base :manifest-id (census/canonical-hash base))))

(defn- literal [value] {:op :literal :value value})

(defn- fixture-m2s [material m0s m1 emitter-source emitter-semantic]
  (mapv
   (fn [ordinal source descriptor]
     (let [functions
           (into {}
                 (map
                  (fn [definition]
                    (let [name (:name definition)
                          instructions
                          (cond
                            (= name 'root0)
                            [{:op :do
                              :body
                              [{:op :function-call :function 'helper :args []}
                               {:op :function-call :function 'remote :args []
                                :external-link
                                {:artifact :gravity/sh07-b51-external-call-v1
                                 :manifest-id (:manifest-id m1)
                                 :edge-ordinal 0}}
                               {:op :builtin-call :function 'count
                                :args [{:op :vector-literal :items []}]}
                               {:op :builtin-call
                                :function 'sh07-canonical-text-compare
                                :compiler-internal-caller 'root0
                                :compiler-internal-provenance
                                {:artifact
                                 :gravity/sh07-compiler-internal-call-provenance-v2
                                 :manifest-id (:manifest-id m1)
                                 :module-source-id (:module-source-id descriptor)
                                 :source-sha256 (:source-sha256 descriptor)
                                 :function 'root0
                                 :function-source-hash
                                 (:source-hash (first (:definitions descriptor)))
                                 :callee 'sh07-canonical-text-compare}
                                :args [(literal "a") (literal "b")]}
                               {:op :builtin-call
                                :function 'sh07-declared-digest-hash
                                :compiler-internal-caller 'root0
                                :compiler-internal-provenance
                                {:artifact
                                 :gravity/sh07-compiler-internal-call-provenance-v2
                                 :manifest-id (:manifest-id m1)
                                 :module-source-id (:module-source-id descriptor)
                                 :source-sha256 (:source-sha256 descriptor)
                                 :function 'root0
                                 :function-source-hash
                                 (:source-hash (first (:definitions descriptor)))
                                 :callee 'sh07-declared-digest-hash}
                                :args
                                [{:op :map-literal
                                  :entries
                                  [{:key (literal :x) :value (literal 1)}]}]}
                               {:op :builtin-call
                                :function 'sh07-observe-utf8-byte-count
                                :compiler-internal-caller 'root0
                                :compiler-internal-provenance
                                {:artifact
                                 :gravity/sh07-compiler-internal-call-provenance-v2
                                 :manifest-id (:manifest-id m1)
                                 :module-source-id (:module-source-id descriptor)
                                 :source-sha256 (:source-sha256 descriptor)
                                 :function 'root0
                                 :function-source-hash
                                 (:source-hash (first (:definitions descriptor)))
                                 :callee 'sh07-observe-utf8-byte-count}
                               :args [(literal "x") (literal 0) (literal 1)]}
                               {:op :builtin-call
                                :function
                                'sh07-b51-admitted-executable-carrier-validation?
                                :compiler-internal-caller 'root0
                                :compiler-internal-provenance
                                {:artifact
                                 :gravity/sh07-compiler-internal-call-provenance-v2
                                 :manifest-id (:manifest-id m1)
                                 :module-source-id (:module-source-id descriptor)
                                 :source-sha256 (:source-sha256 descriptor)
                                 :function 'root0
                                 :function-source-hash
                                 (:source-hash (first (:definitions descriptor)))
                                 :callee
                                 'sh07-b51-admitted-executable-carrier-validation?}
                                :args [(literal :x)]}
                               {:op :quote :value '(remote)}]}]

                            :else [(literal (last (:body definition)))])]
                      [name (assoc definition :instructions instructions)]))
                  (:definitions source)))
           base
           {:source {:sha256 (:source-sha256 descriptor)}
            :module {:module (:module descriptor)}
            :compiler {:source-content-hash emitter-source
                       :semantic-hash emitter-semantic}
            :sh07-b51-module-source-id (:module-source-id descriptor)
            :sh07-b51-link-manifest-id (:manifest-id m1)
            :functions functions}]
       (assoc base :plan-id (digest [:m2 ordinal base]))))
   (range) material m0s))

(defn- fixture-bundle [m0s m1 m2s]
  (let [root-functions '[root0 helper remote root2 root3 root4 root5 root6]
        plan-by-function
        (into {}
              (for [plan m2s, function (keys (:functions plan))]
                [function (:plan-id plan)]))
        base
        {:domain :gravity/sh07-b51-execution-bundle-core-id-v1
         :schema-version 1
         :manifest-id (:manifest-id m1)
         :module-plans
         (mapv (fn [ordinal descriptor plan]
                 {:module-ordinal ordinal
                  :module-source-id (:module-source-id descriptor)
                  :plan-id (:plan-id plan)})
               (range) m0s m2s)
         :routes
         [{:edge-ordinal 0
           :caller-plan-id (:plan-id (first m2s))
           :callee-plan-id (:plan-id (second m2s))}]
         :public-roots
         (mapv (fn [ordinal function]
                 {:root-ordinal ordinal
                  :function function
                  :arity 0
                  :plan-id (get plan-by-function function)})
               (range 1 9) root-functions)}]
    base))

(defn fixture-input []
  (let [forms (fixture-forms)
        material (source-material forms)
        m0s (fixture-m0s material)
        m1 (fixture-m1 m0s)
        emitter-source (digest :emitter-source)
        emitter-semantic (digest :emitter-semantic)
        m2s (fixture-m2s material m0s m1 emitter-source emitter-semantic)
        bundle (fixture-bundle m0s m1 m2s)]
    {:architecture-report-sha256 (digest :attempt30-report)
     :snapshot-commit (apply str (repeat 40 "a"))
     :snapshot-tree (apply str (repeat 40 "b"))
     :predecessor-executable-descriptor-id (digest :predecessor)
     :module-source-identities (mapv :source-sha256 m0s)
     :module-source-byte-counts (mapv :source-byte-count m0s)
     :emitter-source-id emitter-source
     :emitter-semantic-id emitter-semantic
     :m0s m0s
     :m1 m1
     :m2s m2s
     :bundle-core bundle
     :source-forms forms
     :module-transactions []
     :sh06-resolution-plan-authority {}
     :sh06-authority-carrier {}
     :source-semantic-carrier {}
     :semantic-algorithm-source-descriptors []
     :semantic-algorithm-source-root (digest :algorithm-root)
     :checker {:artifact-id (digest :checker-a-artifact)
               :source-sha256 (digest :checker-a-source)
               :semantic-id (digest :checker-a-semantics)
               :executable-id (digest :checker-a-executable)
               :dependency-closure-id (digest :checker-a-closure)
               :algorithm-role :source-first-exhaustive}
     :provenance {:invocation-id (digest :invocation)
                  :tool-closure-id (digest :tool-closure)
                  :reproduction-command-id (digest :command)
                  :evidence-relative-path "target/attempt30/checker-a.edn"
                  :completed-at "2026-09-02T00:00:00Z"}}))

(deftest producer-census-emits-the-closed-source-first-certificate
  (let [output (produce (fixture-input))
        certificate (:certificate output)
        claim (:claim certificate)
        raw-records (get-in output [:raw-source-transcript-evidence :records])]
    (is (= :accepted (:status output)) output)
    (is (true? (census/certificate-valid? certificate)))
    (is (true? (census/checker-output-valid? output)))
    (is (= :producer-census (:role certificate)))
    (is (= :source-first-exhaustive
           (get-in certificate [:checker :algorithm-role])))
    (is (= (:certificate-id certificate)
           (census/canonical-hash (dissoc certificate :certificate-id))))
    (is (= 8 (get-in claim [:physical-census :physical-definition-count])))
    (is (= 1 (get-in claim
                     [:physical-census
                      :physical-external-runtime-callsite-count])))
    (is (= 5 (get-in claim
                     [:physical-census
                      :physical-referenced-builtin-occurrence-count])))
    (is (some #(and (:quoted-subtree-stop %)
                    (= 'quote (:callee %))) raw-records))
    (is (= 1 (count (filter #(= :external-runtime-call (:classification %))
                            raw-records))))))

(deftest producer-census-fails-closed-on-source-and-m2-omissions
  (let [input (fixture-input)
        changed-source
        (assoc-in input [:source-forms 0 1]
                  '(defn root0 []
                     (do (helper) (count [])
                         (sh07-canonical-text-compare "a" "b")
                         (sh07-declared-digest-hash {:x 1})
                         (sh07-observe-utf8-byte-count "x" 0 1)
                         (sh07-b51-admitted-executable-carrier-validation? :x)
                         '(remote))))
        changed-m2
        (update-in input [:m2s 0 :functions 'root0 :instructions 0 :body]
                   #(vec (remove (fn [instruction]
                                   (= 'count (:function instruction))) %)))]
    (doseq [candidate [changed-source changed-m2
                       (assoc input :unexpected true)]]
      (let [result (produce candidate)]
        (is (= :rejected (:status result)))
        (is (= :gravity/sh07-b51-census-checker-rejection-evidence-v1
               (:artifact result)))
        (is (not (contains? result :certificate-id)))
        (is (not (contains? result :certificate)))))))

(deftest producer-census-is-deterministic-and-validator-rejects-rehash-gaps
  (let [input (fixture-input)
        left (produce input)
        right (produce input)
        certificate (:certificate left)]
    (is (= left right))
    (is (false? (census/certificate-valid?
                 (assoc-in certificate [:result :ordering-complete] false))))
    (is (false? (census/certificate-valid?
                 (assoc certificate :certificate-id (digest :fabricated)))))
    (is (false? (census/checker-output-valid?
                 (assoc-in left
                           [:raw-source-transcript-evidence :records 0
                            :classification]
                           :fabricated))))))

(deftest attempt30-certificate-and-offline-bounds-are-inclusive
  (let [certificate-limit
        {:nodes 2048 :depth 12 :width 35 :collection-count 64
         :scalar-utf8 4096 :canonical-reader-bytes 262144}
        offline-limit
        {:node-count 262144 :record-count 262144 :call-row-count 65536
         :byte-count 268435456}]
    (is (true? (census/certificate-profile-within-bounds?
                certificate-limit)))
    (doseq [field (keys certificate-limit)]
      (is (false? (census/certificate-profile-within-bounds?
                   (update certificate-limit field inc))) field))
    (is (true? (census/offline-transcript-within-bounds? offline-limit)))
    (doseq [field (keys offline-limit)]
      (is (false? (census/offline-transcript-within-bounds?
                   (update offline-limit field inc))) field))))
