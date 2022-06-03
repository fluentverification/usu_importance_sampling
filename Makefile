
# Set location of the appropriate PRISM distribution
PRISM_DIR := /usr/local/prism-src/prism/prism

# For compilation, just need access to classes/jars in the PRISM distribution
# We look in both the top-level and the prism sub-directory
# (currently svn/git repos and downloaded distributions differ in structure)
PRISM_CLASSPATH = "$(PRISM_DIR)/classes:classes"
#"prism-api/classes:$(PRISM_DIR)/classes:$(PRISM_DIR)/lib/*:$(PRISM_DIR)/prism/classes:$(PRISM_DIR)/prism/lib/*"

# This Makefile just builds all java files in src and puts the class files in classes

IS_JAVA_FILES := scaffoldImportanceSampling.java
IS_CLASS_FILES = $(IS_JAVA_FILES:%.java=classes/%.class)

SHELL=/usr/bin/bash


default: usage

LINE1="ARGS=\"-M 2.0 -TMAX 1000 -Nruns 10000                            \\"
LINE2="           -catalogFileName models/abstract/10_states/10_states.is   \\"
LINE3="           -modelFileName models/abstract/10_states/10_states.pm\"  run"
usage: 
	@printf "==========================================\nScaffolding Importance Sampling Simulator\n==========================================\n\n"
	@printf "Dependencies:\n   * cli-args library\n   * prism-api library\n\nTo install dependencies, run\n   make init cli-args\n\n"
	@printf "Usage (All parameters optional, default values are shown):\n\nmake %s\n%s\n%s\n\n" $(LINE1) $(LINE2) $(LINE3)

all: $(IS_CLASS_FILES) test

.PHONY: init api cli-args test run

init:
	@mkdir -p classes
	@if [ -d "prism-api" ]; then \
	cd prism-api; git pull; cd ..; \
	else git clone --branch v4.6 --depth 1 https://github.com/prismmodelchecker/prism-api ./prism-api; fi
	@if [ -d "cli-args" ]; then \
	cd cli-args; git pull; cd ..; \
	else git clone https://github.com/jjenkov/cli-args.git ./cli-args; fi

prism:
	@make -C prism/prism 
api:
	@make -C prism-api PRISM_DIR="$(PRISM_DIR)"

classes/%.class: src/%.java
	(javac -classpath $(PRISM_CLASSPATH) -d classes $<)

cli-args: init
	javac -Xlint:deprecation -d classes cli-args/src/main/java/com/jenkov/cliargs/CliArgs.java

# Test execution

test: $(IS_CLASS_FILES)
	@export PRISM_DIR=$(PRISM_DIR); export PRISM_MAINCLASS=scaffoldImportanceSampling; bash -x prism-api/bin/run

run: $(IS_CLASS_FILES)
	@export PRISM_DIR=$(PRISM_DIR); export PRISM_MAINCLASS=scaffoldImportanceSampling; bash prism-api/bin/run $(ARGS)

experiment: $(IS_CLASS_FILES)
	@for M in $$(seq 1.0 0.1 2.0); do make --no-print-directory ARGS="-M $$M -raw true" run; done

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
