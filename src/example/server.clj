(ns example.server
  (:require [reitit.ring :as ring]
            [reitit.ring.spec]
            [reitit.coercion.malli]
            [reitit.openapi :as openapi]
            [reitit.ring.malli]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.ring.coercion :as coercion]
            [reitit.dev.pretty :as pretty]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.parameters :as parameters]
            [ring.middleware.cors :refer [wrap-cors]]
            [muuntaja.core :as m]
            [example.routes :as routes]))

(def app
  (ring/ring-handler
    (ring/router
      routes/routes
      {:validate reitit.ring.spec/validate
       :exception pretty/exception
       :data {:coercion reitit.coercion.malli/coercion
              :muuntaja m/instance
              :middleware [openapi/openapi-feature
                           ;; query-params & form-params
                           parameters/parameters-middleware
                           ;; content-negotiation
                           muuntaja/format-negotiate-middleware
                           ;; encoding response body
                           muuntaja/format-response-middleware
                           ;; exception handling
                           exception/exception-middleware
                           ;; decoding request body
                           muuntaja/format-request-middleware
                           ;; coercing response bodys
                           coercion/coerce-response-middleware
                           ;; coercing request parameters
                           coercion/coerce-request-middleware
                           ;; multipart
                           multipart/multipart-middleware
                           ;; wrap cors
                           [wrap-cors :access-control-allow-origin [#".*"]
                                      :access-control-allow-methods [:get :put :post :patch :delete]]]}})
    ;; (ring/create-default-handler) ;; default handler (if you have cors problem, remove this comment)
    (ring/routes
      (swagger-ui/create-swagger-ui-handler
        {:path "/"
         :config {:validatorUrl nil
                  :urls [{:name "openapi", :url "openapi.json"}]
                  :urls.primaryName "openapi"
                  :operationsSorter "alpha"}}))))

