(ns gravity.reader.artifact-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.reader :as reader]))

(defn- fixture [disposition stem extension]
  (str "bootstrap/clojure/fixtures/" disposition "/" stem extension))

(defn- operations []
  {:read-source-form-records bootstrap/read-source-form-records
   :validate-ns-syntax! bootstrap/validate-ns-syntax!
   :reader-module-context bootstrap/reader-module-context
   :syntax-object-stream bootstrap/syntax-object-stream
   :l1-source-unit-artifacts bootstrap/l1-source-unit-artifacts
   :standard-reader-options bootstrap/standard-reader-options
   :gravity-source-extension bootstrap/gravity-source-extension
   :gravity-source-kind bootstrap/gravity-source-kind
   :utf8-byte-count bootstrap/utf8-byte-count})

(defn- direct-artifact [path text]
  (reader/read-source-artifact
   path text (bootstrap/reader-project-context-for-source path) (operations)))

(defn- rejection [thunk]
  (try (thunk) ::unexpected-success
       (catch clojure.lang.ExceptionInfo exception exception)))

(deftest paired-source-artifacts-preserve-the-existing-reader-product
  (doseq [[extension kind] [[".gravity" :gravity-branded-source]
                            [".qst" :qst-theory-source]]]
    (let [path (fixture "accepted" "surface-syntax" extension)
          text (slurp path)
          direct (direct-artifact path text)
          compatibility (bootstrap/read-source-artifact path text)
          file-artifact (bootstrap/read-file-artifact path)
          syntax (:syntax-object-stream direct)]
      (is (= direct compatibility file-artifact))
      (is (= direct (direct-artifact path text)))
      (is (= :gravity/stage0-reader-artifact (:kind direct)))
      (is (= {:path path :extension extension :encoding :utf-8
              :source-kind kind :byte-count (bootstrap/utf8-byte-count text)
              :form-count 2}
             (:source direct)))
      (is (= {:module 'syntax.surface :profile :hosted :target :jvm :safety :safe}
             (select-keys (:module-context direct)
                          [:module :profile :target :safety])))
      (is (= {:doc "metadata survives reading"} (:metadata (second syntax))))
      (is (= path (get-in (first syntax) [:span :source])))
      (is (= 0 (get-in (first syntax) [:span :byte-start])))
      (is (= 1 (get-in (first syntax) [:span :start :line])))
      (is (seq (:namespace-clause-syntax direct)))
      (is (not (contains? (:module-context direct) :namespace-clause-syntax)))
      (is (= [] (:diagnostics direct)))
      (is (= path (get-in direct [:source-unit-record :path])))
      (is (= extension (get-in direct [:source-unit-record :extension])))
      (is (= kind (get-in direct [:source-unit-record :source-kind])))
      (is (re-matches #"sha256:[0-9a-f]{64}"
                      (get-in direct [:source-unit-record :source-id]))))))

(deftest identity-remains-sensitive-to-source-options-and-project-context
  (let [path (fixture "accepted" "surface-syntax" ".gravity")
        text (slurp path)
        context (bootstrap/reader-project-context-for-source path)
        produce #(reader/read-source-artifact path %1 %2 %3)
        original (produce text context (operations))
        source-id #(get-in % [:source-unit-record :source-id])
        changed-text (produce (str text "\n") context (operations))
        changed-options
        (produce text context
                 (assoc-in (operations) [:standard-reader-options :retain-comments] false))
        changed-context
        (produce text (assoc context :project-root-id
                             (bootstrap/reader-canonical-hash {:project :other}))
                 (operations))]
    (is (= (source-id original) (source-id (produce text context (operations)))))
    (is (not= (source-id original) (source-id changed-text)))
    (is (not= (source-id original) (source-id changed-options)))
    (is (not= (source-id original) (source-id changed-context)))
    (is (= (:syntax-object-stream original) (:syntax-object-stream changed-text)))
    (is (= (:source original) (:source changed-options)))
    (is (not= (:source original) (:source changed-text)))))

(deftest real-invalid-inputs-retain-stage-owned-diagnostics
  (doseq [extension [".gravity" ".qst"]
          [stem expected] [["surface-syntax-l1-delimiter" "L1-DELIMITER"]]]
    (let [path (fixture "rejected" stem extension)
          text (slurp path)
          direct (rejection #(direct-artifact path text))
          compatibility (rejection #(bootstrap/read-source-artifact path text))]
      (is (instance? clojure.lang.ExceptionInfo direct))
      (is (= expected (:id (ex-data direct))))
      (is (= (ex-message direct) (ex-message compatibility)))
      (is (= (ex-data direct) (ex-data compatibility)))
      (is (= path (get-in (ex-data direct) [:source-span :source]))))))

(deftest namespace-shape-rejection-remains-with-the-reader-owner
  (let [path (fixture "rejected" "invalid-ns-shape" ".gravity")
        text (slurp path)
        direct (rejection #(direct-artifact path text))
        compatibility (rejection #(bootstrap/read-source-artifact path text))]
    (is (= "L1-NS-SHAPE" (:id (ex-data direct))))
    (is (= (ex-data direct) (ex-data compatibility)))))

(deftest compatibility-interposition-and-operation-order-survive-extraction
  (let [path (fixture "accepted" "surface-syntax" ".gravity")
        text (slurp path)
        calls (atom [])
        original (operations)
        observed
        (into {} (map (fn [[key operation]]
                        [key (if (fn? operation)
                               (fn [& args]
                                 (swap! calls conj [key args])
                                 (apply operation args))
                               operation)]) original))
        context (bootstrap/reader-project-context-for-source path)
        result (reader/read-source-artifact path text context observed)]
    (is (= result (bootstrap/read-source-artifact path text context)))
    (is (= [:read-source-form-records :validate-ns-syntax!
            :reader-module-context :syntax-object-stream :l1-source-unit-artifacts
            :gravity-source-extension :gravity-source-kind :utf8-byte-count]
           (mapv first @calls)))
    (is (= [path text bootstrap/standard-reader-options context]
           (second (nth @calls 4))))
    (testing "a rejection is propagated unchanged before later stages run"
      (let [sentinel (ex-info "owner rejection" {:id "L1-NS-SHAPE" :owner :reader})
            later-calls (atom 0)
            raised
            (with-redefs [bootstrap/validate-ns-syntax! (fn [& _] (throw sentinel))
                          bootstrap/syntax-object-stream
                          (fn [& _] (swap! later-calls inc))]
              (rejection #(bootstrap/read-source-artifact path text context)))]
        (is (identical? sentinel raised))
        (is (zero? @later-calls))))
    (testing "the compatibility wrapper enters the stage API"
      (let [seen (atom nil)
            sentinel {:kind :test-seam}
            result
            (with-redefs [reader/read-source-artifact
                          (fn [& args] (reset! seen args) sentinel)]
              (bootstrap/read-source-artifact path text context))]
        (is (identical? sentinel result))
        (is (= [path text context] (vec (take 3 @seen))))
        (is (= (operations) (last @seen)))))))

(deftest namespace-contract-is-narrow-and-preserves-compatibility-arities
  (let [contract (:gravity/namespace-contract (meta #'reader/read-source-artifact))]
    (is (= #{'read-source-artifact 'decode-source-bytes 'read-source-text 'reject-source-path!} (set (keys (ns-publics 'gravity.reader)))))
    (is (empty? (ns-aliases 'gravity.reader)))
    (is (= :native-source-intake-diagnostics-and-stage0-reader-artifact-assembly (:contract-boundary contract)))
    (is (= ['clojure.core] (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.cli 'gravity.release]
           (get-in contract [:dependency-direction :forbids])))
    (is (= (set (:operation-inputs contract)) (set (keys (operations)))))
    (is (= ["L1-SOURCE-ENCODING" "L1-SOURCE-EXTENSION"] (get-in contract [:diagnostic-ownership :owns])))
    (is (= :supplied-caller (get-in contract [:diagnostic-ownership :admission-owner])))
    (is (true? (get-in contract [:public-api 'read-source-text :opens-source?])))
    (is (false? (get-in contract [:public-api 'read-source-text :grants-file-authority?])))
    (is (true? (:clojure-seed-boundary? contract)))
    (is (false? (:canonical-c2-reader? contract)))
    (is (false? (:authenticated-reader? contract)))
    (is (false? (:self-hosted? contract)))
    (is (= '([source-path source-text] [source-path source-text project-context])
           (:arglists (meta #'bootstrap/read-source-artifact))))
    (is (= '([path]) (:arglists (meta #'bootstrap/read-gravity-source-text))))))

(defn- decode [path bytes]
  (reader/decode-source-bytes #'bootstrap/sha256-bytes-hex #'bootstrap/fail! path bytes))

(defn- raw-bytes [values] (byte-array (map unchecked-byte values)))

(defn- read-native-source [path]
  (reader/read-source-text #'bootstrap/qst-or-gravity-source?
                           #'bootstrap/source-path-policy-fail!
                           #'bootstrap/decode-gravity-source-bytes path))

(defn- with-native-source-file [extension bytes f]
  (let [path (java.nio.file.Files/createTempFile
              "gravity-reader-intake-" extension
              (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (java.nio.file.Files/write path bytes (make-array java.nio.file.OpenOption 0))
      (f (.toString path))
      (finally (java.nio.file.Files/deleteIfExists path)))))

(deftest native-source-intake-preserves-real-file-bytes-and-spellings
  (doseq [extension [".gravity" ".qst"]]
    (let [text "first\r\n\u03bb\n" input (.getBytes text "UTF-8")]
      (with-native-source-file
        extension input
        (fn [path]
          (is (= text (read-native-source path) (bootstrap/read-gravity-source-text path)))
          (is (= (seq input) (seq (java.nio.file.Files/readAllBytes
                                  (.toPath (java.io.File. path))))))
          (is (= extension (bootstrap/gravity-source-extension path))))))))

(deftest extension-refusal-precedes-source-decoding-and-retains-byte-hash
  (let [input (raw-bytes [65 195 40]) calls (atom 0)]
    (with-native-source-file
      ".txt" input
      (fn [path]
        (with-redefs [bootstrap/decode-gravity-source-bytes
                      (fn [& _] (swap! calls inc) (throw (ex-info "unexpected decode" {})))]
          (doseq [invoke [read-native-source bootstrap/read-gravity-source-text]]
            (let [data (ex-data (rejection #(invoke path)))
                  hash (str "sha256:" (bootstrap/sha256-bytes-hex input))]
              (is (= "L1-SOURCE-EXTENSION" (:id data)))
              (is (= hash (:source-id data) (get-in data [:primary :artifact])))
              (is (= path (get-in data [:source-span :source])))
              (is (= 3 (get-in data [:facts :byte-count])))))
          (is (zero? @calls)))))))

(deftest native-intake-reads-the-decoder-var-after-path-policy
  (doseq [invoke [read-native-source bootstrap/read-gravity-source-text]]
    (with-native-source-file
      ".gravity" (.getBytes "before" "UTF-8")
      (fn [path]
        (let [events (atom []) original-decoder bootstrap/decode-gravity-source-bytes]
          (with-redefs [bootstrap/qst-or-gravity-source?
                        (fn [observed]
                          (swap! events conj [:predicate observed])
                          (spit path "after" :encoding "UTF-8")
                          (alter-var-root
                           #'bootstrap/decode-gravity-source-bytes
                           (constantly (fn [observed bytes]
                                         (swap! events conj [:late-decoder observed (vec bytes)])
                                         (original-decoder observed bytes))))
                          true)
                        bootstrap/source-path-policy-fail! (fn [& _] (throw (ex-info "unexpected policy refusal" {})))
                        bootstrap/decode-gravity-source-bytes (fn [& _] (throw (ex-info "stale decoder" {})))]
            (is (= "after" (invoke path))))
          (is (= [[:predicate path] [:late-decoder path (vec (.getBytes "after" "UTF-8"))]] @events)))))))

(deftest native-intake-preserves-original-dependency-exceptions
  (with-native-source-file
    ".qst" (.getBytes "source" "UTF-8")
    (fn [path]
      (doseq [invoke [read-native-source bootstrap/read-gravity-source-text]
              port [:predicate :policy :decoder]]
        (let [sentinel (ex-info "native intake dependency failure" {:port port})
              fail (fn [& _] (throw sentinel))
              substitutions
              (case port
                :predicate {#'bootstrap/qst-or-gravity-source? fail}
                :policy {#'bootstrap/qst-or-gravity-source? (constantly false)
                         #'bootstrap/source-path-policy-fail! fail}
                :decoder {#'bootstrap/decode-gravity-source-bytes fail})]
          (with-redefs-fn substitutions
            #(is (identical? sentinel (rejection (fn [] (invoke path)))))))))))

(deftest missing-native-files-fail-before-decoder-invocation
  (with-native-source-file
    ".gravity" (byte-array 0)
    (fn [path]
      (java.nio.file.Files/delete (.toPath (java.io.File. path)))
      (let [calls (atom 0)]
        (with-redefs [bootstrap/decode-gravity-source-bytes
                      (fn [& _] (swap! calls inc) (throw (ex-info "unexpected decoder" {})))]
          (doseq [invoke [read-native-source bootstrap/read-gravity-source-text]]
            (let [error (try (invoke path) nil
                             (catch java.nio.file.NoSuchFileException error error))]
              (is (instance? java.nio.file.NoSuchFileException error))
              (is (= path (.getFile ^java.nio.file.NoSuchFileException error)))))
          (is (zero? @calls)))))))

(deftest strict-utf8-preserves-valid-bytes-as-text-without-consulting-error-ports
  (let [contract (:gravity/namespace-contract (meta #'reader/decode-source-bytes))]
    (is (= (:arglists (meta #'reader/decode-source-bytes))
           (get-in contract [:public-api 'decode-source-bytes :arglists])))
    (is (= :none (get-in contract [:public-api 'decode-source-bytes :admission])))
    (is (false? (get-in contract [:public-api 'decode-source-bytes :opens-source?]))))
  (doseq [text ["" "ascii" "first\r\nsecond\n" "\u03bb" "\u00e9" "\u4e2d"
                (String. (Character/toChars 128578)) "\u0000" "\ufefftext"]]
    (let [input (.getBytes ^String text "UTF-8")]
      (is (= text (decode "valid.gravity" input)
             (bootstrap/decode-gravity-source-bytes "valid.gravity" input)))
      (with-redefs [bootstrap/sha256-bytes-hex (fn [_] (throw (ex-info "Unexpected hash" {})))
                    bootstrap/fail! (fn [& _] (throw (ex-info "Unexpected diagnostic" {})))]
        (is (= text (decode "valid.qst" input)))))))

(deftest malformed-utf8-keeps-exact-byte-source-hash-and-diagnostic-provenance
  (doseq [values [[195] [195 40] [65 195 40] [128] [192 175] [224 128 175]
                  [237 160 128] [240 159 153] [244 144 128 128] [245 128 128 128] [255]]]
    (let [input (raw-bytes values)
          path "malformed.qst"
          direct (rejection #(decode path input))
          compatibility (rejection #(bootstrap/decode-gravity-source-bytes path input))
          data (ex-data direct)
          raw-hash (str "sha256:" (bootstrap/sha256-bytes-hex input))]
      (is (= "L1-SOURCE-ENCODING" (:id data)))
      (is (= (ex-message direct) (ex-message compatibility)))
      (is (= data (ex-data compatibility)))
      (is (= raw-hash (:source-id data) (get-in data [:facts :bytes-hash])))
      (is (= path (get-in data [:source-span :source])))
      (is (= (:source-span data) (get-in data [:primary :span])))
      (is (= raw-hash (get-in data [:primary :artifact])))
      (is (= {:stage :source-decoding :encoding :utf-8} (:reader-state data)))))
  (let [data (ex-data (rejection #(decode "offset.gravity" (raw-bytes [65 195 40]))))]
    (is (= {:source "offset.gravity" :byte-start 1 :byte-end 2} (:source-span data)))
    (is (= 1 (get-in data [:facts :malformed-input-length])))))

(deftest real-invalid-source-files-stop-before-form-reading
  (doseq [extension [".gravity" ".qst"]]
    (let [path (java.nio.file.Files/createTempFile
                "gravity-stage-utf8-" extension
                (make-array java.nio.file.attribute.FileAttribute 0))
          input (raw-bytes [65 195 40]) reads (atom 0)]
      (try
        (java.nio.file.Files/write path input (make-array java.nio.file.OpenOption 0))
        (let [error
              (with-redefs [bootstrap/read-source-form-records
                            (fn [& _] (swap! reads inc) (throw (ex-info "Unexpected parser" {})))]
                (rejection #(bootstrap/read-file-artifact (str path))))]
          (is (= "L1-SOURCE-ENCODING" (:id (ex-data error))))
          (is (= (str path) (get-in (ex-data error) [:source-span :source])))
          (is (= (str "sha256:" (bootstrap/sha256-bytes-hex input)) (:source-id (ex-data error))))
          (is (zero? @reads)))
        (finally (java.nio.file.Files/deleteIfExists path))))))

(deftest raw-byte-hash-and-diagnostic-var-order-and-exceptions-remain-native
  ;; Explicit trusted host interposition only; no authenticated input or grant.
  (doseq [operation [decode bootstrap/decode-gravity-source-bytes]]
    (let [input (raw-bytes [195 40]) original-hash bootstrap/sha256-bytes-hex
          calls (atom [])
          final (fn [id message data]
                  (swap! calls conj [:diagnostic id message (:source-id data)])
                  (throw (ex-info message (assoc data :id id))))]
      (with-redefs [bootstrap/sha256-bytes-hex
                    (fn [bytes]
                      (swap! calls conj [:hash (vec bytes)])
                      (alter-var-root #'bootstrap/fail! (constantly final))
                      (original-hash bytes))
                    bootstrap/fail! (fn [& _] (throw (ex-info "Premature diagnostic root" {})))]
        (let [error (rejection #(operation "late.gravity" input))]
          (is (= "L1-SOURCE-ENCODING" (:id (ex-data error))))))
      (is (= [:hash :diagnostic] (mapv first @calls)))))
  (let [sentinel (ex-info "hash owner failed" {:id "HASH-OWNER"}) diagnostics (atom 0)
        error (with-redefs [bootstrap/sha256-bytes-hex (fn [_] (throw sentinel))
                            bootstrap/fail! (fn [& _] (swap! diagnostics inc))]
                (rejection #(decode "hash-failure.gravity" (raw-bytes [195 40]))))]
    (is (identical? sentinel error))
    (is (zero? @diagnostics))))

(defn- direct-path-refusal [path captured-bytes]
  (reader/reject-source-path! #'bootstrap/gravity-source-extension
                              #'bootstrap/co-canonical-source-extensions
                              #'bootstrap/sha256-bytes-hex #'bootstrap/fail!
                              path captured-bytes))

(deftest source-path-refusals-preserve-real-file-and-supplied-snapshot-provenance
  (let [captured (.getBytes "original source snapshot" java.nio.charset.StandardCharsets/UTF_8)]
    (with-native-source-file
      ".txt" captured
      (fn [path]
        (let [one (ex-data (rejection #(bootstrap/source-path-policy-fail! path)))
              two (ex-data (rejection #(bootstrap/source-path-policy-fail! path captured)))
              direct (ex-data (rejection #(direct-path-refusal path captured)))
              expected-hash (str "sha256:" (bootstrap/sha256-bytes-hex captured))]
          (is (= one two direct))
          (is (= "L1-SOURCE-EXTENSION" (:id direct)))
          (is (= {:source path :byte-start 0 :byte-end 0} (:source-span direct)))
          (is (= expected-hash (:source-id direct) (get-in direct [:facts :bytes-hash])
                 (get-in direct [:primary :artifact]) (get-in direct [:origin-chain 0 :source-id])))
          (is (= (alength captured) (get-in direct [:facts :byte-count])))
          (is (= [".gravity" ".qst"] (get-in direct [:facts :allowed-extensions])))
          (is (nil? (:profile direct)))
          (is (nil? (:target direct)))
          (spit path "changed on disk")
          (is (= direct (ex-data (rejection #(direct-path-refusal path captured)))))
          (is (not= expected-hash
                    (:source-id (ex-data (rejection #(bootstrap/source-path-policy-fail! path))))))
          (java.nio.file.Files/delete (.toPath (java.io.File. path)))
          (let [missing (ex-data (rejection #(bootstrap/source-path-policy-fail! path)))]
            (is (= "L1-SOURCE-EXTENSION" (:id missing)))
            (is (= path (get-in missing [:source-span :source])))
            (is (not (contains? (:facts missing) :bytes-hash)))
            (is (not (contains? (:facts missing) :byte-count)))))))))

(deftest source-path-wrapper-retains-late-stage-interposition
  (let [captured (byte-array [1 2]) observed (atom nil)]
    (with-redefs [reader/reject-source-path! (fn [& args] (reset! observed (vec args)) :stage-observed)]
      (is (= :stage-observed (bootstrap/source-path-policy-fail! "module.txt" captured)))
      (is (= [#'bootstrap/gravity-source-extension #'bootstrap/co-canonical-source-extensions
              #'bootstrap/sha256-bytes-hex #'bootstrap/fail! "module.txt" captured] @observed))
      (is (identical? captured (last @observed))))))

(deftest one-argument-path-wrapper-keeps-its-own-late-two-argument-call
  (let [original bootstrap/source-path-policy-fail! captured (byte-array [4 5 6])
        observed (atom nil)]
    (with-native-source-file
      ".txt" captured
      (fn [path]
        (with-redefs [bootstrap/source-path-policy-fail!
                      (fn [selected supplied] (reset! observed [selected (vec supplied)]) :late-two-arity)]
          (is (= :late-two-arity (original path)))
          (is (= [path [4 5 6]] @observed)))))))

(deftest source-path-dependencies-retain-operator-and-argument-lookup-order
  ;; Trusted host interposition only; these labels/hash do not authenticate data.
  (let [events (atom []) received (atom nil)
        after-extension (fn [& _] (swap! events conj :after-extension-failure) :after-extension)
        after-hash (fn [_ _ data] (swap! events conj :after-hash-failure)
                     (reset! received data) :after-hash)]
    (with-redefs [bootstrap/sha256-bytes-hex
                  (fn [_] (swap! events conj :hash)
                    (alter-var-root #'bootstrap/fail! (constantly after-hash)) "synthetic-byte-hash")
                  bootstrap/gravity-source-extension
                  (fn [_] (swap! events conj :extension)
                    (alter-var-root #'bootstrap/fail! (constantly after-extension))
                    (alter-var-root #'bootstrap/co-canonical-source-extensions (constantly #{".after"}))
                    ".observed")
                  bootstrap/co-canonical-source-extensions #{".before"}
                  bootstrap/fail! (fn [& _] :before-hash)]
      (is (= :after-hash (direct-path-refusal "module.txt" (byte-array [1]))))
      (is (= [:hash :extension :after-hash-failure] @events))
      (is (= ".observed" (get-in @received [:facts :actual-extension])))
      (is (= [".after"] (get-in @received [:facts :allowed-extensions])))
      (is (= "sha256:synthetic-byte-hash" (:source-id @received)))
      (is (= :after-extension (direct-path-refusal "module.txt" nil)))
      (is (= [:hash :extension :after-hash-failure :extension :after-extension-failure] @events)))))

(deftest source-path-dependency-exceptions-retain-the-original-identity
  (doseq [dependency [#'bootstrap/sha256-bytes-hex #'bootstrap/gravity-source-extension #'bootstrap/fail!]]
    (let [sentinel (IllegalStateException. "original source-path dependency exception")
          outcome (with-redefs-fn {dependency (fn [& _] (throw sentinel))}
                    #(try (direct-path-refusal "module.txt" (byte-array [1]))
                       (catch Throwable error error)))]
      (is (identical? sentinel outcome)))))

(deftest source-path-api-declares-no-input-admission-or-file-access
  (let [contract (:gravity/namespace-contract (meta #'reader/reject-source-path!))
        api (get-in contract [:public-api 'reject-source-path!])]
    (is (= (:arglists (meta #'reader/reject-source-path!)) (:arglists api)))
    (is (= :none (:admission api)))
    (is (false? (:opens-source? api)))
    (is (= '([source-path] [source-path bytes])
           (:arglists (meta #'bootstrap/source-path-policy-fail!))))))
