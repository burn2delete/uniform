(ns sh-crosswalk
  "Source-bound SH routing projection. Task status is read only from component ledgers."
  (:require [clojure.edn :as edn] [clojure.string :as str] [clojure.pprint :as pp]
            [clojure.java.io :as io] [clojure.walk :as walk]))
(binding [*command-line-args* nil] (load-file "tools/sh_inventory.clj") (load-file "tools/acceptance_dependencies.clj"))
(require '[sh-inventory] '[acceptance-dependencies])
(def baseline "254a5b50ea5e2825c424248c9216802cc866c0fd")
(def applying-source-pins
  {"bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj" "4325a9f46292472a7f984c5adfef81209874bb5c", "docs/artifacts/phase-06/reports/current-reader-decode-source-identity.md" "4325a9f46292472a7f984c5adfef81209874bb5c", "docs/artifacts/phase-06/reports/current-reader-lexical-products-origins.md" "bffefedfada9e3312e68e0a28a69d71db459d0e2", "docs/component-roadmaps/build.md" "3bd7e21145dfe5d71f45375f6e787b384d361e2e", "docs/component-roadmaps/compiler-api.md" "f25e6f571e610587da8142f5b0fc33e7806d440e", "docs/component-roadmaps/reader.md" "2e0292c61603aad200df397e397e94e3c7d24818", "docs/self-hosting-slice-ownership.edn" "c8fd8e437ef921041b181e2f09ee1a37d716d357", "specs/build-workflow/tasks.md" "3bd7e21145dfe5d71f45375f6e787b384d361e2e", "specs/compiler-api-workflow/tasks.md" "f25e6f571e610587da8142f5b0fc33e7806d440e", "specs/reader-workflow/tasks.md" "2e0292c61603aad200df397e397e94e3c7d24818"})
