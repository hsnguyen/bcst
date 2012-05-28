#
# ISL WWW Makefile
#
# Required
#   m4 - Gnu version 1.4 recommended
#   rm

M4=m4
M4ARGS=--prefix-builtins

DIRS :=

TARGET := ../docs

OBJDIRS := $(TARGET) $(TARGET)/images $(patsubst %,$(TARGET)/%,$(DIRS))

SOURCES := $(wildcard *.html.m4) $(wildcard $(patsubst %,%/*.html.m4,$(DIRS)))

WEBOBJECTS := $(patsubst %.html.m4,$(TARGET)/%.html,$(SOURCES))

IMAGES := $(foreach f,$(wildcard images/*.gif images/*.jpg images/*.JPG),$(TARGET)/$(f))

all: $(OBJDIRS) webhtml images

webhtml:	$(WEBOBJECTS)

$(WEBOBJECTS) : $(TARGET)/%.html : %.html.m4 style.m4 webstyle.m4 # Makefile
	cat webstyle.m4 $< | $(M4) $(M4ARGS) > $@

images:	$(OBJDIRS) $(IMAGES)

$(TARGET)/%.gif : %.gif
	cp $< $@

$(TARGET)/%.jpg : %.jpg
	cp $< $@

$(OBJDIRS):
	mkdir -p $@


## Installation

DESTINATION := /home/www/tac/docs/agentware/java

DESTDIRS := $(DESTINATION) $(DESTINATION)/images


install:	$(DESTDIRS) all
	cp -r $(TARGET)/* $(DESTINATION)/
	-@chgrp -R tac $(DESTINATION)/*
	-@chmod -R g+w $(DESTINATION)/*

$(DESTDIRS):
	mkdir -p $@

## Cleanup

clean:
	rm -rf $(WEBOBJECTS) $(wildcard *~)
