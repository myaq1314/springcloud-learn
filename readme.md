# springCloud全家桶demo

## 使用组件
* OpenFeign10.x
* nacos-config、discovery
* sentinel
* spring cloud gateway
* spring boot
* influxdb
* grafana监控

## 工厂结构
``` 
springcloud-demo
├── sc-gateway                                  -- 网关模块
├    ├── sc-gateway-common
├    ├── sc-gateway-dubbo                       -- dubbo转发适配模块
├    ├    ├── gateway-dubbo-client              -- 网关dubbo适配客户端sdk
├    ├    ├── gateway-dubbo-common
├    ├    ├── gateway-dubbo-core
├    ├    └── gateway-dubbo-springboot-starter
├    └── sc-gateway-new                         -- api网关服务
├── sc-server                                   -- 后端微服务模块
├    ├── sc-service-api
├    └── sc-service-api-common
├── sc-common                                   -- 通用公共模块
└── sc-ops-union                                -- 中间件管理平台
```
