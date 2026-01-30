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

$suffix = [DateTime]::UtcNow.ToString("yyyyMMddHHmmss")
$username = "user$suffix"
$password = "secret123"

Write-Host "== Register =="
$register = Invoke-Api -Method "POST" -Url "$BaseUrl/api/auth/register" -Body @{
    username = $username
    password = $password
}
Write-Host "Token recebido."

Write-Host "== Login =="
$login = Invoke-Api -Method "POST" -Url "$BaseUrl/api/auth/login" -Body @{
    username = $username
    password = $password
}
$token = $login.token
Write-Host "Token de login recebido."

$authHeaders = @{ Authorization = "Bearer $token" }

Write-Host "== Create task =="
$task = Invoke-Api -Method "POST" -Url "$BaseUrl/api/tasks" -Headers $authHeaders -Body @{
    title = "Comprar leite"
}
Write-Host "Criada tarefa ID: $($task.id)"

Write-Host "== List tasks =="
$list = Invoke-Api -Method "GET" -Url "$BaseUrl/api/tasks" -Headers $authHeaders
Write-Host "Total tarefas: $($list.Count)"

Write-Host "== Complete task =="
$completed = Invoke-Api -Method "POST" -Url "$BaseUrl/api/tasks/$($task.id)/complete" -Headers $authHeaders
Write-Host "Concluida: $($completed.completed)"

Write-Host "== Search tasks =="
$search = Invoke-Api -Method "GET" -Url "$BaseUrl/api/tasks/search?keyword=leite" -Headers $authHeaders
Write-Host "Encontradas: $($search.Count)"

Write-Host "== Summary =="
$summary = Invoke-Api -Method "GET" -Url "$BaseUrl/api/tasks/summary" -Headers $authHeaders
Write-Host "Resumo: total=$($summary.total) pendentes=$($summary.pending) concluidas=$($summary.done)"

Write-Host "== Clear completed =="
$cleared = Invoke-Api -Method "POST" -Url "$BaseUrl/api/tasks/clear-completed" -Headers $authHeaders
Write-Host "Resumo apos limpar: total=$($cleared.total) pendentes=$($cleared.pending) concluidas=$($cleared.done)"

Write-Host "== Remove task =="
Invoke-Api -Method "DELETE" -Url "$BaseUrl/api/tasks/$($task.id)" -Headers $authHeaders | Out-Null
Write-Host "Removida tarefa ID: $($task.id)"

Write-Host "== Done =="
