
(defn- build-a9-stage-result-e1
  [content rows-by-path bytes-by-path decoded-static]
  (let [descriptors (a9-artifact-descriptors rows-by-path bytes-by-path)
        terminal-descriptor (nth descriptors 2)
        record (read-strict-json! (get decoded-static a9-terminal-path))
        ledger (read-strict-json!
                (get decoded-static "contracts/workstream-ledger.json"))
        manifest (terminal-manifest-entry! ledger record)
        owned [architecture-report-path provisional-report-path]
        history (get record "history")
        expected-manifest
        {"dependencies" a9-dependencies
         "id" a9-workstream-id
         "invariant_family" a9-invariant-family
         "ordinal" 161
         "path" a9-terminal-path
         "sha256" (subs (:raw-sha256 terminal-descriptor) 7)
         "state" "integrated"
         "terminal" true}
        preceding-checks
        {:canonical-json true
         :record-shape-valid (a9-record-shape-valid? record)
         :filename-sha256-match
         (= a9-terminal-path
            (str "contracts/workstream-records/terminal/" a9-workstream-id
                 "-" (subs (:raw-sha256 terminal-descriptor) 7) ".json"))
         :manifest-entry-match (= expected-manifest manifest)
         :workstream-id-match (= a9-workstream-id (get record "id"))
         :invariant-family-match
         (= a9-invariant-family (get record "invariant_family"))
         :architecture-decision-path-match
         (= architecture-report-path (get record "architecture_decision"))
         :owned-paths-match (= owned (get record "owned_paths"))
         :accepted-fixtures-equal-owned-paths
         (= (get record "owned_paths")
            (get-in record ["evidence" "accepted_fixtures"]))
         :base-commit-id-syntax-valid (git-id? (get record "base_commit"))
         :candidate-commit-id-syntax-valid
         (git-id? (get record "candidate_commit"))
         :candidate-differs-from-base
         (not= (get record "candidate_commit") (get record "base_commit"))
         :clean-worktree-recorded (true? (get record "clean_worktree"))
         :independent-accepted-review-present
         (boolean
          (some #(and (= "independent" (get % "kind"))
                      (= "accepted" (get % "result"))
                      (not= (get record "author") (get % "reviewer")))
                (get record "reviews")))
         :history-transitions-valid
         (and (a9-record-shape-valid? record)
              (history-transitions-valid? history))
         :history-final-state-match
         (= (get record "state") (get (last history) "state"))
         :record-state-integrated (= "integrated" (get record "state"))
         :authority-ceiling-match
         (= {"integration_only" true "release" false
             "seed_retirement" false "self_hosting" false}
            (get record "no_overclaim_authority"))}
        checks (assoc preceding-checks :all-checks-pass
                      (every? true? (vals preceding-checks)))]
    (when-not (:all-checks-pass checks)
      (fail! "SH07-B51-GIT-SNAPSHOT" :a9-stage-result-checks
             {:failed-checks
              (->> preceding-checks
                   (keep (fn [[key value]] (when-not (true? value) key)))
                   vec)}))
    {:schema :gravity/sh07-b51-a9-stage-result-e1
     :stage :a9
     :content content
     :a9-artifact-descriptors descriptors
     :terminal-record
     {:coordinate (assoc terminal-descriptor
                         :commit (:commit content) :tree (:tree content))
      :manifest-entry manifest
      :record record}
     :checks checks
     :result :terminal-record-integrated
     :historical-claims
     {:candidate-bound-review false
      :candidate-tree false
      :integration-commit false
      :integration-tree false
      :candidate-integration-ancestry false
      :report-preservation-across-integration false
      :supporting-report-preservation-across-integration false
      :ownership-reservation false
      :ownership-handoff false
      :reservation-release false
      :worktree-removal false
      :branch-removal false
      :finally-entered false}
     :nonclaims nonclaims}))

(def ^:private pre-mask-specs
  [{:path "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj", :load-ordinal 816, :form-ordinal 13, :owner (quote gravity.bootstrap/p15-s23-sh07-b51-expected-m3-id), :rhs-kind :digest-pin}
   {:path "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj", :load-ordinal 816, :form-ordinal 14, :owner (quote gravity.bootstrap/p15-s23-sh07-b51-expected-certificate-a-id), :rhs-kind :digest-pin}
   {:path "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj", :load-ordinal 816, :form-ordinal 15, :owner (quote gravity.bootstrap/p15-s23-sh07-b51-expected-certificate-b-id), :rhs-kind :digest-pin}
   {:path "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj", :load-ordinal 816, :form-ordinal 16, :owner (quote gravity.bootstrap/p15-s23-sh07-b51-expected-semantic-root), :rhs-kind :digest-pin}
   {:path "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00947-p15-s23-sh07-b51-linked-carrier-v4-05.clj", :load-ordinal 843, :form-ordinal 12, :owner (quote gravity.bootstrap/p15-s23-sh07-b51-authoritative-linked-carrier-value-v4), :rhs-kind :quoted-carrier-v4}])

