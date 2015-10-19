[bits 32]

dd EchoTest.$FILE_END - EchoTest.$FILE_START
db "OrcaHLL Class", 0
db "EchoTest", 0
EchoTest.$FILE_START :

EchoTest.$global.text :
	dd 0x0
EchoTest._init: 
pop dword [EchoTest._init.returnVal]
push eax
push ebx
push edx
mov ecx, [EchoTest._init.string_0]
push ecx
mov ax, 0x0100
int 0x30
pop edx
pop ebx
pop eax
push dword [EchoTest._init.returnVal]
ret
	;Vars:
EchoTest._init.string_0 :
	dd EchoTest._init.string_0_data
EchoTest._init.string_0_data :
	db "char: ", 0
EchoTest._init.returnVal:
	dd 0x0


EchoTest._loop: 
pop dword [EchoTest._loop.returnVal]
push eax
push ebx
push edx
mov ax, null
int 0x30
cmp cl, 0xFF
	jne EchoTest.$loop_if.0_close
mov ax, null
int 0x30
mov [EchoTest.$loop_if.0.$local.c], cl
push ebx
mov ebx, EchoTest.$global.text
mov ecx, 0
push ecx
mov ecx, 
push ecx
call String.SetChar
pop ebx
mov ecx, [EchoTest.$global.text]
push ecx
mov ax, 0x0100
int 0x30
EchoTest.$loop_if.0_close :

pop edx
pop ebx
pop eax
push dword [EchoTest._loop.returnVal]
ret
	;Vars:
EchoTest.$loop_if.0.$local.c :
	db 0x0
EchoTest._loop.returnVal:
	dd 0x0


EchoTest.$FILE_END :

