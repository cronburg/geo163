
all: paper.pdf

ARGS=-shell-escape

paper.pdf: $(wildcard *.tex) $(wildcard figures/*.tex)
	latex $(ARGS) paper.tex
	dvips -o paper.ps paper.dvi
	ps2pdf paper.ps

#	pdflatex $(ARGS) paper.tex
#	./fix-figures

clean:
	rm -f paper.pdf paper.ps paper.dvi *.log *.aux *.out

