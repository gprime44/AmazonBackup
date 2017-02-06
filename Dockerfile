FROM openjdk:7

VOLUME /tmp
VOLUME /data
VOLUME /config

RUN apt-get update && apt-get install -y \
python3 \
python3-appdirs \
python3-dateutil \
python3-requests \
python3-sqlalchemy \
python3-pip \
git \
encfs \
unionfs-fuse \
maven

RUN pip3 install --upgrade git+https://github.com/yadayada/acd_cli.git

WORKDIR /code

ADD pom.xml /code/pom.xml
RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]

ADD src /code/src
RUN ["mvn", "package"]

RUN ["mv", "/code/target/app.jar", /app.jar]
RUN ["rm", "-Rf", "/code", "/.m2"]

CMD ["java", "-jar", "/app.jar"]