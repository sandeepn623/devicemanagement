FROM ubuntu:latest
LABEL authors="sandeep"

ENTRYPOINT ["top", "-b"]