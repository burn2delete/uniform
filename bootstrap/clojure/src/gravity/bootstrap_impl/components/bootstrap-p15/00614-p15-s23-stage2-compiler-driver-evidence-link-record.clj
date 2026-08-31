

	(defn p15-s23-stage2-compiler-driver-evidence-link-record
	  [front-end-executor-artifact front-end-artifact
	   runtime-kernel-artifact runtime-artifact plan-emitter-artifact
	   nucleus-artifact pipeline-artifact accepted-artifact
	   rejected-artifact]
  (let [links {:stage2-source-front-end
               {:artifact (:kind front-end-artifact)
                :artifact-id (:artifact-id front-end-artifact)
                :present?
                (= :gravity/p15-s23-stage2-source-front-end-artifact
                   (:kind front-end-artifact))}
               :stage2-front-end-executor
               {:artifact (:kind front-end-executor-artifact)
                :artifact-id (:artifact-id front-end-executor-artifact)
                :present?
                (= :gravity/p15-s23-stage2-front-end-executor-artifact
                   (:kind front-end-executor-artifact))}
	               :stage2-runtime-executor
	               {:artifact (:kind runtime-artifact)
                :artifact-id (:artifact-id runtime-artifact)
                :present?
	                (= :gravity/p15-s23-stage2-runtime-executor-artifact
	                   (:kind runtime-artifact))}
	               :stage2-runtime-kernel
	               {:artifact (:kind runtime-kernel-artifact)
	                :artifact-id (:artifact-id runtime-kernel-artifact)
	                :present?
	                (= :gravity/p15-s23-stage2-runtime-kernel-artifact
	                   (:kind runtime-kernel-artifact))}
	               :stage2-plan-emitter
               {:artifact (:kind plan-emitter-artifact)
                :artifact-id (:artifact-id plan-emitter-artifact)
                :present?
                (= :gravity/p15-s23-stage2-plan-emitter-artifact
                   (:kind plan-emitter-artifact))}
               :stage2-compiler-nucleus
               {:artifact (:kind nucleus-artifact)
                :artifact-id (:artifact-id nucleus-artifact)
                :present?
                (= :gravity/p15-s23-stage2-compiler-nucleus-artifact
                   (:kind nucleus-artifact))}
               :compiler-pipeline-manifest
               {:artifact (:kind pipeline-artifact)
                :artifact-id (:artifact-id pipeline-artifact)
                :present?
                (= :gravity/p15-s23-compiler-pipeline-manifest-artifact
                   (:kind pipeline-artifact))}
               :accepted-app-execution-proof
               {:artifact (:kind accepted-artifact)
                :artifact-id (:artifact-id accepted-artifact)
                :present?
                (= :gravity/p15-s23-accepted-app-execution-artifact
                   (:kind accepted-artifact))}
               :rejected-app-diagnostic-proof
               {:artifact (:kind rejected-artifact)
                :artifact-id (:artifact-id rejected-artifact)
                :present?
                (= :gravity/p15-s23-rejected-app-diagnostic-artifact
                   (:kind rejected-artifact))}}
        missing (set (for [[k v] links :when (not (:present? v))] k))]
    {:artifact :gravity/p15-s23-stage2-compiler-driver-evidence-link-record
     :present-links links
     :missing-links (p15-s23-stage2-sort-values missing)
     :all-required-links-present? (empty? missing)
     :status (if (empty? missing) :complete :failed)}))

(defn p15-s23-stage2-compiler-driver-boundary-record
  [driver]
  (let [claims (:self-hosting-claims driver)
        seed-boundary (:seed-boundary driver)]
    {:artifact :gravity/p15-s23-stage2-compiler-driver-boundary-record
     :implemented-by (:implemented-by driver)
     :executed-by (:executed-by driver)
     :compiled-by (get-in driver [:lineage :compiled-by])
     :stage0-compiler-driver-replaced? true
     :stage0-rule-runner-replaced? true
     :stage0-reader-replaced? true
     :stage0-macro-expander-replaced? true
     :stage2-compiler-driver-executed? true
     :stage2-front-end-executor-used? true
     :stage2-front-end-host-replaced?
     (= :replaced-by-stage2-front-end-executor
        (:stage2-front-end-host-boundary seed-boundary))
	     :stage2-source-front-end-used? true
	     :stage2-plan-emitter-used? true
	     :stage2-runtime-kernel-used?
	     (= :p15-s23-stage2-runtime-kernel
	        (:runtime-kernel driver))
	     :stage2-runtime-executor-used? true
	     :stage2-runtime-host-replaced?
	     (= :replaced-by-stage2-runtime-kernel
	        (:stage0-runtime-host-boundary seed-boundary))
	     :stage2-runtime-primitives-replaced?
	     (= :gravity-runtime-primitives
	        (:host-primitive-boundary seed-boundary))
     :clojure-stage0-driver-host?
     (= :clojure-stage0-driver-host (:executed-by driver))
     :clojure-stage0-rule-runner? false
     :clojure-stage0-reader? false
     :clojure-stage0-macro-expander? false
     :clojure-stage2-front-end-host? false
     :clojure-stage0-runtime-host?
     (= :clojure-stage0-runtime-host
        (:stage0-runtime-host-boundary seed-boundary))
	     :clojure-host-primitive-boundary?
	     (= :clojure-runtime-primitives
	        (:host-primitive-boundary seed-boundary))
	     :gravity-runtime-primitives?
	     (= :gravity-runtime-primitives
	        (:host-primitive-boundary seed-boundary))
     :self-hosted-compiler? false
     :full-language-compiler-self-hosted?
     (:full-language-compiler-self-hosted? claims)
     :clojure-seed-retired? (:clojure-seed-retired? claims)
     :seed-boundary seed-boundary
     :next-required-capability (:next-required-capability driver)
     :status :complete}))