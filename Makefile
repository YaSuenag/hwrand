.PHONY: clean

all:
	$(MAKE) -C src/native
	cp -p src/native/libhwrandx86.so dist/
	ant

clean:
	$(MAKE) -C src/native $@
	$(RM) dist/libhwrandx86.so
	ant clean