(def ^:private provisional-rhs-bytes
  (.getBytes ":gravity/provisional-no-authority" StandardCharsets/UTF_8))

(def ^:private masked-rhs-bytes
  (.getBytes ":gravity/sh07-b51-masked-rhs-v1" StandardCharsets/UTF_8))

;; The PRE and runtime masks are intentionally separate schema families.  PRE
;; is the four-field C/T-only projection authenticated by Q; runtime carries
;; the complete dual-image proof after the four digest pins and quoted carrier
;; have been obtained from fresh execution.  Keep these key sets public so
;; later phase validators can reuse the closed schemas without reconstructing
;; them from prose.
(def pre-masked-root-keys
  #{:coordinates :preimage-spans :preimage-files :preimage-root})

(def runtime-masked-root-keys
  #{:coordinates :preimage-spans :postimage-spans :preimage-files
    :postimage-files :preimage-root :postimage-root :equal})

(def ^:private mask-coordinate-keys
  #{:path :load-ordinal :form-ordinal :owner :rhs-kind
    :left-delimiter :right-delimiter :preceding-byte :following-byte
    :masked-rhs-byte-count})

(def ^:private mask-span-keys
  #{:coordinate-ordinal :form-start-byte :rhs-start-byte :rhs-end-byte
    :form-end-byte :rhs-byte-count :rhs-raw-sha256})

(def ^:private mask-file-keys
  #{:path :masked-byte-count :masked-raw-sha256})

(def runtime-pin-map-keys
  #{:expected-m3-id :expected-certificate-a-id
    :expected-certificate-b-id :expected-semantic-root})

(def ^:private runtime-pin-order
  [:expected-m3-id :expected-certificate-a-id
   :expected-certificate-b-id :expected-semantic-root])

(def ^:private masked-source-root-domain
  :gravity/sh07-b51-masked-source-root-v1)

(defn- byte-match-at? [^bytes haystack ^bytes needle offset]
  (and (<= (+ offset (alength needle)) (alength haystack))
       (loop [index 0]
         (or (= index (alength needle))
             (and (= (aget haystack (+ offset index)) (aget needle index))
                  (recur (inc index)))))))

(defn- byte-match-offsets [^bytes haystack ^bytes needle]
  (loop [offset 0 result []]
    (if (> (+ offset (alength needle)) (alength haystack))
      result
      (recur (inc offset)
             (if (byte-match-at? haystack needle offset)
               (conj result offset)
               result)))))

(defn- source-form-at! [text form-ordinal]
  (with-open [reader (clojure.lang.LineNumberingPushbackReader.
                      (StringReader. text))]
    (binding [*read-eval* false *data-readers* {}
              *default-data-reader-fn*
              (fn [tag _]
                (fail! "SH07-B51-GIT-SNAPSHOT" :source-reader-tag
                       {:tag tag}))]
      (loop [ordinal 0]
        (let [form (read {:eof ::eof :read-cond :allow} reader)]
          (cond
            (= ::eof form)
            (fail! "SH07-B51-GIT-SNAPSHOT" :masked-form-ordinal
                   {:form-ordinal form-ordinal})
            (= ordinal form-ordinal) form
            :else (recur (inc ordinal))))))))

(defn- replace-byte-span
  [^bytes source start end ^bytes replacement]
  (let [result (byte-array (+ start (alength replacement)
                              (- (alength source) end)))]
    (System/arraycopy source 0 result 0 start)
    (System/arraycopy replacement 0 result start (alength replacement))
    (System/arraycopy source end result (+ start (alength replacement))
                      (- (alength source) end))
    result))

(defn- expected-mask-coordinates []
  (mapv
   (fn [{:keys [path load-ordinal form-ordinal owner rhs-kind]}]
     {:path path
      :load-ordinal load-ordinal
      :form-ordinal form-ordinal
      :owner owner
      :rhs-kind rhs-kind
      :left-delimiter (str "(def ^:private " (name owner) "\n  ")
      :right-delimiter ")"
      :preceding-byte 10
      :following-byte 10
      :masked-rhs-byte-count (alength masked-rhs-bytes)})
   pre-mask-specs))

(defn- mask-form-bytes
  [left rhs]
  (let [left-bytes (.getBytes ^String left StandardCharsets/UTF_8)
        result (byte-array (+ 1 (alength left-bytes) (alength ^bytes rhs) 2))
        rhs-start (inc (alength left-bytes))]
    (aset-byte result 0 (byte 10))
    (System/arraycopy left-bytes 0 result 1 (alength left-bytes))
    (System/arraycopy rhs 0 result rhs-start (alength ^bytes rhs))
    (aset-byte result (+ rhs-start (alength ^bytes rhs))
               (byte (int \))))
    (aset-byte result (dec (alength result)) (byte 10))
    result))
