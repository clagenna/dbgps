# dbgps
A JavaFX application that (tries) to manage and interpolate GPS info in Photo files by reading the EXIF tags. It reads :

- Foto Files, 
- Track files in GPX format, 
- Google Maps Timeline Tracking info

is also able to save tuples info on Data Base (for now SQLite 3, SQL Server). You can modify info on photos and save new added coordinates to file. You can rename files names of photos using EXIF date of acquiring.


# Usage
## click on rows
On each row of list view of photos you can:

1. *Ctrl-click* to open GPS coordinates on predefined browser with **Google Maps**
2. *Dbl-Click* to show underlying photo
3. *Right-Click* to show context menu  