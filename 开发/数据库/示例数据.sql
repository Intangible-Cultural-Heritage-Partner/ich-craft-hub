-- 非遗展品模块 示例/占位数据
-- 用途：在列表页快速看到展示效果（封面图用在线占位图 picsum.photos）
-- 执行：mysql -uroot -p 你的库  <  示例数据.sql   或在 Navicat/Workbench 里跑
-- 清理：见文件末尾的删除语句

USE intangible_heritage_platform;

-- 1) 演示匠人账号（role=1 匠人），已存在则忽略
--    登录账号 demo_craftsman / 密码 123456（明文，与当前项目一致）
INSERT IGNORE INTO `user` (`username`, `password`, `nickname`, `role`, `avatar`, `introduce`)
VALUES ('demo_craftsman', '123456', '示例匠人·老周', 1,
        'https://picsum.photos/seed/craftsman/100/100',
        '从事传统手作二十余年，专注剪纸、木雕与苏绣技艺传承。');

-- 2) 若干已审核通过（audit_status=1）的非遗展品，关联到上面的演示匠人
INSERT INTO `heritage_work`
  (`craftsman_id`, `title`, `category`, `cover_img`, `image_list`, `model_url`, `skill_background`, `description`, `audit_status`, `create_time`)
SELECT u.id, d.title, d.category, d.cover_img, d.image_list, d.model_url, d.skill_background, d.description, 1, NOW()
FROM `user` u
JOIN (
    SELECT '福字剪纸·团圆' AS title, '剪纸' AS category,
           'https://picsum.photos/seed/papercut1/600/400' AS cover_img,
           'https://picsum.photos/seed/papercut1/900/600,https://picsum.photos/seed/papercut2/900/600,https://picsum.photos/seed/papercut3/900/600' AS image_list,
           NULL AS model_url,
           '剪纸是最古老的中国民间艺术之一，以剪刀或刻刀在纸上剪刻花纹。此件为传统团花剪纸，寓意团圆美满。' AS skill_background,
           '大红宣纸手工剪制，直径约30cm，适合节庆窗花装饰。' AS description
    UNION ALL
    SELECT '云纹木雕摆件', '木雕',
           'https://picsum.photos/seed/woodcarve1/600/400',
           'https://picsum.photos/seed/woodcarve1/900/600,https://picsum.photos/seed/woodcarve2/900/600',
           NULL,
           '木雕技艺讲究因材施艺，此件取自香樟木，以浅浮雕手法刻云纹祥瑞图样。',
           '香樟实木雕刻，长约25cm，木质温润带天然香气。'
    UNION ALL
    SELECT '苏绣双面团扇', '苏绣',
           'https://picsum.photos/seed/embroidery1/600/400',
           'https://picsum.photos/seed/embroidery1/900/600,https://picsum.photos/seed/embroidery2/900/600,https://picsum.photos/seed/embroidery3/900/600',
           NULL,
           '苏绣以针法细密、色彩雅致著称。双面绣需正反两面同样精美，为苏绣中的高难技法。',
           '真丝底料手工刺绣，双面异色，绣制荷花锦鲤图。'
    UNION ALL
    SELECT '青花瓷纹样茶盏', '陶瓷',
           'https://picsum.photos/seed/porcelain1/600/400',
           'https://picsum.photos/seed/porcelain1/900/600,https://picsum.photos/seed/porcelain2/900/600',
           NULL,
           '青花瓷以钴料在素坯上绘制后施透明釉高温烧制，发色青翠，是中国瓷器的代表。',
           '手工拉坯手绘青花缠枝纹，容量约120ml。'
) d
WHERE u.username = 'demo_craftsman';

-- ============ 清理示例数据（需要回滚时执行）============
-- DELETE FROM `heritage_work` WHERE craftsman_id = (SELECT id FROM `user` WHERE username='demo_craftsman');
-- DELETE FROM `user` WHERE username = 'demo_craftsman';
