(ns gravity.build.parent-producer-observation
  "Source-only RV2 parent adapter. Does not launch work or admit a capture."
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [gravity.build.producer-evidence :as evidence]
            [gravity.self-hosting.sh01-parallel-test-runner :as runner])
  (:import [java.io PushbackReader StringReader]
           [java.nio ByteBuffer]
           [java.nio.channels Channels]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.nio.file LinkOption Path SecureDirectoryStream StandardOpenOption]
           [java.nio.file.attribute FileAttribute PosixFilePermissions]))

(defn observe!
  "Consume one newly launched Process through the canonical ownership API.

  The caller owns launch, exact loader/options/output binding and admission.
  process-result waits, captures and finalizes the same raw token, including
  its exceptional path. Never reconstruct ownership from PIDs or finalize a
  second time here. The returned raw token is local state, not EDN evidence.
  Selected-workload descendant reachability remains explicitly unresolved."
  [process options]
  (let [owned (runner/retain-owned-process! process)
        result (runner/process-result process options owned)]
    {:schema :gravity/rv2-parent-process-observation-v1
     :authority :non-authoritative
     :owned-process owned
     :retained-handles (runner/retained-process-handles owned)
     :process-result result
     :root-exited? (:root-exited? result)
     :tracked-owned-processes-terminal? (:tracked-owned-processes-terminal? result)
     :cleanup-complete? (:cleanup-complete? result)
     :descendant-coverage (:descendant-coverage result)
     :selected-workload-reachability :unresolved
     :capture-v1-ready? false
     :cache-admission? false}))

