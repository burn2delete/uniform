(ns gravity.self-hosting.mir-source-snapshot-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh07-c11-mir-source-preflight-test])
  (:import [java.nio ByteBuffer]
           [java.nio.channels ReadableByteChannel]
           [java.nio.file Files OpenOption StandardOpenOption FileVisitOption]
           [java.nio.file.attribute FileAttribute FileTime]))

(defn- existing-var [name]
  (ns-resolve 'gravity.self-hosting.sh07-c11-mir-source-preflight-test name))

(defn- invoke [name & arguments]
  (apply (var-get (existing-var name)) arguments))

(defn- failure-id [action]
  (try (action) nil
       (catch clojure.lang.ExceptionInfo error (:id (ex-data error)))))

(defn- with-hook [name hook action]
  (with-bindings {(existing-var name) hook} (action)))

(defn- with-directory [action]
  (let [directory (Files/createTempDirectory "mir-snapshot-" (make-array FileAttribute 0))]
    (try (action directory)
         (finally
           ;; Files.walk does not follow symbolic links by default.
           (with-open [paths (Files/walk directory (make-array FileVisitOption 0))]
             (doseq [path (reverse (sort-by #(.getNameCount %) (.toArray paths)))]
               (Files/deleteIfExists path)))))))

(defn- write-bytes! [path bytes]
  (Files/write path bytes (make-array OpenOption 0)))

(deftest current-source-intake-remains-separate-from-historical-authentication
  (let [root @(var-get (existing-var 'root))
        relative (var-get (existing-var 'source-relative-path))
        location (invoke 'safe-contained-source-path root relative)
        snapshot (invoke 'read-source-snapshot location)
        bytes (:bytes snapshot)
        loaded (with-hook '*source-snapshot-loader*
                 #(assoc snapshot :source-text "forged" :source-sha256 "forged"
                                  :size -1 :authenticated? true)
                 #(invoke 'load-source-snapshot))]
    (is (> (:size snapshot) (* 512 1024)))
    (is (= (:size snapshot) (alength ^bytes bytes)))
    (is (= (seq bytes) (seq (Files/readAllBytes (:path location)))))
    (is (= (invoke 'sha256-id bytes) (:source-sha256 snapshot) (:source-sha256 loaded)))
    (is (= (invoke 'strict-utf8 bytes "expected") (:source-text loaded)))
    (is (= (:size snapshot) (:size loaded)))
    (is (false? (:authenticated? loaded)))
    (is (= "SH07-C11-PREFLIGHT-SOURCE-BINDING"
           (failure-id #(invoke 'validate-source-binding! loaded))))
    (is (= 330835 (var-get (existing-var 'expected-source-byte-count))))
    (is (= "sha256:cf7af2e3a7709bcc3ce5056cf75bab1bb0b4ac01c6627fe3d1f3d90d5c83c0aa"
           (var-get (existing-var 'expected-source-revision-id))))
    (is (map? (invoke 'proof-contract)))))

(deftest snapshot-size-representation-is-not-an-allocation-guarantee
  ;; Only pure capacity arithmetic at the large boundary: no large allocation.
  (doseq [size [0 1 (dec Integer/MAX_VALUE)]]
    (is (= (inc size) (invoke 'snapshot-buffer-capacity size))))
  (doseq [size [nil -1 1.5 Integer/MAX_VALUE Long/MAX_VALUE (bigint "999999999999999999999")]]
    (is (= "SH07-C11-PREFLIGHT-SOURCE-BYTES"
           (failure-id #(invoke 'snapshot-buffer-capacity size))))))

(deftest finite-snapshot-eof-and-strict-decoding
  (with-directory
    (fn [directory]
      (let [path (.resolve directory "source.gravity")]
        (doseq [size [0 1 (* 512 1024) (inc (* 512 1024))]]
          (write-bytes! path (byte-array size))
          (let [snapshot (invoke 'read-source-snapshot path)]
            (is (= size (:size snapshot)))
            (is (= size (alength ^bytes (:bytes snapshot))))))
        (write-bytes! path (.getBytes "λ🙂" "UTF-8"))
        (let [snapshot (invoke 'read-source-snapshot path)]
          (is (= 6 (:size snapshot)))
          (is (= "λ🙂" (:source-text snapshot))))
        (write-bytes! path (byte-array [(unchecked-byte 0xc3) (byte 0x28)]))
        (is (= "SH07-C11-PREFLIGHT-SOURCE-UTF8"
               (failure-id #(invoke 'read-source-snapshot path)))))))
  (is (= "SH07-C11-PREFLIGHT-SOURCE-BYTES"
         (failure-id #(with-hook '*source-snapshot-loader* (constantly {:bytes "not bytes"})
                        (fn [] (invoke 'load-source-snapshot)))))))

(deftest bounded-read-loop-retains-progress-and-probe-controls
  (let [calls (atom 0)
        stalled (reify ReadableByteChannel
                  (isOpen [_] true) (close [_])
                  (read [_ _] (swap! calls inc) 0))]
    (is (= "SH07-C11-PREFLIGHT-SOURCE-READ"
           (failure-id #(invoke 'read-snapshot-buffer! stalled (ByteBuffer/allocate 2)))))
    (is (= 3 @calls)))
  (let [calls (atom 0)
        buffer (ByteBuffer/allocate 4)
        growing (reify ReadableByteChannel
                  (isOpen [_] true) (close [_])
                  (read [_ target]
                    (swap! calls inc)
                    (let [remaining (.remaining target)]
                      (dotimes [_ remaining] (.put target (byte 65)))
                      remaining)))]
    (invoke 'read-snapshot-buffer! growing buffer)
    (is (= 4 (.position buffer)))
    (is (= 1 @calls)))
  (let [steps (atom [0 0 1 0 0 -1])
        buffer (ByteBuffer/allocate 2)
        progressing (reify ReadableByteChannel
                      (isOpen [_] true) (close [_])
                      (read [_ target]
                        (let [step (first @steps)]
                          (swap! steps rest)
                          (when (= 1 step) (.put target (byte 65)))
                          step)))]
    (invoke 'read-snapshot-buffer! progressing buffer)
    (is (= 1 (.position buffer)))
    (is (empty? @steps))))

(deftest source-path-and-observed-identity-fail-closed
  (with-directory
    (fn [directory]
      (let [path (.resolve directory "source.gravity")
            link (.resolve directory "link.gravity")
            nested (.resolve directory "nested")
            intermediate (.resolve directory "intermediate")]
        (write-bytes! path (.getBytes "(ns fixture)" "UTF-8"))
        (is (= "SH07-C11-PREFLIGHT-PATH-CONTAINMENT"
               (failure-id #(invoke 'safe-contained-source-path directory "../escape"))))
        (is (= "SH07-C11-PREFLIGHT-PATH-CONTAINMENT"
               (failure-id #(invoke 'safe-contained-source-path directory (str path)))))
        (is (= "SH07-C11-PREFLIGHT-SOURCE-FILE"
               (failure-id #(invoke 'read-source-snapshot directory))))
        (Files/createSymbolicLink link path (make-array FileAttribute 0))
        (is (= "SH07-C11-PREFLIGHT-SOURCE-FILE"
               (failure-id #(invoke 'read-source-snapshot link))))
        (Files/createDirectory nested (make-array FileAttribute 0))
        (write-bytes! (.resolve nested "source.gravity") (.getBytes "(ns nested)" "UTF-8"))
        (Files/createSymbolicLink intermediate nested (make-array FileAttribute 0))
        (is (= "SH07-C11-PREFLIGHT-PATH-CONTAINMENT"
               (failure-id #(invoke 'safe-contained-source-path directory "intermediate/source.gravity"))))
        (let [state-var (existing-var 'basic-file-state)
              original (var-get state-var)]
          (with-redefs-fn {state-var #(assoc (original %) :file-key nil)}
            #(is (= "SH07-C11-PREFLIGHT-SOURCE-IDENTITY"
                    (failure-id (fn [] (invoke 'read-source-snapshot path))))))
          ;; Model an observed ownership change; no chown permission required.
          (let [calls (atom 0)]
            (with-redefs-fn {state-var #(assoc (original %) :owner (swap! calls inc))}
              #(is (= "SH07-C11-PREFLIGHT-SOURCE-MUTATED"
                      (failure-id (fn [] (invoke 'read-source-snapshot path))))))))))))

(deftest descriptor-read-rejects-observed-growth-truncation-and-replacement
  (with-directory
    (fn [directory]
      (let [path (.resolve directory "source.gravity")
            replacement (.resolve directory "replacement.gravity")
            moved (.resolve directory "moved.gravity")
            initial (.getBytes "(ns fixture)" "UTF-8")]
        (doseq [mutate [(fn [] (Files/write path (byte-array [65])
                                           (into-array OpenOption [StandardOpenOption/APPEND])))
                       (fn [] (write-bytes! path (byte-array 0)))
                       (fn [] (Files/setLastModifiedTime path (FileTime/fromMillis 1000)))]]
          (write-bytes! path initial)
          (is (= "SH07-C11-PREFLIGHT-SOURCE-MUTATED"
                 (failure-id #(with-hook '*snapshot-after-open-hook* mutate
                                (fn [] (invoke 'read-source-snapshot path)))))))
        (write-bytes! path initial)
        (write-bytes! replacement initial)
        (is (= "SH07-C11-PREFLIGHT-SOURCE-MUTATED"
               (failure-id #(with-hook '*snapshot-before-open-hook*
                              (fn []
                                (Files/move path moved (make-array java.nio.file.CopyOption 0))
                                (Files/move replacement path (make-array java.nio.file.CopyOption 0)))
                              (fn [] (invoke 'read-source-snapshot path))))))))))

(deftest source-component-replacement-is-not-hidden-by-a-stable-leaf
  (with-directory
    (fn [directory]
      (let [live (.resolve directory "live")
            spare (.resolve directory "spare")
            moved (.resolve directory "moved")]
        (Files/createDirectory live (make-array FileAttribute 0))
        (Files/createDirectory spare (make-array FileAttribute 0))
        (write-bytes! (.resolve live "source.gravity") (.getBytes "(ns fixture)" "UTF-8"))
        ;; Same leaf inode in both directories; path-component observations
        ;; must still reject replacement. No secure-dirfd claim follows.
        (Files/createLink (.resolve spare "source.gravity") (.resolve live "source.gravity"))
        (let [location (invoke 'safe-contained-source-path directory "live/source.gravity")]
          (is (= "SH07-C11-PREFLIGHT-SOURCE-MUTATED"
                 (failure-id #(with-hook '*snapshot-before-open-hook*
                                (fn []
                                  (Files/move live moved (make-array java.nio.file.CopyOption 0))
                                  (Files/move spare live (make-array java.nio.file.CopyOption 0)))
                                (fn [] (invoke 'read-source-snapshot location)))))))))))
