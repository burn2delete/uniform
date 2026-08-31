

(defn p15-s23-b4-wasm-fail! [id source-path subject facts]
  (p15-s23-b4-wasm-throw-record!
   (p15-s23-b4-wasm-diagnostic-record id source-path subject facts)))

(defn- p15-s23-b4-wasm-sanitized-complete-diagnostic [data]
  (when (and (map? data)
             (contains? p15-s23-b4-wasm-diagnostic-rules (:id data))
             (= (:id data) (:rule data))
             (map? (:primary data)) (map? (:facts data)))
    (let [primary (:primary data)
          source-path (or (get-in primary [:span :source]) "<b4-wasm>")
          subject {:artifact-id (:artifact primary)
                   :syntax-id (:syntax-id primary)
                   :op-id (:mir-operation-id primary)
                   :source-span (:span primary)
                   :source {:origin-id (:origin-id primary)}}
          rebuilt (p15-s23-b4-wasm-diagnostic-record
                   (:id data) source-path subject (:facts data))
          required (set c15-diagnostic-required-fields)]
      (when (and (every? (set (keys data)) required)
                 (= rebuilt (select-keys data (keys rebuilt))))
        rebuilt))))

(defn p15-s23-b4-wasm-contain-exception!
  [source-path boundary exception]
  (let [data
        (p15-s23-backend-trusted-exception-data
         exception 65536 128)
        b4 (p15-s23-b4-wasm-sanitized-complete-diagnostic data)
        bridge (p15-s23-b3-llvm-sanitized-complete-diagnostic data)
        c11 (p15-s23-c11-mir-sanitized-complete-diagnostic data)]
    (cond
      b4 (p15-s23-b4-wasm-throw-record! b4)
      bridge
      (p15-s23-b4-wasm-fail!
       (:rule bridge) source-path
       {:artifact-id (get-in bridge [:primary :artifact])
        :op-id (get-in bridge [:primary :mir-operation-id])}
       (:facts bridge))
      c11 (p15-s23-c11-mir-throw-record! c11)
      :else
      (p15-s23-b4-wasm-fail!
       "B4-MANIFEST" source-path {}
       {:missing-fact boundary
        :content-hash
        (str "sha256:" (sha256-hex (.getName (class exception))))}))))