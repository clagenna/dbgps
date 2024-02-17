USE DBGpsFoto
GO

/****** Object:  StoredProcedure dbo.addGpsInfo    Script Date: 17/02/2024 15:24:25 ******/
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


