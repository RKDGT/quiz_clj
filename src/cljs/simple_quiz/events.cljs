(ns simple-quiz.events
  (:require
   [re-frame.core :as re-frame]
   [simple-quiz.db :as db]
   [ajax.core :as ajax]
   [day8.re-frame.http-fx]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/quiz-db))

(re-frame/reg-event-fx
 ::read-quiz-json
 (fn []
   {:http-xhrio {:uri             "http://localhost:3000/question"
                 :method          :get
                 :timeout         10000
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::process-quiz-response]
                 :on-failure      [:bad-response]}}))

(re-frame/reg-event-fx
 ::read-results-json
 (fn []
   {:http-xhrio {:uri             "http://localhost:3000/results"
                 :method          :get
                 :timeout         10000
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::process-results-json]
                 :on-failure      [:bad-response]}}))


(re-frame/reg-event-fx
  ::post-results
  (fn [_world [_ val]]
    {:http-xhrio {:method          :post
                  :uri             "http://localhost:3000/post-results"
                  :params          val
                  :timeout         5000
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [::success-post-result]
                  :on-failure      [:bad-response]}}))

(re-frame/reg-event-db
 ::process-quiz-response
 (fn [db [_ response]]
   (-> db
       (assoc-in [:data :quiz] (js->clj response)))))


(re-frame/reg-event-db
 ::process-results-json
 (fn [db [_ response]]
   (-> db
       (assoc-in [:data :results] (js->clj response)))))

(re-frame/reg-event-db
 ::success-post-result
 (fn [_ [_ response]]
   (js/alert (:m response))
   (.reload js/location)))

(re-frame/reg-event-db
 :bad-response
 (fn [_ [_ response]]
   (println response)))
