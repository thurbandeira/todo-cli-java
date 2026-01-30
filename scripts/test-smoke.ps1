param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        [object]$Body = $null
    )
    if ($null -ne $Body) {
        return Invoke-RestMethod -Method $Method -Uri $Url -Headers $Headers -Body ($Body | ConvertTo-Json) -ContentType "application/json"
    }
    return Invoke-RestMethod -Method $Method -Uri $Url -Headers $Headers
}

function Assert-True {
    param(
        [bool]$Condition,
        [string]$Message
    )
    if (-not $Condition) {
        throw "Falhou: $Message"
    }
}

$suffix = [DateTime]::UtcNow.ToString("yyyyMMddHHmmss")
$username = "user$suffix"
$password = "secret123"

Write-Host "== Smoke Test: Auth =="
$register = Invoke-Api -Method "POST" -Url "$BaseUrl/api/auth/register" -Body @{
    username = $username
    password = $password
}
Assert-True ($null -ne $register.token) "Token nao retornado no registro."

$login = Invoke-Api -Method "POST" -Url "$BaseUrl/api/auth/login" -Body @{
    username = $username
    password = $password
}
$token = $login.token
Assert-True ($null -ne $token) "Token nao retornado no login."

$authHeaders = @{ Authorization = "Bearer $token" }

Write-Host "== Smoke Test: CRUD tasks =="
$task = Invoke-Api -Method "POST" -Url "$BaseUrl/api/tasks" -Headers $authHeaders -Body @{
    title = "Comprar leite"
}
Assert-True ($task.title -eq "Comprar leite") "Criacao de tarefa falhou."

$list = Invoke-Api -Method "GET" -Url "$BaseUrl/api/tasks" -Headers $authHeaders
Assert-True ($list.Count -ge 1) "Listagem de tarefas vazia."

$updated = Invoke-Api -Method "PUT" -Url "$BaseUrl/api/tasks/$($task.id)" -Headers $authHeaders -Body @{
    title = "Comprar leite e pao"
}
Assert-True ($updated.title -eq "Comprar leite e pao") "Edicao de tarefa falhou."

$completed = Invoke-Api -Method "POST" -Url "$BaseUrl/api/tasks/$($task.id)/complete" -Headers $authHeaders
Assert-True ($completed.completed -eq $true) "Conclusao de tarefa falhou."

$search = Invoke-Api -Method "GET" -Url "$BaseUrl/api/tasks/search?keyword=leite" -Headers $authHeaders
Assert-True ($search.Count -ge 1) "Busca por palavra-chave falhou."

$summary = Invoke-Api -Method "GET" -Url "$BaseUrl/api/tasks/summary" -Headers $authHeaders
Assert-True ($summary.total -ge 1) "Resumo nao retornado corretamente."

$paged = Invoke-Api -Method "GET" -Url "$BaseUrl/api/tasks/page?status=all&page=0&size=5&sort=id,asc" -Headers $authHeaders
Assert-True ($paged.items.Count -ge 1) "Paginacao nao retornou itens."

$searchPaged = Invoke-Api -Method "GET" -Url "$BaseUrl/api/tasks/search/page?keyword=leite&page=0&size=5&sort=id,asc" -Headers $authHeaders
Assert-True ($searchPaged.items.Count -ge 1) "Busca paginada falhou."

$cleared = Invoke-Api -Method "POST" -Url "$BaseUrl/api/tasks/clear-completed" -Headers $authHeaders
Assert-True ($cleared.done -eq 0) "Limpeza de concluidas falhou."

Invoke-Api -Method "DELETE" -Url "$BaseUrl/api/tasks/$($task.id)" -Headers $authHeaders | Out-Null

Write-Host "== Smoke Test: Refresh token =="
$refresh = Invoke-Api -Method "POST" -Url "$BaseUrl/api/auth/refresh" -Headers $authHeaders
Assert-True ($null -ne $refresh.token) "Refresh token falhou."

Write-Host "== Smoke Test: Validation =="
try {
    Invoke-Api -Method "POST" -Url "$BaseUrl/api/tasks" -Headers $authHeaders -Body @{ title = "" } | Out-Null
    throw "Falhou: validacao deveria rejeitar titulo vazio."
} catch {
    Write-Host "Validacao OK."
}

Write-Host "== Smoke Test: Done =="
