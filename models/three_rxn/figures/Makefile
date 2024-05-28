TIKZ=$(shell find . -name *.tikz)
TIKZTEX=$(pathsubst %.tikz,%.tex,$(TIKZ))
TIKZPDF=$(pathsubst %.tikz,%.pdf,$(TIKZ))

all: $(TIKZPDF)

%.tex: %.tikz
	pandoc "$<" --template ~/tikz.template -s -o "$@"

%.pdf: %.tex
	pdflatex "$<"


