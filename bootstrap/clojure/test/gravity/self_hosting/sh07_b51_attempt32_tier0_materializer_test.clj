(ns gravity.self-hosting.sh07-b51-attempt32-tier0-materializer-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.tools.sh07-b51-attempt32-tier0-materializer :as tier0]))

(defn- sha [c]
  (str "sha256:" (apply str (repeat 64 c))))

(def content
  {:commit (apply str (repeat 40 "a"))
   :tree (apply str (repeat 40 "b"))})

(def lowering-product
  {:schema :gravity/sh07-b51-lowering-product-v1
   :content content :source-authority {} :module-products []
   :pass-records [] :fact-records [] :provenance-records []
   :fuel-subjects [] :semantic-roots {} :product-id (sha "c")})

(def witness
  (let [operations
        (mapv (fn [ordinal]
                {:ordinal ordinal :owner (nth [:l1 :l2 :c2 :c6 :boot2
                                               :boot3 :boot4] ordinal)
                 :kind :bounded-operation :subject-id (sha "d")
                 :counter-keys [] :algorithm-source-root (sha "e")
                 :lowering-product-id (:product-id lowering-product)
                 :operation-id (str "sha256:" (format "%064x" ordinal))})
              (range 7))]
    {:schema :gravity/sh07-b51-authentic-lowering-witness-v1
     :content content :operation-manifest operations
     :operation-schedule-id (sha "f")
     :fuel-receipts
     (mapv (fn [operation]
             {:operation-id (:operation-id operation) :within-bounds true})
           operations)
     :l1 {} :l2 {} :c2 {} :c6 {} :boot2 {} :boot3 {} :boot4 {}
     :witness-id (sha "1")}))

(defn- role-checker [role]
  (let [a? (= role :producer-census)]
    {:artifact-id (sha (if a? "2" "7"))
     :source-sha256 (sha (if a? "3" "8"))
     :semantic-id (sha (if a? "4" "9"))
     :executable-id (sha (if a? "5" "a"))
     :dependency-closure-id (sha (if a? "6" "b"))
     :algorithm-role (if a? :source-first-exhaustive
                           :m2-first-independent-exhaustive)}))

(defn- certificate [role]
  {:artifact :gravity/sh07-b51-compile-census-certificate
   :domain (if (= role :producer-census)
             :gravity/sh07-b51-producer-census-certificate-v1
             :gravity/sh07-b51-independent-verifier-census-certificate-v1)
   :schema-version 1 :status :accepted :role role
   :architecture-report-sha256 (sha "0") :claim-id (sha "1")
   :claim {:normalized-census-id (sha "2") :physical-census {:rows 1}
           :category-schedule [:source] :category-commitments [:root]}
   :assumptions {:closed true} :checker (role-checker role)
   :inputs (merge {:snapshot-commit (:commit content)
                   :snapshot-tree (:tree content)}
                  {:architecture-report-sha256 (sha "0")
                   :lowering-product-id (:product-id lowering-product)
                   :predecessor-executable-descriptor-id
                   tier0/predecessor-executable-descriptor-id
                   :module-source-identities [] :module-source-byte-counts []
                   :emitter-source-id (sha "3") :emitter-semantic-id (sha "4")
                   :m0-ids [] :m1-id (sha "5") :m2-ids []
                   :bundle-core-id (sha "6")})
   :result {:normalized-census-id (sha "2")}
   :semantic-witness witness :provenance {}
   :raw-source-transcript {} :m2-transcript {} :profile-bounds {}
   :evidence-root (sha "7")
   :certificate-id (sha (if (= role :producer-census) "8" "9"))})

(defn- child-values []
  (let [a (certificate :producer-census)
        b (certificate :independent-verifier-census)]
    {:producer-output {:status :accepted :certificate a}
     :independent-output {:status :accepted :certificate b}
     :producer-identity {:identity-manifest-id (sha "a")
                         :checker (:checker a)}
     :independent-identity {:identity-manifest-id (sha "b")
                            :checker (:checker b)}
     :producer-lowering-product lowering-product
     :independent-lowering-product lowering-product
     :reconstructed-lowering-product lowering-product
     :reconstructed-witness witness}))

