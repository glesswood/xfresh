package com.xfresh.stock.repository;

import com.xfresh.stock.entity.Stock;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockRepository extends JpaRepository<Stock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.productId = :pid")
    Stock forUpdate(@Param("pid") Long productId);


    @Modifying(clearAutomatically = true)          // UPDATE/DELETE 必须
    @Query("""
           update Stock s
              set s.lockedStock = s.lockedStock + :qty ,
                  s.updateTime  = CURRENT_TIMESTAMP
            where s.productId   = :pid
           """)
    int addLocked(@Param("pid") Long pid,
                  @Param("qty") Integer qty);
    // 确认：total_stock -= :qty, locked_stock -= :qty
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update Stock s
       set s.totalStock  = s.totalStock  - :qty,
           s.lockedStock = s.lockedStock - :qty,
           s.updateTime  = :now
     where s.productId   = :pid
       and s.lockedStock >= :qty
       and s.totalStock  >= :qty
    """)
    int confirmDeduct(@Param("pid") Long productId,
                      @Param("qty") int qty,
                      @Param("now") java.time.LocalDateTime now);
}