(defn- __gravity_bootstrap_gate_b_b13_identity_phase_03 [state]
  (let [{:syms
         [gate-a
          transaction
          b14
          c18
          files
          content-hashes
          pass-provenance
          pass-pipeline-digest
          compiler-provenance
          dependency-provenance
          build-identity-base
          build-id
          build-identity
          target-common
          target-fingerprint-id
          source-provenance
          compiler-provenance-id
          dependency-provenance-id
          artifact-files
          edge
          source-node
          origin-node
          checked-core-node
          c11-node
          c13-node
          c14-node
          b1-node]} state
        base {:capabilities #{},
              :artifact-files artifact-files,
              :publication-graph
              [{:from "program.c", :to "program.o", :edge :compile}
               {:from "program.o", :to "program", :edge :link}
               {:from "provenance.edn", :to "manifest.edn", :edge :hash-bound}
               {:from "conformance.edn", :to "manifest.edn", :edge :hash-bound}],
              :compiler-provenance compiler-provenance,
              :diagnostics [],
              :pass-provenance
              (assoc pass-provenance :pass-pipeline-digest pass-pipeline-digest),
              :runtime-reference
              (p15-s23-c11-mir-digest (:runtime-provider-evidence transaction)),
              :publication-file-set
              ["program.c"
               "program.h"
               "program.o"
               "program"
               "manifest.edn"
               "provenance.edn"
               "conformance.edn"],
              :abi-layout-reference
              (p15-s23-c11-mir-digest (:abi-evidence transaction)),
              :build-identity build-identity,
              :evidence
              {:abi (p15-s23-c11-mir-digest (:abi-evidence transaction)),
               :runtime
               (p15-s23-c11-mir-digest (:runtime-provider-evidence transaction)),
               :conformance (:record-id b14),
               :compiler-verification (:record-id c18)},
              :schema-version 1,
              :artifact-graph
              [(edge
                 source-node
                 origin-node
                 :source-generated-origin
                 :c2-through-c3
                 :gravity.compiler/front-end)
               (edge
                 origin-node
                 checked-core-node
                 :checked-core
                 :c6-through-c10
                 :gravity.compiler/checked-core)
               (edge
                 checked-core-node
                 c11-node
                 :mir-construction
                 :c11
                 :gravity.compiler/c11)
               (edge
                 c11-node
                 c13-node
                 :identity-optimization
                 :c13
                 :gravity.compiler/c13)
               (edge c13-node c14-node :c-target-lowering :c14 :gravity.compiler/c14)
               (edge c14-node b1-node :backend-authentication :b1 :gravity.backend/b1)
               (edge
                 b1-node
                 (:artifact-id gate-a)
                 :c-source-emission
                 :b2
                 :gravity.backend/b2-c)
               (edge
                 (:artifact-id gate-a)
                 (get-in files [:source :content-hash])
                 :c-translation-unit
                 :b2
                 :gravity.backend/b2-c)
               (edge
                 (:artifact-id gate-a)
                 (get-in files [:header :content-hash])
                 :c-header
                 :b2
                 :gravity.backend/b2-c)
               (edge
                 (get-in files [:header :content-hash])
                 (get-in files [:source :content-hash])
                 :include
                 :c17-preprocessor
                 :apple-clang-21)
               (edge
                 (get-in files [:source :content-hash])
                 (get-in files [:object :content-hash])
                 :compile
                 :c17-codegen
                 :apple-clang-21)
               (edge
                 (get-in files [:object :content-hash])
                 (get-in files [:executable :content-hash])
                 :link
                 :darwin-link
                 :apple-ld-1267)
               (edge
                 build-id
                 (get-in files [:source :content-hash])
                 :bundle-build-identity
                 :b13
                 :gravity.backend/b13)
               (edge
                 build-id
                 (get-in files [:header :content-hash])
                 :bundle-build-identity
                 :b13
                 :gravity.backend/b13)
               (edge
                 build-id
                 (get-in files [:object :content-hash])
                 :bundle-build-identity
                 :b13
                 :gravity.backend/b13)
               (edge
                 build-id
                 (get-in files [:executable :content-hash])
                 :bundle-build-identity
                 :b13
                 :gravity.backend/b13)
               (edge
                 origin-node
                 (get-in files [:source :content-hash])
                 :source-origin-justification
                 :b13
                 :gravity.backend/b13)
               (edge
                 (p15-s23-b2-c17-gate-b-neutral-content-id
                   (:proof-to-c-assumption-map gate-a))
                 (get-in files [:object :content-hash])
                 :proof-justification
                 :b13
                 :gravity.backend/b13)],
              :content-hashes content-hashes,
              :proof
              {:gate-a-contextual-replay :passed,
               :proof-map-id
               (p15-s23-b2-c17-gate-b-neutral-content-id
                 (:proof-to-c-assumption-map gate-a)),
               :certificate-binding
               (get-in
                 gate-a
                 [:proof-to-c-assumption-map :proof-certificate-binding])},
              :target-record target-common,
              :build-id build-id,
              :inputs
              {:gate-a-semantic-id (:semantic-id gate-a),
               :gate-a-artifact-id (:artifact-id gate-a),
               :source-debug-map-id
               (p15-s23-b2-c17-gate-b-neutral-content-id (:source-debug-map gate-a)),
               :proof-map-id
               (p15-s23-b2-c17-gate-b-neutral-content-id
                 (:proof-to-c-assumption-map gate-a))},
              :reproducibility
              {:pass-pipeline-digest pass-pipeline-digest,
               :content-addressed? true,
               :independent-repeat-required-for-credit? true,
               :timestamp-input? false,
               :linker-reproducibility :apple-ld-reproducible,
               :path-neutral-semantic-identity? true,
               :target-toolchain-digest
               (p15-s23-c11-mir-digest (:toolchain-fingerprint transaction)),
               :environment-inputs-digest
               (p15-s23-c11-mir-digest p15-s23-b2-c17-gate-b-environment-policy),
               :status :single-build-candidate,
               :normalized-tool-output? true,
               :fixed-logical-names ["program.c" "program.h" "program.o" "program"]},
              :status :content-addressed-internal-candidate,
              :effects #{},
              :safety
              {:runtime-checks 0,
               :unsafe-islands 0,
               :implicit-c-ub-permitted? false,
               :proof-authorized-casts? true},
              :kind :hosted-c17-executable-bundle,
              :source-provenance source-provenance,
              :dependency-provenance dependency-provenance,
              :artifact :gravity/b13-bounded-hosted-c17-emission,
              :target :c,
              :unsafe-audit {:required? false, :records []},
              :backend :gravity.backend/c,
              :profile :hosted,
              :release-eligible? false}]
    (assoc state 'base base)))
