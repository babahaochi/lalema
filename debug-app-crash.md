# Debug Session: app-crash

## Status
[OPEN]

## Update (2026-09-01)
- `ApiClient` 已从"全信任 SSL"改为系统默认 CA 信任链 + 标准 hostname 校验，`BASE_URL` 由 IP
  `https://47.109.151.2/api/` 改为域名 `https://5ichat.online/api/`（与 network_security_config 一致）。
- 因此原假设 #1（SSL 验证失败导致闪退）在后端为 CA 签发证书时不再成立；三处闪退更可能源于
  Gson 解析异常 / 主线程网络调用 / 前后端 DTO 字段不一致。仍需真机 logcat 才能定位。

## Symptoms
- 点击注册按钮闪退
- 点击好友功能闪退
- 给AI助手发送消息闪退

## Environment
- Android App (Release Build)
- Backend: https://5ichat.online/api/
- SSL: 自签名证书

## Hypotheses
1. **SSL证书验证失败** - 自签名证书导致HTTPS握手失败（⚠️ 已缓解：ApiClient 已改为系统默认 CA 信任链 + 标准 hostname 校验；若后端确为 CA 签发证书则不再触发，若仍为自签证书则需在 network_security_config 配置证书固定/pinning）
2. **Retrofit baseUrl格式问题** - 缺少尾部斜杠或协议不匹配
3. **EncryptedSharedPreferences初始化失败** - 某些设备上MasterKey创建失败
4. **网络请求在主线程执行** - 协程异常处理不当导致崩溃
5. **API响应解析失败** - 后端返回格式与前端预期不符

## Evidence
- 等待收集...

## Fix
- 等待确认...
