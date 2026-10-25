(ns gravity.self-hosting.sh07-current-local-callee-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

(defn- root []
  (loop [directory (.getParentFile (io/file (io/resource "gravity/self_hosting/sh07_current_local_callee_test.clj")))]
    (cond (nil? directory) (throw (ex-info "Repository absent" {:id "SH07-LOCAL-CALLEE-ROOT"}))
          (.isFile (io/file directory "deps.edn")) directory
          :else (recur (.getParentFile directory)))))
(defn- one [rows predicate]
  (let [matches (filterv predicate rows)]
    (when-not (= 1 (count matches)) (throw (ex-info "Unique source-bound row required" {:count (count matches)})))
    (first matches)))
(defn- original-helper [source function next-function]
  (let [start (.indexOf ^String source (str "(defn " function " "))
        end (.indexOf ^String source (str "(defn " next-function " ") start)]
    (when-not (and (<= 0 start) (< start end))
      (throw (ex-info "Original helper occurrence absent" {:id "SH07-LOCAL-CALLEE-SOURCE" :function function})))
    (str/trim (subs source start end))))

(deftest original-compiler-local-call-preserves-callee-arguments-and-scopes
  ;; C01/C03/C05/C06, L2/C6: actual G source-to-Core. The separate fixture is
  ;; exact original text, not full-owner analysis. Q is UTF-8 text parity only.
  (let [path #(str (io/file (root) %))
        owner (path "bootstrap/gravity/src/gravity/checked_core.gravity")
        reader (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
        fixture "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-vector-subset"
        source (path (str fixture ".gravity")) text (slurp source :encoding "UTF-8")
        original-text (slurp owner :encoding "UTF-8")
        helpers [(original-helper original-text 'sh07-vector-contains? 'sh07-all-unique?)
                 (original-helper original-text 'sh07-vector-subset? 'sh07-lowercase-hex?)]
        originals (binding [*read-eval* false] (mapv read-string helpers))]
    (is (str/ends-with? text (str (str/join "\n\n" helpers) "\n")))
    (is (= text (slurp (path (str fixture ".qst")) :encoding "UTF-8")))
    (is (< (alength (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8)) 2048))
    (println {:local-callee-phase :current-reader-c5-begin}) (flush)
    (let [resolved (b/compiler-c5-resolution-source-artifact source text
                     {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []} reader)
          _ (println {:local-callee-phase :gravity-core-begin})
          artifact (b/sh07-core-from-resolution-artifact resolved)
          boundary (:gravity-core-boundary artifact) core (:canonical-core-artifact boundary)
          request (:authenticated-core-request boundary)
          expanded (:sh05-macro-artifact resolved)
          c2 (get-in expanded [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact])
          functions (into {} (map (juxt :definition-name identity)) (:function-records core))
          caller (functions 'sh07-vector-subset?) callee (functions 'sh07-vector-contains?)
          nodes (into {} (map (juxt :node-id identity)) (:nodes core))
          forms (into {} (map (juxt :form-id identity)) (:forms request))
          bindings (into {} (map (juxt :binding-id identity)) (:binding-table request))
          call-name #(get-in nodes [(:operator-node-id %) :attributes :symbol])
          call (one (:calls core) #(= 'sh07-vector-contains? (call-name %)))
          edge (one (:call-edges core) #(= (:core-node-id call) (:call-core-node-id %)))
          operator (nodes (:operator-node-id call))
          [candidate-argument first-argument] (mapv nodes (:argument-node-ids call))
          first-call (one (:calls core) #(= (:node-id first-argument) (:core-node-id %)))
          first-input (nodes (first (:argument-node-ids first-call)))
          caller-slot (one (:loop-bindings core) #(= (:body-core-node-id caller) (:loop-core-node-id %)))
          callee-slot (one (:loop-bindings core) #(= (:body-core-node-id callee) (:loop-core-node-id %)))
          caller-id (:definition-binding-id caller) callee-id (:definition-binding-id callee)]
      (is (= :accepted (:status resolved) (:status artifact)))
      (is (= :gravity.checked-core (get-in artifact [:pass :owner])))
      (is (= :passed (get-in boundary [:template-verification :status]) (get-in boundary [:resolved-verification :status])))
      (is (= originals (vec (rest (:parsed-semantic-values c2)))))
      (is (= (mapv (fn [form] (list 'def (second form) (cons 'fn (drop 2 form)))) originals)
             (:expanded-forms expanded)))
      (is (= source (get-in c2 [:source-unit-record :path]) (get-in core [:provenance :actual-source-path])))
      (is (not= owner source))
      (is (= (str "sha256:" (b/sha256-hex text)) (get-in c2 [:source-unit-record :bytes-hash])))
      (is (= (str "sha256:" (b/sha256-hex (slurp reader :encoding "UTF-8")))
             (get-in c2 [:sh03-reader-authentication :reader-source-revision :source-content-hash])))
      (is (= '#{sh07-vector-contains? sh07-vector-subset?} (set (keys functions))))
      (is (= (count (:calls core)) (count (:call-edges core))))
      (is (= (mapv :core-node-id (:calls core)) (mapv :call-core-node-id (:call-edges core))))
      (is (= 1 (count (filter #(= :local-function (:classification %)) (:call-edges core)))))
      (doseq [other (remove #(= :local-function (:classification %)) (:call-edges core))]
        (is (= :core (:binding-class (bindings (:callee-binding-id other))))))
      (is (= [] (:recursion-components core)))
      (doseq [function [caller callee]]
        (is (= 2 (:fixed-arity function) (count (:parameter-binding-ids function))))
        (is (= :private (get-in bindings [(:definition-binding-id function) :visibility]))))
      (is (not= caller-id callee-id))
      (is (= [:namespace :function 'sh07-vector-contains?]
             ((juxt :binding-class :kind :name) (bindings callee-id))))
      (is (= [:reference :operator 'sh07-vector-contains?]
             [(:core-form operator) (get-in operator [:attributes :position]) (get-in operator [:attributes :symbol])]))
      (is (= [callee-id] (:resolved-binding-ids operator)))
      (is (= callee-id (:operator-binding-id call) (:callee-binding-id edge)))
      (is (= :local-function (:classification edge)))
      (is (= (:function-core-node-id caller) (:caller-function-core-node-id edge)))
      (is (= (:function-syntax-id caller) (:caller-function-syntax-id edge)))
      (is (= (:function-core-node-id callee) (:callee-function-core-node-id edge)))
      (is (= (:function-syntax-id callee) (:callee-function-syntax-id edge)))
      (is (= (:definition-syntax-id (bindings callee-id)) (:callee-definition-syntax-id edge)))
      (is (= 2 (count (:argument-node-ids call))))
      (is (= (:argument-node-ids call) (:argument-core-node-ids edge)))
      (is (= (into [(:operator-node-id call)] (:argument-node-ids call))
             (:ordered-evaluation-node-ids call) (:ordered-evaluation-node-ids edge)
             (:children (nodes (:core-node-id call)))))
      (is (= :operator-then-arguments (:evaluation-order call) (:evaluation-order edge)))
      (is (= 'candidates (get-in candidate-argument [:attributes :symbol])))
      (is (= (second (:parameter-binding-ids caller)) (get-in candidate-argument [:attributes :binding-id])))
      (is (= 'first (call-name first-call)))
      (is (= 'remaining (get-in first-input [:attributes :symbol])))
      (is (= 'remaining (:name caller-slot) (:name callee-slot)))
      (is (not= (:binding-id caller-slot) (:binding-id callee-slot)))
      (is (= (:binding-id caller-slot) (get-in first-input [:attributes :binding-id])))
      (is (not= (:binding-id callee-slot) (get-in first-input [:attributes :binding-id])))
      (doseq [node (:nodes core)]
        (let [form (forms (get-in node [:source :form-id]))]
          (is (= (select-keys form [:syntax-id :form-id :origin-chain :generated-origin])
                 (select-keys (:source node) [:syntax-id :form-id :origin-chain :generated-origin])))))
      (let [template (get-in boundary [:raw-template-result :core-template])
            original-call (one (:calls template) #(= callee-id (:operator-binding-id %)))
            target (one (:function-records template) #(= 'sh07-vector-subset? (:definition-name %)))
            call-id (:core-node-id original-call) operator-id (:operator-node-id original-call)
            changed (-> template
                        (update :calls #(mapv (fn [row] (if (= call-id (:core-node-id row)) (assoc row :operator-binding-id caller-id) row)) %))
                        (update :call-edges #(mapv (fn [row] (if (= call-id (:call-core-node-id row))
                          (merge row {:callee-binding-id caller-id
                                      :callee-definition-syntax-id (:definition-syntax-id (bindings caller-id))
                                      :callee-function-syntax-id (:function-syntax-id target)
                                      :callee-function-core-node-id (:function-core-node-id target)}) row)) %))
                        (update :nodes #(mapv (fn [node]
                          (cond (= operator-id (:node-id node))
                                (-> node (assoc :resolved-binding-ids [caller-id])
                                    (assoc-in [:attributes :binding-id] caller-id)
                                    (assoc-in [:attributes :symbol] 'sh07-vector-subset?))
                                (= call-id (:node-id node)) (assoc node :resolved-binding-ids [caller-id])
                                :else node)) %)))
            changed (update changed :identity-preimage merge (select-keys changed [:nodes :calls :call-edges]))
            changed-call (one (:calls changed) #(= call-id (:core-node-id %)))
            changed-edge (one (:call-edges changed) #(= call-id (:call-core-node-id %)))
            _ (println {:local-callee-phase :fresh-substituted-callee-replay-begin})
            rejected (b/sh07-core-execute! source 'sh07-verify-core-template [request changed (:digest-requests boundary)])]
        ;; Coordinated binding/node/call/edge substitution, not a claim of full
        ;; digest resealing or a rebuilt self-recursion component certificate.
        (is (= caller-id (:operator-binding-id changed-call) (:callee-binding-id changed-edge)))
        (is (= [caller-id] (:resolved-binding-ids (one (:nodes changed) #(= operator-id (:node-id %))))))
        (is (= (select-keys changed [:nodes :calls :call-edges])
               (select-keys (:identity-preimage changed) [:nodes :calls :call-edges])))
        (is (= [:rejected "C6-VERIFY" :template-replay] ((juxt :status :rule :reason) rejected))))
      (is (= {:C7 :pending :C8 :pending :C9 :pending :C10 :pending}
             (get-in artifact [:execution-boundary :downstream-fact-statuses])))
      (is (= text (slurp source :encoding "UTF-8")))
      (println {:local-callee-phase :complete}) (flush))))
