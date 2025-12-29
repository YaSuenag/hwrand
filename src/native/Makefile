.PHONY: clean all

CC = gcc
AS = as
CFLAGS   = -fPIC -g -O3
CPPFLAGS = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux
ASFLAGS  = -g -c --noexecstack
LDFLAGS  = -Wl,-z,noexecstack

OUTDIR   ?= .
BUILDDIR ?= .
TARGET    = $(OUTDIR)/libhwrandx86.so
OBJS      = $(BUILDDIR)/hwrand.o $(BUILDDIR)/random.o

all: $(TARGET)

$(TARGET): $(OBJS) | $(OUTDIR)
	$(CC) -shared -o $@ $(OBJS) $(LDFLAGS)

$(BUILDDIR)/%.o: %.S | $(BUILDDIR)
	$(AS) $(ASFLAGS) $< -o $@

$(BUILDDIR)/%.o: %.c | $(BUILDDIR)
	$(CC) $(CFLAGS) $(CPPFLAGS) -c $< -o $@

$(OUTDIR) $(BUILDDIR):
	mkdir -p $@

clean:
	$(RM) $(TARGET) $(OBJS)
