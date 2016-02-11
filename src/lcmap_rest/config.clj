(ns lcmap-rest.config
  (:require [clojure.core.memoize :as memo]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [leiningen.core.project :as lein-prj]))

(def env-prefix "LCMAP_SERVER")

(def -get-config
  (memo/lu
    (fn []
      (log/debug "Memoizing LCMAP configuration ...")
      (lein-prj/read))))

(defn get-config
  ([]
    (-get-config))
  ([arg]
    (if-not (= arg :force-reload)
      ;; The only arg we've defined so far is :force-reload -- anything other
      ;; than that, simply ignore.
      (-get-config)
      (do (memo/memo-clear! -get-config)
          (-get-config)))))

(defn make-env-name
  ([]
    env-prefix)
  ([keys]
    (->> keys
         (map (comp string/upper-case name))
         (string/join "_")
         (#(string/replace % "-" "_"))
         (str env-prefix "_"))))

(defn parse-env-var [value keys]
  (cond
    (nil? value)
      value
    (= value "")
      nil
    (= keys [:env :db :hosts])
      (string/split value #":")
    :else
      value))

(defn get-env [keys]
  (let [value (System/getenv (make-env-name (or keys [])))]
    (parse-env-var value keys)))

(defn- -get-value [config keys]
  (let [env-value (get-env keys)]
    (if (nil? env-value)
      (apply get-in [config keys])
      env-value)))

(defn get-value [& keys]
  (log/debug "Getting configuration value for keys:" keys)
  (let [config (first keys)]
    (if (map? config)
      (-get-value config (nthrest keys 1))
      (-get-value (get-config) keys))))

(defn get-db-hosts []
  (get-value :env :db :hosts))

(defn get-db-port []
  (get-value :env :db :port))

(defn get-db-username []
  (get-value :env :db :credentials :username))

(defn get-db-password []
  (get-value :env :db :credentials :password))

(defn get-http-ip []
  (get-value :env :http :ip))

(defn get-http-port []
  (get-value :env :http :port))

(defn get-log-level []
  (keyword (get-value :env :log-level)))

(defn get-auth-endpoint []
  (get-value :env :auth :usgs :endpoint))

(defn get-auth-login-resource []
  (get-value :env :auth :usgs :login-resource))

(defn get-auth-user-resource []
  (get-value :env :auth :usgs :user-resource))

(defn update-overrides
  ([]
    (update-overrides (get-config)))
  ([cfg]
    (-> cfg
        (assoc-in [:env :db :hosts] (get-db-hosts))
        (assoc-in [:env :db :port] (get-db-port))
        (assoc-in [:env :db :credentials :username] (get-db-username))
        (assoc-in [:env :db :credentials :password] (get-db-password))
        (assoc-in [:env :http :ip] (get-http-ip))
        (assoc-in [:env :http :port] (get-http-port))
        (assoc-in [:env :auth :usgs :endpoint] (get-auth-endpoint))
        (assoc-in [:env :auth :usgs :login-resource] (get-auth-login-resource))
        (assoc-in [:env :auth :usgs :user-resource] (get-auth-user-resource))
        (assoc-in [:env :log-level] (get-log-level)))))

;;; Aliases

(def get-updated-config #'update-overrides)
