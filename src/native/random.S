.text

# void fill_with_rdrand(unsigned char data[], int len);
.global fill_with_rdrand
.type   fill_with_rdrand, @function
fill_with_rdrand:
.cfi_startproc
  push %rbp
  movq %rsp, %rbp

  .align 16
  1:
    cmpl $8, %esi
    jl 3f
  2:
    rdrand %rax
    jae 2b
    movq %rax, (%rdi)
    addq $8, %rdi
    subl $8, %esi
    jne 1b

  je 5f

  3:
    rdrand %rax
    jae 3b

  .align 16
  4:
    movb %ah, (%rdi)
    addq $1, %rdi
    salq $1, %rax
    subl $1, %esi
    jne 4b
  5:

  leave
  ret
.cfi_endproc
.size fill_with_rdrand, .-fill_with_rdrand


# void fill_with_rdseed(unsigned char data[], int len);
.global fill_with_rdseed
.type   fill_with_rdseed, @function
fill_with_rdseed:
.cfi_startproc
  push %rbp
  movq %rsp, %rbp

  .align 16
  1:
    cmpl $8, %esi
    jl 3f
  2:
    rdseed %rax
    jae 2b
    movq %rax, (%rdi)
    addq $8, %rdi
    subl $8, %esi
    jne 1b

  je 5f

  3:
    rdseed %rax
    jae 3b

  .align 16
  4:
    movb %ah, (%rdi)
    addq $1, %rdi
    salq $1, %rax
    subl $1, %esi
    jne 4b
  5:

  leave
  ret
.cfi_endproc
.size fill_with_rdseed, .-fill_with_rdseed
