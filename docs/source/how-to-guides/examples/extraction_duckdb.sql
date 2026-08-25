---------------------------------------------------------
-- Load spatial extension, API key and set HTTP header
---------------------------------------------------------
INSTALL spatial;
LOAD spatial;

CREATE SECRET http_auth (
  TYPE http,
  EXTRA_HTTP_HEADERS MAP {
      'Authorization': getenv('OHSOME_API_KEY')
  }
);
SET force_download=true;


---------------------------------------------------------
-- Extraction: Features Single Snapshot Timestamp
---------------------------------------------------------
SET VARIABLE filter = 'building=* and geometry:polygon';
SET VARIABLE time = 'latest';
SET VARIABLE aoi = '8.68812,49.40390,8.72362,49.41582';  -- bbox Heidelberg
SET VARIABLE clip = 'false';

CREATE TABLE ohsome_features AS
SELECT * FROM read_parquet(
    getenv('OHSOME_API_URL') || '/extraction/features.parquet'
    || '?filter=' || getvariable('filter')
    || '&time=' || getvariable('time')
    || '&aoi=' || getvariable('aoi')
    || '&clip=' || getvariable('clip')
);

SELECT count(*) FROM ohsome_features;

COPY ohsome_features TO 'ohsome_features.parquet';


---------------------------------------------------------
-- Extraction: Contributions Time Range
---------------------------------------------------------
SET VARIABLE filter = 'highway=* and geometry:line';
SET VARIABLE time = '2026-08-24/2026-08-25';
SET VARIABLE aoi = '5.98865807458, 47.3024876979, 15.0169958839, 54.983104153';  -- bbox Germany

CREATE TABLE ohsome_contributions AS
SELECT * FROM read_parquet(
    getenv('OHSOME_API_URL') || '/extraction/contributions.parquet'
    || '?filter=' || getvariable('filter')
    || '&time=' || getvariable('time')
    || '&aoi=' || getvariable('aoi')
WHERE map_extract_value(changeset_tags, 'created_by').prefix('StreetComplete')
);

SELECT count(*) FROM ohsome_contributions;

COPY ohsome_contributions TO 'ohsome_contributions.parquet';
