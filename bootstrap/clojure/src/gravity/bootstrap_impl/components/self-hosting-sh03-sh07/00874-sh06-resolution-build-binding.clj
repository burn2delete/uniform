

(defn sh06-resolution-build-binding!
  [request-source]
  (let [source (sh06-resolution-read-pinned-source! request-source)
        emitter
        (:emitter
         (c-backend-stage2-plan-emitter-source-rule!
          (:source-path source) :jvm))
        plan
        (p15-s23-stage2-compiler-artifact-plan
         emitter (:source-path source) (:source-text source))
        identities (sh06-resolution-plan-identities plan)]
    (when-not
     (and (= :gravity/stage2-compiler-artifact-plan (:kind plan))
          (true? (:compiler-artifact-plan? plan))
          (= 'gravity.resolution (get-in plan [:module :module]))
          (= :meta (get-in plan [:module :profile]))
          (= :jvm (get-in plan [:module :target]))
          (= #{} (get-in plan [:module :effects]))
          (= #{} (get-in plan [:module :capabilities]))
          (= :safe (get-in plan [:module :safety]))
          (= sh06-resolution-expected-plan-semantic-hash
             (:plan-semantic-hash identities))
          (= sh06-resolution-expected-functions-semantic-hash
             (:functions-semantic-hash identities))
          (= sh06-resolution-expected-function-count
             (:function-count identities))
          (= sh06-resolution-expected-function-names-hash
             (:function-names-hash identities))
          (= sh06-resolution-expected-function-shapes-hash
             (:function-shapes-hash identities))
          (= sh06-resolution-public-function-hashes
             (:public-function-hashes identities))
          (= sh06-resolution-public-function-shapes
             (:public-function-shapes identities)))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" request-source :pinned-resolution-plan-and-functions
       identities {}))
    (merge source identities
           {:artifact :gravity/sh06-pinned-resolution-plan-binding
            :status :complete
            :semantic-authority :gravity-source
            :compiled-by :clojure-stage0-seed
            :executed-by :clojure-stage2-generic-rule-runner
            :generic-bridge-residual? true
            :self-hosted? false
            :plan plan})))

(def ^:private sh06-resolution-cached-binding
  (delay (sh06-resolution-build-binding!
          "<sh06-resolution-bootstrap>")))

(defn sh06-resolution-current-binding!
  [request-source]
  (let [fresh (sh06-resolution-read-pinned-source! request-source)
        binding @sh06-resolution-cached-binding
        identities (sh06-resolution-plan-identities (:plan binding))]
    (when-not
     (and (= (:source-byte-count fresh) (:source-byte-count binding))
          (= (:source-content-hash fresh) (:source-content-hash binding))
          (= (select-keys
              binding
              [:plan-semantic-hash :functions-semantic-hash
               :function-count :function-names-hash
               :function-shapes-hash :public-function-hashes
               :public-function-shapes])
             identities))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" request-source
       :fresh-resolution-source-and-plan-binding binding {}))
    binding))

(defn sh06-resolution-execute!
  [source-path binding function arguments]
  (try
    (sh04-syntax-strip-host-metadata
     (p15-s23-stage2-runtime-execute-function
      {:engine :gravity-sh06-pinned-resolution-runner
       :compiler-artifact-plan? true}
      (:plan binding) function
      (sh04-syntax-strip-host-metadata arguments)))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch StackOverflowError error
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" source-path :bounded-resolution-host-stack function
       {:contained-host-error (.getName (class error))}))
    (catch AssertionError error
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" source-path :contained-resolution-assertion function
       {:contained-host-error (.getName (class error))}))
    (catch LinkageError error
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" source-path :contained-resolution-linkage function
       {:contained-host-error (.getName (class error))}))
    (catch clojure.lang.ExceptionInfo error
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" source-path
       :contained-resolution-runtime-diagnostic function
       {:contained-diagnostic (:id (ex-data error))}))
    (catch Exception error
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" source-path :contained-resolution-host-failure
       function {:contained-host-error (.getName (class error))
                 :cause-message (.getMessage error)}))))

(def sh06-resolution-transport-bounds
  ;; Per-component request, replay, result, and upstream carrier limits.  These
  ;; remain strict even though the aggregate artifact serializes several
  ;; independently bounded products and projections together.
  {:maximum-carrier-nodes 33554432
   :maximum-carrier-depth 64
   :maximum-container-width 131072})

(def sh06-resolution-artifact-bounds
  ;; The checked-core self-input exceeds the earlier reader-sized envelope.
  ;; Keep a finite power-of-two aggregate ceiling while retaining independent
  ;; component, depth, and width checks.
  {:maximum-carrier-nodes 67108864
   :maximum-carrier-depth 64
   :maximum-container-width 131072
   ;; The serialized ceiling is checked before EDN parsing.
   :maximum-serialized-bytes 1073741824})

(def sh06-resolution-diagnostic-measurement-bounds
  ;; Failure-only measurement ceiling.  This never authorizes transport or
  ;; artifact acceptance; it exists to replace blind resource-bound changes
  ;; with an exact finite observation when the normal aggregate gate rejects.
  {:maximum-carrier-nodes 134217728
   :maximum-carrier-depth 64
   :maximum-container-width 131072})