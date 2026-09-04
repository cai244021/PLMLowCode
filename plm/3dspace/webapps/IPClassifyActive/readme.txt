1.该widget为XEN中选择库和书签时弹出模态框
2.修改项为P3371 将选择库和书签设置为单选
3. 2025/07/23 新增选库后点击选择按钮后校验叶子结点中通过后 发布更新属性事件
4. expandInActiveBookmark 展开树结构的入口方法 20260612
3. todo 判断叶子阶段 在 "DS/IPClassifyActive/utils/EventsManager" 有选中后的监听处理逻辑 根据 OOTB的数据对象即可判断除是否是叶子分类库