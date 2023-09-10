(ns birdsong-as-code.song
  (:require [overtone.live :refer :all :exclude [stop]]
            [leipzig.melody :refer :all]
            [leipzig.scale :as scale]
            [leipzig.live :as live]
            [leipzig.live :refer [stop]]
            [leipzig.chord :as chord]
            [leipzig.temperament :as temperament]))

; Generic machinery
(defsynth walker [out-bus 0 freq 0.5]
  (out:kr out-bus (lf-noise1:kr freq)))
(defonce random-walk (audio-bus))
(defonce walk (walker random-walk))
(def resonance (mul-add (in:kr random-walk) 1000 5000))

(defcgen cut-out [input {:default :none}]
  (:ar (let [_ (detect-silence input :action FREE)]
         input))
  (:default :ar))

(defcgen effects [input  {:default :none}
                  pan    {:default 0}
                  wet    {:default 0.33}
                  room   {:default 0.5}
                  volume {:default 1.0}
                  early  {:default 0.1}
                  high   {:default 20000}
                  low    {:default 0}]
  (:ar (-> input
           (* volume)
           (pan2 pan)
           (free-verb :mix wet :room room)
           (lpf high)
           cut-out))
  (:default :ar))


(definst corgan [freq 440 dur 10.0 depth 1 walk 1 attack 0.01 under-attack 0.3 vol 1.0 pan 0.0 wet 0.5 room 0.5 vibrato 3 limit 99999 gate 1.0]
  (->
    (saw freq)
    (* (env-gen (perc 0.01 dur)))
    (rlpf (mul-add (sin-osc vibrato) (line:kr 0 200 dur) (* freq 4)) 0.3)
    (* vol 4)
    (clip2 0.2)
    (+ (* 1/3 (sin-osc freq) (env-gen (perc under-attack dur))))
    (+ (* (lpf (* 1 (brown-noise)) 500) (env-gen (perc 0.01 0.2))))
    (* (env-gen (adsr attack 0.05 1) (* gate (line:kr 1.0 0.0 dur))))
    (rlpf (* walk resonance) 0.1)
    (* vol 3)
    (effects :pan pan :wet wet :room room :volume vol :high limit)))

(definst blorp [freq 440 dur 1.0 depth 1 walk 1 attack 0.01 under-attack 0.3 vol 1.0 pan 0.0 wet 0.5 room 0.5 vibrato 3 limit 99999 gate 1.0]
  (->
    (square freq)
    (+ (* 3 (sin-osc freq) (env-gen (perc under-attack dur))))
    (* (env-gen (perc 0.001 0.4)))
    (lpf (line:kr (* freq 9) (* freq 1) dur))
    (* vol 9)
    (clip2 0.4)
    (* 3)
    (rlpf (* walk resonance) 0.1)
    (* vol 3/2)
    (effects :pan pan :wet wet :room room :volume vol :high limit)))

(definst butcherbird-19 []
  (let [buffer (load-sample "recordings/AUDIO 19.wav")]
    (play-buf 1 buffer :action FREE :rate 1.0)))

(definst butcherbird-23 []
  (let [buffer (load-sample "recordings/AUDIO 23.wav")]
    (play-buf 1 buffer :action FREE :rate 1.0)))

(definst butcherbird-24 []
  (let [buffer (load-sample "recordings/AUDIO 24.wav")]
    (play-buf 1 buffer :action FREE :rate 1.0)))

(def butcherbirds
  {19 butcherbird-19
   23 butcherbird-23
   24 butcherbird-24})

(definst hermit-thrush-02 []
  (let [buffer (load-sample "recordings/pnas.1406023111.sa02.wav")]
    (play-buf 1 buffer :action FREE :rate 1.0)))

(definst hermit-thrush-04 []
  (let [buffer (load-sample "recordings/pnas.1406023111.sa04.wav")]
    (play-buf 1 buffer :action FREE :rate 1.0)))

(def hermit-thrushes
  {02 hermit-thrush-02
   04 hermit-thrush-04})

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Zoömusicology      ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  "The study of music in animal culture."
)

