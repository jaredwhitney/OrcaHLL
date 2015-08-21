[bits 32]

dd Pointer.$FILE_END - Pointer.$FILE_START
db "OrcaHLL Class", 0
db "Pointer", 0
Pointer.$FILE_START :

Pointer.Get: 
push eax
push ebx
push edx
mov [Pointer.Get.$local.ret], cl
mov edx, ebx	; INLINE ASSEMBLY
add edx, [Pointer.Get.$local.pos]	; INLINE ASSEMBLY
mov byte [Pointer.Get.$local.ret], [edx]	; INLINE ASSEMBLY
mov cl, [Pointer.Get.$local.ret]
pop edx
pop ebx
pop eax
ret
	;Vars:
Pointer.Get.$local.ret :
	db 0x0


Pointer.GetInt: 
push eax
push ebx
push edx
mov [Pointer.GetInt.$local.ret], ecx
mov edx, ebx	; INLINE ASSEMBLY
add edx, [Pointer.GetInt.$local.pos]	; INLINE ASSEMBLY
mov dword [Pointer.GetInt.$local.ret], [edx]	; INLINE ASSEMBLY
mov ecx, [Pointer.GetInt.$local.ret]
pop edx
pop ebx
pop eax
ret
	;Vars:
Pointer.GetInt.$local.ret :
	dd 0x0


Pointer.$FILE_END :
