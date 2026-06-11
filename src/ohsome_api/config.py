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

    model_config = SettingsConfigDict(
        env_prefix="OHSOME_API_",
        env_nested_delimiter="_",
    )


CONFIG = Config()
