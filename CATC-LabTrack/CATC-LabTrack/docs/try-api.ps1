# Run with the application already running. The next unused tag is unique per run.
$base = 'http://localhost:8080/api'
$credentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('user:user-demo'))
$headers = @{ Authorization = "Basic $credentials" }
$csrf = Invoke-RestMethod "$base/csrf" -Headers $headers -SessionVariable session
$headers[$csrf.header] = $csrf.token
$tag = 'EE-' + [guid]::NewGuid().ToString('N').Substring(0,8)
$payload = @{assetCode=$tag; name='Clamp meter'; location='Electrical Engineering Lab'; status='AVAILABLE'} | ConvertTo-Json
$item = Invoke-RestMethod "$base/equipment" -Method Post -ContentType 'application/json' -Body $payload -Headers $headers -WebSession $session
$item
Invoke-RestMethod "$base/equipment/$($item.id)" -Headers $headers -WebSession $session
Invoke-RestMethod "$base/equipment?location=Electrical" -Headers $headers -WebSession $session
