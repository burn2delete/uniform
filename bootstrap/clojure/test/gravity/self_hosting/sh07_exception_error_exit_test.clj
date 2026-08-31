(ns gravity.self-hosting.sh07-exception-error-exit-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_exception_error_exit_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-07 B50 test source is not on the classpath"
                {:id "SH07-B50-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "SH-07 B50 repository root was not found"
                  {:id "SH07-B50-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        (.toAbsolutePath (.normalize candidate))

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07/b50-exception-error-exit")
(def ^:private extensions [".gravity" ".qst"])

(defrecord UnsupportedCarrierRecord [value])

(defn- path [relative]
  (str (.normalize (.toAbsolutePath (.resolve @root relative)))))

(defn- fixture-path [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- source-bytes [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- compile-plan [relative]
  (let [source-path (path relative)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(defn- read-gravity-source-forms [relative]
  (with-open [reader
              (java.io.PushbackReader.
               (io/reader (path relative)))]
    (loop [forms []]
      (let [form (read {:eof ::eof} reader)]
        (if (= ::eof form)
          forms
          (recur (conj forms form)))))))

(defn- gravity-defn-form [forms function-name]
  (some
   (fn [form]
     (when (and (seq? form)
                (contains? #{'defn 'defn-} (first form))
                (= function-name (second form)))
       form))
   forms))

(defn- source-form-symbols [form]
  (set (filter symbol? (tree-seq coll? seq form))))

(def ^:private c6-plan
  (delay
    (compile-plan
     "bootstrap/gravity/src/gravity/compiler/c6_core_lowering_engine.gravity")))

(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh07-authenticated-exception-error-exit
    :compiler-artifact-plan? true}
   @c6-plan function arguments))

(defn- source-unit [b47]
  (get-in b47
          [:sh06-resolution-artifact :sh05-macro-artifact
           :gravity-macro-boundary :authenticated-sh04-artifact
           :c2-reader-artifact :source-unit-record]))

(defn- immutable-post-reader-edn!
  [source-path value]
  (let [node-count (volatile! 0)
        scalar-bytes (volatile! 0)]
    (letfn [(reject! [reason current]
              (throw
               (ex-info
                "B50 carrier is outside the bounded immutable post-reader EDN domain"
                {:id "SH07-B50-POST-READER-DOMAIN"
                 :artifact :gravity/sh07-core-diagnostic
                 :rule "C6-VERIFY"
                 :reason reason
                 :fail-closed true
                 :source-path source-path
                 :value-class (some-> current class .getName)})))
            (convert [current depth]
              (vswap! node-count inc)
              (when (> @node-count 8388608)
                (reject! :carrier-node-bound current))
              (when (> depth 256)
                (reject! :carrier-depth-bound current))
              (let [class-name (some-> current class .getName)]
                (cond
                (or
                 (contains? #{"clojure.lang.PersistentArrayMap"
                              "clojure.lang.PersistentHashMap"}
                            class-name)
                 (and
                  (= "clojure.lang.PersistentTreeMap" class-name)
                  (= "clojure.lang.RT$DefaultComparator"
                     (some-> current .comparator class .getName))))
                (do
                  (when (> (* 2 (count current)) 65536)
                    (reject! :carrier-width-bound current))
                  (when (> (+ @node-count (* 2 (count current))) 8388608)
                    (reject! :carrier-node-bound current))
                  (into {}
                        (map (fn [[key child]]
                               [(convert key (inc depth))
                                (convert child (inc depth))]))
                        current))

                (contains? #{"clojure.lang.PersistentVector"
                             "clojure.lang.APersistentVector$SubVector"}
                           class-name)
                (do
                  (when (> (count current) 65536)
                    (reject! :carrier-width-bound current))
                  (when (> (+ @node-count (count current)) 8388608)
                    (reject! :carrier-node-bound current))
                  (mapv #(convert % (inc depth)) current))

                (or
                 (= "clojure.lang.PersistentHashSet" class-name)
                 (and
                  (= "clojure.lang.PersistentTreeSet" class-name)
                  (= "clojure.lang.RT$DefaultComparator"
                     (some-> current .comparator class .getName))))
                (do
                  (when (> (count current) 65536)
                    (reject! :carrier-width-bound current))
                  (when (> (+ @node-count (count current)) 8388608)
                    (reject! :carrier-node-bound current))
                  (into #{} (map #(convert % (inc depth))) current))

                (contains? #{"clojure.lang.PersistentList"
                             "clojure.lang.PersistentList$EmptyList"}
                           class-name)
                (do
                  (when (> (count current) 65536)
                    (reject! :carrier-width-bound current))
                  (when (> (+ @node-count (count current)) 8388608)
                    (reject! :carrier-node-bound current))
                  (apply list (map #(convert % (inc depth)) current)))

                (symbol? current)
                (let [next-scalar-bytes
                      (+ @scalar-bytes (* 4 (count (str current))))]
                  (when (> next-scalar-bytes 268435456)
                    (reject! :carrier-scalar-byte-bound current))
                  (vreset! scalar-bytes next-scalar-bytes)
                  (with-meta current nil))

                (or (nil? current) (boolean? current) (integer? current)
                    (string? current) (keyword? current))
                (let [next-scalar-bytes
                      (+ @scalar-bytes (* 4 (count (str current))))]
                  (when (> next-scalar-bytes 268435456)
                    (reject! :carrier-scalar-byte-bound current))
                  (vreset! scalar-bytes next-scalar-bytes)
                  current)

                :else
                (reject! :carrier-value-domain-invalid current))))]
      (convert value 0))))

(defn- snapshot-and-carrier
  ([source-path] (snapshot-and-carrier source-path nil))
  ([source-path read-count]
   (let [canonical-path
         (str (.normalize (.toAbsolutePath (.toPath (io/file source-path)))))
         bytes (bootstrap/sh03-reader-read-target-source-bytes! canonical-path)
         _ (when read-count (swap! read-count inc))
         text
         (bootstrap/sh03-reader-strict-source-text!
          canonical-path canonical-path bytes)
         immutable-bytes (mapv #(bit-and 255 (int %)) bytes)
         bytes-hash (str "sha256:" (bootstrap/sha256-bytes-hex bytes))
         project (bootstrap/reader-project-context-for-source canonical-path)
         b47 (bootstrap/sh07-core-source-artifact canonical-path text)
         sh06 (:sh06-resolution-artifact b47)
         unit (source-unit b47)
         manifest-path (str (io/file (:project-root-path project) "deps.edn"))
         manifest-bytes (source-bytes manifest-path)
         snapshot
         {:artifact :gravity/sh07-source-snapshot
          :schema-version 1
          :canonical-path canonical-path
          :project-root-path (:project-root-path project)
          :project-relative-path (:project-relative-path project)
          :source-extension (bootstrap/gravity-source-extension canonical-path)
          :source-kind (bootstrap/gravity-source-kind canonical-path)
          :byte-count (count immutable-bytes)
          :maximum-source-bytes 1048576
         :bytes immutable-bytes
         :bytes-hash bytes-hash
         :text text
          :encoding :utf-8
          :single-read? true
          :nofollow-final-component? true
          :bytes-hash-domain :c2-source-bytes
          :bytes-hash-algorithm :sha-256
          :bytes-hash-encoding :sha256-lowercase-hex}
         root-evidence
         {:root-path (:project-root-path project)
          :manifest-relative-path "deps.edn"
          :manifest-byte-count (alength manifest-bytes)
         :manifest-bytes-hash
         (str "sha256:" (bootstrap/sha256-bytes-hex manifest-bytes))
          :project-root-id (:project-root-id project)}
         membership
         {:normalized-root-path (:project-root-path project)
          :normalized-relative-path (:project-relative-path project)
          :normalized-source-path canonical-path
          :source-extension (:source-extension snapshot)
          :source-kind (:source-kind snapshot)
          :source-id (:source-id unit)
          :source-bytes-hash (:bytes-hash unit)
          :snapshot-byte-count (:byte-count snapshot)
          :snapshot-bytes-hash (:bytes-hash snapshot)
          :project-root-id (:project-root-id project)}
         carrier
         {:artifact :gravity/sh07-c6-authenticated-exception-carrier
          :schema-version 1
          :transport
          {:adapter-contract :gravity/sh07-c6-c6-entrypoint-v1
           :entrypoint :c6-sh07-authenticated-exception-entrypoint
           :source-entrypoint :sh07-core-source-artifact
           :target-reread? false}
          :source-snapshot snapshot
          :project-root-evidence root-evidence
          :physical-membership membership
          :b47-artifact b47
          :sh06-resolution-artifact sh06
          :sh06-verification-report
          (bootstrap/sh06-resolution-artifact-verification sh06)
          :b47-verification-report
          (bootstrap/sh07-core-artifact-verification b47)}
         checked-carrier
         (immutable-post-reader-edn! canonical-path carrier)]
     {:snapshot snapshot :carrier checked-carrier})))

(defn- entry-request [phase carrier template requests digests]
  {:artifact :gravity/sh07-c6-entry-request
   :schema-version 1
   :phase phase
   :carrier carrier
   :template template
   :digest-requests requests
   :resolved-digests digests})

(defn- raw-call-phase [phase carrier template requests digests]
  (invoke 'c6-sh07-authenticated-exception-entrypoint
          [(entry-request phase carrier template requests digests)]))

(defn- call-phase [phase carrier template requests digests]
  (let [request
        (immutable-post-reader-edn!
         nil (entry-request phase carrier template requests digests))
        source-path (get-in request [:carrier :source-snapshot :canonical-path])
        result
        (invoke 'c6-sh07-authenticated-exception-entrypoint [request])]
    (immutable-post-reader-edn! source-path result)))

(def ^:private maximum-source-bytes 1048576)

(defn- resolver-raw-u8-bytes! [source-path values]
  (when-not (and (vector? values)
                 (<= (count values) maximum-source-bytes))
    (throw
     (ex-info "B50 raw digest preimage is outside the bounded byte domain"
              {:id "SH07-B50-RAW-BYTE-DOMAIN"
               :source-path source-path})))
  (let [result (byte-array (count values))]
    (doseq [index (range (count values))]
      (let [value (get values index)]
        (when-not (and (integer? value) (<= 0 value 255))
          (throw
           (ex-info "B50 raw digest preimage is outside the u8 domain"
                    {:id "SH07-B50-RAW-BYTE-DOMAIN"
                     :source-path source-path
                     :byte-index index})))
        (aset-byte result index (unchecked-byte (int value)))))
    result))

(defn- resolver-strict-utf8-bytes! [source-path text]
  (when-not (and (string? text)
                 (<= (.length ^String text) maximum-source-bytes))
    (throw
     (ex-info "B50 text digest preimage is outside the bounded text domain"
              {:id "SH07-B50-UTF8-ENCODING"
               :source-path source-path})))
  (try
    (let [encoder
          (doto (.newEncoder java.nio.charset.StandardCharsets/UTF_8)
            (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
            (.onUnmappableCharacter java.nio.charset.CodingErrorAction/REPORT))
          buffer (.encode encoder (java.nio.CharBuffer/wrap ^String text))
          byte-count (.remaining buffer)]
      (when (> byte-count maximum-source-bytes)
        (throw
         (ex-info "B50 UTF-8 digest preimage exceeds its byte bound"
                  {:id "SH07-B50-UTF8-ENCODING"
                   :source-path source-path
                   :byte-count byte-count})))
      (let [result (byte-array byte-count)]
        (.get buffer result)
        result))
    (catch java.nio.charset.CharacterCodingException _
      (throw
       (ex-info "B50 text digest preimage is not strict UTF-8 encodable"
                {:id "SH07-B50-UTF8-ENCODING"
                 :source-path source-path})))))

(defn- resolve-digests [source-path requests]
  (loop [remaining requests resolved []]
    (if (empty? remaining)
      resolved
      (let [request (first remaining)
            ordinal (:ordinal request)
            _ (when-not (= ordinal (count resolved))
                (throw
                 (ex-info "B50 digest requests are not ordered"
                          {:id "SH07-B50-DIGEST-ORDER"
                           :request request
                           :resolved-count (count resolved)})))
            preimage
            (if (< ordinal 7)
              (:preimage request)
              (bootstrap/p15-s23-c6c10-resolve-digest-references!
               source-path (:preimage request) (count requests)
               ordinal (mapv :digest resolved)))
            digest
            (cond
              (= (:purpose request) :source-snapshot-raw-bytes)
              (str "sha256:"
                   (bootstrap/sha256-bytes-hex
                    (resolver-raw-u8-bytes! source-path preimage)))

              (= (:purpose request) :source-snapshot-text-utf8)
              (str "sha256:"
                   (bootstrap/sha256-bytes-hex
                    (resolver-strict-utf8-bytes! source-path preimage)))

              (= (:purpose request) :sh07-core-artifact-id)
              (bootstrap/reader-canonical-hash
               {:domain :gravity/sh07-declared-digest-v1
                :purpose (:purpose request)
                :preimage preimage})

              (= (:purpose request) :project-root-id)
              (bootstrap/reader-canonical-hash preimage)

              :else
              (bootstrap/reader-canonical-hash
               {:domain :gravity/sh07-c6-declared-digest-v1
                :purpose (:purpose request)
                :preimage preimage}))]
        (recur
         (rest remaining)
         (conj resolved
               {:ordinal ordinal
                :purpose (:purpose request)
                :preimage (:preimage request)
                :digest digest}))))))

(defn- independently-resolve-digests [source-path requests]
  (let [expected-purposes
        [:source-snapshot-raw-bytes
         :source-snapshot-text-utf8
         :sh07-core-artifact-id
         :project-root-id
         :membership-binding
         :projection-binding
         :semantic-artifact
         :provenance-binding
         :verifier-report
         :final-artifact]]
    (when-not (and (vector? requests)
                   (= 10 (count requests))
                   (= expected-purposes (mapv :purpose requests)))
      (throw
       (ex-info "B50 independent digest replay request shape is invalid"
                {:id "SH07-B50-INDEPENDENT-DIGEST-REQUESTS"
                 :source-path source-path})))
    (loop [remaining requests resolved []]
      (if (empty? remaining)
        resolved
        (let [request (first remaining)
              ordinal (:ordinal request)
              _ (when-not (and (= #{:ordinal :purpose :preimage}
                                  (set (keys request)))
                               (= ordinal (count resolved)))
                  (throw
                   (ex-info
                    "B50 independent digest replay is not exactly ordered"
                    {:id "SH07-B50-INDEPENDENT-DIGEST-ORDER"
                     :source-path source-path
                     :request request
                     :resolved-count (count resolved)})))
              preimage
              (if (< ordinal 7)
                (:preimage request)
                (bootstrap/p15-s23-c6c10-resolve-digest-references!
                 source-path (:preimage request) (count requests)
                 ordinal (mapv :digest resolved)))
              digest
              (cond
                (= (:purpose request) :source-snapshot-raw-bytes)
                (str "sha256:"
                     (bootstrap/sha256-bytes-hex
                      (resolver-raw-u8-bytes! source-path preimage)))

                (= (:purpose request) :source-snapshot-text-utf8)
                (str "sha256:"
                     (bootstrap/sha256-bytes-hex
                      (resolver-strict-utf8-bytes! source-path preimage)))

                (= (:purpose request) :sh07-core-artifact-id)
                (bootstrap/reader-canonical-hash
                 {:domain :gravity/sh07-declared-digest-v1
                  :purpose (:purpose request)
                  :preimage preimage})

                (= (:purpose request) :project-root-id)
                (bootstrap/reader-canonical-hash preimage)

                :else
                (bootstrap/reader-canonical-hash
                 {:domain :gravity/sh07-c6-declared-digest-v1
                  :purpose (:purpose request)
                  :preimage preimage}))]
          (recur
           (rest remaining)
           (conj resolved
                 {:ordinal ordinal
                  :purpose (:purpose request)
                  :preimage (:preimage request)
                  :digest digest})))))))

(defn- execute-carrier [carrier]
  ;; This is the governed bootstrap adapter boundary.  Callers supply only the
  ;; authenticated carrier; the adapter owns both opaque resolver passes and
  ;; only the independently replayed verification status is authoritative.
  (let [carrier (immutable-post-reader-edn! nil carrier)
        source-path (get-in carrier [:source-snapshot :canonical-path])
        admitted (call-phase :admit carrier nil [] [])
        template (:template admitted)
        requests (:digest-requests admitted)
        template-verification
        (call-phase :verify-template carrier template requests [])
        digests (resolve-digests source-path requests)
        resolved (call-phase :resolve carrier template requests digests)
        verification-digests
        (independently-resolve-digests source-path requests)
        verified
        (call-phase
         :verify-resolved carrier (:template resolved) requests
         verification-digests)]
    {:status (:status verified)
     :admitted admitted
     :template-verification template-verification
     :digests digests
     :verification-digests verification-digests
     :resolved resolved
     :verified verified}))

(def ^:private accepted-carriers
  (delay
    (into {}
          (for [extension extensions]
            [extension
             (:carrier
              (snapshot-and-carrier
               (fixture-path "accepted" "exception-error-exit"
                             extension)))]))))

(def ^:private declared-import-carriers
  (delay
    (into {}
          (for [extension extensions]
            [extension
             (:carrier
              (snapshot-and-carrier
               (fixture-path "accepted" "declared-import-alias"
                             extension)))]))))

(defn- first-diagnostic [result]
  (first (:diagnostics result)))

(defn- delete-tree! [root-path]
  (when (java.nio.file.Files/exists
         root-path (make-array java.nio.file.LinkOption 0))
    (with-open [stream
                (java.nio.file.Files/walk
                 root-path (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))

(defn- recursively-rewrite-key [value target-key replacement]
  (cond
    (map? value)
    (into {}
          (map (fn [[key child]]
                 [key (if (= key target-key)
                        replacement
                        (recursively-rewrite-key
                         child target-key replacement))]))
          value)

    (vector? value)
    (mapv #(recursively-rewrite-key % target-key replacement) value)

    (set? value)
    (into #{} (map #(recursively-rewrite-key
                     % target-key replacement)) value)

    (list? value)
    (apply list
           (mapv #(recursively-rewrite-key
                   % target-key replacement) value))

    :else value))

(defn- replace-nested-sh05-report [carrier transform]
  (let [sh06-report
        (update (:sh06-verification-report carrier)
                :upstream-verification transform)]
    (-> carrier
        (assoc :sh06-verification-report sh06-report)
        (assoc-in [:b47-verification-report :upstream-verification]
                  sh06-report))))

(deftest sh07-b50-fixtures-are-real-byte-paired-sources
  (doseq [[family basename]
          [["accepted" "exception-error-exit"]
           ["accepted" "declared-import-alias"]
           ["rejected" "ambiguous-exception-error-exit"]
           ["rejected" "unauthorized-cross-fragment"]]]
    (let [gravity (fixture-path family basename ".gravity")
          qst (fixture-path family basename ".qst")]
      (is (.isFile (io/file gravity)))
      (is (.isFile (io/file qst)))
      (is (= (vec (source-bytes gravity))
             (vec (source-bytes qst))))
      (is (pos? (alength (source-bytes gravity)))))))

(deftest sh07-b50-wrapper-reads-target-once-and-retains-exact-artifacts
  (let [source-path
        (fixture-path "accepted" "exception-error-exit" ".gravity")
        reads (atom 0)
        built (snapshot-and-carrier source-path reads)
        carrier (:carrier built)]
    (is (= 1 @reads))
    (is (true? (get-in carrier [:source-snapshot :single-read?])))
    (is (false? (get-in carrier [:transport :target-reread?])))
    (is (vector? (get-in carrier [:source-snapshot :bytes])))
    (is (= (get-in carrier [:source-snapshot :bytes])
           (mapv #(bit-and 255 (int %))
                 (.getBytes
                  ^String (get-in carrier [:source-snapshot :text])
                  java.nio.charset.StandardCharsets/UTF_8))))
    (is (= (get carrier :sh06-resolution-artifact)
           (get-in carrier [:b47-artifact :sh06-resolution-artifact])))
    (is (= :passed (get-in carrier [:sh06-verification-report :status])))
    (is (= :passed (get-in carrier [:b47-verification-report :status])))
    (is (= (get-in carrier [:source-snapshot :bytes-hash])
           (get-in carrier
                   [:physical-membership :source-bytes-hash])))))

(deftest sh07-b50-accepted-source-retains-two-three-and-four-byte-scalars
  (doseq [extension extensions]
    (let [carrier (get @accepted-carriers extension)
          text (get-in carrier [:source-snapshot :text])
          bytes (get-in carrier [:source-snapshot :bytes])]
      (is (.contains ^String text "¢"))
      (is (.contains ^String text "€"))
      (is (.contains ^String text "🙂"))
      (is (some #{[194 162]} (partition 2 1 bytes)))
      (is (some #{[226 130 172]} (partition 3 1 bytes)))
      (is (some #{[240 159 153 130]} (partition 4 1 bytes))))))

(deftest sh07-b50-production-and-independent-utf8-grammars-are-complete
  (let [validators
        ['c6-sh07-exception-utf8-bytes-valid?
         'c6-sh07-independent-exception-strict-utf8?]
        accepted
        [[] [0] [127] [194 128] [223 191]
         [224 160 128] [237 159 191]
         [240 144 128 128] [244 143 191 191]]
        rejected
        [[128] [192 128] [193 191] [194] [194 65]
         [224 159 128] [224 160] [226 130]
         [237 160 128] [240 143 128 128]
         [240 144 128] [244 144 128 128]
         [245 128 128 128] [255] [65 -1] [65 256] [65 :byte]]]
    (doseq [validator validators
            bytes accepted]
      (is (true? (invoke validator [bytes])) [validator bytes]))
    (doseq [validator validators
            bytes rejected]
      (is (false? (invoke validator [bytes])) [validator bytes]))))

(deftest sh07-b50-strict-text-encoding-failure-is-contained
  (let [request {:ordinal 0
                 :purpose :source-snapshot-text-utf8
                 :preimage (str (char 0xd800))}
        result
        (try
          {:value (resolve-digests "<invalid-utf8-text>" [request])}
          (catch clojure.lang.ExceptionInfo exception
            {:exception exception})
          (catch Throwable throwable
            {:raw-host-error throwable}))]
    (is (nil? (:value result)))
    (is (nil? (:raw-host-error result)))
    (is (= "SH07-B50-UTF8-ENCODING"
           (:id (ex-data (:exception result)))))))

(deftest sh07-b50-text-byte-roundtrip-is-bound-by-resolved-digests
  (let [carrier (get @accepted-carriers ".gravity")
        altered (assoc-in carrier [:source-snapshot :text]
                          "forged-but-strictly-encodable-text")
        admitted (call-phase :admit altered nil [] [])
        requests (:digest-requests admitted)
        digests
        (resolve-digests
         (get-in altered [:source-snapshot :canonical-path]) requests)
        resolved
        (call-phase :resolve altered (:template admitted) requests digests)
        diagnostic (first-diagnostic resolved)]
    (is (= :accepted (:status admitted)))
    (is (= (get-in altered [:source-snapshot :bytes])
           (get-in requests [0 :preimage])))
    (is (= (get-in altered [:source-snapshot :text])
           (get-in requests [1 :preimage])))
    (is (not= (get-in digests [0 :digest])
              (get-in digests [1 :digest])))
    (is (= :rejected (:status resolved)))
    (is (= "C6-VERIFY" (:rule diagnostic)))
    (is (= :snapshot-strict-utf8-reencode-mismatch
           (get-in diagnostic [:facts :reason])))))

(deftest sh07-b50-gravity-authenticates-lowers-executes-and-verifies
  (let [runs
        (into {}
              (for [extension extensions]
                [extension (execute-carrier (get @accepted-carriers extension))]))]
    (doseq [[extension run] runs]
      (testing extension
        (is (= :passed (:status run)))
        (is (= :accepted (get-in run [:admitted :status])))
        (is (= :passed (get-in run [:template-verification :status])))
        (is (= 10 (count (:digests run))))
        (is (= [:source-snapshot-raw-bytes
                :source-snapshot-text-utf8
                :sh07-core-artifact-id :project-root-id
                :membership-binding :projection-binding
                :semantic-artifact :provenance-binding
                :verifier-report :final-artifact]
               (mapv :purpose (:digests run))))
        (is (= (:digests run) (:verification-digests run)))
        (is (= :accepted (get-in run [:resolved :status])))
        (is (= :passed (get-in run [:verified :status])))
        (is (= :passed
               (get-in run
                       [:verified :independent-verifier-report :status])))
        (is (= (get-in (get @accepted-carriers extension)
                       [:project-root-evidence :project-root-id])
               (get-in run
                       [:resolved :template :bindings :project-root-id])))
        (is (= (get-in (get @accepted-carriers extension)
                       [:source-snapshot :bytes-hash])
               (get-in run [:resolved :template :bindings
                            :source-raw-bytes-hash])
               (get-in run [:resolved :template :bindings
                            :source-text-utf8-hash])))
        (is (= 2
               (count
                (get-in (get @accepted-carriers extension)
                        [:b47-artifact :gravity-core-boundary
                         :authenticated-core-request :fragment-manifest]))))
        (is (= 6
               (get-in run
                       [:verified :independent-verifier-report :facts
                        :authenticated-external-edge-count])))
        (is (false?
             (get-in run
                     [:verified :independent-verifier-report :facts
                      :extra-throw?])))
        (is (= [:evaluate-protected :evaluate-thrown-value
                :transfer-error :bind-handler
                :evaluate-handler :return-handler]
               (get-in run
                       [:resolved :template :canonical-core-artifact
                        :execution-record :trace])))
        (is (= :fixture/failure
               (get-in run
                       [:resolved :template :canonical-core-artifact
                        :execution-record :result])))
        (is (= {:arity :fixed-zero
                :mutation :absent
                :recursion :absent
                :exception :one-typed-nonresumable-catch
                :pattern :absent
                :declared-effects [:error/throw]
                :capabilities []
                :profile :meta
                :target :jvm
                :unsafe-facts {:safety :safe}}
               (get-in run
                       [:resolved :template :semantic-obligations])))))
    (is (= (get-in runs [".gravity" :resolved :template
                         :bindings :semantic-artifact-id])
           (get-in runs [".qst" :resolved :template
                         :bindings :semantic-artifact-id])))
    (is (not=
         (get-in runs [".gravity" :resolved :template
                       :bindings :membership-binding-id])
         (get-in runs [".qst" :resolved :template
                       :bindings :membership-binding-id])))))

(deftest sh07-b50-declared-import-alias-is-authentically-admitted
  (is (= (vec (source-bytes
               (fixture-path "accepted" "declared-import-alias" ".gravity")))
         (vec (source-bytes
               (fixture-path "accepted" "declared-import-alias" ".qst")))))
  (doseq [extension extensions]
    (let [carrier
          (:carrier
           (snapshot-and-carrier
            (fixture-path "accepted" "declared-import-alias" extension)))
          admitted (call-phase :admit carrier nil [] [])
          template (:template admitted)
          selected (:selected template)
          forms (:raw-forms selected)
          definition-form
          (some #(when (= (:syntax-id %)
                          (get-in selected [:definition :syntax-id])) %)
                forms)
          function-form
          (some #(when (= (:syntax-id %)
                          (get-in selected [:function :function-syntax-id])) %)
                forms)
          try-form
          (some #(when (= (:syntax-id %)
                          (get-in selected [:handler :syntax-id])) %)
                forms)
          external-edges
          (filterv #(= :authenticated-external (:classification %))
                   (:accepted-resolution-edges selected))
          core-names
          (set (map #(get-in % [:binding :name])
                    (filter #(= :core
                                (get-in % [:binding :binding-class]))
                            external-edges)))
          import-edges
          (filterv #(= :import (get-in % [:binding :binding-class]))
                   external-edges)
          alias-records
          (get-in carrier [:b47-artifact :gravity-core-boundary
                           :authenticated-core-request :alias-table])
          requests (:digest-requests admitted)
          digests (resolve-digests
                   (get-in carrier [:source-snapshot :canonical-path])
                   requests)
          resolved (call-phase :resolve carrier template requests digests)
          substituted
          (assoc-in carrier
                    [:b47-artifact :gravity-core-boundary
                     :authenticated-core-request :alias-table 0 :alias]
                    'substituted)
          substitution-result (call-phase :admit substituted nil [] [])]
      (testing extension
        (is (= :passed (get-in carrier
                               [:sh06-verification-report :status])))
        (is (= :passed (get-in carrier
                               [:b47-verification-report :status])))
        (is (= :accepted (:status admitted)))
        (is (not (contains? carrier :membership-binding-id)))
        (is (not (contains? (:physical-membership carrier)
                            :membership-binding-id)))
        (is (= (:physical-membership carrier)
               (get-in template
                       [:identity-preimages :membership
                        :physical-membership])))
        (is (= :project-root-id
               (get-in template [:digest-requests 3 :purpose])))
        (is (= :membership-binding
               (get-in template [:digest-requests 4 :purpose])))
        (is (= 'handle-import-error
               (get-in selected [:definition-binding :name])))
        (is (nil? (:parent-form-id definition-form)))
        (is (= (:form-id definition-form)
               (:parent-form-id function-form)))
        (is (= (:form-id function-form) (:parent-form-id try-form)))
        (is (every? core-names '[def fn try throw catch Exception]))
        (is (= #{'checksum}
               (set (map #(get-in % [:binding :name]) import-edges))))
        (is (= #{'shared/checksum}
               (set (map #(get-in % [:resolution :symbol]) import-edges))))
        (is (= #{:alias-qualified-required-binding}
               (set (map #(get-in % [:resolution :resolution-order])
                         import-edges))))
        (is (= [{:alias 'shared
                 :namespace 'shared.core
                 :kind :namespace
                 :profile :core
                 :targets [:jvm]
                 :dependency-artifact-id
                 (get-in import-edges
                         [0 :binding :definition-artifact-id])}]
               alias-records))
        (is (= :rejected (:status resolved)))
        (is (= "C6-LOWERING-GAP" (:rule (first-diagnostic resolved))))
        (is (= :non-literal-thrown-value-execution-outside-slice
               (get-in (first-diagnostic resolved) [:facts :reason])))
        (is (nil? (:template resolved)))
        (is (= :rejected (:status substitution-result)))
        (is (= "C6-VERIFY"
               (:rule (first-diagnostic substitution-result))))
        (is (nil? (:template substitution-result)))))))

(deftest sh07-b50-deterministic-repeat-and-alternate-cwd-are-stable
  (let [carrier (get @accepted-carriers ".gravity")
        first-run (execute-carrier carrier)
        second-run (execute-carrier carrier)
        prior (System/getProperty "user.dir")
        alternate
        (try
          (System/setProperty "user.dir" (str (io/file prior "target")))
          (execute-carrier carrier)
          (finally
            (System/setProperty "user.dir" prior)))]
    (is (= (:digests first-run) (:digests second-run)))
    (is (= (:digests first-run) (:digests alternate)))
    (is (= (:template (:resolved first-run))
           (:template (:resolved second-run))))
    (is (= :passed (get-in alternate [:verified :status])))))

(deftest sh07-b50-fresh-alternate-root-preserves-semantic-identity
  (let [temporary-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh07-b50-alternate-root-"
         (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [source-dir (.resolve temporary-root "src")
            target (.resolve source-dir "exception-error-exit.qst")
            source
            (fixture-path "accepted" "exception-error-exit" ".gravity")]
        (java.nio.file.Files/createDirectories
         source-dir (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/copy
         (.toPath (io/file (path "deps.edn")))
         (.resolve temporary-root "deps.edn")
         (into-array java.nio.file.CopyOption
                     [java.nio.file.StandardCopyOption/REPLACE_EXISTING]))
        (java.nio.file.Files/copy
         (.toPath (io/file source)) target
         (into-array java.nio.file.CopyOption
                     [java.nio.file.StandardCopyOption/REPLACE_EXISTING]))
        (let [base (execute-carrier (get @accepted-carriers ".gravity"))
              alternate (execute-carrier
                         (:carrier
                          (snapshot-and-carrier
                           (str
                            (.toRealPath
                             target
                             (make-array java.nio.file.LinkOption 0))))))]
          (is (= :passed (get-in alternate [:verified :status]))
              (pr-str
               {:resolved-status (get-in alternate [:resolved :status])
                :resolved-diagnostic
                (get-in alternate [:resolved :diagnostics 0])
                :verified-diagnostic
                (get-in alternate [:verified :diagnostics 0])}))
          (is (= (get-in base [:resolved :template :bindings
                               :semantic-artifact-id])
                 (get-in alternate [:resolved :template :bindings
                                    :semantic-artifact-id])))
          (is (= (get-in base [:resolved :template :bindings
                               :final-artifact-id])
                 (get-in alternate [:resolved :template :bindings
                                    :final-artifact-id])))
          (is (not= (get-in base [:resolved :template :bindings
                                  :membership-binding-id])
                    (get-in alternate [:resolved :template :bindings
                                       :membership-binding-id])))
          (is (not= (get-in base [:resolved :template :bindings
                                  :provenance-binding-id])
                    (get-in alternate [:resolved :template :bindings
                                       :provenance-binding-id])))))
      (finally
        (delete-tree! temporary-root)))))

(deftest sh07-b50-authentic-ambiguous-sources-reject-before-resolution
  (doseq [extension extensions]
    (let [carrier
          (:carrier
           (snapshot-and-carrier
            (fixture-path "rejected" "ambiguous-exception-error-exit"
                          extension)))
          result (call-phase :admit carrier nil [] [])
          diagnostic (first-diagnostic result)]
      (is (= :rejected (:status result)))
      (is (= "SH07-C6-EXCEPTION-ERROR-EXIT"
             (:diagnostic-id diagnostic)))
      (is (= "C6-CORE-SHAPE" (:rule diagnostic)))
      (is (= :unique-owning-top-level-definition-required
             (get-in diagnostic [:facts :reason])))
      (is (true? (get-in diagnostic [:facts :fail-closed]))))))

(deftest sh07-b50-authentic-cross-fragment-edges-are-not-authority
  (doseq [extension extensions]
    (let [carrier
          (:carrier
           (snapshot-and-carrier
            (fixture-path "rejected" "unauthorized-cross-fragment"
                          extension)))
          result (call-phase :admit carrier nil [] [])
          diagnostic (first-diagnostic result)]
      (is (= :rejected (:status result)))
      (is (= "C6-VERIFY" (:rule diagnostic)))
      (is (= :edge-authority-invalid
             (get-in diagnostic [:facts :reason])))
      (is (string? (get-in diagnostic [:source-span :source])))
      (is (true? (get-in diagnostic [:facts :fail-closed]))))))

(deftest sh07-b50-lineage-membership-edge-and-domain-mutations-fail-closed
  (let [carrier (get @accepted-carriers ".gravity")
        request-path
        [:b47-artifact :gravity-core-boundary
         :authenticated-core-request]
        forms (get-in carrier (conj request-path :forms))
        origin-form-index
        (first
         (keep-indexed
          (fn [index form]
            (when (seq (:generated-origin form)) index))
          forms))
        retained-origin
        (first (get-in forms [origin-form-index :generated-origin]))
        zero-id (str "sha256:" (apply str (repeat 64 "0")))
        probes
        [{:name :snapshot-hash
          :carrier (assoc-in carrier [:source-snapshot :bytes-hash] zero-id)
          :rule "C6-VERIFY"}
         {:name :coordinated-snapshot-bytes-text-hash
          :carrier
          (-> carrier
              (assoc-in [:source-snapshot :bytes 0] 41)
              (assoc-in [:source-snapshot :text]
                        (str ")" (subs (get-in carrier
                                               [:source-snapshot :text]) 1)))
              (assoc-in [:source-snapshot :bytes-hash] zero-id))
          :rule "C6-VERIFY"}
         {:name :project-root-extra-field
          :carrier (assoc-in carrier
                             [:project-root-evidence
                              :project-root-id-input :bytes-hash]
                             zero-id)
          :rule "C6-VERIFY"}
         {:name :physical-root
          :carrier (assoc-in carrier
                             [:physical-membership :normalized-root-path]
                             "/tmp/substituted-root")
          :rule "C6-VERIFY"}
         {:name :redundant-sh06
          :carrier (assoc-in carrier
                             [:sh06-resolution-artifact :artifact-id]
                             zero-id)
          :rule "C6-VERIFY"}
         {:name :stale-b47-report
          :carrier (assoc-in carrier
                             [:b47-verification-report :status] :failed)
          :rule "C6-VERIFY"}
         {:name :forged-passed-report-check
          :carrier
          (assoc-in carrier
                    [:b47-verification-report :checks
                     :authenticated-request-replays?]
                    false)
          :rule "C6-VERIFY"}
         {:name :nested-sh05-extra-check
          :carrier
          (replace-nested-sh05-report
           carrier #(assoc-in % [:checks :unrecognized-check?] true))
          :rule "C6-VERIFY"}
         {:name :nested-sh05-verifier-run-count
          :carrier
          (replace-nested-sh05-report
           carrier #(update-in % [:gravity-verifiers :template
                                   :verified-run-count] inc))
          :rule "C6-VERIFY"}
         {:name :report-source-path
          :carrier (assoc-in carrier
                             [:b47-verification-report :source-path]
                             "/tmp/substituted.gravity")
          :rule "C6-VERIFY"}
         {:name :artifact-provenance-path
          :carrier (assoc-in carrier
                             [:b47-artifact :provenance :source-path]
                             "/tmp/substituted.gravity")
          :rule "C6-VERIFY"}
         {:name :relative-path-dot-dot
          :carrier (assoc-in carrier
                             [:source-snapshot :project-relative-path]
                             "../exception-error-exit.gravity")
          :rule "C6-VERIFY"}
         {:name :extension-disagreement
          :carrier (assoc-in carrier
                             [:source-snapshot :source-extension] ".qst")
          :rule "C6-VERIFY"}
         {:name :malformed-request-collection
          :carrier (assoc-in carrier (conj request-path :forms) {})
          :rule "C6-VERIFY"}
         {:name :alias-record-extra-field
          :carrier
          (assoc-in (get @declared-import-carriers ".gravity")
                    (into request-path [:alias-table 0 :untrusted]) true)
          :rule "C6-VERIFY"}
         {:name :macro-trace-extra-field
          :carrier
          (assoc-in carrier
                    (into request-path
                          [:macro-origin-traces 0 :untrusted]) true)
          :rule "C6-VERIFY"}
         {:name :duplicate-retained-origin
          :carrier
          (assoc-in carrier
                    (into request-path
                          [:forms origin-form-index :origin-chain])
                    [retained-origin retained-origin])
          :rule "C6-ORIGIN"}
         {:name :root-form-substitution
          :carrier
          (assoc-in carrier
                    [:b47-artifact :gravity-core-boundary
                     :authenticated-core-request :top-level-form-ids 0]
                    zero-id)
          :rule "C6-VERIFY"}
         {:name :assembly-substitution
          :carrier
          (assoc-in carrier
                    [:b47-artifact :gravity-core-boundary
                     :authenticated-core-request
                     :module-assembly-manifest :root-form-ids 0]
                    zero-id)
          :rule "C6-VERIFY"}
         {:name :coverage-substitution
          :carrier
          (update-in carrier
                     [:b47-artifact :gravity-core-boundary
                      :authenticated-core-request
                      :fragment-coverage :form-count]
                     inc)
          :rule "C6-VERIFY"}
         {:name :unauthorized-edge
          :carrier
          (assoc-in carrier
                    [:b47-artifact :gravity-core-boundary
                     :authenticated-core-request :resolution-table
                     0 :binding-id]
                    zero-id)
          :rule "C6-VERIFY"}
         {:name :malformed-byte-domain
          :carrier (assoc-in carrier [:source-snapshot :bytes 0] -1)
          :rule "C6-VERIFY"}]]
    (doseq [{:keys [name carrier rule]} probes]
      (testing name
        (let [result (call-phase :admit carrier nil [] [])
              diagnostic (first-diagnostic result)]
          (is (= :rejected (:status result)))
          (is (= rule (:rule diagnostic)))
          (is (true? (get-in diagnostic [:facts :fail-closed])))
          (is (nil? (:template result))))))))

(deftest sh07-b50-adapter-rejects-values-outside-immutable-edn
  (let [carrier (get @accepted-carriers ".gravity")
        realizations (atom 0)
        unsupported
        [[:lazy-sequence
          (map (fn [value]
                 (swap! realizations inc)
                 value)
               [40 41])]
         [:function (fn [] :not-edn)]
         [:java-array (byte-array 0)]
         [:record (->UnsupportedCarrierRecord :not-edn)]
         [:custom-comparator-map
          (sorted-map-by #(compare (str %2) (str %1)) :a 1)]
         [:arbitrary-object (Object.)]]]
    (doseq [[name value] unsupported]
      (testing name
        (let [altered (assoc carrier :untrusted-host-value value)
              failure
              (try
                (execute-carrier altered)
                nil
                (catch clojure.lang.ExceptionInfo exception
                  exception))]
          (is (= "SH07-B50-POST-READER-DOMAIN"
                 (:id (ex-data failure))))
          (is (= :gravity/sh07-core-diagnostic
                 (:artifact (ex-data failure))))
          (is (= "C6-VERIFY"
                 (:rule (ex-data failure))))
          (is (= :carrier-value-domain-invalid
                 (:reason (ex-data failure))))
          (is (true? (:fail-closed (ex-data failure)))))))
    (is (zero? @realizations))
    (let [converted
          (immutable-post-reader-edn!
           (get-in carrier [:source-snapshot :canonical-path])
           (with-meta
             {:scalars [nil false 0 "" :keyword 'symbol]
              :list (with-meta (list 1 2) {:host-metadata true})
              :metadata-symbol
              (with-meta 'metadata-symbol {:host-value (Object.)})
              :set #{1 2}}
             {:host-metadata true}))]
      (is (= {:scalars [nil false 0 "" :keyword 'symbol]
              :list (list 1 2)
              :metadata-symbol 'metadata-symbol
              :set #{1 2}}
             converted))
      (is (nil? (meta converted)))
      (is (nil? (meta (:list converted))))
      (is (nil? (meta (:metadata-symbol converted)))))
    (let [rewritten
          (recursively-rewrite-key
           (list {:target "old"} 'tail) :target "new")]
      (is (= (list {:target "new"} 'tail) rewritten))
      (is (= "clojure.lang.PersistentList"
             (some-> rewritten class .getName)))
      (is (= rewritten
             (immutable-post-reader-edn!
              (get-in carrier [:source-snapshot :canonical-path])
              rewritten))))))

(deftest sh07-b50-gravity-preflights-enforce-finite-post-reader-domain
  (let [accepted-values
        [nil false true -1 0 1 "" "text" :keyword 'symbol
         {} [] #{} (list) (list 1 2)]
        rejected-values
        [(fn [] :not-edn) (byte-array 0) (Object.)]
        preflights
        ['c6-sh07-exception-carrier-preflight
         'c6-sh07-independent-exception-carrier-preflight]]
    (doseq [preflight preflights
            value accepted-values]
      (is (= :accepted (:status (invoke preflight [value])))
          (str preflight " accepts bounded immutable EDN class "
               (some-> value class .getName))))
    (doseq [preflight preflights
            value rejected-values]
      (let [result (invoke preflight [value])]
        (is (= :rejected (:status result)))
        (is (= :carrier-value-domain-invalid (:reason result)))))))

(deftest sh07-b50-carrier-census-failures-use-verification-diagnostics
  (let [too-deep (nth (iterate vector nil) 257)
        too-wide (vec (range 65537))
        admissions
        ['c6-sh07-exception-carrier-admission
         'c6-sh07-independent-exception-carrier-admission]]
    (doseq [admission admissions
            [value reason] [[too-deep :carrier-depth-bound]
                            [too-wide :carrier-width-bound]]]
      (let [result (invoke admission [value])]
        (is (= :rejected (:status result)))
        (is (= "C6-VERIFY" (:rule result)))
        (is (= reason (:reason result)))))))

(deftest sh07-b50-outer-first-verification-diagnostic-wins-with-source-provenance
  (let [carrier (get @accepted-carriers ".gravity")
        altered
        (-> carrier
            (assoc-in [:source-snapshot :bytes 0] -1)
            (assoc-in [:b47-verification-report :status] :failed))
        result (call-phase :admit altered nil [] [])
        diagnostic (first-diagnostic result)]
    (is (= :rejected (:status result)))
    (is (= "C6-VERIFY" (:rule diagnostic)))
    (is (= :snapshot-byte-vector-outside-domain
           (get-in diagnostic [:facts :reason])))
    (is (= (get-in carrier [:source-snapshot :canonical-path])
           (get-in diagnostic [:source-span :source])))))

(deftest sh07-b50-independent-admission-reconstructs-reports-products-and-origins
  (let [carrier (get @accepted-carriers ".gravity")
        request-path
        [:b47-artifact :gravity-core-boundary
         :authenticated-core-request]
        forms (get-in carrier (conj request-path :forms))
        origin-index
        (first
         (keep-indexed
          (fn [index form]
            (when (seq (:generated-origin form)) index))
          forms))
        origin (first (get-in forms [origin-index :generated-origin]))
        probes
        [{:name :nested-sh05-schema
          :carrier
          (replace-nested-sh05-report
           carrier #(assoc-in % [:checks :unrecognized-check?] true))
          :rule "C6-VERIFY"}
         {:name :fragment-coverage
          :carrier
          (update-in carrier
                     (into request-path [:fragment-coverage :form-count])
                     inc)
          :rule "C6-VERIFY"}
         {:name :malformed-forms
          :carrier (assoc-in carrier (conj request-path :forms) {})
          :rule "C6-VERIFY"}
         {:name :retained-origin-duplicate
          :carrier
          (assoc-in carrier
                    (into request-path
                          [:forms origin-index :origin-chain])
                    [origin origin])
          :rule "C6-ORIGIN"}]]
    (doseq [{:keys [name carrier rule]} probes]
      (testing name
        (let [result
              (invoke 'c6-sh07-independent-exception-carrier-admission
                      [carrier])]
          (is (= :rejected (:status result)))
          (is (= rule (:rule result))))))))

(deftest sh07-b50-template-output-order-effect-origin-and-id-mutations-fail
  (let [carrier (get @accepted-carriers ".gravity")
        admitted (call-phase :admit carrier nil [] [])
        template (:template admitted)
        requests (:digest-requests admitted)
        digests
        (resolve-digests
         (get-in carrier [:source-snapshot :canonical-path]) requests)
        resolved (call-phase :resolve carrier template requests digests)
        resolved-template (:template resolved)
        zero-id (str "sha256:" (apply str (repeat 64 "0")))
        template-probes
        [{:name :order
          :template (assoc-in template [:evaluation-record :order]
                              [:evaluate-handler])
          :rule "C6-EVAL-ORDER"}
         {:name :origin
          :template (assoc-in template
                              [:selected :nodes 0 :source :origin-chain]
                              [{:forged true}])
          :rule "C6-ORIGIN"}
         {:name :substitution
          :template (assoc-in template [:selected :handler :core-node-id]
                              zero-id)
          :rule "C6-VERIFY"}
         {:name :coordinated-preimage-and-request-replay
          :template
          (let [altered
                (assoc-in template [:identity-preimages :membership :domain]
                          :gravity/forged-membership)]
            (assoc altered :digest-requests
                   (assoc-in requests [4 :preimage]
                             (get-in altered
                                     [:identity-preimages :membership]))))
          :requests
          (assoc-in requests [4 :preimage :domain]
                    :gravity/forged-membership)
          :rule "C6-VERIFY"}]
        resolved-probes
        [{:name :effect-drop
          :template (assoc-in resolved-template
                              [:canonical-core-artifact
                               :semantic-obligations :declared-effects]
                              [])
          :rule "C6-EFFECT-DROP"}
         {:name :unsafe-drop
          :template (assoc-in resolved-template
                              [:canonical-core-artifact
                               :semantic-obligations :unsafe-facts]
                              {})
          :rule "C6-UNSAFE-DROP"}
         {:name :arity-substitution
          :template (assoc-in resolved-template
                              [:canonical-core-artifact
                               :semantic-obligations :arity]
                              :variadic)
          :rule "C6-CORE-SHAPE"}
         {:name :mutation-substitution
          :template (assoc-in resolved-template
                              [:canonical-core-artifact
                               :semantic-obligations :mutation]
                              :present)
          :rule "C6-CORE-SHAPE"}
         {:name :recursion-substitution
          :template (assoc-in resolved-template
                              [:canonical-core-artifact
                               :semantic-obligations :recursion]
                              :present)
          :rule "C6-CORE-SHAPE"}
         {:name :exception-substitution
          :template (assoc-in resolved-template
                              [:canonical-core-artifact
                               :semantic-obligations :exception]
                              :resumable)
          :rule "C6-CORE-SHAPE"}
         {:name :pattern-substitution
          :template (assoc-in resolved-template
                              [:canonical-core-artifact
                               :semantic-obligations :pattern]
                              :present)
          :rule "C6-CORE-SHAPE"}
         {:name :origin-drop
          :template (assoc-in resolved-template
                              [:selected :nodes 0 :source :origin-chain]
                              [{:forged true}])
          :rule "C6-ORIGIN"}
         {:name :final-id
          :template (assoc-in resolved-template
                              [:canonical-core-artifact :artifact-id]
                              zero-id)
          :rule "C6-VERIFY"}
         {:name :digest-replay
          :template resolved-template
          :digests (assoc digests 9 zero-id)
          :rule "C6-VERIFY"}
         {:name :request-replay
          :template resolved-template
          :requests (assoc-in requests [9 :purpose] :forged-final)
          :rule "C6-VERIFY"}]]
    (doseq [{:keys [name template rule] :as probe} template-probes]
      (testing (str "template " name)
        (let [probe-requests (get probe :requests requests)
              result
              (call-phase :verify-template carrier template
                          probe-requests [])]
          (is (= :rejected (:status result)))
          (is (= rule (:rule (first-diagnostic result)))))))
    (doseq [{:keys [name template rule] :as probe} resolved-probes]
      (testing (str "resolved " name)
        (let [probe-requests (get probe :requests requests)
              probe-digests (get probe :digests digests)
              result
              (call-phase :verify-resolved
                          carrier template probe-requests probe-digests)]
          (is (= :rejected (:status result)))
          (is (= rule (:rule (first-diagnostic result)))))))))

(deftest sh07-b50-recursive-path-neutral-alias-mutation-cannot-replay
  (let [carrier (get @accepted-carriers ".gravity")
        zero-id (str "sha256:" (apply str (repeat 64 "0")))
        altered
        (update carrier :b47-artifact
                #(recursively-rewrite-key
                  % :sh06-semantic-projection-id zero-id))
        admitted (call-phase :admit altered nil [] [])]
    (if (= :rejected (:status admitted))
      (is (= "C6-VERIFY" (:rule (first-diagnostic admitted))))
      (let [template (:template admitted)
            requests (:digest-requests admitted)
            digests
            (resolve-digests
             (get-in altered [:source-snapshot :canonical-path]) requests)
            resolved
            (call-phase :resolve altered template requests digests)
            verified
            (call-phase :verify-resolved altered (:template resolved)
                        requests digests)]
        (is (= :rejected (:status verified)))
        (is (= :b47-artifact-id-mismatch
               (get-in (first-diagnostic verified) [:facts :reason])))))))

(deftest sh07-b50-every-phase-reauthenticates-and-binds-resolver-records
  (let [carrier (get @accepted-carriers ".gravity")
        admitted (call-phase :admit carrier nil [] [])
        template (:template admitted)
        requests (:digest-requests admitted)
        digests
        (resolve-digests
         (get-in carrier [:source-snapshot :canonical-path]) requests)
        fresh-verifier-digests
        (resolve-digests
         (get-in carrier [:source-snapshot :canonical-path]) requests)
        resolved (call-phase :resolve carrier template requests digests)
        resolved-template (:template resolved)
        stale-carrier
        (assoc-in carrier [:b47-verification-report :status] :failed)
        arbitrary-id (str "sha256:" (apply str (repeat 64 "a")))
        probes
        [{:name :verify-template-reauthentication
          :phase :verify-template :carrier stale-carrier
          :template template :requests requests :digests []
          :rule "C6-VERIFY"}
         {:name :resolve-reauthentication
          :phase :resolve :carrier stale-carrier
          :template template :requests requests :digests digests
          :rule "C6-VERIFY"}
         {:name :verify-resolved-reauthentication
          :phase :verify-resolved :carrier stale-carrier
          :template resolved-template :requests requests :digests digests
          :rule "C6-VERIFY"}
         {:name :arbitrary-digest-strings-are-not-resolver-records
          :phase :resolve :carrier carrier :template template
          :requests requests
          :digests (vec (repeat 10 arbitrary-id))
          :rule "C6-VERIFY"}
         {:name :resolver-purpose-replay
          :phase :resolve :carrier carrier :template template
          :requests requests
          :digests (assoc-in digests [0 :purpose] :forged-purpose)
          :rule "C6-VERIFY"}
         {:name :resolver-preimage-replay
          :phase :resolve :carrier carrier :template template
          :requests requests
          :digests (assoc-in digests [4 :preimage :domain]
                             :forged-membership)
          :rule "C6-VERIFY"}
         {:name :resolver-digest-only-substitution
          :phase :resolve :carrier carrier :template template
          :requests requests
          :digests (assoc-in digests [0 :digest] arbitrary-id)
          :rule "C6-VERIFY"}
         {:name :text-encoding-digest-only-substitution
          :phase :resolve :carrier carrier :template template
          :requests requests
          :digests (assoc-in digests [1 :digest] arbitrary-id)
          :rule "C6-VERIFY"}
         {:name :coordinated-raw-and-text-digest-substitution
          :phase :resolve :carrier carrier :template template
          :requests requests
          :digests (-> digests
                       (assoc-in [0 :digest] arbitrary-id)
                       (assoc-in [1 :digest] arbitrary-id))
          :rule "C6-VERIFY"}]]
    (doseq [{:keys [name phase carrier template requests digests rule]}
            probes]
      (testing name
        (let [result (call-phase phase carrier template requests digests)]
          (is (= :rejected (:status result)))
          (is (= rule (:rule (first-diagnostic result)))))))
    (let [malformed
          (call-phase :admit carrier template requests digests)]
      (is (= :rejected (:status malformed)))
      (is (= "C6-VERIFY"
             (:rule (first-diagnostic malformed)))))
    (let [mutated-resolver-digests
          (assoc-in digests [4 :digest] arbitrary-id)
          mutated-resolved
          (call-phase :resolve carrier template requests
                      mutated-resolver-digests)
          independently-verified
          (call-phase :verify-resolved carrier
                      (:template mutated-resolved) requests
                      fresh-verifier-digests)]
      (is (= :accepted (:status mutated-resolved)))
      (is (= :rejected (:status independently-verified)))
      (is (= "C6-VERIFY"
             (:rule (first-diagnostic independently-verified)))))))

(deftest sh07-b50-independent-digest-replay-rejects-coherent-derived-forgery
  (let [carrier (get @accepted-carriers ".gravity")
        honest-resolver resolve-digests
        forged-id (str "sha256:" (apply str (repeat 64 "a")))]
    (with-redefs
      [resolve-digests
       (fn [source-path requests]
         (reduce
          (fn [digests ordinal]
            (assoc-in digests [ordinal :digest] forged-id))
          (honest-resolver source-path requests)
          (range 4 10)))]
      (let [run (execute-carrier carrier)
            diagnostic (first-diagnostic (:verified run))]
        (is (= :accepted (get-in run [:resolved :status])))
        (is (= :rejected (:status run)))
        (is (= "C6-VERIFY" (:rule diagnostic)))
        (is (= :resolved-transcript-replay-mismatch
               (get-in diagnostic [:facts :reason])))
        (doseq [ordinal (range 4 10)]
          (is (not= (get-in run [:digests ordinal :digest])
                    (get-in run
                            [:verification-digests ordinal :digest]))))))))

(deftest sh07-b50-independent-verifier-does-not-reuse-production-transcript-validator
  (let [forms
        (read-gravity-source-forms
         "bootstrap/gravity/src/gravity/compiler/c6_core_lowering_engine.gravity")
        wrapper-forms
        (read-gravity-source-forms
         "bootstrap/clojure/test/gravity/self_hosting/sh07_exception_error_exit_test.clj")
        production-validator
        'c6-sh07-exception-digest-transcript-valid?
        independent-validator
        'c6-sh07-independent-exception-digest-transcript-valid?
        expected-request-builder
        'c6-sh07-independent-exception-expected-digest-requests
        from-resolved
        (gravity-defn-form
         forms 'c6-sh07-independent-exception-verifier-from-resolved)
        verifier
        (gravity-defn-form forms 'c6-sh07-independent-exception-verifier)
        replay-resolver
        (gravity-defn-form wrapper-forms 'independently-resolve-digests)
        from-resolved-symbols (source-form-symbols from-resolved)
        verifier-symbols (source-form-symbols verifier)
        replay-resolver-symbols (source-form-symbols replay-resolver)]
    (is (some? from-resolved))
    (is (some? verifier))
    (is (some? replay-resolver))
    (is (contains? from-resolved-symbols independent-validator))
    (is (contains? verifier-symbols independent-validator))
    (is (contains? verifier-symbols expected-request-builder))
    (is (not (contains? from-resolved-symbols production-validator)))
    (is (not (contains? verifier-symbols production-validator)))
    (is (contains? replay-resolver-symbols
                   'bootstrap/reader-canonical-hash))
    (is (not (contains? replay-resolver-symbols 'resolve-digests)))))

(deftest sh07-b50-manifest-hash-must-resolve-to-retained-project-root
  (let [carrier (get @accepted-carriers ".gravity")
        substituted-hash (str "sha256:" (apply str (repeat 64 "a")))
        altered
        (assoc-in carrier
                  [:project-root-evidence :manifest-bytes-hash]
                  substituted-hash)
        admitted (call-phase :admit altered nil [] [])
        template (:template admitted)
        requests (:digest-requests admitted)
        digests
        (resolve-digests
         (get-in altered [:source-snapshot :canonical-path]) requests)
        resolved (call-phase :resolve altered template requests digests)
        diagnostic (first-diagnostic resolved)]
    (is (= :accepted (:status admitted)))
    (is (= {:ordinal 3
            :purpose :project-root-id
            :preimage {:project-manifest "deps.edn"
                       :bytes-hash substituted-hash}}
           (get requests 3)))
    (is (= :rejected (:status resolved)))
    (is (= "C6-VERIFY" (:rule diagnostic)))
    (is (= :project-root-id-mismatch
           (get-in diagnostic [:facts :reason])))))

(deftest sh07-b50-independent-verifier-reconstructs-complete-membership
  (let [carrier (get @accepted-carriers ".gravity")
        {:keys [requests digests resolved]}
        (let [run (execute-carrier carrier)]
          {:requests (get-in run [:admitted :digest-requests])
           :digests (:verification-digests run)
           :resolved (:template (:resolved run))})
        verify
        (fn [candidate]
          (invoke 'c6-sh07-independent-exception-verifier
                  [candidate resolved requests digests]))
        mutations
        [(assoc-in carrier
                   [:project-root-evidence :manifest-byte-count] 0)
         (assoc-in carrier
                   [:project-root-evidence :project-root-id] "not-a-digest")
         (assoc-in carrier
                   [:sh06-resolution-artifact :sh05-macro-artifact
                    :gravity-macro-boundary :authenticated-sh04-artifact
                    :c2-reader-artifact :source-unit-record
                    :project-root-record :path]
                   "/forged-root")
         (assoc-in carrier
                   [:sh06-resolution-artifact :sh05-macro-artifact
                    :gravity-macro-boundary :authenticated-sh04-artifact
                    :c2-reader-artifact :source-unit-record
                    :project-relative-path]
                   "forged.gravity")
         (assoc-in carrier
                   [:sh06-resolution-artifact :sh05-macro-artifact
                    :gravity-macro-boundary :authenticated-sh04-artifact
                    :c2-reader-artifact :source-unit-record :extension]
                   ".qst")
         (assoc-in carrier
                   [:sh06-resolution-artifact :sh05-macro-artifact
                    :gravity-macro-boundary :authenticated-sh04-artifact
                    :c2-reader-artifact :source-unit-record :source-kind]
                   :gravity/query-source)]]
    (is (= :passed (:status (verify carrier))))
    (doseq [mutation mutations]
      (is (= :failed (:status (verify mutation)))))))

(deftest sh07-b50-entrypoint-malformed-phase-request-is-total
  (let [result
        (invoke 'c6-sh07-authenticated-exception-entrypoint
                [{:artifact :gravity/sh07-c6-entry-request
                  :schema-version 1
                  :phase :admit}])
        diagnostic (first-diagnostic result)]
    (is (= :rejected (:status result)))
    (is (= "C6-VERIFY" (:rule diagnostic)))
    (is (= :entry-request-shape-invalid
           (get-in diagnostic [:facts :reason])))
    (is (true? (get-in diagnostic [:facts :fail-closed])))))
