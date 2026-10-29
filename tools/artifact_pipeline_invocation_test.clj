(ns artifact-pipeline-invocation-test
  "Compiler-free controls evaluating the facade's actual orchestration forms.
  No bootstrap, Reader, compiler constructor or real runner is loaded."
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.java.io :as io]))

(def root
  "bootstrap/clojure/src/gravity/authenticated_artifact_pipeline/")

(def stages
  [:source :reader :syntax :macroexpansion :resolution :checked-core])

(def parent-fields
  {:syntax :c2-reader-artifact,
   :macroexpansion :sh04-syntax-artifact,
   :resolution :sh05-macro-artifact,
   :checked-core :sh06-resolution-artifact})

(def modules
  {:artifact :gravity/sh06-module-inputs,
   :schema-version 1,
   :module-artifacts []})

(def invocation
  {:reader-source-path "/fixture/reader.gravity",
   :authenticated-envelope-options {:source :development-current}})

(defn forms [file names]
  (with-open [reader (java.io.PushbackReader.
                      (io/reader (str root file)))]
    (loop [found []]
      (let [form (read {:eof :user/end} reader)]
        (if (= :user/end form)
          (do (assert (= names (set (map second found)))) found)
          (recur
           (cond->
            found
             (and (seq? form) (names (second form)))
             (conj form))))))))

