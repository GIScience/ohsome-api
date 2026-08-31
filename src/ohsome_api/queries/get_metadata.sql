WITH
		first AS (
				SELECT value::timestamptz as first_timestamp
				FROM ohsomedb_metadata
				WHERE key = 'valid_from'
		), last AS (
				SELECT last_timestamp
				FROM contributions_state
		)
SELECT first_timestamp as start, last_timestamp as end FROM first, last;
