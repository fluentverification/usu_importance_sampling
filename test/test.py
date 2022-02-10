import os
import sys

def setup():
    os.system('javac ../src/PrismModelGenerator.java')
    os.system('javac ../src/scaffoldImportanceSampling.java')

def createPrismFile(numStates = 10):
    '''
Creates a prism file of a number of states (input) and returns the path of the file made
    '''
    filename = f"prismFileTmp_{numStates}_states.sm"
    # TODO: make the prism model generator take the number of states as a command line argument
    os.system(f"java ../src/PrismModelGenerator {numStates} > {filename} ")
    return filename