(defn- complete-stream? [result stream]
  (let [field #(get result (keyword (str (name stream) "-" %)))]
    (and (= :complete (field "capture-status"))
         (false? (field "truncated?"))
         (false? (field "capture-forced-close?"))
         (nil? (field "capture-error")))))

(defn serialize-capture
  "Connect retained parent observations to the existing fresh serializer.

  Copies the GLOBAL cleanup field, never tracked-handle completion. Current
  canonical observations leave that field nil and therefore refuse capture v1.
  Supplied/injected observations still confer no execution or review authority.
  Does not launch, wait, finalize again, read a report file or publish output."
  [contract-bytes report observation]
  (when-not (and (= :gravity/rv2-parent-process-observation-v1 (:schema observation))
                 (= :non-authoritative (:authority observation))
                 (map? (:process-result observation)))
    (throw (ex-info "RV2 capture requires a retained parent observation"
                    {:id "RV2-PARENT-OBSERVATION"})))
  (let [result (:process-result observation)
        terminal {:exit-code (:exit-code result)
                  :natural-exit? (and (= :passed (:status result))
                                      (false? (:wait-interrupted? result))
                                      (nil? (:wait-error result))
                                      (true? (:root-exited? result)))
                  :timed-out? (:timeout-containment-unproven? result)
                  :stdout-complete? (complete-stream? result :stdout)
                  :stderr-complete? (complete-stream? result :stderr)
                  :cleanup-complete? (:cleanup-complete? result)
                  :root-dead? (:root-exited? result)}]
    (evidence/serialize-observed-fresh-capture contract-bytes report terminal)))

(defn- io-require! [condition field]
  (when-not condition
    (throw (ex-info "RV2 contained capture IO refused"
                    {:id "RV2-CAPTURE-IO" :field field :cache-admission? false}))))

(defn- capture-name! [name]
  (io-require! (and (string? name) (not (str/blank? name))) :relative-name)
  (let [path (Path/of name (make-array String 0))]
    (io-require! (and (not (.isAbsolute path)) (= 1 (.getNameCount path))
                      (not (#{"." ".."} name)) (= name (str path))) :relative-name)
    path))

(defn- use-capture-channel [channel operation]
  (let [outcome (try {:value (operation channel)} (catch Throwable error {:error error}))
        interrupted? (.isInterrupted (Thread/currentThread))
        close-error (try (.close ^java.nio.channels.Channel channel) nil (catch Throwable error error))]
    (when interrupted? (.interrupt (Thread/currentThread)))
    (if-let [primary (:error outcome)]
      (do (when (and close-error (not (identical? primary close-error))
                     (not-any? #(identical? close-error %) (.getSuppressed ^Throwable primary)))
            (.addSuppressed ^Throwable primary close-error))
          (throw primary))
      (do (when close-error (throw close-error)) (:value outcome)))))

(defn publish-capture!
  "Connect ONLY the existing C2 report to serialize-capture and new output.

  Caller owns authentic private directory handles and leaf names; this function
  never resolves paths from a report, closes caller handles, launches or waits.
  Supplied allowances are existing host capture contracts, not defaults. Read,
  decode, one-frame parse and existing serialization finish before input close;
  every rejection/close failure precedes output admission and CREATE_NEW.

  Output is private, relative and NOFOLLOW. IO failures may leave a partial or
  complete new file, which remains failed evidence: no delete/reuse/retry or
  atomic/durable publication claim. Even a successful write admits no capture
  or cache use. Nil global cleanup is still rejected by the existing serializer."
  [contract-bytes observation input-directory input-name output-directory output-name options]
  (io-require! (and (instance? SecureDirectoryStream input-directory)
                    (instance? SecureDirectoryStream output-directory)) :directory-handles)
  (io-require! (= #{:input-limit-bytes :output-limit-bytes :admit-bytes!} (set (keys options))) :options)
  (let [{:keys [input-limit-bytes output-limit-bytes admit-bytes!]} options
        input-name (capture-name! input-name) output-name (capture-name! output-name)]
    (io-require! (and (integer? input-limit-bytes) (pos? input-limit-bytes)
                      (< input-limit-bytes Integer/MAX_VALUE)
                      (integer? output-limit-bytes) (pos? output-limit-bytes)
                      (<= output-limit-bytes Long/MAX_VALUE) (ifn? admit-bytes!)) :capture-ports)
    (io-require! (not (and (identical? input-directory output-directory) (= input-name output-name))) :distinct-output)
    (let [bytes
          (use-capture-channel
           (.newByteChannel ^SecureDirectoryStream input-directory input-name
                            #{StandardOpenOption/READ LinkOption/NOFOLLOW_LINKS} (make-array FileAttribute 0))
           (fn [channel]
             (let [input (Channels/newInputStream channel)
                   bytes (.readNBytes input (int input-limit-bytes))
                   _ (io-require! (= -1 (.read input)) :input-capture-limit)
                   decoder (doto (.newDecoder StandardCharsets/UTF_8)
                             (.onMalformedInput CodingErrorAction/REPORT)
                             (.onUnmappableCharacter CodingErrorAction/REPORT))
                   text (str (.decode decoder (ByteBuffer/wrap bytes)))
                   reject (fn [& _] (io-require! false :tagged-report))
                   report (with-open [reader (PushbackReader. (StringReader. text))]
                            (let [eof (Object.) opts {:eof eof :readers {'inst reject 'uuid reject} :default reject}
                                  report (edn/read opts reader)]
                              (io-require! (and (not (identical? eof report))
                                                (identical? eof (edn/read opts reader))) :one-report)
                              report))]
               (serialize-capture contract-bytes report observation))))]
      (io-require! (<= (alength ^bytes bytes) output-limit-bytes) :output-capture-limit)
      (admit-bytes! (alength ^bytes bytes))
      (use-capture-channel
       (.newByteChannel ^SecureDirectoryStream output-directory output-name
                        #{StandardOpenOption/CREATE_NEW StandardOpenOption/WRITE LinkOption/NOFOLLOW_LINKS}
                        (into-array FileAttribute [(PosixFilePermissions/asFileAttribute
                                                    (PosixFilePermissions/fromString "rw-------"))]))
       (fn [channel]
         (let [buffer (ByteBuffer/wrap bytes)]
           (while (.hasRemaining buffer) (.write ^java.nio.channels.WritableByteChannel channel buffer)))))
      {:schema :gravity/rv2-contained-capture-publication-v1 :file (str output-name) :bytes (alength ^bytes bytes)
       :real-evidence-accepted? false :cache-admission? false})))
