--
-- File generato con SQLiteStudio v3.4.4 su mar gen 23 16:06:33 2024
--
-- Codifica del testo utilizzata: System
--
PRAGMA foreign_keys = off;
BEGIN TRANSACTION;

-- Tabella: fotofiles
DROP TABLE IF EXISTS fotofiles;

CREATE TABLE IF NOT EXISTS fotofiles (
    id       INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    filepath TEXT
);


-- Tabella: gpspos
DROP TABLE IF EXISTS gpspos;

CREATE TABLE IF NOT EXISTS gpspos (
    id        INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    timestamp INTEGER NOT NULL,
    longitude REAL    NOT NULL,
    latitude  REAL    NOT NULL,
    altitude  REAL    NOT NULL,
    source    TEXT,
    idfile    INTEGER REFERENCES fotofiles (id) 
);
-- Indice: IX_Gpspos_longitude
DROP INDEX IF EXISTS IX_Gpspos_longitude;

CREATE INDEX IF NOT EXISTS IX_Gpspos_longitude ON gpspos (
    longitude
);


-- Indice: IX_Gpspos_timestamp
DROP INDEX IF EXISTS IX_Gpspos_timestamp;

CREATE INDEX IF NOT EXISTS IX_Gpspos_timestamp ON gpspos (
    timestamp
);


-- Vista: selGpsPos
DROP VIEW IF EXISTS selGpsPos;
CREATE VIEW IF NOT EXISTS selGpsPos AS
    SELECT datetime(gp.timestamp, 'unixepoch') AS timestamp,
           gp.latitude,
           gp.longitude,
           gp.altitude,
           gp.source,
           ft.filepath
      FROM gpspos AS gp
           LEFT OUTER JOIN
           fotofiles AS ft ON gp.idfile = ft.id
     WHERE 1 = 1
     ORDER BY timestamp;


COMMIT TRANSACTION;
PRAGMA foreign_keys = on;
