SELECT datetime(timestamp, 'unixepoch') as timestamp
, latitude
, longitude
, altitude
, source
 FROM gpspos
 WHERE 1=1
 AND timestamp between unixepoch('2023-07-01 07:13:32') and unixepoch('2023-07-05 22:13:32')
ORDER BY timestamp