(ns harmonic-leading.core
  (:require [harmonic-leading.wav :as wav])
  (:gen-class))

(defn- normalize
  [fa]
  (let [m (apply max fa)]
    (dotimes [i (count fa)]
      (aset-float fa i (/ (aget fa i) m)))
    fa))

(defn -main
  [& args]
  (let [hz 440.0
        sample-rate 44100.0
        length 1.0
        sample-count (long (* sample-rate length))
        fa (float-array sample-count)]
    (dotimes [i sample-count]
      (aset-float fa i
                  (* (apply + (for [harm (range 8)]
                                (/ (Math/sin (* i 2 Math/PI (/ (* (inc harm) hz) sample-rate))) (inc harm))))
                     (- 1 (/ i sample-count)))))
    (wav/save "test.wav" (normalize fa))
    (wav/play "test.wav")))
