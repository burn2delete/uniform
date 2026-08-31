

;; SH-05 executes the pinned Gravity macro leaf over authenticated SH-04
;; syntax.  Clojure remains the declared plan runner, digest resolver,
;; envelope binder, compatibility packager, and central route host.
(def sh05-macro-source-relative-path
  "bootstrap/gravity/src/gravity/macro.gravity")
(def sh05-macro-facade-relative-path
  "bootstrap/gravity/src/gravity/compiler/c4_macro_expansion_engine.gravity")
(def sh05-macro-expected-source-byte-count 56488)
(def sh05-macro-expected-source-content-hash
  "sha256:19fe589efb27228b8788347439381b61c907a7b6a562a2a3ac3f7256ae77e549")
(def sh05-macro-expected-plan-semantic-hash
  "sha256:fa8d1c27377204ab134d47ab217b39638acd6eb5a1580276d899850f3fd3d1da")
(def sh05-macro-expected-functions-semantic-hash
  "sha256:50d131f8146232fc09929e5fbcfab4f9db011f720b785d05fe40029bd01ce407")
(def sh05-macro-expected-function-count 46)
(def sh05-macro-expected-function-names-hash
  "sha256:42e599bb9b50599135695c0eb6bf39c4ceb0c79884ad6fcd7b08537871b15dbe")
(def sh05-macro-expected-function-shapes-hash
  "sha256:4a49b16738264948c46c15674b9a96fbc32cb473054bebe3893b5a871d156c1a")
(def sh05-macro-public-function-hashes
  {'sh05-expand-macro-template
   "sha256:7bdb4f2ffebbb9519065e005210f5f2fca2efe90017996d82db9fdd807030b04"
   'sh05-verify-macro-template
   "sha256:599304043c490982bb4c58b919cac16d3e18172f348d4359d62a0e3d9630d241"
   'sh05-verify-macro-resolved
   "sha256:0d3f61ce19a0093d674f5411189dcfd3aebf4ae20a7151c227dbdff15a79b432"})
