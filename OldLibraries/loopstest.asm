[bits 32]

dd LoopsTest.$FILE_END - LoopsTest.$FILE_START
db "OrcaHLL Class", 0
db "LoopsTest", 0
LoopsTest.$FILE_START :

LoopsTest._init: 
pop dword [LoopsTest.returnVal]
push eax
push ebx
push edx
mov ecx, 0
mov [LoopsTest._init.$local.x], ecx
LoopsTest.$loop_while.0_open :
push edx
mov ecx, [LoopsTest._init.$local.x]
mov edx, ecx
mov ecx, 100
cmp edx, ecx
pop edx
jl LoopsTest.$comp_3.true
mov cl, 0x0
jmp LoopsTest.$comp_3.done
LoopsTest.$comp_3.true :
mov cl, 0xFF
LoopsTest.$comp_3.done :

cmp cl, 0xFF
	jne LoopsTest.$loop_while.0_end
push edx	; Math start
mov ecx, 1
mov edx, ecx
mov ecx, [LoopsTest._init.$local.x]
add ecx, edx
pop edx	; Math end
mov [LoopsTest._init.$local.x], ecx
push edx
push edx	; Math start
mov ecx, 2
mov edx, ecx
mov ecx, [LoopsTest._init.$local.x]
sub ecx, edx
pop edx	; Math end
mov edx, ecx
mov ecx, 4
cmp edx, ecx
pop edx
je LoopsTest.$comp_5.true
mov cl, 0x0
jmp LoopsTest.$comp_5.done
LoopsTest.$comp_5.true :
mov cl, 0xFF
LoopsTest.$comp_5.done :

cmp cl, 0xFF
	jne LoopsTest.$loop_if.0_close
	; *** x is 6 here	; INLINE ASSEMBLY
LoopsTest.$loop_if.0_close :

	jmp LoopsTest.$loop_while.0_open
LoopsTest.$loop_while.0_end :
pop edx
pop ebx
pop eax
push dword [LoopsTest.returnVal]
ret
	;Vars:
LoopsTest._init.$local.x :
	dd 0x0


LoopsTest._init2: 
pop dword [LoopsTest.returnVal]
push eax
push ebx
push edx
mov ecx, 0
mov [LoopsTest.$loop_for.0.$local.y], ecx
LoopsTest.$loop_for.0_open :
push edx
push edx	; Math start
mov ecx, 2
mov edx, ecx
mov ecx, [LoopsTest.$loop_for.0.$local.y]
sub ecx, edx
pop edx	; Math end
mov edx, ecx
mov ecx, 4
cmp edx, ecx
pop edx
je LoopsTest.$comp_10.true
mov cl, 0x0
jmp LoopsTest.$comp_10.done
LoopsTest.$comp_10.true :
mov cl, 0xFF
LoopsTest.$comp_10.done :

cmp cl, 0xFF
	jne LoopsTest.$loop_if.1_close
	; *** y is 6 here	; INLINE ASSEMBLY
LoopsTest.$loop_if.1_close :

push edx	; Math start
mov ecx, 1
mov edx, ecx
mov ecx, [LoopsTest.$loop_for.0.$local.y]
add ecx, edx
pop edx	; Math end
mov [LoopsTest.$loop_for.0.$local.y], ecx
push edx
mov ecx, [LoopsTest.$loop_for.0.$local.y]
mov edx, ecx
mov ecx, 100
cmp edx, ecx
pop edx
jl LoopsTest.$comp_12.true
mov cl, 0x0
jmp LoopsTest.$comp_12.done
LoopsTest.$comp_12.true :
mov cl, 0xFF
LoopsTest.$comp_12.done :

cmp cl, 0xFF
	je LoopsTest.$loop_for.0_open

pop edx
pop ebx
pop eax
push dword [LoopsTest.returnVal]
ret
	;Vars:
LoopsTest.$loop_for.0.$local.y :
	dd 0x0


LoopsTest.returnVal:
	dd 0x0
LoopsTest.$FILE_END :
