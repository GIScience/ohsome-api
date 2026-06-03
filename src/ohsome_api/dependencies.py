from fastapi.security import APIKeyHeader

api_key_header_scheme = APIKeyHeader(name="authorization", scheme_name="API Key")
