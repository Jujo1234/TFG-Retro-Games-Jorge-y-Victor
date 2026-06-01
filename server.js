const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');

const app = express();

// Configuración de CORS blindada para aceptar peticiones de cualquier sitio
app.use(cors({
    origin: '*',
    methods: ['GET', 'POST'],
    allowedHeaders: ['Content-Type']
}));

app.use(express.json());

// 1. CONEXIÓN A MONGO (Nube de Railway)
const dbUser = "mongo";
const dbPass = encodeURIComponent("yDeJzAJWwPBXaNbvoEaBdXwSvelCgGaH");
const dbHost = "zephyr.proxy.rlwy.net";
const dbPort = "59216";
const dbName = "arcadeDB";

const uri = `mongodb://${dbUser}:${dbPass}@${dbHost}:${dbPort}/${dbName}?authSource=admin`;

mongoose.connect(uri)
    .then(() => console.log("✅ ¡Conectado a MongoDB con éxito!"))
    .catch(err => {
        const uriFallback = `mongodb://${dbUser}:${dbPass}@${dbHost}:${dbPort}/${dbName}`;
        mongoose.connect(uriFallback)
            .then(() => console.log("✅ ¡Conectado a MongoDB con éxito (Plan B)!"))
            .catch(errFinal => console.error("💥 Error definitivo en MongoDB:", errFinal));
    });

// 2. DEFINIR EL MODELO (Estructura de la sugerencia)
const SugerenciaSchema = new mongoose.Schema({
    usuario: String,
    comentario: String,
    fecha: { type: Date, default: Date.now }
});
const Sugerencia = mongoose.model('Sugerencia', SugerenciaSchema);

// 3. RUTAS API
app.get('/api/sugerencias', async (req, res) => {
    try {
        const lista = await Sugerencia.find().sort({ fecha: -1 });
        res.json(lista || []);
    } catch (error) {
        res.json([]);
    }
});

app.post('/api/sugerencias', async (req, res) => {
    try {
        const nueva = new Sugerencia({
            usuario: req.body.usuario,
            comentario: req.body.comentario
        });
        await nueva.save();
        res.json(nueva);
    } catch (error) {
        res.status(500).json({ error: "Error al guardar" });
    }
});

// 4. ARRANCAR SERVIDOR (Configurado para detectar el puerto automático de Railway o usar el 3000 en tu PC)
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`🚀 Servidor corriendo en el puerto ${PORT}`));