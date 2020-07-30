export LANG := en_US.UTF-8
export CLASSPATH := $(CURDIR)/watset.jar

# https://issues.apache.org/jira/browse/GROOVY-8339 is really annoying
export JAVA_OPTS := --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.security=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.annotation=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED

ifeq ($(shell which groovy),)
$(error Please install Groovy and add it to PATH)
endif

RELEASE := 2.0.0

MCL := 14-137

all: HardClustering FuzzyClustering PickleGraph PickleClustering CommandLine

clean:
	-rm -fv *.pkl *.jar *.tar.gz
	-rm -rf mcl "mcl-$(MCL)" "mcl-$(MCL).tar.gz"

%: %.groovy | watset.jar
	groovy $<

%: %.py
	python3 $<

%: %.sh | watset.jar
	$(SHELL) $<

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
