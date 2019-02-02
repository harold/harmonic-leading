(ns harmonic-leading.wav
  (:require [clojure.java.io :as io])
  (:import [javax.sound.sampled
            AudioFileFormat$Type
            AudioFormat
            AudioInputStream
            AudioSystem
            Line$Info
            LineEvent$Type
            LineListener
            Clip]))

(defmacro shortify
  "Takes a floating-point number f in the range [-1.0, 1.0] and scales
  it to the range of a signed 16-bit integer. Clamps any overflows."
  [f]
  (let [max-short-as-double (double Short/MAX_VALUE)]
    `(let [clamped# (-> ~f (min 1.0) (max -1.0))]
       (short (* ~max-short-as-double clamped#)))))

(defn- make-stream
  [fa]
  (let [cursor* (atom 0)]
    (proxy [java.io.InputStream] []
      (read ^int
        ([^bytes buf off len]
         (when-not (zero? off) (throw (Exception. "NON-ZERO OFFSET ENCOUNTERED")))
         (when-not (zero? (mod len 2)) (throw (Exception. "ODD LEN ENCOUNTERED")))
         (dotimes [i (quot len 2)]
           (let [s (shortify (aget fa @cursor*))]
             (swap! cursor* inc)
             (aset-byte buf (* 2 i) (unchecked-byte (bit-shift-right s 8)))
             (aset-byte buf (inc (* 2 i)) (unchecked-byte (bit-and s 0xFF)))))
         len)))))

(defn save
  "Expects a file path and an array of floats in the range [-1.0, 1.0]."
  [path fa]
  (let [stream (make-stream fa)
        sample-rate 44100
        bit-depth 16
        channel-count 1
        signed true
        big-endian true
        audio-format (AudioFormat. sample-rate bit-depth channel-count signed big-endian)
        length (count fa) ;; length in samples
        type-wav AudioFileFormat$Type/WAVE]
    (AudioSystem/write (AudioInputStream. stream audio-format length) type-wav (io/file path))))

(defn play
  [path]
  (let [clip (AudioSystem/getLine (Line$Info. Clip))]
    (.addLineListener clip (proxy [LineListener] []
                             (update [line-event]
                               (when (= LineEvent$Type/STOP (.getType line-event))
                                 (.close clip)))))
    (.open clip (AudioSystem/getAudioInputStream (io/file path)))
    (.start clip)
    (.drain clip)))
