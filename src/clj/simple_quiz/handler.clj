(ns simple-quiz.handler
  (:require
   [hiccup.page :refer [include-js include-css html5]]
   [config.core :refer [env]]
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]
   [clojure.data.json :as json]
   [clojure.walk :as cwalk]
   [clojure.string :as cstring]
   [clojure.core.async :refer [go-loop <! timeout]]))

(def result (atom {}))

(def error-of-update (atom []))

(defn formed-result [questions]
    {:title (:title questions) 
     :questions (for [question (:questions questions)]
                  {:question (:question question)
                   :type (:type question)
                   :values (if (= (:type question) "free-text")
                             []
                             (cwalk/keywordize-keys (zipmap (:values question) (vec (replicate (count (:values question)) 0)))))})})

(defn read-quiz-file []
  (slurp "resources/quiz.json"))

(defn read-results-file []
  (slurp "resources/results.json"))

(defn get-quiz [_]
  (read-quiz-file))

(defn get-results [_]
  (if (seq @result)
    (json/write-str @result)
    (read-results-file)))

(defn update-multiple-choice-res [to-change base-of-change]
  (loop [[answer & rest-answ] base-of-change
         res to-change]
    (if (not (seq answer))
      res
      (recur rest-answ (update-in res [(keyword answer)] inc)))))

(defn updater [to-change base-of-change]
  (let [type (:type to-change)
        base-on-value (:values base-of-change)]
    (when (seq base-on-value)
     (if (and (= (:question to-change) (:question base-of-change)) (= type (:type base-of-change)))
       {:question (:question to-change)
        :type type
        :values (cond
                  (= type
                     "free-text")
                  (conj (:values to-change) base-on-value)
                  (= type
                     "single-choice")
                   (update-in (:values to-change) [(keyword base-on-value)] inc)
                  (= type
                     "multiple-choice")
                  (update-multiple-choice-res (:values to-change) base-on-value))}
       (reset! error-of-update "please reload page, and try pass the test again")))))

(defn update-stats [answers]
    (if (= (count (:questions @result)) (count answers))
      (loop [[res & rest-res] (:questions @result)
             [answ & rest-answ] answers
             updated-stat []]
        (if (or (empty? res) (empty? answ))
          (swap! result assoc-in [:questions] updated-stat)
          (recur rest-res rest-answ (conj updated-stat (updater res answ)))))
      (reset! error-of-update "please reload page, and try pass the test again")))

(defn post-results [data]
  (let [body (json/read-str (slurp (:body data)) :key-fn keyword)]
    (update-stats body)
    {:status 200
     :headers {"Content-Type" "text/json"}
     :body (json/write-str {:m "your answer was saved"})}))

(def mount-target
  [:div#app
   [:h1.loading-main-page "Loading...."]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn convert-js->clj [json-obj] 
  (json/read-str (json-obj) :key-fn keyword))

(defn init-results []
  (let [result-file-info (read-results-file)
        expecting-result (formed-result (convert-js->clj read-quiz-file))]
    (if (cstring/blank? result-file-info)
      (do
        (spit "resources/results.json" (json/write-str expecting-result))
        (reset! result expecting-result))
      (let [res (merge expecting-result (convert-js->clj read-results-file))]
        (spit "resources/results.json" (json/write-str res))
        (reset! result res)))))

(defn auto-res-saver []
  (go-loop []
    (<! (timeout 20000))
    (spit "resources/results.json" (json/write-str @result))))

(defn loading-page []
  (do
    (when (not (seq @result))
      (init-results))
    (auto-res-saver)
    (html5
     (head)
     [:body {:class "body-container"}
      mount-target
      (include-js "/js/app.js")
      [:script "simple_quiz.core.init_BANG_()"]])))


(defn index-handler
  [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (loading-page)})

(defroutes app
  (GET "/" [] index-handler)
  (GET "/stat" [] index-handler)
  (GET "/question" [] get-quiz)
  (GET "/results" [] get-results)
  (POST "/post-results" [] post-results)
  (route/not-found "<h1>Page not found</h1>"))