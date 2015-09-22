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
mov ecx, [HelloWorldWindowProgram._init.string_0]
push ecx
mov ax, 0x0002
int 0x30
call HelloWorldWindowProgram.DisplayWindow
mov ax, 0x0004
int 0x30
mov [HelloWorldWindowProgram._init.$local.ramPercent], ecx
mov ecx, [HelloWorldWindowProgram._init.$local.ramPercent]
push ecx
call HelloWorldWindowProgram.PrintRamInfo
mov ecx, [HelloWorldWindowProgram._init.string_1]
push ecx
mov ax, 0x0001
int 0x30
call HelloWorldWindowProgram.AddOneAndTwo
push ecx
mov ax, 0x0001
int 0x30
pop edx
pop ebx
pop eax
push dword [HelloWorldWindowProgram.returnVal]
ret
	;Vars:
HelloWorldWindowProgram._init.string_0_data :
	db "Hello world!", 0
HelloWorldWindowProgram._init.$local.ramPercent :
	dd 0x0
HelloWorldWindowProgram._init.string_0 :
	dd HelloWorldWindowProgram._init.string_0_data
HelloWorldWindowProgram._init.string_1_data :
	db "1 + 2 = ", 0
HelloWorldWindowProgram._init.string_1 :
	dd HelloWorldWindowProgram._init.string_1_data


HelloWorldWindowProgram.DisplayWindow: 
pop dword [HelloWorldWindowProgram.returnVal]
push eax
push ebx
push edx
mov [HelloWorldWindowProgram.DisplayWindow.$local.w], ecx
mov ecx, [HelloWorldWindowProgram.DisplayWindow.string_0]
mov [HelloWorldWindowProgram.DisplayWindow.$local.text], ecx
push edx	; Begin getting subvar
mov edx, [HelloWorldWindowProgram.DisplayWindow.$local.w]
add dl, Window.buffer
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
push ebx
mov ebx, eax
push ebx
mov ebx, HelloWorldWindowProgram.DisplayWindow.$local.text
pop ebx
call text.RawToWhite
push ecx
pop ebx
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
HelloWorldWindowProgram.DisplayWindow.string_0_data :
	db "Hello World", 0
HelloWorldWindowProgram.DisplayWindow.string_0 :
	dd HelloWorldWindowProgram.DisplayWindow.string_0_data
HelloWorldWindowProgram.DisplayWindow.$local.w :
	dd 0x0
HelloWorldWindowProgram.DisplayWindow.$local.text :
	dd 0x0


HelloWorldWindowProgram.PrintRamInfo: 
pop dword [HelloWorldWindowProgram.returnVal]
pop dword [HelloWorldWindowProgram.PrintRamInfo.$local.ramPercent]
push eax
push ebx
push edx
push edx
mov ecx, [HelloWorldWindowProgram.PrintRamInfo.$local.ramPercent]
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

cmp cl, 0xFF
	jne HelloWorldWindowProgram.$loop_if.0_close
mov ecx, [HelloWorldWindowProgram.$loop_if.0.string_0]
push ecx
mov ax, 0x0002
int 0x30
HelloWorldWindowProgram.$loop_if.0_close :

push edx
mov ecx, [HelloWorldWindowProgram.PrintRamInfo.$local.ramPercent]
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

cmp cl, 0xFF
	jne HelloWorldWindowProgram.$loop_if.1_close
mov ecx, [HelloWorldWindowProgram.$loop_if.1.string_0]
push ecx
mov ax, 0x0002
int 0x30
HelloWorldWindowProgram.$loop_if.1_close :

mov ecx, [HelloWorldWindowProgram.PrintRamInfo.string_0]
push ecx
mov ax, 0x0001
int 0x30
mov ecx, [HelloWorldWindowProgram.PrintRamInfo.$local.ramPercent]
push ecx
mov ax, 0x0003
int 0x30
mov ecx, [HelloWorldWindowProgram.PrintRamInfo.string_1]
push ecx
mov ax, 0x0002
int 0x30
pop edx
pop ebx
pop eax
push dword [HelloWorldWindowProgram.returnVal]
ret
	;Vars:
