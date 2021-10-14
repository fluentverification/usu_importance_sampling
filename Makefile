
# Need to link to a PRISM distribution
PRISM_DIR = /usr/local/prism-src/prism

# For compilation, just need access to classes/jars in the PRISM distribution
# We look in both the top-level and the prism sub-directory
# (currently svn/git repos and downloaded distributions differ in structure)
PRISM_CLASSPATH = "prism-api/classes:$(PRISM_DIR)/classes:$(PRISM_DIR)/lib/*:$(PRISM_DIR)/prism/classes:$(PRISM_DIR)/prism/lib/*"

# This Makefile just builds all java files in src and puts the class files in classes

JAVA_FILES := $(shell cd src && find . -name '*.java')
CLASS_FILES = $(JAVA_FILES:%.java=classes/%.class)

default: all

all: init api $(CLASS_FILES) test

init:
	@mkdir -p classes
	@if [ -d "prism-api" ]; then \
	cd prism-api; git pull; cd ..; \
	else git clone https://github.com/prismmodelchecker/prism-api ./prism-api; fi

api:
	@make -C prism_api PRISM_DIR=/usr/local/prism-src/prism

classes/%.class: src/%.java
	(javac -classpath $(PRISM_CLASSPATH) -d classes $<)

# Test execution

test:
	PRISM_DIR=$(PRISM_DIR) PRISM_MAINCLASS=imsam.runPath prism-api/bin/run

# Clean up

clean:
	@rm -f $(CLASS_FILES)
	@rm -Rf prism-api
	@rm *~
	@rm src/*~
	@rm models/*~

celan: clean
