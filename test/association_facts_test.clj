(ns association-facts-test
  (:require [clojure.java.io :as io] [clojure.java.shell :as shell]
            [clojure.test :refer [deftest is testing]]
            [kotoba.compiler.core :as compiler] [kotoba.compiler.ir :as ir]))
(def source (slurp "src/association_facts.kotoba"))
(defn call [kir function & args] (ir/execute kir function (vec args)))
(defn present [option] (when (second option) (nth option 2)))
(def fields ["id" "title" "association" "isic" "country" "kind" "url" "url-provenance"
             "established-date" "last-revised-date" "retrieved-at"])
(def expected
  [{"id" "nab.political-broadcast-catechism" "title" "Political Broadcast Catechism, 16th Edition"
    "association" "nab" "isic" "6020" "country" "USA" "kind" "best-practices-guide"
    "url" "https://www.nab.org/documents/membership/memberDownloads/Political_Catechism.pdf"
    "url-provenance" "official-association-site" "established-date" nil
    "last-revised-date" "2014" "retrieved-at" "2026-07-15"}
   {"id" "nab.our-mission-centennial" "title" "Our Mission (Celebrating 100 Years, organization profile)"
    "association" "nab" "isic" "6020" "country" "USA" "kind" "governance-program"
    "url" "https://www.nab.org/100/nab/ourMission.asp" "url-provenance" "official-association-site"
    "established-date" "1923" "last-revised-date" nil "retrieved-at" "2026-07-15"}])
(deftest reference-preserves-fields-date-precision-and-topics
  (let [kir (:kir (compiler/compile-source source :js-kotoba-v1))
        observed (mapv (fn [i] (into {} (map (fn [f] [f (present (call kir 'entry-field "nab" i f))]) fields))) [0 1])]
    (is (= expected observed))
    (is (= [[nil "2014"] ["1923" nil]]
           (mapv (fn [i] (mapv #(present (call kir 'entry-field "nab" i %)) ["established-date" "last-revised-date"])) [0 1])))
    (is (= [2 1] (mapv #(call kir 'topic-count "nab" %) [0 1])))
    (is (= ["political-advertising" "disclosure"] (mapv #(present (call kir 'topic "nab" 0 %)) [0 1])))
    (is (= "nab.our-mission-centennial" (present (call kir 'by-topic-id "nab" "governance" 0))))
    (is (= #{} (set (:effects kir))))
    (testing "unknown values and invalid indexes fail closed"
      (is (zero? (call kir 'entry-count "rtdna")))
      (is (nil? (present (call kir 'entry-field "nab" -1 "id"))))
      (is (nil? (present (call kir 'entry-field "nab" 2 "id"))))
      (is (nil? (present (call kir 'entry-field "nab" 0 "established-date"))))
      (is (nil? (present (call kir 'topic "nab" 1 1))))
      (is (zero? (call kir 'by-topic-count "nab" "labor")))
      (is (nil? (present (call kir 'by-topic-id "nab" "governance" 1)))))))
(defn compiler-root []
  (nth (iterate #(.getParent ^java.nio.file.Path %)
                (java.nio.file.Path/of (.toURI (io/resource "kotoba/compiler/core.clj")))) 4))
(defn base64 [value] (.encodeToString (java.util.Base64/getEncoder) value))
(deftest restricted-javascript-and-typed-wasm-conform-semantically
  (let [javascript (compiler/compile-source source :js-kotoba-v1)
        wasm (compiler/compile-source source :wasm32-browser-kotoba-v1)
        js64 (base64 (.getBytes ^String (:source javascript) "UTF-8")) wasm64 (base64 ^bytes (:bytes wasm))
        probe (shell/sh "node" "--input-type=module" "-e"
                (str "import(process.argv[1]).then(async host=>{const j=await import('data:text/javascript;base64," js64 "');"
                     "const w=await host.instantiateKotoba(Buffer.from(process.argv[2],'base64'));const run=x=>{"
                     "if(x['entry-field']('nab',0n,'established-date')[1]!==false||x['entry-field']('nab',0n,'last-revised-date')[2]!=='2014'||x['entry-field']('nab',1n,'established-date')[2]!=='1923')throw Error('dates');"
                     "if(x['topic-count']('nab',0n)!==2n||x['topic']('nab',0n,1n)[2]!=='disclosure')throw Error('topics');"
                     "if(x['by-topic-id']('nab','governance',0n)[2]!=='nab.our-mission-centennial'||x['entry-field']('nab',1n,'last-revised-date')[1]!==false)throw Error('query');};"
                     "run(j.instantiateKotoba({}));run(w.instance.exports);}).catch(e=>{console.error(e);process.exit(99)})")
                (.toString (.toUri (.resolve (compiler-root) "runtime/browser-host.mjs"))) wasm64)]
    (is (zero? (:exit probe)) (str (:out probe) (:err probe)))))
(deftest production-source-authority
  (is (= ["src/association_facts.kotoba"]
         (->> (file-seq (io/file "src")) (filter #(.isFile %)) (map str) sort vec))))
