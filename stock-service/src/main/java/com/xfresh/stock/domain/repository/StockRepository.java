package com.xfresh.stock.domain.repository;

import com.xfresh.stock.domain.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface StockRepository extends JpaRepository<Stock, Long> {

    // TryLock：仅当 可用库存 >= qty 时，原子地增加 locked_stock
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update Stock s
         set s.lockedStock = s.lockedStock + :qty,
             s.updateTime  = :now
       where s.productId   = :pid
         and (s.totalStock - s.lockedStock) >= :qty
    """)
    int addLocked(@Param("pid") Long productId,
                @Param("qty") int qty,
                @Param("now") java.time.LocalDateTime now);

    // Confirm：仅当 locked_stock 与 total_stock 都足够时才扣减
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

    // Rollback：释放锁定，不得为负
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update Stock s
         set s.lockedStock = s.lockedStock - :qty,
             s.updateTime  = :now
       where s.productId   = :pid
         and s.lockedStock >= :qty
    """)
    int rollbackLock(@Param("pid") Long productId,
                     @Param("qty") int qty,
                     @Param("now") java.time.LocalDateTime now);
}