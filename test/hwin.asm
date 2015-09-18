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
call HelloWorldWindowProgram.DisplayWindow
mov ax, 0x0004
int 0x30
mov [HelloWorldWindowProgram._init.$local.ramPercent], ecx
mov ecx, [HelloWorldWindowProgram._init.$local.ramPercent]
push ecx
call HelloWorldWindowProgram.PrintRamInfo
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
push edx	; Begin getting subvar
mov edx, [HelloWorldWindowProgram.DisplayWindow.$local.w]
add dl, Window.xPos
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
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


HelloWorldWindowProgram.PrintRamInfo: 
pop dword [HelloWorldWindowProgram.returnVal]
pop dword [HelloWorldWindowProgram.$local.ramPercent]
push eax
push ebx
push edx
push edx
mov ecx, [HelloWorldWindowProgram.$local.ramPercent]
mov edx, ecx
mov ecx, 50
cmp edx, ecx
pop edx
jg HelloWorldWindowProgram.$comp_17.true
mov cl, 0x0
jmp HelloWorldWindowProgram.$comp_17.done
HelloWorldWindowProgram.$comp_17.true :
mov cl, 0xFF
HelloWorldWindowProgram.$comp_17.done :

push ecx
call HelloWorldWindowProgram.
cmp cl, 0xFF
	jne HelloWorldWindowProgram.$loop_if.0_close
mov ax, 0x0002
int 0x30
HelloWorldWindowProgram.$loop_if.0_close :

push edx
mov ecx, [HelloWorldWindowProgram.$local.ramPercent]
mov edx, ecx
mov ecx, 50
cmp edx, ecx
pop edx
jle HelloWorldWindowProgram.$comp_19.true
mov cl, 0x0
jmp HelloWorldWindowProgram.$comp_19.done
HelloWorldWindowProgram.$comp_19.true :
mov cl, 0xFF
HelloWorldWindowProgram.$comp_19.done :

push ecx
call HelloWorldWindowProgram.
cmp cl, 0xFF
	jne HelloWorldWindowProgram.$loop_if.1_close
mov ax, 0x0002
int 0x30
HelloWorldWindowProgram.$loop_if.1_close :

mov ax, 0x0001
int 0x30
mov ax, 0x0003
int 0x30
mov ax, 0x0002
int 0x30
pop edx
pop ebx
pop eax
push dword [HelloWorldWindowProgram.returnVal]
ret
	;Vars:


HelloWorldWindowProgram.AddOneAndTwo: 
pop dword [HelloWorldWindowProgram.returnVal]
push eax
push ebx
push edx
push edx	; Math start
mov ecx, 2
mov edx, ecx
mov ecx, 1
add ecx, edx
pop edx	; Math end
pop edx
pop ebx
pop eax
push dword [HelloWorldWindowProgram.returnVal]
ret
	;Vars:


HelloWorldWindowProgram.returnVal:
	dd 0x0
HelloWorldWindowProgram.$FILE_END :
