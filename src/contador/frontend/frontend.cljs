(ns contador.frontend.frontend
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [goog.object :as gobj]))

(defonce contador (r/atom 0))
(defonce erro (r/atom nil)) 

(defonce csrf-token (r/atom nil))

(defn fetch-csrf-token []
  (-> (js/fetch "/csrf-token")
      (.then #(.json %))
      (.then #(reset! csrf-token (.-token %)))))

(defn fetch-contador []
  (-> (js/fetch "/api/contador")
      (.then #(.json %))
      (.then #(reset! contador (.-valor %)))))

(defn incrementar []
  (-> (js/fetch "/api/contador?action=incrementa"
                #js {:method "POST"
                     :headers #js {"X-CSRF-Token" @csrf-token}})
      (.then #(fetch-contador))))

(defn zerar []
  (-> (js/fetch "/api/contador?action=zera"
                #js {:method "POST"
                     :headers #js {"X-CSRF-Token" @csrf-token}})
      (.then #(fetch-contador))))

(defonce timeout-id (r/atom nil))

(defn atualizar-valor [e]
  (let [novo-valor (gobj/get (.-target e) "value")]
    (reset! contador novo-valor)
    (if (and (not (clojure.string/blank? novo-valor))
             (re-matches #"^\d+$" novo-valor))
      (do
        (reset! erro nil)
        (when @timeout-id
          (js/clearTimeout @timeout-id))
        (reset! timeout-id
                (js/setTimeout
                 (fn []
                   (-> (js/fetch (str "/api/contador?action=insere&valor=" novo-valor)
                                 #js {:method "POST"
                                      :headers #js {"X-CSRF-Token" @csrf-token}})
                       (.then #(fetch-contador))))
                 300)))
      (do
        (reset! erro "Só é valido numeros inteiros e positivos")
        (js/setTimeout #(reset! erro nil) 3000)))))


(defn app []
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :align-items "center"
                 :justify-content "center"
                 :height "100vh"
                 :font-family "Arial"}}
   [:h1 "Contador"]
   [:div {:style {:font-size "48px" :margin "20px"}} @contador]
   [:div
    [:button {:on-click incrementar
              :style {:margin "5px"}} "Incrementar"]
    [:button {:on-click zerar
              :style {:margin "5px"}} "Zerar"]]
   [:input {:type "number"
            :placeholder "Digite um valor"
            :on-change atualizar-valor
            :style {:margin-top "20px"
                    :padding "10px"
                    :font-size "16px"
                    :width "120px"
                    :text-align "center"}}]
   (when @erro
     [:div {:style {:color "red" :margin-top "10px"}} @erro])])


(defn main []
  (js/console.log "App iniciando...")
  (fetch-contador)
  (fetch-csrf-token)
  (rdom/render [app] (.getElementById js/document "app")))



