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
mov ecx, [Buffer.$local.size]
mov [Buffer.$offs.size], ecx
mov ecx, [Buffer.Create.$local.buf]
pop edx
pop ebx
pop eax
ret
	;Vars:
Buffer.Create.$local.buf :
	dd 0x0


Buffer.GetObjectSize: 
push eax
push ebx
push edx
pop edx
pop ebx
pop eax
ret
	;Vars:


Buffer.Set: 
push eax
push ebx
push edx
mov byte [ebx+Buffer.Set.$local.pos+$LinkedClassSize], [Buffer.Set.$local.val]	; INLINE ASSEMBLY
pop edx
pop ebx
pop eax
ret
	;Vars:


Buffer.Get: 
push eax
push ebx
push edx
mov [Buffer.Get.$local.ret], cl
xor ecx, ecx	; INLINE ASSEMBLY
mov byte [Buffer.Get.$local.ret], [ebx+Buffer.Get.$local.pos+$LinkedClassSize]	; INLINE ASSEMBLY
mov cl, [Buffer.Get.$local.ret]
pop edx
pop ebx
pop eax
ret
	;Vars:
Buffer.Get.$local.ret :
	db 0x0


Buffer.SetInt: 
push eax
push ebx
push edx
mov dword [ebx+Buffer.Set.$local.pos+$LinkedClassSize], [Buffer.Set.$local.val]	; INLINE ASSEMBLY
pop edx
pop ebx
pop eax
ret
	;Vars:


Buffer.GetInt: 
push eax
push ebx
push edx
mov [Buffer.GetInt.$local.ret], ecx
mov dword [Buffer.GetInt.$local.ret], [ebx+Buffer.Get.$local.pos+$LinkedClassSize]	; INLINE ASSEMBLY
mov ecx, [Buffer.GetInt.$local.ret]
pop edx
pop ebx
pop eax
ret
	;Vars:
Buffer.GetInt.$local.ret :
	dd 0x0


Buffer.$FILE_END :
