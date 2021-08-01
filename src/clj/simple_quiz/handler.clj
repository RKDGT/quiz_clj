(ns simple-quiz.handler
  (:require
   [hiccup.page :refer [include-js include-css html5]]
   [config.core :refer [env]]
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]))

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

(defroutes app
  (GET "/" [] index-handler)
  (GET "/question" [] get-quiz)
  (route/not-found "<h1>Page not found</h1>"))
