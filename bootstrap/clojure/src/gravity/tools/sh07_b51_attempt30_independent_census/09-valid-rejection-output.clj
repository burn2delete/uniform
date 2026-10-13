
(defn- valid-rejection-output? [output]
  (let [evidence (:rejection-evidence output)]
    (and (exact-keys? output rejection-output-keys)
         (= :rejected (:status output))
         (exact-keys? evidence rejection-keys)
         (= :gravity/sh07-b51-census-checker-rejection-evidence-v1
            (:artifact evidence))
         (= [1 :rejected :independent-verifier-census
             :m2-first-independent-exhaustive]
            [(:schema-version evidence) (:status evidence) (:role evidence)
             (:algorithm-role evidence)])
         (keyword? (:reason evidence))
         (= (:rejection-id evidence) (h (dissoc evidence :rejection-id)))
         (not (contains-certificate-id-key? output)))))

(defn valid-output? [envelope output]
  (case (:status output)
    :accepted (boolean (valid-accepted-output? envelope output))
    :rejected (boolean (valid-rejection-output? output))
    false))
