
(defn- run-m-session! [options codec]
  (when (some #(str/starts-with?
                (str %) "gravity.tools.sh07-b51-attempt30-")
              (loaded-libs))
    (reject! :m-checker-loaded))
  (require (symbol bootstrap-namespace))
  (require 'gravity.tools.sh07-b51-attempt32-checker-identity)
  (let [builder
        (resolve-public!
         bootstrap-namespace
         "p15-s23-sh07-b51-build-receipted-bundle-core-products")
        content (:content options)
        products (builder (:commit content) (:tree content))
        receipt (:source-read-receipt products)
        source-carrier-valid?
        (resolve-public!
         bootstrap-namespace
         "p15-s23-sh07-b51-source-semantic-carrier-valid?")]
    (when-not
     (and (exact-keys? products product-keys)
          (exact-keys? receipt source-receipt-keys)
          (= :m (:role receipt))
          (= content (select-keys receipt [:commit :tree]))
          (= {:acquire-count 13 :decode-count 13
              :event-count 40 :finalize-count 1}
             (:counters receipt))
          (source-carrier-valid?
           content receipt (:module-transactions products)
           (:source-semantic-carrier products))
          (true? (:finalized receipt)))
      (reject! :m-source-session))
    {:products products
     :source-read-receipt receipt
     :source-session-id (source-session-id (:H codec) receipt)}))

(defn- current-process-handle-id [options codec]
  (let [handle (ProcessHandle/current)
        info (.info handle)
        command (optional-value (.command info))
        arguments (some-> (optional-value (.arguments info)) vec)
        argv (when (and command arguments) (into [command] arguments))
        argv-id
        (when argv
          ((:H codec) [:gravity/sh07-b51-process-argv-id-v1 argv]))]
    (when-not argv-id (reject! :m-process))
    ((:H codec)
     [:gravity/sh07-b51-process-handle-coordinate-id-v1
      {:role :m :child-sequence :m
       :normalized-argv-id argv-id :request-id nil}])))

