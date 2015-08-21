[bits 32]

dd Buffer.$FILE_END - Buffer.$FILE_START
db "OrcaHLL Class", 0
db "Buffer", 0
Buffer.$FILE_START :

Buffer.$offs.size equ 0

Buffer.Create: 
pop dword [Buffer.returnVal]
pop dword [Buffer.$local.size]
push eax
push ebx
push edx
push edx	; Math start
mov ecx, [Buffer.$local.size]
mov edx, ecx
mov ecx, 4
add ecx, edx
pop edx	; Math end
push ecx
call System.Memory.Allocate
mov [Buffer.Create.$local.buf], ecx
mov ecx, [Buffer.$local.size]
mov [Buffer.$offs.size], ecx
mov ecx, [Buffer.Create.$local.buf]
pop edx
pop ebx
pop eax
push dword [Buffer.returnVal]
ret
	;Vars:
Buffer.Create.$local.buf :
	dd 0x0


Buffer.GetObjectSize: 
pop dword [Buffer.returnVal]
push eax
push ebx
push edx
push edx	; Math start
mov ecx, 4
mov edx, ecx
mov ecx, [Buffer.$local.size]
add ecx, edx
pop edx	; Math end
pop edx
pop ebx
pop eax
push dword [Buffer.returnVal]
ret
	;Vars:


Buffer.Set: 
pop dword [Buffer.returnVal]
pop dword [Buffer.$local.pos]
pop dword [Buffer.$local.val]
push eax
push ebx
push edx
mov byte [ebx+Buffer.Set.$local.pos+$LinkedClassSize], [Buffer.Set.$local.val]	; INLINE ASSEMBLY
pop edx
pop ebx
pop eax
push dword [Buffer.returnVal]
ret
	;Vars:


Buffer.Get: 
pop dword [Buffer.returnVal]
pop dword [Buffer.$local.pos]
push eax
push ebx
push edx
mov ecx, 255
mov [Buffer.Get.$local.ret], cl
xor ecx, ecx	; INLINE ASSEMBLY
mov byte [Buffer.Get.$local.ret], [ebx+Buffer.Get.$local.pos+$LinkedClassSize]	; INLINE ASSEMBLY
mov cl, [Buffer.Get.$local.ret]
pop edx
pop ebx
pop eax
push dword [Buffer.returnVal]
ret
	;Vars:
Buffer.Get.$local.ret :
	db 0x0


Buffer.SetInt: 
pop dword [Buffer.returnVal]
pop dword [Buffer.$local.pos]
pop dword [Buffer.$local.val]
push eax
push ebx
push edx
mov dword [ebx+Buffer.Set.$local.pos+$LinkedClassSize], [Buffer.Set.$local.val]	; INLINE ASSEMBLY
pop edx
pop ebx
pop eax
push dword [Buffer.returnVal]
ret
	;Vars:


Buffer.GetInt: 
pop dword [Buffer.returnVal]
pop dword [Buffer.$local.pos]
push eax
push ebx
push edx
mov ecx, 255
mov [Buffer.GetInt.$local.ret], ecx
mov dword [Buffer.GetInt.$local.ret], [ebx+Buffer.Get.$local.pos+$LinkedClassSize]	; INLINE ASSEMBLY
mov ecx, [Buffer.GetInt.$local.ret]
pop edx
pop ebx
pop eax
push dword [Buffer.returnVal]
ret
	;Vars:
Buffer.GetInt.$local.ret :
	dd 0x0


Buffer.returnVal:
	dd 0x0
Buffer.$FILE_END :
