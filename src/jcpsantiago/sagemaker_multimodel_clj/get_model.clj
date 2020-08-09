(ns jcpsantiago.sagemaker-multimodel-clj.getmodel)

(defn model-attr
  [model-name current-models k]
  (-> (filter #(% model-name) @current-models) 
      first 
      (get-in [model-name k])))

(defn get-model
  [model-name current-models]
  (if (seq (filter #(% model-name) @current-models))
    {:status 200
     :body {:modelName (model-attr model-name current-models :modelName)
            :modelUrl (model-attr model-name current-models :modelUrl)}}
    (do
      (println (str "Model " model-name " is not loaded!"))
      {:status 404
       :body ""})))
