FROM openjdk:11-jre

ARG RELEASE

MAINTAINER Dmitry Ustalov <dmitry.ustalov@gmail.com>

WORKDIR /usr/src/watset

RUN curl -fsLo 'watset.jar' "https://github.com/nlpub/watset-java/releases/download/$RELEASE/watset.jar"
