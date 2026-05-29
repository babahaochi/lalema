# Debug Session: app-crash

## Status
[OPEN]

## Symptoms
- 点击注册按钮闪退
- 点击好友功能闪退
- 给AI助手发送消息闪退

## Environment
- Android App (Release Build)
- Backend: https://5ichat.online/api/
- SSL: 自签名证书

## Hypotheses
1. **SSL证书验证失败** - 自签名证书导致HTTPS握手失败，抛出异常
2. **Retrofit baseUrl格式问题** - 缺少尾部斜杠或协议不匹配
3. **EncryptedSharedPreferences初始化失败** - 某些设备上MasterKey创建失败
4. **网络请求在主线程执行** - 协程异常处理不当导致崩溃
5. **API响应解析失败** - 后端返回格式与前端预期不符

## Evidence
- 等待收集...

## Fix
- 等待确认...
