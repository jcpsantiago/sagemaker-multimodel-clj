(ns jcpsantiago.sagemaker-multimodel-clj.loadmodel
  (:require [clj-boost.core :as xgb]
            [jcpsantiago.sagemaker-multimodel-clj.db :as db]))

(defn load-model!
  [model-name url current-models]
  (if (db/loaded? model-name @current-models)
    (do
      (println (str "Model " model-name " is already loaded!"))
      {:status 409
       :body ""})

    (let [xgbmodel (xgb/load-model (str url "/model.xgb"))]
      ; FIXME calling this fn requires to pass the atom unrefd 
      ; which is different from how we're calling the other fns
      ; we should have a consistent way of passing an atom
      (swap! current-models conj 
             {model-name 
              {:model-name model-name :model-url url :model xgbmodel}}))))

(defn model-obj
  [model-name current-models]
  (-> (filter #(% model-name) current-models) 
      first 
      (get-in [model-name :model]))) 

(defn map->dmatrix
  [data]
  ; FIXME maybe a better way to destructure?
  ; It should be flexible to allow arbitrary k v pairs
  ; so that it works with any input data
  (let [values (reduce conj [] (map (fn [[_ v]] v) data))]
    (xgb/dmatrix [values])))
  
(defn invoke-model
  [model-name data current-models]
  (if (db/loaded? model-name @current-models)
   (let [m (model-obj model-name @current-models) 
         d (map->dmatrix data)]
    (first (xgb/predict m d)))
   (do
      (println "Model " model-name " is not loaded!")
      {:status 404
       :body ""}))) 
