[bits 32]

dd Char.$FILE_END - Char.$FILE_START
db "OrcaHLL Class", 0
db "Char", 0
Char.$FILE_START :

Char.$global.NUL :
	db 0x00
Char.$global.NEWLINE :
	db 0x0A
Char.$FILE_END :

