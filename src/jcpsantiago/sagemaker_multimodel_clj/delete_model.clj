(ns jcpsantiago.sagemaker-multimodel-clj.delmodel
  (:require [jcpsantiago.sagemaker-multimodel-clj.db :as db]))

(defn delete-model
  [model-name current-models]
  (if (db/loaded? model-name current-models)
    (do
      (println (str "Unloading " model-name " from memory..."))
      (reset! current-models (filter #(not (% model-name)) current-models)))

    (do
      (println (str "Model " model-name " is not loaded"))
      {:status 404
       :body ""})))
