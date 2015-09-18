[bits 32]

dd Window.$FILE_END - Window.$FILE_START
db "OrcaHLL Class", 0
db "Window", 0
Window.$FILE_START :

Window.$offs.lastxPos equ 14
Window.$offs.winNum equ 43
Window.$offs.lastyPos equ 18
Window.$offs.yPos equ 16
Window.$offs.windowBuffer equ 22
Window.$offs.rectlBase equ 35
Window.$offs.needsRectUpdate equ 34
Window.$offs.xPos equ 12
Window.$offs.title equ 0
Window.$offs.type equ 20
Window.$offs.rectlTop equ 39
Window.$offs.depth equ 21
Window.$offs.width equ 4
Window.$offs.lastWidth equ 6
Window.$offs.buffer equ 26
Window.$offs.lastHeight equ 10
Window.$offs.oldBuffer equ 30
Window.$offs.height equ 8

Window.Create: 
pop dword [Window.returnVal]
pop dword [Window.$local.title]
pop dword [Window.$local.type]
push eax
push ebx
push edx
mov [Window.Create.$local.ret], ecx
mov ecx, [Window.$local.title]
push edx
mov [Window.$offs.title], ecx
mov ecx, 4
push edx
mov [Window.$offs.width], cx
mov ecx, 4
push edx
mov [Window.$offs.height], cx
mov ecx, 0
push edx
mov [Window.$offs.xPos], cx
mov ecx, 8
push edx
mov [Window.$offs.yPos], cx
mov ecx, [Window.$local.type]
push edx
mov [Window.$offs.type], cl
mov ecx, 0
push edx
mov [Window.$offs.depth], cl
push edx	; Math start
mov ecx, 0x200
mov edx, ecx
mov ecx, 0x7D
imul ecx, edx
pop edx	; Math end
mov [Window.Create.$local.size], ecx
push edx
mov [Window.$offs.windowBuffer], ecx
push edx
mov [Window.$offs.buffer], ecx
push edx
mov [Window.$offs.oldBuffer], ecx
mov ecx, [Window.Create.$local.ret]
push ecx
call Dolphin.registerWindow
push edx
mov [Window.$offs.winNum], cx
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
