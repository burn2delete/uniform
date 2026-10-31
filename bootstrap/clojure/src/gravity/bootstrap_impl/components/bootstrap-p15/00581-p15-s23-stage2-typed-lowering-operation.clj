;; Temporary orchestration only: current Gravity C7 owns semantic verification,
;; and current Gravity lowering owns all typed/source/definition joins. The raw
;; internal kernels consume explicit DATA, not a cryptographic authority token.
(defn- p15-s23-stage2-typed-lowering-operation
  [operation emitter module definitions typed-input]
  (let [lowering-path (p15-s23-stage2-compiler-artifact-source-path)
        gravity-root (.getParentFile (.getParentFile (java.io.File. lowering-path)))
        c7-path (.getPath (java.io.File. gravity-root "src/gravity/compiler/c7_type_checker_engine.gravity"))
        lowering-text (read-gravity-source-text lowering-path)
        c7-text (read-gravity-source-text c7-path)
        c7-plan (p15-s23-stage2-compiler-artifact-plan emitter c7-path c7-text)
        lowering-plan (p15-s23-stage2-compiler-artifact-plan emitter lowering-path lowering-text)
        runner {:engine :current-gravity-typed-lowering-orchestration}
        fresh-report (p15-s23-stage2-runtime-execute-function
                      runner c7-plan 'sh08-verify-function-type-result
                      [(:request typed-input) (:typed typed-input)])
        reader (get-in typed-input [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact
                                    :sh05-macro-artifact :gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact])
        source-path (get-in reader [:source-unit-record :path])
        source-text (when (and (= :passed (:status fresh-report)) (= :accepted (get-in typed-input [:typed :status])))
                      (c2-reader-artifact-source-text source-path reader))
        observation {:verification fresh-report
                     :source {:path source-path :text source-text
                              :sha256 (when source-text (str "sha256:" (sha256-hex source-text)))}
                     :compiler-sources {:c7 {:source (:source c7-plan) :plan-id (:plan-id c7-plan)}
                                        :lowering {:source (:source lowering-plan) :plan-id (:plan-id lowering-plan)}}}
        function (if (= :function operation) 'p15-s23-typed-compile-function 'p15-s23-typed-assemble-plan-products)
        result (p15-s23-stage2-runtime-execute-function
                runner lowering-plan function [emitter module definitions typed-input observation])]
    (if (= "P15S23Q002" (:diagnostic result))
      (p15-s23-stage2-plan-emitter-fail! "P15S23Q002" (:source-path module) nil (:facts result))
      (p15-s23-stage2-compiler-result! module result))))
