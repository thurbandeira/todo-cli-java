param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Username = "",
    [string]$Password = "secret123"
)

$ErrorActionPreference = "Stop"
$BaseUrl = $BaseUrl.TrimEnd("/")

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

if ([string]::IsNullOrWhiteSpace($Username)) {
    $suffix = [DateTime]::UtcNow.ToString("yyyyMMddHHmmss")
    $Username = "demo$suffix"
}

Write-Host "== Seed Demo =="
Write-Host "Usuario: $Username"

try {
    $register = Invoke-Api -Method "POST" -Url "$BaseUrl/api/auth/register" -Body @{
        username = $Username
        password = $Password
    }
    $token = $register.token
} catch {
    $login = Invoke-Api -Method "POST" -Url "$BaseUrl/api/auth/login" -Body @{
        username = $Username
        password = $Password
    }
    $token = $login.token
}

$headers = @{ Authorization = "Bearer $token" }

$seed = @(
    @{ title = "Planejar semana"; done = $true },
    @{ title = "Revisar backlog"; done = $false },
    @{ title = "Testar API"; done = $true },
    @{ title = "Montar dashboard"; done = $false },
    @{ title = "Preparar portfolio"; done = $false },
    @{ title = "Limpar tarefas antigas"; done = $true },
    @{ title = "Criar nova funcionalidade"; done = $false }
)

foreach ($item in $seed) {
    $task = Invoke-Api -Method "POST" -Url "$BaseUrl/api/tasks" -Headers $headers -Body @{
        title = $item.title
    }
    if ($item.done) {
        Invoke-Api -Method "POST" -Url "$BaseUrl/api/tasks/$($task.id)/complete" -Headers $headers | Out-Null
    }
}

Write-Host "Seed concluido. Abra: $BaseUrl"
Write-Host "Login: $Username"
Write-Host "Senha: $Password"
