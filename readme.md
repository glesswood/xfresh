# xfresh - 社区生鲜即时配送平台

基于 Spring Cloud Alibaba 的微服务项目，模拟生鲜电商的下单、库存、支付全链路业务，覆盖高并发、异步解耦、消息一致性等场景。

## 项目结构
- **order-service**：订单服务，支持下单、支付、超时取消（延时队列）
- **stock-service**：库存服务，基于 Redis+Lua 实现库存扣减/回滚
- **product-service**：商品服务，管理商品信息与库存同步
- **gateway-service**：Spring Cloud Gateway，统一入口与鉴权
- **nacos**：服务发现与配置中心
- **rabbitmq**：异步消息队列，确保订单-库存解耦

## 技术栈
- Spring Boot 3.x / Spring Cloud Alibaba
- Nacos / RabbitMQ / Redis
- JPA / MySQL
- OpenTelemetry / Zipkin / Micrometer
- Docker Compose（本地一键启动）

## 功能亮点
- 下单 → 锁库存 → 支付 → 成功 / 超时回滚
- Outbox + 死信队列，保证消息最终一致性
- Redis 分布式库存原子扣减，避免超卖
- 高并发压测下 P95 延迟优化至 14ms（QPS≈800）
- 全链路追踪与指标监控

## 快速开始
```bash
git clone https://github.com/glesswood/xfresh.git
cd xfresh
docker-compose up -d  # 启动 Nacos + RabbitMQ + Redis + MySQL