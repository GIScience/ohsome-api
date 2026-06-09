from fastapi import APIRouter
from fastapi.openapi.docs import (
    get_swagger_ui_html,
)
from fastapi.responses import HTMLResponse

from ohsome_api.config import CONFIG

router = APIRouter()


@router.get("/docs", include_in_schema=False)
async def custom_swagger_ui_html() -> HTMLResponse:
    return get_swagger_ui_html(
        openapi_url="/openapi.json",
        title="ohsome-api - Swagger UI",
        oauth2_redirect_url=None,
        swagger_js_url=f"{CONFIG.url_path_prefix}/static/swagger-ui-bundle.js",
        swagger_css_url=f"{CONFIG.url_path_prefix}/static/swagger-ui.css",
    )
