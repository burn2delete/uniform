(ns gravity.self-hosting.sh03-reader-adapter-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

;; Coordinator API assumptions for this dedicated lane:
;; - sh03-reader-resolved-result! is the authenticated low-level entry point.
;; - compiler-c2-reader-file-artifact, compiler-c3-syntax-file-artifact,
;;   p15-s23-source-syntax-c2-artifact, and check-file-artifact all route target
;;   source through that entry point after the SH-03 cutover.
;; Legacy-oracle tripwires are resolved by name so an eventual explicit oracle
;; rename does not require coupling this namespace to a private implementation.

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh03_reader_adapter_test.clj")]
    (when-not resource
      (throw (ex-info "SH-03 test source is not on the classpath"
                      {:id "SH03-TEST-SOURCE"})))
    (loop [path (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? path)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH03-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve path "deps.edn")))
        path

        :else
        (recur (.getParent path))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-03")

(def ^:private rejected-fixtures
  (sorted-map
   "abbreviation-splice-outside" "C2-ABBREV"
   "abbreviation-unattached" "C2-ABBREV"
   "abbreviation-unquote-outside" "C2-ABBREV"
   "character-invalid" "C2-STRING"
   "character-octal-out-of-range" "C2-STRING"
   "character-surrogate-unicode" "C2-STRING"
   "delimiter-mismatched" "C2-DELIMITER"
   "depth-513" "C2-HASH"
   "encoding-invalid-utf8" "C2-ENCODING"
   "extension-invalid-inst-calendar" "C2-EXTENSION"
   "extension-invalid-uuid-payload" "C2-EXTENSION"
   "extension-unsupported" "C2-EXTENSION"
   "malformed-identifier" "C2-IDENTIFIER"
   "malformed-numeric" "C2-NUMERIC"
   "map-odd-arity" "C2-MAP"
   "metadata-invalid-map-key" "C2-METADATA"
   "metadata-invalid-shape" "C2-METADATA"
   "metadata-invalid-target" "C2-METADATA"
   "namespace-clause-dependency-shape" "L1-NS-SHAPE"
   "namespace-clause-doc-shape" "L1-NS-SHAPE"
   "namespace-clause-key-nonkeyword" "L1-NS-SHAPE"
   "namespace-clause-metadata-shape" "L1-NS-SHAPE"
   "namespace-clause-not-list" "L1-NS-SHAPE"
   "namespace-clause-set-shape" "L1-NS-SHAPE"
   "namespace-clause-single-arity" "L1-NS-SHAPE"
   "namespace-clause-unknown" "L1-NS-SHAPE"
   "namespace-clause-vector-shape" "L1-NS-SHAPE"
   "namespace-missing-name" "L1-NS-SHAPE"
   "namespace-name-nonsymbol" "L1-NS-SHAPE"
   "numeric-incomplete-exponent" "C2-NUMERIC"
   "numeric-invalid-radix" "C2-NUMERIC"
   "set-duplicate" "C2-SET"
   "set-equivalent-decimal" "C2-SET"
   "set-equivalent-decimal-exponent" "C2-SET"
   "set-equivalent-integer" "C2-SET"
   "set-equivalent-ratio" "C2-SET"
   "set-numeric-equivalence-limit" "C2-HASH"
   "string-invalid-escape" "C2-STRING"
   "string-invalid-unicode-escape" "C2-STRING"))

