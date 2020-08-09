(ns jcpsantiago.sagemaker-multimodel-clj.handlers
  (:require [ring.util.response :as ring-resp]
            [jcpsantiago.sagemaker-multimodel-clj.db :as db]
            [clj-boost.core :as xgb]))

(defn ping
  [_]
  (ring-resp/response ""))

(defn model-info 
  [current-models] 
  ; FIXME probably not the most efficient solution here
  (reduce-kv 
    (fn [_ _ v] 
     ; Sagemaker expects these names
     {:modelName (:model-name v) 
      :modelUrl (:model-url v)}) 
   {} current-models))

(defn list-models
  [_]
  (let [models @db/current-models]
    (if (empty? models)
      {:status 200
       :body {:models []}}
      (let [res (into [] (map #(model-info %) models))]
        {:status 200
         :body {:models res}}))))

(defn load-model!
  [request]
  (let [{{:keys [model_name url]} :json-params} request]
    (println model_name)
    (println url)
    (println "Loading model" model_name "from" url)
    (if (db/loaded? model_name @db/current-models)
      (do
        (println (str "Model " model_name " is already loaded!"))
        {:status 409
         :body ""})

      (let [fullpath (str url "/model.xgb") 
            xgbmodel (try 
                       (xgb/load-model fullpath)
                       (catch Exception e (str "Exception: " (.getMessage e))))]
        (if (string? xgbmodel)
          (do
            (println "Failed to load model:" xgbmodel)
            {:status 500
             :body xgbmodel})
        ; FIXME calling this fn requires to pass the atom unrefd 
        ; which is different from how we're calling the other fns
        ; we should have a consistent way of passing an atom
          (do
            (println "Model" model_name "is loaded!")
            (swap! db/current-models conj 
                   {model_name 
                    {:model-name model_name :model-url url :model xgbmodel}})
            {:status 200
             :body ""}))))))

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
  [request]
  (let [{{model-name :model-name} :path-params} request
        {{input :input} :json-params} request]
    (if (db/loaded? model-name @db/current-models)
     (let [m (model-obj model-name @db/current-models) 
           d (map->dmatrix input)
           prediction (first (xgb/predict m d))]
      (println "Model" model-name "predicted" prediction)
      {:status 200
       :body {:prediction prediction}})

     (do
        (println "Model" model-name "is not loaded!")
        {:status 404
         :body ""})))) 

(defn unload-model!
  [request]
  (let [{{model-name :model-name} :path-params} request]
    (if (db/loaded? model-name @db/current-models)
      (do
        (println (str "Unloading " model-name " from memory..."))
        (reset! db/current-models (filter #(not (% model-name)) @db/current-models))
        {:status 200
         :body ""})

      (do
        (println (str "Model " model-name " is not loaded"))
        {:status 404
         :body ""}))))
