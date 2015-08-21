[bits 32]

dd Buffer.$FILE_END - Buffer.$FILE_START
db "OrcaHLL Class", 0
db "Buffer", 0
Buffer.$FILE_START :

Buffer.$offs.size equ 0

Buffer.Create: 
push eax
push ebx
push edx
push ecx
mov [Buffer.Create.$local.buf], ecx
pop edx
pop ebx
pop eax
ret
	;Vars:
Buffer.Create.$local.buf :
	dd 0x0


Buffer.$FILE_END :
