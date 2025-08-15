package com.xfresh.stock.repository;

import com.xfresh.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * Try-Lock：仅当可用库存充足时（total - locked >= qty）才增加 locked_stock
     * 返回值 > 0 表示成功。
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update Stock s
              set s.lockedStock = s.lockedStock + :qty,
                  s.updateTime  = :now
            where s.productId   = :pid
              and (s.totalStock - s.lockedStock) >= :qty
           """)
    int addLocked(@Param("pid") Long productId,
                @Param("qty") Integer qty,
                @Param("now") LocalDateTime now);

    /**
     * Confirm：确认扣减（total_stock -= qty, locked_stock -= qty）
     * 仅当 locked_stock >= qty 且 total_stock >= qty 时成功。
     */
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
                      @Param("qty") Integer qty,
                      @Param("now") LocalDateTime now);

    /**
     * Rollback：回滚锁定（locked_stock -= qty）
     * 仅当 locked_stock >= qty 时成功。
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update Stock s
              set s.lockedStock = s.lockedStock - :qty,
                  s.updateTime  = :now
            where s.productId   = :pid
              and s.lockedStock >= :qty
           """)
    int rollbackLock(@Param("pid") Long productId,
                     @Param("qty") Integer qty,
                     @Param("now") LocalDateTime now);
}