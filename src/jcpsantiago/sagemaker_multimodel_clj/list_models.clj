(ns jcpsantiago.sagemaker-multimodel-clj.listmodels)

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
  [current-models]
  (if (empty? current-models)
    {:status 200
     :body {}}
    (let [res (into [] (map #(model-info %) current-models))]
      {:status 200
       :body res})))
