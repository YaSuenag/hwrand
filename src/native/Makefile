.PHONY: clean

CC = gcc
AS = as
CFLAGS   = -fPIC -g -O3
CPPFLAGS = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux
ASFLAGS = -g -c --noexecstack
LDFLAGS  = -Wl,-z,noexecstack

TARGET = libhwrandx86.so
OBJS   = hwrand.o random.o

all: $(TARGET)

$(TARGET): $(OBJS)
	$(CC) -shared -o $@ $^ $(LDFLAGS)

.S.o:
	$(AS) $(ASFLAGS) $< -o $@

.c.o:
	$(CC) $(CFLAGS) $(CPPFLAGS) -c $<

clean:
	$(RM) $(TARGET) $(OBJS)

