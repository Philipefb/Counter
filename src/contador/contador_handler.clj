(ns contador.contador-handler
  (:require [ring.util.response :as resp]
            [cheshire.core :as json]
            [contador.db :as db]))

(defn handle-post-contador [action valor]
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

(defn handle-get-contador []
  (-> (json/generate-string {:valor (db/get-valor)})
      (resp/response)
      (resp/content-type "application/json")))
