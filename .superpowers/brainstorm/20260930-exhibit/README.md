# 非遗展品 2D / 3D 界面预览

用户已确认此版布局：现有米白/棕色风格，左侧展示，右侧展品信息，下方技艺背景。

- `content/exhibit-v1.html`：独立交互草图，不连接业务数据库。
- `content/embroidery.jpg`：复用仓库已有刺绣图片；示例名称和元数据不是经过考证的展品信息。
- 图片展廊支持局部裁切切换和放大查看。
- 3D 页签目前是 **二维透视布局示意**；旋转、缩放、复位、全屏尚未实现。
- 无模型、加载失败是可切换的模拟状态，不是真实模型加载测试。

本地预览使用 brainstorming visual companion server：设定 `BRAINSTORM_DIR` 为本目录、`BRAINSTORM_PORT=18081`、`BRAINSTORM_HOST=127.0.0.1`，运行已安装的 `brainstorming/scripts/server.cjs`，打开 http://127.0.0.1:18081/ 。服务器将 `content` 的最新 HTML 作为首页，图片由 `/files/embroidery.jpg` 提供。

浏览器状态、日志和截图在 `state/` 中，已忽略，不提交个人浏览器数据。业务实现与真实模型接入后续单独进行。