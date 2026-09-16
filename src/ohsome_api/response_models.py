"""Shared response models."""

from importlib.metadata import version

from pydantic import BaseModel, ConfigDict
from pydantic.alias_generators import to_camel

from ohsome_api.models import Attribution

VERSION = version("ohsome-api")


class BaseResponseModel(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)
    api_version: str = VERSION
    attribution: Attribution = Attribution()


class BaseError(BaseModel):
    # Structure mirrors errors by Tyk
    error: str
    type: str | None = None


class HTTPBadRequestError(BaseError):
    pass


class HTTPGatewayTimeoutError(BaseError):
    pass


class HTTPForbiddenError(BaseError):
    pass


class HTTPTooManyRequestsError(BaseError):
    pass


class HTTPUnauthorizedError(BaseError):
    pass
