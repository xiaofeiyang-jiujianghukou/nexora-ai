# ============================================================
# Elasticsearch IK 分词器安装脚本 (PowerShell / Windows)
# ES 版本: 8.15.0
# ============================================================

param(
    [string]$ContainerName = "nexora-es",
    [string]$ESVersion = "8.15.0"
)

Write-Host "正在为 Elasticsearch $ESVersion 安装 IK 分词器..." -ForegroundColor Cyan

# 安装 IK 插件
docker exec -it $ContainerName elasticsearch-plugin install `
    "https://get.infini.cloud/elasticsearch/analysis-ik/$ESVersion"

if ($LASTEXITCODE -eq 0) {
    Write-Host "IK 分词器安装成功！重启 ES 容器..." -ForegroundColor Green
    docker restart $ContainerName
    Write-Host "等待 ES 启动..." -ForegroundColor Yellow
    Start-Sleep -Seconds 15
    Write-Host "验证 IK 分词器: " -ForegroundColor Cyan
    curl -s "http://localhost:9200/_cat/plugins" 2>$null | Select-String "ik"
    Write-Host "完成！" -ForegroundColor Green
} else {
    Write-Host "安装失败，请检查容器是否运行: docker ps | Select-String $ContainerName" -ForegroundColor Red
}
