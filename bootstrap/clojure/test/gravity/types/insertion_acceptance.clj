(ns gravity.types.insertion-acceptance
  "ACC-05 source-bound preparation only. Never loads or runs a compiler/test owner."
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]))

(def owner-namespace "gravity.self-hosting.sh08-function-call-type-test")
(def owner-path "bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj")
(def source-path "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity")
(def support-paths
  ["bootstrap/clojure/test/gravity/types/insertion_acceptance.clj"
   "bootstrap/clojure/test/gravity/types/insertion_acceptance_test.clj"])
(def lanes
  {:inferred {:consumer 'invoke-c7
              :selectors '[sh08-string-set-insertion-development-kernel
                           sh08-keyword-set-insertion-development-kernel
                           sh08-symbol-set-insertion-development-kernel
                           sh08-insertion-inferred-call-ignores-unrelated-function-count]}
   :outer {:consumer 'sh08-tc-products
           :selectors '[sh08-string-set-insertion-throw-consumer-development-kernel
                        sh08-keyword-set-insertion-throw-consumer-development-kernel
                        sh08-symbol-set-insertion-throw-consumer-development-kernel]}
   :identity {:consumer 'invoke-c7
              :selectors '[sh08-keyword-set-insertion-id-guard-development-kernel
                           sh08-symbol-set-insertion-id-guard-development-kernel
                           sh08-symbol-set-carrier-development-kernel
                           pl-semantic-native-ingress-and-rejection-diagnostics]}
   :consumers {:consumer 'pr359-prepend-case-data
               :selectors '[pl-insertion-contract-parity-and-coherent-stale-fields
                            pl-insertion-effects-rederive-and-reject-coherent-stale-fields]}
   :composition {:consumer 'compiler-c5-resolution-file-artifact
                 :selectors '[current-gravity-string-set-insertion-authenticated-composition
                              current-gravity-keyword-set-insertion-authenticated-composition
                              current-gravity-symbol-set-insertion-authenticated-composition]}})

(defn test-namespace [lane]
  (if (= :composition lane) "gravity.self-hosting.sh01-stage2-runtime-iteration-test"
      owner-namespace))
(defn test-path [lane]
  (if (= :composition lane)
    "bootstrap/clojure/test/gravity/self_hosting/sh01_stage2_runtime_iteration_test.clj"
    owner-path))

(defn ensure! [condition reason]
  (when-not condition (throw (ex-info "Insertion acceptance preparation rejected" {:reason reason}))))

(defn sha256 [bytes]
  (format "%064x" (java.math.BigInteger. 1
                    (.digest (java.security.MessageDigest/getInstance "SHA-256") bytes))))

(defn file-digest [root relative]
  (let [path (.toPath (io/file root relative))]
    (ensure! (and (not (java.nio.file.Files/isSymbolicLink path))
                  (java.nio.file.Files/isRegularFile path (make-array java.nio.file.LinkOption 0)))
             :regular-input)
    (sha256 (java.nio.file.Files/readAllBytes path))))

(defn git! [root & args]
  (let [r (apply shell/sh "git" "-C" root args)]
    (ensure! (zero? (:exit r)) :git-read)
    (str/trim (:out r))))

(defn source-forms [root relative]
  (with-open [r (clojure.lang.LineNumberingPushbackReader. (io/reader (io/file root relative)))]
    (binding [*read-eval* false]
      (loop [out {}]
        (let [form (read {:eof ::end} r)]
          (if (= ::end form) out
              (recur (if (and (seq? form) (#{'defn 'defn- 'deftest} (first form)))
                       (assoc out (second form) form) out))))))))

(defn snapshot!
  "Conservative complete tracked bootstrap/resource/contract closure, read-only.
  This is preparation identity, not runtime/classpath authentication."
  [root]
  (ensure! (.isAbsolute (io/file root)) :absolute-root)
  (let [head (git! root "rev-parse" "HEAD")
        paths (str/split (git! root "ls-tree" "-r" "--name-only" "-z" head) #"\u0000")
        inputs (into (sorted-map)
                     (for [p (distinct (concat paths support-paths)) :when (or (= p "deps.edn")
                                             (str/starts-with? p "bootstrap/")
                                             (str/starts-with? p "contracts/"))]
                       [p (file-digest root p)]))
        identity {:head head :tree (git! root "rev-parse" "HEAD^{tree}")
                  :main (git! root "rev-parse" "origin/main")
                  :main-tree (git! root "rev-parse" "origin/main^{tree}")
                  :inputs inputs}]
    (ensure! (= head (git! root "rev-parse" "HEAD")) :head-drift)
    identity))

(defn prepare
  "Pure adapter. Reject missing/drifted identities and wrong routes before any load.
  observed and definitions come from snapshot!/source-forms, never owner execution."
  [expected observed definitions {:keys [lane consumer selectors] :as request}]
  (ensure! (= #{:lane :consumer :selectors} (set (keys request))) :request-shape)
  (ensure! (and (every? #(re-matches #"[0-9a-f]{40}" (or (get expected %) ""))
                        [:head :tree :main :main-tree])
                (seq (:inputs expected))
                (every? #(re-matches #"[0-9a-f]{64}" %) (vals (:inputs expected)))
                (contains? (:inputs expected) owner-path)
                (contains? (:inputs expected) source-path)
                (contains? (:inputs expected) "deps.edn")) :missing-identity)
  (ensure! (= expected observed) :identity-mismatch)
  (let [route (get lanes lane)]
    (ensure! route :unknown-lane)
    (ensure! (= consumer (:consumer route)) :wrong-consumer)
    (ensure! (= selectors (:selectors route)) :wrong-selectors)
    (doseq [selector selectors]
      (ensure! (= 'deftest (first (get definitions selector))) :missing-test))
    {:kind :prepared-insertion-selection :source expected :lane lane :consumer consumer
     :selectors selectors :namespace (test-namespace lane) :execution :unrun
     :command (into ["clojure" "-J-Xmx2g" "-M:dev-test" "--namespace" (test-namespace lane)]
                    (mapcat #(vector "--exact" (str %)) selectors))}))

(defn prepare! [root expected request]
  (let [before (snapshot! root)
        definitions (source-forms root (test-path (:lane request)))
        after (snapshot! root)]
    (ensure! (= before after) :input-drift)
    (prepare expected after definitions request)))

(defn attribute-result
  "Classify a supplied future result. Never convert setup failure into test refusal.
  Synthetic callers exercise this adapter only, not genuine acceptance."
  [prepared {:keys [source lane consumer selectors phase outcome] :as result}]
  (ensure! (= :prepared-insertion-selection (:kind prepared)) :missing-preparation)
  (ensure! (= source (:source prepared)) :result-source)
  (ensure! (= [lane consumer selectors]
              ((juxt :lane :consumer :selectors) prepared)) :result-selection)
  (ensure! (contains? #{:setup :namespace-load :test} phase) :result-phase)
  (ensure! (contains? #{:passed :failed} outcome) :result-outcome)
  (ensure! (or (= :test phase) (= :failed outcome)) :setup-not-acceptance)
  (assoc result :classification (if (= :test phase) :reported-test-result :pre-test-failure)
         :authority :unverified-reported-result))
