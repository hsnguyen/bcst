###############################################################
# Settings
###############################################################

JAVADOC=javadoc
CC=javac
JAR=jar
RM=rm -f
CP=cp
ZIP=zip
ZIPOPTIONS=-9 -o

###############################################################
# System dependent
###############################################################

ifndef WINDIR
  ifdef OS
    ifneq (,$(findstring Windows,$(OS)))
      WINDIR := Windows
    endif
  endif
endif

ifndef WINDIR
  # This settings are for UNIX
  SEPARATOR=/
  PATHSEPARATOR=:
  # Add "'" around filenames when removing them because UNIX expands "$"
  APO='#'  (last apostrophe to avoid incorrect font-lock)
else
  # These setting are for MS-DOS/Windows 95/Windows NT
  SEPARATOR=\\
  PATHSEPARATOR=;
  APO=
endif

###############################################################
# Arguments
###############################################################

CCARGS=-deprecation

###############################################################
# MAKE
###############################################################

AWVERSION := beta-7

AWITEMS := se/sics/tac/aw/ $(addprefix se/sics/tac/util/,LogFormatter ArgEnumerator) com/botbox/util/ArrayQueue

AWSOURCES := $(wildcard $(addsuffix *.java, $(AWITEMS)))

AWJAROBJECTS := $(addsuffix *.class, $(AWITEMS))

AWJAR := tacagent.jar

PACKAGES := se/sics/tac/sicsagent se/sics/tac/solver

SOURCES := $(wildcard $(addsuffix /*.java,. $(PACKAGES)))

OBJECTS := $(patsubst %.java,%.class,$(AWSOURCES) $(SOURCES))

AWBINARY := README.txt RELEASE_NOTES.txt AWManifest.txt agent.conf Makefile

.PHONY: compile jar

all:	jar

compile: $(OBJECTS)

ajr:	jar

jra:	jar

jar:	$(AWJAR)


###############################################################
# ZIP
###############################################################

ZIPFILE := tacaw-java-$(AWVERSION).zip
zip:	$(ZIPFILE)

$(ZIPFILE):	$(AWSOURCES) $(AWBINARY)
	$(ZIP) $(ZIPOPTIONS) $@ $(AWSOURCES) $(AWBINARY)


###############################################################
# INSTALLATION
###############################################################

DESTINATION := /home/www/tac
INSTALL_TARGET := $(DESTINATION)/downloads/$(ZIPFILE)

install:	$(INSTALL_TARGET)

$(INSTALL_TARGET):	$(ZIPFILE)
	cp $< $@
	-@chgrp -R tac $@
	-@chmod -R g+w $@


###############################################################
# CLASS COMPILATION
###############################################################

%.class : %.java
	$(CC) $(CCARGS) $<

$(AWJAR) : $(OBJECTS) AWManifest.txt
	$(JAR) cfm $@ AWManifest.txt $(AWJAROBJECTS)


###############################################################
# CLEAN  (untrusted, use with great care!!!)
###############################################################

.PHONY:	clean claen clena

claen:	clean

clena:	clean

clean:
ifdef %WINDIR
	-$(RM) $(wildcard $(AWJAROBJECTS) $(foreach dir,$(PACKAGES),$(dir)/*.class))
else
	-$(RM) $(foreach f,$(AWJAROBJECTS),$(APO)$(f)$(APO)) $(foreach dir,$(PACKAGES),$(dir)/*.class)
endif
