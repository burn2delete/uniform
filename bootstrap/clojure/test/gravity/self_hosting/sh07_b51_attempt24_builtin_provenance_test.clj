(ns gravity.self-hosting.sh07-b51-attempt24-builtin-provenance-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(def checked-core-path
  "bootstrap/gravity/src/gravity/checked_core.gravity")

(defn- exception-id [f]
  (try
    (f)
    :accepted
    (catch clojure.lang.ExceptionInfo error
      (:id (ex-data error)))
    (catch Throwable error
      [:escaped-host-exception (class error)])))

(defn- parsed-source [path]
  (let [text (slurp path)
        macro-artifact (bootstrap/macro-source-artifact path text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))]
    {:path path
     :text text
     :module module
     :table (bootstrap/stage0-function-table module)}))

(defn- pinned-stage-plan [contract]
  {:compiler-artifact-plan? true
   :kind :gravity/stage2-compiler-artifact-plan
   :plan-id (:plan-id contract)
   :source {:byte-count (:source-byte-count contract)
            :sha256 (:source-content-hash contract)}
   :module {:module (:module contract)
            :profile :meta :target :jvm :effects #{} :capabilities #{}
            :safety :safe}
   :compiler {:stage :p15-s23-stage2-expression-lowering
              (:compiler-source-field contract)
              (:emitter-source-content-hash contract)
              (:compiler-semantic-field contract)
              (:emitter-semantic-hash contract)}})

(def attempt24-authority
  (delay (bootstrap/p15-s23-sh07-b51-build-link-authority)))

(defn- v2-fixture []
  (let [link-authority @attempt24-authority
        descriptors (:module-source-descriptors link-authority)
        descriptor
        (first (filter #(some :compiler-internal? (:builtin-calls %))
                       descriptors))
        ordinal (:module-ordinal descriptor)
        source (nth (:sources link-authority) ordinal)
        builtin-row (first (filter :compiler-internal?
                                   (:builtin-calls descriptor)))
        module
        (assoc (:module source)
               :function-table (:table source)
               :p15-s23-sh07-module-source-descriptor descriptor
               :p15-s23-sh07-link-manifest (:link-manifest link-authority))
        source-authority
        (bootstrap/p15-s23-sh07-compiler-source-authority
         (:path source) (:text source) module (:table source))
        module (assoc module :p15-s23-sh07-source-authority source-authority)
        definition (get (:table source) (:caller builtin-row))
        provenance
        (bootstrap/p15-s23-sh07-compiler-internal-provenance
         module definition (:builtin builtin-row) (:caller builtin-row))
        instruction
        {:op :builtin-call
         :function (:builtin builtin-row)
         :compiler-internal-caller (:caller builtin-row)
         :compiler-internal-provenance provenance
         :args []}
        compiled-definition (assoc definition :instructions [instruction])
        plan {:source {:sha256 (:source-sha256 descriptor)}
              :functions {(:caller builtin-row) compiled-definition}}]
    {:descriptor descriptor
     :manifest (:link-manifest link-authority)
     :module module
     :definition definition
     :builtin-row builtin-row
     :source-authority source-authority
     :provenance provenance
     :instruction instruction
     :compiled-definition compiled-definition
     :plan plan}))

