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
    (is (= #{'read-source-artifact 'decode-source-bytes 'read-source-text 'reject-source-path!
             'compatibility-reader-products 'validate-native-reader-report!}
           (set (keys (ns-publics 'gravity.reader)))))
    (is (= (set (keys (:public-api contract)))
           (set (keep (fn [[name operation]]
                        (when (:gravity/namespace-contract (meta operation)) name))
                      (ns-interns 'gravity.reader)))))
    (is (every? #(= contract (:gravity/namespace-contract (meta %)))
                (vals (ns-publics 'gravity.reader))))
    (is (every? #(not (:private (meta %)))
                (vals (ns-publics 'gravity.reader))))
    (is (empty? (ns-aliases 'gravity.reader)))
    (is (= :native-reader-intake-products-and-report-validation (:contract-boundary contract)))
    (is (= ['clojure.core] (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.cli 'gravity.release]
           (get-in contract [:dependency-direction :forbids])))
    (is (= (set (:operation-inputs contract)) (set (keys (operations)))))
    (is (= ["L1-SOURCE-ENCODING" "L1-SOURCE-EXTENSION"] (get-in contract [:diagnostic-ownership :owns])))
    (is (= :supplied-caller (get-in contract [:diagnostic-ownership :admission-owner])))
    (is (= ["C2-ENCODING"] (get-in contract [:diagnostic-ownership :delegates])))
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
                           #'bootstrap/decode-gravity-source-bytes
                           #'bootstrap/sh03-reader-read-target-source-bytes! path))

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
              port [:predicate :policy :decoder :bytes]]
        (let [sentinel (ex-info "native intake dependency failure" {:port port})
              fail (fn [& _] (throw sentinel))
              substitutions
              (case port
                :predicate {#'bootstrap/qst-or-gravity-source? fail}
                :policy {#'bootstrap/qst-or-gravity-source? (constantly false)
                         #'bootstrap/source-path-policy-fail! fail}
                :decoder {#'bootstrap/decode-gravity-source-bytes fail}
                :bytes {#'bootstrap/sh03-reader-read-target-source-bytes! fail})]
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
            (let [error (ex-data (rejection #(invoke path)))]
              (is (= "C2-ENCODING" (:id error)))
              (is (= path (get-in error [:source-span :source])))
              (is (= :source-file-attributes (get-in error [:facts :failure-kind])))
              (is (= "java.nio.file.NoSuchFileException"
                     (get-in error [:facts :contained-host-error])))))
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

(deftest intake-uses-the-late-byte-owner-and-resolves-decoder-before-capture
  ;; Isolated trusted dependency interposition, not source authentication.
  (doseq [invoke [read-native-source bootstrap/read-gravity-source-text]]
    (let [events (atom []) captured (byte-array [65 66])]
      (with-redefs [bootstrap/qst-or-gravity-source?
                    (fn [path]
                      (swap! events conj [:predicate path])
                      (alter-var-root #'bootstrap/sh03-reader-read-target-source-bytes!
                        (constantly
                          (fn [selected]
                            (swap! events conj [:bytes selected])
                            (alter-var-root #'bootstrap/decode-gravity-source-bytes
                              (constantly (fn [& _] :replaced-during-capture)))
                            captured)))
                      true)
                    bootstrap/sh03-reader-read-target-source-bytes! (fn [& _] (throw (ex-info "stale byte owner" {})))
                    bootstrap/decode-gravity-source-bytes
                    (fn [path bytes]
                      (swap! events conj [:decoder path])
                      (is (identical? captured bytes))
                      :resolved-before-capture)]
        (is (= :resolved-before-capture (invoke "selected.gravity"))))
      (is (= [[:predicate "selected.gravity"] [:bytes "selected.gravity"] [:decoder "selected.gravity"]]
             @events)))))

(deftest intake-preserves-the-byte-owners-regular-file-and-final-link-policy
  (with-native-source-file
    ".gravity" (.getBytes "source" "UTF-8")
    (fn [target]
      (let [link (.resolveSibling (.toPath (java.io.File. target))
                                 (str (.getName (java.io.File. target)) ".link.gravity"))]
        (try
          (java.nio.file.Files/createSymbolicLink link (.toPath (java.io.File. target))
                                                 (make-array java.nio.file.attribute.FileAttribute 0))
          (let [path (.toString link)
                expected (ex-data (rejection #(bootstrap/sh03-reader-read-target-source-bytes! path)))]
            (is (= "C2-ENCODING" (:id expected)))
            (doseq [invoke [read-native-source bootstrap/read-gravity-source-text]]
              (is (= expected (ex-data (rejection #(invoke path)))))))
          (finally (java.nio.file.Files/deleteIfExists link)))))))

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

(defn- compatibility-operations []
  {:source-unit-record-var #'bootstrap/c2-source-unit-record
   :reader-table-var #'bootstrap/stage1-reader-table
   :token-stream-var #'bootstrap/stage1-reader-token-stream
   :token-record-var #'bootstrap/c2-token-record
   :parsed-products-var #'bootstrap/stage1-reader-products-from-token-stream
   :form-record-var #'bootstrap/c2-form-record
   :reader-fail-var #'bootstrap/c2-reader-fail!
   :source-span-var #'bootstrap/source-span})

(defn- compatibility-products [path text context]
  (reader/compatibility-reader-products
   path text bootstrap/standard-reader-options context (compatibility-operations)))

(deftest compatibility-oracle-emits-real-products-with-original-provenance
  (doseq [extension [".gravity" ".qst"]]
    (let [path (fixture "accepted" "surface-syntax" extension)
          text (slurp path)
          context (bootstrap/reader-project-context-for-source path)
          product (compatibility-products path text context)
          source-unit (:source-unit product)
          source-id (:source-id source-unit)]
      (is (= product (bootstrap/c2-reader-products-clojure-oracle
                     path text bootstrap/standard-reader-options)))
      (is (= product (bootstrap/c2-reader-products-clojure-oracle
                     path text bootstrap/standard-reader-options context)))
      (is (= product (compatibility-products path text context)))
      (is (= path (:path source-unit)))
      (is (= extension (:extension source-unit)))
      (is (= (str "sha256:" (bootstrap/sha256-hex text)) (:bytes-hash source-unit)))
      (is (= source-id (bootstrap/reader-canonical-hash (:identity-inputs source-unit))))
      (is (seq (:token-stream product)))
      (is (seq (:form-tree product)))
      (is (= 2 (count (:parsed-records product))))
      (is (every? #(= source-id (:source-id %) (get-in % [:span :file]))
                  (concat (:token-stream product) (:form-tree product))))
      (is (every? #(= path (:source-path %))
                  (concat (:token-stream product) (:form-tree product))))
      (is (not= source-id (get-in (compatibility-products path (str text "\n") context)
                                 [:source-unit :source-id]))))))

(deftest compatibility-oracle-retains-native-malformed-source-diagnostics
  (doseq [extension [".gravity" ".qst"]]
    (let [path (fixture "rejected" "surface-syntax-l1-delimiter" extension)
          text (slurp path)
          context (bootstrap/reader-project-context-for-source path)
          direct (rejection #(compatibility-products path text context))
          wrapper (rejection #(bootstrap/c2-reader-products-clojure-oracle
                                path text bootstrap/standard-reader-options context))]
      (is (= "STAGE1READER002" (:id (ex-data direct))))
      (is (= (ex-message direct) (ex-message wrapper)))
      (is (= (ex-data direct) (ex-data wrapper)))
      (is (= bootstrap/standard-reader-options (:reader-options (ex-data direct))))
      (is (re-matches #"sha256:[0-9a-f]{64}" (:source-id (ex-data direct))))
      (is (= path (get-in (ex-data direct) [:source-span :source])))
      (is (instance? clojure.lang.ExceptionInfo (ex-cause direct))))))

(deftest compatibility-oracle-preserves-containment-boundaries-and-exception-causes
  (let [path "src/compatibility.gravity"
        context (bootstrap/reader-project-context-for-source path)]
    (doseq [dependency [#'bootstrap/c2-source-unit-record #'bootstrap/stage1-reader-table]]
      (let [sentinel (ex-info "outside protected region" {:origin dependency})
            result (with-redefs-fn {dependency (fn [& _] (throw sentinel))}
                     #(rejection (fn [] (compatibility-products path "x" context))))]
        (is (identical? sentinel result))))
    (let [sentinel (ex-info "native parser refusal"
                            {:source-id :owner-id :reader-options :owner-options})
          result (with-redefs [bootstrap/stage1-reader-products-from-token-stream
                               (fn [& _] (throw sentinel))]
                   (rejection #(compatibility-products path "x" context)))]
      (is (not (identical? sentinel result)))
      (is (identical? sentinel (ex-cause result)))
      (is (= (ex-data sentinel) (ex-data result)))
      (is (= (ex-message sentinel) (ex-message result))))
    (let [sentinel (IllegalStateException. "opaque native parser failure")
          result (with-redefs [bootstrap/stage1-reader-products-from-token-stream
                               (fn [& _] (throw sentinel))]
                   (try (compatibility-products path "x" context)
                        (catch Throwable error error)))]
      (is (identical? sentinel result)))))

(deftest compatibility-oracle-preserves-late-owner-lookups-and-stack-containment
  (let [events (atom [])
        captured (compatibility-operations)
        replacement (fn [token source]
                      (swap! events conj :late-token)
                      (assoc token :source-id (:source-id source)))
        source-unit {:source-id :native-source}
        product
        (with-redefs [bootstrap/c2-source-unit-record
                      (fn [& _] (swap! events conj :source) source-unit)
                      bootstrap/stage1-reader-table
                      (fn [] (swap! events conj :table) :native-table)
                      bootstrap/stage1-reader-token-stream
                      (fn [& _] (swap! events conj :tokens)
                        (alter-var-root #'bootstrap/c2-token-record (constantly replacement))
                        {:tokens [{:index 0} {:index 1}]})
                      bootstrap/c2-token-record (fn [& _] (throw (AssertionError. "stale owner")))
                      bootstrap/stage1-reader-products-from-token-stream
                      (fn [_ _ _ stream] (swap! events conj :parse)
                        {:form-tree [] :root-form-ids [] :records []
                         :parsed-values (:tokens stream)})]
          (reader/compatibility-reader-products "native.gravity" "" {} {} captured))]
    (is (= [:source :table :tokens :late-token :late-token :parse] @events))
    (is (identical? source-unit (:source-unit product)))
    (is (= (:token-stream product) (:parsed-values product))))
  (let [path "src/stack.gravity"
        context (bootstrap/reader-project-context-for-source path)
        result (with-redefs [bootstrap/stage1-reader-token-stream
                             (fn [& _] (throw (StackOverflowError. "controlled native stack")))]
                 (rejection #(compatibility-products path "x" context)))]
    (is (= "C2-HASH" (:id (ex-data result))))
    (is (= [:stack-safe-reader-form-construction] (:missing-fields (ex-data result))))
    (is (= :reader-host-stack-overflow (get-in (ex-data result) [:facts :failure-kind])))
    (is (= "java.lang.StackOverflowError"
           (get-in (ex-data result) [:facts :contained-host-error])))))

(deftest compatibility-oracle-retains-failure-operator-before-span-lookup
  (let [events (atom [])
        path "src/stack-order.gravity"
        context (bootstrap/reader-project-context-for-source path)
        result
        (with-redefs [bootstrap/c2-source-unit-record (fn [& _] {:source-id :native-source})
                      bootstrap/stage1-reader-table (constantly :native-table)
                      bootstrap/stage1-reader-token-stream
                      (fn [& _] (throw (StackOverflowError. "controlled native stack")))
                      bootstrap/source-span
                      (fn [& _]
                        (swap! events conj :span)
                        (alter-var-root #'bootstrap/c2-reader-fail!
                                        (constantly (fn [& _] :late-failure)))
                        {:source path})
                      bootstrap/c2-reader-fail!
                      (fn [& _] (swap! events conj :original-failure) :original-return)]
          (compatibility-products path "x" context))]
    (is (= :original-return result))
    (is (= [:span :original-failure] @events))))

(deftest compatibility-oracle-does-not-enter-canonical-reader-admission
  (let [path (fixture "accepted" "surface-syntax" ".gravity")
        text (slurp path)
        context (bootstrap/reader-project-context-for-source path)
        forbidden (fn [& _] (throw (AssertionError. "canonical Reader route entered")))]
    (with-redefs [bootstrap/c2-reader-products forbidden
                  bootstrap/sh03-reader-resolved-result! forbidden
                  bootstrap/sh03-reader-adapt-products! forbidden
                  bootstrap/compiler-c2-reader-source-artifact forbidden]
      (is (= 2 (count (:parsed-records (compatibility-products path text context))))))))

(def ^:private native-reader-rules
  [[:source-unit-hash-stable? "C2-HASH"]
   [:token-and-form-spans-present? "C2-HASH"]
   [:abbreviation-origins-present? "C2-ABBREV"]
   [:literal-facts-present? "C2-STRING"]
   [:trivia-retained? "C2-HASH"]
   [:extension-policy-recorded? "C2-EXTENSION"]
   [:incremental-hashes-stable? "C2-HASH"]
   [:diagnostics-covered? "C2-HASH"]
   [:lexical-token-stream? "C2-HASH"]
   [:nested-form-tree? "C2-HASH"]])

(defn- validate-native-report [path artifact]
  (reader/validate-native-reader-report!
   #'bootstrap/c2-reader-capability-proof #'bootstrap/c2-reader-fail!
   #'bootstrap/source-span path artifact))

(defn- native-compatibility-view [path text]
  (let [products (compatibility-products path text (bootstrap/reader-project-context-for-source path))
        source (:source-unit products) tokens (:token-stream products) forms (:form-tree products)
        module (bootstrap/reader-module-context (:parsed-values products))
        seeds (bootstrap/c2-syntax-seed-stream path products module)
        extensions (bootstrap/c2-reader-extension-invocations forms)]
    {:kind :gravity/native-reader-compatibility-view :admission :none :authenticates-inputs? false
     :source-unit-record source :token-stream tokens :form-tree forms
     :literal-decoding-records (bootstrap/c2-literal-records forms)
     :trivia-retention-records (bootstrap/c2-trivia-records tokens)
     :reader-extension-policy {:status :registered}
     :incremental-reader-hashes (bootstrap/c2-incremental-hashes source tokens forms seeds extensions [])
     :rejected-design-coverage bootstrap/c2-reader-rejected-designs
     :semantic-error-deferment-record {:deferred? true}
     :lexical-product-validation
     (bootstrap/c2-lexical-product-validation text tokens forms (:root-form-ids products))}))

(deftest native-reader-validation-checks-real-compatibility-products-without-admission
  (doseq [extension [".gravity" ".qst"]]
    (let [path (fixture "accepted" "surface-syntax" extension)
          view (native-compatibility-view path (slurp path))]
      (is (= :none (:admission view)))
      (is (false? (:authenticates-inputs? view)))
      (is (= :partial (:status (bootstrap/c2-reader-capability-proof view))))
      (is (= :complete (validate-native-report path view)))
      (is (= :complete (bootstrap/c2-reader-validate! path view)))
      (doseq [[altered expected-id field]
              [[(assoc-in view [:source-unit-record :source-id] "changed") "C2-HASH" :source-unit-hash-stable?]
               [(assoc view :literal-decoding-records []) "C2-STRING" :literal-facts-present?]
               [(dissoc view :reader-extension-policy) "C2-EXTENSION" :extension-policy-recorded?]
               [(assoc-in view [:incremental-reader-hashes :token-stream] "changed") "C2-HASH" :incremental-hashes-stable?]
               [(assoc view :lexical-product-validation {}) "C2-HASH" :lexical-token-stream?]]]
        (let [failure (rejection #(validate-native-report path altered))]
          (is (= expected-id (:id (ex-data failure))))
          (is (= [field] (:missing-fields (ex-data failure))))
          (is (= path (get-in (ex-data failure) [:source-span :source]))))))))

(deftest native-reader-validation-preserves-every-rule-and-native-truthiness
  (let [path "native.gravity" span {:source path :byte-start 0 :byte-end 1}
        artifact {:source-unit-record {:source-id "sha256:native" :reader-options {}}
                  :token-stream [{:span span}]}
        truthy (zipmap (map first native-reader-rules) (repeat :native-truthy))]
    (with-redefs [bootstrap/c2-reader-capability-proof (constantly truthy)
                  bootstrap/c2-reader-fail! (fn [& _] (throw (AssertionError. "truthy field rejected")))
                  bootstrap/source-span (fn [& _] (throw (AssertionError. "unneeded fallback")))]
      (is (= :complete (validate-native-report path artifact))))
    (doseq [[field id] native-reader-rules
            absent [nil false]]
      (with-redefs [bootstrap/c2-reader-capability-proof (constantly (assoc truthy field absent))]
        (let [failure (rejection #(validate-native-report path artifact))]
          (is (= id (:id (ex-data failure))))
          (is (= [field] (:missing-fields (ex-data failure))))
          (is (= (assoc span :file "sha256:native")
                 (:source-span (ex-data failure)))))))))

(deftest native-reader-validation-retains-repeated-failure-and-late-span-order
  (let [events (atom [])
        later (fn [id _ _ extra] (swap! events conj [:late id (:missing-fields extra)]) :ignored)
        result
        (with-redefs [bootstrap/c2-reader-capability-proof
                      (fn [_] (swap! events conj :proof) {})
                      bootstrap/source-span
                      (fn [& _] (swap! events conj :span)
                        (alter-var-root #'bootstrap/c2-reader-fail! (constantly later))
                        {:source "fallback"})
                      bootstrap/c2-reader-fail!
                      (fn [id _ _ extra] (swap! events conj [:first id (:missing-fields extra)]) :ignored)]
          (validate-native-report "native.gravity" {}))
        failures (filterv vector? @events)]
    (is (= :complete result))
    (is (= :proof (first @events)))
    (is (= 10 (count (filter #{:span} @events))))
    (is (= (mapv (fn [[field id]] [id [field]]) native-reader-rules)
           (mapv #(subvec % 1) failures)))
    (is (= [:first] (mapv first (take 1 failures))))
    (is (every? #(= :late (first %)) (rest failures)))))

(deftest native-reader-validation-propagates-owner-exceptions-with-original-identity
  (doseq [dependency [#'bootstrap/c2-reader-capability-proof #'bootstrap/c2-reader-fail!
                      #'bootstrap/source-span]]
    (let [sentinel (ex-info "native owner exception" {:owner dependency})
          result
          (with-redefs [bootstrap/c2-reader-capability-proof (constantly {})
                        bootstrap/c2-reader-fail! (fn [& _] :ignored)
                        bootstrap/source-span (fn [& _] {})]
            (with-redefs-fn {dependency (fn [& _] (throw sentinel))}
              #(rejection (fn [] (validate-native-report "native.gravity" {})))))]
      (is (identical? sentinel result)))))