(defn fixture []
  (let [subject (create-ns
                 (gensym "artifact-pipeline-invocation-subject-"))
        calls (atom [])
        cache (atom {})
        reader-binding (atom
                        {:source-content-hash "reader-bytes",
                         :source-byte-count 10,
                         :plan-semantic-hash "reader-plan",
                         :functions-semantic-hash "reader-functions"})
        owner-error (atom nil)
        reject-replay (atom nil)
        after-build (atom nil)
        authority (Object.)
        put (fn [name value] (intern subject name value))
        result (fn [symbol & args]
                 (swap! calls conj [symbol args])
                 (when @owner-error (throw @owner-error))
                 (when (= symbol @reject-replay)
                   (reset! reject-replay nil)
                   (throw (ex-info "owning replay rejected" {:id "OWNER-REPLAY"})))
                 (case
                  symbol
                   sh03-reader-resolve-source-path
                   (:reader-source-path invocation)
                   sh03-reader-verification-invocation!
                   (do
                     (assert (= invocation (second args)))
                     {:binding @reader-binding})
                   sh06-resolution-module-input-views!
                   (let [inputs (second args)]
                     (when-not (=
                                :gravity/sh06-module-inputs
                                (:artifact inputs))
                       (throw
                        (ex-info
                         "owner module refusal"
                         {:id "C5-UNRESOLVED"})))
                     (mapv
                      #(select-keys % [:artifact-id])
                      (:module-artifacts inputs)))
                   sh06-resolution-with-reader-invocation
                   (do (assert (= invocation (second args))) ((nth args 2)))
                   compiler-c2-reader-source-artifact
                   {:artifact-id "reader"}
                   compiler-c3-syntax-source-artifact
                   {:artifact-id "syntax",
                    :c2-reader-artifact (nth args 3)}
                   sh05-macro-artifact-from-c3-artifact
                   {:artifact-id "macro",
                    :sh04-syntax-artifact (second args)}
                   sh06-resolution-artifact-from-sh05-artifact
                   {:artifact-id "resolve",
                    :sh05-macro-artifact (second args),
                    :gravity-resolution-boundary
                    {:module-inputs
                     (if (= 5 (count args)) (nth args 2) modules)}}
                   sh07-core-from-resolution-artifact
                   (do
                     (when @after-build (@after-build))
                     {:artifact-id "core",
                      :sh06-resolution-artifact (first args)})
                   {:status :passed}))]
    (binding [*ns* subject]
      (clojure.core/refer 'clojure.core)
      (doseq [[name value] {'envelope-valid? (fn [& _] true),
                            'tool-binding {},
                            'policy-binding {},
                            'require-stage!
                            (fn [value]
                              (assert (some #{value} stages))),
                            'envelope
                            (fn [stage
                                 lane
                                 source
                                 provenance
                                 parents
                                 payload
                                 hit?
                                 key]
                              {:payload payload,
                               :stage stage,
                               :artifact-id (str "facade/" stage),
                               :mode lane,
                               :cache-hit? hit?,
                               :source source,
                               :cache-key-id key,
                               :provenance provenance,
                               :input-artifact-ids parents,
                               :cache-policy {},
                               :payload-artifact-id
                               (:artifact-id payload)}),
                            'stage-cache cache,
                            'implementation-binding {},
                            'strict-source-text! (fn [_ bytes] bytes),
                            'canonical-id!
                            (fn [_ value] (pr-str value)),
                            'source-identity
                            (fn [& _] {:bytes-hash "source-bytes"}),
                            'authenticate-reader-source!
                            (fn [& _] true),
                            'project-context
                            (fn [_] {:project-root-id "fixture"}),
                            'source-snapshot!
                            (fn [_]
                              {:canonical-path
                               "/fixture/source.gravity",
                               :bytes "source"}),
                            'require-source-path!
                            (fn [value] (assert (string? value))),
                            'contract-binding {},
                            'stage-pass-ids {},
                            'stable-source-after!
                            (fn [& _]
                              (swap! calls conj [:source-bookend []])),
                            'bootstrap-call result,
                            'pipeline-id :fixture,
                            'receipt (fn [& _] {}),
                            'bootstrap-var
                            (fn [_]
                              (intern subject 'authority authority)),
                            'source-payload
                            (fn [& _] {:source-identity-id "source"}),
                            'pipeline-schema-version 1,
                            'stage-index (zipmap stages (range)),
                            'require-lane!
                            (fn [value]
                              (assert (#{:fast :fresh} value))),
                            'stages stages}]
        (put name value))
      (.setDynamic (intern subject '*pending-cache* nil))
      (put
       'cache-put!
       (fn [key value]
         (swap!
          @(ns-resolve subject '*pending-cache*)
          assoc
          key
          value)))
      (put
       'commit-pending-cache!
       (fn [pending] (swap! cache merge pending)))
      (doseq [form (concat
                    (forms "02-clear-cache.clj" #{'fail!})
                    (forms
                     "03-revalidate-payload.clj"
                     #{'parent-ids
                       'revalidate-payload?
                       'cache-key-preimage})
                    (forms
                     "04-envelope-valid.clj"
                     #{'build-stage!
                       'embedded-parent-id
                       'stable-reader-after!
                       'validate-request
                       'authenticate-embedded-parent!
                       'reader-operation!
                       'build-payload})
                    (forms "05-run-pipeline.clj" #{'run-pipeline}))]
        (eval form)))
    {:namespace subject,
     :calls calls,
     :cache cache,
     :binding reader-binding,
     :owner-error owner-error,
     :reject-replay reject-replay,
     :after-build after-build,
     :authority authority,
     :call (fn [name & args] (apply (ns-resolve subject name) args))}))

(defn with-subject [check]
  (let [subject (fixture)]
    (try
      (check subject)
      (finally (remove-ns (ns-name (:namespace subject)))))))

(defn request [lane]
  {:source-path "/fixture/source.gravity",
   :reader :development-current,
   :mode lane})

(defn constructors [calls]
  (filterv
   #(some
     #{(first %)}
     '[compiler-c2-reader-source-artifact
       compiler-c3-syntax-source-artifact
       sh05-macro-artifact-from-c3-artifact
       sh06-resolution-artifact-from-sh05-artifact
       sh07-core-from-resolution-artifact])
   calls))

(deftest
  closed-public-option-preserves-legacy-and-rejects-caller-invocations
  (with-subject
    (fn [s]
      (is
       (=
        {:source-path "fixture",
         :target-stage :checked-core,
         :mode :fast}
        ((:call s) 'validate-request {:source-path "fixture"})))
      (doseq [bad [{:reader nil}
                   {:reader :historical}
                   {:invocation invocation}
                   {:reader-operation {}}
                   {:module-inputs modules}
                   {:context {:foreign true}}]]
        (is
         (thrown?
          clojure.lang.ExceptionInfo
          ((:call s)
           'validate-request
           (merge {:source-path "fixture"} bad)))))
      (is (empty? @(:calls s))))))

(deftest
  actual-owner-selection-and-module-validation-precede-construction
  (with-subject
    (fn [s]
      (let [operation ((:call s)
                       'reader-operation!
                       "source"
                       (request :fresh))]
        (is (= invocation (:invocation operation)))
        (is (= @(:binding s) (:binding operation)))
        (is (= modules (:module-inputs operation)))
        (is (= [] (:module-artifact-ids operation)))
        (is
         (=
          '[sh03-reader-resolve-source-path
            sh03-reader-verification-invocation!
            sh06-resolution-module-input-views!]
          (mapv first @(:calls s)))))
      (reset! (:binding s) {:source-content-hash "incomplete"})
      (is
       (thrown?
        clojure.lang.ExceptionInfo
        ((:call s) 'reader-operation! "source" (request :fresh)))))))

(deftest
  exact-current-constructor-arities-and-immediate-parent-objects
  (with-subject
    (fn [s]
      (let [operation ((:call s)
                       'reader-operation!
                       "source"
                       (request :fresh))
            request {:reader-operation operation}
            parent (Object.)
            previous [{:payload parent}]]
        (reset! (:calls s) [])
        (doseq [stage (rest stages)]
          ((:call s)
           'build-payload
           stage
           "source"
           "text"
           {:context :actual}
           previous
           request))
        (let [calls (constructors @(:calls s))]
          (is (= [7 7 3 5 2] (mapv #(count (second %)) calls)))
          (doseq [[_ args] calls]
            (is (identical? (:invocation operation) (last args))))
          (doseq [index [1 2 3]]
            (is
             (identical?
              parent
              (nth
               (second (nth calls index))
               (if (= index 1) 3 1)))))
          (is
           (identical?
            (:authority s)
            (nth (second (second calls)) 4)))
          (is (= modules (nth (second (nth calls 3)) 2)))
          (is (identical? parent (first (second (last calls))))))))))

(deftest
  historical-construction-arities-remain-unchanged
  (with-subject
    (fn [s]
      (doseq [stage (rest stages)]
        ((:call s)
         'build-payload
         stage
         "source"
         "text"
         {}
         [{:payload {}}]))
      (is
       (=
        [3 5 2 2 1]
        (mapv #(count (second %)) (constructors @(:calls s))))))))

(deftest
  replay-forwards-the-same-invocation-to-every-owner
  (with-subject
    (fn [s]
      (let [operation {:invocation invocation, :module-inputs modules}
            payload {:gravity-resolution-boundary
                     {:module-inputs modules},
                     :sh06-resolution-artifact
                     {:gravity-resolution-boundary
                      {:module-inputs modules}}}]
        (doseq [stage (rest stages)]
          (is
           (true?
            ((:call s)
             'revalidate-payload?
             stage
             "source"
             payload
             operation))))
        (is
         (=
          '[c2-reader-validate!
            c3-reader-artifact-view
            c3-syntax-validate!
            sh05-macro-artifact-verification
            sh06-resolution-artifact-verification
            sh07-core-artifact-verification]
          (mapv first @(:calls s))))
        (doseq [[symbol args] @(:calls s)
                :when (not= symbol 'c2-reader-validate!)]
          (is (identical? invocation (last args))))
        (is
         (=
          :gravity.bootstrap/legacy
          (second (second (nth @(:calls s) 4)))))
        (reset! (:calls s) [])
        (doseq [stage [:resolution :checked-core]]
          (is
           (false?
            ((:call s)
             'revalidate-payload?
             stage
             "source"
             {}
             operation))))
        (is (empty? @(:calls s)))))))

(deftest
  cache-identity-binds-each-reader-and-module-input-fact
  (with-subject
    (fn [s]
      (let [operation ((:call s)
                       'reader-operation!
                       "source"
                       (request :fast))
            key (fn [value]
                  ((:call s)
                   'cache-key-preimage
                   :syntax
                   :fast
                   {}
                   []
                   {:reader-operation value}))
            original (key operation)]
        (doseq [field (keys (:binding operation))]
          (is
           (not=
            original
            (key (assoc-in operation [:binding field] "changed")))))
        (is
         (not=
          original
          (key
           (assoc
            operation
            :module-artifact-ids
            ["other-module"]))))
        (is
         (not
          (contains?
           ((:call s) 'cache-key-preimage :syntax :fast {} [] {})
           :reader-operation)))))))

(deftest
  fresh-and-fast-actual-pipeline-paths-have-authenticated-replay-and-no-fresh-cache-publication
  (with-subject
    (fn [s]
      (let [fresh ((:call s) 'run-pipeline (request :fresh))]
        (is (= :checked-core (:requested-stage fresh)))
        (is (empty? @(:cache s)))
        (is (= 5 (count (constructors @(:calls s)))))
        (is
         (=
          2
          (count
           (filter
            #(= 'sh03-reader-verification-invocation! (first %))
            @(:calls s))))))
      ((:call s) 'run-pipeline (request :fast))
      (is (= 6 (count @(:cache s))))
      (reset! (:calls s) [])
      (let [hit ((:call s) 'run-pipeline (request :fast))]
        (is (every? :cache-hit? (vals (:stage-artifacts hit))))
        (is (empty? (constructors @(:calls s)))))
      (swap! (:binding s) assoc :plan-semantic-hash "new-plan")
      (reset! (:calls s) [])
      ((:call s) 'run-pipeline (request :fast))
      (is (= 5 (count (constructors @(:calls s))))))))

(deftest
  changed-reader-bookend-and-owner-errors-prevent-cache-publication
  (with-subject
    (fn [s]
      (reset!
       (:after-build s)
       #(swap!
         (:binding s)
         assoc
         :source-content-hash
         "changed-during-build"))
      (let [error (try
                    ((:call s) 'run-pipeline (request :fast))
                    nil
                    (catch clojure.lang.ExceptionInfo error error))]
        (is (= "ARTIFACT-MUTATED" (:id (ex-data error))))
        (is (empty? @(:cache s))))))
  (with-subject
    (fn [s]
      (let [original (ex-info "owning refusal" {:id "C5-UNRESOLVED"})]
        (reset! (:owner-error s) original)
        (let [error (try
                      ((:call s) 'run-pipeline (request :fast))
                      nil
                      (catch clojure.lang.ExceptionInfo error error))]
          (is (identical? original error))
          (is (empty? @(:cache s))))))))

(deftest
  compiler-and-real-operation-namespaces-remain-absent
  (doseq [namespace '[gravity.bootstrap
                      gravity.authenticated-artifact-pipeline
                      gravity.self-hosting.sh01-host-resource-broker
                      gravity.self-hosting.sh01-parallel-test-runner]]
    (is (nil? (find-ns namespace)))))

(deftest cached-module-substitution-is-evicted-before-rebuild
  (with-subject
    (fn [s]
      ((:call s) 'run-pipeline (request :fast))
      (let [key (first (for [[key value] @(:cache s) :when (= :resolution (:stage value))] key))]
        (swap! (:cache s) assoc-in [key :payload :gravity-resolution-boundary :module-inputs] {:foreign true})
        (reset! (:calls s) [])
        ((:call s) 'run-pipeline (request :fast))
        (is (= '[sh06-resolution-artifact-from-sh05-artifact] (mapv first (constructors @(:calls s)))))
        (is (= modules (get-in @(:cache s) [key :payload :gravity-resolution-boundary :module-inputs])))))))

(deftest each-owning-replay-refusal-forces-reconstruction
  (doseq [verifier '[c3-reader-artifact-view c3-syntax-validate! sh05-macro-artifact-verification
                     sh06-resolution-artifact-verification sh07-core-artifact-verification]]
    (with-subject
      (fn [s]
        ((:call s) 'run-pipeline (request :fast))
        (reset! (:calls s) [])
        (reset! (:reject-replay s) verifier)
        ((:call s) 'run-pipeline (request :fast))
        (is (= 1 (count (constructors @(:calls s)))))
        (is (nil? @(:reject-replay s)))))))

(deftest module-input-changes-invalidate-current-keys-and-malformed-input-never-publishes
  (with-subject
    (fn [s]
      ((:call s) 'run-pipeline (request :fast))
      (reset! (:calls s) [])
      (let [inputs (assoc modules :module-artifacts [{:artifact-id "module/second"}])
            result ((:call s) 'run-pipeline (assoc (request :fast) :module-inputs inputs))]
        (is (= 5 (count (constructors @(:calls s)))))
        (is (= 2 (count (filter #(= 'sh06-resolution-with-reader-invocation (first %)) @(:calls s)))))
        (is (= inputs (get-in result [:payload :sh06-resolution-artifact :gravity-resolution-boundary :module-inputs]))))))
  (with-subject
    (fn [s]
      (let [error (try ((:call s) 'run-pipeline (assoc (request :fast) :module-inputs {:foreign true})) nil
                       (catch clojure.lang.ExceptionInfo error error))]
        (is (= "C5-UNRESOLVED" (:id (ex-data error))))
        (is (empty? (constructors @(:calls s))))
        (is (empty? @(:cache s)))))))

(let [result (run-tests 'artifact-pipeline-invocation-test)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
