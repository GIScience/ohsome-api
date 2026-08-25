CREATE SECRET http_auth (
  TYPE http,
  EXTRA_HTTP_HEADERS MAP {
      'Authorization': getenv('OHSOME_API_KEY')
  }
);
SET force_download=true;

SET VARIABLE filter = 'building=* and geometry:polygon';
SET VARIABLE time = 'latest';
SET VARIABLE aoi = '8.68812,49.40390,8.72362,49.41582';
SET VARIABLE clip = 'true';

SELECT * FROM read_parquet(
    getenv('OHSOME_API_URL') || '/extraction/features.parquet'
    || '?filter=' || getvariable('filter')
    || '&time=' || getvariable('time')
    || '&aoi=' || getvariable('aoi')
    || '&clip=' || getvariable('clip')
);
