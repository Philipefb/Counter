(ns contador.core
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :as route]
            [cheshire.core :as json]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.anti-forgery]
            [ring.util.response :as resp]
            [contador.db :as db]
            [contador.contador-handler :as handler])
  (:gen-class))

(defroutes app-routes

  (GET "/csrf-token" req
    (-> (json/generate-string {:token (:anti-forgery-token req)})
        (resp/response)
        (resp/content-type "application/json")))

  (GET "/" []
    (-> (slurp "resources/public/index.html")
        (resp/response)
        (resp/content-type "text/html")))

  (POST "/api/contador" [action valor]
    (handler/handle-post-contador action valor))

  (GET "/api/contador" []
    (handler/handle-get-contador))

  (route/resources "/")
  (route/not-found "Página não encontrada"))

(def app
  (wrap-defaults app-routes site-defaults))

(def app-sem-token
  (-> app-routes
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))

(defonce conn db/conn)

(defn -main []
  (println "Abrindo conexao com o datomic...")
  (db/cria-schema @conn)
  (println "Rodando no http://localhost:8000/")
  (run-jetty app {:port 8000 :join? false}))
