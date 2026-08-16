(ns gravity.self-hosting.sh12-c10-mir-adapter-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh11-c9-safety-adapter-test]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh12_c10_mir_adapter_test.clj")]
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "repository root not found"
                        {:id "SH12-C10-MIR-ADAPTER-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- compile-plan
  [relative-path]
  (let [source-path (str (.resolve @root relative-path))
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c11-plan
  (delay
    (compile-plan
     "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity")))

(defn- invoke-c11
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh12-c10-mir-adapter-test
    :compiler-artifact-plan? true}
   @c11-plan function arguments))

(defn- sh11-value
  [name]
  (var-get
   (ns-resolve
    'gravity.self-hosting.sh11-c9-safety-adapter-test name)))

(defn- sh09-value
  [name]
  (var-get
   (ns-resolve
    'gravity.self-hosting.sh09-c7-effect-adapter-test name)))

(defn- digest
  [ordinal]
  (format "sha256:%064x" (long ordinal)))

(defn- c11-source-exports
  []
  (let [source-path
        (str (.resolve @root
                       "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity"))
        source-text (slurp source-path)
        reader (java.io.PushbackReader.
                (java.io.StringReader. source-text))
        ns-form (read {:eof nil} reader)
        exports-clause
        (some (fn [clause]
                (when (and (seq? clause)
                           (= :exports (first clause)))
                  clause))
              (drop 2 ns-form))]
    (set (second exports-clause))))

(defn- path-bound
  [prepared actual-path]
  (let [provenance {:actual-source-path actual-path}
        bound (:safety-bound prepared)
        bound
        (-> bound
            (assoc :provenance provenance)
            (assoc-in [:ownership-core :provenance] provenance)
            (assoc-in [:ownership-core :ownership-core :provenance]
                      provenance)
            (assoc-in [:ownership-core :ownership-core :effected-core
                       :provenance]
                      provenance)
            (assoc-in [:ownership-core :ownership-core :effected-core
                       :effected-core :provenance]
                      provenance))
        verification
        (-> (:safety-verification prepared)
            (assoc :expected bound)
            (assoc :candidate bound))]
    {:bound bound
     :verification verification}))

(defn- build-c11-case
  [{:keys [bound verification]}]
  (let [template
        (invoke-c11
         'sh12-build-authenticated-c10-mir-template
         [bound verification])
        operation-request
        (invoke-c11
         'sh12-authenticated-c10-mir-identity-request
         [bound verification template])
        operation-resolution
        {:request (:request operation-request)
         :digest (digest 1201)}
        module-request
        (invoke-c11
         'sh12-authenticated-c10-mir-module-identity-request
         [bound verification template operation-resolution])
        resolved
        {:operation-resolution operation-resolution
         :module-resolution
         {:request (:request module-request)
          :digest (digest 1202)}}
        candidate
        (invoke-c11
         'sh12-bind-authenticated-c10-mir
         [bound verification template resolved])
        result
        (invoke-c11
         'sh12-verify-authenticated-c10-mir
         [bound verification template resolved candidate])]
    {:bound bound
     :verification verification
     :template template
     :operation-request operation-request
     :operation-resolution operation-resolution
     :module-request module-request
     :resolved resolved
     :candidate candidate
     :result result}))

(def ^:private prepared-c10
  (delay
    ((sh11-value 'prepared-c10))))

(def ^:private prepared-c11
  (delay
    (build-c11-case
     (path-bound @prepared-c10 "/checkout-a/function.gravity"))))

(deftest sh12-c10-proof-reference-api-and-positive
  (let [functions (:functions @c11-plan)
        exported-names
        #{'sh12-authenticated-c10-mir-adapter-policy
          'sh12-authenticated-c10-mir-carrier-preflight
          'sh12-authenticated-c10-mir-input-valid?
          'sh12-build-authenticated-c10-mir-template
          'sh12-authenticated-c10-mir-identity-request
          'sh12-authenticated-c10-mir-module-identity-request
          'sh12-bind-authenticated-c10-mir
          'sh12-verify-authenticated-c10-mir}
        exported
        {'sh12-authenticated-c10-mir-adapter-policy 0
         'sh12-authenticated-c10-mir-carrier-preflight 1
         'sh12-authenticated-c10-mir-input-valid? 2
         'sh12-build-authenticated-c10-mir-template 2
         'sh12-authenticated-c10-mir-identity-request 3
         'sh12-authenticated-c10-mir-module-identity-request 4
         'sh12-bind-authenticated-c10-mir 4
         'sh12-verify-authenticated-c10-mir 5}
        policy (invoke-c11
                'sh12-authenticated-c10-mir-adapter-policy [])]
    (doseq [[name arity] exported]
      (is (= arity (get-in functions [name :arity])) name))
    (is (every? (set (c11-source-exports)) exported-names))
    (is (= :authenticated-persistent-read-safety-evidence-only
           (:scope policy)))
    (is (= :gravity.type/bool (:accepted-value-type policy)))
    (is (= :proof-reference (:accepted-operation policy)))
    (is (false? (:executable-load? policy)))
    (is (true? (:target-independent? policy)))
    (is (= :blocked (:b1-preflight policy)))
    (is (some #{:trusted-digest-resolution} (:pending policy)))
    (is (some #{:generic-c11-verifier} (:nonclaims policy)))
    (let [{:keys [bound verification template operation-request
                  operation-resolution module-request resolved candidate result]}
          @prepared-c11
          operation-collision-resolution
          {:request (:request operation-request)
           :digest (:proof-id bound)}
          operation-collision-request
          (invoke-c11
           'sh12-authenticated-c10-mir-module-identity-request
           [bound verification template operation-collision-resolution])
          digest-collision-resolved
          (assoc-in resolved [:module-resolution :digest]
                    (:digest operation-resolution))
          operation (:operation candidate)
          operation-id (:op-id operation)
          fn-id (first (keys (:functions candidate)))
          function (get (:functions candidate) fn-id)
          block-id (:entry function)
          block (get (:blocks function) block-id)
          token-id (:result operation)
          token (get (:values candidate) token-id)
          terminator (:terminator block)
          table-ids (select-keys (:facts operation)
                                 [:type-fact-id :effect-fact-id
                                  :capability-proof-id :ownership-fact-id])
          ownership-entry
          (get (:ownership-table candidate)
               (:ownership-fact-id table-ids))
          capability-proof-entry
          (get (:capability-proof-table candidate)
               (:capability-proof-id table-ids))
          safety-entry
          (get (:safety-table candidate)
               (:safety-outcome-id (:facts operation)))
          proof-entry
          (get (:proof-certificate-table candidate)
               (:safety-proof-id (:facts operation)))
          ownership-result
          (first (get-in bound [:ownership-core :ownership-core
                                :ownership-results]))
          ownership-fact (first (:ownership-facts ownership-result))
          fact-identity (first (get-in bound [:ownership-core
                                              :fact-identities]))
          safety-result (:safety-result bound)
          safety-verification (:safety-verification bound)
          safety-proof (first (:proofs safety-result))
          safety-outcome (first (:outcomes safety-result))
          preflight-values [bound verification template resolved candidate]]
      (is (= :accepted (:status template)))
      (is (= :accepted (:status operation-request)))
      (is (= :accepted (:status module-request)))
      (is (= :accepted (:status candidate)))
      (is (= :passed (:status result)))
      (is (= :rejected (:status operation-collision-request)))
      (is (= :upstream-digest-reused-for-mir-operation
             (get-in operation-collision-request [:diagnostics 0 :reason])))
      (let [collision
            (invoke-c11
             'sh12-bind-authenticated-c10-mir
             [bound verification template digest-collision-resolved])]
        (is (= :rejected (:status collision)))
        (is (= :identity-resolution-substitution
               (get-in collision [:diagnostics 0 :reason]))))
      (let [tampered
            (assoc-in candidate [:operation :opcode] :load)
            tampered-verification
            (invoke-c11
             'sh12-verify-authenticated-c10-mir
             [bound verification template resolved tampered])]
        (is (= :rejected (:status tampered-verification))))
      (doseq [value preflight-values]
        (let [receipt
              (invoke-c11
               'sh12-authenticated-c10-mir-carrier-preflight [value])]
          (is (= :accepted (:status receipt)))
          (is (< (:nodes receipt) 32768))))
      (is (= (:digest operation-resolution)
             (:operation-identity-id candidate)))
      (is (= (get-in resolved [:module-resolution :digest])
             (:module-id candidate)))
      (is (= resolved (:identity-resolutions candidate)))
      (is (= (:provenance bound) (:provenance candidate)))
      (is (= (:operation-identity-input template)
             (:operation-identity-input candidate)))
      (is (= (:upstream template) (:upstream candidate)))
      (is (= 1 (count (:functions candidate))))
      (is (= fn-id (:fn-id function)))
      (is (= 1 (count (:blocks function))))
      (is (= block-id (:block-id block)))
      (is (= 1 (count (:instructions block))))
      (is (= operation (first (:instructions block))))
      (is (= :gravity/mir-operation (:artifact operation)))
      (is (= :proof-reference (:opcode operation)))
      (is (= :load (:source-operation operation)))
      (is (= :gravity/proof-token (:type operation)))
      (is (= token-id (:result operation)))
      (is (= :proof-token (:kind token)))
      (is (= token-id (:value-id token)))
      (is (= [token-id] (:operands terminator)))
      (is (= :return (:kind terminator)))
      (is (= #{fn-id block-id operation-id token-id
               (:terminator-id terminator)}
             (set (keys (:source-map candidate)))))
      (is (= (set (keys (:type-table candidate)))
             #{(:type-fact-id table-ids)}))
      (is (= (set (keys (:effect-table candidate)))
             #{(:effect-fact-id table-ids)}))
      (is (= (set (keys (:ownership-table candidate)))
             #{(:ownership-fact-id table-ids)}))
      (is (= (set (keys (:capability-table candidate)))
             #{(:capability-proof-id table-ids)}))
      (is (= (set (keys (:safety-table candidate)))
             #{(:safety-outcome-id (:facts operation))}))
      (is (= (set (keys (:proof-certificate-table candidate)))
             #{(:safety-proof-id (:facts operation))}))
      (is (= #{:call-type-fact :fact-identity}
             (set (keys (get (:type-table candidate)
                            (:type-fact-id table-ids))))))
      (is (= (:effect-fact-id table-ids)
             (get-in candidate
                     [:capability-table (:capability-proof-id table-ids)
                      :effect-fact-id])))
      (is (nil? (get-in candidate
                        [:capability-table (:capability-proof-id table-ids)
                         :capability-proof])))
      (is (= {:proof-id (:capability-proof-id table-ids)
              :source-stage :c8
              :source-kind :authenticated-upstream-reference
              :operation-facts (get-in bound [:operation :facts])}
             capability-proof-entry))
      (is (= {:fact-id (:ownership-fact-id table-ids)
              :fact ownership-fact
              :fact-identity fact-identity
              :ownership-result ownership-result
              :ownership-fact-request (:fact-id-request fact-identity)
              :ownership-core-identity-id
              (:c9-ownership-core-id (:facts operation))
              :source-stage :c9
              :source-kind :authenticated-record}
             ownership-entry))
      (is (= {:outcome-id (:result-id bound)
              :result safety-result
              :outcome-record safety-outcome
              :proof-id (:proof-id bound)
              :proof safety-proof
              :verification safety-verification
              :source-stage :c10
              :source-kind :authenticated-record}
             safety-entry))
      (is (= {:proof-id (:proof-id bound)
              :proof safety-proof
              :proof-id-request (:proof-id-request safety-proof)
              :result-id (:result-id bound)
              :safety-core-id (:safety-core-id bound)
              :source-stage :c10
              :source-kind :authenticated-record}
             proof-entry))
      (is (not (contains? (:upstream candidate) :c10-safety-result)))
      (is (not (contains? (:upstream candidate) :c9-ownership-core))))))

(deftest sh12-c10-path-neutral-provenance-pair
  (let [gravity @prepared-c11
        qst
        (build-c11-case
         (path-bound @prepared-c10 "/checkout-b/function.qst"))
        gravity-operation (:operation-request gravity)
        qst-operation (:operation-request qst)
        gravity-module (:module-request gravity)
        qst-module (:module-request qst)
        gravity-candidate (:candidate gravity)
        qst-candidate (:candidate qst)]
    (is (= :accepted (:status gravity-operation)))
    (is (= :accepted (:status qst-operation)))
    (is (= gravity-operation qst-operation))
    (is (= gravity-module qst-module))
    (is (= :accepted (:status gravity-candidate)))
    (is (= :accepted (:status qst-candidate)))
    (is (= (:operation-identity-id gravity-candidate)
           (:operation-identity-id qst-candidate)))
    (is (= (:module-id gravity-candidate)
           (:module-id qst-candidate)))
    (is (= "/checkout-a/function.gravity"
           (get-in gravity-candidate [:provenance :actual-source-path])))
    (is (= "/checkout-b/function.qst"
           (get-in qst-candidate [:provenance :actual-source-path])))
    (is (not= (:provenance gravity-candidate)
              (:provenance qst-candidate)))
    (is (= (:operation-identity-input (:template gravity))
           (:operation-identity-input (:template qst))))
    (is (= (:upstream (:template gravity))
           (:upstream (:template qst))))))

(deftest sh12-c10-representative-mutation-rejections
  (let [{:keys [bound verification template resolved candidate]}
        @prepared-c11
        bad-bound
        (update bound :pending conj :test-c10-pending)
        bad-bound-result
        (invoke-c11
         'sh12-authenticated-c10-mir-identity-request
         [bad-bound verification template])
        bad-template
        (assoc-in template [:operation :source :source-span :start-byte] 999)
        bad-template-result
        (invoke-c11
         'sh12-authenticated-c10-mir-identity-request
         [bound verification bad-template])
        bad-candidate
        (assoc-in candidate [:operation :opcode] :load)
        bad-candidate-result
        (invoke-c11
         'sh12-verify-authenticated-c10-mir
         [bound verification template resolved bad-candidate])]
    (is (= :rejected (:status bad-bound-result)))
    (is (= :untrusted-or-malformed-c10-input
           (get-in bad-bound-result [:diagnostics 0 :reason])))
    (is (= :rejected (:status bad-template-result)))
    (is (= :template-recomputation-mismatch
           (get-in bad-template-result [:diagnostics 0 :reason])))
    (is (= :rejected (:status bad-candidate-result)))
    (is (= candidate (:expected bad-candidate-result)))
    (is (= bad-candidate (:candidate bad-candidate-result)))))

(defn- nested-carrier
  [depth]
  (loop [remaining depth
         value :leaf]
    (if (= 0 remaining)
      value
      (recur (dec remaining) {:child value}))))

(deftest sh12-c10-carrier-preflight-boundaries
  (let [map-512 (zipmap (range 512) (repeat 512 :leaf))
        map-513 (zipmap (range 513) (repeat 513 :leaf))
        vector-1024 (vec (repeat 1024 :leaf))
        vector-1025 (vec (repeat 1025 :leaf))
        set-512 (set (range 512))
        set-513 (set (range 513))
        lazy-seq (map identity [:leaf])
        cases
        [[map-512 :accepted nil]
         [map-513 :rejected :c10-mir-carrier-width-bound]
         [vector-1024 :accepted nil]
         [vector-1025 :rejected :c10-mir-carrier-width-bound]
         [set-512 :accepted nil]
         [set-513 :rejected :c10-mir-carrier-width-bound]
         [lazy-seq :rejected :c10-mir-carrier-seq-unsupported]
         [(nested-carrier 256) :accepted nil]
         [(nested-carrier 257) :rejected :c10-mir-carrier-depth-bound]]]
    (doseq [[carrier expected reason] cases]
      (let [receipt
            (invoke-c11
             'sh12-authenticated-c10-mir-carrier-preflight [carrier])]
        (is (= expected (:status receipt)) (str "carrier=" (type carrier)))
        (when reason
          (is (= reason (:reason receipt))
              (str "carrier=" (type carrier))))))))

(deftest sh12-c10-verification-envelope-preflight
  (let [prepared @prepared-c10
        bound (:safety-bound prepared)
        verification (:safety-verification prepared)
        map-512 (zipmap (range 512) (repeat 512 :leaf))
        map-513 (zipmap (range 513) (repeat 513 :leaf))
        vector-31x1024
        (vec (repeat 31 (vec (repeat 1024 :leaf))))
        vector-32x1024
        (vec (repeat 32 (vec (repeat 1024 :leaf))))
        near-bound-verification
        (assoc verification
               :expected vector-31x1024
               :candidate vector-31x1024)
        near-bound-receipt
        (invoke-c11
         'sh12-c10-mir-verification-envelope-preflight
         [near-bound-verification])
        expected-over-bound
        (assoc verification :expected map-513)
        expected-over-bound-receipt
        (invoke-c11
         'sh12-c10-mir-verification-envelope-preflight
         [expected-over-bound])
        candidate-over-bound
        (assoc verification :candidate map-513)
        candidate-over-bound-receipt
        (invoke-c11
         'sh12-c10-mir-verification-envelope-preflight
         [candidate-over-bound])
        expected-node-bound
        (assoc verification :expected vector-32x1024)
        expected-node-bound-receipt
        (invoke-c11
         'sh12-c10-mir-verification-envelope-preflight
         [expected-node-bound])
        candidate-node-bound
        (assoc verification :candidate vector-32x1024)
        candidate-node-bound-receipt
        (invoke-c11
         'sh12-c10-mir-verification-envelope-preflight
         [candidate-node-bound])
        metadata-over-bound
        (assoc verification :checks map-513)
        metadata-over-bound-receipt
        (invoke-c11
         'sh12-c10-mir-verification-envelope-preflight
         [metadata-over-bound])
        metadata-malformed
        (assoc verification :checks (map identity [:malformed]))
        metadata-malformed-receipt
        (invoke-c11
         'sh12-c10-mir-verification-envelope-preflight
         [metadata-malformed])
        metadata-poison
        (assoc verification
               :checks
               (lazy-seq
                (throw (ex-info "metadata sequence was realized"
                                {:id :sh12-c10-metadata-poison}))))
        metadata-poison-receipt
        (try
          (invoke-c11
           'sh12-c10-mir-verification-envelope-preflight
           [metadata-poison])
          (catch Throwable error
            {:threw? true :error error}))
        metadata-artifact-wrong
        (assoc verification :artifact :wrong-artifact)
        metadata-status-wrong
        (assoc verification :status :rejected)
        metadata-checks-wrong
        (assoc verification :checks [:wrong-check])
        metadata-nonclaims-wrong
        (assoc verification :nonclaims [:wrong-nonclaim])
        metadata-artifact-wrong-receipt
        (invoke-c11
         'sh12-c10-mir-verification-envelope-preflight
         [metadata-artifact-wrong])
        metadata-status-wrong-receipt
        (invoke-c11
         'sh12-c10-mir-verification-envelope-preflight
         [metadata-status-wrong])
        metadata-checks-wrong-receipt
        (invoke-c11
         'sh12-c10-mir-verification-envelope-preflight
         [metadata-checks-wrong])
        metadata-nonclaims-wrong-receipt
        (invoke-c11
         'sh12-c10-mir-verification-envelope-preflight
         [metadata-nonclaims-wrong])
        missing-key (dissoc verification :candidate)
        extra-key (assoc verification :unexpected :extra)
        missing-key-receipt
        (invoke-c11
         'sh12-c10-mir-verification-envelope-preflight [missing-key])
        extra-key-receipt
        (invoke-c11
         'sh12-c10-mir-verification-envelope-preflight [extra-key])
        input-result
        (invoke-c11
         'sh12-authenticated-c10-mir-input-valid?
         [bound expected-over-bound])
        bind-result
        (invoke-c11
         'sh12-bind-authenticated-c10-mir
         [bound candidate-over-bound {} {}])
        verify-result
        (invoke-c11
         'sh12-verify-authenticated-c10-mir
         [bound expected-over-bound {} {} {}])
        source-path
        (.resolve @root
                  "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity")
        source-text (java.nio.file.Files/readString source-path)
        helper-occurrences
        (count (re-seq
                #"sh12-c10-mir-verification-envelope-preflight"
                source-text))]
    (is (= :accepted (:status near-bound-receipt)))
    (is (= :accepted
           (get-in near-bound-receipt [:expected-preflight :status])))
    (is (= :accepted
           (get-in near-bound-receipt [:candidate-preflight :status])))
    (is (< (get-in near-bound-receipt [:expected-preflight :nodes])
           32768))
    (is (< (get-in near-bound-receipt [:candidate-preflight :nodes])
           32768))
    (is (= :rejected (:status expected-over-bound-receipt)))
    (is (= :c10-mir-verification-expected-bound
           (:reason expected-over-bound-receipt)))
    (is (= :rejected (:status candidate-over-bound-receipt)))
    (is (= :c10-mir-verification-candidate-bound
           (:reason candidate-over-bound-receipt)))
    (is (= :rejected (:status expected-node-bound-receipt)))
    (is (= :c10-mir-verification-expected-bound
           (:reason expected-node-bound-receipt)))
    (is (= :c10-mir-carrier-node-bound
           (get-in expected-node-bound-receipt
                   [:expected-preflight :reason])))
    (is (= false
           (get-in expected-node-bound-receipt
                   [:expected-preflight :nodes-exact?])))
    (is (integer?
         (get-in expected-node-bound-receipt
                [:expected-preflight :observed-nodes])))
    (is (integer?
         (get-in expected-node-bound-receipt
                [:expected-preflight :enqueued])))
    (is (= :rejected (:status candidate-node-bound-receipt)))
    (is (= :c10-mir-verification-candidate-bound
           (:reason candidate-node-bound-receipt)))
    (is (= :c10-mir-carrier-node-bound
           (get-in candidate-node-bound-receipt
                   [:candidate-preflight :reason])))
    (is (= false
           (get-in candidate-node-bound-receipt
                   [:candidate-preflight :nodes-exact?])))
    (is (integer?
         (get-in candidate-node-bound-receipt
                [:candidate-preflight :observed-nodes])))
    (is (integer?
         (get-in candidate-node-bound-receipt
                [:candidate-preflight :enqueued])))
    (is (= :rejected (:status metadata-over-bound-receipt)))
    (is (= :c10-mir-verification-metadata-bound
           (:reason metadata-over-bound-receipt)))
    (is (= :c10-mir-carrier-width-bound
           (get-in metadata-over-bound-receipt
                   [:metadata-preflight :reason])))
    (is (= :rejected (:status metadata-malformed-receipt)))
    (is (= :c10-mir-verification-metadata-bound
           (:reason metadata-malformed-receipt)))
    (is (= :c10-mir-carrier-seq-unsupported
           (get-in metadata-malformed-receipt
                   [:metadata-preflight :reason])))
    (is (not (:threw? metadata-poison-receipt)))
    (is (= :rejected (:status metadata-poison-receipt)))
    (is (= :c10-mir-verification-metadata-bound
           (:reason metadata-poison-receipt)))
    (is (= :c10-mir-carrier-seq-unsupported
           (get-in metadata-poison-receipt
                   [:metadata-preflight :reason])))
    (doseq [receipt [metadata-artifact-wrong-receipt
                     metadata-status-wrong-receipt
                     metadata-checks-wrong-receipt
                     metadata-nonclaims-wrong-receipt]]
      (is (= :rejected (:status receipt)))
      (is (= :c10-mir-verification-metadata-values
             (:reason receipt))))
    (is (= :rejected (:status missing-key-receipt)))
    (is (= :c10-mir-verification-envelope-shape
           (:reason missing-key-receipt)))
    (is (= :rejected (:status extra-key-receipt)))
    (is (= :c10-mir-verification-envelope-shape
           (:reason extra-key-receipt)))
    (is (false? input-result))
    (is (= :rejected (:status bind-result)))
    (is (= :verification-carrier-bound
           (get-in bind-result [:diagnostics 0 :reason])))
    (is (= :c10-mir-verification-candidate-bound
           (get-in bind-result
                  [:diagnostics 0 :preflight :reason])))
    (is (= :c10-mir-carrier-width-bound
           (get-in bind-result
                  [:diagnostics 0 :preflight :candidate-preflight
                   :reason])))
    (is (= :rejected (:status verify-result)))
    (is (= [:verification-carrier-bound]
           (:checks verify-result)))
    (is (= :c10-mir-verification-expected-bound
           (get-in verify-result [:verification-preflight :reason])))
    (is (= :c10-mir-carrier-width-bound
           (get-in verify-result
                  [:verification-preflight :expected-preflight :reason])))
    (is (= 4 helper-occurrences))))

(deftest sh12-c10-authenticated-gravity-boundary
  ;; Explicit five-var candidate lane; the real .gravity boundary is last:
  ;; clojure -J-Xmx8g -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh07-iteration-cache-runner --fail-fast --test-var gravity.self-hosting.sh12-c10-mir-adapter-test/sh12-c10-proof-reference-api-and-positive --test-var gravity.self-hosting.sh12-c10-mir-adapter-test/sh12-c10-path-neutral-provenance-pair --test-var gravity.self-hosting.sh12-c10-mir-adapter-test/sh12-c10-representative-mutation-rejections --test-var gravity.self-hosting.sh12-c10-mir-adapter-test/sh12-c10-carrier-preflight-boundaries --test-var gravity.self-hosting.sh12-c10-mir-adapter-test/sh12-c10-authenticated-gravity-boundary --max-cache-entries 1
  ;; This is an explicit selector order, not a promise about namespace order.
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (require 'gravity.self-hosting.sh08-primitive-function-type-test)
  (let [fixture-namespace
        'gravity.self-hosting.sh08-function-call-type-test
        primitive-namespace
        'gravity.self-hosting.sh08-primitive-function-type-test
        fixture-artifact
        (var-get (ns-resolve fixture-namespace 'fixture-artifact))
        function-request
        (var-get (ns-resolve fixture-namespace 'function-request))
        invoke-c7
        (var-get (ns-resolve primitive-namespace 'invoke-c7))
        invoke-c8 (sh09-value 'invoke-c8)
        invoke-c9 (sh11-value 'invoke-c9)
        invoke-c10 (sh11-value 'invoke-c10)
        resolve-real-requests (sh11-value 'resolve-real-requests)
        fixture-base
        (.resolve @root
                  "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted")
        gravity-path (.resolve fixture-base "function-single-bool-call.gravity")
        qst-path (.resolve fixture-base "function-single-bool-call.qst")
        gravity-bytes (java.nio.file.Files/readAllBytes gravity-path)
        qst-bytes (java.nio.file.Files/readAllBytes qst-path)
        artifact
        (fixture-artifact
         "accepted" "function-single-bool-call" ".gravity")
        request (function-request artifact)
        typed
        (invoke-c7 'sh08-function-type-core-artifact [request])
        typed-verification
        (invoke-c7 'sh08-verify-function-type-result [request typed])
        effected
        (invoke-c8
         'sh09-build-authenticated-pure-effect-result
         [typed typed-verification])
        effect-verification
        (invoke-c8
         'sh09-verify-authenticated-pure-effect-result
         [typed typed-verification effected])
        effect-template
        (invoke-c8
         'sh09-authenticated-effect-identity-requests
         [typed typed-verification effected])
        effect-resolutions (resolve-real-requests (:requests effect-template))
        effect-bound
        (invoke-c8
         'sh09-bind-authenticated-effect-identities
         [typed typed-verification effected effect-resolutions])
        effect-binding-verification
        (invoke-c8
         'sh09-verify-authenticated-effect-identities
         [typed typed-verification effected effect-resolutions effect-bound])
        owned
        (invoke-c9
         'sh10-build-authenticated-ownership-core
         [effect-bound effect-binding-verification])
        ownership-verification
        (invoke-c9
         'sh10-verify-authenticated-ownership-core
         [effect-bound effect-binding-verification owned])
        ownership-fact-template
        (invoke-c9
         'sh10-authenticated-ownership-identity-requests
         [effect-bound effect-binding-verification owned])
        ownership-fact-resolutions
        (resolve-real-requests (:fact-requests ownership-fact-template))
        ownership-core-template
        (invoke-c9
         'sh10-authenticated-ownership-core-identity-request
         [effect-bound effect-binding-verification owned
          ownership-fact-resolutions])
        ownership-resolved
        {:fact-resolutions ownership-fact-resolutions
         :core-resolution
         {:request (:core-request ownership-core-template)
          :digest
          (str "sha256:"
               (bootstrap/sha256-hex
                (pr-str (:core-request ownership-core-template))))}}
        ownership-bound
        (invoke-c9
         'sh10-bind-authenticated-ownership-identities
         [effect-bound effect-binding-verification owned ownership-resolved])
        ownership-binding-verification
        (invoke-c9
         'sh10-verify-authenticated-ownership-identities
         [effect-bound effect-binding-verification owned ownership-resolved
          ownership-bound])
        safety
        (invoke-c10
         'sh11-build-authenticated-safety-core
         [ownership-bound ownership-binding-verification])
        proof-template
        (invoke-c10
         'sh11-authenticated-safety-identity-requests
         [ownership-bound ownership-binding-verification safety])
        proof-resolution
        {:request (:proof-request proof-template)
         :digest
         (str "sha256:"
              (bootstrap/sha256-hex (pr-str (:proof-request proof-template))))}
        result-template
        (invoke-c10
         'sh11-authenticated-safety-result-identity-request
         [ownership-bound ownership-binding-verification safety
          proof-resolution])
        result-resolution
        {:request (:result-request result-template)
         :digest
         (str "sha256:"
              (bootstrap/sha256-hex (pr-str (:result-request result-template))))}
        safety-core-template
        (invoke-c10
         'sh11-authenticated-safety-core-identity-request
         [ownership-bound ownership-binding-verification safety
          proof-resolution result-resolution])
        safety-resolved
        {:proof-resolution proof-resolution
         :result-resolution result-resolution
         :core-resolution
         {:request (:core-request safety-core-template)
          :digest
          (str "sha256:"
               (bootstrap/sha256-hex
                (pr-str (:core-request safety-core-template))))}}
        safety-bound
        (invoke-c10
         'sh11-bind-authenticated-safety-identities
         [ownership-bound ownership-binding-verification safety safety-resolved])
        safety-verification
        (invoke-c10
         'sh11-verify-authenticated-safety-identities
         [ownership-bound ownership-binding-verification safety safety-resolved
          safety-bound])
        bound-preflight
        (invoke-c11
         'sh12-authenticated-c10-mir-carrier-preflight
         [safety-bound])
        legacy-whole-envelope-preflight
        (invoke-c11
         'sh12-authenticated-c10-mir-carrier-preflight
         [safety-verification])
        verification-preflight
        (invoke-c11
         'sh12-c10-mir-verification-envelope-preflight
         [safety-verification])
        verification-expected-preflight
        (invoke-c11
         'sh12-authenticated-c10-mir-carrier-preflight
         [(get safety-verification :expected)])
        verification-candidate-preflight
        (invoke-c11
         'sh12-authenticated-c10-mir-carrier-preflight
         [(get safety-verification :candidate)])
        verification-metadata-preflight
        (invoke-c11
         'sh12-authenticated-c10-mir-carrier-preflight
         [(dissoc safety-verification :expected :candidate)])
        bound-shape-valid?
        (invoke-c11
         'sh12-c10-mir-bound-shape-valid?
         [safety-bound])
        c8-fact-tables-valid?
        (invoke-c11
         'sh12-c10-mir-c8-fact-tables-valid?
         [safety-bound])
        verification-valid?
        (invoke-c11
         'sh12-c10-mir-verification-valid?
         [safety-bound safety-verification])
        input-valid?
        (invoke-c11
         'sh12-authenticated-c10-mir-input-valid?
         [safety-bound safety-verification])
        diagnostic-ladder
        {:bound-preflight
         {:status (:status bound-preflight)
          :nodes (:nodes bound-preflight)
          :reason (:reason bound-preflight)}
         :verification-preflight
         {:status (:status verification-preflight)
          :nodes (:nodes verification-preflight)
         :reason (:reason verification-preflight)}
         :legacy-whole-envelope-preflight
         {:status (:status legacy-whole-envelope-preflight)
          :nodes (:nodes legacy-whole-envelope-preflight)
          :reason (:reason legacy-whole-envelope-preflight)}
         :verification-expected-preflight
         {:status (:status verification-expected-preflight)
          :nodes (:nodes verification-expected-preflight)
          :reason (:reason verification-expected-preflight)}
         :verification-candidate-preflight
         {:status (:status verification-candidate-preflight)
          :nodes (:nodes verification-candidate-preflight)
          :reason (:reason verification-candidate-preflight)}
         :verification-metadata-preflight
         {:status (:status verification-metadata-preflight)
          :nodes (:nodes verification-metadata-preflight)
          :reason (:reason verification-metadata-preflight)}
         :bound-shape-valid? bound-shape-valid?
         :c8-fact-tables-valid? c8-fact-tables-valid?
         :verification-valid? verification-valid?
         :input-valid? input-valid?}
        ownership-result
        (first (get-in safety-bound [:ownership-core :ownership-core
                                     :ownership-results]))
        ownership-fact (first (:ownership-facts ownership-result))]
    (is (java.util.Arrays/equals gravity-bytes qst-bytes))
    (is (= ".gravity"
           (subs (get-in artifact [:provenance :source-path])
                 (- (count (get-in artifact [:provenance :source-path])) 8))))
    (is (= :accepted (:status typed)))
    (is (= :passed (:status typed-verification)))
    (is (= :accepted (:status effected)))
    (is (= :passed (:status effect-verification)))
    (is (= :accepted (:status effect-bound)))
    (is (= :passed (:status effect-binding-verification)))
    (is (= :accepted (:status owned)))
    (is (= :passed (:status ownership-verification)))
    (is (= :accepted (:status ownership-bound)))
    (is (= :passed (:status ownership-binding-verification)))
    (is (= :accepted (:status safety)))
    (is (= :accepted (:status safety-bound)))
    (is (= :passed (:status safety-verification)))
    (is (= :accepted (:status bound-preflight))
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (= :rejected (:status legacy-whole-envelope-preflight))
        (str "C11 legacy whole-envelope diagnostic: "
             legacy-whole-envelope-preflight))
    (is (= :c10-mir-carrier-node-bound
           (:reason legacy-whole-envelope-preflight))
        (str "C11 legacy whole-envelope diagnostic: "
             legacy-whole-envelope-preflight))
    (is (= :accepted (:status verification-preflight))
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (= :accepted
           (get-in verification-preflight
                   [:metadata-preflight :status]))
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (= :accepted
           (get-in verification-preflight
                   [:expected-preflight :status]))
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (= :accepted
           (get-in verification-preflight
                   [:candidate-preflight :status]))
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (= 36113 (:nodes verification-preflight))
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (= true (:nodes-exact? verification-preflight))
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (= 21
           (get-in verification-preflight
                   [:metadata-preflight :nodes]))
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (= true
           (get-in verification-preflight
                   [:metadata-preflight :nodes-exact?]))
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (= 18046
           (get-in verification-preflight
                   [:expected-preflight :nodes]))
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (= true
           (get-in verification-preflight
                   [:expected-preflight :nodes-exact?]))
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (= 18046
           (get-in verification-preflight
                   [:candidate-preflight :nodes]))
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (= true
           (get-in verification-preflight
                   [:candidate-preflight :nodes-exact?]))
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (= :accepted (:status verification-expected-preflight))
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (= :accepted (:status verification-candidate-preflight))
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (= :accepted (:status verification-metadata-preflight))
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (true? bound-shape-valid?)
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (true? c8-fact-tables-valid?)
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (true? verification-valid?)
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (is (true? input-valid?)
        (str "C11 diagnostic ladder: " diagnostic-ladder))
    (if input-valid?
      (let [template
            (invoke-c11
             'sh12-build-authenticated-c10-mir-template
             [safety-bound safety-verification])
            operation-request
            (invoke-c11
             'sh12-authenticated-c10-mir-identity-request
             [safety-bound safety-verification template])
            operation-resolution
            {:request (:request operation-request)
             :digest
             (str "sha256:"
                  (bootstrap/sha256-hex
                   (pr-str (:request operation-request))))}
            module-request
            (invoke-c11
             'sh12-authenticated-c10-mir-module-identity-request
             [safety-bound safety-verification template operation-resolution])
            resolved
            {:operation-resolution operation-resolution
             :module-resolution
             {:request (:request module-request)
              :digest
              (str "sha256:"
                   (bootstrap/sha256-hex
                    (pr-str (:request module-request))))}}
            candidate
            (invoke-c11
             'sh12-bind-authenticated-c10-mir
             [safety-bound safety-verification template resolved])
            fresh
            (invoke-c11
             'sh12-verify-authenticated-c10-mir
             [safety-bound safety-verification template resolved candidate])
            operation (:operation candidate)
            block
            (let [fn-id (first (keys (:functions candidate)))
                  function (get (:functions candidate) fn-id)]
              (get (:blocks function) (:entry function)))]
        (is (= :accepted (:status template)))
    (is (= :accepted (:status operation-request)))
    (is (= :accepted (:status module-request)))
    (is (= :accepted (:status candidate)))
    (is (= :passed (:status fresh)))
    (is (= 1 (count (:call-type-facts typed))))
    (is (= 1 (count (:call-type-facts effected))))
    (is (= 1 (count (:effect-requests effected))))
    (is (= 1 (count (:ownership-requests owned))))
    (is (= 1 (count (:ownership-results owned))))
    (is (= 1 (count (:ownership-facts ownership-result))))
    (is (= :read (:operation ownership-fact)))
    (is (= :load (get-in safety-bound [:operation :operation-id])))
    (is (= :gravity.type/bool
           (get-in safety-bound [:operation :facts :value-type])))
    (is (= :persistent-immutable
           (get-in safety-bound [:operation :facts :ownership-kind])))
    (is (= :read (get-in safety-bound [:operation :facts :event])))
    (is (= :proven-safe (get-in safety-bound [:safety-result :outcome])))
    (is (= :gravity/mir-operation (:artifact operation)))
    (is (= :proof-reference (:opcode operation)))
    (is (= :load (:source-operation operation)))
    (is (= 1 (count (:functions candidate))))
    (is (= 1 (count (:instructions block))))
    (is (= (get-in safety-bound [:operation :source-span])
           (get-in operation [:source :source-span])))
    (is (= (get-in safety-bound [:operation :origin-chain])
           (get-in operation [:source :origin-chain])))
    (is (= (:type-fact-id (:operation safety-bound))
           (:type-fact-id (:facts operation))))
    (is (= (:effect-fact-id (:operation safety-bound))
           (:effect-fact-id (:facts operation))))
    (is (= (:capability-proof-id (:operation safety-bound))
           (:capability-proof-id (:facts operation))))
    (is (= (:ownership-fact-id (:operation safety-bound))
           (:ownership-fact-id (:facts operation))))
    (is (= (:proof-id safety-bound)
           (:safety-proof-id (:facts operation))))
    (is (= (:safety-core-id safety-bound)
           (:c10-safety-core-id (:facts operation))))
    (is (= (:provenance safety-bound) (:provenance candidate)))
    (is (= false (:executable-load? candidate)))
    (is (= true (:target-independent? candidate)))
    (is (= :blocked (get-in candidate [:b1-preflight :status])))
    (is (= :pending-proof-reference-verifier
           (:verification-status candidate)))
    (is (some #{:trusted-digest-resolution} (:nonclaims candidate)))
        (is (some #{:generic-c11-verifier} (:nonclaims fresh))))
      nil)))
