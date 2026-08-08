(ns gravity.p15-native-runtime-driver-test
  "Focused evidence for the bounded native packet runtime.

  A Gravity-authored semantic contract is bound to an independently reviewable
  host-C provider by SHA-256.  The provider is compiled with the repository's
  strict C11 flags and invoked directly with a deliberately minimal
  environment.  No Clojure or JVM executable is made available to the child.
  These tests prove only the selected runtime-provider slice; packet compiler,
  verifier, artifact construction, public wrapper, self-hosting,
  full-language, and release boundaries remain explicitly seed-bound.
  "
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing run-tests]]
            [gravity.bootstrap :as bootstrap]
            [gravity.p15-native-packet-binding :as packet-binding])
  (:import [java.io PushbackReader StringReader]
           [java.nio ByteBuffer]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.nio.file Files LinkOption OpenOption Path Paths]
           [java.nio.file.attribute FileAttribute PosixFilePermissions]
           [java.security MessageDigest]
           [java.util.concurrent TimeUnit]))

(def ^:private compiler "/usr/bin/cc")
(def ^:private file-inspector "/usr/bin/file")
(def ^:private source-relative "bootstrap/native/p15_native_runtime_driver.c")
(def ^:private test-source-relative
  "bootstrap/clojure/test/gravity/p15_native_runtime_driver_test.clj")
(def ^:private contract-relative
  "bootstrap/gravity/p15_s23/native_runtime_driver.gravity")
(def ^:private fixture-root-relative
  "bootstrap/clojure/fixtures/p15-native-runtime-driver")
(def ^:private artifact-relative
  "docs/artifacts/phase-15/native-runtime/p15-s23-bounded-native-runtime-provider.edn")
(def ^:private artifact-read-limit 262144)
(def ^:private bounded-source-read-limit 1048576)
(def ^:private required-runtime-env-var
  "GRAVITY_P15_NATIVE_RUNTIME_REQUIRED")
(def ^:private source-fixture-relative
  (str fixture-root-relative "/accepted-print.gravity"))
(def ^:private packet-limit 65536)
(def ^:private process-timeout-ms 5000)
(def ^:private minimal-environment
  {"PATH" "/nonexistent"
   "LANG" "C"
   "LC_ALL" "C"})
