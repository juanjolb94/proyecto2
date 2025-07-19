@echo off
setlocal enabledelayedexpansion

REM ===== BACKUP AUTOMATICO GCSYS - FIN DE JORNADA =====
REM Configuracion especifica para tu sistema proyecto2

REM ===== CONFIGURACION =====
set DB_NAME=proyecto2
set DB_USER=root
set DB_PASS=root
set DB_HOST=localhost
set DB_PORT=3306

REM Directorios (SIN COMILLAS EN LA VARIABLE)
set BACKUP_DIR=C:\backup\gcsys
set MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin
set LOG_FILE=%BACKUP_DIR%\backup_log.txt

REM ===== CREAR TIMESTAMP =====
for /f "tokens=2-4 delims=/ " %%a in ('date /t') do set DATE=%%c%%a%%b
for /f "tokens=1-2 delims=: " %%a in ('time /t') do set TIME=%%a%%b
set TIMESTAMP=%DATE%_%TIME%
set TIMESTAMP=%TIMESTAMP::=%
set TIMESTAMP=%TIMESTAMP: =%

REM ===== CREAR DIRECTORIO Y LOG =====
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"
echo. >> "%LOG_FILE%"
echo =============================================== >> "%LOG_FILE%"
echo BACKUP AUTOMATICO - %date% %time% >> "%LOG_FILE%"
echo =============================================== >> "%LOG_FILE%"

echo ===== INICIANDO BACKUP AUTOMATICO GCSYS =====
echo Fecha/Hora: %date% %time%
echo.

REM ===== VERIFICAR MYSQL =====
echo Verificando MySQL en: %MYSQL_BIN%
if exist "%MYSQL_BIN%\mysqldump.exe" (
    echo [OK] MySQL encontrado correctamente
    echo MySQL encontrado en: %MYSQL_BIN% >> "%LOG_FILE%"
) else (
    echo [ERROR] MySQL no encontrado en: %MYSQL_BIN%
    echo Verifique la instalacion de MySQL
    echo ERROR: MySQL no encontrado >> "%LOG_FILE%"
    pause
    exit /b 1
)

REM ===== VERIFICAR CONEXION A LA BASE DE DATOS =====
echo Verificando conexion a la base de datos %DB_NAME%...

REM Usar comillas solo alrededor de toda la ruta + comando
"%MYSQL_BIN%\mysql.exe" -u %DB_USER% -p%DB_PASS% -e "USE %DB_NAME%; SELECT COUNT(*) FROM usuarios;" > nul 2>&1

if errorlevel 1 (
    echo.
    echo [ERROR] No se puede conectar a la base de datos %DB_NAME%
    echo.
    echo Verificando paso a paso...
    
    REM Probar conexion basica
    echo Probando conexion basica a MySQL...
    "%MYSQL_BIN%\mysql.exe" -u %DB_USER% -p%DB_PASS% -e "SELECT VERSION();" > temp_test.txt 2>&1
    
    if errorlevel 1 (
        echo [ERROR] No se puede conectar a MySQL Server
        echo Contenido del error:
        type temp_test.txt
    ) else (
        echo [OK] Conexion a MySQL exitosa
        echo Verificando base de datos %DB_NAME%...
        
        "%MYSQL_BIN%\mysql.exe" -u %DB_USER% -p%DB_PASS% -e "SHOW DATABASES;" | findstr /i "%DB_NAME%" > nul
        if errorlevel 1 (
            echo [ERROR] La base de datos '%DB_NAME%' no existe
            echo Bases de datos disponibles:
            "%MYSQL_BIN%\mysql.exe" -u %DB_USER% -p%DB_PASS% -e "SHOW DATABASES;"
        ) else (
            echo [OK] Base de datos '%DB_NAME%' existe
            echo [ERROR] Problema con tabla 'usuarios' o permisos
        )
    )
    
    del temp_test.txt 2>nul
    echo.
    echo ERROR: Validacion de conexion fallo >> "%LOG_FILE%"
    pause
    exit /b 1
)

echo [OK] Conexion a base de datos exitosa
echo Conexion a base de datos OK >> "%LOG_FILE%"

REM ===== REALIZAR BACKUP COMPLETO =====
echo.
echo Realizando backup completo del sistema...
echo Iniciando backup de %DB_NAME%... >> "%LOG_FILE%"

