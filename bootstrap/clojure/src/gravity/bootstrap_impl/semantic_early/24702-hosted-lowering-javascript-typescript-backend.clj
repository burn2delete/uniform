; Semantic decomposition of HEAD reader line 24702.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-hosted-lowering-javascript-typescript-backend
 [source-path state]
 (let
  [{:keys [js-module ts-declarations js-manifest]} state]
  (assoc
   {}
   :js-ts-backend
   {:javascript-modules
    [{:path "gravity-stage0.mjs",
      :content js-module,
      :hash (:content-hash js-manifest)}],
    :nullish-exception-translation-map
    {:nullish-policy :option-or-opaque,
     :exception-policy :gravity-error-or-panic,
     :status :checked},
    :package-dependency-manifest
    {:dependencies [], :side-effects :none, :status :complete},
    :runtime-target-record
    {:runtime :browser,
     :ecmascript :es2022,
     :module-format :esm,
     :source-map :external,
     :status :pinned},
    :async-effect-boundary-map
    {:promises [],
     :timers [],
     :callbacks [],
     :replay-policy :recorded,
     :status :complete},
    :status :complete,
    :typescript-declarations
    [{:path "gravity-stage0.d.ts",
      :content ts-declarations,
      :hash (c4-artifact-id ts-declarations)}],
    :ui-component-metadata {:enabled false, :status :not-applicable},
    :source-map
    {:source-spans :preserved,
     :generated-origin-chain :preserved,
     :status :complete},
    :numeric-representation-manifest
    {:I64 :BigInt,
     :F64 :Number,
     :checked-boundaries true,
     :status :complete},
    :artifact :gravity/js-ts-backend-manifest,
    :backend :gravity.backend/js-ts,
    :capability-manifest
    {:host-globals
     [{:symbol "globalThis",
       :effects #{},
       :capabilities #{},
       :schema :opaque-host-global}],
     :package-imports [],
     :status :declared}})))