(deftest frozen-b47-v1-source-authority-remains-exact
  (let [{:keys [path text module table]} (parsed-source checked-core-path)
        authority
        (bootstrap/p15-s23-sh07-compiler-source-authority
         path text module table)]
    (is (= #{:artifact :source-relative-path :source-sha256
             :source-byte-count :module :function-source-hashes}
           (set (keys authority))))
    (is (= :gravity/sh07-compiler-source-authority-v1
           (:artifact authority)))
    (is (= checked-core-path (:source-relative-path authority)))
    (is (= 444325 (:source-byte-count authority)))
    (is (= 'gravity.checked-core (:module authority)))
    (is (= 305 (count (:function-source-hashes authority))))
    (is (= "L2-UNKNOWN-CORE-FORM"
           (exception-id
            #(bootstrap/p15-s23-sh07-compiler-source-authority
              path (str text "\n") module table))))
    (is (= "L2-UNKNOWN-CORE-FORM"
           (exception-id
            #(bootstrap/p15-s23-sh07-compiler-source-authority
              path text (assoc module :module 'fabricated.module) table))))))

(deftest sh02-through-sh06-augmented-plan-authority-is-a-closed-pin-table
  (let [contracts
        (bootstrap/p15-s23-stage2-pinned-augmented-plan-contracts)]
    (is (= [:sh02 :sh03 :sh04 :sh05 :sh06]
           (mapv :stage contracts)))
    (is (= 5 (count contracts)
           (count (set (map :module contracts)))
           (count (set (map :plan-id contracts)))))
    (is (every?
         bootstrap/p15-s23-stage2-pinned-augmented-plan-contract?
         contracts))
    (doseq [[ordinal contract] (map-indexed vector contracts)]
      (let [plan (pinned-stage-plan contract)
            other (nth contracts (mod (inc ordinal) (count contracts)))]
        (is (true?
             (bootstrap/p15-s23-stage2-pinned-augmented-plan-authorized?
              plan))
            (:stage contract))
        (is (true?
             (bootstrap/p15-s23-stage2-compiler-artifact-plan-context?
              plan))
            (:stage contract))
        (doseq [mutated
                [(assoc-in plan [:module :module] (:module other))
                 (update-in plan [:source :byte-count] inc)
                 (assoc-in plan [:source :sha256]
                           (:source-content-hash other))
                 (assoc plan :plan-id (:plan-id other))
                 (assoc-in plan
                           [:compiler (:compiler-source-field contract)]
                           (str "sha256:" (apply str (repeat 64 "a"))))
                 (assoc-in plan
                           [:compiler (:compiler-semantic-field contract)]
                           (str "sha256:" (apply str (repeat 64 "b"))))]]
          (is (false?
              (bootstrap/p15-s23-stage2-pinned-augmented-plan-authorized?
                mutated))
              [(:stage contract) mutated]))))))

(deftest b51-v2-provenance-is-closed-and-source-authenticated
  (let [{:keys [descriptor manifest module definition builtin-row
                source-authority provenance]}
        (v2-fixture)
        caller (:caller builtin-row)
        callee (:builtin builtin-row)
        authorized?
        (fn [candidate-module candidate-caller candidate-callee]
          (bootstrap/p15-s23-sh07-compiler-internal-caller-authorized?
           candidate-module definition candidate-callee candidate-caller))]
    (is (= #{:artifact :manifest-id :module-source-id :source-sha256
             :function-source-hashes :module :logical-source-path}
           (set (keys source-authority))))
    (is (= #{:artifact :manifest-id :module-source-id :source-sha256
             :function :function-source-hash :callee}
           (set (keys provenance))))
    (is (= :gravity/sh07-compiler-internal-call-provenance-v2
           (:artifact provenance)))
    (is (true? (authorized? module caller callee)))
    (testing "matching names without exact source authority are never enough"
      (is (false? (authorized? (dissoc module :p15-s23-sh07-source-authority)
                               caller callee)))
      (is (false? (authorized? module 'fabricated-caller callee)))
      (is (false? (authorized? module caller 'fabricated-builtin))))
    (doseq [[field value]
            [[:artifact :gravity/fabricated]
             [:manifest-id (str "sha256:" (apply str (repeat 64 "a")))]
             [:module-source-id (str "sha256:" (apply str (repeat 64 "b")))]
             [:source-sha256 (str "sha256:" (apply str (repeat 64 "c")))]
             [:module 'fabricated.module]
             [:logical-source-path "fabricated.gravity"]
             [:function-source-hashes {}]]]
      (is (false?
           (authorized?
            (assoc module :p15-s23-sh07-source-authority
                   (assoc source-authority field value))
            caller callee))
          field))
    (is (false?
         (authorized?
          (assoc module :p15-s23-sh07-source-authority
                 (assoc source-authority :extra :forbidden))
          caller callee)))
    (is (false?
         (authorized?
          (assoc module :p15-s23-sh07-module-source-descriptor
                 (assoc descriptor :source-byte-count 0))
          caller callee)))
    (is (false?
         (authorized?
          (assoc module :p15-s23-sh07-link-manifest
                 (assoc manifest :manifest-id
                        (str "sha256:" (apply str (repeat 64 "d")))))
          caller callee)))))

(deftest b51-v2-runtime-occurrence-and-mutations-fail-closed
  (let [{:keys [descriptor manifest plan instruction provenance]}
        (v2-fixture)
        provenance-authentic?
        (var-get
         (ns-resolve 'gravity.bootstrap
                     'p15-s23-sh07-runtime-v2-provenance-authentic?))
        invoke
        (fn [candidate]
          (exception-id
           #(bootstrap/p15-s23-sh07-runtime-invoke-compiler-internal-builtin
             {} plan candidate [])))]
    (is (true? (provenance-authentic?
                descriptor manifest plan instruction)))
    (let [quoted-plan
          (assoc-in plan
                    [:functions (:compiler-internal-caller instruction)
                     :instructions]
                    [{:op :quote :value instruction}])]
      (is (false? (provenance-authentic?
                   descriptor manifest quoted-plan instruction))))
    (doseq [[field value]
            [[:artifact :gravity/fabricated]
             [:manifest-id (str "sha256:" (apply str (repeat 64 "1")))]
             [:module-source-id (str "sha256:" (apply str (repeat 64 "2")))]
             [:source-sha256 (str "sha256:" (apply str (repeat 64 "3")))]
             [:function 'fabricated-caller]
             [:function-source-hash
              (str "sha256:" (apply str (repeat 64 "4")))]
             [:callee 'fabricated-builtin]]]
      (let [candidate
            (assoc instruction :compiler-internal-provenance
                   (assoc provenance field value))]
        (is (false? (provenance-authentic?
                     descriptor manifest plan candidate)) field)
        (is (= "L2-BUILTIN-ERROR" (invoke candidate)) field)))
    (let [candidate
          (assoc instruction :compiler-internal-provenance
                 (assoc provenance :extra :forbidden))]
      (is (false? (provenance-authentic?
                   descriptor manifest plan candidate)))
      (is (= "L2-BUILTIN-ERROR" (invoke candidate))))
    (is (= "L2-BUILTIN-ERROR" (invoke {})))
    (is (= "L2-BUILTIN-ERROR"
           (invoke (assoc instruction :extra :forbidden))))))
