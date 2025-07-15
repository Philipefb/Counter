(ns contador.core-test
  (:require [cheshire.core :as json]
            [clojure.test :refer :all]
            [contador.core :refer :all]
            [contador.db :as db]
            [ring.mock.request :as mock]
            [ring.util.response :as response]
            [contador.core :refer [app]]))


(deftest core-get-test
  (with-redefs [db/get-valor (fn [] 45)
                db/insere-valor (fn [_] nil)]

    (testing "GET /"
      (let [response (app (mock/request :get "/"))]
        (is (= 200 (:status response)))
        (is (some? (:body response)))))

    (testing "GET /csrf-token"
      (let [response (app (mock/request :get "/csrf-token"))
            body (json/parse-string (:body response) true)]
        (is (= 200 (:status response)))
        (is (contains? body :token))))

    (testing "GET /api/contador"
      (let [response (app (mock/request :get "/api/contador"))
            body (json/parse-string (:body response) true)]
        (is (= 200 (:status response)))
        (is (contains? body :valor))
        (is (= 45 (:valor (json/parse-string (:body response) true))))))))

(deftest core-post-test
  (with-redefs [db/get-valor (fn [] 200)
                db/insere-valor (fn [_] 200)]

    (testing "POST /api/contador?action=incrementa"
      (let [request (-> (mock/request :post "/api/contador")
                        (mock/query-string "action=incrementa"))
            response (app-sem-token request)
            body (json/parse-string (:body response) true)]
        (is (= 200 (:status response)))
        (is (contains? body :valor))))

    (testing "POST /api/contador?action=insere&valor=200"
      (let [request (-> (mock/request :post "/api/contador")
                        (mock/query-string "action=insere&valor=200"))
            response (app-sem-token request)
            body (json/parse-string (:body response) true)]
        (is (= 200 (:status response)))
        (is (= (:valor body) 200))))

    (testing "POST /api/contador?action=zera"
      (with-redefs [db/get-valor (fn [] 0)]
        (let [request (-> (mock/request :post "/api/contador")
                          (mock/query-string "action=zera"))
              response (app-sem-token request)
              body (json/parse-string (:body response) true)]
          (is (= 200 (:status response)))
          (is (= (:valor body) 0)))))))


;; (deftest test-post-incrementa-mockado
;;   (testing "POST incrementa com valor mockado"
;;     (with-redefs [db/get-valor (fn [] 45)
;;                   db/insere-valor (fn [_] nil)]
;;       (let [req (-> (mock/request :post "/api/contador")
;;                     (mock/query-string "action=incrementa"))
;;             res (app req)
;;             body (json/parse-string (:body res) true)]
;;         (is (= 200 (:status res)))
;;         (is (= 99 (:valor body)))))))
