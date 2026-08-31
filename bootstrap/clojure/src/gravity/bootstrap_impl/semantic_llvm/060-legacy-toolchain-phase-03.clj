(defn-
 semantic-llvm-legacy-toolchain-phase-03!
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
  [{:keys
    [compile-step
     link-step
     load-output
     object-load-output
     executable-load-output
     provider-output]}
   state
   dyld-ok?
   (and
    (string? executable-load-output)
    (not (str/includes? object-load-output "LC_LOAD_DYLINKER"))
    (str/includes? executable-load-output "LC_LOAD_DYLINKER")
    (=
     1
     (count
      (re-seq
       #"(?m)^\s+name /usr/lib/dyld \(offset [0-9]+\)$"
       executable-load-output))))
   provider-output-ok?
   (boolean
    (re-matches
     #"\Aprogram:\n\s+/usr/lib/libSystem\.B\.dylib \(compatibility version 1\.0\.0, current version 1356\.0\.0\)\n?\z"
     provider-output))
   provider-paths
   (if provider-output-ok? ["/usr/lib/libSystem.B.dylib"] [])
   forbidden-load-command?
   (boolean
    (re-find
     #"(?m)^\s+cmd LC_(RPATH|LOAD_WEAK_DYLIB|REEXPORT_DYLIB|LOAD_UPWARD_DYLIB)$"
     load-output))
   libsystem-ok?
   (and
    provider-output-ok?
    (= ["/usr/lib/libSystem.B.dylib"] provider-paths)
    (boolean
     (and
      executable-load-output
      (re-find
       #"(?s)cmd LC_LOAD_DYLIB\s+cmdsize 56\s+name /usr/lib/libSystem\.B\.dylib \(offset 24\)\s+time stamp 2 .+?\s+current version 1356\.0\.0\s+compatibility version 1\.0\.0"
       executable-load-output)))
    (not forbidden-load-command?))
   build-version-ok?
   (boolean
    (and
     (string? executable-load-output)
     (re-find
      #"(?s)cmd LC_BUILD_VERSION\s+cmdsize 24\s+platform 1\s+minos 14\.0\s+sdk n/a\s+ntools 0"
      object-load-output)
     (re-find
      #"(?s)cmd LC_BUILD_VERSION\s+cmdsize 32\s+platform 1\s+minos 14\.0\s+sdk 26\.5\s+ntools 1\s+tool 3\s+version 1267\.0"
      executable-load-output)))
   unwind-metadata-ok?
   (boolean
    (and
     (string? executable-load-output)
     (re-find
      #"(?s)sectname __compact_unwind\s+segname __LD"
      object-load-output)
     (re-find
      #"(?s)sectname __unwind_info\s+segname __TEXT"
      executable-load-output)))
   uuid-ok?
   (and
    (string? executable-load-output)
    (=
     1
     (count (re-seq #"(?m)^\s+cmd LC_UUID$" executable-load-output))))
   code-signature-ok?
   (and
    (string? executable-load-output)
    (=
     1
     (count
      (re-seq
       #"(?m)^\s+cmd LC_CODE_SIGNATURE$"
       executable-load-output))))
   entrypoint-ok?
   (and
    (string? executable-load-output)
    (=
     1
     (count (re-seq #"(?m)^\s+cmd LC_MAIN$" executable-load-output))))
   compile-and-link-silent?
   (every?
    (fn
     [step]
     (and
      (zero? (get-in step [:result :stdout :total-byte-count]))
      (zero? (get-in step [:result :stderr :total-byte-count]))))
    [compile-step link-step])]
  (assoc
   state
   :dyld-ok?
   dyld-ok?
   :provider-output-ok?
   provider-output-ok?
   :provider-paths
   provider-paths
   :forbidden-load-command?
   forbidden-load-command?
   :libsystem-ok?
   libsystem-ok?
   :build-version-ok?
   build-version-ok?
   :unwind-metadata-ok?
   unwind-metadata-ok?
   :uuid-ok?
   uuid-ok?
   :code-signature-ok?
   code-signature-ok?
   :entrypoint-ok?
   entrypoint-ok?
   :compile-and-link-silent?
   compile-and-link-silent?)))
