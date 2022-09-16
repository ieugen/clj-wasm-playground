(ns graal-wasm.c2wasm
  (:require [clojure.java.io :as io])
  (:import (org.graalvm.polyglot Context Source Value)
           (org.graalvm.polyglot.io ByteSequence)))

(defn file->bytes 
  "Load file into byte array"
  [file]
  (with-open [xin (io/input-stream file)
              xout (java.io.ByteArrayOutputStream.)]
    (io/copy xin xout)
    (.toByteArray xout)))

(defn wasm->context
  "Load a wasm file and return the ^:org.graalvm.polyglot.Context."
  [file]
  (let [binary (file->bytes (.getAbsolutePath (io/file file)))
        context-builder (Context/newBuilder (into-array String ["wasm"]))
        source-builder (Source/newBuilder "wasm" (ByteSequence/create binary) "c2wasm")
        source (.build source-builder)
        context (.build context-builder)
        _ (.eval context source)]
    context))

(defn context->fn
  "Find a function from the WASM context and return it."
  ^:Value [ctx fn-name]
  (-> ctx
      (.getBindings "wasm")
      (.getMember "main")
      (.getMember fn-name)))

(defn hello
  [& args]
  (let [ctx (wasm->context "c2wasm.wasm")
        wasm-fn (context->fn ctx "hello")]
    (.execute wasm-fn (object-array []))))

(defn add
  [opts]
  (println "Options are" opts)
  (let [{:keys [a b]} opts
        ctx (wasm->context "c2wasm.wasm")
        wasm-fn (context->fn ctx "add")
        ;; call wasm function with 2 argumens
        val (.execute wasm-fn (object-array [(int a) (int b)]))
        result (.asInt val)]
    ;; add function returns an int
    (println "Result is" result)
    result))

(comment
  
  (hello)
  
  (def x (add {:a 4 :b 6}))
  (.asInt x)

  (let [binary (file->bytes (.getAbsolutePath (io/file "c2wasm.wasm")))
        context-builder (Context/newBuilder (into-array String ["wasm"]))
        source-builder (Source/newBuilder "wasm" (ByteSequence/create binary) "c2wasm")
        source (.build source-builder)
        context (.build context-builder)
        _ (.eval context source)
        main-fn ^:Value (-> context
                            (.getBindings "wasm")
                            (.getMember "main")
                            (.getMember "_start"))]
    (def ctx context)
    (def mf main-fn)
    (.execute main-fn (object-array [])))

  (.execute mf (object-array []))

  (-> ctx
      (.getBindings "wasm")
      (.getMember "main")
      (.getMember "add")
      (.execute (object-array [(int 15) (int 32)]))))
