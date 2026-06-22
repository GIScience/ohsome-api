from fastapi import APIRouter, Request
from fastapi.openapi.docs import (
    get_swagger_ui_html,
)
from fastapi.responses import HTMLResponse

router = APIRouter()


@router.get("/docs", include_in_schema=False)
async def custom_swagger_ui_html(request: Request) -> HTMLResponse:
    # root_path = request.scope.get("root_path")
    root_path = ""
    return get_swagger_ui_html(
        openapi_url=f"{root_path}/openapi.json",
        title="ohsome-api - Swagger UI",
        oauth2_redirect_url=None,
        swagger_js_url=f"{root_path}/static/swagger-ui-bundle.js",
        swagger_css_url=f"{root_path}/static/swagger-ui.css",
    )
