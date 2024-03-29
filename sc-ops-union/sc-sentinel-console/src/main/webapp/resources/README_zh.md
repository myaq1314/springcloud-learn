# Sentinel Dashboard Frontend

## 环境要求

- Node.js > 6.x

## 编码规范

- HTML/CSS 遵循 [Bootstrap 编码规范](https://codeguide.bootcss.com/)
- JavaScript 遵循 [Airbnb JavaScript Style Guide](https://github.com/airbnb/javascript/tree/es5-deprecated/es5) 以及最新的 ES 6 标准

## 安装依赖

```
npm i
```

## 开始本地开发

```
npm start
```

## 构建前端资源

```
# 进入package.json所在目录/src/main/webapp/resources
# 新创建的services目录下的js需要添加到gulpfile.js文件中，确保JS_APP中的文件准确
npm run build
```

## Credit

- [sb-admin-angular](https://github.com/start-angular/sb-admin-angular)