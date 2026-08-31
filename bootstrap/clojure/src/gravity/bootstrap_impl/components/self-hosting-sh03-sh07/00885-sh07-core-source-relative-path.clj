

;; SH-07-A/B1/B2/B3/B4/B5/B6/B7/B8/B9/B10/B11/B12/B13/B15/B47 is the checked-core projection
;; owned by Gravity source.
;; The coordinator authenticates the verified SH-06 carrier, projects the
;; bounded literal/function/control-flow subset, resolves declared digest
;; requests, and keeps physical paths out of semantic identity.
(def sh07-core-source-relative-path
  "bootstrap/gravity/src/gravity/checked_core.gravity")

(def sh07-core-adapter-contract
  :gravity/sh07-to-c6-core-products-v16)

(def sh07-core-governing-document
  "docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md")

(def sh07-core-public-function-shapes
  {'sh07-build-core-template {:arity 1 :params '[request]}
   'sh07-verify-core-template
   {:arity 3 :params '[request template digest-requests]}
   'sh07-verify-core-resolved
   {:arity 4
    :params '[request resolved-core digest-requests resolved-digests]}})

(def sh07-core-expected-source-byte-count 444325)
(def sh07-core-expected-source-content-hash
  "sha256:3e15d5707cf4ea37ef37b8e6089ad6ff62712efc5f6c3659a94edf62bae3f092")
(def sh07-core-expected-plan-semantic-hash
  "sha256:5bc9aeebb830350031c42814a3b47495205bd6108a617fcea977f8c0b918aebd")
(def sh07-core-expected-functions-semantic-hash
  "sha256:6942122229f13d1bb14ae01ffdb37ca52cc555fd68f819cca76f30284fa791db")
(def sh07-core-expected-function-count 305)
(def sh07-core-expected-function-names-hash
  "sha256:4e7bbfcd94db26a468920a87917005eee97a85f8f0448ba32cd689cafc9d02e5")
(def sh07-core-expected-function-shapes-hash
  "sha256:61d6a743d65973ec4cb357c7285fae622c25f42b04a7de6aa8bf0fd0f1c02ee4")
(def sh07-core-public-function-hashes
  {'sh07-build-core-template
   "sha256:3c986f70123a51afb4e788199f559b1d571afd825c2ba72c0a53675eb5c34948"
   'sh07-verify-core-template
   "sha256:4bc863464168971648f1c3e7ee17df32155e6c3d77b6c3d69d138566cf3b1791"
   'sh07-verify-core-resolved
   "sha256:d0aa83b35de51eb7fdbbdef6133aa5b20ec825340bf45d8c3833fb6801ffa8ba"})

(defn sh07-core-source-path
  []
  (let [anchor (java.io.File.
                (p15-s23-stage2-compiler-artifact-source-path))
        start (if (.isDirectory anchor) anchor (.getParentFile anchor))]
    (or
     (loop [directory start]
       (when directory
         (let [candidate
               (java.io.File. directory sh07-core-source-relative-path)]
           (if (.isFile candidate)
             (.getPath candidate)
             (recur (.getParentFile directory))))))
     sh07-core-source-relative-path)))

(defn sh07-core-plan-identities
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
           (keys sh07-core-public-function-shapes))
     :public-function-shapes
     (select-keys shapes (keys sh07-core-public-function-shapes))}))

