

(defn c-backend-run-cc!
  ([c-source-path executable-path source-path target]
   (gravity.backend.c/run-compiler! #'c-backend-run-cc! #'c-backend-private-staging-directory! #'c-backend-run-process! #'c-backend-fail! #'c-backend-delete-private-staging! c-source-path executable-path source-path target))
  ([c-source-path executable-path source-path target execute?]
   (gravity.backend.c/run-compiler! #'c-backend-run-cc! #'c-backend-private-staging-directory! #'c-backend-run-process! #'c-backend-fail! #'c-backend-delete-private-staging! c-source-path executable-path source-path target execute?)))
