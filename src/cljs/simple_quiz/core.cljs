(ns simple-quiz.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [reagent.session :as session]
   [reitit.frontend :as reitit]
   [clerk.core :as clerk]
   [accountant.core :as accountant]
   [simple-quiz.components.quiz :as quiz-comp]
   [re-frame.core :as re-frame]
   [simple-quiz.subs :as subs]
   [simple-quiz.events :as events]))

;; (def questions (r/atom {:title "Example Survey"
;;                         :questions [{:question "Enter your city:"
;;                                      :type "free-text"
;;                                      :required false}
;;                                     {:question "Do you like this survey?"
;;                                      :required true
;;                                      :type "single-choice"
;;                                      :values ["Yes" "No"]}
;;                                     {:question "What are your favorite fruits?"
;;                                      :type "multiple-choice"
;;                                      :required true
;;                                      :values ["Apple" "Orange" "Banana" "Tomato"]}]}))

(def questionss (re-frame/subscribe [::subs/quiz]))
;; -------------------------
;; Routes

(def router
  (reitit/router
   [["/" :index]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

;; -------------------------
;; Translate routes -> page components

;; (defn tr []
;;   (let [curr (re-frame/subscribe [::subs/quiz])]
;;     (js/console.log (str (:title @curr)))
;;     [:p "lol"]))

(defn page-for [route]
  (let [questions (re-frame/subscribe [::subs/quiz])]
    (prn @questions)
    (case route
      :index (quiz-comp/quiz @questions)
    ;; :index tr
      )))


;; -------------------------
;; Page mounting component

(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
       [page])))

;; -------------------------
;; Initialize app

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  ;; (let [root-el (.getElementById js/document "app")]
  ;;   (rdom/unmount-component-at-node root-el)
  ;;   (rdom/render [current-page] root-el))
  (rdom/render [current-page] (.getElementById js/document "app"))
  )

(defn init! []
  (re-frame/dispatch-sync [::events/initialize-db])
  (re-frame/dispatch [:request])
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match (reitit/match-by-path router path)
            current-page (:name (:data  match))
            route-params (:path-params match)]
        (r/after-render clerk/after-render!)
        (session/put! :route {:current-page (page-for current-page)
                              :route-params route-params})
        (clerk/navigate-page! path)))
    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))
