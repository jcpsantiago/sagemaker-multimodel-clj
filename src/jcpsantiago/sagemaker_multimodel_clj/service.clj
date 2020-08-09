(ns jcpsantiago.sagemaker-multimodel-clj.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.interceptor.helpers :as interceptor]
            [ring.util.response :as ring-resp]
            [jcpsantiago.sagemaker-multimodel-clj.handlers :as handlers]
            [jsonista.core :as json]))

(def common-interceptors [(body-params/body-params) http/html-body])

(def json-body
  "Set the Content-Type header to \"application/json\" and convert the body to
  JSON if the body is a collection and a type has not been set."
  (interceptor/on-response
    ::json-body
    (fn [response]
      (let [body (:body response)
            content-type (get-in response [:headers "Content-Type"])]
        (if (and (coll? body) (not content-type))
          (-> response
              (ring-resp/content-type "application/json;charset=UTF-8")
              (assoc :body (json/write-value-as-string body)))
          response)))))

;; Tabular routes
(def routes #{["/ping" :get (conj common-interceptors `handlers/ping)]
              ["/models" :get (conj common-interceptors `handlers/list-models)]
              ["/models" :post (conj common-interceptors `handlers/load-model!)]
              ["/models/:model-name" :delete (conj common-interceptors 
                                                   `handlers/unload-model!)]
              ["/models/:model-name/invoke" :post (conj common-interceptors 
                                                        `handlers/invoke-model)]})
;; Map-based routes
;(def routes `{"/" {:interceptors [(body-params/body-params) http/html-body]
;                   :get home-page
;                   "/about" {:get about-page}}})

;; Terse/Vector-based routes
;(def routes
;  `[[["/" {:get home-page}
;      ^:interceptors [(body-params/body-params) http/html-body]
;      ["/about" {:get about-page}]]]])


;; Consumed by sagemaker-multimodel-clj.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service
  (-> {:env :prod
       ;; You can bring your own non-default interceptors. Make
       ;; sure you include routing and set it up right for
       ;; dev-mode. If you do, many other keys for configuring
       ;; default interceptors will be ignored.
       ;; ::http/interceptors []
       ::http/routes routes

       ;; Uncomment next line to enable CORS support, add
       ;; string(s) specifying scheme, host and port for
       ;; allowed source(s):
       ;;
       ;; "http://localhost:8080"
       ;;
       ;;::http/allowed-origins ["scheme://host:port"]

       ;; Tune the Secure Headers
       ;; and specifically the Content Security Policy appropriate to your service/application
       ;; For more information, see: https://content-security-policy.com/
       ;;   See also: https://github.com/pedestal/pedestal/issues/499
       ;;::http/secure-headers {:content-security-policy-settings {:object-src "'none'"
       ;;                                                          :script-src "'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:"
       ;;                                                          :frame-ancestors "'none'"}}

       ;; Root for resource interceptor that is available by default.
       ::http/resource-path "/public"

       ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
       ;;  This can also be your own chain provider/server-fn -- http://pedestal.io/reference/architecture-overview#_chain_provider
       ::http/type :immutant
       ::http/host "0.0.0.0"
       ::http/port 8080
       ;; Options to pass to the container (Jetty)
       ::http/container-options {:h2c? true
                                 :h2? false
                                 ;:keystore "test/hp/keystore.jks"
                                 ;:key-password "password"
                                 ;:ssl-port 8443
                                 :ssl? false}}
                                        ;; Alternatively, You can specify you're own Jetty HTTPConfiguration
                                        ;; via the `:io.pedestal.http.jetty/http-configuration` container option.
                                        ;:io.pedestal.http.jetty/http-configuration (org.eclipse.jetty.server.HttpConfiguration.)
    
      http/default-interceptors
      (update ::http/interceptors conj json-body)))                                    
