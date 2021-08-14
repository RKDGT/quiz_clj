(ns simple-quiz.handler
  (:require
   [hiccup.page :refer [include-js include-css html5]]
   [config.core :refer [env]]
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]
   [clojure.data.json :as json]
   [cheshire.core :as ch]))

(def result (atom {}))

(def mount-target
  [:div#app
   [:h1.loading-main-page "Loading...."]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
   (head)
   [:body {:class "body-container"}
    mount-target
    (include-js "/js/app.js")
    [:script "simple_quiz.core.init_BANG_()"]]))


(defn index-handler
  [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (loading-page)})

(defn get-quiz [_]
  (slurp "resources/quiz.json"))


(defn get-results [_]
  (slurp "resources/results.json"))

(defn post-results [data]
  (let [body (json/read-str (slurp (:body data)))]
    (println body)
    (spit "resources/results.json" (ch/generate-string body)))
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body "your answer was saved"})

(defroutes app
  (GET "/" [] index-handler)
  (GET "/stat" [] index-handler)
  (GET "/question" [] get-quiz)
  (GET "/results" [] get-results)
  (POST "/post-results" [] post-results)
  (route/not-found "<h1>Page not found</h1>"))