(defn run-process-topology!
  "Execute the production two-child topology using bounded asynchronous IPC."
  [options codec]
  (let [operations
        {:m-process-handle-id (current-process-handle-id options codec)
         :build-child #(build-child options codec %)
         :start-child #(start-child! % codec)
         :join-child #(join-child! % codec)
         :validate-child #(validate-child! % codec)
         :m-session #(run-m-session! options codec)}]
    (coordinate! operations (:H codec))))

(defn- payload-values [topology]
  (let [a-response (:response (:a topology))
        b-response (:response (:b topology))
        a-result (:result a-response)
        b-result (:result b-response)
        products (get-in topology [:m-session :products])
        pair [(:certificate (:checker-output a-result))
              (:certificate (:checker-output b-result))]
        pair-valid?
        (resolve-public!
         bootstrap-namespace
         "p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?")
        carrier-builder
        (resolve-public!
         bootstrap-namespace
         "p15-s23-sh07-b51-build-linked-carrier-v4-candidate")
        carrier-valid?
        (resolve-public!
         bootstrap-namespace
         "p15-s23-sh07-b51-linked-carrier-v4-valid?")
        _ (when-not (pair-valid? pair)
            (reject! :certificate-pair))
        carrier (carrier-builder products pair)
        _ (when-not (carrier-valid? carrier)
            (reject! :linked-carrier))]
    {:checker-a-identity.edn (:checker-identity a-result)
     :checker-b-identity.edn (:checker-identity b-result)
     :checker-a-output.edn (:checker-output a-result)
     :checker-b-output.edn (:checker-output b-result)
     :certificate-pair.edn pair
     :linked-carrier-v4-candidate.edn carrier}))

(defn- stage-payloads!
  [options codec topology]
  (let [root (Paths/get (:staging-root options) (make-array String 0))
        values (payload-values topology)]
    (when-not (and (Files/isDirectory root (make-array LinkOption 0))
                   (not (Files/isSymbolicLink root)))
      (reject! :staging-root {:path (str root)}))
    (mapv
     (fn [ordinal [name-key kind]]
       (let [name (name name-key)
             value (get values name-key)
             bytes ((:bounded-wire-bytes codec) :response value)
             path (.resolve root name)]
         (Files/write path bytes
                      (into-array StandardOpenOption
                                  [StandardOpenOption/CREATE_NEW
                                   StandardOpenOption/WRITE]))
         {:ordinal ordinal :name name :kind kind
          :relative-path (str (:evidence-relative-root options) "/" name)
          :wire-codec :gravity/sh07-b51-edn-wire-v1
          :byte-count (alength ^bytes bytes)
          :raw-sha256 ((:R codec) bytes)
          :value-id
          ((:H codec)
           [:gravity/sh07-b51-staged-payload-value-v1
            {:ordinal ordinal :name name :kind kind :value value}])}))
     (range)
     [[:checker-a-identity.edn :checker-identity]
      [:checker-b-identity.edn :checker-identity]
      [:checker-a-output.edn :checker-output]
      [:checker-b-output.edn :checker-output]
      [:certificate-pair.edn :certificate-pair]
      [:linked-carrier-v4-candidate.edn :linked-carrier]])))

(defn- child-process-receipt [codec child triple]
  (let [response (:response child)
        result (:result response)
        base
        {:schema :gravity/sh07-b51-child-process-receipt-v1
         :role (:role child)
         :ordinal-triple triple
         :argv (:argv child)
         :argv-id (:argv-id child)
         :request
         {:value (:request child)
          :request-id (:request-id child)
          :byte-count (alength ^bytes (:request-bytes child))
          :raw-sha256 ((:R codec) (:request-bytes child))}
         :response
         {:transport (:transport response)
          :reconstruction
          {:schema (:schema response) :role (:role response)
           :checker-identity-payload
           (get-in child-role-specs [(:role child) :identity-name])
           :checker-output-payload
           (get-in child-role-specs [(:role child) :output-name])
           :source-read-receipt (:source-read-receipt result)
           :production (:production result) :sentinel (:sentinel result)
           :diagnostics (:diagnostics response)}
          :byte-count (:response-byte-count child)
          :raw-sha256 (:response-raw-sha256 child)
          :response-preimage-id (:response-preimage-id child)}
         :process
         (assoc (:process-start child)
                :lifecycle
                {:domain :coordinator
                 :builder-ordinal (first triple)
                 :start-ordinal (second triple)
                 :exit-ordinal (nth triple 2)}
                :exit-code (:exit-code child))
         :process-join
         {:self-and-retained-pid-equal true
          :self-and-retained-parent-pid-equal true
          :self-and-retained-command-equal true
          :self-and-retained-arguments-equal true
          :self-and-retained-start-instant-equal true
          :mxbean-pid-equal true :mxbean-host-equal true}
         :counters (:counters response)
         :tcb (:tcb response)
         :self-id-omits [[:receipt-id]]}
        receipt-id
        ((:H codec)
         [:gravity/sh07-b51-child-process-receipt-v1 base])]
    (assoc base :receipt-id receipt-id)))

(defn- project-library? [namespace]
  (str/starts-with? (str namespace) "gravity."))

(defn- source-record-index [source-authority]
  (reduce
   (fn [result record]
     (if (or (str/ends-with? (:path record) ".clj")
             (str/ends-with? (:path record) ".cljc"))
       (if-let [namespace
                (some-> (source-namespace-core! (:bytes record) (:path record))
                        :namespace)]
         (do
           (when (contains? result namespace)
             (reject! :loaded-namespace-source-duplicate
                      {:namespace namespace}))
           (assoc result namespace record))
         result)
       result))
   {} (vals (:source-objects source-authority))))

(defn- loaded-project-source-records [options projects]
  (let [source-authority (:source-authority options)
        index (source-record-index source-authority)
        coordinate
        {:artifact 'project/bootstrap-clojure-src
         :commit (get-in options [:content :commit])
         :tree (get-in options [:content :tree])}]
    (mapv
     (fn [namespace]
       (let [record (get index namespace)]
         (when-not record
           (reject! :loaded-namespace-source-missing
                    {:namespace namespace}))
         {:namespace namespace :logical-coordinate coordinate
          :relative-path (:path record) :blob-oid (:blob-oid record)
          :byte-count (:byte-count record) :sha256 (:raw-sha256 record)}))
     projects)))
