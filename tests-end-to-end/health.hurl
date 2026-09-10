HEAD {{BASE_URL}}/health
HTTP *
[Asserts]
status == 200

GET {{BASE_URL}}/health
HTTP *
[Asserts]
status == 200
