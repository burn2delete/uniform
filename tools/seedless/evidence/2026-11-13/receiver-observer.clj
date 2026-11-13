;; Supplemental source-bound replay of the unchanged authentic retained C7
;; request. This does not rerun the frontend or establish compiler acceptance.
(require '[clojure.edn :as edn] '[clojure.java.io :as io]
         '[clojure.java.shell :as shell] '[clojure.string :as str]
         '[gravity.bootstrap :as bootstrap] '[gravity.compiler-probe :as probe]
         '[gravity.runtime :as runtime]
         '[gravity.self-hosting.sh08-function-call-kernel-support :as support])
(import '[java.io PushbackReader BufferedInputStream]
        '[java.security MessageDigest DigestInputStream]
        '[java.util HexFormat])

(def output-directory "/workspace/scratch/bb81fa003696/foreground-frontier/receiver-retained-request")
(def root (.getCanonicalPath (io/file ".")))
(def c7-source "language/compiler/src/gravity/compiler/c7_type_checker_engine.gravity")
(def c7-hash (str "sha256:" (probe/input-sha256 root c7-source)))
(def input "/workspace/scratch/0ca27623ede8/next-c7-diagnostic-20261112/producer-arguments.edn")
(def expected-request-sha "1d311b33c3cdd8024c3ad992617ae54a6b28299cb78130d467884ac880e3eda3")
(def expected-core "sha256:28ba1a5232045882c90b746e5b0db4d6cb04310f71aa23c3be2d9fb571004c12")
(def started (System/nanoTime))
(def phase (atom :observer-check))
(def seen (atom 0))

(defn event! [value]
  (spit (io/file output-directory "events.edn")
        (str (pr-str (assoc value :elapsed-ms (/ (- (System/nanoTime) started) 1000000.0))) "\n")
        :append true))

(defn sha-file [file]
  (let [digest (MessageDigest/getInstance "SHA-256")]
    (with-open [stream (DigestInputStream. (BufferedInputStream. (io/input-stream file)) digest)]
      (let [buffer (byte-array 65536)]
        (loop [] (when (not= -1 (.read stream buffer)) (recur)))))
    (.formatHex (HexFormat/of) (.digest digest))))

(defn read-one [file]
  (with-open [reader (PushbackReader. (io/reader file :encoding "UTF-8"))]
    (let [eof (Object.) options {:eof eof :readers {}
                                :default (fn [tag _] (throw (ex-info "Unsupported retained request tag" {:tag tag})))}
          value (edn/read options reader)]
      (when (or (identical? eof value) (not (identical? eof (edn/read options reader))))
        (throw (ex-info "Expected exactly one retained request value" {})))
      value)))

(event! {:event :start :scope :supplemental-retained-request-replay-not-acceptance
         :source-root root :execution-revision (str/trim (:out (shell/sh "git" "rev-parse" "HEAD")))
         :c7-source c7-source :c7-sha256 c7-hash :request-path input
         :request-source-revision "0de2f6a2087e27c5dec5e4af7ab71c2a2ae24cd5"
         :frontend-reexecuted false :request-arguments-modified false
         :clojure (clojure-version) :java (System/getProperty "java.version")
         :max-heap-bytes (.maxMemory (Runtime/getRuntime))})

(let [emitter (bootstrap/p15-s23-stage2-current-expression-development-binding!
               (.getCanonicalPath (io/file root "bootstrap/gravity/p15_s23/emitter.gravity")))
      original runtime/execute-function-scoped]
  (binding [bootstrap/*p15-s23-stage2-compiler-artifact-binding* emitter]
    (with-redefs
      [runtime/execute-function-scoped
       (fn [context plan callee arguments scope operations]
         (let [matched (and (= c7-hash (get-in plan [:source :sha256]))
                            (= 'gravity.compiler.c7-type-checker-engine (get-in plan [:module :module])))
               result (original context plan callee arguments scope operations)]
           (when (and matched (= :observer-check @phase) (= 'sh08-mkeys-type? callee))
             (swap! seen inc)
             (event! {:event :delegating-observer-check :plan-id (:plan-id plan)
                      :plan-source (:source plan) :plan-module (:module plan) :result result}))
           (when (and matched (= :retained-request @phase)
                      (= 'sh08-mkeys-header callee) (= :rejected (:status result)))
             (let [[core node call edge nodes bindings types module] arguments
                   receiver-id (first (:argument-node-ids call))
                   owner-id (get-in node [:evaluation :owner-function-syntax-id])
                   owner (first (filter #(= owner-id (:function-syntax-id %)) (:function-records core)))]
               (event! {:event :actual-map-keys-rejection :callee callee :result result
                        :plan-id (:plan-id plan) :plan-source (:source plan)
                        :core-artifact-id (:artifact-id core) :module module
                        :call call :edge edge :node node :receiver-node (get nodes receiver-id)
                        :receiver-node-id receiver-id :receiver-type (get types receiver-id)
                        :operator-binding (get bindings (:operator-binding-id call))
                        :owner-function owner :type-table-size (count types)
                        :argument-count (count arguments) :unchanged-delegation true})))
           result))]
      (let [result (support/invoke-c7 'sh08-mkeys-type?
                                     [{:kind :gravity.type/map :shape :literal :member-types []}])]
        (when-not (and (= true result) (= 1 @seen))
          (throw (ex-info "Delegating observer did not match exact current C7 plan" {:result result :seen @seen}))))
      (let [actual-sha (sha-file input)]
        (when-not (= expected-request-sha actual-sha)
          (throw (ex-info "Authentic retained request bytes changed" {:sha256 actual-sha})))
        (event! {:event :request-sha-verified :sha256 actual-sha :bytes (.length (io/file input))}))
      (let [arguments (read-one input)
            request (first arguments) core (:canonical-core-artifact request)]
        (when-not (and (vector? arguments) (= 1 (count arguments))
                       (= expected-core (:artifact-id core))
                       (= 70 (count (:function-records core)))
                       (empty? (:recursion-components core))
                       (= :passed (get-in request [:coordinator-verification :report :status])))
          (throw (ex-info "Retained request provenance/shape differs" {})))
        (event! {:event :retained-request-read :core-artifact-id (:artifact-id core)
                 :core-function-count (count (:function-records core))
                 :verified-source-path (get-in request [:coordinator-verification :verified-source-path])
                 :original-verification-status (get-in request [:coordinator-verification :report :status])
                 :envelope-source-sha256 (probe/input-sha256 root "language/compiler/src/gravity/compiler/authenticated_envelope.gravity")})
        (reset! phase :retained-request)
        (let [result (support/invoke-c7 'sh08-function-type-core-artifact arguments)]
          (spit (io/file output-directory "type-result.edn") (pr-str result))
          (event! {:event :c7-result :status (:status result)
                   :first-diagnostic (first (:diagnostics result))
                   :c7-sha256-after (probe/input-sha256 root c7-source)}))))))
(shutdown-agents)
