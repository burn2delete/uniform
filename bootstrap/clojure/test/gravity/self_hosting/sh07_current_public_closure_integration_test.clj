;; Reproduce from the repository root with the existing development runner:
;; clojure -J-Xmx2g -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e '
;; (require (quote [gravity.self-hosting.sh01-host-resource-broker :as broker])
;;          (quote [gravity.development-test-runner :as runner]))
;; (let [exit (broker/with-lease
;;              {:coordination-root "/private/tmp/gravity-sh01-resource-broker-v1" :timeout-ms 600000}
;;              :normal
;;              (fn []
;;                (let [watchdog (doto (Thread. (fn [] (try (Thread/sleep 1200000) (System/exit 124)
;;                                                        (catch InterruptedException _))))
;;                                 (.setDaemon true) (.start))]
;;                  (try (runner/run-cli!
;;                         ["--namespace" "gravity.self-hosting.sh07-current-public-closure-integration-test"
;;                          "--namespace" "gravity.self-hosting.sh07-public-development-ingress-test"
;;                          "--prefix" "paired-"
;;                          "--exact" "public-api-options-are-closed-before-target-read"
;;                          "--exact" "default-public-api-retains-pinned-constructor"
;;                          "--exact" "public-cli-options-have-exact-arity-and-spelling"
;;                          "--exact" "strict-public-source-policy-applies-to-both-modes"
;;                          "--exact" "development-public-api-retains-the-single-target-snapshot"])
;;                       (finally (.interrupt watchdog))))))]
;;   (shutdown-agents) (System/exit exit))'
;; This is development integration evidence. The watchdog is a host process
;; safeguard; no source quota, accounting rule, admission registry or lane is added.

(ns gravity.self-hosting.sh07-current-public-closure-integration-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as b]))

;; Actual public source construction, not historical capture association. These
;; selected occurrences are integration cases; the complete six-owner required
;; population in self-hosting-sh07-completion-plan.md remains a separate gate.
(def observations (atom []))
(def fixture-root "bootstrap/clojure/fixtures/self-hosting/")
(def current-reader {:reader :development-current})

(defn path [relative] (.getCanonicalPath (io/file relative)))
(defn one [rows predicate]
  (let [matches (filterv predicate rows)]
    (when-not (= 1 (count matches))
      (throw (ex-info "Expected one authentic Core record" {:matches (count matches)})))
    (first matches)))

