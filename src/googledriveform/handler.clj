(ns googledriveform.handler
  (:require [hiccup.core :refer :all]
            [hiccup.page :refer :all]
            [ring.util.anti-forgery :refer [anti-forgery-field]])
  (:gen-class))

;; https://codepen.io/sakamies/pen/yzYypW
(def submit-spinner
  "function LoadingSpinner(form, spinnerHTML) {
    form = form || document

    //Keep track of button & spinner, so there's only one automatic spinner per form
    var button
    var spinner = document.createElement('div')
    spinner.innerHTML = spinnerHTML
    spinner = spinner.firstChild

    //Delegate events to a root element, so you don't need to attach a spinner to each individual button.
    form.addEventListener('click', start)

    //Stop automatic spinner if validation prevents submitting the form
    //Invalid event doesn't bubble, so use capture
    form.addEventListener('invalid', stop, true)

    //Start spinning only when you click a submit button
    function start(event) {
        if (button) stop()
        button = event.target
        if (button.type === 'submit') {
            LoadingSpinner.start(button, spinner)
        }
    }

    function stop() {
        LoadingSpinner.stop(button, spinner)
    }

    function destroy() {
        stop()
        form.removeEventListener('click', start)
        form.removeEventListener('invalid', stop, true)
    }

    return {
        start: start,
        stop: stop,
        destroy: destroy
     }
  }

  LoadingSpinner.start = function(element, spinner) {
      element.classList.add('loading')
      return element.appendChild(spinner)
  }

  LoadingSpinner.stop = function(element, spinner) {
      element.classList.remove('loading')
      return spinner.remove()
  }

  var exampleForm = document.querySelector('#uploadform')
  var exampleLoader = new LoadingSpinner(exampleForm, '...')")

(def upload-button-jq
  "$('#choose-file-btn').click(function () {
   $('#choose-file').trigger('click')
   });")

(def upload-filename-js
  (str "$('input[type=file]').change(function(){
  var filename = $(this).val().split(" (str "'\\") (str "\\'") ").pop();
  var idname = $(this).attr('id');
  console.log($(this));
  console.log(filename);
  console.log(idname);
  $('span.'+idname).next().find('span').html(filename);
});"))
                                 ;

(defn success-page
  [visibility]
  [:div {:id "success" :class visibility}
   [:h4 {:class "tc mb3"}
    "File successfuly uploaded"]
   [:p "Thank you for using this form and sending your file into the void, you're awesome!"]
   [:p {:class "fl"}"&mdash; Mr Pepe"]])

(def input-field-style "input-reset ba b--black-20 pa2 mb2 db w-100 code f4")

(defn home-page [inner-viz success-viz]
  "Landing page"
  (html5 {:lang "en"}
         [:head (include-css "https://unpkg.com/tachyons@4.10.0/css/tachyons.min.css")
                (include-js "https://kit.fontawesome.com/8a9f12dee1.js"
                            "https://code.jquery.com/jquery-3.4.1.min.js")]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]

    [:body
     [:div {:class "w-100 fixed bg-yellow"}
      [:div {:class "w-70 center"}
       [:h1 {:class "f2 fl athelas i fw2"} "pepe."]
       [:h1 {:class "f2 fr fw2 tracking-mega"}
        [:a {:href "https://github.com/jcpsantiago"}
         [:p {:class "dn"} "hidden :D"] ;; helps with alignment
         [:i {:class "black-20 hover-black fab fa-github"}]]
        [:a {:href "https://www.linkedin.com/in/jcpsantiago/"}
         [:i {:class "black-20 hover-black fab fa-linkedin pl2"}]]]]]


     [:header {:class "w-100 pa6 ph5-ns bg-yellow"}]
      ;;[:div {:class "w-30 center"}
       ;;[:svg {:class "bottom absolute mw-100" :xmlns "http://www.w3.org/2000/svg"}
        ;;[:circle {:cx "50" :cy "50" :r "50" :fill "black"}]]

     [:div {:class "w-60 center"}
      [:div {:class "w-100 pb4"}
       [:h2 {:class "f6 athelas ttu trackin fw4 gray mb0 pb0"} "useless clojure things"]
       [:h1 {:class "f1 athelas tracking lh-title mv0"} "The Google Drive void"]]

      [:div {:class (str "lh-copy pb3 measure " inner-viz)}
       [:p {:class "f4 athelas"}
           "I lose files in Google Drive all the time. Someone sends me a link,
            I open it and it's gone forever. Unfortunately, it's a useful service,
            so I created this form to throw files into the depths. Fill the form
            below, press send, and rejoice in the fact you'll never find that file again."]]

      ;; -- form starts here--
      [:form {:id "uploadform" :class "w-100 measure mb5 avenir"
              :method "POST" :enctype "multipart/form-data"}
       (anti-forgery-field)
       [:div {:class (str "forminner " inner-viz)}

        ;; -- name --
        [:div {:class "mt2"}
         [:label {:class "f6 b db mb2" :for "name"} "Your name"]
         [:input {:id "name" :type "text" :name "name" :required ""
                  :class input-field-style}]]

        ;; -- random number in european format --
        [:div {:class "mt3"}
         [:label {:class "f6 b db mb2" :for "randnumber"} "Random number"]
         [:input {:id "randnumber" :type "text" :name "randnumber" :required ""
                  :pattern #"\s*-?((\d{1,3}(\.(\d){3})*)|\d*)(,\d{1,2})?\s?(\u20AC)?\s*"
                  :class input-field-style}]
         [:small {:class "f6 black-60 db mb2"} "In european format like 12.345,67"]

        ;; -- csv input --
         [:div {:class "mt3"}
          ;; https://stackoverflow.com/questions/31937112/html-form-cannot-post-file-content
          [:label {:class "f6 b db mb3" :for "choose-file"} "Your important work spreadsheet"]

          ;; -- https://jsfiddle.net/m7eqj9ku/1/
          [:span {:class "file-01"}
           [:input {:id "file-01" :type "file" :name "choose-file" :required ""
                    :accept ".xls, .xlsx, .ods, .csv, .tsv, .pages,
                             application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,
                             application/vnd.ms-excel, text/csv,
                             application/vnd.oasis.opendocument.spreadsheet"
                    :class "dn w-100 file-01"}]]
          [:label {:class "b ph3 pv2 ba b--black bg-transparent pointer f6" :for "file-01"}
           [:span "Upload a file"]]]]

          ;; -- submit button --
        [:div {:class "mt4"}
         [:input {:class "b ph3 pv2 input-reset ba b--black white
                          pointer f6 bg-animate bg-black hover-pink"
                   :type "submit" :name "lose-file" :value "Lose it forever"}]]]

       ;; -- success page --
       (success-page success-viz)]]]

    ;; -- show the loading "spinner" after clicking send --
    [:script submit-spinner]
    ;;[:script upload-button-jq]
    [:script upload-filename-js]))
