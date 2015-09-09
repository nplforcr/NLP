# define a variable for compiler flags (JFLAGS)
# define a variable for the compiler (JC)  
#

SHELL = /bin/bash

################################################################################
#
#  Model Code Compiling
#
################################################################################


JAVAINCLUDES = bin:src:lib/stanford-parser-2010-11-30.jar:lib/opennlp-tools-1.4.3.jar:lib/stanford-postagger-2008-09-28.jar:lib/jdom.jar:../ACE02Navigator/src

TAGGER = /home/dingcheng/Documents/NLPWorkspace/stanford-postagger-full-2008-09-28/models/bidirectional-wsj-0-18.tagger
INPUT = data
OUTPUT = output

JFLAGS = $(JAVAINCLUDES) # -g
JC = javac

#
# Clear any default targets for building .class files from .java files; we 
# will provide our own target entry to do this in this makefile.
# make has a set of default targets for different suffixes (like .c.o) 
# Currently, clearing the default for .java.class is not necessary since 
# make does not have a definition for this target, but later versions of 
# make may, so it doesn't hurt to make sure that we clear any default 
# definitions for these
#

.SUFFIXES: .java .class

#
# Here is our target entry for creating .class files from .java files 
# This is a target entry that uses the suffix rule syntax:
#	DSTS:
#		rule
# DSTS (Dependency Suffix Target Suffix)
# 'TS' is the suffix of the target file, 'DS' is the suffix of the dependency 
#  file, and 'rule'  is the rule for building a target	
# '$*' is a built-in macro that gets the basename of the current target 
# Remember that there must be a < tab > before the command line ('rule') 
#

.java.class:
	$(JC) -classpath $(JFLAGS) $*.java

#
# CLASSES is a macro consisting of several words (one for each java source file)
#   the backslash "\" at the end of the line is a line continuation character *
# so that the same line can continue over several lines 


CLASSES = \
	src/aceProcessor/ConvertCot2Prob.java 

#
# the default make target entry
# for this example it is the target classes

default: run

# Next line is a target dependency line
# This target entry uses Suffix Replacement within a macro: 
# $(macroname:string1=string2)
# In the words in the macro named 'macroname' replace 'string1' with 'string2'
# Below we are replacing the suffix .java of all words in the macro CLASSES 
# with the .class suffix
#

classes: $(CLASSES:.java=.class)

# this line is to remove all unneeded files from
# the directory when we are finished executing(saves space)
# and "cleans up" the directory of unneeded .class files
# RM is a predefined macro in make (RM = rm -f)
#


################################################################################
#
#  Corpus pre-processing
#
################################################################################


run: classes
	mkdir -p $(OUTPUT)
	java -Xmx1024m -Xms1024m -classpath $(JFLAGS) aceProcessor/ConvertCot2Prob /Users/m048100/Documents/corpora/ace_phase2/data/ace2_train/three2one/9801.327.sgm test/9801.327.txt
################################################################################
#
#  Misc utilities
#
################################################################################
clean:
	$(RM) src/aceProcessor/*.class
	$(RM) bin/aceProcessor/*.class
	$(RM) bin/utils/*.class

	
