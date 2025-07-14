(ns contador.db
  (:require [datomic.api :as d]))

;; comando para rodar o datomic
;; bin/transactor .\config\samples\dev-transactor-template.properties


(def uri "datomic:dev://localhost:4334/contador")

(defn cria-db [] (d/create-database uri))

(def schema
  [{:db/ident       :count/valor
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc         "Valor do contador"}])

;; conexao
(def conn (d/connect uri))

(defn atributo-existe? [db ident]
  (boolean
   (d/q '[:find ?e .
          :in $ ?ident
          :where [?e :db/ident ?ident]]
        db ident)))

;; somente cria se nao existir
(defn cria-schema [conn]
  (let [db (d/db conn)]
    (when-not (atributo-existe? db :count/valor)
      (println "Atributo nao existe, criando um novo...")
      @(d/transact conn [{:db/ident       :count/valor
                          :db/valueType   :db.type/long
                          :db/cardinality :db.cardinality/one
                          :db/doc         "Valor do contador"}]))))

;; adiciona uma transacao em um id especifico
(defn insere-valor [valor]
  (let [db (d/db conn)
        id-schema (d/q '[:find ?e . :where [?e :count/valor _]] db)]
    @(d/transact conn [{:db/id id-schema :count/valor valor}])))

;; busca o valor em um atributo especifico
(defn get-valor []
  (let [db (d/db conn)
        id-schema (d/q '[:find ?e . :where [?e :count/valor _]] db)]
    (ffirst (d/q '[:find ?v :in $ ?id :where [?id :count/valor ?v]] db id-schema))))


;; cria o schema
;; @(d/transact conn schema)

;;buscar o id de um schema
;; (def id (d/q '[:find ?e . :where [?e :count/valor _]] db))

;; buscar atributos existentes
;; (d/q '[:find ?ident :where [_ :db/ident ?ident]] db) 

;; busca o valor mais recente do atributo
;; (d/q '[:find ?v . :where [_ :count/valor ?v]] db)