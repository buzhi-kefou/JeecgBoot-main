# 销售订单财务信息同步设计

## 目标

调整销售订单 `queryByDate` 同步流程，使金蝶销售订单返回的一对一财务信息能够随销售订单一起解析并保存到 MySQL，同时提供对应建表脚本。

## 数据模型

- `ErpSalesOrderEntity.financeEntity` 表示销售订单与财务信息的一对一关联，不映射到销售订单表字段。
- `ErpSalesOrderFinanceEntity.entryId` 使用金蝶 `FSaleOrderFinance_fEntryId`，作为财务表主键。
- `ErpSalesOrderFinanceEntity.pid` 保存所属销售订单的 `fid`。
- 财务表对 `pid` 建唯一索引，从数据库层保证一张销售订单最多一条财务记录。
- 财务实体声明 `@TableName("erp_sales_order_finance")`。

## 接口解析

`ErpCommonEntity.getJsonPropertyNames` 已能递归收集单对象嵌套实体的查询字段，因此销售订单查询会自动带上财务字段。

扩展 `ErpRequestService` 的嵌套映射能力：

- 保留当前 `List<? extends ErpCommonEntity>` 嵌套实体解析行为。
- 新增 `ErpCommonEntity` 单对象嵌套实体识别和赋值。
- 同一行内属于财务实体的字段聚合成一个对象，并赋给 `financeEntity`。
- 没有任何财务字段时不创建空财务对象。

这项改动属于通用解析能力，后续其他一对一 ERP 关联实体可直接复用。

## 数据库存储

新增 `ErpSalesOrderFinanceEntityMapper`，使用 MyBatis-Plus `BaseMapper`。

销售订单 `queryByDate` 保持一次 ERP 请求和一个数据库事务。在事务内：

1. 按现有规则批量新增或更新销售订单。
2. 收集非空财务对象并将其 `pid` 设置为父订单 `fid`。
3. 批量查询已存在的财务主键。
4. 按 `entryId` 分为新增与更新集合，分别批量写入。

当接口未返回财务对象时，不删除已有财务记录，避免接口字段缺失或临时空值导致历史数据丢失。请求中缺少 `entryId` 的财务对象不落库，因为无法进行可靠幂等更新。

## 事务与异常

销售订单和财务信息使用现有 `TransactionTemplate` 包裹，在同一事务中保存。解析、查询或写入异常继续向上抛出，由事务统一回滚，不吞掉异常。

## MySQL 脚本

在 `jeecg-boot/db/erp_sales_order_finance_table.sql` 新增建表语句：

- `entry_id BIGINT` 主键。
- `pid VARCHAR(64)` 非空并建立唯一索引。
- 金额与比例字段使用 `DECIMAL(19,6)`，布尔字段使用 `TINYINT(1)`。
- 文本字段按内容采用 `VARCHAR(100)`、`VARCHAR(255)` 或 `VARCHAR(500)`。

不创建数据库外键，保持与当前 ERP 同步表风格一致，避免批量同步时增加跨表约束成本；订单关联由 `pid` 唯一索引和事务写入保证。

## 测试

采用测试优先方式覆盖：

- `ErpRequestService` 能将销售订单行中的财务字段解析为单个 `financeEntity`。
- 销售订单同步时正确回填 `pid`。
- 新财务记录进入批量新增，已有记录进入批量更新。
- 订单与财务写入均发生在同一事务范围内。
- 空财务对象以及缺少 `entryId` 的财务对象不会写入数据库。

验证包括目标 JUnit 测试、模块测试编译，以及相关文件的 `git diff --check`。