set BACKUP_FILE=%BACKUP_DIR%\gcsys_finJornada_%TIMESTAMP%.sql

echo Ejecutando mysqldump...
"%MYSQL_BIN%\mysqldump.exe" ^
    --host=%DB_HOST% ^
    --port=%DB_PORT% ^
    --user=%DB_USER% ^
    --password=%DB_PASS% ^
    --routines ^
    --triggers ^
    --events ^
    --single-transaction ^
    --lock-tables=false ^
    --add-drop-database ^
    --complete-insert ^
    --databases %DB_NAME% > "%BACKUP_FILE%" 2>&1

if errorlevel 1 (
    echo.
    echo [ERROR] Fallo el backup de la base de datos
    echo Contenido del error:
    type "%BACKUP_FILE%"
    echo.
    echo ERROR: Fallo el backup >> "%LOG_FILE%"
    pause
    exit /b 1
)

REM Verificar que el archivo de backup no este vacio
for %%A in ("%BACKUP_FILE%") do set BACKUP_SIZE=%%~zA
if %BACKUP_SIZE% LSS 1000 (
    echo.
    echo [ERROR] El archivo de backup esta vacio o muy pequeno (%BACKUP_SIZE% bytes)
    echo Contenido del archivo:
    type "%BACKUP_FILE%"
    echo.
    pause
    exit /b 1
)

echo [OK] Backup de base de datos completado (%BACKUP_SIZE% bytes)
echo Backup de base de datos completado >> "%LOG_FILE%"

REM ===== COMPRIMIR ARCHIVO =====
echo.
if exist "C:\Program Files\7-Zip\7z.exe" (
    echo Comprimiendo archivo con 7-Zip...
    "C:\Program Files\7-Zip\7z.exe" a -tgzip "%BACKUP_FILE%.gz" "%BACKUP_FILE%" > nul 2>&1
    if not errorlevel 1 (
        del "%BACKUP_FILE%"
        set FINAL_FILE=%BACKUP_FILE%.gz
        echo [OK] Archivo comprimido correctamente
        echo Archivo comprimido correctamente >> "%LOG_FILE%"
    ) else (
        set FINAL_FILE=%BACKUP_FILE%
        echo [INFO] Compresion fallo, archivo sin comprimir
        echo Backup sin comprimir (error en 7-Zip) >> "%LOG_FILE%"
    )
) else (
    set FINAL_FILE=%BACKUP_FILE%
    echo [INFO] 7-Zip no instalado, archivo sin comprimir
    echo Backup sin comprimir (7-Zip no instalado) >> "%LOG_FILE%"
)

REM ===== OBTENER ESTADISTICAS =====
for %%A in ("!FINAL_FILE!") do set FILE_SIZE=%%~zA
set /a FILE_SIZE_MB=!FILE_SIZE!/1048576

echo [OK] Tamano del backup: !FILE_SIZE_MB! MB
echo Tamano del backup: !FILE_SIZE_MB! MB >> "%LOG_FILE%"

REM ===== LIMPIEZA DE ARCHIVOS ANTIGUOS =====
echo.
echo Limpiando backups antiguos (mas de 7 dias)...
echo Eliminando backups anteriores a 7 dias... >> "%LOG_FILE%"
forfiles /p "%BACKUP_DIR%" /s /m gcsys_*.sql* /d -7 /c "cmd /c del @path" 2>nul
forfiles /p "%BACKUP_DIR%" /s /m gcsys_*.gz /d -7 /c "cmd /c del @path" 2>nul

REM ===== RESUMEN FINAL =====
echo.
echo ===== BACKUP COMPLETADO EXITOSAMENTE =====
echo Archivo generado: !FINAL_FILE!
echo Tamano: !FILE_SIZE_MB! MB
echo Ubicacion: %BACKUP_DIR%
echo.
echo BACKUP COMPLETADO - %date% %time% >> "%LOG_FILE%"
echo Archivo: !FINAL_FILE! (!FILE_SIZE_MB! MB) >> "%LOG_FILE%"
echo =============================================== >> "%LOG_FILE%"

echo.
echo El backup automatico se completo correctamente.
echo Presione cualquier tecla para cerrar...
pause

exit /b 0