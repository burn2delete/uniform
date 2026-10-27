(ns ci.darwin-probe
  "Read-only ABI diagnostics; invalid descriptors never authorize publication."
  (:require [gravity.darwin-publication.contract :as contract]
            [gravity.darwin-publication.native-runtime :as native]
            [gravity.darwin-publication.stat :as stat])
  (:import [java.lang.foreign Linker]))

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
        :required-flags contract/relative-unique-stat-flags
        :probes
        (mapv (fn [flags]
                {:fd -1 :leaf "uniform-ci-abi-probe" :flags flags
                 :result (stat/fstatat-result runtime -1 "uniform-ci-abi-probe" flags)})
              [0 0x800 0x2000 0x2800 0xa800])}))
