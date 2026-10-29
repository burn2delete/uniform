(ns c6-source-binding-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.java.io :as io]
            [gravity.digest :as digest]))

(binding [*ns* *ns*] (load-file "tools/c6_contract_data_test.clj"))
(def controls (find-ns 'c6-contract-data-test))
(def owner (var-get (ns-resolve controls 'owner)))
(def root (var-get (ns-resolve controls 'root)))
(defn evaluate [file predicate]
  (doseq [form ((ns-resolve controls 'forms) (str root file)) :when (predicate form)]
    (binding [*ns* owner] (eval form))))
(defn named? [names] #(and (seq? %) (contains? names (second %))))
(defn call [name & args] (apply (ns-resolve owner name) args))
(defn diagnostic [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))
(binding [*ns* owner]
  (intern owner 'sha256-bytes-hex digest/sha256-bytes-hex)
  (eval '(def ^:dynamic *p15-s23-c6c10-compiled-binding-metrics* nil))
  ;; These external definitions are deliberately unbound. Tests never invoke
  ;; the real compiler or emitter owner through this isolated namespace.
  (eval '(declare p15-s23-c6c10-resolve-source-path
                  c-backend-resolve-p15-s23-compiler-source-path
                  c-backend-stage2-plan-emitter-source-rule! p15-s23-stage2-plan-emitter-fail!
                  p15-s23-stage2-compiler-artifact-plan p15-s23-c6c10-function-merkle-manifest
                  p15-s23-c6c10-plan-pin-input p15-s23-c6c10-canonical-digest
                  p15-s23-c6c10-path-neutral-value)))
(evaluate "00701-p15-s23-c6c10-source-relative-path.clj" #(and (seq? %) (= 'def (first %))))
(evaluate "00702-p15-s23-c6c10-host-fail.clj" (named? '#{p15-s23-c6c10-source-program-version}))
(evaluate "00645-p15-s23-stage2-runtime-artifact-source-relative-path.clj"
          (named? '#{p15-s23-stage2-compiler-expected-source-content-hash
                     p15-s23-stage2-compiler-expected-source-byte-count}))
(evaluate "00719-p15-s23-c6c10-record-compiled-binding-metric.clj" (constantly true))
(evaluate "00720-p15-s23-c6c10-compiled-binding-cache-key.clj" (constantly true))
(defn redefine [functions f]
  (with-redefs-fn (into {} (map (fn [[name function]] [(ns-resolve owner name) function]) functions)) f))

(defn fixture-binding [version]
  (let [contract (call 'p15-s23-c6c10-source-program-contract version)]
    (cond-> (assoc (select-keys contract [:source-byte-count :source-content-hash
                                         :plan-semantic-hash :functions-semantic-hash
                                         :builder-semantic-hash :verifier-semantic-hash])
                   :kind :gravity/p15-s23-c6-c10-source-binding :status :complete
                   :function-shapes (:required-functions contract)
                   :function-manifest {:function-count (:function-count contract)
                                       :chunk-size (:chunk-size contract)
                                       :chunks (mapv #(hash-map :count %) (or (:chunk-counts contract) [64 64 11]))}
                   :plan {:synthetic-compiled-program version})
      (not= 1 version) (assoc :source-contract-version version)
      (= 3 version) (assoc :semantic-family (:semantic-family contract)))))
(def events (atom []))
(defn fixture-inputs [version]
  (let [contract (call 'p15-s23-c6c10-source-program-contract version)]
    (cond-> {:source-snapshot {:source-path "synthetic-c6.gravity"
                              :canonical-source-path "synthetic-c6.gravity"
                              :source-byte-count (:source-byte-count contract)
                              :source-content-hash (:source-content-hash contract)}
             :emitter-rule {:source-path "synthetic-emitter.gravity" :source-rule-hash "synthetic-emitter-id"}}
      (not= 1 version) (assoc :source-contract-version version))))
(defn with-cache-oracles [options f]
  (call 'p15-s23-c6c10-clear-compiled-binding-cache!)
  (reset! events [])
  (try
    (redefine
     {'p15-s23-c6c10-authenticated-source-binding-inputs!
      (fn [_ version]
        (swap! events conj [:input-oracle version])
        (when-let [check (:before-input options)] (check version))
        (fixture-inputs version))
      'p15-s23-c6c10-compile-authenticated-source-binding
      (fn [request inputs]
        (let [version (call 'p15-s23-c6c10-source-program-version inputs)]
          (swap! events conj [:compile-oracle version])
          (when-let [check (:before-compile options)] (check version))
          (assoc (fixture-binding version) :request-source request
                 :source-path (get-in inputs [:source-snapshot :source-path]))))}
     f)
    (finally (call 'p15-s23-c6c10-clear-compiled-binding-cache!))))

(deftest cache-keys-and-binding-validation-are-version-specific
  (let [keys (mapv #(call 'p15-s23-c6c10-compiled-binding-cache-key (fixture-inputs %)) [1 2 3])]
    (is (= 3 (count (set keys))))
    (is (not (contains? (first keys) :source-contract-version)))
    (is (= [2 3] (mapv :source-contract-version (rest keys)))))
  (doseq [version [1 2 3]]
    (is (true? (call 'p15-s23-c6c10-source-binding-valid? (fixture-binding version) version))))
  (doseq [change [#(assoc % :source-contract-version 2)
                  #(assoc % :source-contract-version 3N)
                  #(assoc % :semantic-family :other)
                  #(assoc % :source-content-hash "changed")
                  #(assoc % :plan-semantic-hash "changed")
                  #(assoc % :functions-semantic-hash "changed")
                  #(assoc % :builder-semantic-hash "changed")
                  #(assoc % :verifier-semantic-hash "changed")
                  #(assoc % :function-shapes {})
                  #(assoc-in % [:function-manifest :function-count] 1)
                  #(assoc-in % [:function-manifest :chunks 0 :count] 1)]]
    (is (false? (call 'p15-s23-c6c10-source-binding-valid? (change (fixture-binding 3)) 3)))))

(deftest cache-hit-still-consults-input-owner-and-rebinds-request
  (with-cache-oracles {}
    (fn []
      (let [a (call 'p15-s23-c6c10-source-binding! "first.gravity" 2)
            b (call 'p15-s23-c6c10-source-binding! "second.gravity" 2)]
        (is (identical? (:plan a) (:plan b)))
        (is (= "first.gravity" (:request-source a)))
        (is (= "second.gravity" (:request-source b)))
        (is (= [[:input-oracle 2] [:compile-oracle 2] [:input-oracle 2]] @events))
        (is (= 1 (:entry-count (call 'p15-s23-c6c10-compiled-binding-cache-state))))))))

(deftest input-failure-never-falls-back-to-a-warm-cache
  (let [deny? (atom false)]
    (with-cache-oracles {:before-input (fn [_] (when @deny? (throw (ex-info "Input oracle failure" {:oracle :input-failure}))))}
      (fn []
        (call 'p15-s23-c6c10-source-binding! "first.gravity" 3)
        (reset! deny? true)
        (is (= :input-failure (:oracle (diagnostic #(call 'p15-s23-c6c10-source-binding! "second.gravity" 3)))))
        (is (= [[:input-oracle 3] [:compile-oracle 3] [:input-oracle 3]] @events))))))

(deftest version-switch-and-compiler-failure-do-not-return-another-version
  (with-cache-oracles {:before-compile (fn [version] (when (= 3 version)
                                                     (throw (ex-info "Compile oracle failure" {:oracle :compile-failure}))))}
    (fn []
      (call 'p15-s23-c6c10-source-binding! "v2.gravity" 2)
      (is (= :compile-failure (:oracle (diagnostic #(call 'p15-s23-c6c10-source-binding! "v3.gravity" 3)))))
      (is (= 2 (:source-contract-version (call 'p15-s23-c6c10-source-binding! "v2-again.gravity" 2))))
      (is (= [[:input-oracle 2] [:compile-oracle 2] [:input-oracle 3] [:compile-oracle 3] [:input-oracle 2]] @events)))))

(deftest legacy-helper-and-opaque-cache-entry-identity
  (with-cache-oracles {}
    (fn []
      (let [result (call 'p15-s23-c6c10-source-binding! "legacy.gravity")
            key (call 'p15-s23-c6c10-compiled-binding-cache-key (fixture-inputs 1))]
        (is (not (contains? result :source-contract-version)))
        (is (= [[:input-oracle 1] [:compile-oracle 1]] @events))
        (is (false? (call 'p15-s23-c6c10-valid-compiled-binding-cache-entry?
                          {:schema-version 1 :authority-token (Object.) :cache-key key
                           :binding (fixture-binding 1)} key)))))))

(deftest wrong-version-compiled-binding-is-never-published
  (with-cache-oracles {}
    (fn []
      (redefine {'p15-s23-c6c10-compile-authenticated-source-binding
                 (fn [& _] (fixture-binding 2))}
        (fn []
          (is (= :exact-pinned-gravity-c6-c10-source-binding
                 (:missing-fact (diagnostic #(call 'p15-s23-c6c10-source-binding! "v3.gravity" 3)))))
          (is (zero? (:entry-count (call 'p15-s23-c6c10-compiled-binding-cache-state)))))))))

(defn with-snapshot [bytes contract-change before f]
  (let [directory (java.nio.file.Files/createTempDirectory "c6-snapshot-control-" (make-array java.nio.file.attribute.FileAttribute 0))
        path (.resolve directory "compiler.gravity")
        contract (merge {:source-byte-count (alength ^bytes bytes)
                         :source-content-hash (str "sha256:" (digest/sha256-bytes-hex bytes))} contract-change)]
    (try
      (java.nio.file.Files/write path bytes (make-array java.nio.file.OpenOption 0))
      (when before (before path))
      (redefine {'p15-s23-c6c10-resolve-source-path (constantly (str path))
                 'p15-s23-c6c10-source-program-contract (constantly contract)} #(f path))
      (finally (doseq [file (reverse (file-seq (.toFile directory)))] (io/delete-file file true))))))

(deftest actual-snapshot-checks-pin-bytes-utf8-and-native-file-shape
  (let [bytes (.getBytes "fixture\n" "UTF-8")]
    (with-snapshot bytes {} nil
      (fn [path]
        (let [snapshot (call 'p15-s23-c6c10-read-pinned-source-snapshot! "request.gravity" 3)]
          (is (= "fixture\n" (:source-text snapshot)))
          (is (= (str path) (:source-path snapshot)))
          (is (= 8 (:source-byte-count snapshot))))))
    (doseq [change [{:source-byte-count 7} {:source-content-hash "changed"}]]
      (with-snapshot bytes change nil
        (fn [_] (is (= :pinned-gravity-c6-c10-source-bytes
                         (:missing-fact (diagnostic #(call 'p15-s23-c6c10-read-pinned-source-snapshot! "request.gravity" 3)))))))))
  (with-snapshot (byte-array [(unchecked-byte 0xc3) (unchecked-byte 0x28)]) {} nil
    (fn [_] (is (= :strict-utf8-pinned-gravity-c6-c10-source
                     (:missing-fact (diagnostic #(call 'p15-s23-c6c10-read-pinned-source-snapshot! "request.gravity" 3))))))))

(deftest invalid-version-refuses-before-source-resolution-or-callbacks
  (let [resolutions (atom 0) callbacks (atom 0)
        foreign (proxy [Object clojure.lang.IMeta] []
                  (meta [] (swap! callbacks inc) {})
                  (hashCode [] (swap! callbacks inc) 0)
                  (equals [_] (swap! callbacks inc) true)
                  (toString [] (swap! callbacks inc) "foreign"))]
    (redefine {'p15-s23-c6c10-resolve-source-path #(do (swap! resolutions inc) "missing.gravity")}
      #(doseq [version [nil 4 (int 3) 3N "3" foreign]]
         (is (= :explicit-c6-source-contract-version
                (:missing-fact (diagnostic (fn [] (call 'p15-s23-c6c10-read-pinned-source-snapshot! "request.gravity" version))))))))
    (is (zero? @resolutions))
    (is (zero? @callbacks))))

(deftest missing-and-symlink-source-files-refuse
  (doseq [[before expected]
          [[(fn [path] (java.nio.file.Files/delete path)) :pinned-gravity-c6-c10-source-present]
           [(fn [path]
              (let [target (.resolveSibling path "target.gravity")]
                (java.nio.file.Files/move path target (make-array java.nio.file.CopyOption 0))
                (java.nio.file.Files/createSymbolicLink path target (make-array java.nio.file.attribute.FileAttribute 0))))
            :pinned-gravity-c6-c10-source-bytes]]]
    (with-snapshot (.getBytes "fixture\n" "UTF-8") {} before
      (fn [_] (is (= expected (:missing-fact (diagnostic #(call 'p15-s23-c6c10-read-pinned-source-snapshot! "request.gravity" 3)))))))))

(deftest actual-emitter-file-bookends-and-selected-version-propagation
  (doseq [mutate? [false true]]
    (with-snapshot (.getBytes "fixture\n" "UTF-8") {} nil
      (fn [path]
        (let [emitter (.resolveSibling path "emitter.gravity") calls (atom 0)
              size @(ns-resolve owner 'p15-s23-stage2-compiler-expected-source-byte-count)]
          (java.nio.file.Files/write emitter (byte-array size) (make-array java.nio.file.OpenOption 0))
          (redefine
           {'c-backend-resolve-p15-s23-compiler-source-path (constantly (str emitter))
            'p15-s23-stage2-plan-emitter-fail!
            (fn [id _ _ data] (throw (ex-info "Synthetic emitter diagnostic transport" (assoc data :id id))))
            'c-backend-stage2-plan-emitter-source-rule!
            (fn [_ target]
              (is (= :jvm target))
              (swap! calls inc)
              (when mutate?
                (java.nio.file.Files/setLastModifiedTime
                 emitter (java.nio.file.attribute.FileTime/fromMillis
                          (+ 1000 (.toMillis (java.nio.file.Files/getLastModifiedTime
                                             emitter (make-array java.nio.file.LinkOption 0)))))))
              {:source-path (str emitter) :synthetic-emitter-owner true})}
           (fn []
             (if mutate?
               (is (= :stage2-compiler-source-stable-snapshot
                      (:missing-fact (diagnostic #(call 'p15-s23-c6c10-authenticated-source-binding-inputs! "request.gravity" 3)))))
               (let [inputs (call 'p15-s23-c6c10-authenticated-source-binding-inputs! "request.gravity" 3)]
                 (is (= 3 (:source-contract-version inputs)))
                 (is (= (str path) (get-in inputs [:source-snapshot :source-path])))))
             (is (= 1 @calls)))))))))

(let [result (run-tests 'c6-source-binding-test)]
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (println {:scope :versioned-source-binding-controls :snapshot-io-and-sha256 :actual
            :cache-and-validation :actual :input-owner-and-compiler :declared-oracles
            :fixture-contract :synthetic :source-authentication false :bootstrap-loaded false})
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
