$token = "rnd_YvepfYe5CIlvFjsauwMFh3yuITgV"
$headers = @{
    "Authorization" = "Bearer $token"
    "Accept" = "application/json"
    "Content-Type" = "application/json"
}

$json = @"
{
  "type": "web_service",
  "autoDeploy": "yes",
  "branch": "main",
  "name": "medvit-backend",
  "ownerId": "tea-d9d49057vvec73erfkig",
  "region": "singapore",
  "repo": "https://github.com/RahulReddy3069/MedVit-Backend",
  "plan": "free",
  "serviceDetails": {
    "env": "docker",
    "pullRequestPreviewsEnabled": "no",
    "dockerfilePath": "./Dockerfile",
    "dockerContext": "."
  },
  "envVars": [
    { "key": "JWT_SECRET", "value": "medvitals-super-secret-2024" },
    { "key": "FRONTEND_URL", "value": "https://med-vit.vercel.app" },
    { "key": "PORT", "value": "8080" }
  ]
}
"@

Write-Host "Creating Render web service..."
try {
    $service = Invoke-RestMethod -Uri "https://api.render.com/v1/services" -Method POST -Headers $headers -Body $json
    Write-Host "SUCCESS!"
    Write-Host "Service ID: $($service.service.id)"
    Write-Host "Service Name: $($service.service.name)"
    Write-Host "URL: $($service.service.serviceDetails.url)"
} catch {
    $err = $_.ErrorDetails.Message
    Write-Host "ERROR: $err"
}