(deftest tier0-source-closure-is-parent-safe
  (let [source
        (slurp (io/resource
                "gravity/tools/sh07_b51_attempt32_tier0_materializer.clj"))]
    (is (not (re-find #"sh07-b51-attempt30-(producer|independent)-census"
                      source)))
    (is (not (re-find #"sh07-b51-attempt32-checker-identity" source)))
    (is (not (re-find #"requiring-resolve|ns-resolve|find-var|load-string"
                      source)))
    (is (empty? (ns-aliases
                 'gravity.tools.sh07-b51-attempt32-tier0-materializer)))))

(deftest already-produced-pair-joins-are-authentic-role-neutral
  (let [values (child-values)
        authority {:H (constantly (sha "f"))
                   :certificate-valid? (constantly true)
                   :certificate-pair-valid? (constantly true)}
        result (tier0/validate-produced-child-pair! authority values)]
    (is (= [:producer-census :independent-verifier-census]
           (mapv :role (:certificates result))))
    (is (= witness
           (get-in result [:semantic-witness-runtime :witness])))
    (is (true? (get-in result [:semantic-witness-runtime :equal])))
    (testing "role labels cannot authenticate swapped inert output"
      (is (thrown? clojure.lang.ExceptionInfo
                   (tier0/validate-produced-child-pair!
                    authority
                    (assoc values
                           :producer-output (:independent-output values)
                           :independent-output (:producer-output values))))))
    (testing "child agreement cannot replace M's reconstructed product"
      (is (thrown? clojure.lang.ExceptionInfo
                   (tier0/validate-produced-child-pair!
                    authority
                    (assoc-in values [:reconstructed-lowering-product
                                      :product-id] (sha "0"))))))
    (testing "a byte-different witness rejects even if both statuses accept"
      (is (thrown? clojure.lang.ExceptionInfo
                   (tier0/validate-produced-child-pair!
                    authority
                    (assoc-in values [:independent-output :certificate
                                      :semantic-witness :boot4]
                              {:forged true})))))))

(deftest bad-actual-requires-exact-nil-row-and-output
  (let [actual {:status :rejected :code "SH07-B51-EDGE-AUTHORITY"
                :rule "L2-UNKNOWN-CORE-FORM"
                :reason :unauthenticated-b51-external-link
                :input-id (sha "1") :mutated-id (sha "2")
                :expected {:module 1} :actual {:module 2}
                :committed-row nil :output nil}
        construct! (fn [& _]
                     {:bad-actual actual :bad-actual-expected actual})]
    (is (= actual (tier0/validate-bad-actual! actual actual)))
    (is (= actual
           (:bad-actual
            (tier0/construct-negative-edge-evidence!
             construct! {} [] [] []))))
    (doseq [mutation [(assoc actual :committed-row {})
                      (assoc actual :output false)
                      (dissoc actual :output)
                      (assoc actual :receipt-id (sha "3"))]]
      (is (thrown? clojure.lang.ExceptionInfo
                   (tier0/validate-bad-actual! mutation))))))

(deftest owner-and-fuel-apis-enter-only-as-passed-values
  (let [calls (atom [])
        api {:build-owner-registry
             (fn [transactions]
               (swap! calls conj [:registry transactions]) [:registry])
             :build-owner-selection
             (fn [roots core registry]
               (swap! calls conj [:selection roots core registry]) :selection)
             :build-fuel-subjects
             (fn [roots stages collections]
               (swap! calls conj [:subjects roots stages collections])
               [:subject])
             :build-fixed-fuel-operation-specs
             (fn [subjects]
               (swap! calls conj [:operations subjects]) [:operation])}
        inputs {:module-transactions [:transactions] :root-specs [:roots]
                :bundle-core :core :fuel-source-roots :source-roots
                :pre-stages :stages :collections-by-owner :collections}]
    (is (= {:owner-registry [:registry] :owner-selection :selection
            :fuel-subjects [:subject] :operation-specs [:operation]}
           (tier0/reconstruct-parent-safe-v9-inputs! api inputs)))
    (is (= [[:registry [:transactions]]
            [:selection [:roots] :core [:registry]]
            [:subjects :source-roots :stages :collections]
            [:operations [:subject]]]
           @calls))))

(deftest legacy-same-process-materialization-fails-closed
  (is (= :parent-safe-external-child-required
         (try (tier0/materialize-current {}) nil
              (catch clojure.lang.ExceptionInfo failure
                (:reason (ex-data failure)))))))
