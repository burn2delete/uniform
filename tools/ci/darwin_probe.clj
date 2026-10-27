(ns ci.darwin-probe
  "Read-only ABI diagnostics; invalid descriptors never authorize publication."
  (:require [gravity.darwin-publication.contract :as contract]
            [gravity.darwin-publication.native-call :as call]
            [gravity.darwin-publication.native-runtime :as native]
            [gravity.darwin-publication.stat :as stat])
  (:import [java.lang.foreign Arena Linker]))

(let [runtime (native/native-runtime!)
      lookup (.defaultLookup (Linker/nativeLinker))
      symbols (into {}
                    (for [name ["fstatat" "fstatat64" "fstatat$INODE64"]]
                      (let [symbol (.find lookup name)]
                        [name (when (.isPresent symbol) (.address (.get symbol)))])))]
  (prn {:diagnostic-only true
        :publication-attempted false
        :java-version (System/getProperty "java.version")
        :os-version (System/getProperty "os.version")
        :os-arch (System/getProperty "os.arch")
        :symbol-addresses symbols
        :bound-symbol "fstatat"
        :method-type (str (.type ^java.lang.invoke.MethodHandle (:fstatat runtime)))
        :capture-layout (str (:state-layout runtime))
        :required-flags contract/relative-unique-stat-flags})
  (flush)
  (doseq [flags [0 0x800 0x2000 0x2800 0xa800]]
    (prn {:fd (int -1) :leaf "uniform-ci-abi-probe" :flags (int flags)
          :result (stat/fstatat-result runtime (int -1) "uniform-ci-abi-probe" flags)})
    (flush))
  ;; O_RDONLY (zero) without O_CREAT/O_TRUNC: inspect the existing directory.
  ;; A valid fd is necessary because EBADF can precede flag validation.
  (with-open [arena (Arena/ofConfined)]
    (let [name (.allocateFrom arena ".")
          opened (call/int-call-result runtime arena :open [name (int 0) (int 0)])]
      (when (neg? (:value opened))
        (prn {:read-only-directory "." :open-flags 0 :open-result opened})
        (flush)
        (throw (ex-info "Read-only diagnostic directory open failed" opened)))
      (let [fd (:value opened)]
        (try
          (prn {:read-only-directory "." :open-flags 0 :open-result opened})
          (flush)
          (doseq [flags [0 0x800 0x2000 0x2800 0xa800]]
            (prn {:fd fd :leaf "uniform-ci-abi-probe" :flags (int flags)
                  :result (stat/fstatat-result runtime fd "uniform-ci-abi-probe" flags)})
            (flush))
          (finally (call/close-fd! runtime fd :diagnostic-directory-close)))))))
