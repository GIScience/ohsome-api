from fastapi.security import APIKeyHeader

api_key_header_scheme = APIKeyHeader(
    name="authorization",
    scheme_name="APIKey",
    description=(
        "Please enter your "
        '<a href="https://account.heigit.org/" target="_blank">API key</a>.'
        "<br><br>"
    ),
    auto_error=False,
)