(comment
  "Is Birdsong Music?"
  "Hollis Taylor"

  "Pied Butcherbird, cracticus nigrogularis"
)

(comment
  "Overtone-based pitch selection in hermit thrush song"
  "Doolittle, Gingras, Endres and Fitch"

  "Hermit Thrush, catharus guttatus"
)

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Birdsong is music  ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (butcherbird-19)
  (butcherbird-23)
  (butcherbird-24)
  (hermit-thrush-02)
  (hermit-thrush-04)
)











;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Frequency is pitch ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(definst tone [frequency 440]
  (sin-osc frequency))

(comment
  (tone 300)
)


(definst beep [frequency 440 volume 1.0]
  (let [envelope (env-gen (perc 0.01 0.9) :action FREE)]
    (* envelope volume (sin-osc frequency))))

(comment
  (beep 300)
)



;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Harmonics          ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (do
    (beep 300)
    (beep 500))
)

(definst whistle [freq 1320 dur 1.0 volume 1.0 pan 0.0 wet 0.5 room 0.5 limit 20000]
  (let [freq (* freq (+ 1 (* -0.02 (env-gen (perc 0.15 0.15)))))]
    (-> (sin-osc freq)
        (+ (* 1/5 (sin-osc (* 2 freq))))
        (+ (* 1/8 (sin-osc (* 3 freq))))
        (+ (* 1/14 (sin-osc (* 4 freq))))
        (+ (* 1/25 (sin-osc (* 5 freq))))
        (+ (* 1/35 (sin-osc (* 6 freq))))
        (lpf (+ freq (* freq 6 (env-gen (perc 0.1 (- dur 0.1))))))
        (* (env-gen (perc (min 0.3 dur) (- dur 0.3))))
        (* 1/6 2 volume)
        (effects :pan pan :wet wet :room room :volume volume :high limit))))

(comment
  (do
    (whistle 300)
    (whistle 500)
    (whistle 1500)
))

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Scales             ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(def just-ratios
  [1/1 9/8 5/4 4/3 3/2 5/3 15/8 2/1])

(defn relative-to [root ratios]
  (map (partial * root) ratios))


(comment
  (->> just-ratios (relative-to 100))

  (->>
    (range 0 8)           ; start with ranks of a scale
    (map just-ratios)     ; translate into ratios
    (relative-to 300)     ; make 300 our "zero"
    (phrase (repeat 1/2)) ; add durations
    live/play)
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Octave equivalence ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn octave-normalise [n ratios]
  (cond
    (neg? n) (-> n (+ 7) (octave-normalise ratios) (/ 2))
    (< 7 n)  (-> n (- 7) (octave-normalise ratios) (* 2))
    :else    (nth ratios n)))

(comment
  (octave-normalise 0 just-ratios)
  (octave-normalise 7 just-ratios)
  (octave-normalise 4 just-ratios)
  (octave-normalise -3 just-ratios)
)

(def concert-A 440)
(defn A-major [n]
  (->> just-ratios
       (relative-to concert-A)
       (octave-normalise n)))

(comment
  (->> (phrase
         (repeat 1/2)  ; duration
         (range -7 8)) ; pitch
       (where :pitch A-major)
       live/play)

  (->> (phrase
         [1 1 2]                    ; duration
         [[1 4 6] [3 5 7] [2 4 7]]) ; pitch
       (where :pitch A-major)
       live/play)
)

(def row-row
  (->> (phrase [3/6 3/6 2/6 1/6 3/6]
               [0 0 0 1 2])
       (where :pitch A-major)))

(def high-row-row
  (->> row-row
       (where :pitch (partial * 2))))

(def low-row-row
  (->> row-row
       (where :pitch (partial * 1/2))))

(comment
  (live/play row-row)
  (live/play high-row-row)
  (live/play low-row-row)
  (live/play (->> row-row (with high-row-row) (with low-row-row)))
)

(def C5 523.25)
(defn C-major [n]
  (->> just-ratios
       (relative-to C5)
       (octave-normalise n)))

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Absolute scale     ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn harmonic [root]
  (fn [n] (-> n (* root))))

