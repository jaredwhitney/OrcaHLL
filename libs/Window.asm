[bits 32]

dd Window.$FILE_END - Window.$FILE_START
db "OrcaHLL Class", 0
db "Window", 0
Window.$FILE_START :

Window.winNum equ 38
Window.yPos equ 16
Window.windowBuffer equ 22
Window.xPos equ 12
Window.title equ 0
Window.type equ 20
Window.depth equ 21
Window.lastYpos equ 18
Window.lastXpos equ 14
Window.width equ 4
Window.lastWidth equ 6
Window.buffer equ 26
Window.lastHeight equ 10
Window.oldBuffer equ 34
Window.height equ 8
Window.bufferSize equ 30

Window.$global.TYPE_TEXT :
	dd 0x0
Window._dummyFunc: 
pop dword [Window._dummyFunc.returnVal]
push eax
push ebx
push edx
mov ecx, 0
mov [Window._dummyFunc.$local.y], ecx
pop edx
pop ebx
pop eax
push dword [Window._dummyFunc.returnVal]
ret
	;Vars:
Window._dummyFunc.$local.y :
	dd 0x0
Window._dummyFunc.returnVal:
	dd 0x0


Window.$FILE_END :

