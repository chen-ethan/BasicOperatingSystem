JFLAGS = -g
JC = javac
JVM= java -jar
.SUFFIXES: .java .class
	.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	      src/app/driverClass2.java 

make: classes run

default: make

classes: 
	$(JC) $(JFLAGS) src/app/driverClass2.java

run:
	$(JVM) EthanOSRun.jar

clean:
	$(RM)*.class
