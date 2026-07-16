---
name: java-rebuild-rule
description: 修改 Java 代码后必须先 mvn clean install，无异常再启动
metadata:
  type: feedback
---

修改任何 Java 源代码后，必须先执行 `mvn clean install -DskipTests`（从 `backend/` 根目录），确保所有模块编译通过且安装到本地 Maven 仓库。

- 有编译异常 → 解决异常，重新 install，直到无异常
- 无异常 → 才能启动 `mvn spring-boot:run`

**Why:** Maven 多模块项目中，`nexora-app` 依赖其他模块（nexora-module-ai, nexora-module-news 等），如果只编译不 install，app 模块启动时会使用本地仓库中的旧版本 jar，新代码不生效。

**How to apply:** 每次修改 Java 文件后，先 `cd backend && mvn clean install -DskipTests`，确认 BUILD SUCCESS，再启动。
