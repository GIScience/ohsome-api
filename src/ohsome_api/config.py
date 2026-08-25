from pathlib import Path

from pydantic import BaseModel, FilePath, computed_field
from pydantic_settings import BaseSettings, SettingsConfigDict

# NOTE: Logging is configured in `ohsome_api/__init__.py


class DatabaseConfig(BaseModel):
    user: str = "ohsomedb"
    password: str = "ohsomedb"  # noqa: S105
    host: str = "localhost"
    port: int = 5432
    dbname: str = "ohsomedb"
    schemaname: str = "current"
    application_name: str = "ohsome-api"
    pool_min_size_stats: int = 1
    pool_max_size_stats: int = 10
    pool_min_size_extraction: int = 0
    pool_max_size_extraction: int = 10
    timeout_stats: int = 60 * 3
    timeout_extraction: int = 60 * 3

    @computed_field
    @property
    def connection_string(self) -> str:
        return "postgresql://{user}:{password}@{host}:{port}/{dbname}?application_name={application_name}".format(
            user=self.user,
            password=self.password,
            host=self.host,
            port=self.port,
            dbname=self.dbname,
            application_name=self.application_name,
        )


class Config(BaseSettings):
    ohsomedb: DatabaseConfig = DatabaseConfig()
    log_config: FilePath = Path(Path(__file__).parent / "log_config.yaml").resolve()
    root_path: str = ""
    swagger_js_url: str = (
        "https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui-bundle.js"
    )
    swagger_css_url: str = (
        "https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui.css"
    )
    external_docs_url: str = "https://docs.ohsome.org/ohsome-api/v2"
    time_series_size_limit: int = 1_000  # rows
    group_by_time_series_size_limit: int = 1_000_000  # rows
    model_config = SettingsConfigDict(
        env_prefix="OHSOME_API_",
        env_nested_delimiter="_",
    )


CONFIG = Config()
