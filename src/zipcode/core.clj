
(ns zipcode.core
  (:gen-class)
  (:use clojure.core)
  (:use [clojure.java.io :only (resource reader input-stream output-stream file copy)])
  (:import (java.io FileOutputStream))
  (:import (java.net URL))
  (:import (java.util.zip ZipFile ZipEntry)))

(def file-names-url (resource "zipcode/file-names.txt"))
(def base-dir (.getCanonicalFile (file ".")))
(def zip-dir (file base-dir "zip"))
(def csv-dir (file base-dir "csv"))
(def base-url (URL. "http://www.post.japanpost.jp/zipcode/dl/oogaki/zip/"))

(defn get-file-names
  []
  (with-open [rdr (reader (file file-names-url))]
    (doall (line-seq rdr))))

(defn init
  []
  (letfn [(mkdir
            [dir]
            (when-not (.exists dir)
              (.mkdir dir)
              (println (format "mkdir: %s" (.getAbsolutePath dir)))))]
    (mkdir zip-dir)
    (mkdir csv-dir)))

(defn download
  []
  (doseq [file-name (get-file-names)]
    (with-open [is (.openStream (URL. base-url file-name))
                os (output-stream (file zip-dir file-name))]
      (copy is os :buffer-size 1024))))

(defn unzip
  []
  (doseq [f (filter #(.. % getPath (endsWith ".zip")) (file-seq zip-dir))]
    (let [zip (ZipFile. f)]
      (doseq [entry (enumeration-seq (.entries zip))]
        (with-open [is (.getInputStream zip entry)
                    os (output-stream (file csv-dir (.getName entry)))]
          (copy is os :buffer-size 1024))))))

(defn -main
  [& args]
  (init)
  (download)
  (unzip))

