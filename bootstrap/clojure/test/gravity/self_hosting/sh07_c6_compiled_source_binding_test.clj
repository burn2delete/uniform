(ns gravity.self-hosting.sh07-c6-compiled-source-binding-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(deftest function-manifest-covers-a-table-with-257-definitions
  (let [source "/virtual/large-function-manifest.gravity"
        source-hash (str "sha256:" (bootstrap/sha256-hex "function manifest fixture"))
        entries (mapv (fn [value]
                        (let [name (symbol (str "fixture-" value))]
                          [name {:name name :arity 0 :params []
                                 :instructions [{:op :literal :value value}]}]))
                      (range 1000 1257))
        names (mapv first entries)
        last-name (peek names)
        functions (apply array-map (mapcat identity entries))
        reordered-functions (apply array-map (mapcat identity (rseq entries)))
        manifest-for #(bootstrap/p15-s23-c6c10-function-merkle-manifest
                       source source-hash %)
        plan {:source {:path source :sha256 source-hash}
              :module {:source-path source}
              :functions functions}
        pin-digest (fn [plan manifest]
                     (bootstrap/p15-s23-c6c10-canonical-digest
                      source
                      (bootstrap/p15-s23-c6c10-plan-pin-input
                       source-hash plan manifest)))
        manifest (manifest-for functions)
        chunks (:chunks manifest)]
    (testing "every function appears once in canonical order"
      (is (= 257 (:function-count manifest)))
      (is (= [64 64 64 64 1] (mapv :count chunks)))
      (is (= names (mapv :name (mapcat :records chunks)))))
    (testing "input order does not affect the manifest root or plan pin"
      (let [reordered-manifest (manifest-for reordered-functions)]
        (is (not= (keys functions) (keys reordered-functions)))
        (is (= (:root-digest manifest) (:root-digest reordered-manifest)))
        (is (= (pin-digest plan manifest)
               (pin-digest (assoc plan :functions reordered-functions)
                           reordered-manifest)))))
    (testing "changing the last definition reaches the plan pin"
      (let [changed-plan (assoc-in plan [:functions last-name :instructions 0 :value] -1)
            changed-manifest (manifest-for (:functions changed-plan))
            changed-chunks (:chunks changed-manifest)]
        (is (= (pop chunks) (pop changed-chunks)))
        (is (not= (:chunk-digest (peek chunks))
                  (:chunk-digest (peek changed-chunks))))
        (is (not= (:root-digest manifest) (:root-digest changed-manifest)))
        (is (not= (pin-digest plan manifest)
                  (pin-digest changed-plan changed-manifest)))))
    (doseq [[label invalid-functions]
            [["non-map definition" (assoc functions last-name :invalid)]
             ["non-symbol key" (assoc (dissoc functions last-name)
                                      :invalid (get functions last-name))]]]
      (testing label
        (let [diagnostic (try
                           (manifest-for invalid-functions)
                           nil
                           (catch clojure.lang.ExceptionInfo error (ex-data error)))]
          (is (= "C6-VERIFY" (:id diagnostic)))
          (is (= :bounded-compiled-function-table (:missing-fact diagnostic)))
          (is (= source (get-in diagnostic [:source-span :source]))))))))

