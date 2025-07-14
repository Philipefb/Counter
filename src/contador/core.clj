(ns contador.core
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :as route]
            [cheshire.core :as json]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.anti-forgery]
            [ring.util.response :as resp]
            [contador.db :as db])
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
    (case action
      "incrementa" (try
                     (let [valor-atual (db/get-valor)]
                       (db/insere-valor (inc valor-atual)))
                     (catch Exception _
                       (throw (ex-info "Valor inválido" {:valor (db/get-valor)}))))
      "zera" (try
               (db/insere-valor 0)
               (catch Exception _
                 (throw (ex-info "Valor inválido" {:valor 0}))))
      "insere" (try
                 (let [n (Integer/parseInt valor)]
                   (db/insere-valor n))
                 (catch Exception _
                   (throw (ex-info "Valor inválido para inserção" {:valor valor}))))
      (throw (ex-info "Ação inválida" {:acao action})))
    (-> (json/generate-string {:valor (db/get-valor)})
        (resp/response)
        (resp/content-type "application/json")))
  
  (GET "/api/contador" []
    (-> (json/generate-string {:valor (db/get-valor)})
        (resp/response)
        (resp/content-type "application/json")))

  (route/resources "/")
  (route/not-found "Página não encontrada"))

(def app
  (wrap-defaults app-routes site-defaults))

(defonce conn (delay db/conn))

(defn -main []
  (println "Abrindo conexao com o datomic...")
  (db/cria-schema @conn)
  (println "Rodando no http://localhost:8000/")
  (run-jetty app {:port 8000 :join? false}))
