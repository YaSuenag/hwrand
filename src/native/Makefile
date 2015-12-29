.PHONY: clean

CC = gcc
CFLAGS   = -fPIC -g -O3
CPPFLAGS = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux
LDFLAGS  = -Wl,-z,noexecstack

all: libhwrandx86.so

libhwrandx86.so: hwrand.o random.o
	$(CC) -shared -o $@ $^ $(LDFLAGS)

.S.o:
	$(CC) $(CFLAGS) -c $<

.c.o:
	$(CC) $(CFLAGS) $(CPPFLAGS) -c $<

clean:
	$(RM) libhwrandx86.so *.o

