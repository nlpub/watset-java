export LANG := en_US.UTF-8
export CLASSPATH := $(CURDIR)/watset.jar

RELEASE := 2.2.1

all: HardClustering FuzzyClustering PickleGraph PickleClustering CommandLine

clean:
	rm -fv *.pkl *.jar

%: %.java | watset.jar
	java "$<"

%: %.py
	python3 "$<"

%: %.sh | watset.jar
	$(SHELL) "$<"

watset.jar:
	curl -sLo "$@" "https://github.com/nlpub/watset-java/releases/download/$(RELEASE)/watset.jar"
