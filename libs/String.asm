[bits 32]

dd String.$FILE_END - String.$FILE_START
db "OrcaHLL Class", 0
db "String", 0
String.$FILE_START :

String.Append: 
pop dword [String.Append.returnVal]
pop dword [String.Append.$local.s]
push eax
push ebx
push edx
mov ecx, 0
mov [String.Append.$local.q], ecx
push ebx
mov ebx, ebx
mov ecx, [String.Append.$local.q]
push ecx
call String.GetChar
pop ebx
mov [String.Append.$local.ch], cl
String.$loop_while.0_open :
push edx
mov ecx, 
mov edx, ecx
mov ecx, 0
cmp edx, ecx
pop edx
jne String.$comp_4.true
mov cl, 0x0
jmp String.$comp_4.done
String.$comp_4.true :
mov cl, 0xFF
String.$comp_4.done :

push ecx
call String.
cmp cl, 0xFF
	jne String.$loop_while.0_end
mov ecx, [ebx]	; INLINE ASSEMBLY
add ecx, [String.Append.$local.q]	; INLINE ASSEMBLY
sub ecx, 1	; INLINE ASSEMBLY
mov byte [ecx], [String.Append.$local.ch]	; INLINE ASSEMBLY
push edx	; Math start
mov ecx, 1
mov edx, ecx
mov ecx, [String.Append.$local.q]
add ecx, edx
pop edx	; Math end
mov [String.Append.$local.q], ecx
push ebx
mov ebx, ebx
mov ecx, [String.Append.$local.q]
push ecx
call String.GetChar
pop ebx
mov [String.Append.$local.ch], cl
	jmp String.$loop_while.0_open
String.$loop_while.0_end :
mov ecx, [ebx]	; INLINE ASSEMBLY
mov ecx, [String.Append.$local.q]	; INLINE ASSEMBLY
sub ecx, 1	; INLINE ASSEMBLY
mov byte [ecx], 0x0	; INLINE ASSEMBLY
pop edx
pop ebx
pop eax
push dword [String.Append.returnVal]
ret
	;Vars:
String.Append.$local.q :
	dd 0x0
String.Append.$local.s :
	dd 0x0
String.Append.$local.ch :
	db 0x0
String.Append.returnVal:
	dd 0x0


String.GetChar: 
pop dword [String.GetChar.returnVal]
pop dword [String.GetChar.$local.pos]
push eax
push ebx
push edx
mov ecx, [ebx]	; INLINE ASSEMBLY
add ecx, [String.GetChar.$local.pos]	; INLINE ASSEMBLY
mov cl, [ecx]	; INLINE ASSEMBLY
and ecx, 0xFF	; INLINE ASSEMBLY
pop edx
pop ebx
pop eax
push dword [String.GetChar.returnVal]
ret
	;Vars:
String.GetChar.$local.pos :
	dd 0x0
String.GetChar.returnVal:
	dd 0x0


String.SetChar: 
pop dword [String.SetChar.returnVal]
pop dword [String.SetChar.$local.pos]
pop dword [String.SetChar.$local.ch]
push eax
push ebx
push edx
mov ecx, [ebx]	; INLINE ASSEMBLY
add ecx, [String.SetChar.$local.pos]	; INLINE ASSEMBLY
mov al, [String.SetChar.$local.ch]	; INLINE ASSEMBLY
mov byte [ecx], al	; INLINE ASSEMBLY
pop edx
pop ebx
pop eax
push dword [String.SetChar.returnVal]
ret
	;Vars:
String.SetChar.$local.pos :
	dd 0x0
String.SetChar.$local.ch :
	dd 0x0
String.SetChar.returnVal:
	dd 0x0


String.RawToWhite: 
pop dword [String.RawToWhite.returnVal]
push eax
push ebx
push edx
push ebx
mov ebx, ebx
call String.GetLength
pop ebx
push ecx
mov ax, null
int 0x30
mov [String.RawToWhite.$local.ret], ecx
mov ecx, 0xFF
mov [String.RawToWhite.$local.white], cl
mov ecx, 0
mov [String.$loop_for.0.$local.z], ecx
String.$loop_for.0_open :
push ebx
mov ebx, String.RawToWhite.$local.ret
push edx	; Math start
push edx	; Math start
mov ecx, 1
mov edx, ecx
mov ecx, 0
add ecx, edx
pop edx	; Math end
mov [String.$loop_for.0.$local.z], ecx
push edx
mov ecx, 0
mov edx, ecx
push ebx
mov ebx, ebx
call String.GetLength
pop ebx
cmp edx, ecx
pop edx
jl String.$comp_32.true
mov cl, 0x0
jmp String.$comp_32.done
String.$comp_32.true :
mov cl, 0xFF
String.$comp_32.done :

cmp cl, 0xFF
	je String.$loop_for.0_open

pop edx
pop ebx
pop eax
push dword [String.RawToWhite.returnVal]
ret
	;Vars:
String.RawToWhite.$local.ret :
	dd 0x0
String.RawToWhite.$local.white :
	db 0x0
String.$loop_for.0.$local.z :
	dd 0x0
String.RawToWhite.returnVal:
	dd 0x0


String.$FILE_END :

