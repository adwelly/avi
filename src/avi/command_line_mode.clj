(ns avi.command-line-mode
  (:require [avi.eventmap :as em]
            [avi.editor :as e]))

(defn- append-to-command-line
  [editor s]
  (assoc editor :command-line (str (:command-line editor) s)))

(defn- line-number?
  [command]
  (every? #(Character/isDigit %) command))

(defn- process-command
  [editor]
  (let [command-line (:command-line editor)
        editor (assoc editor :mode :normal)]
    (cond
      (= "q" command-line)
      (assoc editor :mode :finished)

      (= "" command-line)
      editor

      (line-number? command-line)
      (e/change-line editor (constantly (dec (Long/parseLong command-line))))

      :else
      editor)))

(def eventmap
  (em/eventmap
    ("<Enter>"
      [editor]
      (process-command editor))

    ("<BS>"
      [editor]
      (let [command-line (:command-line editor)]
        (if (zero? (count command-line))
          (assoc editor :mode :normal)
          (assoc editor :command-line (.substring command-line 0 (dec (count command-line)))))))
    
    (:else
      [editor event]
      (let [[event-type event-data] event]
        (if-not (= event-type :keystroke)
          (e/beep editor)
          (append-to-command-line editor event-data))))))

(defn enter
  [editor]
  (assoc editor :mode :command-line, :command-line ""))

(defmethod e/respond :command-line
  [editor event]
  (em/invoke-event-handler eventmap editor event))