;; Ordinary development binding maintenance, not C6 subject construction,
;; current expression-emitter validation, or renewed bootstrap evidence.
(deftest sh07-c6-compiled-source-binding-retains-full-plan-identity
  (let [source "/virtual/sh07-c6-compiled-source-binding.gravity"
        compiled (bootstrap/p15-s23-c6c10-compile-source-binding source)
        manifest (:function-manifest compiled)
        preimage (bootstrap/p15-s23-c6c10-plan-pin-input
                  (:source-content-hash compiled) (:plan compiled) manifest)
        digest #(bootstrap/p15-s23-c6c10-canonical-digest source %)
        reject (fn [candidate]
                 (try
                   (bootstrap/p15-s23-c6c10-validate-source-binding! candidate)
                   nil
                   (catch clojure.lang.ExceptionInfo error (ex-data error))))
        check-rejected (fn [candidate]
                         (let [diagnostic (reject candidate)]
                           (is (= "C6-VERIFY" (:id diagnostic)))
                           (is (= :exact-pinned-gravity-c6-c10-source-binding
                                  (:missing-fact diagnostic)))
                           (is (= source (get-in diagnostic [:source-span :source])))))
        changed-preimage
        (fn [new-preimage]
          ;; Independently re-digest the changed full preimage. Never rebind an
          ;; expected pin, override authentication, or submit a cached result.
          (assoc compiled :plan-semantic-hash (digest new-preimage)))
        compiler-identities
        {:artifact-hash
         "sha256:e25ae2c9b7853627f8f9db45eb916b6b3775e28b51fbe6e29ecd734598ca8ac2"
         :source-content-hash
         "sha256:e273535bc06e5f67eb5a54ddcba0ef726814bc9dbe484df73bf7d3ab56d1e949"
         :semantic-hash
         "sha256:2004f388034a182483d7b9c28f4e6d6baa76126d29867c50183b0329fa5e0ddc"}
        old-preimage
        (update-in preimage [:plan :compiler]
                   #(apply dissoc % (keys compiler-identities)))
        forged-hash
        "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"]
    (is (identical? compiled
                    (bootstrap/p15-s23-c6c10-validate-source-binding! compiled)))
    (is (= 141562 (:source-byte-count compiled)))
    (doseq [[field expected]
            {:source-content-hash
             "sha256:0299511c26c8b2a191309a4a4358528de397c88551ed98a2aed03d172067b2a5"
             :plan-semantic-hash
             "sha256:4465715d7fb62e90667455a825915a93b530b3c395c812ce641744ece0aa0ed8"
             :functions-semantic-hash
             "sha256:79984c3b2cfd56f64ee161cb6d9611be55c2a8d614120c9ebd2477d5835a4aff"
             :builder-semantic-hash
             "sha256:1e14438321405f6f0126fab7ddebd6e4f1450e6074f5a832a56ec53ed64252f7"
             :verifier-semantic-hash
             "sha256:5695bc710e197df7b43efb9288480db43ae1e56a1da4d54a12180fd9e3b6906a"}]
      (is (= expected (get compiled field))))
    (is (= '{c6-c10-build-checked-core-template {:arity 1 :params [input]}
             c6-c10-verify-checked-core-template
             {:arity 3 :params [input template requests]}}
           (:function-shapes compiled)))
    (is (= 139 (:function-count manifest) (:function-count preimage)))
    (is (= 64 (:chunk-size manifest)))
    (is (= 3 (:chunk-count manifest)))
    (is (= [64 64 11] (mapv :count (:chunks manifest))))
    (is (= (:functions-semantic-hash compiled) (:root-digest manifest)
           (:function-manifest-root preimage)))
    (is (= (:plan-semantic-hash compiled) (digest preimage)))
    (is (= #{:domain :source-content-hash :function-count
             :function-manifest-root :plan}
           (set (keys preimage))))
    (is (= #{:kind :compiler-artifact-plan? :entrypoint :source :compiler
             :module :instruction-summary :effect-summary}
           (set (keys (:plan preimage)))))
    (is (= (merge {:owner :gravity-source
                   :stage :p15-s23-stage2-expression-lowering
                   :compiled-by :clojure-stage0-seed
                   :generic-bridge-residual? true}
                  compiler-identities)
           (get-in preimage [:plan :compiler])))
    ;; The retained Git constructor's four-key compiler map reproduces the
    ;; historical digest, but cannot pass the CURRENT exact aggregate gate.
    (is (= "sha256:c0abe759e32feb1810cbb477bba2e1db6bf41735b5e06beed8f0308babd24339"
           (digest old-preimage)))
    (check-rejected (changed-preimage old-preimage))
    (doseq [key (keys compiler-identities)]
      (let [mutant (assoc-in preimage [:plan :compiler key] forged-hash)]
        (is (not= (:plan-semantic-hash compiled) (digest mutant)))
        (check-rejected (changed-preimage mutant))))
    (doseq [mutant [(assoc compiled :source-byte-count 141563)
                    (assoc compiled :source-content-hash forged-hash)
                    (assoc compiled :functions-semantic-hash forged-hash)
                    (assoc compiled :builder-semantic-hash forged-hash)
                    (assoc compiled :verifier-semantic-hash forged-hash)
                    (assoc-in compiled [:function-shapes
                                        'c6-c10-build-checked-core-template
                                        :arity] 2)
                    (assoc-in compiled [:function-manifest :function-count] 140)
                    (assoc-in compiled [:function-manifest :chunks 0 :count] 65)]]
      (check-rejected mutant))))
