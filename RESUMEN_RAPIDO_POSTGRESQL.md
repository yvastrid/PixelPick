# ⚡ Resumen Rápido: PostgreSQL en Producción

## 🎯 Pasos Rápidos (Render - Más Fácil)

### 1️⃣ Crear Base de Datos PostgreSQL

```
Render Dashboard → New + → PostgreSQL
- Name: pixelpick-db
- Plan: Free (o Starter para producción)
- Crear → Copiar "Internal Database URL"
```

### 2️⃣ Configurar Variables de Entorno

En tu Web Service → Environment → Agregar:

```
DATABASE_URL = postgresql://usuario:contraseña@host:puerto/db
SECRET_KEY = [genera una clave con el script]
```

### 3️⃣ Generar SECRET_KEY

```bash
# Opción 1: Usar el script
python3 script_generar_secret_key.py

# Opción 2: Comando directo
python3 -c "import secrets; print(secrets.token_urlsafe(32))"
```

### 4️⃣ Re-desplegar

```
Render detectará los cambios automáticamente
O haz clic en "Manual Deploy"
```

### 5️⃣ Verificar

```
✅ Logs muestran "Database connection successful"
✅ Puedes registrarte con un nuevo usuario
✅ Los datos persisten
```

---

## 📝 Variables de Entorno Necesarias

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `DATABASE_URL` | URL de conexión a PostgreSQL | `postgresql://user:pass@host:5432/db` |
| `SECRET_KEY` | Clave para sesiones seguras | `3Ra2iYdWZmPp_GWsrPYQFbylE4x2L0XSwWvVFY6QTUM` |

---

## 🔗 Enlaces Rápidos

- **Render Dashboard**: https://dashboard.render.com
- **Railway Dashboard**: https://railway.app
- **Heroku Dashboard**: https://dashboard.heroku.com

---

## 📚 Guías Completas

- **Guía detallada**: Ver `GUIA_POSTGRESQL_PRODUCCION.md`
- **Guía de despliegue general**: Ver `GUIA_DESPLIEGUE.md`

---

## ⚠️ Checklist Final

- [ ] Base de datos PostgreSQL creada
- [ ] `DATABASE_URL` configurada
- [ ] `SECRET_KEY` configurada
- [ ] Aplicación re-desplegada
- [ ] Logs muestran conexión exitosa
- [ ] Puedes registrarte e iniciar sesión

---

## 🆘 ¿Problemas?

1. **Error de conexión**: Verifica que `DATABASE_URL` sea correcta
2. **Tablas no creadas**: Revisa los logs, deberían crearse automáticamente
3. **Datos no persisten**: Verifica que estés usando PostgreSQL, no SQLite

---

**¡Tu aplicación estará lista en menos de 10 minutos!** 🚀

