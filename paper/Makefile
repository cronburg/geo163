
all: paper.pdf

ARGS=-shell-escape

paper.pdf: $(wildcard *.tex) $(wildcard figures/*.tex) Makefile
	latex $(ARGS) paper.tex
	latex $(ARGS) paper.tex
	dvips -o paper.ps paper.dvi
	ps2pdf paper.ps
ifeq ($(USER),karl)
	scp paper.pdf linux:~/p/geo/dynamic-visibility.pdf
	ssh linux exec chmod 644 /h/karl/p/geo/dynamic-visibility.pdf
endif

#	pdflatex $(ARGS) paper.tex
#	./fix-figures

clean:
	rm -f paper.pdf paper.ps paper.dvi *.log *.aux *.out

