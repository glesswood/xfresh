package com.xfresh.stock.mq;
public interface ProcessedEventService {
    boolean tryMarkProcessing(String eventId);  // SETNX processed:{id} = PROCESSING, TTL
    void markSuccess(String eventId);          // 覆盖为 DONE，延长 TTL 或永久
    void markFailed(String eventId, Throwable ex); // 记录失败原因、计数
}