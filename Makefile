.PHONY: clean

all:
	$(MAKE) -C src/native
	cp -p src/native/libhwrandx86.so dist/
	mvn package
	cp -p target/hwrand.jar dist/

clean:
	$(MAKE) -C src/native $@
	$(RM) dist/libhwrandx86.so dist/hwrand.jar
	mvn clean