(def A-harmonic (harmonic concert-A))

(comment
  (A-harmonic 1)
  (A-harmonic 8)
  (map A-harmonic (range 8 17))
)

(def middle-C 261.63)
(def low-C 65.406)
(def C-harmonic (harmonic low-C))

(comment
  (->> (phrase
         (repeat 1/2)
         (range 8 17))
       (where :pitch C-harmonic)
       live/play)
)

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Species motif      ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(def species-motif
  (phrase [1/2 1/2 1] [15 17 16]))

(comment
  (->> species-motif
       (where :pitch C-harmonic)
       live/play)
)










;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Audio 24           ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

; TODO accidentals
(def hollis-transcription
  (let [a (phrase
            [1/4 1/4 1/4 1/4 1/4 1]
            [3 3.5 4 5 nil 6.5])
        b (phrase
            [1/4 1/2 1/2 1/2 1/2]
            [3 3 3 2 2])
        a' (phrase
             [1/2 1/2 1]
             [10 10 9])
        b' (phrase
             [3/16 3/16 3/16 3/8 3/16 3/16 3/16]
             [3 5 5 5 4 4 6.5])]
    (->> a (then b) (then a') (then b'))))

(comment
  (->> hollis-transcription
       (where :pitch A-major)
       ;(where :pitch (comp temperament/equal scale/A scale/major))
       live/play)
)

(def my-transcription
  (let [a (->> (phrase
                 [1 1/2 1/2 3/2 1]
                 [4 4 4 6 6]))
        b (->> (phrase
                 [1/2 1/2 1]
                 [2 2 1]
                 ))
        a' (->> (phrase
                  [1/2 1/2 1]
                  [10 10 9]))
        b' (->> (phrase
                 [1 1/2 1/2 3/2]
                 [4 4 4 6])) ]
    (->> a (then b) (then a') (then b'))))

(comment
  (->> my-transcription
       (where :pitch A-harmonic)
       ;(map :pitch)
       live/play)
)


(def my-other-transcription
  (let [a (->> (phrase
                 [1 1/2 1/2 3/2 1]
                 [2 2 2 4 4]))
        b (->> (phrase
                 [1/2 1/2 1]
                 [0.5 0.5 0]))
        a' (->> (phrase
                  [1/2 1/2 1]
                  [9.5 9.5 8]))
        b' (->> (phrase
                 [1 1/2 1/2 3/2]
                 [2 2 2 4])) ]
    (->> a (then b) (then a') (then b'))))

(comment
  (->> my-other-transcription
       (where :pitch (comp (partial * 2) C-harmonic))
       ;(map :pitch)
       live/play)
)

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Using it           ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(def melody
  (->>
    [8 9 11 16 13 14 12 16 12 11 17 15 14 16]
    (phrase [1/4 1/4 1/7 1/5 1/4 1/2 2/1 1/4 1/4 1/16 1/4 1/6 1/2 2/1])))

