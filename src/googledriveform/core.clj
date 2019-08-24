(ns googledriveform.core
  (:require [org.httpkit.server :as server]
            [org.httpkit.client :as http]
            [clojure.core.async :refer [thread]]
            [clojure.string :as str]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [googledriveform.handler :refer :all]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [clojure.pprint :refer [pprint]]
            [cheshire.core :as json]
            [clojure.tools.logging :as log])
  (:gen-class))

;;(filter #(nil? (val %1)) {:a g-client-id :b g-client-id-non})
(def port (Integer/parseInt (or (System/getenv "PORT") "3000")))
(def hook-url (System/getenv "SLACK_WEBHOOK_URL"))
(def g-client-id (System/getenv "GOOGLE_CLIENT_ID"))
(def g-client-secret (System/getenv "GOOGLE_CLIENT_SECRET"))
(def g-refresh-token (System/getenv "GOOGLE_REFRESH_TOKEN"))
(def g-parentfolder-id (System/getenv "GOOGLE_PARENT_ID"))

(defn log-http-status
  [{:keys [status body error]} service type]
  (if (not (= status 200))
    (log/error "Failed, exception is" body)
    (log/info (str service " async HTTP " type " success: ") status)))

(defn post-to-slack
  "Post message to Slack"
  [payload url]
  (-> @(http/post
        url
        {:body (json/generate-string payload)
         :content-type :json})
      (log-http-status "Slack" "POST")))


;; what a nightmare... no library did the job correctly at the time of writing
;; in the end I setup https://developers.google.com/oauthplayground/
;; with webapp credentials (because of redirct uri) and reproduced
;; the http call found there
(defn refresh-access-token
  "POSTs requests to googleapis to refresh the access token"
  []
  (log/info "Refreshing Google OAuth2 token")
  (-> @(http/post "https://www.googleapis.com/oauth2/v4/token"
                  {:query-params
                   {"client_secret" g-client-secret
                    "grant_type" "refresh_token"
                    "refresh_token" g-refresh-token
                    "client_id" g-client-id}})
      :body
      (json/parse-string true)
      :access_token))

(defn upload-to-drive
  "Uploads a file to Google Drive"
  [auth-token filename parent-id file content-type]
  (-> @(http/request
        {:url "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
         :method :post
         :headers {"Authorization" (str "Bearer " auth-token)
                   "Content-Type" "multipart/related,multipart/form-data; boundary=--------------------------multipartboundary"}
         :multipart [{:name "metadata"
                      :content (json/generate-string {:name (str/trim filename)
                                                      :parents [parent-id]})
                      :content-type "application/json"}
                     {:name "media" :content file
                      :content-type content-type}]})
      :body
      (json/parse-string)))

(defn write-to-file
  "Write string to file"
  [target-file x]
  (log/info "Writing to temporary file")
  (with-open [file (clojure.java.io/writer target-file)]
    (binding [*out* file]
      (println x))))

(defn create-temp-file
  "Create a temporary file to hold the current bank balance"
  [filename extension]
  (log/info (str "Creating " filename extension " temporary file"))
  (java.io.File/createTempFile filename extension))

(defroutes app-routes
  (GET "/" [] (home-page "display:block" "display:none"))
  (POST "/" req
       (let [params (get req :multipart-params)
             name (params "name")
             content-type (:content-type (params "uploaded-file"))
             file-extension (re-find #"\.(csv|xls|xlsx|tsv|ods)$"
                                     (:filename (params "uploaded-file")))
             file (:tempfile (params "uploaded-file"))
             main-file-name (str name "_stuff" (first file-extension))
             local-temp-file (create-temp-file "temp-randnumber-file" ".txt")
             rand-number (params "rand-number")
             access-token (refresh-access-token)]
         (do
           (log/info (str name " is submitting data"))
           ;; main file
           (log/info (str "Uploading " main-file-name " to google drive"))
           (upload-to-drive access-token main-file-name
                            g-parentfolder-id  file content-type)
           (.delete file)

           ;; save form field as file
           (write-to-file local-temp-file rand-number)
           (log/info (str "Uploading balance from " name " to google drive"))
           (upload-to-drive access-token (str name "_balance.txt")
                            g-parentfolder-id local-temp-file "text/plain")
           (.delete local-temp-file)

           ;; slack channel is linked to the webhook url
           (post-to-slack
            {:text (str "<!here> *" name "* uploaded a file to the drive!")}
            hook-url)

           ;; show success page :D
           (log/info "Showing success page")
           (home-page "display:none" "display:block"))))

  (route/resources "/")
  (route/not-found "404"))

(defn -main
  "Main entry point"
  [& args]
  (log/info "Starting server")
  (server/run-server
   (-> app-routes
       (wrap-defaults site-defaults)
       wrap-params)
   {:port port})
  (log/info "clj-googledrive-form is alive!")
  (log/info (str "Running webserver at http:/127.0.0.1:" port "/")))
