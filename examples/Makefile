export LANG := en_US.UTF-8
export CLASSPATH := $(CURDIR)/watset.jar

RELEASE := 2.5.2

MCL := 14-137

all: HardClustering FuzzyClustering PickleGraph PickleClustering EvaluateClustering CommandLine

clean:
	-rm -fv *.pkl *.jar *.tar.gz
	-rm -rf mcl "mcl-$(MCL)" "mcl-$(MCL).tar.gz"

%: %.java | watset.jar
	java "$<"

%: %.py
	python3 "$<"

%: %.sh | watset.jar
	$(SHELL) "$<"

watset.jar:
	curl -sLo "$@" "https://github.com/nlpub/watset-java/releases/download/$(RELEASE)/watset.jar"

mcl: mcl-$(MCL).tar.gz
	rm -rf "mcl-$(MCL)"
	tar zxf "mcl-$(MCL).tar.gz"
	cd "mcl-$(MCL)" && ./configure --prefix="$(CURDIR)/mcl-$(MCL)"
	$(MAKE) -j"$(shell nproc)" -C "mcl-$(MCL)" all
	$(MAKE) -C mcl-$(MCL) install
	ln -sf "$(CURDIR)/mcl-$(MCL)/bin/mcl" .

mcl-$(MCL).tar.gz:
	curl -sLo "$@" "https://micans.org/mcl/src/mcl-$(MCL).tar.gz"
