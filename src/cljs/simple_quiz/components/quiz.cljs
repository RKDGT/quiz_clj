(ns simple-quiz.components.quiz
  (:require
   [re-frame.core :as re-frame]
   [simple-quiz.subs :as subs]
   [simple-quiz.events :as events]
   [reagent.core :as r]))

(def question-id (atom 0))

(def answer-key (atom 0))

(def required (atom ""))

(defmulti question-answers (fn [question] (:type question)))

(defmethod question-answers
  "free-text"
  [question]
  [:div.free-text (when
                   (:required question)
                    {:data-type (:required question)})
   [:input (if (:required  question)
                           {:type "text" :name @question-id :required ""}
                           {:type "text" :name @question-id})]])

(defmethod question-answers
  "single-choice"
  [question]
  [:div.single-choice (when
                       (:required question)
                        {:data-type (:required question)})
   (for [answer (:values question)]
     (do
       (swap! answer-key inc @answer-key)
       [:div.choice-answer {:key @answer-key}
        [:input.radio (if (:required  question)
                           {:type "radio" :name @question-id :required ""}
                           {:type "radio" :name @question-id})]
        [:p answer]]))])

(defmethod question-answers
  "multiple-choice"
  [question]
  [:div.multiple-choice
   (for [answer (:values question)]
     (do
       (swap! answer-key inc @answer-key)
       [:div.choice-answer {:key @answer-key}
        [:input.checkbox (if (:required question)
                           {:type "checkbox" :name @question-id :required ""}
                           {:type "checkbox" :name @question-id})]
        [:p answer]]))])

(defn question-card 
  [question]
  (swap! question-id inc @question-id)
  [:div.question {:key @question-id}
   [:div.quesion-header 
    [:h2 (:question question)]
    (when (:required question)
      [:p "*"])]
   [question-answers question]])

(defn get-radio-answer 
  [question]
  (loop [[answer & answers]  (.getElementsByClassName question "choice-answer")]
    (if (.-checked (.-firstChild answer))
      (.-innerHTML (.-lastChild answer))
      (recur answers))))

(defn get-check-answer 
  [question]
  (loop [[answer & answers]  (.getElementsByClassName question "choice-answer")
         res []]
    (if (not answer)
      res
      (if (.-checked (.-firstChild answer))
        (recur answers (conj res (.-innerHTML (.-lastChild answer))))
        (recur answers res)))))

(defn get-answer-by-question
  [question]
  (case (.-lastChild (.-className question))
    "free-text" (.-value (first (.getElementsByTagName question "input")))
    "single-choice" (get-radio-answer question)
    "multiple-choice" (get-check-answer question)))
 
(defn read-answers
  []
  (loop [result []
         [question & questions] (.getElementsByClassName js/document "question")]
    (if (not question)
      (let [result {:title (.-innerHTML (first (.getElementsByTagName js/document "h1")))
                    :results result}]
        (re-frame/dispatch-sync [::events/post-results result]))
      (recur (conj result {:question (.-innerHTML (first (.getElementsByTagName question "h2")))
                           :type (.-className (.-lastChild question))
                           :values (get-answer-by-question question)})
             questions))))

(defn quiz 
  []
  (fn []
    (let [questions (:quiz @(re-frame/subscribe [::subs/quiz]))]
      [:div.questions
       [:div.questions-title
        [:div.questions-title-upper-line]
        [:h1 (:title questions)]
        [:p "all question that marked by * is required"]]
       (for [question (:questions questions)]
         [question-card question])
       [:button.send-answer {:type "submit" :on-click read-answers} "Send answer"]])))
