(ns gravity.backend.llvm.elf-header-adoption-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

; Development execution feedback, not Linux execution or migration evidence.
(defn- private-call [function & arguments]
  (apply (ns-resolve 'gravity.bootstrap function) arguments))

(defn- valid? [bytes kind]
  (private-call 'p15-s23-b3-llvm-linux-elf-bytes-valid? bytes kind))

(defn- header [type]
  [127 69 76 70 2 1 1 0 0 0 0 0 0 0 0 0 type 0 62 0])

(defn- native-bytes [octets]
  (byte-array (map unchecked-byte octets)))

(deftest production-predicate-executes-current-uniform-source
  (let [execute bootstrap/p15-s23-stage2-runtime-execute-function
        source-hash (str "sha256:"
                         (private-call
                          'sha256-hex
                          (slurp (io/file "bootstrap/gravity/src/gravity/backend/w5_b13_artifact_emitter.gravity"))))
        calls (atom [])]
    (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                  (fn [runtime plan function arguments]
                    (when (= 'w5-b13-linux-elf-header-prefix-valid? function)
                      (swap! calls conj {:plan plan :function function
                                         :arguments arguments}))
                    (execute runtime plan function arguments))]
      (doseq [[kind type] [[:object 1] [:executable 2] [:other 2] [nil 2]]]
        (is (true? (valid? (native-bytes (header type)) kind)))
        (is (false? (valid? (native-bytes (header (if (= type 1) 2 1))) kind))))
      (doseq [[kind type] [[:object 1] [:executable 2]]]
        (is (true? (valid? (native-bytes (into (header type) [128 255])) kind)))
        (doseq [index [0 1 2 3 4 5 6 16 17 18 19]]
          (is (false? (valid? (native-bytes (assoc (header type) index 255)) kind))))))
    (is (seq @calls))
    (doseq [{:keys [plan function arguments]} @calls]
      (is (= :gravity/stage2-compiler-artifact-plan (:kind plan)))
      (is (= source-hash (get-in plan [:source :sha256])))
      (is (= 'w5-b13-linux-elf-header-prefix-valid? function))
      (is (vector? (first arguments))))
    (is (some #(= [128 255] (subvec (first (:arguments %)) 20))
              (filter #(= 22 (count (first (:arguments %)))) @calls)))))

(deftest native-admission-and-truncation-remain-fail-closed
  (let [touches (atom [])
        opaque (reify clojure.lang.Seqable
                 (seq [_] (swap! touches conj :seq) (throw (ex-info "opaque" {})))
                 Object
                 (toString [_] (swap! touches conj :string) "opaque"))]
    (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                  (fn [& _] (throw (ex-info "non-native input reached execution" {})))]
      (doseq [carrier [nil false "ELF" (header 1) (apply list (header 1))
                       {:bytes (native-bytes (header 1))} (int-array (header 1))
                       opaque]]
        (is (false? (valid? carrier :object)))))
    (is (empty? @touches)))
  (doseq [length (range 20)]
    (is (false? (valid? (native-bytes (subvec (header 1) 0 length)) :object)))))

(deftest failed-execution-cannot-use-a-host-fallback-or-success-label
  (doseq [result [nil :accepted {:status :accepted} {:valid? true} "true" 1]]
    (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function (fn [& _] result)]
      (is (false? (valid? (native-bytes (header 1)) :object)))))
  (let [failure (ex-info "Uniform execution unavailable" {:reason :unavailable})]
    (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function (fn [& _] (throw failure))]
      (is (identical? failure
                      (try (valid? (native-bytes (header 1)) :object)
                           (catch clojure.lang.ExceptionInfo error error)))))))

(def ^:private source-path "elf-header-adoption.gravity")

(defn- failure-data [f]
  (try (f) nil
       (catch clojure.lang.ExceptionInfo error (ex-data error))))

; Both wrappers delegate to production. Neither supplies an acceptance result.
(defn- trace-adoption [calls f]
  (let [predicate-var (ns-resolve 'gravity.bootstrap 'p15-s23-b3-llvm-linux-elf-bytes-valid?)
        predicate @predicate-var
        execute bootstrap/p15-s23-stage2-runtime-execute-function]
    (with-redefs-fn
      {predicate-var
       (fn [bytes kind]
         (swap! calls update :predicate (fnil conj []) [bytes kind])
         (predicate bytes kind))
       #'bootstrap/p15-s23-stage2-runtime-execute-function
       (fn [runtime plan function arguments]
         (let [result (execute runtime plan function arguments)]
           (when (= 'w5-b13-linux-elf-header-prefix-valid? function)
             (swap! calls update :uniform (fnil conj [])
                    {:source (:source plan) :arguments arguments :result result}))
           result))}
      f)))

(defn- file-snapshot [kind octets]
  (let [bytes (native-bytes octets)]
    {:status :observed :bytes bytes :byte-count (alength bytes)
     :content-hash (private-call 'p15-s23-b3-llvm-sha256-bytes bytes)
     :format :elf :architecture :x86_64
     :header (str "Format: elf64-x86-64\nArch: x86_64\nMachine: EM_X86_64\nType: "
                  (if (= kind :object) "Relocatable" "Executable"))}))

(def ^:private tool-snapshot-text
  (str/join "\n"
            (for [tool ["clang" "llc" "opt" "llvm-as" "llvm-dis" "llvm-readobj"
                        "llvm-objdump" "ld.lld"]]
              (str tool " /usr/bin/" tool " sha256:" (apply str (repeat 64 "a"))))))

(defn- observation-state []
  (let [object (file-snapshot :object (header 1))
        executable (file-snapshot :executable (header 2))
        policy @(ns-resolve 'gravity.bootstrap 'p15-s23-b3-llvm-policy)
        image @(ns-resolve 'gravity.bootstrap 'p15-s23-b3-llvm-linux-image)]
    (assoc
     (into {}
           (for [[step text]
                 [[:version-step "Docker captured version"]
                  [:info-step "Docker captured info"]
                  [:image-step image]
                  [:clang-step "clang version 20.1.8"]
                  [:llvm-step "LLVM version 20.1.8"]
                  [:triple-step (:target-triple policy)]
                  [:hashes-step tool-snapshot-text]
                  [:emulation-step "uname -m x86_64\nkernel captured-kernel\nbinfmt-status unavailable\nqemu-x86_64-static unavailable"]
                  [:object-step ""] [:link-step ""]
                  [:object-header-step (:header object)]
                  [:executable-header-step (:header executable)]
                  [:sections-step ""] [:providers-step ""] [:run-step ""]]]
             [step {:record {:step step :fixture? true}
                    :result {:stdout {:text text} :exit-code 42}}]))
     :object-snapshot object :executable-snapshot executable)))

(defn- observe! [state]
  (private-call
   'semantic-llvm-linux-toolchain-observations!
   nil source-path {:expected-exit-code 42} nil nil nil nil
   @(ns-resolve 'gravity.bootstrap 'p15-s23-b3-llvm-linux-image)
   (:target-triple @(ns-resolve 'gravity.bootstrap 'p15-s23-b3-llvm-policy))
   nil state))

(deftest toolchain-observation-consumer-executes-the-adopted-predicate
  (let [state (observation-state)
        calls (atom {})
        result (trace-adoption calls #(observe! state))]
    (is (= [:object :executable] (mapv second (:predicate @calls))))
    (doseq [[index kind] [[0 :object] [1 :executable]]]
      (is (identical? (get-in state [(keyword (str (name kind) "-snapshot")) :bytes])
                      (first (get-in @calls [:predicate index])))))
    (is (= [[(header 1) :object] [(header 2) :executable]]
           (mapv :arguments (:uniform @calls))))
    (is (= [true true] (mapv :result (:uniform @calls))))
    (is (= (:object-snapshot state) (:object-snapshot result)))
    (is (= (:executable-snapshot state) (:executable-snapshot result)))
    (is (= (select-keys (:object-snapshot state) [:byte-count :content-hash])
           (:object-content result)))
    (is (= (select-keys (:executable-snapshot state) [:byte-count :content-hash])
           (:executable-content result)))
    (is (= 15 (count (:records result))))
    (is (= 42 (:observed-exit result)))))

(deftest invalid-snapshots-keep-the-observation-diagnostic
  (doseq [[kind type] [[:object 1] [:executable 2]]
          octets [(assoc (header type) 0 0) (subvec (header type) 0 19)]]
    (let [state (assoc (observation-state)
                       (keyword (str (name kind) "-snapshot"))
                       (file-snapshot kind octets))
          calls (atom {})
          failure (trace-adoption calls #(failure-data (fn [] (observe! state))))]
      (is (= "B3-TARGET" (:id failure)))
      (is (= :pinned-llvm-20-1-8-linux-toolchain (:missing-fact failure)))
      (is (= false (:result (last (:uniform @calls)))))
      (is (= [octets kind] (:arguments (last (:uniform @calls))))))))

; Synthetic envelopes exercise local checks with real digest derivations. They
; are not compiler-produced packets and are never admitted as replay authority.
(defn- seal-stage [stage]
  (let [semantic-id
        (bootstrap/p15-s23-c11-mir-digest
         {:kind (:artifact stage)
          :record (bootstrap/p15-s23-c13-c14-b1-stage-semantic-input stage)})]
    (assoc stage :semantic-id semantic-id
           :artifact-id (bootstrap/p15-s23-c11-mir-digest
                         {:kind (:artifact stage) :schema-version (:schema-version stage)
                          :semantic-id semantic-id}))))

(defn- seal-packet [packet]
  (let [semantic-id (bootstrap/p15-s23-c13-c14-b1-semantic-id packet)]
    (assoc packet :semantic-id semantic-id
           :artifact-id (bootstrap/p15-s23-c11-mir-digest
                         {:kind (:kind packet) :schema-version (:schema-version packet)
                          :semantic-id semantic-id})
           :actual-path-binding-id
           (bootstrap/p15-s23-c13-c14-b1-actual-path-binding-id
            semantic-id (:actual-path-provenance packet)))))

(defn- seal-artifact [artifact]
  (let [semantic-id (bootstrap/p15-s23-b3-llvm-artifact-id artifact)]
    (assoc artifact :semantic-id semantic-id
           :artifact-id (bootstrap/p15-s23-c11-mir-digest
                         {:kind (:kind artifact) :schema-version (:schema-version artifact)
                          :semantic-id semantic-id})
           :actual-path-binding-id
           (bootstrap/p15-s23-b3-llvm-actual-path-binding-id
            semantic-id (:actual-path-provenance artifact)))))

(defn- local-envelope []
  (let [c11 {:artifact-id (bootstrap/p15-s23-c11-mir-digest :fixture-c11)}
        c13 (seal-stage {:artifact :gravity/test-c13 :schema-version 1
                         :input {:c11-artifact-id (:artifact-id c11)}})
        request {:artifact :gravity/c14-bounded-llvm-lowering-request
                 :input {:artifact-id (:artifact-id c13)} :target :llvm-x86_64-linux}
        request (assoc request :request-id
                       (bootstrap/p15-s23-c11-mir-digest
                        {:kind :gravity/c14-bounded-llvm-lowering-request :request request}))
        c14 (seal-stage {:artifact :gravity/test-c14 :schema-version 1 :request request})
        b1 (seal-stage {:artifact :gravity/test-b1 :schema-version 1
                       :backend-manifest {:c14-artifact-id (:artifact-id c14)}})
        packet (seal-packet {:kind :gravity/test-packet :schema-version 1
                             :c11 c11 :c13 c13 :c14 c14 :b1 b1
                             :optimized-mir {:operations []}
                             :actual-path-provenance {:source source-path}})
        snapshot (private-call 'p15-s23-b3-llvm-linux-tool-snapshot tool-snapshot-text)
        toolchain {:status :complete
                   :tool-hashes {:snapshot tool-snapshot-text :hashes snapshot}
                   :tool-paths {:paths (into (sorted-map)
                                            (map (fn [[tool value]] [tool (:path value)]))
                                            snapshot)}
                   :qemu-binfmt {:status :observed :machine "x86_64"
                                 :binfmt-state "unavailable" :qemu-state :unavailable}}]
    (seal-artifact
     {:kind :gravity/test-local-elf-envelope :schema-version 1
      :target :llvm-x86_64-linux
      :target-policy @(ns-resolve 'gravity.bootstrap 'p15-s23-b3-llvm-policy)
      :c13-c14-b1-packet packet :actual-path-provenance {:source source-path}
      :b13-record {:target {:canonical-target :llvm-x86_64-linux
                            :object-format :elf :architecture :x86_64}
                   :artifact-files {:object (file-snapshot :object (header 1))
                                    :executable (file-snapshot :executable (header 2))}}
      :toolchain-evidence toolchain
      :replay-projection
      {:toolchain (private-call 'p15-s23-b3-llvm-stable-toolchain-projection toolchain)}})))

(defn- integrity! [artifact]
  (private-call 'p15-s23-b3-llvm-linux-evidence-integrity! artifact source-path))

(deftest local-integrity-consumer-executes-the-same-adopted-predicate
  (let [artifact (local-envelope)
        calls (atom {})]
    (is (= :passed (trace-adoption calls #(integrity! artifact))))
    (is (= [:object :executable] (mapv second (:predicate @calls))))
    (is (= [[(header 1) :object] [(header 2) :executable]]
           (mapv :arguments (:uniform @calls))))
    (is (= [true true] (mapv :result (:uniform @calls))))
    (doseq [[kind type] [[:object 1] [:executable 2]]
            octets [(assoc (header type) 18 0) (subvec (header type) 0 19)]]
      (reset! calls {})
      (let [invalid (seal-artifact
                     (assoc-in artifact [:b13-record :artifact-files kind]
                               (file-snapshot kind octets)))
            failure (trace-adoption calls #(failure-data (fn [] (integrity! invalid))))]
        (is (= "B3-MANIFEST" (:id failure)))
        (is (= :local-elf-artifact-integrity (:missing-fact failure)))
        (is (= false (:result (last (:uniform @calls)))))
        (is (= [octets kind] (:arguments (last (:uniform @calls)))))))))

(deftest local-integrity-retains-independent-checks
  (let [artifact (local-envelope)]
    (doseq [kind [:object :executable]
            [field value] [[:byte-count 21] [:content-hash "sha256:incorrect"]
                           [:header "Format: elf64-x86-64"] [:format :macho]
                           [:architecture :aarch64]]]
      (let [invalid (seal-artifact
                     (assoc-in artifact [:b13-record :artifact-files kind field] value))
            failure (failure-data #(integrity! invalid))]
        (is (= "B3-MANIFEST" (:id failure)))
        (is (= :local-elf-artifact-integrity (:missing-fact failure)))
        (is (= :rejected (:fallback-status failure)))))
    (doseq [[field value fact]
            [[:semantic-id :changed :local-semantic-id-recompute]
             [:artifact-id :changed :local-artifact-id-recompute]
             [:actual-path-provenance {:source "changed.gravity"}
              :local-actual-path-binding-recompute]]]
      (let [failure (failure-data #(integrity! (assoc artifact field value)))]
        (is (= "B3-MANIFEST" (:id failure)))
        (is (= fact (:missing-fact failure)))))
    (let [packet (-> (:c13-c14-b1-packet artifact)
                     (assoc-in [:c11 :artifact-id] :changed)
                     seal-packet)]
      (doseq [invalid [(assoc artifact :target :llvm-x86_64-darwin)
                       (assoc-in artifact [:target-policy :target-triple] "aarch64-linux-gnu")
                       (assoc artifact :c13-c14-b1-packet packet)]]
        (let [failure (failure-data #(integrity! (seal-artifact invalid)))]
          (is (= "B3-MANIFEST" (:id failure)))
          (is (= :local-c13-c14-b1-target-lineage (:missing-fact failure))))))))
