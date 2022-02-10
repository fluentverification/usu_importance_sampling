import os
import sys

def setup():
    os.system('javac ../src/PrismModelGenerator.java')
    os.system('javac ../src/scaffoldImportanceSampling.java')

def createPrismFile(numStates = 10):
    # TODO: make the prism model generator take the number of states as a command line argument
    os.system(f"java ../src/PrismModelGenerator ")
