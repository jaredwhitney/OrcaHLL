[bits 32]

dd Key.$FILE_END - Key.$FILE_START
db "OrcaHLL Class", 0
db "Key", 0
Key.$FILE_START :

Key.$global.KEY_DOWN :
	db 0x50
Key.$global.KEY_LEFT :
	db 0x4B
Key.$global.TAB :
	db 0x3A
Key.$global.ESC :
	db 0x01
Key.$global.KEY_RIGHT :
	db 0x4D
Key.$global.ENTER :
	db 0xFE
Key.$global.KEY_UP :
	db 0x48
Key.$global.BACKSPACE :
	db 0xFF
Key.$FILE_END :

