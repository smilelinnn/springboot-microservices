#!/bin/bash

echo "启动Kafka服务..."

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "错误: Docker未运行，请先启动Docker"
    exit 1
fi

# 停止并删除现有容器
docker stop zookeeper kafka 2>/dev/null || true
docker rm zookeeper kafka 2>/dev/null || true

# 创建Kafka网络
docker network create kafka-network 2>/dev/null || true

# 启动Zookeeper
echo "启动Zookeeper..."
docker run -d \
    --name zookeeper \
    --network kafka-network \
    -p 2181:2181 \
    -e ZOOKEEPER_CLIENT_PORT=2181 \
    confluentinc/cp-zookeeper:latest

# 等待Zookeeper启动
echo "等待Zookeeper启动..."
sleep 10

# 启动Kafka
echo "启动Kafka..."
docker run -d \
    --name kafka \
    --network kafka-network \
    -p 9092:9092 \
    -e KAFKA_BROKER_ID=1 \
    -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
    -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
    -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
    confluentinc/cp-kafka:latest

# 等待Kafka启动
echo "等待Kafka启动..."
sleep 15

# 创建主题
echo "创建Kafka主题..."
docker exec kafka kafka-topics --create --topic employee-events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 2>/dev/null || true
docker exec kafka kafka-topics --create --topic department-events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 2>/dev/null || true
docker exec kafka kafka-topics --create --topic notifications --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 2>/dev/null || true

echo "Kafka服务启动完成！"
echo "Zookeeper: localhost:2181"
echo "Kafka: localhost:9092"
echo ""
echo "可用主题:"
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
