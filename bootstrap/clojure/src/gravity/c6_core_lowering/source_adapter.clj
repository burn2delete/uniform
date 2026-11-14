(ns gravity.c6-core-lowering.source-adapter
  "Opt-in bridge from the current Gravity C6 source to the hosted operation map.

   This namespace deliberately accepts the existing compiler and runtime
   functions instead of requiring gravity.bootstrap.  The dependency stays
   one-way: a development caller supplies the current-source plan machinery,
   while the normal hosted Clojure operation remains the default route."
  (:require [clojure.java.io :as io]
            [gravity.digest :as digest])
  (:import [java.nio ByteBuffer]
           [java.nio.charset CharacterCodingException CodingErrorAction
            StandardCharsets]
           [java.nio.file Files LinkOption]
           [java.nio.file.attribute BasicFileAttributes]))

(def source-relative-path
  "language/compiler/src/gravity/compiler/c6_core_lowering_engine.gravity")

(def source-function 'c6-form->core-form)

(def evaluation-order-source-function 'c6-eval-order)

(def ^:private source-engine
  :gravity-c6-core-lowering-source-classifier)

(def ^:private no-follow-options
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))

(defn- fail! [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))

(defn- repository-file [repository-root]
  (if (instance? java.nio.file.Path repository-root)
    (.toFile ^java.nio.file.Path repository-root)
    (io/file repository-root)))

(defn source-path
  "Return the canonical path for the current Gravity C6 source."
  [repository-root]
  (.getCanonicalPath (io/file (repository-file repository-root)
                              source-relative-path)))

(defn- source-failure! [source-path reason message data]
  (fail! "C6-SOURCE-CLASSIFIER-SOURCE"
         message
         (merge {:source-path source-path
                 :failure-reason reason}
                data)))

(defn- source-attributes [^java.nio.file.Path path source-path]
  (try
    (Files/readAttributes path BasicFileAttributes no-follow-options)
    (catch java.io.IOException error
      (source-failure!
       source-path :unreadable
       "Current C6 source file cannot be inspected"
       {:cause-message (.getMessage error)}))))

(defn- validate-source-file! [^BasicFileAttributes attributes source-path]
  (when-not (and (.isRegularFile attributes)
                 (not (.isSymbolicLink attributes)))
    (source-failure!
     source-path :not-regular-source-file
     "Current C6 source must be a regular non-symlink file"
     {:regular-file? (.isRegularFile attributes)
      :symbolic-link? (.isSymbolicLink attributes)}))
  attributes)

(defn- strict-source-text [source-path ^bytes bytes]
  (try
    (let [decoder
          (doto (.newDecoder StandardCharsets/UTF_8)
            (.onMalformedInput CodingErrorAction/REPORT)
            (.onUnmappableCharacter CodingErrorAction/REPORT))]
      (.toString (.decode decoder (ByteBuffer/wrap bytes))))
    (catch CharacterCodingException error
      (source-failure!
       source-path :invalid-utf8
       "Current C6 source is not valid UTF-8"
       {:cause-message (.getMessage error)}))))

(defn- source-snapshot! [^java.io.File source-file source-path]
  (let [path (.toPath source-file)
        before (validate-source-file!
                (source-attributes path source-path) source-path)
        bytes (try
                (Files/readAllBytes path)
                (catch java.io.IOException error
                  (source-failure!
                   source-path :unreadable
                   "Current C6 source bytes cannot be read"
                   {:cause-message (.getMessage error)})))
        after (validate-source-file!
               (source-attributes path source-path) source-path)
        source-hash (str "sha256:" (digest/sha256-bytes-hex bytes))]
    (when-not (and (= (.fileKey before) (.fileKey after))
                   (= (.size before) (.size after))
                   (= (.size after) (alength bytes))
                   (= (.lastModifiedTime before)
                      (.lastModifiedTime after)))
      (source-failure!
       source-path :source-changed-during-read
       "Current C6 source changed while being read"
       {:initial-byte-count (.size before)
        :observed-byte-count (alength bytes)}))
    {:source-path source-path
     :source-text (strict-source-text source-path bytes)
     :source-byte-count (alength bytes)
     :source-hash source-hash
     :file-key (.fileKey before)
     :last-modified-time (.lastModifiedTime before)}))

(defn- same-source-snapshot? [left right]
  (and (= (:source-path left) (:source-path right))
       (= (:source-byte-count left) (:source-byte-count right))
       (= (:source-hash left) (:source-hash right))
       (= (:file-key left) (:file-key right))
       (= (:last-modified-time left) (:last-modified-time right))))

(defn- validate-source-plan! [source-path plan]
  (when-not (= :gravity/stage2-compiler-artifact-plan (:kind plan))
    (fail! "C6-SOURCE-CLASSIFIER-PLAN"
           "Current C6 source did not produce a stage2 compiler plan"
           {:source-path source-path :kind (:kind plan)}))
  (let [definition (get-in plan [:functions source-function])]
    (when-not (and (map? definition)
                   (= :def-fn (:definition-form definition))
                   (= 1 (:arity definition))
                   (= ['form] (:params definition)))
      (fail! "C6-SOURCE-CLASSIFIER-ABI"
             "Current C6 source classifier has an unexpected ABI"
             {:source-path source-path
              :function source-function
              :definition (select-keys definition
                                       [:name :params :arity :definition-form])})))
  (let [definition (get-in plan [:functions evaluation-order-source-function])]
    (when-not (and (map? definition)
                   (= :def-fn (:definition-form definition))
                   (= 2 (:arity definition))
                   (= ['form 'child-count] (:params definition)))
      (fail! "C6-SOURCE-EVAL-ORDER-ABI"
             "Current C6 source evaluation-order operation has an unexpected ABI"
             {:source-path source-path
              :function evaluation-order-source-function
              :definition (select-keys definition
                                       [:name :params :arity :definition-form])})))
  plan)

(defn current-source-plan
  "Compile the current C6 source through supplied existing stage2 functions.

   The plan is intentionally explicit and uncached.  Callers may retain it for
   one development invocation, but this adapter does not establish a second
   source cache or modify the default hosted bootstrap route."
  [repository-root compiler-api]
  (let [{:keys [c-backend-stage2-plan-emitter-source-rule!
                p15-s23-stage2-compiler-artifact-plan]}
        compiler-api
        source-file (io/file (repository-file repository-root)
                             source-relative-path)
        source-path (.getCanonicalPath source-file)]
    (when-not (and (fn? c-backend-stage2-plan-emitter-source-rule!)
                   (fn? p15-s23-stage2-compiler-artifact-plan))
      (fail! "C6-SOURCE-CLASSIFIER-COMPILER-API"
             "Current C6 source adapter requires existing compiler functions"
             {:source-path source-path
              :required [:c-backend-stage2-plan-emitter-source-rule!
                         :p15-s23-stage2-compiler-artifact-plan]}))
    (let [before (source-snapshot! source-file source-path)
          source (:source-text before)
          source-hash (:source-hash before)
          emitter-result
          (c-backend-stage2-plan-emitter-source-rule! source-path :jvm)
          emitter (:emitter emitter-result)]
      (when-not (map? emitter)
        (fail! "C6-SOURCE-CLASSIFIER-EMITTER"
               "Current C6 source adapter did not receive an emitter"
               {:source-path source-path
                :emitter-result (select-keys emitter-result [:kind :target])}))
      (let [plan (p15-s23-stage2-compiler-artifact-plan
                  emitter source-path source)
            after (source-snapshot! source-file source-path)]
        (when-not (= source-hash (get-in plan [:source :sha256]))
          (fail! "C6-SOURCE-CLASSIFIER-SOURCE-DRIFT"
                 "Current C6 source plan does not describe the read source"
                 {:source-path source-path
                  :expected-source-hash source-hash
                  :actual-source-hash (get-in plan [:source :sha256])}))
        (when-not (same-source-snapshot? before after)
          (fail! "C6-SOURCE-CLASSIFIER-SOURCE-DRIFT"
                 "Current C6 source changed while its plan was built"
                 {:source-path source-path
                  :expected-source-hash (:source-hash before)
                  :actual-source-hash (:source-hash after)
                  :expected-source-byte-count (:source-byte-count before)
                  :actual-source-byte-count (:source-byte-count after)}))
        (validate-source-plan! source-path plan)))))

(defn c6-form->core-form-operation
  "Build one operation that executes the current source classifier."
  [runtime-execute-function plan]
  (when-not (fn? runtime-execute-function)
    (fail! "C6-SOURCE-CLASSIFIER-RUNTIME-API"
           "Current C6 source adapter requires the existing runtime function"
           {:required :p15-s23-stage2-runtime-execute-function}))
  (validate-source-plan! (get-in plan [:source :path]) plan)
  (fn [form]
    (runtime-execute-function
     {:engine source-engine :compiler-artifact-plan? true}
     plan source-function [form])))

(defn c6-eval-order-operation
  "Build one operation that executes the current source evaluation-order leaf."
  [runtime-execute-function plan]
  (when-not (fn? runtime-execute-function)
    (fail! "C6-SOURCE-EVAL-ORDER-RUNTIME-API"
           "Current C6 source adapter requires the existing runtime function"
           {:required :p15-s23-stage2-runtime-execute-function}))
  (validate-source-plan! (get-in plan [:source :path]) plan)
  (fn [form child-count]
    (runtime-execute-function
     {:engine :gravity-c6-core-lowering-source-evaluation-order
      :compiler-artifact-plan? true}
     plan evaluation-order-source-function [form child-count])))

(defn source-operations
  "Return the explicit operation override for hosted C6 lowering."
  [runtime-execute-function plan]
  {:c6-form->core-form
   (c6-form->core-form-operation runtime-execute-function plan)
   :c6-eval-order
   (c6-eval-order-operation runtime-execute-function plan)})
