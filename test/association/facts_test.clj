(ns association.facts-test
  (:require [clojure.test :refer [deftest is]]
            [association.facts :as facts]))

(deftest nab-has-spec-basis
  (let [sb (facts/spec-basis "nab")]
    (is (= 2 (count sb)))
    (is (every? #(= "6020" (:association-rule/isic %)) sb))
    (is (every? #(= "USA" (:association-rule/country %)) sb))))

(deftest unknown-association-has-no-spec-basis
  (is (nil? (facts/spec-basis "rtdna")))
  (is (nil? (facts/spec-basis "zzz"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["nab" "rtdna"])]
    (is (= 2 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["rtdna"] (:missing-associations c)))))

(deftest by-topic-filters
  (is (= ["nab.political-broadcast-catechism"]
         (mapv :association-rule/id (facts/by-topic "nab" :political-advertising))))
  (is (empty? (facts/by-topic "nab" :labor)))
  (is (empty? (facts/by-topic "rtdna" :governance))))
