(ns gravity.self-hosting.sh07-b51-attempt24-plan-linker-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(def authority-value
  (delay (bootstrap/p15-s23-sh07-b51-build-link-authority)))

(defn authority [] @authority-value)

(def current-emitter-semantic-hash
  "sha256:866309d5e875149882648e3ed2126a6590c1ffc4ac6b2f6a60018b15203813e2")
(def current-emitter-source-hash
  "sha256:b79945124e095079763c23c7316ec7b0a84b91d9fd2d01c45002a24c01d5c1d9")

(defn with-current-emitter-pins [f]
  (f))

(def authentic-linked-products
  (delay
    (bootstrap/p15-s23-sh07-b51-build-execution-bundle)))

(def authentic-emitter-artifact-plan
  (delay
    (with-current-emitter-pins
      (fn []
        (let [compiler-source "bootstrap/gravity/p15_s23/compiler.gravity"
              emitter-source "bootstrap/gravity/p15_s23/emitter.gravity"
              forms
              (:forms
               (bootstrap/p15-s23-compiler-source-form-record-from-text
                compiler-source (slurp compiler-source)))
              emitter
              (bootstrap/p15-s23-compiler-def-value
               compiler-source forms 'p15-s23-stage2-plan-emitter)]
          (bootstrap/p15-s23-stage2-compiler-artifact-plan
           (bootstrap/p15-s23-stage2-compiler-artifact-augmented-emitter
            emitter)
           emitter-source (slurp emitter-source)))))))

(deftest exact-seven-module-authority-is-closed
  (let [{:keys [module-source-descriptors link-manifest]} (authority)]
    (is (= 7 (count module-source-descriptors)))
    (is (= [2112 2398 2365 2295 1981 2020 816]
           (mapv :aggregate-binding-count module-source-descriptors)))
    (is (= [294 254 282 215 181 164 61]
           (mapv (comp count :definitions) module-source-descriptors)))
    (is (= [262 262 262 262 262 262 262]
           (mapv :core-binding-count module-source-descriptors)))
    (is (= [1555 1825 1718 1692 1535 1542 481]
           (mapv :lexical-binding-count module-source-descriptors)))
    (is (= [1 57 103 126 3 52 12]
           (mapv (comp count :external-bindings)
                 module-source-descriptors)))
    (is (= 1423 (count (:edges link-manifest))))
    (is (bootstrap/p15-s23-sh07-b51-link-manifest-valid?
         module-source-descriptors link-manifest))
    (is (= (vec (range 1423)) (mapv :ordinal (:edges link-manifest))))
    (is (= 1
           (count
            (filter #(and (= 'sh07-b51-finalize-rejection
                             (:caller-function %))
                          (= 'sh07-b51-build-template
                             (:callee-function %)))
                    (:edges link-manifest)))))))

(deftest link-identity-and-callsite-mutations-fail-closed
  (let [{:keys [module-source-descriptors link-manifest]} (authority)
        edges (:edges link-manifest)
        first-edge (first edges)
        second-edge (second edges)
        swapped (assoc link-manifest :edges
                       (assoc edges 0 second-edge 1 first-edge))
        duplicate (assoc link-manifest :edges
                         (assoc edges 1 (assoc first-edge :ordinal 1)))
        wrong-path (assoc-in link-manifest [:edges 0 :callsite-path]
                             [999])]
    (testing "downstream fields cannot reseal a noncanonical ordinal swap"
      (is (false? (bootstrap/p15-s23-sh07-b51-link-manifest-valid?
                   module-source-descriptors
                   (assoc swapped :manifest-id
                          (bootstrap/p15-s23-sh07-b51-manifest-id swapped))))))
    (is (false? (bootstrap/p15-s23-sh07-b51-link-manifest-valid?
                 module-source-descriptors
                 (assoc duplicate :manifest-id
                        (bootstrap/p15-s23-sh07-b51-manifest-id duplicate)))))
    (is (false? (bootstrap/p15-s23-sh07-b51-link-manifest-valid?
                 module-source-descriptors
                 (assoc wrong-path :manifest-id
                        (bootstrap/p15-s23-sh07-b51-manifest-id wrong-path)))))
    (is (false? (bootstrap/p15-s23-sh07-b51-link-manifest-valid?
                 (assoc module-source-descriptors 0
                        (assoc (first module-source-descriptors)
                               :source-byte-count 0))
                 link-manifest)))))

(deftest external-instruction-is-exactly-manifest-bound
  (let [{:keys [module-source-descriptors link-manifest]} (authority)
        edge (first (:edges link-manifest))
        descriptor
        (first (filter #(= (:caller-module-source-id edge)
                           (:module-source-id %))
                       module-source-descriptors))
        module {:p15-s23-sh07-module-source-descriptor descriptor
                :p15-s23-sh07-link-manifest link-manifest}
        link (bootstrap/p15-s23-sh07-b51-external-instruction
              module (:caller-function edge) (:callee-function edge)
              (:arity edge) (:callsite-path edge))]
    (is (= {:artifact :gravity/sh07-b51-external-call-v1
            :manifest-id (:manifest-id link-manifest)
            :edge-ordinal (:ordinal edge)}
           link))
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"unauthenticated"
         (bootstrap/p15-s23-sh07-b51-external-instruction
         module (:caller-function edge) (:callee-function edge)
          (:arity edge) (conj (:callsite-path edge) 0))))))

