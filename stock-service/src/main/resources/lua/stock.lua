-- 返回码:
--  0  => 扣减成功，返回剩余库存
-- -1  => 库存不足
-- -2  => Key 不存在（缓存 miss，需要回源 DB）

local current = redis.call('GET', KEYS[1])
if not current then
  return -2
end

local stock  = tonumber(current)
local delta  = tonumber(ARGV[1])
if stock + delta < 0 then
  return -1
end

stock = stock + delta
redis.call('SET', KEYS[1], stock)
return stock