(jde-project-file-version "1.0")
(jde-set-variables
 '(jde-project-name "SICS TAC AgentWare")
 '(jde-global-classpath '("./"))
 '(jde-sourcepath
   (if (and (symbolp 'java-path-source) (boundp 'java-path-source))
       (list "./" java-path-source)
       '("./")))
 '(jde-make-working-directory "./")
 '(jde-javadoc-author-tag-template
   "* @Author  : Joakim Eriksson, Niclas Finne, Sverker Janson")
 '(jde-gen-class-buffer-template
   '(
     "\"/**\" '>'n"
     "\"* TAC AgentWare\" '>'n"
     "\"* http://www.sics.se/tac/    tac-dev@sics.se\" '>'n"
     "\"*\" '>'n"
     "\"* Copyright (c) 2001, 2002 SICS AB. All rights reserved.\" '>'n"
     "\"* -----------------------------------------------------------------\" '>'n"
     "\"*\" '>'n"
     "\"* \""
     "(file-name-sans-extension (file-name-nondirectory buffer-file-name))"
     "'>'n"
     "\"*\" '>'n"
     "\"* Author  : Joakim Eriksson, Niclas Finne, Sverker Janson\" '>'n"
     "\"* Created : \"" "(current-time-string)" "'>'n"
     "\"* Updated : $Date: 2002/10/24 15:32:07 $\" '>'n"
     "\"*           $Revision: 1.2 $\" '>'n"
     "\"* Purpose :\" '>'n"
     "\"*\" '>'n"
     "\"*/\" '>'n"
     "(jde-gen-get-package-statement)"
     "\"public class \""
     "(file-name-sans-extension (file-name-nondirectory buffer-file-name))"
     "\" \" " "(jde-gen-get-extend-class)" " \"{\" '>'n n"
     "\"public \""
     "(file-name-sans-extension (file-name-nondirectory buffer-file-name))"
     "jde-gen-method-signature-padding-1 \"()\""
     "jde-gen-method-signature-padding-3 \"{\" '>' p n"
     "\"}\" '>'n"
     "n"
     "\"} // \""
     "(file-name-sans-extension (file-name-nondirectory buffer-file-name))"
     "'>'n"
     )
   )
)
