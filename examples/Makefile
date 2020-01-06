export LANG := en_US.UTF-8
export CLASSPATH := $(CURDIR)/watset.jar

ifeq ($(shell which groovy),)
$(error Please install Groovy and add it to PATH)
endif

WATSET := 2.0.0-rc1

%: %.groovy | watset.jar
	groovy $<

watset.jar:
	curl -sLo "$@" "https://github.com/nlpub/watset-java/releases/download/$(WATSET)/watset.jar"
