; Semantic decomposition of HEAD reader line 26010.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-artifact-emission-provenance
 [source-path state]
 (let
  [{:keys [manifest-hashes module]} state]
  (assoc
   {}
   :source-debug-map-record
   {:generated-origin-chain :preserved,
    :source-spans :preserved,
    :artifact-hashes manifest-hashes,
    :source-path source-path,
    :locations
    [(str source-path ":read")
     (str source-path ":macro-expansion")
     (str source-path ":core")
     (str source-path ":mir")
     (str source-path ":backend-emission")],
    :phases
    [:read
     :macro-expansion
     :core
     :checked-core
     :mir
     :domain-ir
     :optimization
     :lowering
     :backend-emission],
    :status :preserved,
    :source-unit (:module module),
    :artifact :gravity/source-debug-map}
   :compiler-provenance-record
   {:artifact :gravity/compiler-provenance-record,
    :compiler "gravity-stage0-clojure",
    :compiler-build-id
    (c4-artifact-id
     {:compiler "gravity-stage0-clojure", :task "P07-T05"}),
    :pass-pipeline
    ["C18" "B1" "B2/B3/B7" "B4/B5/B6" "B8/B9/B10/B11/B12" "B13" "B14"],
    :generator :artifact-emission,
    :status :complete}
   :dependency-provenance-record
   {:artifact :gravity/dependency-provenance-record,
    :dependencies ["dependency-graph:stage0"],
    :runtime-providers
    ["runtime-provider:clojure-jvm-stage0"
     "runtime-provider:stage0-gpu"
     "runtime-provider:stage0-mobile"],
    :target-toolchains
    {:gpu :not-required-for-stage0,
     :workflow :not-required-for-stage0,
     :c :not-required-for-stage0,
     :jvm :not-required-for-stage0,
     :mlir :not-required-for-stage0,
     :hdl :not-required-for-stage0,
     :llvm :not-required-for-stage0,
     :mobile :not-required-for-stage0,
     :query :not-required-for-stage0,
     :js-ts :not-required-for-stage0,
     :wasm :not-required-for-stage0},
    :status :complete})))
