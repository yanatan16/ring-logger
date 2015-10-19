(ns ring.logger.messages-test
  (:require [ring.logger.messages :as msg]
            [ring.logger.protocols :refer [Logger]]
            [clojure.edn :as edn]
            [clojure.test :refer [is deftest]]))

(deftest redacted-headers-and-params-test
  (let [options {:printer :no-color
                 :logger (reify Logger (log [_ _ _ message] message))
                 :redact-fn (msg/redact-some #{:authorization :password} (constantly "[I WAS REDACTED]"))}
        request {:request-method "GET"
                 :uri "http://example.com/some/endpoint"
                 :remote-addr "172.243.12.12"
                 :query-string "some=query&string"
                 :headers {:authorization "Basic my-fancy-encoded-secret"}
                 :params {:password "1234"
                          :user "nick"
                          :foo :bar}}
        starting-msg (msg/starting options request)
        logged-params (msg/request-params options request)]
    (is (.endsWith starting-msg
                   (pr-str {:authorization "[I WAS REDACTED]"})))
    (is (= {:password "[I WAS REDACTED]"
            :user "nick"
            :foo :bar} 
           (edn/read-string (subs logged-params (count msg/request-params-default-prefix)))))))
