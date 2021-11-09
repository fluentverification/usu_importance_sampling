
# Set location of the appropriate PRISM distribution
PRISM_DIR := /usr/local/prism-src/prism/prism

# For compilation, just need access to classes/jars in the PRISM distribution
# We look in both the top-level and the prism sub-directory
# (currently svn/git repos and downloaded distributions differ in structure)
PRISM_CLASSPATH = "$(PRISM_DIR)/classes"
#"prism-api/classes:$(PRISM_DIR)/classes:$(PRISM_DIR)/lib/*:$(PRISM_DIR)/prism/classes:$(PRISM_DIR)/prism/lib/*"

# This Makefile just builds all java files in src and puts the class files in classes

JAVA_FILES := $(shell cd src && find . -name '*.java')
CLASS_FILES = $(JAVA_FILES:%.java=classes/%.class)

default: all

all: $(CLASS_FILES) test

.PHONY: init api test

init:
	@mkdir -p classes
	@if [ -d "prism-api" ]; then \
	cd prism-api; git pull; cd ..; \
	else git clone --branch v4.6 --depth 1 https://github.com/prismmodelchecker/prism-api ./prism-api; fi

prism:
	@make -C prism/prism 
api:
	@make -C prism-api PRISM_DIR="$(PRISM_DIR)"

classes/%.class: src/%.java
	(javac -classpath $(PRISM_CLASSPATH) -d classes $<)

# Test execution

test:
	PRISM_DIR=$(PRISM_DIR) PRISM_MAINCLASS=runPath prism-api/bin/run

# Clean up

clean:
	@rm -f $(CLASS_FILES)
	@rm -Rf prism-api
	@rm -f *~
	@rm -f src/*~
	@rm -f models/*~
	@-make -C prism-api clean
	@-make -C prism/prism clean

celan: clean
