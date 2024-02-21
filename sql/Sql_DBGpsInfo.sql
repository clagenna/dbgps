USE DBGpsFoto
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE dbo.GpsFile(
	id int IDENTITY(1,1) NOT NULL,
	filedir nvarchar(512) NOT NULL,
	filename nvarchar(128) NOT NULL,
	modtime datetime NULL,
 CONSTRAINT PK_GpsFile PRIMARY KEY CLUSTERED  ( id ASC )
    WITH ( 
    	PAD_INDEX = OFF, 
    	STATISTICS_NORECOMPUTE = OFF, 
    	IGNORE_DUP_KEY = OFF, 
    	ALLOW_ROW_LOCKS = ON, 
    	ALLOW_PAGE_LOCKS = ON) 
) 
GO


SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.GPSInfo(
	id int IDENTITY(1,1) NOT NULL,
	timestamp datetime NOT NULL,
	longitude float NULL,
	latitude float NULL,
	altitude int NULL,
	source varchar(64) NOT NULL,
	idfile int NULL,
 CONSTRAINT PK_GPSInfo PRIMARY KEY CLUSTERED  ( id ASC )
 WITH (
 	PAD_INDEX = OFF, 
 	STATISTICS_NORECOMPUTE = OFF, 
 	IGNORE_DUP_KEY = OFF, 
 	ALLOW_ROW_LOCKS = ON, 
 	ALLOW_PAGE_LOCKS = ON) 
) 
GO

ALTER TABLE dbo.GPSInfo  WITH CHECK 
	ADD  CONSTRAINT FK_GPSInfo_GpsFile 
		FOREIGN KEY(idfile)
		REFERENCES dbo.GpsFile (id)
GO

ALTER TABLE dbo.GPSInfo CHECK CONSTRAINT FK_GPSInfo_GpsFile
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO


CREATE PROCEDURE dbo.addGpsInfo
			@timestamp 	datetime ,
			@latitude 	float ,
			@longitude	float ,
			@distprec 	float ,
			@altitude 	int ,
			@source 	varchar(64) ,
			@info 		varchar(64) ,
			@filedir 	varchar(512) ,
			@filenam	varchar(128) 
AS 
BEGIN
	SET NOCOUNT ON
	DECLARE @lid int
	
	SELECT @lid = id
	  FROM dbo.GPSInfo
	  WHERE latitude = @latitude
	    AND longitude = @longitude
		AND source = @source
		AND timestamp = @timestamp

	IF @lid IS NOT NULL
		RETURN

BEGIN TRAN
	INSERT INTO dbo.GPSInfo
			   (timestamp
			   ,latitude
			   ,longitude
			   ,altitude
			   ,source
			   )
		 VALUES
			   (@timestamp 
			   ,@latitude
			   ,@longitude
			   ,@altitude
			   ,@source
			)
	SET @lid = SCOPE_IDENTITY()

	IF @filedir IS NOT NULL
		INSERT INTO dbo.GpsFile
			   (idGpsInfo
			   ,filedir
			   ,filename
			   ,modtime)
		 VALUES
			   (@lid ,@filedir, @filenam, @timestamp)
COMMIT TRAN

END
GO
