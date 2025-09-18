Herramienta a utilizar para la codificacionde imagenes png, jpeg
exiftool
Herramienta para decodificar la imagen dentro de android
exinterface (jpeg)
metadata-extractor (png)


    Lee los bytes de la imagen desde los assets.
    Intenta extraer el script primero como JPG (EXIF), luego como PNG (tEXt/iTXt).
    Decodifica el texto (base64 a UTF-8) y determina el tipo (sh, js o unknown).
    Retorna un objeto con el tipo detectado y el contenido del script en texto plano.