(defn build! [relative]
  (println {:public-source-core :begin :source relative}) (flush)
  (let [source (path relative)
        text (slurp source :encoding "UTF-8")
        artifact (b/sh07-public-core-file-artifact source current-reader)
        resolved (:sh06-resolution-artifact artifact)
        boundary (:gravity-core-boundary artifact)
        core (:canonical-core-artifact boundary)
        c2 (get-in resolved [:sh05-macro-artifact :gravity-macro-boundary
                            :authenticated-sh04-artifact :c2-reader-artifact])]
    (is (= :accepted (:status artifact) (:status resolved)))
    (is (= :gravity.checked-core (get-in artifact [:pass :owner])))
    (is (= :passed (get-in boundary [:template-verification :status])
           (get-in boundary [:resolved-verification :status])))
    (is (= source (get-in artifact [:provenance :source-path])
           (get-in core [:provenance :actual-source-path])
           (get-in c2 [:source-unit-record :path])))
    (is (= (str "sha256:" (b/sha256-hex text))
           (get-in c2 [:source-unit-record :bytes-hash])))
    (let [forms (into {} (map (juxt :form-id identity)) (:forms (:authenticated-core-request boundary)))
          mappings (into {} (map (juxt :core-node-id identity)) (:source-map core))
          nodes (into {} (map (juxt :node-id identity)) (:nodes core))]
      (doseq [node (:nodes core)
              :let [source (:source node) form (forms (:form-id source))
                    mapping (mappings (:node-id node))
                    fields [:syntax-id :form-id :origin-chain :generated-origin]]]
        (is (and (map? form) (map? mapping)))
        (is (= (select-keys form fields) (select-keys source fields) (select-keys mapping fields)))
        (is (= (:source-span form) (:source-span mapping)))
        (when (= :if (:core-form node))
          (is (= 3 (count (:children node))))
          (is (= (vec (rest (:child-form-ids form)))
                 (mapv #(get-in nodes [% :source :form-id]) (:children node)))))))
    (is (false? (:target-source-reread? boundary)))
    (is (false? (get-in artifact [:execution-boundary :self-hosted?])))
    (swap! observations conj {:source relative :status (:status artifact)
                             :artifact-id (:artifact-id artifact)
                             :source-hash (get-in c2 [:source-unit-record :bytes-hash])})
    artifact))

(defn pair! [basename check!]
  (let [base (str fixture-root "sh-07/" basename)
        gravity (str base ".gravity") qst (str base ".qst")]
    (is (= (slurp gravity :encoding "UTF-8") (slurp qst :encoding "UTF-8")))
    (let [g (build! gravity) q (build! qst)]
      (check! g) (check! q)
      (is (= (:artifact-id g) (:artifact-id q)))
      (is (= (b/sh07-core-artifact-identity-input g)
             (b/sh07-core-artifact-identity-input q)))
      (is (not= (:provenance g) (:provenance q)))
      g)))

(defn selected-source! [fixture owner start-marker end-marker]
  (let [source (slurp (path owner) :encoding "UTF-8")
        start (.indexOf ^String source start-marker)
        end (.indexOf ^String source end-marker start)]
    (is (and (<= 0 start) (< start end)))
    (when (and (<= 0 start) (< start end))
      (is (str/ends-with? (slurp (path (str fixture-root "sh-07/" fixture ".gravity")) :encoding "UTF-8")
                         (str (str/trim (subs source start end)) "\n"))))))

(defn check-two-slot! [artifact]
  (let [core (get-in artifact [:gravity-core-boundary :canonical-core-artifact])
        nodes (into {} (map (juxt :node-id identity)) (:nodes core))
        slots (:loop-bindings core)
        target (one (:recur-targets core) #(= :loop (:target-kind %)))
        transfer (one (:recur-transfers core) (constantly true))
        calls (mapv #(get-in nodes [% :attributes :symbol])
                    (map :operator-node-id (:calls core)))
        by-call (into {} (map (fn [call] [(get-in nodes [(:operator-node-id call) :attributes :symbol]) call])) (:calls core))
        loop-node (nodes (:owner-core-node-id target))]
    (is (= '[remaining observed] (mapv :name slots)))
    (is (= 2 (:arity target) (:arity transfer)))
    (is (= (:target-id target) (:target-id transfer)))
    (is (= (mapv :binding-id slots) (:binding-ids target)))
    (is (= (mapv :initializer-node-id slots) (vec (take 2 (:children loop-node)))))
    (is (= :collection-literal (:core-form (nodes (:initializer-node-id (second slots))))))
    (is (= #{} (get-in nodes [(:initializer-node-id (second slots)) :attributes :source-value])))
    (is (= [(:core-node-id (by-call 'rest)) (:core-node-id (by-call 'conj))]
           (:argument-node-ids transfer)))
    (is (= true (:tail-position transfer)))
    (is (= :arguments-left-to-right (:evaluation-order transfer)))
    (is (= 2 (count (filter #(= :if (:core-form %)) (:nodes core)))))
    (let [verified (b/sh07-core-artifact-verification artifact)]
      (is (= :passed (:status verified)))
      (is (empty? (:failed-checks verified)))
      (is (true? (get-in verified [:checks :authoritative-products-replay?]))))
    (is (every? (set calls) '[count = contains? first rest conj]))))

(defn coherent-bool-substitution [artifact]
  ;; Deliberately mutate a genuine product, including its declared digest. This
  ;; is an adversarial output, never a substitute for source construction.
  (let [core (get-in artifact [:gravity-core-boundary :canonical-core-artifact])
        index (first (keep-indexed (fn [index node]
                                    (when (and (= :literal (:core-form node))
                                               (instance? Boolean (get-in node [:attributes :value]))) index)) (:nodes core)))
        _ (assert (some? index))
        nodes (update-in (:nodes core) [index :attributes :value] not)
        preimage (assoc (:identity-preimage core) :nodes nodes)
        id (b/reader-canonical-hash {:domain :gravity/sh07-declared-digest-v1
                                     :purpose :sh07-core-artifact-id :preimage preimage})]
    (-> artifact
        (assoc :artifact-id id)
        (assoc-in [:gravity-core-boundary :canonical-core-artifact]
                  (assoc core :artifact-id id :nodes nodes :identity-preimage preimage))
        (assoc-in [:gravity-core-boundary :authenticated-envelope :semantic-artifact-id] id)
        (assoc-in [:gravity-core-boundary :authenticated-envelope-descriptor :semantic-artifact-id] id))))

(deftest paired-original-two-slot-source-preserves-core-products
  (selected-source! "compiler-all-unique" "bootstrap/gravity/src/gravity/checked_core.gravity"
                    "(defn sh07-all-unique? " "(defn sh07-vector-subset? ")
  (let [g (pair! "compiler-all-unique" check-two-slot!)
        repeated (build! (str fixture-root "sh-07/compiler-all-unique.gravity"))]
    (is (= g repeated))
    (let [tampered (coherent-bool-substitution g)
          rejected (b/sh07-core-artifact-verification tampered)]
      (is (not= (:artifact-id g) (:artifact-id tampered)))
      (is (= :failed (:status rejected)))
      (is (true? (get-in rejected [:checks :semantic-artifact-id-current?])))
      (is (false? (get-in rejected [:checks :canonical-core-replays?])))
      (is (false? (get-in rejected [:checks :authoritative-products-replay?]))))))

(deftest paired-original-reader-quote-remains-inert
  (selected-source! "compiler-reader-quote" "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"
                    "(defn read-source-form-records [source-bytes]\n" "(def stage1-reader-table\n")
  (pair! "compiler-reader-quote"
    (fn [artifact]
      (let [core (get-in artifact [:gravity-core-boundary :canonical-core-artifact])
            quote-node (one (:nodes core) #(= :quote (:core-form %)))]
        (is (= {:status :stage1-source-owned :input 'source-bytes
                :emits [:forms :source-spans :reader-diagnostics]}
               (get-in quote-node [:attributes :quoted-value])))
        (is (= [] (:children quote-node) (get-in quote-node [:evaluation :order])))
        (is (= :no-evaluation (get-in quote-node [:evaluation :kind])))
        (is (empty? (:calls core)))))))

(deftest paired-malformed-source-is-rejected-at-the-owning-stage
  (doseq [[base expected-rule expected-reason]
          [["sh-07/rejected/if-missing-branch" "C6-CORE-SHAPE" nil]
           ["sh-07-b4/rejected/loop-recur-arity-mismatch" "C6-VERIFY" :recur-arity-mismatch]
           ["sh-07-b4/rejected/recur-not-tail" "C6-VERIFY" :recur-tail-position-required]]
          extension [".gravity" ".qst"]]
    (testing (str base extension)
      (let [relative (str fixture-root base extension) source (path relative)
            rejected (try (b/sh07-public-core-file-artifact source current-reader)
                          nil (catch clojure.lang.ExceptionInfo error (ex-data error)))]
        (is (map? rejected))
        (is (= expected-rule (:rule rejected)))
        (is (= :core-lowering (:stage rejected)))
        (is (= :error (:severity rejected)))
        (is (= source (get-in rejected [:source-span :source])))
        (when expected-reason (is (= expected-reason (get-in rejected [:facts :reason]))))
        (swap! observations conj {:source relative :rejected-rule (:rule rejected)
                                 :reason (get-in rejected [:facts :reason])})))))
