(ns c11-source-binding-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.java.io :as io]
            [gravity.digest :as digest]
            [gravity.mir]
            [gravity.c11-mir.source-contract :as contract-data]))

(def root "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/")
(defn forms [file]
  (with-open [reader (java.io.PushbackReader. (io/reader (str root file)))]
    (binding [*read-eval* false]
      (loop [out []]
        (let [form (read {:eof ::end} reader)]
          (if (= ::end form) out (recur (conj out form))))))))
(def owner (create-ns (gensym "c11-source-binding-owner-")))
(def observations (atom []))
(def construction-token (Object.))
(def construction-invocations (atom 0))
(defn fail! [id _ _ extra] (throw (ex-info "Synthetic diagnostic transport" (assoc extra :id id))))
(binding [*ns* owner]
  (clojure.core/refer 'clojure.core)
  (alias 'c11-source-contract (ns-name (:ns (meta #'contract-data/v2-source-contract))))
  (intern owner 'sha256-hex digest/sha256-hex)
  (intern owner 'p15-s23-c11-mir-fail! fail!)
  (intern owner 'p15-s23-c11-mir-resolve-source-path (constantly "<no-fixture>"))
  (intern owner 'p15-s23-c11-mir-contain-exception!
          (fn [_ boundary error]
            (throw (ex-info "Synthetic diagnostic containment"
                            {:missing-fact boundary :cause-class (class error)} error))))
  ;; Declared compile/identity oracles are never a real emitter or compiler.
  (doseq [name '[c-backend-stage2-plan-emitter-source-rule!
                p15-s23-stage2-compiler-artifact-plan
                p15-s23-stage2-compiler-artifact-semantic-input p15-s23-c11-mir-digest]]
    (intern owner name (fn [& _] (throw (ex-info "Unexpected oracle call" {:function name}))))))
(doseq [file ["00726-p15-s23-c6c10-gravity-candidate-verification.clj"
              "00727-p15-s23-c11-mir-required-functions.clj"]
        form (forms file) :when (and (seq? form) (= 'def (first form)))]
  (binding [*ns* owner] (eval form)))
(doseq [file ["00732-p15-s23-c11-mir-owned-upstream-diagnostic.clj"
              "00733-p15-s23-c11-mir-source-binding.clj"]
        form (forms file)
        :when (and (seq? form)
                   (contains? '#{p15-s23-c11-mir-contract
                                 p15-s23-c11-mir-selected-builder-function
                                 p15-s23-c11-mir-selected-verifier-function
                                 p15-s23-c11-mir-current-source-input?
                                 p15-s23-c11-mir-source-contract-for
                                 p15-s23-c11-mir-source-binding!*
                                 p15-s23-c11-mir-source-binding!} (second form)))]
  (binding [*ns* owner] (eval form)))
(binding [*ns* owner]
  ;; This is an isolated token/runner seam.  The runtime, carrier check and
  ;; v3 verifier are declared oracles; the test only proves the opaque token
  ;; is checked before exactly one selected-builder invocation for each
  ;; version.
  (intern owner '__gravity_bootstrap_lexical_values_119416 [construction-token])
  (intern owner '__gravity_bootstrap_lexical_119416_classify-checked-core-ingress-pair
          (fn [& _] :synthetic-ingress-classifier))
  (intern owner 'p15-s23-stage2-runtime-execute-function
          (fn [_ _ selected-function arguments]
            (swap! construction-invocations inc)
            {:status :accepted :selected-function selected-function :arguments arguments}))
  (intern owner 'p15-s23-c11-mir-require-trusted-carrier! (fn [& _] :synthetic-carrier-ok))
  (intern owner 'p15-s23-c11-mir-verify-flat-produced! (fn [& _] :synthetic-v3-verify-ok))
  (eval (first (forms "00744-opaque-c11-construction-token-helper-04-invoke-pinned-c11-builder.clj"))))
(defn call [name & args] (apply (ns-resolve owner name) args))
(defn diagnostic [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))
(defn hash-value [value] (str "sha256:" (digest/sha256-hex (pr-str value))))
(def text "fixture\n")
(def builder 'fixture-builder)
(def verifier 'fixture-verifier)
(def shapes {builder {:arity 1 :params ['core]} verifier {:arity 1 :params ['mir]}})
(def functions (into {} (map (fn [[name shape]] [name (assoc shape :body [:synthetic name])]) shapes)))
(def source-hash (str "sha256:" (digest/sha256-hex text)))
(def plan {:source {:sha256 source-hash} :functions functions :synthetic true})
(def contract
  {:source-byte-count (alength (.getBytes text java.nio.charset.StandardCharsets/UTF_8))
   :source-content-hash source-hash :required-functions shapes
   :plan-semantic-hash (hash-value plan) :functions-semantic-hash (hash-value functions)
   :builder-semantic-hash (hash-value (get functions builder))
   :verifier-semantic-hash (hash-value (get functions verifier))
   :semantic-family :synthetic-unit-fixture})
(defn with-fixture [options f]
  (let [directory (java.nio.file.Files/createTempDirectory "c11-binding-control-"
                                                          (make-array java.nio.file.attribute.FileAttribute 0))
        path (.resolve directory "source.gravity")
        bytes (or (:bytes options) (.getBytes text java.nio.charset.StandardCharsets/UTF_8))
        actual-contract (merge contract (:contract options))
        actual-plan (or (:plan options) plan)
        replacements
        {'p15-s23-c11-mir-resolve-source-path #(str path)
         'p15-s23-c11-mir-contract (fn [version]
                                   (swap! observations conj [:contract version]) actual-contract)
         'p15-s23-c11-mir-selected-builder-function (constantly builder)
         'p15-s23-c11-mir-selected-verifier-function (constantly verifier)
         'c-backend-stage2-plan-emitter-source-rule!
         (fn [& args] (swap! observations conj [:emitter args]) {:emitter :synthetic})
         'p15-s23-stage2-compiler-artifact-plan
         (fn [emitter _source source-text]
           (swap! observations conj [:compiler emitter source-text])
           (when-let [action (:during-compile options)] (action path))
           actual-plan)
         'p15-s23-stage2-compiler-artifact-semantic-input identity
         'p15-s23-c11-mir-digest hash-value}]
    (reset! observations [])
    (try
      (java.nio.file.Files/write path bytes (make-array java.nio.file.OpenOption 0))
      (when-let [action (:before options)] (action path))
      (with-redefs-fn (into {} (map (fn [[name function]] [(ns-resolve owner name) function]) replacements))
        #(f path))
      (finally
        (doseq [file (reverse (file-seq (.toFile directory)))] (io/delete-file file true))))))
(defn binding! [version] (call 'p15-s23-c11-mir-source-binding!* "request.gravity" :native version))
(defn invoke-token-builder [version token]
  (call '__gravity_bootstrap_lexical_119416_invoke-pinned-c11-builder
        {:checked-core true}
        {:source-path "synthetic.gravity"}
        (cond-> {:plan :synthetic-plan}
          (not= 1 version) (assoc :interface-version version))
        :verification-replay token))

(deftest strict-built-in-version-precedes-filesystem-or-compiler
  (doseq [version [nil 0 4 (int 3) 3N "3" {:version 3}]]
    (is (= :reviewed-c11-source-contract (:missing-fact (diagnostic #(binding! version))))))
  (let [callbacks (atom 0)
        foreign (proxy [Object clojure.lang.IMeta] []
                  (meta [] (swap! callbacks inc) {})
                  (hashCode [] (swap! callbacks inc) 0)
                  (equals [_] (swap! callbacks inc) true)
                  (toString [] (swap! callbacks inc) "foreign"))]
    (is (= :reviewed-c11-source-contract (:missing-fact (diagnostic #(binding! foreign)))))
    (is (zero? @callbacks))))

(deftest actual-filesystem-and-byte-refusals-precede-compile-oracles
  (doseq [[options expected]
          [[{:bytes (.getBytes "short" "UTF-8")} :pinned-gravity-c11-source-byte-size]
           [{:contract {:source-content-hash "sha256:changed"}} :pinned-gravity-c11-source-identity]]]
    (with-fixture options
      (fn [_]
        (is (= expected (:missing-fact (diagnostic #(binding! 3)))))
        (is (= [[:contract 3]] @observations)))))
  (with-fixture {:bytes (byte-array [(unchecked-byte 0xc3) (unchecked-byte 0x28)])
                 :contract {:source-byte-count 2}}
    (fn [_]
      (is (= :contained-c11-source-binding-host-failure
             (:missing-fact (diagnostic #(call 'p15-s23-c11-mir-source-binding! "request.gravity" :native 3)))))
      (is (= [[:contract 3]] @observations)))))

(deftest explicit-versions-propagate-selected-identities
  ;; These positive results are declared oracle controls, not compiler plans
  ;; or source authentication evidence. No public constructor/verifier runs.
  (doseq [version [1 2 3]]
    (with-fixture {}
      (fn [path]
        (let [result (binding! version)]
          (is (= (str path) (:source-path result)))
          (is (= source-hash (:source-content-hash result)))
          (is (= shapes (:function-shapes result)))
          (is (identical? plan (:plan result)))
          (is (= (when (not= 1 version) version) (:interface-version result)))
          (is (= (when (= 3 version) builder) (:builder-function result)))
          (is (= (when (= 3 version) verifier) (:verifier-function result)))
          (is (= [:contract :emitter :compiler] (mapv first @observations))))))))

(deftest opaque-token-precedes-one-selected-builder-invocation-across-versions
  (reset! construction-invocations 0)
  (doseq [version [1 2 3]]
    (let [result (invoke-token-builder version construction-token)
          expected (if (= 3 version)
                     (:builder-function (call 'p15-s23-c11-mir-contract 3))
                     (call 'p15-s23-c11-mir-selected-builder-function 1))]
      (is (= :accepted (:status result)))
      (is (= expected (:selected-function result)))))
  (is (= 3 @construction-invocations))
  (let [before @construction-invocations
        data (diagnostic #(invoke-token-builder 3 (Object.)))]
    (is (= :opaque-c11-construction-token (:missing-fact data)))
    (is (= before @construction-invocations))))

(deftest plan-and-function-tampering-is-rejected
  (doseq [[options expected]
          [[{:plan (assoc-in plan [:source :sha256] "changed")} :selected-c11-plan-source-identity]
           [{:plan (assoc-in plan [:functions builder :arity] 2)} :pinned-gravity-c11-function-shapes]
           [{:contract {:plan-semantic-hash "changed"}} :pinned-gravity-c11-function-identity]
           [{:contract {:functions-semantic-hash "changed"}} :pinned-gravity-c11-function-identity]
           [{:contract {:builder-semantic-hash "changed"}} :pinned-gravity-c11-function-identity]
           [{:contract {:verifier-semantic-hash "changed"}} :pinned-gravity-c11-function-identity]]]
    (with-fixture options
      (fn [_]
        (is (= expected (:missing-fact (diagnostic #(binding! 3)))))))))

(deftest legacy-helper-arities-still-select-version-one
  (with-fixture {}
    (fn [_]
      (is (not (contains? (call 'p15-s23-c11-mir-source-binding!* "request.gravity" :native)
                          :interface-version)))
      (is (= [:contract 1] (first @observations)))
      (reset! observations [])
      (is (not (contains? (call 'p15-s23-c11-mir-source-binding! "request.gravity" :native)
                          :interface-version)))
      (is (= [:contract 1] (first @observations))))))

(deftest selected-v3-source-must-remain-the-same-file-and-bytes
  (with-fixture {:during-compile (fn [path]
                                  (java.nio.file.Files/write path (.getBytes "mutated\n" "UTF-8")
                                                             (make-array java.nio.file.OpenOption 0)))}
    (fn [_] (is (= :stable-selected-c11-source (:missing-fact (diagnostic #(binding! 3)))))))
  (with-fixture {:during-compile (fn [path]
                                  (java.nio.file.Files/delete path)
                                  (java.nio.file.Files/write path (.getBytes text "UTF-8")
                                                             (make-array java.nio.file.OpenOption 0)))}
    (fn [_] (is (= :stable-selected-c11-source (:missing-fact (diagnostic #(binding! 3)))))))
  (with-fixture {:before (fn [path]
                          (let [target (.resolveSibling path "target.gravity")]
                            (java.nio.file.Files/move path target (make-array java.nio.file.CopyOption 0))
                            (java.nio.file.Files/createSymbolicLink path target
                                                                   (make-array java.nio.file.attribute.FileAttribute 0))))}
    (fn [_]
      (is (= :stable-selected-c11-source (:missing-fact (diagnostic #(binding! 3)))))
      (is (= [[:contract 3]] @observations)))))

(let [result (run-tests 'c11-source-binding-test)]
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (println {:scope :source-binding-mechanism-controls :filesystem-and-utf8 :actual
            :contract-compiler-and-semantic-identity :declared-oracles
            :source-authentication false :bootstrap-loaded false})
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
