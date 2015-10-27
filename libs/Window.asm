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

Window.$global.TYPE_IMAGE :
	db 0x0
Window.$global.TYPE_TEXT :
	db 0x0
Window.Create: 
pop dword [Window.Create.returnVal]
pop ecx
mov [Window.Create.$local.type], cl
pop ecx
mov [Window.Create.$local.title], ecx
push eax
push ebx
push edx
mov ecx, 39
push ecx
mov ax, 0x0502
int 0x30
mov [Window.Create.$local.ret], ecx
mov ecx, [Window.Create.$local.title]
push edx	; Begin getting subvar
mov edx, [Window.Create.$local.ret]
add dl, Window.title
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
xor ecx, ecx
mov cl, [Window.Create.$local.type]
push edx	; Begin getting subvar
mov edx, [Window.Create.$local.ret]
add dl, Window.type
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
mov ecx, 40
mov [Window.Create.$local.wk], cx
xor ecx, ecx
mov cx, [Window.Create.$local.wk]
push edx	; Begin getting subvar
mov edx, [Window.Create.$local.ret]
add dl, Window.width
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
xor ecx, ecx
mov cx, [Window.Create.$local.wk]
push edx	; Begin getting subvar
mov edx, [Window.Create.$local.ret]
add dl, Window.height
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
mov ecx, 0x1	; System Constant
push ecx
mov ax, 0x0001
int 0x30
mov [Window.Create.$local.size], ecx
push edx	; Math start
mov ecx, 0x2	; System Constant
push ecx
mov ax, 0x0001
int 0x30
mov edx, ecx
mov ecx, [Window.Create.$local.size]
imul ecx, edx
pop edx	; Math end
mov [Window.Create.$local.size], ecx
mov ecx, [Window.Create.$local.size]
push ecx
mov ax, 0x0501
int 0x30
push edx	; Begin getting subvar
mov edx, [Window.Create.$local.ret]
add dl, Window.windowBuffer
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
mov ecx, [Window.Create.$local.size]
push ecx
mov ax, 0x0502
int 0x30
push edx	; Begin getting subvar
mov edx, [Window.Create.$local.ret]
add dl, Window.buffer
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
mov ecx, [Window.Create.$local.size]
push ecx
mov ax, 0x0502
int 0x30
push edx	; Begin getting subvar
mov edx, [Window.Create.$local.ret]
add dl, Window.oldBuffer
mov eax, edx
mov edx, [edx]
pop edx	; End getting subvar
mov [eax], ecx
mov ecx, [Window.Create.$local.ret]
pop edx
pop ebx
pop eax
push dword [Window.Create.returnVal]
ret
	;Vars:
Window.Create.$local.ret :
	dd 0x0
Window.Create.$local.wk :
	dw 0x0
Window.Create.$local.size :
	dd 0x0
Window.Create.$local.title :
	dd 0x0
Window.Create.$local.type :
	db 0x0
Window.Create.returnVal:
	dd 0x0


Window.$FILE_END :