(def ^:private no-follow-options
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
(def ^:private c-flags
  ["-arch" "arm64" "-std=c11" "-O0" "-Wall" "-Wextra" "-Werror" "-pedantic"
   "-Wno-deprecated-declarations"])

(def ^:private reviewed-fixture-relatives
  ["accepted-branch.gravity"
   "accepted-branch.payload"
   "accepted-print.gravity"
   "accepted-print.payload"
   "accepted-print.qst"
   "accepted-str.gravity"
   "accepted-str.payload"
   "rejected-halt.payload"
   "rejected-int-leading-zero.payload"
   "rejected-int-negative-zero.payload"
   "rejected-int-plus.payload"
   "rejected-invalid-utf8-ff.payload"
   "rejected-invalid-utf8-overlong.payload"
   "rejected-jump-leading-zero.payload"
   "rejected-jump-negative-zero.payload"
   "rejected-jump-plus.payload"
   "rejected-missing-halt.payload"
   "rejected-operand.payload"
   "rejected-output-overflow.payload"
   "rejected-underflow.payload"
   "rejected-unsupported.payload"
   "rejected-value-overflow.payload"])

(def ^:private authenticated-fixture-relatives
  ["bound-packet.gravity"
   "bound-packet.qst"
   "rejected-bound-if.gravity"
   "rejected-bound-let.gravity"])

(def ^:private reviewed-accepted-evidence
  [{:source (str fixture-root-relative "/accepted-print.gravity")
    :extension ".gravity"
    :payload (str fixture-root-relative "/accepted-print.payload")
    :stdout "Hello Gravity\n"
    :exit 0}
   {:source (str fixture-root-relative "/accepted-print.qst")
    :extension ".qst"
    :payload (str fixture-root-relative "/accepted-print.payload")
    :stdout "Hello Gravity\n"
    :exit 0}
   {:source (str fixture-root-relative "/accepted-branch.gravity")
    :payload (str fixture-root-relative "/accepted-branch.payload")
    :stdout "ok\n"
    :exit 0}
   {:source (str fixture-root-relative "/accepted-str.gravity")
    :payload (str fixture-root-relative "/accepted-str.payload")
    :stdout "name42\n"
    :exit 0}])

(def ^:private reviewed-rejected-evidence
  {:stable-exit 125
   :diagnostics ["P15NR001" "P15NR002" "P15NR003" "P15NR004" "P15NR005"
                 "P15NR006" "P15NR007" "P15NR008" "P15NR009" "P15NR010"]
   :families [:bad-cli-usage :packet-bound :embedded-nul :header-shape
              :source-extension :runtime-rule-tamper :payload-hash-tamper
              :unsupported-operation :invalid-utf8 :noncanonical-integer
              :noncanonical-instruction-count :stack-underflow
              :bounded-value-overflow :bounded-output-overflow :invalid-halt
              :missing-halt]})

(def ^:private reviewed-seed-boundary
  {:selected-runtime-invokes-clojure? false
   :selected-runtime-invokes-jvm? false
   :selected-runtime-clojure-seed-boundary? false
   :selected-child-clojure-seed-boundary? false
   :compiler-clojure-seed-boundary? true
   :adapter-clojure-seed-boundary? true
   :verifier-clojure-seed-boundary? true
   :artifact-clojure-seed-boundary? true
   :artifact-construction-clojure-seed-boundary? true
   :process-clojure-seed-boundary? true
   :file-io-clojure-seed-boundary? true
   :process-and-file-io-clojure-seed-boundary? true
   :public-clojure-seed-boundary? true
   :public-wrapper-clojure-seed-boundary? true
   :global-clojure-seed-boundary? true
   :public-path-boundary-reduced? false
   :clojure-seed-boundary? true})

(def ^:private reviewed-limitations
  {:public-command-route? false
   :descriptor-relative-execution? false
   :os-process-tree-containment? false
   :packet-signature-verified? false
   :source-content-hash-verified-by-provider? false
   :compiler-authored-in-gravity? false
   :provider-authored-in-gravity? false
   :whole-language? false
   :formal-language-complete? false
   :full-language-completion-count 0
   :self-hosted? false
   :release-ready? false
   :seedless-release? false})

(defn- repository-root
  []
  (let [resource (io/resource "gravity/p15_native_runtime_driver_test.clj")]
    (when-not resource
      (throw (ex-info "P15 native runtime test source is not on the classpath"
                      {:id "P15NR-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "P15 native runtime repository root is unavailable"
                        {:id "P15NR-TEST-ROOT"}))
        (Files/isRegularFile (.resolve candidate "deps.edn")
                             (make-array LinkOption 0))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (.resolve ^Path @root relative))

(defn- required-native-runtime?
  []
  (= "1" (System/getenv required-runtime-env-var)))

(defn- require-artifact!
  [condition details]
  (when-not condition
    (throw (ex-info "P15 native runtime artifact contract mismatch"
                    (merge {:id "P15NR-ARTIFACT-CONTRACT"} details))))
  true)

(defn- read-bounded-regular-file
  "Read one repository-relative regular file after a no-follow and size check.

  The caller deliberately supplies a fixed relative path and a small bound;
  this is source-only evidence, not a claim of race-free artifact attestation.
  "
  [relative maximum-bytes]
  (let [target (path relative)]
    (require-artifact! (not (Files/isSymbolicLink target))
                       {:path relative :reason :symlink})
    (require-artifact! (Files/isRegularFile target no-follow-options)
                       {:path relative :reason :not-regular-file})
    (let [before-size (Files/size target)
          before-key (Files/getAttribute target "basic:fileKey" no-follow-options)]
      (require-artifact! (<= before-size maximum-bytes)
                         {:path relative :size before-size
                          :maximum-bytes maximum-bytes})
      (require-artifact! (some? before-key)
                         {:path relative :reason :identity-unavailable})
      (try
        (with-open [channel (Files/newByteChannel
                             target
                             (into-array OpenOption
                                         [java.nio.file.StandardOpenOption/READ
                                          LinkOption/NOFOLLOW_LINKS]))]
          (let [buffer (ByteBuffer/allocate (inc maximum-bytes))]
            (loop []
              (let [read (.read channel buffer)]
                (cond
                  (= -1 read)
                  (let [after-size (.size channel)
                        after-key (Files/getAttribute target "basic:fileKey"
                                                      no-follow-options)
                        size (.position buffer)]
                    (require-artifact! (= before-size after-size)
                                       {:path relative :before-size before-size
                                        :after-size after-size})
                    (require-artifact! (= before-key after-key)
                                       {:path relative :reason :identity-changed})
                    (require-artifact! (<= size maximum-bytes)
                                       {:path relative :reason :read-exceeded-bound
                                        :maximum-bytes maximum-bytes})
                    (let [bytes (byte-array size)]
                      (.flip buffer)
                      (.get buffer bytes)
                      bytes))

                  (>= (.position buffer) (inc maximum-bytes))
                  (throw (ex-info "P15 native runtime artifact exceeds read bound"
                                  {:id "P15NR-ARTIFACT-BOUND"
                                   :path relative
                                   :maximum-bytes maximum-bytes}))

                  :else
                  (recur))))))
        (catch Exception error
          (throw (ex-info "P15 native runtime bounded read failed"
                          {:id "P15NR-ARTIFACT-READ"
                           :path relative}
                          error)))))))

(defn- strict-utf8
  [^bytes bytes]
  (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
                  (.onUnmappableCharacter java.nio.charset.CodingErrorAction/REPORT))]
    (str (.decode decoder (ByteBuffer/wrap bytes)))))

(defn- exact-map-keys
  [value expected label]
  (require-artifact! (map? value) {:label label :value value})
  (require-artifact! (= expected (set (keys value)))
                     {:label label :keys (set (keys value))}))

(defn- read-artifact
  []
  (when-let [bytes (read-bounded-regular-file artifact-relative artifact-read-limit)]
    (try
      (let [sentinel (Object.)]
        (with-open [reader (PushbackReader.
                            (StringReader. (strict-utf8 bytes)))]
          (let [value (edn/read {:eof sentinel} reader)
                trailing (edn/read {:eof sentinel} reader)]
            (require-artifact! (not (identical? value sentinel))
                               {:path artifact-relative :reason :missing-edn-form})
            (require-artifact! (identical? trailing sentinel)
                               {:path artifact-relative :reason :trailing-edn-form})
            value)))
      (catch Exception error
        (throw (ex-info "P15 native runtime artifact EDN parse failed"
                        {:id "P15NR-ARTIFACT-EDN"
                         :path artifact-relative}
                        error))))))

(defn- assert-reviewed-fixture-set
  [expected-relatives exact-directory?]
  (let [directory (path fixture-root-relative)]
    (require-artifact! (not (Files/isSymbolicLink directory))
                       {:path fixture-root-relative :reason :symlink})
    (require-artifact! (Files/isDirectory directory no-follow-options)
                       {:path fixture-root-relative :reason :not-directory})
    (let [entries (with-open [stream (Files/list directory)]
                    (vec (iterator-seq (.iterator stream))))
          actual (sort (mapv #(str fixture-root-relative "/" (.getFileName ^Path %))
                             entries))
          expected (sort (mapv #(str fixture-root-relative "/" %)
                               expected-relatives))]
      (doseq [entry entries]
        (require-artifact! (not (Files/isSymbolicLink entry))
                           {:path (str entry) :reason :symlink})
        (require-artifact! (Files/isRegularFile entry no-follow-options)
                           {:path (str entry) :reason :not-regular-file}))
      (require-artifact!
       (if exact-directory?
         (= expected actual)
         (every? (set actual) expected))
       {:expected expected :actual actual :exact-directory? exact-directory?})
      (doseq [relative expected-relatives]
        (read-bounded-regular-file (str fixture-root-relative "/" relative)
                                   bounded-source-read-limit)))))

(defn- arm64-darwin-toolchain-available?
  []
  (and (= "Mac OS X" (System/getProperty "os.name"))
       (contains? #{"aarch64" "arm64"} (System/getProperty "os.arch"))
       (Files/isExecutable (Paths/get compiler (make-array String 0)))
       (Files/isExecutable (Paths/get file-inspector (make-array String 0)))))

(defn- owner-only-attributes
  []
  (into-array FileAttribute
              [(PosixFilePermissions/asFileAttribute
                (PosixFilePermissions/fromString "rwx------"))]))

(defn- delete-tree!
  [^Path directory]
  (when (Files/exists directory no-follow-options)
    (let [entries (with-open [stream (Files/walk
                                      directory
                                      (make-array java.nio.file.FileVisitOption 0))]
                    (vec (iterator-seq (.iterator stream))))]
      (doseq [entry (reverse entries)]
        (Files/deleteIfExists ^Path entry)))))

(defn- with-private-root
  [f]
  (let [directory (Files/createTempDirectory "gravity-p15-native-runtime-"
                                             (owner-only-attributes))]
    (try
      (f directory)
      (finally
        (delete-tree! directory)))))

(defn- utf8-bytes
  [value]
  (.getBytes ^String value StandardCharsets/UTF_8))

(defn- sha256-hex
  [^bytes bytes]
  (let [digest (.digest (doto (MessageDigest/getInstance "SHA-256")
                          (.update bytes)))]
    (apply str (map #(format "%02x" (bit-and (int %) 0xff)) digest))))

(defn- sha256-file
  [relative]
  (when-let [bytes (read-bounded-regular-file relative bounded-source-read-limit)]
    (str "sha256:" (sha256-hex bytes))))

(defn- hex-encode
  [^bytes bytes]
  (apply str (map #(format "%02x" (bit-and (int %) 0xff)) bytes)))

(defn- payload-instruction-count
  [payload]
  (count (remove str/blank? (str/split payload #"\n" -1))))

(defn- run-process!
  "Run one direct process with all input/output files in the test-owned root."
  [^Path directory command input-bytes]
  (let [input (.resolve directory "process-input")
        stdout (.resolve directory "process-stdout")
        stderr (.resolve directory "process-stderr")]
    (Files/write input (or input-bytes (byte-array 0))
                 (make-array OpenOption 0))
    (let [builder (doto (ProcessBuilder. ^java.util.List (vec command))
                    (.directory (.toFile directory))
                    (.redirectInput (.toFile input))
                    (.redirectOutput (.toFile stdout))
                    (.redirectError (.toFile stderr)))
          environment (.environment builder)]
      (.clear environment)
      (doseq [[key value] minimal-environment]
        (.put environment key value))
      (let [process (.start builder)
            completed? (.waitFor process process-timeout-ms
                                 TimeUnit/MILLISECONDS)]
        (when-not completed?
          (.destroyForcibly process)
          (.waitFor process process-timeout-ms TimeUnit/MILLISECONDS))
        {:command (vec command)
         :environment (into {} environment)
         :completed? completed?
         :exit (when completed? (.exitValue process))
         :out (String. (Files/readAllBytes stdout) StandardCharsets/UTF_8)
         :err (String. (Files/readAllBytes stderr) StandardCharsets/UTF_8)}))))

(defn- compile-provider!
  [^Path directory]
  (let [output (.resolve directory "p15-native-runtime-driver")
        result (run-process!
                directory
                (concat [compiler] c-flags
                        [(str (path source-relative)) "-o" (str output)])
                nil)]
    (assoc result
           :output output
           :accepted? (and (:completed? result)
                           (zero? (long (or (:exit result) -1)))
                           (Files/isRegularFile output no-follow-options)
                           (Files/isExecutable output)))))

(defn- fixture-path
  [filename]
  (path (str fixture-root-relative "/" filename)))

(defn- fixture-text
  [filename]
  (String. (Files/readAllBytes (fixture-path filename)) StandardCharsets/UTF_8))

(defn- fixture-source
  ([] (fixture-source source-fixture-relative))
  ([relative]
   {:source-path relative
    :bytes (Files/readAllBytes (path relative))}))

(defn- packet
  "Build the exact six-line header plus delimiter and canonical payload.

  Header overrides are intentionally exposed to negative tests so each
  rejection is caused by one changed field rather than a replay or manifest.
  "
  [rule-hash payload {:keys [source-path bytes source-bytes rule-sha256
                             source-sha256 payload-sha256 instruction-count]
                      :or {source-path (:source-path (fixture-source))}}]
  (let [source-bytes (or source-bytes bytes (:bytes (fixture-source)))
        payload-bytes (utf8-bytes payload)
        source-sha256 (or source-sha256 (sha256-hex source-bytes))
        payload-sha256 (or payload-sha256 (sha256-hex payload-bytes))
        rule-sha256 (or rule-sha256 rule-hash)
        resolved-instruction-count
        (or instruction-count (payload-instruction-count payload))
        packet-text
        (str "gravity-native-runtime-v1\n"
             "rule-sha256 " rule-sha256 "\n"
             "source-path-hex " (hex-encode (utf8-bytes source-path)) "\n"
             "source-sha256 " source-sha256 "\n"
             "payload-sha256 " payload-sha256 "\n"
             "instruction-count " resolved-instruction-count "\n"
             "--\n" payload)]
    {:text packet-text
     :bytes (utf8-bytes packet-text)
     :source-path source-path
     :source-sha256 source-sha256
     :payload-sha256 payload-sha256
     :rule-sha256 rule-sha256
     :instruction-count resolved-instruction-count}))

(defn- assert-minimal-environment
  [result]
  (is (= minimal-environment (:environment result))
      (select-keys result [:command :environment])))

(defn- assert-rejected
  [result diagnostic-id source-path]
  (is (:completed? result) result)
  (is (= 125 (:exit result))
      (assoc result :expected-diagnostic diagnostic-id))
  (let [diagnostic (edn/read-string (:err result))]
    (is (= #{:diagnostic :severity :stage :message :source-path :profile
             :target :runtime-provider :remediation :public-command-route?
             :clojure-seed-boundary? :self-hosted?}
           (set (keys diagnostic)))
        diagnostic)
    (is (= diagnostic-id (:diagnostic diagnostic)) diagnostic)
    (is (= (or source-path "packet:unknown") (:source-path diagnostic))
        diagnostic)
    (is (= {:severity :error
            :stage :p15-native-runtime-driver
            :profile :native
            :target :arm64-macos
            :runtime-provider :gravity.native/libsystem-stdio-v1
            :remediation :repair_bounded_native_runtime_packet
            :public-command-route? false
            :clojure-seed-boundary? true
            :self-hosted? false}
           (select-keys diagnostic
                        [:severity :stage :profile :target :runtime-provider
                         :remediation :public-command-route?
                         :clojure-seed-boundary? :self-hosted?]))
        diagnostic)))

(defn- run-packet!
  [^Path directory binary packet-record]
  (run-process! directory [(str binary)] (:bytes packet-record)))

(defn- with-provider
  [f]
  (if-not (arm64-darwin-toolchain-available?)
    (if (required-native-runtime?)
      (throw (ex-info
              "required native-runtime provider execution is unavailable"
              {:id "P15NR-PLATFORM-UNSUPPORTED"
               :required-environment required-runtime-env-var}))
      (is true (str "no native-runtime claim: an executable ARM64 macOS "
                    "Clang/file toolchain is unavailable")))
    (with-private-root
      (fn [directory]
        (let [compiled (compile-provider! directory)]
          (is (:accepted? compiled) compiled)
          (when (:accepted? compiled)
            (f directory (:output compiled) compiled)))))))

(def ^:private real-stage2-packet-cache (atom {}))

(defn- real-stage2-packet
  [relative]
  (or (get @real-stage2-packet-cache relative)
      (let [source-bytes (Files/readAllBytes (path relative))
            decoder (doto (.newDecoder StandardCharsets/UTF_8)
                      (.onMalformedInput CodingErrorAction/REPORT)
                      (.onUnmappableCharacter CodingErrorAction/REPORT))
            source-text (str (.decode decoder (ByteBuffer/wrap source-bytes)))
            context (bootstrap/p15-s23-closed-runtime-packet-context
                     relative source-text :c)
            result
            {:packet (bootstrap/stage2-runtime-derived-packet
                      relative source-text :c)
             :context context}]
        (get (swap! real-stage2-packet-cache
                    #(if (contains? % relative) % (assoc % relative result)))
             relative))))

(defn- bind-and-run-provider!
  [counter directory binary packet context]
  (let [binding (packet-binding/bind-native-runtime-packet packet context)]
    (swap! counter inc)
    {:binding binding
     :execution (run-process! directory [(str binary)]
                              (get-in binding [:wire :bytes]))}))

(defn- diagnostic-id
  [f]
  (try
    (f)
    nil
    (catch clojure.lang.ExceptionInfo ex
      (:id (ex-data ex)))))

(defn- artifact-shared-identity!
  "Validate shared artifact identity without enumerating profile fixtures.

  Both fixed profiles call this helper so the packet-binding profile remains
  independently useful when selected without the fast contract profile.
  "
  [artifact]
  (let [contract-hash (sha256-file contract-relative)
        provider-hash (sha256-file source-relative)
        test-source-hash (sha256-file test-source-relative)]
    (require-artifact! (map? artifact) {:label :artifact :value artifact})
    (exact-map-keys
     artifact
     #{:artifact :schema-version :status :scope :authority :semantic-contract :provider
       :packet-contract :packet-binding :accepted-evidence :rejected-evidence
       :focused-validation :seed-boundary :limitations}
     :artifact)
    (require-artifact! (= :gravity/p15-s23-bounded-native-runtime-provider-proof
                          (:artifact artifact)) artifact)
    (require-artifact! (= 1 (:schema-version artifact)) artifact)
    (require-artifact!
     (= :complete-for-internal-bounded-native-runtime-provider (:status artifact))
     artifact)
    (require-artifact! (= :content-bound-precompiled-packet-runtime (:scope artifact))
                       artifact)
    (require-artifact! (= :none (:authority artifact)) artifact)
    (let [semantic-contract (:semantic-contract artifact)
          provider (:provider artifact)
          focused (:focused-validation artifact)]
      (exact-map-keys semantic-contract #{:path :content-hash :owner}
                      :semantic-contract)
      (require-artifact!
       (= {:path contract-relative :content-hash contract-hash :owner :gravity-source}
          semantic-contract)
       semantic-contract)
      (exact-map-keys
       provider
       #{:path :content-hash :implementation :target :profile
         :runtime-provider :allocation-provider :packet-transport
         :application-output :diagnostics :build-command :observed-binary-kind
         :sample-binary-content-hash :reproducible-binary-proven?
         :undefined-imports :process-imports? :filesystem-imports?
         :network-imports? :dynamic-loader-imports?}
       :provider)
      (require-artifact! (= source-relative (:path provider)) provider)
      (require-artifact! (= provider-hash (:content-hash provider)) provider)
      (require-artifact!
       (= {:implementation :host-authored-c
           :target :arm64-macos
           :profile :native
           :runtime-provider :gravity.native/libsystem-stdio-v1
           :allocation-provider :gravity.native/bounded-stack-storage-v1
           :packet-transport :inherited-stdin
           :application-output :inherited-stdout
           :diagnostics :inherited-stderr
           :observed-binary-kind "Mach-O 64-bit executable arm64"
           :reproducible-binary-proven? false
           :process-imports? false
           :filesystem-imports? false
           :network-imports? false
           :dynamic-loader-imports? false}
          (select-keys provider
                       [:implementation :target :profile :runtime-provider
                        :allocation-provider :packet-transport :application-output
                        :diagnostics :observed-binary-kind
                        :reproducible-binary-proven? :process-imports?
                        :filesystem-imports? :network-imports?
                        :dynamic-loader-imports?]))
       provider)
      (require-artifact!
       (= ["/usr/bin/cc" "-arch" "arm64" "-std=c11" "-O0" "-Wall"
           "-Wextra" "-Werror" "-pedantic" "-Wno-deprecated-declarations"
           "bootstrap/native/p15_native_runtime_driver.c" "-o" "<private-output>"]
          (:build-command provider))
       provider)
      (require-artifact!
       (= "sha256:ebbcb45ad69700ca83588dc475bf6083b2606e5ce9981c40ffeb0873188b4879"
          (:sample-binary-content-hash provider))
       provider)
      (require-artifact!
       (= ["_CC_SHA256" "___chkstk_darwin" "___memcpy_chk" "___snprintf_chk"
           "___stack_chk_fail" "___stack_chk_guard" "___stderrp" "___stdinp"
           "___stdoutp" "_bzero" "_ferror" "_fflush" "_fprintf" "_fread"
           "_fwrite" "_memchr" "_memcpy" "_printf" "_puts" "_strchr"
           "_strcmp" "_strlen" "_strncmp" "_strstr"]
          (:undefined-imports provider))
       provider)
      (let [packet-contract (:packet-contract artifact)]
        (exact-map-keys
         packet-contract
         #{:format :maximum-packet-bytes :maximum-instructions
           :maximum-stack-values :maximum-value-bytes :maximum-output-bytes
           :payload-content-binding :runtime-rule-binding
           :source-path-and-extension-preserved? :source-sha256-declared?
           :source-content-hash-verified-by-provider?
           :trusted-packet-constructor :authenticated-packet-adapter
           :supported-operations
           :canonical-integer-grammar? :strict-utf8? :embedded-nul-rejected?
           :stdout-buffered-until-complete-halt?}
         :packet-contract)
        (require-artifact!
         (= {:format "gravity-native-runtime-v1"
             :maximum-packet-bytes 65536
             :maximum-instructions 128
             :maximum-stack-values 128
             :maximum-value-bytes 1024
             :maximum-output-bytes 8192
             :payload-content-binding :sha256
             :runtime-rule-binding :gravity-source-sha256
             :source-path-and-extension-preserved? true
             :source-sha256-declared? true
             :source-content-hash-verified-by-provider? false
             :trusted-packet-constructor :clojure-test-and-bootstrap-boundary
             :authenticated-packet-adapter
             'gravity.p15-native-packet-binding/bind-native-runtime-packet
             :supported-operations
             (vec '(push-string push-int push-bool push-nil str println jump
                    jump-if-false halt))
             :canonical-integer-grammar? true
             :strict-utf8? true
             :embedded-nul-rejected? true
             :stdout-buffered-until-complete-halt? true}
            packet-contract)
         packet-contract))
      (exact-map-keys focused
                      #{:test-namespace :tests :assertions :failures :errors
                        :receipt-scope :test-source-content-hash :profiles
                        :historical-receipt :coverage-audit
                        :gravity-contract-check}
                      :focused-validation)
      (require-artifact! (= 'gravity.p15-native-runtime-driver-test
                            (:test-namespace focused)) focused)
      (require-artifact!
       (= {:tests 15 :assertions 305 :failures 0 :errors 0}
          (select-keys focused [:tests :assertions :failures :errors]))
       focused)
      (exact-map-keys (:profiles focused)
                      #{:fast :authenticated-packet-binding}
                      :focused-validation-profiles)
      (exact-map-keys (:fast (:profiles focused))
                      #{:tests :assertions :failures :errors}
                      :focused-validation-fast-profile)
      (require-artifact!
       (= {:tests 10 :assertions 235 :failures 0 :errors 0}
          (select-keys (:fast (:profiles focused))
                       [:tests :assertions :failures :errors]))
       (:fast (:profiles focused)))
      (exact-map-keys (:authenticated-packet-binding (:profiles focused))
                      #{:tests :assertions :failures :errors}
                      :focused-validation-authenticated-profile)
      (require-artifact!
       (= {:tests 5 :assertions 70 :failures 0 :errors 0}
          (select-keys (:authenticated-packet-binding (:profiles focused))
                       [:tests :assertions :failures :errors]))
       (:authenticated-packet-binding (:profiles focused)))
      (require-artifact!
       (= test-source-hash (:test-source-content-hash focused))
       {:expected test-source-hash
        :actual (:test-source-content-hash focused)})
      (require-artifact! (= :source-only-census (:receipt-scope focused)) focused)
      (exact-map-keys (:historical-receipt focused)
                      #{:tests :assertions :failures :errors :supervisor
                        :canonical-lock :run-id
                        :elapsed-seconds :peak-rss-bytes :peak-process-count
                        :log-content-hash :status-content-hash
                        :test-source-content-hash :attempt-history
                        :authority}
                      :historical-receipt)
      (require-artifact!
       (= {:tests 13 :assertions 303 :failures 0 :errors 0}
          (select-keys (:historical-receipt focused)
                       [:tests :assertions :failures :errors]))
       (:historical-receipt focused))
      (require-artifact!
       (= :historical-nonauthority
          (:authority (:historical-receipt focused)))
       (:historical-receipt focused))
      (require-artifact!
       (= {:supervisor :gravity/hardened-shared-capacity-runner
           :canonical-lock "/private/tmp/gravity-sh07-heavy.lock"}
          (select-keys (:historical-receipt focused)
                       [:supervisor :canonical-lock]))
       (:historical-receipt focused))
      (exact-map-keys (:coverage-audit focused)
                      #{:documents :full-language-complete :without-executable-owner
                        :public-accepted :accepted-total :public-rejected-specific
                        :rejected-total}
                      :coverage-audit)
      (exact-map-keys (:gravity-contract-check focused)
                      #{:verification-profile :verification-target :result
                        :log-content-hash :status-content-hash}
                      :gravity-contract-check)
      (exact-map-keys (:seed-boundary artifact)
                      #{:selected-runtime-invokes-clojure?
                        :selected-runtime-invokes-jvm?
                        :selected-runtime-clojure-seed-boundary?
                        :selected-child-clojure-seed-boundary?
                        :compiler-clojure-seed-boundary?
                        :adapter-clojure-seed-boundary?
                        :verifier-clojure-seed-boundary?
                        :artifact-clojure-seed-boundary?
                        :artifact-construction-clojure-seed-boundary?
                        :process-clojure-seed-boundary?
                        :file-io-clojure-seed-boundary?
                        :process-and-file-io-clojure-seed-boundary?
                        :public-clojure-seed-boundary?
                        :public-wrapper-clojure-seed-boundary?
                        :global-clojure-seed-boundary?
                        :public-path-boundary-reduced?
                        :clojure-seed-boundary?}
                      :seed-boundary)
      (require-artifact! (= reviewed-seed-boundary (:seed-boundary artifact)) artifact)
      (exact-map-keys (:limitations artifact)
                      #{:public-command-route? :descriptor-relative-execution?
                        :os-process-tree-containment? :packet-signature-verified?
                        :source-content-hash-verified-by-provider?
                        :compiler-authored-in-gravity? :provider-authored-in-gravity?
                        :whole-language? :formal-language-complete?
                        :full-language-completion-count :self-hosted? :release-ready?
                        :seedless-release?}
                      :limitations)
      (require-artifact! (= reviewed-limitations (:limitations artifact)) artifact))
    true))

(defn- shared-artifact-rejected?
  "Exercise shared identity rejection without touching either fixture profile."
  [artifact]
  (try
    (artifact-shared-identity! artifact)
    false
    (catch clojure.lang.ExceptionInfo ex
      (= "P15NR-ARTIFACT-CONTRACT" (:id (ex-data ex))))))

(defn- artifact-fast-contract!
  []
  (let [artifact (read-artifact)]
    (artifact-shared-identity! artifact)
    (require-artifact! (= reviewed-accepted-evidence (:accepted-evidence artifact))
                       artifact)
    (require-artifact! (= reviewed-rejected-evidence (:rejected-evidence artifact))
                       artifact)
    (assert-reviewed-fixture-set reviewed-fixture-relatives false)
    true))

(defn- artifact-authenticated-packet-binding-contract!
  []
  (let [artifact (read-artifact)
        binding (:packet-binding artifact)
        binder-hash (sha256-file
                     "bootstrap/clojure/src/gravity/p15_native_packet_binding.clj")
        expected-api 'gravity.p15-native-packet-binding/bind-native-runtime-packet
        expected-authenticator
        'gravity.bootstrap/p15-s23-closed-runtime-packet-authentic?]
    (artifact-shared-identity! artifact)
    ;; Explicit selection of this independent profile must reject shared drift
    ;; without relying on the fast profile or reading its old fixture set.
    (doseq [[label mutated]
            [[:semantic-contract-hash
              (assoc-in artifact [:semantic-contract :content-hash]
                        "sha256:shared-contract-drift")]
             [:provider-content-hash
              (assoc-in artifact [:provider :content-hash]
                        "sha256:provider-content-drift")]
             [:test-source-content-hash
              (assoc-in artifact [:focused-validation :test-source-content-hash]
                        "sha256:test-source-drift")]
             [:provider-build-command
              (assoc-in artifact [:provider :build-command]
                        ["/usr/bin/false"])]
             [:limitations
              (assoc-in artifact [:limitations :release-ready?] true)]]]
      (require-artifact! (shared-artifact-rejected? mutated)
                         {:label label :reason :shared-drift-accepted}))
    (exact-map-keys
     binding
     #{:path :public-api :input :requested-target :accepted-source-extensions
       :packet-and-context-authenticator :authenticator-receives-both-packet-and-context?
       :authentication-completes-before-plan-lowering? :supported-plan-operations
       :supported-builtin-arities :stable-diagnostics :provider-invoked-by-binding?
       :trusted-input :raw-source-bytes-consumed-by-adapter?
       :executed-provider-binary-bound-by-adapter?
       :source-content-hash-verified-by-provider? :implementation-content-hash
       :implementation-status :focused-validation-status
       :accepted-contextual-packet-executions :pre-child-rejection-families}
     :packet-binding)
    (require-artifact!
     (= {:path "bootstrap/clojure/src/gravity/p15_native_packet_binding.clj"
         :public-api expected-api
         :input :actual-target-neutral-stage2-runtime-packet-and-exact-trusted-context
         :requested-target :c
         :accepted-source-extensions [".gravity" ".qst"]
         :packet-and-context-authenticator expected-authenticator
         :authenticator-receives-both-packet-and-context? true
         :authentication-completes-before-plan-lowering? true
         :supported-plan-operations (vec '(literal quote str println))
         :supported-builtin-arities [1 2]
         :stable-diagnostics
         {"authentication-and-tamper" "P15NP001"
          "unsupported-plan" "P15NP002"
          "wire-and-bound" "P15NP003"}
         :provider-invoked-by-binding? false
         :trusted-input :strictly-decoded-source-text-reencoded-as-utf8
         :raw-source-bytes-consumed-by-adapter? false
         :executed-provider-binary-bound-by-adapter? false
         :source-content-hash-verified-by-provider? false
         :implementation-content-hash binder-hash
         :implementation-status :complete
         :focused-validation-status :passed
         :accepted-contextual-packet-executions 2
         :pre-child-rejection-families
         [:packet-envelope-tamper :coherent-source-context-mismatch
          :coherent-source-path-mismatch :runtime-rule-tamper :plan-tamper
          :unsupported-if :unsupported-let :wire-path-bound]}
        binding)
     binding)
    (assert-reviewed-fixture-set authenticated-fixture-relatives false)
    true))

(deftest p15-native-runtime-provider-artifact-identity-and-fixture-contract
  (is (true? (artifact-fast-contract!))))

(deftest p15-native-runtime-authenticated-packet-binding-artifact-contract
  (is (true? (artifact-authenticated-packet-binding-contract!))))

(deftest p15-native-runtime-provider-strict-compiles
  (with-provider
    (fn [directory binary compiled]
      (is (= 0 (:exit compiled)) compiled)
      (is (empty? (:out compiled)) compiled)
      (is (not (str/includes? (:err compiled) "warning:")) compiled)
      (let [file-result (run-process! directory [file-inspector
                                                 (str binary)] nil)
            symbols (run-process! directory ["/usr/bin/nm" "-u"
                                             (str binary)] nil)
            forbidden
            #"(?im)\s_?(fork|vfork|exec[a-z]*|system|popen|dlopen|socket|connect|open|fopen|getenv)(?:\s|$)"]
        (assert-minimal-environment file-result)
        (is (= 0 (:exit file-result)) file-result)
        (is (re-find #"Mach-O 64-bit executable arm64" (:out file-result))
            file-result)
        (assert-minimal-environment symbols)
        (is (= 0 (:exit symbols)) symbols)
        (is (nil? (re-find forbidden (:out symbols)))
            {:undefined-symbols (:out symbols)
             :forbidden-pattern (str forbidden)})))))

(deftest p15-native-runtime-provider-boundary-is-explicitly-partial
  (with-provider
    (fn [directory binary _]
      (let [result (run-process! directory [(str binary) "--boundary"] nil)]
        (assert-minimal-environment result)
        (is (= 0 (:exit result)) result)
        (let [boundary (edn/read-string (:out result))
              contract-sha256
              (sha256-hex (Files/readAllBytes (path contract-relative)))]
          (is (= {:artifact :gravity/p15-s23-native-runtime-boundary
                  :runtime-rule-sha256 contract-sha256
                  :source-sha256-declared? true
                  :source-content-hash-verified-by-provider? false
                  :selected-runtime-clojure-seed-boundary? false
                  :compiler-clojure-seed-boundary? true
                  :verifier-clojure-seed-boundary? true
                  :artifact-construction-clojure-seed-boundary? true
                  :process-and-file-io-clojure-seed-boundary? true
                  :public-wrapper-clojure-seed-boundary? true
                  :clojure-seed-boundary? true
                  :public-command-route? false
                  :whole-language? false
                  :formal-language-complete? false
                  :self-hosted? false
                  :release-ready? false
                  :seedless-release? false}
                 boundary)
              boundary))))))

(deftest p15-native-runtime-accepted-packets-execute-without-host-tools
  (with-provider
    (fn [directory binary _]
        (let [rule-result (run-process! directory [(str binary) "--rule-hash"] nil)
            rule-hash (str/trim (:out rule-result))]
        (assert-minimal-environment rule-result)
        (is (= 0 (:exit rule-result)) rule-result)
        (is (re-matches #"[0-9a-f]{64}" rule-hash) rule-result)
        (is (= (sha256-hex (Files/readAllBytes (path contract-relative)))
               rule-hash)
            {:rule-hash rule-hash :contract contract-relative})
          (doseq [[fixture expected source]
                [["accepted-print.payload" "Hello Gravity\n"
                 (fixture-source)]
                 ["accepted-branch.payload" "ok\n"
                  (fixture-source
                   (str fixture-root-relative "/accepted-branch.gravity"))]
                 ["accepted-str.payload" "name42\n"
                  (fixture-source
                   (str fixture-root-relative "/accepted-str.gravity"))]
                 ["accepted-print.payload" "Hello Gravity\n"
                  (fixture-source
                   (str fixture-root-relative "/accepted-print.qst"))]]]
          (let [payload (fixture-text fixture)
                packet-record (packet rule-hash payload source)
                result (run-packet! directory binary packet-record)]
            (testing fixture
              (assert-minimal-environment result)
              (is (:completed? result) result)
              (is (= 0 (:exit result)) result)
              (is (= expected (:out result)) result)
              (is (= "" (:err result)) result)
              (is (= (:source-path source) (:source-path packet-record))
                  packet-record)
              (is (or (str/ends-with? (:source-path packet-record) ".gravity")
                      (str/ends-with? (:source-path packet-record) ".qst"))
                  packet-record)
              (is (= (sha256-hex (:bytes source))
                     (:source-sha256 packet-record)) packet-record))))))))

(deftest p15-native-runtime-rejects-header-provenance-and-hash-tampering
  (with-provider
    (fn [directory binary _]
      (let [rule-result (run-process! directory [(str binary) "--rule-hash"] nil)
            rule-hash (str/trim (:out rule-result))
            source (fixture-source)
            payload (fixture-text "accepted-print.payload")
            valid (packet rule-hash payload source)
            malformed (assoc valid :bytes
                             (utf8-bytes (str/replace (:text valid)
                                                     "\n--\n" "\n---\n")))
            wrong-rule (packet rule-hash payload
                               (assoc source :rule-sha256 (apply str (repeat 64 "0"))))
            wrong-payload-hash
            (packet rule-hash payload
                    (assoc source :payload-sha256 (apply str (repeat 64 "0"))))
            wrong-extension
            (packet rule-hash payload
                    (assoc source :source-path
                           "bootstrap/clojure/fixtures/p15-native-runtime-driver/not-a-source.txt"))]
        (testing "missing delimiter is rejected before provenance is trusted"
          (let [result (run-packet! directory binary malformed)]
            (assert-minimal-environment result)
            (assert-rejected result "P15NR003" nil)))
        (testing "pinned rule hash mismatch is rejected"
          (let [result (run-packet! directory binary wrong-rule)]
            (assert-minimal-environment result)
            (assert-rejected result "P15NR004" (:source-path valid))))
        (testing "payload hash tampering is rejected after declared source provenance"
          (let [result (run-packet! directory binary wrong-payload-hash)]
            (assert-minimal-environment result)
            (assert-rejected result "P15NR005" (:source-path valid))))
        (testing "source extension is preserved in declared packet provenance"
          (let [result (run-packet! directory binary wrong-extension)]
            (assert-minimal-environment result)
            (assert-rejected result "P15NR003" (:source-path wrong-extension))))))))

(deftest p15-native-runtime-rejects-embedded-nul-with-matching-hash-and-count
  (with-provider
    (fn [directory binary _]
      (let [rule-result (run-process! directory [(str binary) "--rule-hash"] nil)
            rule-hash (str/trim (:out rule-result))
            payload (str "push-nil\n" \u0000 "halt\n")
            packet-record (packet rule-hash payload (fixture-source))
            result (run-packet! directory binary packet-record)]
        (is (= (sha256-hex (utf8-bytes payload))
               (:payload-sha256 packet-record))
            packet-record)
        (is (= 2 (:instruction-count packet-record)) packet-record)
        (assert-minimal-environment result)
        (assert-rejected result "P15NR003" nil)))))

(deftest p15-native-runtime-rejects-unsupported-and-malformed-instructions
  (with-provider
    (fn [directory binary _]
      (let [rule-result (run-process! directory [(str binary) "--rule-hash"] nil)
            rule-hash (str/trim (:out rule-result))
            source (fixture-source)]
        (doseq [[fixture diagnostic-id]
                [["rejected-unsupported.payload" "P15NR006"]
                 ["rejected-operand.payload" "P15NR007"]
                 ["rejected-invalid-utf8-ff.payload" "P15NR007"]
                 ["rejected-invalid-utf8-overlong.payload" "P15NR007"]
                 ["rejected-int-plus.payload" "P15NR007"]
                 ["rejected-int-leading-zero.payload" "P15NR007"]
                 ["rejected-int-negative-zero.payload" "P15NR007"]
                 ["rejected-jump-plus.payload" "P15NR007"]
                 ["rejected-jump-leading-zero.payload" "P15NR007"]
                 ["rejected-jump-negative-zero.payload" "P15NR007"]
                 ["rejected-underflow.payload" "P15NR008"]
                 ["rejected-halt.payload" "P15NR010"]
                 ["rejected-missing-halt.payload" "P15NR010"]]]
          (let [packet-record (packet rule-hash (fixture-text fixture) source)
                result (run-packet! directory binary packet-record)]
            (testing fixture
              (assert-minimal-environment result)
              (assert-rejected result diagnostic-id (:source-path packet-record)))))))))

(deftest p15-native-runtime-rejects-cli-usage-and-bounded-overflow
  (with-provider
    (fn [directory binary _]
      (testing "bad CLI usage rejects before packet processing"
        (let [result (run-process! directory [(str binary) "--unsupported"] nil)]
          (assert-minimal-environment result)
          (assert-rejected result "P15NR001" nil)))
      (let [rule-result (run-process! directory [(str binary) "--rule-hash"] nil)
            rule-hash (str/trim (:out rule-result))
            source (fixture-source)]
        (doseq [fixture ["rejected-value-overflow.payload"
                         "rejected-output-overflow.payload"]]
          (let [packet-record (packet rule-hash (fixture-text fixture) source)
                result (run-packet! directory binary packet-record)]
            (testing fixture
              (assert-minimal-environment result)
              (assert-rejected result "P15NR009"
                               (:source-path packet-record)))))))))

(deftest p15-native-runtime-rejects-noncanonical-instruction-counts
  (with-provider
    (fn [directory binary _]
      (let [rule-result (run-process! directory [(str binary) "--rule-hash"] nil)
            rule-hash (str/trim (:out rule-result))
            payload "halt\n"
            source (fixture-source)]
        (doseq [declared-count ["+01" "01" "-0"]]
          (let [packet-record
                (packet rule-hash payload
                        (assoc source :instruction-count declared-count))
                result (run-packet! directory binary packet-record)]
            (testing (str "instruction-count " declared-count)
              (assert-minimal-environment result)
              (assert-rejected result "P15NR002"
                               (:source-path packet-record)))))))))

(deftest p15-native-runtime-rejects-overbound-packets
  (with-provider
    (fn [directory binary _]
      (let [result (run-process! directory [(str binary)]
                                 (byte-array (inc packet-limit)))]
        (assert-minimal-environment result)
        (assert-rejected result "P15NR002" nil)))))

(deftest p15-native-runtime-real-stage2-packets-bind-and-execute
  (with-provider
    (fn [directory binary _]
      (doseq [relative
              [(str fixture-root-relative "/bound-packet.gravity")
               (str fixture-root-relative "/bound-packet.qst")]]
        (testing relative
          (let [{:keys [packet context]} (real-stage2-packet relative)
                provider-calls (atom 0)
                {:keys [binding execution]}
                (bind-and-run-provider! provider-calls directory binary
                                        packet context)]
            (assert-minimal-environment execution)
            (is (= 1 @provider-calls))
            (is (= :complete-for-internal-bounded-native-runtime-provider
                   (:status binding)))
            (is (= "gravity-native-runtime-v1"
                   (get-in binding [:wire :format])))
            (is (= (:source-content-hash context)
                   (get-in binding [:source :content-hash])))
            (is (= #{:memory/allocate :io/write}
                   (get-in binding [:effects :required-effects])))
            (is (= #{:io/write}
                   (get-in binding [:effects :required-inferred-effects])))
            (is (= #{:memory/allocator :io/stdout}
                   (get-in binding [:capabilities :required-capabilities])))
            (is (false? (:source-content-hash-verified-by-provider?
                         binding)))
            (is (false? (get-in binding
                                [:provenance
                                 :selected-runtime-clojure-seed-boundary?])))
            (is (false? (get-in binding
                                [:provenance
                                 :selected-child-clojure-seed-boundary?])))
            (doseq [flag [:adapter-clojure-seed-boundary?
                          :compiler-clojure-seed-boundary?
                          :verifier-clojure-seed-boundary?
                          :artifact-clojure-seed-boundary?
                          :artifact-construction-clojure-seed-boundary?
                          :process-clojure-seed-boundary?
                          :file-io-clojure-seed-boundary?
                          :process-and-file-io-clojure-seed-boundary?
                          :public-clojure-seed-boundary?
                          :public-wrapper-clojure-seed-boundary?
                          :global-clojure-seed-boundary?]]
              (is (true? (get-in binding [:provenance flag])) flag))
            (is (= 0 (:exit execution)) execution)
            (is (= "Hello Gravity\n" (:out execution)) execution)
            (is (= "" (:err execution)) execution)
            (is (= (:expected-stdout binding) (:out execution)))
            (is (= (str "sha256:" (sha256-hex (utf8-bytes (:out execution))))
                   (:expected-stdout-hash binding)))))))))

(deftest p15-native-runtime-binding-rejects-tamper-before-provider
  (with-provider
    (fn [directory binary _]
      (let [relative (str fixture-root-relative "/bound-packet.gravity")
            {:keys [packet context]} (real-stage2-packet relative)
            provider-calls (atom 0)
            changed-source (str (:source-text context) "\n")
            changed-context
            (bootstrap/p15-s23-closed-runtime-packet-context
             relative changed-source :c)
            changed-path-context
            (bootstrap/p15-s23-closed-runtime-packet-context
             (str relative ".qst") (:source-text context) :c)
            wrong-hash (str "sha256:" (apply str (repeat 64 "0")))
            cases
            [[:packet (assoc packet :status :tampered) context]
             [:coherent-source-context packet changed-context]
             [:coherent-path-context packet changed-path-context]
             [:rule (assoc-in packet [:stage2-runtime-rule :runtime-rule-hash]
                             wrong-hash) context]
             [:plan (assoc-in packet
                              [:plan :functions (:entrypoint (:plan packet))
                               :instructions 0 :args 0 :value]
                              "tampered") context]]]
        (doseq [[label candidate candidate-context] cases]
          (testing (name label)
            (is (= "P15NP001"
                   (diagnostic-id
                    #(bind-and-run-provider! provider-calls directory binary
                                             candidate candidate-context))))))
        (is (zero? @provider-calls))))))

(deftest p15-native-runtime-binding-rejects-wire-bound-before-provider
  (with-provider
    (fn [directory binary _]
      (let [relative (str fixture-root-relative "/bound-packet.gravity")
            source-bytes (Files/readAllBytes (path relative))
            decoder (doto (.newDecoder StandardCharsets/UTF_8)
                      (.onMalformedInput CodingErrorAction/REPORT)
                      (.onUnmappableCharacter CodingErrorAction/REPORT))
            source-text (str (.decode decoder (ByteBuffer/wrap source-bytes)))
            unrepresentable-path
            (str fixture-root-relative "/bound packet.gravity")
            context (bootstrap/p15-s23-closed-runtime-packet-context
                     unrepresentable-path source-text :c)
            packet (bootstrap/stage2-runtime-derived-packet
                    unrepresentable-path source-text :c)
            provider-calls (atom 0)]
        (is (= "P15NP003"
               (diagnostic-id
                #(bind-and-run-provider! provider-calls directory binary
                                         packet context))))
        (is (zero? @provider-calls))))))

(deftest p15-native-runtime-binding-rejects-real-unsupported-plans-before-provider
  (with-provider
    (fn [directory binary _]
      (let [provider-calls (atom 0)]
        (doseq [relative
                [(str fixture-root-relative "/rejected-bound-if.gravity")
                 (str fixture-root-relative "/rejected-bound-let.gravity")]]
          (testing relative
            (let [{:keys [packet context]} (real-stage2-packet relative)]
              (is (= "P15NP002"
                     (diagnostic-id
                      #(bind-and-run-provider! provider-calls directory binary
                                               packet context)))))))
        (is (zero? @provider-calls))))))

(defn -main
  [& _]
  (let [result (run-tests 'gravity.p15-native-runtime-driver-test)]
    (when (or (pos? (:fail result)) (pos? (:error result)))
      (System/exit 1))))
