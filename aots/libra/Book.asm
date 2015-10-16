[bits 32]

dd Book.$FILE_END - Book.$FILE_START
db "OrcaHLL Class", 0
db "Book", 0
Book.$FILE_START :

Book.pages equ 4
Book.title equ 0

Book.Create: 
pop dword [Book.Create.returnVal]
pop dword [Book.Create.$local.title]
push eax
push ebx
push edx
mov ecx, 8
push ecx
mov ax, 0x0501
int 0x30
mov [Book.Create.$local.ret], ecx
mov ecx, 0x10
push edx	; Begin getting subvar
mov edx, [Book.Create.$local.ret]
add dl, Book.pages
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
mov ecx, [Book.Create.$local.title]
push edx	; Begin getting subvar
mov edx, [Book.Create.$local.ret]
add dl, Book.title
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
mov ecx, [Book.Create.$local.ret]
pop edx
pop ebx
pop eax
push dword [Book.Create.returnVal]
ret
	;Vars:
Book.Create.$local.ret :
	dd 0x0
Book.Create.$local.title :
	dd 0x0
Book.Create.returnVal:
	dd 0x0


Book.$FILE_END :

