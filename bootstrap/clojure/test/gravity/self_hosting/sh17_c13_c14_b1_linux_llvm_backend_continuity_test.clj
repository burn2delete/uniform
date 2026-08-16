(ns gravity.self-hosting.sh17-c13-c14-b1-linux-llvm-backend-continuity-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [gravity.bootstrap :as bootstrap]))

;; SH-17 owns the continuity assertion for the exact Linux LLVM target.  The
;; Docker/LLVM/ELF route is opt-in so the ordinary test suite cannot silently
;; turn an emulated development observation into native or public authority.

(def ^:private source-path "sh17-c13-c14-b1-linux-llvm.gravity")
(def ^:private source-text
  (str "(ns checked.sh17 (:profile :hosted) (:target :jvm) "
       "(:safety :safe) (:effects #{}) (:capabilities #{}) "
       "(:exports [main]))\n"
       "(defn main [] 42)\n"))

(def ^:private run-linux-development-tools?
  (= "1" (System/getenv "GRAVITY_RUN_LINUX_DEVELOPMENT_TOOLS")))

(declare build-c11-for-target)

(defn- semantic-identities-resolved?
  []
  (every?
   #(and (string? %)
         (re-matches #"sha256:[0-9a-f]{64}" %))
   [bootstrap/p15-s23-c13-expected-plan-semantic-hash
    bootstrap/p15-s23-c13-expected-functions-semantic-hash
    bootstrap/p15-s23-c13-expected-builder-semantic-hash
    bootstrap/p15-s23-c14-expected-plan-semantic-hash
    bootstrap/p15-s23-c14-expected-functions-semantic-hash
    bootstrap/p15-s23-c14-expected-builder-semantic-hash
    bootstrap/p15-s23-b1-expected-plan-semantic-hash
    bootstrap/p15-s23-b1-expected-functions-semantic-hash
    bootstrap/p15-s23-b1-expected-builder-semantic-hash
    bootstrap/p15-s23-b3-llvm-expected-plan-semantic-hash
    bootstrap/p15-s23-b3-llvm-expected-functions-semantic-hash
    bootstrap/p15-s23-b3-llvm-expected-builder-semantic-hash]))

(defn- build-linux-artifact
  []
  (let [[c11 checked-core context]
        (build-c11-for-target :llvm-x86_64-linux)]
    [(bootstrap/p15-s23-stage2-b3-llvm-artifact-from-c11!
      c11 checked-core context
      (if run-linux-development-tools?
        {:run-linux-development-tools? true}
        {}))
     checked-core context]))

(defn- incomplete-candidate
  []
  {:kind :gravity/p15-s23-b3-authenticated-llvm-x86_64-linux-artifact
   :schema-version 1
   :status :incomplete
   :target :llvm-x86_64-linux
   :clojure-seed-boundary? true
   :public-target? false
   :release-credit? false
   :self-hosted? false
   :toolchain-evidence {:status :incomplete}
   :b3-record {:status :incomplete}
   :b13-record {:status :incomplete
                :artifact-files
                {:object {:format :elf}
                 :executable {:format :elf}}}
   :b14-record {:status :incomplete :same-result? :not-established}
   :c18-record {:status :incomplete}})

(defn- sha256-bytes
  [bytes]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")]
    (.update digest ^bytes bytes)
    (str "sha256:"
         (apply str
                (map #(format "%02x" (bit-and % 0xff))
                     (.digest digest))))))

(deftest sh17-linux-llvm-static-candidate-is-nonclaiming
  (let [candidate (incomplete-candidate)
        thrown
        (try
          (bootstrap/p15-s23-stage2-b3-llvm-verification-report
           candidate {} {:source-path source-path})
          nil
          (catch clojure.lang.ExceptionInfo exception
            (ex-data exception)))]
    (is (= "B3-MANIFEST" (:id thrown)))
    (is (= :complete-linux-development-evidence-required
           (get-in thrown [:facts :missing-fact])))
    (is (= :incomplete (:status candidate)))
    (is (= :incomplete (get-in candidate [:toolchain-evidence :status])))))

