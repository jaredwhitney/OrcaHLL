[bits 32]

dd VideoInfo.$FILE_END - VideoInfo.$FILE_START
db "OrcaHLL Class", 0
db "VideoInfo", 0
VideoInfo.$FILE_START :

VideoInfo._init: 
pop dword [VideoInfo._init.returnVal]
push eax
push ebx
push edx
mov ax, 0x0104
int 0x30
mov ecx, 0x7	; System Constant
push ecx
mov ax, 0x0001
int 0x30
mov [VideoInfo._init.$local.gcName], ecx
mov ecx, [VideoInfo._init.string_0]
push ecx
mov ax, 0x0100
int 0x30
mov ecx, [VideoInfo._init.$local.gcName]
push ecx
mov ax, 0x0101
int 0x30
pop edx
pop ebx
pop eax
push dword [VideoInfo._init.returnVal]
ret
	;Vars:
VideoInfo._init.string_0 :
	dd VideoInfo._init.string_0_data
VideoInfo._init.string_0_data :
	db "Graphics card name: ", 0
VideoInfo._init.$local.gcName :
	dd 0x0
VideoInfo._init.returnVal:
	dd 0x0


VideoInfo.$FILE_END :

