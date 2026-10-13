
(defn- p15-s23-sh07-b51-assert-content-coordinate!
  [commit tree phase ordinal]
  (let [actual-commit
        (p15-s23-sh07-b51-single-lf-line
         (p15-s23-sh07-b51-git-bytes
          ["rev-parse" "--verify" (str commit "^{commit}")])
         [phase ordinal :commit])
        actual-tree
        (p15-s23-sh07-b51-single-lf-line
         (p15-s23-sh07-b51-git-bytes
          ["rev-parse" "--verify" (str commit "^{tree}")])
         [phase ordinal :tree])]
    (when-not (and (= commit actual-commit) (= tree actual-tree))
      (p15-s23-sh07-b51-governed-read-fail!
       :content-coordinate
       {:phase phase :ordinal ordinal
        :expected {:commit commit :tree tree}
        :actual {:commit actual-commit :tree actual-tree}}))))

(defn- p15-s23-sh07-b51-ls-tree-row
  [commit path]
  (let [bytes (p15-s23-sh07-b51-git-bytes
               ["ls-tree" "-z" commit "--" path])
        text (p15-s23-sh07-b51-strict-utf8 bytes [:ls-tree path])]
    (when-not (and (pos? (count text))
                   (= \u0000 (last text))
                   (= 1 (count (filter #(= \u0000 %) text))))
      (p15-s23-sh07-b51-governed-read-fail!
       :ls-tree-record {:path path}))
    (let [record (subs text 0 (dec (count text)))
          tab (.indexOf ^String record "\t")
          header (when (pos? tab) (subs record 0 tab))
          observed-path (when (pos? tab) (subs record (inc tab)))
          fields (when header (str/split header #" " -1))]
      (when-not (and (= 3 (count fields))
                     (= "100644" (nth fields 0 nil))
                     (= "blob" (nth fields 1 nil))
                     (boolean (re-matches #"[0-9a-f]{40}"
                                          (nth fields 2 "")))
                     (= path observed-path))
        (p15-s23-sh07-b51-governed-read-fail!
         :ls-tree-projection {:path path :record record}))
      {:mode (nth fields 0) :type (nth fields 1)
       :blob-oid (nth fields 2)})))

(defn- p15-s23-sh07-b51-acquire-object-proofs
  [commit tree path]
  (let [commit-bytes
        (p15-s23-sh07-b51-git-bytes ["cat-file" "commit" commit])
        content {:commit commit :tree tree}
        content-proof
        {:oid commit
         :raw-bytes
         (p15-s23-sh07-b51-unsigned-byte-vector commit-bytes)}
        _ (p15-s23-sh07-b51-validate-content-object-proof!
           content content-proof)
        segments (p15-s23-sh07-b51-safe-proof-path-segments path)
        {:keys [tree-chain leaf-oid leaf-bytes]}
        (loop [index 0 tree-oid tree result []]
          (let [component (nth segments index)
                tree-bytes
                (p15-s23-sh07-b51-git-bytes ["cat-file" "tree" tree-oid])
                entries
                (p15-s23-sh07-b51-parse-tree-object!
                 tree-bytes [path index :acquisition])
                matches (filterv #(= component (:name %)) entries)
                selected (first matches)
                final? (= index (dec (count segments)))]
            (when-not
             (and (= tree-oid
                     (p15-s23-sh07-b51-git-object-id "tree" tree-bytes))
                  (= 1 (count matches))
                  (= (if final? "100644" "40000") (:mode selected)))
              (p15-s23-sh07-b51-governed-read-fail!
               :source-object-proof-acquisition
               {:path path :index index :component component}))
            (let [row {:tree-oid tree-oid
                       :tree-bytes
                       (p15-s23-sh07-b51-unsigned-byte-vector tree-bytes)
                       :component component
                       :selected-mode (:mode selected)
                       :selected-oid (:oid selected)}
                  next-result (conj result row)]
              (if final?
                (let [blob-bytes
                      (p15-s23-sh07-b51-git-bytes
                       ["cat-file" "blob" (:oid selected)])]
                  {:tree-chain next-result
                   :leaf-oid (:oid selected)
                   :leaf-bytes blob-bytes})
                (recur (inc index) (:oid selected) next-result)))))
        source-proof
        {:schema :gravity/sh07-b51-git-tree-inclusion-proof-v1
         :path path
         :root-tree tree
         :tree-chain tree-chain
         :blob-bytes
         (p15-s23-sh07-b51-unsigned-byte-vector leaf-bytes)}]
    {:content-object-proof content-proof
     :source-object-proof source-proof
     :leaf-oid leaf-oid
     :leaf-bytes leaf-bytes}))

(defn- p15-s23-sh07-b51-acquire-source-entry
  [commit tree spec]
  (when (and *p15-s23-sh07-b51-governed-source-session*
             (true? @(get-in *p15-s23-sh07-b51-governed-source-session*
                            [:state :finalized] (atom false))))
    (p15-s23-sh07-b51-governed-read-fail!
     :acquisition-after-finalize {:path (:path spec)}))
  (let [{:keys [ordinal path]} spec]
    (let [{:keys [content-object-proof source-object-proof
                  leaf-oid leaf-bytes]}
          (p15-s23-sh07-b51-acquire-object-proofs commit tree path)]
    (p15-s23-sh07-b51-assert-content-coordinate!
     commit tree :pre-acquisition ordinal)
    (let [tree-row (p15-s23-sh07-b51-ls-tree-row commit path)
          bytes (p15-s23-sh07-b51-git-bytes
                 ["cat-file" "blob" (:blob-oid tree-row)])
          text (p15-s23-sh07-b51-strict-utf8 bytes [:source ordinal path])
          descriptor
          {:ordinal ordinal
           :kind (:kind spec)
           :module-ordinal (:module-ordinal spec)
           :path path
           :mode (:mode tree-row)
           :blob-oid (:blob-oid tree-row)
           :byte-count (alength bytes)
           :raw-sha256 (p15-s23-sh07-b51-raw-sha256 bytes)}]
      (when-not (and (= (:blob-oid tree-row) leaf-oid)
                     (p15-s23-sh07-b51-byte-array-equal?
                      bytes leaf-bytes))
        (p15-s23-sh07-b51-governed-read-fail!
         :object-and-ls-tree-route-mismatch {:path path :ordinal ordinal}))
      (p15-s23-sh07-b51-validate-source-object-proof!
       {:commit commit :tree tree} descriptor source-object-proof)
      (p15-s23-sh07-b51-assert-content-coordinate!
       commit tree :post-acquisition ordinal)
      {:descriptor descriptor :bytes bytes :text text
       :content-object-proof content-object-proof
       :source-object-proof source-object-proof}))))

(defn- p15-s23-sh07-b51-process-role-fail!
  [reason details]
  (throw (ex-info "SH-07 B51 process role rejected"
                  {:id "SH07-B51-TRUSTED-TCB"
                   :reason reason
                   :details details})))

(defn- p15-s23-sh07-b51-process-role-for-observation
  [{:keys [arguments loaded-project-namespaces]}]
  (let [arguments (when (vector? arguments) arguments)
        loaded (set loaded-project-namespaces)
        checker-child
        'gravity.tools.sh07-b51-attempt32-checker-child
        coordinator
        'gravity.tools.sh07-b51-attempt32-coordinator-child
        producer
        'gravity.tools.sh07-b51-attempt30-producer-census
        independent
        'gravity.tools.sh07-b51-attempt30-independent-census
        bootstrap 'gravity.bootstrap
        checker-tail (when (and arguments (<= 5 (count arguments)))
                       (subvec arguments (- (count arguments) 5)))
        coordinator-tail-count
        (+ 3 (* 2 (count p15-s23-sh07-b51-coordinator-option-names)))
        coordinator-tail
        (when (and arguments
                   (<= coordinator-tail-count (count arguments)))
          (subvec arguments (- (count arguments) coordinator-tail-count)))
        coordinator-option-names
        (when coordinator-tail
          (mapv first (partition 2 (subvec coordinator-tail 3))))
        producer-process?
        (and (= ["clojure.main" "-m" p15-s23-sh07-b51-checker-entrypoint
                 "--role" "producer-census"]
                checker-tail)
             (every? loaded [bootstrap checker-child producer])
             (not-any? loaded [coordinator independent]))
        independent-process?
        (and (= ["clojure.main" "-m" p15-s23-sh07-b51-checker-entrypoint
                 "--role" "independent-verifier-census"]
                checker-tail)
             (every? loaded [bootstrap checker-child independent])
             (not-any? loaded [coordinator producer]))
        coordinator-process?
        (and (= ["clojure.main" "-m" p15-s23-sh07-b51-coordinator-entrypoint]
                (when coordinator-tail (subvec coordinator-tail 0 3)))
             (= p15-s23-sh07-b51-coordinator-option-names
                coordinator-option-names)
             (every? loaded [bootstrap coordinator])
             (not-any? loaded [checker-child producer independent]))]
    (cond
      producer-process? :producer-census
      independent-process? :independent-verifier-census
      coordinator-process? :m
      :else
      (p15-s23-sh07-b51-process-role-fail!
       :unauthenticated-process-role
       {:argument-count (count arguments)
        :loaded-project-namespaces (vec (sort-by str loaded))}))))

(defn- p15-s23-sh07-b51-process-role
  []
  (let [info (.info (java.lang.ProcessHandle/current))
        argument-value (.arguments info)
        arguments (when (.isPresent argument-value)
                    (vec (.get argument-value)))
        loaded-project-namespaces
        (->> (loaded-libs)
             (filter #(str/starts-with? (str %) "gravity."))
             set)]
    (p15-s23-sh07-b51-process-role-for-observation
     {:arguments arguments
      :loaded-project-namespaces loaded-project-namespaces})))

(defn- p15-s23-sh07-b51-open-source-session
  [commit tree]
  (when-not (and (string? commit)
                 (boolean (re-matches #"[0-9a-f]{40}" commit))
                 (string? tree)
                 (boolean (re-matches #"[0-9a-f]{40}" tree)))
    (p15-s23-sh07-b51-governed-read-fail!
     :content-coordinate-shape {:commit commit :tree tree}))
  (let [entries (mapv #(p15-s23-sh07-b51-acquire-source-entry
                        commit tree %)
                      p15-s23-sh07-b51-source-specs)
        descriptors (mapv :descriptor entries)
        token (Object.)
        state {:token token
               :thread (Thread/currentThread)
               :role (p15-s23-sh07-b51-process-role)
               :commit commit :tree tree
               :entries entries
               :descriptors descriptors
               :descriptors-root
               (reader-canonical-hash
                [:gravity/sh07-b51-source-descriptors-root-v1 descriptors])
               :cursor (atom 0)
               :events (atom [])
               :checked-products (atom {})
               :finalized (atom false)}]
    {:token token :state state}))
