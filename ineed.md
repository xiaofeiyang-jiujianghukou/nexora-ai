  # 安装 ES IK 分词器
  .\deploy\install-es-ik.ps1

  # 确认 RocketMQ 在跑
  docker ps | Select-String nexora-rmq

  # 启动后端（Flyway 会自动插入 RSS 源）
  cd backend && mvn spring-boot:run -pl nexora-app "-Dspring-boot.run.profiles=dev"