(defn sh07-core-build-binding!
  []
  (let [source-path (sh07-core-source-path)
        bytes (java.nio.file.Files/readAllBytes
               (.toPath (java.io.File. source-path)))
        source-text
        (String. bytes java.nio.charset.StandardCharsets/UTF_8)
        source-hash (str "sha256:" (sha256-bytes-hex bytes))
        emitter
        (:emitter
         (c-backend-stage2-plan-emitter-source-rule! source-path :jvm))
        plan
        (p15-s23-stage2-compiler-artifact-plan
         emitter source-path source-text)
        identities (sh07-core-plan-identities plan)]
    (when-not
     (and (= sh07-core-expected-source-byte-count (alength bytes))
          (= sh07-core-expected-source-content-hash source-hash)
          (= sh07-core-expected-plan-semantic-hash
             (:plan-semantic-hash identities))
          (= sh07-core-expected-functions-semantic-hash
             (:functions-semantic-hash identities))
          (= sh07-core-expected-function-count
             (:function-count identities))
          (= sh07-core-expected-function-names-hash
             (:function-names-hash identities))
          (= sh07-core-expected-function-shapes-hash
             (:function-shapes-hash identities))
          (= sh07-core-public-function-hashes
             (:public-function-hashes identities))
          (= sh07-core-public-function-shapes
             (:public-function-shapes identities))
          (= 'gravity.checked-core (get-in plan [:module :module]))
          (= :meta (get-in plan [:module :profile]))
          (= :jvm (get-in plan [:module :target]))
          (= #{} (get-in plan [:module :effects]))
          (= #{} (get-in plan [:module :capabilities]))
          (= :safe (get-in plan [:module :safety])))
      (throw
       (ex-info "Pinned SH-07 checked-core source or plan changed"
                {:id "C6-VERIFY"
                 :stage :core-lowering
                 :missing-fields [:pinned-sh07-core-plan]
                 :observed
                 (merge {:source-byte-count (alength bytes)
                         :source-content-hash source-hash}
                        identities)})))
    (merge
     {:artifact :gravity/sh07-pinned-core-plan-binding
      :source-path source-path
      :source-byte-count (alength bytes)
      :source-content-hash source-hash
      :plan plan
      :semantic-authority :gravity-source
      :compiled-by :clojure-stage0-seed
      :executed-by :clojure-stage2-generic-rule-runner
      :generic-bridge-residual? true
      :self-hosted? false}
     identities)))

(def ^:private sh07-core-cached-binding
  (delay (sh07-core-build-binding!)))

(defn sh07-core-execute!
  [source-path function arguments]
  (try
    (sh04-syntax-strip-host-metadata
     (p15-s23-stage2-runtime-execute-function
      {:engine :gravity-sh07-pinned-core-runner
       :compiler-artifact-plan? true}
      (:plan @sh07-core-cached-binding)
      function
      (sh04-syntax-strip-host-metadata arguments)))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch StackOverflowError error
      (throw (ex-info "SH-07 core boundary rejected host stack exhaustion"
                      {:id "C6-VERIFY" :stage :core-lowering
                       :source-path source-path
                       :contained-host-error (.getName (class error))})))
    (catch OutOfMemoryError error
      (throw (ex-info "SH-07 core boundary rejected host memory exhaustion"
                      {:id "C6-VERIFY" :stage :core-lowering
                       :source-path source-path
                       :contained-host-error (.getName (class error))})))
    (catch AssertionError error
      (throw (ex-info "SH-07 core boundary contained an assertion"
                      {:id "C6-VERIFY" :stage :core-lowering
                       :source-path source-path
                       :contained-host-error (.getName (class error))})))
    (catch LinkageError error
      (throw (ex-info "SH-07 core boundary contained a linkage failure"
                      {:id "C6-VERIFY" :stage :core-lowering
                       :source-path source-path
                       :contained-host-error (.getName (class error))})))
    (catch clojure.lang.ExceptionInfo error
      (throw error))
    (catch Exception error
      (throw (ex-info "SH-07 core boundary contained a host failure"
                      {:id "C6-VERIFY" :stage :core-lowering
                       :source-path source-path
                       :contained-host-error (.getName (class error))}
                      error)))))

(defn sh07-core-source-path-from-resolution
  [resolution-artifact]
  (or (get-in resolution-artifact [:provenance :source-path])
      (get-in resolution-artifact
              [:gravity-resolution-boundary
               :authenticated-resolution-request
               :provenance :actual-source-path])
      "<sh07-core-input>"))

(defn sh07-core-semantic-span
  [span]
  (select-keys
   (or (:primary span) span {})
   [:byte-start :byte-end :line-start :column-start
    :line-end :column-end :scalar-start :scalar-end]))

(defn sh07-core-value-kind
  [value]
  (cond
    (nil? value) :nil
    (boolean? value) :boolean
    (integer? value) :integer
    (or (instance? java.math.BigDecimal value)
        (instance? Double value)
        (instance? Float value)) :decimal
    (ratio? value) :ratio
    (char? value) :character
    (string? value) :string
    (keyword? value) :keyword
    (symbol? value) :symbol
    (vector? value) :vector
    (map? value) :map
    (set? value) :set
    (seq? value) :list
    :else :unsupported))