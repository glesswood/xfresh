// com/xfresh/order/outbox/OutboxRepository.java
package com.xfresh.order.infrastructure.outbox;

import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findTop200ByStatusOrderByIdAsc(Integer status);
}