(def pinned-evidence-hashes
  {"docs/artifacts/phase-06/reports/current-reader-lexical-products-origins.md"
 "6bf8d0fce1744526f500857eedfd03f11138ee4b0941c49ab787e5dacb5ba5d6",
 "bootstrap/clojure/src/gravity/authenticated_artifact_pipeline.clj"
 "7bde0be1b0e264276827deeac058f7fed5d283f58a7f23580173c147d336d644",
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00224-backend-test-matrix-file-artifact.clj"
 "cb3e62b8b02c86928ecc99ab529f6aaeac467c8d0d4353dad9a175073f22fdba",
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00225-b1-document-source-artifact.clj"
 "36735e2c6b87a13001458fd082f7e9792a7f5fbdc64d402394157d0054b1c50c",
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00231-b4-document-feature-requirement.clj"
 "efb0d89ceba4fee78cc505381062719533da7105079e3d2eab72e7c0a34fd047",
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00287-minimal-native-runtime-manifest.clj"
 "5e3a7c67e7c4b4d60d4ae4f8d3f7eec580bdfa2cdf9967b6a6de54889cea160b",
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00499-standard-library-source-artifact.clj"
 "dee011c3155f1b6b1a0c760d8611015f54963adfae51751f2369f79142becd37",
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00569-stage0-compiled-plan-package-report.clj"
 "b6412dcea3ca0381b5e9a8148c385a6a67a7d155b0d1ab63c786a6836e531bd0",
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/runtimes/00284-runtime-selection-validate.clj"
 "ca1375b5ca24d23f718f24c8a3758a0d27ceffdf215022d98ac7b013ae18b7ce",
 "bootstrap/clojure/src/gravity/bootstrap_impl/semantic_llvm/036-linux-artifact-record.clj"
 "cf8ba3f557e37ef26c3529269df1b36ff4de386fff41453930c1a21b81339957",
 "bootstrap/clojure/src/gravity/c10_safety_analysis/contract.clj"
 "8cf168a5af4006e251cfaaae637728cf88b73afe72fc4af3df52ae4858269ce3",
 "bootstrap/clojure/src/gravity/c10_safety_analysis/pipeline.clj"
 "58f1bbb4a653ef2a06477a26fb34fd1474344d86c68d9afb5b417cd38aaad92e",
 "bootstrap/clojure/src/gravity/c15_diagnostics/projection.clj"
 "b55d236b871aa7349f4fd54bf5789549feb77973f9649a71278bf874e57e3cff",
 "bootstrap/clojure/src/gravity/c15_diagnostics/records.clj"
 "b1f43e279d969602fe9451683bf252ddbd025ae8cd4f35faa4599b5a09981ac4",
 "bootstrap/clojure/src/gravity/c9_ownership_checker/contract.clj"
 "ecc8150d635099807efa7144c2c88f1ea32c2ef4292413ba71ccd5604c88d5f7",
 "bootstrap/clojure/src/gravity/capability_validation/authority.clj"
 "0eac12123d0cb9e391de99aabf88171a96307b9cc6146b207592a5c00d3c30c4",
 "bootstrap/clojure/src/gravity/capability_validation/contract.clj"
 "d67e00692336c8cc798225d52c761f905d70639fbac988311472b0103832992f",
 "bootstrap/clojure/src/gravity/cli/compile_command.clj"
 "eaaeefa2248422926eb18ca2509fb1e3c77c3d53fa54d1545490888a86fad1b7",
 "bootstrap/clojure/src/gravity/cli/diagnostic_presentation.clj"
 "0620ea14272bd374b17f735d934beeaff9eb5983864fbf0c57fa02da86aab776",
 "bootstrap/clojure/src/gravity/cli/dispatch.clj"
 "1c23d0f7a305e65446c8edb1289fb9ff3bb85b9c159835452a6983f338313b41",
 "bootstrap/clojure/src/gravity/diagnostics.clj"
 "3192c25407f204fe059fa2360b6025702ec6fb80385f61b7a49434362e9761dd",
 "bootstrap/clojure/src/gravity/pass_cache.clj"
 "876b51a55b516e2f4e278f81432d2d3ae012b66d4d2dbeab3b25ba660cc9ebe9",
 "bootstrap/clojure/src/gravity/profile_validation/contract.clj"
 "59183cc2a887358e346c24af5f5b1a2c714ff330c6ffd0e923cfebf6e67ad70a",
 "bootstrap/clojure/test/gravity/bootstrap_test.clj"
 "cecacec87f9b081872f4ca46e4c7c518ddf3cfadad765056f01921dba87d7d4f",
 "bootstrap/clojure/test/gravity/self_hosting/sh01_development_test_runner_test.clj"
 "431116755e5825f665a321c33bbdd644e2381dfd49bbcfaa326432f9bbcd9943",
 "bootstrap/clojure/test/gravity/self_hosting/sh01_language_boundary_test.clj"
 "3d972955a3d9515a83b8a881c9704b794ca9235ca9dfe559bbdd5f81ed26e2f9",
 "bootstrap/clojure/test/gravity/self_hosting/sh01_ownership_test.clj"
 "66d0eefb780c0f4b45accc9feb7c11b9d1f298080dd0136a6d2181cca27f8099",
 "bootstrap/clojure/test/gravity/self_hosting/sh01_stage2_runtime_iteration_test.clj"
 "887d9b65ee46b71ab4317e5953813bca9f1813e7ba681930086217d863b6ba47",
 "bootstrap/clojure/test/gravity/self_hosting/sh02_authenticated_envelope_test.clj"
 "a3bcdc1df57330a4d7de956f67cb4a6f16582acdbbb12da939fe4e3f6fcc2fd3",
 "bootstrap/clojure/test/gravity/self_hosting/sh02_sh06_authenticated_plan_context_test.clj"
 "4798aa96b495c8c5d00b4231ab2544c9189f407e0fd265d81c4277a59fae46ef",
 "bootstrap/clojure/test/gravity/self_hosting/sh03_reader_adapter_test.clj"
 "27421a5d70b2baa3ad3ee69aeebcfea9319670830d2460fa8d49ef25e1fc2302",
 "bootstrap/clojure/test/gravity/self_hosting/sh04_syntax_adapter_test.clj"
 "9e4690a34aa256a33bbb801a5613dfc61e79269d8b099141399bc4afca6c1266",
 "bootstrap/clojure/test/gravity/self_hosting/sh05_macro_adapter_test.clj"
 "52a77cca2d5f2f481800b60474997c2df0ea6329e59439ae17493524b6654c43",
 "bootstrap/clojure/test/gravity/self_hosting/sh06_resolution_adapter_test.clj"
 "238a7ed27c00913763115af50c1c931dd7bf3719d44847b3d4298e872e7e2f76",
 "bootstrap/clojure/test/gravity/self_hosting/sh07_control_flow_test.clj"
 "76f75ad41e28fd6652924b48b2cef37b8e6b14c229777bcb3cabb6480f9445cd",
 "bootstrap/clojure/test/gravity/self_hosting/sh07_lexical_binding_test.clj"
 "8ce18d0f249afc866aef63dcbbc0c2b3923eb5d886e267ad896d062ba85f0966",
 "bootstrap/clojure/test/gravity/self_hosting/sh07_loop_recur_test.clj"
 "15fb348163ae8bf306d94be7fa118d6343282a17590e0326f913e44666a393cd",
 "bootstrap/clojure/test/gravity/self_hosting/sh07_match_lowering_test.clj"
 "ba6d622223a196172c8d991691732f14800584ccdddaba0ca312afa2b4e37f32",
 "bootstrap/clojure/test/gravity/self_hosting/sh07_mutation_test.clj"
 "05974c9863ec5935f5d816701bdf7d5cbe15cb3da93092783247a9caca74695c",
 "bootstrap/clojure/test/gravity/self_hosting/sh07_references_calls_test.clj"
 "d8cef53696c68509db5017bfeced6900e9dde4f04921818b59f494dfd05f13f6",
 "bootstrap/clojure/test/gravity/self_hosting/sh07_throw_test.clj"
 "fc68dc99a8f2182e9214a4875b38391ffa1b02ea71c20d987a6645d2e56895b7",
 "bootstrap/clojure/test/gravity/self_hosting/sh07_try_catch_test.clj"
 "653ad9ae33f484a95ce70945b90b7ef96be4ec4398854d590167bce1f42a7faa",
 "bootstrap/clojure/test/gravity/self_hosting/sh07_var_reference_test.clj"
 "1a26d06cac7f8d3a1aeeecbb15a414d05d01ec0e8b8c24a083606f88b2d00cfe",
 "bootstrap/clojure/test/gravity/self_hosting/sh07_vector_pattern_test.clj"
 "a6354270568ccf2e2307d33bf34357dcf8ae976db40598540c50bb9aaa224131",
 "bootstrap/clojure/test/gravity/self_hosting/sh08_authenticated_type_integration_test.clj"
 "0511784e670ee38a38307fd294d626da116d0306e00fbe7bb01cda1bcefb190b",
 "bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj"
 "b2c0dd7f07918dcee4b3aa9fba8d853ebd0eb8317ef3f2262abf0a23e46cbd55",
 "bootstrap/clojure/test/gravity/self_hosting/sh08_loop_recur_type_test.clj"
 "5138701462f4befb0c95402f860814e0bf85820377a2db6f2d9b4ecd023e46fe",
 "bootstrap/clojure/test/gravity/self_hosting/sh08_primitive_type_test.clj"
 "3a7a9688aa06af90ae57db2f8fe6192babe18ca70fbda61a56d336d8ac5df7eb",
 "bootstrap/clojure/test/gravity/self_hosting/sh08_record_access_match_typing_test.clj"
 "90c82e179de877e080b8d9fb33d1bca4da2f0b7aea5fa3a45145c1f7014683c7",
 "bootstrap/clojure/test/gravity/self_hosting/sh08_records_unions_type_test.clj"
 "e568874e11fefbc4fe5901287db0f1402fd58e9f9731a5ddc0e838f360f1d8c9",
 "bootstrap/clojure/test/gravity/self_hosting/sh08_recursive_function_type_test.clj"
 "7bf84a2794cfe0566a284774f2f3c807626910e24285c7c1ea411b972432f989",
 "bootstrap/clojure/test/gravity/self_hosting/sh09_authenticated_effect_integration_test.clj"
 "dc23d7db311eb702b40082f7b112e24572b933475a7698a1f3426de5a47c5687",
 "bootstrap/clojure/test/gravity/self_hosting/sh09_body_effect_test.clj"
 "6b3d16a8905ebe41aa3fd8bf9e247c120b28351700059dd467de13bebe3bdd40",
 "bootstrap/clojure/test/gravity/self_hosting/sh09_effect_legality_test.clj"
 "bc64aabba65f4096f7e5ad1c0b15399b510f7373e4a0edd7cc26b36ed6fa0ee1",
 "bootstrap/clojure/test/gravity/self_hosting/sh09_error_effect_integration_test.clj"
 "372d583538739d6925d2ecfd53a3084cd63754a0fea9143c0e8df1194ad87392",
 "bootstrap/clojure/test/gravity/self_hosting/sh10_authenticated_ownership_integration_test.clj"
 "829a618c542480743ad37dd25bf363d63131ba1a7aaab3651a564d797790a326",
 "bootstrap/clojure/test/gravity/self_hosting/sh10_initialization_move_family_test.clj"
 "fceca0912fb5b735333bd4f307b76df42f8f6e6c4ea3591303dbdf312ac86f2a",
 "bootstrap/clojure/test/gravity/self_hosting/sh10_ownership_transition_test.clj"
 "7e7b79152b46aecf074bd71734ad522ddb0c1a4f4b14048c1df5172265d215ca",
 "bootstrap/clojure/test/gravity/self_hosting/sh10_runtime_borrow_conflict_family_test.clj"
 "901cd3f147375cd6de4d704faf2e8c92c95a6600c57c6833cbd3ef31aa20d690",
 "bootstrap/clojure/test/gravity/self_hosting/sh11_authenticated_safety_integration_test.clj"
 "752d26c8360b5a2ffb919159d0ee3a45da296c9aed8efab93430180f2d3f7aa1",
 "bootstrap/clojure/test/gravity/self_hosting/sh11_division_bounds_safety_test.clj"
 "e44b66d801f61a1d8289ff17f79eec976eaa7865e88e98bf5ce5724d31ab392c",
 "bootstrap/clojure/test/gravity/self_hosting/sh11_numeric_safety_test.clj"
 "29b031ce2aaa1fb85c837ab6d5e326352828a88e3e01ed2b29cd89738ecf9eaa",
 "bootstrap/clojure/test/gravity/self_hosting/sh11_overflow_cast_safety_test.clj"
 "a9958fe1dacf2b0e2d5717e1b3c08615bb2181182d6206b91841338505ae614d",
 "bootstrap/clojure/test/gravity/self_hosting/sh11_runtime_checked_verifier_test.clj"
 "57500369d36f0f0a38e882a997e9d0bca6c3885fbf1af56e88dc2c0b852a3df1",
 "bootstrap/clojure/test/gravity/self_hosting/sh12_authenticated_mir_integration_test.clj"
 "b3f12a2494754901f70b5fc05825504df59896c3175a5eade452cc9a418171de",
 "bootstrap/clojure/test/gravity/self_hosting/sh12_branch_join_mir_test.clj"
 "0eb16ecacd53d121878b8915b8fd8b795ecef2f0e684953e70864a26f03967b8",
 "bootstrap/clojure/test/gravity/self_hosting/sh12_c10_mir_adapter_test.clj"
 "fe33063821c6abc341e26010de0e6cb795efc634137551769da39863ca8a117d",
 "bootstrap/clojure/test/gravity/self_hosting/sh12_runtime_checked_mir_test.clj"
 "393637c9a5baa04da7031a6408816c9e10787bd4a2e75fed597013fe82611351",
 "bootstrap/clojure/test/gravity/self_hosting/sh13_c11_domain_evidence_adapter_test.clj"
 "535b34b19b2012d91360cbe1812b9edaa91eb6ede4b96c182860945c659eb942",
 "bootstrap/clojure/test/gravity/self_hosting/sh13_control_flow_test.clj"
 "a8677e97accfc62249262c077bb33366e140260d3e59c46df0ee222394e18dc5",
 "bootstrap/clojure/test/gravity/self_hosting/sh14_authenticated_layout_test.clj"
 "1250a522fbb357923259da392a5721c17344429936f865e464e6b02d34672d23",
 "bootstrap/clojure/test/gravity/self_hosting/sh14_data_layout_test.clj"
 "974678d777c137168fbfa128843b896fe10c5ef577131e7ccd52e56a9deb2074",
 "bootstrap/clojure/test/gravity/self_hosting/sh15_diagnostic_boundary_test.clj"
 "c196af2412af8c93356321106df62a8b870e0b1861a9a6c87863d8cbe8a977f5",
 "bootstrap/clojure/test/gravity/self_hosting/sh16_c12_domain_evidence_boundary_test.clj"
 "a8919f49e5c33c2f40ee87fdc4cb894eb621432563b683f93c410f1d05d9bf4c",
 "bootstrap/clojure/test/gravity/self_hosting/sh16_mir_optimization_test.clj"
 "df7ca8a3020c69230f5fd849947e53b0bca92ce39352fcb97c86157da98bf2e8",
 "bootstrap/clojure/test/gravity/self_hosting/sh17_c13_evidence_rejection_test.clj"
 "a86d18747c8db900cf4cc0e1c6b8602c5fe0c2ba0412dc36ae2c7ce2b7291863",
 "bootstrap/clojure/test/gravity/self_hosting/sh17_c13_optimized_mir_carrier_test.clj"
 "24f01f3573a07526e94e3954eda508fe113bf6e939ec0c9f85a2593eb1509f81",
 "bootstrap/clojure/test/gravity/self_hosting/sh17_target_lowering_hardening_test.clj"
 "1ed99ccced41d9861a07b9083013b02539e9a31a1bc53f8a0225295cdbe1dfdf",
 "bootstrap/clojure/test/gravity/self_hosting/sh18_native_c_emitter_test.clj"
 "decb3fc6f4201ce92d35abeefb4197ef371d19f9ae760fe853f056c6295aa3b0",
 "bootstrap/clojure/test/gravity/self_hosting/sh18_native_toolchain_harness_test.clj"
 "41fc1a1437a08e30234636e0642f5de559e7f8a24d0eec29fa1ccb6bc03f1e2d",
 "bootstrap/clojure/test/gravity/self_hosting/sh19_minimal_runtime_test.clj"
 "4be7d51e30e250db493daff7b16b14c950358d8fbb7ff03e1859a88da1475df7",
 "bootstrap/clojure/test/gravity/self_hosting/sh20_artifact_emission_test.clj"
 "492ec3fcfe0ae9975472fa91cee6fa6dba944ea0d2d7bd561d1a3477409cf07d",
 "bootstrap/clojure/test/gravity/self_hosting/sh21_meta_compiler_legality_test.clj"
 "2d6d39e6818011222f7d5c785d49136b598c376d339f94ea97d7c44e4f2bf29a",
 "bootstrap/clojure/test/gravity/self_hosting/sh22_authenticated_library_integration_test.clj"
 "e8754e923f4df7f195c2dfab9912a48782df0b0475659e12c33e7323614c0384",
 "bootstrap/clojure/test/gravity/self_hosting/sh22_standard_library_test.clj"
 "7657111768d7d83fc41c39cdec9eadc63ddd9e128eae59770ed60e161fa95929",
 "bootstrap/clojure/test/gravity/self_hosting/sh23_hermetic_package_build_test.clj"
 "14ce07bc8df79b8d6d231aaf3346923bfb3f996b6639b0a74afd22ca97cc5a0e",
 "bootstrap/clojure/test/gravity/self_hosting/sh25_component_build_test.clj"
 "1a04f682369e30980f5508cd417ffdb8fe07b0c1890f469e7fa2838a503b2fd3",
 "bootstrap/clojure/test/gravity/self_hosting/sh26_stage_rebuild_test.clj"
 "d2c0865d3e2ebc34855c765897e1719e838ff849bc8e5d106483cd9969202d91",
 "bootstrap/clojure/test/gravity/self_hosting/sh27_stage_equivalence_test.clj"
 "ec7cb6dd712c1442c95ffc45d4a136dcde10988e81f4c38e1b8a902fda37d39f",
 "bootstrap/clojure/test/gravity/self_hosting/sh28_trust_provenance_test.clj"
 "eb49a8c58c2fa9817c12d13120fa973a1fef32444f66938059b9f546e4f08403",
 "bootstrap/gravity/src/gravity/runtime.gravity"
 "e1084872868b99eb8dc38a2e23f0b871864529eb107a68ab16dd3825867a13fe",
 "docs/artifacts/phase-06/reports/current-reader-decode-source-identity.md"
 "1c879a874faa0b995c2ad5899bcbee9167d831d65203a5e6b38b2cda28599f91",
 "docs/artifacts/phase-06/reports/current-reader-fresh-syntax-handoff.md"
 "8349689573969359e95c678518bb81df7a8b9d685d764927f33ac7dfd4a5f7c9",
 "docs/artifacts/phase-15/bootstrap/p15-s23-final-seed-retirement-proof.edn"
 "d62075e0ae7d974d69d24796a101bcf6e6ceab34f4d18a60d18877f0618b6d76",
 "docs/artifacts/phase-15/bootstrap/p15-s23-whole-language-self-hosting-gate.edn"
 "5c13d84dbec78cc7afad44c749d32c2ee5716621ba08af89de08d45a32564f18",
 "docs/implementation-roadmap.md"
 "c1116495bf4140ccbefe45e5fa6952d370917abf8c11cf170444827aee32d834",
 "docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md"
 "3791a95f860d4d8fca8e73d7bb57701cdf1e099c14c2b8d0f1778d8ce1e95f75",
 "docs/phase-01-core-language/025-l15-capability-provider-specification.md"
 "81a875ffbbc43bf05f95aafd60774aaf1b1fa2e54c0eb9331b03dd28597f6ae5",
 "docs/phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md"
 "030fa2eca59ce7be61371bc12e02c312de2f0aa413ef0d12dc2858e531c84c36",
 "docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md"
 "f61931ada4a1759bcdac74ef5c114e34f674d1eee9199f0833b72a5c31c205b0",
 "docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md"
 "7c263f211703f6c6a8ec083b20aa1d65ec10ef326072d74cd1852dfd13b056d3",
 "docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md"
 "96ed68655996cd72b483602bb9b1ef792d7fdcb61f0f13dcd6d04c1f421d07c5",
 "docs/phase-08-runtime-architecture/README.md"
 "339b1cf4dedbc7a6dade0c3ab971e1a2e169aa565cc3471f0d799439fa6666fe",
 "docs/phase-12-build-package-and-artifact-system/166-pkg2-build-system-architecture.md"
 "0514e123045e234c3abb4d96b9ff92986d3d43e65d6306d59ed3c7d99e7c53d8",
 "docs/phase-12-build-package-and-artifact-system/167-pkg3-artifact-model-specification.md"
 "fd6751abce2cac644e3659d1a2ad0e411bb79f42c252b17feab73ae660d93a9e",
 "docs/phase-12-build-package-and-artifact-system/171-pkg7-reproducible-build-specification.md"
 "be029602301a0a9f1f392df06d84f8b6ea56226f5d61754a78de7dbc589e9933",
 "docs/phase-13-tooling-and-developer-experience/README.md"
 "f376b4cafaaa5a968ef2a589a824b868f0ae2505cc63efc8d425c019808cb736",
 "docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md"
 "d4bcdb03479049bdfb30b3c59591f2c97242102aee07e8ce714d52664a6aa75f",
 "docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md"
 "769fc29e7c1872f4b9634f0b542f2d6da6174b77fd84c8240f6a0eb9aa2a40d5",
 "docs/phase-16-standard-library/211-std1-standard-library-architecture.md"
 "117423ed96525984f0f643133d90c6118a2e72827e107e9796885f2e486aae2a",
 "docs/phase-16-standard-library/README.md"
 "a6d2ecdd024ab3b5da368bb8eb8d1630037b07642f2905f0ffb776a40fe5a59b",
 "docs/phase-18-binary-distribution-and-seedless-release/IMPLEMENTATION-ROADMAP.md"
 "a4659aaa03fafe5f99d711a48d4dec62721716d0ae7aa435c9d387c2dcdf2e57",
 "docs/phase-18-binary-distribution-and-seedless-release/README.md"
 "45c126ec6370358245c666a2dda960a4f3d9666433f140552ade73dd52c486b5"}
)
(defn source-pin [path] (get applying-source-pins path baseline))
(def original-baseline "15a5499e97798621d270c36dd2546ce98ddd62a9")
(def output-path "specs/sh-family-crosswalk/crosswalk.edn")
(def rollup-path "specs/sh-family-crosswalk/rollup.md")
(def sh07-path "docs/self-hosting-sh07-completion-plan.md")
(def original-sh07-sha256 "dc43b208677945318effa93734449716785b27b5737256279b1c75a9195ae17e")
(def audit-path "docs/self-hosting-sh00-sh06-evidence-audit.md")
(def p15-whole-gate "docs/artifacts/phase-15/bootstrap/p15-s23-whole-language-self-hosting-gate.edn")
(def p15-final-proof "docs/artifacts/phase-15/bootstrap/p15-s23-final-seed-retirement-proof.edn")
(def release-flags [:status :task :full-language-compiler-self-hosted? :clojure-seed-retired? :clojure-seed-boundary?])
(def legacy-accounting-facet-ids #{"SH-02.5" "SH-03.4" "SH-04.5" "SH-05.5" "SH-16.6"})

;; Integers below select pinned literal IDs, never live document/display order.
;; The mapping keys are local routing shorthand, not tasks or completion units.
(def stable-task-routing-keys
  {"auth"
 ["auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions"],
 "backends"
 ["backend-interface"
  "backend-c"
  "backend-llvm"
  "backend-wasm"
  "backend-jvm"
  "backend-js"
  "backend-mlir"
  "backend-gpu"
  "backend-hdl"
  "backend-workflow"
  "backend-ai"
  "backend-query"
  "backend-mobile"
  "backend-emission"
  "backend-conformance"],
 "build"
 ["build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
  "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"
  "build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation"
  "build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records"
  "build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix"],
 "cli"
 ["cli/remaining-tasks-and-evidence/reconcile-supported-commandoptions-with-normative-families"
  "cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services"
  "cli/remaining-tasks-and-evidence/connect-replformatlintdocinspectprofileauditairegistryexplain-services"
  "cli/remaining-tasks-and-evidence/normalize-json-text-exit-codes-and-redaction"
  "cli/remaining-tasks-and-evidence/package-and-verify-user-entrypoints"],
 "compiler-api"
 ["pipeline" "plugins" "pass-evidence" "development-boundaries" "domain-ir-boundaries"],
 "core"
 ["core/current-evidence-and-remaining-work/core1-lower-core-forms"
  "core/current-evidence-and-remaining-work/core2-preserve-binding-structure"
  "core/current-evidence-and-remaining-work/core3-desugar-without-semantic-loss"
  "core/current-evidence-and-remaining-work/core4-verify-and-serialize"
  "core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses"],
 "diag"
 ["diag/remaining-tasks-and-evidence/reconcile-stage-carriers-with-canonical-record-schema"
  "diag/remaining-tasks-and-evidence/preserve-primaryrelatedgenerated-origins-and-stable-order"
  "diag/remaining-tasks-and-evidence/close-textjsonredaction-and-explain-contracts"
  "diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift"],
 "domains" ["math" "schema" "query" "workflow" "ai" "gpu" "ui"],
 "effects"
 ["effects/current-evidence-and-remaining-work/effects1-infer-direct-effects"
  "effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation"
  "effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers"],
 "emit"
 ["emit/current-evidence-and-remaining-work/emit1-reconcile-selected-target-contracts"
  "emit/current-evidence-and-remaining-work/emit2-emit-real-outputs"
  "emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes"
  "emit/current-evidence-and-remaining-work/emit4-establish-conformance"
  "emit/current-evidence-and-remaining-work/emit5-integrate-artifact-consumers"],
 "expand"
 ["expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
  "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"],
 "legality"
 ["legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
  "legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution"
  "legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface"],
 "lower"
 ["lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs"
  "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"
  "lower/current-evidence-and-remaining-work/lower3-select-explicit-providers"
  "lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins"
  "lower/current-evidence-and-remaining-work/lower5-integrate-target-emission"],
 "mir"
 ["mir/current-evidence-and-remaining-work/mir1-construct-semantic-mir"
  "mir/current-evidence-and-remaining-work/mir2-attach-fact-tables"
  "mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality"
  "mir/current-evidence-and-remaining-work/mir4-serialize-stable-modules"
  "mir/current-evidence-and-remaining-work/mir5-gate-downstream-consumption"],
 "opt"
 ["opt/current-evidence-and-remaining-work/opt1-execute-pass-contracts"
  "opt/current-evidence-and-remaining-work/opt2-implement-structural-families"
  "opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors"
  "opt/current-evidence-and-remaining-work/opt4-verify-and-regenerate"
  "opt/current-evidence-and-remaining-work/opt5-integrate-reproducible-pipeline"],
 "platform" ["hardware" "firmware" "kernel" "devices"],
 "reader"
 ["reader/finite-remaining-work/contract-coverage-reconciliation"
  "reader/finite-remaining-work/decode-and-source-identity-closure"
  "reader/finite-remaining-work/lexical-products-and-origins-closure"
  "reader/finite-remaining-work/extension-policy-closure"
  "reader/finite-remaining-work/authentication-and-quota-removal-closure"
  "reader/finite-remaining-work/fresh-syntax-handoff"
  "reader/finite-remaining-work/component-release-evidence"],
 "release"
 ["release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance"
  "release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler"
  "release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution"
  "release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom"
  "release/remaining-tasks-and-evidence/obtain-independent-acceptance-for-the-exact-release-candidate"],
 "resolve"
 ["resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions"
  "resolve/current-evidence-and-remaining-work/resolve2-validate-module-boundaries"
  "resolve/current-evidence-and-remaining-work/resolve3-check-graph-legality"
  "resolve/current-evidence-and-remaining-work/resolve4-publish-stable-indexes"
  "resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers"],
 "runtime"
 ["runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
  "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
  "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
  "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
  "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"],
 "stdlib"
 ["stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations"
  "stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis"
  "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis"
  "stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters"
  "stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests"],
 "syntax"
 ["syntax/current-evidence-and-remaining-work/syntax1-construct-canonical-syntax"
  "syntax/current-evidence-and-remaining-work/syntax2-preserve-transformations"
  "syntax/current-evidence-and-remaining-work/syntax3-invalidate-facts"
  "syntax/current-evidence-and-remaining-work/syntax4-serialize-and-verify"
  "syntax/current-evidence-and-remaining-work/syntax5-integrate-consumers"],
 "tools"
 ["tools/finite-service-outcomes-and-remaining-work/format-formatter-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/lint-analysis-tool-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/language-server-editor-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/debug-debugger-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/repl-interactive-runtime-tooling-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/ir-explorer-compiler-tool-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/profile-performance-tool-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/docs-documentation-tool-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/dev-server-development-session-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/registry-view-package-tool-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/safety-explorer-safety-tool-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/ai-development-ai-tool-maintainer"],
 "types"
 ["types/current-evidence-and-remaining-work/types1-solve-core-constraints"
  "types/current-evidence-and-remaining-work/types2-check-declared-abstractions"
  "types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit"
  "types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts"
  "types/current-evidence-and-remaining-work/types5-integrate-typed-core"]}
)

(def routes
  [[[["mir" 1] ["lower" 1]] [["backends" "backend-wasm"] ["emit" 2]] [["backends" "backend-conformance"] ["emit" 4]] [["lower" 1] ["backends" "backend-interface"]] [["compiler-api" "pass-evidence"]] [["release" 5]]]
   [[["compiler-api" "development-boundaries"]] [["compiler-api" "development-boundaries"]] [["compiler-api" "development-boundaries"]] [["compiler-api" "development-boundaries"]] [["compiler-api" "development-boundaries"]] [["compiler-api" "development-boundaries"]]]
   [[["auth" 4] ["compiler-api" "pipeline"]] [["auth" 4]] [["auth" 4]] [["auth" 4]] [["auth" 4]] [["auth" 4] ["compiler-api" "pass-evidence"]]]
   [[["reader" 2] ["reader" 3]] [["reader" 3]] [["reader" 4] ["reader" 3]] [["reader" 2] ["reader" 3]] [["reader" 6]] [["reader" 1] ["reader" 7]]]
   [[["syntax" 1]] [["syntax" 2]] [["syntax" 2] ["syntax" 4]] [["syntax" 4]] [["syntax" 4] ["auth" 4]] [["syntax" 5]]]
   [[["expand" 1]] [["expand" 2]] [["expand" 1] ["expand" 5]] [["expand" 3] ["auth" 2]] [["expand" 4]] [["expand" 5] ["compiler-api" "pass-evidence"]]]
   [[["resolve" 2] ["resolve" 3]] [["resolve" 1]] [["resolve" 1]] [["resolve" 2] ["resolve" 3]] [["resolve" 4] ["resolve" 5]] [["resolve" 5]]]
   [[["core" 1] ["core" 2]] [["core" 1] ["core" 3]] [["core" 1] ["core" 2]] [["core" 1] ["core" 3]] [["core" 1] ["core" 2]] [["core" 4] ["core" 5] ["compiler-api" "pass-evidence"]]]
   [[["types" 1]] [["types" 1] ["types" 2]] [["types" 1] ["types" 2]] [["types" 1] ["types" 2]] [["types" 2] ["types" 3] ["types" 4]] [["types" 5]]]
   [[["effects" 1] ["effects" 2]] [["effects" 2] ["effects" 3]] [["auth" 1] ["auth" 2]] [["legality" 1] ["effects" 3]] [["effects" 4] ["auth" 3]] [["effects" 5] ["legality" 5]]]
   [[["legality" 2]] [["legality" 2]] [["legality" 2]] [["legality" 2]] [["legality" 2] ["legality" 4]] [["legality" 4] ["mir" 2]]]
   [[["legality" 3]] [["legality" 3]] [["legality" 3]] [["legality" 4]] [["legality" 3] ["auth" 2]] [["legality" 4] ["mir" 2]]]
   [[["mir" 1]] [["mir" 2]] [["mir" 3]] [["mir" 3] ["mir" 5]] [["mir" 4]] [["mir" 5]]]
   [[["mir" 1] ["runtime" 2]] [["mir" 1] ["runtime" 2]] [["mir" 1] ["mir" 3] ["emit" 4] ["runtime" 2]] [["mir" 1] ["runtime" 2]] [["mir" 5] ["emit" 4] ["runtime" 2]] [["mir" 5] ["emit" 4] ["runtime" 2]]]
   [[["mir" 1] ["stdlib" 2]] [["mir" 1] ["stdlib" 2]] [["lower" 2] ["mir" 1]] [["runtime" 3] ["legality" 2]] [["runtime" 3] ["lower" 2]] [["lower" 2] ["emit" 4] ["runtime" 3]]]
   [[["diag" 1]] [["diag" 2]] [["diag" 1] ["diag" 3]] [["diag" 4] ["cli" 4]] [["diag" 3]] [["diag" 1] ["diag" 2] ["release" 2]]]
   [[["opt" 1]] [["opt" 1] ["opt" 2]] [["opt" 4]] [["opt" 3]] [["opt" 4] ["compiler-api" "pass-evidence"]] [["opt" 5] ["lower" 1]]]
   [[["lower" 1]] [["lower" 1] ["emit" 1]] [["lower" 2] ["lower" 3]] [["lower" 4]] [["lower" 4] ["emit" 3]] [["lower" 5] ["backends" "backend-interface"]]]
   [[["emit" 2] ["backends" "backend-llvm"]] [["backends" "backend-llvm"] ["runtime" 2] ["stdlib" 2]] [["emit" 2] ["backends" "backend-llvm"]] [["emit" 4] ["backends" "backend-conformance"]] [["emit" 4] ["lower" 1]] [["emit" 3] ["compiler-api" "pass-evidence"]]]
   [[["runtime" 2]] [["runtime" 3]] [["runtime" 2] ["auth" 3] ["stdlib" 3]] [["runtime" 2] ["runtime" 5]] [["runtime" 1] ["auth" 3]] [["runtime" 5] ["auth" 5]]]
   [[["emit" 2]] [["emit" 3]] [["emit" 3] ["build" 4]] [["emit" 3] ["emit" 5]] [["emit" 5] ["build" 4]] [["build" 4] ["release" 4]]]
   [[["compiler-api" "pipeline"] ["release" 1]] [["legality" 1] ["legality" 2] ["legality" 3] ["types" 5] ["effects" 5]] [["auth" 2] ["effects" 3]] [["expand" 5] ["compiler-api" "pipeline"]] [["auth" 2] ["auth" 3]] [["compiler-api" "pass-evidence"] ["legality" 5]]]
   [[["stdlib" 1]] [["stdlib" 2]] [["stdlib" 2]] [["stdlib" 2] ["stdlib" 4]] [["stdlib" 3] ["auth" 3]] [["stdlib" 1] ["stdlib" 2] ["stdlib" 3]]]
   [[["build" 1]] [["build" 1] ["build" 2]] [["build" 2]] [["build" 2] ["auth" 3]] [["build" 3] ["build" 4]] [["build" 2] ["release" 3]]]
   [[["cli" 1] ["cli" 5]] [["cli" 2] ["compiler-api" "pipeline"]] [["cli" 2]] [["cli" 4]] [["cli" 5] ["reader" 2]] [["cli" 2] ["cli" 5] ["release" 3]]]
   [[["release" 1]] [["release" 1] ["compiler-api" "pipeline"]] [["release" 1] ["build" 2]] [["build" 2] ["release" 3]] [["release" 1] ["auth" 4]] [["release" 3] ["compiler-api" "pass-evidence"]]]
   [[["release" 3] ["build" 5]] [["release" 3] ["build" 2]] [["release" 3]] [["release" 3] ["cli" 2]] [["release" 3] ["auth" 3]] [["release" 3] ["emit" 3]]]
   [[["release" 2] ["build" 5]] [["release" 2] ["emit" 3]] [["release" 2] ["diag" 1]] [["release" 2] ["runtime" 2]] [["release" 2] ["compiler-api" "pass-evidence"]] [["release" 4] ["build" 5]]]
   [[["release" 4] ["build" 5]] [["release" 4] ["auth" 4]] [["release" 4] ["build" 4]] [["release" 4] ["auth" 2]] [["release" 5] ["compiler-api" "pass-evidence"]] [["release" 3] ["release" 5]]]
   [[["release" 3] ["cli" 5]] [["release" 3] ["build" 2]] [["release" 1] ["cli" 2]] [["release" 4]] [["release" 5] ["cli" 5]] [["release" 5]]]])

(def historical-commits
  ["dbd7764ca648d808e6fe6c4af971303152bdd7ea" "cbc55cd1477deca08a9c042c91db98005833021a"
   "914215e3b99822e4c4ef48e004c26bdf3e075216" "eeb9253dff4c7e7d4fb0152b7fa1a720b56e052d"
   "64f7b968b577013e47ae7d02bad3e39748b4250d" "bd8384b5344d9d99626e698976ee6e8a190859e5"
   "8f4b91d3efc4c4f42442002001707b89ca388025"])

(defn span-matches [path pattern]
  (let [text (slurp path) m (re-matcher pattern text)]
    (loop [rows []] (if (.find m)
                     (recur (conj rows {:groups (mapv #(.group m %) (range 1 (inc (.groupCount m))))
                                        :source (sh-inventory/source-span path text (.start m) (.end m))})) rows))))
(defn task-records [component]
  (let [path (str "specs/" component "-workflow/tasks.md") text (slurp path)
        marker (str "<!-- " component "-implementation-tasks:")
        start (+ (.indexOf text (str marker "start -->")) (count (str marker "start -->")))
        end (.indexOf text (str marker "end -->"))
        _ (acceptance-dependencies/implementation-ids text component)
        m (re-matcher #"(?m)^- \[([ xX])\] `([^`]+)`[^\n]*" text)
        positions (loop [v []] (if (.find m) (recur (if (< start (.start m) end)
                   (conj v {:start (.start m) :id (.group m 2) :checked (not= " " (.group m 1))}) v)) v))]
    (mapv (fn [row next-row]
            (let [stop (or (:start next-row) end) raw (str/trimr (subs text (:start row) stop))]
              {:id (:id row) :component component :path path :checked (:checked row)
               :authority :component-implementation-ledger
               :source-commit (source-pin path) :file-sha256 (sh-inventory/sha256 text)
               :source (sh-inventory/source-span path text (:start row) (+ (:start row) (count raw)))
               :owner-source (str "docs/component-roadmaps/" component ".md")}))
          positions (concat (rest positions) [nil]))))
(defn task-ref [tasks [component selector]]
  (let [id (if (integer? selector) (get-in stable-task-routing-keys [component (dec selector)]) selector)
        row (first (filter #(= id (:id %)) (tasks component)))]
    (when-not row (throw (ex-info "Unknown task route" {:component component :selector selector})))
    (select-keys row [:id :component :path])))
(defn review-fingerprint [result]
  (sh-inventory/sha256
    (pr-str [(dissoc result :requirements :source-digests :source-snapshots)
             (mapv #(dissoc % :mapping-review :coverage) (:requirements result))
             (dissoc (:source-digests result) "docs/acceptance-dependencies.md")
             (sh-inventory/sha256 (slurp "tools/acceptance_dependencies.clj"))])))
(def predecessor-mapping-review
  {:status :accepted-mapping-only
   :input-fingerprint "660701592149e71eae4ec4408301c54d34fd4ca7dcf58eebef1888cacadb5abb"
   :reviewer "01a17a6d-1145-74dd-94c5-ecec12b8b005"
   :reviewed-commit "ac5f4c6ea4105da0cf2626634e31424623d66b1e"
   :reviewed-tree "b824c0d40abb35d47ed1625beac8088b1f916e27"
   :receipt-sha256 "fdb720fd1a9682753dcc2a334b414f9dea906ebaf788629f405c5b88c1ccd6b8"
   :audit-commit "9ac62964ac00eef5730785ba18062bcd508b238e"
   :qualification "Independently reviewed PR530 ownership source-version metadata adoption only, bound to this exact input fingerprint. All180 current whole-criterion acceptance states remain unassessed; no semantic, implementation, release or seedless credit."
   :predecessor-review
{:status :accepted-mapping-only
   :reviewer "01a17a6d-8d52-7002-999b-89d8cdd34008"
   :reviewed-commit "c6c80530dd2b36a9b66ffd1a2a9042f0f43b4f36"
   :reviewed-tree "1229798edab46b3774dbab18c1c555c043eafa55"
   :receipt-sha256 "39e91ae6270c92ad789e15b635e7dbaf8890726ea2dd7c432ec019077375037c"
   :review-text-sha256 "1269e58c6d1dc859f3d064bb8c3342a1d82061a26acc8252af7d32cc6e046942"
   :predecessor-input-fingerprint "cdca8f11a6ad11d3917646a932235a302f3356860ab5c559e2e4dd12166c4c5e"
   :input-fingerprint "655a9b89bf18eb04770ab27181d77097e72e751565352dad16745c9f108ecf68"
   :source-version-review
   {:reviewed-commit "a7b52d4592d6b82e191ee1fba308ae5dc26625d4"
    :reviewed-tree "49119f422a98f51131a5ee171ef3a107bdb7a2fb"
    :receipt-sha256 "a660add10af34afc961231571dfc0763d1fd31fd79c6f4ad49442cd1922f3b70"
    :scope "Four named version changes, new Reader receipt, complete explicit-pin guard and metadata-only adoption for the identical ae42 payload."}
   :locator-and-current-main-review
   {:reviewer "01a17a6d-1145-74dd-94c5-ecec12b8b005"
    :reviewed-commit "79305c95a2440d3879940043494a5cad396b6100"
    :reviewed-tree "bddcb3d9e7130eca8868ff2be1b98afdacc5747d"
    :audit-commit "90b6f524a2f498fbb21bf9d63b7aef7c368922c3"
    :receipt-sha256 "6130242a544f3db7bfd073d097279e7680144183022e29b36dc20efb4056050f"
    :locator-receipt-sha256 "2cb188cd202a46cff696f818480f49230511269587482b728221bcbb924f833f"
    :composition-receipt-sha256 "1357cf8975587dbf661ddd5ea9dfd4a2d1a8e6cde4ff423093cdb06900f73904"
    :scope "Exact inspected pins/live drift, current-main source-version composition, six lint fixes and corrected ownership provenance. Metadata-only adoption for the identical655a9b89 payload; no implementation or whole-SH acceptance."}
   :qualification "Independent acceptance of all180 mappings/18sequence rows and graph only. No implementation acceptance, global requirement denominator or rate."}})
(def reader-dispatch-mapping-review
{:status :pending-source-version-review,
 :input-fingerprint
 "96346270c7fd3d6fd31e9f8e0aabf805cd7b9258a136ce6ec05df03e43ef9a9a",
 :predecessor-review
 {:status :pending-source-version-review,
  :input-fingerprint
  "8f9bca309f67fcdf293603bc1ad5392c138942cac159adb1de950c47ce73e587",
  :predecessor-review
  {:reviewed-tree "b824c0d40abb35d47ed1625beac8088b1f916e27",
   :reviewed-commit "ac5f4c6ea4105da0cf2626634e31424623d66b1e",
   :predecessor-review
   {:reviewed-tree "1229798edab46b3774dbab18c1c555c043eafa55",
    :source-version-review
    {:reviewed-commit "a7b52d4592d6b82e191ee1fba308ae5dc26625d4",
     :reviewed-tree "49119f422a98f51131a5ee171ef3a107bdb7a2fb",
     :receipt-sha256
     "a660add10af34afc961231571dfc0763d1fd31fd79c6f4ad49442cd1922f3b70",
     :scope
     "Four named version changes, new Reader receipt, complete explicit-pin guard and metadata-only adoption for the identical ae42 payload."},
    :reviewed-commit "c6c80530dd2b36a9b66ffd1a2a9042f0f43b4f36",
    :locator-and-current-main-review
    {:reviewer "01a17a6d-1145-74dd-94c5-ecec12b8b005",
     :reviewed-commit "79305c95a2440d3879940043494a5cad396b6100",
     :reviewed-tree "bddcb3d9e7130eca8868ff2be1b98afdacc5747d",
     :audit-commit "90b6f524a2f498fbb21bf9d63b7aef7c368922c3",
     :receipt-sha256
     "6130242a544f3db7bfd073d097279e7680144183022e29b36dc20efb4056050f",
     :locator-receipt-sha256
     "2cb188cd202a46cff696f818480f49230511269587482b728221bcbb924f833f",
     :composition-receipt-sha256
     "1357cf8975587dbf661ddd5ea9dfd4a2d1a8e6cde4ff423093cdb06900f73904",
     :scope
     "Exact inspected pins/live drift, current-main source-version composition, six lint fixes and corrected ownership provenance. Metadata-only adoption for the identical655a9b89 payload; no implementation or whole-SH acceptance."},
    :status :accepted-mapping-only,
    :receipt-sha256
    "39e91ae6270c92ad789e15b635e7dbaf8890726ea2dd7c432ec019077375037c",
    :qualification
    "Independent acceptance of all180 mappings/18sequence rows and graph only. No implementation acceptance, global requirement denominator or rate.",
    :predecessor-input-fingerprint
    "cdca8f11a6ad11d3917646a932235a302f3356860ab5c559e2e4dd12166c4c5e",
    :reviewer "01a17a6d-8d52-7002-999b-89d8cdd34008",
    :review-text-sha256
    "1269e58c6d1dc859f3d064bb8c3342a1d82061a26acc8252af7d32cc6e046942",
    :input-fingerprint
    "655a9b89bf18eb04770ab27181d77097e72e751565352dad16745c9f108ecf68"},
   :audit-commit "9ac62964ac00eef5730785ba18062bcd508b238e",
   :status :accepted-mapping-only,
   :receipt-sha256
   "fdb720fd1a9682753dcc2a334b414f9dea906ebaf788629f405c5b88c1ccd6b8",
   :qualification
   "Independently reviewed PR530 ownership source-version metadata adoption only, bound to this exact input fingerprint. All180 current whole-criterion acceptance states remain unassessed; no semantic, implementation, release or seedless credit.",
   :reviewer "01a17a6d-1145-74dd-94c5-ecec12b8b005",
   :input-fingerprint
   "660701592149e71eae4ec4408301c54d34fd4ca7dcf58eebef1888cacadb5abb"},
  :source-version-bridge
   "specs/sh-family-crosswalk/reader-dispatch-source-version.edn",
  :qualification
  "Reader metadata source 2e0292c6 reconciled with main43112981 in e446f326; both standalone ownership records retained. Incoming and Reader predecessor reviews remain historical; composed inputs await independent source-version review. No implementation acceptance or task credit."},
 :pkg5-source-review
 {:reviewed-commit "023a28488f8f161c68cbe57aa29fa48247197d62",
  :reviewed-tree "e8b660791bdaf3e865528f943725b5d1dd307b41",
  :receipt-sha256
  "c0714e09a510f173d0e69380478beb1a5790b07635283009749612aff9754b57",
  :prior-pkg5-mapping-review
  {:status :pending-source-version-review,
   :input-fingerprint
   "05038c26ff9d7255528b483b008236ed5dbb540ca1dd417a32cc000403464d9c",
   :predecessor-review
   {:reviewed-tree "b824c0d40abb35d47ed1625beac8088b1f916e27",
    :reviewed-commit "ac5f4c6ea4105da0cf2626634e31424623d66b1e",
    :predecessor-review
    {:reviewed-tree "1229798edab46b3774dbab18c1c555c043eafa55",
     :source-version-review
     {:reviewed-commit "a7b52d4592d6b82e191ee1fba308ae5dc26625d4",
      :reviewed-tree "49119f422a98f51131a5ee171ef3a107bdb7a2fb",
      :receipt-sha256
      "a660add10af34afc961231571dfc0763d1fd31fd79c6f4ad49442cd1922f3b70",
      :scope
      "Four named version changes, new Reader receipt, complete explicit-pin guard and metadata-only adoption for the identical ae42 payload."},
     :reviewed-commit "c6c80530dd2b36a9b66ffd1a2a9042f0f43b4f36",
     :locator-and-current-main-review
     {:reviewer "01a17a6d-1145-74dd-94c5-ecec12b8b005",
      :reviewed-commit "79305c95a2440d3879940043494a5cad396b6100",
      :reviewed-tree "bddcb3d9e7130eca8868ff2be1b98afdacc5747d",
      :audit-commit "90b6f524a2f498fbb21bf9d63b7aef7c368922c3",
      :receipt-sha256
      "6130242a544f3db7bfd073d097279e7680144183022e29b36dc20efb4056050f",
      :locator-receipt-sha256
      "2cb188cd202a46cff696f818480f49230511269587482b728221bcbb924f833f",
      :composition-receipt-sha256
      "1357cf8975587dbf661ddd5ea9dfd4a2d1a8e6cde4ff423093cdb06900f73904",
      :scope
      "Exact inspected pins/live drift, current-main source-version composition, six lint fixes and corrected ownership provenance. Metadata-only adoption for the identical655a9b89 payload; no implementation or whole-SH acceptance."},
     :status :accepted-mapping-only,
     :receipt-sha256
     "39e91ae6270c92ad789e15b635e7dbaf8890726ea2dd7c432ec019077375037c",
     :qualification
     "Independent acceptance of all180 mappings/18sequence rows and graph only. No implementation acceptance, global requirement denominator or rate.",
     :predecessor-input-fingerprint
     "cdca8f11a6ad11d3917646a932235a302f3356860ab5c559e2e4dd12166c4c5e",
     :reviewer "01a17a6d-8d52-7002-999b-89d8cdd34008",
     :review-text-sha256
     "1269e58c6d1dc859f3d064bb8c3342a1d82061a26acc8252af7d32cc6e046942",
     :input-fingerprint
     "655a9b89bf18eb04770ab27181d77097e72e751565352dad16745c9f108ecf68"},
    :audit-commit "9ac62964ac00eef5730785ba18062bcd508b238e",
    :status :accepted-mapping-only,
    :receipt-sha256
    "fdb720fd1a9682753dcc2a334b414f9dea906ebaf788629f405c5b88c1ccd6b8",
    :qualification
    "Independently reviewed PR530 ownership source-version metadata adoption only, bound to this exact input fingerprint. All180 current whole-criterion acceptance states remain unassessed; no semantic, implementation, release or seedless credit.",
    :reviewer "01a17a6d-1145-74dd-94c5-ecec12b8b005",
    :input-fingerprint
    "660701592149e71eae4ec4408301c54d34fd4ca7dcf58eebef1888cacadb5abb"},
   :qualification
   "Build PKG5 task-interface successor; predecessor acceptance retained as history. Changed task acceptance and source pins need independent exact-source review. All180 whole-criterion acceptance states remain unassessed."},
  :scope :finite-task-interface-and-source-conservation-only},
 :qualification
 "Normal Reader fixture ownership and current Build task source pins composed; both predecessor source/review histories retained. Changed exact composition awaits independent review; no implementation acceptance or mapping promotion.",
 :reader-normal-source-review
 {:status :pending-source-version-review,
  :input-fingerprint
  "98c5fa7283ba55654d8cd4f5bce73bbcd687115d64adbccd0f2cd8fbb7362959",
  :source-version-bridge
  "specs/sh-family-crosswalk/reader-dispatch-source-version.edn",
  :qualification
  "Normal Reader dispatch fixture ownership added at 4d96671d; prior Reader/task/roadmap and all predecessor reviews retained. Changed ownership source awaits independent source-version review. No implementation acceptance or task credit.",
  :source-commit "afe5d20b3d013beabec45fa23768949ea05f2611"},
 :prior-current-main-input-fingerprint
 "adbb73b9a2bec1a367ea2b743350420aabf5292f84b6010a9ac347105e520a1a",
 :prior-current-main-qualification
 "Composed Reader and Build task source pins; both predecessor histories are retained. Changed composition awaits independent review; all180 whole-criterion implementation assessments stay unassessed."}
)
(def compiler-domain-predecessor-review
  (assoc reader-dispatch-mapping-review
         :input-fingerprint "72d98bc3492489de1de0f43982ac06f707578f57468b3df934e2eec3c8e85aed"
         :source-version-bridge "specs/sh-family-crosswalk/reader-assembly-source-version.edn"
         :reader-assembly-predecessor-review reader-dispatch-mapping-review
         :reader-assembly-source-version
         "specs/sh-family-crosswalk/reader-assembly-source-version.edn"))

(def syntax-pipeline-predecessor-review (assoc compiler-domain-predecessor-review :compiler-domain-predecessor-review compiler-domain-predecessor-review :input-fingerprint "6f182237a61e553c07904d63bd73a309babe58154f7965b8b1a6cb6e66596465" :compiler-domain-current-source {:status :pending-independent-affected-composition-review, :path "specs/compiler-api-workflow/domain-contract-source-composition.edn", :sha256 "ee5fe99c1b1184cbc4432237fffe1e955d3660271f4f1e6f57c1b7e03dce9e5e", :source-commit "ce96db1e2d4fd2846b8939ca9b01d6f8d5934474", :implementation-credit :none, :global-count-adoption :none}))

(def macro-runtime-predecessor-review
  (assoc syntax-pipeline-predecessor-review
         :input-fingerprint "9bbe81fd3a6b40aa2cff2ba10d32764ab86faf692bd8d3cb133e018a022a4a4e"
         :source-version-bridge "specs/sh-family-crosswalk/syntax-pipeline-source-version.edn"
         :syntax-pipeline-predecessor-review
         (select-keys syntax-pipeline-predecessor-review
                      [:status :input-fingerprint :source-version-bridge])))

(def macro-artifacts-predecessor-review
  (assoc macro-runtime-predecessor-review
         :input-fingerprint "904a047459a1ddab17e8ac975c0c35df6e863ece53bb4383c284f4d5850e8048"
         :source-version-bridge "specs/sh-family-crosswalk/macro-runtime-source-version.edn"
         :macro-runtime-predecessor-review
         (select-keys macro-runtime-predecessor-review
                      [:status :input-fingerprint :source-version-bridge])))

(def macro-artifacts-current-review
  (assoc macro-artifacts-predecessor-review
         :input-fingerprint "42a769c041b8bfdeda4ad7713877221c0a270c98d8b2b7a9986dfdf538197b86"
         :source-version-bridge "specs/sh-family-crosswalk/macro-artifacts-source-version.edn"
         :macro-artifacts-predecessor-review
         (select-keys macro-artifacts-predecessor-review
                      [:status :input-fingerprint :source-version-bridge])))

(def pr574-predecessor-review macro-runtime-predecessor-review)

(def resolution-main-predecessor-review
  (assoc macro-artifacts-current-review
         :input-fingerprint "5fe8f9a8d3b7297cd8613e48637528ec01fe0a35f58182a98a4dfa28bfaa8af7"
         :macro-artifacts-current-review (select-keys macro-artifacts-current-review [:status :input-fingerprint :source-version-bridge])
         :current-main-predecessor-review macro-artifacts-predecessor-review
         :pr574-predecessor-review pr574-predecessor-review
         :published-d15-review (:published-d15-review (edn/read-string (slurp "specs/sh-family-crosswalk/pr574-conflict-composition-source-version.edn")))
         :native-pure-source-review (:native-pure-source-review (edn/read-string (slurp "specs/sh-family-crosswalk/pr574-composition-source-version.edn")))
         :previous-Native-review (:previous-review (edn/read-string (slurp "specs/sh-family-crosswalk/pr574-macro-artifacts-composition.edn")))
         :source-version-bridge "specs/sh-family-crosswalk/pr574-macro-artifacts-composition.edn"
         :qualification "Actual macro artifacts and accepted Native header reporting composition only; preserve all predecessor histories and pending unassessed scope."))

(defn git-source [commit path]
  (let [process (.start (doto (ProcessBuilder. ^java.util.List ["git" "show" (str commit ":" path)])
                         (.redirectErrorStream true)))
        text (slurp (.getInputStream process))]
    (when-not (zero? (.waitFor process))
      (throw (ex-info "Pinned local Git source unavailable; no projection written" {:path path :source-commit commit})))
    text))

(def resolution-runtime-current-review
  (:mapping-review (edn/read-string
                    (git-source "a83255c9de9158c6925b97637cd5d64cbdb71892"
                                "specs/sh-family-crosswalk/crosswalk.edn"))))

(def resolution-runtime-predecessor-review
  (:resolution-runtime-predecessor-review resolution-runtime-current-review))

(def mapping-review
  (assoc resolution-main-predecessor-review
         :input-fingerprint "f16680ce6e29658f827e6a50a5bd13fd3c90241aeb01614e4465422c40533f8e"
         :source-version-bridge "specs/sh-family-crosswalk/resolution-runtime-composition-source-version.edn"
         :resolution-main-predecessor-review
         (select-keys resolution-main-predecessor-review
                      [:status :input-fingerprint :source-version-bridge])
         :resolution-runtime-current-review
         (select-keys resolution-runtime-current-review
                      [:status :input-fingerprint :source-version-bridge])
         :resolution-runtime-predecessor-review resolution-runtime-predecessor-review
         :qualification "Native and resolution ownership source composition only; immutable predecessor projections remain pinned, all current states unassessed."))

(defn source-view []
  (let [inventory (edn/read-string (slurp "specs/sh-family-crosswalk/original-inventory.edn"))
        _ (when-not (= original-sh07-sha256 (sh-inventory/sha256 (slurp sh07-path)))
            (throw (ex-info "SH07 source changed; preserve lineage before amending sequence" {})))
        _ (when-not (= (:requirements inventory) (sh-inventory/inventory (slurp sh-inventory/plan-path)))
            (throw (ex-info "Original source changed; retain lineage before rebinding" {})))
        components (mapv second (re-seq #"(?m)^\| [^|\n]+ / `([a-z-]+)` \|" (slurp "docs/component-roadmaps/README.md")))
        tasks (into (sorted-map) (map (fn [c] [c (task-records c)]) components))
        _ (doseq [[c rows] tasks]
            (when-not (= (set (map :id rows)) (set (stable-task-routing-keys c)))
              (throw (ex-info "Task identity population changed; preserve source/ownership routing before amendment" {:component c}))))
        test-files (group-by #(.getName %) (filter #(.isFile %) (file-seq (io/file "bootstrap/clojure/test"))))
        locator (memoize (fn [path] {:path path :sha256 (or (pinned-evidence-hashes path)
                                                          (throw (ex-info "New evidence locator requires an explicit source-version pin" {:path path})))
                                    :source-commit (source-pin path) :kind :source-inspection-not-execution
                                    :current-applicability :unassessed}))
        sh-graph (acceptance-dependencies/milestone-graph (slurp sh-inventory/plan-path))
        historical (into {} (map (fn [r] [(first (:groups r)) r])
                          (span-matches audit-path #"(?m)^\| (SH-\d{2}\.[1-6]) \| [^\n]+$")))
        sections (into {} (map (fn [r] [(first (:groups r)) (:source r)])
                             (span-matches sh-inventory/plan-path #"(?ms)^## (SH-\d{2}) - [^\n]+\n.*?(?=^## |\z)")))
        crows (span-matches sh07-path #"(?m)^\| (SH07-C\d{2}) \| ([^|]+) \| ([^|]+) \| ([^|]+) \| ([^\n]+)\|$")
        sources (distinct (concat [sh-inventory/plan-path sh07-path audit-path p15-whole-gate p15-final-proof
                                    "docs/artifacts/phase-06/reports/current-reader-lexical-products-origins.md"
                                    "docs/phase-15-bootstrap-and-self-hosting/IMPLEMENTATION-ROADMAP.md"
                                    "docs/artifacts/phase-18/self-hosted/p18-t03-self-hosted-release-artifact-proof.edn"
                                    "docs/artifacts/phase-06/reports/current-reader-fresh-syntax-handoff.md"
                                    "docs/artifacts/phase-06/reports/current-reader-decode-source-identity.md"
                                    "docs/self-hosting-slice-backlog.md" "docs/self-hosting-current-migration-cohort.md"
                                    "docs/self-hosting-slice-ownership.edn" "docs/document-inventory.json"
                                    "docs/acceptance-dependencies.md" "docs/component-roadmaps/README.md"
                                    "docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md"
                                    "docs/phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md"
                                    "docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md"
                                    "docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md"
                                    "docs/phase-18-binary-distribution-and-seedless-release/README.md"
                                    "docs/phase-18-binary-distribution-and-seedless-release/IMPLEMENTATION-ROADMAP.md"]
                                  (mapcat (fn [[c rows]] (conj (mapv :path rows) (str "docs/component-roadmaps/" c ".md"))) tasks)))
        requirements
        (mapv (fn [{:keys [id milestone source]}]
                (let [n (Long/parseLong (subs milestone 3)) r (dec (Long/parseLong (subs id 6)))
                      hs (get historical id)
                      refs (mapv #(task-ref tasks %) (get-in routes [n r]))
                      evidence-targets
                      (distinct (for [t refs row (tasks (:component t)) :when (= (:id t) (:id row))
                                      [_ target] (re-seq #"\]\(([^)#]+)(?:#[^)]*)?\)" (get-in row [:source :raw]))
                                      :when (not (re-find #"^[a-z]+:" target))
                                      :let [p (str (.normalize (.resolve (.getParent (.toPath (io/file (:path row)))) target)))]
                                      :when (.isFile (io/file p))] p))
                      checks (distinct (mapcat #(map str (get test-files %))
                                         (re-seq #"[a-z0-9_]+_test\.clj" (:raw (sections milestone)))))]
                  {:id id :milestone milestone :acceptance-source source
                   :context-source (sections milestone)
                   :task-references refs
                   :evidence-source-locators (mapv locator (sort (distinct (concat evidence-targets checks))))
                   :evidence-qualification "Owning test/source/doc locations are exact inspected bytes at the task baseline, not executed or accepted results; missing full closure and current applicability remain explicit."
                   :coverage :mapped-owner-review-pending
                   :relation :acceptance-facet-of-existing-outcomes
                   :completion-prerequisites (vec (sort (sh-graph milestone)))
                   :shared-completion-rule (when (<= 3 n) :sh03-and-later-uniform-completion)
                   :source-policy-qualification (when (legacy-accounting-facet-ids id) :legacy-project-accounting-wording)
                   :historical-completion (if (< n 7)
                     {:state :retained-accepted-original-scope :source-commit (historical-commits n)
                      :evidence-source (:source hs)
                      :original-audit-classification (some-> (re-find #"\| ([^|]+) \|$" (get-in hs [:source :raw])) second str/trim)
                      :current-applicability :unassessed}
                     {:state :no-whole-milestone-completion-claim})
                   :current-evidence (cond
                     (#{"SH-05.1" "SH-05.6" "SH-06.6"} id) :partial-current-cohort-open
                     (< n 7) :historical-supported-slice-current-closure-unassessed
                     (or (= n 7) (<= 8 n 13) (<= 15 n 17) (= n 20)) :partial-work-not-accepted-complete
                     :else :executable-coverage-unassessed)
                   :accepted-current-requirement :unassessed
                   :mapping-review :pending-independent-owner-review
                   :qualification (cond
                     (= n 1) "Owner-reviewed task-authority gap filled by one development-boundaries record with six acceptance facets; historical E01 completion retained, current acceptance unassessed."
                     (= n 7) "All required C01-C18 joins remain open. Six-owner pilot does not define the complete population."
                     (= n 16) "Only selected required pass set. Residual-cost wording is historical project accounting where superseded; semantic proof/check preservation survives."
                     (= n 18) "Selected Linux LLVM release target; Darwin harness evidence cannot substitute."
                     (<= 21 n 29) "Real execution/rebuild/release evidence required; supplied-record validation cannot close this facet."
                     :else "Retain exact source scope, rejects, host boundaries and independently accepted evidence; task mapping grants no completion.")}))
              (:requirements inventory))]
    (when-not (and (= 30 (count routes)) (every? #(= 6 (count %)) routes) (= 18 (count crows)))
      (throw (ex-info "Incomplete original routing population" {})))
    {:schema-version 2 :kind :sh-family-derived-crosswalk :authority :read-only-acceptance-rollup
     :applying-task-source-commit baseline :original-inventory-commit original-baseline
     :applying-source-pins applying-source-pins
     :pr574-macro-artifacts-composition
     {:path "specs/sh-family-crosswalk/pr574-macro-artifacts-composition.edn" :sha256 (sh-inventory/sha256 (slurp "specs/sh-family-crosswalk/pr574-macro-artifacts-composition.edn"))}
     :pr574-composition-source-version
     {:path "specs/sh-family-crosswalk/pr574-composition-source-version.edn" :sha256 (sh-inventory/sha256 (slurp "specs/sh-family-crosswalk/pr574-composition-source-version.edn"))}
     :pr574-conflict-composition-source-version
     {:path "specs/sh-family-crosswalk/pr574-conflict-composition-source-version.edn" :sha256 (sh-inventory/sha256 (slurp "specs/sh-family-crosswalk/pr574-conflict-composition-source-version.edn"))}
     :resolution-runtime-source-version
     {:path "specs/sh-family-crosswalk/resolution-runtime-source-version.edn"
      :sha256 (sh-inventory/sha256 (slurp "specs/sh-family-crosswalk/resolution-runtime-source-version.edn"))}
     :resolution-runtime-composition-source-version
     {:path "specs/sh-family-crosswalk/resolution-runtime-composition-source-version.edn"
      :sha256 (sh-inventory/sha256 (slurp "specs/sh-family-crosswalk/resolution-runtime-composition-source-version.edn"))}
     :macro-artifacts-source-version
     {:path "specs/sh-family-crosswalk/macro-artifacts-source-version.edn"
      :sha256 (sh-inventory/sha256 (slurp "specs/sh-family-crosswalk/macro-artifacts-source-version.edn"))}
     :macro-runtime-source-version
     {:path "specs/sh-family-crosswalk/macro-runtime-source-version.edn"
      :sha256 (sh-inventory/sha256 (slurp "specs/sh-family-crosswalk/macro-runtime-source-version.edn"))}
     :syntax-pipeline-source-version
     {:path "specs/sh-family-crosswalk/syntax-pipeline-source-version.edn"
      :sha256 (sh-inventory/sha256 (slurp "specs/sh-family-crosswalk/syntax-pipeline-source-version.edn"))}
     :reader-assembly-source-version
     {:path "specs/sh-family-crosswalk/reader-assembly-source-version.edn"
      :sha256 (sh-inventory/sha256 (slurp "specs/sh-family-crosswalk/reader-assembly-source-version.edn"))}
     :source-version-bridge {:path "specs/sh-family-crosswalk/source-version-bridge.edn"
                             :sha256 (sh-inventory/sha256 (slurp "specs/sh-family-crosswalk/source-version-bridge.edn"))}
     :task-authority-lineage {:path "specs/sh-family-crosswalk/task-authority-lineage.edn"
                              :sha256 (sh-inventory/sha256 (slurp "specs/sh-family-crosswalk/task-authority-lineage.edn"))}
     :shared-completion-rule
     (let [text (slurp sh-inventory/plan-path)
           start (str/index-of text "## Three boundaries and one shared completion rule")
           end (str/index-of text "### How to use the check references")]
       {:id :sh03-and-later-uniform-completion :applicability :sh03-through-sh29
        :source (sh-inventory/source-span sh-inventory/plan-path text start end)
        :qualification "Implementation, integration and authoritative completion remain separate. Actual owning execution, rejection/preservation/composition, exact provenance/support/TCB and independently accepted stable revision are required only for the applicable claim. SH00-SH02 retain accepted baseline/process scope; no unconditional replay of unchanged evidence."})
     :component-owner-evidence
     (into (sorted-map)
       (for [c components :let [path (str "docs/component-roadmaps/" c ".md") text (slurp path)
                                end (or (str/index-of text "\n## ") (count text))]]
         [c {:source (sh-inventory/source-span path text 0 end)
             :assignment :existing-reservations-preserved-not-inferred
             :qualification "Use the named component owner role and existing actor reservations; a source-unassigned person remains unassigned, not permission for a new writer."}]))
     :global-requirement-owner "01a179ad-2198-7392-966a-1e3c31209027"
     :global-requirement-handoff {:sha256 "25be0fe3d53bf133b72f405d50d039ba3ab9fb186499036587ab72a860174954"
                                :authority :join-proposals-only :accepted-denominator false}
     :source-digests (into (sorted-map) (map (fn [p] [p (sh-inventory/sha256 (slurp p))]) sources))
     :stable-task-routing-keys stable-task-routing-keys
     :retained-native-history {:path "specs/sh-family-crosswalk/retained-native-evidence.edn"
                              :sha256 (sh-inventory/sha256 (slurp "specs/sh-family-crosswalk/retained-native-evidence.edn"))
                              :relation :bounded-history-not-whole-sh-acceptance
                              :scope "Three actual native ABI producers and selected PR368 SH22 consumer progress; original source/provider qualifications and14source facets retained; no generic authentication/C12/selfhost/release acceptance."}
     :accepted-current-scoped-evidence
     [{:task-id "reader/finite-remaining-work/lexical-products-and-origins-closure"
       :implementation-commit "8a0d6a90f00e53fa90a9c0e9fe2c2207c0b09190"
       :composition-commit "3b7c1280a0a5e731e6415dc9e89f2f83e38e354a"
       :integrated-main "2766511b1c68410627775926d9215ee51e40395b"
       :receipt (locator "docs/artifacts/phase-06/reports/current-reader-lexical-products-origins.md")
       :relation :retained-explicit-accepted-task-record
       :qualification "Owner accepted only the existing selected-current enabled lexical/origin row in PR496. Retain baseline escape-offset failures, original genuine seven-call receipt and separate compiler-free composition. New grammar, extension authorization, authentication closure, release and downstream acceptance stay open; all180 whole SH states unassessed."}
      {:task-id "reader/finite-remaining-work/fresh-syntax-handoff"
       :implementation-commit "bcf9c72ed9445e0b8bf259dc5d025c3192ddacc6"
       :receipt (locator "docs/artifacts/phase-06/reports/current-reader-fresh-syntax-handoff.md")
       :relation :retained-explicit-accepted-task-record
       :qualification "Selected-current development-unadmitted fresh Reader/Syntax handoff only; original reported mixed-suite failure retained. No full SH03, canonical Syntax, C5/C14 or release closure."}
      {:task-id "reader/finite-remaining-work/decode-and-source-identity-closure"
       :implementation-commit "1d6c26812ce3f32edefb36b82a54317e2f739586"
       :composition-commit "07e5a32d3f63b236fbd92a1f33b0c7a2995df66d"
       :reviewed-final-head "dd044747cfa25ca58d35d1aed91387d9cf60ff34"
       :integrated-main "4325a9f46292472a7f984c5adfef81209874bb5c"
       :receipt (locator "docs/artifacts/phase-06/reports/current-reader-decode-source-identity.md")
       :relation :retained-explicit-accepted-task-record
       :qualification "Existing Reader owner closed only the original selected-current development-unadmitted decode/source-identity row in PR492. Earlier foreign-directory failure and broker nonexecution remain preserved. No full Reader/C3/C5/C14 or public admission; no SH whole-criterion promotion."}]
     :source-conflicts
     [{:id :p15-whole-gate-vs-final-seed-retirement
       :whole-gate-source (locator p15-whole-gate)
       :whole-gate-recorded-flags (select-keys (edn/read-string (slurp p15-whole-gate)) release-flags)
       :final-seed-retirement-source (locator p15-final-proof)
       :final-seed-retirement-recorded-flags (select-keys (edn/read-string (slurp p15-final-proof)) release-flags)
       :disposition :conflicting-whole-gate-record-no-current-closure
       :qualification "Distinct retained artifacts, not duplicate proof identities. Final proof and P15/P18 roadmap retain incomplete seed retirement; old whole-gate complete flags cannot substitute. Preserve14finite stage0 and P18T00/01/02/04 historical completions; no artifact regeneration or compiler replay."}
      {:id :core-representation-vs-resolved-semantic-lowering
       :sources (mapv locator ["docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md"
                               "docs/phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md"
                               "docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md"
                               "docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md"])
       :disposition :operational-c6-requires-c5-bindings-core-interface-can-precede-resolution
       :qualification "D1 places Core Gravity AST before Name Resolution; C1 places resolve-names before lower-to-core. C5 resolves syntax after expansion and before type/effects; C6 explicitly consumes resolved syntax and C5 bindings. Use that actual C6 consumer edge without requiring completed semantic Core for Resolve. Whether D1 means the identical AST construction event remains an explicit architecture-owner qualification; no source rewrite or invented fused pass."}
      {:id :legacy-project-accounting-wording
       :criterion-ids (vec (sort legacy-accounting-facet-ids))
       :current-policy-source
       (let [path "docs/self-hosting-current-migration-cohort.md" text (slurp path)
             start (str/index-of text "## Current requirements: no project quotas or cost accounting")
             end (str/index-of text "Assessment revision:")]
         (sh-inventory/source-span path text start end))
       :disposition :preserve-mixed-source-with-existing-supersession-qualification
       :global-equivalence-owner "01a179ad-2198-7392-966a-1e3c31209027"
       :qualification "Original raw wording and historical checks remain provenance. Existing current direction retires project work/capacity-only refusals; it does not retire malformed/cyclic/tampered/stale rejection, intrinsic type/Unicode/index/arity/target constraints, semantic resource ownership, actual broker/heap/watchdog/capture/platform controls or external provider spending limits. OOM/timeout remains a resource failure. This SH view makes no new global facet/equivalence adjudication."}]
     :task-authorities tasks :requirements requirements
     :sh07-sequence (mapv (fn [{:keys [groups source]}]
                            {:id (nth groups 0) :requirement-scope (str/trim (nth groups 1))
                             :accountability (str/trim (nth groups 2))
                             :dependency-declaration (str/trim (nth groups 3)) :source source
                             :state :open-source-recorded
                             :relation (if (#{"SH07-C17" "SH07-C18"} (nth groups 0))
                                         :scoped-migration-or-milestone-decision-gate
                                         :implementation-or-composition-provenance-step)
                             :current-ownership-source {:path sh07-path :anchor "outcome-ownership-and-evidence-decisions"}
                             :task-references (case (nth groups 0)
                               "SH07-C01" [(task-ref tasks ["core" 1]) (task-ref tasks ["compiler-api" "pass-evidence"])]
                               "SH07-C03" [(task-ref tasks ["core" 2]) (task-ref tasks ["resolve" 5])]
                               "SH07-C05" [(task-ref tasks ["core" 1]) (task-ref tasks ["core" 2])]
                               "SH07-C06" [(task-ref tasks ["core" 1]) (task-ref tasks ["core" 2])]
                               "SH07-C11" [(task-ref tasks ["core" 4])]
                               "SH07-C12" [(task-ref tasks ["core" 4]) (task-ref tasks ["compiler-api" "pipeline"])]
                               "SH07-C13" [(task-ref tasks ["core" 5]) (task-ref tasks ["compiler-api" "pipeline"])]
                               "SH07-C14" [(task-ref tasks ["core" 5]) (task-ref tasks ["types" 5])]
                               "SH07-C15" [(task-ref tasks ["core" 4]) (task-ref tasks ["diag" 1])]
                               "SH07-C16" [(task-ref tasks ["expand" 5]) (task-ref tasks ["resolve" 5])]
                               "SH07-C17" [(task-ref tasks ["compiler-api" "pass-evidence"])]
                               "SH07-C18" []
                               [(task-ref tasks ["core" 1])])}) crows)
     :scope-boundaries {:deployment "SH29 first declared seedless release; wider platform, targets, deployment services and physical product retain their existing task authorities."
                        :platform-tasks (mapv #(select-keys % [:id :path]) (tasks "platform"))
                        :completion-units "Neither180 source criteria nor18 SH07 provenance IDs nor graph facets are implementation task denominators."
                        :status "Read only component ledgers; preserve scoped historical/current receipts."
                        :missing-coverage "Task entailment and owner review pending; do not infer complete coverage from a nonempty route."
                        :denial "Restricted C12 work remains with its existing owner; no execution or transfer authorized by this mapping."}}))

(defn adopt-review [result review]
  (when-not (and (#{:accepted-mapping-only :pending-source-version-review} (:status review))
                 (= (:input-fingerprint review) (review-fingerprint result)))
    (throw (ex-info "Source/routing inputs differ from accepted review; preserve the old receipt and obtain an explicit successor disposition" {})))
  (if (= :pending-source-version-review (:status review))
    (assoc result :mapping-review review)
    (-> result
      (assoc :mapping-review review)
      (update :requirements (fn [rows] (mapv #(assoc % :coverage :owner-and-independent-reviewed
                                                     :mapping-review :independently-reviewed) rows)))
      (assoc-in [:scope-boundaries :missing-coverage]
                "Exact source/routing mapping reviewed; changed inputs require successor review. Implementation coverage and current whole-criterion acceptance remain separate and unassessed."))))

(defn- map-has-key? [value key]
  (some #(= key (clojure.core/key %)) value))

(defn validate-source-snapshots!
  "Validate a normalized projection without consulting mutable source files."
  [result]
  (let [snapshots (:source-snapshots result)]
    (when-not (map? snapshots)
      (throw (ex-info "Normalized crosswalk lacks source snapshots" {})))
    (doseq [[reference raw] snapshots]
      (when-not (and (string? reference) (string? raw)
                     (= reference (sh-inventory/sha256 raw)))
        (throw (ex-info "Source snapshot digest or payload is invalid"
                        {:source-snapshot-ref reference}))))
    (doseq [node (tree-seq coll? seq (dissoc result :source-snapshots))
            :when (and (map? node) (map-has-key? node :source-snapshot-ref))
            :let [reference (:source-snapshot-ref node)]]
      (when-not (contains? snapshots reference)
        (throw (ex-info "Source snapshot reference is missing"
                        {:source-snapshot-ref reference :path (:path node)}))))
    (when (some #(and (map? %) (map-has-key? % :raw))
                (tree-seq coll? seq (dissoc result :source-snapshots)))
      (throw (ex-info "Normalized crosswalk retains an inline source payload" {})))
    result))

(defn normalize-source-payloads
  "Store each emitted source span once and retain locators on every reference.
  The raw bytes are keyed by their content digest; a span's own path, offsets,
  lines, commit and digest remain on the referencing record."
  [result]
  (if (map-has-key? result :source-snapshots)
    (let [snapshots (into (sorted-map)
                          (map (fn [[reference snapshot]]
                                 [reference (if (map? snapshot) (:raw snapshot) snapshot)])
                               (:source-snapshots result)))]
      (validate-source-snapshots!
        (assoc result :schema-version 2 :source-snapshots snapshots)))
    (let [snapshots (atom (sorted-map))
          normalized
          (walk/postwalk
            (fn [value]
              (if (and (map? value) (map-has-key? value :raw))
                (let [raw (:raw value)]
                  (when-not (string? raw)
                    (throw (ex-info "Source snapshot must contain text bytes" {})))
                  (let [digest (sh-inventory/sha256 raw)]
                    (when (and (:sha256 value) (not= digest (:sha256 value)))
                      (throw (ex-info "Source span digest does not match its bytes"
                                      {:path (:path value)})))
                    (swap! snapshots assoc digest raw)
                    (assoc (dissoc value :raw) :source-snapshot-ref digest)))
                value))
            result)]
      (validate-source-snapshots!
        (assoc normalized :schema-version 2 :source-snapshots @snapshots)))))

(defn normalize-existing-projection []
  (let [projection (edn/read-string (slurp output-path))]
    (when-not (= :sh-family-derived-crosswalk (:kind projection))
      (throw (ex-info "Cannot normalize an unrelated SH projection" {})))
    (let [normalized (normalize-source-payloads projection)]
      ;; Representation-only normalization still authenticates the complete
      ;; source-bound view; it may not launder a forged route or disposition.
      (acceptance-dependencies/task-sh-view normalized slurp)
      normalized)))

(defn build []
  (normalize-source-payloads (adopt-review (source-view) mapping-review)))

(defn rollup [result]
  (str "# SH acceptance rollup\n\nGenerated by tools/sh_crosswalk.clj; read-only. Edit component task authorities,\nnot this view. Source criteria and sequence IDs are provenance, not additive\nimplementation tasks. Mapping/graph reviewed at c6c80530; implementation and\ncurrent whole-criterion acceptance remain separate and unassessed.\n\n"
       "Original source: " original-baseline ". Task authority amendment: " baseline ".\n\n"
       "Source-version adoption: " (name (get-in result [:mapping-review :status])) ". The explicit source-version bridge preserves predecessor pins and new owner-accepted Reader evidence.\n\n"
       "Every requirement retains its complete acceptance/context text, source\nlocators, lines and SHA256; crosswalk.edn stores each distinct source payload once\nin its source-snapshots table and references it by content digest. Current\nacceptance is unassessed unless an exact scoped task receipt explicitly establishes it.\n\n"
       "SH03-SH29 additionally retain the original [shared completion rule](../../docs/self-hosting-milestone-completion-plan.md#three-boundaries-and-one-shared-completion-rule): actual owning execution, rejection/preservation/composition and applicable independently accepted migration evidence. SH00-SH02 keep their historical baseline/process scope.\n\n"
       (apply str
         (for [rows (partition-by :milestone (:requirements result))
               :let [milestone (:milestone (first rows))]]
           (str "## " milestone "\n\nCompletion-credit prerequisites: "
                (if (seq (:completion-prerequisites (first rows)))
                  (str/join ", " (:completion-prerequisites (first rows))) "none")
                ". These do not prohibit interface-based development.\n\n"
                "| Criterion / exact source | Existing canonical task IDs | Evidence classification |\n"
                "| --- | --- | --- |\n"
                (apply str
                  (for [r rows]
                    (str "| [" (:id r) "](https://github.com/burn2delete/uniform/blob/" original-baseline "/"
                         sh-inventory/plan-path "#L" (get-in r [:acceptance-source :start-line]) ") | "
                         (str/join "; " (for [t (:task-references r)]
                                          (str "[" (:component t) "](../" (:component t)
                                               "-workflow/tasks.md#implementation-tasks): `" (:id t) "`")))
                         " | " (name (:current-evidence r))
                         (when (= :retained-accepted-original-scope (get-in r [:historical-completion :state]))
                           (str "; historical original-scope completion `" (get-in r [:historical-completion :source-commit]) "` retained"))
                         ". Current full criterion unassessed. |\n"))) "\n")))
       "## SH07 implementation sequence\n\nThe following 18 rows expand the same six SH07 criteria. They do not add 18\nimplementation completion units. Current outcomes remain open at original scope.\n\n"
       "| Provenance step | Source dependency declaration | Canonical task IDs |\n| --- | --- | --- |\n"
       (apply str (for [r (:sh07-sequence result)]
                    (str "| [" (:id r) "](https://github.com/burn2delete/uniform/blob/" original-baseline "/" sh07-path
                         "#L" (get-in r [:source :start-line]) ") | " (:dependency-declaration r) " | "
                         (if (seq (:task-references r))
                           (str/join "; " (map #(str "`" (:id %) "`") (:task-references r)))
                           "Coordinator milestone decision; no separate implementation task") " |\n")))
       "\n## Wider platform and deployment scope\n\nSH29 closes only the declared first seedless release. The existing hardware,\nfirmware, kernel and devices records plus conditional platform-product decisions\nremain in the Platform task authority. Additional backend, runtime and deployment\nservices retain their own source-bound outcomes. They are not silently completed\nor made universal prerequisites for the selected compiler release.\n"))

(defn validate-pinned-sources! [result read-version]
  (let [locators (for [node (tree-seq coll? seq result) :when (map? node)
                       :let [m (into {} node)]
                       :when (and (:path m) (:source-commit m) (:sha256 m))]
                   [(:source-commit m) (:path m) (:sha256 m)])
        task-files (for [[_ rows] (:task-authorities result) row rows]
                     [(:source-commit row) (:path row) (:file-sha256 row)])
        digests (into (or (:source-digests result) {})
                      (map (fn [[_ path digest]] [path digest]) (concat locators task-files)))
        explicit-pins (for [[path commit] (:applying-source-pins result)
                            :let [digest (get digests path)]]
                        (do (when-not digest
                              (throw (ex-info "Explicit applying pin lacks a whole-file digest" {:path path})))
                            [commit path digest]))]
    (doseq [[commit path expected] (distinct (concat locators task-files explicit-pins))]
      (when-not (= expected (sh-inventory/sha256 (read-version commit path)))
        (throw (ex-info "Source differs from its applying Git pin; preserve lineage before regeneration"
                        {:path path :source-commit commit}))))))

(defn run [args]
  (let [normalize? (contains? #{["--normalize"] ["--normalize-check"]} (vec args))
        result (if normalize? (normalize-existing-projection) (build))]
    (case (vec args)
      ["--write"] (do (validate-pinned-sources! result git-source)
                       (spit output-path (with-out-str (pp/pprint result))) (spit rollup-path (rollup result)))
      ["--normalize"] (do (spit output-path (with-out-str (pp/pprint result)))
                          (spit rollup-path (rollup result)))
      ["--check"] (when-not (and (= result (edn/read-string (slurp output-path))) (= (rollup result) (slurp rollup-path)))
                    (throw (ex-info "Stale SH projection" {})))
      ["--normalize-check"] (when-not (and (= result (edn/read-string (slurp output-path)))
                                            (= (rollup result) (slurp rollup-path)))
                               (throw (ex-info "Stale normalized SH projection" {})))
      (throw (ex-info "Usage: clojure -M tools/sh_crosswalk.clj --write|--normalize|--check|--normalize-check" {})))
    (println "SH projection:180 criteria,18 sequence rows; review state" (get-in result [:mapping-review :status]) "; implementation acceptance unchanged.")))
(when (seq *command-line-args*) (run *command-line-args*))