HelloWorldWindowProgram.PrintRamInfo.string_1 :
	dd HelloWorldWindowProgram.PrintRamInfo.string_1_data
HelloWorldWindowProgram.PrintRamInfo.$local.ramPercent :
	dd 0x0
HelloWorldWindowProgram.PrintRamInfo.string_1_data :
	db "", 0
HelloWorldWindowProgram.PrintRamInfo.string_0_data :
	db "Percentage of RAM in use: ", 0
HelloWorldWindowProgram.PrintRamInfo.string_0 :
	dd HelloWorldWindowProgram.PrintRamInfo.string_0_data
HelloWorldWindowProgram.$loop_if.1.string_0 :
	dd HelloWorldWindowProgram.$loop_if.1.string_0_data
HelloWorldWindowProgram.$loop_if.0.string_0 :
	dd HelloWorldWindowProgram.$loop_if.0.string_0_data
HelloWorldWindowProgram.$loop_if.0.string_0_data :
	db "Over half of RAM in use!", 0
HelloWorldWindowProgram.$loop_if.1.string_0_data :
	db "Under half of RAM in use!", 0


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
; *** LIB IMPORT 'Window' ***
[bits 32]
dd Window.$FILE_END - Window.$FILE_START
db "OrcaHLL Class", 0
db "Window", 0
Window.$FILE_START :

Window.lastxPos equ 14
Window.winNum equ 43
Window.lastyPos equ 18
Window.yPos equ 16
Window.windowBuffer equ 22
Window.rectlBase equ 35
Window.needsRectUpdate equ 34
Window.xPos equ 12
Window.title equ 0
Window.type equ 20
Window.rectlTop equ 39
Window.depth equ 21
Window.width equ 4
Window.lastWidth equ 6
Window.buffer equ 26
Window.lastHeight equ 10
Window.oldBuffer equ 30
Window.height equ 8

Window.Create: 
pop dword [Window.returnVal]
pop dword [Window.$local.title]
pop dword [Window.$local.type]
push eax
push ebx
push edx
mov [Window.Create.$local.ret], ecx
mov ecx, [Window.$local.title]
push edx	; Begin getting subvar
mov edx, [Window.Create.$local.ret]
add dl, Window.title
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
mov ecx, 4
push edx	; Begin getting subvar
mov edx, [Window.Create.$local.ret]
add dl, Window.width
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
mov ecx, 4
push edx	; Begin getting subvar
mov edx, [Window.Create.$local.ret]
add dl, Window.height
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
mov ecx, 0
push edx	; Begin getting subvar
mov edx, [Window.Create.$local.ret]
add dl, Window.xPos
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
mov ecx, 8
push edx	; Begin getting subvar
mov edx, [Window.Create.$local.ret]
add dl, Window.yPos
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
mov ecx, [Window.$local.type]
push edx	; Begin getting subvar
mov edx, [Window.Create.$local.ret]
add dl, Window.type
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
mov ecx, 0
push edx	; Begin getting subvar
mov edx, [Window.Create.$local.ret]
add dl, Window.depth
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
push edx	; Math start
mov ecx, 0x200
mov edx, ecx
mov ecx, 0x7D
imul ecx, edx
pop edx	; Math end
mov [Window.Create.$local.size], ecx
push edx	; Begin getting subvar
mov edx, [Window.Create.$local.ret]
add dl, Window.windowBuffer
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
push edx	; Begin getting subvar
mov edx, [Window.Create.$local.ret]
add dl, Window.buffer
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
push edx	; Begin getting subvar
mov edx, [Window.Create.$local.ret]
add dl, Window.oldBuffer
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
mov ecx, [Window.Create.$local.ret]
push ecx
mov ax, 0x0005
int 0x30
push edx	; Begin getting subvar
mov edx, [Window.Create.$local.ret]
add dl, Window.winNum
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
mov ecx, [Window.Create.$local.ret]
pop edx
pop ebx
pop eax
push dword [Window.returnVal]
ret
	;Vars:
Window.Create.$local.ret :
	dd 0x0
Window.Create.$local.size :
	dd 0x0


Window.returnVal:
	dd 0x0
Window.$FILE_END :


