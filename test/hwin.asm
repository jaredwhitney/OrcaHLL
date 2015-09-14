[bits 32]

dd HelloWorldWindowProgram.$FILE_END - HelloWorldWindowProgram.$FILE_START
db "OrcaHLL Class", 0
db "HelloWorldWindowProgram", 0
HelloWorldWindowProgram.$FILE_START :

HelloWorldWindowProgram._init: 
pop dword [HelloWorldWindowProgram.returnVal]
push eax
push ebx
push edx
mov ax, 0x0002
int 0x30
call DisplayWindow
mov ax, 0x0004
int 0x30
mov [HelloWorldWindowProgram._init.$local.ramPercent], ecx
mov ecx, [HelloWorldWindowProgram._init.$local.ramPercent]
push ecx
call PrintRamInfo
mov ax, 0x0001
int 0x30
mov ax, 0x0001
int 0x30
pop edx
pop ebx
pop eax
push dword [HelloWorldWindowProgram.returnVal]
ret
	;Vars:
HelloWorldWindowProgram._init.$local.ramPercent :
	dd 0x0


HelloWorldWindowProgram.DisplayWindow: 
pop dword [HelloWorldWindowProgram.returnVal]
push eax
push ebx
push edx
mov [HelloWorldWindowProgram.DisplayWindow.$local.w], ecx
mov [HelloWorldWindowProgram.DisplayWindow.$local.text], ecx
call text.RawToWhite
push ecx
call w.buffer.AppendLine
mov ecx, 10
push edx
mov ecx, [edx]
add cl, [Window.HelloWorldWindowProgram.DisplayWindow.$local.w]
mov edx, [ecx]
pop edx
pop ebx
pop eax
push dword [HelloWorldWindowProgram.returnVal]
ret
	;Vars:
HelloWorldWindowProgram.DisplayWindow.$local.w :
	dd 0x0
HelloWorldWindowProgram.DisplayWindow.$local.text :
	dd 0x0


HelloWorldWindowProgram.returnVal:
	dd 0x0
HelloWorldWindowProgram.$FILE_END :
