-- KEYS[1] = "stock:商品ID"
-- ARGV[1] = 要扣(负数) / 回滚(正数) 的数量
local stock  = tonumber(redis.call('GET', KEYS[1]) or '0')
local delta  = tonumber(ARGV[1])
if stock + delta < 0 then
  return -1   -- 库存不足
end
stock = stock + delta
redis.call('SET', KEYS[1], stock)
return stock