(def sh05-macro-public-function-shapes
  {'sh05-expand-macro-template {:arity 1 :params ['request]}
   'sh05-verify-macro-template
   {:arity 2 :params ['expansion-template 'digest-requests]}
   'sh05-verify-macro-resolved
   {:arity 3
    :params ['resolved-expansion 'digest-requests 'resolved-digests]}})
(def sh05-macro-adapter-contract
  :gravity/sh05-to-c4-macro-products-v1)
(def sh05-macro-version
  (get sh05-macro-public-function-hashes 'sh05-expand-macro-template))
(def sh05-macro-envelope-stage :c4-macro)
(def sh05-macro-sealed-artifact-kind :gravity/sh05-macro-products)

(defn sh05-macro-boundary-fail!
  [rule source-path missing-field observed facts]
  (c4-macro-fail!
   rule source-path
   {:source-span (source-span source-path 0)
    :macro 'gravity.core/defn
    :macro-version sh05-macro-version
    :profile :meta
    :target :jvm
    :build-effects []
    :capabilities []
    :hygiene {:marks [] :lexical-scopes []}}
   {:severity :error
    :missing-fields [missing-field]
    :facts (merge {:sh05-boundary :gravity-macro-plan} facts)
    :observed observed}))

(defn sh05-macro-resolve-source-path
  []
  (let [anchor (java.io.File.
                (p15-s23-stage2-compiler-artifact-source-path))
        start (if (.isDirectory anchor) anchor (.getParentFile anchor))]
    (or
     (loop [directory start]
       (when directory
         (let [candidate
               (java.io.File. directory sh05-macro-source-relative-path)]
           (if (.isFile candidate)
             (.getPath candidate)
             (recur (.getParentFile directory))))))
     sh05-macro-source-relative-path)))

(defn sh05-macro-read-pinned-source!
  [request-source]
  (let [source-path (sh05-macro-resolve-source-path)
        nio-path (.toPath (java.io.File. source-path))
        nofollow (into-array java.nio.file.LinkOption
                             [java.nio.file.LinkOption/NOFOLLOW_LINKS])
        attributes
        (try
          (java.nio.file.Files/readAttributes
           nio-path java.nio.file.attribute.BasicFileAttributes nofollow)
          (catch Exception error
            (sh05-macro-boundary-fail!
             "C4-TRACE" request-source :pinned-macro-source-readable
             source-path {:cause-message (.getMessage error)})))
        _ (when-not (and attributes (.isRegularFile attributes)
                         (= (long sh05-macro-expected-source-byte-count)
                            (.size attributes)))
            (sh05-macro-boundary-fail!
             "C4-TRACE" request-source :exact-pinned-macro-source
             source-path
             {:expected-byte-count sh05-macro-expected-source-byte-count
              :observed-byte-count (when attributes (.size attributes))}))
        bytes
        (try
          (java.nio.file.Files/readAllBytes nio-path)
          (catch Exception error
            (sh05-macro-boundary-fail!
             "C4-TRACE" request-source :pinned-macro-source-bytes
             source-path {:cause-message (.getMessage error)})))
        content-hash (str "sha256:" (sha256-bytes-hex bytes))]
    (when-not (and (= sh05-macro-expected-source-byte-count
                      (alength bytes))
                   (= sh05-macro-expected-source-content-hash content-hash))
      (sh05-macro-boundary-fail!
       "C4-TRACE" request-source :pinned-macro-source-identity
       source-path {:observed-content-hash content-hash
                    :observed-byte-count (alength bytes)}))
    {:source-path source-path
     :source-text (String. bytes java.nio.charset.StandardCharsets/UTF_8)
     :source-byte-count (alength bytes)
     :source-content-hash content-hash}))

(defn- sh05-macro-compute-plan-identities
  [plan]
  (let [functions (:functions plan)
        shapes
        (into (sorted-map)
              (map (fn [[name function]]
                     [name (select-keys function [:arity :params])]))
              functions)]
    {:plan-semantic-hash
     (p15-s23-c11-mir-digest
      (p15-s23-stage2-compiler-artifact-semantic-input plan))
     :functions-semantic-hash (p15-s23-c11-mir-digest functions)
     :function-count (count functions)
     :function-names-hash
     (p15-s23-c11-mir-digest (vec (keys functions)))
     :function-shapes-hash (p15-s23-c11-mir-digest shapes)
     :public-function-hashes
     (into (sorted-map)
           (map (fn [name]
                  [name (p15-s23-c11-mir-digest (get functions name))]))
           (keys sh05-macro-public-function-hashes))
     :public-function-shapes
     (select-keys shapes (keys sh05-macro-public-function-shapes))}))

(def ^:private sh05-macro-pinned-plan-identities
  (atom nil))

(defn sh05-macro-plan-identities
  [plan]
  (let [[pinned-plan pinned-identities] @sh05-macro-pinned-plan-identities]
    (if (and pinned-plan (identical? pinned-plan plan))
      pinned-identities
      (sh05-macro-compute-plan-identities plan))))

(defn sh05-macro-build-binding!
  [request-source]
  (let [source (sh05-macro-read-pinned-source! request-source)
        emitter
        (:emitter
         (c-backend-stage2-plan-emitter-source-rule!
          (:source-path source) :jvm))
        plan
        (p15-s23-stage2-compiler-artifact-plan
         emitter (:source-path source) (:source-text source))
        identities (sh05-macro-compute-plan-identities plan)]
    (when-not
     (and (= :gravity/stage2-compiler-artifact-plan (:kind plan))
          (true? (:compiler-artifact-plan? plan))
          (= 'gravity.macro (get-in plan [:module :module]))
          (= :meta (get-in plan [:module :profile]))
          (= :jvm (get-in plan [:module :target]))
          (= #{} (get-in plan [:module :effects]))
          (= #{} (get-in plan [:module :capabilities]))
          (= :safe (get-in plan [:module :safety]))
          (= sh05-macro-expected-plan-semantic-hash
             (:plan-semantic-hash identities))
          (= sh05-macro-expected-functions-semantic-hash
             (:functions-semantic-hash identities))
          (= sh05-macro-expected-function-count (:function-count identities))
          (= sh05-macro-expected-function-names-hash
             (:function-names-hash identities))
          (= sh05-macro-expected-function-shapes-hash
             (:function-shapes-hash identities))
          (= sh05-macro-public-function-hashes
             (:public-function-hashes identities))
          (= sh05-macro-public-function-shapes
             (:public-function-shapes identities)))
      (sh05-macro-boundary-fail!
       "C4-TRACE" request-source :pinned-macro-plan-and-functions
       identities {}))
    (reset! sh05-macro-pinned-plan-identities [plan identities])
    (merge source identities
           {:artifact :gravity/sh05-pinned-macro-plan-binding
            :status :complete
            :semantic-authority :gravity-source
            :compiled-by :clojure-stage0-seed
            :executed-by :clojure-stage2-generic-rule-runner
            :generic-bridge-residual? true
            :self-hosted? false
            :plan plan})))

(def ^:private sh05-macro-cached-binding
  (delay (sh05-macro-build-binding! "<sh05-macro-bootstrap>")))

(defn sh05-macro-current-binding!
  [request-source]
  (let [fresh (sh05-macro-read-pinned-source! request-source)
        binding @sh05-macro-cached-binding
        identities (sh05-macro-plan-identities (:plan binding))]
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
      (sh05-macro-boundary-fail!
       "C4-TRACE" request-source :fresh-macro-source-and-plan-binding
       binding {}))
    binding))