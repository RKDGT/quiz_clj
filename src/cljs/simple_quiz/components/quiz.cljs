(ns simple-quiz.components.quiz
  (:require
   [re-frame.core :as re-frame]
   [simple-quiz.subs :as subs]
   [reagent.core :as r]))

(defmulti question-answers (fn [question] (:type question)))

(defmethod question-answers
  "free-text"
  []
  ;; (reset! question-card-answer-id (inc @question-card-answer-id))
  [:div.free-text
   [:input {:type "text"}]])

(defmethod question-answers
  "single-choice"
  [question]
  [:div.single-choice
   (for [answer (:values question)]
     [:div.single-choice-answer
      [:input {:type "radio" :name "lol"}]
      [:p answer]])])

(defmethod question-answers
  "multiple-choice"
  [question]
  [:div.multiple-choice
   (for [answer (:values question)]
     [:div.single-choice-answer
      [:input {:type "checkbox" :name "lol"}]
      [:p answer]])])

(defn question-card [question]
  [:div.question
   [:div.quesion-header [:h2 (:question question)]
    (when (:required question)
      [:p "*"])]
   [question-answers question]])



(defn read-answers []
  (loop [result []
         [question & questions] (.getElementsByClassName js/document "question")
         formed-obj {}]
    (if (not questions)
      {:title (.-innerHTML (first (.getElementsByTagName js/document "h1")))
       :result result}
      (do
        (assoc formed-obj :question (.-innerHTML (first (.getElementsByTagName question "h2"))))
        (assoc formed-obj :type (.-className (.-lastChild (first (.getElementsByClassName js/document "question")))))
        (assoc formed-obj :values (.-value (.-firstChild (.-lastChild (first (.getElementsByClassName js/document "question"))))))
        (recur result questions {})))))

;; (comment 
;; ;;   (js->clj (slurp "resources/quiz.json"))
;;   (js/console.log (.-className (.-lastChild (first (.getElementsByClassName js/document "question")))))
;;   (js/console.log (.-value (.-firstChild (.-lastChild (first (.getElementsByClassName js/document "question"))))))
;;   (js/console.log (.-lastChild (second (.getElementsByClassName js/document "question"))))
;;   (read-answers))
;; -------------------------
;; Page components

(defn quiz []
  (fn []
    (let [questions (:quiz @(re-frame/subscribe [::subs/quiz]))]
      [:div.questions
       [:h1 (:title questions)]
       [:p "all question that marked by * is required"]
       (for [question (:questions questions)]
         [question-card question])
       [:button {:type "submit" :on-click read-answers} "Send answer"]])))
