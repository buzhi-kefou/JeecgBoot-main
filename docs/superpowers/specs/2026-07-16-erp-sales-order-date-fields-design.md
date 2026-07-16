# ERP 销售订单日期字段类型调整设计

## 背景

`ErpSalesOrderEntity` 中明确表示日期时间的字段目前均声明为 `String`。金蝶接口实际返回 ISO 本地日期时间，例如 `2022-09-03T10:59:38.07`。字符串类型会让数据库字段类型、排序筛选以及 Java 侧日期运算失去类型约束。

## 调整范围

以下 9 个字段改为 `LocalDateTime`：

- `date`（`FDate`）
- `createDate`（`FCreateDate`）
- `modifyDate`（`FModifyDate`）
- `approveDate`（`FApproveDate`）
- `closeDate`（`FCloseDate`）
- `cancelDate`（`FCancelDate`）
- `changeDate`（`FChangeDate`）
- `zghyJsrq`（`F_ZGHY_JSRQ`）
- `antiCloseDate`（`FAntiCloseDate`）

`zghyZyqx`（装运期限）继续使用 `String`，因为该字段语义可能是期限描述而非确定时间点。

## 转换方案

在 `ErpRequestService` 的通用字段归一化逻辑中增加 `LocalDateTime` 转换，使所有 ERP 实体共用同一处理方式。使用 `DateTimeFormatter.ISO_LOCAL_DATE_TIME` 解析 `T` 分隔的本地日期时间；该格式支持无小数秒以及可变位数的小数秒，包括示例中的两位小数秒。

空字符串沿用现有规则转换为 `null`。非法日期值抛出包含 ERP 字段名、目标 Java 字段名和原始值的异常，防止错误数据被静默写入。

## 数据库变更

MySQL 表 `erp_sales_order` 中对应的 9 列改为 `DATETIME(3) NULL`。毫秒精度可以完整容纳接口的小数秒，同时兼容没有小数秒的值。脚本使用 `ALTER TABLE ... MODIFY COLUMN`，不负责清洗已有的非日期字符串；执行前应确认现有列值均为空或可转换为日期时间。

## 测试与验证

- 为 `ErpRequestService` 增加解析两位小数秒的测试，断言得到正确的 `LocalDateTime`。
- 覆盖空日期字符串转换为 `null`。
- 覆盖非法日期字符串失败并包含字段上下文。
- 更新销售订单相关测试中受字段类型变化影响的构造或断言。
- 运行目标单元测试、后端生产代码编译和测试代码编译。