(deftest sh17-linux-llvm-authenticated-continuity-when-enabled
  (if-not (and run-linux-development-tools?
               (semantic-identities-resolved?))
    (is true "Docker/LLVM gate or runtime-derived source identities are absent")
    (let [[artifact checked-core context] (build-linux-artifact)
          packet (:c13-c14-b1-packet artifact)
          c11 (:c11 packet)
          c13 (:c13 packet)
          c14 (:c14 packet)
          b1 (:b1 packet)
          ids [(:artifact-id c11) (:artifact-id c13) (:artifact-id c14)
               (:artifact-id b1) (:artifact-id packet)
               (:artifact-id artifact)]
          report
          (bootstrap/p15-s23-stage2-b3-llvm-verification-report
           artifact checked-core context)
          toolchain (:toolchain-evidence artifact)
          required-tools
          #{"clang" "llc" "opt" "llvm-as" "llvm-dis" "llvm-readobj"
            "llvm-objdump" "ld.lld"}]
      (testing "distinct authenticated stage identities"
        (is (every? string? ids))
        (is (= (count ids) (count (distinct ids)))))
      (testing "canonical Linux target and ELF development boundary"
        (is (= :llvm-x86_64-linux (:target artifact)))
        (is (= :llvm-x86_64-linux
               (get-in artifact [:target-policy :canonical-target])))
        (is (= "x86_64-unknown-linux-gnu"
               (get-in artifact [:target-policy :target-triple])))
        (is (= :elf (get-in artifact [:target-policy :object-format])))
        (is (= :x86_64 (get-in artifact [:target-policy :architecture])))
        (is (= :sysv-amd64 (get-in artifact [:target-policy :abi])))
        (is (= "silkeh/clang@sha256:ae2f3deffd84470fbb2904cfb990db208a5f9880b4bcf9d3eae080a50a8900b4"
               (get-in artifact [:toolchain-evidence :image])))
        (is (= :none (get-in artifact [:toolchain-evidence :network])))
        (is (= :never (get-in artifact [:toolchain-evidence :pull-policy])))
        (is (= false (get-in artifact [:toolchain-evidence :authoritative?])))
        (is (= :passed (:status report)))
        (is (= :development-emulation-observed (:status artifact)))
        (is (= :development-emulation-observed
               (get-in artifact [:b13-record :status])))
        (is (= :development-emulation-observed
               (get-in artifact [:b14-record :status])))
        (is (= :internal-experimental-observed
               (get-in artifact [:c18-record :status])))
        (is (= 42 (get-in artifact [:b14-record :process-result
                                     :observed-exit-code])))
        (is (= required-tools
               (set (keys (get-in toolchain [:tool-hashes :hashes])))))
        (is (= required-tools
               (set (keys (get-in toolchain [:tool-paths :paths])))))
        (is (every? #(and (str/starts-with?
                          (get-in toolchain [:tool-paths :paths %]) "/")
                          (re-matches #"sha256:[0-9a-f]{64}"
                                      (get-in toolchain
                                              [:tool-hashes :hashes %])))
                    required-tools))
        (doseq [kind [:object :executable]]
          (let [file (get-in artifact [:b13-record :artifact-files kind])]
            (is (= (:byte-count file) (alength ^bytes (:bytes file))))
            (is (= (:content-hash file) (sha256-bytes (:bytes file))))
            (is (= :elf (:format file)))
            (is (= :x86_64 (:architecture file)))
            (is (re-find #"(?i)Format:\s*elf64-x86-64" (:header file)))
            (is (re-find #"(?i)Machine:\s*EM_X86_64" (:header file)))))
        (is (re-find #"(?i)Type:\s*Relocatable"
                     (get-in artifact [:b13-record :artifact-files
                                       :object :header])))
        (is (re-find #"(?i)Type:\s*Executable"
                     (get-in artifact [:b13-record :artifact-files
                                       :executable :header])))
        (is (= :observed (get-in toolchain [:b13])))
        (is (= :observed (get-in toolchain [:b14])))
        (is (= :observed (get-in toolchain [:c18])))
        (is (= :observed (get-in toolchain [:provenance :status])))
        (is (= :observed (get-in toolchain [:replay :status]))))
      (testing "authenticated lineage"
        (is (= (:artifact-id c11) (get-in c13 [:input :c11-artifact-id])))
        (is (= (:artifact-id c13) (get-in c14 [:request :input :artifact-id])))
        (is (= (:artifact-id c14)
               (get-in b1 [:backend-manifest :c14-artifact-id])))
        (is (= (:artifact-id packet)
               (get-in artifact [:c13-c14-b1-packet :artifact-id])))
        (is (= true (:clojure-seed-boundary? artifact)))
        (is (= false (:public-target? artifact)))
        (is (= false (:release-credit? artifact)))
        (is (= false (:self-hosted? artifact)))))))

(defn- build-c11-for-target
  [target]
  (let [fixture-targets
        (conj bootstrap/stage2-runtime-derived-source-targets
              :llvm-x86_64-darwin)
        context
        (binding [bootstrap/*additional-bootstrap-targets*
                  fixture-targets]
          (bootstrap/p15-s23-stage2-gravity-checked-core-context
           source-path source-text target))
        checked-core
        (bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
         context)
        c11 (bootstrap/p15-s23-stage2-c11-mir-artifact
             checked-core context)]
    [c11 checked-core context]))

(deftest sh17-hostile-targets-reject-before-tool-invocation
  (doseq [target [:llvm :llvm-x86_64-darwin]]
    (testing (str "reject target " target)
      (let [[c11 checked-core context] (build-c11-for-target target)
            before (bootstrap/p15-s23-b3-llvm-tool-execution-snapshot)
            thrown
            (try
              (bootstrap/p15-s23-stage2-b3-llvm-artifact-from-c11!
               c11 checked-core context)
              nil
              (catch clojure.lang.ExceptionInfo exception
                (ex-data exception)))
            after (bootstrap/p15-s23-b3-llvm-tool-execution-snapshot)]
        (is (= "B3-TARGET" (:id thrown)))
        (is (= :canonical-linux-target-required
               (get-in thrown [:facts :missing-fact])))
        (is (= before after))))))

(deftest sh17-linux-static-tool-and-elf-hostiles
  (let [tool-snapshot-valid?
        (deref (ns-resolve 'gravity.bootstrap
                           'p15-s23-b3-llvm-linux-tool-snapshot-valid?))
        elf-header-valid?
        (deref (ns-resolve 'gravity.bootstrap
                           'p15-s23-b3-llvm-linux-elf-header-valid?))
        elf-bytes-valid?
        (deref (ns-resolve 'gravity.bootstrap
                           'p15-s23-b3-llvm-linux-elf-bytes-valid?))
        tools ["clang" "llc" "opt" "llvm-as" "llvm-dis" "llvm-readobj"
               "llvm-objdump" "ld.lld"]
        snapshot
        (into (sorted-map)
              (map (fn [tool]
                     [tool {:path (str "/usr/bin/" tool)
                            :hash (str "sha256:" (apply str (repeat 64 "a")))}]))
              tools)
        snapshot-text
        (str/join "\n"
                  (map (fn [[tool value]]
                         (str tool " " (:path value) " " (:hash value)))
                       snapshot))
        valid-object-header
        "Format: ELF64-x86-64\nArch: x86_64\nMachine: EM_X86_64\nType: Relocatable"
        valid-executable-header
        "Format: ELF64-x86-64\nArch: x86_64\nMachine: EM_X86_64\nType: Executable"
        elf-bytes
        (fn [type machine]
          (let [bytes (byte-array 20)]
            (doseq [[index value] [[0 0x7f] [1 0x45] [2 0x4c] [3 0x46]
                                   [4 2] [5 1] [6 1]
                                   [16 (bit-and type 0xff)]
                                   [17 (bit-shift-right type 8)]
                                   [18 (bit-and machine 0xff)]
                                   [19 (bit-shift-right machine 8)]]]
              (aset-byte bytes index (unchecked-byte value)))
            bytes))]
    (is (true? (tool-snapshot-valid? snapshot-text snapshot)))
    (is (false? (tool-snapshot-valid?
                 (str/replace snapshot-text "clang /usr/bin/clang " "")
                 (dissoc snapshot "clang"))))
    (is (false? (tool-snapshot-valid?
                 (str/replace snapshot-text "/usr/bin/clang" "clang")
                 (assoc snapshot "clang" {:path "clang"
                                           :hash (str "sha256:" (apply str
                                                                         (repeat 64 "a")))}))))
    (is (true? (elf-header-valid? valid-object-header :object)))
    (is (true? (elf-header-valid? valid-executable-header :executable)))
    (is (false? (elf-header-valid?
                 (str/replace valid-executable-header
                              "Type: Executable" "Type: Dynamic")
                 :executable)))
    (is (false? (elf-header-valid?
                 (str/replace valid-executable-header
                              "Machine: EM_X86_64" "Machine: EM_AARCH64")
                 :executable)))
    (is (true? (elf-bytes-valid? (elf-bytes 1 62) :object)))
    (is (true? (elf-bytes-valid? (elf-bytes 2 62) :executable)))
    (is (false? (elf-bytes-valid?
                 (doto (elf-bytes 2 62) (aset-byte 0 (byte 0)))
                 :executable)))
    (is (false? (elf-bytes-valid? (elf-bytes 3 62) :executable)))
    (is (false? (elf-bytes-valid? (elf-bytes 2 183) :executable)))))

(deftest sh17-linux-tampered-complete-envelope-rejects-before-tools
  (let [candidate
        (-> (incomplete-candidate)
            (assoc :status :development-emulation-observed)
            (assoc-in [:b3-record :status] :development-emulation-observed)
            (assoc-in [:b13-record :status] :development-emulation-observed)
            (assoc-in [:b14-record :status] :development-emulation-observed)
            (assoc-in [:b14-record :same-result?] true)
            (assoc-in [:c18-record :status] :internal-experimental-observed)
            (assoc-in [:toolchain-evidence :status] :complete)
            (assoc-in [:toolchain-evidence :b13] :observed)
            (assoc-in [:toolchain-evidence :b14] :observed)
            (assoc-in [:toolchain-evidence :c18] :observed)
            (assoc-in [:toolchain-evidence :process-result :matched?] true)
            (assoc-in [:b13-record :artifact-files :object :content-hash]
                      "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
            (assoc-in [:b13-record :artifact-files :executable :content-hash]
                      "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"))
        before (bootstrap/p15-s23-b3-llvm-tool-execution-snapshot)
        thrown
        (try
          (bootstrap/p15-s23-stage2-b3-llvm-verification-report
           candidate {} {:source-path source-path})
          nil
          (catch clojure.lang.ExceptionInfo exception
            (ex-data exception)))
        after (bootstrap/p15-s23-b3-llvm-tool-execution-snapshot)]
    (is (= "B3-MANIFEST" (:id thrown)))
    (is (= :local-semantic-id-recompute
           (get-in thrown [:facts :missing-fact])))
    (is (= before after))))

(deftest sh17-linux-stage-content-tamper-rejects-after-envelope-rehash
  (let [digest bootstrap/p15-s23-c11-mir-digest
        seal-stage
        (fn [record]
          (let [semantic-id
                (digest
                 {:kind (:artifact record)
                  :record
                  (bootstrap/p15-s23-c13-c14-b1-stage-semantic-input
                   record)})]
            (assoc record
                   :semantic-id semantic-id
                   :artifact-id
                   (digest {:kind (:artifact record)
                            :schema-version (:schema-version record)
                            :semantic-id semantic-id}))))
        request
        {:artifact :gravity/c14-bounded-llvm-lowering-request
         :input {:artifact-id (str "sha256:" (apply str (repeat 64 "1")))}
         :target :llvm-x86_64-linux}
        request
        (assoc request :request-id
               (digest {:kind :gravity/c14-bounded-llvm-lowering-request
                        :request request}))
        c13 (seal-stage {:artifact :gravity/test-c13
                         :schema-version 1
                         :decision {:preserved? true}})
        c14 (seal-stage {:artifact :gravity/test-c14
                         :schema-version 1
                         :request request})
        b1 (seal-stage {:artifact :gravity/test-b1
                        :schema-version 1
                        :backend-manifest
                        {:c14-artifact-id (:artifact-id c14)}})
        packet-base
        {:kind :gravity/test-c13-c14-b1-packet
         :schema-version 1
         :c11 {:artifact-id (str "sha256:" (apply str (repeat 64 "0")))}
         :c13 c13 :c14 c14 :b1 b1
         :optimized-mir {:operations []}
         :actual-path-provenance {:source source-path}}
        seal-packet
        (fn [packet]
          (let [semantic-id
                (bootstrap/p15-s23-c13-c14-b1-semantic-id packet)]
            (assoc packet
                   :semantic-id semantic-id
                   :artifact-id
                   (digest {:kind (:kind packet)
                            :schema-version (:schema-version packet)
                            :semantic-id semantic-id})
                   :actual-path-binding-id
                   (bootstrap/p15-s23-c13-c14-b1-actual-path-binding-id
                    semantic-id (:actual-path-provenance packet)))))
        ;; The attacker changes stage content but leaves that stage's own ids
        ;; stale, then makes the enclosing packet and artifact look coherent.
        packet
        (seal-packet
         (assoc-in packet-base [:c13 :decision :preserved?] false))
        artifact-base
        {:kind :gravity/p15-s23-b3-authenticated-llvm-x86_64-linux-artifact
         :schema-version 1
         :c13-c14-b1-packet packet
         :actual-path-provenance {:source source-path}}
        semantic-id (bootstrap/p15-s23-b3-llvm-artifact-id artifact-base)
        artifact
        (assoc artifact-base
               :semantic-id semantic-id
               :artifact-id
               (digest {:kind (:kind artifact-base)
                        :schema-version (:schema-version artifact-base)
                        :semantic-id semantic-id})
               :actual-path-binding-id
               (bootstrap/p15-s23-b3-llvm-actual-path-binding-id
                semantic-id (:actual-path-provenance artifact-base)))
        integrity!
        (deref (ns-resolve 'gravity.bootstrap
                           'p15-s23-b3-llvm-linux-evidence-integrity!))
        before (bootstrap/p15-s23-b3-llvm-tool-execution-snapshot)
        thrown
        (try
          (integrity! artifact source-path)
          nil
          (catch clojure.lang.ExceptionInfo exception
            (ex-data exception)))
        after (bootstrap/p15-s23-b3-llvm-tool-execution-snapshot)]
    (is (= (:semantic-id packet)
           (bootstrap/p15-s23-c13-c14-b1-semantic-id packet)))
    (is (= (:semantic-id artifact)
           (bootstrap/p15-s23-b3-llvm-artifact-id artifact)))
    (is (= "B13-HASH" (:id thrown)))
    (is (= :reproducible-c13-c14-b1-sidecar-identity
           (get-in thrown [:facts :missing-fact])))
    (is (= before after))))
