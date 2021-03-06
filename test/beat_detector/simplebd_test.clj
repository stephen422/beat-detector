(ns beat-detector.simplebd-test
  (:use [beat-detector.simplebd])
  (:require [clojure.test :refer :all]
            [beat-detector.packet :as packet]))

(def ^:dynamic *raw* '(([1 2 3 4] [0 0 0 0]) ([5 6 7 8] [0 0 0 0])))

(deftest generate-energy-buffer-test
  (testing "general"
    (is (= (generate-energy-buffer *raw* 2 8)
           [5 25 61 113])))
  (testing "when history is longer than raw"
    (is (= (generate-energy-buffer *raw* 3 9)
           [14 77 113])))
  (testing "when instance is longer than raw"
    (is (= (generate-energy-buffer *raw* 9 9)
           [204]))))

(deftest next-energy-buffer-test
  (testing "general"
    (is (= (next-energy-buffer [0 1 2 3] *raw* 2)
           [1 2 3 5])))
  (testing "when there are insufficient datas (< instance-num) in raw"
    (is (= (next-energy-buffer [0 1 2 3] *raw* 9)
           [1 2 3 204])))
  (testing "when there are no more remaining raw"
    (is (= (next-energy-buffer [0 1 2 3] '() 2)
           [1 2 3 0])))
  (testing "when given buffer is empty (which should not be)"
    (is (= (next-energy-buffer [] *raw* 2)
           nil))))


(deftest initialize-test
  (testing "general"
    (let [packet (packet/pack *raw* 0 2 6 nil)
          {buffer :buffer raw :raw pos :pos} (initialize packet)]
      (is (= buffer [5 25 61]))
      (is (= raw '([[7 8] [0 0]])))
      (is (= pos 3))))
  (testing "when raw is completely consumed"
    (let [packet (packet/pack *raw* 0 2 8 nil)]
      (is (= (take 2 (vals (initialize packet)))
             '([5 25 61 113] ())))))
  (testing "when raw is empty"
    (let [packet (packet/pack [] 0 2 8 nil)]
      (is (nil? (initialize packet))))))

(deftest reload-test
  (testing "general"
      (let [packet (initialize (packet/pack *raw* 0 2 4 nil))
            {buffer :buffer raw :raw pos :pos} (reload packet)]
        (is (= buffer [25 61]))
        (is (= raw '([[7 8] [0 0]])))
        (is (= pos (inc (/ 4 2))))))
  (testing "when raw depletes on update"
      (let [packet (initialize (packet/pack *raw* 0 2 6 nil))
            {buffer :buffer raw :raw pos :pos} (reload packet)]
        (is (= buffer [25 61 113]))
        (is (= raw '()))
        (is (= pos (inc (/ 6 2)))))))

(def ^:dynamic *rawl* '(([1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 11] [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 11]) ([1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 11] [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 11])))
