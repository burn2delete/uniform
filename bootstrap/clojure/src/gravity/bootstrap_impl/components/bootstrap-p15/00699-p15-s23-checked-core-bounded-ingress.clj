

(defn p15-s23-checked-core-bounded-ingress!
  [diagnostic-id definition-name value maximum-nodes maximum-depth]
  (try
    (p15-s23-reference-runtime-bounded-value!
     "checked-core-bounded-ingress" :jvm definition-name value
     maximum-nodes maximum-depth)
    value
    (catch StackOverflowError _
      (p15-s23-closed-core-fail!
       diagnostic-id "<checked-core-ingress>" {}
       {:missing-fact :bounded-checked-core-ingress-host-stack
        :ingress-definition definition-name}))
    (catch Exception exception
      (p15-s23-closed-core-fail!
       diagnostic-id "<checked-core-ingress>" {}
       {:missing-fact :bounded-checked-core-ingress
        :ingress-definition definition-name
        :cause-class-hash
        (str "sha256:"
             (sha256-hex (.getName (class exception))))}))))

(defn p15-s23-checked-core-bounded-context!
  [context]
  (when-not
   (and (map? context)
        (contains?
         p15-s23-reference-runtime-supported-collection-class-names
         (some-> context class .getName))
        (<= (count context) 5))
    (p15-s23-closed-core-fail!
     "C6-CORE-SHAPE" "<checked-core-context>" {}
     {:missing-fact :bounded-supported-checked-core-context}))
  (p15-s23-checked-core-bounded-ingress!
   "C6-CORE-SHAPE" :checked-core-context context
   p15-s23-reference-runtime-max-contract-nodes
   p15-s23-reference-runtime-max-contract-depth))