(deftest malformed-link-carriers-are-total-and-fail-closed
  (let [{:keys [module-source-descriptors link-manifest]} (authority)
        descriptor (first module-source-descriptors)
        malformed (assoc descriptor :lexical-binding-count [])
        instruction {:op :function-call
                     :function 'sh07-b51-build-template
                     :args []
                     :external-link
                     {:artifact :gravity/sh07-b51-external-call-v1
                      :manifest-id (:manifest-id link-manifest)
                      :edge-ordinal -1}}]
    (is (false? (bootstrap/p15-s23-sh07-b51-module-source-valid?
                 malformed)))
    (is (false? (bootstrap/p15-s23-sh07-b51-link-manifest-valid?
                 [] {:edges []})))
    (try
      (bootstrap/p15-s23-sh07-b51-runtime-authorize-external-link
       {} {} instruction)
      (is false "negative ordinal must reject")
      (catch clojure.lang.ExceptionInfo error
        (is (= "L2-UNKNOWN-CORE-FORM" (:id (ex-data error))))
        (is (= :unauthenticated-b51-external-link
               (:reason (ex-data error))))))))

(deftest no-recoverable-var-can-seal-caller-supplied-m2-or-m3
  (let [forbidden
        '[p15-s23-sh07-b51-seal-plan
          p15-s23-sh07-b51-seal-execution-bundle
          p15-s23-sh07-b51-seal-execution-bundle*
          p15-s23-sh07-b51-plan-id
          p15-s23-sh07-b51-bundle-id
          p15-s23-sh07-b51-compilation-authority-token
          *p15-s23-sh07-b51-compilation-authority*
          p15-s23-sh07-b51-compilation-authorized?]]
    (doseq [symbol forbidden]
      (is (nil? (ns-resolve 'gravity.bootstrap symbol))
          (str symbol " must not be recoverable with ns-resolve/var-get")))))

(deftest authentic-seven-plan-compilation-is-the-only-m2-m3-authority
  (with-current-emitter-pins
    (fn []
      (let [products @authentic-linked-products
            descriptors (:module-source-descriptors products)
            manifest (:link-manifest products)
            plans (:module-plans products)
            bundle (:execution-bundle products)]
        (is (= [294 254 282 215 181 164 61]
               (mapv #(count (:functions %)) plans)))
        (is (= 1423 (count (:routes bundle))))
        (is (bootstrap/p15-s23-sh07-b51-execution-bundle-valid?
             descriptors manifest plans bundle))
        (testing "a coherently rehashed caller plan and all M3 references reject"
          (let [forged-plan-base (assoc (first plans) :kind :caller-forged)
                forged-plan
                (assoc forged-plan-base :plan-id
                       (bootstrap/c4-artifact-id
                        (bootstrap/c-backend-canonical-value
                         (-> forged-plan-base
                             (dissoc :plan-id)
                             (update :source dissoc :path)
                             (update :module dissoc :source-path)))))
                old-plan-id (:plan-id (first plans))
                new-plan-id (:plan-id forged-plan)
                forged-plans (assoc plans 0 forged-plan)
                forged-bundle-base
                (-> bundle
                    (dissoc :bundle-id)
                    (update :module-plans
                            (fn [rows]
                              (mapv #(if (= old-plan-id (:plan-id %))
                                       (assoc % :plan-id new-plan-id) %)
                                    rows)))
                    (update :routes
                            (fn [rows]
                              (mapv #(cond-> %
                                       (= old-plan-id (:caller-plan-id %))
                                       (assoc :caller-plan-id new-plan-id)
                                       (= old-plan-id (:callee-plan-id %))
                                       (assoc :callee-plan-id new-plan-id))
                                    rows)))
                    (update :public-roots
                            (fn [rows]
                              (mapv #(if (= old-plan-id (:plan-id %))
                                       (assoc % :plan-id new-plan-id) %)
                                    rows))))
                forged-bundle
                (assoc forged-bundle-base :bundle-id
                       (bootstrap/p15-s23-sh07-b51-canonical-id
                        forged-bundle-base))]
            (is (false?
                 (bootstrap/p15-s23-sh07-b51-execution-bundle-valid?
                  descriptors manifest forged-plans forged-bundle)))))))))

(deftest dedicated-emitter-runtime-context-is-exact-identity-only
  (with-current-emitter-pins
    (fn []
      (let [plan @authentic-emitter-artifact-plan]
        (is (bootstrap/p15-s23-stage2-compiler-artifact-plan-context? plan))
        (is (false?
             (bootstrap/p15-s23-stage2-compiler-artifact-plan-context?
              (assoc-in plan [:module :module] 'caller.forged))))
        (is (false?
             (bootstrap/p15-s23-stage2-compiler-artifact-plan-context?
              (assoc-in plan [:compiler :semantic-hash]
                        "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))))))))
