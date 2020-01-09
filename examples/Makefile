export LANG := en_US.UTF-8
export CLASSPATH := $(CURDIR)/watset.jar

# https://issues.apache.org/jira/browse/GROOVY-8339 is really annoying
export JAVA_OPTS := --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.security=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.annotation=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED

ifeq ($(shell which groovy),)
$(error Please install Groovy and add it to PATH)
endif

RELEASE := 2.0.0-rc2

all: HardClustering FuzzyClustering PickleGraph PickleClustering

clean:
	rm -fv *.pkl *.jar

%: %.groovy | watset.jar
	groovy $<

%: %.py
	python3 $<

watset.jar:
	curl -sLo "$@" "https://github.com/nlpub/watset-java/releases/download/$(RELEASE)/watset.jar"
