(ns gravity.self-hosting.sh07-b51-attempt30-independent-census-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.tools.sh07-b51-attempt30-independent-census :as census]
            [gravity.tools.sh07-b51-attempt32-materialize :as materialize]))

(defn- h [value]
  (bootstrap/reader-canonical-hash value))

(defn- digest [digit]
  (str "sha256:" (apply str (repeat 64 digit))))

(deftest e5-checker-rejects-every-missing-or-extra-input-key
  (is (= 21 (count census/input-keys)))
  (is (= census/input-keys materialize/checker-semantic-input-keys-e3))
  (let [input (zipmap census/input-keys (repeat nil))]
    (doseq [invalid (concat (map #(dissoc input %) census/input-keys)
                           [(assoc input :unexpected true)
                            (assoc input :external-binding-authorities [])])]
      (is (= :input-schema
             (get-in (census/produce-certificate invalid)
                     [:rejection-evidence :reason]))))))

(defn- unit-precertificate [input]
  (let [product-id (h [:unit-lowering-product (:snapshot-tree input)])
        operations
        (mapv (fn [ordinal]
                {:ordinal ordinal :operation-id (h [:unit-operation ordinal])
                 :lowering-product-id product-id}) (range 7))
        receipts (mapv (fn [operation]
                         {:operation-id (:operation-id operation)
                          :within-bounds true}) operations)
        base {:schema :gravity/sh07-b51-authentic-lowering-witness-v1
              :content {:commit (:snapshot-commit input)
                        :tree (:snapshot-tree input)}
              :operation-manifest operations
              :operation-schedule-id (h [:unit-operation-schedule operations])
              :fuel-receipts receipts
              :l1 {} :l2 {} :c2 {} :c6 {} :boot2 {} :boot3 {} :boot4 {}}
        witness (assoc base :witness-id
                       (h [:gravity/sh07-b51-authentic-lowering-witness-v1 base]))]
    {:lowering-product {:product-id product-id}
     :semantic-witness witness :measurement-count 34}))

(defn- produce [input]
  (with-redefs [materialize/build-checker-precertificate unit-precertificate]
    (census/produce-certificate input)))

(def source-forms
  [[(list 'ns 'test.m0)
    '(defn a0 [] (b0) (sh07-declared-digest-hash :x))
    '(defn a1 [] (a0) (sh07-canonical-text-compare "a" "b"))]
   [(list 'ns 'test.m1)
    '(defn b0 []
       (sh07-observe-utf8-byte-count "a" "b" "c")
       (sh07-b51-admitted-executable-carrier-validation? :x))]
   [(list 'ns 'test.m2) '(defn c0 [] 2)]
   [(list 'ns 'test.m3) '(defn d0 [] 3)]
   [(list 'ns 'test.m4) '(defn e0 [] 4)]
   [(list 'ns 'test.m5) '(defn f0 [] 5)]
   [(list 'ns 'test.m6) '(defn g0 [] 6)]])

(defn- source-definition [form]
  (let [[_ name params & body] form]
    {:name name :arity (count params) :params params :body body}))

(defn- definition-hash [definition]
  (h (select-keys definition [:name :arity :params :body])))

(defn- module-definitions [forms]
  (mapv source-definition (filter #(and (seq? %) (= 'defn (first %))) forms)))

(defn- build-m0s []
  (let [definitions (mapv module-definitions source-forms)
        b0-hash (definition-hash (first (nth definitions 1)))
        builtin-rows
        {0 [{:caller 'a0
             :caller-function-source-hash
             (definition-hash (first (nth definitions 0)))
             :builtin 'sh07-declared-digest-hash :arity 1
             :compiler-internal? true}
            {:caller 'a1
             :caller-function-source-hash
             (definition-hash (second (nth definitions 0)))
             :builtin 'sh07-canonical-text-compare :arity 2
             :compiler-internal? true}]
         1 [{:caller 'b0 :caller-function-source-hash b0-hash
             :builtin 'sh07-observe-utf8-byte-count :arity 3
             :compiler-internal? true}
            {:caller 'b0 :caller-function-source-hash b0-hash
             :builtin 'sh07-b51-admitted-executable-carrier-validation?
             :arity 1 :compiler-internal? true}]}
        source-shas (mapv #(digest (char (+ (int \0) %))) (range 7))]
    (mapv
     (fn [module-ordinal]
       (let [module-defs (nth definitions module-ordinal)
             externals
             (if (zero? module-ordinal)
               [{:symbol 'b0 :callee-module-ordinal 1 :callee-module 'test.m1
                 :callee-source-sha256 (nth source-shas 1)
                 :callee-definition-source-hash b0-hash :arity 0
                 :owner-edge :producer-internal}]
               [])
             base
             {:artifact :gravity/sh07-b51-module-source-v1
              :schema-version 1 :module-source-id nil
              :module-ordinal module-ordinal
              :module (symbol (str "test.m" module-ordinal))
              :logical-source-path (str "synthetic/m" module-ordinal ".gravity")
              :owner :r1-producer :source-byte-count (+ 100 module-ordinal)
              :source-sha256 (nth source-shas module-ordinal)
              :profile :meta :target :jvm :effects #{} :capabilities #{}
              :safety :safe
              :definitions
              (mapv (fn [definition]
                      {:name (:name definition) :arity (:arity definition)
                       :visibility :public
                       :source-hash (definition-hash definition)}) module-defs)
              :exports (mapv :name module-defs)
              :core-binding-count 0 :lexical-binding-count 0
              :external-bindings externals
              :aggregate-binding-count (+ (count module-defs)
                                          (count externals))
              :builtin-calls (vec (get builtin-rows module-ordinal []))}]
         (assoc base :module-source-id (h (dissoc base :module-source-id)))))
     (range 7))))

(defn- build-m1 [m0s]
  (let [a0 (first (:definitions (nth m0s 0)))
        b0 (first (:definitions (nth m0s 1)))
        base
        {:artifact :gravity/sh07-b51-link-manifest-v1 :schema-version 1
         :module-source-ids (mapv :module-source-id m0s)
         :edges
         [{:ordinal 0 :caller-module-source-id
           (:module-source-id (nth m0s 0)) :caller-function 'a0
           :caller-function-source-hash (:source-hash a0)
           :callsite-path [0]
           :callee-module-source-id (:module-source-id (nth m0s 1))
           :callee-function 'b0 :callee-function-source-hash (:source-hash b0)
           :arity 0 :owner-edge :producer-internal
           :authorization :authenticated-b51-namespace-external}]}
        manifest-id (h base)]
    (assoc base :manifest-id manifest-id)))

(defn- provenance [m0 m1 definition callee]
  {:artifact :gravity/sh07-compiler-internal-call-provenance-v2
   :manifest-id (:manifest-id m1) :module-source-id (:module-source-id m0)
   :source-sha256 (:source-sha256 m0) :function (:name definition)
   :function-source-hash (definition-hash definition) :callee callee})

(defn- literal [value] {:op :literal :value value})

(defn- build-m2s [m0s m1]
  (let [definitions (mapv module-definitions source-forms)
        emitter-source (digest \8)
        emitter-semantic (digest \9)
        raw-plans
        (mapv
         (fn [module-ordinal]
           (let [m0 (nth m0s module-ordinal)
                 compiled
                 (into
                  {}
                  (map
                   (fn [definition]
                     (let [name (:name definition)
                           instructions
                           (case name
                             a0
                             [{:op :function-call :function 'b0 :args []
                               :external-link
                               {:artifact :gravity/sh07-b51-external-call-v1
                                :manifest-id (:manifest-id m1)
                                :edge-ordinal 0}}
                              {:op :builtin-call
                               :function 'sh07-declared-digest-hash
                               :compiler-internal-caller 'a0
                               :compiler-internal-provenance
                               (provenance m0 m1 definition
                                           'sh07-declared-digest-hash)
                               :args [(literal :x)]}]
                             a1
                             [{:op :function-call :function 'a0 :args []}
                              {:op :builtin-call
                               :function 'sh07-canonical-text-compare
                               :compiler-internal-caller 'a1
                               :compiler-internal-provenance
                               (provenance m0 m1 definition
                                           'sh07-canonical-text-compare)
                               :args [(literal "a") (literal "b")]}]
                             b0
                             [{:op :builtin-call
                               :function 'sh07-observe-utf8-byte-count
                               :compiler-internal-caller 'b0
                               :compiler-internal-provenance
                               (provenance m0 m1 definition
                                           'sh07-observe-utf8-byte-count)
                               :args [(literal "a") (literal "b")
                                      (literal "c")]}
                              {:op :builtin-call
                               :function
                               'sh07-b51-admitted-executable-carrier-validation?
                               :compiler-internal-caller 'b0
                               :compiler-internal-provenance
                               (provenance
                                m0 m1 definition
                                'sh07-b51-admitted-executable-carrier-validation?)
                               :args [(literal :x)]}]
                             [(literal (first (:body definition)))])]
                       [name (assoc definition :instructions instructions)]))
                   (nth definitions module-ordinal)))
                 base
                 {:plan-id nil :functions compiled
                  :source {:sha256 (:source-sha256 m0)}
                  :module {:module (:module m0)}
                  :compiler {:source-content-hash emitter-source
                             :semantic-hash emitter-semantic}
                  :sh07-b51-module-source-id (:module-source-id m0)
                  :sh07-b51-link-manifest-id (:manifest-id m1)}]
             base))
         (range 7))]
    (mapv
     (fn [plan]
       (assoc plan :plan-id (census/m2-plan-id plan)))
     raw-plans)))

(def root-functions '[a0 a1 b0 c0 d0 e0 f0 g0])

(defn- build-bundle [m0s m1 m2s]
  {:domain :gravity/sh07-b51-execution-bundle-core-id-v1 :schema-version 1
   :manifest-id (:manifest-id m1)
   :module-plans
   (mapv (fn [m0 m2]
           {:module-ordinal (:module-ordinal m0)
            :module-source-id (:module-source-id m0) :plan-id (:plan-id m2)})
         m0s m2s)
   :routes [{:edge-ordinal 0 :caller-plan-id (:plan-id (nth m2s 0))
             :callee-plan-id (:plan-id (nth m2s 1))}]
   :public-roots
   (mapv (fn [ordinal function]
           (let [module-ordinal (if (< ordinal 2) 0 (dec ordinal))]
             {:root-ordinal (inc ordinal) :function function :arity 0
              :plan-id (:plan-id (nth m2s module-ordinal))}))
         (range 8) root-functions)})

(defn envelope []
  (let [m0s (build-m0s)
        m1 (build-m1 m0s)
        m2s (build-m2s m0s m1)]
    {:architecture-report-sha256 (digest \7)
     :snapshot-commit (apply str (repeat 40 \1))
     :snapshot-tree (apply str (repeat 40 \2))
     :predecessor-executable-descriptor-id (digest \6)
     :module-source-identities (mapv :source-sha256 m0s)
     :module-source-byte-counts (mapv :source-byte-count m0s)
     :emitter-source-id (get-in (first m2s) [:compiler :source-content-hash])
     :emitter-semantic-id
     (get-in (first m2s) [:compiler :semantic-hash])
     :m0s m0s :m1 m1 :m2s m2s
     :bundle-core (build-bundle m0s m1 m2s)
     :source-forms source-forms
     :module-transactions []
     :sh06-resolution-plan-authority {}
     :sh06-authority-carrier {}
     :source-semantic-carrier {}
     :semantic-algorithm-source-descriptors []
     :semantic-algorithm-source-root (digest \c)
     :checker {:artifact-id (digest \1) :source-sha256 (digest \2)
               :semantic-id (digest \3) :executable-id (digest \4)
               :dependency-closure-id (digest \5)
               :algorithm-role :m2-first-independent-exhaustive}
     :provenance
     {:invocation-id "attempt30-checker-b-unit"
      :tool-closure-id (digest \a)
      :reproduction-command-id (digest \b)
      :evidence-relative-path "target/attempt30/checker-b.edn"
      :completed-at "2026-09-02T00:00:00Z"}}))

(deftest checker-b-produces-the-exact-closed-certificate-and-transcripts
  (let [input (envelope)
        output (produce input)
        certificate (:certificate output)
        raw (:raw-source-transcript-artifact output)
        m2 (:m2-transcript-artifact output)]
    (is (= :accepted (:status output)) (pr-str output))
    (is (true? (census/certificate-valid? certificate)))
    (is (true? (census/checker-output-valid? output)))
    (is (true? (census/valid-output? input output)))
    (is (= census/certificate-keys (set (keys certificate))))
    (is (= :independent-verifier-census (:role certificate)))
    (is (= :m2-first-independent-exhaustive
           (get-in certificate [:checker :algorithm-role])))
    (is (= (:certificate-id certificate)
           (h (dissoc certificate :certificate-id))))
    (is (= census/transcript-descriptor-keys
           (set (keys (:raw-source-transcript certificate)))
           (set (keys (:m2-transcript certificate)))))
    (is (= (:artifact-id raw)
           (get-in certificate [:provenance :raw-transcript-artifact-id])))
    (is (= (:artifact-id m2)
           (get-in certificate [:provenance :m2-transcript-artifact-id])))
    (is (= [8 1 1 4]
           ((juxt :definition-count :local-count :external-count
                  :builtin-count) (:result certificate))))
    (is (= 7 (count (get-in certificate
                            [:claim :category-commitments]))))))

(deftest checker-b-is-deterministic-and-own-ids-are-not-in-preimages
  (let [input (envelope)
        left (produce input)
        right (produce input)
        certificate (:certificate left)
        raw (:raw-source-transcript-artifact left)]
    (is (= left right))
    (is (= (:certificate-id certificate)
           (h (dissoc certificate :certificate-id))))
    (is (= (:record-root raw)
           (h {:domain :gravity/sh07-b51-raw-source-census-record-root-v1
               :schema-version 1
               :checker-algorithm-role :m2-first-independent-exhaustive
               :records (:records raw)})))))

(deftest checker-b-rejects-source-and-m2-negative-space-mutations
  (testing "a quoted fake external call cannot satisfy the M1 bijection"
    (let [input (envelope)
          mutated (assoc-in input [:source-forms 0 1]
                            '(defn a0 [] (quote (b0))
                               (sh07-declared-digest-hash :x)))
          output (produce mutated)]
      (is (= :rejected (:status output)))
      (is (= :source-m2-call-parity
             (get-in output [:rejection-evidence :reason])))
      (is (true? (census/checker-output-valid? output)))
      (is (true? (census/valid-output? mutated output)))
      (is (not (.contains (pr-str output) ":certificate-id")))))
  (testing "an internal M2 builtin without provenance fails closed"
    (let [input (envelope)
          instruction (get-in input [:m2s 0 :functions 'a0 :instructions 1])
          changed-plan
          (-> (get-in input [:m2s 0])
              (assoc-in [:functions 'a0 :instructions 1]
                        (select-keys instruction [:op :function :args]))
              (assoc :plan-id nil))
          changed-plan
          (assoc changed-plan :plan-id (census/m2-plan-id changed-plan))
          new-plan-id (:plan-id changed-plan)
          mutated
          (-> input
              (assoc-in [:m2s 0] changed-plan)
              (assoc-in [:bundle-core :module-plans 0 :plan-id] new-plan-id)
              (assoc-in [:bundle-core :routes 0 :caller-plan-id] new-plan-id)
              (assoc-in [:bundle-core :public-roots 0 :plan-id] new-plan-id)
              (assoc-in [:bundle-core :public-roots 1 :plan-id] new-plan-id))
          output (produce mutated)]
      (is (= :rejected (:status output)))
      (is (= :m2-provenance-presence-mismatch
             (get-in output [:rejection-evidence :reason])))
      (is (true? (census/checker-output-valid? output)))
      (is (true? (census/valid-output? mutated output)))
      (is (not (.contains (pr-str output) ":certificate-id"))))))

(deftest checker-b-bounds-admit-at-limit-and-reject-first-plus-one
  (let [certificate-profile
        {:nodes 2048 :depth 12 :width 35 :collection-count 64
         :scalar-utf8 4096 :canonical-reader-bytes 262144}
        transcript-counts
        {:node-count 262144 :record-count 262144 :call-row-count 65536
         :byte-count 268435456}]
    (is (true? (census/certificate-profile-within? certificate-profile)))
    (doseq [field [:nodes :depth :width :collection-count :scalar-utf8
                   :canonical-reader-bytes]]
      (is (false? (census/certificate-profile-within?
                   (update certificate-profile field inc))) (str field)))
    (is (true? (census/transcript-counts-within? transcript-counts)))
    (doseq [field [:node-count :record-count :call-row-count :byte-count]]
      (is (false? (census/transcript-counts-within?
                   (update transcript-counts field inc))) (str field)))))