(defn- fixture-path
  [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- fixture-basenames
  [family extension]
  (let [directory (io/file (path (str fixture-root "/" family)))]
    (->> (.listFiles directory)
         (filter #(.isFile %))
         (map #(.getName %))
         (filter #(.endsWith ^String % extension))
         (map #(subs % 0 (- (count %) (count extension))))
         set)))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes
   (.toPath (java.io.File. source-path))))

(defn- utf8-text
  [bytes]
  (String. bytes java.nio.charset.StandardCharsets/UTF_8))

(defn- sh03-result
  [source-path]
  (let [bytes (source-bytes source-path)]
    (bootstrap/sh03-reader-resolved-result!
     source-path bytes
     (bootstrap/reader-project-context-for-source source-path)
     bootstrap/standard-reader-options)))

(defn- sh03-result-with-options
  [source-path reader-options]
  (bootstrap/sh03-reader-resolved-result!
   source-path (source-bytes source-path)
   (bootstrap/reader-project-context-for-source source-path)
   reader-options))

(def ^:private pinned-sh03-plan
  (delay (:plan (bootstrap/sh03-reader-current-binding!
                 "<sh03-dedicated-test>"))))

;; These checks exercise current source in the Clojure seed. They neither use
;; nor renew the authenticated reader binding, whose source bytes are pinned.
(def ^:private current-sh03-seed-plan
  (delay
    (let [source-path (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
          source-text (utf8-text (source-bytes source-path))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                  source-path :jvm))
       source-path source-text))))

(defn- current-sh03-seed-execute
  [function arguments]
  (with-redefs [bootstrap/sh03-reader-current-binding!
                (fn [& _]
                  (throw (ex-info "Current-source test invoked the pinned reader"
                                  {:id "SH03-CURRENT-SOURCE-PINNED-ROUTE"})))]
    (bootstrap/p15-s23-stage2-runtime-execute-function
     {:engine :gravity-sh03-current-source-seed-test
      :compiler-artifact-plan? true}
     @current-sh03-seed-plan function arguments)))

(defn- previous-sh03-hex-boolean-reference
  [characters]
  ;; Reference for the previous seed algorithm's Bool result only. Host rest
  ;; here supplies no Gravity Vector-rest law or collection-preservation claim.
  (loop [remaining characters]
    (if (= (count remaining) 0)
      true
      (if (contains? (set "0123456789abcdef") (first remaining))
        (recur (rest remaining))
        false))))

(deftest sh03-current-source-hex-seed-binding
  (is (true? (current-sh03-seed-execute 'sh03-lowercase-hex? [[]])))
  (let [source-path (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
        source-text (utf8-text (source-bytes source-path))]
    (is (= (str "sha256:" (bootstrap/sha256-hex source-text))
           (get-in @current-sh03-seed-plan [:source :sha256])))
    (is (contains? (:functions @current-sh03-seed-plan) 'sh03-sha256-id?))))

(deftest sh03-current-source-hex-preserves-vector-tail
  (let [invoke bootstrap/p15-s23-stage2-runtime-invoke-builtin
        unary-var (ns-resolve 'gravity.bootstrap
                              'p15-s23-stage2-runtime-invoke-unary-or-generic-builtin)
        invoke-unary @unary-var
        calls (atom [])
        characters (vec "0af")]
    ;; Force compilation before tracing just the helper's actual execution.
    (current-sh03-seed-execute 'sh03-lowercase-hex? [[]])
    (with-redefs-fn
      {#'bootstrap/p15-s23-stage2-runtime-invoke-builtin
       (fn [runtime plan callee arguments]
         (swap! calls conj [callee arguments])
         (invoke runtime plan callee arguments))
       unary-var
       (fn [runtime plan callee value]
         (swap! calls conj [callee [value]])
         (invoke-unary runtime plan callee value))}
      #(is (true? (current-sh03-seed-execute
                   'sh03-lowercase-hex? [characters]))))
    (is (= [] (filterv #(= 'rest (first %)) @calls)))
    (is (= [[characters 1 3] [(vec "af") 1 2] [(vec "f") 1 1]]
           (mapv second (filter #(= 'subvec (first %)) @calls))))
    (is (every? vector?
                (map #(first (second %))
                     (filter #(contains? #{'count 'first 'subvec} (first %))
                             @calls))))
    (is (= characters (vec "0af")))))

(deftest sh03-current-source-hex-finite-boolean-equivalence
  (let [hexadecimal "0123456789abcdef"
        full (vec (take 64 (cycle hexadecimal)))
        cases (concat [[] full (vec hexadecimal)]
                      (map #(vector (char %)) (range 128))
                      (map #(vec (take % (cycle hexadecimal))) (range 65))
                      (for [position (range 64) invalid [\g \A]]
                        (assoc full position invalid)))]
    (doseq [characters cases]
      (let [expected (previous-sh03-hex-boolean-reference characters)
            actual (current-sh03-seed-execute 'sh03-lowercase-hex? [characters])]
        (is (boolean? actual))
        (is (= expected actual) (pr-str characters))))))

(deftest sh03-current-source-sha256-caller-seed-behavior
  (let [suffix (apply str (take 64 (cycle "0123456789abcdef")))
        valid (str "sha256:" suffix)
        rejected [nil false 71 [] (vec valid) "" suffix
                  (str "SHA256:" suffix) (str "sha257:" suffix)
                  (str "sha256:" (subs suffix 1))
                  (str valid "0")
                  (str "sha256:g" (subs suffix 1))
                  (str "sha256:" (subs suffix 0 63) "g")
                  (str "sha256:A" (subs suffix 1))
                  (str "sha256:" (subs suffix 0 63) "F")]]
    (is (true? (current-sh03-seed-execute 'sh03-sha256-id? [valid])))
    (doseq [value rejected]
      (is (false? (current-sh03-seed-execute 'sh03-sha256-id? [value]))
          (pr-str value)))))

(deftest sh03-current-hex-definitions-compose-through-authenticated-core
  ;; A new two-function source subject, not whole-reader analysis or execution.
  (let [reader-path (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
        require! (fn [ok? phase]
                   (when-not ok?
                     (throw (ex-info "bounded hex source composition stopped" {:phase phase}))))
        check (fn [ok? label] (is (boolean ok?) (name label)))
        stage (fn [label operation]
                (println (pr-str {:hex-source-stage label :event :begin}))
                (let [value (operation)]
                  (println (pr-str {:hex-source-stage label :event :complete})) value))
        _ (require! (<= (.length (io/file reader-path)) 1048576) :reader-source-bytes)
        reader-text (slurp reader-path :encoding "UTF-8")
        names '[sh03-lowercase-hex? sh03-sha256-id?]
        definitions (filterv #(and (seq? %) (= 'defn (first %))
                                  (some #{(second %)} names))
                             (map :form (bootstrap/read-source-form-records reader-path reader-text)))
        _ (require! (= names (mapv second definitions)) :two-current-definitions)
        header '(ns self-hosting.sh03.hex-source-composition
                  (:profile :meta) (:target :jvm) (:effects #{}) (:capabilities #{})
                  (:safety :safe) (:exports [sh03-sha256-id?]))
        text (str (pr-str header) "\n" (apply str (map #(str (pr-str %) "\n") definitions)))
        _ (require! (<= (alength (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8))
                         2048) :two-kib-source)
        temporary (java.io.File/createTempFile "gravity-sh03-hex-source-" ".gravity")
        source-path (.getCanonicalPath temporary)
        core-path [:gravity-core-boundary :canonical-core-artifact]
        one (fn [values predicate]
              (let [selected (filterv predicate values)]
                (require! (= 1 (count selected)) :unique-selected-record) (first selected)))]
    (try
      (spit temporary text :encoding "UTF-8")
      ;; Host parsing selects the current bodies; only source4 authenticates the new subject.
      (check (= definitions (mapv :form (rest (bootstrap/read-source-form-records source-path text))))
             :selected-current-bodies)
      (let [resolved (stage :current-reader-c5
                           #(bootstrap/compiler-c5-resolution-source-artifact
                             source-path text
                             {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
                             reader-path))
            expanded (:sh05-macro-artifact resolved)
            c3 (get-in expanded [:gravity-macro-boundary :authenticated-sh04-artifact])
            c2 (:c2-reader-artifact c3)
            forms (:parsed-semantic-values c2)
            seeds (:syntax-seed-stream c2)
            input-syntax (bootstrap/sh05-source-syntax-stream c3)
            output-syntax (:expanded-syntax-stream expanded)
            trace (:macro-expansion-trace expanded)
            records (mapv (fn [form seed] (assoc seed :form form)) forms seeds)
            reference (bootstrap/macro-source-artifact-from-records source-path text records)
            artifact (stage :gravity-core #(bootstrap/sh07-core-from-resolution-artifact resolved))
            product (get-in artifact core-path)
            request (get-in artifact [:gravity-core-boundary :authenticated-core-request])
            _ (require! (and (<= (count (:nodes product)) 256)
                             (<= (count (:forms request)) 512)
                             (<= (count (:calls product)) 64)) :bounded-products)
            index (into {} (map (juxt :node-id identity)) (:nodes product))
            source-forms (into {} (map (juxt :form-id identity)) (:forms request))
            bindings (into {} (map (juxt :binding-id identity)) (:binding-table request))
            mappings (into {} (map (juxt :core-node-id identity)) (:source-map product))
            calls (:calls product)
            operator #(get-in index [(:operator-node-id %) :attributes :symbol])
            subvec-calls (filterv #(= 'subvec (operator %)) calls)
            changed (one subvec-calls
                         #(= 1 (get-in index [(second (:argument-node-ids %)) :attributes :value])))
            arguments (:argument-node-ids changed)
            count-call (one calls #(= (last arguments) (:core-node-id %)))
            remaining (get index (first arguments))
            counted (get index (first (:argument-node-ids count-call)))
            loop-binding (one (:loop-bindings product) #(= 'remaining (:name %)))
            loop-target (one (:recur-targets product) #(= :loop (:target-kind %)))
            transfer (one (:recur-transfers product) (constantly true))
            hex-function (one (:function-records product) #(= 'sh03-lowercase-hex? (:definition-name %)))
            function-target (one (:recur-targets product)
                                 #(= (:function-core-node-id hex-function) (:owner-core-node-id %)))
            report (stage :fresh-public-verification #(bootstrap/sh07-core-artifact-verification artifact))]
        (check (= :accepted (:status resolved) (:status expanded) (:status artifact)) :accepted-products)
        (check (= :passed (:status report)) :fresh-public-replay)
        (check (= source-path (get-in resolved [:provenance :source-path])
                  (get-in c2 [:source-unit-record :path])
                  (get-in product [:provenance :actual-source-path]) (:source-path report)) :new-source-provenance)
        (check (not= source-path reader-path) :not-whole-reader-provenance)
        (check (= (str "sha256:" (bootstrap/sha256-hex reader-text))
                  (get-in c3 [:gravity-syntax-boundary :reader-source-revision :source-content-hash])
                  (get-in c2 [:sh03-reader-authentication :reader-source-revision :source-content-hash]))
               :actual-reader-revision)
        (check (= (str "sha256:" (bootstrap/sha256-hex text))
                  (get-in c2 [:source-unit-record :bytes-hash])) :actual-subject-bytes)
        (check (= 3 (count forms) (count seeds) (count input-syntax) (count output-syntax)) :complete-source-streams)
        (check (= 2 (:expanded-defn-count expanded) (count trace) (count (:function-records product)))
               :two-function-expansion)
        (check (= (:expanded-forms reference) (:expanded-forms expanded)) :prior-stage-expansion-parity)
        (require! (= 2 (count (rest forms)) (count (:expanded-forms expanded))
                     (count (rest input-syntax)) (count (rest output-syntax)) (count trace))
                  :two-definition-pair-cardinality)
        (doseq [[original lowered input output step]
                (map vector (rest forms) (:expanded-forms expanded)
                     (rest input-syntax) (rest output-syntax) trace)]
          (check (= 'def (first lowered)) :generated-definition)
          (check (= (second original) (second lowered)) :definition-name)
          (check (= (cons 'fn (drop 2 original)) (nth lowered 2)) :parameters-and-original-body)
          (check (= (:syntax/id input) (:input-syntax-id step)) :input-syntax-origin)
          (check (= (:syntax/id output) (:output-syntax-id step)) :output-syntax-origin)
          (check (= (:span input) (:call-site-span step) (:span output)) :macro-source-span)
          (check (and (seq (:generated-origin step))
                      (= (last (:origin output)) (first (:generated-origin step))))
                 :introduced-origin)
          (check (seq (get-in step [:hygiene :introduced-identifiers])) :introduced-hygiene))
        (check (= 3 (count subvec-calls)) :three-actual-subvec-calls)
        (doseq [call (filter #(contains? '#{subvec count sh03-lowercase-hex?} (operator %)) calls)]
          (let [symbol (operator call)
                binding (get bindings (:operator-binding-id call))
                ids (into [(:operator-node-id call)] (:argument-node-ids call))
                node (get index (:core-node-id call))
                form (get source-forms (get-in node [:source :form-id]))]
            (check (= (if (= 'sh03-lowercase-hex? symbol)
                        [:namespace 'self-hosting.sh03.hex-source-composition symbol]
                        [:core 'gravity.core symbol])
                      ((juxt :binding-class :namespace :name) binding)) :actual-operator-binding)
            (check (= ids (:children node) (:ordered-evaluation-node-ids call)
                      (mapv :core-node-id (get-in node [:evaluation :order]))) :operator-then-arguments)
            (check (= (:child-form-ids form) (mapv #(get-in index [% :source :form-id]) ids))
                   :ordered-source-occurrences)))
        (check (= 3 (count arguments)) :changed-subvec-arity)
        (check (= ['remaining 1] [(get-in remaining [:attributes :symbol])
                                 (get-in index [(second arguments) :attributes :value])]) :subvec-prefix-operands)
        (check (= 'count (operator count-call)) :derived-count-operator)
        (check (= 1 (count (:argument-node-ids count-call))) :derived-count-arity)
        (check (= 'remaining (get-in counted [:attributes :symbol])) :derived-count-source)
        (check (and (= (get-in remaining [:attributes :binding-id]) (:binding-id loop-binding)
                       (get-in counted [:attributes :binding-id]))
                    (not= (get-in remaining [:source :form-id]) (get-in counted [:source :form-id])))
               :same-binding-distinct-occurrences)
        (check (= (:loop-core-node-id loop-binding) (:owner-core-node-id loop-target)) :loop-binding-owner)
        (check (= 'characters (get-in index [(:initializer-node-id loop-binding) :attributes :symbol]))
               :original-loop-initializer)
        (check (= [(:binding-id loop-binding)] (:binding-ids loop-target)) :loop-binding-target)
        (check (= (:target-id function-target) (:parent-target-id loop-target)) :nested-function-target)
        (check (= :loop (:target-kind transfer)) :nearest-loop-target-kind)
        (check (= (:target-id loop-target) (:target-id transfer)) :nearest-loop-target)
        (check (= 1 (:arity loop-target) (:arity transfer) (count (:argument-node-ids transfer))) :recur-arity)
        (check (= [(:core-node-id changed)] (:argument-node-ids transfer)) :recur-subvec-value)
        (check (and (:tail-position transfer) (= :nearest-lexical-recur-target (:transfer-policy transfer)))
               :recur-tail-transfer)
        (doseq [node (:nodes product)]
          (let [source (:source node)
                form (get source-forms (:form-id source))
                mapping (get mappings (:node-id node))
                fields [:syntax-id :form-id :origin-chain :generated-origin]]
            (check (and (map? form) (map? mapping)) :source-records-present)
            (check (= (select-keys form fields) (select-keys source fields) (select-keys mapping fields))
                   :source-and-generated-origin-joins)
            (check (= (:source-span form) (:source-span mapping)) :source-map-span)
            (when (#{:def :fn} (:core-form node))
              (check (seq (:generated-origin source)) :generated-core-origin))))
        (let [changed-id (:core-node-id changed)
              swapped [(first arguments) (last arguments) (second arguments)]
              ordered (into [(:operator-node-id changed)] swapped)
              update-table (fn [candidate table key transform]
                             (update-in candidate (conj core-path table)
                                        #(mapv (fn [row] (if (= changed-id (get row key))
                                                          (transform row) row)) %)))
              candidate
              (-> artifact
                  (update-table :nodes :node-id
                                #(assoc % :children ordered :evaluation
                                        (assoc (:evaluation %) :order
                                               (mapv (fn [index id] {:index index :core-node-id id})
                                                     (range) ordered))))
                  (update-table :calls :core-node-id
                                #(assoc % :argument-node-ids swapped :ordered-evaluation-node-ids ordered))
                  (update-table :evaluation-order :core-node-id #(assoc % :children ordered))
                  (update-table :call-edges :call-core-node-id
                                #(assoc % :argument-core-node-ids swapped :ordered-evaluation-node-ids ordered)))
              candidate (update-in candidate (conj core-path :identity-preimage)
                                   merge (select-keys (get-in candidate core-path)
                                                      [:nodes :calls :call-edges :evaluation-order]))
              changed-product (get-in candidate core-path)
              rejected (stage :fresh-mutated-public-verification
                              #(bootstrap/sh07-core-artifact-verification candidate))]
          (check (not= arguments swapped) :nonidentity-operand-carrier)
          (check (identical? request (get-in candidate [:gravity-core-boundary :authenticated-core-request]))
                 :unchanged-authenticated-source-request)
          (check (identical? resolved (:sh06-resolution-artifact candidate)) :unchanged-authenticated-parent)
          (check (= ordered (:children (get (into {} (map (juxt :node-id identity))
                                                     (get-in candidate (conj core-path :nodes))) changed-id)))
                 :coherent-node-child-copy)
          (check (= swapped (:argument-node-ids (one (:calls changed-product)
                                                    #(= changed-id (:core-node-id %))))) :coherent-call-copy)
          (check (= ordered (:children (one (:evaluation-order changed-product)
                                            #(= changed-id (:core-node-id %))))) :coherent-evaluation-copy)
          (check (= swapped (:argument-core-node-ids (one (:call-edges changed-product)
                                                         #(= changed-id (:call-core-node-id %)))))
                 :coherent-call-edge-copy)
          (check (= (select-keys changed-product [:nodes :calls :call-edges :evaluation-order])
                    (select-keys (:identity-preimage changed-product)
                                 [:nodes :calls :call-edges :evaluation-order])) :coherent-preimage-copies)
          (check (= :failed (:status rejected)) :mutated-carrier-rejected)
          (check (= source-path (:source-path rejected)) :original-rejection-source)
          (check (every? (set (:failed-checks rejected))
                         [:canonical-core-replays? :authoritative-products-replay?]) :original-replay-failures)
          (println (pr-str {:hex-source-mutation :subvec-operands-reordered
                            :status (:status rejected) :failed-checks (:failed-checks rejected)})))
        (check (= text (slurp source-path :encoding "UTF-8")) :subject-unchanged)
        (check (= {:C7 :pending :C8 :pending :C9 :pending :C10 :pending}
                  (get-in artifact [:execution-boundary :downstream-fact-statuses])) :no-typed-or-runtime-claim))
      (finally (check (.delete temporary) :temporary-source-removed)))))

(def ^:private sh03-facade-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/l1_c2_surface_syntax_reader.gravity")

(def ^:private sh03-facade-source-byte-count 36920)
(def ^:private sh03-facade-source-content-hash
  "sha256:cb416baa7330fd7db5507fcd5fc1d78d5c9e848feb9020552d4c21b9e1c17fe0")
(def ^:private sh03-facade-plan-semantic-hash
  "sha256:1012a51721cd2c29e260a6f7bc3bc0b2ced5aefe1222cbbd95a155fc44f8977f")
(def ^:private sh03-facade-functions-semantic-hash
  "sha256:1ac79857179b3729eb47c3ce364f990333799ffec49f5047e700be7e7a827123")

(def ^:private sh03-facade-plan
  (delay
    (let [source-path (path sh03-facade-source-relative-path)
          source-bytes (source-bytes source-path)
          emitter (:emitter
                   (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                    source-path :jvm))]
      (is (= sh03-facade-source-byte-count (alength source-bytes)))
      (is (= sh03-facade-source-content-hash
             (str "sha256:" (bootstrap/sha256-bytes-hex source-bytes))))
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter source-path (utf8-text source-bytes)))))

(defn- sh03-facade-execute
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh03-facade-host-runner
    :compiler-artifact-plan? true}
   @sh03-facade-plan function arguments))

(defn- sh03-facade-inputs
  [source-path]
  (let [bytes (source-bytes source-path)
        source-unit
        (bootstrap/sh03-reader-input-source-unit
         source-path bytes
         (bootstrap/reader-project-context-for-source source-path))
        reader-policy
        (bootstrap/sh03-reader-input-policy bootstrap/standard-reader-options)
        resolved
        (bootstrap/sh03-reader-resolved-result!
         source-path bytes
         (bootstrap/reader-project-context-for-source source-path)
         bootstrap/standard-reader-options)]
    [source-unit reader-policy (:raw-result resolved)
     (:verification-report resolved)]))

(defn- sh03-facade-substitutions
  [[source-unit reader-policy reader-result verification-report]]
  (let [other-root (str "sha256:" (apply str (repeat 64 "0")))]
    [[(assoc source-unit :project-root-id other-root)
      reader-policy reader-result verification-report]
     [source-unit reader-policy
      (assoc-in reader-result [:source-unit :project-root-id] other-root)
      verification-report]
     [(update source-unit :source-byte-count inc)
      reader-policy reader-result verification-report]
     [source-unit reader-policy
      (update-in reader-result [:source-unit :source-byte-count] inc)
      verification-report]
     [(assoc source-unit :encoding :utf-16)
      reader-policy reader-result verification-report]
     [source-unit reader-policy
      (assoc-in reader-result [:source-unit :encoding] :utf-16)
      verification-report]
     [source-unit (update reader-policy :retain-trivia not)
      reader-result verification-report]
     [source-unit reader-policy
      (update-in reader-result [:source-unit :reader-options
                                :retain-trivia] not)
      verification-report]
     [(assoc-in source-unit [:actual-path-provenance :path]
                "/substituted/source.gravity")
      reader-policy reader-result verification-report]
     [source-unit reader-policy
      (assoc-in reader-result [:actual-path-provenance :path]
                "/substituted/source.gravity")
      verification-report]
     [source-unit reader-policy reader-result
      (assoc verification-report :unexpected true)]
     [source-unit reader-policy reader-result
      (dissoc verification-report :bounds)]
     [source-unit reader-policy reader-result
      (update-in verification-report [:bounds :maximum-tokens] inc)]
     [source-unit reader-policy
      (update-in reader-result [:bounds :maximum-tokens] inc)
      verification-report]
     [(assoc source-unit :unexpected true)
      reader-policy reader-result verification-report]
     [source-unit (assoc reader-policy :unexpected true)
      reader-result verification-report]
     [source-unit reader-policy (assoc reader-result :unexpected true)
      verification-report]]))

(defn- direct-sh03-result
  [source-unit source-bytes reader-policy]
  (bootstrap/sh03-reader-execute-plan!
   "<sh03-dedicated-test>" @pinned-sh03-plan
   bootstrap/sh03-reader-entrypoint
   [source-unit source-bytes reader-policy]))

(defn- direct-sh03-verification
  [source-unit source-bytes reader-policy result]
  (bootstrap/sh03-reader-execute-plan!
   "<sh03-dedicated-test>" @pinned-sh03-plan
   bootstrap/sh03-reader-verifier
   [source-unit source-bytes reader-policy result]))

(defn- thrown-data
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))
    (catch Throwable error
      {:unexpected-host-error (.getName (class error))
       :message (.getMessage error)})))

(deftest sh03-current-source-c3-intake-retains-the-original-reader-revision
  ;; Explicit source input selects development construction, not migration or
  ;; permission to substitute a reader plan, accepted product, or authority.
  (let [source-path (fixture-path "accepted" "newline-lf" ".gravity")
        text (slurp source-path)
        context (bootstrap/reader-project-context-for-source source-path)
        reader-path (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
        failure (thrown-data #(bootstrap/compiler-c2-reader-source-artifact
                              source-path text context))]
    (is (= "C2-HASH" (:id failure)))
    (let [artifact (bootstrap/compiler-c3-syntax-source-artifact
                    source-path text context nil nil reader-path)
          boundary (:gravity-syntax-boundary artifact)
          revision (:reader-source-revision boundary)
          actual (get-in boundary [:reader-authentication-provenance
                                   :actual-sh03-envelope-descriptor :source-revision])]
      (is (= :passed (get-in artifact [:syntax-verification-report :status])))
      (is (= (str "sha256:" (bootstrap/sha256-hex (slurp reader-path)))
             (:source-content-hash revision) (:source-content-hash actual)))
      (is (not= bootstrap/sh03-reader-expected-source-content-hash
                (:source-content-hash revision)))
      (is (= actual (get-in artifact [:c2-reader-artifact :sh03-reader-authentication
                                     :reader-source-revision])))
      (is (= (:verifier-semantic-hash revision)
             (get-in actual [:function-shapes bootstrap/sh03-reader-verifier :semantic-hash])))
      (is (true? (bootstrap/c3-syntax-stream-reader-products-authentic?
                   (:syntax-object-stream artifact) (:c2-reader-artifact artifact) boundary))))))

(deftest sh03-current-reader-registered-literals-bind-the-original-envelope
  (let [temporary (java.io.File/createTempFile "gravity-sh03-current-registered-" ".gravity")
        source-path (.getAbsolutePath temporary)
        reader-path (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
        text (str "(ns gravity.sh03.current-registered (:profile :hosted) (:target :jvm) "
                  "(:effects #{}) (:capabilities #{}) (:safety :safe) (:exports [main]))\n"
                  "(defn main [] #uuid \"123e4567-e89b-12d3-a456-426614174000\")\n")]
    (try
      (spit temporary text)
      (let [artifact (bootstrap/compiler-c3-syntax-source-artifact source-path text
                       (bootstrap/reader-project-context-for-source source-path) nil nil reader-path)
            c2 (:c2-reader-artifact artifact) boundary (:gravity-syntax-boundary artifact)
            stream (:syntax-object-stream artifact)
            authentic? #(bootstrap/c3-syntax-stream-reader-products-authentic? stream %1 %2)
            revision (get-in c2 [:sh03-reader-authentication :reader-source-revision])
            other (assoc revision :source-content-hash (:plan-semantic-hash revision))
            retained (assoc-in c2 [:sh03-reader-authentication :reader-source-revision] other)
            replaced (assoc-in boundary [:reader-authentication-provenance
                                         :actual-sh03-envelope-descriptor :source-revision] other)
            derived (bootstrap/sh04-syntax-reader-binding source-path retained
                      (bootstrap/sh04-syntax-semantic-source-id source-path (:source-unit-record c2)))
            coherent-c2 (-> retained
                            (assoc-in [:registered-literal-projection :reader-binding] (:reader-binding derived))
                            (assoc-in [:registered-literal-projection :reader-source-revision] (:reader-source-revision derived)))
            coherent-boundary (assoc replaced :reader-semantic-binding (:reader-binding derived)
                                              :reader-source-revision (:reader-source-revision derived))]
        (is (= :passed (get-in artifact [:syntax-verification-report :status])))
        (is (seq (get-in c2 [:registered-literal-projection :bindings])))
        (is (true? (authentic? c2 boundary)))
        (is (not= revision other))
        (is (= (get-in boundary [:reader-authentication-provenance :actual-sh03-authenticated-envelope])
               (get-in coherent-boundary [:reader-authentication-provenance :actual-sh03-authenticated-envelope])))
        (doseq [[candidate candidate-boundary]
                [[(update c2 :sh03-reader-authentication dissoc :reader-source-revision) boundary]
                 [retained boundary] [c2 replaced] [retained replaced] [coherent-c2 coherent-boundary]
                 [(assoc-in c2 [:sh03-reader-authentication :reader-source-revision
                                :function-shapes bootstrap/sh03-reader-verifier :semantic-hash] nil) boundary]]]
          (is (false? (authentic? candidate candidate-boundary))))
        ;; Supplied DATA isolates the current-only obligation even when the
        ;; source SHA equals history; this does not execute a historical reader.
        (let [guard (ns-resolve 'gravity.bootstrap 'c3-current-reader-revision-authentic?)
              omitted (update c2 :sh03-reader-authentication dissoc :reader-source-revision)
              historical (-> boundary
                             (assoc-in [:reader-source-revision :source-content-hash]
                                       bootstrap/sh03-reader-expected-source-content-hash)
                             (assoc-in [:reader-authentication-provenance
                                        :actual-sh03-envelope-descriptor :source-revision :source-content-hash]
                                       bootstrap/sh03-reader-expected-source-content-hash))]
          (is (true? (guard c2 boundary)))
          (doseq [hash [(get-in revision [:function-shapes bootstrap/sh03-reader-verifier :semantic-hash]) nil]]
            (let [candidate (assoc-in historical [:reader-authentication-provenance
                                                  :actual-sh03-envelope-descriptor :source-revision
                                                  :function-shapes bootstrap/sh03-reader-verifier :semantic-hash] hash)]
              (is (contains? (get-in candidate [:reader-authentication-provenance
                                               :actual-sh03-envelope-descriptor :source-revision
                                               :function-shapes bootstrap/sh03-reader-verifier]) :semantic-hash))
              (is (false? (guard omitted candidate)))
              (is (false? (authentic? omitted candidate)))))))
      (finally (.delete temporary)))))

(deftest sh03-current-reader-plan-checks-keep-historical-priority
  ;; Supplied-plan validation units, not historical reader admission. The
  ;; current plan is derived by the original source-selected constructor.
  (let [source-path (fixture-path "accepted" "newline-lf" ".gravity")
        reader-path (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
        binding (bootstrap/sh03-reader-current-binding! source-path reader-path)
        plan (:plan binding)
        structure (ns-resolve 'gravity.bootstrap 'sh03-reader-plan-structure!)
        identities (bootstrap/sh03-reader-plan-identities plan)
        functions (:functions plan)
        function-names (vec (sort-by str (keys functions)))
        function-shapes (into (sorted-map)
                              (map (fn [[name function]]
                                     [name (select-keys function [:arity :params])])) functions)
        summary (bootstrap/sh03-reader-plan-instruction-summary source-path functions)
        audit (bootstrap/sh03-reader-plan-static-audit! source-path plan)
        builtin-names (vec (sort-by str (:builtin-functions audit)))
        removable (first (remove #{bootstrap/sh03-reader-entrypoint bootstrap/sh03-reader-verifier}
                                  (keys (:functions plan))))
        wrong-count (-> plan (update :functions dissoc removable)
                            (assoc-in [:functions bootstrap/sh03-reader-entrypoint :instructions]
                                      [{:op :unreviewed-current-test}]))]
    (is (= :gravity/sh03-current-source-reader-plan-binding (:artifact binding)))
    (is (= (count (:functions plan)) (:function-count (structure source-path plan nil))))
    (is (= [:exact-sh03-reader-plan-envelope]
           (:missing-fields (thrown-data #(bootstrap/sh03-reader-plan-validate! source-path wrong-count)))))
    (is (= [:pinned-recomputable-sh03-reader-plan-identities]
           (:missing-fields (thrown-data #(bootstrap/sh03-reader-plan-validate! source-path plan)))))
    ;; One returned current plan, not a new pin or a runtime-used population.
    (is (= identities (select-keys binding (keys identities))))
    (is (= (:plan-id plan) (:plan-semantic-hash identities)
           (bootstrap/reader-canonical-hash (dissoc plan :plan-id))))
    (is (= (:instruction-summary plan) (:instruction-summary identities)
           (:instruction-summary summary) (:instruction-summary audit)))
    (is (= summary audit))
    (is (every? #(contains? bootstrap/sh03-reader-allowed-builtins %) builtin-names))
    (is (= (:builtin-functions-hash binding) (bootstrap/reader-canonical-hash builtin-names)))
    (is (= (:function-count binding) (count function-names)))
    (is (= (:function-names-hash binding) (bootstrap/reader-canonical-hash function-names)))
    (is (= (:function-shapes-hash binding) (bootstrap/reader-canonical-hash function-shapes)))
    (is (= (:functions-semantic-hash binding) (bootstrap/reader-canonical-hash functions)))
    (is (= (:source-content-hash binding) (get-in plan [:source :sha256])))
    (is (= (:source-byte-count binding) (get-in plan [:source :byte-count])))
    (let [observation
          (pr-str {:current-reader-plan-observation
                   (merge identities
                          (select-keys binding [:source-path :source-byte-count :source-content-hash])
                          {:plan-id (:plan-id plan) :compiler (:compiler plan)
                           :population :compiled-potential
                           :function-names function-names :builtin-functions builtin-names})})]
      (when (> (alength (.getBytes ^String observation java.nio.charset.StandardCharsets/UTF_8)) 65536)
        (throw (ex-info "current reader plan observation exceeds 64 KiB" {:maximum-bytes 65536})))
      (println observation))
    ;; Supplied DATA for the existing static auditor only: no substituted plan
    ;; is admitted or executed, and quote payloads are not instructions.
    (let [instruction-path [:functions bootstrap/sh03-reader-entrypoint :instructions]
          fake-payload [{:op :unreviewed-current-test}
                        {:op :builtin-call :function 'unreviewed-current-builtin-test :args []}
                        {:op :function-call :function 'sh03-unbound-plan-boundary-callee :args []}]
          benign (assoc-in plan instruction-path [{:op :quote :value :plain-data}])
          quoted (assoc-in plan instruction-path [{:op :quote :value fake-payload}])]
      (is (= (bootstrap/sh03-reader-plan-static-audit! source-path benign)
             (bootstrap/sh03-reader-plan-static-audit! source-path quoted)))
      (is (= (bootstrap/sh03-reader-plan-instruction-summary source-path (:functions benign))
             (bootstrap/sh03-reader-plan-instruction-summary source-path (:functions quoted))))
      (is (not= (bootstrap/reader-canonical-hash (:functions benign))
                (bootstrap/reader-canonical-hash (:functions quoted))))
      (is (not (contains? bootstrap/sh03-reader-allowed-builtins 'unreviewed-current-builtin-test)))
      (is (not (contains? functions 'sh03-unbound-plan-boundary-callee)))
      (doseq [[instruction missing]
              [[{:op :unreviewed-current-test} :exact-bounded-sh03-reader-instruction]
               [{:op :builtin-call :function 'unreviewed-current-builtin-test :args []}
                :approved-pure-sh03-reader-builtin]
               [{:op :function-call :function 'sh03-unbound-plan-boundary-callee :args []}
                :exact-sh03-reader-function-call]]]
        (let [failure (thrown-data #(bootstrap/sh03-reader-plan-static-audit!
                                    source-path (assoc-in plan instruction-path [instruction])))]
          (is (= "C2-HASH" (:id failure)))
          (is (= [missing] (:missing-fields failure))))))))

(deftest sh03-current-reader-selection-is-source-input-not-product-authority
  (let [source-path (fixture-path "accepted" "newline-lf" ".gravity")
        text (slurp source-path) context (bootstrap/reader-project-context-for-source source-path)
        reader-path (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
        callbacks (atom {:hash 0 :equal 0})
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks update :hash inc) 17)
                 (equals [_] (swap! callbacks update :equal inc) false))]
    (.hashCode opaque)
    (.equals opaque (Object.))
    (is (= {:hash 1 :equal 1} @callbacks))
    (reset! callbacks {:hash 0 :equal 0})
    (doseq [selected [opaque nil false 17 [] {} source-path (str reader-path \u0000)
                     (apply str (repeat 1025 "x"))]]
      (is (= "C2-HASH" (:id (thrown-data #(bootstrap/sh03-reader-current-binding!
                                          source-path selected))))))
    (is (= {:hash 0 :equal 0} @callbacks))
    (doseq [construct [bootstrap/compiler-c2-reader-source-artifact bootstrap/compiler-c3-syntax-source-artifact]]
      (is (= "C2-HASH" (:id (thrown-data #(construct source-path text context {:forged true})))))
      (is (= "C2-HASH" (:id (thrown-data #(construct source-path text context
                                                    {:forged true} nil reader-path))))))
    (let [rejected (fixture-path "rejected" "delimiter-mismatched" ".gravity")]
      (is (= "C2-DELIMITER" (:id (thrown-data #(bootstrap/compiler-c3-syntax-source-artifact
                                               rejected (slurp rejected)
                                               (bootstrap/reader-project-context-for-source rejected)
                                               nil nil reader-path))))))))

(defn- accepted-reader-products
  [result]
  (select-keys
   result
   [:status :token-stream :form-tree :top-level-form-ids
    :top-level-parsed-records :parsed-semantic-values
    :semantic-value-table
    :literal-decoding-records :semantic-error-deferment-record
    :reader-source-map :bounds :execution-boundary]))

(defn- diagnostic-parity-view
  [diagnostic]
  (select-keys
   diagnostic
   [:id :severity :stage :diagnostic-family :raw-spelling :facts
    :remapped-from :reader-engine-diagnostic :reader-state
   :remediation-records :redactions]))

(defn- records-by-tag
  [artifact]
  (into {} (map (juxt :tag identity)
                (:reader-extension-invocation-records artifact))))

(defn- ids-resolve?
  [records id-key products product-id-key]
  (let [ids (set (map product-id-key products))]
    (every? ids (keep id-key records))))

(defn- ascii-codepoints-text
  [codepoints]
  (apply str (map char codepoints)))

(defn- delete-tree!
  [root-path]
  (doseq [file (reverse (file-seq (.toFile root-path)))]
    (java.nio.file.Files/deleteIfExists (.toPath file))))

(defn- write-bytes!
  [target bytes]
  (java.nio.file.Files/createDirectories (.getParent target)
                                         (make-array
                                          java.nio.file.attribute.FileAttribute
                                          0))
  (java.nio.file.Files/write
   target bytes (make-array java.nio.file.OpenOption 0))
  target)

(defn- same-file?
  [target candidate]
  (when (or (string? candidate) (instance? java.io.File candidate))
    (try
      (= (.getCanonicalPath (java.io.File. (str target)))
         (.getCanonicalPath (io/file candidate)))
      (catch Exception _ false))))

(def ^:private target-reader-tripwire-symbols
  '[read-gravity-source-text
    read-source-form-records-host-oracle
    read-forms
    read-source-artifact
    stage1-reader-token-stream
    stage1-reader-products-from-token-stream
    stage1-reader-decode-atom
    stage1-reader-decode-string
    stage1-reader-decode-character
    p15-s23-stage2-front-end-read-source-form-records
    c2-reader-products-clojure-oracle
    c2-reader-products-legacy-oracle
    c2-reader-products-host-oracle])

(defn- tripwire-binding
  [target var]
  (let [original (var-get var)
        label (-> var meta :name)]
    (fn [& arguments]
      (if (same-file? target (first arguments))
        (throw
         (ex-info
          "A prohibited target-source reader boundary was invoked"
          {:id "SH03-TARGET-SOURCE-REREAD"
           :target target
           :boundary label}))
        (apply original arguments)))))

(defn- target-reader-tripwire-bindings
  [target]
  (let [bootstrap-bindings
        (into {}
              (keep
               (fn [symbol]
                 (when-let [var (ns-resolve 'gravity.bootstrap symbol)]
                   [var (tripwire-binding target var)])))
              target-reader-tripwire-symbols)
        slurp-var #'clojure.core/slurp]
    (assoc bootstrap-bindings slurp-var
           (tripwire-binding target slurp-var))))

(defn- with-target-reader-tripwires
  [target thunk]
  (with-redefs-fn (target-reader-tripwire-bindings target) thunk))

(deftest sh03-co-canonical-fixtures-have-accepted-and-rejected-parity
  (testing "the explicit rejection table covers the complete checked-in surface"
    (let [expected (set (keys rejected-fixtures))]
      (is (= expected (fixture-basenames "rejected" ".gravity")))
      (is (= expected (fixture-basenames "rejected" ".qst")))))
  (testing "accepted complete surface and physical newline forms"
    (let [basenames ["complete-reader-surface"
                     "depth-512"
                     "namespace-clause-shapes"
                     "newline-lf"
                     "newline-crlf"
                     "newline-cr"
                     "numeric-work-boundary"
                     "numeric-semantic-work-deferred"
                     "numeric-set-distinct-deferred"
                     "unicode-supplementary"]]
      (is (= (set basenames) (fixture-basenames "accepted" ".gravity")))
      (is (= (set basenames) (fixture-basenames "accepted" ".qst")))
      (doseq [basename basenames]
      (let [gravity-path (fixture-path "accepted" basename ".gravity")
            qst-path (fixture-path "accepted" basename ".qst")
            gravity (sh03-result gravity-path)
            qst (sh03-result qst-path)]
        (is (java.util.Arrays/equals (source-bytes gravity-path)
                                    (source-bytes qst-path)))
        (is (= :accepted (get-in gravity [:result :status])))
        (is (= :accepted (get-in qst [:result :status])))
        (is (= (accepted-reader-products (:result gravity))
               (accepted-reader-products (:result qst))))
        (is (= gravity-path
               (get-in gravity [:result :actual-path-provenance :path])))
        (is (= qst-path
               (get-in qst [:result :actual-path-provenance :path])))
        (is (= [".gravity" ".qst"]
               [(get-in gravity
                        [:result :actual-path-provenance :extension])
                (get-in qst
                        [:result :actual-path-provenance :extension])]))))))
  (testing "every structured rejection is co-canonical"
    (doseq [[basename expected-id] rejected-fixtures]
      (let [gravity-path (fixture-path "rejected" basename ".gravity")
            qst-path (fixture-path "rejected" basename ".qst")
            gravity (sh03-result gravity-path)
            qst (sh03-result qst-path)
            gravity-diagnostic (get-in gravity [:result :diagnostics 0])
            qst-diagnostic (get-in qst [:result :diagnostics 0])]
        (is (java.util.Arrays/equals (source-bytes gravity-path)
                                    (source-bytes qst-path)))
        (is (= :rejected (get-in gravity [:result :status])))
        (is (= :rejected (get-in qst [:result :status])))
        (is (= expected-id (:id gravity-diagnostic)))
        (is (= expected-id (:id qst-diagnostic)))
        (is (= (diagnostic-parity-view gravity-diagnostic)
               (diagnostic-parity-view qst-diagnostic)))
        (is (not (contains? gravity-diagnostic :diagnostic-id-request)))
        (is (boolean
             (re-matches #"sha256:[0-9a-f]{64}"
                         (:diagnostic-id gravity-diagnostic))))))))

(deftest sh03-public-c2-c3-and-p15-routing-preserves-both-extensions
  (let [artifacts
        (into {}
              (for [extension [".gravity" ".qst"]]
                (let [source-path
                      (fixture-path "accepted"
                                    "complete-reader-surface" extension)
                      text (utf8-text (source-bytes source-path))
                      c2 (bootstrap/compiler-c2-reader-file-artifact
                          source-path)
                      c3 (bootstrap/compiler-c3-syntax-file-artifact
                          source-path)
                      p15-c2
                      (bootstrap/p15-s23-source-syntax-c2-artifact
                       source-path text)
                      p15-c3
                      (bootstrap/p15-s23-source-syntax-c3-artifact
                       source-path p15-c2)]
                  [extension {:c2 c2 :c3 c3
                              :p15-c2 p15-c2 :p15-c3 p15-c3}])))]
    (doseq [[extension {:keys [c2 c3 p15-c2 p15-c3]}] artifacts]
      (is (= :gravity/stage0-c2-reader-document-artifact (:kind c2))
          extension)
      (is (= :gravity/stage0-c3-syntax-object-artifact (:kind c3))
          extension)
      (is (= :gravity/stage0-c2-reader-document-artifact (:kind p15-c2))
          extension)
      (is (= :gravity/stage0-c3-syntax-object-artifact (:kind p15-c3))
          extension)
      (is (true? (get-in c2
                         [:representation-boundary :lexical-token-stream?])))
      (is (true? (get-in c2
                         [:representation-boundary :nested-form-tree?])))
      (is (= :complete
             (get-in c2 [:representation-boundary
                         :sh03-bootstrap-subset-status])))
      (is (= #{:full-language-literal-surface
               :full-language-reader-abbreviation-surface
               :full-language-reader-extension-registry
               :host-and-seed-retirement}
             (set (get-in c2 [:representation-boundary
                              :remaining-reader-boundaries]))))
      (is (= :complete-bootstrap-subset
             (get-in c2 [:c2-reader-results :literal-status])))
      (is (= :complete-bootstrap-subset
             (get-in c2 [:c2-reader-results :abbreviation-status])))
      (is (= :complete-bootstrap-subset
             (get-in c2 [:c2-reader-results :extension-status])))
      (is (= :partial (get-in c2 [:c2-reader-results :status])))
      (is (= :passed
             (get-in c3 [:syntax-verification-report :status])))
      (is (= :passed
             (get-in p15-c3 [:syntax-verification-report :status])))
      (let [extensions (records-by-tag c2)
            forms (:form-tree c2)
            tokens (:token-stream c2)
            gravity-map (:gravity-reader-source-map c2)
            envelope (get-in c2 [:gravity-reader-boundary
                                 :authenticated-envelope])
            literals (:literal-decoding-records c2)
            deferred (get-in c2 [:semantic-error-deferment-record
                                 :deferred-literal-records])]
        (is (= #{'inst 'uuid} (set (keys extensions))) extension)
        (doseq [tag ['inst 'uuid]]
          (let [record (get extensions tag)]
            (is (= :invoked (:status record)) (str extension " " tag))
            (is (seq (:invocations record)) (str extension " " tag))
            (is (= #{} (:build-effects record) (:capabilities record)))
            (is (= #{:kernel :core :hosted :meta} (:profiles record)))
            (is (ids-resolve? (:invocations record) :form-id
                              forms :form-id))))
        (is (= literals (bootstrap/c2-literal-records forms)) extension)
        (is (= deferred
               (bootstrap/c2-deferred-semantic-literals forms)) extension)
        (is (some #(and (= :ratio (:kind %))
                        (= "1/0" (:raw %)))
                  deferred)
            extension)
        (is (= (mapv #(select-keys % [:token-id :span]) tokens)
               (:token-spans gravity-map))
            extension)
        (is (= (mapv #(select-keys % [:form-id :span :parent-form-id])
                     forms)
               (:form-spans gravity-map))
            extension)
        (is (ids-resolve? literals :form-id forms :form-id) extension)
        (is (ids-resolve? deferred :form-id forms :form-id) extension)
        (is (= false
               (get-in c2 [:gravity-reader-boundary
                           :target-source-reread?]))
            extension)
        (is (= :SH-03
               (get-in c2 [:gravity-reader-boundary :slice])) extension)
        (is (= {:status :not-executed
                :entrypoints
                bootstrap/sh03-reader-uncredited-source-model-entrypoints
                :self-hosting-credit? false
                :seed-retirement-credit? false
                :release-credit? false}
               (get-in c2 [:gravity-reader-boundary
                           :uncredited-source-models])) extension)
        (is (= bootstrap/p15-s23-sh02-stage-envelope-keys
               (set (keys envelope))) extension)
        (is (= :gravity/sh02-stage-authenticated-envelope
               (:artifact envelope)) extension)
        (is (= :accepted (:status envelope)) extension)
        (is (= :c2-reader (:stage envelope)) extension)
        (is (= :gravity/sh03-reader-products
               (get-in envelope [:sealed-artifact :artifact-kind])) extension)
        (is (= :gravity-source (:semantic-authority envelope)) extension)
        (is (= :template-replay-passed
               (get-in envelope [:gravity-template-replay :status])) extension)
        (is (= :pending-host-resolution
               (get-in envelope
                       [:gravity-template-replay
                        :identity-enforcement])) extension)
        (is (false?
             (get-in envelope
                     [:gravity-template-replay
                      :eligible-for-contextual-acceptance?])) extension)
        (is (pos? (:request-count envelope)) extension)
        (is (every? #(re-matches #"sha256:[0-9a-f]{64}" %)
                    [(:semantic-envelope-id envelope)
                     (:provenance-binding-id envelope)
                     (:request-graph-id envelope)]) extension)))
    (is (= (get-in artifacts [".gravity" :c2 :parsed-semantic-values])
           (get-in artifacts [".qst" :c2 :parsed-semantic-values])))
    (is (= (get-in artifacts [".gravity" :p15-c2
                              :parsed-semantic-values])
           (get-in artifacts [".qst" :p15-c2
                              :parsed-semantic-values]))))
  (testing "public rejected routes contain source failures"
    (doseq [basename ["malformed-numeric" "encoding-invalid-utf8"
                      "namespace-clause-unknown" "depth-513"
                      "set-equivalent-decimal-exponent"]
            extension [".gravity" ".qst"]]
      (let [source-path (fixture-path "rejected" basename extension)
            expected-id (if (= "L1-NS-SHAPE"
                               (get rejected-fixtures basename))
                          "C2-NS-SHAPE"
                          (get rejected-fixtures basename))]
        (doseq [[route invoke]
                [[:c2 #(bootstrap/compiler-c2-reader-file-artifact
                        source-path)]
                 [:c3 #(bootstrap/compiler-c3-syntax-file-artifact
                        source-path)]
                 [:check #(bootstrap/check-file-artifact source-path)]]]
          (let [data (thrown-data invoke)]
            (is (= expected-id (:id data)) (str route " " source-path))
            (is (nil? (:unexpected-host-error data))
                (str route " " source-path))))))))

(deftest sh03-extension-policy-covers-invoked-and-idle-handlers
  (doseq [extension [".gravity" ".qst"]]
    (let [complete-path
          (fixture-path "accepted" "complete-reader-surface" extension)
          newline-path
          (fixture-path "accepted" "newline-lf" extension)
          complete
          (bootstrap/compiler-c2-reader-file-artifact
           complete-path)
          newline
          (bootstrap/compiler-c2-reader-file-artifact
           newline-path)
          gravity-complete
          (get-in (sh03-result complete-path)
                  [:result :reader-extension-invocation-records])
          gravity-newline
          (get-in (sh03-result newline-path)
                  [:result :reader-extension-invocation-records])
          policy (:reader-extension-policy complete)
          complete-records (records-by-tag complete)
          newline-records (records-by-tag newline)]
      (is (= :registered (:status policy)) extension)
      (is (= ['inst 'uuid] (mapv :tag (:extensions policy))) extension)
      (is (= ["inst" "uuid"]
             (mapv #(ascii-codepoints-text (:tag-codepoints %))
                   gravity-complete)) extension)
      (is (= [:invoked :invoked]
             (mapv :status gravity-complete)) extension)
      (is (every? (comp seq :invocations) gravity-complete) extension)
      (is (= [:registered-not-invoked :registered-not-invoked]
             (mapv :status gravity-newline)) extension)
      (is (every? (comp empty? :invocations) gravity-newline) extension)
      (doseq [tag ['inst 'uuid]]
        (is (= :invoked (get-in complete-records [tag :status]))
            (str extension " " tag))
        (is (= :registered-not-invoked
               (get-in newline-records [tag :status]))
            (str extension " " tag))
        (is (= [] (get-in newline-records [tag :invocations]))
            (str extension " " tag))))))

(deftest sh03-resolved-result-binds-digests-and-rejects-replay-mutation
  (let [source-path
        (fixture-path "accepted" "newline-lf" ".gravity")
        execution (sh03-result source-path)
        raw (:raw-result execution)
        result (:result execution)
        report (:verification-report execution)
        requests (:resolved-requests execution)
        digests (:resolved-digests execution)]
    (is (= :accepted (:status raw) (:status result)))
    (is (= :accepted (:status report)))
    (is (true? (:verified? report)))
    (is (= [:source-content :source-unit :token-stream
            :form-tree :extension-invocation-set :reader-result]
           (mapv :key requests)))
    (is (= 6 (count digests)))
    (is (every? #(re-matches #"sha256:[0-9a-f]{64}" %) digests))
    (is (= (first digests) (:observed-id (first requests))))
    (is (= (second digests) (get-in result [:source-unit :source-id])))
    (is (= (nth digests 2)
           (get-in result [:incremental-reader-hashes :token-stream])))
    (is (= (nth digests 3)
           (get-in result [:incremental-reader-hashes :form-tree])))
    (is (= (nth digests 4)
           (get-in result
                   [:incremental-reader-hashes
                    :extension-invocation-set])))
    (is (= (nth digests 5)
           (get-in result [:incremental-reader-hashes :reader-result])
           (:reader-result-id execution)))
    (is (= :gravity-source
           (get-in execution [:plan-binding :semantic-authority])))
    (is (true? (get-in execution
                       [:plan-binding :generic-bridge-residual?])))
    (is (false? (get-in execution [:plan-binding :self-hosted?]))))
  (let [source-path
        (fixture-path "accepted" "newline-lf" ".gravity")
        original bootstrap/sh03-reader-execute-plan!
        changed? (atom false)
        data
        (with-redefs
          [bootstrap/sh03-reader-execute-plan!
           (fn [request-source plan function arguments]
             (let [value (original request-source plan function arguments)]
               (if (and (= function bootstrap/sh03-reader-entrypoint)
                        (compare-and-set! changed? false true))
                 (assoc-in value [:semantic-reader-template :status]
                           :rejected)
                 value)))]
          (thrown-data #(sh03-result source-path)))]
    (is (= "C2-HASH" (:id data)))
    (is (some #{:fresh-gravity-sh03-reader-result-replay}
              (:missing-fields data)))
    (is (nil? (:unexpected-host-error data)))))

(deftest sh03-digest-resolution-reuses-only-its-fresh-canonical-form
  (let [form-var (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-form-and-stats)
        hash-var (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-digest-validated-form)
        phase-var (ns-resolve 'gravity.bootstrap 'sh03-reader-resolve-digest-request-sequence!)
        original-form (var-get form-var) hash-form (var-get hash-var)
        original-phase (var-get phase-var)
        original-resolve bootstrap/p15-s23-c6c10-resolve-digest-references!]
    (is (true? (:private (meta hash-var))))
    (is (true? (:private (meta phase-var))))
    (is (not (contains? (ns-publics 'gravity.bootstrap) 'sh03-reader-resolve-digest-request-sequence!)))
    (is (contains? (ns-publics 'gravity.bootstrap) 'sh03-reader-resolve-digest-requests!))
    (doseq [[disposition basename status] [["accepted" "newline-lf" :accepted]
                                          ["rejected" "delimiter-mismatched" :rejected]]]
      (let [source (fixture-path disposition basename ".gravity") bytes (source-bytes source)
            execution (sh03-result source) raw (:raw-result execution)
            requests (:resolved-requests execution) nonraw (vec (rest requests))
            calls (atom {}) completions (atom []) phase-returns (atom []) phase-complete? (atom false)
            resolve-once (fn []
                           (with-redefs-fn
                             {form-var (fn [request-source value]
                                         (doseq [request nonraw :when (= value (:preimage request))]
                                           (swap! calls update (:key request) (fnil inc 0)))
                                         (original-form request-source value))
                              phase-var (fn [request-source input input-bytes]
                                          (let [result (original-phase request-source input input-bytes)]
                                            (swap! phase-returns conj result)
                                            (reset! phase-complete? true)
                                            result))
                              #'bootstrap/p15-s23-c6c10-resolve-digest-references!
                              (fn [request-source value count consumer digests]
                                (when (nil? consumer)
                                  (is (true? @phase-complete?))
                                  (swap! completions conj value))
                                (original-resolve request-source value count consumer digests))}
                             #(bootstrap/sh03-reader-resolve-digest-requests! source raw bytes)))
            first-result (resolve-once)]
        (is (= status (:status raw)))
        (is (= (if (= status :accepted)
                 bootstrap/sh03-reader-accepted-digest-request-names
                 bootstrap/sh03-reader-rejected-digest-request-names)
               (mapv :key requests)))
        (is (= (select-keys execution [:resolved-requests :resolved-digests :reader-result-id])
               (select-keys first-result [:resolved-requests :resolved-digests :reader-result-id])))
        (is (= (zipmap (map :key nonraw) (repeat 1)) @calls))
        (is (= [(assoc raw :digest-requests [])] @completions))
        (is (= [(select-keys first-result [:resolved-digests :resolved-requests])] @phase-returns))
        (let [legacy-result (original-resolve source raw (count requests) nil (:resolved-digests first-result))
              legacy-result (assoc legacy-result :digest-requests (:resolved-requests first-result)
                              :incremental-reader-hashes
                              (assoc (:incremental-reader-hashes legacy-result) :status
                                     (if (= status :accepted) :stable :rejected)))]
          (is (= legacy-result (:result first-result))))
        (doseq [request nonraw]
          (let [identity (bootstrap/sh03-reader-canonical-identity source (:preimage request))]
            (is (= (:digest request)
                   (bootstrap/sh03-reader-canonical-digest source (:preimage request))
                   (hash-form identity)))))
        (reset! calls {})
        (reset! completions [])
        (reset! phase-returns [])
        (reset! phase-complete? false)
        (is (= first-result (resolve-once)))
        (is (= (zipmap (map :key nonraw) (repeat 1)) @calls))
        (is (= [(assoc raw :digest-requests [])] @completions))
        (is (= [(select-keys first-result [:resolved-digests :resolved-requests])] @phase-returns))))))

(deftest sh03-fused-digest-resolution-preserves-rejection-controls
  (let [source (fixture-path "accepted" "newline-lf" ".gravity") bytes (source-bytes source)
        execution (sh03-result source) raw (:raw-result execution)
        request-one (get-in raw [:digest-requests 1])
        cases [[(update raw :digest-requests #(vec (reverse %))) :exact-sh03-reader-digest-request-set]
               [(assoc-in raw [:digest-requests 1 :ordinal] 2) :exact-sh03-reader-digest-request-schema]
               [(assoc-in raw [:digest-requests 1 :algorithm] :sha512) :exact-sh03-reader-digest-request-schema]
               [(update-in raw [:digest-requests 1] dissoc :preimage) :exact-sh03-reader-digest-request-schema]
               [(assoc-in raw [:digest-requests 1 :depends-on] [1]) :prior-only-sh03-reader-digest-dependencies]
               [(assoc-in raw [:digest-requests 1 :encoding] :raw-byte-vector-v1) :declared-sh03-reader-digest-encoding]
               [(assoc-in raw [:digest-requests 0 :preimage] [99]) :source-bound-sh03-reader-digest-request]
               [(assoc-in raw [:digest-requests 0 :observed-id] "sha256:forged") :unique-bound-sh03-reader-digest]
               [(-> raw (assoc-in [:digest-requests 2 :preimage] (:preimage request-one))
                        (assoc-in [:digest-requests 2 :depends-on] (:depends-on request-one)))
                :unique-bound-sh03-reader-digest]]]
    (doseq [[candidate missing] cases]
      (let [data (thrown-data #(bootstrap/sh03-reader-resolve-digest-requests! source candidate bytes))]
        (is (= "C2-HASH" (:id data)))
        (is (some #{missing} (:missing-fields data)))))
    (let [hash-var (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-digest-validated-form)
          original (var-get hash-var) calls (atom 0) previous (atom nil)
          boundary (var-get (ns-resolve 'gravity.bootstrap 'sh03-reader-boundary-fail!))
          data (with-redefs-fn
                 {hash-var (fn [form]
                             (let [result (original form)]
                               (if (= 2 (swap! calls inc)) @previous
                                 (do (reset! previous result) result))))}
                 #(thrown-data (fn [] (bootstrap/sh03-reader-resolve-digest-requests! source raw bytes))))]
      (is (= "C2-HASH" (:id data)))
      (is (some #{:unique-bound-sh03-reader-digest} (:missing-fields data)))
      (is (= (thrown-data #(boundary source :unique-bound-sh03-reader-digest
                                    (get-in raw [:digest-requests 2])
                                    {:ordinal 2 :digest @previous}))
             data)))
    (let [boundary (var-get (ns-resolve 'gravity.bootstrap 'sh03-reader-boundary-fail!))
          replace-pair
          (fn [left right]
            (-> raw
                (assoc-in [:digest-requests 1 :preimage] left)
                (assoc-in [:digest-requests 1 :depends-on] [])
                (assoc-in [:digest-requests 2 :preimage] right)
                (assoc-in [:digest-requests 2 :depends-on] [])))]
      (doseq [[left right] [[(array-map :a 1 :b 2) (array-map :b 2 :a 1)]
                            [(hash-set :a :b) (hash-set :b :a)]
                            [1 (bigint 1)]]]
        (let [candidate (replace-pair left right)
              digest (bootstrap/sh03-reader-canonical-digest source left)
              legacy-error
              (thrown-data #(boundary source :unique-bound-sh03-reader-digest
                                      (get-in candidate [:digest-requests 2])
                                      {:ordinal 2 :digest digest}))
              actual-error
              (thrown-data #(bootstrap/sh03-reader-resolve-digest-requests!
                             source candidate bytes))]
          (is (= (bootstrap/sh03-reader-canonical-identity source left)
                 (bootstrap/sh03-reader-canonical-identity source right)))
          (is (= legacy-error actual-error))))
      (doseq [[left right] [[(list 1) [1]] [1.0M 1.00M]]]
        (let [candidate (replace-pair left right)
              result (bootstrap/sh03-reader-resolve-digest-requests! source candidate bytes)]
          (is (not= (bootstrap/sh03-reader-canonical-identity source left)
                    (bootstrap/sh03-reader-canonical-identity source right)))
          (is (not= (get (:resolved-digests result) 1)
                    (get (:resolved-digests result) 2)))
          (is (= result (bootstrap/sh03-reader-resolve-digest-requests!
                         source candidate bytes))))))
    (let [hash-var (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-digest-validated-form)
          original (var-get hash-var) calls (atom [])
          candidate (-> raw
                        (assoc-in [:digest-requests 1 :preimage]
                                  (java.math.BigDecimal. java.math.BigInteger/ONE 65537))
                        (assoc-in [:digest-requests 1 :depends-on] []))
          data (with-redefs-fn
                 {hash-var (fn [form]
                             (swap! calls conj
                                    (if (some #{[:keyword "gravity" "c6-c10-upstream-diagnostic-id-v1"]}
                                              (tree-seq vector? seq form))
                                      :diagnostic :request))
                             (original form))}
                 #(thrown-data (fn [] (bootstrap/sh03-reader-resolve-digest-requests!
                                      source candidate bytes))))]
      (is (= "C6-VERIFY" (:id data)))
      (is (= :bounded-decimal-scale (:missing-fact data)))
      (is (= [:diagnostic] @calls)))))

(deftest sh03-completion-reuse-preserves-map-set-collisions-and-precedence
  (let [source (fixture-path "accepted" "newline-lf" ".gravity") bytes (source-bytes source)
        execution (sh03-result source) raw (:raw-result execution)
        digests (:resolved-digests execution) count (count digests)
        collision-map (array-map {:digest-ref 0} :reference (first digests) :literal)
        collision-set (hash-set {:digest-ref 0} (first digests))]
    (doseq [[collision missing] [[collision-map :collision-free-resolved-map-keys]
                                  [collision-set :collision-free-resolved-set-items]]]
      (let [inside (-> raw (assoc-in [:digest-requests 1 :preimage] collision)
                           (assoc-in [:digest-requests 1 :depends-on] [0]))
            outside (assoc-in raw [:semantic-reader-template :completion-collision] collision)
            old-inside (thrown-data #(bootstrap/p15-s23-c6c10-resolve-digest-references!
                                      source collision count 1 [(first digests)]))
            actual-inside (thrown-data #(bootstrap/sh03-reader-resolve-digest-requests! source inside bytes))
            old-outside (thrown-data #(bootstrap/p15-s23-c6c10-resolve-digest-references!
                                       source outside count nil digests))
            actual-outside (thrown-data #(bootstrap/sh03-reader-resolve-digest-requests! source outside bytes))]
        (is (= "C6-VERIFY" (:id old-inside) (:id old-outside)))
        (is (= missing (:missing-fact old-inside) (:missing-fact old-outside)))
        (is (= old-inside actual-inside))
        (is (= old-outside actual-outside))))
    (let [inside (-> raw (assoc-in [:digest-requests 1 :preimage] collision-map)
                         (assoc-in [:digest-requests 1 :depends-on] [0]))
          both (assoc-in inside [:semantic-reader-template :completion-collision] collision-set)
          inside-error (thrown-data #(bootstrap/sh03-reader-resolve-digest-requests! source inside bytes))
          both-error (thrown-data #(bootstrap/sh03-reader-resolve-digest-requests! source both bytes))]
      (is (= :collision-free-resolved-map-keys (:missing-fact both-error)))
      (is (= inside-error both-error)))))

(deftest sh03-gravity-facade-rejects-source-result-and-report-substitution
  (let [source-path
        (fixture-path "accepted" "complete-reader-surface" ".gravity")
        inputs (sh03-facade-inputs source-path)
        plan @sh03-facade-plan]
    (is (= 30 (count (:functions plan))))
    (is (= sh03-facade-plan-semantic-hash
           (bootstrap/p15-s23-c11-mir-digest
            (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input
             plan))))
    (is (= sh03-facade-functions-semantic-hash
           (bootstrap/p15-s23-c11-mir-digest (:functions plan))))
    (is (true?
         (sh03-facade-execute
          'l1-c2-reader-result-compatible? inputs)))
    (doseq [candidate (sh03-facade-substitutions inputs)]
      (is (false?
           (sh03-facade-execute
            'l1-c2-reader-result-compatible? candidate)) candidate))))

(deftest sh03-gravity-facade-product-count-bounds-are-executable
  (let [within?
        (fn [token-count form-count]
          (sh03-facade-execute
           'l1-c2-reader-product-counts-within-bounds?
           [token-count form-count]))]
    (is (true? (within? 65537 32769)))
    (is (true? (within? 131072 65536)))
    (is (false? (within? 131073 65536)))
    (is (false? (within? 131072 65537)))
    (is (false? (within? -1 0)))
    (is (false? (within? 0 -1)))
    (is (false? (within? "131072" 65536)))))

(deftest sh03-gravity-facade-package-boundary-fails-closed
  (let [accepted-path
        (fixture-path "accepted" "complete-reader-surface" ".qst")
        rejected-path
        (fixture-path "rejected" "malformed-numeric" ".qst")
        accepted-inputs (sh03-facade-inputs accepted-path)
        rejected-inputs (sh03-facade-inputs rejected-path)
        accepted
        (sh03-facade-execute 'l1-c2-package-reader-result accepted-inputs)
        rejected-source
        (sh03-facade-execute 'l1-c2-package-reader-result rejected-inputs)
        no-credit
        {:artifact :gravity/l1-c2-reader-compatibility-package
         :compatibility-status :compatible
         :compatibility-only? true
         :authentication-credit? false
         :authoritative-reader-result? false
         :authoritative-route :sh03-fresh-verifier-and-digest-resolution
         :fresh-verifier-executed-here? false
         :digest-resolution-executed-here? false}]
    (doseq [[expected-status package]
            [[:accepted accepted] [:rejected rejected-source]]]
      (is (= no-credit
             (select-keys package (keys no-credit))))
      (is (= expected-status (:reader-result-status package)))
      (is (map? (:reader-verification-report package))))
    (doseq [candidate (sh03-facade-substitutions accepted-inputs)]
      (let [package
            (sh03-facade-execute 'l1-c2-package-reader-result candidate)]
        (is (= :gravity/l1-c2-reader-compatibility-package
               (:artifact package)) candidate)
        (is (= :rejected (:compatibility-status package)) candidate)
        (is (true? (:compatibility-only? package)) candidate)
        (is (false? (:authentication-credit? package)) candidate)
        (is (false? (:authoritative-reader-result? package)) candidate)
        (is (nil? (:source-unit-record package)) candidate)
        (is (empty? (:token-stream package)) candidate)
        (is (empty? (:form-tree package)) candidate)
        (is (empty? (:semantic-value-table package)) candidate)
        (is (= "C2-HASH"
               (get-in package [:reader-diagnostics 0 :id])) candidate)
        (is (false?
             (get-in package [:execution-boundary :self-hosted?]))
            candidate)))))

(deftest sh03-precomputed-reader-and-c3-seams-require-opaque-authority
  (let [source-path
        (fixture-path "accepted" "newline-lf" ".gravity")
        bytes (source-bytes source-path)
        source-text (utf8-text bytes)
        project-context
        (bootstrap/reader-project-context-for-source source-path)
        legacy-products
        (bootstrap/c2-reader-products-clojure-oracle
         source-path source-text bootstrap/standard-reader-options
         project-context)
        public-c2-data
        (thrown-data
         #(bootstrap/compiler-c2-reader-source-artifact
           source-path source-text project-context legacy-products))
        resolved
        (bootstrap/sh03-reader-resolved-result!
         source-path bytes project-context bootstrap/standard-reader-options)
        products
        (bootstrap/sh03-reader-adapt-products!
         source-path source-text bytes bootstrap/standard-reader-options
         project-context resolved)
        authority
        (var-get
         (ns-resolve 'gravity.bootstrap
                     'sh03-reader-internal-product-authority))
        mutated-products
        (assoc products :sh03-reader-adapter-contract :substituted)
        private-c2-data
        (thrown-data
         #(bootstrap/compiler-c2-reader-source-artifact
           source-path source-text project-context mutated-products authority))
        payload-c2-data
        (thrown-data
         #(bootstrap/compiler-c2-reader-source-artifact
           source-path source-text project-context
           (assoc products :parsed-values [:substituted]) authority))
        raw-result-c2-data
        (thrown-data
         #(bootstrap/compiler-c2-reader-source-artifact
           source-path source-text project-context
           (assoc-in products
                     [:sh03-reader-raw-result :parsed-semantic-values 0]
                     {:artifact :gravity/semantic-value-reference
                      :value-id 999})
           authority))
        c2 (bootstrap/compiler-c2-reader-file-artifact source-path)
        public-c3-data
        (thrown-data
         #(bootstrap/compiler-c3-syntax-source-artifact
           source-path source-text project-context c2))
        private-c3-data
        (thrown-data
         #(bootstrap/compiler-c3-syntax-source-artifact
           source-path source-text project-context
           (assoc-in c2 [:gravity-reader-boundary :adapter-contract]
                     :substituted)
           authority))
        substituted-c2
        (let [candidate (assoc c2 :parsed-semantic-values [:substituted])]
          (assoc candidate :artifact-id
                 (bootstrap/c2-reader-artifact-id candidate)))
        payload-c3-data
        (thrown-data
         #(bootstrap/compiler-c3-syntax-source-artifact
           source-path source-text project-context substituted-c2 authority))]
    (doseq [data [public-c2-data private-c2-data payload-c2-data
                  raw-result-c2-data
                  public-c3-data private-c3-data]]
      (is (= "C2-HASH" (:id data)) data)
      (is (seq (:missing-fields data)) data)
      (is (nil? (:unexpected-host-error data)) data))
    (is (= "C3-FACT-STALE" (:id payload-c3-data)) payload-c3-data)
    (is (some #{:parsed-semantic-values-valid?}
              (:missing-fields payload-c3-data))
        payload-c3-data)
    (is (= :gravity/stage0-c2-reader-document-artifact (:kind c2)))
    (is (= :gravity/stage0-c3-syntax-object-artifact
           (:kind (bootstrap/compiler-c3-syntax-file-artifact source-path))))))

(deftest sh03-public-routing-does-not-invoke-target-source-reader-oracles
  (let [temporary
        (java.nio.file.Files/createTempFile
         "gravity-sh03-tripwire-" ".gravity"
         (make-array java.nio.file.attribute.FileAttribute 0))
        source-path (str temporary)
        text (str "(ns sh03.tripwire (:profile :hosted))\n"
                  "(def value ^:reader-owned [1 2 3])\n")]
    (try
      (java.nio.file.Files/write
       temporary (.getBytes text java.nio.charset.StandardCharsets/UTF_8)
       (make-array java.nio.file.OpenOption 0))
      (let [products
            (with-target-reader-tripwires
              source-path
              (fn []
                (let [low-level (sh03-result source-path)
                      c2 (bootstrap/compiler-c2-reader-file-artifact
                          source-path)
                      c3 (bootstrap/compiler-c3-syntax-file-artifact
                          source-path)
                      p15
                      (bootstrap/p15-s23-source-syntax-c2-artifact
                       source-path text)
                      checked (bootstrap/check-file-artifact source-path)]
                  {:low-level low-level :c2 c2 :c3 c3
                   :p15 p15 :checked checked})))]
        (is (= :accepted (get-in products [:low-level :result :status])))
        (is (= :gravity/stage0-c2-reader-document-artifact
               (get-in products [:c2 :kind])))
        (is (= :gravity/stage0-c3-syntax-object-artifact
               (get-in products [:c3 :kind])))
        (is (= :gravity/stage0-c2-reader-document-artifact
               (get-in products [:p15 :kind])))
        (is (map? (:checked products))))
      (finally
        (java.nio.file.Files/deleteIfExists temporary)))))

(deftest sh03-specialized-check-routes-reuse-authenticated-records
  (doseq [[relative expected-kind]
          [["bootstrap/clojure/fixtures/accepted/backend-test-matrix.gravity"
            :gravity/stage0-b14-backend-conformance-document-artifact]
           ["bootstrap/clojure/fixtures/accepted/core-semantics.gravity"
            :gravity/stage0-core-artifact]
           ["bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity"
            :gravity/stage0-runtime-selection-artifact]
           ["bootstrap/clojure/fixtures/accepted/runtime-managed-host.gravity"
            :gravity/stage0-managed-runtime-artifact]]]
    (let [source-path (path relative)
          artifact
          (with-target-reader-tripwires
            source-path #(bootstrap/check-file-artifact source-path))]
      (is (= expected-kind (:kind artifact)) relative)
      (is (map? artifact) relative))))

(deftest sh03-public-reader-accepts-its-own-gravity-source
  (let [source-path (path bootstrap/sh03-reader-source-relative-path)
        c2 (bootstrap/compiler-c2-reader-file-artifact source-path)]
    (is (= :gravity/stage0-c2-reader-document-artifact (:kind c2)))
    (is (= source-path (get-in c2 [:source-unit-record :path])))
    (is (= :SH-03 (get-in c2 [:gravity-reader-boundary :slice])))
    (is (= false
           (get-in c2 [:gravity-reader-boundary :target-source-reread?])))
    (is (seq (:token-stream c2)))
    (is (seq (:form-tree c2)))
    (is (= :complete-for-slice
           (get-in c2 [:representation-boundary :status])))
    (is (= :accepted
           (get-in c2 [:gravity-reader-boundary :authenticated-envelope
                       :status])))))

(deftest sh03-unicode-character-literals-cover-supplementary-scalars
  (let [temporary-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh03-unicode-scalar-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        scalar-codepoint 0x1f600
        scalar-text (String. (Character/toChars scalar-codepoint))
        accepted-text
        (str "(ns sh03.unicode-scalar (:profile :hosted))\n"
             "(def smile \\" scalar-text ")\n")
        rejected-text "\\uD800\n"]
    (try
      (doseq [extension [".gravity" ".qst"]]
        (let [accepted-path
              (str (write-bytes!
                    (.resolve temporary-root (str "accepted" extension))
                    (.getBytes accepted-text
                               java.nio.charset.StandardCharsets/UTF_8)))
              rejected-path
              (str (write-bytes!
                    (.resolve temporary-root (str "rejected" extension))
                    (.getBytes rejected-text
                               java.nio.charset.StandardCharsets/UTF_8)))
              low-level (sh03-result accepted-path)
              artifact
              (bootstrap/compiler-c2-reader-file-artifact accepted-path)
              host-value
              (some
               (fn [value]
                 (when (and (map? value)
                            (= :gravity/unicode-scalar-character
                               (:artifact value)))
                   value))
               (tree-seq coll? seq (:parsed-semantic-values artifact)))
              rejected
              (thrown-data
               #(bootstrap/compiler-c2-reader-file-artifact rejected-path))]
          (is (= :accepted (get-in low-level [:result :status])) extension)
          (is (some #(= scalar-codepoint
                        (get-in % [:descriptor :codepoint]))
                    (get-in low-level [:result :semantic-value-table]))
              extension)
          (is (= scalar-codepoint (:codepoint host-value)) extension)
          (is (= scalar-text (:text host-value)) extension)
          (is (= "C2-STRING" (:id rejected)) extension)
          (is (nil? (:unexpected-host-error rejected)) extension)))
      (finally
        (delete-tree! temporary-root)))))

(deftest sh03-numeric-set-equivalence-is-bounded-and-fail-closed
  (let [temporary-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh03-numeric-equivalence-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        huge-positive (str (apply str (repeat 257 "1")) ".0")
        huge-negative (str "-" huge-positive)
        huge-zero-left (str "0." (apply str (repeat 257 "0")))
        huge-zero-right (str "0." (apply str (repeat 258 "0")))
        nested-left (str (apply str (repeat 32 "[")) huge-zero-left
                         (apply str (repeat 32 "]")))
        nested-right (str (apply str (repeat 32 "[")) huge-zero-right
                          (apply str (repeat 32 "]")))
        cases
        [{:name "bounded-equivalent" :source "#{1.0 1.00}\n"
          :expected-id "C2-SET"}
         {:name "bounded-distinct" :source "#{1.0 2.0}\n"
          :accepted? true}
         {:name "deferred-ambiguous"
          :source (str "#{" huge-zero-left " " huge-zero-right "}\n")
          :expected-id "C2-HASH" :expected-reason
          :numeric-set-equivalence-limit}
         {:name "deferred-provably-distinct"
          :source (str "#{" huge-positive " " huge-negative "}\n")
          :accepted? true}
         {:name "deferred-standalone" :source (str huge-positive "\n")
          :accepted? true :deferred? true}
         {:name "deferred-nested-ambiguous"
          :source (str "#{" nested-left " " nested-right "}\n")
          :expected-id "C2-HASH" :expected-reason
          :numeric-set-equivalence-limit}]]
    (try
      (doseq [extension [".gravity" ".qst"]
              {:keys [name source accepted? deferred? expected-id
                      expected-reason]} cases]
        (let [source-path
              (str (write-bytes!
                    (.resolve temporary-root (str name extension))
                    (.getBytes source
                               java.nio.charset.StandardCharsets/UTF_8)))]
          (if accepted?
            (let [artifact
                  (bootstrap/compiler-c2-reader-file-artifact source-path)]
              (is (= :gravity/stage0-c2-reader-document-artifact
                     (:kind artifact)) (str extension " " name))
              (when deferred?
                (is (true? (get-in artifact
                                   [:semantic-error-deferment-record
                                    :deferred?]))
                    (str extension " " name))))
            (let [data
                  (thrown-data
                   #(bootstrap/compiler-c2-reader-file-artifact source-path))
                  low-level (when expected-reason (sh03-result source-path))
                  reason (get-in low-level
                                 [:result :diagnostics 0
                                  :reader-state :reason])]
              (is (= expected-id (:id data)) (str extension " " name))
              (when expected-reason
                (is (= expected-reason reason) (str extension " " name)))
              (is (nil? (:unexpected-host-error data))
                  (str extension " " name))))))
      (finally
        (delete-tree! temporary-root)))))

(deftest sh03-invalid-direct-inputs-have-compact-replayable-summaries
  (let [normal-path
        (fixture-path "accepted" "newline-lf" ".gravity")
        empty-bytes (byte-array 0)
        empty-vector []
        source-unit
        (bootstrap/sh03-reader-input-source-unit
         normal-path empty-bytes
         (bootstrap/reader-project-context-for-source normal-path))
        policy (bootstrap/sh03-reader-input-policy
                bootstrap/standard-reader-options)
        giant-inner-tag (vec (repeat 100000 105))
        giant-outer-tags (vec (repeat 100000 [105 110 115 116]))
        oversized-bytes (vec (repeat 1048577 0))
        cases
        [[:long-logical-id
          (assoc source-unit :logical-source-id (apply str (repeat 1025 "a")))
          empty-vector policy]
         [:long-path
          (assoc-in source-unit [:actual-path-provenance :path]
                    (apply str (repeat 1025 "p")))
          empty-vector policy]
         [:mismatched-bytes source-unit [0] policy]
         [:oversized-bytes source-unit oversized-bytes policy]
         [:outer-tag-count source-unit empty-vector
          (assoc policy :enabled-reader-tags giant-outer-tags)]
         [:inner-tag-width source-unit empty-vector
          (assoc policy :enabled-reader-tags [giant-inner-tag])]
         [:duplicate-tags source-unit empty-vector
          (assoc policy :enabled-reader-tags
                 [[105 110 115 116] [105 110 115 116]])]]]
    (doseq [[label unit bytes reader-policy] cases]
      (let [result (direct-sh03-result unit bytes reader-policy)
            report (direct-sh03-verification
                    unit bytes reader-policy result)
            carrier
            (bootstrap/p15-s23-trusted-carrier-validation
             result :default-only
             bootstrap/sh03-reader-result-maximum-nodes
             bootstrap/sh03-reader-result-maximum-depth
             bootstrap/sh03-reader-result-maximum-width)]
        (is (= :rejected (:status result)) (name label))
        (is (= [] (get-in result [:digest-requests 0 :preimage]))
            (name label))
        (is (= :passed (:status carrier)) (name label))
        (is (< (:observed-nodes carrier) 1024) (name label))
        (is (true? (:verified? report)) (name label))
        (is (= :accepted (:status report)) (name label))))))

(deftest sh03-result-carrier-depth-is-bounded-with-structured-failure
  (let [small-validation
        (bootstrap/sh03-reader-require-result-carrier!
         "carrier.gravity" :test-carrier {:value [1 2 3]})
        over-depth
        (loop [remaining (inc bootstrap/sh03-reader-result-maximum-depth)
               value nil]
          (if (zero? remaining)
            value
            (recur (dec remaining) [value])))
        data
        (thrown-data
         #(bootstrap/sh03-reader-require-result-carrier!
           "carrier.gravity" :test-carrier over-depth))]
    (is (= :passed (:status small-validation)))
    (is (= "C2-HASH" (:id data)))
    (is (some #{:trusted-bounded-sh03-reader-result}
              (:missing-fields data)))
    (is (= :test-carrier (get-in data [:facts :carrier])))
    (is (integer? (get-in data [:facts :observed-depth])))
    (is (= bootstrap/sh03-reader-result-maximum-depth
           (get-in data [:facts :maximum-depth])))
    (is (nil? (:unexpected-host-error data)))))

(deftest sh03-source-identity-input-is-bounded-before-reader-execution
  (let [normal-path
        (fixture-path "accepted" "newline-lf" ".gravity")
        oversized-path
        (str (apply str
                    (repeat
                     (inc bootstrap/sh03-reader-input-maximum-identity-code-units)
                     "a"))
             ".gravity")
        data
        (thrown-data
         #(bootstrap/sh03-reader-input-source-unit
           oversized-path
           (byte-array 0)
           (bootstrap/reader-project-context-for-source normal-path)))]
    (is (= "C2-HASH" (:id data)))
    (is (some #{:bounded-sh03-reader-source-identity}
              (:missing-fields data)))
    (is (= bootstrap/sh03-reader-input-maximum-identity-code-units
           (get-in data [:facts :maximum-code-units])))
    (is (= bootstrap/sh03-reader-input-maximum-identity-utf8-bytes
           (get-in data [:facts :maximum-utf8-bytes])))
    (is (not (.contains (pr-str data) oversized-path)))
    (is (nil? (:unexpected-host-error data)))))

(deftest sh03-identities-are-cross-root-neutral-with-actual-path-provenance
  (let [left-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh03-left-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        right-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh03-right-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        manifest-bytes
        (.getBytes "{:paths [\"src\"]}\n"
                   java.nio.charset.StandardCharsets/UTF_8)
        source
        (str "(ns sh03.same-relative (:profile :hosted))\n"
             "(def value ^:stable 1/0)\n")
        bytes (.getBytes source java.nio.charset.StandardCharsets/UTF_8)
        left-source (.resolve left-root "src/same.gravity")
        right-source (.resolve right-root "src/same.gravity")]
    (try
      (write-bytes! (.resolve left-root "deps.edn") manifest-bytes)
      (write-bytes! (.resolve right-root "deps.edn") manifest-bytes)
      (write-bytes! left-source bytes)
      (write-bytes! right-source bytes)
      (let [left-path (str left-source)
            right-path (str right-source)
            left (sh03-result left-path)
            right (sh03-result right-path)
            left-result (:result left)
            right-result (:result right)
            left-c2 (bootstrap/compiler-c2-reader-file-artifact left-path)
            right-c2 (bootstrap/compiler-c2-reader-file-artifact right-path)]
        (is (not= left-path right-path))
        (is (= left-path
               (get-in left-result [:actual-path-provenance :path])))
        (is (= right-path
               (get-in right-result [:actual-path-provenance :path])))
        (is (= (dissoc left-result :actual-path-provenance)
               (dissoc right-result :actual-path-provenance)))
        (is (= (:reader-result-id left) (:reader-result-id right)))
        (is (= (get-in left-c2 [:source-unit-record :source-id])
               (get-in right-c2 [:source-unit-record :source-id])))
        (is (= (:incremental-reader-hashes left-c2)
               (:incremental-reader-hashes right-c2)))
        (is (= (:artifact-id left-c2) (:artifact-id right-c2)))
        (is (= left-path (get-in left-c2 [:source-unit-record :path])))
        (is (= right-path (get-in right-c2 [:source-unit-record :path]))))
      (finally
        (delete-tree! left-root)
        (delete-tree! right-root)))))

(deftest sh03-reader-policy-is-bound-into-resolved-and-adapted-identity
  (let [source-path
        (fixture-path "accepted" "complete-reader-surface" ".gravity")
        source-text (utf8-text (source-bytes source-path))
        without-trivia (assoc bootstrap/standard-reader-options
                              :retain-comments false)
        standard (sh03-result-with-options
                  source-path bootstrap/standard-reader-options)
        compact (sh03-result-with-options source-path without-trivia)
        context (bootstrap/reader-project-context-for-source source-path)
        standard-products
        (bootstrap/c2-reader-products
         source-path source-text bootstrap/standard-reader-options context)
        compact-products
        (bootstrap/c2-reader-products
         source-path source-text without-trivia context)]
    (is (= :accepted (get-in standard [:result :status])))
    (is (= :accepted (get-in compact [:result :status])))
    (is (not= (:reader-result-id standard) (:reader-result-id compact)))
    (is (not= (get-in standard [:result :incremental-reader-hashes])
              (get-in compact [:result :incremental-reader-hashes])))
    (is (true? (get-in standard [:result :source-unit
                                 :reader-options :retain-trivia])))
    (is (false? (get-in compact [:result :source-unit
                                 :reader-options :retain-trivia])))
    (is (some :trivia? (get-in standard [:result :token-stream])))
    (is (not-any? :trivia? (get-in compact [:result :token-stream])))
    (is (not= (get-in standard-products [:source-unit :source-id])
              (get-in compact-products [:source-unit :source-id])))
    (is (not= (:token-stream standard-products)
              (:token-stream compact-products)))
    (is (= (get-in standard [:result :actual-path-provenance])
           (get-in compact [:result :actual-path-provenance])))))

(deftest sh03-public-reader-accepts-the-exact-depth-limit-deterministically
  (let [temporary-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh03-depth-limit-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        source-text
        (str "(ns sh03.depth-limit (:profile :hosted))\n"
             (apply str (repeat 512 "["))
             "0"
             (apply str (repeat 512 "]"))
             "\n")
        source-bytes
        (.getBytes source-text java.nio.charset.StandardCharsets/UTF_8)]
    (try
      (doseq [extension [".gravity" ".qst"]]
        (let [source-path
              (str (write-bytes!
                    (.resolve temporary-root (str "depth-512" extension))
                    source-bytes))
              first-low-level (sh03-result source-path)
              second-low-level (sh03-result source-path)
              first-artifact
              (bootstrap/compiler-c2-reader-file-artifact source-path)
              second-artifact
              (bootstrap/compiler-c2-reader-file-artifact source-path)]
          (is (= :accepted (get-in first-low-level [:result :status]))
              extension)
          (is (= 512 (get-in first-low-level
                             [:result :bounds :maximum-delimiter-depth]))
              extension)
          (is (= (:reader-result-id first-low-level)
                 (:reader-result-id second-low-level)) extension)
          (is (= :gravity/stage0-c2-reader-document-artifact
                 (:kind first-artifact)) extension)
          (is (= :passed
                 (get-in first-artifact
                         [:lexical-product-validation :status])) extension)
          ;; The atomic leaf is one graph level below the 512 collection
          ;; levels; the reader resource bound itself counts delimiters.
          (is (= 513
                 (get-in first-artifact
                         [:lexical-product-validation :max-form-depth]))
              extension)
          (is (= (:artifact-id first-artifact)
                 (:artifact-id second-artifact)) extension)
          (is (= (:incremental-reader-hashes first-artifact)
                 (:incremental-reader-hashes second-artifact)) extension)))
      (doseq [extension [".gravity" ".qst"]]
        (let [data
              (thrown-data
               #(bootstrap/compiler-c2-reader-file-artifact
                 (fixture-path "rejected" "depth-513" extension)))]
          (is (= "C2-HASH" (:id data)) extension)
          (is (nil? (:unexpected-host-error data)) extension)))
      (finally
        (delete-tree! temporary-root)))))

(deftest sh03-public-c2-reader-scales-to-five-thousand-top-level-forms
  (let [temporary
        (java.nio.file.Files/createTempFile
         "gravity-sh03-scale-" ".gravity"
         (make-array java.nio.file.attribute.FileAttribute 0))
        source-path (str temporary)
        source-text
        (str "(ns sh03.scale (:profile :hosted))\n"
             (apply str (repeat 4999 "0\n")))]
    (try
      (java.nio.file.Files/write
       temporary
       (.getBytes source-text java.nio.charset.StandardCharsets/UTF_8)
       (make-array java.nio.file.OpenOption 0))
      (let [first-artifact
            (bootstrap/compiler-c2-reader-file-artifact source-path)
            second-artifact
            (bootstrap/compiler-c2-reader-file-artifact source-path)]
        (is (= :gravity/stage0-c2-reader-document-artifact
               (:kind first-artifact)))
        (is (= 5000 (count (:top-level-form-ids first-artifact))))
        (is (= 5000 (count (:parsed-semantic-values first-artifact))))
        (is (= :complete-for-slice
               (get-in first-artifact [:representation-boundary :status])))
        (is (true? (get-in first-artifact
                           [:lexical-product-validation :graph-valid?])))
        (is (= (:artifact-id first-artifact)
               (:artifact-id second-artifact)))
        (is (= (:incremental-reader-hashes first-artifact)
               (:incremental-reader-hashes second-artifact))))
      (finally
        (java.nio.file.Files/deleteIfExists temporary)))))

(deftest sh03-host-boundary-failures-are-structured-and-contained
  (doseq [[label error-factory expected-field]
          [[:stack #(StackOverflowError.) :bounded-sh03-reader-host-stack]
           [:assertion #(AssertionError. "assertion")
            :contained-sh03-reader-assertion]
           [:linkage #(LinkageError. "linkage")
            :contained-sh03-reader-linkage]
           [:diagnostic #(ex-info "inner" {:id "INNER"})
            :contained-sh03-reader-runtime-diagnostic]
           [:exception #(RuntimeException. "runtime")
            :contained-sh03-reader-host-failure]]]
    (let [data
          (with-redefs
            [bootstrap/p15-s23-stage2-runtime-execute-function
             (fn [& _] (throw (error-factory)))]
            (thrown-data
             #(bootstrap/sh03-reader-execute-plan!
               "contained.gravity" {} 'sh03-read-source-unit [])))]
      (is (= "C2-HASH" (:id data)) (name label))
      (is (some #{expected-field} (:missing-fields data)) (name label))
      (is (nil? (:unexpected-host-error data)) (name label)))))
