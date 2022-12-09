export LANG := en_US.UTF-8
export CLASSPATH := $(CURDIR)/watset.jar

RELEASE := 2.8.2

CFF := 22-273
MCL := 22-282

all: HardClustering FuzzyClustering ABCGraph PickleGraph PickleClustering EvaluateClustering CommandLine

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

mcl: cimfomfa-$(CFF).tar.gz mcl-$(MCL).tar.gz
	rm -rf "mcl-build" "cimfomfa-$(CFF)" "mcl-$(MCL)"
	tar zxf "cimfomfa-$(CFF).tar.gz"
	tar zxf "mcl-$(MCL).tar.gz"
	cd "cimfomfa-$(CFF)" && ./configure --prefix="$(CURDIR)/mcl-build" --disable-shared
	$(MAKE) -j"$(shell nproc)" -C "cimfomfa-$(CFF)"
	$(MAKE) -C "cimfomfa-$(CFF)" install
	cd "mcl-$(MCL)" && ./configure CFLAGS=-I"$(CURDIR)/mcl-build/include" LDFLAGS=-L"$(CURDIR)/mcl-build/lib" --prefix="$(CURDIR)/mcl-build" --enable-rcl
	$(MAKE) -j"$(shell nproc)" -C "mcl-$(MCL)"
	$(MAKE) -C "mcl-$(MCL)" install
	ln -sf "$(CURDIR)/mcl-build/bin/mcl" .

cimfomfa-$(CFF).tar.gz:
	curl -sLo "$@" "https://micans.org/mcl/src/cimfomfa-$(CFF).tar.gz"

mcl-$(MCL).tar.gz:
	curl -sLo "$@" "https://micans.org/mcl/src/mcl-$(MCL).tar.gz"
