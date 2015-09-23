[bits 32]

dd Simple.$FILE_END - Simple.$FILE_START
db "OrcaHLL Class", 0
db "Simple", 0
Simple.$FILE_START :

Simple._run: 
pop dword [Simple.returnVal]
push eax
push ebx
push edx
push ebx
mov ebx, ebx
call Simple._test
pop ebx
pop edx
pop ebx
pop eax
push dword [Simple.returnVal]
ret
	;Vars:


Simple._test: 
pop dword [Simple.returnVal]
push eax
push ebx
push edx
mov ecx, [ebx]
mov [Simple._test.$local.obj], ecx
mov ecx, [Simple._test.$local.obj]
mov [Simple._test.$local.obj2], ecx
pop edx
pop ebx
pop eax
push dword [Simple.returnVal]
ret
	;Vars:
Simple._test.$local.obj :
	dd 0x0
Simple._test.$local.obj2 :
	dd 0x0


Simple.returnVal:
	dd 0x0
Simple.$FILE_END :

