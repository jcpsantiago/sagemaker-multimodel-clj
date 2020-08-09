(ns jcpsantiago.sagemaker-multimodel-clj.db)

; not sure if Sagemaker will create race conditions to load models
; using an atom here in case it does
(def current-models
  (atom [])) 

(defn loaded?
  [model-name current-models]
  (->> current-models
       (filter #(% model-name))
       seq
       seq?))
