<p align="center">
    <img src=https://img.qimuu.icu/typory/logo.gif width=188/>
</p>
<h1 align="center">LKJAPI 接口开放平台</h1>
<p align="center"><strong>LKJAPI 接口开放平台是一个为用户和开发者提供全面API接口调用服务的平台 🛠</strong></p>

## 项目背景 📋

&emsp;&emsp;我的初衷是尽可能帮助和服务更多的用户和开发者，让他们更加方便快捷的获取他们想要的信息和功能。
接口平台可以帮助开发者快速接入一些常用的服务，从而提高他们的开发效率，比如随机头像，随机壁纸，随机动漫图片(二次元爱好者专用)等服务，他们是一些应用或者小程序常见的功能，所以提供这些接口可以帮助开发者更加方便地实现这些功能。这些接口也可以让用户在使用应用时获得更加全面的功能和服务，从而提高他们的用户体验


## 项目介绍 🙋
**😀 作为用户您可以通过注册登录账户，获取接口调用权限，并根据自己的需求浏览和选择适合的接口。您可以在线进行接口调试，快速验证接口的功能和效果。**

**💻 作为开发者 我们提供了[客户端SDK: LKJAPI-SDK](xxx)，即可将轻松集成接口到您的项目中，实现更高效的开发和调用。**

**🏁 无论您是用户还是开发者，LKJAPI 接口开放平台都致力于提供稳定、安全、高效的接口调用服务，帮助您实现更快速、便捷的开发和调用体验。**
## 网站导航 🧭
-  **[LKJAPI 接口开放平台 🔗](https://github.com/LKJ-np/LKJAPI)**
- [**LKJAPI 后端 🏘️**](https://github.com/LKJ-np/LKJAPI)
- [**LKJAPI 前端 🏘**️](XXX)
-  **[LKJAPI-SDK](XXX)** 🛠

## 目录结构 📑
| 目录                                                                                                                                                        | 描述           |
|-----------------------------------------------------------------------------------------------------------------------------------------------------------|--------------|
| **🏘️ [LKJAPI-backend](./LKJAPI-backend)**                                                                                                                | LKJAPI后端服务模块 |
| **🏘️ [LKJAPI-common](./LKJAPI-common)**                                                                                                                  | 公共服务模块       |
| **🕸️ [LKJAPI-Gateway](./LKJAPI-Gateway)**                                                                                                                | 网关模块         |
| **🔗 [LKJAPI-interface](./LKJAPI-interface)**                                                                                                             | 接口模块         |
| **🛠 [LKJAPI-client-sdk](LKJAPI-client-sdk)**                                                                                                                           | 开发者调用sdk     |  |
| **✔️ [LKJAPI-thrid-party](LKJAPI-third-party)** | 第三方服务        |

## 项目架构 🗺

![LKJAPI 接口开放平台](./img/项目架构.png)

## 项目流程 🗺️

![LKJAPI 接口开放平台](xxx)


## 快速启动 🚀
### 前端

环境要求：Node.js >= 16

安装依赖：

```bash
yarn or  npm install
```

启动：

```bash
yarn run dev or npm run start:dev
```

部署：

```bash
yarn build or npm run build
```

### 后端

数据库:sql目录下

## 项目选型 🎯

### **后端**

- Spring Boot 2.7.0
- Spring MVC
- MySQL 数据库
- Dubbo 分布式（RPC、Nacos）
- Spring Cloud Gateway 微服务网关
- API 签名认证（Http 调用）
- Swagger + Knife4j 接口文档
- Spring Boot Starter（SDK 开发）
- Jakarta.Mail 邮箱通知、验证码
- Spring Session Redis 分布式登录
- Apache Commons Lang3 工具类
- MyBatis-Plus 及 MyBatis X 自动生成
- Hutool、Apache Common Utils、Gson 等工具库

### 前端

- React 18

- Ant Design Pro 5.x 脚手架

- Ant Design & Procomponents 组件库

- Umi 4 前端框架

- OpenAPI 前端代码生成

## 功能展示 ✨
### 登录页

![index](./img/登录注册.png)

### 接口列表页

![interfaceSquare](./img/接口列表.png)


### 接口详情页

![interfaceSquare](./img/接口文档.png)

#### **在线API**

![interfaceSquare](./img/在线调用API.png)

#### **订单页（待支付/已支付/已过期）**

![interfaceSquare](./img/接口订单页1.png)
![interfaceSquare](./img/接口订单页2.png)
![interfaceSquare](./img/接口订单页3.png)


#### **我的接口**

![interfaceSquare](./img/已拥有的接口.png)

#### **管理页（用户管理/接口管理/接口统计）**

![interfaceSquare](./img/用户管理.png)
![interfaceSquare](./img/接口管理.png)
![interfaceSquare](./img/接口调用次数与购买次数统计.png)

#### **个人中心**

![interfaceSquare](./img/个人中心.png)

## 欢迎贡献

项目需要大家的支持，期待更多小伙伴的贡献，你可以：

- 对于项目中的Bug和建议，能够在Issues区提出建议，我会积极响应





