(ns ^{:doc
  "LCMAP REST server configuration

  The LCMAP REST server will extract configuration values from the following,
  in order of highest precedence to lowest:

  * System evnironment variables
  * The 'LCMAP Server' section of the config/INI file in ~/.usgs/lcmap.ini
    (same file as used by the LCMAP client libraries)
  * The values nested under the project.clj file's :env key"}
  lcmap.rest.config
  (:require [clojure.core.memoize :as memo]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clojure-ini.core :as ini]
            [leiningen.core.project :as lein-prj]
            [lcmap.client.config])
  (:refer-clojure :exclude [read]))

(def env-prefix "LCMAP_SERVER")

(defn file-exists?
  [filename]
  (.exists (io/as-file filename)))

(defn read-ini
  [filename serializer-fn]
  (let [ini-file (io/file filename)]
    (if (file-exists? ini-file)
      (do
        (log/debug "Memoizing LCMAP config ini ...")
        (serializer-fn
          (ini/read-ini ini-file :keywordize? true)))
      (do
        (log/warn "No configuration file found")
        {}))))

(defn read-edn
  [filename serializer-fn]
  )

(def -read
  (memo/lu
    (fn [filename serializer-fn type]
      (case type
        :edn (read-edn filename serializer-fn)
        :ini (read-ini filename serializer-fn)))))

(defn read
  "Read a config file (either :ini or :edn). After the file is read from disk
  it is put into a least-used cache and only re-read from disk if the option
  ``:force-reload true`` is passed."
  [filename & {:keys [force-reload type serializer]
               :or {force-reload false type :edn serializer #'identity}}]
  (if (not= force-reload true)
    (-read filename serializer type)
    (do (memo/memo-clear! -read)
        (-read filename serializer type))))

(def -get-config
  (memo/lu
    (fn []
      (log/debug "Memoizing LCMAP project.clj configuration ...")
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

(defn make-cfgini-name
  ([]
    env-prefix)
  ([keys]
    (->> keys
         (#(nthrest % 1)) ; skip the :env key -- not used in .ini
         (map (comp string/lower-case name))
         (string/join "-")
         (#(string/replace % "_" "-"))
         (keyword))))

(defn parse-env-var [value keys]
  (cond
    (nil? value)
      value
    (= value "")
      nil
    (= keys [:env :db :hosts])
      (map string/trim (string/split value #":"))
    :else
      value))

(defn parse-cfgini-var [value keys]
  (cond
    (nil? value)
      value
    (= value "")
      nil
    (= keys [:env :db :hosts])
      (map string/trim (string/split value #","))
    :else
      value))

(defn get-env [keys]
  (log/debug "\tChecking env for keys ...")
  (let [env-key (make-env-name (or keys []))
        value (System/getenv env-key)]
    (parse-env-var value keys)))

(defn get-cfgini [keys]
  (log/debug "\tChecking config/INI for keys ...")
  (let [cfgini-key (make-cfgini-name (or keys []))
        cfgini-data (lcmap.client.config/get-section "LCMAP Server")
        value (cfgini-data cfgini-key)]
    (parse-cfgini-var value keys)))

(defn get-proj [config keys]
  (log/debug "\tChecking project.clj for keys ...")
  (apply get-in [config keys]))

(defn- -get-value [config keys]
  ;; First check for environment values
  (let [env-value (get-env keys)]
    (if (nil? env-value)
      ;; Failing that, check for config/INI values
      (let [cfgini-value (get-cfgini keys)]
        (if (nil? cfgini-value)
          ;; Failing that, use the project.clj default values
          (get-proj config keys)
          cfgini-value))
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
