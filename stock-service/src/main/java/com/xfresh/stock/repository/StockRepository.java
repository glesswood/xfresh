package com.xfresh.stock.repository;

import com.xfresh.stock.entity.Stock;
import feign.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface StockRepository extends JpaRepository<Stock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.productId = :pid")
    Stock forUpdate(@Param("pid") Long productId);

    //void addLocked(Long productId, Integer quantity);
    /**
     *  locked_stock = locked_stock + :delta
     *  同时更新时间，成功返回受影响行数（1 = 成功，0 = 记录不存在）
     */
    /** locked_stock = locked_stock + :qty */
    @Modifying(clearAutomatically = true)          // UPDATE/DELETE 必须
    @Query("""
           update Stock s
              set s.lockedStock = s.lockedStock + :qty ,
                  s.updateTime  = CURRENT_TIMESTAMP
            where s.productId   = :pid
           """)
    int addLocked(@Param("pid") Long pid,
                  @Param("qty") Integer qty);
}