(defn join-up [[prev curr & notes]]
  (when curr
    (let [curr' (assoc curr :previous (:pitch prev))]
      (cons prev (join-up (cons curr' notes))))))

(defn absolute-harmonic-scale [root]
  (fn [pitch] (* root pitch)))

(def harmonic
  (let [root 110]
    (->>
      melody
      (where :pitch (absolute-harmonic-scale root))
      (all :previous (* 16 root))
      join-up
      (tempo (bpm 130)))))

(def diatonic
  (let [root 110]
    (->>
      melody
      (where :pitch A-major)
      (all :previous (* 16 root))
      join-up
      (tempo (bpm 130)))))

(defmethod live/play-note :default [{hertz :pitch seconds :duration}]
  (when hertz (whistle hertz seconds)))

(comment
  ; Loop the track, allowing live editing.
  (live/jam (var harmonic))
  (live/play harmonic)
  (live/jam (var diatonic))
  (live/play diatonic)
  (live/stop)
)

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Not in tune        ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn rand-pitch [n]
  (cons n (lazy-seq (rand-pitch (+ n (rand-int 3) -1)))))

(defn rand-duration []
  (cons (rand-nth [1/8 1/4 1/2 1 2 4]) (lazy-seq (rand-duration))))

(defn rand-phrase []
  (let [start (rand-nth (range 8 16))]
    (->> (phrase (rand-duration) (rand-pitch start))
       (where :pitch C-harmonic)
       (tempo (bpm 130))
       (take 12))))

(comment
  (->> (rand-phrase)
       (with (->> (rand-phrase) (where :pitch (partial * 1.13)) (after 1)))
       (with (->> (rand-phrase) (where :pitch (partial * 0.93)) (after 2)))
       ;(with (->> (rand-phrase) (after 1)))
       ;(with (->> (rand-phrase) (after 2)))
       live/play))

(comment
  (let [offset1 1.13
        offset2 0.93]
    (->> harmonic
       (with (->> harmonic
                  (where :pitch (partial * offset1))
                  (after 0.6)))
       (with (->> harmonic
                  (where :pitch (partial * offset2))
                  (after 1.6)))
       (live/play))))


;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Species call       ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(def species-call'
  (let [root 110]
    (->> [14 18 16]
         (phrase [1/8 1/8 1/2])
         (where :pitch (absolute-harmonic-scale root)))))

(comment
  (live/play species-call')
)

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Ketar              ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn midi->harmonic [midi]
  (let [x nil]
    (get
      [   1    x    x    x    x    x    x    x    x    x    x    x
          2    x    x    x    x    x    x    3    x    x    x    x
          4    x    x    x    5    x    x    6    x    x    7    x
          8    x    9    x   10    x   11   12   13    x   14   15
         16   17   18   19   20   21   22,  24,  26   27   28,  30
         32,  34,  36,  38,  40,  42,  44,, 48,, 52,  54,  56,, 60]
      midi)))

(defn midi->diatonic [midi]
  (let [x nil]
    (get
      [[0 0]  x  [0 2]  x  [0 4][0 5]  x  [0 7]  x  [0 9]  x  [0 11]
       [1 0]  x  [1 2]  x  [1 4][1 5]  x  [1 7]  x  [1 9]  x  [1 11]
       [2 0]  x  [2 2]  x  [2 4][2 5]  x  [2 7]  x  [2 9]  x  [2 11]
       [3 0]  x  [3 2]  x  [3 4][3 5]  x  [3 7]  x  [3 9]  x  [3 11]
       [4 0]  x  [4 2]  x  [4 4][4 5]  x  [4 7]  x  [4 9]  x  [4 11]
       [5 0]  x  [5 2]  x  [5 4][5 5]  x  [5 7]  x  [5 9]  x  [5 11]]
      midi)))

(defn midi->freq [midi]
  (let [c1-midi 24
        c1-freq 37.71
        c2-midi 36
        c2-freq 65.41]
    ;(some-> midi (- c2-midi) midi->harmonic (* c2-freq))
    (some-> midi (- c1-midi) midi->harmonic (* c1-freq))
    ))

(def keytar-instrument corgan #_blorp)

(defonce notes-in-progress (atom {}))

(def keytar-note-on
  (on-event [:midi :note-on]
            (fn [{note :note velocity :velocity}]
              (let [unit-volume (+ 1/4 (* 3/4 (/ velocity 128)))
                    synth (some-> note midi->freq (/ 2) (keytar-instrument :dur 15 :vol (/ velocity 128)))]
                (swap! notes-in-progress assoc note synth)))
            ::midi-note-on))

(def keytar-note-off
  (on-event [:midi :note-off]
           (fn [{note :note}]
             (ctl (@notes-in-progress note) :gate 0))
           ::midi-note-off))

(defmethod live/play-note :butcherbird [{n :bird seconds :duration}]
  ((butcherbirds n)))

(def birdloop
  [{:time 0 :duration 8 :bird 23 :part :butcherbird}
   {:time 8 :duration 8 :bird 24 :part :butcherbird}
   {:time 16 :duration 8 :bird 19 :part :butcherbird}])

(comment
  (live/jam (var birdloop))
  (live/play birdloop)
  )
