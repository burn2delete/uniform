

(defn sh06-resolution-build-binding!
  [request-source]
  (let [source (sh06-resolution-read-current-source! request-source)
        emitter
        (:emitter
         (c-backend-stage2-plan-emitter-source-rule!
          (:source-path source) :jvm))
        raw-plan
        (p15-s23-stage2-compiler-artifact-plan
         emitter (:source-path source) (:source-text source))
        identities (sh06-resolution-plan-identities raw-plan)]
    (when-not
     (and (= :gravity/stage2-compiler-artifact-plan (:kind raw-plan))
          (true? (:compiler-artifact-plan? raw-plan))
          (= 'gravity.resolution (get-in raw-plan [:module :module]))
          (= :meta (get-in raw-plan [:module :profile]))
          (= :jvm (get-in raw-plan [:module :target]))
          (= #{} (get-in raw-plan [:module :effects]))
          (= #{} (get-in raw-plan [:module :capabilities]))
          (= :safe (get-in raw-plan [:module :safety]))
          (= (:source-content-hash source) (get-in raw-plan [:source :sha256]))
          (= sh06-resolution-public-function-shapes
             (:public-function-shapes identities)))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" request-source :pinned-resolution-plan-and-functions
       identities {}))
    (let [authenticated-plan-base
          (assoc-in (dissoc raw-plan :plan-id)
                    [:source :byte-count] (:source-byte-count source))
          plan
          (assoc authenticated-plan-base :plan-id
                 (str "sha256:"
                      (sha256-hex
                       (pr-str
                        (c-backend-canonical-value
                         authenticated-plan-base)))))]
      (merge source identities
             {:artifact :gravity/sh06-current-resolution-plan-binding
              :revision-status :development-unadmitted
              :status :complete
              :semantic-authority :gravity-source
              :compiled-by :clojure-stage0-seed
              :executed-by :clojure-stage2-generic-rule-runner
              :generic-bridge-residual? true
              :self-hosted? false
              :plan plan}))))

(defn- sh06-resolution-current-source-key []
  (let [source (sh06-resolution-resolve-source-path)
        compiler (java.io.File. (c-backend-resolve-p15-s23-compiler-source-path))
        emitter (java.io.File. (p15-s23-stage2-compiler-artifact-source-path))
        snapshot (fn [file] [(.getCanonicalPath ^java.io.File file)
                            (str "sha256:" (sha256-bytes-hex
                              (java.nio.file.Files/readAllBytes (.toPath ^java.io.File file))))])]
    {:source (snapshot (java.io.File. source)) :toolchain [(snapshot compiler) (snapshot emitter)]}))

(def ^:private sh06-resolution-cached-binding (atom nil))
(def ^:private ^:dynamic *sh06-resolution-operation-binding* nil)

(defn- sh06-resolution-binding-current! [source-path binding]
  (when-not (= (:input-key binding) (sh06-resolution-current-source-key))
    (sh06-resolution-boundary-fail! "C5-UNRESOLVED" source-path
      :resolution-source-or-toolchain-drift (dissoc binding :plan :source-text) {}))
  binding)

(defn sh06-resolution-current-binding!
  [request-source]
  (if *sh06-resolution-operation-binding*
    (sh06-resolution-binding-current! request-source *sh06-resolution-operation-binding*)
    (locking sh06-resolution-cached-binding
      (let [key (sh06-resolution-current-source-key)
            cached @sh06-resolution-cached-binding]
        (when (and cached (not= (:toolchain key) (:toolchain (:input-key cached))))
          (sh06-resolution-boundary-fail! "C5-UNRESOLVED" request-source
            :resolution-toolchain-drift {} {}))
        (if (= key (:input-key cached)) cached
          (let [binding (assoc (sh06-resolution-build-binding! request-source) :input-key key)]
            (when-not (= (second (:source key)) (:source-content-hash binding))
              (sh06-resolution-boundary-fail! "C5-UNRESOLVED" request-source :resolution-source-drift {} {}))
            (sh06-resolution-binding-current! request-source binding)
            (reset! sh06-resolution-cached-binding binding)
            binding))))))

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
