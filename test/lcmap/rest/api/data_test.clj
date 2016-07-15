(ns lcmap.rest.api.data-test
  (:require [clojure.test :refer :all]
            [lcmap.client.data :as client-data]
            [lcmap.rest.api.data :as api-data]
            [lcmap.rest.shared-test :as shared]))

(deftest ^:integration get-happy-path
  (shared/with-system [sys shared/cfg-opts]
    (testing "getting tiles"
      (let [{result :result} (client-data/get-tiles
                              :band  "LANDSAT_5/TM/sr_band1"
                              :point [-2062080,2952960]
                              :time  "2002-05-01/2002-06-01")]
        (is (some? (:spec result)))
        (is (some? (:tiles result)))
        (is (= 4 (count (:tiles result))))))
    #_(testing "getting specs"
      (let [{result :result} (client-data/get-specs :band "LANDSAT_5/TM/sr_band1")
            spec (first result)]
        (is (some? spec))
        (is (:ubid spec))
        (is (:tile_x spec))
        (is (:tile_y spec))
        (is (:pixel_x spec))
        (is (:pixel_y spec))
        (is (:shift_x spec))
        (is (:shift_y spec)))))
    #_(testing "getting scenes"
      (shared/with-system [sys shared/cfg-opts]
        (let [{result :result} (client-data/get-scene :scene "LT50470282002001LGS01")
              scene (first result)]
          (is (some? result))
          (is (:source scene))
          (is (:acquired scene))
          (is (:satellite scene))
          (is (:instrument scene))
          (is (:provider scene))
          scene))))


#_(deftest ^:integration get-invalid-params-case
  (shared/with-system [sys shared/cfg-opts]
    (testing "getting tiles using an invalid UBID"
      (let [invalid-params {:band  "not-a-band"
                            :point [-2062080,2952960]
                            :time  "2002-05-01/2002-06-01"}
            {errors :errors} (client-data/get-tiles invalid-params)]
        (is (some? errors))))
    (testing "getting specs"
      (let [invalid-params {:band "not-a-band"}
            {errors :errors} (client-data/get-specs invalid-params)]
        (is (some? errors))))
    (testing "getting scenes"
      (let [invalid-params {:scene "not-a-scene"}
            {errors :errors} (client-data/get-scene invalid-params)]
        (is (some? errors))))))
