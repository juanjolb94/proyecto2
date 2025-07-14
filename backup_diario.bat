@echo off
setlocal enabledelayedexpansion

REM ===== BACKUP AUTOMÁTICO GCSYS - FIN DE JORNADA =====
REM Configuración específica para tu sistema proyecto2

REM ===== CONFIGURACIÓN =====
set DB_NAME=proyecto2
set DB_USER=root
set DB_PASS=root
set DB_HOST=localhost
set DB_PORT=3306

REM Directorios (AJUSTAR SEGÚN TU SISTEMA)
set BACKUP_DIR=C:\backup\gcsys
set MYSQL_BIN="C:\Program Files\MySQL\MySQL Server 8.0\bin"
set LOG_FILE=%BACKUP_DIR%\backup_log.txt

REM ===== CREAR TIMESTAMP =====
for /f "tokens=2-4 delims=/ " %%a in ('date /t') do set DATE=%%c%%a%%b
for /f "tokens=1-2 delims=: " %%a in ('time /t') do set TIME=%%a%%b
set TIMESTAMP=%DATE%_%TIME%
set TIMESTAMP=%TIMESTAMP::=%
set TIMESTAMP=%TIMESTAMP: =%

REM ===== INICIAR LOG =====
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"
echo. >> "%LOG_FILE%"
echo =============================================== >> "%LOG_FILE%"
echo BACKUP AUTOMATICO - %date% %time% >> "%LOG_FILE%"
echo =============================================== >> "%LOG_FILE%"

REM ===== VALIDACIONES =====
echo Iniciando backup automático del sistema GCSYS...
echo Validando componentes del sistema... >> "%LOG_FILE%"

REM Verificar MySQL
if not exist %MYSQL_BIN%\mysqldump.exe (
    echo ERROR: MySQL no encontrado en %MYSQL_BIN% >> "%LOG_FILE%"
    echo ERROR: Verifique la instalación de MySQL
    timeout /t 10
    exit /b 1
)
echo MySQL encontrado correctamente >> "%LOG_FILE%"

REM Verificar conexión a la base de datos
%MYSQL_BIN%\mysql.exe -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% -e "USE %DB_NAME%; SELECT COUNT(*) FROM ventas;" > nul 2>&1
if errorlevel 1 (
    echo ERROR: No se puede conectar a la base de datos %DB_NAME% >> "%LOG_FILE%"
    echo ERROR: Verifique que MySQL esté ejecutándose y la base de datos existe
    timeout /t 10
    exit /b 1
)
echo Conexión a base de datos OK >> "%LOG_FILE%"

REM ===== REALIZAR BACKUP COMPLETO =====
echo.
echo Realizando backup completo del sistema...
echo Iniciando backup de %DB_NAME%... >> "%LOG_FILE%"

set BACKUP_FILE=%BACKUP_DIR%\gcsys_finJornada_%TIMESTAMP%.sql

%MYSQL_BIN%\mysqldump.exe ^
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
    --databases %DB_NAME% > "%BACKUP_FILE%"

if errorlevel 1 (
    echo ERROR: Falló el backup >> "%LOG_FILE%"
    echo ERROR: El backup automático falló. Contacte al administrador.
    timeout /t 15
    exit /b 1
)

echo Backup de base de datos completado >> "%LOG_FILE%"

REM ===== COMPRIMIR ARCHIVO =====
if exist "C:\Program Files\7-Zip\7z.exe" (
    echo Comprimiendo archivo...
    "C:\Program Files\7-Zip\7z.exe" a -tgzip "%BACKUP_FILE%.gz" "%BACKUP_FILE%" > nul 2>&1
    if not errorlevel 1 (
        del "%BACKUP_FILE%"
        set FINAL_FILE=%BACKUP_FILE%.gz
        echo Archivo comprimido correctamente >> "%LOG_FILE%"
    ) else (
        set FINAL_FILE=%BACKUP_FILE%
        echo Backup sin comprimir (7-Zip no disponible) >> "%LOG_FILE%"
    )
) else (
    set FINAL_FILE=%BACKUP_FILE%
    echo Backup sin comprimir (7-Zip no instalado) >> "%LOG_FILE%"
)

REM ===== OBTENER ESTADÍSTICAS =====
for %%A in ("!FINAL_FILE!") do set FILE_SIZE=%%~zA
set /a FILE_SIZE_MB=!FILE_SIZE!/1048576

echo Tamaño del backup: !FILE_SIZE_MB! MB >> "%LOG_FILE%"

REM ===== LIMPIEZA DE ARCHIVOS ANTIGUOS =====
echo Limpiando backups antiguos...
echo Eliminando backups anteriores a 7 días... >> "%LOG_FILE%"
forfiles /p "%BACKUP_DIR%" /s /m gcsys_*.sql* /d -7 /c "cmd /c del @path" 2>nul
forfiles /p "%BACKUP_DIR%" /s /m gcsys_*.gz /d -7 /c "cmd /c del @path" 2>nul

REM ===== RESUMEN FINAL =====
echo.
echo ===== BACKUP COMPLETADO EXITOSAMENTE =====
echo Archivo generado: !FINAL_FILE!
echo Tamaño: !FILE_SIZE_MB! MB
echo.
echo BACKUP COMPLETADO - %date% %time% >> "%LOG_FILE%"
echo Archivo: !FINAL_FILE! (!FILE_SIZE_MB! MB) >> "%LOG_FILE%"
echo =============================================== >> "%LOG_FILE%"

echo El backup automático se completó correctamente.
echo Puede cerrar esta ventana o presionar cualquier tecla.
timeout /t 5

exit /b 0