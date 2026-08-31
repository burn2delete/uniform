(defn-
 semantic-llvm-legacy-toolchain-phase-02!
 [candidate
  source-path
  lowering
  publication-intent?
  workspace
  ir-path
  object-path
  executable-path
  target
  primary-failure
  state]
 (let
  [{:keys [load-step provider-step file-output header-output]}
   state
   load-output
   (get-in load-step [:result :stdout :text])
   header-parts
   (re-matches
    #"(?s)\Aprogram\.o:\n(.*?)\nprogram:\n(.*)\z"
    header-output)
   load-parts
   (re-matches
    #"(?s)\Aprogram\.o:\n(.*?)\nprogram:\n(.*)\z"
    load-output)
   object-header-output
   (nth header-parts 1 nil)
   executable-header-output
   (nth header-parts 2 nil)
   object-load-output
   (nth load-parts 1 nil)
   executable-load-output
   (nth load-parts 2 nil)
   provider-output
   (get-in provider-step [:result :stdout :text])
   object-format-ok?
   (=
    "program.o: Mach-O 64-bit object arm64"
    (first (str/split-lines file-output)))
   executable-format-ok?
   (and
    (= 2 (count (str/split-lines file-output)))
    (boolean
     (re-matches
      #"program:\s+Mach-O 64-bit executable arm64"
      (second (str/split-lines file-output)))))
   object-header-ok?
   (when
    object-header-output
    (re-matches
     #"(?s)\AMach header\n\s*magic\s+cputype\s+cpusubtype\s+caps\s+filetype\s+ncmds\s+sizeofcmds\s+flags\nMH_MAGIC_64\s+ARM64\s+ALL\s+0x00\s+OBJECT\s+([0-9]{1,5})\s+([0-9]{1,7})\s+SUBSECTIONS_VIA_SYMBOLS\s*\z"
     object-header-output))
   executable-header-ok?
   (when
    executable-header-output
    (re-matches
     #"(?s)\AMach header\n\s*magic\s+cputype\s+cpusubtype\s+caps\s+filetype\s+ncmds\s+sizeofcmds\s+flags\nMH_MAGIC_64\s+ARM64\s+ALL\s+0x00\s+EXECUTE\s+([0-9]{1,5})\s+([0-9]{1,7})\s+NOUNDEFS\s+DYLDLINK\s+TWOLEVEL\s+PIE\s*\z"
     executable-header-output))
   object-ncmds
   (when object-header-ok? (parse-long (nth object-header-ok? 1)))
   object-sizeofcmds
   (when object-header-ok? (parse-long (nth object-header-ok? 2)))
   executable-ncmds
   (when
    executable-header-ok?
    (parse-long (nth executable-header-ok? 1)))
   executable-sizeofcmds
   (when
    executable-header-ok?
    (parse-long (nth executable-header-ok? 2)))
   object-load-command-inventory
   (when
    object-load-output
    (mapv
     second
     (re-seq #"(?m)^\s+cmd (LC_[A-Z0-9_]+)$" object-load-output)))
   object-all-command-lines
   (when
    object-load-output
    (mapv second (re-seq #"(?m)^\s+cmd\s+(\S+)$" object-load-output)))
   object-load-labels
   (when
    object-load-output
    (mapv
     (comp parse-long second)
     (re-seq #"(?m)^Load command ([0-9]{1,5})$" object-load-output)))
   executable-load-command-inventory
   (when
    executable-load-output
    (mapv
     second
     (re-seq #"(?m)^\s+cmd (LC_[A-Z0-9_]+)$" executable-load-output)))
   executable-all-command-lines
   (when
    executable-load-output
    (mapv
     second
     (re-seq #"(?m)^\s+cmd\s+(\S+)$" executable-load-output)))
   executable-load-labels
   (when
    executable-load-output
    (mapv
     (comp parse-long second)
     (re-seq
      #"(?m)^Load command ([0-9]{1,5})$"
      executable-load-output)))
   expected-object-load-command-inventory
   ["LC_SEGMENT_64" "LC_BUILD_VERSION" "LC_SYMTAB" "LC_DYSYMTAB"]
   expected-executable-load-command-inventory
   ["LC_SEGMENT_64"
    "LC_SEGMENT_64"
    "LC_SEGMENT_64"
    "LC_DYLD_CHAINED_FIXUPS"
    "LC_DYLD_EXPORTS_TRIE"
    "LC_SYMTAB"
    "LC_DYSYMTAB"
    "LC_LOAD_DYLINKER"
    "LC_UUID"
    "LC_BUILD_VERSION"
    "LC_SOURCE_VERSION"
    "LC_MAIN"
    "LC_LOAD_DYLIB"
    "LC_FUNCTION_STARTS"
    "LC_DATA_IN_CODE"
    "LC_CODE_SIGNATURE"]
   header-ok?
   (boolean
    (and
     object-ncmds
     object-sizeofcmds
     executable-ncmds
     executable-sizeofcmds
     (= object-ncmds (count object-load-command-inventory))
     (= executable-ncmds (count executable-load-command-inventory))
     (=
      expected-object-load-command-inventory
      object-load-command-inventory)
     (= object-all-command-lines object-load-command-inventory)
     (= (vec (range object-ncmds)) object-load-labels)
     (=
      expected-executable-load-command-inventory
      executable-load-command-inventory)
     (= executable-all-command-lines executable-load-command-inventory)
     (= (vec (range executable-ncmds)) executable-load-labels)
     (<= 1 object-sizeofcmds 65536)
     (<= 1 executable-sizeofcmds 65536)))]
  (assoc
   state
   :load-output
   load-output
   :header-parts
   header-parts
   :load-parts
   load-parts
   :object-header-output
   object-header-output
   :executable-header-output
   executable-header-output
   :object-load-output
   object-load-output
   :executable-load-output
   executable-load-output
   :provider-output
   provider-output
   :object-format-ok?
   object-format-ok?
   :executable-format-ok?
   executable-format-ok?
   :object-header-ok?
   object-header-ok?
   :executable-header-ok?
   executable-header-ok?
   :object-ncmds
   object-ncmds
   :object-sizeofcmds
   object-sizeofcmds
   :executable-ncmds
   executable-ncmds
   :executable-sizeofcmds
   executable-sizeofcmds
   :object-load-command-inventory
   object-load-command-inventory
   :object-all-command-lines
   object-all-command-lines
   :object-load-labels
   object-load-labels
   :executable-load-command-inventory
   executable-load-command-inventory
   :executable-all-command-lines
   executable-all-command-lines
   :executable-load-labels
   executable-load-labels
   :expected-object-load-command-inventory
   expected-object-load-command-inventory
   :expected-executable-load-command-inventory
   expected-executable-load-command-inventory
   :header-ok?
   header-ok?)))
